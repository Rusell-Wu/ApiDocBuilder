package com.rq.apidoc.doc.entity;

import java.util.ArrayList;
import java.util.List;

public class Module {

    private String name;
    private String typeVariableName;
    private List<Api> apiList=new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Api> getApiList() {
        return apiList;
    }

    public void setApiList(List<Api> apiList) {
        this.apiList = apiList;
    }

    public String getTypeVariableName() {
        return typeVariableName;
    }

    public void setTypeVariableName(String typeVariableName) {
        this.typeVariableName = typeVariableName;
    }
}
