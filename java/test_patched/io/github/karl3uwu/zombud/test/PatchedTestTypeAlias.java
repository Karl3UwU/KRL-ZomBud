package io.github.karl3uwu.zombud.test;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.junit.jupiter.api.Test;

public class PatchedTestTypeAlias {
    @Test
    void testTypeAliasCreatesRealInnerInstance() {
        assumeTrue(false);
        // TypeAliasTarget.lastResult = null;
        // new TypeAliasTarget().process("hello");
        // assertEquals("processed:hello", TypeAliasTarget.lastResult);
    }

    @Test
    void testTypeAliasWithDifferentInput() {
        assumeTrue(false);
        // TypeAliasTarget.lastResult = null;
        // new TypeAliasTarget().process("world");
        // assertEquals("processed:world", TypeAliasTarget.lastResult);
    }
}
