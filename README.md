# cli-power-tools

Tools and utilities for Java CLI applications

## Features
- Home and local directory management for CLI apps
- Properties file management (read/write/merge)
- Safe, cross-platform file and directory naming
- Builder pattern for flexible configuration
- Directory deletion utility

## Installation

The library is built and available on jitpack: https://jitpack.io/#teggr/cli-power-tools

### Maven (via JitPack)

Add the JitPack repository and dependency to your `pom.xml`:

```xml
<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>

<dependency>
  <groupId>com.github.teggr</groupId>
  <artifactId>cli-power-tools</artifactId>
  <version>main-SNAPSHOT</version>
</dependency>
```

### JBang

You can use this project as a JBang dependency:

```
//DEPS com.github.teggr:cli-power-tools:main-SNAPSHOT
```

## Usage

### Basic Example

```java
import dev.rebelcraft.cli.App;
import java.util.Properties;

public class Example {
    public static void main(String[] args) {
        // create an App with both home and local directories
        App app = new App.Builder()
            .appName("my-cool-app")
            .withHomeDirectory()
            .withLocalDirectory()
            .build();

        // save properties
        Properties props = new Properties();
        props.setProperty("foo", "bar");
        app.saveHomeProperties(props);

        props.setProperty("foo", "wizz");
        app.saveLocalProperties(props);

        // get merged properties home < local
        Properties loaded = app.getMergedProperties();

        // then: use the loaded properties - foo => wizz
        System.out.println("Loaded foo: " + loaded.getProperty("foo"));
    }
}
```

### Custom Working Directory

```java
App app = new App.Builder()
    .appName("my-cool-app")
    .withWorkingDirectory("/tmp")
    .withLocalDirectory()
    .build();
```

### Deleting App Directories

```java
app.deleteApp();
```
