package org.jbd.backend.payment.dto;

import lombok.Data;
import org.jbd.backend.payment.domain.enums.PlanType;

import java.math.BigDecimal;

@Data
public class PaymentRequestDto {
    private BigDecimal amount;
    private String itemName;
    private String orderId;
    private Long userId;
    private PlanType planType;
    private String academyCouponCode;
}