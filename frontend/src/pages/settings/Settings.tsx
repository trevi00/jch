import { useState } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import {
  Settings as SettingsIcon,
  Lock,
  Bell,
  User,
  Shield,
  LogOut,
  CreditCard,
} from "lucide-react";
import { useAuthStore } from "@/hooks/useAuthStore";
import { apiClient } from "@/services/api";

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

  const tabs = [
    { id: "profile", label: "프로필", icon: User },
    { id: "security", label: "보안", icon: Shield },
    { id: "notifications", label: "알림", icon: Bell },
    { id: "account", label: "계정", icon: Lock },
    { id: "credit", label: "요금", icon: CreditCard },
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
              {activeTab === "credit" && (
                <div>
                  <h2 className="text-lg font-semibold mb-4">카드 등록</h2>
                  {/* 카드 번호 입력 */}
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      카드 번호
                    </label>
                    <div className="flex space-x-2">
                      <input
                        type="text"
                        maxLength={4}
                        className="input w-16 text-center"
                        required
                      />
                      <input
                        type="text"
                        maxLength={4}
                        className="input w-16 text-center"
                        required
                      />
                      <input
                        type="text"
                        maxLength={4}
                        className="input w-16 text-center"
                        required
                      />
                      <input
                        type="text"
                        maxLength={4}
                        className="input w-16 text-center"
                        required
                      />
                    </div>
                  </div>

                  {/* 만료일 + CVC */}
                  <div className="flex flex-wrap sm:flex-nowrap space-x-4">
                    {/* 만료일 */}
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        만료일 (MM/YY)
                      </label>
                      <div className="flex space-x-2">
                        <input
                          type="text"
                          maxLength={2}
                          className="input w-14 text-center"
                          placeholder="MM"
                          required
                        />
                        <span className="mt-2">/</span>
                        <input
                          type="text"
                          maxLength={2}
                          className="input w-14 text-center"
                          placeholder="YY"
                          required
                        />
                      </div>
                    </div>
                  </div>

                  {/* CVC */}
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      CVC
                    </label>
                    <input
                      type="text"
                      maxLength={4}
                      className="input w-32 text-center"
                      placeholder="3자리"
                      required
                    />
                  </div>
                  {/* 은행 선택 */}
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      은행 선택
                    </label>
                    <select className="input w-full" required>
                      <option value="">은행을 선택하세요</option>
                      <option value="shinhan">신한은행</option>
                      <option value="kb">국민은행</option>
                      <option value="hana">하나은행</option>
                      <option value="woori">우리은행</option>
                      <option value="ibk">IBK기업은행</option>
                      <option value="kakao">카카오뱅크</option>
                      <option value="toss">토스뱅크</option>
                    </select>
                    
                  </div>


                  {/* 제출 버튼 */}
                  <button type="submit" className="btn-primary">
                    카드 등록
                  </button>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
