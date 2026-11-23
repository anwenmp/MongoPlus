package com.mongoplus.transactional;

import com.mongoplus.annotation.transactional.MongoTransactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

final class MongoTransactionDefinition extends DefaultTransactionDefinition {

    private final MongoTransactional mongoTransactional;

    MongoTransactionDefinition(MongoTransactional mongoTransactional) {
        this.mongoTransactional = mongoTransactional;
        setPropagationBehavior(PROPAGATION_REQUIRED);
    }

    MongoTransactional getMongoTransactional() {
        return mongoTransactional;
    }
}
