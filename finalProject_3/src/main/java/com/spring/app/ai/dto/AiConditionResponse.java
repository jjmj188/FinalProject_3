package com.spring.app.ai.dto;

import java.util.List;

public class AiConditionResponse {
    private String grade;
    private Double confidence;
    private String summary;
    private List<String> reasons;
    private List<String> visibleIssues;

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }

    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public List<String> getReasons() { return reasons; }
    public void setReasons(List<String> reasons) { this.reasons = reasons; }

    public List<String> getVisibleIssues() { return visibleIssues; }
    public void setVisibleIssues(List<String> visibleIssues) { this.visibleIssues = visibleIssues; }
}