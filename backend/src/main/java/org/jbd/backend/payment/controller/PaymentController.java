package org.jbd.backend.payment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jbd.backend.payment.domain.Subscription;
import org.jbd.backend.payment.dto.KakaoPayReadyResponseDto;
import org.jbd.backend.payment.dto.PaymentRequestDto;
import org.jbd.backend.payment.service.PaymentService;
import org.jbd.backend.user.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;
    private final UserRepository userRepository;

    /**
     * 카카오페이 결제 준비
     */
    @PostMapping("/kakao/ready")
    public ResponseEntity<KakaoPayReadyResponseDto> readyPayment(
            @RequestBody PaymentRequestDto request,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("카카오페이 결제 준비 요청: {}", request);
        log.info("UserDetails: {}", userDetails);
        log.info("Request userId: {}", request.getUserId());

        try {
            KakaoPayReadyResponseDto response = paymentService.readyPayment(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("카카오페이 결제 준비 실패", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 카카오페이 결제 승인
     */
    @PostMapping("/kakao/approve")
    public ResponseEntity<Map<String, Object>> approvePayment(
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal UserDetails userDetails) {

        String pgToken = request.get("pgToken");
        String orderId = request.get("orderId");
        String tid = request.get("tid");

        log.info("카카오페이 결제 승인 요청: orderId={}, tid={}", orderId, tid);

        try {
            Map<String, Object> response = paymentService.approvePayment(pgToken, orderId, tid);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("카카오페이 결제 승인 실패", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 현재 구독 정보 조회
     */
    @GetMapping("/subscription/current")
    public ResponseEntity<Map<String, Object>> getCurrentSubscription(
            @AuthenticationPrincipal UserDetails userDetails) {

        // UserDetails에서 userId 추출 (실제 구현에 맞게 수정)
        Long userId = getUserIdFromUserDetails(userDetails);

        Optional<Subscription> subscription = paymentService.getCurrentSubscription(userId);

        if (subscription.isPresent()) {
            Subscription sub = subscription.get();
            Map<String, Object> response = new HashMap<>();
            response.put("id", sub.getId());
            response.put("planType", sub.getPlanType());
            response.put("status", sub.getStatus());
            response.put("startDate", sub.getStartDate());
            response.put("endDate", sub.getEndDate());
            response.put("amount", sub.getAmount());
            response.put("academyName", sub.getAcademyName());
            response.put("academyVerified", sub.getAcademyVerified());

            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 구독 취소
     */
    @PostMapping("/subscription/{subscriptionId}/cancel")
    public ResponseEntity<Map<String, Object>> cancelSubscription(
            @PathVariable Long subscriptionId,
            @AuthenticationPrincipal UserDetails userDetails) {

        // UserDetails에서 userId 추출
        Long userId = getUserIdFromUserDetails(userDetails);

        try {
            paymentService.cancelSubscription(subscriptionId, userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "구독이 취소되었습니다.");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("구독 취소 실패", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 학원 소속 확인 (쿠폰 코드)
     */
    @PostMapping("/academy/check")
    public ResponseEntity<Map<String, Object>> checkAcademyEligibility(
            @RequestBody Map<String, String> request) {

        String couponCode = request.get("couponCode");
        log.info("학원 소속 확인 요청: couponCode={}", couponCode);

        Map<String, Object> response = paymentService.checkAcademyEligibility(couponCode);
        return ResponseEntity.ok(response);
    }

    /**
     * 무료 학원 구독 생성 (카카오페이 없이)
     */
    @PostMapping("/academy/subscribe")
    public ResponseEntity<Map<String, Object>> createFreeAcademySubscription(
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal UserDetails userDetails) {

        String couponCode = request.get("couponCode");
        log.info("무료 학원 구독 생성 요청: couponCode={}", couponCode);

        // UserDetails에서 userId 추출
        Long userId = getUserIdFromUserDetails(userDetails);

        try {
            Map<String, Object> response = paymentService.createFreeAcademySubscription(userId, couponCode);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("무료 학원 구독 생성 실패", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * UserDetails에서 userId 추출
     */
    private Long getUserIdFromUserDetails(UserDetails userDetails) {
        // UserDetails.getUsername()은 이메일을 반환함
        String email = userDetails.getUsername();

        // UserRepository를 통해 이메일로 사용자 조회
        return userRepository.findByEmailAndIsDeletedFalse(email)
                .map(user -> user.getId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + email));
    }
}