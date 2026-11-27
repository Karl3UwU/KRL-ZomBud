package io.github.karl3uwu.zombud;

import java.util.List;

/**
 * Names that identify KRL ZomBud, and the compatibility mapping from upstream ZombieBuddy's mod id.
 *
 * <p>KRL ZomBud ships as mod id {@value #MOD_ID} with agent jar {@value #JAR_NAME}. Existing Java mods declare
 * {@code require=\ZombieBuddy}; the game resolves that by mod id, so the loader rewrites a requirement on
 * {@value #LEGACY_MOD_ID} to {@value #MOD_ID} when a mod.info is read (see {@code Patch_ModRequireAlias}).
 * See {@code doc/RENAME.md}.
 */
public final class ModIdentity {
    private ModIdentity() {}

    /** Human-readable product name. */
    public static final String DISPLAY_NAME = "KRL ZomBud";

    /** Mod id in {@code mod.info} (and the folder name under {@code mods/}). */
    public static final String MOD_ID = "KRLZomBud";

    /** File name of the agent jar, in the mod's {@code libs/} folder and in the game folder. */
    public static final String JAR_NAME = MOD_ID + ".jar";

    /** Upstream ZombieBuddy's mod id, still used by existing mods in {@code require=}. */
    public static final String LEGACY_MOD_ID = "ZombieBuddy";

    /** Upstream ZombieBuddy's agent jar name. */
    public static final String LEGACY_JAR_NAME = LEGACY_MOD_ID + ".jar";

    public static boolean isLegacyModId(String modId) {
        return modId != null && modId.trim().equals(LEGACY_MOD_ID);
    }

    /**
     * Rewrites a mod's {@code require} list in place so that a requirement on {@value #LEGACY_MOD_ID} is
     * satisfied by {@value #MOD_ID}. Other entries, including their surrounding whitespace, are kept as they are.
     * If {@value #MOD_ID} is already required the legacy entry is dropped instead of duplicated.
     *
     * @return {@code true} if the list changed
     */
    public static boolean rewriteLegacyRequires(List<String> require) {
        if (require == null || require.isEmpty()) {
            return false;
        }
        boolean hasModId = false;
        for (String id : require) {
            if (id != null && id.trim().equals(MOD_ID)) {
                hasModId = true;
                break;
            }
        }
        boolean changed = false;
        for (int i = 0; i < require.size(); i++) {
            if (!isLegacyModId(require.get(i))) {
                continue;
            }
            if (hasModId) {
                require.remove(i--);
            } else {
                require.set(i, MOD_ID);
                hasModId = true;
            }
            changed = true;
        }
        return changed;
    }
}
