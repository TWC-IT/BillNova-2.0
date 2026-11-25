package com.thewizecompany.billnova.service;



import com.thewizecompany.billnova.dto.ChangeOrderResponse;
import com.thewizecompany.billnova.dto.CreateChangeOrderRequest;
import com.thewizecompany.billnova.dto.UpdateChangeOrderRequest;
import com.thewizecompany.billnova.entity.ChangeOrder;
import com.thewizecompany.billnova.entity.Project;
import com.thewizecompany.billnova.enums.ChangeOrderStatus;
import com.thewizecompany.billnova.enums.ProjectStatus;
import com.thewizecompany.billnova.enums.SequenceType;
import com.thewizecompany.billnova.exception.ResourceNotFoundException;
import com.thewizecompany.billnova.repository.ChangeOrderRepository;
import com.thewizecompany.billnova.repository.ProjectRepository;
import com.thewizecompany.billnova.responses.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChangeOrderService {
    private final ChangeOrderRepository changeOrderRepository;
    private final ProjectRepository projectRepository;
    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public ChangeOrderResponse create(CreateChangeOrderRequest request) {
        Project project = projectRepository.findByIdAndIsDeletedFalse(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: ", request.getProjectId().toString()));

        if (project.getStatus().equals(ProjectStatus.LOST)) {
            throw new IllegalArgumentException("Project is already lost");
        }
        ChangeOrder changeOrder = ChangeOrder.builder()
                .code(generateCode(project.getCode(), project.getId()))
                .status(ChangeOrderStatus.DRAFT)
                .amount(request.getAmount())
                .projectId(request.getProjectId())
                .metaData(request.getDescription().trim().toLowerCase(Locale.ROOT))
                .build();
        changeOrderRepository.save(changeOrder);
        project.setTotalAmount(project.getTotalAmount().add(changeOrder.getAmount()));
        projectRepository.save(project);

        return ChangeOrderResponse.builder()
                .id(changeOrder.getId())
                .code(changeOrder.getCode())
                .description(changeOrder.getMetaData())
                .amount(changeOrder.getAmount())
                .invoicedAmount(changeOrder.getInvoicedAmount())
                .receivedAmount(changeOrder.getReceivedAmount())
                .build();
    }

    @Transactional
    public void delete(UUID id) {
        ChangeOrder cor = changeOrderRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Change Order not found with id: ", id.toString()));
        if (cor.getStatus().equals(ChangeOrderStatus.ISSUED) || cor.getStatus().equals(ChangeOrderStatus.PAID)) {
            throw new IllegalArgumentException("Change Order is already issued");
        }
        cor.markDeleted();
        changeOrderRepository.delete(cor);
    }

    @Transactional
    public ChangeOrderResponse update(UUID id, UpdateChangeOrderRequest request){
        ChangeOrder cor = changeOrderRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Change Order not found with id: ", id.toString()));
        if (cor.getStatus().equals(ChangeOrderStatus.ISSUED) || cor.getStatus().equals(ChangeOrderStatus.PAID)) {
            throw new IllegalArgumentException("Change Order is already issued");
        }
        if (request.getAmount() != null) {
            cor.setAmount(request.getAmount());
        }
        if (!request.getDescription().isBlank()) {
            cor.setMetaData(request.getDescription().trim().toLowerCase(Locale.ROOT));
        }
        changeOrderRepository.save(cor);
        return ChangeOrderResponse.builder()
                .id(cor.getId())
                .code(cor.getCode())
                .status(cor.getStatus())
                .description(cor.getMetaData())
                .amount(cor.getAmount())
                .invoicedAmount(cor.getInvoicedAmount())
                .receivedAmount(cor.getReceivedAmount())
                .build();
    }

    public PageResponse<ChangeOrderResponse> getAllByProjectId(Pageable pageable,  UUID projectId) {
            projectRepository.findByIdAndIsDeletedFalse(projectId)
                    .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: ", projectId.toString()));
            Page<ChangeOrder> page = changeOrderRepository.findAllByProjectIdAndIsDeletedFalse(projectId,  pageable);

            return PageResponse.from(
                    page.map(changeOrder -> ChangeOrderResponse.builder()
                            .id(changeOrder.getId())
                            .code(changeOrder.getCode())
                            .status(changeOrder.getStatus())
                            .description(changeOrder.getMetaData())
                            .amount(changeOrder.getAmount())
                            .invoicedAmount(changeOrder.getInvoicedAmount())
                            .receivedAmount(changeOrder.getReceivedAmount())
                            .build())
            );
    }

    @Transactional
    private String generateCode(String projectCode, UUID projectId) {

        String query = """
                INSERT INTO change_order_sequences (project_id, last_sequence)
                VALUES (?, 1)
                ON CONFLICT (project_id)
                DO UPDATE SET last_sequence = change_order_sequences.last_sequence + 1
                RETURNING last_sequence;
                """;

        Integer nextSequence = jdbcTemplate.queryForObject(query, Integer.class, projectId);

        return String.format("%s-COR-%03d",projectCode, nextSequence);
    }
}
