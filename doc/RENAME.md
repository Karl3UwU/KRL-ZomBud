# KRL ZomBud and ZombieBuddy

KRL ZomBud is a fork of [ZombieBuddy](https://github.com/zed-0xff/ZombieBuddy) by Zed (zed-0xff), released under the
same MIT license (see `LICENSE.txt` and `NOTICE`). It is a **standalone** project: it does not need ZombieBuddy
installed, and it does not pretend to be ZombieBuddy. Mods built for ZombieBuddy need a small update to work with it.

## Names

| | Upstream ZombieBuddy | KRL ZomBud |
|---|---|---|
| Display name | ZombieBuddy | KRL ZomBud |
| Mod id (`mod.info`, `mods/<id>`) | `ZombieBuddy` | `KRLZomBud` |
| Agent jar | `ZombieBuddy.jar` | `KRLZomBud.jar` |
| Native shim | `zbNative.dll`, `-agentlib:zbNative` | same file name and launch option |
| Java package for new code | n/a | `io.github.karl3uwu.zombud` |

The names are defined once per language: `ModIdentity` (Java), `MOD_ID` in `java/build.gradle`, `ZB_JAR` in
`c/windows/zbNative.c` and `KRL_MOD_ID` in `installer/installer.go`. Keep them in sync.

Not changed yet (still being decided): the `me.zed_0xff.zombie_buddy` Java package and its annotations, the
2.x→3.x compatibility transformers, the `ZBVersionMin`/`ZBVersionMax` keys, the `ZombieBuddy` Lua global and the
`~/.zombie_buddy` config directory.

## Updating a mod built for ZombieBuddy

What a mod author changes today:

1. **`mod.info`:** replace `require=\ZombieBuddy` with `require=\KRLZomBud`. The game resolves `require=` by mod id,
   and KRL ZomBud does not answer to `ZombieBuddy`. A mod that still requires `ZombieBuddy` shows as missing its
   requirement.
2. **Version range, if you set one:** `ZBVersionMin`/`ZBVersionMax` are compared with KRL ZomBud's version, which for
   now continues ZombieBuddy's numbering (`java/VERSION`, currently `3.0.0-beta3`). Check that your range includes it.
3. **Build (optional):** compile against `KRLZomBud.jar` instead of `ZombieBuddy.jar`
   (`compileOnly files("path/to/KRLZomBud.jar")`).

Nothing else changes for now: `javaJarFile`, `javaPkgName`, the `@Patch`/`@Exposer` annotations and their package, the
Lua API (`ZombieBuddy.*`) and `.zbs` signing all work as before. When these change, this list will say so.

The `require=` line can't name "either one", so a mod can't depend on both projects with a single `mod.info`.

## If upstream ZombieBuddy is installed too

KRL ZomBud doesn't need it, and running both isn't supported. What happens:

- **Upstream mod enabled next to `KRLZomBud`.** Its `mod.info` declares the same `javaPkgName`, so the loader treats
  its jar as itself and never loads it, and logs a warning saying so.
- **Two agent jars in one JVM** (e.g. `-javaagent:ZombieBuddy.jar` next to `-agentlib:zbNative`). Both jars contain
  the same classes, so only the first one on the class path runs. At startup KRL ZomBud lists every agent jar it finds
  and marks which one is active (`io.github.karl3uwu.zombud.AgentConflicts`). Remove the extra hook.
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
