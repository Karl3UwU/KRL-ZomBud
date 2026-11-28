package io.github.karl3uwu.zombud.test;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import testjar.ArgumentOverrideTarget;

public class VanillaTestArgumentOverride {
    @Test
    void testMultiplyNoOverride() {
        // Original multiply(2, 3) should return 6 (unpatched)
        assertEquals(6, ArgumentOverrideTarget.multiply(2, 3));
    }
}
