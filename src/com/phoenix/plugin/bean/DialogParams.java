package com.phoenix.plugin.bean;

public class DialogParams {
    private String author;
    private String className;
    private boolean isFragment;
    private boolean isGenerateBase;
    private String baseName;
    private boolean isKotlin;

    public DialogParams(String author, String className, boolean isFragment, boolean isGenerateBase, String baseName, boolean isKotlin) {
        this.author = author;
        this.className = className;
        this.isFragment = isFragment;
        this.isGenerateBase = isGenerateBase;
        this.baseName = baseName;
        this.isKotlin = isKotlin;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public boolean isFragment() {
        return isFragment;
    }

    public void setFragment(boolean fragment) {
        isFragment = fragment;
    }

    public boolean isGenerateBase() {
        return isGenerateBase;
    }

    public void setGenerateBase(boolean generateBase) {
        isGenerateBase = generateBase;
    }

    public String getBaseName() {
        return baseName;
    }

    public void setBaseName(String baseName) {
        this.baseName = baseName;
    }

    public boolean isKotlin() {
        return isKotlin;
    }

    public void setKotlin(boolean kotlin) {
        isKotlin = kotlin;
    }
}
