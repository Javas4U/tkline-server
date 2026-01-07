package com.bytelab.tkline.server.service.modules.impl;

import com.bytelab.tkline.server.dto.user.SendCodeRequest;
import com.bytelab.tkline.server.service.modules.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 邮箱服务实现类
 * 使用内存Map存储验证码（适用于单机部署）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${spring.mail.properties.mail.smtp.replyto:bytelab@126.com}")
    private String replyTo;
    
    /**
     * 验证码有效期（分钟）
     */
    private static final long CODE_EXPIRE_MINUTES = 5;
    
    /**
     * 验证码长度
     */
    private static final int CODE_LENGTH = 6;
    
    /**
     * 验证码存储Map
     * Key: email:purpose, Value: CodeInfo
     */
    private final Map<String, CodeInfo> codeMap = new ConcurrentHashMap<>();
    
    /**
     * 发送限制Map
     * Key: email:purpose, Value: lastSendTime
     */
    private final Map<String, Long> sendLimitMap = new ConcurrentHashMap<>();
    
    /**
     * 重置密码Token存储Map
     * Key: email, Value: TokenInfo
     */
    private final Map<String, TokenInfo> resetTokenMap = new ConcurrentHashMap<>();
    
    /**
     * 每分钟最多发送次数（发送间隔：60秒）
     */
    private static final long SEND_INTERVAL_SECONDS = 60;

    @Override
    public String sendVerificationCode(String email, SendCodeRequest.CodePurpose purpose) {
        String key = buildKey(email, purpose);
        
        // 检查发送频率限制
        Long lastSendTime = sendLimitMap.get(key);
        if (lastSendTime != null) {
            long timePassed = (System.currentTimeMillis() - lastSendTime) / 1000;
            if (timePassed < SEND_INTERVAL_SECONDS) {
                long waitSeconds = SEND_INTERVAL_SECONDS - timePassed;
                throw new RuntimeException("发送过于频繁，请" + waitSeconds + "秒后再试");
            }
        }
        
        // 生成6位随机验证码
        String code = generateCode();
        
        try {
            // 发送邮件（根据用途定制内容）
            sendEmail(email, code, purpose);
            
            // 存储验证码到内存Map（5分钟有效期）
            long expireTime = System.currentTimeMillis() + CODE_EXPIRE_MINUTES * 60 * 1000;
            codeMap.put(key, new CodeInfo(code, expireTime));
            
            // 记录发送时间（用于频率限制）
            sendLimitMap.put(key, System.currentTimeMillis());
            
            log.info("验证码发送成功，邮箱：{}, 用途：{}, 过期时间：{}", 
                    email, purpose, new java.util.Date(expireTime));
            return code;
        } catch (Exception e) {
            log.error("验证码发送失败，邮箱：{}, 用途：{}", email, purpose, e);
            throw new RuntimeException("验证码发送失败：" + e.getMessage());
        }
    }

    @Override
    public boolean verifyCode(String email, String code, SendCodeRequest.CodePurpose purpose) {
        if (email == null || code == null) {
            return false;
        }
        
        String key = buildKey(email, purpose);
        CodeInfo codeInfo = codeMap.get(key);
        
        if (codeInfo == null) {
            log.warn("验证码不存在，邮箱：{}, 用途：{}", email, purpose);
            return false;
        }
        
        // 检查是否过期
        if (System.currentTimeMillis() > codeInfo.expireTime()) {
            log.warn("验证码已过期，邮箱：{}, 用途：{}", email, purpose);
            codeMap.remove(key);
            return false;
        }
        
        boolean isValid = codeInfo.code().equals(code);
        log.info("验证码验证{}，邮箱：{}, 用途：{}", 
                isValid ? "成功" : "失败", email, purpose);
        
        return isValid;
    }

    @Override
    public void clearCode(String email, SendCodeRequest.CodePurpose purpose) {
        String key = buildKey(email, purpose);
        codeMap.remove(key);
        log.info("验证码已清除，邮箱：{}, 用途：{}", email, purpose);
    }
    
    /**
     * 构建存储Key（email + purpose）
     */
    private String buildKey(String email, SendCodeRequest.CodePurpose purpose) {
        return email + ":" + purpose.name();
    }
    
    /**
     * 定时清理过期的验证码和发送限制记录
     * 每10分钟执行一次
     */
    @Scheduled(fixedRate = 10 * 60 * 1000)
    public void cleanExpiredCodes() {
        long now = System.currentTimeMillis();
        
        // 清理过期的验证码
        int removedCodes = 0;
        for (Map.Entry<String, CodeInfo> entry : codeMap.entrySet()) {
            if (now > entry.getValue().expireTime()) {
                codeMap.remove(entry.getKey());
                removedCodes++;
            }
        }
        
        // 清理过期的发送限制记录（超过5分钟的）
        int removedLimits = 0;
        for (Map.Entry<String, Long> entry : sendLimitMap.entrySet()) {
            if (now - entry.getValue() > 5 * 60 * 1000) {
                sendLimitMap.remove(entry.getKey());
                removedLimits++;
            }
        }
        
        if (removedCodes > 0 || removedLimits > 0) {
            log.info("定时清理完成，清理过期验证码：{} 个，清理发送限制：{} 个", removedCodes, removedLimits);
        }
    }
    
    /**
     * 生成随机验证码
     */
    private String generateCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
    
    /**
     * 发送邮件（根据用途定制内容）
     */
    private void sendEmail(String to, String code, SendCodeRequest.CodePurpose purpose) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setReplyTo(replyTo);
        
        // 根据用途设置邮件主题和内容
        String subject = getEmailSubject(purpose);
        String htmlContent = buildEmailContent(code, purpose);
        
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        
        mailSender.send(message);
    }
    
    /**
     * 获取邮件主题（根据用途）
     */
    private String getEmailSubject(SendCodeRequest.CodePurpose purpose) {
        return switch (purpose) {
            case LOGIN -> "【Apex隧道】邮箱登录验证码";
            case RESET_PASSWORD -> "【Apex隧道】重置密码验证码";
            default -> "【Apex隧道】邮箱验证码";
        };
    }
    
    /**
     * 构建邮件HTML内容（根据用途定制）
     */
    private String buildEmailContent(String code, SendCodeRequest.CodePurpose purpose) {
        String title = getContentTitle(purpose);
        String description = getContentDescription(purpose);
        
        return buildHtmlTemplate(code, title, description);
    }
    
    /**
     * 获取邮件标题（根据用途）
     */
    private String getContentTitle(SendCodeRequest.CodePurpose purpose) {
        return switch (purpose) {
            case LOGIN -> "邮箱验证码登录";
            case RESET_PASSWORD -> "重置密码";
            default -> "邮箱验证";
        };
    }
    
    /**
     * 获取邮件描述（根据用途）
     */
    private String getContentDescription(SendCodeRequest.CodePurpose purpose) {
        return switch (purpose) {
            case LOGIN -> "您正在使用邮箱验证码登录 <strong>Apex隧道服务</strong>。";
            case RESET_PASSWORD -> "您正在重置 <strong>Apex隧道服务</strong> 的登录密码。";
            default -> "您正在进行邮箱验证操作。";
        };
    }
    
    /**
     * 构建HTML邮件模板
     */
    private String buildHtmlTemplate(String code, String title, String description) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset=\"UTF-8\">" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "<style>" +
                "body { font-family: Arial, sans-serif; background-color: #f5f5f5; margin: 0; padding: 20px; }" +
                ".container { max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }" +
                ".header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; border-radius: 8px 8px 0 0; }" +
                ".header h1 { margin: 0; font-size: 24px; }" +
                ".content { padding: 40px 30px; }" +
                ".code-box { background-color: #f8f9fa; border: 2px dashed #667eea; border-radius: 8px; padding: 20px; text-align: center; margin: 20px 0; transition: all 0.3s; }" +
                ".code-box:hover { background-color: #e8ecf7; border-color: #5568d3; transform: scale(1.02); }" +
                ".code-box:active { transform: scale(0.98); }" +
                ".code { font-size: 32px; font-weight: bold; color: #667eea; letter-spacing: 8px; margin: 10px 0; user-select: all; -webkit-user-select: all; -moz-user-select: all; -ms-user-select: all; }" +
                ".tips { color: #666; font-size: 14px; line-height: 1.6; margin-top: 20px; }" +
                ".footer { background-color: #f8f9fa; padding: 20px; text-align: center; color: #999; font-size: 12px; border-radius: 0 0 8px 8px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class=\"container\">" +
                "<div class=\"header\">" +
                "<h1>🔐 Apex隧道 - " + title + "</h1>" +
                "</div>" +
                "<div class=\"content\">" +
                "<p>您好！</p>" +
                "<p>" + description + "</p>" +
                "<div class=\"code-box\">" +
                "<div style=\"font-size: 14px; color: #666; margin-bottom: 10px;\">您的验证码是：</div>" +
                "<div class=\"code\" id=\"verificationCode\">" + code + "</div>" +
                "<div style=\"font-size: 12px; color: #999; margin-top: 10px;\">💡 点击或长按验证码即可选中复制</div>" +
                "</div>" +
                "<div class=\"tips\">" +
                "<p><strong>⏰ 有效期：</strong>5分钟</p>" +
                "<p><strong>🔒 安全提示：</strong></p>" +
                "<ul style=\"margin: 10px 0; padding-left: 20px;\">" +
                "<li>请勿将验证码告知他人</li>" +
                "<li>如非本人操作，请忽略此邮件</li>" +
                "<li>验证码仅用于本次登录，5分钟后自动失效</li>" +
                "</ul>" +
                "</div>" +
                "</div>" +
                "<div class=\"footer\">" +
                "<p>此邮件由系统自动发送，请勿回复</p>" +
                "<p>© 2024 Apex隧道服务 | <a href=\"https://apextunnel.com\" style=\"color: #667eea;\">apextunnel.com</a></p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
    
    @Override
    public String sendResetPasswordLink(String email, String username, String resetUrl) {
        String limitKey = "reset_link:" + email;
        
        // 检查发送频率限制（与验证码共用限制机制）
        Long lastSendTime = sendLimitMap.get(limitKey);
        if (lastSendTime != null) {
            long timePassed = (System.currentTimeMillis() - lastSendTime) / 1000;
            if (timePassed < SEND_INTERVAL_SECONDS) {
                long waitSeconds = SEND_INTERVAL_SECONDS - timePassed;
                throw new com.bytelab.tkline.server.exception.BusinessException(
                        "发送过于频繁，请" + waitSeconds + "秒后再试");
            }
        }
        
        // 生成重置Token（32位随机字符串）
        String token = generateResetToken();
        
        // 存储Token（5分钟有效期）
        long expireTime = System.currentTimeMillis() + CODE_EXPIRE_MINUTES * 60 * 1000;
        resetTokenMap.put(email, new TokenInfo(token, username, expireTime));
        
        try {
            // 构建重置链接
            String resetLink = resetUrl + "?username=" + java.net.URLEncoder.encode(username, StandardCharsets.UTF_8) +
                              "&email=" + java.net.URLEncoder.encode(email, StandardCharsets.UTF_8) +
                              "&token=" + token;
            
            // 发送邮件
            sendResetLinkEmail(email, username, resetLink);
            
            // 记录发送时间（用于频率限制）
            sendLimitMap.put(limitKey, System.currentTimeMillis());
            
            log.info("重置密码链接发送成功，邮箱：{}, username: {}", email, username);
            return token;
        } catch (Exception e) {
            log.error("重置密码链接发送失败，邮箱：{}", email, e);
            throw new com.bytelab.tkline.server.exception.BusinessException("重置密码链接发送失败：" + e.getMessage());
        }
    }
    
    @Override
    public boolean verifyResetToken(String email, String token) {
        if (email == null || token == null) {
            return false;
        }
        
        TokenInfo tokenInfo = resetTokenMap.get(email);
        
        if (tokenInfo == null) {
            log.warn("重置Token不存在，邮箱：{}", email);
            return false;
        }
        
        // 检查是否过期
        if (System.currentTimeMillis() > tokenInfo.expireTime()) {
            log.warn("重置Token已过期，邮箱：{}", email);
            resetTokenMap.remove(email);
            return false;
        }
        
        boolean isValid = tokenInfo.token().equals(token);
        log.info("重置Token验证{}，邮箱：{}", isValid ? "成功" : "失败", email);
        
        return isValid;
    }
    
    @Override
    public EmailService.ResetTokenInfo verifyResetTokenWithDetails(
            String username, String email, String token) {
        if (username == null || email == null || token == null) {
            log.warn("验证失败：参数不能为空");
            return null;
        }
        
        TokenInfo tokenInfo = resetTokenMap.get(email);
        
        if (tokenInfo == null) {
            log.warn("重置Token不存在，邮箱：{}", email);
            return null;
        }
        
        // 检查是否过期
        if (System.currentTimeMillis() > tokenInfo.expireTime()) {
            log.warn("重置Token已过期，邮箱：{}", email);
            resetTokenMap.remove(email);
            return null;
        }
        
        // 验证token是否匹配
        if (!tokenInfo.token().equals(token)) {
            log.warn("Token不匹配，邮箱：{}", email);
            return null;
        }
        
        // 验证username是否匹配
        if (!tokenInfo.username().equals(username)) {
            log.warn("用户名不匹配，邮箱：{}, 期望：{}, 实际：{}", 
                    email, tokenInfo.username(), username);
            return null;
        }
        
        log.info("重置Token验证成功，用户名：{}, 邮箱：{}", username, email);
        
        return new EmailService.ResetTokenInfo(
                tokenInfo.username(),
                email,
                tokenInfo.expireTime()
        );
    }
    
    @Override
    public void clearResetToken(String email) {
        resetTokenMap.remove(email);
        log.info("重置Token已清除，邮箱：{}", email);
    }
    
    /**
     * 生成重置Token（32位随机字符串）
     */
    private String generateResetToken() {
        return java.util.UUID.randomUUID().toString().replace("-", "");
    }
    
    /**
     * 发送重置密码链接邮件
     */
    private void sendResetLinkEmail(String to, String username, String resetLink) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setReplyTo(replyTo);
        helper.setSubject("【Apex隧道】重置密码链接");
        
        // HTML邮件内容
        String htmlContent = buildResetLinkEmailContent(username, resetLink);
        helper.setText(htmlContent, true);
        
        mailSender.send(message);
    }
    
    /**
     * 构建重置密码链接邮件内容
     */
    private String buildResetLinkEmailContent(String username, String resetLink) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset=\"UTF-8\">" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "<style>" +
                "body { font-family: Arial, sans-serif; background-color: #f5f5f5; margin: 0; padding: 20px; }" +
                ".container { max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }" +
                ".header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; border-radius: 8px 8px 0 0; }" +
                ".header h1 { margin: 0; font-size: 24px; }" +
                ".content { padding: 40px 30px; }" +
                ".link-box { background-color: #f8f9fa; border: 2px solid #667eea; border-radius: 8px; padding: 20px; text-align: center; margin: 20px 0; }" +
                ".reset-button { display: inline-block; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 15px 40px; text-decoration: none; border-radius: 6px; font-size: 16px; font-weight: bold; margin: 10px 0; }" +
                ".reset-button:hover { opacity: 0.9; }" +
                ".warning { background-color: #fff3cd; border: 1px solid #ffc107; border-radius: 4px; padding: 15px; margin: 20px 0; color: #856404; }" +
                ".tips { color: #666; font-size: 14px; line-height: 1.6; margin-top: 20px; }" +
                ".footer { background-color: #f8f9fa; padding: 20px; text-align: center; color: #999; font-size: 12px; border-radius: 0 0 8px 8px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class=\"container\">" +
                "<div class=\"header\">" +
                "<h1>🔑 Apex隧道 - 重置密码</h1>" +
                "</div>" +
                "<div class=\"content\">" +
                "<p>您好，<strong>" + username + "</strong>！</p>" +
                "<p>我们收到了您的重置密码请求。</p>" +
                "<div class=\"link-box\">" +
                "<p style=\"margin: 10px 0; color: #666;\">点击下方按钮重置您的密码：</p>" +
                "<a href=\"" + resetLink + "\" class=\"reset-button\">立即重置密码</a>" +
                "</div>" +
                "<div class=\"warning\">" +
                "<p style=\"margin: 5px 0;\"><strong>⚠️ 重要提示：</strong></p>" +
                "<ul style=\"margin: 10px 0; padding-left: 20px;\">" +
                "<li>此链接仅在<strong>5分钟内</strong>有效</li>" +
                "<li>链接仅可使用一次，使用后自动失效</li>" +
                "<li>如非本人操作，请忽略此邮件并确保账户安全</li>" +
                "</ul>" +
                "</div>" +
                "<div class=\"tips\">" +
                "<p><strong>🔒 安全建议：</strong></p>" +
                "<ul style=\"margin: 10px 0; padding-left: 20px;\">" +
                "<li>请不要将此链接转发给他人</li>" +
                "<li>如有疑问，请联系客服</li>" +
                "<li>定期修改密码，保护账户安全</li>" +
                "</ul>" +
                "<p style=\"margin-top: 20px; font-size: 12px; color: #999;\">如果按钮无法点击，请复制以下链接到浏览器：</p>" +
                "<p style=\"word-break: break-all; background-color: #f8f9fa; padding: 10px; border-radius: 4px; font-size: 12px;\">" + resetLink + "</p>" +
                "</div>" +
                "</div>" +
                "<div class=\"footer\">" +
                "<p>此邮件由系统自动发送，请勿回复</p>" +
                "<p>© 2024 Apex隧道服务 | <a href=\"https://apextunnel.com\" style=\"color: #667eea;\">apextunnel.com</a></p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    /**
     * 验证码信息内部类
     */
    private record CodeInfo(String code, long expireTime) {

    }

    /**
     * 重置Token信息内部类
     */
    private record TokenInfo(String token, String username, long expireTime) {

    }
}
