# 打印混淆信息
-verbose
# 不对class进行优化，默认开启优化
-dontoptimize
# 不忽略非公共的库的类的成员
-dontskipnonpubliclibraryclassmembers
# 声明不进行压缩操作，默认除了-keep的类及其直接或间接引用到的类，都会被移除
-dontshrink
# 将.class信息中的类名重新定义为"SourceFile"字符串
-renamesourcefileattribute SourceFile
# 保留源文件名为"SourceFile"字符串，而非原始的类名 并保留行号
-keepattributes SourceFile, LineNumberTable, RuntimeVisible*Annotation*
-keeppackagenames

-dontshrink
-keepattributes RuntimeVisible*Annotation*,InnerClasses
-keepattributes Signature,EnclosingMethod

-dontwarn android.**
-dontwarn com.android.**
-dontwarn ref.**

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.ContentProvider

-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
#-repackageclasses z1

#-keepattributes SourceFile,LineNumberTable

-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

# Keep (ie. don't remove) all public constructors of all public classes, but still obfuscate+optimize their content. This is necessary because optimization removes constructors which I use through reflection.
-keepclassmembers class * {
    <init>(...);
}

-keepclassmembers class * extends java.lang.Enum {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class androidx.** {*;}
-keep public class * extends androidx.**
-keep interface androidx.** {*;}
-keep class android.view.** {*;}
-keep class android.content.** {*;}
-dontwarn androidx.**
# ----- common ---------------------------------------  end

-keep class com.google.gson.** { *; }
-keep class com.tencent.mars.** { *; }
-keep class okhttp3.** { *; }
-keep class retrofit2.** { *; }
-keep class me.jessyan.autosize.** { *; }
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }
-keep class com.google.** { *; }
-keep class com.tencent.** { *; }
-keep class jonathanfinerty.once.** { *; }
-keep class es.dmoral.toasty.** { *; }
-keep class com.airbnb.lottie.** { *; }
-keep class com.luck.picture.lib.** { *; }
-keep class com.makeramen.roundedimageview.** { *; }
-keep class com.github.promeg.pinyinhelper.** { *; }
-keep class com.bumptech.glide.** { *; }

-keep class com.gmspace.app.bean.** { *; }
-keep class com.baidu.protect.** {
    *;
}
-keep class com.ssy185.** {
    *;
}