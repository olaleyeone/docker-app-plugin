package com.github.olaleyeone.dockerapp;

import org.gradle.api.artifacts.ResolvedDependency;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ProjectDescriptor {

    private File gradleHome;
    private String name;
    private String mainClassName;
    private DockerAppPluginExtension extension;

    private final Set<MavenArtifactRepository> repositories;
    //    private final Set<String> subProjects;
    private Set<File> fileDependencies;
    private Set<RemoteDependency> remoteDependencies;

    private Set<String> declaredDependencies;
    private final Map<String, Set<ResolvedDependency>> groupDependants;

    public ProjectDescriptor(Map<String, Set<ResolvedDependency>> groupDependants, Set<MavenArtifactRepository> repositories) {
        this.groupDependants = groupDependants;
        this.repositories = repositories;
    }

    public DockerAppPluginExtension getExtension() {
        return extension;
    }

    public void setExtension(DockerAppPluginExtension extension) {
        this.extension = extension;
    }

    public File getGradleHome() {
        return gradleHome;
    }

    public void setGradleHome(File gradleHome) {
        this.gradleHome = gradleHome;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMainClassName() {
        return mainClassName;
    }

    public void setMainClassName(String mainClassName) {
        this.mainClassName = mainClassName;
    }

//    public Set<String> getSubProjects() {
//        return subProjects;
//    }

    public Set<File> getFileDependencies() {
        return fileDependencies;
    }

    public void setFileDependencies(Set<File> fileDependencies) {
        this.fileDependencies = fileDependencies;
    }

    public Set<RemoteDependency> getRemoteDependencies() {
        return remoteDependencies;
    }

    public void setRemoteDependencies(Set<RemoteDependency> remoteDependencies) {
        this.remoteDependencies = remoteDependencies;
    }

    public Set<MavenArtifactRepository> getRepositories() {
        return repositories;
    }

    public Set<String> getDeclaredDependencies() {
        return declaredDependencies;
    }

    public void setDeclaredDependencies(Set<String> declaredDependencies) {
        this.declaredDependencies = declaredDependencies;
    }

    public Set<String> getDependentGroups(String groupName) {
        Set<ResolvedDependency> resolvedDependencies = groupDependants.get(groupName);
        if (resolvedDependencies == null) {
            return Collections.EMPTY_SET;
        }
        Set<String> anchor = resolvedDependencies.stream()
                .map(resolvedDependency -> resolvedDependency.getModuleGroup())
                .filter(group -> declaredDependencies.contains(group))
                .collect(Collectors.toSet());

        anchor.addAll(resolvedDependencies.stream()
                .filter(resolvedDependency -> !declaredDependencies.contains(resolvedDependency.getModuleGroup()))
                .flatMap(resolvedDependency -> getDependentGroups(resolvedDependency.getModuleGroup()).stream())
                .collect(Collectors.toSet()));
        return anchor;
    }
}
