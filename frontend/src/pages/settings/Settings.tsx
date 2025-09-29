import { useState, useEffect } from "react";
import { useMutation, useQueryClient, useQuery } from "@tanstack/react-query";
import {
  Settings as SettingsIcon,
  Lock,
  Bell,
  User,
  Shield,
  LogOut,
  CreditCard,
  CheckCircle,
  XCircle,
  Calendar,
  DollarSign,
} from "lucide-react";
import { useAuthStore } from "@/hooks/useAuthStore";
import { apiClient } from "@/services/api";
import { paymentService } from "@/services/paymentService";
import { SubscriptionInfo } from "@/types/payment";

export default function Settings() {
  const { user, logout } = useAuthStore();
  const queryClient = useQueryClient();
  const [activeTab, setActiveTab] = useState("profile");

  const [profileData, setProfileData] = useState({
    name: user?.name || "",
    email: user?.email || "",
    phoneNumber: user?.phoneNumber || "",
  });

  const [passwordData, setPasswordData] = useState({
    currentPassword: "",
    newPassword: "",
    confirmPassword: "",
  });

  const updateProfileMutation = useMutation({
    mutationFn: (data: any) => apiClient.updateProfile(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["profile"] });
      alert("프로필이 업데이트되었습니다.");
    },
  });

  const changePasswordMutation = useMutation({
    mutationFn: (data: any) =>
      fetch("/api/users/change-password", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${localStorage.getItem("accessToken")}`,
        },
        body: JSON.stringify(data),
      }),
    onSuccess: () => {
      setPasswordData({
        currentPassword: "",
        newPassword: "",
        confirmPassword: "",
      });
      alert("비밀번호가 변경되었습니다.");
    },
  });

  const handleProfileSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    updateProfileMutation.mutate(profileData);
  };

  const handlePasswordSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (passwordData.newPassword !== passwordData.confirmPassword) {
      alert("새 비밀번호가 일치하지 않습니다.");
      return;
    }
    changePasswordMutation.mutate({
      currentPassword: passwordData.currentPassword,
      newPassword: passwordData.newPassword,
    });
  };

  const handleLogout = () => {
    if (window.confirm("로그아웃 하시겠습니까?")) {
      logout();
      window.location.href = "/login";
    }
  };

  // 학원 소속 확인
  const handleCheckAcademy = async () => {
    if (!academyCouponCode) return;

    setIsCheckingAcademy(true);
    try {
      const result = await paymentService.checkAcademyEligibility(academyCouponCode);
      setAcademyCheckResult(result);
    } catch (error) {
      alert("학원 소속 확인 중 오류가 발생했습니다.");
    } finally {
      setIsCheckingAcademy(false);
    }
  };

  // 학원 3개월 무료 구독 신청
  const handleAcademySubscription = async () => {
    if (!academyCheckResult?.eligible || !user || !academyCouponCode) return;

    try {
      const result = await paymentService.createFreeAcademySubscription(academyCouponCode);

      if (result.success) {
        alert("무료 학원 구독이 성공적으로 활성화되었습니다!");
        // 구독 정보 새로고침
        fetchSubscriptionInfo();
        // 상태 리셋
        setAcademyCouponCode('');
        setAcademyCheckResult(null);
      } else {
        alert(result.message || "무료 구독 활성화에 실패했습니다.");
      }
    } catch (error) {
      console.error('무료 구독 신청 오류:', error);
      alert("무료 구독 신청 중 오류가 발생했습니다.");
    }
  };

  // 월 1원 정액제 결제
  const handleMonthlyPayment = async () => {
    if (!user) return;

    try {
      const orderId = paymentService.generateOrderId();
      await paymentService.initiatePayment({
        amount: 1,
        itemName: "JCH 월 정액제",
        orderId,
        userId: user.id,
        planType: "PAID_MONTHLY",
      });
    } catch (error) {
      alert("결제 요청 중 오류가 발생했습니다.");
    }
  };

  // 구독 취소
  const cancelSubscriptionMutation = useMutation({
    mutationFn: (subscriptionId: number) => paymentService.cancelSubscription(subscriptionId),
    onSuccess: () => {
      alert("구독이 취소되었습니다.");
      refetchSubscription();
    },
    onError: () => {
      alert("구독 취소 중 오류가 발생했습니다.");
    },
  });

  const [academyCouponCode, setAcademyCouponCode] = useState("");
  const [isCheckingAcademy, setIsCheckingAcademy] = useState(false);
  const [academyCheckResult, setAcademyCheckResult] = useState<{eligible: boolean; academyName?: string} | null>(null);

  // 구독 정보 조회
  const { data: subscription, refetch: refetchSubscription } = useQuery({
    queryKey: ["subscription"],
    queryFn: () => paymentService.getCurrentSubscription(),
  });

  const tabs = [
    { id: "profile", label: "프로필", icon: User },
    { id: "security", label: "보안", icon: Shield },
    { id: "notifications", label: "알림", icon: Bell },
    { id: "account", label: "계정", icon: Lock },
    { id: "subscription", label: "구독 관리", icon: CreditCard },
  ];

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">설정</h1>
        <p className="text-gray-600">계정 및 개인정보를 관리하세요</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
        {/* Sidebar */}
        <div className="lg:col-span-1">
          <div className="card">
            <div className="card-content">
              <nav className="space-y-1">
                {tabs.map((tab) => {
                  const Icon = tab.icon;
                  return (
                    <button
                      key={tab.id}
                      onClick={() => setActiveTab(tab.id)}
                      className={`w-full flex items-center px-3 py-2 text-sm font-medium rounded-md transition-colors ${
                        activeTab === tab.id
                          ? "bg-primary-100 text-primary-700"
                          : "text-gray-600 hover:text-gray-900 hover:bg-gray-50"
                      }`}
                    >
                      <Icon className="w-4 h-4 mr-3" />
                      {tab.label}
                    </button>
                  );
                })}
              </nav>
            </div>
          </div>
        </div>

        {/* Main Content */}
        <div className="lg:col-span-3">
          <div className="card">
            <div className="card-content">
              {activeTab === "profile" && (
                <div>
                  <h2 className="text-lg font-semibold mb-4">프로필 설정</h2>
                  <form onSubmit={handleProfileSubmit} className="space-y-4">
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        이름
                      </label>
                      <input
                        type="text"
                        value={profileData.name}
                        onChange={(e) =>
                          setProfileData({
                            ...profileData,
                            name: e.target.value,
                          })
                        }
                        className="input"
                      />
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        이메일
                      </label>
                      <input
                        type="email"
                        value={profileData.email}
                        onChange={(e) =>
                          setProfileData({
                            ...profileData,
                            email: e.target.value,
                          })
                        }
                        className="input"
                        disabled
                      />
                      <p className="text-sm text-gray-500 mt-1">
                        이메일은 변경할 수 없습니다.
                      </p>
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        연락처
                      </label>
                      <input
                        type="tel"
                        value={profileData.phoneNumber}
                        onChange={(e) =>
                          setProfileData({
                            ...profileData,
                            phoneNumber: e.target.value,
                          })
                        }
                        className="input"
                      />
                    </div>

                    <button type="submit" className="btn-primary">
                      저장하기
                    </button>
                  </form>
                </div>
              )}

              {activeTab === "security" && (
                <div>
                  <h2 className="text-lg font-semibold mb-4">비밀번호 변경</h2>
                  <form onSubmit={handlePasswordSubmit} className="space-y-4">
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        현재 비밀번호
                      </label>
                      <input
                        type="password"
                        value={passwordData.currentPassword}
                        onChange={(e) =>
                          setPasswordData({
                            ...passwordData,
                            currentPassword: e.target.value,
                          })
                        }
                        className="input"
                        required
                      />
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        새 비밀번호
                      </label>
                      <input
                        type="password"
                        value={passwordData.newPassword}
                        onChange={(e) =>
                          setPasswordData({
                            ...passwordData,
                            newPassword: e.target.value,
                          })
                        }
                        className="input"
                        required
                      />
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        새 비밀번호 확인
                      </label>
                      <input
                        type="password"
                        value={passwordData.confirmPassword}
                        onChange={(e) =>
                          setPasswordData({
                            ...passwordData,
                            confirmPassword: e.target.value,
                          })
                        }
                        className="input"
                        required
                      />
                    </div>

                    <button type="submit" className="btn-primary">
                      비밀번호 변경
                    </button>
                  </form>
                </div>
              )}

              {activeTab === "notifications" && (
                <div>
                  <h2 className="text-lg font-semibold mb-4">알림 설정</h2>
                  <div className="space-y-4">
                    <div className="flex items-center justify-between">
                      <div>
                        <h3 className="font-medium">이메일 알림</h3>
                        <p className="text-sm text-gray-600">
                          새로운 채용 공고 및 면접 일정 알림
                        </p>
                      </div>
                      <label className="switch">
                        <input type="checkbox" defaultChecked />
                        <span className="slider"></span>
                      </label>
                    </div>

                    <div className="flex items-center justify-between">
                      <div>
                        <h3 className="font-medium">푸시 알림</h3>
                        <p className="text-sm text-gray-600">
                          실시간 알림 받기
                        </p>
                      </div>
                      <label className="switch">
                        <input type="checkbox" />
                        <span className="slider"></span>
                      </label>
                    </div>
                  </div>
                </div>
              )}

              {activeTab === "account" && (
                <div>
                  <h2 className="text-lg font-semibold mb-4">계정 관리</h2>
                  <div className="space-y-6">
                    <div className="p-4 bg-yellow-50 rounded-lg">
                      <h3 className="font-medium text-yellow-800 mb-2">
                        계정 삭제
                      </h3>
                      <p className="text-sm text-yellow-700 mb-3">
                        계정을 삭제하면 모든 데이터가 영구적으로 삭제되며 복구할
                        수 없습니다.
                      </p>
                      <button className="btn-outline text-red-600 border-red-600 hover:bg-red-50">
                        계정 삭제
                      </button>
                    </div>

                    <div className="border-t pt-6">
                      <button
                        onClick={handleLogout}
                        className="flex items-center text-red-600 hover:text-red-700"
                      >
                        <LogOut className="w-4 h-4 mr-2" />
                        로그아웃
                      </button>
                    </div>
                  </div>
                </div>
              )}
              {activeTab === "subscription" && (
                <div>
                  <h2 className="text-lg font-semibold mb-6">구독 관리</h2>

                  {/* 현재 구독 상태 */}
                  {subscription ? (
                    <div className="bg-green-50 border border-green-200 rounded-lg p-4 mb-6">
                      <div className="flex items-center justify-between">
                        <div className="flex items-center">
                          <CheckCircle className="w-5 h-5 text-green-600 mr-2" />
                          <div>
                            <h3 className="font-medium text-green-800">
                              {subscription.planType === 'FREE_ACADEMY' ? '솔데스크 학원 무료 구독' : '월 정액제'}
                            </h3>
                            <p className="text-sm text-green-600">
                              {new Date(subscription.startDate).toLocaleDateString()} ~ {new Date(subscription.endDate).toLocaleDateString()}
                            </p>
                          </div>
                        </div>
                        <div className="text-right">
                          <p className="font-medium text-green-800">
                            {subscription.planType === 'FREE_ACADEMY' ? '무료' : '월 1원'}
                          </p>
                          <p className="text-sm text-green-600">
                            상태: {subscription.status === 'ACTIVE' ? '활성' : '만료'}
                          </p>
                        </div>
                      </div>
                      {subscription.status === 'ACTIVE' && (
                        <button
                          onClick={() => cancelSubscriptionMutation.mutate(subscription.id)}
                          disabled={cancelSubscriptionMutation.isPending}
                          className="mt-4 btn-outline text-red-600 border-red-600 hover:bg-red-50"
                        >
                          {cancelSubscriptionMutation.isPending ? '취소 중...' : '구독 취소'}
                        </button>
                      )}
                    </div>
                  ) : (
                    <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4 mb-6">
                      <div className="flex items-center">
                        <XCircle className="w-5 h-5 text-yellow-600 mr-2" />
                        <p className="text-yellow-800">현재 활성화된 구독이 없습니다.</p>
                      </div>
                    </div>
                  )}

                  {/* 솔데스크 학원 무료 구독 */}
                  <div className="bg-blue-50 border border-blue-200 rounded-lg p-6 mb-6">
                    <h3 className="text-lg font-medium text-blue-800 mb-4">
                      🎓 솔데스크 학원 소속 확인 (3개월 무료)
                    </h3>

                    <div className="space-y-4">
                      <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">
                          학원 쿠폰 코드
                        </label>
                        <div className="flex space-x-2">
                          <input
                            type="text"
                            value={academyCouponCode}
                            onChange={(e) => setAcademyCouponCode(e.target.value)}
                            placeholder="soldeskjongro"
                            className="input flex-1"
                          />
                          <button
                            onClick={handleCheckAcademy}
                            disabled={isCheckingAcademy || !academyCouponCode}
                            className="btn-outline px-4"
                          >
                            {isCheckingAcademy ? '확인 중...' : '확인'}
                          </button>
                        </div>
                      </div>

                      {academyCheckResult && (
                        <div className={`p-3 rounded-lg ${
                          academyCheckResult.eligible
                            ? 'bg-green-100 text-green-800'
                            : 'bg-red-100 text-red-800'
                        }`}>
                          {academyCheckResult.eligible ? (
                            <div>
                              <p className="font-medium">✅ 솔데스크 학원 소속이 확인되었습니다!</p>
                              <p className="text-sm">학원명: {academyCheckResult.academyName}</p>
                              <button
                                onClick={handleAcademySubscription}
                                className="mt-2 btn-primary"
                              >
                                3개월 무료 구독 신청
                              </button>
                            </div>
                          ) : (
                            <p>❌ 솔데스크 학원 소속을 확인할 수 없습니다.</p>
                          )}
                        </div>
                      )}
                    </div>
                  </div>

                  {/* 월 정액제 */}
                  <div className="bg-white border border-gray-200 rounded-lg p-6">
                    <h3 className="text-lg font-medium text-gray-900 mb-4">
                      💳 월 정액제 (일반 사용자)
                    </h3>

                    <div className="space-y-4">
                      <div className="flex items-center justify-between">
                        <div>
                          <p className="font-medium">JCH 월 정액제</p>
                          <p className="text-sm text-gray-600">
                            모든 AI 기능을 제한 없이 이용할 수 있습니다.
                          </p>
                        </div>
                        <div className="text-right">
                          <p className="text-2xl font-bold text-primary-600">₩1</p>
                          <p className="text-sm text-gray-600">/ 월</p>
                        </div>
                      </div>

                      <div className="bg-gray-50 p-4 rounded-lg">
                        <h4 className="font-medium mb-2">포함된 기능:</h4>
                        <ul className="text-sm text-gray-600 space-y-1">
                          <li>• AI 면접 연습 (무제한)</li>
                          <li>• 자기소개서 작성 도우미</li>
                          <li>• 번역 서비스</li>
                          <li>• 이미지 생성</li>
                          <li>• 감정 분석</li>
                          <li>• 챗봇 서비스</li>
                        </ul>
                      </div>

                      <button
                        onClick={handleMonthlyPayment}
                        disabled={subscription?.status === 'ACTIVE'}
                        className="w-full btn-primary flex items-center justify-center"
                      >
                        <DollarSign className="w-4 h-4 mr-2" />
                        카카오페이로 결제하기
                      </button>
                    </div>
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
