package com.zmcsoft.rex.utils;


import java.io.*;
import java.util.TreeSet;
import java.util.zip.*;

/**
 * Created by liuke on 2018/7/3.
 */
public class ZipUtil {

    public static TreeSet<String> ts = new TreeSet<String>();

    public static void ZipArbitraryLayerFolderMethod(File sFoder, File zipFolder, String exceptDir) throws IOException {

        ZipOutputStream zipoutFolder = new ZipOutputStream(new FileOutputStream(zipFolder));

        try {
            InputStream in = null;

            zipoutFolder.setComment("文件夹的压缩");

            //列出所有文件的路径，保存到集合中，在ListAllDirectory(sFoder)方法中用到递归
            TreeSet<String> pathTreeSet = ListAllDirectory(sFoder);


            String[] pathStr = pathTreeSet.toString().substring(1, pathTreeSet.toString().length() - 1).split(",");

            for (int i = 0; i < pathStr.length; i++) {
                String filePath = pathStr[i].trim();
                StringBuffer pathURL = new StringBuffer();
                String[] tempStr = filePath.split("\\\\");  //这个地方需要注意，在Java中需要“\\\\”表示“\”字符串。
                for (int j = 1; j < tempStr.length - 1; j++) {
                    pathURL.append(tempStr[j] + File.separator);
                }
                String path = pathURL.append(tempStr[tempStr.length - 1]).toString();
                in = new FileInputStream(new File(filePath));

                String s = path.replace(exceptDir, "");
                zipoutFolder.putNextEntry(new ZipEntry(s));

                int temp = 0;
                while ((temp = in.read()) != -1) {
                    zipoutFolder.write(temp);
                }

                in.close();
            }
        } catch (Exception e) {

        } finally {
            zipoutFolder.close();
        }
    }


    public static void zipFile(File zipFolder, String filePath,String fileName) throws IOException {

        ZipOutputStream zipoutFolder = new ZipOutputStream(new FileOutputStream(zipFolder));

        try {
            InputStream in = null;

            zipoutFolder.setComment("文件夹的压缩");
            in = new FileInputStream(new File(filePath));
            zipoutFolder.putNextEntry(new ZipEntry(fileName));
            int temp = 0;
            while ((temp = in.read()) != -1) {
                zipoutFolder.write(temp);
            }

            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            zipoutFolder.close();
        }
    }

    public static TreeSet<String> ListAllDirectory(File sFolder) {
        if (sFolder != null) {
            if (sFolder.isDirectory()) {
                File f[] = sFolder.listFiles();
                if (f != null) {
                    for (int i = 0; i < f.length; i++) {
                        ListAllDirectory(f[i]);
                    }
                }
            } else {
                ts.add(sFolder.toString());
            }
        }
        return ts;
    }

    public static void main(String[] args) {
        try {
            ZipUtil.zipFile(new File("/data/rex-learn/"+"123"+".zip"),"/data/rex-learn/" + "123.mfxxdj","123.mfxxdj");
            File file = new File("/data/rex-learn/"+"123"+".zip");
            file.renameTo(new File("/data/rex-learn/"+"456"+".txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
