package io.github.karl3uwu.zombud;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

class AgentConflictsTest {

    @TempDir
    Path tempDir;

    private Path agentJar(String name) throws Exception {
        Path jar = tempDir.resolve(name);
        try (OutputStream out = Files.newOutputStream(jar); JarOutputStream jos = new JarOutputStream(out)) {
            jos.putNextEntry(new JarEntry(AgentConflicts.AGENT_CLASS_RESOURCE));
            jos.write(new byte[] {(byte) 0xCA, (byte) 0xFE});
            jos.closeEntry();
        }
        return jar;
    }

    @Test
    void singleJarIsNoConflict() throws Exception {
        try (URLClassLoader loader = new URLClassLoader(new URL[] {agentJar("KRLZomBud.jar").toUri().toURL()}, null)) {
            List<URL> locations = AgentConflicts.agentClassLocations(loader);
            assertEquals(1, locations.size());
            assertTrue(AgentConflicts.describeConflict(locations, locations.get(0)).isEmpty());
        }
    }

    @Test
    void twoJarsAreReportedWithTheActiveOneMarked() throws Exception {
        URL ours = agentJar("KRLZomBud.jar").toUri().toURL();
        URL upstream = agentJar("ZombieBuddy.jar").toUri().toURL();
        try (URLClassLoader loader = new URLClassLoader(new URL[] {upstream, ours}, null)) {
            List<URL> locations = AgentConflicts.agentClassLocations(loader);
            assertEquals(2, locations.size());
            assertTrue(locations.get(0).toString().contains("ZombieBuddy.jar"), "class-path order is kept");

            List<String> lines = AgentConflicts.describeConflict(locations, locations.get(0));
            assertFalse(lines.isEmpty());
            String text = String.join("\n", lines);
            assertTrue(text.contains("[active] " + locations.get(0)));
            assertTrue(text.contains("[ignored] " + locations.get(1)));
        }
    }

    @Test
    void nullLoaderIsSafe() {
        assertTrue(AgentConflicts.agentClassLocations(null).isEmpty());
        assertTrue(AgentConflicts.describeConflict(null, null).isEmpty());
    }
}
