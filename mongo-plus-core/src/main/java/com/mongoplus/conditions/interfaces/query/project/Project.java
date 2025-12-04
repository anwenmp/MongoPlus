package com.mongoplus.conditions.interfaces.query.project;

import com.mongoplus.conditions.interfaces.query.BaseQueryCondition;
import com.mongoplus.model.Projection;
import com.mongoplus.constant.SqlOperationConstant;
import com.mongoplus.enums.ProjectionEnum;
import com.mongoplus.support.SFunction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * project操作
 *
 * @author anwen
 */
@SuppressWarnings("unchecked")
public interface Project<T, Children> extends BaseQueryCondition<T, Children> {

    /**
     * 要显示哪写字段或者不显示哪些字段
     * @param projections Projection对象
     * @return Children
     * @author JiaChaoYang
     */
    default Children project(Projection... projections) {
        return project(Arrays.asList(projections));
    }

    /**
     * 要显示哪写字段或者不显示哪些字段
     * @param projectionList Projection集合
     * @return Children
     * @author JiaChaoYang
     */
    default Children project(List<Projection> projectionList) {
        return addCondition(projectionList);
    }

    /**
     * 显示哪些字段
     * @param columns 列名，字段名
     * @return Children
     * @author JiaChaoYang
     */
    default Children projectDisplay(SFunction<T,Object>... columns) {
        return projectDisplay(Arrays.stream(columns).map(SFunction::getFieldNameLine).toArray(String[]::new));
    }

    /**
     * 显示哪些字段
     * @param columns 列名、字段名
     * @return Children
     * @author JiaChaoYang
     */
    default Children projectDisplay(String... columns) {
        List<Projection> projectionList = new ArrayList<>();
        for (String column : columns) {
            Projection projection = Projection.builder()
                    .column(column)
                    .value(ProjectionEnum.DISPLAY.getValue())
                    .build();
            projectionList.add(projection);
        }
        return project(projectionList);
    }

    /**
     * 不显示哪些字段
     * @param column 列名、字段名
     * @return Children
     * @author JiaChaoYang
     */
    default Children projectNone(SFunction<T,Object>... column) {
        return projectNone(Arrays.stream(column).map(SFunction::getFieldNameLine).toArray(String[]::new));
    }

    /**
     * 不显示哪些字段
     * @param columns 列名、字段名
     * @return Children
     * @author JiaChaoYang
     */
    default Children projectNone(String... columns) {
        List<Projection> projectionList = new ArrayList<>();
        for (String column : columns) {
            Projection projection = Projection.builder()
                    .column(column)
                    .value(ProjectionEnum.NONE.getValue())
                    .build();
            projectionList.add(projection);
        }
        return project(projectionList);
    }

    /**
     * 要显示哪写字段或者不显示哪些字段
     * @param displayId 是否显示_id
     * @param projections Projection对象
     * @return Children
     * @author JiaChaoYang
     */
    default Children project(boolean displayId,Projection... projections) {
        project(projections);
        return displayId ? typeThis() : project(
                Projection.builder()
                        .column(SqlOperationConstant._ID)
                        .value(ProjectionEnum.NONE.getValue())
                        .build()
        );
    }

    /**
     * 显示哪些字段
     * @param displayId 是否显示_id
     * @param columns 列名，字段名
     * @return Children
     * @author JiaChaoYang
     */
    default Children projectDisplay(boolean displayId,SFunction<T,Object>... columns) {
        return projectDisplay(displayId,Arrays.stream(columns).map(SFunction::getFieldNameLine).toArray(String[]::new));
    }

    /**
     * 显示哪些字段
     * @param displayId 是否显示_id
     * @param columns 列名、字段名
     * @return Children
     * @author JiaChaoYang
     */
    default Children projectDisplay(boolean displayId,String... columns) {
        List<Projection> projectionList = new ArrayList<>();
        for (String column : columns) {
            Projection projection = Projection.builder()
                    .column(column)
                    .value(ProjectionEnum.DISPLAY.getValue())
                    .build();
            projectionList.add(projection);
        }
        if (!displayId){
            projectionList.add(Projection.builder()
                    .column(SqlOperationConstant._ID)
                    .value(ProjectionEnum.NONE.getValue())
                    .build());
        }
        return project(projectionList);
    }

    /**
     * 不显示哪些字段
     * @param displayId 是否显示_id
     * @param columns 列名、字段名
     * @return Children
     * @author JiaChaoYang
     */
    default Children projectNone(boolean displayId,SFunction<T,Object>... columns) {
        return projectNone(displayId,Arrays.stream(columns).map(SFunction::getFieldNameLine).toArray(String[]::new));
    }

    /**
     * 不显示哪些字段
     * @param displayId 是否显示_id
     * @param column 列名、字段名
     * @return Children
     * @author JiaChaoYang
     */
    default Children projectNone(boolean displayId,String... column) {
        List<Projection> projectionList = new ArrayList<>();
        for (String columnName : column) {
            Projection projection = Projection.builder()
                    .column(columnName)
                    .value(ProjectionEnum.NONE.getValue())
                    .build();
            projectionList.add(projection);
        }
        if (!displayId){
            projectionList.add(Projection.builder()
                    .column(SqlOperationConstant._ID)
                    .value(ProjectionEnum.NONE.getValue())
                    .build());
        }
        return project(projectionList);
    }

}
