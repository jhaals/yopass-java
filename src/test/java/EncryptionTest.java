import junit.framework.TestCase;

public class EncryptionTest extends TestCase {

    public void testEncrypt() throws Exception {
        Encryption encryption = new Encryption("randompassword");
        String encryptedText = encryption.encrypt("message");
        assertEquals(encryptedText.length(), 24);
    }

    public void testDecrypt() throws Exception {
        Encryption encryption = new Encryption("randompassword");
        String decryptedText = encryption.decrypt("ohAbLRoJ1Pp+z4HV7M8YBQ==");
        assertEquals(decryptedText, "message");
    }

    public void testInvalidDecrypt() throws Exception {
        Encryption encryption = new Encryption("invalidpassword");
        String decryptedText = encryption.decrypt("ohAbLRoJ1Pp+z4HV7M8YBQ==");
        assertEquals(decryptedText, null);
    }
}