package com.mongoplus.registry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertSame;

public class MongoEntityMappingRegistryDataSourceIsolationTest {

    private final MongoEntityMappingRegistry registry = MongoEntityMappingRegistry.getInstance();

    @Before
    @After
    public void clearRegistry() {
        registry.clearMappingRelations();
    }

    @Test
    public void sameNamespaceOnDifferentDataSourcesShouldResolveTheirOwnEntity() {
        String sharedNamespace = "shared_database.shared_collection";

        registry.setMappingRelation(sharedNamespace, FirstDataSourceEntity.class);
        registry.setMappingRelation(sharedNamespace, SecondDataSourceEntity.class);

        assertSame("The second datasource must resolve its own entity metadata",
                SecondDataSourceEntity.class,
                registry.getMappingResource(sharedNamespace));
    }

    private static class FirstDataSourceEntity {
    }

    private static class SecondDataSourceEntity {
    }
}
