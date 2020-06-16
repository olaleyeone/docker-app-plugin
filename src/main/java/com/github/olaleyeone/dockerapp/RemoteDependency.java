package com.github.olaleyeone.dockerapp;

import org.gradle.api.artifacts.ModuleVersionIdentifier;
import org.gradle.api.artifacts.ResolvedArtifact;

import java.io.File;

public class RemoteDependency implements Comparable<RemoteDependency> {

    private final String group;
    private final String name;
    private final String version;
    private final String ext;
    private final File file;

    public RemoteDependency(ResolvedArtifact resolvedArtifact) {
        file = resolvedArtifact.getFile();
        ModuleVersionIdentifier moduleVersionIdentifier = resolvedArtifact.getModuleVersion().getId();
        group = moduleVersionIdentifier.getGroup();
        name = moduleVersionIdentifier.getName();
        version = moduleVersionIdentifier.getVersion();
        ext = resolvedArtifact.getExtension();
    }

    public String getGroup() {
        return group;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getExt() {
        return ext;
    }

    public File getFile() {
        return file;
    }

    public String getGradleCoordinate() {
        return String.format("group: '%s', name: '%s', version: '%s', ext: '%s'",
                group,
                name,
                version,
                ext
        );
    }

    @Override
    public int compareTo(RemoteDependency remoteDependency) {
        return getGradleCoordinate().compareTo(remoteDependency.getGradleCoordinate());
    }
}
