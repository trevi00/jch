package org.jbd.backend.admin.dto;

import lombok.Builder;
import lombok.Data;
import org.jbd.backend.dashboard.dto.AdminDashboardDto;

/**
 * 관리자 대시보드 응답 DTO
 *
 * 관리자 대시보드에 필요한 데이터를 담는 래퍼 클래스입니다.
 * 추후 추가적인 관리자 전용 데이터를 포함할 수 있습니다.
 *
 * @author JBD Backend Team
 * @version 1.0
 * @since 2025-01-26
 */
@Data
@Builder
public class AdminDashboardResponse {

    /**
     * 대시보드 데이터
     */
    private AdminDashboardDto dashboardData;

    /**
     * 관리자 권한으로 조회된 추가 정보
     */
    private String adminNote;

    /**
     * 조회 시간
     */
    private String timestamp;
}