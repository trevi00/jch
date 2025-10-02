package org.jbd.backend.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jbd.backend.payment.config.KakaoPayConfig;
import org.jbd.backend.payment.domain.Subscription;
import org.jbd.backend.payment.domain.enums.PlanType;
import org.jbd.backend.payment.domain.enums.SubscriptionStatus;
import org.jbd.backend.payment.dto.KakaoPayReadyResponseDto;
import org.jbd.backend.payment.dto.PaymentRequestDto;
import org.jbd.backend.payment.repository.SubscriptionRepository;
import org.jbd.backend.user.domain.User;
import org.jbd.backend.user.repository.UserRepository;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final KakaoPayConfig kakaoPayConfig;
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 카카오페이 결제 준비
     */
    @Transactional
    public KakaoPayReadyResponseDto readyPayment(PaymentRequestDto request) {
        log.info("카카오페이 결제 준비 시작: {}", request);

        // 사용자 조회
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 기존 활성 구독 체크
        if (subscriptionRepository.existsByUserIdAndStatus(user.getId(), SubscriptionStatus.ACTIVE)) {
            throw new RuntimeException("이미 활성화된 구독이 있습니다.");
        }

        // 구독 정보 미리 생성 (결제 성공 시 활성화)
        Subscription subscription = createSubscription(user, request);
        subscriptionRepository.save(subscription);

        // 카카오페이 결제 준비 요청
        KakaoPayReadyResponseDto response = callKakaoPayReady(request, subscription);

        // TID 저장
        subscription.setKakaoTid(response.getTid());
        subscriptionRepository.save(subscription);

        log.info("카카오페이 결제 준비 완료: TID={}", response.getTid());
        return response;
    }

    /**
     * 카카오페이 결제 승인
     */
    @Transactional
    public Map<String, Object> approvePayment(String pgToken, String orderId, String tid) {
        log.info("카카오페이 결제 승인 시작: orderId={}, tid={}", orderId, tid);

        // 구독 정보 조회
        Subscription subscription = subscriptionRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("결제 정보를 찾을 수 없습니다."));

        // 카카오페이 승인 요청
        Map<String, Object> approvalResponse = callKakaoPayApprove(pgToken, tid, orderId, subscription);

        // 구독 활성화
        subscription.activate();
        subscriptionRepository.save(subscription);

        // 응답 데이터 구성
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "결제가 완료되었습니다.");
        response.put("itemName", subscription.getPlanType().getDescription());
        response.put("amount", subscription.getAmount());
        response.put("startDate", subscription.getStartDate());
        response.put("endDate", subscription.getEndDate());

        log.info("카카오페이 결제 승인 완료: subscriptionId={}", subscription.getId());
        return response;
    }

    /**
     * 현재 구독 정보 조회
     */
    public Optional<Subscription> getCurrentSubscription(Long userId) {
        return subscriptionRepository.findTopByUserIdAndStatusOrderByCreatedAtDesc(userId, SubscriptionStatus.ACTIVE);
    }

    /**
     * 구독 취소
     */
    @Transactional
    public void cancelSubscription(Long subscriptionId, Long userId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("구독 정보를 찾을 수 없습니다."));

        if (!subscription.getUser().getId().equals(userId)) {
            throw new RuntimeException("구독을 취소할 권한이 없습니다.");
        }

        subscription.cancel();
        subscriptionRepository.save(subscription);
    }

    /**
     * 학원 소속 확인 (쿠폰 코드 방식)
     */
    public Map<String, Object> checkAcademyEligibility(String couponCode) {
        Map<String, Object> result = new HashMap<>();

        // 솔데스크 학원 쿠폰 코드 체크
        if (couponCode != null && (couponCode.equals("soldeskjongro") ||
                                   couponCode.equals("soldesk2024") ||
                                   couponCode.equals("soldesk"))) {
            result.put("eligible", true);
            result.put("academyName", "솔데스크 학원");
        } else {
            result.put("eligible", false);
        }

        return result;
    }

    /**
     * 무료 학원 구독 생성 (카카오페이 없이)
     */
    @Transactional
    public Map<String, Object> createFreeAcademySubscription(Long userId, String couponCode) {
        log.info("무료 학원 구독 생성 시작: userId={}, couponCode={}", userId, couponCode);

        // 쿠폰 코드 검증
        Map<String, Object> eligibilityCheck = checkAcademyEligibility(couponCode);
        if (!(boolean) eligibilityCheck.get("eligible")) {
            throw new RuntimeException("유효하지 않은 학원 쿠폰 코드입니다.");
        }

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 기존 활성 구독 체크
        if (subscriptionRepository.existsByUserIdAndStatus(user.getId(), SubscriptionStatus.ACTIVE)) {
            throw new RuntimeException("이미 활성화된 구독이 있습니다.");
        }

        // 무료 구독 생성
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusMonths(3); // 3개월 무료

        Subscription subscription = new Subscription(user, PlanType.FREE_ACADEMY, startDate, endDate);
        subscription.setAmount(BigDecimal.ZERO);
        subscription.setAcademyInfo("솔데스크 학원", couponCode);
        subscription.activate(); // 즉시 활성화

        subscriptionRepository.save(subscription);

        // 응답 생성
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "무료 학원 구독이 활성화되었습니다.");
        response.put("subscriptionId", subscription.getId());
        response.put("planType", subscription.getPlanType().name());
        response.put("startDate", subscription.getStartDate());
        response.put("endDate", subscription.getEndDate());
        response.put("academyName", subscription.getAcademyName());

        log.info("무료 학원 구독 생성 완료: subscriptionId={}", subscription.getId());
        return response;
    }

    /**
     * 구독 생성
     */
    private Subscription createSubscription(User user, PaymentRequestDto request) {
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate;

        if (request.getPlanType() == PlanType.FREE_ACADEMY) {
            endDate = startDate.plusMonths(3); // 3개월 무료
        } else {
            endDate = startDate.plusMonths(1); // 1개월
        }

        Subscription subscription = new Subscription(user, request.getPlanType(), startDate, endDate);
        subscription.setOrderId(request.getOrderId());
        subscription.setAmount(request.getAmount());

        if (request.getPlanType() == PlanType.FREE_ACADEMY && request.getAcademyCouponCode() != null) {
            subscription.setAcademyInfo("솔데스크 학원", request.getAcademyCouponCode());
        }

        return subscription;
    }

    /**
     * 카카오페이 결제 준비 API 호출
     */
    private KakaoPayReadyResponseDto callKakaoPayReady(PaymentRequestDto request, Subscription subscription) {
        String url = kakaoPayConfig.getBaseUrl() + "/v1/payment/ready";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", kakaoPayConfig.getAuthorizationHeader());
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("cid", kakaoPayConfig.getCid());
        params.add("partner_order_id", request.getOrderId());
        params.add("partner_user_id", String.valueOf(request.getUserId()));
        params.add("item_name", request.getItemName());
        params.add("quantity", "1");
        params.add("total_amount", String.valueOf(request.getAmount().intValue()));
        params.add("vat_amount", "0");
        params.add("tax_free_amount", "0");
        params.add("approval_url", kakaoPayConfig.getSuccessUrl());
        params.add("fail_url", kakaoPayConfig.getFailUrl());
        params.add("cancel_url", kakaoPayConfig.getCancelUrl());

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);

        try {
            return restTemplate.postForObject(url, entity, KakaoPayReadyResponseDto.class);
        } catch (Exception e) {
            log.error("카카오페이 결제 준비 실패", e);
            throw new RuntimeException("카카오페이 결제 준비에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 카카오페이 결제 승인 API 호출
     */
    private Map<String, Object> callKakaoPayApprove(String pgToken, String tid, String orderId, Subscription subscription) {
        String url = kakaoPayConfig.getBaseUrl() + "/v1/payment/approve";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", kakaoPayConfig.getAuthorizationHeader());
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("cid", kakaoPayConfig.getCid());
        params.add("tid", tid);
        params.add("partner_order_id", orderId);
        params.add("partner_user_id", String.valueOf(subscription.getUser().getId()));
        params.add("pg_token", pgToken);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);

        try {
            return restTemplate.postForObject(url, entity, Map.class);
        } catch (Exception e) {
            log.error("카카오페이 결제 승인 실패", e);
            throw new RuntimeException("카카오페이 결제 승인에 실패했습니다: " + e.getMessage());
        }
    }
}