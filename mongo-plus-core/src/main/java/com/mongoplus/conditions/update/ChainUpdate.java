package com.mongoplus.conditions.update;

import com.mongodb.client.model.DeleteOptions;
import com.mongodb.client.model.UpdateOptions;

/**
 * 修改方法定义
 * @author JiaChaoYang
*/
public interface ChainUpdate {

    /**
     * 执行修改
     * @return {@link boolean}
     * @author anwen
     */
    default boolean update(){
        return update(null);
    }

    /**
     * 执行修改
     * @param options 修改选项
     * @return {@link boolean}
     * @author anwen
     */
    boolean update(UpdateOptions options);

    /**
     * 执行删除
     * @return {@link boolean}
     * @author anwen
     */
    default boolean remove(){
        return remove(null);
    }

    /**
     * 执行删除
     * @param options 删除选项
     * @return {@link boolean}
     * @author anwen
     */
    boolean remove(DeleteOptions options);

}
