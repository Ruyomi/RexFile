package com.ruyomi.dev.utils.rex.file

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import com.ruyomi.dev.utils.rex.file.impl.DocFile
import com.ruyomi.dev.utils.rex.file.impl.IoFile
import com.ruyomi.dev.utils.rex.file.impl.RootFile
import com.ruyomi.dev.utils.rex.file.impl.ShizukuFile
import com.ruyomi.dev.utils.rex.file.utils.RootUtil
import com.ruyomi.dev.utils.rex.file.utils.ShizukuUtil
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

fun file(path: String) = when (RexFileConfig.instance.fileModel) {
    RexFileModel.FILE -> IoFile(path)
    RexFileModel.DOCUMENT -> DocFile(path)
    RexFileModel.SHIZUKU -> ShizukuFile(path)
    RexFileModel.ROOT -> RootFile(path)
}

fun file(file: RexFile, child: String) = when (RexFileConfig.instance.fileModel) {
    RexFileModel.FILE -> IoFile(file, child)
    RexFileModel.DOCUMENT -> DocFile(file, child)
    RexFileModel.SHIZUKU -> ShizukuFile(file, child)
    RexFileModel.ROOT -> RootFile(file, child)
}

fun RexFile.readString(): String = String(readBytes())
fun RexFile.readBytes(): ByteArray = try {
    openInputStream()?.let { readStream(it) } ?: ByteArray(0)
} catch (e: Exception) {
    ByteArray(0)
}

fun RexFile.write(string: String): Boolean = write(string.toByteArray())
fun RexFile.write(bytes: ByteArray): Boolean = try {
    openOutputStream()?.let { writeStream(it, bytes) } == true
} catch (_: Exception) {
    false
}

fun RexFile.copy(target: String, cover: Boolean = false): Boolean = copy(file(target), cover)
fun RexFile.copy(target: RexFile, cover: Boolean = false): Boolean {
    return try {
        // 判断原文件是否存在
        if (!exists()) {
            return false
        }
        // 分类处理
        if (isDirectory()) {
            // 判断对象目录是否存在 以及 是否覆盖（cover） 或 对象是文件夹
            if (target.exists() && (!cover || !target.isDirectory())) {
                return false
            }
            // 对象目录如果不存在就 新建一个文件夹
            if (!target.exists()) {
                target.mkdirs()
            }
            // 遍历原目录下的文件 并 复制到对象目录下
            listFiles().forEach {
                it.copy(file(target, it.name), cover)
            }
        } else if (isFile()) {
            // 判断对象文件是否存在 以及 是否覆盖（cover） 或 对象是文件
            if (target.exists() && (!cover || !target.isFile())) {
                return false
            }
            // 对象文件如果不存在就 新建一个文件
            if (!target.exists()) {
                target.createNewFile()
            }
            // 若原文件长度不为0 也就是不为空时
            if (length() != 0L) {
                // 开始IO流处理
                val inStream = openInputStream()
                val inputStream = BufferedInputStream(inStream)
                val outStream = target.openOutputStream()
                val outputStream = BufferedOutputStream(outStream)

                val bytes = ByteArray(8192)
                var length: Int
                while (inputStream.read(bytes).apply { length = this } > 0) {
                    outputStream.write(bytes, 0, length)
                }
                outputStream.apply {
                    flush()
                    close()
                }
                inputStream.close()
            }
        }
        true
    } catch (e: Exception) {
        false
    }
}

fun RexFile.toZip(
    target: String, cover: Boolean = false, keepStructure: Boolean = true, keepRoot: Boolean = true
): Boolean = toZip(file(target), cover, keepStructure, keepRoot)

fun RexFile.toZip(
    target: RexFile,
    cover: Boolean = false,
    keepStructure: Boolean = true,
    keepRoot: Boolean = true
): Boolean {
    return try {
        if (!exists()) return false
        if (target.exists() && !cover) return false
        if (!target.exists()) target.createNewFile()
        if (target.exists() && !target.isFile()) return false

        val outStream = target.openOutputStream()
        val outputStream = ZipOutputStream(outStream)

        toZip(this, outputStream, keepStructure, keepRoot, if (keepRoot) name else "")

        outputStream.close()

        true
    } catch (_: Exception) {
        false
    }
}

