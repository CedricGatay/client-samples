// Save as "HelloJNI.c"
#include <jni.h>        // JNI header provided by JDK
#include <stdio.h>      // C Standard IO Header
#include "jni_HelloJNI.h"   // Generated

// Implementation of the native method sayHello()
JNIEXPORT void JNICALL Java_jni_HelloJNI_sayHello
  (JNIEnv *env, jobject thisObject) {
   printf("Hello World!\n");
   return;
}
