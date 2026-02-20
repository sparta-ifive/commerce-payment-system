package com.spartaifive.commercepayment.domain.webhookevent;

import com.spartaifive.commercepayment.common.exception.ErrorCode;
import com.spartaifive.commercepayment.common.exception.ServiceErrorException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;

@Component
public class PortOneWebhookVerifier {

    @Value("${portone.webhook.secret}")
    private String secret;

    @Value("${portone.webhook.secret-format:base64}")
    private String secretFormat; // base64 | raw

    private byte[] signingKey; // HMAC에 사용할 "진짜 키 바이트"

    // (선택) 허용 시간 오차(초) – 리플레이 공격 방지
    private static final long ALLOWED_TIMESTAMP_SKEW_SECONDS = 300; // 5분

    @PostConstruct
    void init() {
        this.signingKey = decodeSigningKey(secret, secretFormat);
    }

    /**
     * PortOne Webhook V2 시그니처 검증
     */
    public boolean verify(
            byte[] rawBody,
            String webhookId,
            String webhookTimestamp,
            String webhookSignature
    ) {
        // 1) 필수 값 누락 방어
        if (rawBody == null ||
                webhookId == null ||
                webhookTimestamp == null ||
                webhookSignature == null ||
                signingKey == null) {
            return false;
        }

        webhookId = webhookId.trim();
        webhookTimestamp = webhookTimestamp.trim();
        webhookSignature = webhookSignature.trim();

        // 2) 시그니처 포맷 확인
        if (!webhookSignature.startsWith("v1,")) {
            return false;
        }

        // 3) (선택) timestamp 리플레이 방지
        if (!isTimestampValid(webhookTimestamp)) {
            return false;
        }

        // 4) 서명 대상 데이터 생성: "{id}.{timestamp}.{payload}"
        byte[] toSign = buildToSign(webhookId, webhookTimestamp, rawBody);

        // 5) 서버에서 HMAC 계산 (digest bytes)
        byte[] computedMac = hmacSha256(toSign, signingKey);

        // 6) 헤더의 Base64 서명 디코딩 (digest bytes)
        byte[] givenMac;
        try {
            String givenBase64 = webhookSignature.substring(3).trim(); // "v1," 제거
            givenMac = Base64.getDecoder().decode(givenBase64);
        } catch (IllegalArgumentException e) {
            return false;
        }

        // 7) 상수 시간 비교
        return MessageDigest.isEqual(computedMac, givenMac);
    }

    private static byte[] buildToSign(String webhookId, String timestamp, byte[] body) {
        byte[] prefix = (webhookId + "." + timestamp + ".").getBytes(StandardCharsets.UTF_8);

        byte[] result = new byte[prefix.length + body.length];
        System.arraycopy(prefix, 0, result, 0, prefix.length);
        System.arraycopy(body, 0, result, prefix.length, body.length);
        return result;
    }

    private static byte[] hmacSha256(byte[] data, byte[] keyBytes) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(keyBytes, "HmacSHA256"));
            return mac.doFinal(data);
        } catch (Exception e) {
            throw new ServiceErrorException(ErrorCode.ERR_WEBHOOK_SIGNATURE_INVALID);
        }
    }

    private static boolean isTimestampValid(String webhookTimestamp) {
        try {
            long ts = Long.parseLong(webhookTimestamp);
            long now = Instant.now().getEpochSecond();
            return Math.abs(now - ts) <= ALLOWED_TIMESTAMP_SKEW_SECONDS;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static byte[] decodeSigningKey(String secret, String format) {
        if (secret == null) return null;

        String s = secret.trim();

        // raw 모드면 그대로 UTF-8 bytes
        if ("raw".equalsIgnoreCase(format)) {
            return s.getBytes(StandardCharsets.UTF_8);
        }

        // base64 모드 (기본)
        // "whsec_" prefix
        if (s.startsWith("whsec_")) {
            s = s.substring("whsec_".length());
        }

        return Base64.getDecoder().decode(s);
    }
}
