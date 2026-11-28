package io.github.karl3uwu.zombud.patches.experimental;

import io.github.karl3uwu.zombud.Logger;
import io.github.karl3uwu.zombud.api.Patch;

public class Patch_DebugLog {
    @Patch(className = "zombie.debug.DebugLog", methodName = "init")
    public static class Patch_setOut {
        @Patch.OnExit
        public static void exit() {
            Logger.debug("after DebugLog.init");
        }
    }
}
