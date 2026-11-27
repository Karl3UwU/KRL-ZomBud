<#
.SYNOPSIS
    Decompiles Project Zomboid's own classes into a local, read-only reference source tree.

.DESCRIPTION
    Patches target game methods by name, so you need to read the game's code to write them.
    This script decompiles the game-owned packages (zombie.*, se.krka.* = Kahlua Lua VM, fmod.*,
    tis.*, astar.*, N3D.*) from projectzomboid.jar with Vineflower. Bundled third-party libraries are skipped.

    The output is The Indie Stone's code: it is written OUTSIDE this repository by default and
    must never be committed or redistributed.

    One folder per game version makes it easy to diff builds when a game update breaks a patch.

.PARAMETER GameDir
    Project Zomboid install folder (on macOS, the folder that contains "Project Zomboid.app").
    Defaults to $env:PZ_DIR, then the standard Steam location for your OS.
    Runs on Windows PowerShell 5.1 and on PowerShell 7 (pwsh) for Linux/macOS.

.PARAMETER OutRoot
    Parent folder for the reference trees. Defaults to <repo>\..\pz-src.

.PARAMETER Version
    Folder name for this build. Defaults to the version in ~\Zomboid\version.txt (the last version launched).

.EXAMPLE
    .\tools\decompile-game.ps1 -GameDir "C:\Program Files (x86)\Steam\steamapps\common\ProjectZomboid"
#>
param(
    [string]$GameDir = $env:PZ_DIR,
    [string]$OutRoot = (Join-Path (Split-Path $PSScriptRoot -Parent) "..\pz-src"),
    [string]$Version,
    [string]$VineflowerVersion = "1.12.0",
    [switch]$Force
)

$ErrorActionPreference = "Stop"
$OutRoot = [System.IO.Path]::GetFullPath($OutRoot)

# Windows PowerShell 5.1 has no $IsWindows; it only runs on Windows.
$onWindows = ($PSVersionTable.PSEdition -eq "Desktop") -or $IsWindows
if (-not $GameDir) {
    $GameDir = if ($onWindows) { "C:\Program Files (x86)\Steam\steamapps\common\ProjectZomboid" }
               elseif ($IsMacOS) { Join-Path $HOME "Library/Application Support/Steam/steamapps/common/ProjectZomboid" }
               else { Join-Path $HOME ".steam/steam/steamapps/common/ProjectZomboid" }
}

# Same lookup as java/build.gradle: install folder (Windows/Linux) or app bundle (macOS).
$gameJar = @(
    "projectzomboid.jar",
    "projectzomboid/projectzomboid.jar",
    "Project Zomboid.app/Contents/Java/projectzomboid.jar",
    "Contents/Java/projectzomboid.jar"
) | ForEach-Object { Join-Path $GameDir $_ } | Where-Object { Test-Path $_ } | Select-Object -First 1
if (-not $gameJar) {
    throw "projectzomboid.jar not found under '$GameDir'. Pass -GameDir or set PZ_DIR."
}

if (-not $Version) {
    $versionFile = Join-Path $HOME "Zomboid/version.txt"
    if (-not (Test-Path $versionFile)) {
        throw "Cannot detect the game version ($versionFile is missing). Pass -Version, e.g. -Version 42.22.2"
    }
    $Version = ((Get-Content $versionFile -TotalCount 1) -split '\s+')[0]
    Write-Host "Detected game version $Version from $versionFile (the last version launched)."
}

# --- Java 25: prefer the JDK that Gradle provisioned for this project, then JAVA_HOME, then PATH.
$javaExe = if ($onWindows) { "java.exe" } else { "java" }
$java = Get-ChildItem (Join-Path $HOME ".gradle/jdks") -Directory -Filter "*-25-*" -ErrorAction SilentlyContinue |
    ForEach-Object { @((Join-Path $_.FullName "bin/$javaExe"), (Join-Path $_.FullName "Contents/Home/bin/$javaExe")) } |
    Where-Object { Test-Path $_ } | Select-Object -First 1
if (-not $java -and $env:JAVA_HOME) { $java = Join-Path $env:JAVA_HOME "bin/$javaExe" }
if (-not $java) { $java = "java" }
# `java -version` writes to stderr; Windows PowerShell 5.1 turns native stderr into terminating errors under "Stop".
$prevEap = $ErrorActionPreference
$ErrorActionPreference = "Continue"
try { $javaVersion = & $java -version 2>&1 | Select-Object -First 1 | ForEach-Object { "$_" } }
finally { $ErrorActionPreference = $prevEap }
if ($javaVersion -notmatch '"(25|2[6-9]|[3-9]\d)[."]') {
    throw "Need Java 25+ to read the game's class files, found: $javaVersion. Run a Gradle build once to provision JDK 25."
}

