if not PZAPI or not PZAPI.ModOptions then return end

local options = PZAPI.ModOptions:create("ZombieBuddy", "KRL ZomBud") -- id kept: it keys the saved options

local config = {
    watermarkOpacity   = options:addSlider( "watermarkOpacity",    "UI_KRL_WatermarkOpacity", 0.0, 1.0, 0.05, 0.4 ),
    suppressSandboxLog = options:addTickBox( "suppressSandboxLog", "UI_KRL_SuppressSandboxLog", false, "UI_KRL_SuppressSandboxLog_desc" ),
    autoFixModOrder           = options:addTickBox( "autoFixModOrder",           "UI_KRL_AutoFixModOrder",           true, "UI_KRL_AutoFixModOrder_desc" ),
    fixApprovalDialogCursor   = options:addTickBox( "fixApprovalDialogCursor",   "UI_KRL_FixApprovalDialogCursor",   true, "UI_KRL_FixApprovalDialogCursor_desc" ),
    fixMacOSRetina            = options:addTickBox( "fixMacOSRetina",            "UI_KRL_FixMacOSRetina",            false, "UI_KRL_FixMacOSRetina_desc" ),
}

local function onChangeWatermarkOpacity(self, value)
    if KRLZomBud and KRLZomBud.Watermark and KRLZomBud.Watermark.setAlpha then
        KRLZomBud.Watermark.setAlpha(value)
    end
end

config.watermarkOpacity.onChange = onChangeWatermarkOpacity

-- ---------------------------------------------------------------------------
-- Suppress sandbox options logging (GameLoadingState.exit)
-- ---------------------------------------------------------------------------

local function applySettings()
    if KRLZomBud then
        if KRLZomBud.setAutoFixModOrder then
            KRLZomBud.setAutoFixModOrder(config.autoFixModOrder:getValue())
        end
        if KRLZomBud.setFixApprovalDialogCursor then
            KRLZomBud.setFixApprovalDialogCursor(config.fixApprovalDialogCursor:getValue())
        end
        if KRLZomBud.setFixMacOSRetina then
            KRLZomBud.setFixMacOSRetina(config.fixMacOSRetina:getValue())
        end
        if KRLZomBud.Watermark and KRLZomBud.Watermark.setAlpha then
            KRLZomBud.Watermark.setAlpha(config.watermarkOpacity:getValue())
        end
        if KRLZomBud.Patches and KRLZomBud.Patches.GameLoadingState and KRLZomBud.Patches.GameLoadingState.setSuppressSandboxLog then
            KRLZomBud.Patches.GameLoadingState.setSuppressSandboxLog(config.suppressSandboxLog:getValue())
        end
    end
end

options.apply = applySettings

Events.OnMainMenuEnter.Add(applySettings) -- apply settings at game launch
