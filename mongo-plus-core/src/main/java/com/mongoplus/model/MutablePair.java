package com.mongoplus.model;

import com.mongoplus.support.SFunction;

/**
 * 可变的Pair
 *
 * @author JiaChaoYang
 **/
public class MutablePair<L,R> extends Pair<L, R> {

   /**
    * 左侧对象
   */
    public L left;

    /**
     * 右侧对象
    */
    public R right;

    /**
     * 从两个推断泛型类型的对象中获得一对不可变的。
     * 这个工厂允许使用推理来创建对，以获得泛型类型。
     * @author JiaChaoYang
    */
    public static <L, R> MutablePair<L, R> of(final L left, final R right) {
        return new MutablePair<>(left, right);
    }

    /**
     * 从两个推断泛型类型的对象中获得一对不可变的。
     * 这个工厂允许使用推理来创建对，以获得泛型类型。
     * @author JiaChaoYang
     */
    public static <T, R> MutablePair<String, R> of(final SFunction<T,Object> left, final R right) {
        return new MutablePair<>(left, right);
    }

    /**
     * 创建两个null的新对实例
     * @author JiaChaoYang
    */
    public MutablePair() {
        super();
    }

    /**
     * 创建一个新的配对实例。
     * @author JiaChaoYang
    */
    public MutablePair(final L left, final R right) {
        super();
        this.left = left;
        this.right = right;
    }

    /**
     * 创建一个新的配对实例。
     * @author JiaChaoYang
     */
    @SuppressWarnings("unchecked")
    public MutablePair(final SFunction<?,Object> left, final R right) {
        super();
        this.left = (L) left.getFieldNameLine();
        this.right = right;
    }

    //-----------------------------------------------------------------------
    /**
     * {@inheritDoc}
     * @author JiaChaoYang
    */
    @Override
    public L getLeft() {
        return left;
    }

    /**
     * 设置该对的左侧元素
     * @author JiaChaoYang
    */
    public void setLeft(final L left) {
        this.left = left;
    }

    /**
     * {@inheritDoc}
     * @author JiaChaoYang
     */
    @Override
    public R getRight() {
        return right;
    }

    /**
     * 设置对的右侧元素
     * @author JiaChaoYang
    */
    public void setRight(final R right) {
        this.right = right;
    }

    /**
     * 这设置了对中正确的元素
     * @author JiaChaoYang
    */
    @Override
    public R setValue(final R value) {
        final R result = getRight();
        setRight(value);
        return result;
    }

}
