package io.github.karl3uwu.zombud.testpatches;

import io.github.karl3uwu.zombud.api.Patch;

public class PatchOverloadedDrawText {
    @Patch(className = "testjar.OverloadedDrawTextTarget", methodName = "DrawText")
    public static class DrawTextPatch {
        @Patch.OnEnter
        public static void enter(@Patch.Argument(value = 1, readOnly = false) String text) {
            if (text != null) {
                text = "patched:" + text;
            }
        }
    }
}
