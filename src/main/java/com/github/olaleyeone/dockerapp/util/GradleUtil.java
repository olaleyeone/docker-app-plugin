package com.github.olaleyeone.dockerapp.util;

import com.github.olaleyeone.dockerapp.ProjectDescriptor;
import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class GradleUtil {

    public void setUp(ProjectDescriptor projectDescriptor, Path application) {
        writeBuildFile(application, projectDescriptor);
        writeSettingsFile(application, projectDescriptor);

        try (ProjectConnection connection = GradleConnector.newConnector()
                .useInstallation(projectDescriptor.getGradleHome())
                .forProjectDirectory(application.toFile())
                .connect()) {
            BuildLauncher build = connection.newBuild();
            build.forTasks("wrapper");
            try {
                build.run();
            } finally {
                connection.close();
            }
        }
    }

    private void writeSettingsFile(Path application, ProjectDescriptor projectDescriptor) {
        try (ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream()) {
            StreamUtil.transferTo(getClass().getResourceAsStream("/settings.gradle"), arrayOutputStream);
            String buildSettings = new String(arrayOutputStream.toByteArray());
            buildSettings = buildSettings.replace("${rootProject.name}", projectDescriptor.getName());
            Files.write(application.resolve("settings.gradle"), buildSettings.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeBuildFile(Path application, ProjectDescriptor projectDescriptor) {
        try (ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream()) {
            StreamUtil.transferTo(getClass().getResourceAsStream("/build.gradle"), arrayOutputStream);
            String buildFile = new String(arrayOutputStream.toByteArray());
            buildFile = buildFile.replace("@mainClassName", projectDescriptor.getMainClassName());
            buildFile = buildFile.replace("//dependencies", projectDescriptor.getRemoteDependencies().stream()
                    .map(s -> String.format("\t runtimeOnly %s", s.getGradleCoordinate()))
                    .reduce((s, s2) -> String.format("%s\n%s", s, s2))
                    .get());
            buildFile = buildFile.replace("//repositories", projectDescriptor.getRepositories().stream()
                    .map(s -> String.format("\t maven {url '%s'}", s.getUrl()))
                    .reduce((s, s2) -> String.format("%s\n%s", s, s2))
                    .get());
            Files.write(application.resolve("build.gradle"), buildFile.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
