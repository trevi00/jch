package org.jbd.backend.config;

import org.jbd.backend.community.domain.Category;
import org.jbd.backend.community.repository.CategoryRepository;
import org.jbd.backend.user.domain.User;
import org.jbd.backend.user.domain.enums.UserType;
import org.jbd.backend.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(CategoryRepository categoryRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        initializeCategories();
        initializeAdminUser();
    }

    private void initializeCategories() {
        // 카테고리가 이미 존재하면 초기화하지 않음
        if (categoryRepository.count() > 0) {
            return;
        }

        // 6개 커뮤니티 카테고리 생성
        createCategory("취업정보", "취업 관련 정보 공유", 1);
        createCategory("면접정보", "면접 관련 정보 공유", 2);
        createCategory("Q&A", "질문과 답변", 3);
        createCategory("자유게시판", "자유로운 소통 공간 (AI 이미지 생성 및 감정분석 지원)", 4);
        createCategory("기업게시판", "기업 전용 게시판", 5);
        createCategory("공지", "관리자 공지사항", 6);

        System.out.println("✅ 커뮤니티 카테고리 초기화 완료");
    }

    private void createCategory(String name, String description, int displayOrder) {
        Category category = new Category(name, description);
        category.updateDisplayOrder(displayOrder);
        
        categoryRepository.save(category);
        System.out.println("카테고리 생성: " + name);
    }

    private void initializeAdminUser() {
        // 관리자 계정이 이미 존재하면 생성하지 않음
        if (userRepository.findByEmail("admin2@test.com").isPresent()) {
            return;
        }

        // 관리자 계정 생성
        User adminUser = new User(
                "admin2@test.com",
                passwordEncoder.encode("admin123!"),
                UserType.ADMIN
        );

        userRepository.save(adminUser);
        System.out.println("✅ 관리자 계정 생성 완료: admin2@test.com / admin123!");
    }
}