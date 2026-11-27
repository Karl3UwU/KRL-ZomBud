package me.zed_0xff.zombie_buddy;

import static me.zed_0xff.zombie_buddy.SteamWorkshop.SteamID64;

import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.crypto.signers.Ed25519Signer;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.HexFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The known-authors registry: SteamID64 to display name and Ed25519 ZBS public keys.
 *
 * <p>Read only from the copy of {@code authors.json} bundled in the agent jar ({@value #BUNDLED_RESOURCE}).
 * Nothing is fetched from the network and no cache file is read or written, so startup does not depend on
 * GitHub (upstream downloaded the file from its repository on every start). If the resource is missing or
 * its signature does not verify, the registry is empty. Authors who are not in it are simply unknown, which
 * never makes a mod trusted: a {@code .zbs} signature must still verify against a key from another source.
 *
 * <p>File format: JSON object with {@code updated_at}, {@code authors}, and {@code signature} fields.
 * The signature is Ed25519 over {@code "ZBAuthors:" + sha256hex(file_with_signature_value_cleared)},
 * verified against the hardcoded {@link #AUTHORS_PUBKEY_HEX}.
 */
public final class KnownAuthors {
    /** Classpath location of the bundled registry (copied byte-for-byte from the repo root at build time). */
    public static final String BUNDLED_RESOURCE = "/authors.json";

    // Ed25519 public key for verifying authors.json. Same key as ZBS signing key, baked into
    // the JAR (which is X.509-signed), so it can't be tampered with at runtime.
    static final String AUTHORS_PUBKEY_HEX = "c0fe7daaa1fbd1ee54096d96669a7bbb10cd0a2ad08949d664d4804c50e34db1";

    // Matches the signature field value; groups: (prefix-with-quote) (hex) (closing-quote)
    private static final Pattern SIG_PATTERN =
        Pattern.compile("(\"signature\"\\s*:\\s*\")([0-9a-fA-F]*)(\")");

    private static final Type FILE_DATA_TYPE = new TypeToken<FileData>() {}.getType();

    private static final class FileData {
        @SerializedName("updated_at") String updatedAt;
        @SerializedName("authors")    List<AuthorEntry> authors;
        @SerializedName("signature")  String signature;
    }

    public static final class AuthorEntry {
        @SerializedName("id")
        public SteamID64 id;
        @SerializedName("name")
        public String name;
        @SerializedName("keys")
        public List<String> keys;
    }

    private KnownAuthors() {}

    /**
     * Loads the bundled registry and verifies its signature.
     * Returns an empty map if the resource is missing, unreadable or wrongly signed.
     */
    public static Map<SteamID64, AuthorEntry> loadAuthors() {
        String body = readBundledBody();
        if (body == null) {
            Logger.warn("authors.json: not bundled in the agent jar; known authors list is empty");
            return Collections.emptyMap();
        }
        if (!verifySignature(body)) {
            Logger.warn("authors.json: bundled copy has an invalid signature, ignoring");
            return Collections.emptyMap();
        }
        return parseAuthorsJSON(body);
    }

    public static Map<SteamID64, String> loadSteamIdToDisplayName() {
        Map<SteamID64, String> out = new LinkedHashMap<>();
        for (Map.Entry<SteamID64, AuthorEntry> entry : loadAuthors().entrySet()) {
            AuthorEntry author = entry.getValue();
            if (author != null && !Utils.isBlank(author.name)) {
                out.put(entry.getKey(), author.name);
            }
        }
        return out;
    }

    public static List<AuthorEntry> loadAuthorEntries() {
        return new ArrayList<>(loadAuthors().values());
    }

    /**
     * Verifies the Ed25519 signature embedded in the authors.json body.
     * Canonical form: the raw body bytes with the signature hex value replaced by {@code ""}.
     * Signed payload: {@code "ZBAuthors:" + sha256hex(canonical_bytes)}.
     */
    static boolean verifySignature(String body) {
        Matcher m = SIG_PATTERN.matcher(body);
        if (!m.find()) {
            Logger.warn("authors.json: no 'signature' field found");
            return false;
        }
        String sigHex = m.group(2);
        if (sigHex.length() != 128) {
            Logger.warn("authors.json: signature wrong length (" + sigHex.length() + "), expected 128 hex chars");
            return false;
        }
        // Canonical: same bytes but signature value cleared to ""
        String canonical = m.replaceFirst("$1$3");
        String hashHex = Utils.sha256Hex(canonical.getBytes(StandardCharsets.UTF_8));
        if (hashHex == null) {
            Logger.warn("authors.json: failed to compute sha256 of canonical form");
            return false;
        }
        byte[] msg = ("ZBAuthors:" + hashHex).getBytes(StandardCharsets.UTF_8);
        try {
            HexFormat hex = HexFormat.of();
            byte[] pubRaw = hex.parseHex(AUTHORS_PUBKEY_HEX);
            Ed25519PublicKeyParameters pub = new Ed25519PublicKeyParameters(pubRaw, 0);
            Ed25519Signer signer = new Ed25519Signer();
            signer.init(false, pub);
            signer.update(msg, 0, msg.length);
            boolean ok = signer.verifySignature(hex.parseHex(sigHex));
            if (!ok) Logger.warn("authors.json: signature verification failed");
            return ok;
        } catch (Exception e) {
            Logger.warn("authors.json: signature verification error: " + e.getMessage());
            return false;
        }
    }

    static String readBundledBody() {
        try (InputStream in = KnownAuthors.class.getResourceAsStream(BUNDLED_RESOURCE)) {
            if (in == null) {
                return null;
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            Logger.warn("Could not read bundled authors.json: " + e.getMessage());
            return null;
        }
    }

    static Map<SteamID64, AuthorEntry> parseAuthorsJSON(String body) {
        Map<SteamID64, AuthorEntry> out = new LinkedHashMap<>();
        if (Utils.isBlank(body)) {
            return out;
        }
        try {
            FileData data = ZBGson.PRETTY.fromJson(body, FILE_DATA_TYPE);
            if (data != null && data.authors != null) {
                for (AuthorEntry e : data.authors) {
                    if (e != null && e.id != null) {
                        out.put(e.id, e);
                    }
                }
            }
        } catch (Exception e) {
            Logger.warn("Failed to parse authors.json: " + e.getMessage());
        }
        return out;
    }
}
