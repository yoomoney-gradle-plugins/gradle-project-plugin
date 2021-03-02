package ru.yandex.money.gradle.plugins.gradleproject;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.plugin.devel.plugins.JavaGradlePluginPlugin;
import org.gradle.util.VersionNumber;
import ru.yandex.money.gradle.plugins.library.git.expired.branch.GitExpiredBranchPlugin;
import ru.yandex.money.gradle.plugins.task.monitoring.BuildMonitoringPlugin;
import ru.yoomoney.gradle.plugins.javapublishing.JavaArtifactPublishPlugin;
import ru.yoomoney.gradle.plugins.moduleproject.ModuleProjectPlugin;
import ru.yoomoney.gradle.plugins.release.ReleasePlugin;

import java.util.Arrays;
import java.util.Collection;

/**
 * Входная точка gradle-project-plugin'а, подключает все необходимые плагины-зависимости.
 *
 * @author Oleg Kandaurov
 * @since 14.11.2018
 */
public class GradleProjectPlugin implements Plugin<Project> {

    private static final Collection<Class<?>> PLUGINS_TO_APPLY = Arrays.asList(
            ModuleProjectPlugin.class,
            JavaArtifactPublishPlugin.class,
            ReleasePlugin.class,
            GitExpiredBranchPlugin.class,
            BuildMonitoringPlugin.class
    );

    @Override
    public void apply(Project project) {
        if (VersionNumber.parse(project.getGradle().getGradleVersion()).compareTo(VersionNumber.parse("6.4.1'")) < 0) {
            throw new IllegalStateException("Gradle >= 6.4.1 is required");
        }

        project.getPluginManager().apply(JavaGradlePluginPlugin.class);
        configureRepos(project);
        ExtensionConfigurator.configurePublishPlugin(project);
        PLUGINS_TO_APPLY.forEach(pluginClass -> project.getPluginManager().apply(pluginClass));
        ExtensionConfigurator.configure(project);
    }

    private void configureRepos(Project project) {
        project.getRepositories().maven(repo -> repo.setUrl("https://nexus.yamoney.ru/repository/gradle-plugins/"));
    }
}
