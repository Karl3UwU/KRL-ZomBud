package io.github.karl3uwu.zombud.patches.experimental;

import io.github.karl3uwu.zombud.api.Patch;

public class Patch_IsoWorld {
    @Patch(className = "zombie.iso.IsoWorld", methodName = "init")
    public static class Patch_init {
        @Patch.OnEnter
        public static void enter() {
            HttpServer.maybeRunLuaTasks();
        }

        @Patch.OnExit
        public static void exit() {
            HttpServer.maybeRunLuaTasks();
        }
    }
}
