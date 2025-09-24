package org.jbd.backend.dashboard.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RequestStatus {
    PENDING("처리 대기중"),
    APPROVED("승인됨"),
    REJECTED("거부됨"),
    COMPLETED("완료됨");

    private final String description;
}