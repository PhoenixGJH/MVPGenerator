package com.phoenix.plugin;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.actions.CodeInsightAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.groovy.lang.psi.GroovyElementVisitor;
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile;
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElementFactory;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.blocks.GrClosableBlock;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrApplicationStatement;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.path.GrMethodCallExpression;

public class AddDependencies extends CodeInsightAction {
    @NotNull
    @Override
    protected CodeInsightActionHandler getHandler() {
        return new AddDependenciesHandler();
    }

    public static class AddDependenciesHandler implements CodeInsightActionHandler {

        @Override
        public boolean startInWriteAction() {
            return true;
        }

        @Override
        public void invoke(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile psiFile) {
            String gradlePath = project.getBasePath() + "/build.gradle";
            VirtualFile file = LocalFileSystem.getInstance().findFileByPath(gradlePath);
            if (file == null) {
                return;
            }

            GroovyPsiElementFactory factory = GroovyPsiElementFactory.getInstance(project);
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
    }
}
