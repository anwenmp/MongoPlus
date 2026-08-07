package com.mongoplus.interceptor.business;

import com.mongodb.MongoNamespace;
import com.mongodb.client.MongoCollection;
import com.mongoplus.cache.global.DataSourceNameCache;
import com.mongoplus.enums.ExecuteMethodEnum;
import com.mongoplus.mapper.BaseMapper;
import com.mongoplus.model.OperationResult;
import org.bson.Document;
import org.junit.After;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * 固定 Recorder 保存审计记录时不得污染业务数据源，并且保存失败后必须清理线程上下文。
 */
public class DataChangeRecorderInnerInterceptorTest {

    @After
    public void cleanThreadState() throws Exception {
        DataSourceNameCache.clear();
        operationResultThreadLocal().remove();
    }

    /** 正常保存审计记录后恢复进入 Recorder 前的业务数据源。 */
    @Test
    public void savingAuditRecordShouldRestoreBusinessDatasource() {
        List<OperationResult> saved = new ArrayList<>();
        DataChangeRecorderInnerInterceptor interceptor = interceptor(saved, false);
        DataSourceNameCache.setDataSource("business-datasource");

        MongoCollection<Document> collection = collection("business", "orders");
        execute(interceptor, collection, 1);

        assertEquals("business-datasource", DataSourceNameCache.getDataSource());
        assertEquals("business-datasource", saved.get(0).getDatasourceName());
    }

    /** 保存审计记录抛出异常后仍恢复业务数据源，并保持原异常向外传播。 */
    @Test
    public void failedAuditSaveShouldRestoreBusinessDatasourceAndPropagateException() {
        DataChangeRecorderInnerInterceptor interceptor = interceptor(new ArrayList<>(), true);
        DataSourceNameCache.setDataSource("business-datasource");

        try {
            execute(interceptor, collection("business", "orders"), 1);
            fail("audit save exception should propagate");
        } catch (IllegalStateException expected) {
            assertEquals("audit save failed", expected.getMessage());
        }

        assertEquals("business-datasource", DataSourceNameCache.getDataSource());
    }

    /** 进入 Recorder 前未设置数据源时，正常完成后仍保持未设置状态。 */
    @Test
    public void savingAuditRecordShouldClearDatasourceWhenPreviouslyUnset() {
        DataChangeRecorderInnerInterceptor interceptor = interceptor(new ArrayList<>(), false);
        DataSourceNameCache.clear();

        execute(interceptor, collection("business", "orders"), 1);

        assertNull(DataSourceNameCache.getDataSourceOrNull());
    }

    /** 正常保存审计记录后清理本次 OperationResult。 */
    @Test
    public void successfulAuditSaveShouldClearOperationResultThreadLocal() throws Exception {
        DataChangeRecorderInnerInterceptor interceptor = interceptor(new ArrayList<>(), false);

        execute(interceptor, collection("business", "orders"), 1);

        assertNull(operationResultThreadLocal().get());
    }

    /** 保存审计记录失败后也清理本次 OperationResult。 */
    @Test
    public void failedAuditSaveShouldClearOperationResultThreadLocal() throws Exception {
        DataChangeRecorderInnerInterceptor interceptor = interceptor(new ArrayList<>(), true);
        MongoCollection<Document> collection = collection("business", "orders");

        try {
            execute(interceptor, collection, 1);
        } catch (IllegalStateException expected) {
            // 模拟审计库写入失败；业务写入此时已经正常返回。
        }

        assertNull(operationResultThreadLocal().get());
    }

    /** 同一线程连续调用时，每次保存各自的记录且不继承上一次 datasource。 */
    @Test
    public void consecutiveCallsShouldNotReusePreviousOperationOrAuditDatasource() {
        List<OperationResult> saved = new ArrayList<>();
        DataChangeRecorderInnerInterceptor interceptor = interceptor(saved, false);
        MongoCollection<Document> collection = collection("business", "orders");

        DataSourceNameCache.setDataSource("business-one");
        execute(interceptor, collection, 1);
        assertEquals("business-one", DataSourceNameCache.getDataSource());

        DataSourceNameCache.setDataSource("business-two");
        execute(interceptor, collection, 2);

        assertEquals(2, saved.size());
        assertNotSame(saved.get(0), saved.get(1));
        assertEquals("business-one", saved.get(0).getDatasourceName());
        assertEquals("business-two", saved.get(1).getDatasourceName());
        assertEquals("business-two", DataSourceNameCache.getDataSource());
    }

    private DataChangeRecorderInnerInterceptor interceptor(List<OperationResult> saved, boolean fail) {
        DataChangeRecorderInnerInterceptor interceptor = new DataChangeRecorderInnerInterceptor();
        interceptor.setDatasourceName("audit-datasource");
        interceptor.setDatabaseName("audit");
        interceptor.enableSaveDatabase(baseMapper(saved, fail));
        return interceptor;
    }

    private void execute(DataChangeRecorderInnerInterceptor interceptor,
                         MongoCollection<Document> collection, int id) {
        interceptor.beforeExecute(ExecuteMethodEnum.SAVE_ONE,
                new Object[]{new Document("_id", id)}, collection);
        interceptor.afterExecute(ExecuteMethodEnum.SAVE_ONE, new Object[0], true, collection);
    }

    private BaseMapper baseMapper(List<OperationResult> saved, boolean fail) {
        return (BaseMapper) Proxy.newProxyInstance(
                BaseMapper.class.getClassLoader(), new Class<?>[]{BaseMapper.class}, (proxy, method, args) -> {
                    if ("save".equals(method.getName()) && args.length == 3) {
                        if (fail) {
                            throw new IllegalStateException("audit save failed");
                        }
                        saved.add((OperationResult) args[2]);
                        return true;
                    }
                    throw new AssertionError("Unexpected BaseMapper method: " + method);
                });
    }

    @SuppressWarnings("unchecked")
    private MongoCollection<Document> collection(String database, String collection) {
        return (MongoCollection<Document>) Proxy.newProxyInstance(
                MongoCollection.class.getClassLoader(), new Class<?>[]{MongoCollection.class}, (proxy, method, args) -> {
                    if ("getNamespace".equals(method.getName())) {
                        return new MongoNamespace(database, collection);
                    }
                    throw new AssertionError("Unexpected collection method: " + method.getName());
                });
    }

    @SuppressWarnings("unchecked")
    private ThreadLocal<OperationResult> operationResultThreadLocal() throws Exception {
        Field field = DataChangeRecorderInnerInterceptor.class.getDeclaredField("operationResultThreadLocal");
        field.setAccessible(true);
        return (ThreadLocal<OperationResult>) field.get(null);
    }
}
