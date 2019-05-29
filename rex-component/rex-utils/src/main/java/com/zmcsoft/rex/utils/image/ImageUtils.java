package com.zmcsoft.rex.utils.image;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhouhao
 * @since 3.0
 */
public class ImageUtils {

    /**
     * 生成条码
     *
     * @see this#createQrCode(int, int, String, OutputStream)
     */
    public static void createBarCode(int width, int height, String content, OutputStream outputStream) throws Exception {
        String format = "png";
        Map<EncodeHintType, Object> hints = new HashMap<>(6);
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.MARGIN, 2);
        BitMatrix bitMatrix = new MultiFormatWriter()
                .encode(content, BarcodeFormat.CODE_128, width, height, hints);
        MatrixToImageWriter.writeToStream(bitMatrix, format, outputStream);
    }

    /**
     * 创建二维码图片,格式为png,
     * <pre>
     *      OutputStream outputStream = new FileOutPutStream("./二维码.png");
     *      createQrCode(200, 200, "hello xxxx", qrcode);
     * </pre>
     *
     * @param width        二维码宽度
     * @param height       二维码高度
     * @param content      二维码内容
     * @param outputStream 二维码输出结果
     * @throws Exception 生成失败异常
     */
    public static void createQrCode(int width, int height, String content, OutputStream outputStream) throws Exception {
        String format = "png";
        Map<EncodeHintType, Object> hints = new HashMap<>(6);
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.MARGIN, 2);
        BitMatrix bitMatrix = new MultiFormatWriter()
                .encode(content, BarcodeFormat.QR_CODE, width, height, hints);

        MatrixToImageWriter.writeToStream(bitMatrix, format, outputStream);
    }

    /**
     * 合并图片
     * <pre>
     * try (InputStream inputStream = new FileInputStream("./模板.png");
     * InputStream logo = new FileInputStream("./logo.jpg");
     * ByteArrayOutputStream qrcode = new ByteArrayOutputStream(); //二维码
     * OutputStream outputStream = new FileOutputStream("./合成结果.jpg")) {
     *
     * createQrCode(200, 200, "hello xxxx", qrcode); //生成二维码
     * InputStream qrcodeStream = new ByteArrayInputStream(qrcode.toByteArray());
     *
     * ImageLayer logoLayer = ImageLayer.builder()
     * .imageInput(logo)
     * .x(50).y(50)
     * .width(128).height(128)
     * .build();
     *
     * ImageLayer qrCodeLayer = ImageLayer.builder()
     * .imageInput(qrcodeStream)
     * .x(120).y(50)
     * .width(200).height(200)
     * .build();
     * //合并
     * merge(inputStream, outputStream, Arrays.asList(logoLayer, qrCodeLayer));
     * }
     * </pre>
     *
     * @param backgroundImageImage 背景图片
     * @param outputStream         合成结果输出
     * @param layers               图层集合
     * @throws IOException 合成失败异常
     */
    public static void merge(InputStream backgroundImageImage, OutputStream outputStream, List<Layer> layers) throws IOException {
        BufferedImage image = ImageIO.read(backgroundImageImage);
        Graphics graphics = image.getGraphics();
        for (Layer layer : layers) {
            layer.draw(graphics);
        }
        ImageIO.write(image, "jpg", outputStream);
    }

}
