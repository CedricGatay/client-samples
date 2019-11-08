package jni;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class HelloJNI {  // Save as HelloJNI.java
    static {
         // Load native library hello.dll (Windows) or libhello.so (Unixes)
        //  at runtime
        // This library contains a native method called sayHello()
    }

    // Declare an instance native method sayHello() which receives no parameter and returns void
    private native void sayHello();

    HelloJNI(){
        try {
            Path jniTest = Files.createTempDirectory("jni_test");
            File extractedLibFile = new File(jniTest.toFile(), "libhello.dylib");
            InputStream reader = HelloJNI.class.getResourceAsStream("libhello.dylib");
            FileOutputStream writer = new FileOutputStream(extractedLibFile);
            try {
                byte[] buffer = new byte[8192];
                int bytesRead = 0;
                while((bytesRead = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, bytesRead);
                }
            }
            finally {
                // Delete the extracted lib file on JVM exit.
                extractedLibFile.deleteOnExit();

                if(writer != null) {
                    writer.close();
                }
                if(reader != null) {
                    reader.close();
                }
            }

            // Set executable (x) flag to enable Java to load the native library
            extractedLibFile.setReadable(true);
            extractedLibFile.setWritable(true, true);
            extractedLibFile.setExecutable(true);

            System.load(extractedLibFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // Test Driver
    public static void main(String[] args) {
        new HelloJNI().sayHello();  // Create an instance and invoke the native method
    }
}
