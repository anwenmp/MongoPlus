package com.mongoplus.config;

import com.mongodb.client.MongoClient;
import com.mongoplus.cache.codec.MongoPlusCodecCache;
import com.mongoplus.cache.global.DataSourceNameCache;
import com.mongoplus.cache.global.MongoPlusClientCache;
import com.mongoplus.cache.global.SimpleCache;
import com.mongoplus.codecs.MongoPlusCodec;
import com.mongoplus.conn.CollectionManager;
import com.mongoplus.constant.DataSourceConstant;
import com.mongoplus.datasource.MongoDataSourceAspect;
import com.mongoplus.enums.BannerType;
import com.mongoplus.factory.MongoClientFactory;
import com.mongoplus.logic.MongoLogicIgnoreAspect;
import com.mongoplus.manager.DataSourceManager;
import com.mongoplus.manager.MongoPlusClient;
import com.mongoplus.mapper.BaseMapper;
import com.mongoplus.mapper.DefaultBaseMapperImpl;
import com.mongoplus.mapping.MappingMongoConverter;
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.mapping.SimpleTypeHolder;
import com.mongoplus.meta.MongoPlusBanner;
import com.mongoplus.property.*;
import com.mongoplus.tenant.TenantAspect;
import com.mongoplus.toolkit.CollUtil;
import com.mongoplus.transactional.MongoTransactionalAspect;
import org.noear.solon.Solon;
import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Condition;
import org.noear.solon.annotation.Configuration;
import org.noear.solon.annotation.Inject;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.mongoplus.toolkit.MongoUtil.getMongo;

/**
 * @author JiaChaoYang
 * 连接配置
 * @since 2023-02-09 14:27
 **/
@Configuration
public class MongoPlusConfiguration {

    @Inject("${mongo-plus.data.mongodb}")
    private MongoDBConnectProperty mongoDBConnectProperty;

    @Inject(value = "${mongo-plus.configuration.collection}",required = false)
    private MongoDBCollectionProperty mongoDBCollectionProperty;

    @Inject(value = "${mongo-plus.configuration}",required = false)
    private MongoDBConfigurationProperty mongoDBConfigurationProperty;

    @Inject("${mongo-plus}")
    private MongoDBLogProperty mongoDBLogProperty;

    /**
     * 将MongoClient注册为Bean
     * @author JiaChaoYang
     */
    @Bean
    @Condition(onMissingBean = MongoClient.class)
    public MongoClient mongo(MongoClientFactory mongoClientFactory){
        return mongoClientFactory.getMongoClient();
    }

    @Bean
    @Condition(onMissingBean = MongoClientFactory.class)
    public MongoClientFactory mongoClientFactory(){
        // 设置编解码器
        Solon.context().getBeansOfType(MongoPlusCodec.class).forEach(MongoPlusCodecCache::addCodec);
        MongoClientFactory mongoClientFactory = MongoClientFactory
                .getInstance(getMongo(DataSourceConstant.DEFAULT_DATASOURCE,mongoDBConnectProperty));
        if (CollUtil.isNotEmpty(mongoDBConnectProperty.getSlaveDataSource())){
            mongoDBConnectProperty.getSlaveDataSource()
                    .forEach(slaveDataSource ->
                            mongoClientFactory.addMongoClient(
                                    slaveDataSource.getSlaveName(),
                                    getMongo(slaveDataSource.getSlaveName(),
                                            slaveDataSource)
                            ));
        }
        return mongoClientFactory;
    }


