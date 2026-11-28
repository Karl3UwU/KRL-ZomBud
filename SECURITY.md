# Security policy

## Reporting a vulnerability

Please report security problems privately through GitHub's private vulnerability reporting:

**https://github.com/Karl3UwU/KRL-ZomBud/security/advisories/new**

Don't open a public issue, pull request or discussion for a vulnerability. Include what you found, how to reproduce it,
which version or commit you tested and what an attacker could do with it. You'll get a reply in the advisory thread.

This repository is a fork. Problems that also affect upstream ZombieBuddy should still be reported here first for KRL
ZomBud; please report them to upstream separately through its own channels.

## Supported versions

KRL ZomBud is pre-release. Only the latest commit on `master` is supported; there are no maintained release branches
yet, and fixes are not backported.

| Version | Supported |
|---|---|
| latest `master` | yes |
| anything older, including upstream ZombieBuddy releases | no |

## Scope

In scope:

- **The loader**: the Java agent (`KRLZomBud.jar`) and its handling of mod jars: discovery, the approval policy, `.zbs`
  signature checks, the bundled known-authors list, jar rewriting and patch application.
- **The installer** (`installer/`): what it copies into the game folder and how it edits `ProjectZomboid64.json`,
  `ProjectZomboid64.bat` and Steam's `localconfig.vdf`.
- **The native shim** (`c/windows/zbNative.dll`): DLL loading and the runtime-library guard.

Out of scope:

- Third-party Java mods themselves. Running a Java mod runs its code with the game's full permissions; that is the
  purpose of the loader. A way to load or run a mod *without* the approval or signature checks that should apply is in
  scope.
- Project Zomboid, Steam and the bundled Java runtime.
- The experimental developer tools (`patches/experimental`, enabled only with the `experimental` agent option), unless
  they are reachable without that option.
