package hellofx;

import com.oracle.svm.core.SubstrateUtil;
import com.oracle.svm.core.option.RuntimeOptionParser;
import com.oracle.svm.jni.nativeapi.JNIEnvironment;
import com.oracle.svm.jni.nativeapi.JNIMethodId;
import com.oracle.svm.jni.nativeapi.JNINativeInterface;
import com.oracle.svm.jni.nativeapi.JNIObjectHandle;
import com.oracle.svm.jni.nativeapi.JNIValue;
import org.graalvm.nativeimage.IsolateThread;
import org.graalvm.nativeimage.StackValue;
import org.graalvm.nativeimage.c.function.CEntryPoint;
import org.graalvm.nativeimage.c.type.CCharPointerPointer;
import org.graalvm.nativeimage.c.type.CTypeConversion;

import java.util.Arrays;
import java.util.Properties;

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


    @CEntryPoint static void run_eclair(IsolateThread thread, int paramArgc, CCharPointerPointer paramArgv) {
        String[] args = SubstrateUtil.getArgs(paramArgc, paramArgv);
        args = RuntimeOptionParser.parseAndConsumeAllOptions(args);
        System.out.println("args = " + args);
        InitEclair.init();
    }



    @CEntryPoint(name = "Java_org_pkg_apinative_Native_add")
    static int add(JNIEnvironment env, JNIObjectHandle clazz, @CEntryPoint.IsolateThreadContext long isolateThreadId, int a, int b) {
        JNINativeInterface fn = env.getFunctions();

        System.out.println("Calling add");
        try (
                CTypeConversion.CCharPointerHolder name = CTypeConversion.toCString("hello");
                CTypeConversion.CCharPointerHolder sig = CTypeConversion.toCString("()V");
        ) {
            System.out.println("try to call hello");
            JNIMethodId helloId = fn.getGetStaticMethodID().invoke(env, clazz, name.get(), sig.get());
        }

        return a + b;
    }
}
