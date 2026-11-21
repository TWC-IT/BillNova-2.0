package com.thewizecompany.billnova.service;


import com.thewizecompany.billnova.dto.CreateProjectRequest;
import com.thewizecompany.billnova.dto.ProjectResponse;
import com.thewizecompany.billnova.dto.UpdateProjectRequest;
import com.thewizecompany.billnova.entity.ClientContact;
import com.thewizecompany.billnova.entity.Clients;
import com.thewizecompany.billnova.entity.Project;
import com.thewizecompany.billnova.enums.ProjectStatus;
import com.thewizecompany.billnova.enums.SequenceType;
import com.thewizecompany.billnova.exception.ResourceNotFoundException;
import com.thewizecompany.billnova.repository.ChangeOrderRepository;
import com.thewizecompany.billnova.repository.ClientContactRepository;
import com.thewizecompany.billnova.repository.ClientRepository;
import com.thewizecompany.billnova.repository.ProjectRepository;
import com.thewizecompany.billnova.responses.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Year;
import java.util.Locale;
import java.util.UUID;



@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final ChangeOrderRepository changeOrderRepository;
    private final ClientRepository clientRepository;
    private final ClientContactRepository clientContactRepository;
    private final SequenceService sequenceService;

    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request) {
        Clients client = clientRepository.findByIdAndIsDeletedFalse(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Client not found with id: ", request.getClientId().toString()));

        ClientContact contact = clientContactRepository.findByIdAndIsDeletedFalse(request.getClientContactId())
                .orElseThrow(() -> new ResourceNotFoundException("Client Contact not found with id: ",
                        request.getClientContactId().toString()));

        if (!contact.getClientId().equals(client.getId())) {
            throw new IllegalArgumentException("Client Contact Id and Client Id doesn't match");
        }
        String projectCode = generateCode();
        Project project = Project.builder()
                .code(projectCode)
                .name(request.getName().trim().toLowerCase(Locale.ROOT))
                .status(request.isSaveAsDraft() ? ProjectStatus.DRAFT:ProjectStatus.ONGOING)
                .location(request.getLocation() != null ? request.getLocation().trim().toLowerCase() : null)
                .contractAmount(request.getContractAmount())
                .clientId(request.getClientId())
                .clientContactId(request.getClientContactId())
                .build();
        projectRepository.save(project);

        return ProjectResponse.builder()
                .code(projectCode)
                .name(project.getName())
                .status(project.getStatus())
                .location(project.getLocation())
                .contractAmount(project.getContractAmount())
                .invoicedAmount(project.getInvoicedAmount())
                .totalAmount(project.getTotalAmount())
                .amountReceived(project.getAmountReceived())
                .client(client.getName())
                .clientContact(contact.getName())
                .clientContactEmail(contact.getEmail())
                .build();
    }

    @Transactional
    public void deleteProject(UUID projectId) {
        Project project = projectRepository.findByIdAndIsDeletedFalse(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: ", projectId.toString()));

        if(!project.getStatus().equals(ProjectStatus.DRAFT)) {
            throw new IllegalArgumentException("Project status must be DRAFT");
        }
        project.markDeleted();
        projectRepository.save(project);
    }

    @Transactional
    public ProjectResponse updateProject(UUID projectId, UpdateProjectRequest request) {
        Project project = projectRepository.findByIdAndIsDeletedFalse(projectId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Project not found with project id: ", projectId.toString()));

        if (request.getClientContactId() != null) {
            ClientContact contact = clientContactRepository.findByIdAndIsDeletedFalse(request.getClientContactId())
                    .orElseThrow(() -> new ResourceNotFoundException("Client Contact not found with id: ", request.getClientContactId().toString()));
            if(!project.getClientId().equals(contact.getClientId())) {
                throw new IllegalArgumentException("Client Contact Id and Client Id doesn't match");
            }
            project.setClientContactId(request.getClientContactId());
        }
        if (request.getLocation() != null) {
            project.setLocation(request.getLocation());
        }
        if (request.getContractAmount() != null) {
            if (request.getContractAmount().compareTo(project.getInvoicedAmount()) < 0) {
                throw new IllegalArgumentException("Amount already invoiced for the project. Increase the contract amount value");
            }
            project.setContractAmount(request.getContractAmount());
        }
        if (request.getName() != null) {
            project.setName(request.getName().trim().toLowerCase(Locale.ROOT));
        }
        projectRepository.save(project);

        Clients client = clientRepository.findByIdAndIsDeletedFalse(project.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: ", project.getClientId().toString()));
        ClientContact contact = clientContactRepository.findByIdAndIsDeletedFalse(project.getClientContactId())
                .orElseThrow(() -> new ResourceNotFoundException("Client Contact not found with id: ", client.getId().toString()));

        return ProjectResponse.builder()
                .code(project.getCode())
                .name(project.getName())
                .status(project.getStatus())
                .location(project.getLocation())
                .client(client.getName())
                .clientContact(contact.getName())
                .clientContactEmail(contact.getEmail())
                .contractAmount(project.getContractAmount())
                .invoicedAmount(project.getInvoicedAmount())
                .totalAmount(project.getTotalAmount())
                .amountReceived(project.getAmountReceived())
                .build();
    }

    public PageResponse<ProjectResponse> getAll(Pageable pageable) {
        Page<Project> page = projectRepository.findAllByIsDeletedFalse(pageable);
        return PageResponse.from(
                page.map(project -> ProjectResponse.builder()
                        .code(project.getCode())
                        .name(project.getName())
                        .status(project.getStatus())
                        .location(project.getLocation())
                        .contractAmount(project.getContractAmount())
                        .invoicedAmount(project.getInvoicedAmount())
                        .totalAmount(project.getTotalAmount())
                        .amountReceived(project.getAmountReceived())
                        .build())
        );
    }

    public PageResponse<ProjectResponse> getAllByClientId(Pageable pageable, UUID clientId) {
        Clients client = clientRepository.findByIdAndIsDeletedFalse(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: ", clientId.toString()));
        Page<Project> page = projectRepository.findAllByClientIdAndIsDeletedFalse(pageable, clientId);

        return PageResponse.from(
                page.map(project -> ProjectResponse.builder()
                        .code(project.getCode())
                        .name(project.getName())
                        .status(project.getStatus())
                        .location(project.getLocation())
                        .contractAmount(project.getContractAmount())
                        .invoicedAmount(project.getInvoicedAmount())
                        .totalAmount(project.getTotalAmount())
                        .amountReceived(project.getAmountReceived())
                        .client(client.getName())
                        .clientContact(
                                clientContactRepository.findByIdAndIsDeletedFalse(project.getClientContactId())
                                        .orElseThrow(() -> new ResourceNotFoundException("Client Contact with id: ", project.getClientContactId().toString()))
                                        .getName()
                        )
                        .build())
        );
    }

    @Transactional
    public void markLost(UUID projectId){
        Project project = projectRepository.findByIdAndIsDeletedFalse(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: ", projectId.toString()));
        if(project.getStatus().equals(ProjectStatus.LOST)) {
            throw new IllegalStateException("Project is already lost");
        }
        project.setStatus(ProjectStatus.LOST);
        projectRepository.save(project);
    }



    private String generateCode() {
        int year = Year.now().getValue();
        Integer sequence = sequenceService.nextSequence(
                SequenceType.PROJECT, year
        );
        return String.format("J-TWC-%d-%03d", year, sequence);
    }
}
