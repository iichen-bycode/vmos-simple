# 盖米双开空间 - 接入文档

## 一、联系客服获取授权相关信息

> 【重要】请提供您需要使用 SDK 的 **包名** 给到客服

* 仓库用户名：用于拉取 SDK 仓库

* 仓库密码：用于拉取 SDK 仓库

* 授权文件：用于主工程运行时授权使用

* accessKey：用于 SDK 初始化时鉴权

* accessSecret：用于 SDK 初始化时鉴权





## 二、工程配置

### 1. 配置授权文件

将授权文件 `vmos_lite_sdk-license-com.xxxx.yyyy-zzzzz.txt` 放入主工程根目录下



### 2. 配置Maven

#### 2.1 AGP 7.1以上

在工程根目录 `settings.gradle` 添加 Maven 地址，填写仓库用户名、密码

```text
pluginManagement {
    ...
    repositories {
        maven {
            url "https://maven.vmos.pro"
            credentials {
                username 'username'		// 客服给到的仓库用户名
                password 'password'		// 客服给到的仓库密码
            }
        }
    }
}

dependencyResolutionManagement {
    ...
    repositories {
    	maven { url 'mavenLocal'}
        maven {
            url "https://maven.vmos.pro"
            credentials {
                username 'username'		// 客服给到的仓库用户名
                password 'password'		// 客服给到的仓库密码
            }
        }
    }
}
```

#### 2.2 AGP 7.1以下

在工程根目录 `build.gradle` 添加 Maven 地址，填写仓库用户名、密码

```text
// 插件相关的仓库配置
buildscript {
    repositories {
        maven {
            url "https://maven.vmos.pro"
            credentials {
                username 'username'		// 客服给到的仓库用户名
                password 'password'		// 客服给到的仓库密码
            }
        }
    }
}

// 依赖库相关的仓库配置
allprojects {
    repositories {
    	maven { url 'mavenLocal'}
        maven {
            url "https://maven.vmos.pro"
            credentials {
                username 'username'		// 客服给到的仓库用户名
                password 'password'		// 客服给到的仓库密码
            }
        }
    }
}
```



### 3.  配置工程

####3.1  添加插件

在工程根目录 `build.gradle` 中导入插件

```text
buildscript {
    dependencies {
        classpath 'com.vmos:vmos-build-gradle-plugin:1.2.9'
    }
}
```

在使用 SDK 的 module 的 `build.gradle` 中按如下配置

```
plugins {
    id 'vmos-build'
}
```

####3.2  导入SDK

将 mavenLocal 文件夹（请在 Demo 工程中查看） 拷贝到项目根目录

在主工程的 `build.gradle` 中导入插件

```xml
implementation 'com.sample.app:sdk:1.0.0'
implementation("com.squareup.okhttp3:okhttp:4.10.0")
implementation 'com.google.code.gson:gson:2.11.0'
```

#### 3.3 AndroidManifest.xml 处理

当 `minSdkVersion` >= 23，请将 `extractNativeLibs` 设置为 `true`

```xml
<application
    android:extractNativeLibs="true"
    ...
</application>
```

当 `targetSdkVersion` >= 30，还需要将 `memtagMode` 设置为 `off`

```xml
<application
    android:memtagMode="off"
    ...
</application>
```



## 三、SDK 使用

> 下述方法均为 GmSpaceObject 内的方法

### 1. 初始化

* 作用：鉴权判断，确认 SDK 可用
* 调用时机：在 `Application` 中进行初始化

```java
/**
 * SDK 初始化方法，请在 Application 的 onCreate 方法中调用
 *
 * @param application 当前 application
 * @param accessKey 客服给到的授权 key
 * @param accessSecret 客服给到的授权 secret
 * @param initCallBack 初始化结束后回调
 */
public static void initialize(Application application, String accessKey, String accessSecret, IGmSpaceInitCallBack initCallBack);

/**
 * 回调 code 说明
 */
public class GmSpaceResultCode {
    public static final int INIT_SUCCESS = 0;								// 初始化成功
    public static final int INIT_FAILED_NO_PERMISSION = 1;	// 没有开启权限，请联系客服获取权限
    public static final int INIT_FAILED_CONNECT_FAILED = 2;	// 连接服务器失败，建议间隔一定时间后再次发起重试
    public static final int INIT_FAILED_NETWORK_FAILED = 3;	// 网络连接器失败，建议检查本地网络
}

/**
 * SDK 初始化前准备方法，请在 Application 的 attachBaseContext 方法中调用
 */
public static void attachBaseContext(Context base);
```

