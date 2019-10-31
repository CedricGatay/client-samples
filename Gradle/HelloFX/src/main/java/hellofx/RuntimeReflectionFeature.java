package hellofx;

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
import com.oracle.svm.core.annotate.AutomaticFeature;
import com.typesafe.config.Config;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeReflection;

import java.util.concurrent.ThreadFactory;

@AutomaticFeature
public class RuntimeReflectionFeature implements Feature {
    @Override
    public void beforeAnalysis(BeforeAnalysisAccess access) {
        try {


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
            RuntimeReflection.register(StreamRefSerializer.class);
        } catch (Exception e) {
            System.err.println("Unable to register logging filter " + e);
        }
    }
}
