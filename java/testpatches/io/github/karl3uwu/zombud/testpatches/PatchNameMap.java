package io.github.karl3uwu.zombud.testpatches;

import java.util.HashMap;
import java.util.Map;

import io.github.karl3uwu.zombud.api.Patch;
import testjar.FieldValueTarget;

@Patch(className = "testjar.FieldValueTarget", methodName = "captureNameMap")
public class PatchNameMap {
    @Patch.NameMap
    public static Map<String, String> nameMap = new HashMap<>();

    @Patch.OnEnter
    public static void enter(
            @Patch.Field(value = {"logicalName", "name"}, readOnly = true) final String name,
            @Patch.Field(readOnly = true) final int counter
    ) {
        FieldValueTarget.capturedNameMap = nameMap;
    }
}
