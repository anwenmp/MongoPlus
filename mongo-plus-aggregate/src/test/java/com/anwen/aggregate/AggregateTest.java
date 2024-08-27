package com.anwen.aggregate;

import com.alibaba.fastjson.JSON;
import com.anwen.mongo.aggregate.Aggregate;
import com.anwen.mongo.aggregate.AggregateWrapper;
import com.anwen.mongo.aggregate.pipeline.Accumulators;
import com.anwen.mongo.aggregate.pipeline.Facet;
import com.anwen.mongo.aggregate.pipeline.UnwindOption;
import com.anwen.mongo.bson.MongoPlusDocument;
import com.anwen.mongo.conditions.interfaces.ConditionOperators;
import com.anwen.mongo.conditions.interfaces.Projection;
import com.anwen.mongo.conditions.query.QueryWrapper;
import com.anwen.mongo.config.Configuration;
import com.anwen.mongo.manager.MongoPlusClient;
import com.anwen.mongo.mapper.BaseMapper;
import com.anwen.mongo.mapping.TypeReference;
import com.anwen.mongo.model.BaseModelID;
import com.anwen.mongo.model.BaseProperty;
import com.anwen.mongo.toolkit.FunctionUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoNamespace;
import com.mongodb.client.model.BucketOptions;
import com.mongodb.client.model.GraphLookupOptions;
import com.mongodb.client.model.MergeOptions;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 管道测试
 *
 * @author anwen
 */
@Log4j2
public class AggregateTest {

    private final BaseMapper baseMapper;

    private final MongoPlusClient mongoPlusClient;

    public AggregateTest() {
        BaseProperty baseProperty = new BaseProperty();
        baseProperty.setHost("127.0.0.1");
        baseProperty.setPort("27017");
        baseProperty.setDatabase("mongo-demo");
        Configuration configuration = Configuration.builder().connection(baseProperty).log(true);
        this.baseMapper = configuration.getBaseMapper();
        this.mongoPlusClient = configuration.getMongoPlusClient();
    }

    /**
     * $addFields测试
     *
     * @author anwen
     */
    @Test
    public void addFields() {
        try {
            boolean addFields = baseMapper.saveBatch("addFields", getDocumentArray("addFields.json"));
            log.info("addFields:{}", addFields);
            //以下聚合操作fuel_type向嵌入文档和一级文档添加一个新字段specs
            AggregateWrapper aggregateWrapper = new AggregateWrapper();
            aggregateWrapper.addFields("specs.fuel_type", "unleaded");
            List<Map<String, Object>> aggregatedList = execute("addFields",aggregateWrapper);
            boolean exist = JSON.toJSONString(aggregatedList).contains("fuel_type");
            Assertions.assertTrue(exist);
        } finally {
            removeCollection("addFields");
        }
    }

    /**
     * $set测试
     *
     * @author anwen
     */
    @Test
    public void set() {
        try {
            boolean set = baseMapper.saveBatch("set", getDocumentArray("set.json"));
            log.info("set:{}", set);
            //以下聚合操作fuel_type向嵌入文档和一级文档添加一个新字段specs
            AggregateWrapper aggregateWrapper = new AggregateWrapper();
            aggregateWrapper.set("specs.fuel_type", "unleaded");
            List<Map<String, Object>> aggregatedList = execute("set",aggregateWrapper);
            boolean exist = JSON.toJSONString(aggregatedList).contains("fuel_type");
            Assertions.assertTrue(exist);
            aggregatedList.forEach(System.out::println);
        } finally {
            removeCollection("set");
        }
    }

    @Test
    public void bucket() {
        try {
            Boolean bucket = baseMapper.saveBatch("bucket", getDocumentArray("bucket.json"));
            log.info("bucket:{}", bucket);
            //以下操作根据字段将文档分组到存储桶中，year_born并根据存储桶中的文档数量进行过滤
            AggregateWrapper aggregateWrapper = new AggregateWrapper();
            BucketOptions bucketOptions = new BucketOptions();
            bucketOptions.defaultBucket("Other");
            bucketOptions.output(
                    Accumulators.sum("count", 1),
                    Accumulators.push("artists",
                            new BasicDBObject("$concat", Arrays.asList("$first_name", " ", "$last_name")))
            );
            aggregateWrapper.bucket("$your_born", Arrays.asList(1840, 1850, 1860, 1870, 1880), bucketOptions);
            List<Map<String, Object>> aggregatedList = execute("bucket",aggregateWrapper);
            aggregatedList.forEach(System.out::println);
        } finally {
            removeCollection("bucket");
        }

    }

