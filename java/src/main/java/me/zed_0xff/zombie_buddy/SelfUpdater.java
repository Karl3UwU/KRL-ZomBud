package me.zed_0xff.zombie_buddy;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * Self-updating is DISABLED in KRL ZomBud.
 *
 * <p>Upstream ZombieBuddy replaced its own agent jar with any newer jar signed by the upstream maintainer's
 * certificate (a hardcoded SHA-256 fingerprint). In this fork that would let an upstream-signed
 * {@code ZombieBuddy.jar} silently replace ours, so the replacement code, the certificate pin and the native
 * shim's deferred {@code .jar.new} swap have been removed. Nothing here writes, renames or replaces the agent jar.
 *
 * <p>Updating stays manual (installer or copying the jar) until KRL ZomBud has its own release signing
 * (roadmap phase 5). Only the read-only helpers the loader still uses for log messages remain.
 */
public final class SelfUpdater {
    private SelfUpdater() {}

    /** Always {@code false}: self-updating is disabled until KRL ZomBud has its own release signing. */
    public static final boolean ENABLED = false;

    /** No update is ever pending, so this always returns {@code null}. */
    public static String getNewVersion() {
        return null;
    }

    /**
     * Describes the agent's own mod jar for the "skipping normal mod loading" log line.
     * Read-only: reports the jar's version and never updates anything.
     */
    public static String getExclusionReasonSuffix(Path jarPath) {
        if (jarPath == null) {
            return " (null not found)";
        }
        if (!Files.isRegularFile(jarPath)) {
            return " (" + jarPath.toAbsolutePath() + " not found)";
        }
        Manifest manifest = getJarManifest(jarPath);
        if (manifest != null) {
            String manifestVersion = manifest.getMainAttributes().getValue("Implementation-Version");
            if (manifestVersion != null) {
                return " (version " + manifestVersion + ")";
            }
        }
        return "";
    }

    public static Manifest getJarManifest(Path jarPath) {
        if (jarPath == null) {
            return null;
        }
        try (JarFile jar = new JarFile(jarPath.toFile(), false)) {
            return jar.getManifest();
        } catch (Exception e) {
            Logger.error("Error getting JAR manifest: " + e);
            return null;
        }
    }
}
