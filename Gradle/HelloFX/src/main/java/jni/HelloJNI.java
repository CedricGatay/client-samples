package jni;

public class HelloJNI {  // Save as HelloJNI.java
    static {
         // Load native library hello.dll (Windows) or libhello.so (Unixes)
        //  at runtime
        // This library contains a native method called sayHello()
        System.loadLibrary("hello");
    }

    // Declare an instance native method sayHello() which receives no parameter and returns void
    private native void sayHello();

    HelloJNI(){
    }

    // Test Driver
    public static void main(String[] args) {

        new HelloJNI().sayHello();  // Create an instance and invoke the native method
    }
}