    @Test
    public void bucketAuto() {
        try {
            Boolean bucketAuto = baseMapper.saveBatch("bucketAuto", getDocumentArray("bucketAuto.json"));
            log.info("bucketAuto:{}", bucketAuto);
            //在以下操作中，根据 price 字段中的值将输入文档分为四个存储桶
            AggregateWrapper aggregateWrapper = new AggregateWrapper();
            aggregateWrapper.bucketAuto("$price", 4);
            List<Map<String, Object>> aggregateList = execute("bucketAuto",aggregateWrapper);
            Assertions.assertEquals(4, aggregateList.size());
        } finally {
            removeCollection("bucketAuto");
        }
    }

    @Test
    public void count() {
        try {
            Boolean count = baseMapper.saveBatch("count", getDocumentArray("count.json"));
            log.info("count:{}", count);
            //以下聚合操作有两个阶段
            //$match 阶段会排除 score 值小于或等于 80 的文档，以便将 score 大于 80 的文档传递到下一个阶段。
            //$count 阶段会返回聚合管道中剩余文档的计数，并将该值分配给名为 passing_scores 的字段。
            AggregateWrapper aggregateWrapper = new AggregateWrapper();
            aggregateWrapper.match(new QueryWrapper<>().gt("score", 80));
            aggregateWrapper.count("passing_scores");
            List<Map<String, Object>> aggregateList = execute("count",aggregateWrapper);
            Assertions.assertEquals(1, aggregateList.size());
            Assertions.assertEquals(4, aggregateList.get(0).get("passing_scores"));
        } finally {
            removeCollection("count");
        }
    }

    @Test
    public void match() {
        try {
            Boolean match = baseMapper.saveBatch("match", getDocumentArray("match.json"));
            log.info("match:{}", match);
            //相等匹配
            //以下操作使用 $match 来执行简易等值匹配
            AggregateWrapper aggregateWrapper = new AggregateWrapper();
            aggregateWrapper.match(new QueryWrapper<>().eq("author", "dave"));
            List<Map<String, Object>> aggregateList = execute("match", aggregateWrapper);
            Assertions.assertEquals(2, aggregateList.size());
            //执行计数
            //以下示例使用 $match 管道操作符选择要处理的文档，然后将结果导入到 $group 管道操作符，以计算文档的数量
            AggregateWrapper aggregateWrapperCount = new AggregateWrapper();
            aggregateWrapperCount.match(
                    new QueryWrapper<>()
                            .or(orWrapper ->
                                    orWrapper.between("score", 70, 90, true)
                                            .gte("views", 1000)
                            )
            );
            aggregateWrapperCount.group(null, Accumulators.sum("count", 1));
            List<Map<String, Object>> aggregateCountList = execute("match", aggregateWrapperCount);
            Assertions.assertEquals(2, aggregateList.size());
            Assertions.assertEquals(5, aggregateCountList.get(0).get("count"));
        } finally {
            removeCollection("match");
        }
    }

    @Test
    public void project() {
        try {
            Boolean project = baseMapper.saveBatch("project", getDocumentArray("project.json"));
            log.info("project:{}", project);
            //抑制输出文档中的 _id 字段
            //以下 $project 阶段在其输出文档中排除 _id 字段，但包括 title 和 author 字段
            AggregateWrapper aggregateWrapper = new AggregateWrapper();
            aggregateWrapper.projectDisplay(false, "title", "author");
            List<Map<String, Object>> aggregateList = execute("project", aggregateWrapper);
            Assertions.assertFalse(aggregateList.get(0).containsKey("_id"));
            //有条件地排除字段
            //可以在聚合表达式中使用变量 REMOVE 来有条件地隐藏字段
            //只有当 $project 字段等于 REMOVE 时，以下 author.middle 阶段才使用 "" 变量将该字段排除
            AggregateWrapper removeAggregateWrapper = new AggregateWrapper();
            Bson cond = ConditionOperators.cond("eq",
                    Arrays.asList("", "$author.middle"), "$$REMOVE", "$author.middle");
            removeAggregateWrapper.project(Projection.builder()
                    .display("title", "author.first", "author.last", "author.middle")
                    .projection("author.middle", cond).buildList());
            List<Document> aggregateRemoveList = execute("project", removeAggregateWrapper, Document.class);
            Assertions.assertEquals(3, aggregateRemoveList.size());
            Assertions.assertTrue(aggregateRemoveList.get(2).get("author", Document.class).containsKey("middle"));
        } finally {
            removeCollection("project");
        }
    }

