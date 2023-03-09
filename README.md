## 编译工程
1. 将授权文件放到工程根目录下
2. 将Maven账号按以下格式填入`local.properties`文件
```
artifactory.user=xxx
artifactory.password=xxx
```



### Keep选项

|           作用范围           |       保持所指定类、成员       | 所指定类、成员在压缩阶段没有被删除，才能被保持 |
| :-: | :-: | :-: |
|          类和类成员          |          `-keep`          |  `-keepnames`               |
|           仅类成员           |    `-keepclassmembers`    | `-keepclassmembernames`          |
| 类和类成员(前提是成员都存在) | `-keepclasseswithmembers` |    `-keepclasseswithmembernames` |