private fun toZip(
    file: RexFile,
    outputStream: ZipOutputStream,
    keepStructure: Boolean,
    keepRoot: Boolean,
    name: String
) {
    if (file.isDirectory()) {
        val files = file.listFiles()
        if (files.isEmpty()) {
            outputStream.apply {
                putNextEntry(ZipEntry("$name/"))
                closeEntry()
            }
            return
        }
        files.forEach {
            toZip(
                it,
                outputStream,
                keepStructure,
                keepRoot,
                "${if (keepStructure) "$name${if (name.isEmpty()) "" else "/"}" else ""}${it.name}"
            )
        }
    } else {
        outputStream.putNextEntry(ZipEntry(name))

        val inStream = file.openInputStream()
        val inputStream = BufferedInputStream(inStream)

        val bytes = ByteArray(8192)
        var length: Int
        while (inputStream.read(bytes).apply { length = this } > 0) {
            outputStream.write(bytes, 0, length)
        }
        outputStream.flush()

        inputStream.close()

        outputStream.closeEntry()
    }
}

fun RexFile.unZip(entry: String, target: String, cover: Boolean, keepStructure: Boolean): Int =
    unZip(entry, file(target), cover, keepStructure)

fun RexFile.unZip(entry: String, target: RexFile, cover: Boolean, keepStructure: Boolean): Int {
    return try {
        val isDir = unZip(this, entry)
        if (!target.exists()) if (isDir) target.mkdirs() else target.createNewFileAnd()
        if (target.exists() && (!cover || (isDir && !target.isDirectory()) || (!isDir && !target.isFile()))) return -1

        var result = 0

        val inStream = openInputStream()
        val inputStream = ZipInputStream(inStream)

        var zEntry = ZipEntry("")
        var zName: String
        if (isDir) {
            while (inputStream.nextEntry?.apply { zEntry = this } != null) {
                zName = zEntry.name
                if (zName.endsWith("/")) zName = zName.substring(0, zName.length - 1)
                if (!zName.contains(entry)) {
                    inputStream.closeEntry()
                    continue
                }
                val zType = zEntry.isDirectory
                val targetFile = file(
                    "${target.path}/${
                        if (keepStructure) name else name.substring(name.lastIndexOf("/") + 1)
                    }"
                ).apply {
                    if (zType) mkdirs() else createNewFileAnd()
                }
                if (!zType) {
                    val outStream = targetFile.openOutputStream()
                    val outputStream = BufferedOutputStream(outStream)

                    val bytes = ByteArray(8192)
                    var length: Int
                    while (inputStream.read(bytes).apply { length = this } > 0) {
                        outputStream.write(bytes, 0, length)
                    }

                    outputStream.apply {
                        flush()
                        close()
                    }
                }

                result++
                inputStream.closeEntry()
            }
        } else {
            while (inputStream.nextEntry?.apply { zEntry = this } != null) {
                zName = zEntry.name
                if (!zName.contains(entry) || zName.endsWith("/")) {
                    inputStream.closeEntry()
                    continue
                }
                val outStream = target.openOutputStream()
                val outputStream = BufferedOutputStream(outStream)

                val bytes = ByteArray(8192)
                var length: Int
                while (inputStream.read(bytes).apply { length = this } > 0) {
                    outputStream.write(bytes, 0, length)
                }

                outputStream.apply {
                    flush()
                    close()
                }

                result++
                inputStream.closeEntry()

                break
            }
        }

        inputStream.close()

        result
    } catch (_: Exception) {
        -1
    }
}

private fun unZip(file: RexFile, entry: String): Boolean {
    return try {
        if (!file.exists() || !file.isFile()) return false
        var isDir = 0

        val inStream = file.openInputStream()
        val inputStream = ZipInputStream(inStream)

        var entryName = ""
        while (inputStream.nextEntry?.apply { entryName = name } != null) {
            if (entryName.contains(entry)) isDir++
            inputStream.closeEntry()
        }

        inputStream.close()

        isDir > 1
    } catch (_: Exception) {
        false
    }
}

fun readStream(stream: InputStream, close: Boolean = true): ByteArray = try {
    val inputStream = BufferedInputStream(stream)
    val outputStream = ByteArrayOutputStream()

    val bytes = ByteArray(8192)
    var length: Int
    while (inputStream.read(bytes).apply { length = this } > 0) {
        outputStream.write(bytes, 0, length)
    }
    outputStream.apply {
        flush()
        close()
    }
    if (close) inputStream.close()
    stream.close()
    outputStream.toByteArray()
} catch (_: Exception) {
    ByteArray(0)
}