    @Test
    public void sort() {
        try {
            Boolean sort = baseMapper.saveBatch("sort", getDocumentArray("sort.json"));
            log.info("sort:{}", sort);
            //以下命令使用 $sort 阶段对 borough 字段进行排序
            AggregateWrapper ascAggregateWrapper = new AggregateWrapper();
            ascAggregateWrapper.sortAsc("borough");
            List<Document> ascAggregateList = execute("sort", ascAggregateWrapper, Document.class);
            Assertions.assertEquals(3, ascAggregateList.get(0).getInteger("_id"));

            AggregateWrapper descAggregateWrapper = new AggregateWrapper();
            descAggregateWrapper.sortDesc("borough");
            List<Document> descAggregateList = execute("sort", descAggregateWrapper, Document.class);
            Assertions.assertEquals(2, descAggregateList.get(0).getInteger("_id"));
        } finally {
            removeCollection("sort");
        }
    }

    @Test
    public void sortByCount() {
        try {
            Boolean sortByCount = baseMapper.saveBatch("sortByCount", getDocumentArray("sortByCount.json"));
            log.info("sortByCount:{}", sortByCount);
            //以下操作unwinds tags数组，并使用$sortByCount阶段来计算与每个标签关联的文档数量
            AggregateWrapper aggregateWrapper = new AggregateWrapper();
            aggregateWrapper.unwind("$tags");
            aggregateWrapper.sortByCount("$tags");
            List<Document> aggregateList = execute("sortByCount", aggregateWrapper, Document.class);
            Assertions.assertEquals(10, aggregateList.size());
            Assertions.assertEquals(6, aggregateList.get(0).getInteger("count"));
        } finally {
            removeCollection("sortByCount");
        }
    }

    @Test
    public void skip() {
        // skip没什么好演示的
        new AggregateWrapper().skip(1);
    }

    @Test
    public void limit() {
        // limit没什么好演示的
        new AggregateWrapper().limit(10);
    }

    @Test
    public void lookup() {
        try {
            //用这些文档创建一个集合 orders
            Boolean orders = baseMapper.saveBatch("orders", getDocumentArray("lookup1.json"));
            log.info("orders:{}", orders);
            //用这些文档创建另一个集合 inventory
            Boolean inventory = baseMapper.saveBatch("inventory", getDocumentArray("lookup2.json"));
            log.info("inventory:{}", inventory);
            //orders 集合上的如下聚合操作使用来自 orders 集合的字段 item 和来自 inventory 集合的 sku 字段，将来自 orders 的文档与
            //来自 inventory 集合的文档联接在一起
            //该操作对应于如下伪 SQL 语句
            /*
            * SELECT *, inventory_docs
                FROM orders
                WHERE inventory_docs IN (
                        SELECT *
                        FROM inventory
                        WHERE sku = orders.item
                );
            */
            AggregateWrapper aggregateWrapper = new AggregateWrapper();
            aggregateWrapper.lookup(
                    "inventory",
                    "item",
                    "sku",
                    "inventory_docs"
            );
            List<Document> aggregateList = execute("orders", aggregateWrapper, Document.class);
            Assertions.assertEquals(3, aggregateList.size());
            Assertions.assertFalse(aggregateList.get(2).containsKey("item"));
        } finally {
            removeCollection("orders");
            removeCollection("inventory");
        }
    }

