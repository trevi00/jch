package org.jbd.backend.user.repository;

import org.jbd.backend.user.domain.UserProfile;
import org.jbd.backend.user.domain.User;
import org.jbd.backend.user.domain.enums.Gender;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    Optional<UserProfile> findByUser(User user);

    Optional<UserProfile> findByUserId(Long userId);

    boolean existsByUser(User user);

    boolean existsByUserId(Long userId);

    List<UserProfile> findByGender(Gender gender);

    List<UserProfile> findByLocation(String location);

    List<UserProfile> findByDesiredJob(String desiredJob);

    @Query("SELECT up FROM UserProfile up WHERE up.user.isDeleted = false AND up.user.isActive = true")
    List<UserProfile> findAllActiveProfiles();

    @Query("SELECT up FROM UserProfile up WHERE up.location LIKE %:location% AND up.user.isDeleted = false AND up.user.isActive = true")
    List<UserProfile> findByLocationContaining(@Param("location") String location);

    @Query("SELECT up FROM UserProfile up WHERE up.desiredJob LIKE %:job% AND up.user.isDeleted = false AND up.user.isActive = true")
    List<UserProfile> findByDesiredJobContaining(@Param("job") String job);

    @Query("SELECT up FROM UserProfile up WHERE up.age BETWEEN :minAge AND :maxAge AND up.user.isDeleted = false AND up.user.isActive = true")
    List<UserProfile> findByAgeBetween(@Param("minAge") Integer minAge, @Param("maxAge") Integer maxAge);

    @Query("SELECT " +
           "COUNT(CASE WHEN up.gender = 'MALE' THEN 1 END) as maleCount, " +
           "COUNT(CASE WHEN up.gender = 'FEMALE' THEN 1 END) as femaleCount, " +
           "COUNT(CASE WHEN up.gender = 'OTHER' THEN 1 END) as otherCount, " +
           "AVG(up.age) as averageAge " +
           "FROM UserProfile up WHERE up.user.isDeleted = false AND up.user.isActive = true")
    Object[] findDemographicStatistics();
}