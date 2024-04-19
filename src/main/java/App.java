import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.arangodb.ArangoDB;
import com.arangodb.Protocol;
import com.arangodb.config.HostDescription;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.ByteArrayInputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.time.LocalDateTime;
import java.util.*;

/*
envvars:
- ADB_LOG_LEVEL: `ALL`|`TRACE`|`DEBUG`|`INFO`|`WARN`|`ERROR`|`OFF`
- ADB_CHECK_INTERVAL_MS: default `1000`
- ADB_USER: default `root`
- ADB_PASSWORD:
- ADB_ENDPOINTS: comma-separated list of `host:port` pairs, e.g. `coordinator1:8529,coordinator2:8529`
- ADB_PROTOCOL: `VST`|`HTTP_JSON`|`HTTP_VPACK`|`HTTP2_JSON`|`HTTP2_VPACK`, default `HTTP2_JSON`
- ADB_USE_SSL: `true`|`false`
- ADB_VERIFY_HOST: `true`|`false`
- ADB_CERT: base64 encoded cert
 */
public class App {
    static String ADB_LOG_LEVEL = System.getenv("ADB_LOG_LEVEL");
    static String ADB_CHECK_INTERVAL_MS = System.getenv("ADB_CHECK_INTERVAL_MS");
    static String ADB_USER = System.getenv("ADB_USER");
    static String ADB_PASSWORD = System.getenv("ADB_PASSWORD");
    static String ADB_ENDPOINTS = System.getenv("ADB_ENDPOINTS");
    static String ADB_PROTOCOL = System.getenv("ADB_PROTOCOL");
    static String ADB_USE_SSL = System.getenv("ADB_USE_SSL");
    static String ADB_VERIFY_HOST = System.getenv("ADB_VERIFY_HOST");
    static String ADB_CERT = System.getenv("ADB_CERT");

    public static void main(String[] args) throws Exception {
        Logger root = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.toLevel(ADB_LOG_LEVEL, Level.INFO));

        ArangoDB.Builder builder = new ArangoDB.Builder();
        if (ADB_USER != null) {
            builder.user(ADB_USER);
        }
        if (ADB_PASSWORD != null) {
            builder.password(ADB_PASSWORD);
        }
        for (HostDescription ep : getEndpoints()) {
            builder.host(ep.getHost(), ep.getPort());
        }
        if (ADB_PROTOCOL != null) {
            builder.protocol(Protocol.valueOf(ADB_PROTOCOL));
        }
        if (Boolean.parseBoolean(ADB_USE_SSL)) {
            builder.useSsl(true);
            if (ADB_VERIFY_HOST != null) {
                builder.verifyHost(Boolean.parseBoolean(ADB_VERIFY_HOST));
            }
            if (ADB_CERT != null) {
                builder.sslContext(createSslContext());
            }
        }

        ArangoDB adb = builder.build();
        int interval = Optional.ofNullable(ADB_CHECK_INTERVAL_MS)
                .map(Integer::parseInt)
                .orElse(1_000);
        try {
            while (true) {
                Thread.sleep(interval);
                adb.getVersion();
                System.out.println(LocalDateTime.now());
            }
        } catch (Exception e) {
            adb.shutdown();
            throw new RuntimeException(e);
        }
    }

    static List<HostDescription> getEndpoints() {
        ArrayList<HostDescription> eps = new ArrayList<>();
        String adbEps = ADB_ENDPOINTS;
        Objects.requireNonNull(adbEps, "ADB_ENDPOINTS");
        for (String e : adbEps.split(",")) {
            eps.add(HostDescription.parse(e));
        }
        return eps;
    }

    static SSLContext createSslContext() throws Exception {
        KeyStore ks = KeyStore.getInstance("jks");
        ByteArrayInputStream is = new ByteArrayInputStream(Base64.getDecoder().decode(ADB_CERT));
        Certificate cert = CertificateFactory.getInstance("X.509").generateCertificate(is);
        ks.load(null);
        ks.setCertificateEntry("adb", cert);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);
        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, tmf.getTrustManagers(), null);
        return sc;
    }
}
