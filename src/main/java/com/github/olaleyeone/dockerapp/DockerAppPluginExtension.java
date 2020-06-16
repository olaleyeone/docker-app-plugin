package com.github.olaleyeone.dockerapp;

public class DockerAppPluginExtension {

    private String mainClassName;
    private DockerExtension docker;

    public String getMainClassName() {
        return mainClassName;
    }

    public void setMainClassName(String mainClassName) {
        this.mainClassName = mainClassName;
    }

    public DockerExtension getDocker() {
        return docker;
    }

    public void setDocker(DockerExtension docker) {
        this.docker = docker;
    }

}