fun writeStream(stream: OutputStream, bytes: ByteArray, close: Boolean = true): Boolean = try {
    val outputStream = BufferedOutputStream(stream)

    outputStream.apply {
        write(bytes)
        flush()
        if (close) close()
    }

    true
} catch (_: Exception) {
    false
}

@SuppressLint("Recycle")
fun RexFile.openInputStream() = when (this) {
    is IoFile -> FileInputStream(path)

    is DocFile -> RexFileConfig.instance.context.contentResolver.openInputStream(
        docFile?.uri ?: path.documentPathToUri()
    )

    is ShizukuFile -> FileInputStream(
        ParcelFileDescriptor.AutoCloseInputStream(
            ShizukuUtil.getShizukuFileService().getParcelFileDescriptor(path)
        ).fd
    )

    is RootFile -> newInputStream()
    else -> null
}

@SuppressLint("Recycle")
fun RexFile.openOutputStream() = when (this) {
    is IoFile -> FileOutputStream(path)

    is DocFile -> RexFileConfig.instance.context.contentResolver.openOutputStream(
        run {
            createNewFileAnd()
            docFile?.uri ?: path.documentPathToUri()
        }, "rwt"
    )

    is ShizukuFile -> FileOutputStream(
        ParcelFileDescriptor.AutoCloseOutputStream(
            ShizukuUtil.getShizukuFileService().getParcelFileDescriptor(path)
        ).fd
    )

    is RootFile -> newOutputStream()
    else -> null
}

private fun RootFile.newInputStream(): InputStream {
    if (!exists()) throw FileNotFoundException("No such file or directory: $path")
    if (isDirectory()) throw FileNotFoundException("Is a directory: $path")
    Log.d("TAG", "ok first!")
    try {
        val fifo = createTempFifo()
        if (RootUtil.executeCommand("cp -f $path ${fifo.path}").first != 0) throw FileNotFoundException(
            "cp failed: $path"
        )
        val inputStream = FileInputStream(fifo)
        return object : InputStream() {
            override fun read(): Int = inputStream.read()
            override fun read(b: ByteArray?): Int = inputStream.read(b)
            override fun read(b: ByteArray?, off: Int, len: Int): Int =
                inputStream.read(b, off, len)

            override fun available(): Int = inputStream.available()
            override fun close() {
                inputStream.close()
                fifo.delete()
            }
        }
    } catch (e: Exception) {
        if (e is FileNotFoundException) throw e
        val cause = e.cause
        if (cause is FileNotFoundException) throw cause
        val err = FileNotFoundException("Failed to open fifo").initCause(e)
        throw (err as FileNotFoundException)
    }
}

private fun RootFile.newOutputStream(): OutputStream {
    if (isDirectory()) throw FileNotFoundException("Is a directory: $path")
    if (!exists() && !createNewFileAnd()) {
        throw FileNotFoundException("No such file or directory: $path")
    } else if (!clear()) {
        throw FileNotFoundException("Failed to clear file: $path")
    }
    Log.d("TAG", "ok first out!")
    try {
        val fifo = createTempFifo()
        if (RootUtil.executeCommand("cp -f ${fifo.path} $path").first != 0) throw FileNotFoundException(
            "cp failed: $path"
        )
        val outputStream = FileOutputStream(fifo)
        return object : OutputStream() {
            override fun write(b: Int) = outputStream.write(b)
            override fun write(b: ByteArray) = outputStream.write(b)
            override fun write(b: ByteArray, off: Int, len: Int) = outputStream.write(b, off, len)
            override fun flush() = outputStream.flush()
            override fun close() {
                if (!fifo.exists()) throw FileNotFoundException("No such file or directory: ${fifo.path}")
                try {
                    outputStream.close()
                } finally {
                    if (RootUtil.executeCommand("mv -f \"${fifo.path}\" \"${path}\"").first != 0) throw FileNotFoundException(
                        "cp failed: $path"
                    )
                }
            }
        }
    } catch (e: Exception) {
        Log.d("TAG newOut", e.toString())
        if (e is FileNotFoundException) throw e
        val cause = e.cause
        if (cause is FileNotFoundException) throw cause
        val err = FileNotFoundException("Failed to open fifo").initCause(e)
        throw (err as FileNotFoundException)
    }
}

