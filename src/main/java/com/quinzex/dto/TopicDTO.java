package com.quinzex.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TopicDTO {
    private Long id;
    private String name;
    private String slug;
}