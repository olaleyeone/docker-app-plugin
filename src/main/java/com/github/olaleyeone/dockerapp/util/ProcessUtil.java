package com.github.olaleyeone.dockerapp.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;

public class ProcessUtil {

    public static String awaitProcess(String command, Path workDir) {
        try {
            System.out.println(command);
            Process process = Runtime.getRuntime().exec(command, null, workDir.toFile());
            String response = printProcessData(process);
            int status = process.waitFor();
            if (status != 0) {
                throw new RuntimeException(String.format("%s failed with %d", command, status));
            }
            return response;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String printProcessData(Process process) throws IOException {
        String line = null;
        String errLine;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (BufferedReader errorReader = new BufferedReader(
                new InputStreamReader(process.getErrorStream()))) {
            while ((errLine = errorReader.readLine()) != null) {
                System.err.println(errLine);
            }
        }
        return line;
    }
}