    @Test
    public void facet() {
        try {
            // 以下操作使用 MongoDB 的分面功能为客户提供按多个维度（如标签、价格和创建年份）分类的商店库存。
            // 此 $facet 阶段有三个子管道，分别使用 $sortByCount、$bucket 或 $bucketAuto 来执行此次分面聚合。
            // artwork 中的输入文档只在操作开始时从数据库中获取一次
            Boolean facet = baseMapper.saveBatch("facet", getDocumentArray("facet.json"));
            log.info("facet:{}", facet);
            AggregateWrapper aggregateWrapper = new AggregateWrapper();
            aggregateWrapper.facet(
                    new Facet("categorizedByTags", wrapper -> wrapper.unwind("$tags").sortByCount("$tags")),
                    new Facet("categorizedByPrice", wrapper -> wrapper
                            .match(queryWrapper -> queryWrapper.exists("price", true))
                            .bucket("$price",
                                    Arrays.asList(0, 150, 200, 300, 400),
                                    new BucketOptions().defaultBucket("Other")
                                            .output(
                                                    Accumulators.sum("count", 1),
                                                    Accumulators.push("titles", "$title")
                                            )
                            )
                    ),
                    new Facet("categorizedByYears(Auto)", wrapper -> wrapper.bucketAuto("$year", 4))
            );
            List<Document> aggregateList = execute("facet", aggregateWrapper, Document.class);
            Document document = aggregateList.get(0);
            Assertions.assertEquals(3, document.keySet().size());
            Assertions.assertEquals(4, document.getList("categorizedByYears(Auto)", Document.class).size());
            Assertions.assertEquals(5, document.getList("categorizedByPrice", Document.class).size());
            Assertions.assertEquals(10, document.getList("categorizedByTags", Document.class).size());
        } finally {
            removeCollection("facet");
        }
    }

    /**
     * 这里构建的没有问题，但是MongoDB官网给的示例有问题
     * 使用MongoDB给的示例和语句，查询结果不一样，他的示例是有问题的
     *
     * @author anwen
     * @date 2024/8/23 01:22
     */
    @Test
    public void graphLookup() {
        try {
            //单个集合内
            //名为 employees 的集合包含以下文档
            Boolean employees = baseMapper.saveBatch("employees", getDocumentArray("graphLookup.json"));
            log.info("employees:{}", employees);
            AggregateWrapper aggregateWrapper = new AggregateWrapper();
            aggregateWrapper.graphLookup(
                    "employees",
                    "$reportsTo",
                    "reportsTo",
                    "name",
                    "reportingHierarchy"
            );
            List<Document> aggregateList = execute("employees", aggregateWrapper, Document.class);
            Assertions.assertTrue(
                    aggregateList.get(aggregateList.size()-1)
                            .getList("reportingHierarchy",Document.class).stream()
                            .map(document -> document.getInteger("_id")).anyMatch(id -> id == 4));
            //跨多个集合
            //与 $lookup 一样，$graphLookup 可以访问同一数据库中的另一个集合。
            //例如，创建一个包含两个集合的数据库
            Boolean airports = baseMapper.saveBatch("airports", getDocumentArray("graphLookups1.json"));
            log.info("airports:{}", airports);
            Boolean travelers = baseMapper.saveBatch("travelers", getDocumentArray("graphLookups2.json"));
            log.info("travelers:{}", travelers);
            AggregateWrapper aggregateWrappers = new AggregateWrapper();
            aggregateWrappers.graphLookup(
                    "airports",
                    "$nearestAirport",
                    "connects",
                    "airport",
                    "destinations",
                    new GraphLookupOptions().maxDepth(2).depthField("numConnections")
            );
            List<Document> aggregateLists = execute("travelers", aggregateWrappers, Document.class);
        } finally {
            removeCollection("employees");
            removeCollection("airports");
            removeCollection("travelers");
        }
    }