fun createTempFifo(): File {
    val dir = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
        ".rex-file-tmp"
    )
    if (!dir.exists()) dir.mkdirs()
    return File(dir, "fifo-${UUID.randomUUID()}.tmp").apply { createNewFile() }
}

//------ FILE ------//
fun isBug() =
    File("${documentRootPath}Android\u200b/").list()
        ?.contentEquals(File("${documentRootPath}Android/").list()) == true

fun String.useBug() =
    if (isBug() && !contains("\u200b")) {
        replaceFirst("Android", "Android\u200b")
    } else {
        replace("\u200b", "")
    }

private val storagePermissions =
    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)

fun hasStoragePermission(): Boolean {
    storagePermissions.forEach {
        if (ContextCompat.checkSelfPermission(
                RexFileConfig.instance.context, it
            ) != PackageManager.PERMISSION_GRANTED
        ) return false
    }
    return true
}

fun ComponentActivity.registerStoragePermission(
    granted: (() -> Unit)? = null, denied: (() -> Unit)? = null
) = registerForActivityResult(
    ActivityResultContracts.RequestMultiplePermissions()
) {
    if (hasStoragePermission()) granted?.let { it1 -> it1() }
    else denied?.let { it1 -> it1() }
}

fun Fragment.registerStoragePermission(
    granted: (() -> Unit)? = null, denied: (() -> Unit)? = null
) = registerForActivityResult(
    ActivityResultContracts.RequestMultiplePermissions()
) {
    if (hasStoragePermission()) granted?.let { it1 -> it1() }
    else denied?.let { it1 -> it1() }
}

fun ActivityResultLauncher<Array<String>>.requestStoragePermission() = launch(storagePermissions)
//------ FILE ------//

//------ DOCUMENT ------//
internal val documentRootPath = "${Environment.getExternalStorageDirectory().path}/"

val documentPermissions: Array<String>
    get() = RexFileConfig.instance.context.contentResolver.persistedUriPermissions.mapNotNull {
        if (it.isReadPermission && it.isWritePermission) it.uri.documentPath()
        else ""
    }.toTypedArray()

fun String.documentPathToPath(): String {
    var path = this
    if (path.lowercase().startsWith(documentRootPath.lowercase()))
        path = path.substring(documentRootPath.length)
    return path.trim('/')
}

fun String.documentPathToUri(tree: String? = null): Uri {
    var path = documentPathToPath()
    var trees: String
    return Uri.Builder().scheme("content").authority("com.android.externalstorage.documents")
        .appendPath("tree").appendPath("primary:${
            (tree?.documentPathToPath() ?: documentPermissions.find {
                path.startsWith(it)
            } ?: path).apply { trees = this }
        }").run {
            if (path != "" && path.lowercase() != trees.lowercase()) {
                if (path.lowercase().startsWith("${trees.lowercase()}/")) {
                    path = path.substring("$trees/".length)
                }
                appendPath("document")
                appendPath("primary:$trees/$path")
            }
            build()
        }
}

fun Uri.documentPath() = Uri.decode(toString()).run { substring(lastIndexOf(":") + 1) }
fun Uri.documentAbsolutePath() = "$documentRootPath${documentPath()}"

fun hasAllFilePermission() =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) Environment.isExternalStorageManager() else true

fun ComponentActivity.registerAllFilePermission(
    granted: (() -> Unit)? = null, denied: (() -> Unit)? = null
) = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
    if (hasAllFilePermission()) granted?.let { it1 -> it1() }
    else denied?.let { it1 -> it1() }
}

fun Fragment.registerAllFilePermission(
    granted: (() -> Unit)? = null, denied: (() -> Unit)? = null
) = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
    if (hasAllFilePermission()) granted?.let { it1 -> it1() }
    else denied?.let { it1 -> it1() }
}

fun ActivityResultLauncher<Intent>.requestAllFilePermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
        launch(Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
            data = Uri.parse("package:${RexFileConfig.instance.context.packageName}")
        })
}

