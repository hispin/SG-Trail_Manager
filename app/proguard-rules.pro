# R8 rules of the release build.
# R8 removes the code that the app does not use (e.g. the parts of the Material library that call the
# deprecated Window.setStatusBarColor, which Google Play reports for edge-to-edge).

# keep the names of the classes and methods (readable crash reports, no risk for reflection)
-dontobfuscate

-keepattributes Signature, *Annotation*, InnerClasses, EnclosingMethod, SourceFile, LineNumberTable

# The models are stored as JSON with Gson (the field names are the keys of the stored data)
-keep class com.sensoguard.trailmanager.classes.Alarm { *; }
-keep class com.sensoguard.trailmanager.classes.Camera { *; }
-keep class com.sensoguard.trailmanager.classes.MyEmailAccount { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# JavaMail loads the providers (IMAP etc.) and the data handlers by name
-keep class javax.mail.** { *; }
-keep class com.sun.mail.** { *; }
-keep class javax.activation.** { *; }
-keep class com.sun.activation.** { *; }
-keep class myjava.awt.datatransfer.** { *; }
-dontwarn java.awt.**
-dontwarn java.beans.**
-dontwarn javax.naming.**
-dontwarn javax.security.auth.callback.**
-dontwarn javax.security.sasl.**
-dontwarn javax.activation.**
-dontwarn com.sun.activation.**
-dontwarn sun.security.**

