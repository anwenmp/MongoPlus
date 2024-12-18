package com.mongoplus.conditions.interfaces;

import com.mongoplus.support.SFunction;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author JiaChaoYang
 **/
public class Projection {

    private String column;

    private Object value;

    public static <T> Projection chain(SFunction<T, Object> column, Boolean value) {
        return builder().column(column.getFieldNameLine()).value(value ? 1 : 0).build();
    }

    public static <T> Projection chain(SFunction<T, Object> column, Integer value) {
        return builder().column(column.getFieldNameLine()).value(value).build();
    }

    public static <T> Projection chain(SFunction<T, Object> column, Object value) {
        return builder().column(column.getFieldNameLine()).value(value).build();
    }

    public static <T> Projection chain(String column, Boolean value) {
        return builder().column(column).value(value ? 1 : 0).build();
    }

    public static <T> Projection chain(String column, Integer value) {
        return builder().column(column).value(value).build();
    }

    public static <T> Projection chain(String column, Object value) {
        return builder().column(column).value(value).build();
    }

    public static Projection display(String column){
        return builder().display(column).build();
    }

    public static <T> Projection display(SFunction<T,?> column){
        return builder().display(column).build();
    }

    public static Projection none(String column){
        return builder().none(column).build();
    }

    public static <T> Projection none(SFunction<T,?> column){
        return builder().none(column).build();
    }

    public static ProjectionBuilder builder() {
        return new ProjectionBuilder();
    }

    public static ProjectionBuilder builder(List<Projection> projectionList) {
        return new ProjectionBuilder(projectionList);
    }

    public String getColumn() {
        return this.column;
    }

    public Object getValue() {
        return this.value;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Projection(String column, Object value) {
        this.column = column;
        this.value = value;
    }

    public Projection() {
    }

    public static class ProjectionBuilder {
        private final List<Projection> projectionList;
        private String column;
        private Object value;

        ProjectionBuilder() {
            this.projectionList = new CopyOnWriteArrayList<>();
        }

        ProjectionBuilder(List<Projection> projectionList){
            this.projectionList = projectionList;
        }

        public ProjectionBuilder column(String column) {
            this.column = column;
            return this;
        }

        public <T> ProjectionBuilder column(SFunction<T,?> column) {
            this.column = column.getFieldNameLine();
            return this;
        }

        public ProjectionBuilder value(Object value) {
            this.value = value;
            return this;
        }

        public ProjectionBuilder display(String... column){
            for (String c : column) {
                this.projectionList.add(new Projection(c,1));
            }
            return this;
        }

        public ProjectionBuilder none(String... column){
            for (String c : column) {
                this.projectionList.add(new Projection(c,0));
            }
            return this;
        }

        @SafeVarargs
        public final <T> ProjectionBuilder display(SFunction<T, ?>... column){
            for (SFunction<T, ?> func : column) {
                this.projectionList.add(new Projection(func.getFieldNameLine(),1));
            }
            return this;
        }

        @SafeVarargs
        public final <T> ProjectionBuilder none(SFunction<T, ?>... column){
            for (SFunction<T, ?> func : column) {
                this.projectionList.add(new Projection(func.getFieldNameLine(),0));
            }
            return this;
        }

        public ProjectionBuilder projection(String column,Object value){
            this.projectionList.add(Projection.chain(column,value));
            this.column = column;
            this.value = value;
            return this;
        }

        public <T> ProjectionBuilder projection(SFunction<T,?> column,Object value){
            String fieldNameLine = column.getFieldNameLine();
            this.projectionList.add(Projection.chain(fieldNameLine,value));
            this.column = fieldNameLine;
            this.value = value;
            return this;
        }

        public Projection build() {
            return new Projection(this.column, this.value);
        }

        public List<Projection> buildList(){
            return this.projectionList;
        }

        public String toString() {
            return "Projection.ProjectionBuilder(column=" + this.column + ", value=" + this.value + ")";
        }
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof Projection)) {
            return false;
        } else {
            Projection other = (Projection)o;
            if (!other.canEqual(this)) {
                return false;
            } else {
                Object this$value = this.getValue();
                Object other$value = other.getValue();
                if (this$value == null) {
                    if (other$value != null) {
                        return false;
                    }
                } else if (!this$value.equals(other$value)) {
                    return false;
                }

                Object this$column = this.getColumn();
                Object other$column = other.getColumn();
                if (this$column == null) {
                    if (other$column != null) {
                        return false;
                    }
                } else if (!this$column.equals(other$column)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof Projection;
    }

    public int hashCode() {
        int result = 1;
        Object $value = this.getValue();
        result = result * 59 + ($value == null ? 43 : $value.hashCode());
        Object $column = this.getColumn();
        result = result * 59 + ($column == null ? 43 : $column.hashCode());
        return result;
    }

    public String toString() {
        return "Projection(column=" + this.getColumn() + ", value=" + this.getValue() + ")";
    }

}