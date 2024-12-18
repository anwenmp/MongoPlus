package com.mongoplus.toolkit;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtil {

    public static void zipDirectory(String sourceDirPath, String zipFilePath) {
        Path sourceDir = Paths.get(sourceDirPath);  // 获取源文件夹路径
        try (FileOutputStream fos = new FileOutputStream(zipFilePath);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            // 调用递归方法压缩文件夹
            zipDirectoryRecursively(sourceDir, sourceDir, zos);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 递归方法：压缩文件夹及其中的文件和子文件夹
    private static void zipDirectoryRecursively(Path rootDir, Path currentDir, ZipOutputStream zos) throws IOException {
        // 获取当前文件夹中的所有文件和子文件夹
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(currentDir)) {
            for (Path path : directoryStream) {
                if (Files.isDirectory(path)) {
                    // 如果是文件夹，递归调用该方法处理子文件夹
                    zipDirectoryRecursively(rootDir, path, zos);
                } else {
                    // 如果是文件，直接压缩
                    zipFile(rootDir, path, zos);
                }
            }
        }
    }

    // 压缩单个文件
    private static void zipFile(Path rootDir, Path file, ZipOutputStream zos) throws IOException {
        // 获取相对于根目录的文件路径，确保压缩时不包含绝对路径
        Path relativePath = rootDir.relativize(file);
        // 创建一个 ZipEntry，用来表示文件在 ZIP 压缩包中的路径
        ZipEntry zipEntry = new ZipEntry(relativePath.toString());
        zos.putNextEntry(zipEntry);

        // 读取文件并将内容写入 ZIP 文件
        try (InputStream is = Files.newInputStream(file)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) >= 0) {
                zos.write(buffer, 0, length);
            }
        }

        // 关闭当前的 ZipEntry
        zos.closeEntry();
    }

}
