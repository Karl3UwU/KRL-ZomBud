package io.github.karl3uwu.zombud;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Detects a second ZombieBuddy-family agent jar in the same JVM, e.g. upstream {@code ZombieBuddy.jar} added
 * with {@code -javaagent} next to KRL ZomBud started by {@code zbNative.dll}.
 *
 * <p>Both jars contain the same {@code me.zed_0xff.zombie_buddy} classes, so the JVM uses the classes of whichever
 * jar comes first on the class path and the other agent's {@code premain} runs that same code (it is then ignored
 * as a duplicate). Which product actually runs is therefore down to command-line order; this check makes that
 * visible in the log instead of leaving it silent.
 */
public final class AgentConflicts {
    private AgentConflicts() {}

    /** Class file present in every ZombieBuddy-family agent jar. */
    static final String AGENT_CLASS_RESOURCE = "me/zed_0xff/zombie_buddy/Agent.class";

    /**
     * Every agent jar that provides the agent class, in class-path order (first one wins). Plain class directories
     * (a development or test class path) are not agent jars and are left out.
     */
    public static List<URL> agentClassLocations(ClassLoader loader) {
        if (loader == null) {
            return Collections.emptyList();
        }
        List<URL> out = new ArrayList<>();
        try {
            for (URL url : Collections.list(loader.getResources(AGENT_CLASS_RESOURCE))) {
                if ("jar".equals(url.getProtocol())) {
                    out.add(url);
                }
            }
        } catch (IOException e) {
            return Collections.emptyList();
        }
        return out;
    }

    /**
     * Returns warning lines to log when more than one agent jar is present, or an empty list when there is no
     * conflict. {@code active} is the location the running agent class was loaded from (may be {@code null}).
     */
    public static List<String> describeConflict(List<URL> locations, URL active) {
        if (locations == null || locations.size() < 2) {
            return Collections.emptyList();
        }
        List<String> lines = new ArrayList<>();
        lines.add("More than one ZombieBuddy-family agent jar is on the class path; only the first one runs:");
        for (URL url : locations) {
            boolean isActive = active != null && active.toString().equals(url.toString());
            lines.add("  " + (isActive ? "[active] " : "[ignored] ") + url);
        }
        lines.add("Remove the extra -javaagent / launch option so only " + ModIdentity.JAR_NAME
            + " (via -agentlib:zbNative) is loaded. Upstream " + ModIdentity.LEGACY_JAR_NAME
            + " and " + ModIdentity.DISPLAY_NAME + " should not be installed at the same time.");
        return lines;
    }
}
