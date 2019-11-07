package io.netty.handler.ssl;

import javax.net.ssl.SSLEngine;

public class JettyAlpnSslEngine extends JdkSslEngine {
    JettyAlpnSslEngine(SSLEngine engine) {
        super(engine);
    }
}