    @Test
    public void group() {
        try {
            // 计算集合中的文档数量
            // 创建名为 sales 的示例集合
            Boolean group = baseMapper.saveBatch("group", getDocumentArray("group.json"));
            log.info("group:{}", group);
            // 以下聚合操作使用 $group 阶段来计算 sales 集合中的文档数量
            AggregateWrapper aggregateWrapper = new AggregateWrapper();
            aggregateWrapper.group(null, Accumulators.count("count"));
            // 这个聚合操作相当于以下 SQL 语句
            // SELECT COUNT(*) AS count FROM sales
            List<Document> documentList = execute("group", aggregateWrapper, Document.class);
            Assertions.assertEquals(8, documentList.get(0).getInteger("count"));

            //按列项分组
            //下面的聚合操作按 item 字段对文档进行分组，计算每个列项的总销售额，然后只返回总销售额大于或等于 100 的项目
            AggregateWrapper itemAggregateWrapper = new AggregateWrapper();
            itemAggregateWrapper.group(
                    "$item",
                    Accumulators.sum("totalSaleAmount", ConditionOperators.multiply("$price", "$quantity"))
            );
            itemAggregateWrapper.match(queryWrapper -> queryWrapper.gte("totalSaleAmount", 100));
            /*
             * 这个聚合操作相当于以下 SQL 语句
             *  SELECT item,
             *      Sum(( price * quantity )) AS totalSaleAmount
             *  FROM   sales
             *  GROUP  BY item
             *  HAVING totalSaleAmount >= 100
             */
            List<Document> itemDocumentList = execute("group", itemAggregateWrapper, Document.class);
            Assertions.assertEquals(3, itemDocumentList.size());

            // 计算数量、总和和平均值
            // 按当年天数分组
            // 以下管道计算 2014 年每一天的总销售额、平均销售数量和销售数量
            // 这个聚合操作相当于以下 SQL 语句
            /*
            SELECT date,
                   Sum(( price * quantity )) AS totalSaleAmount,
                   Avg(quantity)             AS averageQuantity,
                   Count(*)                  AS Count
            FROM   sales
            WHERE  date >= '01/01/2014' AND date < '01/01/2015'
            GROUP  BY date
            ORDER  BY totalSaleAmount DESC
             */
            AggregateWrapper groupAggregateWrapper = new AggregateWrapper();
            groupAggregateWrapper.match(wrapper -> wrapper
                    .gte("date", LocalDate.of(2014, 1, 1))
                    .lt("date", LocalDate.of(2015, 1, 1))
            );
            groupAggregateWrapper.group(
                    ConditionOperators.dateToString("%Y-%m-%d", "$date"),
                    Accumulators.sum("totalSaleAmount", ConditionOperators.multiply("$price", "$quantity")),
                    Accumulators.avg("averageQuantity", "$quantity"),
                    Accumulators.sum()
            );
            groupAggregateWrapper.sortDesc("totalSaleAmount");
            List<Document> groupDocumentList = execute("group", groupAggregateWrapper, Document.class);
        } finally {
            removeCollection("group");
        }
    }

    @Test
    public void unionWith(){
        try {
            //从年度数据集合联合创建销售报告
            //以下示例使用 $unionWith 阶段合并数据，并从多个集合中返回结果。在这些示例中，每个集合都包含一年的销售数据。
            //填充样本数据
            Boolean sales_2017 = baseMapper.saveBatch("sales_2017", getDocumentArray("unionWith1.json"));
            log.info("sales_2017:{}",sales_2017);
            Boolean sales_2018 = baseMapper.saveBatch("sales_2018", getDocumentArray("unionWith2.json"));
            log.info("sales_2018:{}",sales_2018);
            Boolean sales_2019 = baseMapper.saveBatch("sales_2019", getDocumentArray("unionWith3.json"));
            log.info("sales_2019:{}",sales_2019);
            Boolean sales_2020 = baseMapper.saveBatch("sales_2020", getDocumentArray("unionWith4.json"));
            log.info("sales_2020:{}",sales_2020);

            //报告 1：按年份、商店和商品列出的所有销售额
            //以下聚合创建了一份年度销售报告，其中按季度和门店列出了所有销售额。该管道使用 $unionWith 来合并所有四个集合的文档

            //$set 阶段，用于更新 _id 字段以包含年份。
            //
            //一系列 $unionWith 阶段，用于将来自四个集合的所有文档合并，每个集合还会使用 $set 阶段对文档进行处理。
            //
            //按$sort （年份）、 _id和store排序的item阶段。
            AggregateWrapper aggregateWrapperOne = new AggregateWrapper();
            aggregateWrapperOne.set(BaseModelID::getId,"2017");
            aggregateWrapperOne.unionWith("sales_2018",new AggregateWrapper().set(BaseModelID::getId,"2018"));
            aggregateWrapperOne.unionWith("sales_2019",new AggregateWrapper().set(BaseModelID::getId,"2019"));
            aggregateWrapperOne.unionWith("sales_2020",new AggregateWrapper().set(BaseModelID::getId,"2020"));
            aggregateWrapperOne.sortAsc("_id","store","item");
            List<Map<String,Object>> aggregateListOne = execute("sales_2017",aggregateWrapperOne);
            Assertions.assertEquals(35,aggregateListOne.size());

            //报告 2：按商品分类的合计销售额
            //以下聚合创建了一份销售报告，其中列出了每个商品的销售数量。该管道使用 $unionWith 来合并所有四年的文档
            AggregateWrapper aggregateWrapperTwo = new AggregateWrapper();
            aggregateWrapperTwo.unionWith("sales_2018");
            aggregateWrapperTwo.unionWith("sales_2019");
            aggregateWrapperTwo.unionWith("sales_2020");
            aggregateWrapperTwo.group(
                    "$item",
                    Accumulators.sum("total","$quantity")
            );
            aggregateWrapperTwo.sortDesc("total");
            List<Map<String,Object>> aggregateListTwo = execute("sales_2017",aggregateWrapperTwo);
            Assertions.assertEquals(5,aggregateListTwo.size());
        } finally {
            removeCollection("sales_2017");
            removeCollection("sales_2018");
            removeCollection("sales_2019");
            removeCollection("sales_2020");
        }
    }

