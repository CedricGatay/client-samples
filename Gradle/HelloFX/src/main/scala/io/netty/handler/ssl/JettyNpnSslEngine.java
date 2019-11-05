package io.netty.handler.ssl;

import javax.net.ssl.SSLEngine;

public class JettyNpnSslEngine extends io.netty.handler.ssl.JdkSslEngine {
    JettyNpnSslEngine(SSLEngine engine) {
        super(engine);
    }
}
