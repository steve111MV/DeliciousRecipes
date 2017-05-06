-dontwarn org.androidannotations.*
-dontwarn org.springframework.*
#-dontwarn java.nio.file.*
#-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
#-dontwarn okio.**
-dontnote org.apache.http.**
-dontwarn org.xmlpull.v1.**
-dontnote org.xmlpull.v1.**
-dontwarn org.postgresql.**
-dontwarn java.awt.**

#-dontwarn org.springframework.http.**
#-dontwarn org.springframework.web.**
#-dontwarn org.springframework.core.**

-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.GeneratedRootGlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}