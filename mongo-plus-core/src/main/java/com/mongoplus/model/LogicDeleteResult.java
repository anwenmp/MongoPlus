package com.mongoplus.model;

import com.mongoplus.enums.LogicDataType;
import com.mongoplus.logging.Log;
import com.mongoplus.logging.LogFactory;
import org.bson.*;

import java.util.Objects;

/**
 * 逻辑删除信息
 *
 * @author loser
 */
public class LogicDeleteResult {

    Log log = LogFactory.getLog(LogicDeleteResult.class);

    /**
     * 逻辑删除指定的列
     */
    private String column;

    /**
     * 逻辑删除全局值（默认 1、表示已删除）
     */
    private String logicDeleteValue = "1";

    /**
     * 逻辑未删除全局值（默认 0、表示未删除）
     */
    private String logicNotDeleteValue = "0";

    /**
     * 逻辑删除数据类型（默认 String）
     */
    private LogicDataType logicDataType = LogicDataType.DEFAULT;

    public LogicDataType getLogicDataType() {
        return logicDataType;
    }

    public void setLogicDataType(LogicDataType logicDataType) {
        this.logicDataType = logicDataType;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public Object getLogicDeleteValue() {
        return getLogicValue(this.logicDeleteValue);
    }

    public void setLogicDeleteValue(String logicDeleteValue) {
        this.logicDeleteValue = logicDeleteValue;
    }

    public Object getLogicNotDeleteValue() {
        return getLogicValue(this.logicNotDeleteValue);
    }

    public BsonValue getLogicDeleteBsonValue() {
        return getLogicBsonValue(getLogicDeleteValue());
    }

    public BsonValue getLogicNotDeleteBsonValue() {
        return getLogicBsonValue(getLogicNotDeleteValue());
    }

    public BsonValue getLogicBsonValue(Object value) {
        if (value instanceof String) {
            return new BsonString((String) value);
        } else if (value instanceof Integer) {
            return new BsonInt32((Integer) value);
        } else if (value instanceof Long) {
            return new BsonInt64((Long) value);
        } else if (value instanceof Boolean) {
            return new BsonBoolean((Boolean) value);
        }
        log.debug("logicDeleteValue is not support type: " + value.getClass());
        return new BsonString(value.toString());
    }

    @SuppressWarnings("unchecked")
    <T> T getLogicValue(String value) {
        T result = (T) value;
        if (this.logicDataType == LogicDataType.INTEGER) {
            result = (T) Integer.valueOf(value);
        } else if (this.logicDataType == LogicDataType.BOOLEAN) {
            Boolean bool = null;
            if (value.equals("0") || value.equals("1")) {
                bool = Boolean.valueOf(Objects.equals(value, "0") ? "false" : "true");
            }
            if (value.equals("true") || value.equals("false")){
                bool = Boolean.valueOf(value);
            }
            result = (T) bool;
        } else if (this.logicDataType == LogicDataType.LONG) {
            result = (T) Long.valueOf(value);
        }
        return result;
    }

    public void setLogicNotDeleteValue(String logicNotDeleteValue) {
        this.logicNotDeleteValue = logicNotDeleteValue;
    }
}
