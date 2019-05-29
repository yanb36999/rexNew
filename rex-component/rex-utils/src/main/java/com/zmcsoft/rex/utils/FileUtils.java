package com.zmcsoft.rex.utils;

import org.apache.commons.codec.binary.Base64;
import org.hswebframework.expands.request.RequestBuilder;
import org.hswebframework.expands.request.SimpleRequestBuilder;
import org.springframework.util.StreamUtils;

import java.io.*;

public class FileUtils {

    private static final RequestBuilder requestBuilder = new SimpleRequestBuilder();

    public static InputStream getFileInputStream(String fileNameOrUrl) throws IOException {
        if (fileNameOrUrl.startsWith("http")) {
            return requestBuilder.http(fileNameOrUrl)
                    .download().get().response().asStream();
        }
        return new FileInputStream(fileNameOrUrl);
    }

    /**
     * 将文件转为base64
     * @param fileNameOrUrl 文件名称或者文件的url
     * @return base64字符串
     * @throws IOException
     */
    public static String convertFileToBase64(String fileNameOrUrl) throws IOException {
        if (fileNameOrUrl==null||fileNameOrUrl.equals("")){
            return "没有图片";
        }else {
            try (InputStream inputStream = getFileInputStream(fileNameOrUrl)) {
                return Base64.encodeBase64String(StreamUtils.copyToByteArray(inputStream));
            }
        }
    }

    //
//    public static void main(String[] args) throws IOException {
//        String base64 = getFileBase64("http://file.rex.cdjg.gov.cn:8090/upload/20171213/1080143419364102.jpg");
//
//        System.out.println(base64);
//        byte[] data = Base64.decodeBase64(base64);
//        new FileOutputStream("static/test.jpg").write(data);
//    }
}
