package com.github.olaleyeone.dockerapp.tasks;

import com.github.olaleyeone.dockerapp.DockerApp;
import com.github.olaleyeone.dockerapp.DockerExtension;
import com.github.olaleyeone.dockerapp.DockerAppPluginExtension;
import com.github.olaleyeone.dockerapp.util.ProcessUtil;
import org.gradle.api.DefaultTask;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.tasks.TaskAction;

import java.nio.file.Path;

public class DockerBuildTask extends DefaultTask {

    @TaskAction
    public void buildImages() {
        ExtensionAware pluginExtension = (ExtensionAware) getProject().getExtensions().findByType(DockerAppPluginExtension.class);
        DockerExtension dockerExtension = pluginExtension.getExtensions().findByType(DockerExtension.class);

        Path appRoot = getProject().getBuildDir().toPath().resolve(DockerApp.workDir);

//        System.out.println(Arrays.toString(dockerExtension.getTag()));
        String tag = getProject().getName();
        if (dockerExtension.getRepository() != null) {
            tag = dockerExtension.getRepository() + "/" + tag;
        }
        String command = String.format("docker build . -t %s", tag);
        ProcessUtil.awaitProcess(command, appRoot);
    }
}
