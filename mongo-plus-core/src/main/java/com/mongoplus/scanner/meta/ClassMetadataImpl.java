package com.mongoplus.scanner.meta;

/**
 * @author anwen
 */
public class ClassMetadataImpl implements ClassMetadata {

    private final String className;
    private final Class<?> superClass;
    private final Class<?>[] interfaces;
    private final boolean isInterface;
    private final boolean isAbstract;
    private final boolean isFinal;

    public ClassMetadataImpl(String className, Class<?> superClass, Class<?>[] interfaces,
                             boolean isInterface, boolean isAbstract, boolean isFinal) {
        this.className = className;
        this.superClass = superClass;
        this.interfaces = interfaces;
        this.isInterface = isInterface;
        this.isAbstract = isAbstract;
        this.isFinal = isFinal;
    }

    @Override
    public String getClassName() {
        return className;
    }

    @Override
    public Class<?> getSuperClass() {
        return superClass;
    }

    @Override
    public Class<?>[] getInterfaces() {
        return interfaces;
    }

    @Override
    public boolean isInterface() {
        return isInterface;
    }

    @Override
    public boolean isAbstract() {
        return isAbstract;
    }

    @Override
    public boolean isFinal() {
        return isFinal;
    }
}
