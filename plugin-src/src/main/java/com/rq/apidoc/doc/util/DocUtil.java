package com.rq.apidoc.doc.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.rq.apidoc.doc.ExtClassDoc;
import com.rq.apidoc.doc.JavadocReader;
import com.rq.apidoc.doc.entity.Api;
import com.rq.apidoc.doc.entity.FilterTuple;
import com.rq.apidoc.doc.entity.Param;
import com.rq.apidoc.doc.entity.Result;
import com.sun.javadoc.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DocUtil {

    private String sourcePath;
    private String classPath;

    public DocUtil(String sourcePath,String classPath){
        this.sourcePath=sourcePath;
        this.classPath=classPath;
    }

    public String getParamJson(Parameter[] parameters,ParamTag[] paramTags){
        Map<String,Type> parameterMap=new HashMap<>();
        for (Parameter parameter : parameters) {
            parameterMap.put(parameter.name(),parameter.type());
        }
        JSONObject result=new JSONObject();
        for (ParamTag paramTag : paramTags) {
            String comment = paramTag.parameterComment();
            Type type = parameterMap.get(paramTag.parameterName());
            boolean isList=false;
            if(type.typeName().equals("List")){
                ParameterizedType parameterizedType = type.asParameterizedType();
                Type[] types = parameterizedType.typeArguments();
                type=types[0];
                isList=true;
            }
            if(comment.contains("required:")||comment.contains("optional:")){
                JSONObject objArg=new JSONObject();
                if(comment.contains("required:")){
                    String required = comment.replace("required:","");
                    int i = required.indexOf("optional:");
                    if(i>0) {
                        required = required.substring(0, i);
                    }
                    String[] fields = required.split(",");
                    for (String field : fields) {
                        String trim = field.trim();
                        if(trim.length()>0) {
                            if(trim.contains(".")){
                                JSONObject obj=new JSONObject();
                                String[] split = trim.split("\\.");
                                if(objArg.containsKey(split[0])){
                                    Object o = objArg.get(split[0]);
                                    if(o instanceof JSONArray){
                                        o=((JSONArray) o).getJSONObject(0);
                                    }
                                    obj= (JSONObject) o;
                                }
                                String rootKey=split[0];
                                JSONObject lastPath=obj;
                                for(int j=1;j<split.length;j++){
                                    if(j!=split.length-1){
                                        JSONObject child = new JSONObject();
                                        if(lastPath.containsKey(split[j])) {
                                            child= (JSONObject) lastPath.get(split[j]);
                                        }else{
                                            lastPath.put(split[j],child);
                                        }
                                        lastPath=child;
                                    }else{
                                        lastPath.put(split[j],"");
                                    }
                                }
                                Type childType = getFieldType(type, split[0]);
                                if(getListType(childType)!=null){
                                    JSONArray jsonArray=new JSONArray();
                                    jsonArray.add(obj);
                                    objArg.put(split[0],jsonArray);
                                }else{
                                    objArg.put(split[0],obj);
                                }
                            }else {
                                Type childType = getFieldType(type, trim);
                                if(childType!=null&&isCustomClass(childType)){
                                    Object customClassJson = getCustomClassJson(childType, null);
                                    objArg.put(trim, customClassJson);
                                }else {
                                    objArg.put(trim, "");
                                }
                            }
                        }
                    }
                }
                if(comment.contains("optional:")){
                    int i = comment.indexOf("optional:");
                    String optional = comment.substring(i).replace("optional:","");
                    String[] fields = optional.split(",");
                    for (String field : fields) {
                        String trim = field.trim();
                        if(trim.length()>0) {
                            if(trim.contains(".")){
                                JSONObject obj=new JSONObject();
                                String[] split = trim.split("\\.");
                                if(objArg.containsKey(split[0])){
                                    Object o = objArg.get(split[0]);
                                    if(o instanceof JSONArray){
                                        o=((JSONArray) o).getJSONObject(0);
                                    }
                                    obj= (JSONObject) o;
                                }
                                String rootKey=split[0];
                                JSONObject lastPath=obj;
                                for(int j=1;j<split.length;j++){
                                    if(j!=split.length-1){
                                        JSONObject child = new JSONObject();
                                        if(lastPath.containsKey(split[j])) {
                                            child= (JSONObject) lastPath.get(split[j]);
                                        }else{
                                            lastPath.put(split[j],child);
                                        }
                                        lastPath=child;
                                    }else{
                                        lastPath.put(split[j],"");
                                    }
                                }
                                Type childType = getFieldType(type, split[0]);
                                if(getListType(childType)!=null){
                                    JSONArray jsonArray=new JSONArray();
                                    jsonArray.add(obj);
                                    objArg.put(split[0],jsonArray);
                                }else{
                                    objArg.put(split[0],obj);
                                }
                            }else {
                                Type childType = getFieldType(type, trim);
                                if(childType!=null&&isCustomClass(childType)){
                                    Object customClassJson = getCustomClassJson(childType, null);
                                    objArg.put(trim, customClassJson);
                                }else {
                                    objArg.put(trim, "");
                                }
                            }
                        }
                    }
                }
                if(isList) {
                    JSONArray jsonArray = new JSONArray();
                    jsonArray.add(objArg);
                    result.put(paramTag.parameterName(), jsonArray);
                }else{
                    result.put(paramTag.parameterName(), objArg);
                }
            }else{
                if(isList) {
                    result.put(paramTag.parameterName(), new JSONArray());
                }else{
                    result.put(paramTag.parameterName(),"");
                }
            }
        }
        return result.toJSONString();
    }


    public  List<Param> getParamList(Parameter[] parameters,ParamTag[] paramTags){
        Map<String,Type> parameterMap=new HashMap<>();
        List<Param> resultList=new ArrayList<>();
        for (Parameter parameter : parameters) {
            parameterMap.put(parameter.name(),parameter.type());
        }
        Set<String> fieldSet=new HashSet<>();
        for (ParamTag paramTag : paramTags) {
            String comment = paramTag.parameterComment();
            if(comment.equals("required:appUser,isPushRetreatPrice,goodsList")){
                System.out.println();
            }
            if(comment.contains("required:")||comment.contains("optional:")){
                //自定义类型
                Type type = parameterMap.get(paramTag.parameterName());
                if(type.typeName().equals("List")){
                    ParameterizedType parameterizedType = type.asParameterizedType();
                    Type[] types = parameterizedType.typeArguments();
                    type=types[0];
                }
                ClassDoc classDoc = type.asClassDoc();
                FieldDoc[] fields = classDoc.fields();
                int optionalIndex = comment.indexOf("optional:");

                for (FieldDoc field : fields) {
                    String fieldName = field.name();
                    if(!fieldSet.contains(fieldName)&&isParam(comment,fieldName)){
                        Param param = new Param();
                        param.setComment(field.commentText());
                        param.setFieldName(fieldName);
                        param.setType(getTypeString(field.type()));
                        param.setIsRequired(optionalIndex==-1||comment.indexOf(fieldName)<optionalIndex);
                        resultList.add(param);
                        fieldSet.add(fieldName);
                    }
                }
            }else{
                Type type = parameterMap.get(paramTag.parameterName());
                //基本类型
                Param param = new Param();
                param.setComment(comment);
                param.setFieldName(paramTag.parameterName());
                param.setIsRequired(true);
                param.setType(type.typeName());
                resultList.add(param);
                fieldSet.add(paramTag.parameterName());
            }
        }
        return resultList;
    }


    public  void createResult(Type returnType,AnnotationDesc annotation, Api api){
        if(returnType.typeName().equals("List")){
            returnType=getListType(returnType);
        }
        JSONObject resultJson=new JSONObject();
        List<Result> resultList=new ArrayList<>();
        Map<String, JSONObject> filterMap = new HashMap<>(0);
        Map<String,List<Result>> filterFieldComment = new HashMap<>(0);
        AnnotationDesc.ElementValuePair[] elementValuePairs = annotation.elementValues();
        for (AnnotationDesc.ElementValuePair elementValuePair : elementValuePairs) {
            String name = elementValuePair.element().name();
            if(name.equals("filter")){
                AnnotationValue value = elementValuePair.value();
                FilterTuple<Map<String, JSONObject>, Map<String, List<Result>>> filterTuple = getFilterObjectField(returnType,value);
                filterMap=filterTuple.getFilterJson();
                filterFieldComment=filterTuple.getFieldComment();
                break;
            }
        }
        boolean hasInclude=false;
        boolean hasExclude=false;
        String filterField="";
        for (AnnotationDesc.ElementValuePair elementValuePair : elementValuePairs) {
            String name = elementValuePair.element().name();
            if(name.equals("include")){
                hasInclude=true;
                filterField=elementValuePair.value().toString();
            }else if(name.equals("exclude")){
                hasExclude=true;
                filterField=elementValuePair.value().toString();
            }
        }

        ClassDoc classDoc = returnType.asClassDoc();
        FieldDoc[] fields = classDoc.fields();
        for (FieldDoc field : fields) {
            if (!field.isPrivate()) {
                continue;
            }
            if(isUnSerialize(field)){
                continue;
            }
            if (((hasInclude && filterField.contains("\"" + field.name() + "\"")) || (hasExclude && !filterField.contains("\"" + field.name() + "\""))) || (!hasExclude&&!hasInclude)) {
                Type type = field.type();
                boolean isList = false;
                if (type.simpleTypeName().equals("List")) {
                    type = getListType(type);
                    isList = true;
                }
                Boolean isCustomClass = isCustomClass(type);
                if (!isCustomClass) {
                    if(hasInclude||hasExclude) {
                        resultJson.put(field.name(), "");
                    }
                } else {
                    if (filterMap.containsKey(type.simpleTypeName())) {
                        if (isList) {
                            JSONArray array = new JSONArray();
                            JSONObject filterData = filterMap.get(type.simpleTypeName());
                            array.add(filterData.clone());
                            resultJson.put(field.name(), array);
                        } else {
                            resultJson.put(field.name(), filterMap.get(type.simpleTypeName()));
                        }
                    } else {
                        if(hasInclude||hasExclude) {
                            if (isList) {
                                resultJson.put(field.name(), new JSONArray());
                            } else {
                                resultJson.put(field.name(), new JSONObject());
                            }
                        }
                    }
                }
                if(resultJson.containsKey(field.name())) {
                    Result result = new Result();
                    result.setFieldName(field.name());
                    result.setComment(field.commentText());
                    result.setType(getTypeString(field.type()));
                    resultList.add(result);
                    if (isCustomClass) {
                        if (filterFieldComment.containsKey(type.simpleTypeName())) {
                            resultList.addAll(filterFieldComment.get(type.simpleTypeName()));
                        } else {
                            FieldDoc[] modelFields = getModelFields(type.simpleTypeName());
                            for (FieldDoc modelField : modelFields) {
                                if (modelField.isPrivate() && !isCustomClass(modelField.type())) {
                                    Result r = new Result();
                                    r.setType(getTypeString(modelField.type()));
                                    r.setFieldName("&emsp;┃━ " + modelField.name());
                                    r.setComment(modelField.commentText());
                                    resultList.add(r);
                                }
                            }
                        }
                    }
                }
            }
        }
        api.setResult(JSON.toJSONString(resultJson, SerializerFeature.DisableCircularReferenceDetect));
        api.setResultList(resultList);
    }

    private  List<Result> getObjectFieldCommentList(String typeName,Map<String,List<Result>> filterFieldComment,int floor){
        List<Result> resultList=new ArrayList<>();
        if(filterFieldComment.containsKey(typeName)){
            List<Result> commentList = filterFieldComment.get(typeName);
            for (Result result : commentList) {
                Result clone = result.clone(floor);
                resultList.add(clone);
                if(result.getType().equals("Object")){
                    if(result.getTypeName().equals(typeName)){
                        continue;
                    }
                    List<Result> childCommentList = getObjectFieldCommentList(result.getTypeName(), filterFieldComment,floor+1);
                    resultList.addAll(childCommentList);
                }
            }
        }
        return resultList;
    }

    public  void main(String []args){
//        Map<String, JSONObject> filterObjectField = getFilterObjectField("{@dev.paoding.longan.annotation.Filter(type=dev.paoding.qihuang.model.Doctor.class, include={\"id\", \"name\", \"avatar\", \"gender\", \"nation\", \"birthday\", \"jobStartYear\", \"clinicalDepartmentList\", \"practitionerTypeList\", \"clinics\", \"introduction\", \"skill\", \"prescriptionCount\", \"clinicMonthCount\", \"avgCommentScore\", \"isOnline\"}), @dev.paoding.longan.annotation.Filter(type=dev.paoding.qihuang.model.ClinicalDepartment.class, include=\"name\"), @dev.paoding.longan.annotation.Filter(type=dev.paoding.qihuang.model.PractitionerType.class, include=\"name\"), @dev.paoding.longan.annotation.Filter(type=dev.paoding.qihuang.model.Clinics.class, include=\"name\"), @dev.paoding.longan.annotation.Filter(type=dev.paoding.qihuang.model.DoctorConsultation.class, include=\"type\")}\n");
//        for (String s : filterObjectField.keySet()) {
//            System.out.println(s+":"+JSON.toJSONString(filterObjectField.get(s)));
//        }
//        String str="dev.paoding.longan.annotation.Filter(type=dev.paoding.qihuang.model.Province.class,include={\"id\",\"name\"}),";
//        Pattern pattern = Pattern.compile("Filter\\(type=(.+),include=(.+)\\)");
//        Matcher matcher = pattern.matcher(str);
//        while(matcher.find()){
//            System.out.println("Group 0:"+matcher.group(0));//得到第0组——整个匹配
//            System.out.println("Group 1:"+matcher.group(1));//得到第一组匹配——与(or)匹配的
//            System.out.println("Group 2:"+matcher.group(2));//得到第二组匹配——与(ld!)匹配的，组也就是子表达式
//            System.out.println("Start 0:"+matcher.start(0)+" End 0:"+matcher.end(0));//总匹配的索引
//            System.out.println("Start 1:"+matcher.start(1)+" End 1:"+matcher.end(1));//第一组匹配的索引
//            System.out.println("Start 2:"+matcher.start(2)+" End 2:"+matcher.end(2));//第二组匹配的索引
//            System.out.println(str.substring(matcher.start(0),matcher.end(1)));//从总匹配开始索引到第1组匹配的结束索引之间子串——Wor
//        }
    }


    private  FilterTuple<Map<String,JSONObject>,Map<String,List<Result>>> getFilterObjectField(Type container,AnnotationValue filterValue){
        String containerName = container.simpleTypeName();
        String valueStr = filterValue.toString();
        if(valueStr.startsWith("{")) {
            valueStr = valueStr.substring(1, valueStr.lastIndexOf("}"));
        }
        String[] split = valueStr.split("@");
        Map<String,JSONObject> filterMap=new HashMap<>(split.length);
        Map<String,List<Result>> fieldCommentMap=new HashMap<>(split.length);
        Pattern pattern=Pattern.compile("Filter\\(type=.+\\.(\\S+)\\.class,(.+)\\)");
        for (String s : split) {
            Matcher m=pattern.matcher(s);
            while(m.find()) {
                String modelClassName=m.group(1);

                JSONObject modelData=null;
                if(filterMap.containsKey(modelClassName)){
                    modelData=filterMap.get(modelClassName);
                }else{
                    modelData=new JSONObject();
                }

                FieldDoc[] modelFields = getModelFields(modelClassName);
                String filterFields = m.group(2).trim();
                boolean isInclude = filterFields.startsWith("include");
                List<Result> resultList=new LinkedList<>();
                for (FieldDoc modelField : modelFields) {
                    Type type = modelField.type();
                    if(!modelField.isPrivate()){
                        continue;
                    }
                    if(isUnSerialize(modelField)){
                        continue;
                    }
                    if( (isInclude && filterFields.contains("\""+modelField.name()+"\"")) || (!isInclude && !filterFields.contains("\""+modelField.name()+"\""))) {
                        if (!isCustomClass(type)) {
                            modelData.put(modelField.name(), getExampleValueOfType(type));
                        }else{
                            boolean isList=false;
                            if(type.simpleTypeName().contains("List")){
                                type=getListType(type);
                                isList=true;
                            }
                            if(!type.simpleTypeName().equals(containerName)) {
                                if (filterMap.containsKey(type.simpleTypeName())) {
                                    JSONObject modelFiledData = filterMap.get(type.simpleTypeName());
                                    if (isList) {
                                        JSONArray array = new JSONArray();
                                        array.add(modelFiledData);
                                        modelData.put(modelField.name(), array);
                                    } else {
                                        modelData.put(modelField.name(), modelFiledData);
                                    }
                                } else {
                                    JSONObject classData = new JSONObject();
                                    if (isList) {
                                        JSONArray array = new JSONArray();
                                        array.add(classData);
                                        modelData.put(modelField.name(), array);
                                    } else {
                                        modelData.put(modelField.name(), classData);
                                    }
                                    filterMap.put(type.simpleTypeName(), classData);
                                }
                            }
                        }
                        if(modelData.containsKey(modelField.name())){
                            Result result=new Result();
                            result.setType(getTypeString(type));
                            result.setComment(modelField.commentText());
                            result.setFieldName(modelField.name());
                            result.setTypeName(type.simpleTypeName());
                            resultList.add(result);
                        }

                    }
                }
                fieldCommentMap.put(modelClassName,resultList);
                filterMap.put(modelClassName,modelData);
            }
        }
        Map<String,List<Result>> newFieldCommentMap=new HashMap<>(filterMap.size());
        for (String typeName : filterMap.keySet()) {
            List<Result> resultList = getObjectFieldCommentList(typeName, fieldCommentMap,1);
            newFieldCommentMap.put(typeName,resultList);
        }
        return FilterTuple.with(filterMap,newFieldCommentMap);
    }


    private  FieldDoc[] getModelFields(String className){
        ExtClassDoc read = JavadocReader.read(sourcePath+"/"+className+".java", classPath);
        if(read==null){
            return new FieldDoc[0];
        }
        ClassDoc classDoc = read.classDoc;
        return classDoc.fields();
    }

    private  Boolean isParam(String tag,String fieldName){
        String pattern = "(((required|(^|.*\\s+)optional):)|(.*,))"+fieldName+"((,.*)|(\\..*)|(\\s.*)|$)";
        return Pattern.matches(pattern, tag);
    }


    private  Boolean isCustomClass(Type type){
        String primitiveType="void,boolean,byte,char,short,int,long,float,double,integer,string,localdate,localdatetime,map";
        if((primitiveType.contains(type.typeName().toLowerCase())||type.typeName().contains("java.lang")||type.typeName().contains("java.time"))&&!type.typeName().contains("List")){
            return false;
        }
        return true;
    }

    private  String getTypeString(Type type){
        if(isCustomClass(type)){
            return "Object";
        }else{
            if(type.typeName().contains("LocalDate")||type.typeName().contains("LocalDateTime")){
                return "String";
            }
            return type.typeName();
        }
    }

    private  Object getExampleValueOfType(Type type){
        if(isCustomClass(type)){
            return new JSONObject();
        }else{
            String typeName = type.typeName().toLowerCase();
            if(typeName.contains("localdate")){
                return "2020-08-29";
            }else if(typeName.contains("localdatetime")){
                return "2020-08-29 12:30:10";
            }else if(typeName.contains("map")) {
                return new JSONObject();
            }else{
                //void,boolean,byte,char,short,int,long,float,double,integer,string,localdate,localdatetime
                switch (typeName){
                    case "double":return 0.0;
                    case "float":return 0.0;
                    case "boolean":return true;
                    case "string":return "";
                }
            }
            return 0;
        }
    }

    private  Type getListType(Type type){
        if(type.typeName().equals("List")){
            ParameterizedType parameterizedType = type.asParameterizedType();
            Type[] types = parameterizedType.typeArguments();
            return types[0];
        }
        return null;
    }

    private  Type getFieldType(Type type,String fieldName){
        ClassDoc classDoc = type.asClassDoc();
        FieldDoc[] childField = classDoc.fields();
        Type fieldType=null;
        for (FieldDoc fieldDoc : childField) {
            if(fieldDoc.name().equals(fieldName)){
                fieldType=fieldDoc.type();
            }
        }
        return fieldType;
    }

    private  Object getCustomClassJson(Type type,Map<String,Object> handledClassMap){
        if(handledClassMap==null){
            handledClassMap=new HashMap<>();
        }
        Type listType = getListType(type);
        Boolean isList=false;
        if(listType!=null){
            type=listType;
            isList=true;
        }
        ClassDoc classDoc = type.asClassDoc();
        JSONObject rootClass=new JSONObject();
        FieldDoc[] rootClassFields = classDoc.fields();
        handledClassMap.put(classDoc.typeName(),rootClass);
        for (FieldDoc rootClassField : rootClassFields) {
            Type rootClassFieldType = rootClassField.type();
            if(rootClassField.isPrivate()) {
                if (isCustomClass(rootClassFieldType)){
                    String typeName=rootClassFieldType.typeName();
                    Type listType1 = getListType(rootClassFieldType);
                    if(listType1!=null){
                        typeName=listType1.typeName();
                    }
                    if (handledClassMap.containsKey(typeName)) {
                        JSONObject o = (JSONObject) handledClassMap.get(typeName);
                        rootClass.put(rootClassField.name(), o.clone());
                        continue;
                    }
                    Object json = getCustomClassJson(rootClassFieldType, handledClassMap);
                    if(json instanceof JSONArray){
                        json=((JSONArray) json).get(0);
                    }
                    handledClassMap.put(rootClassFieldType.typeName(), json);
                    rootClass.put(rootClassField.name(), json);
                }else{
                    rootClass.put(rootClassField.name(), "");
                }
            }
        }
        if(isList){
            JSONArray jsonArray=new JSONArray();
            jsonArray.add(rootClass);
            return jsonArray;
        }
        return rootClass;
    }

    private boolean isUnSerialize(FieldDoc fieldDoc){
        AnnotationDesc[] annotations = fieldDoc.annotations();
        for (AnnotationDesc annotation : annotations) {
            if(annotation.toString().contains("serialize=false")){
                return true;
            }
        }
        return false;
    }


}
