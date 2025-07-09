package dev.rebelcraft.cli;

import org.junit.jupiter.api.*;
import java.nio.file.*;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

public class AppTest {
    private App app;
    private String originalUserHome;
    private String originalUserDir;
    private Path tempHomeDir;
    private Path tempLocalDir;

    @BeforeEach
    void setUp() throws Exception {
        // Save original system properties
        originalUserHome = System.getProperty("user.home");
        originalUserDir = System.getProperty("user.dir");
        // Create temp directories
        tempHomeDir = Files.createTempDirectory("apptest-home");
        tempLocalDir = Files.createTempDirectory("apptest-local");
        System.setProperty("user.home", tempHomeDir.toString());
        System.setProperty("user.dir", tempLocalDir.toString());
        // Do not create or delete app here; each test will create its own app
    }

    @AfterEach
    void tearDown() throws Exception {
        // Only delete app after each test
        if (app != null) {
            app.deleteApp();
        }
        // Restore system properties
        System.setProperty("user.home", originalUserHome);
        System.setProperty("user.dir", originalUserDir);
        // Delete temp directories
        Files.walk(tempHomeDir).sorted(java.util.Comparator.reverseOrder()).forEach(path -> {
            try { Files.delete(path); } catch (Exception ignored) {}
        });
        Files.walk(tempLocalDir).sorted(java.util.Comparator.reverseOrder()).forEach(path -> {
            try { Files.delete(path); } catch (Exception ignored) {}
        });
    }

    private String uniqueAppName() {
        return "test-cli-app-" + System.nanoTime();
    }

    @Test
    void testDirectoriesCreated() {
        // given: a unique app name and builder with both directories enabled
        String appName = uniqueAppName();
        app = new App.Builder().appName(appName).withHomeDirectory().withLocalDirectory().build();
        Path homeDir = Paths.get(System.getProperty("user.home"), "." + appName);
        Path localDir = Paths.get(System.getProperty("user.dir"), "." + appName);

        // when: nothing else, just creation

        // then: both directories should exist
        assertTrue(Files.exists(homeDir), "Home directory should exist");
        assertTrue(Files.exists(localDir), "Local directory should exist");
    }

    @Test
    void testSaveAndLoadHomePropertiesThrowsIfNoHomeDir() {
        // given: a unique app name and builder with no directories
        String appName = uniqueAppName();
        app = new App.Builder().appName(appName).build();
        Properties props = new Properties();
        props.setProperty("foo", "bar");

        // when/then: saving or loading home properties should throw
        assertThrows(RuntimeException.class, () -> app.saveHomeProperties(props), "Should throw if home dir does not exist");
        assertThrows(RuntimeException.class, () -> app.loadHomeProperties(), "Should throw if home dir does not exist");
    }

    @Test
    void testSaveAndLoadLocalPropertiesThrowsIfNoLocalDir() {
        // given: a unique app name and builder with no directories
        String appName = uniqueAppName();
        app = new App.Builder().appName(appName).build();
        Properties props = new Properties();
        props.setProperty("baz", "qux");

        // when/then: saving or loading local properties should throw
        assertThrows(RuntimeException.class, () -> app.saveLocalProperties(props), "Should throw if local dir does not exist");
        assertThrows(RuntimeException.class, () -> app.loadLocalProperties(), "Should throw if local dir does not exist");
    }

    @Test
    void testSaveAndLoadHomeProperties() {
        // given: a unique app name and builder with home directory enabled
        String appName = uniqueAppName();
        app = new App.Builder().appName(appName).withHomeDirectory().build();
        Properties props = new Properties();
        props.setProperty("foo", "bar");

        // when: saving and loading home properties
        app.saveHomeProperties(props);
        Properties loaded = app.loadHomeProperties();

        // then: the property should be saved and loaded
        assertEquals("bar", loaded.getProperty("foo"));
    }

    @Test
    void testSaveAndLoadLocalProperties() {
        // given: a unique app name and builder with local directory enabled
        String appName = uniqueAppName();
        app = new App.Builder().appName(appName).withLocalDirectory().build();
        Properties props = new Properties();
        props.setProperty("baz", "qux");

        // when: saving and loading local properties
        app.saveLocalProperties(props);
        Properties loaded = app.loadLocalProperties();

        // then: the property should be saved and loaded
        assertEquals("qux", loaded.getProperty("baz"));
    }

    @Test
    void testMergedProperties() {
        // given: a unique app name and builder with both directories enabled
        String appName = uniqueAppName();
        app = new App.Builder().appName(appName).withHomeDirectory().withLocalDirectory().build();
        Properties homeProps = new Properties();
        homeProps.setProperty("key", "home");
        app.saveHomeProperties(homeProps);
        Properties localProps = new Properties();
        localProps.setProperty("key", "local");
        app.saveLocalProperties(localProps);

        // when: getting merged properties
        Properties merged = app.getMergedProperties();

        // then: local should override home
        assertEquals("local", merged.getProperty("key"), "Local should override home");
    }

