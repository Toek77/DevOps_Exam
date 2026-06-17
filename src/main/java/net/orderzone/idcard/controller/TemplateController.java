package net.orderzone.idcard.controller;

import lombok.RequiredArgsConstructor;
import net.orderzone.idcard.model.Template;
import net.orderzone.idcard.service.TemplateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;

    @GetMapping
    public List<Template> getAll() {
        return templateService.findAll();
    }

    @GetMapping("/{id}")
    public Template getById(@PathVariable Long id) {
        return templateService.findById(id);
    }

    @PostMapping
    public Template create(@RequestBody Template template) {
        return templateService.create(template);
    }

    @PutMapping("/{id}")
    public Template update(@PathVariable Long id, @RequestBody Template template) {
        return templateService.update(id, template);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        templateService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
