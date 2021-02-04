
package com.zpf.lintrule.detector;

import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.client.api.UElementHandler;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.uast.UClass;
import org.jetbrains.uast.UElement;
import org.jetbrains.uast.UMethod;
import org.jetbrains.uast.visitor.AbstractUastVisitor;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class NamingConventionDetector extends Detector implements Detector.UastScanner {
    //定义命名规范规则
    public static final Issue ISSUE = Issue.create("NamingConventionWarning",
            "命名规范错误",
            "使用驼峰命名法，方法命名开头小写",
            Category.USABILITY,
            5,
            Severity.WARNING,
            new Implementation(NamingConventionDetector.class,
                    EnumSet.of(Scope.JAVA_FILE)));

    //返回我们所有感兴趣的类，即返回的类都被会检查
    @Nullable
    @Override
    public List<Class<? extends UElement>> getApplicableUastTypes() {
        return Collections.<Class<? extends UElement>>singletonList(UClass.class);
    }

    //重写该方法，创建自己的处理器
    @Nullable
    @Override
    public UElementHandler createUastHandler(@NotNull final JavaContext context) {
        return new UElementHandler() {
            @Override
            public void visitClass(@NotNull UClass node) {
                node.accept(new NamingConventionVisitor(context, node));
            }
        };
    }
    //定义一个继承自AbstractUastVisitor的访问器，用来处理感兴趣的问题
    public static class NamingConventionVisitor extends AbstractUastVisitor {

        JavaContext context;

        UClass uClass;

        public NamingConventionVisitor(JavaContext context, UClass uClass) {
            this.context = context;
            this.uClass = uClass;
        }

        @Override
        public boolean visitClass(@org.jetbrains.annotations.NotNull UClass node) {
            //获取当前类名
            char beginChar = node.getName().charAt(0);
            int code = beginChar;
            //如果类名不是大写字母，则触碰Issue，lint工具提示问题
            if (97 < code && code < 122) {
                context.report(ISSUE,context.getNameLocation(node),
                        "the  name of class must start with uppercase:" + node.getName());
                //返回true表示触碰规则，lint提示该问题；false则不触碰
                return true;
            }

            return super.visitClass(node);
        }

        @Override
        public boolean visitMethod(@NotNull UMethod node) {
            //当前方法不是构造方法
            if (!node.isConstructor()) {
                char beginChar = node.getName().charAt(0);
                int code = beginChar;
                //当前方法首字母是大写字母，则报Issue
                if (65 < code && code < 90) {
                    context.report(ISSUE, context.getLocation(node),
                            "the method must start with lowercase:" + node.getName());
                    //返回true表示触碰规则，lint提示该问题；false则不触碰
                    return true;
                }
            }
            return super.visitMethod(node);

        }

    }
}