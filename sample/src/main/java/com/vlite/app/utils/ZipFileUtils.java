package com.vlite.app.utils;



import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipFileUtils {
     public static boolean unZip(String zipFilePath,String destDir){
        try {
            File destDirFile = new File(destDir);
            ZipFile zipFile = new ZipFile(zipFilePath);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()){
                ZipEntry zipEntry = entries.nextElement();
                boolean isSuccess = unZipChildFile(destDirFile, zipFile, zipEntry);
                if(!isSuccess){
                    return false;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    private static boolean unZipChildFile(final File destDir, final ZipFile zip, final ZipEntry entry)
            throws IOException {
        File file = new File(destDir, entry.getName());
        if (entry.isDirectory()) {
            return file.mkdirs();
        } else {
            file.getParentFile().mkdirs();
            file.createNewFile();
            InputStream in = null;
            OutputStream out = null;
            try {
                in = new BufferedInputStream(zip.getInputStream(entry));
                out = new BufferedOutputStream(new FileOutputStream(file));
                byte buffer[] = new byte[1024*1024];
                int len;
                while ((len = in.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
            }finally {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            }
        }
        return true;
    }
}
