package com.zpf.plugin.lint

import com.android.build.gradle.AbstractAppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.tasks.LintBaseTask
import org.gradle.api.*
import org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency

class MyLintPlugin implements Plugin<Project> {

//    private static boolean hasCheckHookFile = false

    @Override
    void apply(Project project) {
        def androidVariants = getAndroidVariants(project)
        if (androidVariants != null) {
            applyTask(project, androidVariants)
        }
//        if (!hasCheckHookFile) {
        checkGitHookFile(project)
//        }
    }

    private static DomainObjectSet<BaseVariant> getAndroidVariants(Project project) {
        def extension = project.extensions.findByName("android") as BaseExtension
        if (extension == null) {
            return null
        }
        if (extension instanceof LibraryExtension) {
            return (extension as LibraryExtension).libraryVariants
        } else if (extension instanceof AbstractAppExtension) {
            return (extension as AbstractAppExtension).applicationVariants
        } else {
            return null
        }
    }

    /**
     * 查找并复制pre-push.sh文件
     * @param project
     */
    private static void checkGitHookFile(Project project) {
        println("search and copy pre-push.sh")
        def rootFile = project.getRootDir()
        def gitFile = new File(rootFile, ".git")
        if (!gitFile.exists()) {
            gitFile = new File(rootFile.getParentFile(), ".git")
        }
        println(".git file exists=" + gitFile.exists())
        if (gitFile.exists()) {
            def prePushHookFile = new File(gitFile, "hooks/pre-push.sh")
            if (!prePushHookFile.exists() || prePushHookFile.length() == 0) {
                println("start copy pre-push.sh")
                try {
                    InputStream inputStream = MyLintPlugin.class.getResourceAsStream("/config/pre-push.sh")
                    OutputStream outputStream = new FileOutputStream(prePushHookFile)
                    IOUtils.writeToFile(inputStream, outputStream)
                    println("copy pre-push.sh success")
                } catch (Exception ignored) {
                    println("copy pre-push.sh failed!")
                }
            }
        }
    }

    private void applyTask(Project project, DomainObjectSet<BaseVariant> variants) {
//        project.dependencies {
//            if (project.getPlugins().hasPlugin('com.android.application')) {
//                implementation project(':lintLib') {
//                    force = true
//                }
//            } else {
//                implementation project(':lintLib') {
//                    force = true
//                }
//            }
//        }
        project.configurations.all {
            resolutionStrategy.cacheDynamicVersionsFor 0, 'seconds'
        }
        def archonTaskExists = false
        variants.all { variant ->
            def variantName = variant.name.capitalize()
            LintBaseTask lintTask = project.tasks.getByName("lint" + variantName) as LintBaseTask
            //Lint 会把project下的lint.xml和lintConfig指定的lint.xml进行合并，为了确保只执行插件中的规则，采取此策略
            File lintFile = project.file("lint.xml")
            def lintOptions = lintTask.lintOptions
            lintOptions.lintConfig = lintFile
            lintOptions.abortOnError = true
            lintOptions.htmlReport = true
            lintOptions.htmlOutput = project.file("${project.projectDir}/lint-report/lint-report.html")
            lintOptions.xmlReport = false
            lintTask.doFirst {
                println("lintFile.exists()=" + lintFile.exists())
                if (!lintFile.exists()) {
                    try {
                        InputStream inputStream = MyLintPlugin.class.getResourceAsStream("/config/lint.xml")
                        OutputStream outputStream = new FileOutputStream(lintFile)
                        IOUtils.writeToFile(inputStream, outputStream)
                        println("copy /config/lint.xml success")
                    } catch (Exception ignored) {
                        println("copy /config/lint.xml failed")
                    }
                }
            }
            if (!archonTaskExists) {
                archonTaskExists = true
                println("make lintForArchon")
                project.task("lintForArchon").dependsOn lintTask
            }
        }
    }

}