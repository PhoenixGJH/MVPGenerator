package com.phoenix.plugin;

import com.intellij.compiler.actions.CompileProjectAction;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.groovy.lang.psi.GroovyElementVisitor;
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile;
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElementFactory;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.blocks.GrClosableBlock;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrApplicationStatement;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.path.GrMethodCallExpression;
import org.jetbrains.plugins.groovy.lang.psi.impl.GroovyPsiElementFactoryImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MVPGenerator extends AnAction {
    //存储的base的名称
    private static final String KEY_BASE_NAME = "key_base_name";

    private Project project;
    private String packageName = "";
    //Base所在的包名
    private String basePackageName = "";
    //Base所在的module名
    private String baseName = "app";
    private String author;
    private String className;
    private String currentPath;
    private boolean isFragment;
    private boolean isGenerateBase;
    private GeneratorDialog dialog;
    private String moduleName;

    private enum CodeType {
        Bean, Dao,
        Activity, Fragment,
        Contract,
        Presenter,
        Layout,
        BaseView,
        BasePresenter,
        BaseActivity,
        BaseMVPActivity,
        BaseFragment,
        BaseMVPFragment,
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        project = e.getData(PlatformDataKeys.PROJECT);
        getCurrentPath(e);
        getModuleName();
        packageName = getPackageName();
        initDialog(e);
        refreshProject(e);
    }

    /**
     * 获取当前的路径
     */
    private void getCurrentPath(AnActionEvent e) {
        VirtualFile file = CommonDataKeys.VIRTUAL_FILE.getData(e.getDataContext());
        if (file == null) {
            return;
        }
        currentPath = file.getPath();
    }

    /**
     * 刷新项目
     */
    private void refreshProject(AnActionEvent e) {
        project.getBaseDir().refresh(false, true);
    }

    /**
     * 初始化Dialog
     */
    private void initDialog(AnActionEvent e) {
        dialog = new GeneratorDialog((author1, className1, isFragment1, isGenerateBase1, baseName1) -> {
            this.author = author1;
            this.className = className1;
            this.isFragment = isFragment1;
            this.isGenerateBase = isGenerateBase1;
            this.baseName = baseName1;
            PropertiesComponent.getInstance().setValue(KEY_BASE_NAME, baseName1);
            basePackageName = getAppPackageName();
            createClassFiles();
//            findGradleFile();
            dialog.setVisible(false);
            Messages.showInfoMessage(project, "Create MVP success", "Title");
//            rebuildAction(e);
        });

        dialog.pack();
        String baseNameTmp = PropertiesComponent.getInstance().getValue(KEY_BASE_NAME);
        dialog.updateBaseName(baseNameTmp);
        dialog.setVisible(true);
    }

    /**
     * 生成类文件
     */
    private void createClassFiles() {
        if (isGenerateBase) {
            createBaseClassFile(CodeType.BaseActivity);
            createBaseClassFile(CodeType.BaseMVPActivity);
            createBaseClassFile(CodeType.BasePresenter);
            createBaseClassFile(CodeType.BaseView);
            createBaseClassFile(CodeType.BaseFragment);
            createBaseClassFile(CodeType.BaseMVPFragment);
        }

        createClassFile(CodeType.Bean);
        createClassFile(CodeType.Dao);
        if (isFragment) {
            createClassFile(CodeType.Fragment);
        } else {
            createClassFile(CodeType.Activity);
        }
        createClassFile(CodeType.Contract);
        createClassFile(CodeType.Presenter);
        createClassFile(CodeType.Layout);
    }

    /**
     * 生成Base类
     */
    private void createBaseClassFile(CodeType codeType) {
        String fileName;
        String content;
        String basePath = getAppPath() + "base/";
        switch (codeType) {
            case BaseView:
                if (!new File(basePath + "BaseView.java").exists()) {
                    fileName = "TemplateBaseView.txt";
                    content = readTemplateFile(fileName);
                    content = dealTemplateContent(content);
                    writeToFile(content, basePath, "BaseView.java");
                }
                break;
            case BasePresenter:
                if (!new File(basePath + "BasePresenter.java").exists()) {
                    fileName = "TemplateBasePresenter.txt";
                    content = readTemplateFile(fileName);
                    content = dealTemplateContent(content);
                    writeToFile(content, basePath, "BasePresenter.java");
                }
                break;
            case BaseActivity:
                if (!new File(basePath + "BaseActivity.java").exists()) {
                    fileName = "TemplateBaseActivity.txt";
                    content = readTemplateFile(fileName);
                    content = dealTemplateContent(content);
                    writeToFile(content, basePath, "BaseActivity.java");
                }
                break;
            case BaseMVPActivity:
                if (!new File(basePath + "BaseMVPActivity.java").exists()) {
                    fileName = "TemplateBaseMVPActivity.txt";
                    content = readTemplateFile(fileName);
                    content = dealTemplateContent(content);
                    writeToFile(content, basePath, "BaseMVPActivity.java");
                }
                break;
            case BaseFragment:
                if (!new File(basePath + "BaseFragment.java").exists()) {
                    fileName = "TemplateBaseFragment.txt";
                    content = readTemplateFile(fileName);
                    content = dealTemplateContent(content);
                    writeToFile(content, basePath, "BaseFragment.java");
                }
            case BaseMVPFragment:
                if (!new File(basePath + "BaseMVPFragment.java").exists()) {
                    fileName = "TemplateBaseMVPFragment.txt";
                    content = readTemplateFile(fileName);
                    content = dealTemplateContent(content);
                    writeToFile(content, basePath, "BaseMVPFragment.java");
                }
                break;
        }
    }

    private void createClassFile(CodeType codeType) {
        String fileName;
        String content;
        switch (codeType) {
            case Bean:
                if (!new File(currentPath + "/models/bean/" + className + "Bean.java").exists()) {
                    fileName = "TemplateBean.txt";
                    content = readTemplateFile(fileName);
                    content = dealTemplateContent(content);
                    writeToFile(content, currentPath + "/models/bean", className + "Bean.java");
                }
                break;
            case Dao:
                if (!new File(currentPath + "/models/dao/" + className + "Data.java").exists()) {
                    fileName = "TemplateDao.txt";
                    content = readTemplateFile(fileName);
                    content = dealTemplateContent(content);
                    writeToFile(content, currentPath + "/models/dao", className + "Data.java");
                }
                break;
            case Activity:
                if (!new File(currentPath + "/views/activities/" + className + "Activity.java").exists()) {
                    fileName = "TemplateActivity.txt";
                    content = readTemplateFile(fileName);
                    content = dealTemplateContent(content);
                    writeToFile(content, currentPath + "/views/activities", className + "Activity.java");
                }
                break;
            case Fragment:
                if (!new File(currentPath + "/views/fragments/" + className + "Fragment.java").exists()) {
                    fileName = "TemplateFragment.txt";
                    content = readTemplateFile(fileName);
                    content = dealTemplateContent(content);
                    writeToFile(content, currentPath + "/views/fragments", className + "Fragment.java");
                }
                break;
            case Contract:
                if (!new File(currentPath + "/contracts/" + className + "Contract.java").exists()) {
                    fileName = "TemplateContract.txt";
                    content = readTemplateFile(fileName);
                    content = dealTemplateContent(content);
                    writeToFile(content, currentPath + "/contracts", className + "Contract.java");
                }
                break;
            case Presenter:
                if (!new File(currentPath + "/presenters/" + className + "Presenter.java").exists()) {
                    fileName = "TemplatePresenter.txt";
                    content = readTemplateFile(fileName);
                    content = dealTemplateContent(content);
                    writeToFile(content, currentPath + "/presenters", className + "Presenter.java");
                }
                break;
            case Layout:
                String layoutPath = getLayoutPath();
                fileName = "TemplateLayout.txt";
                content = readTemplateFile(fileName);
                if (isFragment) {
                    if (!new File(layoutPath + "/fragment_" + className.toLowerCase() + ".xml").exists()) {
                        writeToFile(content, layoutPath, "fragment_" + className.toLowerCase() + ".xml");
                    }
                } else {
                    if (!new File(layoutPath + "/activity_" + className.toLowerCase() + ".xml").exists()) {
                        writeToFile(content, layoutPath, "activity_" + className.toLowerCase() + ".xml");
                    }
                }
                break;
        }
    }

    /**
     * 生成包文件路径
     */
    private String getAppPath() {
        String packagePath = basePackageName.replace(".", "/");
        return project.getBasePath() + "/" + baseName + "/src/main/java/" + packagePath + "/";
    }

    /**
     * 获取ModuleName
     */
    private void getModuleName() {
        String basePath = project.getBasePath();
        if (currentPath.contains(basePath)) {
            String[] split = currentPath.split(basePath + "/");
            moduleName = split[1].substring(0, split[1].indexOf("/"));
        }
    }

    /**
     * 获取布局文件的路径
     */
    private String getLayoutPath() {
        String basePath = project.getBasePath();
        return basePath + "/" + moduleName + "/src/main/res/layout";
    }

    /**
     * 替换模板中字符
     */
    private String dealTemplateContent(String content) {
        if (content.contains("activity_$name")) {
            content = content.replace("activity_$name", "activity_" + className.toLowerCase());
        }
        if (content.contains("fragment_$name")) {
            content = content.replace("fragment_$name", "fragment_" + className.toLowerCase());
        }
        content = content.replace("$name", className);
        if (content.contains("$packageName")) {
            content = content.replace("$packageName", packageName);
        }
        if (content.contains("$basePackageName")) {
            content = content.replace("$basePackageName", basePackageName);
        }
        content = content.replace("$author", author);
        content = content.replace("$date", getDate());
        return content;
    }

    /**
     * 获取当前时间
     */
    private String getDate() {
        Date currentTime = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        return format.format(currentTime);
    }

    /**
     * 读取模板文件中的字符内容
     */
    private String readTemplateFile(String fileName) {
        InputStream in = getClass().getResourceAsStream("/com/phoenix/plugin/Template/" + fileName);
        String content = "";
        try {
            content = new String(readStream(in));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

    private byte[] readStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int len;
        while ((len = inputStream.read(buf)) != -1) {
            outputStream.write(buf, 0, len);
        }
        outputStream.close();
        inputStream.close();
        return outputStream.toByteArray();
    }

    private void writeToFile(String content, String classPath, String className) {
        File folder = new File(classPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        File file = new File(classPath + "/" + className);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            FileWriter writer = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bufferedWriter = new BufferedWriter(writer);
            bufferedWriter.write(content);
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从AndroidManifest.xml文件中获取当前module的包名
     */
    private String getPackageName() {
        String packageName = "";
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(project.getBasePath() + "/" + moduleName + "/src/main/AndroidManifest.xml");
            if (document == null) {
                onError("找不到AndroidManifest");
                return packageName;
            }
            NodeList nodeList = document.getElementsByTagName("manifest");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node item = nodeList.item(i);
                Element element = ((Element) item);
                packageName = element.getAttribute("package");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return packageName;
    }

    /**
     * 从AndroidManifest.xml文件中获取当前app的包名
     */
    private String getAppPackageName() {
        String packageName = "";
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(project.getBasePath() + "/" + baseName + "/src/main/AndroidManifest.xml");
            if (document == null) {
                onError("找不到AndroidManifest");
                return packageName;
            }
            NodeList nodeList = document.getElementsByTagName("manifest");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node item = nodeList.item(i);
                Element element = ((Element) item);
                packageName = element.getAttribute("package");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return packageName;
    }

    /**
     * 错误提示
     *
     * @param msg 提示信息
     */
    private void onError(String msg) {
        if (dialog != null) {
            dialog.setVisible(false);
        }
        NotificationGroup notify = new NotificationGroup("MvpGenerator", NotificationDisplayType.BALLOON, true);
        notify.createNotification("Error",
                msg,
                NotificationType.INFORMATION
                , null)
                .notify(project);
    }

    private String[] rebuildSelections = {"Rebuild", "Cancel"};


    /**
     * 找到build.gradle文件
     */
    private void findGradleFile() {
//        String gradlePath = currentPath + "/build.gradle";
        String gradlePath = project.getBasePath() + "/build.gradle";
        VirtualFile file = LocalFileSystem.getInstance().findFileByPath(gradlePath);
        if (file == null) {
            return;
        }

        GroovyPsiElementFactoryImpl factory = new GroovyPsiElementFactoryImpl(project, PsiManager.getInstance(project));
//        GroovyPsiElementFactory factory = GroovyPsiElementFactoryImpl.getInstance(project);
//        GroovyPsiElementFactory factory = GroovyPsiElementFactory.getInstance(project);
        String rxJava = "implementation 'io.reactivex.rxjava2:rxjava:2.2.6'";
        String rxAndroid = "implementation 'io.reactivex.rxjava2:rxandroid:2.1.0'";
        GrExpression expressionRxJava = factory.createExpressionFromText(rxJava);
        GrExpression expressionRxAndroid = factory.createExpressionFromText(rxAndroid);

        GroovyFile groovyFile = (GroovyFile) PsiManager.getInstance(project).findFile(file);
        groovyFile.accept(new GroovyElementVisitor() {
            @Override
            public void visitMethodCallExpression(@NotNull GrMethodCallExpression methodCallExpression) {
                super.visitMethodCallExpression(methodCallExpression);
                if (methodCallExpression.getInvokedExpression().getText().equals("dependencies")) {
                    GrClosableBlock[] closureArguments = methodCallExpression.getClosureArguments();
                    GrClosableBlock closureArgument = closureArguments[0];
                    PsiElement psiElement = closureArgument.addBefore(expressionRxJava, closureArgument.getRBrace());
                    closureArgument.addBefore(expressionRxAndroid, closureArgument.getRBrace());
                    ((GrApplicationStatement) psiElement).navigate(true);
                }
            }
        });
    }

    /**
     * 是否ReBuild对话框
     */
    private void rebuildAction(AnActionEvent e) {
        int selectResult = JOptionPane.showOptionDialog(null,
                "Create MVP success!\nDo you want to rebuild this project?",
                "MVPGenerator Plugin",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                rebuildSelections,
                null);
        if (selectResult == JOptionPane.YES_OPTION) {
            rebuild(e);
        }
    }

    /**
     * 对项目进行ReBuild
     */
    private void rebuild(AnActionEvent e) {
        new CompileProjectAction().actionPerformed(e);
    }
}
