package io.github.llewvallis.commandbuilder;

import lombok.Cleanup;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.regex.Pattern;

@Log
@RequiredArgsConstructor
/* package-private */ class JarAutoCommandSource implements AutoCommandSource {

    private final File file;
    private final Pattern pattern;

    @Override
    public Set<Class<?>> getClassesForScanning() {
        try {
            @Cleanup JarFile jarFile = new JarFile(file);

            Set<Class<?>> classes = new HashSet<>();
            for (Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements(); ) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();

                if (entryName.endsWith(".class")) {
                    String className = entryName
                            .substring(0, entryName.length() - ".class".length())
                            .replace("/", ".");

                    if (pattern.matcher(className).find()) {
                        try {
                            classes.add(Class.forName(className));
                        } catch (ClassNotFoundException | NoClassDefFoundError ignored) {
                        }
                    }
                }
            }

            return classes;
        } catch (IOException e) {
            log.log(Level.SEVERE, "Failed to read JAR " + file, e);
            return Set.of();
        }
    }
}
