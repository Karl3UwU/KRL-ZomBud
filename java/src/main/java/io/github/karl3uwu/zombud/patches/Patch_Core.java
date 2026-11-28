package io.github.karl3uwu.zombud.patches;

import io.github.karl3uwu.zombud.api.Patch;
import io.github.karl3uwu.zombud.api.Callbacks;

import zombie.core.Core;

public class Patch_Core {
    @Patch(className = "zombie.core.Core", methodName = "EndFrameUI")
    class Patch_Core_EndFrameUI {
        @Patch.OnEnter
        static void enter() {
            Callbacks.onEndFrameUI.run();
        }
    }
}