调用示例：

```java
public class DemoApplication extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        GmSpaceObject.attachBaseContext(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();
      	GmSpaceObject.initialize(this, accesskey, accessSecret, initCallBack);
    }
}
```



### 2. 事件回调

* 作用：部分 SDK 事件、SDK 内部 App 事件，将通过回调形式进行通知
* 调用时机：在初始化成功后，在 调用实际 SDK 功能前进行注册事件

#### 2.1 注册事件监听

```java
/**
 * 注册事件监听
 * 
 * 主进程注册：收到所有事件
 * server进程注册：收到来自 Server / App 的事件
 * App进程注册：收到来自 App 的事件
 *
 * @param listener 监听回调
 */
public static void registerGmSpaceReceivedEventListener(OnGmSpaceReceivedEventListener listener);
```

#### 2.2 注销事件监听

```java
public static void unregisterGmSpaceReceivedEventListener(OnGmSpaceReceivedEventListener listener);
```

#### 2.3 回调事件说明

##### 2.3.1 应用安装成功事件

* 应用安装成功后，回调此事件（主线程回调）

```java
GmSpaceEvent.TYPE_PACKAGE_INSTALLED == type
  
// 回调时附带的信息
var packageName = extras.getString(GmSpaceEvent.KEY_PACKAGE_NAME)		// 应用包名
var referrer = extras.getString(GmSpaceEvent.KEY_REFERRER)					// 安装来源
var isOverride = extras.getBoolean(GmSpaceEvent.KEY_IS_OVERRIDE)		// 是否覆盖安装
```

##### 2.3.2  应用安装过程事件

* 应用安装过程中的节点，进行阶段性回调

```java
GmSpaceEvent.TYPE_PACKAGE_INSTALLING == type
  
// 回调时附带的信息
var installAction = extras.getString(GmSpaceEvent.KEY_INSTALLING_ACTION);		// 目前处于什么安装阶段

// installAction 值说明：
GmSpaceEvent.VALUE_INSTALLING_ACTION_START = 收到安装请求
GmSpaceEvent.VALUE_INSTALLING_ACTION_PACKAGE = 已经解析出包名
```

##### 2.3.3 应用安装失败事件

* 应用安装失败后，回调此事件（主线程回调）

```java
GmSpaceEvent.TYPE_PACKAGE_INSTALL_FAILURE == type
  
// 回调时附带的信息
var packageName = extras.getString(GmSpaceEvent.KEY_PACKAGE_NAME);			// 应用包名
var code = extras.getInt(GmSpaceEvent.KEY_CODE, GmSpaceResultParcel.CODE_UNKNOWN);		// 安装失败的错误码
var message = extras.getString(GmSpaceEvent.KEY_MESSAGE);								// 安装失败的错误信息

// 错误码说明
GmSpaceResultParcel.CODE_SUCCEED = 成功
GmSpaceResultParcel.CODE_UNKNOWN = 未知异常
GmSpaceResultParcel.CODE_FILE_NOT_EXIST = 文件不存在
GmSpaceResultParcel.CODE_PARAMS_INVALID = 参数不合法
GmSpaceResultParcel.CODE_BLACK_LIST_LIMIT = 黑名单限制
GmSpaceResultParcel.CODE_WHITE_LIST_LIMIT = 白名单限制
GmSpaceResultParcel.CODE_DEVICE_ARCH_NOT_SUPPORTED = 不支持的平台
GmSpaceResultParcel.CODE_PACKAGE_CANT_DOWNGRADE = 不能降级
```

##### 2.3.4 应用卸载事件

* 应用卸载完成后，回调此事件（主线程回调）

```java
GmSpaceEvent.TYPE_PACKAGE_UNINSTALLED == type

// 回调时附带的信息
var packageName = extras.getString(GmSpaceEvent.KEY_PACKAGE_NAME);		// 安装的应用包名
```

##### 2.3.5 进程死亡事件

