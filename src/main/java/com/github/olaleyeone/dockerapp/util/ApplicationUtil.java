package com.github.olaleyeone.dockerapp.util;

import com.github.olaleyeone.dockerapp.DockerAppPluginExtension;
import com.github.olaleyeone.dockerapp.ProjectDescriptor;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginConvention;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;

import static org.gradle.api.tasks.SourceSet.MAIN_SOURCE_SET_NAME;

public class ApplicationUtil {

    private final FileUtils fileUtils;
    private final ProjectUtils projectUtils;

    public ApplicationUtil(
            FileUtils fileUtils,
            ProjectUtils projectUtils) {
        this.fileUtils = fileUtils;
        this.projectUtils = projectUtils;
    }

    public ProjectDescriptor create(Project project, DockerAppPluginExtension extension, Path appRoot) {
        ProjectDescriptor descriptor = projectUtils.getDescriptor(project, extension);
        exportApplication(project, descriptor, appRoot);
//        gradleUtil.setUp(descriptor, appRoot);
        return descriptor;
    }

    private void exportApplication(Project project, ProjectDescriptor descriptor, Path appRoot) {
        copyOutput(project, appRoot);
        copyFileDependencies(descriptor.getFileDependencies(), appRoot);
        copyRemoteDependencies(descriptor, appRoot);
    }

    private void copyOutput(Project project, Path appRoot) {
        Path classesDir = appRoot.resolve("classes");
        project.getConvention().getPlugin(JavaPluginConvention.class).getSourceSets()
                .forEach(sourceSet -> {
                    if (!sourceSet.getName().equals(MAIN_SOURCE_SET_NAME)) {
                        return;
                    }
                    if (sourceSet.getOutput().getResourcesDir().exists()) {
                        fileUtils.copyDirectoryContent(sourceSet.getOutput().getResourcesDir().toPath(), classesDir);
                    }
                    sourceSet.getOutput().getClassesDirs().getFiles()
                            .forEach(file -> fileUtils.copyDirectoryContent(file.toPath(), classesDir));
                });
    }

    private void copyFileDependencies(Set<File> resolvedDependenciesLocation, Path appRoot) {
        Path libs = appRoot.resolve("local-lib");
        try {
            Files.createDirectory(libs);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        resolvedDependenciesLocation.forEach(file -> {
            try {
                Files.copy(file.toPath(), libs.resolve(file.getName()), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void copyRemoteDependencies(ProjectDescriptor descriptor, Path appRoot) {
        Path libs = appRoot.resolve("remote-lib");
        try {
            Files.createDirectory(libs);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        descriptor.getRemoteDependencies().forEach(remoteDependency -> {
            try {
                Path groupPath = libs.resolve(remoteDependency.getGroup());
                File file = remoteDependency.getFile();
                if (!descriptor.getDeclaredDependencies().contains(remoteDependency.getGroup())) {
                    Set<String> dependentGroups = descriptor.getDependentGroups(remoteDependency.getGroup());
                    if (dependentGroups.isEmpty()) {
                        groupPath = libs;
                    } else if (dependentGroups.size() == 1) {
                        groupPath = libs.resolve(dependentGroups.iterator().next())
                                .resolve("lib");
                    }
                }
                if (!Files.exists(groupPath)) {
                    Files.createDirectories(groupPath);
                }
                Files.copy(file.toPath(), groupPath.resolve(file.getName()), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
