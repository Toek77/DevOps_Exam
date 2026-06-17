package net.orderzone.idcard.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import com.google.zxing.oned.EAN13Writer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import net.orderzone.idcard.model.BarcodeType;
import net.orderzone.idcard.model.Profile;
import net.orderzone.idcard.model.Template;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
public class IdCardPdfService {

    @Value("${app.photo.upload-dir}")
    private String uploadDir;

    /**
     * Generate a PDF ID card for a single profile.
     */
    public byte[] generatePdf(Profile profile) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        Template tmpl = profile.getTemplate();
        String layout = (tmpl != null && tmpl.getLayout() != null)
                ? tmpl.getLayout().toUpperCase()
                : "VERTICAL";

        PageSize pageSize;
        if ("HORIZONTAL".equals(layout)) {
            pageSize = new PageSize(540, 324); // landscape card
        } else {
            pageSize = new PageSize(324, 540); // portrait card
        }
        pdfDoc.setDefaultPageSize(pageSize);
        document.setMargins(15, 15, 15, 15);

        // Parse template colors
        DeviceRgb primary   = parseHex(tmpl != null ? tmpl.getPrimaryColor()   : "#1d4ed8");
        DeviceRgb secondary = parseHex(tmpl != null ? tmpl.getSecondaryColor() : "#e0e7ff");
        DeviceRgb textColor = parseHex(tmpl != null ? tmpl.getTextColor()     : "#111827");

        // --- HEADER ---
        Paragraph orgName = new Paragraph(
                tmpl != null && tmpl.getOrganizationName() != null
                        ? tmpl.getOrganizationName()
                        : "ID Card")
                .setFontSize(14).setBold()
                .setFontColor(primary)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(orgName);

        if (tmpl != null && tmpl.getTagline() != null) {
            Paragraph tagline = new Paragraph(tmpl.getTagline())
                    .setFontSize(8).setItalic()
                    .setFontColor(textColor)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(tagline);
        }

        // --- PHOTO ---
        if (profile.hasPhoto()) {
            Path photoPath = Paths.get(uploadDir, profile.getPhotoFileName());
            if (Files.exists(photoPath)) {
                Image photo = new Image(ImageDataFactory.create(photoPath.toUri().toURL()))
                        .scaleToFit(80, 100)
                        .setHorizontalAlignment(HorizontalAlignment.CENTER);
                document.add(photo);
            }
        }

        document.add(new Paragraph("\n").setFontSize(4));

        // --- DETAILS TABLE ---
        Table table = new Table(UnitValue.createPercentArray(new float[]{35, 65}))
                .useAllAvailableWidth();

        addRow(table, "Name", profile.getFullName(), textColor);
        addRow(table, "ID", profile.getRegistrationNumber(), textColor);
        if (profile.getTitle() != null) {
            addRow(table, "Title", profile.getTitle(), textColor);
        }
        if (profile.getDepartment() != null) {
            addRow(table, "Dept", profile.getDepartment(), textColor);
        }
        if (profile.getEmail() != null) {
            addRow(table, "Email", profile.getEmail(), textColor);
        }
        if (profile.getPhone() != null) {
            addRow(table, "Phone", profile.getPhone(), textColor);
        }
        if (profile.getBloodGroup() != null) {
            addRow(table, "Blood", profile.getBloodGroup(), textColor);
        }
        if (profile.getIssueDate() != null) {
            addRow(table, "Issued", profile.getIssueDate().toString(), textColor);
        }
        if (profile.getExpiryDate() != null) {
            addRow(table, "Expires", profile.getExpiryDate().toString(), textColor);
        }
        document.add(table);

        document.add(new Paragraph("\n").setFontSize(4));

        // --- QR CODE ---
        String qrContent = "https://idcard.orderzone.net/verify/" + profile.getUuid();
        byte[] qrBytes = generateQrCode(qrContent, 120, 120);
        Image qrImage = new Image(ImageDataFactory.create(qrBytes))
                .scaleToFit(80, 80)
                .setHorizontalAlignment(HorizontalAlignment.CENTER);
        document.add(qrImage);

        // --- BARCODE ---
        byte[] barcodeBytes = generateBarcode(
                profile.getRegistrationNumber(),
                profile.getBarcodeType() != null ? profile.getBarcodeType() : BarcodeType.CODE_128,
                200, 50);
        Image barcodeImage = new Image(ImageDataFactory.create(barcodeBytes))
                .scaleToFit(180, 40)
                .setHorizontalAlignment(HorizontalAlignment.CENTER);
        document.add(barcodeImage);

        document.close();
        return baos.toByteArray();
    }

    /**
     * Generate a QR code PNG as byte[].
     */
    public byte[] generateQrCode(String content, int width, int height) throws Exception {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);

        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height, hints);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", baos);
        return baos.toByteArray();
    }

    /**
     * Generate a linear barcode PNG as byte[].
     */
    public byte[] generateBarcode(String data, BarcodeType type, int width, int height) throws Exception {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);

        BitMatrix matrix;
        if (type == BarcodeType.EAN_13) {
            // EAN-13 must be exactly 12 or 13 digits
            String eanData = data.replaceAll("[^0-9]", "");
            if (eanData.length() > 12) eanData = eanData.substring(0, 12);
            while (eanData.length() < 12) eanData = "0" + eanData;
            matrix = new EAN13Writer().encode(eanData, BarcodeFormat.EAN_13, width, height, hints);
        } else {
            matrix = new Code128Writer().encode(data, BarcodeFormat.CODE_128, width, height, hints);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", baos);
        return baos.toByteArray();
    }

    /**
     * Generate PDFs for multiple profiles and return them as a ZIP byte array.
     */
    public byte[] generateBatchPdf(List<Profile> profiles) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(baos)) {
            for (Profile profile : profiles) {
                byte[] pdfBytes = generatePdf(profile);
                java.util.zip.ZipEntry entry = new java.util.zip.ZipEntry(
                        profile.getRegistrationNumber() + ".pdf");
                zos.putNextEntry(entry);
                zos.write(pdfBytes);
                zos.closeEntry();
            }
        }
        return baos.toByteArray();
    }

    // --- helpers ---

    private void addRow(Table table, String label, String value, DeviceRgb textColor) {
        table.addCell(new Cell()
                .add(new Paragraph(label).setFontSize(7).setBold().setFontColor(textColor))
                .setBorder(Border.NO_BORDER));
        table.addCell(new Cell()
                .add(new Paragraph(value).setFontSize(8).setFontColor(textColor))
                .setBorder(Border.NO_BORDER));
    }

    private DeviceRgb parseHex(String hex) {
        if (hex == null || hex.length() < 7) return new DeviceRgb(17, 24, 39); // #111827
        String h = hex.startsWith("#") ? hex.substring(1) : hex;
        try {
            int r = Integer.parseInt(h.substring(0, 2), 16);
            int g = Integer.parseInt(h.substring(2, 4), 16);
            int b = Integer.parseInt(h.substring(4, 6), 16);
            return new DeviceRgb(r, g, b);
        } catch (Exception e) {
            return new DeviceRgb(17, 24, 39);
        }
    }
}
