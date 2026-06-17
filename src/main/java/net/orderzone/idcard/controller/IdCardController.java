package net.orderzone.idcard.controller;

import lombok.RequiredArgsConstructor;
import net.orderzone.idcard.model.Profile;
import net.orderzone.idcard.service.IdCardPdfService;
import net.orderzone.idcard.service.ProfileService;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/idcard")
@RequiredArgsConstructor
public class IdCardController {

    private final ProfileService profileService;
    private final IdCardPdfService pdfService;

    /** Live HTML preview using Thymeleaf. */
    @GetMapping("/{id}/preview")
    public String preview(@PathVariable Long id, Model model) {
        Profile profile = profileService.findById(id);
        model.addAttribute("profile", profile);

        // Build QR code as base64 for inline display
        try {
            byte[] qrBytes = pdfService.generateQrCode(
                    "https://idcard.orderzone.net/verify/" + profile.getUuid(), 200, 200);
            model.addAttribute("qrBase64",
                    "data:image/png;base64," + java.util.Base64.getEncoder().encodeToString(qrBytes));
        } catch (Exception ignored) {}

        // Build barcode as base64
        try {
            byte[] bcBytes = pdfService.generateBarcode(
                    profile.getRegistrationNumber(),
                    profile.getBarcodeType() != null ? profile.getBarcodeType() : net.orderzone.idcard.model.BarcodeType.CODE_128,
                    300, 80);
            model.addAttribute("barcodeBase64",
                    "data:image/png;base64," + java.util.Base64.getEncoder().encodeToString(bcBytes));
        } catch (Exception ignored) {}

        return "card-preview";
    }

    /** Download PDF of a single ID card. */
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) throws Exception {
        Profile profile = profileService.findById(id);
        byte[] pdfBytes = pdfService.generatePdf(profile);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(profile.getRegistrationNumber() + ".pdf").build());

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    /** Download a QR code image for a profile. */
    @GetMapping("/{id}/qr")
    public ResponseEntity<byte[]> getQrCode(@PathVariable Long id) throws Exception {
        Profile profile = profileService.findById(id);
        byte[] qrBytes = pdfService.generateQrCode(
                "https://idcard.orderzone.net/verify/" + profile.getUuid(), 300, 300);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);

        return new ResponseEntity<>(qrBytes, headers, HttpStatus.OK);
    }

    /** Batch generate PDFs as a ZIP file. */
    @PostMapping("/batch")
    public ResponseEntity<byte[]> batchGenerate(@RequestBody List<Long> profileIds) throws Exception {
        List<Profile> profiles = profileIds.stream()
                .map(profileService::findById)
                .toList();

        byte[] zipBytes = pdfService.generateBatchPdf(profiles);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("id-cards-batch.zip").build());

        return new ResponseEntity<>(zipBytes, headers, HttpStatus.OK);
    }
}
