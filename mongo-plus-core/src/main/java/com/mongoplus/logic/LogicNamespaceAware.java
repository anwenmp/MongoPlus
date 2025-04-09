package com.mongoplus.logic;

import com.mongoplus.aware.impl.NamespaceAware;
import com.mongoplus.registry.MongoEntityMappingRegistry;

/**
 * 逻辑删除链接命名空间感知类
 *
 * @author loser
 */
public class LogicNamespaceAware implements NamespaceAware {

    @Override
    public void nameSpaceAware(Namespace namespace) {
        String fullName = namespace.getDataBase() + "." + namespace.getCollectionName();
        MongoEntityMappingRegistry.getInstance().setMappingRelation(fullName, namespace.getEntityClass());
    }

}
