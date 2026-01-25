// src/main/java/socialMediaApp/services/MailService.java
package socialMediaApp.services;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import socialMediaApp.models.Ticket;

import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;
    private String buildTicketPayload(Ticket t) {
        return "TICKET:" + t.getPost().getId() + ":" + t.getCode().toUpperCase();
    }
    public void sendTicketEmail(Ticket t) {
        String payload = buildTicketPayload(t);   // <-- TICKET:<postId>:<code>
        byte[] qr  = generateQrPng(payload);
        byte[] pdf = generateTicketPdf(t, qr);
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


            helper.addInline(cid, new ByteArrayDataSource(qr, "image/png"));


            String fileName = "ticket-" + t.getCode() + ".pdf";
            helper.addAttachment(fileName, new ByteArrayDataSource(pdf, "application/pdf"));

            mailSender.send(msg);
        } catch (Exception e) {
            throw new IllegalStateException("Email send failed", e);
        }
    }

    private byte[] generateTicketPdf(Ticket t, byte[] qrPng) {
        try (PDDocument doc = new PDDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            PDPage page = new PDPage(PDRectangle.A6);
            doc.addPage(page);

            float w = page.getMediaBox().getWidth();
            float h = page.getMediaBox().getHeight();
            float margin = 18f;


            float footerReserve = 26f;
            float qrSize = 140f;
            float qrX = (w - qrSize) / 2f;
            float qrY = margin + footerReserve;

            PDImageXObject qrImg = PDImageXObject.createFromByteArray(doc, qrPng, "qr.png");

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {

                cs.setLineWidth(1.2f);
                cs.addRect(margin / 2, margin / 2, w - margin, h - margin);
                cs.stroke();


                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 16);
                cs.newLineAtOffset(margin, h - margin - 14);
                cs.showText("TICKET");
                cs.endText();


                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
                cs.newLineAtOffset(margin, h - margin - 40);
                cs.showText(safe(t.getPost().getTitle()));
                cs.endText();


                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA, 11);
                cs.newLineAtOffset(margin, h - margin - 62);
                cs.showText("Code: " + safe(t.getCode()));
                cs.endText();

                String owner = (safe(t.getUser().getName()) + " " + safe(t.getUser().getLastName())).trim();
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA, 11);
                cs.newLineAtOffset(margin, h - margin - 80);
                cs.showText("Owner: " + owner);
                cs.endText();


                if (t.getCreatedAt() != null) {
                    String ts = t.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                    cs.beginText();
                    cs.setFont(PDType1Font.HELVETICA, 10);
                    cs.newLineAtOffset(margin, h - margin - 98);
                    cs.showText("Issued: " + ts);
                    cs.endText();
                }


                cs.drawImage(qrImg, qrX, qrY, qrSize, qrSize);

                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_OBLIQUE, 8.5f);
                cs.newLineAtOffset(margin, margin + 10);
                cs.showText("Present this ticket at entry. If scanning fails, type the code manually.");
                cs.endText();
            }

            doc.save(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("PDF generation failed", e);
        }
    }

    public void sendAccountVerificationEmail(String toEmail, String link) {
        String html = """
        <h2>Activate your account</h2>
        <p>Click the link below to activate your account:</p>
        <p><a href="%s">Activate account</a></p>
        <p>If you didn't register, ignore this email.</p>
    """.formatted(link);

        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    msg,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            helper.setTo(toEmail);
            helper.setSubject("Activate your account");
            helper.setText(html, true);

            mailSender.send(msg);
        } catch (Exception e) {
            throw new IllegalStateException("Verification email send failed", e);
        }
    }



    private byte[] generateQrPng(String text) {
        try {
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.MARGIN, 1);

            BitMatrix matrix = new MultiFormatWriter()
                    .encode(text, BarcodeFormat.QR_CODE, 280, 280, hints);

            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                MatrixToImageWriter.writeToStream(matrix, "PNG", out);
                return out.toByteArray();
            }
        } catch (Exception e) {
            throw new IllegalStateException("QR generation failed", e);
        }
    }

    private String safe(String s) { return s == null ? "" : s; }
}
