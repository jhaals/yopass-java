import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.MemcachedClientBuilder;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.command.BinaryCommandFactory;
import net.rubyeye.xmemcached.utils.AddrUtil;

import java.io.IOException;

public class Memcached {

    MemcachedClient client;

    Memcached() throws IOException {
        String address = System.getenv("YP_MEMCACHED") != null ? System.getenv("YP_MEMCACHED") : "localhost:11211";
        MemcachedClientBuilder builder = new XMemcachedClientBuilder(AddrUtil.getAddresses(address));
        builder.setFailureMode(false);
        builder.setCommandFactory(new BinaryCommandFactory());

        client = builder.build();
        client.setEnableHealSession(false);
    }

    public Boolean save(String key, int lifetime, String value) {
        try {
            client.add(key, lifetime, value);
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public String get(String key) {
        try {
            return client.get(key);
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public Boolean delete(String key) {
        try {
            return client.delete(key);
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public Boolean increment(String key) {
        try {
            client.incr(key, 43200, 1);
            return true;
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
