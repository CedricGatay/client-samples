import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.RecomputeFieldValue;
import com.oracle.svm.core.annotate.TargetClass;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

// see https://medium.com/graalvm/instant-netty-startup-using-graalvm-native-image-generation-ed6f14ff7692

@TargetClass(className = "akka.actor.LightArrayRevolverScheduler$")
final class Target_akka_actor_LightArrayRevolverScheduler$ {
    @Alias
    @RecomputeFieldValue(kind = RecomputeFieldValue.Kind.FieldOffset, declClassName = "akka.actor.LightArrayRevolverScheduler$TaskHolder", name = "task")
    public long akka$actor$LightArrayRevolverScheduler$$taskOffset;
}

public class AkkaSubstitutions {
}

class ScalaSubstitutions {

    static final Unsafe UNSAFE;

    static {
        try {
            sun.misc.Unsafe found = null;
            for (Field field : sun.misc.Unsafe.class.getDeclaredFields()) {
                if (field.getType() == sun.misc.Unsafe.class) {
                    field.setAccessible(true);
                    found = (sun.misc.Unsafe) field.get(null);
                    break;
                }
            }
            if (found == null)
                throw new IllegalStateException("CG| Can't find instance of sun.misc.Unsafe");
            else UNSAFE = found;
        } catch (Throwable t) {
            throw new ExceptionInInitializerError(t);
        }
    }
}
