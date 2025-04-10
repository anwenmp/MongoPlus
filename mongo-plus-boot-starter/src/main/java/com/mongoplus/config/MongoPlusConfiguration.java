package com.mongoplus.config;

import com.mongodb.TransactionOptions;
import com.mongodb.client.MongoClient;
import com.mongoplus.annotation.SpelAnnotationHandler;
import com.mongoplus.cache.codec.MongoPlusCodecCache;
import com.mongoplus.cache.global.DataSourceNameCache;
import com.mongoplus.cache.global.MongoPlusClientCache;
import com.mongoplus.cache.global.SimpleCache;
import com.mongoplus.codecs.MongoPlusCodec;
import com.mongoplus.conn.CollectionManager;
import com.mongoplus.constant.DataSourceConstant;
import com.mongoplus.datasource.MongoDataSourceAspect;
import com.mongoplus.factory.MongoClientFactory;
import com.mongoplus.handlers.collection.AnnotationOperate;
import com.mongoplus.logic.MongoLogicIgnoreAspect;
import com.mongoplus.manager.DataSourceManager;
import com.mongoplus.manager.MongoPlusClient;
import com.mongoplus.mapper.BaseMapper;
import com.mongoplus.mapper.DefaultBaseMapperImpl;
import com.mongoplus.mapping.MappingMongoConverter;
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.mapping.SimpleTypeHolder;
import com.mongoplus.property.MongoDBCollectionProperty;
import com.mongoplus.property.MongoDBConfigurationProperty;
import com.mongoplus.property.MongoDBConnectProperty;
import com.mongoplus.property.MongoDBLogProperty;
import com.mongoplus.tenant.TenantAspect;
import com.mongoplus.toolkit.CollUtil;
import com.mongoplus.transactional.MongoTransactionalAspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

import static com.mongoplus.toolkit.MongoUtil.getMongo;

/**
 * @author JiaChaoYang
 * 连接配置
 * @since 2023-02-09 14:27
 **/
@EnableConfigurationProperties(value = {
        MongoDBConnectProperty.class,
        MongoDBCollectionProperty.class,
        MongoDBConfigurationProperty.class,
        MongoDBLogProperty.class
})
public class MongoPlusConfiguration {

    private final MongoDBConnectProperty mongoDBConnectProperty;

    private final MongoDBConfigurationProperty mongoDBConfigurationProperty;
    protected final MongoDBLogProperty mongoDBLogProperty;


    public MongoPlusConfiguration(MongoDBConnectProperty mongodbConnectProperty,
                                  MongoDBConfigurationProperty mongodbConfigurationProperty,
                                  MongoDBLogProperty mongoDBLogProperty) {
        this.mongoDBConnectProperty = mongodbConnectProperty;
        this.mongoDBConfigurationProperty = mongodbConfigurationProperty;
        this.mongoDBLogProperty = mongoDBLogProperty;
    }

    /**
     * 注册MongoClient工厂
     * @return {@link MongoClientFactory}
     * @author anwen
     */
    @Bean
    @ConditionalOnMissingBean
    public MongoClientFactory mongoClientFactory(ApplicationContext applicationContext) {
        // 编解码器
        applicationContext.getBeansOfType(MongoPlusCodec.class).values().forEach(MongoPlusCodecCache::addCodec);
        MongoClientFactory mongoClientFactory = MongoClientFactory.getInstance(
                getMongo(DataSourceConstant.DEFAULT_DATASOURCE,mongoDBConnectProperty)
        );
        if (CollUtil.isNotEmpty(mongoDBConnectProperty.getSlaveDataSource())){
            mongoDBConnectProperty.getSlaveDataSource()
                    .forEach(slaveDataSource -> mongoClientFactory.addMongoClient(
                            slaveDataSource.getSlaveName(),
                            getMongo(slaveDataSource.getSlaveName(), slaveDataSource)
                    ));
        }
        return mongoClientFactory;
    }

    /**
     * 这里将MongoClient注册为Bean，但是只是给MongoTemplate使用，master的client
     * @author JiaChaoYang
     */
    @Bean
    @ConditionalOnMissingBean
    public MongoClient mongo(MongoClientFactory mongoClientFactory){
        return mongoClientFactory.getMongoClient();
    }

