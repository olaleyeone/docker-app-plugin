package com.github.olaleyeone.dockerapp;

public class DockerExtension {

    private String repository;
    private String[] tag;
    private String baseImage;

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public String[] getTag() {
        return tag;
    }

    public void setTag(String[] tag) {
        this.tag = tag;
    }

    public String getBaseImage() {
        return baseImage;
    }

    public void setBaseImage(String baseImage) {
        this.baseImage = baseImage;
    }

    @Override
    public String toString() {
        return "DockerExtension{" +
                "tag=" + tag +
                ", baseImage=" + baseImage +
                '}';
    }
}
