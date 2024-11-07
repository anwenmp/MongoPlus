package com.anwen.mongo.conditions.update;

import com.mongodb.client.model.DeleteOptions;
import com.mongodb.client.model.UpdateOptions;

/**
 * 修改方法定义
 * @author JiaChaoYang
 * @date 2023/6/24/024 2:58
*/
public interface ChainUpdate {

    /**
     * 执行修改
     * @return {@link boolean}
     * @author anwen
     * @date 2024/11/5 14:57
     */
    default boolean update(){
        return update(null);
    }

    /**
     * 执行修改
     * @param options 修改选项
     * @return {@link boolean}
     * @author anwen
     * @date 2024/11/5 14:58
     */
    boolean update(UpdateOptions options);

    /**
     * 执行删除
     * @return {@link boolean}
     * @author anwen
     * @date 2024/11/5 14:58
     */
    default boolean remove(){
        return remove(null);
    }

    /**
     * 执行删除
     * @param options 删除选项
     * @return {@link boolean}
     * @author anwen
     * @date 2024/11/5 14:58
     */
    boolean remove(DeleteOptions options);

}
