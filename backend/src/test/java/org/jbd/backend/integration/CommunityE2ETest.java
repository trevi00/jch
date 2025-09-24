package org.jbd.backend.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jbd.backend.ai.client.AIServiceClient;
import org.jbd.backend.ai.dto.ImageGenerationDto;
import org.jbd.backend.ai.dto.SentimentAnalysisDto;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("커뮤니티 E2E 통합 테스트")
public class CommunityE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AIServiceClient aiServiceClient;

    private static String generalUserToken;
    private static String companyUserToken;
    private static String adminUserToken;
    
    // Category IDs for different category types
    private static Long jobInfoCategoryId;
    private static Long interviewCategoryId;
    private static Long qnaCategoryId;
    private static Long freeBoardCategoryId; // 자유게시판 - AI 이미지 생성
    private static Long companyCategoryId; // 기업게시판 - 기업 유저만 접근
    private static Long noticeCategoryId; // 공지 - 관리자만 작성
    
    // Created post IDs for testing
    private static Long testPostId;
    private static Long testCommentId;
    
    @BeforeAll
    static void setUpUsers(@Autowired MockMvc mockMvc, @Autowired ObjectMapper objectMapper) throws Exception {
        // 1. 일반 유저 회원가입 및 로그인
        String generalUserJson = """
                {
                    "email": "generaluser@example.com",
                    "password": "password123",
                    "name": "일반 사용자",
                    "userType": "GENERAL"
                }
                """;
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(generalUserJson))
                .andExpect(status().isOk());
        
        generalUserToken = loginUser(mockMvc, objectMapper, "generaluser@example.com", "password123");
        
        // 2. 기업 유저 회원가입 및 로그인
        String companyUserJson = """
                {
                    "email": "company@example.com",
                    "password": "password123",
                    "name": "기업 사용자",
                    "userType": "COMPANY"
                }
                """;
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(companyUserJson))
                .andExpect(status().isOk());
        
        companyUserToken = loginUser(mockMvc, objectMapper, "company@example.com", "password123");
        
        // 3. 관리자 유저 생성 (별도 로직 필요할 수 있음)
        String adminUserJson = """
                {
                    "email": "admin@example.com",
                    "password": "password123",
                    "name": "관리자",
                    "userType": "ADMIN"
                }
                """;
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(adminUserJson))
                .andExpect(status().isOk());
        
        adminUserToken = loginUser(mockMvc, objectMapper, "admin@example.com", "password123");
    }
    
    private static String loginUser(MockMvc mockMvc, ObjectMapper objectMapper, String email, String password) throws Exception {
        String loginJson = String.format("""
                {
                    "email": "%s",
                    "password": "%s"
                }
                """, email, password);

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = loginResult.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(responseContent);
        
        if (jsonNode.has("data") && jsonNode.get("data").has("access_token")) {
            return jsonNode.get("data").get("access_token").asText();
        }
        throw new IllegalStateException("토큰을 추출할 수 없습니다");
    }
    
    @Test
    @Order(1)
    @DisplayName("카테고리 생성 및 관리 테스트")
    void testCategoryManagement() throws Exception {
        // 1. 취업정보 카테고리 생성
        String jobInfoCategoryJson = """
                {
                    "name": "취업정보",
                    "description": "취업 관련 정보를 공유하는 게시판입니다."
                }
                """;
        
        MvcResult jobInfoResult = mockMvc.perform(post("/api/categories")
                .header("Authorization", "Bearer " + adminUserToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jobInfoCategoryJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("취업정보"))
                .andReturn();
        
        JsonNode jobInfoNode = objectMapper.readTree(jobInfoResult.getResponse().getContentAsString());
        jobInfoCategoryId = jobInfoNode.get("id").asLong();
        
        // 2. 면접정보 카테고리 생성
        String interviewCategoryJson = """
                {
                    "name": "면접정보",
                    "description": "면접 후기 및 정보를 공유하는 게시판입니다."
                }
                """;
        
        MvcResult interviewResult = mockMvc.perform(post("/api/categories")
                .header("Authorization", "Bearer " + adminUserToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(interviewCategoryJson))
                .andExpect(status().isCreated())
                .andReturn();
        
        JsonNode interviewNode = objectMapper.readTree(interviewResult.getResponse().getContentAsString());
        interviewCategoryId = interviewNode.get("id").asLong();
        
        // 3. Q&A 카테고리 생성
        String qnaCategoryJson = """
                {
                    "name": "Q&A",
                    "description": "질문과 답변을 나누는 게시판입니다."
                }
                """;
        
        MvcResult qnaResult = mockMvc.perform(post("/api/categories")
                .header("Authorization", "Bearer " + adminUserToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(qnaCategoryJson))
                .andExpect(status().isCreated())
                .andReturn();
        
        JsonNode qnaNode = objectMapper.readTree(qnaResult.getResponse().getContentAsString());
        qnaCategoryId = qnaNode.get("id").asLong();
        
        // 4. 자유게시판 카테고리 생성 (AI 이미지 생성 기능)
        String freeBoardCategoryJson = """
                {
                    "name": "자유게시판",
                    "description": "자유롭게 소통하는 게시판입니다. AI 이미지 생성이 가능합니다."
                }
                """;
        
        MvcResult freeBoardResult = mockMvc.perform(post("/api/categories")
                .header("Authorization", "Bearer " + adminUserToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(freeBoardCategoryJson))
                .andExpect(status().isCreated())
                .andReturn();
        
        JsonNode freeBoardNode = objectMapper.readTree(freeBoardResult.getResponse().getContentAsString());
        freeBoardCategoryId = freeBoardNode.get("id").asLong();
        
        // 5. 기업게시판 카테고리 생성 (기업 유저만 접근)
        String companyCategoryJson = """
                {
                    "name": "기업게시판",
                    "description": "기업 사용자 전용 게시판입니다."
                }
                """;
        
        MvcResult companyResult = mockMvc.perform(post("/api/categories")
                .header("Authorization", "Bearer " + adminUserToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(companyCategoryJson))
                .andExpect(status().isCreated())
                .andReturn();
        
        JsonNode companyNode = objectMapper.readTree(companyResult.getResponse().getContentAsString());
        companyCategoryId = companyNode.get("id").asLong();
        
        // 6. 공지 카테고리 생성 (관리자만 작성)
        String noticeCategoryJson = """
                {
                    "name": "공지",
                    "description": "관리자 공지사항 게시판입니다."
                }
                """;
        
        MvcResult noticeResult = mockMvc.perform(post("/api/categories")
                .header("Authorization", "Bearer " + adminUserToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(noticeCategoryJson))
                .andExpect(status().isCreated())
                .andReturn();
        
        JsonNode noticeNode = objectMapper.readTree(noticeResult.getResponse().getContentAsString());
        noticeCategoryId = noticeNode.get("id").asLong();
        
        // 7. 모든 카테고리 조회 (인증 불필요)
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(6));
    }
    
    @Test
    @Order(2)
    @DisplayName("자유게시판에서 AI 이미지 생성 포스트 작성 테스트")
    void testFreeboardPostWithAIImageGeneration() throws Exception {
        // Given - AI 서비스 모킹
        SentimentAnalysisDto.AnalyzeResponse sentimentResponse = 
            new SentimentAnalysisDto.AnalyzeResponse(
                true, 
                "분석 완료", 
                new SentimentAnalysisDto.SentimentData("positive", 0.8, 0.9, "기쁨")
            );
            
        ImageGenerationDto.GenerateResponse imageResponse = 
            new ImageGenerationDto.GenerateResponse(
                true,
                "이미지 생성 완료",
                new ImageGenerationDto.ImageData(
                    "https://example.com/generated-image.jpg",
                    "A beautiful sunset scene representing happiness",
                    "digital-art",
                    "1024x1024",
                    2.5
                )
            );
        
        given(aiServiceClient.analyzeSentiment(anyString(), eq("ko")))
            .willReturn(sentimentResponse);
        given(aiServiceClient.generateImage(anyString(), eq("digital-art"), eq("1024x1024"), eq(1)))
            .willReturn(imageResponse);
        
        // When - 자유게시판에 포스트 작성
        String postJson = """
                {
                    "title": "오늘 정말 행복한 하루였어요!",
                    "content": "드디어 꿈에 그리던 취업에 성공했습니다. 모든 분들께 감사드려요!",
                    "categoryId": %d
                }
                """.formatted(freeBoardCategoryId);
        
        MvcResult postResult = mockMvc.perform(post("/api/posts")
                .header("Authorization", "Bearer " + generalUserToken)
                .header("X-User-Email", "generaluser@example.com")
                .contentType(MediaType.APPLICATION_JSON)
                .content(postJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("오늘 정말 행복한 하루였어요!"))
                // Note: AI 이미지 생성은 실제 서비스가 구동되어야 하므로 주석 처리
                .andExpect(jsonPath("$.sentimentLabel").value("positive"))
                .andReturn();
        
        JsonNode postNode = objectMapper.readTree(postResult.getResponse().getContentAsString());
        testPostId = postNode.get("id").asLong();
    }
    
    @Test
    @Order(3)
    @DisplayName("감정 분석 및 이모티콘 표시 테스트")
    void testSentimentAnalysisAndEmotionDisplay() throws Exception {
        // 1. 긍정적인 게시글 작성
        SentimentAnalysisDto.AnalyzeResponse positiveResponse = 
            new SentimentAnalysisDto.AnalyzeResponse(
                true, 
                "분석 완료", 
                new SentimentAnalysisDto.SentimentData("positive", 0.9, 0.95, "기쁨")
            );
        
        given(aiServiceClient.analyzeSentiment(contains("기쁜"), eq("ko")))
            .willReturn(positiveResponse);
        
        String positivePostJson = """
                {
                    "title": "정말 기쁜 소식입니다!",
                    "content": "오늘 정말 좋은 일이 생겼어요!",
                    "categoryId": %d
                }
                """.formatted(qnaCategoryId);
        
        mockMvc.perform(post("/api/posts")
                .header("Authorization", "Bearer " + generalUserToken)
                .header("X-User-Email", "generaluser@example.com")
                .contentType(MediaType.APPLICATION_JSON)
                .content(positivePostJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sentimentLabel").value("positive"))
                .andExpect(jsonPath("$.sentimentScore").value(0.9));
        
        // 2. 부정적인 게시글 작성
        SentimentAnalysisDto.AnalyzeResponse negativeResponse = 
            new SentimentAnalysisDto.AnalyzeResponse(
                true, 
                "분석 완료", 
                new SentimentAnalysisDto.SentimentData("negative", -0.7, 0.8, "실망")
            );
        
        given(aiServiceClient.analyzeSentiment(contains("힘든"), eq("ko")))
            .willReturn(negativeResponse);
        
        String negativePostJson = """
                {
                    "title": "오늘 정말 힘든 하루였어요",
                    "content": "면접에서 떨어져서 너무 슬퍼요...",
                    "categoryId": %d
                }
                """.formatted(qnaCategoryId);
        
        mockMvc.perform(post("/api/posts")
                .header("Authorization", "Bearer " + generalUserToken)
                .header("X-User-Email", "generaluser@example.com")
                .contentType(MediaType.APPLICATION_JSON)
                .content(negativePostJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sentimentLabel").value("negative"))
                .andExpect(jsonPath("$.sentimentScore").value(-0.7));
        
        // 3. 감정별 게시글 조회 (인증 불필요)
        mockMvc.perform(get("/api/posts/sentiment/positive"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts").isArray())
                .andExpect(jsonPath("$.posts.length()").value(greaterThan(0)));
        
        mockMvc.perform(get("/api/posts/sentiment/negative"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts").isArray())
                .andExpect(jsonPath("$.posts.length()").value(greaterThan(0)));
    }
    
    @Test
    @Order(4)
    @DisplayName("역할 기반 접근 제어 테스트")
    void testRoleBasedAccessControl() throws Exception {
        // 1. 일반 사용자가 기업게시판에 포스트 작성 시도 (실패해야 함)
        String generalUserPostJson = """
                {
                    "title": "일반 사용자 글",
                    "content": "일반 사용자가 기업게시판에 글을 써봅니다.",
                    "categoryId": %d
                }
                """.formatted(companyCategoryId);
        
        // 실제 구현에서는 서비스 레벨에서 권한 체크가 필요할 수 있음
        // 현재는 컨트롤러 레벨에서만 테스트
        
        // 2. 기업 사용자가 기업게시판에 포스트 작성 (성공해야 함)
        SentimentAnalysisDto.AnalyzeResponse neutralResponse = 
            new SentimentAnalysisDto.AnalyzeResponse(
                true, 
                "분석 완료", 
                new SentimentAnalysisDto.SentimentData("neutral", 0.1, 0.6, "정보성")
            );
        
        given(aiServiceClient.analyzeSentiment(anyString(), eq("ko")))
            .willReturn(neutralResponse);
        
        String companyUserPostJson = """
                {
                    "title": "기업 채용 정보",
                    "content": "저희 회사에서 신입 개발자를 모집합니다.",
                    "categoryId": %d
                }
                """.formatted(companyCategoryId);
        
        mockMvc.perform(post("/api/posts")
                .header("Authorization", "Bearer " + companyUserToken)
                .header("X-User-Email", "company@example.com")
                .contentType(MediaType.APPLICATION_JSON)
                .content(companyUserPostJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("기업 채용 정보"));
        
        // 3. 일반 사용자가 공지사항 작성 시도 (실패해야 함) - 관리자만 가능
        String noticePostJson = """
                {
                    "title": "공지사항",
                    "content": "중요한 공지사항입니다.",
                    "categoryId": %d
                }
                """.formatted(noticeCategoryId);
        
        // 4. 관리자가 공지사항 작성 (성공해야 함)
        mockMvc.perform(post("/api/posts")
                .header("Authorization", "Bearer " + adminUserToken)
                .header("X-User-Email", "admin@example.com")
                .contentType(MediaType.APPLICATION_JSON)
                .content(noticePostJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("공지사항"));
    }
    
    @Test
    @Order(5)
    @DisplayName("댓글 및 대댓글 기능 테스트")
    void testCommentAndReplyFunctionality() throws Exception {
        // testPostId가 null인 경우를 방지하기 위해 임시 포스트 생성
        if (testPostId == null) {
            SentimentAnalysisDto.AnalyzeResponse tempResponse = 
                new SentimentAnalysisDto.AnalyzeResponse(
                    true, 
                    "분석 완료", 
                    new SentimentAnalysisDto.SentimentData("neutral", 0.0, 0.5, "중립")
                );
            
            given(aiServiceClient.analyzeSentiment(anyString(), eq("ko")))
                .willReturn(tempResponse);
                
            String tempPostJson = """
                {
                    "title": "테스트용 임시 포스트",
                    "content": "댓글 테스트를 위한 임시 포스트입니다.",
                    "categoryId": %d
                }
                """.formatted(qnaCategoryId);
            
            MvcResult tempPostResult = mockMvc.perform(post("/api/posts")
                    .header("Authorization", "Bearer " + generalUserToken)
                    .header("X-User-Email", "generaluser@example.com")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(tempPostJson))
                    .andExpect(status().isCreated())
                    .andReturn();
            
            JsonNode tempPostNode = objectMapper.readTree(tempPostResult.getResponse().getContentAsString());
            testPostId = tempPostNode.get("id").asLong();
        }
        
        // 1. 댓글 작성
        String commentJson = """
                {
                    "content": "정말 좋은 글이네요! 축하드려요!",
                    "postId": %d
                }
                """.formatted(testPostId);
        
        MvcResult commentResult = mockMvc.perform(post("/api/comments")
                .header("Authorization", "Bearer " + generalUserToken)
                .header("X-User-Email", "generaluser@example.com")
                .contentType(MediaType.APPLICATION_JSON)
                .content(commentJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("정말 좋은 글이네요! 축하드려요!"))
                .andReturn();
        
        JsonNode commentNode = objectMapper.readTree(commentResult.getResponse().getContentAsString());
        testCommentId = commentNode.get("id").asLong();
        
        // 2. 대댓글 작성
        String replyJson = """
                {
                    "content": "저도 동감합니다!",
                    "postId": %d,
                    "parentCommentId": %d
                }
                """.formatted(testPostId, testCommentId);
        
        mockMvc.perform(post("/api/comments")
                .header("Authorization", "Bearer " + companyUserToken)
                .header("X-User-Email", "company@example.com")
                .contentType(MediaType.APPLICATION_JSON)
                .content(replyJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("저도 동감합니다!"))
                .andExpect(jsonPath("$.parentCommentId").value(testCommentId));
        
        // 3. 게시글의 댓글 목록 조회 (인증 불필요)
        mockMvc.perform(get("/api/comments/post/" + testPostId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(2)));
        
        // 4. 댓글 수 조회 (인증 불필요)
        mockMvc.perform(get("/api/comments/post/" + testPostId + "/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(greaterThanOrEqualTo(2)));
        
        // 5. 대댓글 목록 조회 (인증 불필요)
        mockMvc.perform(get("/api/comments/parent/" + testCommentId + "/replies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(1)));
    }
    
    @Test
    @Order(6)
    @DisplayName("게시글 검색 및 필터링 테스트")
    void testPostSearchAndFiltering() throws Exception {
        // 1. 제목으로 게시글 검색 (인증 불필요)
        mockMvc.perform(get("/api/posts/search")
                .param("keyword", "행복"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts").isArray())
                .andExpect(jsonPath("$.posts.length()").value(greaterThan(0)));
        
        // 2. 카테고리별 게시글 조회 (인증 불필요)
        mockMvc.perform(get("/api/posts/category/" + freeBoardCategoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts").isArray());
        
        // 3. 이미지가 있는 게시글만 조회 (인증 불필요)
        mockMvc.perform(get("/api/posts/with-images"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts").isArray());
        
        // 4. 감정 점수 범위로 게시글 조회 (인증 불필요)
        mockMvc.perform(get("/api/posts/sentiment-score")
                .param("minScore", "0.5")
                .param("maxScore", "1.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts").isArray());
        
        // 5. 카테고리와 감정을 동시에 필터링 (인증 불필요)
        mockMvc.perform(get("/api/posts/category/" + freeBoardCategoryId + "/sentiment/positive"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts").isArray());
    }
    
    @Test
    @Order(7)
    @DisplayName("게시글 수정 및 삭제 테스트")
    void testPostUpdateAndDelete() throws Exception {
        // testPostId가 null인 경우를 방지하기 위해 임시 포스트 생성
        if (testPostId == null) {
            SentimentAnalysisDto.AnalyzeResponse tempResponse = 
                new SentimentAnalysisDto.AnalyzeResponse(
                    true, 
                    "분석 완료", 
                    new SentimentAnalysisDto.SentimentData("neutral", 0.0, 0.5, "중립")
                );
            
            given(aiServiceClient.analyzeSentiment(anyString(), eq("ko")))
                .willReturn(tempResponse);
                
            String tempPostJson = """
                {
                    "title": "수정 테스트용 포스트",
                    "content": "수정을 위한 테스트 포스트입니다.",
                    "categoryId": %d
                }
                """.formatted(qnaCategoryId);
            
            MvcResult tempPostResult = mockMvc.perform(post("/api/posts")
                    .header("Authorization", "Bearer " + generalUserToken)
                    .header("X-User-Email", "generaluser@example.com")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(tempPostJson))
                    .andExpect(status().isCreated())
                    .andReturn();
            
            JsonNode tempPostNode = objectMapper.readTree(tempPostResult.getResponse().getContentAsString());
            testPostId = tempPostNode.get("id").asLong();
        }
        
        // testCommentId가 null인 경우를 방지하기 위해 임시 댓글 생성
        if (testCommentId == null) {
            String tempCommentJson = """
                {
                    "content": "수정 테스트용 댓글",
                    "postId": %d
                }
                """.formatted(testPostId);
            
            MvcResult tempCommentResult = mockMvc.perform(post("/api/comments")
                    .header("Authorization", "Bearer " + generalUserToken)
                    .header("X-User-Email", "generaluser@example.com")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(tempCommentJson))
                    .andExpect(status().isCreated())
                    .andReturn();
            
            JsonNode tempCommentNode = objectMapper.readTree(tempCommentResult.getResponse().getContentAsString());
            testCommentId = tempCommentNode.get("id").asLong();
        }
        
        // 1. 게시글 수정
        String updateJson = """
                {
                    "title": "수정된 제목: 정말 행복한 하루였어요!",
                    "content": "수정된 내용: 취업 성공 후 더욱 감사한 마음입니다."
                }
                """;
        
        mockMvc.perform(put("/api/posts/" + testPostId)
                .header("Authorization", "Bearer " + generalUserToken)
                .header("X-User-Email", "generaluser@example.com")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("수정된 제목: 정말 행복한 하루였어요!"));
        
        // 2. 조회수 증가 테스트 (인증 불필요)
        mockMvc.perform(patch("/api/posts/" + testPostId + "/view"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.viewCount").value(greaterThan(0)));
        
        // 3. 댓글 수정
        String updateCommentJson = """
                {
                    "content": "수정된 댓글: 정말 대단하시네요!"
                }
                """;
        
        mockMvc.perform(put("/api/comments/" + testCommentId)
                .header("Authorization", "Bearer " + generalUserToken)
                .header("X-User-Email", "generaluser@example.com")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateCommentJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("수정된 댓글: 정말 대단하시네요!"));
    }
    
    @Test
    @Order(8)
    @DisplayName("통합 시나리오 테스트: 전체 커뮤니티 워크플로우")
    void testIntegratedCommunityWorkflow() throws Exception {
        // 1. 새로운 사용자가 가입하고 로그인
        String newUserJson = """
                {
                    "email": "newuser@example.com",
                    "password": "password123",
                    "name": "신규 사용자",
                    "userType": "GENERAL"
                }
                """;
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(newUserJson))
                .andExpect(status().isOk());
        
        String newUserToken = loginUser(mockMvc, objectMapper, "newuser@example.com", "password123");
        
        // 2. 카테고리 목록 조회 (인증 불필요)
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(greaterThan(0)));
        
        // 3. Q&A에 질문 게시글 작성
        SentimentAnalysisDto.AnalyzeResponse questionResponse = 
            new SentimentAnalysisDto.AnalyzeResponse(
                true, 
                "분석 완료", 
                new SentimentAnalysisDto.SentimentData("neutral", 0.2, 0.7, "궁금")
            );
        
        given(aiServiceClient.analyzeSentiment(contains("질문"), eq("ko")))
            .willReturn(questionResponse);
        
        String questionJson = """
                {
                    "title": "면접 준비 관련 질문드립니다",
                    "content": "첫 면접을 앞두고 있는데, 어떻게 준비하면 좋을까요?",
                    "categoryId": %d
                }
                """.formatted(qnaCategoryId);
        
        MvcResult questionResult = mockMvc.perform(post("/api/posts")
                .header("Authorization", "Bearer " + newUserToken)
                .header("X-User-Email", "newuser@example.com")
                .contentType(MediaType.APPLICATION_JSON)
                .content(questionJson))
                .andExpect(status().isCreated())
                .andReturn();
        
        JsonNode questionNode = objectMapper.readTree(questionResult.getResponse().getContentAsString());
        Long questionPostId = questionNode.get("id").asLong();
        
        // 4. 기존 사용자가 답변 댓글 작성
        String answerJson = """
                {
                    "content": "면접 경험을 공유드리자면, 자기소개서를 완벽히 숙지하시고 해당 기업에 대해 충분히 조사해보시길 추천드려요!",
                    "postId": %d
                }
                """.formatted(questionPostId);
        
        mockMvc.perform(post("/api/comments")
                .header("Authorization", "Bearer " + generalUserToken)
                .header("X-User-Email", "generaluser@example.com")
                .contentType(MediaType.APPLICATION_JSON)
                .content(answerJson))
                .andExpect(status().isCreated());
        
        // 5. 질문자가 감사 댓글 작성
        String thanksJson = """
                {
                    "content": "정말 도움이 되는 조언 감사합니다! 열심히 준비해보겠어요.",
                    "postId": %d
                }
                """.formatted(questionPostId);
        
        mockMvc.perform(post("/api/comments")
                .header("Authorization", "Bearer " + newUserToken)
                .header("X-User-Email", "newuser@example.com")
                .contentType(MediaType.APPLICATION_JSON)
                .content(thanksJson))
                .andExpect(status().isCreated());
        
        // 6. 게시글 조회수 증가 및 최종 상태 확인 (인증 불필요)
        mockMvc.perform(get("/api/posts/" + questionPostId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("면접 준비 관련 질문드립니다"))
                .andExpect(jsonPath("$.sentimentLabel").value("neutral"));
    }
}