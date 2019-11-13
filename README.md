# Eclair to iOS Graal Sample


## Repository forked from Hello Gluon Samples

NOTE: Only HelloFX is kept and adapted to make things "work"


## Purpose

This repository is a base for building out Eclair SDK for usage on apple mobile devices. 
It leverage the work done by Gluon team to allow using GraalVM to target iOS platforms.

## Steps

### Prerequisites

The work on this repository relies on forked version of `com.gluonhq.client-gradle-plugin` ([here](https://github.com/CedricGatay/client-gradle-plugin)) to be able to set the following parameters :
 
 * `graalPath` points to a local GraalVM version
 * `buildStaticLib` to build the result to a static library (`.a` file)
 * `nativeBuildOptions` to be able to pass specific parameters to `native-image` command.

It also relies on a forked version of `substrate` (GluonHQ's wrapper to `native-image`, [here](https://github.com/CedricGatay/substrate/tree/experiment/akka_scala_eclair)) implementing the flags described in the previous part and stubbing methods missing from libraries : `JVM_fillInStackTrace` and `Java_jdk_net_MacOSXSocketOptions_*`.

GraalVM version should be a Java 11 compatible one, tested with unreleased version 19.3.0 at the time of this writing (http://download2.gluonhq.com/substrate/graalvm/graalvm-unknown-java11-19.3.0-dev-gvm-3-darwin-amd64.zip).

We need to use llvm 8.0 (https://releases.llvm.org/8.0.0/clang+llvm-8.0.0-x86_64-apple-darwin.tar.xz)

This is documented in issue https://github.com/gluonhq/substrate/issues/47 of original `substrate`'s repository.

If you're using `sdkman` you can source the file `env.cgatay` that will try to setup things the way it should (adapt paths relative to your machine, the same goes for the gradle build file where `graalPath` is pointing to a local version)


### How to build

iOS sample project is available [here](https://github.com/CedricGatay/graal-test-ios-static-lib).

```sh
source env.cgatay
./gradlew clean nB
```

This will take a while (~ 8min), even on a powerful machine, then copy static lib and headers to iOS project directory

`cp build/client/arm64-ios/gvm/shared/HelloFX.a $IOSPROJECT/hellofx.main.a`
`cp build/client/arm64-ios/gvm/shared/hellofx.main.h $IOSPROJECT/hellofx.main.h`

Add the following flags the the linker (they are already set for the sample project):  `-lpthread -lz -ldl -lc++` and disable _dead code stripping_ as it make the linker segfaults.

Then run iOS project, it should link properly against a physical device (targetting a simulator is broken for the time being).

This means you need to have a valid developer account that can sign apps for your device.


## Side notes

### Build native app

Instead of building a static lib it is possible to build an iOS app. However this app won't have any GUI if you're not setting the JavaFX part back on track.

You can still use it to test things and check the console output. To do so, the steps are the following: 

 * remove the `buildStaticLib` flag from build file
 * `./gradlew clean nB`
 * open XCode > Device and Simulators, select target device and drag and drop `build/client/arm64-ios/HelloFX.app` to the list of app, this will install the app on your device.
 * You can launch the app from the _springboard_ but it won't show the logs, the easiest way of doing it is using `instruments`. From your terminal, launch the app using `instruments` : `instruments -w $DEVICEID -t "/Applications/Xcode.app/Contents/Applications/Instruments.app/Contents/Resources/templates/Allocations.tracetemplate" hellofx.Main` (where `$DEVICEID` can be found using `instruments -s devices`)
 * open the `.trace` with instruments and check the console output.

### JNI library loading

Eclair relies on two libraries using JNI : `libsecp256k1` `libsqlitejdbc`. 
They are using a trick in their loading to allow bundling the dynamic library in the jar file and loading it without setting the `java.library.path`.
Unfortunately, this does not work as expected on mobile device. 

We have to set the `java.library.path` system property when we boot the GraalVM part and point it to a location where the dynamic libraries are. 
To prevent the "copy" logic to happen, we also have to fake the JVM vendor flag to `The Android Project` so that it triggers a "classic" load of the library (note that this flag has to be passed to `native-image` so that it builds the proper code path).


Fortunately, this is rather easy to pass flags to GraalVM library, it is done in the sample ios project. The tricky part is building the native code for the target architecture `arm64` (or `aarch64`).

### Build native dynamic libraries

NOTE: fat libraries are not considered for the time being, only targetting `arm64`, we do not bundle different architectures, though it would be doable using `lipo`.

To be able to cross compile to `arm64` we use the following fork of `ios-autotools` that builds _dynamic_ libraries instead of static ones : https://github.com/CedricGatay/ios-autotools.

#### `libsecp256k1`

Using the fork [here](https://github.com/araspitzu/secp256k1/tree/jni_non_static_init).

Then building is pretty straightforward: 

```sh
iconfigure arm64 --enable-experimental --enable-module_ecdh --enable-jni
make clean
make
cp .libs/libsecp256k1.dylib $IOSPROJECT/
```

#### `libsqlitejdbc`

Using the fork [here](https://github.com/CedricGatay/sqlite-jdbc). It adds configuration for architecture `arm64`.

```sh
make jni-header
OS_ARCH=arm64 make native
cp ./target/classes/org/sqlite/native/Mac/arm64/libsqlitejdbc.jnilib $IOSPROJECT/libsqlitejdbc.dylib
```

#### Dummy classes

There are dummy classes for Netty and Jetty parts that are here to please Graal during image generation, there is dead code to track easily what has been done and fallback to simpler use cases if required.