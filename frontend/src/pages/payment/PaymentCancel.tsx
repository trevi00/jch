import { useNavigate } from 'react-router-dom';
import { XCircle, ArrowLeft, CreditCard } from 'lucide-react';

export default function PaymentCancel() {
  const navigate = useNavigate();

  const handleGoToSettings = () => {
    navigate('/settings');
  };

  const handleGoToHome = () => {
    navigate('/');
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="max-w-md mx-auto bg-white rounded-lg shadow-lg p-8 text-center">
        <div className="w-16 h-16 bg-yellow-100 rounded-full flex items-center justify-center mx-auto mb-4">
          <XCircle className="w-8 h-8 text-yellow-600" />
        </div>

        <h1 className="text-2xl font-bold text-gray-900 mb-2">결제 취소</h1>
        <p className="text-gray-600 mb-6">
          결제가 취소되었습니다. 언제든지 다시 시도하실 수 있습니다.
        </p>

        <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-6">
          <p className="text-sm text-blue-800">
            💡 <strong>솔데스크 학원 소속이신가요?</strong><br />
            학원 이메일로 3개월 무료 구독을 신청하실 수 있습니다!
          </p>
        </div>

        <div className="space-y-3">
          <button
            onClick={handleGoToSettings}
            className="w-full btn-primary flex items-center justify-center"
          >
            <CreditCard className="w-4 h-4 mr-2" />
            다시 시도하기
          </button>
          <button
            onClick={handleGoToHome}
            className="w-full btn-outline flex items-center justify-center"
          >
            <ArrowLeft className="w-4 h-4 mr-2" />
            홈으로 돌아가기
          </button>
        </div>
      </div>
    </div>
  );
}