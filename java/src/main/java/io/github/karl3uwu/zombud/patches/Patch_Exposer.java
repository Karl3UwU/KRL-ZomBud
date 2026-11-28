package io.github.karl3uwu.zombud.patches;

import io.github.karl3uwu.zombud.api.Callbacks;
import io.github.karl3uwu.zombud.api.Patch;

public class Patch_Exposer {
    @Patch(className = "zombie.Lua.LuaManager$Exposer", methodName = "exposeAll", warmUp = true/*, IKnowWhatIAmDoing = true*/)
    public static class Patch_exposeAll {
        @Patch.OnExit
        public static void exit() {
            Callbacks.afterExposeAll.run();
        }
    }
}
