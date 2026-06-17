package net.orderzone.idcard.service;

import lombok.RequiredArgsConstructor;
import net.orderzone.idcard.repository.ProfileRepository;
import org.springframework.stereotype.Service;

import java.time.Year;

@Service
@RequiredArgsConstructor
public class RegistrationNumberGenerator {

    private final ProfileRepository profileRepository;

    public String generate(String department) {

        long count =
                profileRepository
                        .findByDepartment(department)
                        .size() + 1;

        return String.format(
                "%d-%s-%03d",
                Year.now().getValue(),
                department.toUpperCase(),
                count
        );
    }
}