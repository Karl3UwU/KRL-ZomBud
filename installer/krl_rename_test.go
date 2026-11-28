package main

import (
	"os"
	"path/filepath"
	"reflect"
	"strings"
	"testing"
)

func writeTestFile(t *testing.T, path string, content string) {
	t.Helper()
	if err := os.MkdirAll(filepath.Dir(path), 0755); err != nil {
		t.Fatal(err)
	}
	if err := os.WriteFile(path, []byte(content), 0644); err != nil {
		t.Fatal(err)
	}
}

func TestFindKRLSource(t *testing.T) {
	tests := []struct {
		name   string
		layout string // where both files go, relative to the base dir; "" = base itself, "-" = nowhere
		want   string
	}{
		{"next to the installer", "", ""},
		{"mod root libs folder", "libs", "libs"},
		{"workshop item layout", filepath.Join("mods", KRL_MOD_ID, "libs"), filepath.Join("mods", KRL_MOD_ID, "libs")},
		{"missing", "-", ""},
	}
	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			base := t.TempDir()
			if tt.layout != "-" {
				writeTestFile(t, filepath.Join(base, tt.layout, "zbNative.dll"), "dll")
				writeTestFile(t, filepath.Join(base, tt.layout, KRL_JAR_NAME), "jar")
			}
			got, err := findKRLSource([]string{"", base})
			if tt.layout == "-" {
				if err == nil {
					t.Fatalf("expected an error, got %q", got)
				}
				return
			}
			if err != nil {
				t.Fatal(err)
			}
			if want := filepath.Join(base, tt.want); got != want {
				t.Fatalf("got %q, want %q", got, want)
			}
		})
	}
}

func TestFindKRLSourceIgnoresUpstreamJar(t *testing.T) {
	// An upstream mod folder (ZombieBuddy.jar) must never be picked as the install source.
	base := t.TempDir()
	writeTestFile(t, filepath.Join(base, "zbNative.dll"), "dll")
	writeTestFile(t, filepath.Join(base, LEGACY_JAR_NAME), "upstream jar")
	writeTestFile(t, filepath.Join(base, "mods", "ZombieBuddy", "libs", "zbNative.dll"), "dll")
	writeTestFile(t, filepath.Join(base, "mods", "ZombieBuddy", "libs", LEGACY_JAR_NAME), "upstream jar")
	if got, err := findKRLSource([]string{base}); err == nil {
		t.Fatalf("expected an error, got %q", got)
	}
}

func TestCopyCoreFilesFromSourceDir(t *testing.T) {
	src := t.TempDir()
	pz := t.TempDir()
	writeTestFile(t, filepath.Join(src, "zbNative.dll"), "dll")
	writeTestFile(t, filepath.Join(src, KRL_JAR_NAME), "krl jar")
	if err := copyCoreFiles(pz, src); err != nil {
		t.Fatal(err)
	}
	got, err := os.ReadFile(filepath.Join(pz, KRL_JAR_NAME))
	if err != nil || string(got) != "krl jar" {
		t.Fatalf("copied jar = %q, %v", got, err)
	}
	if _, err := os.Stat(filepath.Join(pz, LEGACY_JAR_NAME)); !os.IsNotExist(err) {
		t.Fatalf("%s must not be created, stat err = %v", LEGACY_JAR_NAME, err)
	}
}

func TestLegacyCoreFilesAreCleanedUp(t *testing.T) {
	pz := t.TempDir()
	for _, name := range []string{"zbNative.dll", KRL_JAR_NAME, LEGACY_JAR_NAME, LEGACY_JAR_NAME + ".new"} {
		writeTestFile(t, filepath.Join(pz, name), name)
	}
	wantLegacy := []string{filepath.Join(pz, LEGACY_JAR_NAME), filepath.Join(pz, LEGACY_JAR_NAME+".new")}
	if got := legacyCoreFilePaths(pz); !reflect.DeepEqual(got, wantLegacy) {
		t.Fatalf("legacyCoreFilePaths = %v, want %v", got, wantLegacy)
	}
	if got := coreFileDeletionPlan(pz); len(got) != 4 {
		t.Fatalf("uninstall must remove our files and the upstream leftovers, got %v", got)
	}
}

