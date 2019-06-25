package com.tskj.QRCode;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 创建二维码的工具类<br>
 * 要求：jdk1.7 或以上
 * @author LeonSu
 */
public class QRCodeUtil {

    /**
     * 默认二维码宽度
     */
    private static final int WIDTH = 300;

    /**
     * 默认二维码高度
     */
    private static final int HEIGHT = 300;

    /**
     * 默认二维码文件格式
     */
    private static final String format = "png";

    /**
     * 二维码参数
     */
    private static final Map<EncodeHintType, Object> hints = new HashMap();

    static {
        // 字符编码
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        // 容错等级 L、M、Q、H 其中 L 为最低, H 为最高
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        // 二维码与图片边距
        hints.put(EncodeHintType.MARGIN, 2);
    }

    /**
     * 返回一个 BufferedImage 对象<br>
     * 使用默认宽度（300）和高度（300）
     * @param content 二维码内容
     */
    public static BufferedImage toBufferedImage(String content) throws WriterException {
        return toBufferedImage(content, WIDTH, HEIGHT);
    }

    /**
     * 返回一个 BufferedImage 对象
     * @param content 二维码内容
     * @param width   宽
     * @param height  高
     */
    public static BufferedImage toBufferedImage(String content, int width, int height) throws WriterException {
        BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }


    /**
     * 将二维码图片输出到一个流中<br>
     * 使用默认宽度（300）和高度（300）
     * @param content 二维码内容
     * @param stream  输出流
     */
    public static void writeToStream(String content, OutputStream stream) throws WriterException, IOException {
        writeToStream(content, stream, WIDTH, HEIGHT);
    }

    /**
     * 将二维码图片输出到一个流中
     * @param content 二维码内容
     * @param stream  输出流
     * @param width   宽
     * @param height  高
     */
    public static void writeToStream(String content, OutputStream stream, int width, int height) throws WriterException, IOException {
        BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);
        MatrixToImageWriter.writeToStream(bitMatrix, format, stream);
    }

    /**
     * 生成二维码图片文件<br>
     * 使用默认宽度（300）和高度（300）
     * @param content 二维码内容
     * @param path    保存的文件名（带路径）
     */
    public static void createQRCode(String content, String path) throws WriterException, IOException {
        createQRCode(content, path, WIDTH, HEIGHT);
    }

    /**
     * 生成二维码图片文件
     * @param content 二维码内容
     * @param path    文件保存路径
     * @param width   宽
     * @param height  高
     */
    public static void createQRCode(String content, String path, int width, int height) throws WriterException, IOException {
        BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);
        //toPath() 方法由 jdk1.7 及以上提供
        MatrixToImageWriter.writeToPath(bitMatrix, format, new File(path).toPath());
    }
}
