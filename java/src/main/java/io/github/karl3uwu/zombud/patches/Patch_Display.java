package io.github.karl3uwu.zombud.patches;

import io.github.karl3uwu.zombud.api.Callbacks;
import io.github.karl3uwu.zombud.api.Patch;

public class Patch_Display {
    @Patch(className = "org.lwjglx.opengl.Display", methodName = "create")
    public static class Patch_create {
        @Patch.OnExit
        public static void exit() {
            Callbacks.onDisplayCreate.run();
        }
    }
}
