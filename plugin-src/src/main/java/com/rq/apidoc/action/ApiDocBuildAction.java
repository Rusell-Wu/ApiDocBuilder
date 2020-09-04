package com.rq.apidoc.action;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.rq.apidoc.doc.ApiBuilder;
import com.rq.apidoc.doc.entity.Module;
import com.rq.apidoc.doc.exception.IllegalDocException;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ApiDocBuildAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        // TODO: insert com.rq.apidoc.action logic here
        //获取当前在操作的工程上下文

        final Project project = e.getData(PlatformDataKeys.PROJECT);
        final String projectName = project.getName();

        ProgressManager.getInstance().run(new Task.Backgroundable(project, "正在生成接口文档") {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                String servicePackage = PropertiesComponent.getInstance().getValue(projectName + ":servicePackage");
                String sourcePath = PropertiesComponent.getInstance().getValue(projectName + ":sourcePath");
                String jarPath = PropertiesComponent.getInstance().getValue(projectName + ":jarPath");

                if (servicePackage == null || sourcePath != null || jarPath != null) {

                    String buildJarPath = project.getBasePath() + "/build/libs";
                    File file = new File(buildJarPath);
                    if (!file.exists()) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                Messages.showMessageDialog(project, "请先执行gradle jar命令生成项目jar文件！", "生成文档失败", Messages.getInformationIcon());
                            }
                        });
                        return;
                    }
                    //搜索jar文件
                    List<File> jarFiles = findFile(file, projectName, ".jar");
                    if (jarFiles.isEmpty()) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                Messages.showMessageDialog(project, "请先执行gradle jar命令生成项目jar文件！", "生成文档失败", Messages.getInformationIcon());
                            }
                        });
                        return;
                    } else {
                        jarPath = jarFiles.get(0).getAbsolutePath();
                    }

                    //搜索启动类文件
                    List<File> files = searchFiles(new File(project.getBasePath() + "/src/main/java"), "Application.java");
                    if (files.isEmpty()) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                Messages.showMessageDialog(project, "未找到启动类文件：Application.java", "生成文档失败", Messages.getInformationIcon());
                            }
                        });
                        return;
                    } else {
                        servicePackage = getPackageName(files.get(0).getAbsolutePath().replace("Application.java", "service"));
                    }
                    sourcePath = project.getBasePath() + "/src/main/java";

                    PropertiesComponent.getInstance().setValue(projectName + ":servicePackage", servicePackage);
                    PropertiesComponent.getInstance().setValue(projectName + ":jarPath", jarPath);
                    PropertiesComponent.getInstance().setValue(projectName + ":sourcePath", sourcePath);
                }

                try {
                    List<Module> moduleList = ApiBuilder.create(servicePackage, jarPath, sourcePath,indicator);
                    if(indicator.isCanceled()){
                        return;
                    }
                    String jsonString = JSON.toJSONString(moduleList, SerializerFeature.DisableCircularReferenceDetect);
                    File apiJsonFile = new File(project.getBasePath() + "/build/libs/apiList.json");
                    if (apiJsonFile.exists()) {
                        apiJsonFile.delete();
                    }
                    apiJsonFile.createNewFile();
                    FileOutputStream out = new FileOutputStream(apiJsonFile);
                    out.write(jsonString.getBytes(StandardCharsets.UTF_8));
                    out.flush();
                    out.close();
                } catch (IllegalDocException exception) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            Messages.showMessageDialog(project, exception.getMessage(), "生成文档失败", Messages.getErrorIcon());
                        }
                    });
                } catch (IOException e1) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            Messages.showMessageDialog(project, e1.getMessage(), "生成文档失败", Messages.getErrorIcon());
                        }
                    });
                }
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        Messages.showMessageDialog(project, "生成文档成功", "提示", Messages.getInformationIcon());
                    }
                });
            }
        });

    }


    private List<File> searchFiles(File folder, final String fileName) {
        List<File> result = new ArrayList<File>();
        if (folder.isFile())
            result.add(folder);

        File[] subFolders = folder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                if (file.isDirectory()) {
                    return true;
                }
                if (file.getName().equals(fileName)) {
                    return true;
                }
                return false;
            }
        });

        if (subFolders != null) {
            for (File file : subFolders) {
                if (file.isFile()) {
                    // 如果是文件则将文件添加到结果列表中
                    result.add(file);
                    return result;
                } else {
                    // 如果是文件夹,则递归调用本方法,然后把所有的文件加到结果列表中
                    result.addAll(searchFiles(file, fileName));
                }
            }
        }
        return result;
    }

    //根据扩展名搜索
    private List<File> findFile(File folder,final String startWith, final String extName){
        List<File> result = new ArrayList<File>();
        if (folder.isFile())
            result.add(folder);

        File[] subFolders = folder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                if (file.isDirectory()) {
                    return true;
                }
                if (file.getName().endsWith(extName)&&file.getName().startsWith(startWith)) {
                    return true;
                }
                return false;
            }
        });

        if (subFolders != null) {
            for (File file : subFolders) {
                if (file.isFile()) {
                    // 如果是文件则将文件添加到结果列表中
                    result.add(file);
                    return result;
                } else {
                    // 如果是文件夹,则递归调用本方法,然后把所有的文件加到结果列表中
                    result.addAll(searchFiles(file, extName));
                }
            }
        }
        return result;
    }

    private String getPackageName(String applicationPath){
        int index = applicationPath.indexOf("java");
        applicationPath=applicationPath.substring(index+5);
        return applicationPath.replaceAll("\\\\",".");
    }
}
