package io.github.karl3uwu.zombud;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ModIdentityTest {

    @Test
    void names() {
        assertEquals("KRLZomBud", ModIdentity.MOD_ID);
        assertEquals("KRLZomBud.jar", ModIdentity.JAR_NAME);
        assertEquals("KRL ZomBud", ModIdentity.DISPLAY_NAME);
        assertEquals("ZombieBuddy", ModIdentity.LEGACY_MOD_ID);
        assertEquals("ZombieBuddy.jar", ModIdentity.LEGACY_JAR_NAME);
    }

    @Test
    void detectsUpstreamModId() {
        assertTrue(ModIdentity.isLegacyModId("ZombieBuddy"));
        assertTrue(ModIdentity.isLegacyModId(" ZombieBuddy "));
        assertFalse(ModIdentity.isLegacyModId("KRLZomBud"));
        assertFalse(ModIdentity.isLegacyModId("zombiebuddy"));
        assertFalse(ModIdentity.isLegacyModId(null));
    }
}
