/**
 * Login.tsx - 사용자 로그인 페이지 컴포넌트
 *
 * 🔧 사용 기술:
 * - React 18 (useState Hook, 함수형 컴포넌트)
 * - TypeScript (타입 안전성)
 * - React Hook Form (폼 관리 및 검증)
 * - Zod (스키마 기반 데이터 검증)
 * - React Router (페이지 네비게이션)
 * - Zustand (인증 상태 관리)
 * - Lucide React (아이콘)
 *
 * 📋 주요 기능:
 * - 이메일/비밀번호 기반 로그인
 * - Google OAuth 소셜 로그인
 * - 실시간 폼 검증 (Zod 스키마)
 * - 비밀번호 표시/숨김 토글
 * - 로딩 상태 관리
 * - 에러 핸들링 및 표시
 *
 * 🎯 이벤트 처리:
 * - 폼 제출: React Hook Form의 handleSubmit
 * - OAuth 로그인: Google 인증 URL 리다이렉트
 * - 네비게이션: 로그인 성공 시 대시보드로 이동
 * - 상태 관리: Zustand를 통한 전역 인증 상태 업데이트
 */

import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Eye, EyeOff } from 'lucide-react'
import { useAuthStore } from '@/hooks/useAuthStore'
import { apiClient } from '@/services/api'

/**
 * 🔍 Zod 스키마를 사용한 로그인 폼 검증
 *
 * 검증 규칙:
 * - 이메일: 올바른 이메일 형식 확인
 * - 비밀번호: 최소 8자 이상 (보안 강화)
 */
const loginSchema = z.object({
  email: z.string().email('올바른 이메일 주소를 입력해주세요'),
  password: z.string().min(8, '비밀번호는 8자 이상이어야 합니다'),
})

// 🏷️ TypeScript 타입 추론을 통한 타입 안전성 확보
type LoginForm = z.infer<typeof loginSchema>

/**
 * 로그인 페이지 메인 컴포넌트
 *
 * 🔐 인증 프로세스:
 * 1. 사용자 입력 → Zod 스키마 검증
 * 2. API 호출 → 서버 인증 처리
 * 3. 토큰 저장 → Zustand 스토어 업데이트
 * 4. 대시보드 리다이렉트 → 인증된 사용자 영역 이동
 *
 * 🎨 UI/UX 특징:
 * - 반응형 디자인 (모바일/데스크탑 대응)
 * - 비밀번호 표시/숨김 토글
 * - 로딩 상태 시각적 피드백
 * - 실시간 에러 메시지 표시
 * - Google OAuth 원클릭 로그인
 *
 * 🎯 이벤트 처리:
 * - onSubmit: 폼 제출 시 API 인증 요청
 * - handleGoogleLogin: Google OAuth 인증 시작
 * - showPassword 토글: 비밀번호 가시성 제어
 */
