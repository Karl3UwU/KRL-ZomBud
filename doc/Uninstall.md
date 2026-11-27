# Uninstalling ZombieBuddy

## Windows

You can uninstall automatically by running `KRLZomBudInstaller.exe` again and choosing **Uninstall KRL ZomBud**. The installer shows a confirmation preview before applying changes.

Manual uninstall:

1. Open Project Zomboid properties in Steam and remove the ZombieBuddy launch option:

   ```
   -agentlib:zbNative ... --
   ```

2. Delete these files from the Project Zomboid game directory:
   - `KRLZomBud.jar`
   - `zbNative.dll`
   - `ZombieBuddy.jar` and `ZombieBuddy.jar.new`, if left over from upstream ZombieBuddy (the installer removes these too)

3. Optional: delete ZombieBuddy's config directory if you also want to remove approval decisions and caches:
   - Default location: `%USERPROFILE%\.zombie_buddy`
   - Usually: `C:\Users\<your username>\.zombie_buddy`

## macOS / Linux

1. Open Project Zomboid properties in Steam and remove the ZombieBuddy launch option:

   ```
   -javaagent:KRLZomBud.jar ... --
   ```

2. Delete these files from the Project Zomboid game directory:
   - `KRLZomBud.jar`
   - `ZombieBuddy.jar` and `ZombieBuddy.jar.new`, if left over from upstream ZombieBuddy

3. Optional: delete ZombieBuddy's config directory if you also want to remove approval decisions and caches:
   - Default location: `~/.zombie_buddy`