fun String.hasDocPermission(): Boolean =
    documentPermissions.find { documentPathToPath().startsWith(it) } != null

@SuppressLint("WrongConstant")
fun ComponentActivity.registerDocPermission(
    granted: (() -> Unit)? = {}, denied: (() -> Unit)? = {}
) = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            result.data!!.data?.let {
                contentResolver.takePersistableUriPermission(
                    it,
                    result.data!!.flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                )
            }
            granted?.let { it() }
        } else denied?.let { it() }
    }

@SuppressLint("WrongConstant")
fun Fragment.registerDocPermission(
    granted: (() -> Unit)? = {}, denied: (() -> Unit)? = {}
) = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
    if (result.resultCode == Activity.RESULT_OK && result.data != null) {
        result.data!!.data?.let {
            requireContext().contentResolver.takePersistableUriPermission(
                it,
                result.data!!.flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            )
        }
        granted?.let { it() }
    } else denied?.let { it() }
}

fun ActivityResultLauncher<Intent>.requestDocPermission(path: String, sub: Boolean = true) =
    launch(Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
    flags =
        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_PREFIX_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
    putExtra(
        "android.provider.extra.INITIAL_URI", DocumentFile.fromTreeUri(
            RexFileConfig.instance.context, "".documentPathToUri(
                if (sub) {
                    StringBuilder().apply {
                        if (path.replace("\u200b", "").contains("Android/data")) {
                            path.documentPathToPath().split("/").apply {
                                for (i in 0 until if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) 3 else 2) {
                                    append("${this[i]}/")
                                }
                            }
                        } else {
                            path.documentPathToPath().split("/").onEach { str ->
                                append("$str/")
                            }
                        }
                    }.toString()
                } else path
            )
        )?.uri
    )
})
//------ DOCUMENT ------//

//------ SHIZUKU ------//
fun hasShizukuPermission() = ShizukuUtil.hasPermission()

fun registerShizukuPermission(
    granted: (() -> Unit)? = {}, denied: (() -> Unit)? = {}
) = ShizukuUtil.addRequestPermissionResultListener { _, grantResult ->
    if (grantResult == PackageManager.PERMISSION_GRANTED) {
        if (!ShizukuUtil.peekService() && ShizukuUtil.hasPermission()) {
            ShizukuUtil.bindService()
        }
        granted?.let { it() }
    } else {
        denied?.let { it() }
    }
}

fun requestShizukuPermission(requestCode: Int) = ShizukuUtil.requestPermission(requestCode)

fun peekShizukuService() = ShizukuUtil.peekService()

fun bindShizukuService() = ShizukuUtil.bindService()

fun unbindShizukuService() = ShizukuUtil.unbindService()
//------ SHIZUKU ------//

//------ ROOT ------//
fun hasRootPermission() = RootUtil.hasPermission()

fun requestRootPermission() = RootUtil.requestPermission()
//------ ROOT ------//

abstract class RexFile : Comparator<RexFile> {

    internal val file: File

    constructor(path: String) {
        this.file = File(path)
    }

    constructor(file: RexFile, child: String) {
        this.file = File(file.file, child)
    }

    override fun compare(o1: RexFile, o2: RexFile): Int = o1.file.compareTo(o2.file)

    val name: String
        get() = file.name
    val path: String
        get() = file.path

    @Throws(IOException::class)
    abstract fun createNewFile(): Boolean

    @Throws(IOException::class)
    abstract fun createNewFileAnd(): Boolean

    abstract fun canRead(): Boolean
    abstract fun canWrite(): Boolean
    abstract fun delete(): Boolean
    abstract fun deleteAnd(): Boolean
    abstract fun exists(): Boolean
    abstract fun getAbsolutePath(): String
    abstract fun getParent(): String
    abstract fun getParentFile(): RexFile
    abstract fun isDirectory(): Boolean
    abstract fun isFile(): Boolean
    abstract fun lastModified(): Long
    abstract fun length(): Long
    abstract fun lengthAnd(): Long
    abstract fun list(): Array<String>
    abstract fun list(filter: (String) -> Boolean): Array<String>
    abstract fun listFiles(): Array<RexFile>
    abstract fun listFiles(filter: (RexFile) -> Boolean): Array<RexFile>
    abstract fun mkdirs(): Boolean
    abstract fun renameTo(dest: String): Boolean

}