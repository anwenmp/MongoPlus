package com.mongoplus.convert;

import com.mongodb.client.ListIndexesIterable;
import com.mongodb.client.MongoCursor;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Document转对象
 * @author anwen
 * @date 2024/8/2 上午1:13
 */
public class DocumentMapperConvert {

    public static List<Document> indexesIterableToDocument(ListIndexesIterable<Document> indexesIterable){
        return new ArrayList<Document>(){{
            try (MongoCursor<Document> cursor = indexesIterable.iterator()) {
                while (cursor.hasNext()) {
                    add(cursor.next());
                }
            }
        }};
    }
}
