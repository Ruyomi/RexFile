package com.ruyomi.dev.utils.rexfile.file.impl

import com.ruyomi.dev.utils.rexfile.file.RexFile
import com.ruyomi.dev.utils.rexfile.shell.RootUtil
import java.io.File

class RootFile : RexFile {

    constructor(path: String) : super(path)
    constructor(file: RexFile, child: String) : super(file, child)

    private fun runCommand(command: String): Boolean = RootUtil.executeCommand(command).first == 0

    private fun runCommandWithOutput(command: String): String? {
        val (exitValue, output) = RootUtil.executeCommand(command)
        return if (exitValue == 0) output.trim() else null
    }

    override fun createNewFile(): Boolean = runCommand("touch $path")

    override fun createNewFileAnd(): Boolean {
        val parentPath = file.parentFile?.path ?: return false
        return runCommand("mkdir -p $parentPath") && createNewFile()
    }

    override fun canRead(): Boolean = runCommand("test -r $path")

    override fun canWrite(): Boolean = runCommand("test -w $path")

    override fun delete(): Boolean = runCommand("rm $path")

    override fun deleteAnd(): Boolean = runCommand("rm -r $path")

    override fun exists(): Boolean = runCommand("test -e $path")

    override fun getAbsolutePath(): String = file.absolutePath

    override fun getParent(): String = runCommandWithOutput("dirname $path") ?: ""

    override fun getParentFile(): RootFile = RootFile(getParent())

    override fun isDirectory(): Boolean = runCommand("test -d $path")

    override fun isFile(): Boolean = runCommand("test -f $path")

    override fun lastModified(): Long =
        runCommandWithOutput("stat -c %Y $path")?.toLongOrNull() ?: 0L

    override fun length(): Long =
        runCommandWithOutput("stat -c %s $path")?.toLongOrNull() ?: 0L

    override fun lengthAnd(): Long =
        runCommandWithOutput("du -sb $path | cut -f1")?.toLongOrNull() ?: 0L

    override fun list(): Array<String> =
        runCommandWithOutput("ls -1 $path")?.lines()?.map {
            "$path${File.separator}$it"
        }?.toTypedArray() ?: arrayOf()

    override fun list(filter: (String) -> Boolean): Array<String> =
        runCommandWithOutput("ls -1 $path")?.lines()?.filter {
            filter("$path${File.separator}$it")
        }?.toTypedArray() ?: arrayOf()

    override fun listFiles(): Array<RexFile> =
        runCommandWithOutput("ls -1 $path")?.lines()?.map {
            RootFile("$path${File.separator}$it")
        }?.toTypedArray() ?: arrayOf()

    override fun listFiles(filter: (RexFile) -> Boolean): Array<RexFile> =
        runCommandWithOutput("ls -1 $path")?.lines()?.map {
            RootFile(it)
        }?.filter { filter(it) }?.toTypedArray() ?: arrayOf()

    override fun mkdirs(): Boolean = runCommand("mkdir -p $path")

    override fun renameTo(dest: String): Boolean = runCommand("mv $path $dest")

    fun clear(): Boolean = runCommand("echo -n > $path")
}