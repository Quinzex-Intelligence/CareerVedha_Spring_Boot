package com.quinzex.dto;

import lombok.Data;

@Data
public class CreateQuestion {

    private String question;
    private String option1;
    private String option2;
    private String option3;
    private String option4;
    private String correctAnswer;
    private Long chapterId;
    private String category;
}