export default function Login() {
  // 🎛️ 컴포넌트 로컬 상태 관리
  const [showPassword, setShowPassword] = useState(false)        // 비밀번호 표시/숨김 토글
  const [isLoading, setIsLoading] = useState(false)              // 로딩 상태 (API 요청 중)

  // 🧭 라우팅 및 전역 상태 훅
  const navigate = useNavigate()                                 // React Router 네비게이션
  const { login } = useAuthStore()                               // Zustand 인증 상태 관리

  // 📝 React Hook Form 설정 - 폼 상태 관리 및 검증
  const {
    register,                                                      // 📋 입력 필드 등록 함수
    handleSubmit,                                                  // 🚀 폼 제출 핸들러
    formState: { errors },                                         // ❌ 검증 에러 상태
    setError,                                                      // 🎯 수동 에러 설정 함수
  } = useForm<LoginForm>({
    resolver: zodResolver(loginSchema),                            // 🔍 Zod 스키마를 통한 검증 연결
  })

  /**
   * 📤 로그인 폼 제출 처리 함수
   *
   * @param data - Zod 스키마로 검증된 로그인 폼 데이터
   *
   * 🔄 처리 흐름:
   * 1. 로딩 상태 활성화 → 버튼 비활성화 및 스피너 표시
   * 2. API 호출 → 서버에 인증 요청
   * 3. 응답 처리 → 성공/실패에 따른 분기 처리
   * 4. 상태 업데이트 → Zustand 스토어에 사용자 정보 저장
   * 5. 페이지 이동 → 대시보드로 리다이렉트
   *
   * 🎯 에러 처리:
   * - 네트워크 에러: 일반적인 오류 메시지 표시
   * - 인증 실패: 서버에서 전달받은 구체적인 메시지 표시
   * - 예외 상황: try-catch를 통한 안전한 에러 핸들링
   */
  const onSubmit = async (data: LoginForm) => {
    setIsLoading(true)                                             // 🔄 로딩 상태 시작
    try {
      // 🌐 API 클라이언트를 통한 로그인 요청
      const response = await apiClient.login(data)

      if (response.success && response.data) {
        // ✅ 로그인 성공: Zustand 스토어에 인증 정보 저장
        login(
          response.data.user,                                      // 👤 사용자 정보
          response.data.access_token,                              // 🔑 액세스 토큰
          response.data.refresh_token                              // 🔄 리프레시 토큰
        )
        navigate('/dashboard')                                     // 🏠 대시보드로 이동
      } else {
        // ❌ 로그인 실패: 서버 에러 메시지 표시
        setError('root', { message: response.message || '로그인에 실패했습니다' })
      }
    } catch (error: any) {
      // 🚨 예외 처리: 네트워크 에러 등
      setError('root', { message: '로그인 중 오류가 발생했습니다' })
    } finally {
      setIsLoading(false)                                          // ⏹️ 로딩 상태 종료
    }
  }

  /**
   * 🔗 Google OAuth 로그인 처리 함수
   *
   * 🔄 OAuth 프로세스:
   * 1. 콜백 URL 생성 → 현재 도메인 + /auth/callback
   * 2. Google 인증 URL 요청 → 서버에서 OAuth URL 생성
   * 3. 외부 리다이렉트 → 사용자를 Google 로그인 페이지로 이동
   * 4. 콜백 처리 → /auth/callback에서 인증 완료 처리
   *
   * 💡 보안 고려사항:
   * - CSRF 방지를 위한 state 파라미터 사용
   * - HTTPS 필수 (OAuth 보안 요구사항)
   * - 콜백 URL 화이트리스트 검증
   *
   * 🎯 에러 처리:
   * - API 에러: OAuth URL 생성 실패
   * - 네트워크 에러: 연결 문제
   */
  const handleGoogleLogin = async () => {
    try {
      // 🔗 OAuth 콜백 URL 생성 (현재 도메인 기반)
      const redirectUri = `${window.location.origin}/auth/callback`

      // 🌐 서버에서 Google OAuth URL 요청
      const response = await apiClient.getGoogleAuthUrl(redirectUri, 'GENERAL', 'LOGIN')

      if (response.success && response.data) {
        // ✅ 성공: Google 인증 페이지로 리다이렉트
        // window.location.href를 사용하여 전체 페이지 이동 (SPA 네비게이션 아님)
        window.location.href = response.data
      } else {
        // ❌ 실패: OAuth URL 생성 실패
        setError('root', { message: 'Google 인증 URL을 가져오는데 실패했습니다' })
      }
    } catch (error) {
      // 🚨 예외 처리: 네트워크 에러 등
      setError('root', { message: 'Google 인증 중 오류가 발생했습니다' })
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full space-y-8">
        <div>
          <Link to="/" className="flex justify-center">
            <span className="text-6xl font-bold text-primary-600">JOB았다</span>
          </Link>
          <h2 className="mt-6 text-center text-3xl font-bold text-gray-900">
            로그인
          </h2>
          <p className="mt-2 text-center text-sm text-gray-600">
            또는{' '}
            <Link
              to="/register"
              className="font-medium text-primary-600 hover:text-primary-500"
            >
              새 계정 만들기
            </Link>
          </p>
        </div>

        <form className="mt-8 space-y-6" onSubmit={handleSubmit(onSubmit)}>
          <div className="space-y-4">
            <div>
              <label htmlFor="email" className="sr-only">
                이메일
              </label>
              <input
                {...register('email')}
                type="email"
                autoComplete="email"
                className="input"
                placeholder="이메일 주소"
              />
              {errors.email && (
                <p className="mt-1 text-sm text-red-600">{errors.email.message}</p>
              )}
            </div>

            <div className="relative">
              <label htmlFor="password" className="sr-only">
                비밀번호
              </label>
              <input
                {...register('password')}
                type={showPassword ? 'text' : 'password'}
                autoComplete="current-password"
                className="input pr-10"
                placeholder="비밀번호"
              />
              <button
                type="button"
                className="absolute inset-y-0 right-0 pr-3 flex items-center"
                onClick={() => setShowPassword(!showPassword)}
              >
                {showPassword ? (
                  <EyeOff className="h-5 w-5 text-gray-400" />
                ) : (
                  <Eye className="h-5 w-5 text-gray-400" />
                )}
              </button>
              {errors.password && (
                <p className="mt-1 text-sm text-red-600">{errors.password.message}</p>
              )}
            </div>
          </div>

          {errors.root && (
            <div className="bg-red-50 border border-red-200 rounded-md p-3">
              <p className="text-sm text-red-600">{errors.root.message}</p>
            </div>
          )}

          <div className="flex items-center justify-between">
            <div className="flex items-center">
              <input
                id="remember-me"
                name="remember-me"
                type="checkbox"
                className="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded"
              />
              <label htmlFor="remember-me" className="ml-2 block text-sm text-gray-900">
                로그인 상태 유지
              </label>
            </div>

            <div className="text-sm">
              <Link
                to="/forgot-password"
                className="font-medium text-primary-600 hover:text-primary-500"
              >
                비밀번호 찾기
              </Link>
            </div>
          </div>

          <div>
            <button
              type="submit"
              disabled={isLoading}
              className="w-full btn-primary py-3 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {isLoading ? '로그인 중...' : '로그인'}
            </button>
          </div>

          <div className="mt-6">
            <div className="relative">
              <div className="absolute inset-0 flex items-center">
                <div className="w-full border-t border-gray-300" />
              </div>
              <div className="relative flex justify-center text-sm">
                <span className="px-2 bg-gray-50 text-gray-500">또는</span>
              </div>
            </div>

            <div className="mt-6">
              <button
                type="button"
                onClick={handleGoogleLogin}
                className="w-full flex justify-center items-center px-4 py-3 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 transition-colors"
              >
                <svg className="w-5 h-5 mr-2" viewBox="0 0 24 24">
                  <path
                    fill="#4285F4"
                    d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"
                  />
                  <path
                    fill="#34A853"
                    d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
                  />
                  <path
                    fill="#FBBC05"
                    d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"
                  />
                  <path
                    fill="#EA4335"
                    d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"
                  />
                </svg>
                Google로 로그인
              </button>
            </div>
          </div>
        </form>
      </div>
    </div>
  )
}