package io.github.karl3uwu.zombud.testpatches;

import io.github.karl3uwu.zombud.api.Patch;

@Patch(className = "testjar.FieldValueTarget", methodName = "incrementExplicit")
public class PatchRWFieldExplicit {
    @Patch.OnEnter
    public static void enter(@Patch.Field("counter") int cnt) {
        cnt++;
    }
}
