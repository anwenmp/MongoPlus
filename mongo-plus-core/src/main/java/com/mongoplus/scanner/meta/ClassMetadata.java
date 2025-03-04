package com.mongoplus.scanner.meta;

/**
 * @author anwen
 */
public interface ClassMetadata {

    /**
     * 返回类名
     */
    String getClassName();

    /**
     * 获取父类
     */
    Class<?> getSuperClass();

    /**
     * 返回父类的类名
     */
    default String getSuperClassName() {
        Class<?> clazz;
        if ((clazz = getSuperClass()) != null) {
            return clazz.getName();
        }
        return null;
    }

    /**
     * 获取接口类
     */
    Class<?>[] getInterfaces();

    /**
     * 获取指定接口
     * @param clazz clazz
     * @return {@link java.lang.Class}
     * @author anwen
     */
    default Class<?> getInterface(Class<?> clazz) {
        for (Class<?> interfaceClazz : getInterfaces()) {
            if (interfaceClazz.equals(clazz)) {
                return interfaceClazz;
            }
        }
        return null;
    }

    /**
     * 返回接口类的名字
     */
    default String[] getInterfaceNames() {
        Class<?>[] interfaces = getInterfaces();
        if (interfaces != null) {
            String[] interfaceNames = new String[interfaces.length];
            for (int i = 0; i < interfaces.length; i++) {
                interfaceNames[i] = interfaces[i].getName();
            }
            return interfaceNames;
        }
        return null;
    }

    /**
     * 判断类是否为接口
     */
    boolean isInterface();

    /**
     * 判断类是否为抽象类
     */
    boolean isAbstract();

    /**
     * 判断类是否为最终类
     */
    boolean isFinal();

}
