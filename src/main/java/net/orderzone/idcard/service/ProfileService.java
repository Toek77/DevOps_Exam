package net.orderzone.idcard.service;

import lombok.RequiredArgsConstructor;
import net.orderzone.idcard.exception.ResourceNotFoundException;
import net.orderzone.idcard.model.Profile;
import net.orderzone.idcard.model.ProfileType;
import net.orderzone.idcard.repository.ProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final PhotoStorageService photoStorageService;
    private final RegistrationNumberGenerator generator;

    public Profile create(Profile profile, MultipartFile photo) throws Exception {
        profile.setUuid(UUID.randomUUID().toString());
        profile.setRegistrationNumber(generator.generate(profile.getDepartment()));

        if (photo != null && !photo.isEmpty()) {
            String fileName = photoStorageService.store(photo);
            profile.setPhotoFileName(fileName);
            profile.setPhotoContentType(photo.getContentType());
        }

        return profileRepository.save(profile);
    }

    public Profile update(Long id, Profile updated, MultipartFile photo) throws Exception {
        Profile existing = findById(id);

        existing.setType(updated.getType());
        existing.setFullName(updated.getFullName());
        existing.setDepartment(updated.getDepartment());
        existing.setTitle(updated.getTitle());
        existing.setEmail(updated.getEmail());
        existing.setPhone(updated.getPhone());
        existing.setBloodGroup(updated.getBloodGroup());
        existing.setDateOfBirth(updated.getDateOfBirth());
        existing.setExpiryDate(updated.getExpiryDate());
        existing.setBarcodeType(updated.getBarcodeType());

        if (updated.getTemplate() != null) {
            existing.setTemplate(updated.getTemplate());
        }

        if (photo != null && !photo.isEmpty()) {
            // Delete old photo
            if (existing.hasPhoto()) {
                photoStorageService.delete(existing.getPhotoFileName());
            }
            String fileName = photoStorageService.store(photo);
            existing.setPhotoFileName(fileName);
            existing.setPhotoContentType(photo.getContentType());
        }

        return profileRepository.save(existing);
    }

    public Profile uploadPhoto(Long id, MultipartFile photo) throws Exception {
        Profile existing = findById(id);

        if (existing.hasPhoto()) {
            photoStorageService.delete(existing.getPhotoFileName());
        }

        String fileName = photoStorageService.store(photo);
        existing.setPhotoFileName(fileName);
        existing.setPhotoContentType(photo.getContentType());

        return profileRepository.save(existing);
    }

    public List<Profile> findAll() {
        return profileRepository.findAll();
    }

    public Profile findById(Long id) {
        return profileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found with id: " + id));
    }

    public Profile findByUuid(String uuid) {
        return profileRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found with uuid: " + uuid));
    }

    public List<Profile> search(String name, String department, ProfileType type) {
        if (name != null && !name.isBlank()) {
            return profileRepository.findByFullNameContainingIgnoreCase(name);
        }
        if (department != null && !department.isBlank() && type != null) {
            return profileRepository.findByDepartmentAndType(department, type);
        }
        if (department != null && !department.isBlank()) {
            return profileRepository.findByDepartment(department);
        }
        if (type != null) {
            return profileRepository.findByType(type);
        }
        return profileRepository.findAll();
    }

    public void delete(Long id) {
        if (!profileRepository.existsById(id)) {
            throw new ResourceNotFoundException("Profile not found with id: " + id);
        }
        profileRepository.deleteById(id);
    }
}
