package model.persistence;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Hashes and verifies short local profile PINs using PBKDF2.
 */
public final class PinHashingService {

  private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
  private static final int ITERATIONS = 65_536;
  private static final int KEY_LENGTH = 256;
  private static final int SALT_BYTES = 16;

  private final SecureRandom secureRandom;

  /**
   * Creates a PIN hashing service backed by a cryptographically secure random source.
   */
  public PinHashingService() {
    this.secureRandom = new SecureRandom();
  }

  /**
   * Generates a new random salt encoded in Base64.
   *
   * @return Base64 salt string
   */
  public String generateSaltBase64() {
    byte[] salt = new byte[SALT_BYTES];
    secureRandom.nextBytes(salt);
    return Base64.getEncoder().encodeToString(salt);
  }

  /**
   * Hashes the supplied PIN with the provided salt.
   *
   * @param pin raw PIN characters
   * @param saltBase64 Base64-encoded salt
   * @return Base64-encoded PBKDF2 hash
   */
  public String hashPin(char[] pin, String saltBase64) {
    byte[] salt = Base64.getDecoder().decode(saltBase64);
    PBEKeySpec keySpec = new PBEKeySpec(pin, salt, ITERATIONS, KEY_LENGTH);
    try {
      SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM);
      byte[] hash = keyFactory.generateSecret(keySpec).getEncoded();
      return Base64.getEncoder().encodeToString(hash);
    } catch (NoSuchAlgorithmException | InvalidKeySpecException exception) {
      throw new IllegalStateException("Could not hash PIN.", exception);
    } finally {
      keySpec.clearPassword();
    }
  }

  /**
   * Verifies whether the provided PIN matches the stored salted hash.
   *
   * @param pin raw PIN characters
   * @param saltBase64 Base64-encoded salt
   * @param expectedHashBase64 stored Base64-encoded hash
   * @return {@code true} when the PIN is correct
   */
  public boolean verifyPin(char[] pin, String saltBase64, String expectedHashBase64) {
    String actualHash = hashPin(pin, saltBase64);
    return actualHash.equals(expectedHashBase64);
  }
}
