import org.apache.commons.lang3.RandomStringUtils;
import static spark.Spark.*;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class Yopass {

    public final static int KEY_LENGTH = 16;
    public final static int PASSWORD_LENGTH = 8;
    public final static int SECRET_MAX_LENGTH = 10000;

    public static int getLifeTime(String lifetime) {
        HashMap<String, Integer> duration = new HashMap<>();
        duration.put("1h", 3600);
        duration.put("1d", 3600 * 12);
        duration.put("1w", 3600 * 12 * 7);
        if(duration.get(lifetime) == null) {
           return 3600;
        }
        return duration.get(lifetime);
    }

    public static void main(String[] args) {
        staticFileLocation("/public");

        post("/v1/secret", (request, response) -> {
            response.type("application/json");

            // Create json objects for request and result
            JSONObject jsonObject = new JSONObject(request.body());
            JSONObject result = new JSONObject();

            // Get secret
            String secret = null;
            try {
                secret = jsonObject.getString("secret");
            } catch(JSONException e) {
                result.put("message", "Invalid request");
                halt(500, result.toString());
            }

            // Validate secret length
            if(secret.length() > SECRET_MAX_LENGTH) {
                result.put("message", "Request is too large");
                halt(400, result.toString());
            }

            // Get lifetime
            int lifetime = 3600;
            try {
                lifetime = getLifeTime(jsonObject.getString("lifetime"));
            } catch(JSONException e) {
                // Default to 3600
            }

            // Generate key and password for accessing message
            String decryptionKey = RandomStringUtils.randomAlphanumeric(PASSWORD_LENGTH);
            String key = RandomStringUtils.randomAlphanumeric(KEY_LENGTH);

            // Connect to memcached
            Memcached memcached = null;
            try {
                memcached = new Memcached();
            } catch (Exception e) {
                result.put("message", "failed to connect to memcached");
                halt(500, result.toString());
            }

            // Encrypt message and store in memcached
            Encryption e = new Encryption(decryptionKey);
            String encryptedMessage = e.encrypt(secret);

            if (!memcached.save(key, lifetime, encryptedMessage)) {
                response.status(500);
                result.put("message", "Failed to store message");
            } else {
                result.put("key", key);
                result.put("decryption_key", decryptionKey);
                result.put("full_url", "/v1/secret/" + key + "/" + decryptionKey);
                result.put("short_url", "/v1/secret/" + key);
                result.put("message", "secret stored");
            }
            return result;
        });

        get("/v1/secret/:key/:decryption_key", (request, response) -> {
            response.type("application/json");

            JSONObject result = new JSONObject();

            Memcached memcached = null;
            try {
                memcached = new Memcached();
            } catch(Exception e) {
                result.put("message", "failed to connect to memcached");
                halt(500, result.toString());
            }
            String key = request.params(":key");
            String message = memcached.get(key);
            if(message == null) {
                result.put("message", "Not found");
                halt(404, result.toString());
            }
            Encryption e = new Encryption(request.params(":decryption_key"));
            String decryptedMessage = e.decrypt(message);
            if(decryptedMessage == null) {
                result.put("message", "Invalid decryption key");

                /* Check for ratelimit key, increment if < 3 otherwise delete message. */
                String tries = memcached.get(key + "_ratelimit");
                if(tries == null) {
                    memcached.increment(key + "_ratelimit");
                    halt(401, result.toString());
                }
                if(Integer.parseInt(tries) > 3) memcached.delete(key);
                else memcached.increment(key + "_ratelimit");

                halt(401, result.toString());
            }

            result.put("message", "OK");
            result.put("secret", decryptedMessage);
            memcached.delete(request.params(":key"));
            return result;
        });
    }
}