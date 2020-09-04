package com.rq.apidoc.doc.exception;

public class IllegalDocException extends Exception{
    private String path;

    public IllegalDocException(String path){
        this.path=path;
    }

    @Override
    public String getMessage() {
        return "接口“"+path+"”Java doc注释有误！";
    }
}
