package net.orderzone.idcard.controller;

import lombok.RequiredArgsConstructor;
import net.orderzone.idcard.model.Profile;
import net.orderzone.idcard.model.ProfileType;
import net.orderzone.idcard.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public List<Profile> getAll() {
        return profileService.findAll();
    }

    @GetMapping("/{id}")
    public Profile getById(@PathVariable Long id) {
        return profileService.findById(id);
    }

    @GetMapping("/search")
    public List<Profile> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) ProfileType type) {
        return profileService.search(name, department, type);
    }

    @PostMapping
    public Profile create(
            @RequestPart("profile") Profile profile,
            @RequestPart(value = "photo", required = false) MultipartFile photo) throws Exception {
        return profileService.create(profile, photo);
    }

    @PutMapping("/{id}")
    public Profile update(
            @PathVariable Long id,
            @RequestPart("profile") Profile profile,
            @RequestPart(value = "photo", required = false) MultipartFile photo) throws Exception {
        return profileService.update(id, profile, photo);
    }

    @PostMapping("/{id}/photo")
    public Profile uploadPhoto(
            @PathVariable Long id,
            @RequestParam("photo") MultipartFile photo) throws Exception {
        return profileService.uploadPhoto(id, photo);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        profileService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
