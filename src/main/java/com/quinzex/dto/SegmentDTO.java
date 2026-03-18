package com.quinzex.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SegmentDTO {
    private Long id;
    private String name;
    private String slug;
    private Integer rank;
    private Integer depth;

    private List<TopicDTO> topics;
}