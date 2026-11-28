package me.zed_0xff.zombie_buddy;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class VersionCheckTest {
    @Test
    void compareVersions_matchesVersionCheckTestExpectations() {
        assertEquals(0, Utils.compareVersions("1.0.0", "1.0.0"));
        assertEquals(1, Utils.compareVersions("1.1.0", "1.0.0"));
        assertEquals(-1, Utils.compareVersions("1.0.0", "1.1.0"));
        assertEquals(1, Utils.compareVersions("1.10.0", "1.2.0"));
        assertEquals(-1, Utils.compareVersions("1.2.0", "1.10.0"));
        assertEquals(0, Utils.compareVersions("1.0.0-beta", "1.0.0"));
        assertEquals(1, Utils.compareVersions("2.0", "1.9.9"));
        assertEquals(-1, Utils.compareVersions("unknown", "1.0.0"));
        assertEquals(1, Utils.compareVersions("1.0.0", "unknown"));
    }

    @Test
    void compareVersionsForUpdate_ordersAlphaBetaAndReleaseVersions() {
        assertEquals(1, Utils.compareVersionsForUpdate("1.2.3-beta", "1.2.3-alpha"));
        assertEquals(1, Utils.compareVersionsForUpdate("1.2.3-beta1", "1.2.3-beta"));
        assertEquals(1, Utils.compareVersionsForUpdate("1.2.3-beta1", "1.2.3-beta0"));
        assertEquals(1, Utils.compareVersionsForUpdate("1.2.3-beta2", "1.2.3-beta"));
        assertEquals(1, Utils.compareVersionsForUpdate("1.2.3", "1.2.3-alpha"));
        assertEquals(1, Utils.compareVersionsForUpdate("1.2.3", "1.2.3-alpha1"));
        assertEquals(1, Utils.compareVersionsForUpdate("1.2.3", "1.2.3-beta"));
        assertEquals(1, Utils.compareVersionsForUpdate("1.2.3", "1.2.3-beta2"));
        assertEquals(-1, Utils.compareVersionsForUpdate("1.2.3-alpha", "1.2.3"));
        assertEquals(-1, Utils.compareVersionsForUpdate("1.2.3-beta2", "1.2.3"));
    }

    @Test
    void isVersionNewer_equalVersions_returnsFalse() {
        assertFalse(Utils.isVersionNewer("1.0.0", "1.0.0"));
        assertFalse(Utils.isVersionNewer("1.0.0-beta", "1.0.0"));
    }

    @Test
    void isVersionNewer_firstNewer_returnsTrue() {
        assertTrue(Utils.isVersionNewer("1.1.0", "1.0.0"));
        assertTrue(Utils.isVersionNewer("1.10.0", "1.2.0"));
        assertTrue(Utils.isVersionNewer("2.0", "1.9.9"));
        assertTrue(Utils.isVersionNewer("1.0.0", "unknown"));
        assertTrue(Utils.isVersionNewer("1.2.3-beta", "1.2.3-alpha"));
        assertTrue(Utils.isVersionNewer("1.2.3-beta1", "1.2.3-beta"));
        assertTrue(Utils.isVersionNewer("1.2.3", "1.2.3-beta2"));
    }

    @Test
    void isVersionNewer_firstOlder_returnsFalse() {
        assertFalse(Utils.isVersionNewer("1.0.0", "1.1.0"));
        assertFalse(Utils.isVersionNewer("1.2.0", "1.10.0"));
        assertFalse(Utils.isVersionNewer("unknown", "1.0.0"));
    }

    @Test
    void testIsVersionInRange() {
        // Current version is 1.2.3
        String current = "1.2.3";
        
        // No limits
        assertTrue(JavaModInfo.isVersionInRange(current, null, null));
        assertTrue(JavaModInfo.isVersionInRange(current, "", ""));
        
        // Min limit
        assertTrue(JavaModInfo.isVersionInRange(current, "1.0.0", null));
        assertTrue(JavaModInfo.isVersionInRange(current, "1.2.3", null));
        assertFalse(JavaModInfo.isVersionInRange(current, "1.2.4", null));
        assertFalse(JavaModInfo.isVersionInRange(current, "2.0.0", null));
        
        // Max limit
        assertTrue(JavaModInfo.isVersionInRange(current, null, "2.0.0"));
        assertTrue(JavaModInfo.isVersionInRange(current, null, "1.2.3"));
        assertFalse(JavaModInfo.isVersionInRange(current, null, "1.2.2"));
        assertFalse(JavaModInfo.isVersionInRange(current, null, "1.0.0"));
        
        // Both limits
        assertTrue(JavaModInfo.isVersionInRange(current, "1.0.0", "2.0.0"));
        assertTrue(JavaModInfo.isVersionInRange(current, "1.2.3", "1.2.3"));
        assertFalse(JavaModInfo.isVersionInRange(current, "1.2.4", "2.0.0"));
        assertFalse(JavaModInfo.isVersionInRange(current, "1.0.0", "1.2.2"));

        // A prerelease sorts before its release (same order as Utils.compareVersionsForUpdate).
        assertFalse(JavaModInfo.isVersionInRange("1.2.3-beta", "1.2.3", "1.2.3"));
    }

    @Test
    void isVersionInRange_ordersPrereleases() {
        // min: a prerelease of the min version is too old
        assertFalse(JavaModInfo.isVersionInRange("3.0.0-beta3", "3.0.0", null));
        assertTrue (JavaModInfo.isVersionInRange("3.0.0",       "3.0.0", null));
        assertTrue (JavaModInfo.isVersionInRange("3.0.1-beta1", "3.0.0", null));
        assertTrue (JavaModInfo.isVersionInRange("3.0.0-beta3", "3.0.0-beta3", null));
        assertTrue (JavaModInfo.isVersionInRange("3.0.0-beta3", "3.0.0-beta2", null));
        assertFalse(JavaModInfo.isVersionInRange("3.0.0-beta3", "3.0.0-beta4", null));
        assertTrue (JavaModInfo.isVersionInRange("3.0.0-beta1", "3.0.0-alpha9", null));

        // max: a prerelease of the max version is still in range, the next patch is not
        assertTrue (JavaModInfo.isVersionInRange("3.0.0-beta3", null, "3.0.0"));
        assertTrue (JavaModInfo.isVersionInRange("3.0.0",       null, "3.0.0"));
        assertFalse(JavaModInfo.isVersionInRange("3.0.1-beta1", null, "3.0.0"));
        assertFalse(JavaModInfo.isVersionInRange("3.0.0",       null, "3.0.0-beta3"));
        assertFalse(JavaModInfo.isVersionInRange("3.0.0-beta10", null, "3.0.0-beta9"));

        // prerelease-only window
        assertTrue (JavaModInfo.isVersionInRange("3.0.0-beta3", "3.0.0-beta1", "3.0.0-beta9"));
        assertFalse(JavaModInfo.isVersionInRange("3.0.0-alpha", "3.0.0-beta1", "3.0.0-beta9"));

        // unknown own version (dev build) always passes
        assertTrue (JavaModInfo.isVersionInRange("unknown", "9.0.0", "9.9.9"));
        assertTrue (JavaModInfo.isVersionInRange(null,      "9.0.0", "9.9.9"));
    }
}
