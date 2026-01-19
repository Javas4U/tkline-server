package com.bytelab.tkline.server.service.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * QR码生成服务
 *
 * @author Tkline Team
 * @date 2026-01-09
 */
@Slf4j
@Service
public class QRCodeService {

    /**
     * 生成QR码并返回Base64编码的图片
     *
     * @param content 要编码的内容（如订阅链接）
     * @param width   图片宽度（像素）
     * @param height  图片高度（像素）
     * @return Base64编码的PNG图片数据URI
     */
    public String generateQRCodeDataUri(String content, int width, int height) {
        try {
            log.debug("生成QR码: content={}, size={}x{}", content, width, height);

            // 配置QR码参数
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.MARGIN, 1); // 边距

            // 生成QR码矩阵
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints);

            // 转换为PNG图片
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            byte[] imageBytes = outputStream.toByteArray();

            // 转换为Base64 Data URI
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            String dataUri = "data:image/png;base64," + base64Image;

            log.debug("QR码生成成功，大小: {} bytes", imageBytes.length);
            return dataUri;

        } catch (WriterException | IOException e) {
            log.error("生成QR码失败: content={}", content, e);
            throw new RuntimeException("生成QR码失败: " + e.getMessage(), e);
        }
    }

    /**
     * 生成默认尺寸的QR码（300x300）
     *
     * @param content 要编码的内容
     * @return Base64编码的PNG图片数据URI
     */
    public String generateQRCodeDataUri(String content) {
        return generateQRCodeDataUri(content, 300, 300);
    }
}
