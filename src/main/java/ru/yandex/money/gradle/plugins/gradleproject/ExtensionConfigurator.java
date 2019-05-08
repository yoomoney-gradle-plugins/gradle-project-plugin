package ru.yandex.money.gradle.plugins.gradleproject;

import org.gradle.api.Project;
import org.gradle.plugins.ide.idea.model.IdeaModel;
import ru.yandex.money.gradle.plugins.gradleproject.git.GitManager;
import ru.yandex.money.gradle.plugins.library.git.expired.branch.settings.EmailConnectionExtension;
import ru.yandex.money.gradle.plugins.library.git.expired.branch.settings.GitConnectionExtension;
import ru.yandex.money.gradle.plugins.release.ReleaseExtension;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

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
        configureIdeaExtPlugin(project);
        configureReleasePlugin(project);
    }

    private static void configureReleasePlugin(Project project) {
        ReleaseExtension releaseExtension = project.getExtensions().getByType(ReleaseExtension.class);
        releaseExtension.getReleaseTasks().clear();
        releaseExtension.getReleaseTasks().addAll(Arrays.asList("build", "publish"));
        releaseExtension.setChangelogRequired(true);
        releaseExtension.setPathToGitPrivateSshKey(System.getenv("GIT_PRIVATE_SSH_KEY_PATH"));

        try (GitManager git = new GitManager(project.getRootDir())) {
            if (!git.isCurrentBranchForRelease()) {
                project.getTasks().getByName("build")
                        .dependsOn(project.getTasks().getByName("checkChangelog"));
            }
        }
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
    }

    private static void configureGitExpiredBranchesExtension(Project project) {
        EmailConnectionExtension emailConnection = project.getExtensions().getByType(EmailConnectionExtension.class);
        emailConnection.emailHost = "mail.yamoney.ru";
        emailConnection.emailPort = 25;
        emailConnection.emailAuthUser = System.getenv("MAIL_USER");
        emailConnection.emailAuthPassword = System.getenv("MAIL_PASSWORD");


        GitConnectionExtension gitConnectionExtension =
                project.getExtensions().findByType(GitConnectionExtension.class);

        gitConnectionExtension.setPathToGitPrivateSshKey(System.getenv("GIT_PRIVATE_SSH_KEY_PATH"));
    }

}
