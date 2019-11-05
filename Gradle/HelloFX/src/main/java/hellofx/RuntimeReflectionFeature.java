package hellofx;
/*
import akka.actor.ActorSystem;
import akka.actor.DynamicAccess;
import akka.actor.ExtendedActorSystem;
import akka.actor.LightArrayRevolverScheduler;
import akka.actor.LocalActorRefProvider;
import akka.event.DefaultLoggingFilter;
import akka.event.EventStream;
import akka.event.LoggingAdapter;
import akka.event.slf4j.Slf4jLoggingFilter;
import akka.io.TcpExt;
import akka.io.TcpManager;
import akka.routing.ConsistentHashingPool;
import akka.stream.serialization.StreamRefSerializer;
import com.typesafe.config.Config;*/
import com.oracle.svm.core.annotate.AutomaticFeature;
import com.oracle.svm.core.jni.JNIRuntimeAccess;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeReflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

@AutomaticFeature
public class RuntimeReflectionFeature implements Feature {
    @Override
    public void beforeAnalysis(BeforeAnalysisAccess access) {
        try {
            setupClasses();

/*
            RuntimeReflection.register(DefaultLoggingFilter.class);
            RuntimeReflection.register(DefaultLoggingFilter.class.getDeclaredConstructor());
            RuntimeReflection.register(TcpManager.class);
            RuntimeReflection.register(TcpManager.class.getDeclaredConstructor(TcpExt.class));
            RuntimeReflection.register(TcpExt.class);
            RuntimeReflection.register(TcpExt.class.getDeclaredConstructor(ExtendedActorSystem.class));

            RuntimeReflection.register(DefaultLoggingFilter.class.getDeclaredConstructor(ActorSystem.Settings.class, EventStream.class));
            RuntimeReflection.register(Slf4jLoggingFilter.class);
            RuntimeReflection.register(Slf4jLoggingFilter.class.getDeclaredConstructor(ActorSystem.Settings.class, EventStream.class));
            RuntimeReflection.register(LightArrayRevolverScheduler.class);
            RuntimeReflection.register(LightArrayRevolverScheduler.class.getDeclaredConstructor(Config.class, LoggingAdapter.class, ThreadFactory.class));
            RuntimeReflection.register(LocalActorRefProvider.class);
            RuntimeReflection.register(LocalActorRefProvider.class.getDeclaredConstructor(String.class, ActorSystem.Settings.class, EventStream.class, DynamicAccess.class));
            RuntimeReflection.register(ConsistentHashingPool.class);
            RuntimeReflection.register(ConsistentHashingPool.class.getDeclaredConstructor(Config.class));
            RuntimeReflection.register(akka.routing.RoundRobinPool.class);
            RuntimeReflection.register(akka.routing.RoundRobinPool.class.getDeclaredConstructor(Config.class));
            RuntimeReflection.register(StreamRefSerializer.class);*/
        } catch (Exception e) {
            System.err.println("Unable to register logging filter " + e);
        }
    }

    /**
     * All classes defined here will have reflection support
     */
    static Class<?>[] getClasses(){
        return new Class[]{
                jni.HelloJNI.class,
                java.lang.Throwable.class,
                boolean[].class
        };
    }

    static void setupClasses() {
        try {
            System.out.println("> Loading classes for future reflection support");
            for (final Class<?> clazz : getClasses()) {
                process(clazz);
            }
        } catch (Error e){
            if(!e.getMessage().contains("The class ImageSingletons can only be used when building native images")){
                throw e;
            }
        }
    }

    /**
     * Register all constructors and methods on graalvm to reflection support at runtime
     */
    private static void process(Class<?> clazz) {
        try {
            System.out.println("> Declaring class: " + clazz.getCanonicalName());
            RuntimeReflection.register(clazz);
            for (final Method method : clazz.getDeclaredMethods()) {
                System.out.println("\t> method: " + method.getName() + "(" + Arrays.toString(method.getParameterTypes()) + ")");
                JNIRuntimeAccess.register(method);
                RuntimeReflection.register(method);
            }
            for (final Field field : clazz.getDeclaredFields()) {
                System.out.println("\t> field: " + field.getName());
                JNIRuntimeAccess.register(field);
                RuntimeReflection.register(field);
            }
            for (final Constructor<?> constructor : clazz.getDeclaredConstructors()) {
                System.out.println("\t> constructor: " + constructor.getName() + "(" + constructor.getParameterCount() + ")");
                JNIRuntimeAccess.register(constructor);
                RuntimeReflection.register(constructor);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
