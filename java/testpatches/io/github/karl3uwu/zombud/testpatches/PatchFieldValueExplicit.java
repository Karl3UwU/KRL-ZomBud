package io.github.karl3uwu.zombud.testpatches;

import io.github.karl3uwu.zombud.api.Patch;
import testjar.FieldValueTarget;

@Patch(className = "testjar.FieldValueTarget", methodName = "doSomethingExplicit")
public class PatchFieldValueExplicit {
    @Patch.OnEnter
    public static void enter(@Patch.This Object self, @Patch.Field(value = "name", readOnly = true) final String name_) {
        FieldValueTarget.capturedName = name_;
    }
}