    @Test
    public void unwind(){
        try {
            //展开数组
            //在 mongosh 中创建名为 inventory 的示例集合，其中包含以下文档
            Boolean unwind1 = baseMapper.save("unwind1", getDocument("unwind1.json"));
            log.info("unwind1:{}",unwind1);
            //以下聚合使用 $unwind 阶段为 sizes 数组中的每个元素输出一个文档
            AggregateWrapper aggregateWrapper = new AggregateWrapper();
            aggregateWrapper.unwind("$sizes");
            List<Map<String,Object>> aggregateList = execute("unwind1",aggregateWrapper);
            Assertions.assertEquals(3,aggregateList.size());

            //以下 $unwind 操作使用 preserveNullAndEmptyArrays 选项来纳入 sizes 字段为 null、缺失或空数组的文档。
            Boolean unwind2 = baseMapper.saveBatch("unwind2", getDocumentArray("unwind2.json"));
            log.info("unwind2:{}",unwind2);
            AggregateWrapper aggregateWrapperArray = new AggregateWrapper();
            aggregateWrapperArray.unwind("$sizes",new UnwindOption().preserveNullAndEmptyArrays(true));
            List<Map<String,Object>> aggregateArrayList = execute("unwind2",aggregateWrapperArray);
            Assertions.assertEquals(7,aggregateArrayList.size());
        } finally {
            removeCollection("unwind1");
            removeCollection("unwind2");
        }
    }

    @Test
    public void out() {
        try {
            Boolean out = baseMapper.saveBatch("out_save", getDocumentArray("out.json"));
            log.info("out:{}",out);
            //输出到其他数据库
            //$out可以输出到与运行聚合的数据库不同的数据库中的集合。

            //以下聚合操作对 out_save 集合中的数据进行透视，从而按作者对书名分组，然后将结果写入 mongo-plus-out 数据库中的 out_save 集合
            AggregateWrapper aggregateWrapper = new AggregateWrapper();
            aggregateWrapper.group("$author",Accumulators.push("books","$title"));
            aggregateWrapper.out("mongo-plus-out","out_save");
            execute("out_save",aggregateWrapper);
            List<Document> documentList = baseMapper.list("mongo-plus-out","out_save",Document.class);
            Assertions.assertEquals(2,documentList.size());
        } finally {
            removeCollection("out_save");
            removeDatabase("mongo-plus-out");
        }
    }

    @Test
    public void merge(){
        try {
            //按需物化视图：初始创建
            //如果输出集合不存在，则 $merge 会创建该集合。
            //例如，数据库中的 merge 集合填充有员工工资和部门历史
            Boolean merge = baseMapper.saveBatch("merge_save", getDocumentArray("merge.json"));
            log.info("merge:{}",merge);
            //您可以使用 $group 和 $merge 阶段从当前位于 salaries 集合中的数据初始创建名为 budgets 的集合（在 mongo-plus-merge 数据库中）
            AggregateWrapper aggregateWrapper = new AggregateWrapper();
            aggregateWrapper.group(
                    new MongoPlusDocument(){{
                        put("fiscal_year","$fiscal_year");
                        put("dept","$dept");
                    }},
                    Accumulators.sum("salaries","$salary")
            );
            aggregateWrapper.merge(
                    new MongoNamespace("mongo-plus-merge","merge_save"),
                    new MergeOptions()
                            .uniqueIdentifier(FunctionUtil.getFieldName(BaseModelID::getId))
                            .whenMatched(MergeOptions.WhenMatched.REPLACE)
                            .whenNotMatched(MergeOptions.WhenNotMatched.INSERT)
            );
            execute("merge_save",aggregateWrapper);
            List<Document> documentList = baseMapper.list("mongo-plus-merge","merge_save",Document.class);
            Assertions.assertEquals(6,documentList.size());
        } finally {
            removeCollection("merge_save");
            removeDatabase("mongo-plus-merge");
        }
    }

