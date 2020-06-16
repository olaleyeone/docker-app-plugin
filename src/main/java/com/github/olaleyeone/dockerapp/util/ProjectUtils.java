package com.github.olaleyeone.dockerapp.util;

import com.github.olaleyeone.dockerapp.DockerAppPluginExtension;
import com.github.olaleyeone.dockerapp.ProjectDescriptor;
import com.github.olaleyeone.dockerapp.RemoteDependency;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ModuleVersionIdentifier;
import org.gradle.api.artifacts.ResolvedDependency;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;

import java.io.File;
import java.util.*;

public class ProjectUtils {

    public ProjectDescriptor getDescriptor(Project project, DockerAppPluginExtension extension) {

        Configuration standalone = project.getConfigurations().create("application")
                .extendsFrom(
                        project.getConfigurations().findByName("implementation"),
                        project.getConfigurations().findByName("runtime"),
                        project.getConfigurations().findByName("runtimeOnly"));

        Set<String> declaredDependencies = new HashSet<>();
        Map<String, Set<ResolvedDependency>> groupReferences = new HashMap<>();
        standalone.getResolvedConfiguration().getFirstLevelModuleDependencies()
                .forEach(resolvedDependency -> {
                    declaredDependencies.add(resolvedDependency.getModuleGroup());
                    processNode(groupReferences, resolvedDependency);
                });

        ProjectDescriptor projectDescriptor = new ProjectDescriptor(groupReferences, getMavenArtifactRepositories(project));
        projectDescriptor.setGradleHome(project.getGradle().getGradleHomeDir());
        projectDescriptor.setName(project.getName());
        projectDescriptor.setMainClassName(extension.getMainClassName());
        projectDescriptor.setDeclaredDependencies(declaredDependencies);

        setDependencies(project.getRootProject(), projectDescriptor, standalone);

//        System.out.println(declaredDependencies);
//        System.out.println(declaredDependencies.size());
//        System.out.println(groupReferences.entrySet().size());

//        Set<Map.Entry<String, Integer>> counter = new TreeSet<>(Comparator.comparing(Map.Entry::getValue));
//        groupReferences.entrySet().forEach(entry -> counter.add(new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue().size())));
//        counter.forEach(count -> System.out.println(count));

//        Set<Map.Entry<String, Set<String>>> counter = new TreeSet<>((entry, t1) -> {
//            int sizeCompare = Integer.valueOf(entry.getValue().size()).compareTo(t1.getValue().size());
//            if (sizeCompare == 0) {
//                return entry.getKey().compareTo(t1.getKey());
//            }
//            return sizeCompare;
//        });
//        groupReferences.entrySet().forEach(entry -> counter.add(new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue())));
//        counter.forEach(count -> System.out.println(count + "\n"));

        return projectDescriptor;
    }

    private void setDependencies(Project rootProject, ProjectDescriptor projectDescriptor, Configuration configuration) {
        Set<File> localDependencies = new HashSet<>();
        Set<RemoteDependency> resolvedDependenciesCoordinates = new TreeSet<>();

        configuration.resolve().forEach(file -> localDependencies.add(file));

        configuration.getResolvedConfiguration().getResolvedArtifacts()
                .forEach(resolvedArtifact -> {
                    ModuleVersionIdentifier moduleVersionIdentifier = resolvedArtifact.getModuleVersion().getId();

                    Optional<Project> optionalProject = rootProject.getAllprojects().stream().filter(project ->
                            project.getGroup().equals(moduleVersionIdentifier.getGroup())
                                    && project.getName().equals(moduleVersionIdentifier.getName())
                    ).findFirst();

                    if (optionalProject.isPresent()) {
                        projectDescriptor.getRepositories().addAll(getMavenArtifactRepositories(optionalProject.get()));

                        optionalProject.get().getConfigurations().getByName("runtimeClasspath")
                                .getResolvedConfiguration().getFirstLevelModuleDependencies()
                                .forEach(resolvedDependency -> projectDescriptor.getDeclaredDependencies().add(resolvedDependency.getModuleGroup()));
                        return;
                    }

                    if (!localDependencies.remove(resolvedArtifact.getFile())) {
                        return;
                    }
//                    if (!projectDescriptor.getGroupReferences().containsKey(moduleVersionIdentifier.getGroup())
//                            && !projectDescriptor.getDeclaredDependencies().contains(moduleVersionIdentifier.getGroup())) {
//                        System.out.println(moduleVersionIdentifier);
//                    }

                    resolvedDependenciesCoordinates.add(new RemoteDependency(resolvedArtifact));
                });

        projectDescriptor.setFileDependencies(localDependencies);
        projectDescriptor.setRemoteDependencies(resolvedDependenciesCoordinates);
    }

    private void processNode(Map<String, Set<ResolvedDependency>> flatTree, ResolvedDependency resolvedDependency) {
//        System.out.println(resolvedDependency);
        resolvedDependency.getChildren().forEach(child -> {
            if (resolvedDependency.getModuleGroup().equals(child.getModuleGroup())) {
                return;
            }
            flatTree.computeIfAbsent(child.getModuleGroup(), s -> new HashSet<>());
            flatTree.compute(child.getModuleGroup(), (key, value) -> {
                value.add(resolvedDependency);
                return value;
            });
            processNode(flatTree, child);
        });
    }

    private Set<MavenArtifactRepository> getMavenArtifactRepositories(Project project) {
        Set<MavenArtifactRepository> repositories = new HashSet<>();
        project.getRepositories().forEach(artifactRepository -> {
            if (!(artifactRepository instanceof MavenArtifactRepository)) {
                return;
            }
            repositories.add((MavenArtifactRepository) artifactRepository);
        });
        return repositories;
    }
}
