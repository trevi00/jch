import { useEffect, useState } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { CheckCircle, Calendar, CreditCard } from 'lucide-react';

export default function PaymentSuccess() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [isProcessing, setIsProcessing] = useState(true);
  const [paymentInfo, setPaymentInfo] = useState<any>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const processPayment = async () => {
      const pgToken = searchParams.get('pg_token');
      const orderId = sessionStorage.getItem('payment_order_id');
      const tid = sessionStorage.getItem('kakao_pay_tid');

      if (!pgToken || !orderId || !tid) {
        setError('결제 정보가 올바르지 않습니다.');
        setIsProcessing(false);
        return;
      }

      try {
        const response = await fetch('/api/payment/kakao/approve', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${localStorage.getItem('accessToken')}`,
          },
          body: JSON.stringify({
            pgToken,
            orderId,
            tid,
          }),
        });

        if (!response.ok) {
          throw new Error('결제 승인 실패');
        }

        const result = await response.json();
        setPaymentInfo(result);

        // 세션 정리
        sessionStorage.removeItem('payment_order_id');
        sessionStorage.removeItem('kakao_pay_tid');
      } catch (error) {
        console.error('결제 승인 처리 중 오류:', error);
        setError('결제 승인 처리 중 오류가 발생했습니다.');
      } finally {
        setIsProcessing(false);
      }
    };

    processPayment();
  }, [searchParams]);

  const handleGoToSettings = () => {
    navigate('/settings');
  };

  const handleGoToHome = () => {
    navigate('/');
  };

  if (isProcessing) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600 mx-auto mb-4"></div>
          <p className="text-gray-600">결제를 처리하고 있습니다...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="max-w-md mx-auto bg-white rounded-lg shadow-lg p-8 text-center">
          <div className="w-16 h-16 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-4">
            <svg className="w-8 h-8 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            </svg>
          </div>
          <h1 className="text-2xl font-bold text-gray-900 mb-2">결제 실패</h1>
          <p className="text-gray-600 mb-6">{error}</p>
          <div className="space-y-3">
            <button
              onClick={handleGoToSettings}
              className="w-full btn-primary"
            >
              다시 시도하기
            </button>
            <button
              onClick={handleGoToHome}
              className="w-full btn-outline"
            >
              홈으로 돌아가기
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="max-w-md mx-auto bg-white rounded-lg shadow-lg p-8 text-center">
        <div className="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
          <CheckCircle className="w-8 h-8 text-green-600" />
        </div>

        <h1 className="text-2xl font-bold text-gray-900 mb-2">결제 완료!</h1>
        <p className="text-gray-600 mb-6">구독이 성공적으로 활성화되었습니다.</p>

        {paymentInfo && (
          <div className="bg-gray-50 rounded-lg p-4 mb-6 text-left">
            <h3 className="font-medium text-gray-900 mb-3">결제 정보</h3>
            <div className="space-y-2 text-sm">
              <div className="flex justify-between">
                <span className="text-gray-600">상품명:</span>
                <span className="font-medium">{paymentInfo.itemName}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">결제 금액:</span>
                <span className="font-medium">
                  {paymentInfo.amount === 0 ? '무료' : `₩${paymentInfo.amount.toLocaleString()}`}
                </span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">구독 기간:</span>
                <span className="font-medium">
                  {new Date(paymentInfo.startDate).toLocaleDateString()} ~ {new Date(paymentInfo.endDate).toLocaleDateString()}
                </span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">결제 방법:</span>
                <span className="font-medium">카카오페이</span>
              </div>
            </div>
          </div>
        )}

        <div className="space-y-3">
          <button
            onClick={handleGoToSettings}
            className="w-full btn-primary flex items-center justify-center"
          >
            <CreditCard className="w-4 h-4 mr-2" />
            구독 관리하기
          </button>
          <button
            onClick={handleGoToHome}
            className="w-full btn-outline"
          >
            홈으로 돌아가기
          </button>
        </div>
      </div>
    </div>
  );
}