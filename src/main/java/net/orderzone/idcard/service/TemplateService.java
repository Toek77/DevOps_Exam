package net.orderzone.idcard.service;

import lombok.RequiredArgsConstructor;
import net.orderzone.idcard.model.Template;
import net.orderzone.idcard.repository.TemplateRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TemplateService {

    private final TemplateRepository templateRepository;

    public List<Template> findAll() {
        return templateRepository.findAll();
    }

    public Template findById(Long id) {
        return templateRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Template not found"));
    }

    public Template create(Template template) {
        return templateRepository.save(template);
    }

    public Template update(Long id, Template template) {

        Template existing = findById(id);

        existing.setCode(template.getCode());
        existing.setName(template.getName());

        return templateRepository.save(existing);
    }

    public void delete(Long id) {
        templateRepository.deleteById(id);
    }
}