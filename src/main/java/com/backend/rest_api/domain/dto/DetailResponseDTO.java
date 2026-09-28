package com.backend.rest_api.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetailResponseDTO {
    private PostResponseDTO post;
    private UserResponseDTO user;
    private List<CommentResponseDTO> comments;
}
