/*
 * Copyright (c) JiaChaoYang 2024-7-13 MongoPlus版权所有
 * 适度编码益脑，沉迷编码伤身，合理安排时间，享受快乐生活。
 * email: j15030047216@163.com
 * phone: 15030047216
 * weChat: JiaChaoYang_
 */

package com.anwen.compare.entity;

import com.anwen.mongo.annotation.ID;
import com.anwen.mongo.annotation.collection.CollectionName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

/**
 * 比较
 *
 * @author anwen
 */
@CollectionName("compare")
@Document("compare")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Compare {

    @ID
    @Id
    private String id;

    private String index;

    private String type;

    private String version;

    private Source source;

    private Map<String,Object> fields;

    private Map<String,Object> highlight;

    private List<Long> sort;

}
