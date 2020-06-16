package com.github.olaleyeone.dockerapp.util;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class FileUtils {

    public void deleteDirectory(Path path) {
        if (!Files.exists(path)) {
            return;
        }
        deleteDirectoryContent(path);
        try {
            Files.delete(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteDirectoryContent(Path path) {
        try {
            Files.walk(path, FileVisitOption.FOLLOW_LINKS)
                    .forEach(subPath -> {
                        if (subPath == path) {
                            return;
                        }
                        try {
                            if (Files.isDirectory(subPath)) {
                                deleteDirectory(subPath);
                            } else {
                                Files.delete(subPath);
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void copyDirectoryContent(Path source, Path destination) {
        try {
            copyDirectoryContent(source, source, destination);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void copyDirectoryContent(Path root, Path source, Path destination) throws IOException {
        Files.walk(source, FileVisitOption.FOLLOW_LINKS)
                .forEach(subPath -> {
                    if (subPath == source) {
                        return;
                    }
                    try {
                        if (Files.isDirectory(subPath)) {
                            copyDirectoryContent(root, subPath, destination);
                        } else {
                            Path resolve = destination.resolve(root.relativize(subPath));
                            Files.createDirectories(resolve.getParent());
                            Files.copy(subPath, destination.resolve(root.relativize(subPath)), StandardCopyOption.REPLACE_EXISTING);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}
