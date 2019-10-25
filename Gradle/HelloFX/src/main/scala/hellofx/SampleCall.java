package hellofx;

import org.graalvm.nativeimage.IsolateThread;
import org.graalvm.nativeimage.c.function.CEntryPoint;

public class SampleCall {
    @CEntryPoint static int add(IsolateThread thread, int a, int b) {
        return a + b;
    }

    @CEntryPoint static void start(IsolateThread thread) {
        HelloFX.instance().init();
    }

    @CEntryPoint static void stop(IsolateThread thread) {
        HelloFX.instance().stop();
    }
}
