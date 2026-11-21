package com.thewizecompany.billnova.service;


import com.thewizecompany.billnova.dto.CreateTemplateRequest;
import com.thewizecompany.billnova.dto.TemplateResponse;
import com.thewizecompany.billnova.dto.UpdateTemplateRequest;
import com.thewizecompany.billnova.entity.Template;
import com.thewizecompany.billnova.exception.DuplicateResourceException;
import com.thewizecompany.billnova.exception.ResourceNotFoundException;
import com.thewizecompany.billnova.repository.TemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateService {
    private final TemplateRepository templateRepository;

    @Transactional
    public TemplateResponse create(CreateTemplateRequest request) {
        if (templateRepository.existsByNameAndIsDeletedFalseAndTemplateNameIgnoreCase(request.getTemplateName())){
            throw new DuplicateResourceException("Template already exists");
        }
        Template template = Template.builder()
                .name(request.getTemplateName().trim().toLowerCase(Locale.ROOT))
                .content(request.getContent())
                .isDefault(request.isSetAsDefault())
                .build();
        templateRepository.save(template);

        return TemplateResponse.builder()
                .id(template.getId())
                .name(template.getName())
                .isDefault(template.isDefault())
                .content(template.getContent())
                .build();
    }

    @Transactional
    public TemplateResponse update(UUID id, UpdateTemplateRequest request) {
        Template template = templateRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found with id: ",  id.toString()));

        if(!request.getTemplateName().isBlank()){
            if (templateRepository.existsByNameAndIsDeletedFalseAndTemplateNameIgnoreCase(request.getTemplateName())){
                throw new DuplicateResourceException("Template already exists");
            }
            template.setName(request.getTemplateName().trim().toLowerCase(Locale.ROOT));
        }
        if(!request.getContent().isBlank()){
            template.setContent(request.getContent().trim().toLowerCase(Locale.ROOT));
        }
        if (request.isDefault()){
            templateRepository.demoteCurrentDefault();
            template.setDefault(true);
        }
        templateRepository.save(template);
        return TemplateResponse.builder()
                .id(template.getId())
                .name(template.getName())
                .isDefault(template.isDefault())
                .content(template.getContent())
                .build();
    }

    @Transactional
    public void delete(UUID id) {
        Template template = templateRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found with id: ",  id.toString()));

        template.markDeleted();
        templateRepository.save(template);
    }

    @Transactional(readOnly = true)
    public List<TemplateResponse> getAll() {
        List<Template> templates = templateRepository.findAllByIsDeletedFalse();

        return templates.stream().map(
                        template -> TemplateResponse.builder()
                                .id(template.getId())
                                .name(template.getName())
                                .isDefault(template.isDefault())
                                .build()
        ).toList();
    }
}
