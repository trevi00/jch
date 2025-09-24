package org.jbd.backend.ai.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.jbd.backend.common.entity.BaseEntity;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "interview_questions")
public class InterviewQuestion extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "interview_id", nullable = false)
    private Interview interview;

    @Column(name = "temp_question_id")
    private String questionId; // 프론트엔드에서 전달하는 임시 ID
    
    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;
    
    @Column(name = "question_type")
    private String questionType; // "technical", "behavioral", "situational"
    
    @Column(name = "answer", columnDefinition = "TEXT")
    private String answer;
    
    @Column(name = "score")
    private BigDecimal score;
    
    @Column(name = "strengths", columnDefinition = "JSON")
    private String strengths; // JSON 배열로 저장
    
    @Column(name = "improvements", columnDefinition = "JSON")
    private String improvements; // JSON 배열로 저장
    
    @Column(name = "suggestion", columnDefinition = "TEXT")
    private String suggestion;
    
    @Column(name = "question_order")
    private Integer questionOrder;
    
    public InterviewQuestion() {}
    
    public InterviewQuestion(Interview interview, String questionId, String questionText, String questionType) {
        this.interview = interview;
        this.questionId = questionId;
        this.questionText = questionText;
        this.questionType = questionType;
    }
    
    public void setFeedback(BigDecimal score, List<String> strengths, List<String> improvements, String suggestion) {
        this.score = score;
        this.strengths = convertListToJson(strengths);
        this.improvements = convertListToJson(improvements);
        this.suggestion = suggestion;
    }
    
    private String convertListToJson(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            sb.append("\"").append(list.get(i).replace("\"", "\\\"")).append("\"");
            if (i < list.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }
    
    // Getters
    public Long getId() {
        return id;
    }
    
    public Interview getInterview() {
        return interview;
    }
    
    public String getQuestionId() {
        return questionId;
    }
    
    public String getQuestionText() {
        return questionText;
    }
    
    public String getQuestionType() {
        return questionType;
    }
    
    public String getAnswer() {
        return answer;
    }
    
    public BigDecimal getScore() {
        return score;
    }
    
    public String getStrengths() {
        return strengths;
    }
    
    public String getImprovements() {
        return improvements;
    }
    
    public String getSuggestion() {
        return suggestion;
    }
    
    public Integer getQuestionOrder() {
        return questionOrder;
    }
    
    // Setters
    public void setId(Long id) {
        this.id = id;
    }
    
    public void setInterview(Interview interview) {
        this.interview = interview;
    }
    
    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }
    
    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }
    
    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }
    
    public void setAnswer(String answer) {
        this.answer = answer;
    }
    
    public void setScore(BigDecimal score) {
        this.score = score;
    }
    
    public void setStrengths(String strengths) {
        this.strengths = strengths;
    }
    
    public void setImprovements(String improvements) {
        this.improvements = improvements;
    }
    
    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }
    
    public void setQuestionOrder(Integer questionOrder) {
        this.questionOrder = questionOrder;
    }
}