export interface PaymentRequest {
  amount: number;
  itemName: string;
  orderId: string;
  userId: number;
  planType: 'FREE_ACADEMY' | 'PAID_MONTHLY';
  academyCouponCode?: string;
}

export interface PaymentResponse {
  success: boolean;
  message: string;
  data?: {
    subscriptionId: number;
    planType: string;
    startDate: string;
    endDate: string;
  };
}

export interface SubscriptionInfo {
  id: number;
  planType: 'FREE_ACADEMY' | 'PAID_MONTHLY';
  status: 'ACTIVE' | 'EXPIRED' | 'CANCELLED';
  startDate: string;
  endDate: string;
  amount?: number;
  academyName?: string;
  academyVerified?: boolean;
}

export interface KakaoPayReadyResponse {
  tid: string;
  next_redirect_pc_url: string;
  next_redirect_mobile_url: string;
  android_app_scheme: string;
  ios_app_scheme: string;
  created_at: string;
}

export interface KakaoPayApprovalResponse {
  aid: string;
  tid: string;
  cid: string;
  sid?: string;
  partner_order_id: string;
  partner_user_id: string;
  payment_method_type: string;
  amount: {
    total: number;
    tax_free: number;
    vat: number;
    point: number;
    discount: number;
  };
  card_info?: {
    kakaopay_purchase_corp: string;
    kakaopay_purchase_corp_code: string;
    kakaopay_issuer_corp: string;
    kakaopay_issuer_corp_code: string;
    bin: string;
    card_type: string;
    install_month: string;
    approved_id: string;
    card_mid: string;
    interest_free_install: string;
    card_item_code: string;
  };
  item_name: string;
  item_code: string;
  quantity: number;
  created_at: string;
  approved_at: string;
  payload: string;
}