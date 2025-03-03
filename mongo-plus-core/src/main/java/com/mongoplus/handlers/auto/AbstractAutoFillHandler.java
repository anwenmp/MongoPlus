package com.mongoplus.handlers.auto;

import com.mongoplus.annotation.collection.CollectionField;
import com.mongoplus.bson.ImmutableDocument;
import com.mongoplus.bson.MongoPlusDocument;
import com.mongoplus.cache.global.HandlerCache;
import com.mongoplus.enums.FieldFill;
import com.mongoplus.handlers.MetaObjectHandler;
import com.mongoplus.mapping.FieldInformation;
import com.mongoplus.mapping.TypeInformation;
import com.mongoplus.model.AutoFillMetaObject;
import com.mongoplus.toolkit.CollUtil;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 自动填充处理，重写可继承此抽象类
 *
 * @author Administrator
 */
public abstract class AbstractAutoFillHandler implements AutoFillHandler {

    /**
     * 自动填充字段
     */
    protected final Map<Class<?>, Map<FieldFill, List<FieldInformation>>> autoFillFields = new ConcurrentHashMap<>();

    /**
     * 初始化自动填充字段映射
     *
     * @author anwen
     */
    protected void initAutoFill(TypeInformation typeInformation) {
        autoFillFields.putIfAbsent(typeInformation.getClazz(), typeInformation.getFields()
                .stream()
                .filter(this::isValidField)
                .collect(Collectors.groupingBy(this::getFieldFill)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(Document source, TypeInformation typeInformation, FieldFill fieldFill) {
        if (null == HandlerCache.metaObjectHandler) {
            return;
        }
        initAutoFill(typeInformation);
        if (!autoFillFields.containsKey(typeInformation.getClazz())) {
            return;
        }
        AutoFill autoFill = getAutoFill(source, typeInformation, fieldFill);
        if (autoFill == null) {
            return;
        }
        AutoFillMetaObject autoFillMetaObject = autoFill.toAutoFillMetaObject();
        fillHandle(autoFill, autoFillMetaObject);
        source.putAll(autoFillMetaObject.getAllFillField());
    }

    protected AutoFill getAutoFill(Document source, TypeInformation typeInformation, FieldFill fieldFill) {
        Map<FieldFill, List<FieldInformation>> fieldFillListMap = autoFillFields.get(typeInformation.getClazz());
        List<FieldInformation> autoFillFieldList = Optional.ofNullable(fieldFillListMap.get(fieldFill))
                .map(ArrayList::new)
                .orElseGet(ArrayList::new);

        Optional.ofNullable(fieldFillListMap.get(FieldFill.INSERT_UPDATE))
                .ifPresent(autoFillFieldList::addAll);
        if (CollUtil.isEmpty(autoFillFieldList)) {
            return null;
        }
        return new AutoFill(new ImmutableDocument(source),
                typeInformation,
                HandlerCache.metaObjectHandler,
                autoFillFieldList, fieldFill);
    }

    /**
     * 新增处理
     *
     * @author anwen
     */
    protected abstract void fillHandle(AutoFill autoFill, AutoFillMetaObject autoFillMetaObject);

    /**
     * 提取判断，扩展性延伸
     *
     * @return {@link boolean}
     * @author anwen
     */
    protected boolean isValidField(FieldInformation fieldInformation) {
        return Optional.ofNullable(fieldInformation.getCollectionField())
                .map(CollectionField::fill)
                .filter(fill -> fill != FieldFill.DEFAULT)
                .isPresent();
    }

    protected FieldFill getFieldFill(FieldInformation fieldInformation) {
        return fieldInformation.getCollectionField().fill();
    }

    protected static class AutoFill {
        /**
         * 源文档
         */
        private final Document source;

        /**
         * 类包装对象
         */
        private final TypeInformation typeInformation;

        /**
         * 元对象处理器
         */
        private final MetaObjectHandler metaObjectHandler;

        /**
         * 自动填充字段
         */
        private final List<FieldInformation> fieldInformationList;

        /**
         * 操作
         */
        private final FieldFill fieldFill;

        public AutoFill(Document source, TypeInformation typeInformation, MetaObjectHandler metaObjectHandler,
                        List<FieldInformation> fieldInformationList, FieldFill fieldFill) {
            this.source = source;
            this.typeInformation = typeInformation;
            this.metaObjectHandler = metaObjectHandler;
            this.fieldInformationList = fieldInformationList;
            this.fieldFill = fieldFill;
        }

        public Document getSource() {
            return source;
        }

        public TypeInformation getTypeInformation() {
            return typeInformation;
        }

        public MetaObjectHandler getMetaObjectHandler() {
            return metaObjectHandler;
        }

        public List<FieldInformation> getFieldInformationList() {
            return fieldInformationList;
        }

        public FieldFill getFieldFill() {
            return fieldFill;
        }

        /**
         * 将字段转为Document
         * @return {@link Document}
         * @author anwen
         */
        public MongoPlusDocument asDocument() {
            return new MongoPlusDocument(){{
                getFieldInformationList()
                        .forEach(fieldInformation ->
                                put(
                                        fieldInformation.getCamelCaseName(),
                                        fieldInformation.getValue(typeInformation.getInstance())
                                )
                        );
            }};
        }

        /**
         * 转为AutoFillMetaObject
         * @author anwen
         */
        protected AutoFillMetaObject toAutoFillMetaObject() {
            return new AutoFillMetaObject(asDocument(),getTypeInformation());
        }

    }

}
