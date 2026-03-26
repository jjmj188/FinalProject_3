package com.spring.app.ai.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class AiSellTextResponse {

    private String titleSuggestion;
    private String description;
    private List<String> cautions = new ArrayList<>();
}