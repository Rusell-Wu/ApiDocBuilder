package com.rq.apidoc.doc;

import com.alibaba.fastjson.JSON;
import com.google.protobuf.ServiceException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.rq.apidoc.doc.entity.Api;
import com.rq.apidoc.doc.entity.Module;
import com.rq.apidoc.doc.exception.IllegalDocException;
import com.rq.apidoc.doc.util.DocUtil;
import com.sun.javadoc.*;

import java.util.ArrayList;
import java.util.List;

public class ApiBuilder {
    public static List<Module> create(String servicePackage, String classpath, String sourcePath, ProgressIndicator progressIndicator) throws IllegalDocException {
        RootDoc rootDoc = JavadocReader.readDocs(servicePackage,classpath,sourcePath);
        String modelPath=sourcePath+"/"+servicePackage.replaceAll("\\.","/").replace("service","model");
        DocUtil docUtil=new DocUtil(modelPath,classpath);
        List<Module> moduleList=new ArrayList<>();
        ClassDoc[] classes = rootDoc.classes();
        for(ClassDoc classDoc:classes){
            if(progressIndicator.isCanceled()){
                break;
            }
            Module module=new Module();
            module.setName(classDoc.commentText());
            String typeName = classDoc.typeName();
            module.setTypeVariableName(typeName.substring(0,typeName.lastIndexOf("Service")));
            Type superclassType = classDoc.superclassType();
            if(superclassType!=null) {
                ParameterizedType parameterizedType = superclassType.asParameterizedType();
                if(parameterizedType!=null) {
                    Type[] types = parameterizedType.typeArguments();
                    if (types != null) {
                        for (Type t : types) {
                            module.setName(t.asClassDoc().commentText());
                        }
                    }
                    MethodDoc[] methods = classDoc.methods();
                    for (MethodDoc method : methods) {
                        AnnotationDesc[] annotations = method.annotations();
                        Api api =null;
                        try {
                            for (AnnotationDesc annotation : annotations) {
                                if (annotation.toString().contains("RestMethod")) {
                                    api=new Api();
                                    api.setPath("/api/" + typeName.substring(0, 1).toLowerCase() + typeName.substring(1,typeName.lastIndexOf("Service")) + "/" + method.name());
                                    api.setName(method.commentText());
                                    ParamTag[] paramTags = method.paramTags();
                                    Parameter[] parameters = method.parameters();
                                    api.setParam(docUtil.getParamJson(parameters,paramTags));
                                    api.setParamList(docUtil.getParamList(parameters, paramTags));
                                    break;
                                }
                            }
                            for (AnnotationDesc annotation : annotations) {
                                if (annotation.toString().contains("Json")) {
                                    if (api != null) {
                                        docUtil.createResult(method.returnType(), annotation, api);
                                    }
                                    break;
                                }
                            }
                            if(api!=null){
                                module.getApiList().add(api);
                            }
                        }catch (Exception e){
                            if(api!=null){
                                throw new IllegalDocException("接口“"+api.getPath()+"”Java doc注释有误！");
                            }
                            throw new IllegalDocException("未知错误,位置："+module.getName()+"-"+method.name());
                        }
                    }
                    moduleList.add(module);
                }
            }
        }
//        System.out.println(JSON.toJSONString(moduleList));
        return moduleList;
    }
}
