package net.orderzone.idcard.config;

import lombok.RequiredArgsConstructor;
import net.orderzone.idcard.model.Template;
import net.orderzone.idcard.repository.TemplateRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@RequiredArgsConstructor
@Profile("!test")
public class DataLoader {

    private final TemplateRepository templateRepository;

    @Bean
    public Runnable initData() {

        return () -> {

            if (templateRepository.count() == 0) {

                Template template = new Template();

                template.setCode("DEFAULT");
                template.setName("Default Template");

                templateRepository.save(template);
            }
        };
    }
}