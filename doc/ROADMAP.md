# Roadmap: from ZombieBuddy to a Fabric-style modding environment

This fork starts from ZombieBuddy 3.0.0-beta3 (upstream `master`, Nov 2026). The goal is a Java modding
environment for Project Zomboid that modders can rely on the way Minecraft modders rely on Fabric:

- a **small, stable loader** that does one job well,
- a **rich patching API** that survives game updates,
- **build tooling** that catches breakage before players do,
- **open, automated releases** that don't depend on one person or one machine.

## Where we start

ZombieBuddy's core technique is sound: a Java agent (started through the tiny `zbNative.dll` shim) that rewrites
mod jars with ASM, then weaves `@Patch` advice into game classes with ByteBuddy, and exposes Java classes to Lua through
the game's own exposer. It is MIT-licensed, has a real test suite, and already ships a 2.x → 3.x compatibility
transformer.

The problems are structural:

| Problem | Evidence (upstream, Nov 2026) |
|---|---|
| Java mods load late, inside `ZomboidFileSystem.loadMods()` | Most game classes are already loaded, so patches need retransformation, which cannot add fields or methods (upstream #119, #149) |
| One very large class plus global state | `Loader.java` is 1,261 lines, with a 346-line `loadMods()` and about 60 mutable static fields across the code |
| Scope creep in the core jar | About 2,500 lines of approval UI in four toolkits; about 3,300 lines of experimental dev tools |
| Patch targets are unchecked strings | `@Patch(className=..., methodName=...)` breaks without warning on game updates; mods ship one jar per game build |
| No stable public API | The 2.x → 3.x change broke mods; internal and public classes aren't separated |
| Only one person can release | No CI; jar signing uses a key stored on the maintainer's Mac; the 3.0 beta was published and then withdrawn |
| Central trust registry | 68 of the last 113 upstream commits only add entries to `authors.json`; Steam profile lookups hit rate limits (#129, #133) |
| Install friction | Native shim plus three launch paths patched at once (JSON, .bat, Steam options) |

## Target architecture

```
loader-core     agent bootstrap, mod discovery, dependency/ordering, class loading, patch application
api             the public, versioned modder API (annotations, Lua exposure, lifecycle events)
patch-engine    bytecode weaving behind the api (ByteBuddy today; Mixin is a candidate, see decision 3)
compat-zb       runs existing ZombieBuddy 2.x/3.x mods unmodified (extends upstream's ZB2Compat idea)
trust           jar approvals, signature checks, ban checks; one UI frontend
gradle-plugin   game classpath discovery, patch-target validation per game build, run configs, packaging, signing
devtools        today's "experimental" package (Lua console, watches, overlays), as an optional separate mod
installer       one launch method, one install path, clean uninstall
```

Rules for the core: no UI code, no network access on the startup path, no mutable global state outside a
single explicit `LoaderContext`, and everything a mod can touch lives in `api`.

## Decisions to make before the big changes

1. **Name and identity.** *Decided: **KRL ZomBud**.*
   - Mod id, Workshop item and jar: `KRLZomBud` (`KRLZomBud.jar`).
   - Java package root for new code: `io.github.karl3uwu.zombud`.
   - Upstream's `me.zed_0xff.zombie_buddy.*` classes stay for compatibility (decision 2), so existing mods keep
     working, and the loader can still satisfy `require=\ZombieBuddy`.
   - The rename lands with the phase 2 restructure, not as a separate search-and-replace.
2. **Compatibility with existing ZombieBuddy mods.** *Recommended: yes.* Better FPS, Project Viewpoint and others are
   compiled against `me.zed_0xff.zombie_buddy.*`; without a compatibility layer the new environment starts with zero mods.
3. **Patching engine.** Keep ByteBuddy behind a cleaner API, or adopt SpongePowered Mixin (injection at call sites,
   redirects, local-variable changes, added fields and interfaces, large existing documentation). *Recommended: a time-boxed
   Mixin spike before committing.*
4. **Resolve Java mods at launch.** Read the enabled mod list at startup so every patch is applied as its class
   first loads (no retransformation). Changing Java mods then needs a game restart, as in Fabric. *Recommended: yes.*
5. **Trust model.** Replace the hand-maintained author registry with signatures bound to the Workshop uploader through
   Steam's API, plus per-hash approvals. Decide whether unsigned mods stay allowed by default.
6. **CI without the proprietary game jar.** The game jar can't be committed or downloaded in public CI. Options: a
   generated signature-only stub jar, a self-hosted runner with the game installed, or both (stubs for compile and unit
   tests, self-hosted runner for integration tests). Check the legal side of stubs before publishing them.

## Phases

**Phase 0: fork hygiene (branch `chore/fork-cleanup`)**
- Remove upstream-specific files (maintainer artwork sources, funding links, Workshop page texts, macOS-only Rake scripts).
- Make the build work out of the box on Windows: `-PpzDir`/`PZ_DIR`, automatic JDK 25 provisioning, Windows 10 target for the native shim.
- Make the test suite independent of the developer's machine.
- Developer setup guide (`doc/Development.md`) and `tools/decompile-game.ps1` for a local, never-committed game reference.

**Phase 1: reproducible builds and CI**
- GitHub Actions for the native shim, the Go installer and (per decision 6) the Java build and tests.
- Release workflow that builds, tests and publishes artifacts with checksums.

**Phase 2: restructure without behavior changes**
- Split into Gradle subprojects following the target architecture.
- Break up `Loader` into discovery, policy, loading and patching services; move global state into `LoaderContext`.
- Move the experimental package and the extra UI frontends out of the core jar.
- Keep the existing test suites green throughout; add golden tests for the jar rewrite pipeline.

**Phase 3: launch-time loading and the patch engine**
- Resolve Java mods at startup (decision 4).
- Mixin spike (decision 3), then implement the chosen engine behind `api`.
- `compat-zb` passes the existing upstream test suites plus real-world mods (Better FPS, Fast Loading, Tripping Zombies Real Limbs).

**Phase 4: modder tooling**
- Gradle plugin: find the game install, validate patch targets against a chosen game build, run the game with the
  agent attached, package and sign the mod.
- Template mod repository and a short getting-started guide.

**Phase 5: trust, distribution, install**
- New signing design (decision 5), single-method installer, our own Workshop item.

**Phase 6: API 1.0**
- Freeze the public API under semantic versioning with documented deprecation periods.

## Upstream issues to keep in view

Already fixed on upstream master and inherited by this fork: #80 (duplicate agent load), #88 (JVM DLL selection),
#96/#102 (mod class definition order).

Still open upstream and relevant to the redesign: #107 (stale preload registrations), #119 (patches on a second
already-loaded class), #147 (NPE in timed actions with Java mods in multiplayer), #149 (adding fields to classes).
