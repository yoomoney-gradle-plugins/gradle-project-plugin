package ru.yandex.money.gradle.plugins.gradleproject;

import org.gradle.api.Project;
import org.gradle.api.internal.file.pattern.HasSuffixPatternStep;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.plugins.ide.idea.model.IdeaModel;
import org.gradle.plugins.ide.idea.model.IdeaProject;
import org.jetbrains.gradle.ext.ActionDelegationConfig;
import org.jetbrains.gradle.ext.ProjectSettings;
import ru.yandex.money.gradle.plugins.library.dependencies.CheckDependenciesPluginExtension;
import ru.yandex.money.gradle.plugins.library.dependencies.checkversion.MajorVersionCheckerExtension;
import ru.yandex.money.gradle.plugins.library.git.expired.branch.settings.EmailConnectionExtension;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;

/**
 * Конфигуратор настроек плагинов.
 *
 * @author Dmitry Komarov
 * @since 05.12.2018
 */
public class ExtensionConfigurator {

    /**
     * Конфигурирует плагины.
     *
     * @param project целевой проект
     */
    static void configure(Project project) {
        configureGitExpiredBranchesExtension(project);
        configureMajorVersionCheckerExtension(project);
        configureCheckDependenciesExtension(project);
        configureIdeaExtPlugin(project);
    }


    private static void configureIdeaExtPlugin(Project project) {
        IdeaModel ideaModel = project.getExtensions().getByType(IdeaModel.class);
        ideaModel.getModule().setDownloadSources(true);
        ideaModel.getModule().setDownloadJavadoc(true);
        ideaModel.getModule().setInheritOutputDirs(true);
        Set<File> excludeDirs = new HashSet<>(ideaModel.getModule().getExcludeDirs());
        excludeDirs.removeIf(file -> file.equals(project.getBuildDir()));
        Stream.of("classes", "docs", "jacoco", "deb-templates", "publications", "out", "tmp",
                "dependency-cache", "resources", "libs", "test-results", "test-reports", "reports",
                "production", "test", "findbugsReports", "debSource", "debSourceDeploy", "debian",
                "distributions", "bindings-common", "schema", "checkstyleReports", "../build")
                .forEach(folderName -> excludeDirs.add(new File(project.getBuildDir().getPath() + "/" + folderName)));
        ideaModel.getModule().setExcludeDirs(excludeDirs);

        if (!"false".equalsIgnoreCase(System.getProperty("delegateActionsToGradle"))) {
            ExtensionAware ideaProject = (ExtensionAware) ideaModel.getProject();
            if (ideaProject != null) {
                ExtensionAware ideaProjectSettings = (ExtensionAware) ideaProject.getExtensions().getByType(ProjectSettings.class);
                ideaProjectSettings.getExtensions().getByType(ActionDelegationConfig.class).setDelegateBuildRunToGradle(true);
            }
        }
    }

    private static void configureGitExpiredBranchesExtension(Project project) {
        EmailConnectionExtension emailConnection = project.getExtensions().getByType(EmailConnectionExtension.class);
        emailConnection.emailHost = "mail.yamoney.ru";
        emailConnection.emailPort = 25;
        emailConnection.emailAuthUser = System.getenv("MAIL_USER");
        emailConnection.emailAuthPassword = System.getenv("MAIL_PASSWORD");
    }

    private static void configureMajorVersionCheckerExtension(Project project) {
        Set<String> includeGroupIdPrefixes = new HashSet<>();
        includeGroupIdPrefixes.add("ru.yamoney");
        includeGroupIdPrefixes.add("ru.yandex.money");

        project.getExtensions().getByType(MajorVersionCheckerExtension.class)
                .includeGroupIdPrefixes = includeGroupIdPrefixes;
    }

    private static void configureCheckDependenciesExtension(Project project) {
        CheckDependenciesPluginExtension checkDependenciesPluginExtension =
                project.getExtensions().getByType(CheckDependenciesPluginExtension.class);

        checkDependenciesPluginExtension.excludedConfigurations = Arrays.asList(
                "checkstyle", "errorprone", "optional", "findbugs");
    }
}