    @Bean
    @Condition(onMissingBean = MongoPlusClient.class)
    public MongoPlusClient mongoPlusClient(MongoClient mongo,MongoClientFactory mongoClientFactory){
        mongoDBConfigurationProperty = Optional.ofNullable(mongoDBConfigurationProperty).orElseGet(MongoDBConfigurationProperty::new);
        MongoPlusClient mongoPlusClient = com.mongoplus.config.Configuration.builder().initMongoPlusClient(mongo,mongoDBConnectProperty);
        mongoClientFactory.getMongoClientMap().forEach((ds,mongoClient) -> mongoPlusClient.getCollectionManagers().put(ds,new LinkedHashMap<String, CollectionManager>(){{
            String database = DataSourceNameCache.getBaseProperty(ds).getDatabase();
            Arrays.stream(database.split(",")).collect(Collectors.toList()).forEach(db -> put(db,new CollectionManager(db)));
        }}));
        MongoPlusClientCache.mongoPlusClient = mongoPlusClient;
        MongoPlusBanner.printBanner(
                mongoDBConfigurationProperty.getBanner(),
                mongoDBConfigurationProperty.getIkun() ? BannerType.IKUN : BannerType.DEFAULT
        );
        return mongoPlusClient;
    }

    @Bean
    @Condition(onMissingBean = SimpleTypeHolder.class)
    public SimpleTypeHolder simpleTypeHolder() {
        SimpleTypeHolder simpleTypeHolder = new SimpleTypeHolder();
        SimpleCache.setSimpleTypeHolder(simpleTypeHolder);
        return simpleTypeHolder;
    }

    @Bean
    @Condition(onMissingBean = MongoConverter.class)
    public MongoConverter mongoConverter() {
        return new MappingMongoConverter();
    }

    @Bean("mongoTransactionalAspect")
    @Condition(onMissingBean = MongoTransactionalAspect.class)
    public MongoTransactionalAspect mongoTransactionalAspect() {
        return new MongoTransactionalAspect();
    }

    @Bean
    public MongoPlusAutoConfiguration mongoPlusAutoConfiguration(@Inject BaseMapper baseMapper,
                                                                 @Inject(
                                                                         value = "${mongo-plus.configuration.logic}",
                                                                         required = false)
                                                                 MongoLogicDelProperty mongoLogicDelProperty,
                                                                 MongoPlusClient mongoPlusClient){
        return new MongoPlusAutoConfiguration(
                baseMapper,
                mongoDBLogProperty,
                mongoDBCollectionProperty,
                mongoLogicDelProperty,
                mongoPlusClient,
                mongoDBConfigurationProperty
        );
    }

    @Bean
    @Condition(onMissingBean = BaseMapper.class)
    public BaseMapper baseMapper(MongoPlusClient mongoPlusClient,MongoConverter mongoConverter){
        return new DefaultBaseMapperImpl(mongoPlusClient,mongoConverter);
    }

    /**
     * 数据源管理器
     * @param mongoPlusClient mongoPlus客户端
     * @param mongoClientFactory mongoClient工厂
     * @return {@link DataSourceManager}
     * @author anwen
     */
    @Bean
    @Condition(onMissingBean = DataSourceManager.class)
    public DataSourceManager dataSourceManager(MongoPlusClient mongoPlusClient,
                                               MongoClientFactory mongoClientFactory){
        return new DataSourceManager(mongoPlusClient,mongoClientFactory);
    }

    /**
     * 注册mongoPlus多数据源切面
     * @return {@link MongoDataSourceAspect}
     * @author anwen
     */
    @Bean("mongoDataSourceAspect")
    @Condition(onMissingBean = MongoDataSourceAspect.class)
    public MongoDataSourceAspect mongoDataSourceAspect() {
        return new MongoDataSourceAspect();
    }

    /**
     * 忽略逻辑删除
     *
     * @return {@link MongoLogicIgnoreAspect}
     * @author loser
     */
    @Bean("mongoLogicIgnoreAspect")
    @Condition(onMissingBean = MongoLogicIgnoreAspect.class)
    public MongoLogicIgnoreAspect mongoLogicIgnoreAspect() {
        return new MongoLogicIgnoreAspect();
    }

    /**
     * 忽略租户
     * @author anwen
     */
    @Bean("tenantAspect")
    @Condition(onMissingBean = TenantAspect.class)
    public TenantAspect tenantAspect(){
        return new TenantAspect();
    }

}
