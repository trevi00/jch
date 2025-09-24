package org.jbd.backend.common.service;

import lombok.RequiredArgsConstructor;
import org.jbd.backend.community.domain.Post;
import org.jbd.backend.job.domain.JobPosting;
import org.jbd.backend.user.domain.User;
import org.jbd.backend.user.domain.enums.UserType;
import org.jbd.backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;

/**
 * 권한 관리 서비스
 *
 * 애플리케이션 전반에서 사용되는 권한 검증 로직을 중앙화하여 관리하는 서비스입니다.
 * 사용자 타입과 리소스 소유권을 기반으로 CRUD 권한을 검증합니다.
 *
 * 주요 권한 정책:
 * - 일반 사용자: 자신이 작성한 게시글만 수정/삭제 가능
 * - 기업 사용자: 자신이 등록한 채용공고만 관리 가능
 * - 관리자: 모든 리소스에 대한 전체 권한 보유
 *
 * 사용 예시:
 * - 게시글 수정/삭제 전 권한 확인
 * - 채용공고 관리 권한 검증
 * - 관리자 전용 기능 접근 제어
 *
 * @author JBD Backend Team
 * @version 1.0
 * @since 2025-09-19
 * @see UserType
 * @see Post
 * @see JobPosting
 */
@Service
@RequiredArgsConstructor
public class PermissionService {

    /** 사용자 정보 조회를 위한 레포지토리 */
    private final UserRepository userRepository;

    /**
     * 게시글 수정 권한을 확인합니다.
     *
     * 게시글 작성자와 관리자만 게시글을 수정할 수 있습니다.
     * null 값에 대한 안전성 검사를 포함합니다.
     *
     * @param userId 권한을 확인할 사용자 ID
     * @param post 수정 대상 게시글
     * @return 수정 권한이 있으면 true, 없으면 false
     */
    public boolean canEditPost(Long userId, Post post) {
        // 필수 매개변수 검증
        if (userId == null || post == null) {
            return false;
        }

        // 사용자 정보 조회
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return false;
        }

        // 작성자는 수정 가능
        if (post.getAuthor().getId().equals(userId)) {
            return true;
        }

        // 관리자는 모든 게시글 수정 가능
        return user.getUserType() == UserType.ADMIN;
    }

    /**
     * 게시글 삭제 권한을 확인합니다.
     *
     * 게시글 작성자와 관리자만 게시글을 삭제할 수 있습니다.
     * 수정 권한과 동일한 정책을 적용합니다.
     *
     * @param userId 권한을 확인할 사용자 ID
     * @param post 삭제 대상 게시글
     * @return 삭제 권한이 있으면 true, 없으면 false
     */
    public boolean canDeletePost(Long userId, Post post) {
        // 필수 매개변수 검증
        if (userId == null || post == null) {
            return false;
        }

        // 사용자 정보 조회
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return false;
        }

        // 작성자는 삭제 가능
        if (post.getAuthor().getId().equals(userId)) {
            return true;
        }

        // 관리자는 모든 게시글 삭제 가능
        return user.getUserType() == UserType.ADMIN;
    }

    /**
     * 채용공고 수정 권한을 확인합니다.
     *
     * 채용공고 등록자(기업 사용자)와 관리자만 수정할 수 있습니다.
     * 기업의 채용공고 관리 권한을 보장합니다.
     *
     * @param userId 권한을 확인할 사용자 ID
     * @param jobPosting 수정 대상 채용공고
     * @return 수정 권한이 있으면 true, 없으면 false
     */
    public boolean canEditJobPosting(Long userId, JobPosting jobPosting) {
        // 필수 매개변수 검증
        if (userId == null || jobPosting == null) {
            return false;
        }

        // 사용자 정보 조회
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return false;
        }

        // 작성자(기업 사용자)는 수정 가능
        if (jobPosting.getCompanyUser().getId().equals(userId)) {
            return true;
        }

        // 관리자는 모든 채용공고 수정 가능
        return user.getUserType() == UserType.ADMIN;
    }

    /**
     * 채용공고 삭제 권한을 확인합니다.
     *
     * 채용공고 등록자(기업 사용자)와 관리자만 삭제할 수 있습니다.
     * 수정 권한과 동일한 정책을 적용합니다.
     *
     * @param userId 권한을 확인할 사용자 ID
     * @param jobPosting 삭제 대상 채용공고
     * @return 삭제 권한이 있으면 true, 없으면 false
     */
    public boolean canDeleteJobPosting(Long userId, JobPosting jobPosting) {
        // 필수 매개변수 검증
        if (userId == null || jobPosting == null) {
            return false;
        }

        // 사용자 정보 조회
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return false;
        }

        // 작성자(기업 사용자)는 삭제 가능
        if (jobPosting.getCompanyUser().getId().equals(userId)) {
            return true;
        }

        // 관리자는 모든 채용공고 삭제 가능
        return user.getUserType() == UserType.ADMIN;
    }

    /**
     * 채용공고 관리 권한을 확인합니다.
     *
     * 채용공고의 상태 관리(발행, 마감, 중단 등)에 대한 권한을 확인합니다.
     * 등록자와 관리자만 채용공고의 상태를 변경할 수 있습니다.
     *
     * @param userId 권한을 확인할 사용자 ID
     * @param jobPosting 관리 대상 채용공고
     * @return 관리 권한이 있으면 true, 없으면 false
     */
    public boolean canManageJobPosting(Long userId, JobPosting jobPosting) {
        // 필수 매개변수 검증
        if (userId == null || jobPosting == null) {
            return false;
        }

        // 사용자 정보 조회
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return false;
        }

        // 작성자(기업 사용자)는 관리 가능 (발행, 마감 등)
        if (jobPosting.getCompanyUser().getId().equals(userId)) {
            return true;
        }

        // 관리자는 모든 채용공고 관리 가능
        return user.getUserType() == UserType.ADMIN;
    }

    /**
     * 사용자가 관리자인지 확인합니다.
     *
     * 관리자 전용 기능에 대한 접근 제어에 사용됩니다.
     * null 값에 대한 안전성 검사를 포함합니다.
     *
     * @param userId 확인할 사용자 ID
     * @return 관리자면 true, 아니면 false
     */
    public boolean isAdmin(Long userId) {
        // null 값 검증
        if (userId == null) {
            return false;
        }

        // 사용자 조회 및 권한 확인
        User user = userRepository.findById(userId).orElse(null);
        return user != null && user.getUserType() == UserType.ADMIN;
    }

    /**
     * 사용자가 기업 사용자인지 확인합니다.
     *
     * 기업 전용 기능(채용공고 등록, 지원자 관리 등)에 대한
     * 접근 제어에 사용됩니다.
     *
     * @param userId 확인할 사용자 ID
     * @return 기업 사용자면 true, 아니면 false
     */
    public boolean isCompanyUser(Long userId) {
        // null 값 검증
        if (userId == null) {
            return false;
        }

        // 사용자 조회 및 권한 확인
        User user = userRepository.findById(userId).orElse(null);
        return user != null && user.getUserType() == UserType.COMPANY;
    }
}