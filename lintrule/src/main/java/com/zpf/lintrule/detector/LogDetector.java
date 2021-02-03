package com.zpf.lintrule.detector;

import com.android.tools.lint.client.api.UElementHandler;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.intellij.psi.PsiMethod;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.uast.UCallExpression;
import org.jetbrains.uast.UElement;
import org.jetbrains.uast.util.UastExpressionUtils;

import java.util.Collections;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class LogDetector extends Detector implements Detector.UastScanner{

    public static final Issue ISSUE = Issue.create(
            "LogUsage",
            "避免使用android.util.Log",
            "请使用统一的日志工具类!",
            Category.CORRECTNESS,
            6,
            Severity.ERROR,
            new Implementation(LogDetector.class, Scope.JAVA_FILE_SCOPE)
    );

    @Nullable
    @Override
    public List<Class<? extends UElement>> getApplicableUastTypes() {
        return Collections.singletonList(UCallExpression.class);
    }

    @Nullable
    @Override
    public UElementHandler createUastHandler(@NotNull JavaContext context) {
        return new LogHandler(context);
    }

    static class LogHandler extends UElementHandler {

        private final JavaContext context;

        LogHandler(JavaContext context) {
            this.context = context;
        }

        @Override
        public void visitCallExpression(@NotNull UCallExpression node) {
            if (!UastExpressionUtils.isMethodCall(node)) return;
            if (node.getReceiver() != null
                    && node.getMethodName() != null) {
                String methodName = node.getMethodName();
                if (methodName.equals("i")
                        || methodName.equals("d")
                        || methodName.equals("e")
                        || methodName.equals("v")
                        || methodName.equals("w")
                        || methodName.equals("wtf")) {
                    PsiMethod method = node.resolve();
                    if (context.getEvaluator().isMemberInClass(method, "android.util.Log")) {
                        context.report(ISSUE,
                                node,
                                context.getLocation(node),
                                "\u21E2 避免使用android.util.Log"
                        );
                    }
                }
            }
        }
    }
}