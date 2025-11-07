// src/main/java/socialMediaApp/services/MailService.java
package socialMediaApp.services;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import socialMediaApp.models.Ticket;

import javax.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    public void sendTicketEmail(Ticket t) {
        byte[] qr = generateQrPng(t.getCode());
        String cid = "qr-" + t.getCode();

        String html = """
        <h2>Ticket for %s</h2>
        <p><b>Code:</b> %s</p>
        <img src="cid:%s" alt="QR code" width="220" height="220"/>
        <p><i>If scanning fails, type the code manually.</i></p>
        """.formatted(t.getPost().getTitle(), t.getCode(), cid);

        try {
            MimeMessage msg = mailSender.createMimeMessage();


            MimeMessageHelper helper = new MimeMessageHelper(
                    msg,
                    MimeMessageHelper.MULTIPART_MODE_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            helper.setTo(t.getUser().getEmail());
            helper.setSubject("Your ticket – " + t.getPost().getTitle());
            helper.setText(html, true);


            javax.mail.util.ByteArrayDataSource ds =
                    new javax.mail.util.ByteArrayDataSource(qr, "image/png");
            helper.addInline(cid, ds);

            mailSender.send(msg);
        } catch (Exception e) {
            throw new IllegalStateException("Email send failed", e);
        }
    }




    private byte[] generateQrPng(String text) {
        try {
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.MARGIN, 1);

            BitMatrix matrix = new MultiFormatWriter()
                    .encode(text, BarcodeFormat.QR_CODE, 280, 280, hints);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("QR generation failed", e);
        }
    }
}