func TestJSONVMArgsReplaceLegacyJavaAgent(t *testing.T) {
	args, changed := addZombieBuddyVMArg([]string{"-javaagent:ZombieBuddy.jar", "-Xmx3072m"})
	if !changed || !reflect.DeepEqual(args, []string{ZB_LAUNCH_ARG, "-Xmx3072m"}) {
		t.Fatalf("add = %v, %v", args, changed)
	}

	args, changed = addZombieBuddyVMArg([]string{ZB_LAUNCH_ARG, "-javaagent:ZombieBuddy.jar=verbosity=1", "-Xmx3072m"})
	if !changed || !reflect.DeepEqual(args, []string{ZB_LAUNCH_ARG, "-Xmx3072m"}) {
		t.Fatalf("add with both hooks = %v, %v", args, changed)
	}

	args, changed = addZombieBuddyVMArg([]string{ZB_LAUNCH_ARG, "-Xmx3072m"})
	if changed || !reflect.DeepEqual(args, []string{ZB_LAUNCH_ARG, "-Xmx3072m"}) {
		t.Fatalf("already installed = %v, %v", args, changed)
	}

	if !hasZombieBuddyVMArg([]string{"-javaagent:ZombieBuddy.jar"}) {
		t.Fatal("uninstall must notice a legacy -javaagent hook")
	}
	args, changed = removeZombieBuddyVMArg([]string{"-javaagent:ZombieBuddy.jar", ZB_LAUNCH_ARG, "-Xmx3072m"})
	if !changed || !reflect.DeepEqual(args, []string{"-Xmx3072m"}) {
		t.Fatalf("remove = %v, %v", args, changed)
	}

	// Other agents are not ours to touch.
	args, changed = removeZombieBuddyVMArg([]string{"-javaagent:Other.jar"})
	if changed || !reflect.DeepEqual(args, []string{"-javaagent:Other.jar"}) {
		t.Fatalf("unrelated agent = %v, %v", args, changed)
	}
}

func TestBatchJavaOptionsReplaceLegacyJavaAgent(t *testing.T) {
	got, changed := addZombieBuddyJavaOption("-javaagent:ZombieBuddy.jar -Xmx3072m")
	if !changed || got != ZB_LAUNCH_ARG+" -Xmx3072m" {
		t.Fatalf("add = %q, %v", got, changed)
	}
	got, changed = removeZombieBuddyJavaOption("-javaagent:ZombieBuddy.jar " + ZB_LAUNCH_ARG + " -Xmx3072m")
	if !changed || got != "-Xmx3072m" {
		t.Fatalf("remove = %q, %v", got, changed)
	}
	if !containsZombieBuddyJavaOption("-javaagent:ZombieBuddy.jar") {
		t.Fatal("uninstall must notice a legacy -javaagent hook")
	}
}

func TestSteamLaunchOptionsReplaceLegacyJavaAgent(t *testing.T) {
	tests := []struct {
		input   string
		want    string
		changed bool
	}{
		{"-javaagent:ZombieBuddy.jar --", ZB_LAUNCH_OPTIONS, true},
		{"-javaagent:ZombieBuddy.jar -- -debug", ZB_LAUNCH_OPTIONS + " -debug", true},
		{ZB_LAUNCH_OPTIONS + " -javaagent:ZombieBuddy.jar -debug", ZB_LAUNCH_OPTIONS + " -debug", true},
		{ZB_LAUNCH_OPTIONS + " -debug", ZB_LAUNCH_OPTIONS + " -debug", false},
		{"-debug", ZB_LAUNCH_OPTIONS + " -debug", true},
	}
	for _, tt := range tests {
		got, changed := installZombieBuddyLaunchOptions(tt.input)
		if got != tt.want || changed != tt.changed {
			t.Fatalf("install(%q) = %q, %v; want %q, %v", tt.input, got, changed, tt.want, tt.changed)
		}
	}

	stripped, changed := stripZombieBuddyLaunchOptions("-javaagent:ZombieBuddy.jar -- -debug")
	if !changed || stripped != "-debug" {
		t.Fatalf("strip legacy = %q, %v", stripped, changed)
	}
	stripped, changed = stripZombieBuddyLaunchOptions("-javaagent:Other.jar -debug")
	if changed || !strings.Contains(stripped, "-javaagent:Other.jar") {
		t.Fatalf("unrelated agent must stay, got %q, %v", stripped, changed)
	}
}
