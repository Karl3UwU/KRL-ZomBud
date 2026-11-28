package io.github.karl3uwu.zombud;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Detects a second agent jar in the same JVM: another copy of {@code KRLZomBud.jar}, or upstream
 * {@code ZombieBuddy.jar} added with {@code -javaagent} next to KRL ZomBud started by {@code zbNative.dll}.
 *
 * <p>Two copies of KRL ZomBud contain the same classes, so only the first one on the class path runs. Upstream
 * ZombieBuddy lives in its own package ({@code me.zed_0xff.zombie_buddy}), so its agent runs <em>as well</em> and
 * both patch the game. This check makes either case visible in the log instead of leaving it silent.
 */
public final class AgentConflicts {
    private AgentConflicts() {}

    /** Class file of KRL ZomBud's agent. */
    static final String AGENT_CLASS_RESOURCE = "io/github/karl3uwu/zombud/Agent.class";

    /** Class file of upstream ZombieBuddy's agent. */
    static final String UPSTREAM_AGENT_CLASS_RESOURCE = "me/zed_0xff/zombie_buddy/Agent.class";

    /**
     * Every agent jar that provides either agent class: KRL ZomBud copies first (in class-path order, first one
     * wins), then upstream ZombieBuddy jars. Plain class directories (a development or test class path) are not
     * agent jars and are left out.
     */
    public static List<URL> agentClassLocations(ClassLoader loader) {
        if (loader == null) {
            return Collections.emptyList();
        }
        List<URL> out = new ArrayList<>();
        try {
            for (String resource : new String[] {AGENT_CLASS_RESOURCE, UPSTREAM_AGENT_CLASS_RESOURCE}) {
                for (URL url : Collections.list(loader.getResources(resource))) {
                    if ("jar".equals(url.getProtocol())) {
                        out.add(url);
                    }
                }
            }
        } catch (IOException e) {
            return Collections.emptyList();
        }
        return out;
    }

    static boolean isUpstream(URL url) {
        return url != null && url.toString().endsWith("!/" + UPSTREAM_AGENT_CLASS_RESOURCE);
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
        lines.add("More than one agent jar is on the class path:");
        for (URL url : locations) {
            String tag;
            if (isUpstream(url)) {
                tag = "[upstream ZombieBuddy, also running] ";
            } else if (active != null && active.toString().equals(url.toString())) {
                tag = "[active] ";
            } else {
                tag = "[ignored] ";
            }
            lines.add("  " + tag + url);
        }
        lines.add("Remove the extra -javaagent / launch option so only " + ModIdentity.JAR_NAME
            + " (via -agentlib:zbNative) is loaded. Upstream " + ModIdentity.LEGACY_JAR_NAME
            + " and " + ModIdentity.DISPLAY_NAME + " should not be installed at the same time.");
        return lines;
    }
}
