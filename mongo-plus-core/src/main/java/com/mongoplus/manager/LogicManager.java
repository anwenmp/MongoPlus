package com.mongoplus.manager;

import com.mongoplus.model.LogicDeleteResult;
import com.mongoplus.model.LogicProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public class LogicManager {

    private static final ThreadLocal<Boolean> logicIgnore = new InheritableThreadLocal<>();

    /**
     * 是否开启逻辑删除功能
     */
    public static Boolean open = false;

    /**
     * 逻辑删除配置
     */
    public static LogicProperty logicProperty = new LogicProperty();

    /**
     * 目标文档对应的逻辑删除字段
     */
    public static final Map<Class<?>, LogicDeleteResult> logicDeleteResultHashMap = new HashMap<>();

    /**
     * 忽略逻辑删除
     * @param supplier 执行方法
     * @return {@link T}
     * @author anwen
     */
    public static <T> T withoutLogic(Supplier<T> supplier){
        try {
            ignoreLogicCondition();
            return supplier.get();
        } finally {
            restoreLogicCondition();
        }
    }

    /**
     * 忽略逻辑删除
     * @param runnable 执行方法
     * @author anwen
     */
    public static void withoutLogic(Runnable runnable){
        try {
            ignoreLogicCondition();
            runnable.run();
        } finally {
            restoreLogicCondition();
        }
    }

    /**
     * 忽略逻辑删除条件
     * @author anwen
     */
    public static void ignoreLogicCondition() {
        logicIgnore.set(true);
    }

    /**
     * 是否忽略逻辑删除
     * @return {@link boolean}
     * @author anwen
     */
    public static boolean isIgnoreLogic() {
        Boolean ignore = logicIgnore.get();
        return Objects.nonNull(ignore) && ignore;

    }

    /**
     * 恢复逻辑删除条件
     * @author anwen
     */
    public static void restoreLogicCondition() {
        logicIgnore.remove();
    }

}
