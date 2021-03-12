package com.zpf.plugin.lint

import com.android.build.gradle.AbstractAppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.tasks.LintBaseTask
import org.gradle.api.*
import org.gradle.api.tasks.TaskState

class MyLintPlugin implements Plugin<Project> {
    private static String hookFileName = "pre-push"

    @Override
    void apply(Project project) {
        def androidVariants = getAndroidVariants(project)
        if (androidVariants != null) {
            applyTask(project, androidVariants)
        }
        //检查git hook文件是否复制到了指定位置
        checkGitHookFile(project)
    }

    private static DomainObjectSet<BaseVariant> getAndroidVariants(Project project) {
        //只有使用了com.android.library或com.android.application插件的工程lint规则才起作用
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
     * 查找并复制pre-push文件
     */
    private static void checkGitHookFile(Project project) {
        def projectDir = project.getProjectDir()
        def gitFile = new File(projectDir, ".git")
        while (!gitFile.exists()) {
            projectDir = projectDir.getParentFile()
            if (projectDir == null) {
                break
            }
            gitFile = new File(projectDir, ".git")
        }
        println(".git file exists=" + gitFile.exists())
        if (gitFile.exists()) {
            def hookFile = new File(gitFile, "hooks/" + hookFileName)
            if (!hookFile.exists() || hookFile.length() == 0) {
                println("start write git hook file===>")
                try {
                    def head = "#!/bin/sh\ndriPath=\"" +
                            project.getRootDir().absolutePath +
                            "\"\nprojectName=\"" + project.name + "\"\n"
                    InputStream inputStream = MyLintPlugin.class.getResourceAsStream("/config/lint-hook")
                    IOUtils.copyFile(head, inputStream, hookFile)
                    println("create git hook file success")
                } catch (Exception ignored) {
                    println("create git hook file failed!==>" + ignored.message)
                }
            } else {
                println("file already exist:" + hookFile.getAbsolutePath())
            }
        }
    }

    private void applyTask(Project project, DomainObjectSet<BaseVariant> variants) {
        project.dependencies {
            //添加最新版的自定义lint依赖
            implementation("com.zpf.android:tool-lint:latest.integration") {
                force = true
            }
        }
        project.configurations.all {
            //设置缓存有效时长
            resolutionStrategy.cacheDynamicVersionsFor 0, 'seconds'
        }
        def archonTaskExists = false
        variants.all { variant ->
            def variantName = variant.name.capitalize()
            LintBaseTask lintTask = project.tasks.getByName("lint" + variantName) as LintBaseTask
            //通过lint.xml配置规则
            File lintFile = project.file("lint.xml")
            def lintOptions = lintTask.lintOptions
            lintOptions.lintConfig = lintFile
            lintOptions.abortOnError = true
            lintOptions.htmlReport = true
            lintOptions.htmlOutput = project.file("${project.projectDir}/lint-report/lint-report.html")
            lintOptions.xmlReport = false
            def hasCustomLint = lintFile.exists()
            lintTask.doFirst {
                hasCustomLint = lintFile.exists()
                println("hasCustomLint=" + hasCustomLint)
                //如果本地已有lint.xml配置文件则使用本地配置
                if (!hasCustomLint) {
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
            project.gradle.taskGraph.afterTask { task, TaskState state ->
                if (task == lintTask) {
                    //如果使用的插件中的规则，则检测结束后删除lint.xml
                    if (!hasCustomLint && lintFile != null && lintFile.exists()) {
                        lintFile.delete()
                    }
                }
            }
            if (!archonTaskExists) {
                //创建自定义task用于执行lint检测，此task会在gti hook中调用
                archonTaskExists = true
                println("create lintForArchon task in project " + project.name)
                project.task("lintForArchon").dependsOn lintTask
            }
        }
    }

}