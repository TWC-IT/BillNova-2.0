package com.thewizecompany.billnova.controller;

import com.thewizecompany.billnova.dto.ClientContactResponse;
import com.thewizecompany.billnova.dto.ClientResponse;
import com.thewizecompany.billnova.dto.CreateClientRequest;
import com.thewizecompany.billnova.dto.UpdateClientRequest;
import com.thewizecompany.billnova.responses.ApiResponse;
import com.thewizecompany.billnova.responses.PageResponse;
import com.thewizecompany.billnova.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v2/clients")
@RequiredArgsConstructor
public class ClientController {
    private ClientService clientService;

    @PostMapping
    public ResponseEntity<ApiResponse<ClientResponse>> createClient(
            @Valid @RequestBody CreateClientRequest request){
        ClientResponse response = clientService.createClient(request);

        return ResponseEntity.ok(
                ApiResponse.ok(response, "Client Created Successfully")
        );
    }

    @PatchMapping("/{id}/update")
    public ResponseEntity<ApiResponse<ClientResponse>> updateClient(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateClientRequest request
    ){
        ClientResponse response = clientService.updateClient(request, id);

        return ResponseEntity.ok(
                ApiResponse.ok(
                        response, "Client Update Successfully"
                )
        );
    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<ApiResponse<Void>> deleteClient(
            @PathVariable UUID id
    ){
        clientService.deleteClient(id);
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Client Deleted Successfully"
                )
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ClientResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ){
        Sort sort = sortDir.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() :
                Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(
                ApiResponse.ok(
                        clientService.getAll(pageable)
                )
        );
    }

    @GetMapping("/{id}/contacts")
    public ResponseEntity<ApiResponse<PageResponse<ClientContactResponse>>> getContacts(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ){
        Sort sort = sortDir.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() :
                Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(
                ApiResponse.ok(
                        clientService.getAllContacts(pageable, id)
                )
        );
    }
}
