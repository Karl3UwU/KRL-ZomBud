package io.github.karl3uwu.zombud;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class ModIdentityTest {

    private static List<String> list(String... ids) {
        return new ArrayList<>(Arrays.asList(ids));
    }

    @Test
    void names() {
        assertEquals("KRLZomBud", ModIdentity.MOD_ID);
        assertEquals("KRLZomBud.jar", ModIdentity.JAR_NAME);
        assertEquals("KRL ZomBud", ModIdentity.DISPLAY_NAME);
        assertEquals("ZombieBuddy", ModIdentity.LEGACY_MOD_ID);
    }

    @Test
    void rewritesLegacyRequirement() {
        List<String> require = list("ZombieBuddy");
        assertTrue(ModIdentity.rewriteLegacyRequires(require));
        assertEquals(List.of("KRLZomBud"), require);
    }

    @Test
    void keepsOtherEntriesAndTheirWhitespace() {
        // "require=\ZombieBuddy, \OtherMod" is parsed by the game as ["ZombieBuddy", " OtherMod"].
        List<String> require = list("ZombieBuddy", " OtherMod");
        assertTrue(ModIdentity.rewriteLegacyRequires(require));
        assertEquals(List.of("KRLZomBud", " OtherMod"), require);

        require = list("OtherMod", " ZombieBuddy ");
        assertTrue(ModIdentity.rewriteLegacyRequires(require));
        assertEquals(List.of("OtherMod", "KRLZomBud"), require);
    }

    @Test
    void doesNotDuplicate() {
        List<String> require = list("KRLZomBud", "ZombieBuddy", "Other");
        assertTrue(ModIdentity.rewriteLegacyRequires(require));
        assertEquals(List.of("KRLZomBud", "Other"), require);

        require = list("ZombieBuddy", "ZombieBuddy");
        assertTrue(ModIdentity.rewriteLegacyRequires(require));
        assertEquals(List.of("KRLZomBud"), require);
    }

    @Test
    void leavesUnrelatedListsAlone() {
        List<String> require = list("zombiebuddy", "ZombieBuddyAddon", "KRLZomBud");
        assertFalse(ModIdentity.rewriteLegacyRequires(require));
        assertEquals(List.of("zombiebuddy", "ZombieBuddyAddon", "KRLZomBud"), require);
        assertFalse(ModIdentity.rewriteLegacyRequires(null));
        assertFalse(ModIdentity.rewriteLegacyRequires(new ArrayList<>()));
    }
}
