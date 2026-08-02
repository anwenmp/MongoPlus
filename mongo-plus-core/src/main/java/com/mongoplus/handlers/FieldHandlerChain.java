package com.mongoplus.handlers;

import com.mongoplus.handlers.field.DBRefHandler;
import com.mongoplus.handlers.field.EncryptFieldHandler;
import com.mongoplus.handlers.field.TypeHandlerFieldHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.function.UnaryOperator;

/**
 * 字段处理器责任链
 *
 * <p>当处理程序被注册时，链表会按照升序排列（根据{@link FieldHandler#order()}方法），因此字段转换只需遍历该链表即可。</p>
 */
public class FieldHandlerChain extends ArrayList<FieldHandler> {

    private static final long serialVersionUID = 1L;

    private static final Comparator<FieldHandler> ORDER_COMPARATOR =
            Comparator.comparingInt(FieldHandler::order);

    private static final FieldHandlerChain INSTANCE = new FieldHandlerChain();

    static {
        INSTANCE.registerAll(Arrays.asList(
                new TypeHandlerFieldHandler(),
                new EncryptFieldHandler(),
                new DBRefHandler()
        ));
    }

    /**
     * 获取全局字段处理责任链。
     *
     * @return 字段处理责任链
     */
    public static FieldHandlerChain getInstance() {
        return INSTANCE;
    }

    /**
     * 注册字段处理器，注册完成后按 order 排序。
     *
     * @param fieldHandler 字段处理器
     */
    public void register(FieldHandler fieldHandler) {
        add(fieldHandler);
    }

    /**
     * 批量注册字段处理器，全部注册完成后只排序一次。
     *
     * @param fieldHandlers 字段处理器
     */
    public void registerAll(Collection<? extends FieldHandler> fieldHandlers) {
        addAll(fieldHandlers);
    }

    @Override
    public boolean add(FieldHandler fieldHandler) {
        boolean changed = super.add(fieldHandler);
        sortHandlers();
        return changed;
    }

    @Override
    public void add(int index, FieldHandler element) {
        super.add(index, element);
        sortHandlers();
    }

    @Override
    public boolean addAll(Collection<? extends FieldHandler> fieldHandlers) {
        boolean changed = super.addAll(fieldHandlers);
        if (changed) {
            sortHandlers();
        }
        return changed;
    }

    @Override
    public boolean addAll(int index, Collection<? extends FieldHandler> fieldHandlers) {
        boolean changed = super.addAll(index, fieldHandlers);
        if (changed) {
            sortHandlers();
        }
        return changed;
    }

    @Override
    public FieldHandler set(int index, FieldHandler element) {
        FieldHandler previous = super.set(index, element);
        sortHandlers();
        return previous;
    }

    @Override
    public void replaceAll(UnaryOperator<FieldHandler> operator) {
        super.replaceAll(operator);
        sortHandlers();
    }

    private void sortHandlers() {
        super.sort(ORDER_COMPARATOR);
    }
}
