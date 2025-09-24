package org.jbd.backend.dashboard.repository;

import org.jbd.backend.dashboard.domain.SystemMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SystemMetricsRepository extends JpaRepository<SystemMetrics, Long> {
    
    /**
     * 특정 메트릭 타입의 최신 값 조회
     */
    Optional<SystemMetrics> findFirstByMetricTypeOrderByCreatedAtDesc(SystemMetrics.MetricType metricType);
    
    /**
     * 특정 메트릭 타입의 특정 기간 데이터 조회
     */
    List<SystemMetrics> findByMetricTypeAndMetricDateBetweenOrderByMetricDateAsc(
            SystemMetrics.MetricType metricType, LocalDate start, LocalDate end);
    
    /**
     * 일별 API 호출 수 통계
     */
    @Query("SELECT sm.metricDate as date, SUM(sm.value) as totalCalls " +
           "FROM SystemMetrics sm " +
           "WHERE sm.metricType = 'API_CALLS' " +
           "AND sm.metricDate >= :startDate " +
           "GROUP BY sm.metricDate " +
           "ORDER BY sm.metricDate DESC")
    List<Object[]> findDailyApiCallsStatistics(@Param("startDate") LocalDate startDate);
    
    /**
     * 오늘 일일 활성 사용자 수
     */
    @Query("SELECT COALESCE(sm.value, 0) FROM SystemMetrics sm " +
           "WHERE sm.metricType = 'DAILY_ACTIVE_USERS' " +
           "AND sm.metricDate = CURRENT_DATE")
    Optional<Long> findTodayActiveUsers();
    
    /**
     * 오늘 에러 발생 횟수
     */
    @Query("SELECT COALESCE(sm.value, 0) FROM SystemMetrics sm " +
           "WHERE sm.metricType = 'ERROR_COUNT' " +
           "AND sm.metricDate = CURRENT_DATE")
    Optional<Long> findTodayErrorCount();
    
    /**
     * 전체 API 호출 수 (누적)
     */
    @Query("SELECT COALESCE(SUM(sm.value), 0) FROM SystemMetrics sm WHERE sm.metricType = 'API_CALLS'")
    Long findTotalApiCalls();
    
    /**
     * 최근 7일간 평균 활성 사용자 수
     */
    @Query("SELECT AVG(sm.value) FROM SystemMetrics sm " +
           "WHERE sm.metricType = 'DAILY_ACTIVE_USERS' " +
           "AND sm.metricDate >= :weekAgo")
    Optional<Double> findWeeklyAverageActiveUsers(@Param("weekAgo") LocalDate weekAgo);
    
    /**
     * 특정 메트릭 타입의 데이터 개수
     */
    Long countByMetricType(SystemMetrics.MetricType metricType);
    
    /**
     * 날짜와 메트릭 타입으로 조회
     */
    Optional<SystemMetrics> findByMetricDateAndMetricType(LocalDate date, SystemMetrics.MetricType metricType);
    
    /**
     * 특정 날짜의 모든 메트릭 조회
     */
    List<SystemMetrics> findByMetricDateOrderByMetricType(LocalDate date);
    
    /**
     * 메트릭 타입별 날짜 범위 조회
     */
    List<SystemMetrics> findByMetricTypeAndMetricDateBetweenOrderByMetricDate(
            SystemMetrics.MetricType metricType, LocalDate startDate, LocalDate endDate);
    
    /**
     * 메트릭 타입별 최신 값 조회
     */
    Optional<SystemMetrics> findFirstByMetricTypeOrderByMetricDateDesc(SystemMetrics.MetricType metricType);
    
    /**
     * 오래된 메트릭 데이터 정리
     */
    void deleteByMetricDateBefore(LocalDate cutoffDate);
}