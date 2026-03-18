package com.spring.app.ai.dto;

import java.util.List;

public class AiSellTextResponse {

    private String titleSuggestion;
    private String description;
    private List<String> cautions;

    public String getTitleSuggestion() {
        return titleSuggestion;
    }

    public void setTitleSuggestion(String titleSuggestion) {
        this.titleSuggestion = titleSuggestion;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getCautions() {
        return cautions;
    }

    public void setCautions(List<String> cautions) {
        this.cautions = cautions;
    }
}