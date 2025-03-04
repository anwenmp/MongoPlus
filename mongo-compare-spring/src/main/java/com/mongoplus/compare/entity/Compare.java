package com.mongoplus.compare.entity;

import com.mongoplus.annotation.ID;
import com.mongoplus.annotation.collection.CollectionName;
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
