package me.zed_0xff.zombie_buddy.test;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * The bundled Patch_ModRequireAlias must rewrite require=\ZombieBuddy to KRLZomBud in the real game class.
 * The game jar is only on this suite's runtime class path, so the class is used reflectively. The instance is
 * allocated without running its constructor (which needs ZomboidFileSystem); setRequire/getRequire are plain.
 */
public class PatchedTestModRequireAlias {
    @Test
    @SuppressWarnings("unchecked")
    void requireOnZombieBuddyIsMappedToKRLZomBud() throws Exception {
        Class<?> modClass = Class.forName("zombie.gameStates.ChooseGameInfo$Mod");

        Field unsafeField = Class.forName("sun.misc.Unsafe").getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        Object unsafe = unsafeField.get(null);
        Object mod = unsafe.getClass().getMethod("allocateInstance", Class.class).invoke(unsafe, modClass);

        Method setRequire = modClass.getMethod("setRequire", ArrayList.class);
        Method getRequire = modClass.getMethod("getRequire");

        setRequire.invoke(mod, new ArrayList<>(List.of("ZombieBuddy", " OtherMod")));
        assertEquals(List.of("KRLZomBud", " OtherMod"), (List<String>) getRequire.invoke(mod));

        setRequire.invoke(mod, new ArrayList<>(List.of("OtherMod")));
        assertEquals(List.of("OtherMod"), (List<String>) getRequire.invoke(mod));
    }
}
