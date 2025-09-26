import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-hot-toast';
import { apiClient } from '../../services/api';

const AdminPromote: React.FC = () => {
  const [formData, setFormData] = useState({
    email: '',
    secretKey: ''
  });
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!formData.email || !formData.secretKey) {
      toast.error('이메일과 시크릿 키를 모두 입력해주세요.');
      return;
    }

    setIsLoading(true);

    try {
      await apiClient.promoteToAdmin(formData.email, formData.secretKey);
      toast.success(`${formData.email}이 관리자로 승격되었습니다.`);

      // 폼 초기화
      setFormData({
        email: '',
        secretKey: ''
      });

      // 사용자 관리 페이지로 이동
      navigate('/admin/users');
    } catch (error: any) {
      console.error('Admin promotion failed:', error);
      toast.error(error.response?.data?.message || '관리자 승격에 실패했습니다.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md mx-auto">
        <div className="bg-white py-8 px-4 shadow sm:rounded-lg sm:px-10">
          <div className="sm:mx-auto sm:w-full sm:max-w-md">
            <h2 className="mt-6 text-center text-3xl font-extrabold text-gray-900">
              관리자 승격
            </h2>
            <p className="mt-2 text-center text-sm text-gray-600">
              사용자를 관리자로 승격시킵니다
            </p>
          </div>

          <div className="mt-8">
            <form className="space-y-6" onSubmit={handleSubmit}>
              <div>
                <label htmlFor="email" className="block text-sm font-medium text-gray-700">
                  이메일 주소
                </label>
                <div className="mt-1">
                  <input
                    id="email"
                    name="email"
                    type="email"
                    autoComplete="email"
                    required
                    value={formData.email}
                    onChange={handleInputChange}
                    className="appearance-none block w-full px-3 py-2 border border-gray-300 rounded-md placeholder-gray-400 focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                    placeholder="승격할 사용자의 이메일을 입력하세요"
                  />
                </div>
              </div>

              <div>
                <label htmlFor="secretKey" className="block text-sm font-medium text-gray-700">
                  시크릿 키
                </label>
                <div className="mt-1">
                  <input
                    id="secretKey"
                    name="secretKey"
                    type="password"
                    required
                    value={formData.secretKey}
                    onChange={handleInputChange}
                    className="appearance-none block w-full px-3 py-2 border border-gray-300 rounded-md placeholder-gray-400 focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                    placeholder="관리자 시크릿 키를 입력하세요"
                  />
                </div>
                <p className="mt-1 text-xs text-gray-500">
                  관리자 승격을 위한 시크릿 키가 필요합니다
                </p>
              </div>

              <div>
                <button
                  type="submit"
                  disabled={isLoading}
                  className={`group relative w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white ${
                    isLoading
                      ? 'bg-gray-400 cursor-not-allowed'
                      : 'bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500'
                  }`}
                >
                  {isLoading ? (
                    <div className="flex items-center">
                      <div className="animate-spin -ml-1 mr-3 h-5 w-5 text-white">
                        <svg className="animate-spin h-5 w-5" viewBox="0 0 24 24">
                          <circle
                            className="opacity-25"
                            cx="12"
                            cy="12"
                            r="10"
                            stroke="currentColor"
                            strokeWidth="4"
                            fill="none"
                          />
                          <path
                            className="opacity-75"
                            fill="currentColor"
                            d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                          />
                        </svg>
                      </div>
                      처리 중...
                    </div>
                  ) : (
                    '관리자로 승격'
                  )}
                </button>
              </div>

              <div className="text-center">
                <button
                  type="button"
                  onClick={() => navigate('/admin')}
                  className="text-sm text-blue-600 hover:text-blue-500"
                >
                  관리자 대시보드로 돌아가기
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AdminPromote;