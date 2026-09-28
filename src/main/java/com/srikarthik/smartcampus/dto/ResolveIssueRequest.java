package com.srikarthik.smartcampus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for resolving an issue with resolution notes.
 */
public class ResolveIssueRequest {

    @NotBlank(message = "Resolution notes are required when resolving an issue")
    @Size(min = 5, max = 2000, message = "Resolution notes must be between 5 and 2000 characters")
    private String resolutionNotes;

    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }
}
