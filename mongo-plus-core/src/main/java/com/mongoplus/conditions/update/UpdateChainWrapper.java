package com.mongoplus.conditions.update;

import com.mongodb.BasicDBObject;
import com.mongoplus.conditions.AbstractChainWrapper;
import com.mongoplus.conditions.interfaces.PushOptions;
import com.mongoplus.conditions.interfaces.Update;
import com.mongoplus.conditions.interfaces.condition.CompareCondition;
import com.mongoplus.conditions.query.QueryChainWrapper;
import com.mongoplus.conditions.query.QueryWrapper;
import com.mongoplus.domain.MongoPlusException;
import com.mongoplus.enums.CurrentDateType;
import com.mongoplus.enums.PopType;
import com.mongoplus.handlers.condition.Condition;
import com.mongoplus.model.BaseConditionResult;
import com.mongoplus.model.MutablePair;
import com.mongoplus.support.SFunction;
import com.mongoplus.toolkit.ClassTypeUtil;
import org.bson.conversions.Bson;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import static com.mongoplus.handlers.condition.BuildCondition.condition;

/**
 * update接口实现
 * @author JiaChaoYang
*/
public class UpdateChainWrapper<T,Children extends UpdateChainWrapper<T,Children>>
        extends AbstractChainWrapper<T, Children> implements Update<T,Children> {

    @SuppressWarnings("unchecked")
    protected final Children typedThis = (Children) this;

    private final List<CompareCondition> updateCompareList = new CopyOnWriteArrayList<>();

    private final List<Bson> updateBson = new CopyOnWriteArrayList<>();

    public List<Bson> getUpdateBson() {
        return updateBson;
    }

    public List<CompareCondition> getUpdateCompareList() {
        return updateCompareList;
    }

    @Override
    public synchronized void clear() {
        super.clear();
        updateCompareList.clear();
        updateBson.clear();
    }

    /**
     * 构建修改条件
     * @return {@link com.mongoplus.model.MutablePair}
     * @author anwen
     */
    public MutablePair<BasicDBObject, BasicDBObject> buildUpdateCondition(){
        return buildUpdateCondition(condition());
    }

    /**
     * 构建修改条件
     * @param condition 条件构造器
     * @return {@link com.mongoplus.model.MutablePair}
     * @author anwen
     */
    public MutablePair<BasicDBObject, BasicDBObject> buildUpdateCondition(Condition condition){
        return condition.updateCondition(this);
    }

    @Override
    public Children setNull(boolean condition, SFunction<T, Object> column) {
        return condition ? setNull(column) : typedThis;
    }

    @Override
    public Children setNull(SFunction<T, Object> column) {
        return set(column, Null.class);
    }

    @Override
    public Children setNull(boolean condition, String column) {
        return condition ? setNull(column) : typedThis;
    }

    @Override
    public Children setNull(String column) {
        return set(column,Null.class);
    }

    @Override
    public Children set(boolean condition, SFunction<T, Object> column, Object value) {
        return condition ? set(column,value) : typedThis;
    }

    @Override
    public Children set(SFunction<T, Object> column, Object value) {
        return getBaseUpdateCompare(column,value);
    }

    @Override
    public Children set(boolean condition, String column, Object value) {
        return condition ? set(column,value) : typedThis;
    }

    @Override
    public Children set(String column, Object value) {
        return getBaseUpdateCompare(column,value);
    }

    @Override
    public Children setOnInsert(boolean condition, SFunction<T, Object> column, Object value) {
        return condition ? set(column,value) : typedThis;
    }

    @Override
    public Children setOnInsert(SFunction<T, Object> column, Object value) {
        return getBaseUpdateCompare(column,value);
    }

    @Override
    public Children setOnInsert(boolean condition, String column, Object value) {
        return condition ? set(column,value) : typedThis;
    }

    @Override
    public Children setOnInsert(String column, Object value) {
        return getBaseUpdateCompare(column,value);
    }

    @Override
    public Children push(boolean condition, SFunction<T, Object> column, Object value) {
        return condition ? push(column,value) : typedThis;
    }

    @Override
    public Children push(SFunction<T, Object> column, Object value) {
        return getBaseUpdateCompare(column,value);
    }

    @Override
    public Children push(boolean condition, SFunction<T, Object> column, Object value, boolean each) {
        return condition ? push(column,value,each) : typedThis;
    }

    @Override
    public Children push(SFunction<T, Object> column, Object value, boolean each) {
        if (each && !ClassTypeUtil.isTargetClass(Collection.class,value.getClass())){
            throw new MongoPlusException("$each requires data of Collection type");
        }
        return each ? push(column, Collections.singletonList(value)) : push(column,value);
    }

    @Override
    public Children push(boolean condition, String column, Object value) {
        return condition ? push(column,value) : typedThis;
    }

    @Override
    public Children push(String column, Object value) {
        return getBaseUpdateCompare(column,value);
    }

    @Override
    public Children push(boolean condition, String column, Object value, boolean each) {
        return condition ? push(column,value,each) : typedThis;
    }

    @Override
    public Children push(String column, Object value, boolean each) {
        if (each && !ClassTypeUtil.isTargetClass(Collection.class,value.getClass())){
            throw new MongoPlusException("$each requires data of Collection type");
        }
        return each ? push(column, Collections.singletonList(value)) : push(column,value);
    }

    @Override
    public Children push(boolean condition, SFunction<T, Object> column, Object... value) {
        return condition ? push(column,value) : typedThis;
    }

    @Override
    public Children push(SFunction<T, Object> column, Object... value) {
        return push(column,new PushOptions(),value);
    }

    @Override
    public Children push(boolean condition, SFunction<T, Object> column, PushOptions options, Object... value) {
        return condition ? push(column,Arrays.stream(value).collect(Collectors.toList()),options) : typedThis;
    }

    @Override
    public Children push(boolean condition, SFunction<T, Object> column, PushOptions options, Collection<?> value) {
        return condition ? push(column,options,value) : typedThis;
    }

    @Override
    public Children push(SFunction<T, Object> column, PushOptions options, Object... value) {
        return push(column,options,Arrays.stream(value).collect(Collectors.toList()));
    }

    @Override
    public Children push(SFunction<T, Object> column, PushOptions options, Collection<?> value) {
        return getBaseUpdateCompare(column, value,options);
    }

    @Override
    public Children push(boolean condition, String column, Object... value) {
        return condition ? push(column,value) : typedThis;
    }

    @Override
    public Children push(String column, Object... value) {
        return push(column,new PushOptions(),value);
    }

    @Override
    public Children push(boolean condition, String column, PushOptions options, Object... value) {
        return condition ? push(column,value,options) : typedThis;
    }

    @Override
    public Children push(String column, PushOptions options, Object... value) {
        return push(column,options,Arrays.stream(value).collect(Collectors.toList()));
    }

    @Override
    public Children push(boolean condition, String column, PushOptions options, Collection<?> value) {
        return condition ? push(column,options,value) : typedThis;
    }

    @Override
    public Children push(String column, PushOptions options, Collection<?> value) {
        return getBaseUpdateCompare(column,value,options);
    }

    @Override
    public Children push(boolean condition, SFunction<T, Object> column, Collection<?> value) {
        return condition ? push(column,value) : typedThis;
    }

    @Override
    public Children push(SFunction<T, Object> column, Collection<?> value) {
        return push(column,new PushOptions(),value);
    }

    @Override
    public Children push(boolean condition, SFunction<T, Object> column, List<?> value, PushOptions options) {
        return condition ? push(column,value,options) : typedThis;
    }

    @Override
    public Children push(SFunction<T, Object> column, List<?> value, PushOptions options) {
        return getBaseUpdateCompare(column,value,options);
    }

    @Override
    public Children push(boolean condition, String column, List<?> value) {
        return condition ? push(column,value) : typedThis;
    }

    @Override
    public Children push(String column, List<?> value) {
        return push(column,value,new PushOptions());
    }

    @Override
    public Children push(boolean condition, String column, List<?> value, PushOptions options) {
        return condition ? push(column,value,options) : typedThis;
    }

    @Override
    public Children push(String column, List<?> value, PushOptions options) {
        return getBaseUpdateCompare(column,value,options);
    }

    @Override
    public Children inc(boolean condition, SFunction<T, Object> column, Number value) {
        return condition ? inc(column,value) : typedThis;
    }

    @Override
    public Children inc(SFunction<T, Object> column, Number value) {
        return getBaseUpdateCompare(column,value);
    }

    @Override
    public Children inc(boolean condition, String column, Number value) {
        return condition ? inc(column,value) : typedThis;
    }

    @Override
    public Children inc(String column, Number value) {
        return getBaseUpdateCompare(column,value);
    }

    @Override
    public Children currentDate(boolean condition, SFunction<T, Object> column) {
        return condition ? currentDate(column) : typedThis;
    }

    @Override
    public Children currentDate(SFunction<T, Object> column) {
        return currentDate(column,CurrentDateType.DATE);
    }

    @Override
    public Children currentDate(boolean condition, String column) {
        return condition ? currentDate(column) : typedThis;
    }

    @Override
    public Children currentDate(String column) {
        return currentDate(column,CurrentDateType.DATE);
    }

    @Override
    public Children currentDate(boolean condition, SFunction<T, Object> column, CurrentDateType currentDateType) {
        return condition ? currentDate(column,currentDateType) : typedThis;
    }

    @Override
    public Children currentDate(SFunction<T, Object> column, CurrentDateType currentDateType) {
        return getBaseUpdateCompare(column,currentDateType);
    }

    @Override
    public Children currentDate(boolean condition, String column, CurrentDateType currentDateType) {
        return condition ? currentDate(column,currentDateType) : typedThis;
    }

    @Override
    public Children currentDate(String column, CurrentDateType currentDateType) {
        return getBaseUpdateCompare(column,currentDateType);
    }

    @Override
    public Children min(boolean condition, SFunction<T, Object> column, Object value) {
        return condition ? min(column,value) : typedThis;
    }

    @Override
    public Children min(SFunction<T, Object> column, Object value) {
        return getBaseUpdateCompare(column,value);
    }

    @Override
    public Children min(boolean condition, String column, Object value) {
        return condition ? min(column,value) : typedThis;
    }

    @Override
    public Children min(String column, Object value) {
        return getBaseUpdateCompare(column,value);
    }

    @Override
    public Children max(boolean condition, SFunction<T, Object> column, Object value) {
        return condition ? max(column,value) : typedThis;
    }

    @Override
    public Children max(SFunction<T, Object> column, Object value) {
        return getBaseUpdateCompare(column,value);
    }

    @Override
    public Children max(boolean condition, String column, Object value) {
        return condition ? max(column,value) : typedThis;
    }

    @Override
    public Children max(String column, Object value) {
        return getBaseUpdateCompare(column,value);
    }

    @Override
    public Children mul(boolean condition, SFunction<T, Object> column, Number value) {
        return condition ? mul(column,value) : typedThis;
    }

    @Override
    public Children mul(SFunction<T, Object> column, Number value) {
        return getBaseUpdateCompare(column,value);
    }

    @Override
    public Children mul(boolean condition, String column, Number value) {
        return condition ? mul(column,value) : typedThis;
    }

    @Override
    public Children mul(String column, Number value) {
        return getBaseUpdateCompare(column,value);
    }

    @Override
    public Children rename(boolean condition, String oldFieldName, String newFieldName) {
        return condition ? rename(oldFieldName,newFieldName) : typedThis;
    }

    @Override
    public Children rename(String oldFieldName, String newFieldName) {
        return getBaseUpdateCompare(new MutablePair<>(oldFieldName, newFieldName));
    }

    @Override
    public <O> Children rename(boolean condition, SFunction<O, Object> oldFieldName, String newFieldName) {
        return condition ? rename(oldFieldName,newFieldName) : typedThis;
    }

    @Override
    public <O> Children rename(SFunction<O, Object> oldFieldName, String newFieldName) {
        return getBaseUpdateCompare(new MutablePair<>(oldFieldName.getFieldNameLine(),newFieldName));
    }

    @Override
    public <O, N> Children rename(boolean condition, SFunction<O, Object> oldFieldName, SFunction<N, Object> newFieldName) {
        return condition ? rename(oldFieldName,newFieldName) : typedThis;
    }

    @Override
    public <O, N> Children rename(SFunction<O, Object> oldFieldName, SFunction<N, Object> newFieldName) {
        return getBaseUpdateCompare(new MutablePair<>(oldFieldName.getFieldNameLine(),newFieldName.getFieldNameLine()));
    }

    @SafeVarargs
    @Override
    public final Children unset(SFunction<T, Object>... columns) {
        return unset(Arrays.stream(columns).map(SFunction::getFieldNameLine).collect(Collectors.toList()));
    }

    @SafeVarargs
    @Override
    public final Children unset(boolean condition, SFunction<T, Object>... columns) {
        return condition ? unset(columns) : typedThis;
    }

    @Override
    public Children unset(String... columns) {
        return unset(Arrays.asList(columns));
    }

    @Override
    public Children unset(boolean condition, String... columns) {
        return condition ? unset(columns) : typedThis;
    }

    @Override
    public Children unset(List<String> columns) {
        return getBaseUpdateCompare(columns);
    }

    @Override
    public Children unset(boolean condition, List<String> columns) {
        return condition ? unset(columns) : typedThis;
    }

    @Override
    public Children addToSet(boolean condition, SFunction<T, Object> column, Object value, boolean each) {
        return condition ? addToSet(column,value,each) : typedThis;
    }

    @Override
    public Children addToSet(SFunction<T, Object> column, Object value, boolean each) {
        if (each && !ClassTypeUtil.isTargetClass(Collection.class,value.getClass())){
            throw new MongoPlusException("$each requires data of Collection type");
        }
        return getBaseUpdateCompare(column,value,each);
    }

    @Override
    public Children addToSet(boolean condition, SFunction<T, Object> column, List<?> value) {
        return condition ? addToSet(column,value) : typedThis;
    }

    @Override
    public Children addToSet(SFunction<T, Object> column, List<?> value) {
        return addToSet(column,value,true);
    }

    @Override
    public Children addToSet(boolean condition, String column, Object value, boolean each) {
        return condition ? addToSet(column,value,each) : typedThis;
    }

    @Override
    public Children addToSet(String column, Object value, boolean each) {
        if (each && !ClassTypeUtil.isTargetClass(Collection.class,value.getClass())){
            throw new MongoPlusException("$each requires data of Collection type");
        }
        return getBaseUpdateCompare(column,value,each);
    }

    @Override
    public Children addToSet(boolean condition, String column, List<?> value) {
        return condition ? addToSet(column,value) : typedThis;
    }

    @Override
    public Children addToSet(String column, List<?> value) {
        return addToSet(column,value,true);
    }

    @Override
    public Children pop(boolean condition, SFunction<T, Object> column, PopType popType) {
        return condition ? pop(column,popType) : typedThis;
    }

    @Override
    public Children pop(SFunction<T, Object> column, PopType popType) {
        return getBaseUpdateCompare(column,popType.getValue(),true);
    }

    @Override
    public Children pop(boolean condition, String column, PopType popType) {
        return condition ? pop(column,popType) : typedThis;
    }

    @Override
    public Children pop(String column, PopType popType) {
        return getBaseUpdateCompare(column,popType.getValue(),false);
    }

    @Override
    public Children pull(boolean condition, SFunction<T, Object> column, Object value) {
        return condition ? pull(column,value) : typedThis;
    }

    @Override
    public Children pull(SFunction<T, Object> column, Object value) {
        return getBaseUpdateCompare(column,value,false);
    }

    @Override
    public Children pull(boolean condition, QueryChainWrapper<?, ?> wrapper) {
        return condition ? pull(wrapper) : typedThis;
    }

    @Override
    public Children pull(boolean condition, SFunction<QueryChainWrapper<?, ?>, QueryChainWrapper<?, ?>> function) {
        return pull(condition,function.apply(new QueryWrapper<>()));
    }

    @Override
    public Children pull(QueryChainWrapper<?, ?> wrapper) {
        return getBaseUpdateCompare(wrapper,true);
    }

    @Override
    public Children pull(SFunction<QueryChainWrapper<?, ?>, QueryChainWrapper<?, ?>> function) {
        return pull(function.apply(new QueryWrapper<>()));
    }

    @Override
    public Children pull(boolean condition, String column, Object value) {
        return condition ? pull(column,value) : typedThis;
    }

    @Override
    public Children pull(String column, Object value) {
        return getBaseUpdateCompare(column,value,false);
    }

    @Override
    public Children pullAll(boolean condition, SFunction<T, Object> column, Collection<?> values) {
        return condition ? pullAll(column,values) : typedThis;
    }

    @Override
    public Children pullAll(SFunction<T, Object> column, Collection<?> values) {
        return getBaseUpdateCompare(column,values);
    }

    @Override
    public Children pullAll(boolean condition, SFunction<T, Object> column, Object... values) {
        return condition ? pullAll(column,values) : typedThis;
    }

    @Override
    public Children pullAll(SFunction<T, Object> column, Object... values) {
        return pullAll(column,Arrays.asList(values));
    }

    @Override
    public Children pullAll(boolean condition, String column, Collection<?> values) {
        return condition ? pullAll(column,values) : typedThis;
    }

    @Override
    public Children pullAll(String column, Collection<?> values) {
        return getBaseUpdateCompare(column,values);
    }

    @Override
    public Children pullAll(boolean condition, String column, Object... values) {
        return condition ? pullAll(column,values) : typedThis;
    }

    @Override
    public Children pullAll(String column, Object... values) {
        return pullAll(column,Arrays.asList(values));
    }

    @SafeVarargs
    @Override
    public final Children pullAll(boolean condition, MutablePair<String, Collection<?>>... pullAllPair) {
        return condition ? pullAll(pullAllPair) : typedThis;
    }

    @SafeVarargs
    @Override
    public final Children pullAll(MutablePair<String, Collection<?>>... pullAllPair) {
        Arrays.stream(pullAllPair).forEach(pair -> pullAll(pair.getLeft(),pair.getValue()));
        return typedThis;
    }

    @Override
    public Children updateCustom(Bson bson) {
        updateBson.add(bson);
        return typedThis;
    }

    private Children getBaseUpdateCompare(SFunction<T, Object> column, Object value,Object extraValue){
        updateCompareList.add(new CompareCondition(Thread.currentThread().getStackTrace()[2].getMethodName(),column.getFieldNameLine(),value,column.getImplClass(),column.getField(),extraValue));
        return typedThis;
    }

    private Children getBaseUpdateCompare(SFunction<T, Object> column, Object value){
        updateCompareList.add(new CompareCondition(Thread.currentThread().getStackTrace()[2].getMethodName(),column.getFieldNameLine(),value,column.getImplClass(),column.getField()));
        return typedThis;
    }

    private Children getBaseUpdateCompare(Object value){
        String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        updateCompareList.add(new CompareCondition(methodName,methodName,value,Object.class,null));
        return typedThis;
    }

    private Children getBaseUpdateCompare(Object value,Object extraValue){
        String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        updateCompareList.add(new CompareCondition(methodName,methodName,value,Object.class,null,extraValue));
        return typedThis;
    }

    private Children getBaseUpdateCompare(String column, Object value){
        updateCompareList.add(new CompareCondition(Thread.currentThread().getStackTrace()[2].getMethodName(),column,value,Object.class,null));
        return typedThis;
    }

    private Children getBaseUpdateCompare(String column, Object value,Object extraValue){
        updateCompareList.add(new CompareCondition(Thread.currentThread().getStackTrace()[2].getMethodName(),column,value,Object.class,null,extraValue));
        return typedThis;
    }

    @Override
    public BaseConditionResult buildCondition(Condition condition) {
        return condition.queryCondition(this);
    }
}
