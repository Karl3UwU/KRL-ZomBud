package io.github.karl3uwu.zombud.patches;

import io.github.karl3uwu.zombud.api.Callbacks;
import io.github.karl3uwu.zombud.api.Patch;

@Patch(className = "zombie.network.GameServer", methodName = "startServer")
public class Patch_GameServer {
    @Patch.OnEnter
    static void enter() {
        Callbacks.onGameInitComplete.run();
    }
}
