# Rename: ZombieBuddy → KRL ZomBud

KRL ZomBud is a fork of ZombieBuddy (by Zed, MIT). It installs under its own names so it can't be confused with, or
overwrite, upstream's Workshop item:

| | Upstream ZombieBuddy | KRL ZomBud |
|---|---|---|
| Display name | ZombieBuddy | KRL ZomBud |
| Mod id (`mod.info`, `mods/<id>`) | `ZombieBuddy` | `KRLZomBud` |
| Agent jar | `ZombieBuddy.jar` | `KRLZomBud.jar` |
| Native shim | `zbNative.dll`, `-agentlib:zbNative` | unchanged |
| Java package of existing classes | `me.zed_0xff.zombie_buddy` | unchanged (compatibility) |
| Java package for new code | — | `io.github.karl3uwu.zombud` |
| Lua API (`ZombieBuddy.*`), Lua file names, mod options id | | unchanged |
| Config dir | `~/.zombie_buddy` | unchanged |
| Version used by `ZBVersionMin`/`ZBVersionMax` | | unchanged (`java/VERSION`) |

The names are defined in one place per language: `ModIdentity` (Java), `MOD_ID` in `java/build.gradle`, `ZB_JAR` in
`c/windows/zbNative.c` and `KRL_MOD_ID` in `installer/installer.go`. Keep them in sync.

## Mods that `require=\ZombieBuddy`

The game resolves `require=` by mod id: `ChooseGameInfo.readModInfoAux()` parses the line and calls
`Mod.setRequire()`, and everything else (the mod selector, `Mod.isAvailableRequired()`,
`ZomboidFileSystem.loadModAndRequired()`) reads the list back through `getRequire()`.

KRL ZomBud patches `ChooseGameInfo$Mod.setRequire()` (`patches/Patch_ModRequireAlias.java`) and rewrites a
`ZombieBuddy` entry to `KRLZomBud` before it is stored (`ModIdentity.rewriteLegacyRequires`). So:

- a mod with `require=\ZombieBuddy` is available and loads KRL ZomBud's mod as its dependency;
- if a mod requires both, the entry appears once;
- other entries are left exactly as they were.

The rewrite needs the agent, which is always the case when KRL ZomBud's Java code runs at all. Without the agent such
mods show as missing their requirement, just as they would without upstream ZombieBuddy.

Only `require=` is mapped. A server's `Mods=` list or a saved mod list that names `ZombieBuddy` directly still means
upstream's mod; switch those to `KRLZomBud`.

`ZBVersionMin`/`ZBVersionMax` are compared with `java/VERSION`, which still follows ZombieBuddy's version line
(`3.0.0-beta3`), so existing version ranges keep working.

## If upstream ZombieBuddy is installed too

- **Upstream mod enabled next to `KRLZomBud`.** Its `mod.info` declares the same `javaPkgName`, so the loader treats
  its jar as itself and never loads it, and logs a warning saying so. Its Lua files have the same paths as ours, so
  the game keeps only one copy. Requirements on `ZombieBuddy` resolve to `KRLZomBud`.
- **Two agent jars in one JVM** (e.g. `-javaagent:ZombieBuddy.jar` next to `-agentlib:zbNative`). Both jars contain
  the same classes, so only the first one on the class path runs; the second `premain` is ignored as a duplicate.
  At startup KRL ZomBud lists every agent jar it finds and marks which one is active
  (`io.github.karl3uwu.zombud.AgentConflicts`). Remove the extra hook.
- **`zbNative.dll`** has the same file name and launch option in both projects. Whichever was installed last decides
  which jar is loaded (`KRLZomBud.jar` or `ZombieBuddy.jar`).

## Installer

The installer copies `zbNative.dll` and `KRLZomBud.jar` from the folder it runs from (or its `libs\` folder, or
`%USERPROFILE%\Zomboid\mods\KRLZomBud\libs`). It doesn't use upstream's Workshop items. On install it also:

- deletes upstream's `ZombieBuddy.jar` and `ZombieBuddy.jar.new` from the game folder (shown in the preview first);
- replaces a legacy `-javaagent:ZombieBuddy.jar` hook with `-agentlib:zbNative` in each launch location it patches
  (`ProjectZomboid64.json`, `ProjectZomboid64.bat`, Steam launch options), so the game doesn't start two agents.
  Locations you didn't choose are left alone, except that a JSON-only install still clears ZombieBuddy hooks from the
  Steam launch options, as before.

`-agentlib:zbNative` hooks written by upstream's installer are the same hook and are kept as they are. Uninstall removes
both kinds of hook and both sets of files.
