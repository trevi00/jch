package org.jbd.backend.user.repository;

import org.jbd.backend.user.domain.User;
import org.jbd.backend.user.domain.enums.UserType;
import org.jbd.backend.user.domain.enums.OAuthProvider;
import org.jbd.backend.user.domain.enums.EmploymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndIsDeletedFalse(String email);

    Optional<User> findByOauthProviderAndOauthId(OAuthProvider oauthProvider, String oauthId);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIsDeletedFalse(String email);

    List<User> findByUserType(UserType userType);

    List<User> findByEmploymentStatus(EmploymentStatus employmentStatus);

    List<User> findByIsActiveTrue();

    List<User> findByIsActiveFalse();

    @Query("SELECT u FROM User u WHERE u.userType = :userType AND u.isDeleted = false AND u.isActive = true")
    List<User> findActiveUsersByType(@Param("userType") UserType userType);

    @Query("SELECT COUNT(u) FROM User u WHERE u.userType = :userType AND u.isDeleted = false AND u.isActive = true")
    long countActiveUsersByType(@Param("userType") UserType userType);

    @Query("SELECT COUNT(u) FROM User u WHERE u.userType = :userType AND u.createdAt >= :startDate AND u.isDeleted = false")
    long countNewUsersByTypeFromDate(@Param("userType") UserType userType, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :startDate AND u.isDeleted = false")
    long countAllNewUsersFromDate(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT u FROM User u WHERE u.emailVerified = false AND u.isDeleted = false AND u.isActive = true")
    List<User> findUsersWithUnverifiedEmail();

    @Query("SELECT u FROM User u WHERE u.companyEmailVerified = false AND u.userType = :userType AND u.isDeleted = false AND u.isActive = true")
    List<User> findUsersWithUnverifiedCompanyEmail(@Param("userType") UserType userType);

    @Query("SELECT u FROM User u WHERE u.companyEmailVerified = true AND u.userType = :userType AND u.isDeleted = false")
    List<User> findUsersWithVerifiedCompanyEmail(@Param("userType") UserType userType);

    @Query("SELECT u FROM User u WHERE u.oauthProvider = :provider AND u.isDeleted = false")
    List<User> findByOAuthProvider(@Param("provider") OAuthProvider provider);

    /**
     * 성능 최적화: 사용자 통계를 한 번의 쿼리로 조회
     */
    @Query("SELECT " +
           "COUNT(CASE WHEN u.userType = org.jbd.backend.user.domain.enums.UserType.GENERAL THEN 1 END) as generalCount, " +
           "COUNT(CASE WHEN u.userType = org.jbd.backend.user.domain.enums.UserType.COMPANY THEN 1 END) as companyCount, " +
           "COUNT(CASE WHEN u.userType = org.jbd.backend.user.domain.enums.UserType.ADMIN THEN 1 END) as adminCount, " +
           "COUNT(*) as totalCount " +
           "FROM User u WHERE u.isDeleted = false AND u.isActive = true")
    Object[] findUserStatistics();

    /**
     * 성능 최적화: 기간별 신규 사용자 통계를 한 번의 쿼리로 조회
     */
    @Query("SELECT " +
           "COUNT(CASE WHEN u.createdAt >= :today THEN 1 END) as todayCount, " +
           "COUNT(CASE WHEN u.createdAt >= :weekAgo THEN 1 END) as weekCount, " +
           "COUNT(CASE WHEN u.createdAt >= :monthAgo THEN 1 END) as monthCount, " +
           "COUNT(CASE WHEN u.createdAt >= :today AND u.userType = org.jbd.backend.user.domain.enums.UserType.GENERAL THEN 1 END) as todayGeneralCount, " +
           "COUNT(CASE WHEN u.createdAt >= :today AND u.userType = org.jbd.backend.user.domain.enums.UserType.COMPANY THEN 1 END) as todayCompanyCount " +
           "FROM User u WHERE u.isDeleted = false AND u.isActive = true")
    Object[] findNewUserStatistics(@Param("today") LocalDateTime today,
                                  @Param("weekAgo") LocalDateTime weekAgo,
                                  @Param("monthAgo") LocalDateTime monthAgo);

    /**
     * 일반 사용자들의 조회
     */
    @Query("SELECT u FROM User u WHERE u.userType = org.jbd.backend.user.domain.enums.UserType.GENERAL AND u.isDeleted = false AND u.isActive = true")
    List<User> findAllGeneralUsers();

    /**
     * OAuth 제공자별 통계
     */
    @Query("SELECT " +
           "COUNT(CASE WHEN u.oauthProvider = org.jbd.backend.user.domain.enums.OAuthProvider.NATIVE THEN 1 END) as nativeCount, " +
           "COUNT(CASE WHEN u.oauthProvider = org.jbd.backend.user.domain.enums.OAuthProvider.GOOGLE THEN 1 END) as googleCount " +
           "FROM User u WHERE u.isDeleted = false AND u.isActive = true")
    Object[] findOAuthProviderStatistics();

    /**
     * UserType별 사용자 수 카운트
     */
    long countByUserType(UserType userType);
}