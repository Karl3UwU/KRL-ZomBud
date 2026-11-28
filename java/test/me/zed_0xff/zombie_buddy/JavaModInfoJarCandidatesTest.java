package me.zed_0xff.zombie_buddy;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Covers the numbered javaJarFileN / KRLVersionMinN / KRLVersionMaxN mod.info keys that let a single
 * mod.info offer several JARs built against different KRL ZomBud API versions.
 */
class JavaModInfoJarCandidatesTest {

    @TempDir
    Path tempDir;

    private MockedStatic<Utils> utilsMock;
    private MockedStatic<ZombieBuddy> zbMock;

    @BeforeEach
    void setUp() {
        utilsMock = mockStatic(Utils.class, CALLS_REAL_METHODS);
        utilsMock.when(Utils::isServer).thenReturn(false);

        zbMock = mockStatic(ZombieBuddy.class, CALLS_REAL_METHODS);
    }

    @AfterEach
    void tearDown() {
        utilsMock.close();
        zbMock.close();
    }

    private void setKrlVersion(String version) {
        zbMock.when(ZombieBuddy::getVersion).thenReturn(version);
    }

    private void writeModInfo(String content) throws Exception {
        Files.writeString(tempDir.resolve("mod.info"), content);
    }

    private void touchJar(String name) throws Exception {
        Files.createFile(tempDir.resolve(name));
    }

    @Test
    void bareKeysOnly_stillWorkLikeBefore() throws Exception {
        touchJar("foo.jar");
        writeModInfo("""
            javaJarFile=foo.jar
            javaPkgName=some.pkg
            KRLVersionMin=1.0.0
            """);
        setKrlVersion("2.0.0");

        JavaModInfo info = JavaModInfo.parse(tempDir);
        assertNotNull(info);
        assertEquals(tempDir.resolve("foo.jar"), info.jarPath());
    }

    @Test
    void numberedCandidate_selectedWhenItsRangeMatches() throws Exception {
        touchJar("old.jar");
        touchJar("new.jar");
        writeModInfo("""
            javaJarFile=old.jar
            javaPkgName=some.pkg
            KRLVersionMin=1.0.0
            KRLVersionMax=1.9.9

            javaJarFile2=new.jar
            KRLVersionMin2=2.0.0
            """);
        setKrlVersion("3.0.0");

        JavaModInfo info = JavaModInfo.parse(tempDir);
        assertNotNull(info);
        assertEquals(tempDir.resolve("new.jar"), info.jarPath());
    }

    @Test
    void numberedCandidate_lowestIndexWinsWhenMultipleMatch() throws Exception {
        touchJar("a.jar");
        touchJar("b.jar");
        writeModInfo("""
            javaPkgName=some.pkg
            javaJarFile1=a.jar
            KRLVersionMin1=1.0.0

            javaJarFile2=b.jar
            KRLVersionMin2=1.0.0
            """);
        setKrlVersion("5.0.0");

        JavaModInfo info = JavaModInfo.parse(tempDir);
        assertNotNull(info);
        assertEquals(tempDir.resolve("a.jar"), info.jarPath());
    }

    @Test
    void orphanVersionKeyWithoutMatchingJar_isIgnoredNotFatal() throws Exception {
        touchJar("foo.jar");
        writeModInfo("""
            KRLVersionMin5=1.0.0
            javaJarFile=foo.jar
            javaPkgName=some.pkg
            """);
        setKrlVersion("2.0.0");

        JavaModInfo info = JavaModInfo.parse(tempDir);
        assertNotNull(info);
        assertEquals(tempDir.resolve("foo.jar"), info.jarPath());
    }

    @Test
    void noCandidateInRange_parseReturnsNull() throws Exception {
        touchJar("foo.jar");
        writeModInfo("""
            javaJarFile=foo.jar
            javaPkgName=some.pkg
            KRLVersionMax=1.0.0
            """);
        setKrlVersion("9.9.9");

        assertNull(JavaModInfo.parse(tempDir));
    }

    @Test
    void zombieBuddyVersionKeys_areNotRead() throws Exception {
        touchJar("foo.jar");
        writeModInfo("""
            javaJarFile=foo.jar
            javaPkgName=some.pkg
            ZBVersionMin=9.0.0
            ZBVersionMax=9.9.9
            """);
        setKrlVersion("1.0.0");

        // the old keys would reject 1.0.0; ignored, the jar has no range and loads
        JavaModInfo info = JavaModInfo.parse(tempDir);
        assertNotNull(info);
        assertNull(info.krlVersionMin());
        assertNull(info.krlVersionMax());
    }

    @Test
    void keysAreCaseInsensitive() throws Exception {
        touchJar("foo.jar");
        writeModInfo("""
            javaJarFile=foo.jar
            javaPkgName=some.pkg
            krlversionmin=1.0.0
            KRLVERSIONMAX=2.0.0
            """);
        setKrlVersion("1.5.0");

        JavaModInfo info = JavaModInfo.parse(tempDir);
        assertNotNull(info);
        assertEquals("1.0.0", info.krlVersionMin());
        assertEquals("2.0.0", info.krlVersionMax());
    }

    @Test
    void prereleaseIsBelowItsRelease_whenPickingCandidate() throws Exception {
        touchJar("beta.jar");
        touchJar("release.jar");
        writeModInfo("""
            javaPkgName=some.pkg
            javaJarFile1=release.jar
            KRLVersionMin1=3.0.0

            javaJarFile2=beta.jar
            KRLVersionMin2=3.0.0-beta1
            KRLVersionMax2=3.0.0-beta9
            """);

        setKrlVersion("3.0.0-beta3");
        assertEquals(tempDir.resolve("beta.jar"), JavaModInfo.parse(tempDir).jarPath());

        setKrlVersion("3.0.0");
        assertEquals(tempDir.resolve("release.jar"), JavaModInfo.parse(tempDir).jarPath());
    }
}
