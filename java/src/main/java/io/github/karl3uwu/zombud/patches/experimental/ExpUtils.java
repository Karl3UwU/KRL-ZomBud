package io.github.karl3uwu.zombud.patches.experimental;

import io.github.karl3uwu.zombud.Utils;

// separate from Utils for less dependencies (ZomboidFileSystem)
public final class ExpUtils {
    private ExpUtils() {}

    /**
     * Returns the console log file path for the current process.
     * Server uses Server/server-console.txt, client/SP use console.txt.
     */
    public static String getConsoleLogPath() {
        String cacheDir = Utils.getCacheDir();
        if (Utils.isServer()) {
            return cacheDir + "/server-console.txt";
        }
        return cacheDir + "/console.txt";
    }
}
