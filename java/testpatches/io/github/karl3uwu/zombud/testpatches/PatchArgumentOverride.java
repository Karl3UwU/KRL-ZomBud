package io.github.karl3uwu.zombud.testpatches;

import io.github.karl3uwu.zombud.api.Patch;

public class PatchArgumentOverride {
    @Patch(className = "testjar.ArgumentOverrideTarget", methodName = "multiply")
    public static class MultiplyPatch {
        @Patch.OnEnter
        public static void enter(@Patch.Argument(value = 1, readOnly = false) int b) {
            b = 10;
        }
    }
}
