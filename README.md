# RexFile

## 前言
RexFile是一个十分强大的android-file库。
是Ruyomi团队的第一个开源作品，希望各位能够多多提Issues。

### 特点
 - 强大的功能（支持File、DocumentFile、Shizuku、Root）
 - 极高的效率（DocumentFile处理Android/data目录下的资源十分快，实测创建700个文件耗时大约30-40ms）
 - 十分便捷的使用方式
   
## 引用

### RexFile库

Gradle：
```Gradle
implementation 'com.ruyomi.dev.utils:rex-file:1.0.0'
```
or
Kotlin：
```Kotlin
implementation("com.ruyomi.dev.utils:rex-file:1.0.0")
```
### 依赖库（必须）
**DocumentFile 需要 `1.0.0` 以上**

Gradle：
```Gradle
implementation 'androidx.documentfile:documentfile:1.0.1'
```
or
Kotlin：
```Kotlin
implementation("androidx.documentfile:documentfile:1.0.1")
```

### AndroidManifest.xml声明
```Html
<uses-permission
  android:name="android.permission.MANAGE_EXTERNAL_STORAGE"
  tools:ignore="ScopedStorage" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

## 使用方式

初始化：
```Kotlin
RexFileConfig.instance.init(this) // 第二个参数可以传入RexFileModel的FILE、DOCUMENT、SHIZUKU、ROOT四种操作模式
```

注册权限回调：
```Kotlin
// 在AppCompatActivity或ComponentActivity类下注册Activity Result API

val storagePermission = registerStoragePermission( // 注册Storage文件读写权限
  granted = {
    // 授权成功
  },
  denied = {
    // 授权失败
  }
)

val allFilePermission = registerAllFilePermission( // 注册AllFile所有文件读写权限
  granted = {
    // 授权成功
  },
  denied = {
    // 授权失败
  }
)

val docPermission = registerDocPermission( // 注册DocumentFile访问权限
  granted = {
    // 授权成功
  },
  denied = {
    // 授权失败
  }
)
```

发起权限申请：
```Kotlin
storagePermission.requestStoragePermission() // 发起Storage文件读写权限申请
allFilePermission.requestAllFilePermission() // 发起AllFile所有文件读写权限申请
docPermission.requestDocPermission() // 发起DocumentFile访问权限申请
```

初始化文件操作类：
```Kotlin
file(path)
```

详细内容文档还在狂肝ing...
欢迎各位来给Star，感谢！

## 开源协议
[LGPL v2.1](https://www.gnu.org/licenses/old-licenses/lgpl-2.1.txt)
