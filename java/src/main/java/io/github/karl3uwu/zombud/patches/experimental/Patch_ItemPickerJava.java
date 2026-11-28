package io.github.karl3uwu.zombud.patches.experimental;

import io.github.karl3uwu.zombud.api.Patch;

public class Patch_ItemPickerJava {
    @Patch(className = "zombie.inventory.ItemPickerJava", methodName = "Parse")
    public static class Patch_Parse {
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
