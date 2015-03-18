import org.jasypt.util.text.BasicTextEncryptor;

public class Encryption {

    BasicTextEncryptor textEncryptor;

    Encryption(String password) {
        textEncryptor = new BasicTextEncryptor();
        textEncryptor.setPassword(password);
    }

    public String encrypt(String message) {
        return textEncryptor.encrypt(message);
    }

    public String decrypt(String message) {
        try {
            return textEncryptor.decrypt(message);

        } catch(Exception e) {
            return null;
        }
    }
}
