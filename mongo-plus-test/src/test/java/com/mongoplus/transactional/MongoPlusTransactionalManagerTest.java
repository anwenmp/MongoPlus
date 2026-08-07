package com.mongoplus.transactional;

import com.mongodb.TransactionOptions;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongoplus.context.MongoTransactionContext;
import com.mongoplus.factory.MongoClientFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class MongoPlusTransactionalManagerTest {

    private final List<SessionState> sessions = new ArrayList<>();
    private MongoPlusTransactionalManager transactionManager;

    @Before
    public void setUp() {
        MongoClientFactory.getInstance(createMongoClient());
        transactionManager = new MongoPlusTransactionalManager(TransactionOptions.builder().build());
    }

    @After
    public void tearDown() {
        MongoTransactionContext.clear();
        if (TransactionSynchronizationManager.hasResource(transactionManager)) {
            TransactionSynchronizationManager.unbindResource(transactionManager);
        }
    }

    @Test
    public void requiredNestedTransactionCommitsAndClosesOnlyAtOuterBoundary() {
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        TransactionStatus outer = transactionManager.getTransaction(definition);
        ClientSession outerSession = MongoTransactionContext.getClientSessionContext();
        TransactionStatus inner = transactionManager.getTransaction(definition);

        assertSame(outerSession, MongoTransactionContext.getClientSessionContext());
        transactionManager.commit(inner);
        assertEquals(0, sessions.get(0).commitCount);
        assertEquals(0, sessions.get(0).closeCount);

        transactionManager.commit(outer);
        assertEquals(1, sessions.get(0).commitCount);
        assertEquals(1, sessions.get(0).closeCount);
        assertNull(MongoTransactionContext.getClientSessionContext());
    }

    @Test
    public void participatingRollbackMarksOuterTransactionRollbackOnly() {
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        TransactionStatus outer = transactionManager.getTransaction(definition);
        TransactionStatus inner = transactionManager.getTransaction(definition);

        transactionManager.rollback(inner);
        boolean unexpectedRollback = false;
        try {
            transactionManager.commit(outer);
        } catch (UnexpectedRollbackException expected) {
            unexpectedRollback = true;
        }

        assertTrue(unexpectedRollback);
        assertEquals(1, sessions.get(0).abortCount);
        assertEquals(1, sessions.get(0).closeCount);
    }

    @Test
    public void requiresNewSuspendsAndRestoresOuterSession() {
        TransactionStatus outer = transactionManager.getTransaction(new DefaultTransactionDefinition());
        ClientSession outerSession = MongoTransactionContext.getClientSessionContext();
        DefaultTransactionDefinition requiresNew = new DefaultTransactionDefinition();
        requiresNew.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        TransactionStatus inner = transactionManager.getTransaction(requiresNew);
        ClientSession innerSession = MongoTransactionContext.getClientSessionContext();
        assertNotSame(outerSession, innerSession);

        transactionManager.commit(inner);
        assertSame(outerSession, MongoTransactionContext.getClientSessionContext());
        transactionManager.commit(outer);

        assertEquals(2, sessions.size());
        assertEquals(1, sessions.get(0).closeCount);
        assertEquals(1, sessions.get(1).closeCount);
    }

    private MongoClient createMongoClient() {
        return (MongoClient) Proxy.newProxyInstance(
                MongoClient.class.getClassLoader(),
                new Class<?>[]{MongoClient.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) {
                        if ("startSession".equals(method.getName())) {
                            SessionState state = new SessionState();
                            sessions.add(state);
                            return state.session;
                        }
                        return defaultValue(method.getReturnType());
                    }
                });
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) {
            return null;
        }
        if (type == boolean.class) {
            return false;
        }
        if (type == byte.class) {
            return (byte) 0;
        }
        if (type == short.class) {
            return (short) 0;
        }
        if (type == int.class) {
            return 0;
        }
        if (type == long.class) {
            return 0L;
        }
        if (type == float.class) {
            return 0F;
        }
        if (type == double.class) {
            return 0D;
        }
        if (type == char.class) {
            return '\0';
        }
        return null;
    }

    private static final class SessionState implements InvocationHandler {
        private final ClientSession session;
        private boolean active;
        private int commitCount;
        private int abortCount;
        private int closeCount;

        private SessionState() {
            session = (ClientSession) Proxy.newProxyInstance(
                    ClientSession.class.getClassLoader(), new Class<?>[]{ClientSession.class}, this);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            String name = method.getName();
            if ("startTransaction".equals(name)) {
                active = true;
                return null;
            }
            if ("hasActiveTransaction".equals(name)) {
                return active;
            }
            if ("commitTransaction".equals(name)) {
                commitCount++;
                active = false;
                return null;
            }
            if ("abortTransaction".equals(name)) {
                abortCount++;
                active = false;
                return null;
            }
            if ("close".equals(name)) {
                closeCount++;
                active = false;
                return null;
            }
            if ("getTransactionOptions".equals(name)) {
                return TransactionOptions.builder().build();
            }
            return defaultValue(method.getReturnType());
        }
    }
}
