package io.github.karl3uwu.zombud.testpatches;

import io.github.karl3uwu.zombud.api.Patch;

public class PatchSkipOn {
    @Patch(className = "testjar.SkipOnTarget", methodName = "testMethod")
    public static class SkipOnPatch {
        @Patch.OnEnter(skipOn = true)
        public static boolean enter() {
            return true; // Should skip original method
        }
    }
}
