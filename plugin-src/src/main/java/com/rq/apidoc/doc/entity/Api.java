package com.rq.apidoc.doc.entity;

import java.util.ArrayList;
import java.util.List;

public class Api {

    private String name;
    private String path;
    private String param;
    private List<Param> paramList=new ArrayList<>();
    private List<Result> resultList=new ArrayList<>();
    private String result="{\n" +
            "  \"code\": 200,\n" +
            "  \"data\": {}\n" +
            "}";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public List<Param> getParamList() {
        return paramList;
    }

    public void setParamList(List<Param> paramList) {
        this.paramList = paramList;
    }

    public List<Result> getResultList() {
        return resultList;
    }

    public void setResultList(List<Result> resultList) {
        this.resultList = resultList;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
