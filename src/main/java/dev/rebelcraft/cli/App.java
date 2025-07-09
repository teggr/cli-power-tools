package dev.rebelcraft.cli;

import java.io.*;
import java.nio.file.*;
import java.util.Comparator;
import java.util.Properties;

/**
 * The App class provides the cli developer a facility to specify an environment for the cli app to run it.
 * 
 * - A home directory named after the app in the user's home directory e.g ~/.{appname}
 * - A local directory named after the app in the provided working directory e.g ./.{appname}
 * - A properties file in the home directory to which properties can be written and read
 * - A properties file in the local directory to which properties can be written and read
 * - A merged properties feature that includes both home and local, where local overrides home
 * - Use the builder class to spec your App
 * - The app directories can be deleted using the deleteApp method()
 * 
 */
public class App {

    private final String appName;
    private final Path homeDir;
    private final Path localDir;
    private final Path homePropertiesFile;
    private final Path localPropertiesFile;

    // Private constructor for builder
    private App(String appName, Path homeDir, Path localDir, Path homePropertiesFile, Path localPropertiesFile, boolean createHomeDir, boolean createLocalDir) {
        this.appName = appName;
        this.homeDir = homeDir;
        this.localDir = localDir;
        this.homePropertiesFile = homePropertiesFile;
        this.localPropertiesFile = localPropertiesFile;
        if (createHomeDir) {
            try { Files.createDirectories(homeDir); } catch (IOException e) { throw new RuntimeException("Failed to create home directory", e); }
        }
        if (createLocalDir) {
            try { Files.createDirectories(localDir); } catch (IOException e) { throw new RuntimeException("Failed to create local directory", e); }
        }
    }

    // Update the original constructor to not create directories by default
    private App(String appName, Path homeDir, Path localDir, Path homePropertiesFile, Path localPropertiesFile) {
        this(appName, homeDir, localDir, homePropertiesFile, localPropertiesFile, false, false);
    }

    public Properties loadHomeProperties() {
        if (!Files.exists(homeDir)) {
            throw new RuntimeException("Home directory does not exist: " + homeDir);
        }
        return loadProperties(homePropertiesFile);
    }

    public Properties loadLocalProperties() {
        if (!Files.exists(localDir)) {
            throw new RuntimeException("Local directory does not exist: " + localDir);
        }
        return loadProperties(localPropertiesFile);
    }

    public void saveHomeProperties(Properties props) {
        if (!Files.exists(homeDir)) {
            throw new RuntimeException("Home directory does not exist: " + homeDir);
        }
        saveProperties(homePropertiesFile, props);
    }

    public void saveLocalProperties(Properties props) {
        if (!Files.exists(localDir)) {
            throw new RuntimeException("Local directory does not exist: " + localDir);
        }
        saveProperties(localPropertiesFile, props);
    }

    public Properties getMergedProperties() {
        Properties homeProps = loadHomeProperties();
        Properties localProps = loadLocalProperties();
        Properties merged = new Properties();
        merged.putAll(homeProps);
        merged.putAll(localProps); // local overrides home
        return merged;
    }

    private Properties loadProperties(Path file) {
        Properties props = new Properties();
        if (Files.exists(file)) {
            try (InputStream in = Files.newInputStream(file)) {
                props.load(in);
            } catch (IOException e) {
                throw new RuntimeException("Failed to load properties from " + file, e);
            }
        }
        return props;
    }

    private void saveProperties(Path file, Properties props) {
        try (OutputStream out = Files.newOutputStream(file)) {
            props.store(out, "App properties");
        } catch (IOException e) {
            throw new RuntimeException("Failed to save properties to " + file, e);
        }
    }

    /**
     * Deletes the app's home and local directories and all their contents.
     */
    public void deleteApp() {
        System.out.println("Deleting app directories: " + homeDir + " and " + localDir);
        deleteDirectoryRecursively(homeDir);
        deleteDirectoryRecursively(localDir);
    }

    private void deleteDirectoryRecursively(Path dir) {
        if (Files.exists(dir)) {
            try {
                Files.walk(dir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        System.out.println("Deleting: " + path);
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to delete " + path, e);
                        }
                    });
            } catch (IOException e) {
                throw new RuntimeException("Failed to delete directory " + dir, e);
            }
        }
    }

    public static class Builder {
        private String appName;
        private String homeDirOverride;
        private String localDirOverride;
        private String homePropertiesFileName = "app.properties";
        private String localPropertiesFileName = "app.properties";
        private boolean createHomeDir = false;
        private boolean createLocalDir = false;

        public Builder appName(String appName) {
            this.appName = appName;
            return this;
        }

        public Builder homeDir(String homeDir) {
            this.homeDirOverride = homeDir;
            return this;
        }

        public Builder localDir(String localDir) {
            this.localDirOverride = localDir;
            return this;
        }

        public Builder homePropertiesFileName(String fileName) {
            this.homePropertiesFileName = fileName;
            return this;
        }

        public Builder localPropertiesFileName(String fileName) {
            this.localPropertiesFileName = fileName;
            return this;
        }

        /**
         * Enable creation of the app home directory and its properties file.
         */
        public Builder withHomeDirectory() {
            this.createHomeDir = true;
            return this;
        }

        /**
         * Enable creation of the app local directory and its properties file.
         */
        public Builder withLocalDirectory() {
            this.createLocalDir = true;
            return this;
        }

        public App build() {
            String appNameToUse = appName != null ? appName : "app";
            Path homeDir = homeDirOverride != null ? Paths.get(homeDirOverride) : Paths.get(System.getProperty("user.home"), "." + appNameToUse);
            Path localDir = localDirOverride != null ? Paths.get(localDirOverride) : Paths.get(System.getProperty("user.dir"), "." + appNameToUse);
            Path homePropertiesFile = homeDir.resolve(homePropertiesFileName);
            Path localPropertiesFile = localDir.resolve(localPropertiesFileName);
            App app = new App(appNameToUse, homeDir, localDir, homePropertiesFile, localPropertiesFile, createHomeDir, createLocalDir);
            return app;
        }
    }

    public static void main(String[] args) {
        App app = new App.Builder()
            .appName("cli-power-tools")
            .withHomeDirectory()
            .withLocalDirectory()
            .build();
        System.out.println("Home directory: " + app.homeDir);
        System.out.println("Local directory: " + app.localDir);
        // Example usage
        Properties props = app.getMergedProperties();
        System.out.println("Merged properties: " + props);

        app.deleteApp();

    }
}
