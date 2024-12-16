package com.anwen.mongo.registrar;

import com.anwen.mongo.mapper.MongoMapper;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.lang.NonNull;

/**
 * MongoMapper接口的扫描过滤器，如果实现的接口不是MongoMapper，则返回false,
 * AssignableTypeFilter类默认返回null，实现的接口不是MongoMapper也会扫描到
 * @author anwen
 */
public class MongoMapperTypeFilter extends AssignableTypeFilter {

    public MongoMapperTypeFilter() {
        super(MongoMapper.class);
    }

    @Override
    protected Boolean matchTargetType(@NonNull String typeName) {
        Boolean _b = super.matchTargetType(typeName);
        return _b != null && _b;
    }
}
