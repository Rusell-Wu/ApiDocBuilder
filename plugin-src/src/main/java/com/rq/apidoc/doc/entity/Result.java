package com.rq.apidoc.doc.entity;

import com.alibaba.fastjson.annotation.JSONField;

public class Result {
    private String fieldName;
    private String comment;
    private String type;

    @JSONField(serialize = false)
    private String typeName;

    public Result(){

    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public Result clone(int floor){
        Result result=new Result();
        StringBuilder sb=new StringBuilder();
        for(int i=0;i<floor;i++){
            sb.append("&emsp;");
        }
        sb.append("┃━ ");
        sb.append(fieldName);
        result.setFieldName(sb.toString());
        result.setTypeName(typeName);
        result.setType(type);
        result.setComment(comment);
        return result;
    }
}
