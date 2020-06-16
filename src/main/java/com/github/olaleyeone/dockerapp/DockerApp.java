package com.github.olaleyeone.dockerapp;

import com.github.olaleyeone.dockerapp.tasks.DockerBuildTask;
import com.github.olaleyeone.dockerapp.tasks.DockerPushTask;
import com.github.olaleyeone.dockerapp.tasks.DockerAppTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.JavaPlugin;

public class DockerApp implements Plugin<Project> {

    public static final String workDir = "dockerApp";

    @Override
    public void apply(Project project) {

        DockerAppPluginExtension extension = project.getExtensions()
                .create("dockerApp", DockerAppPluginExtension.class);

        ((ExtensionAware) extension).getExtensions()
                .create("docker", DockerExtension.class);

        project.getPluginManager().apply(JavaPlugin.class);

        Task dockerAppTask = project.getTasks().create("dockerApp", DockerAppTask.class)
                .dependsOn(project.getTasksByName("build", false));

        Task dockerBuild = project.getTasks().create("dockerBuild", DockerBuildTask.class)
                .dependsOn(dockerAppTask);

        project.getTasks().create("dockerPush", DockerPushTask.class)
                .dependsOn(dockerBuild);
    }
}
