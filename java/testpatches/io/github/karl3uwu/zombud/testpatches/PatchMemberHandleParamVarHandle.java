package io.github.karl3uwu.zombud.testpatches;

import java.lang.invoke.VarHandle;

import io.github.karl3uwu.zombud.api.Patch;

// verifies: @VarHandle on a VarHandle parameter resolves and allows field writes via the parameter
@Patch(className = "testjar.MemberHandleTarget", methodName = "doParamVarHandle")
public class PatchMemberHandleParamVarHandle {
    @Patch.OnEnter
    public static void enter(
            @Patch.VarHandle(className = "testjar.MemberHandleHelper", type = String.class) VarHandle capturedParamVarHandle
    ) throws Throwable {
        capturedParamVarHandle.set("via_param_varhandle");
    }
}
