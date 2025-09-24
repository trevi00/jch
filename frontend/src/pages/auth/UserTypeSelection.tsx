import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { apiClient } from '@/services/api'

export default function UserTypeSelection() {
  const navigate = useNavigate()
  const [selectedType, setSelectedType] = useState<'GENERAL' | 'COMPANY' | null>(null)
  const [isLoading, setIsLoading] = useState(false)

  const handleGoogleLogin = async () => {
    if (!selectedType) {
      alert('사용자 유형을 선택해주세요.')
      return
    }

    setIsLoading(true)
    try {
      const response = await apiClient.getGoogleAuthUrl(
        window.location.origin + '/auth/callback',
        selectedType,
        'SIGNUP'
      )
      
      if (response.success) {
        window.location.href = response.data
      } else {
        alert('구글 로그인 URL 생성에 실패했습니다.')
      }
    } catch (error) {
      console.error('Google login error:', error)
      alert('구글 로그인 중 오류가 발생했습니다.')
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full space-y-8">
        <div>
          <h2 className="mt-6 text-center text-3xl font-extrabold text-gray-900">
            사용자 유형 선택
          </h2>
          <p className="mt-2 text-center text-sm text-gray-600">
            회원가입할 계정 유형을 선택해주세요
          </p>
        </div>
        
        <div className="space-y-4">
          {/* 일반 사용자 선택 */}
          <div 
            className={`relative cursor-pointer rounded-lg border p-6 shadow-sm focus:outline-none ${
              selectedType === 'GENERAL' 
                ? 'border-primary-500 ring-2 ring-primary-500 bg-primary-50' 
                : 'border-gray-300 hover:border-gray-400'
            }`}
            onClick={() => setSelectedType('GENERAL')}
          >
            <div className="flex items-start">
              <div className="flex-shrink-0">
                <div className={`h-5 w-5 rounded-full border-2 ${
                  selectedType === 'GENERAL' 
                    ? 'border-primary-500 bg-primary-500' 
                    : 'border-gray-300'
                }`}>
                  {selectedType === 'GENERAL' && (
                    <div className="h-full w-full rounded-full bg-white scale-50"></div>
                  )}
                </div>
              </div>
              <div className="ml-3">
                <h3 className="text-lg font-medium text-gray-900">
                  일반 사용자
                </h3>
                <p className="text-sm text-gray-500">
                  취업을 준비하는 개인 사용자입니다. 이력서 작성, 면접 준비, 취업 정보 확인 등의 서비스를 이용할 수 있습니다.
                </p>
              </div>
            </div>
          </div>

          {/* 기업 사용자 선택 */}
          <div 
            className={`relative cursor-pointer rounded-lg border p-6 shadow-sm focus:outline-none ${
              selectedType === 'COMPANY' 
                ? 'border-primary-500 ring-2 ring-primary-500 bg-primary-50' 
                : 'border-gray-300 hover:border-gray-400'
            }`}
            onClick={() => setSelectedType('COMPANY')}
          >
            <div className="flex items-start">
              <div className="flex-shrink-0">
                <div className={`h-5 w-5 rounded-full border-2 ${
                  selectedType === 'COMPANY' 
                    ? 'border-primary-500 bg-primary-500' 
                    : 'border-gray-300'
                }`}>
                  {selectedType === 'COMPANY' && (
                    <div className="h-full w-full rounded-full bg-white scale-50"></div>
                  )}
                </div>
              </div>
              <div className="ml-3">
                <h3 className="text-lg font-medium text-gray-900">
                  기업 사용자
                </h3>
                <p className="text-sm text-gray-500">
                  채용을 진행하는 기업 담당자입니다. 구인 공고 등록, 지원자 관리, 기업 정보 관리 등의 서비스를 이용할 수 있습니다.
                </p>
              </div>
            </div>
          </div>
        </div>

        <div>
          <button
            type="button"
            disabled={!selectedType || isLoading}
            onClick={handleGoogleLogin}
            className={`group relative w-full flex justify-center py-3 px-4 border border-transparent text-sm font-medium rounded-md text-white focus:outline-none focus:ring-2 focus:ring-offset-2 ${
              !selectedType || isLoading
                ? 'bg-gray-400 cursor-not-allowed'
                : 'bg-primary-600 hover:bg-primary-700 focus:ring-primary-500'
            }`}
          >
            <div className="flex items-center">
              <svg className="w-5 h-5 mr-2" viewBox="0 0 24 24">
                <path fill="currentColor" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
                <path fill="currentColor" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
                <path fill="currentColor" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
                <path fill="currentColor" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
              </svg>
              {isLoading ? '처리 중...' : 'Google로 계속하기'}
            </div>
          </button>

          <div className="mt-4 text-center">
            <button
              type="button"
              onClick={() => navigate('/login')}
              className="text-sm text-primary-600 hover:text-primary-500"
            >
              일반 로그인으로 돌아가기
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}