* 当安装到双开空间内的的 App 的进程死亡后，回调此事件

```java
GmSpaceEvent.TYPE_PROCESS_DIED == type

// 回调时附带的信息
var processName = extras.getString(GmSpaceEvent.KEY_PROCESS_NAME)			// 进程名称
var pid = extras.getInt(GmSpaceEvent.KEY_PID)		// 进程 PID
var packageNameArray = extras.getStringArray(GmSpaceEvent.KEY_PACKAGE_NAME_ARRAY)		// 进程所属的包名列表
var packageName = extras.getString(GmSpaceEvent.KEY_PACKAGE_NAME)		// 进程所属的包名
```



### 3. 应用相关方法

#### 3.1 安装应用到双开空间

```java
public static GmSpaceResultParcel installPackage(String path) {
    return GmObject.installPackage(path);
}

gmSpaceResultParcel.getCode();
gmSpaceResultParcel.getData();
gmSpaceResultParcel.getMessage();

public static final int CODE_UNKNOWN = -1;													// 未知异常
public static final int CODE_SUCCEED = 0;														// 安装成功
public static final int CODE_PARAMS_INVALID = 10001;								// 参数不合法
public static final int CODE_FILE_NOT_EXIST = 10003;								// 文件不存在
public static final int CODE_DEVICE_ARCH_NOT_SUPPORTED = 10008;			// 不支持的架构
public static final int CODE_BLACK_LIST_LIMIT = 10009;							// App 在黑名单中
public static final int CODE_WHITE_LIST_LIMIT = 10010;							// App 不在白名单中
public static final int CODE_PACKAGE_CANT_DOWNGRADE = 10011;				// 无法降级安装
```

#### 3.2 查看指定的应用是否正在安装

```java
/**
 * 查看应用是否正在安装中
 *
 * @param packageName 应用包名
 * @return true：正在安装中 false：已经安装结束，或者未开始安装
 */
public static boolean isGmSpacePackageInstallationInProgress(String packageName) {
    return GmObject.isGmSpacePackageInstallationInProgress(packageName);
}
```

#### 3.3 从双开空间中卸载应用

```java
/**
 * 卸载应用
 *
 * @param packageName 要卸载的应用
 * @return true 卸载成功 false 卸载失败
 */
public static boolean uninstallGmSpacePackage(String packageName) {
    return GmObject.uninstallGmSpacePackage(packageName);
}
```

#### 3.4 在双开空间中打开应用

```java
/**
 * 打开应用
 *
 * @param packageName 应用包名
 */
public static void startApp(String packageName) {
    GmObject.startApp(packageName);
}
```

####  3.5 退出双开空间中运行的应用

```java
/**
 * 退出应用
 *
 * @param packageName 应用包名
 */
public static void killApp(String packageName) {
    GmObject.killApp(packageName);
}
```

#### 3.6 获取应用信息

```java
public static PackageInfo getGmSpacePackageInfo(String packageName) {
    return GmObject.getGmSpacePackageInfo(packageName);
}

public static ApplicationInfo getGmSpaceApplicationInfo(String packageName) {
    return GmObject.getGmSpaceApplicationInfo(packageName);
}
```

#### 3.7 获取已安装的应用

```java
/**
 * 已安装的应用信息列表
 *
 * @return
 */
public static List<PackageInfo> getGmSpaceInstalledPackages() {
    return GmObject.getGmSpaceInstalledPackages();
}

//已安装的应用包名列表
public static List<String> getGmSpaceInstalledPackageNames() {
    return GmObject.getGmSpaceInstalledPackageNames();
}
```

#### 3.8 获取当前运行中的应用

```java
/**
 * 获取运行中的包名列表
 *
 * @return 包名列表
 */
public static List<String> getGmSpaceRunningPackageNames(){
    return GmObject.getGmSpaceRunningPackageNames();
}
```

#### 3.9 获取应用目录在真机上的目录

```java
// 获取应用目录
public static File getGmSpaceHostDir(String packageName, String clientType) {
    return GmObject.getGmSpaceHostDir(packageName, clientType);
}
```

#### 3.10  根据包名获取应用的文件路径信息

