package io.github.karl3uwu.zombud.patches.experimental;

import io.github.karl3uwu.zombud.api.Patch;

/**
 * Patch IngameState.UpdateStuff() to poll Lua tasks.
 * Called every ~20ms during gameplay. Redundant with Patch_UIManager on client,
 * but kept for compatibility. Safe to call maybeRunLuaTasks() multiple times per frame.
 */
@Patch(className = "zombie.gameStates.IngameState", methodName = "UpdateStuff")
public class Patch_IngameState {
    @Patch.OnEnter
    public static void enter() {
        HttpServer.maybeRunLuaTasks();
    }
}
