package com.thewizecompany.billnova.controller;


import com.thewizecompany.billnova.dto.ClientContactResponse;
import com.thewizecompany.billnova.dto.CreateClientContactRequest;
import com.thewizecompany.billnova.dto.UpdateClientContactRequest;
import com.thewizecompany.billnova.responses.ApiResponse;
import com.thewizecompany.billnova.service.ClientContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v2/clientContact")
@RequiredArgsConstructor
public class ClientContactController {
    private ClientContactService clientContactService;

    @PostMapping
    public ResponseEntity<ApiResponse<ClientContactResponse>> create(@Valid @RequestBody CreateClientContactRequest request){
        ClientContactResponse response = clientContactService.create(request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Client Contact Created"));
    }

    @PatchMapping("/{id}/update")
    public ResponseEntity<ApiResponse<ClientContactResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateClientContactRequest request){

        ClientContactResponse response = clientContactService.update(request, id);

        return ResponseEntity.ok(
                ApiResponse.ok(
                        response, "Client Contact Updated Successfully"
                )
        );
    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id){
        clientContactService.delete(id);
        return ResponseEntity.ok(
                ApiResponse.ok("Client Contact Deleted")
        );
    }

}
