package io.github.karl3uwu.zombud.testpatches;

import io.github.karl3uwu.zombud.api.Patch;
import testjar.FieldValueTarget;

@Patch(className = "testjar.FieldValueTarget", methodName = "readNameAsObject")
public class PatchFieldValueStringAsObject {
    @Patch.OnEnter
    public static void enter(@Patch.This Object self, @Patch.Field(readOnly = true) final Object name) {
        FieldValueTarget.capturedCounter = name;
    }
}
