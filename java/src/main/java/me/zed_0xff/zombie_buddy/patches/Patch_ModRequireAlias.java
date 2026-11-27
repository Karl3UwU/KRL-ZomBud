package me.zed_0xff.zombie_buddy.patches;

import java.util.ArrayList;

import io.github.karl3uwu.zombud.ModIdentity;
import me.zed_0xff.zombie_buddy.Logger;
import me.zed_0xff.zombie_buddy.annotations.Patch;

/**
 * Lets existing mods that declare {@code require=\ZombieBuddy} work with KRL ZomBud (mod id {@code KRLZomBud}).
 *
 * <p>The game resolves {@code require=} by mod id: {@code ChooseGameInfo.readModInfoAux()} parses the line and
 * passes the list to {@code Mod.setRequire()}; the mod selector, {@code Mod.isAvailableRequired()} and
 * {@code ZomboidFileSystem.loadModAndRequired()} all read it back through {@code getRequire()}. Rewriting the list
 * once, at {@code setRequire()}, makes every one of those see {@code KRLZomBud} instead of {@code ZombieBuddy}.
 *
 * <p>Lives in the legacy package because bundled patches are discovered there; the logic is in
 * {@link ModIdentity#rewriteLegacyRequires}. See {@code doc/RENAME.md}.
 */
public class Patch_ModRequireAlias {
    @Patch(className = "zombie.gameStates.ChooseGameInfo$Mod", methodName = "setRequire")
    public static class Patch_setRequire {
        @Patch.OnEnter
        public static void enter(ArrayList<String> require) {
            try {
                if (ModIdentity.rewriteLegacyRequires(require)) {
                    Logger.debug("mapped require=" + ModIdentity.LEGACY_MOD_ID + " to " + ModIdentity.MOD_ID);
                }
            } catch (RuntimeException e) {
                // An immutable or odd list must never break reading mod.info.
                Logger.warn("could not map require=" + ModIdentity.LEGACY_MOD_ID + ": " + e);
            }
        }
    }
}
