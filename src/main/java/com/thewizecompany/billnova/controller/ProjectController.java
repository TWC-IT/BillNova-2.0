package com.thewizecompany.billnova.controller;


import com.thewizecompany.billnova.dto.CreateProjectRequest;
import com.thewizecompany.billnova.dto.ProjectResponse;
import com.thewizecompany.billnova.dto.UpdateProjectRequest;
import com.thewizecompany.billnova.responses.ApiResponse;
import com.thewizecompany.billnova.responses.PageResponse;
import com.thewizecompany.billnova.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v2/projects")
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(
            @Valid @RequestBody CreateProjectRequest request
            ){
        ProjectResponse response = projectService.createProject(request);

        return ResponseEntity.ok(
                ApiResponse.ok(
                        response, "Project Created"
                )
        );
    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<ApiResponse<Void>> deleteProject(@PathVariable UUID id){
        projectService.deleteProject(id);
        return ResponseEntity.ok(
                ApiResponse.ok("Project Deleted")
        );
    }

    @PatchMapping("/{id}/update")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(
            @Valid @RequestBody UpdateProjectRequest request,
            @PathVariable UUID id
    ){
        ProjectResponse response = projectService.updateProject(id, request);
        return ResponseEntity.ok(
                ApiResponse.ok(
                        response, "Project Updated"
                ));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProjectResponse>>> getAllProjects(
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
                        projectService.getAll(pageable)
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PageResponse<ProjectResponse>>> getAllProjectsByClientId(
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
                        projectService.getAllByClientId(pageable, id)
                )
        );
    }

    @PatchMapping("/{id}/lost")
    public ResponseEntity<ApiResponse<Void>> markLost(
            @PathVariable UUID id
    ){
        projectService.markLost(id);

        return ResponseEntity.ok(
                ApiResponse.ok("Project Marked Lost")
        );
    }
}
