## 下载授权文件
1. 登录SDK后台 https://sdk-service.vmos.cn
2. 下载包名为`com.vlite.app`的授权文件

## 编译工程
1. 将授权文件放到工程根目录下
2. 将Maven账号（一般与SDK后台同账号）按以下格式填入`local.properties`文件
```
artifactory.user=hangzhoushubao
artifactory.password=j1p4sm5A
```
### 环境要求
- **CMake 3.22.1** - 本工程编译需要 CMake 的 3.22.1 版本，请确保你的 IDE 或开发环境中安装并配置了正确版本的CMake。
