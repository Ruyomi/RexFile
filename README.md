# RexFile

## 前言
RexFile是一个十分强大的android-file库。  
同时这也是Ruyomi团队的第一个开源作品，希望各位能够多多提Issues。

### 特点
 - 强大的功能（支持File、DocumentFile、Shizuku、Root）
 - 极高的效率（DocumentFile处理Android/data目录下的资源十分快，实测创建700个文件耗时大约30-40ms）
 - 十分便捷的使用方式

### 注意事项

- 在使用`ShizukuFile`或`RootFile`
  时，确保使用`openInputStream()`/`openOutputStream()`/`newInputStream()`/`newOutputStream()`
  开启的流的`close()`方法仅被调用一次，否则可能会出现问题。
- 针对于最新Android14版本的Android/data目录访问问题，你可以尝试使用`(String).useBug()`来对`path`进行处理。
   
## 引用

### RexFile库

Gradle：

```groovy
implementation 'com.ruyomi.dev.utils:rex-file:1.0.1'
```
or
Kotlin：

```kotlin
implementation("com.ruyomi.dev.utils:rex-file:1.0.1")
```
### 依赖库（必须）
**DocumentFile 需要 `1.0.0` 以上**

Gradle：

```groovy
implementation 'androidx.documentfile:documentfile:1.0.1'
```
or
Kotlin：

```kotlin
implementation("androidx.documentfile:documentfile:1.0.1")
```

### AndroidManifest.xml声明

```html
<uses-permission
  android:name="android.permission.MANAGE_EXTERNAL_STORAGE"
  tools:ignore="ScopedStorage" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

## 使用方式

初始化：

```kotlin
class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    // fileModel参数可以传入RexFileModel的FILE、DOCUMENT、SHIZUKU、ROOT四种操作模式 默认是 FILE
    RexFileConfig.instance.init(this)
  }
  override fun onDestroy() {
    super.onDestroy()
    RexFileConfig.instance.destroy()
  }
}
```

注册权限回调：

```kotlin
// 在AppCompatActivity或ComponentActivity类下注册Activity Result API
class MainActivity : ComponentActivity() {
    val storagePermission = registerStoragePermission( // 注册Storage文件读写权限
        granted = {
            // 授权成功
        },
        denied = {
            // 授权失败
        }
    )

    val allFilePermission = registerAllFilePermission(...) // 注册AllFile所有文件读写权限

    val docPermission = registerDocPermission(...) // 注册DocumentFile访问权限
}
```

发起权限申请：

```kotlin
storagePermission.requestStoragePermission() // 发起Storage文件读写权限申请
allFilePermission.requestAllFilePermission() // 发起AllFile所有文件读写权限申请
docPermission.requestDocPermission() // 发起DocumentFile访问权限申请
requestShizukuPermission(requestCode) // 发起Shizuku权限申请
requestRootPermission() // 发起Root权限申请
```

判断权限：

```kotlin
hasStoragePermission() // 判断Storage文件读写权限
hasAllFilePermission() // 判断AllFile所有文件读写权限
"path".hasDocPermission() // 判断DocumentFile访问权限
hasShizukuPermission() // 判断Shizuku权限
hasRootPermission() // 判断Root权限
```

初始化文件操作类：
```Kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        file(path) // 使用方式于java.io.File差不多，但是封装了一些比较方便好用的方法
    }
}
```

详细内容文档还在狂肝ing...  
欢迎各位给Star，感谢！

## 开源协议
[LGPL v2.1](https://www.gnu.org/licenses/old-licenses/lgpl-2.1.txt)
