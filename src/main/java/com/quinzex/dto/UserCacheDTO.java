package com.quinzex.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserCacheDTO {
    private String email;
    private int tokenVersion;
    private int roleVersion;
}
