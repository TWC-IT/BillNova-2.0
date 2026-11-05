package com.thewizecompany.billnova.service;


import com.thewizecompany.billnova.dto.ClientContactResponse;
import com.thewizecompany.billnova.dto.ClientResponse;
import com.thewizecompany.billnova.dto.CreateClientRequest;
import com.thewizecompany.billnova.dto.UpdateClientRequest;
import com.thewizecompany.billnova.entity.ClientContact;
import com.thewizecompany.billnova.entity.Clients;
import com.thewizecompany.billnova.exception.DuplicateResourceException;
import com.thewizecompany.billnova.exception.ResourceNotFoundException;
import com.thewizecompany.billnova.repository.ClientContactRepository;
import com.thewizecompany.billnova.repository.ClientRepository;
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
public class ClientService {

    private ClientRepository clientRepository;
    private ClientContactRepository clientContactRepository;

    @Transactional
    public ClientResponse createClient(CreateClientRequest request){
        if(clientRepository.existsByEmail(request.getEmail())){
            throw new DuplicateResourceException("Client with this Email already exists");
        }
        Clients client = Clients.builder()
                .name(request.getName().toLowerCase(Locale.ROOT).trim())
                .email(request.getEmail().toLowerCase(Locale.ROOT).trim())
                .billingAddress(request.getBillingAddress())
                .build();
        clientRepository.save(client);

        return ClientResponse.builder()
                .id(client.getId())
                .name(client.getName())
                .email(client.getEmail())
                .billingAddress(client.getBillingAddress())
                .build();
    }

    @Transactional
    public ClientResponse updateClient(UpdateClientRequest request, UUID clientId){
        Clients client = clientRepository.findByIdAndIsDeletedFalse(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client with Client ID", clientId.toString()));

        if(request.getName() != null){
            client.setName(request.getName().toLowerCase(Locale.ROOT).trim());
        }
        if (request.getEmail() != null){
            client.setEmail(request.getEmail().toLowerCase(Locale.ROOT).trim());
        }
        if(request.getBillingAddress() != null){
            client.setBillingAddress(request.getBillingAddress().toLowerCase(Locale.ROOT).trim());
        }
        clientRepository.save(client);
        return ClientResponse.builder()
                .id(client.getId())
                .name(client.getName())
                .email(client.getEmail())
                .billingAddress(client.getBillingAddress())
                .build();
    }

    @Transactional
    public void deleteClient(UUID clientId){
        Clients client = clientRepository.findByIdAndIsDeletedFalse(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client with Client ID", clientId.toString()));

        client.markDeleted();
        clientRepository.save(client);
    }

    @Transactional(readOnly = true)
    public PageResponse<ClientResponse> getAll(Pageable pageable){
        Page<Clients> page = clientRepository.findAll(pageable);
        return PageResponse.from(
                page.map(client -> ClientResponse.builder()
                        .id(client.getId())
                        .name(client.getName())
                        .email(client.getEmail())
                        .billingAddress(client.getBillingAddress())
                        .build())
        );
    }

    @Transactional(readOnly = true)
    public PageResponse<ClientContactResponse> getAllContacts(Pageable pageable, UUID clientId){
        if(clientRepository.findByIdAndIsDeletedFalse(clientId).isEmpty()){
            throw new ResourceNotFoundException("Client not found with ID: ", clientId.toString());
        }

        Page<ClientContact> page = clientContactRepository.findByIsDeletedFalseAndClientId(clientId, pageable);
        return PageResponse.from(
                page.map(clientContact -> ClientContactResponse.builder()
                        .id(clientContact.getId())
                        .name(clientContact.getName())
                        .email(clientContact.getEmail())
                        .build())
        );
    }
}

