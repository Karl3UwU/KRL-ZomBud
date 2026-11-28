package io.github.karl3uwu.zombud.patches.experimental;

import io.github.karl3uwu.zombud.api.Patch;

// called by server every 100ms even if game is paused (no players connected)
@Patch(className = "zombie.network.ServerMap", methodName = "preupdate")
public class Patch_ServerMap {
    @Patch.OnEnter
    public static void enter() {
        HttpServer.maybeRunLuaTasks();
    }
}
