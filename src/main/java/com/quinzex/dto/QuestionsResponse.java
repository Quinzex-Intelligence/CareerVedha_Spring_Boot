package com.quinzex.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class QuestionsResponse {
    private Long id;
    private String question;
    private String opt1;
    private String opt2;
    private String opt3;
    private String opt4;
    private String category;
    private Long chapterId;
    private String correctOption;
}