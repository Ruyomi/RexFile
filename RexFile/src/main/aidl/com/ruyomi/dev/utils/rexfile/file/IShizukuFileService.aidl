// IShizukuFileService.aidl
package com.ruyomi.dev.utils.rexfile.file;

// Declare any non-default types here with import statements
// import com.ruyomi.dev.utils.rexfile.file.RexFile;

interface IShizukuFileService {
    boolean createNewFile(String path);
    boolean createNewFileAnd(String path);

    boolean canRead(String path);
    boolean canWrite(String path);
    boolean delete(String path);
    boolean deleteAnd(String path);
    boolean exists(String path);
    String getAbsolutePath(String path);
    String getParent(String path);
    boolean isDirectory(String path);
    boolean isFile(String path);
    long lastModified(String path);
    long length(String path);
    long lengthAnd(String path);
    List<String> list(String path);
    boolean mkdirs(String path);
    boolean renameTo(String path, String dest);

    ParcelFileDescriptor getParcelFileDescriptor(String path);
}