```java
public static GmSpaceEnvironmentInfo getGmSpacePackageEnvironmentInfo(String packageName) {
    return GmObject.getGmSpacePackageEnvironmentInfo(packageName);
}
public String dataAppDir;// 同 /data/app/com.xxx.xxx/
public String dataDir;//同 /data/user/0/com.xxx.xxx/
public String externalDataDir;//同 /sdcard/Android/data/com.xxx.xxx/
public String externalObbDir;//同 /sdcard/Android/obb/com.xxx.xxx/
public String externalMediaDir;//同 /sdcard/Android/media/com.xxx.xxx/
public String sdcardExternalDataDir;//同 /storage/emulated/0/Android/data/com.xxx.xxx/
public String sdcardExternalObbDir;//同 /storage/emulated/0/Android/obb/com.xxx.xxx/
public String sdcardExternalMediaDir;//同 /storage/emulated/0/Android/media/com.xxx.xxx/
```

#### 3.11 清除应用数据

```java
// 清除应用数据
public static void clearGmSpaceApplicationUserData(String packageName) {
    GmObject.clearGmSpaceApplicationUserData(packageName);
}
```



## 四、特殊业务场景说明

### 1. 将真机上已安装的应用（非 apk 文件），直接安装到双开空间

```java
// 1. 获取当前真机已安装的应用列表
getContext().getPackageManager().getInstalledPackages(0);

// 2. 将 1 中需要安装的应用 uri 取出，作为参数传入
public static GmSpaceResultParcel installApk(Context context, String uri) {
    try {
        boolean isNeedDelAfterInstall = false;
        // installPackage 接口支持传入apk文件路径和文件夹路径
        // 如果是apks和xapk文件，先解压，再安装
        if (uri.endsWith(".apks") || uri.endsWith(".xapk")) {
            isNeedDelAfterInstall = true;

            uri = unZipFile(context,uri);
        }
        // 安装前，检查apk是否支持当前系统
        checkApkEnable(context,uri);
        String referrer = getReferrer(context, uri);
        // 安装应用，传入apk路径 或者 apks/xapk解压后的文件夹路径
        mGlobalInstallConfig.setIgnorePackageList(false);
        GmSpaceResultParcel resultParcel =  GmSpaceObject.installPackage(uri, mGlobalInstallConfig);
        if(isNeedDelAfterInstall){
            // 拷贝obb文件
            copyObbFile(uri);
            // 如果是xapk和apks文件，安装完成后删除解压的文件
            deleteUnZipFile(uri);
        }
        return resultParcel;
    } catch (Exception e) {
        return GmSpaceResultParcel.failure(e);
    }
}

private static void copyObbFile(String uri) {
    try {
        String obbFolderPath = uri + File.separator + "Android" + File.separator + "obb";
        File obbFolder = new File(obbFolderPath);
        if (obbFolder.exists() && obbFolder.isDirectory()) {
            FileUtils.copyDirectory(obbFolder, GmSpaceObject.getGmSpaceHostDir("", IGmSpaceClient.DIRECTORY_KEY_SDCARD_EXTERNAL_OBB));
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}

private static void checkApkEnable(Context context, String uri) {
    int minSDKVersion =  getMinSdkVersion(uri,context);
    if (minSDKVersion > Build.VERSION.SDK_INT) {
        // 检查apk要求的最低系统版本
        throw  new RuntimeException("apk最低要求系统api版本 " + minSDKVersion + ", 当前 " + Build.VERSION.SDK_INT);
    }
}

private static String unZipFile(Context context, String uri) throws Exception {
    File file = new File(uri);
    String fileName = file.getName();
    // 去掉文件后缀名，作为解压的文件夹名称
    String unZipDirName = FilenameUtils.getNameWithoutExtension(fileName);
    File unZipFile = new File(context.getCacheDir(), unZipDirName);
    String unZipFilePath = unZipFile.getAbsolutePath();
    // 删除残留文件
    deleteUnZipFile(unZipFilePath);
    try {
        // 解压文件
        ZipUtil.unpack(new File(uri),unZipFile);
    }catch (Exception e){
        e.printStackTrace();
        // 解压失败时，可能有残留文件，删除后，把异常抛出去显示提示信息
        deleteUnZipFile(unZipFilePath);
        throw new Exception(fileName+"解压失败");
    }
    return unZipFilePath;
}
```

