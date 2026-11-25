package com.thewizecompany.billnova.service;


import com.thewizecompany.billnova.dto.ClientContactResponse;
import com.thewizecompany.billnova.dto.CreateClientContactRequest;
import com.thewizecompany.billnova.dto.UpdateClientContactRequest;
import com.thewizecompany.billnova.entity.ClientContact;
import com.thewizecompany.billnova.exception.DuplicateResourceException;
import com.thewizecompany.billnova.exception.ResourceNotFoundException;
import com.thewizecompany.billnova.repository.ClientContactRepository;
import com.thewizecompany.billnova.responses.PageResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientContactService {
    private ClientContactRepository clientContactRepository;

    @Transactional
    public ClientContactResponse create(CreateClientContactRequest request){
        if(clientContactRepository.existsByEmail(request.getEmail())){
            throw new DuplicateResourceException("Client Contact already exists");
        }
        ClientContact contact = ClientContact.builder()
                .name(request.getName().toLowerCase(Locale.ROOT).trim())
                .email(request.getEmail().toLowerCase(Locale.ROOT).trim())
                .clientId(request.getClientId())
                .build();

        clientContactRepository.save(contact);

        return ClientContactResponse.builder()
                .id(contact.getId())
                .name(contact.getName())
                .email(contact.getEmail())
                .build();
    }

    @Transactional
    public ClientContactResponse update(UpdateClientContactRequest request, UUID clientContactId){
        ClientContact contact = clientContactRepository.findByIdAndIsDeletedFalse(clientContactId)
                .orElseThrow(() -> new ResourceNotFoundException("Contact Not found with id: ", clientContactId.toString()));

        if (request.getEmail() != null && !request.getEmail().isBlank()){
            contact.setEmail(request.getEmail().toLowerCase(Locale.ROOT).trim());
        }
        if (request.getName() != null && !request.getName().isBlank()){
            contact.setName(request.getName().toLowerCase(Locale.ROOT).trim());
        }

        ClientContact savedContact = clientContactRepository.save(contact);
        return ClientContactResponse.builder()
                .name(savedContact.getName())
                .email(savedContact.getEmail())
                .id(savedContact.getId())
                .build();
    }

    @Transactional
    public void delete(UUID clientContactId){
        ClientContact contact = clientContactRepository.findByIdAndIsDeletedFalse(clientContactId)
                .orElseThrow(() -> new ResourceNotFoundException("Contact Not found with id: ", clientContactId.toString()));

        contact.markDeleted();
        clientContactRepository.save(contact);
    }

}