    @Test
    public void replaceRoot(){
        try {
            Boolean rootPlaceRoot = baseMapper.saveBatch("replaceRoot_save",getDocumentArray("replaceRoot.json"));
            log.info("rootPlaceRoot:{}",rootPlaceRoot);
            //以下操作使用 $replaceRoot 阶段将每个输入文档替换为 $mergeObjects 操作的结果。$mergeObjects 表达式将指定的默认文档与 pets 文档合并
            AggregateWrapper aggregateWrapper = new AggregateWrapper();
        /*aggregateWrapper.replaceRoot(ConditionOperators.mergeObjects(
                new MongoPlusDocument(){{
                    putOption(BaseModelID::getId,BaseModelID::getId);
                    put("first","");
                    put("last","");
                }},
                "$name"
        ));*/
            aggregateWrapper.replaceRoot(
                    ConditionOperators.mergeObjects(
                            new MongoPlusDocument(){{
                                put("dogs",0);
                                put("cats",0);
                                put("birds",0);
                                put("fish",0);
                            }},
                            "$pets"
                    )
            );
            List<Map<String, Object>> aggregateList = execute("replaceRoot_save", aggregateWrapper);
            Assertions.assertEquals(3,aggregateList.size());
        } finally {
            removeCollection("replaceRoot_save");
        }
    }

    @Test
    public void replaceWith(){
        //以下操作使用 $replaceWith 阶段将每个输入文档替换为 $mergeObjects 操作的结果。$mergeObjects 表达式将指定的默认文档与 pets 文档合并
        try {
            Boolean replaceWith = baseMapper.saveBatch("replaceWith_save", getDocumentArray("replaceWith.json"));
            log.info("replaceWith:{}",replaceWith);
            AggregateWrapper aggregateWrapper = new AggregateWrapper();
            aggregateWrapper.replaceWith(
                    ConditionOperators.mergeObjects(
                            new MongoPlusDocument(){{
                                put("dogs",0);
                                put("cats",0);
                                put("birds",0);
                                put("fish",0);
                            }},
                            "$pets"
                    )
            );
            List<Map<String, Object>> aggregateList = execute("replaceWith_save", aggregateWrapper);
            Assertions.assertEquals(3,aggregateList.size());
        } finally {
            removeCollection("replaceWith_save");
        }
    }

    @Test
    public void sample(){
        // sample没什么好演示的
        new AggregateWrapper().sample(3);
    }

    @Test
    public void unset(){
        try {
            Boolean unset = baseMapper.saveBatch("unset_save", getDocumentArray("unset.json"));
            log.info("unset:{}",unset);
            // 以下示例将删除顶级字段 copies
            AggregateWrapper aggregateWrapper = new AggregateWrapper();
            aggregateWrapper.unset("copies");
            List<Map<String, Object>> aggregateList = execute("unset_save", aggregateWrapper);
            Assertions.assertFalse(aggregateList.get(0).containsKey("copies"));
        } finally {
            removeCollection("unset_save");
        }
    }

    @SneakyThrows
    private Document getDocument(String json) {
        byte[] bytes = Files.readAllBytes(Paths.get(System.getProperty("user.dir") + "/json/" + json));
        return JSON.parseObject(bytes, Document.class);
    }

    @SneakyThrows
    private List<Document> getDocumentArray(String json) {
        byte[] bytes = Files.readAllBytes(Paths.get(System.getProperty("user.dir") + "/json/" + json));
        return JSON.parseArray(new String(bytes, StandardCharsets.UTF_8), Document.class);
    }

    private List<Map<String, Object>> execute(String collectionName, Aggregate<?> aggregate) {
        return baseMapper.aggregateList(collectionName, aggregate, new TypeReference<Map<String, Object>>() {});
    }

    private <T> List<T> execute(String collectionName, Aggregate<?> aggregate, Class<T> clazz) {
        return baseMapper.aggregateList(collectionName, aggregate, clazz);
    }

    private void removeCollection(String collectionName) {
        mongoPlusClient.getCollection("mongo-demo", collectionName).drop();
    }

    private void removeDatabase(String database) {
        mongoPlusClient.getMongoClient().getDatabase(database).drop();
    }


}