    /**
     * MongoPlusClient注册
     * @param mongo MongoClient
     * @param mongoClientFactory MongoClient工厂
     * @return {@link com.mongoplus.manager.MongoPlusClient}
     * @author anwen
     */
    @Bean
    @ConditionalOnMissingBean(MongoPlusClient.class)
    public MongoPlusClient mongoPlusClient(MongoClient mongo,MongoClientFactory mongoClientFactory){
        MongoPlusClient mongoPlusClient = Configuration.builder().initMongoPlusClient(mongo,mongoDBConnectProperty);
        mongoClientFactory.getMongoClientMap().forEach((ds,mongoClient) -> mongoPlusClient.getCollectionManagerMap()
                .put(ds,new LinkedHashMap<String, CollectionManager>(){{
            String database = DataSourceNameCache.getBaseProperty(ds).getDatabase();
            Arrays.stream(database.split(",")).collect(Collectors.toList()).forEach(db ->
                    put(db,new CollectionManager(db)));
        }}));
        MongoPlusClientCache.mongoPlusClient = mongoPlusClient;
        if (mongoDBConfigurationProperty.getBanner()){
            // 参考 Easy-ES
            if (mongoDBConfigurationProperty.getIkun()){
                System.out.println("                 鸡你太美\n" +
                        "               鸡你实在太美\n" +
                        "                鸡你是太美\n" +
                        "                 鸡你太美\n" +
                        "              实在是太美鸡你\n" +
                        "         鸡你 实在是太美鸡你 美\n" +
                        "       鸡你  实在是太美鸡美   太美\n" +
                        "      鸡你  实在是太美鸡美      太美\n" +
                        "    鸡你    实在是太美鸡美       太美\n" +
                        "   鸡你    鸡你实在是美太美    美蓝球球球\n" +
                        "鸡 鸡     鸡你实在是太美     篮球篮球球球球\n" +
                        " 鸡      鸡你太美裆鸡太啊     球球蓝篮球球\n" +
                        "         鸡你太美裆裆鸡美       球球球\n" +
                        "          鸡你裆小 j 鸡太美\n" +
                        "           鸡太美    鸡太美\n" +
                        "            鸡美      鸡美\n" +
                        "            鸡美       鸡美\n" +
                        "             鸡美       鸡美\n" +
                        "             鸡太       鸡太\n" +
                        "           鸡 脚       鸡 脚\n" +
                        "           皮 鞋       皮 鞋\n" +
                        "       金光 大道         金光 大道\n" +
                        "      鸡神保佑       永不宕机     永无BUG");
            }else {
                System.out.println("___  ___                       ______ _           \n" +
                        "|  \\/  |                       | ___ \\ |          \n" +
                        "| .  . | ___  _ __   __ _  ___ | |_/ / |_   _ ___ \n" +
                        "| |\\/| |/ _ \\| '_ \\ / _` |/ _ \\|  __/| | | | / __|\n" +
                        "| |  | | (_) | | | | (_| | (_) | |   | | |_| \\__ \\\n" +
                        "\\_|  |_/\\___/|_| |_|\\__, |\\___/\\_|   |_|\\__,_|___/\n" +
                        "                     __/ |                        \n" +
                        "                    |___/                         ");
            }
            System.out.println(":: MongoPlus ::                        (v" + MongoPlusClient.getVersion()+")");
        }
        return mongoPlusClient;
    }

    /**
     * 简单类型注册
     * @return {@link SimpleTypeHolder}
     * @author anwen
     */
    @Bean
    @ConditionalOnMissingBean(SimpleTypeHolder.class)
    public SimpleTypeHolder simpleTypeHolder(){
        SimpleTypeHolder simpleTypeHolder = new SimpleTypeHolder();
        SimpleCache.setSimpleTypeHolder(simpleTypeHolder);
        return simpleTypeHolder;
    }

    /**
     * 注册mongo转换器
     * @return {@link com.mongoplus.mapping.MongoConverter}
     * @author anwen
     */
    @Bean
    @ConditionalOnMissingBean(MongoConverter.class)
    public MongoConverter mongoConverter(){
        return new MappingMongoConverter();
    }

    /**
     * baseMapper注册
     * @param mongoPlusClient mongoPlusClient
     * @param mongoConverter 转换器
     * @return {@link com.mongoplus.mapper.BaseMapper}
     * @author anwen
     */
    @Bean
    @ConditionalOnMissingBean(BaseMapper.class)
    public BaseMapper mongoBaseMapper(MongoPlusClient mongoPlusClient, MongoConverter mongoConverter){
        return new DefaultBaseMapperImpl(mongoPlusClient,mongoConverter);
    }

    /**
     * 注册MongoPlus事务切面
     * @return {@link MongoTransactionalAspect}
     * @author anwen
     */
    @Bean("mongoTransactionalAspect")
    @ConditionalOnMissingBean
    public MongoTransactionalAspect mongoTransactionalAspect() {
        return new MongoTransactionalAspect();
    }

    @Bean("transactionOptions")
    @ConditionalOnMissingBean
    public TransactionOptions transactionOptions(){
        return TransactionOptions.builder().build();
    }

    /**
     * 注册mongoPlus多数据源切面
     * @return {@link MongoDataSourceAspect}
     * @author anwen
     */
    @Bean("mongoDataSourceAspect")
    @ConditionalOnMissingBean
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
    @ConditionalOnMissingBean
    public MongoLogicIgnoreAspect mongoLogicIgnoreAspect() {
        return new MongoLogicIgnoreAspect();
    }

    /**
     * 忽略租户
     * @author anwen
     */
    @Bean("tenantAspect")
    @ConditionalOnMissingBean
    public TenantAspect tenantAspect(){
        return new TenantAspect();
    }

    /**
     * 数据源管理器
     * @param mongoPlusClient mongoPlus客户端
     * @param mongoClientFactory mongoClient工厂
     * @return {@link DataSourceManager}
     * @author anwen
     */
    @Bean("dataSourceManager")
    @ConditionalOnMissingBean
    public DataSourceManager dataSourceManager(MongoPlusClient mongoPlusClient,
                                               MongoClientFactory mongoClientFactory){
        return new DataSourceManager(mongoPlusClient,mongoClientFactory);
    }

    /**
     * spel朱姐处理
     * @return {@link com.mongoplus.annotation.SpelAnnotationHandler}
     * @author anwen
     */
    @Bean
    @ConditionalOnMissingBean
    public SpelAnnotationHandler spelAnnotationHandler(ApplicationContext applicationContext){
        SpelAnnotationHandler annotationHandler = new SpelAnnotationHandler(applicationContext);
        AnnotationOperate.setAnnotationHandler(annotationHandler);
        return annotationHandler;
    }

}
