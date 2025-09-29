import { PaymentRequest, PaymentResponse, SubscriptionInfo, KakaoPayReadyResponse } from '@/types/payment';

const KAKAO_JAVASCRIPT_KEY = import.meta.env.VITE_KAKAO_JAVASCRIPT_KEY;
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '';

class PaymentService {
  private kakao: any = null;

  constructor() {
    this.initializeKakao();
  }

  private async initializeKakao() {
    // 카카오 SDK 동적 로드
    if (!window.Kakao) {
      await this.loadKakaoSDK();
    }

    if (window.Kakao && !window.Kakao.isInitialized()) {
      window.Kakao.init(KAKAO_JAVASCRIPT_KEY);
    }

    this.kakao = window.Kakao;
  }

  private loadKakaoSDK(): Promise<void> {
    return new Promise((resolve, reject) => {
      if (document.getElementById('kakao-js-sdk')) {
        resolve();
        return;
      }

      const script = document.createElement('script');
      script.id = 'kakao-js-sdk';
      script.src = 'https://developers.kakao.com/sdk/js/kakao.js';
      script.onload = () => resolve();
      script.onerror = () => reject(new Error('카카오 SDK 로드 실패'));
      document.head.appendChild(script);
    });
  }

  async initiatePayment(paymentData: PaymentRequest): Promise<void> {
    try {
      // 1. 결제 준비 요청
      const response = await fetch(`${API_BASE_URL}/api/payment/kakao/ready`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('accessToken')}`,
        },
        body: JSON.stringify(paymentData),
      });

      if (!response.ok) {
        throw new Error('결제 준비 요청 실패');
      }

      const readyData: KakaoPayReadyResponse = await response.json();

      // 2. 카카오페이 결제 페이지로 리다이렉트
      const isMobile = /Android|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent);
      const redirectUrl = isMobile ? readyData.next_redirect_mobile_url : readyData.next_redirect_pc_url;

      // TID를 세션에 저장
      sessionStorage.setItem('kakao_pay_tid', readyData.tid);
      sessionStorage.setItem('payment_order_id', paymentData.orderId);

      window.location.href = redirectUrl;
    } catch (error) {
      console.error('결제 요청 중 오류:', error);
      throw error;
    }
  }

  async checkAcademyEligibility(couponCode: string): Promise<{ eligible: boolean; academyName?: string }> {
    try {
      const response = await fetch(`${API_BASE_URL}/api/payment/academy/check`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('accessToken')}`,
        },
        body: JSON.stringify({ couponCode }),
      });

      if (!response.ok) {
        throw new Error('학원 소속 확인 실패');
      }

      return await response.json();
    } catch (error) {
      console.error('학원 소속 확인 중 오류:', error);
      return { eligible: false };
    }
  }

  async getCurrentSubscription(): Promise<SubscriptionInfo | null> {
    try {
      const response = await fetch(`${API_BASE_URL}/api/payment/subscription/current`, {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('accessToken')}`,
        },
      });

      if (!response.ok) {
        if (response.status === 404) {
          return null; // 구독 정보 없음
        }
        throw new Error('구독 정보 조회 실패');
      }

      return await response.json();
    } catch (error) {
      console.error('구독 정보 조회 중 오류:', error);
      return null;
    }
  }

  async cancelSubscription(subscriptionId: number): Promise<PaymentResponse> {
    try {
      const response = await fetch(`${API_BASE_URL}/api/payment/subscription/${subscriptionId}/cancel`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('accessToken')}`,
        },
      });

      if (!response.ok) {
        throw new Error('구독 취소 실패');
      }

      return await response.json();
    } catch (error) {
      console.error('구독 취소 중 오류:', error);
      throw error;
    }
  }

  async createFreeAcademySubscription(couponCode: string): Promise<PaymentResponse> {
    try {
      const response = await fetch(`${API_BASE_URL}/api/payment/academy/subscribe`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('accessToken')}`,
        },
        body: JSON.stringify({ couponCode }),
      });

      if (!response.ok) {
        throw new Error('무료 구독 생성 실패');
      }

      return await response.json();
    } catch (error) {
      console.error('무료 구독 생성 중 오류:', error);
      throw error;
    }
  }

  generateOrderId(): string {
    return `ORDER_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
  }
}

// 전역 타입 확장
declare global {
  interface Window {
    Kakao: any;
  }
}

export const paymentService = new PaymentService();