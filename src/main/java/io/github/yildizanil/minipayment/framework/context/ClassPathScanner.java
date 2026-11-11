package io.github.yildizanil.minipayment.framework.context;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

/**
 * Lists the classes in a package and its subpackages by looking them up on the classpath.
 * It only finds classes; deciding which of them become beans is left to the container.
 *
 * <p>The JVM has no "list this package" API, so the scanner relies on the rule the compiler
 * follows when it writes class files: package {@code a.b} lives under the path {@code a/b}. It
 * asks the class loader for every classpath directory holding that path and collects the
 * {@code .class} files there. Classes packaged in JARs are not supported.
 *
 * <p>Classes are loaded with {@code Class.forName(name, false, loader)}: loaded so they can be
 * inspected, but not initialized, so no static initializer runs during scanning.
 */
public final class ClassPathScanner {

    private static final String CLASS_FILE_SUFFIX = ".class";

    private final ClassLoader classLoader;

    public ClassPathScanner(ClassLoader classLoader) {
        this.classLoader = Objects.requireNonNull(classLoader, "classLoader can't be null");
    }

    /**
     * Returns the classes of {@code packageName} and all of its subpackages, sorted by name.
     * Anonymous, local and synthetic classes are skipped. A package that does not exist, or has
     * no classes, yields an empty list.
     */
    public List<Class<?>> scan(String packageName) {
        if (packageName == null || packageName.isBlank()) {
            throw new IllegalArgumentException("packageName is null or blank");
        }
        String packagePath = packageName.replace('.', '/');
        List<URL> locations = locationsOf(packagePath);

        Set<String> classNames = new TreeSet<>();
        for (URL location : locations) {
            if (!"file".equals(location.getProtocol())) {
                // Only directories are supported; JAR scanning is deliberately left out (see ADR-0002).
                throw new IllegalStateException("Unsupported classpath location: " + location);
            }
            collectFromDirectory(location, packagePath, classNames);
        }

        return classNames.stream()
                .<Class<?>>map(this::load)
                .filter(type -> !type.isAnonymousClass() && !type.isLocalClass() && !type.isSynthetic())
                .toList();
    }

    private List<URL> locationsOf(String packagePath) {
        try {
            return Collections.list(classLoader.getResources(packagePath));
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read classpath for " + packagePath, e);
        }
    }

    private static void collectFromDirectory(URL location, String packagePath, Set<String> sink) {
        Path directory = toPath(location);
        try (Stream<Path> files = Files.walk(directory)) {
            files
                    .filter(Files::isRegularFile)
                    .map(file -> directory.relativize(file).toString().replace(File.separatorChar, '/'))
                    .filter(ClassPathScanner::isClassFile)
                    .forEach(relative -> sink.add(toClassName(packagePath + "/" + relative)));
        } catch (IOException e) {
            throw new UncheckedIOException("Could not scan directory " + directory, e);
        }
    }

    private static Path toPath(URL location) {
        try {
            return Path.of(location.toURI());
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Invalid classpath location: " + location, e);
        }
    }

    private static boolean isClassFile(String path) {
        return path.endsWith(CLASS_FILE_SUFFIX)
                && !path.endsWith("package-info" + CLASS_FILE_SUFFIX)
                && !path.endsWith("module-info" + CLASS_FILE_SUFFIX);
    }

    /** {@code a/b/Foo$Bar.class} becomes {@code a.b.Foo$Bar}, the binary name Class.forName expects. */
    private static String toClassName(String classFilePath) {
        return classFilePath
                .substring(0, classFilePath.length() - CLASS_FILE_SUFFIX.length())
                .replace('/', '.');
    }

    private Class<?> load(String className) {
        try {
            return Class.forName(className, false, classLoader);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Class file found but class could not be loaded: " + className, e);
        }
    }
}
