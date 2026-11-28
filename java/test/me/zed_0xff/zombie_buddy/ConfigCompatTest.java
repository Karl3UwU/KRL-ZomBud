package me.zed_0xff.zombie_buddy;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ConfigCompatTest {

    @Test
    void oldConfigWithStealthModeStillLoads() {
        // config.json written by ZombieBuddy / earlier builds, before stealth mode was removed.
        String json = "{\n"
            + "  \"trusted_authors\": [],\n"
            + "  \"preload_mods\": {},\n"
            + "  \"auto_fix_mod_order\": false,\n"
            + "  \"fix_approval_dialog_cursor\": true,\n"
            + "  \"fix_macos_retina\": true,\n"
            + "  \"stealth_mode\": true\n"
            + "}";
        Config config = ZBGson.PRETTY.fromJson(json, Config.class);
        assertNotNull(config);
        assertFalse(config.auto_fix_mod_order());
        assertTrue(config.fix_approval_dialog_cursor());
        assertTrue(config.fix_macos_retina());
        assertFalse(ZBGson.PRETTY.toJson(config).contains("stealth_mode"), "the old field is dropped on save");
    }
}
