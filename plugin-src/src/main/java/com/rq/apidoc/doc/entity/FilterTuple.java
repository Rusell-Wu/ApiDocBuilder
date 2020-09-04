package com.rq.apidoc.doc.entity;

public class FilterTuple<A,B> {
    private A filterJson;
    private B fieldComment;

    public static <A,B> FilterTuple<A,B> with(A filterJson,B fieldComment){
        FilterTuple <A,B> filterTuple=new FilterTuple<>();
        filterTuple.setFilterJson(filterJson);
        filterTuple.setFieldComment(fieldComment);
        return filterTuple;
    }

    public A getFilterJson() {
        return filterJson;
    }

    public void setFilterJson(A filterJson) {
        this.filterJson = filterJson;
    }

    public B getFieldComment() {
        return fieldComment;
    }

    public void setFieldComment(B fieldComment) {
        this.fieldComment = fieldComment;
    }
}
