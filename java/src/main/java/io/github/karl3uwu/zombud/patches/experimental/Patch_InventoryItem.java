package io.github.karl3uwu.zombud.patches.experimental;

import io.github.karl3uwu.zombud.api.Patch;

import zombie.inventory.InventoryItem;

// show mod id instead of mod name in item tooltip
@Patch(className = "zombie.inventory.InventoryItem", methodName = "getModName")
public class Patch_InventoryItem {
    @Patch.OnExit
    public static void exit(
        @Patch.This Object self,
        @Patch.Return(readOnly = false) String result
    ) {
        result = ((InventoryItem) self).getModID();
    }
}
