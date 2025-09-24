package org.jbd.backend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jbd.backend.ai.client.AIServiceClient;
import org.jbd.backend.ai.dto.ImageGenerationDto;
import org.jbd.backend.ai.dto.SentimentAnalysisDto;
import org.jbd.backend.community.domain.Category;
import org.jbd.backend.community.domain.Post;
import org.jbd.backend.community.dto.PostDto;
import org.jbd.backend.community.repository.CategoryRepository;
import org.jbd.backend.community.repository.PostRepository;
import org.jbd.backend.community.service.PostService;
import org.jbd.backend.user.domain.User;
import org.jbd.backend.user.domain.enums.EmploymentStatus;
import org.jbd.backend.user.domain.enums.UserType;
import org.jbd.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("AI 서비스 통합 테스트")
public class AIIntegrationTest {

    @Autowired
    private PostService postService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private PostRepository postRepository;
    
    @MockBean
    private AIServiceClient aiServiceClient;
    
    private User testUser;
    private Category testCategory;
    
    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성
        testUser = new User("test@example.com", "testUser", "google", "123", UserType.GENERAL);
        testUser.setEmploymentStatus(EmploymentStatus.JOB_SEEKING);
        testUser = userRepository.save(testUser);
        
        // 테스트 카테고리 생성 (이미지 생성이 되는 카테고리)
        testCategory = new Category("창작", "창작 관련 게시판");
        testCategory = categoryRepository.save(testCategory);
    }
    
    @Test
    @DisplayName("게시글 생성 시 감정 분석이 수행되어야 한다")
    void shouldPerformSentimentAnalysisOnPostCreation() {
        // Given
        String title = "오늘 정말 기쁜 일이 있었어요!";
        String content = "드디어 취업에 성공했습니다. 너무 행복하고 감사해요!";
        
        SentimentAnalysisDto.AnalyzeResponse sentimentResponse = 
            new SentimentAnalysisDto.AnalyzeResponse(
                true, 
                "분석 완료", 
                new SentimentAnalysisDto.SentimentData("positive", 0.8, 0.9, "기쁨")
            );
            
        given(aiServiceClient.analyzeSentiment(anyString(), eq("ko")))
            .willReturn(sentimentResponse);
        
        PostDto.CreateRequest request = new PostDto.CreateRequest();
        request.setTitle(title);
        request.setContent(content);
        request.setCategoryId(testCategory.getId());
        
        // When
        PostDto.Response response = postService.createPost(request, testUser.getEmail());
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo(title);
        assertThat(response.getContent()).isEqualTo(content);
        
        // 감정 분석 호출 확인
        verify(aiServiceClient).analyzeSentiment(title + " " + content, "ko");
        
        // 게시글에 감정 정보 저장 확인
        Post savedPost = postRepository.findById(response.getId()).orElseThrow();
        assertThat(savedPost.getSentimentLabel()).isEqualTo("positive");
        assertThat(savedPost.getSentimentScore()).isEqualTo(0.8);
    }
    
    @Test
    @DisplayName("창작 카테고리 게시글 생성 시 이미지가 생성되어야 한다")
    void shouldGenerateImageForCreativePost() {
        // Given
        String title = "나의 새로운 프로젝트";
        String content = "창의적인 아이디어로 멋진 작품을 만들고 있습니다.";
        
        SentimentAnalysisDto.AnalyzeResponse sentimentResponse = 
            new SentimentAnalysisDto.AnalyzeResponse(
                true, 
                "분석 완료", 
                new SentimentAnalysisDto.SentimentData("positive", 0.7, 0.8, "창의성")
            );
            
        ImageGenerationDto.GenerateResponse imageResponse = 
            new ImageGenerationDto.GenerateResponse(
                true,
                "이미지 생성 완료",
                new ImageGenerationDto.ImageData(
                    "https://example.com/generated-image.jpg",
                    "A creative illustration representing: " + title,
                    "digital-art",
                    "1024x1024",
                    2.5
                )
            );
            
        given(aiServiceClient.analyzeSentiment(anyString(), eq("ko")))
            .willReturn(sentimentResponse);
        given(aiServiceClient.generateImage(anyString(), eq("digital-art"), eq("1024x1024"), eq(1)))
            .willReturn(imageResponse);
        
        PostDto.CreateRequest request = new PostDto.CreateRequest();
        request.setTitle(title);
        request.setContent(content);
        request.setCategoryId(testCategory.getId());
        
        // When
        PostDto.Response response = postService.createPost(request, testUser.getEmail());
        
        // Then
        assertThat(response).isNotNull();
        
        // 이미지 생성 호출 확인
        verify(aiServiceClient).generateImage(
            contains(title), 
            eq("digital-art"), 
            eq("1024x1024"), 
            eq(1)
        );
        
        // 게시글에 이미지 URL 저장 확인
        Post savedPost = postRepository.findById(response.getId()).orElseThrow();
        assertThat(savedPost.getImageUrl()).isEqualTo("https://example.com/generated-image.jpg");
    }
    
    @Test
    @DisplayName("일반 카테고리 게시글은 이미지가 생성되지 않아야 한다")
    void shouldNotGenerateImageForGeneralPost() {
        // Given
        Category generalCategory = new Category("일반", "일반 게시판");
        generalCategory = categoryRepository.save(generalCategory);
        
        SentimentAnalysisDto.AnalyzeResponse sentimentResponse = 
            new SentimentAnalysisDto.AnalyzeResponse(
                true, 
                "분석 완료", 
                new SentimentAnalysisDto.SentimentData("neutral", 0.5, 0.7, "일반")
            );
            
        given(aiServiceClient.analyzeSentiment(anyString(), eq("ko")))
            .willReturn(sentimentResponse);
        
        PostDto.CreateRequest request = new PostDto.CreateRequest();
        request.setTitle("일반적인 질문입니다");
        request.setContent("궁금한 점이 있어서 질문드려요.");
        request.setCategoryId(generalCategory.getId());
        
        // When
        PostDto.Response response = postService.createPost(request, testUser.getEmail());
        
        // Then
        assertThat(response).isNotNull();
        
        // 이미지 생성이 호출되지 않아야 함
        verify(aiServiceClient, never()).generateImage(anyString(), anyString(), anyString(), anyInt());
        
        // 게시글에 이미지 URL이 없어야 함
        Post savedPost = postRepository.findById(response.getId()).orElseThrow();
        assertThat(savedPost.getImageUrl()).isNullOrEmpty();
    }
    
    @Test
    @DisplayName("감정별 게시글 조회가 정상 작동해야 한다")
    void shouldRetrievePostsBySentiment() {
        // Given - 긍정적인 게시글 생성
        Post positivePost = new Post("행복한 소식", "정말 기쁜 일이 있었어요!", testUser, testCategory);
        positivePost.updateSentiment(0.8, "positive");
        positivePost = postRepository.save(positivePost);
        
        // Given - 부정적인 게시글 생성
        Post negativePost = new Post("힘든 하루", "오늘은 정말 힘들었네요.", testUser, testCategory);
        negativePost.updateSentiment(-0.6, "negative");  
        negativePost = postRepository.save(negativePost);
        
        // When - 긍정적인 게시글만 조회
        PostDto.PageResponse positiveResults = postService.getPostsBySentiment(
            "positive", 
            org.springframework.data.domain.PageRequest.of(0, 10)
        );
        
        // Then
        assertThat(positiveResults.getPosts()).hasSize(1);
        assertThat(positiveResults.getPosts().get(0).getTitle()).isEqualTo("행복한 소식");
        
        // When - 부정적인 게시글만 조회
        PostDto.PageResponse negativeResults = postService.getPostsBySentiment(
            "negative",
            org.springframework.data.domain.PageRequest.of(0, 10)
        );
        
        // Then
        assertThat(negativeResults.getPosts()).hasSize(1);
        assertThat(negativeResults.getPosts().get(0).getTitle()).isEqualTo("힘든 하루");
    }
    
    @Test
    @DisplayName("감정 점수 범위별 게시글 조회가 정상 작동해야 한다")
    void shouldRetrievePostsBySentimentScore() {
        // Given
        Post highPositivePost = new Post("최고의 날", "인생 최고의 하루!", testUser, testCategory);
        highPositivePost.updateSentiment(0.9, "positive");
        postRepository.save(highPositivePost);
        
        Post lowPositivePost = new Post("괜찮은 하루", "나쁘지 않네요", testUser, testCategory);  
        lowPositivePost.updateSentiment(0.3, "positive");
        postRepository.save(lowPositivePost);
        
        // When - 고도의 긍정 감정 게시글만 조회 (0.7~1.0)
        PostDto.PageResponse highPositiveResults = postService.getPostsBySentimentScore(
            0.7, 1.0,
            org.springframework.data.domain.PageRequest.of(0, 10)
        );
        
        // Then
        assertThat(highPositiveResults.getPosts()).hasSize(1);
        assertThat(highPositiveResults.getPosts().get(0).getTitle()).isEqualTo("최고의 날");
    }
    
    @Test
    @DisplayName("이미지가 있는 게시글만 조회할 수 있어야 한다")
    void shouldRetrievePostsWithImages() {
        // Given
        Post postWithImage = new Post("이미지 포스트", "사진이 포함된 게시글", testUser, testCategory);
        postWithImage.setImageUrl("https://example.com/image1.jpg");
        postRepository.save(postWithImage);
        
        Post postWithoutImage = new Post("텍스트 포스트", "사진이 없는 게시글", testUser, testCategory);
        postRepository.save(postWithoutImage);
        
        // When
        PostDto.PageResponse results = postService.getPostsWithImages(
            org.springframework.data.domain.PageRequest.of(0, 10)
        );
        
        // Then
        assertThat(results.getPosts()).hasSize(1);
        assertThat(results.getPosts().get(0).getTitle()).isEqualTo("이미지 포스트");
    }
}