    @Test
    void testDeleteApp() {
        // given: a unique app name and builder with both directories enabled
        String appName = uniqueAppName();
        app = new App.Builder().appName(appName).withHomeDirectory().withLocalDirectory().build();
        Path homeDir = Paths.get(System.getProperty("user.home"), "." + appName);
        Path localDir = Paths.get(System.getProperty("user.dir"), "." + appName);

        // when: deleting the app
        app.deleteApp();

        // then: both directories should be deleted
        assertFalse(Files.exists(homeDir), "Home directory should be deleted");
        assertFalse(Files.exists(localDir), "Local directory should be deleted");
    }

    @Test
    void testNoDirectoriesCreatedByDefault() {
        // given: a unique app name and builder with no directories enabled
        String appName = uniqueAppName();
        app = new App.Builder().appName(appName).build();
        Path homeDir = Paths.get(System.getProperty("user.home"), "." + appName);
        Path localDir = Paths.get(System.getProperty("user.dir"), "." + appName);

        // when: nothing else, just creation

        // then: neither directory should exist
        assertFalse(Files.exists(homeDir), "Home directory should NOT exist by default");
        assertFalse(Files.exists(localDir), "Local directory should NOT exist by default");
    }

    @Test
    void testOnlyHomeDirectoryCreated() {
        // given: a unique app name and builder with only home directory enabled
        String appName = uniqueAppName();
        app = new App.Builder().appName(appName).withHomeDirectory().build();
        Path homeDir = Paths.get(System.getProperty("user.home"), "." + appName);
        Path localDir = Paths.get(System.getProperty("user.dir"), "." + appName);

        // when: nothing else, just creation

        // then: only home directory should exist
        assertTrue(Files.exists(homeDir), "Home directory should exist");
        assertFalse(Files.exists(localDir), "Local directory should NOT exist");
    }

    @Test
    void testOnlyLocalDirectoryCreated() {
        // given: a unique app name and builder with only local directory enabled
        String appName = uniqueAppName();
        app = new App.Builder().appName(appName).withLocalDirectory().build();
        Path homeDir = Paths.get(System.getProperty("user.home"), "." + appName);
        Path localDir = Paths.get(System.getProperty("user.dir"), "." + appName);

        // when: nothing else, just creation

        // then: only local directory should exist
        assertFalse(Files.exists(homeDir), "Home directory should NOT exist");
        assertTrue(Files.exists(localDir), "Local directory should exist");
    }

    @Test
    void testBothDirectoriesCreated() {
        // given: a unique app name and builder with both directories enabled
        String appName = uniqueAppName();
        app = new App.Builder().appName(appName).withHomeDirectory().withLocalDirectory().build();
        Path homeDir = Paths.get(System.getProperty("user.home"), "." + appName);
        Path localDir = Paths.get(System.getProperty("user.dir"), "." + appName);

        // when: nothing else, just creation

        // then: both directories should exist
        assertTrue(Files.exists(homeDir), "Home directory should exist");
        assertTrue(Files.exists(localDir), "Local directory should exist");
    }

    @Test
    void testLocalDirectoryWithWorkingDirectoryOverride() throws Exception {
        // given: a unique app name and a custom working directory
        String appName = uniqueAppName();
        Path customWorkingDir = Files.createTempDirectory("apptest-custom-working-dir");
        app = new App.Builder()
            .appName(appName)
            .withWorkingDirectory(customWorkingDir.toString())
            .withLocalDirectory()
            .build();
        Path expectedLocalDir = customWorkingDir.resolve("." + appName);

        // when: nothing else, just creation

        // then: local directory should exist in the custom working directory
        assertTrue(Files.exists(expectedLocalDir), "Local directory should exist in the custom working directory");
        // Clean up
        app.deleteApp();
        Files.walk(customWorkingDir).sorted(java.util.Comparator.reverseOrder()).forEach(path -> {
            try { Files.delete(path); } catch (Exception ignored) {}
        });
    }

    @Test
    void testAppNameEscapingInDirectoriesAndFiles() {
        // given: an unsafe app name
        String unsafeName = "my/app:name?*|<>";
        String safeName = FileNameUtil.escapeName(unsafeName);
        app = new App.Builder().appName(unsafeName).withHomeDirectory().withLocalDirectory().build();
        Path homeDir = Paths.get(System.getProperty("user.home"), "." + safeName);
        Path localDir = Paths.get(System.getProperty("user.dir"), "." + safeName);
        Path homeProps = homeDir.resolve(safeName + ".properties");
        Path localProps = localDir.resolve(safeName + ".properties");

        // when: saving properties to both locations
        Properties props = new Properties();
        props.setProperty("foo", "bar");
        app.saveHomeProperties(props);
        app.saveLocalProperties(props);

        // then: the escaped directories and files should exist
        assertTrue(Files.exists(homeDir), "Escaped home directory should exist");
        assertTrue(Files.exists(localDir), "Escaped local directory should exist");
        assertTrue(Files.exists(homeProps), "Escaped home properties file should exist after save");
        assertTrue(Files.exists(localProps), "Escaped local properties file should exist after save");
    }
}
