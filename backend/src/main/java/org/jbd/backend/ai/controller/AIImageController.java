package org.jbd.backend.ai.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jbd.backend.ai.dto.ImageGenerationDto;
import org.jbd.backend.ai.client.AIServiceClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai/image")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AIImageController {

    private final AIServiceClient aiServiceClient;

    @PostMapping("/generate")
    public ResponseEntity<ImageGenerationDto.GenerateResponse> generateImage(
            @Valid @RequestBody GenerateImageRequest request) {
        
        ImageGenerationDto.GenerateResponse response = aiServiceClient.generateImage(
            request.getPrompt(),
            request.getStyle(),
            request.getSize(),
            request.getN()
        );
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/generate-profile")
    public ResponseEntity<ImageGenerationDto.GenerateResponse> generateProfileImage(
            @Valid @RequestBody GenerateProfileImageRequest request) {
        
        // 프로필 이미지 전용 프롬프트 생성
        String enhancedPrompt = createProfilePrompt(request.getDescription(), request.getStyle());
        
        ImageGenerationDto.GenerateResponse response = aiServiceClient.generateImage(
            enhancedPrompt,
            request.getStyle(),
            request.getSize() != null ? request.getSize() : "512x512",
            1
        );
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/generate-with-sentiment")
    public ResponseEntity<ImageGenerationDto.GenerateResponse> generateImageWithSentiment(
            @Valid @RequestBody GenerateWithSentimentRequest request) {
        
        // FastAPI의 감정 분석 기반 이미지 생성 호출
        // 현재는 간단히 텍스트를 프롬프트로 변환
        String prompt = createSentimentBasedPrompt(request.getText());
        
        ImageGenerationDto.GenerateResponse response = aiServiceClient.generateImage(
            prompt,
            request.getStyle(),
            request.getSize() != null ? request.getSize() : "512x512",
            1
        );
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/styles")
    public ResponseEntity<StylesResponse> getAvailableStyles() {
        StylesResponse response = new StylesResponse();
        response.addStyle("professional", "전문적이고 비즈니스용 스타일");
        response.addStyle("casual", "편안하고 자연스러운 스타일");
        response.addStyle("creative", "창의적이고 예술적인 스타일");
        response.addStyle("modern", "현대적이고 세련된 스타일");
        response.addStyle("minimal", "미니멀하고 깔끔한 스타일");
        
        return ResponseEntity.ok(response);
    }

    /**
     * 프로필 이미지용 프롬프트 생성
     */
    private String createProfilePrompt(String description, String style) {
        String basePrompt = "professional headshot portrait";
        
        if (description != null && !description.trim().isEmpty()) {
            basePrompt += ", " + description.trim();
        }
        
        return switch (style != null ? style.toLowerCase() : "professional") {
            case "professional" -> basePrompt + ", business attire, clean background, high quality";
            case "casual" -> basePrompt + ", casual clothing, natural lighting, friendly expression";
            case "creative" -> basePrompt + ", artistic style, creative background, unique composition";
            case "modern" -> basePrompt + ", contemporary style, sleek appearance, modern setting";
            case "minimal" -> basePrompt + ", minimalist style, simple background, clean composition";
            default -> basePrompt + ", professional style";
        };
    }

    /**
     * 감정 기반 프롬프트 생성
     */
    private String createSentimentBasedPrompt(String text) {
        // 간단한 감정 분석 기반 프롬프트 생성
        // 실제로는 더 정교한 감정 분석이 필요
        String lowerText = text.toLowerCase();
        
        if (lowerText.contains("행복") || lowerText.contains("기쁨") || lowerText.contains("성공")) {
            return "bright, cheerful, positive atmosphere, warm colors, success celebration";
        } else if (lowerText.contains("도전") || lowerText.contains("성장") || lowerText.contains("발전")) {
            return "dynamic, energetic, growth concept, upward movement, bright future";
        } else if (lowerText.contains("팀워크") || lowerText.contains("협업") || lowerText.contains("함께")) {
            return "teamwork, collaboration, people working together, unity, partnership";
        } else {
            return "professional, clean, modern, business concept";
        }
    }

    // Request DTOs
    public static class GenerateImageRequest {
        private String prompt;
        private String style = "professional";
        private String size = "512x512";
        private int n = 1;

        public GenerateImageRequest() {}

        public String getPrompt() {
            return prompt;
        }

        public void setPrompt(String prompt) {
            this.prompt = prompt;
        }

        public String getStyle() {
            return style;
        }

        public void setStyle(String style) {
            this.style = style;
        }

        public String getSize() {
            return size;
        }

        public void setSize(String size) {
            this.size = size;
        }

        public int getN() {
            return n;
        }

        public void setN(int n) {
            this.n = n;
        }
    }

    public static class GenerateProfileImageRequest {
        private String description;
        private String style = "professional";
        private String size = "512x512";

        public GenerateProfileImageRequest() {}

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getStyle() {
            return style;
        }

        public void setStyle(String style) {
            this.style = style;
        }

        public String getSize() {
            return size;
        }

        public void setSize(String size) {
            this.size = size;
        }
    }

    public static class GenerateWithSentimentRequest {
        private String text;
        private String style = "professional";
        private String size = "512x512";

        public GenerateWithSentimentRequest() {}

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getStyle() {
            return style;
        }

        public void setStyle(String style) {
            this.style = style;
        }

        public String getSize() {
            return size;
        }

        public void setSize(String size) {
            this.size = size;
        }
    }

    // Response DTOs
    public static class StylesResponse {
        private java.util.List<StyleInfo> styles = new java.util.ArrayList<>();

        public void addStyle(String name, String description) {
            styles.add(new StyleInfo(name, description));
        }

        public java.util.List<StyleInfo> getStyles() {
            return styles;
        }

        public static class StyleInfo {
            private String name;
            private String description;

            public StyleInfo(String name, String description) {
                this.name = name;
                this.description = description;
            }

            public String getName() {
                return name;
            }

            public String getDescription() {
                return description;
            }
        }
    }
}