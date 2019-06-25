package com.tskj.classtable.search.condition;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tskj.classtable.search.consts.OperationType;
import com.tskj.classtable.search.consts.RelationType;

/**
 * @author LeonSu
 */
public class SearchCondition {
    private String fieldName;
    private OperationType operation;
    private String value;

    /**
     * 搜索条件之前的关系，and：与，or：或
     */
    private RelationType relation;

    public String getFieldName() {
        return fieldName;
    }

    protected void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public int getOperation() {
        return operation.ordinal();
    }

    protected void setOperation(int operation) {
        this.operation = OperationType.values()[operation];
    }

    public String getValue() {
        return value;
    }

    protected void setValue(String value) {
        this.value = value;
    }

    public int getRelation() {
        return relation.ordinal();
    }

    protected void setRelation(int relation) {
        this.relation = RelationType.values()[relation + 1];
    }

    protected String getString() throws Exception {
        String str = "";
        boolean success = true;
        switch (operation) {
            case LESS:
                str = "<'";
                break;
            case NOT_LESS:
                str = ">='";
                break;
            case EQUAL:
                str = "='";
                break;
            case NOT_EQUAL:
                str = "<>'";
                break;
            case GREATER:
                str = ">'";
                break;
            case NOT_GREATER:
                str = "<='";
                break;
            case LIKE:
                str = " like '%";
                value += "%";
                break;
            case NOT_LIKE:
                str = " not like '%";
                value += "%";
                break;
            default:
                success = false;
                break;
        }
        String rel = "";
        switch (relation) {
            case AND:
                rel = " and ";
                break;
            case OR:
                rel = " or ";
                break;
            default:
        }
        if (!success) {
            throw new Exception("约束不正确");
        }

        str = fieldName + str + value + "'" + rel;
        return str;
    }

    public String getString(JSONArray jsonArray) throws Exception {
        String whereStr = "";
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject json = jsonArray.getJSONObject(i);
            setFieldName(json.getString("fieldName"));
            setOperation(json.getInteger("operation"));
            setValue(json.getString("value"));
            if (i == jsonArray.size() - 1) {
                setRelation(-1);
            } else {
                setRelation(json.getInteger("relation"));
            }
            whereStr += getString();
        }
        return whereStr;
    }
}
