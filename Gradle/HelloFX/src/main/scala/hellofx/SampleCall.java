package hellofx;

import com.oracle.svm.core.SubstrateUtil;
import com.oracle.svm.core.c.ProjectHeaderFile;
import com.oracle.svm.core.option.RuntimeOptionParser;
import com.oracle.svm.jni.nativeapi.JNIEnvironment;
import com.oracle.svm.jni.nativeapi.JNIMethodId;
import com.oracle.svm.jni.nativeapi.JNINativeInterface;
import com.oracle.svm.jni.nativeapi.JNIObjectHandle;
import com.oracle.svm.jni.nativeapi.JNIValue;
import org.graalvm.nativeimage.IsolateThread;
import org.graalvm.nativeimage.StackValue;
import org.graalvm.nativeimage.c.CContext;
import org.graalvm.nativeimage.c.function.CEntryPoint;
import org.graalvm.nativeimage.c.struct.CField;
import org.graalvm.nativeimage.c.struct.CStruct;
import org.graalvm.nativeimage.c.type.CCharPointer;
import org.graalvm.nativeimage.c.type.CCharPointerPointer;
import org.graalvm.nativeimage.c.type.CTypeConversion;
import org.graalvm.word.PointerBase;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

@CContext(SampleCall.EclairDirectives.class)
public class SampleCall {

    @CEntryPoint
    static void run_eclair(IsolateThread thread, int paramArgc, CCharPointerPointer paramArgv) {
        String[] args = SubstrateUtil.getArgs(paramArgc, paramArgv);
        args = RuntimeOptionParser.parseAndConsumeAllOptions(args);
        System.out.println("args = " + args);
        try {
            InitEclair.init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @CEntryPoint(name = "send_message")
    static void message(IsolateThread isolateThread, CMessageSendStruct cmessageSends) {
        final String s = CTypeConversion.toJavaString(cmessageSends.getBody());
        System.out.println("s = " + s);
        InitEclair.message(s);
    }

    static class EclairDirectives implements CContext.Directives {

        @Override
        public List<String> getHeaderFiles() {
            /*
             * The header file with the C declarations that are imported. Here we give the
             * name of the header file. SVM searches the header file according to the file
             * name specified here and the relative path specified in H:CLibraryPath in
             * option.
             */
            final String hellofx = ProjectHeaderFile.resolve("hellofx", "eclair.h");
            System.out.println(">>>> hellofx = " + hellofx);
            return Collections.singletonList(hellofx);
        }
    }

    /**
     * This interface gives a Java version description of Message_Send_Struct data
     * structure defined in the C header file.
     * <p>
     * This declaration MUST be enclosed inside the @CContext class.
     *
     * @author cengfeng.lzy
     */
    @CStruct("Message_Send_Struct")
    interface CMessageSendStruct extends PointerBase {
        @CField("producer_name")
        CCharPointer getProducerName();

        @CField("producer_name")
        void setProducerName(CCharPointer value);

        @CField("topic")
        CCharPointer getTopic();

        @CField("topic")
        void setTopic(CCharPointer value);

        @CField("tags")
        CCharPointer getTags();

        @CField("tags")
        void setTags(CCharPointer value);

        @CField("keys")
        CCharPointer getKeys();

        @CField("keys")
        void setKeys(CCharPointer value);

        @CField("body")
        CCharPointer getBody();

        @CField("body")
        void setBody(CCharPointer value);
    }
}
