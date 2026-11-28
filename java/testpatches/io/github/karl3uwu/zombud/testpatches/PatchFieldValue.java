package io.github.karl3uwu.zombud.testpatches;

import io.github.karl3uwu.zombud.api.Patch;
import testjar.FieldValueTarget;

@Patch(className = "testjar.FieldValueTarget", methodName = "doSomething")
public class PatchFieldValue {
    @Patch.OnEnter
    public static void enter(@Patch.This Object self, @Patch.Field(readOnly = true) final String name) {
        FieldValueTarget.capturedName = name;
    }
}
