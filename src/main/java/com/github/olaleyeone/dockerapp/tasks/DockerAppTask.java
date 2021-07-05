package com.github.olaleyeone.dockerapp.tasks;

import com.github.olaleyeone.dockerapp.DockerApp;
import com.github.olaleyeone.dockerapp.DockerAppPluginExtension;
import com.github.olaleyeone.dockerapp.DockerExtension;
import com.github.olaleyeone.dockerapp.ProjectDescriptor;
import com.github.olaleyeone.dockerapp.util.ApplicationUtil;
import com.github.olaleyeone.dockerapp.util.FileUtils;
import com.github.olaleyeone.dockerapp.util.ProjectUtils;
import com.github.olaleyeone.dockerapp.util.StreamUtil;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.tasks.TaskAction;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class DockerAppTask extends DefaultTask {

    private String DEFAULT_BASE_IMAGE = "openjdk:8-alpine";

    private FileUtils fileUtils;
    private final ApplicationUtil applicationUtil;

    private Path appRoot;

    public DockerAppTask() {
        fileUtils = new FileUtils();
        applicationUtil = new ApplicationUtil(fileUtils, new ProjectUtils());
    }

    @TaskAction
    public void createApplication() throws ClassNotFoundException {
        DockerAppPluginExtension pluginExtension = getProject().getExtensions().findByType(DockerAppPluginExtension.class);
        if (pluginExtension.getMainClassName() == null) {
            throw new IllegalArgumentException("MainClassName required");
        }
        appRoot = createAppDirectory(getProject());
        ProjectDescriptor projectDescriptor = applicationUtil.create(getProject(), pluginExtension, appRoot);

        Path mainClass = appRoot.resolve("classes");

        Iterator<String> iterator = Arrays.asList(pluginExtension.getMainClassName().split("\\.")).iterator();
        while (iterator.hasNext()) {
            String next = iterator.next();
            if (!iterator.hasNext()) {
                mainClass = mainClass.resolve(next + ".class");
                break;
            }
            mainClass = mainClass.resolve(next);
        }
        if (!Files.exists(mainClass) || Files.isDirectory(mainClass)) {
            throw new ClassNotFoundException(pluginExtension.getMainClassName());
        }

        generateDockerfile(projectDescriptor, pluginExtension, appRoot);
    }

    public void generateDockerfile(ProjectDescriptor projectDescriptor, DockerAppPluginExtension pluginExtension, Path appRoot) {
        DockerExtension dockerExtension = ((ExtensionAware) pluginExtension).getExtensions().findByType(DockerExtension.class);
        if (dockerExtension == null) {
            throw new RuntimeException();
        }

        try (ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream()) {
            StreamUtil.transferTo(getClass().getResourceAsStream("/Dockerfile"), arrayOutputStream);
            String buildSettings = new String(arrayOutputStream.toByteArray());
            buildSettings = buildSettings.replaceAll("@baseImage", Optional.ofNullable(dockerExtension.getBaseImage())
                    .orElse(DEFAULT_BASE_IMAGE));
            buildSettings = buildSettings.replaceAll("@appName", getProject().getName());
            buildSettings = buildSettings.replaceAll("@mainClassName", pluginExtension.getMainClassName());

            Path remoteLibs = appRoot.resolve("remote-lib");
            Set<Path> sourceFolders = getOrderGroups(projectDescriptor, remoteLibs);

            String copyCommands = sourceFolders.stream()
                    .map(path -> {
                        Path relativePath =  appRoot.relativize(path);
                        StringBuilder stringBuilder = new StringBuilder();
                        for(int i=0; i<relativePath.getNameCount(); i++){
                            stringBuilder.append(relativePath.getName(i)).append('/');
                        }
                        String relativePathString = stringBuilder.substring(0, stringBuilder.length()-1);
                        String copySource = String.format("%s/*.jar", relativePathString);
                        if (Files.exists(path.resolve("lib"))) {
                            copySource = String.format("%s/*.jar %s/lib/*.jar", relativePathString, relativePathString);
                        }
//                        Optional.ofNullable(projectDescriptor.getDependentGroups(path.toFile().getName()))
//                                .map(Set::size).orElse(0),
//                                projectDescriptor.getDeclaredDependencies().contains(path.toFile().getName()) ? "*" : ""
                        return String.format("COPY %s lib/", copySource);
                    })
                    .collect(Collectors.joining("\n"));
            copyCommands += String.format("\nCOPY %s/*.jar lib/", appRoot.relativize(remoteLibs).toString());
            buildSettings = buildSettings.replaceAll("#COPY_REMOTE_LIB", copyCommands);

            Files.write(appRoot.resolve("Dockerfile"), buildSettings.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Set<Path> getOrderGroups(ProjectDescriptor projectDescriptor, Path libs) throws IOException {
        Set<Path> sourceFolders = new TreeSet<>((p1, p2) -> {
//            if (projectDescriptor.getDeclaredDependencies().contains(p1.toFile().getName())
//                    && !projectDescriptor.getDeclaredDependencies().contains(p2.toFile().getName())) {
//                return 1;
//            }
//            if (projectDescriptor.getDeclaredDependencies().contains(p2.toFile().getName())) {
//                return -1;
//            }

            Set<String> p1References = projectDescriptor.getDependentGroups(p1.toFile().getName());
            Set<String> p2References = projectDescriptor.getDependentGroups(p2.toFile().getName());
            if (p1References == null && p2References != null) {
                return -1;
            }
            if (p2References == null) {
                return 1;
            }

            int order = Integer.compare(p1References.size(), p2References.size());
            if (order == 0) {
                return p1.toFile().getName().compareTo(p2.toFile().getName());
            }
            return order;
        });

        Files.walk(libs, 1).forEach(path -> {
            if (path.equals(libs) || !Files.isDirectory(path)) {
                return;
            }
            sourceFolders.add(path);
        });
        return sourceFolders;
    }

    public Path createAppDirectory(Project project) {
        Path application = project.getBuildDir().toPath().resolve(DockerApp.workDir);
        try {
            if (Files.exists(application)) {
                fileUtils.deleteDirectoryContent(application);
            } else {
                Files.createDirectory(application);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return application;
    }
}