# --- Vineflower: cached next to the output, verified against Maven Central's SHA-1.
$toolsDir = Join-Path $OutRoot ".tools"
New-Item -ItemType Directory -Force $toolsDir | Out-Null
$vfJar = Join-Path $toolsDir "vineflower-$VineflowerVersion.jar"
if (-not (Test-Path $vfJar)) {
    $base = "https://repo1.maven.org/maven2/org/vineflower/vineflower/$VineflowerVersion/vineflower-$VineflowerVersion.jar"
    Write-Host "Downloading Vineflower $VineflowerVersion from Maven Central..."
    # Windows PowerShell 5.1 may default to TLS versions Maven Central no longer accepts.
    [Net.ServicePointManager]::SecurityProtocol = [Net.ServicePointManager]::SecurityProtocol -bor [Net.SecurityProtocolType]::Tls12
    $tmp = "$vfJar.download"
    Invoke-WebRequest -Uri $base -OutFile $tmp -UseBasicParsing
    $expected = (Invoke-WebRequest -Uri "$base.sha1" -UseBasicParsing).Content.Trim().Substring(0, 40)
    $actual = (Get-FileHash $tmp -Algorithm SHA1).Hash.ToLower()
    if ($actual -ne $expected.ToLower()) {
        Remove-Item $tmp
        throw "Vineflower checksum mismatch (expected $expected, got $actual)."
    }
    Move-Item $tmp $vfJar
}

# --- Decompile.
$outDir = Join-Path $OutRoot $Version
$srcDir = Join-Path $outDir "src"
if ((Test-Path $srcDir) -and -not $Force) {
    throw "$srcDir already exists. Use -Force to regenerate it."
}
if (Test-Path $srcDir) { Remove-Item -Recurse -Force $srcDir }
New-Item -ItemType Directory -Force $outDir | Out-Null

# Copy only the game-owned classes into a temporary jar. (Vineflower's --included-classes filter
# still lets most bundled libraries through.) The full game jar is passed as a library so types resolve.
$gamePrefixes = @("zombie/", "se/krka/", "fmod/", "tis/", "astar/", "N3D/")
$filteredJar = Join-Path $outDir ".game-classes.jar"
Add-Type -AssemblyName System.IO.Compression, System.IO.Compression.FileSystem
if (Test-Path $filteredJar) { Remove-Item $filteredJar }
$in = [System.IO.Compression.ZipFile]::OpenRead($gameJar)
$out = [System.IO.Compression.ZipFile]::Open($filteredJar, [System.IO.Compression.ZipArchiveMode]::Create)
try {
    $classCount = 0
    foreach ($entry in $in.Entries) {
        if (-not $entry.FullName.EndsWith(".class")) { continue }
        if (-not ($gamePrefixes | Where-Object { $entry.FullName.StartsWith($_) })) { continue }
        $target = $out.CreateEntry($entry.FullName)
        $src = $entry.Open(); $dst = $target.Open()
        try { $src.CopyTo($dst) } finally { $src.Dispose(); $dst.Dispose() }
        $classCount++
    }
} finally { $in.Dispose(); $out.Dispose() }

Write-Host "Decompiling $classCount game classes ($($gamePrefixes -join ', ')) into $srcDir (takes a few minutes)..."
$log = Join-Path $outDir "decompile.log"
# Start-Process keeps Vineflower's stderr out of PowerShell's error stream (see the java -version note above).
$vfArgs = "-Xmx6g -jar `"$vfJar`" --folder --skip-extra-files=1 --silent=1 `"-e=$gameJar`" `"$filteredJar`" `"$srcDir`""
$proc = Start-Process -FilePath $java -ArgumentList $vfArgs -NoNewWindow -Wait -PassThru `
    -RedirectStandardOutput $log -RedirectStandardError "$log.stderr"
$vfExit = $proc.ExitCode
Get-Content "$log.stderr" | Add-Content $log
Remove-Item "$log.stderr", $filteredJar
if ($vfExit -ne 0) { throw "Vineflower failed (exit $vfExit). See $log" }

$gameJarHash = (Get-FileHash $gameJar -Algorithm SHA256).Hash
@"
Decompiled reference source for Project Zomboid $Version.

DO NOT COMMIT OR REDISTRIBUTE. This is The Indie Stone's proprietary code, decompiled for local reference only.

Source jar : $gameJar
Jar SHA-256: $gameJarHash
Decompiler : Vineflower $VineflowerVersion
Packages   : $($gamePrefixes -join ', ') (se/krka = Kahlua Lua VM)
Classes    : $classCount
Generated  : $(Get-Date -Format "yyyy-MM-dd HH:mm")
"@ | Set-Content (Join-Path $outDir "SOURCE.txt")

$count = (Get-ChildItem $srcDir -Recurse -Filter *.java).Count
Write-Host "Done: $count Java files in $srcDir"
