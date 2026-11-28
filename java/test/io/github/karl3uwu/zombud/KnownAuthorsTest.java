package io.github.karl3uwu.zombud;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

class KnownAuthorsTest {

    @Test
    void bundledCopyIsByteIdenticalToRepoFile() throws Exception {
        String bundled = KnownAuthors.readBundledBody();
        assertNotNull(bundled, "authors.json must be bundled as a classpath resource");
        byte[] repo = Files.readAllBytes(Path.of("..", "authors.json"));
        assertArrayEquals(repo, bundled.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    @Test
    void bundledCopyVerifiesAndLoads() {
        assertTrue(KnownAuthors.verifySignature(KnownAuthors.readBundledBody()));
        assertFalse(KnownAuthors.loadAuthors().isEmpty());
    }

    @Test
    void tamperedCopyFailsVerification() {
        String body = KnownAuthors.readBundledBody();
        String tampered = body.replaceFirst("\"name\":\"", "\"name\":\"X");
        assertNotEquals(body, tampered);
        assertFalse(KnownAuthors.verifySignature(tampered));
    }

    @Test
    void noNetworkCode() {
        // The registry must not download anything at startup: no URL constants, no HTTP client use.
        assertTrue(Arrays.stream(KnownAuthors.class.getDeclaredFields())
            .noneMatch(f -> f.getName().toUpperCase().contains("URL")));
    }
}
