package io.github.karl3uwu.zombud.testpatches;

import io.github.karl3uwu.zombud.api.Patch;

@Patch(className = "testjar.FieldValueTarget", methodName = "increment")
public class PatchRWField {
    @Patch.OnEnter
    public static void enter(@Patch.Field int counter) {
        counter++;
    }
}
