/**
 * AdminLogin.tsx - 관리자 로그인 페이지 컴포넌트
 *
 * 🔧 사용 기술:
 * - React 18 (useState Hook, 함수형 컴포넌트)
 * - TypeScript (타입 안전성)
 * - React Hook Form (폼 관리 및 검증)
 * - Zod (스키마 기반 데이터 검증)
 * - React Router (페이지 네비게이션)
 * - Lucide React (아이콘)
 *
 * 📋 주요 기능:
 * - 이메일/비밀번호 기반 관리자 로그인
 * - 관리자 전용 JWT 토큰 처리
 * - 실시간 폼 검증 (Zod 스키마)
 * - 비밀번호 표시/숨김 토글
 * - 로딩 상태 관리
 * - 에러 핸들링 및 표시
 *
 * 🎯 이벤트 처리:
 * - 폼 제출: React Hook Form의 handleSubmit
 * - 네비게이션: 로그인 성공 시 관리자 대시보드로 이동
 * - 관리자 토큰 관리: localStorage에 별도 저장
 */

import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Eye, EyeOff, Shield } from 'lucide-react'
import { apiClient } from '@/services/api'

/**
 * 🔍 Zod 스키마를 사용한 관리자 로그인 폼 검증
 *
 * 검증 규칙:
 * - 이메일: 올바른 이메일 형식 확인
 * - 비밀번호: 최소 8자 이상 (보안 강화)
 */
const adminLoginSchema = z.object({
  email: z.string().email('올바른 이메일 주소를 입력해주세요'),
  password: z.string().min(8, '비밀번호는 8자 이상이어야 합니다'),
})

// 🏷️ TypeScript 타입 추론을 통한 타입 안전성 확보
type AdminLoginForm = z.infer<typeof adminLoginSchema>

/**
 * 관리자 로그인 페이지 메인 컴포넌트
 *
 * 🔐 관리자 인증 프로세스:
 * 1. 사용자 입력 → Zod 스키마 검증
 * 2. API 호출 → 관리자 인증 처리
 * 3. 관리자 토큰 저장 → localStorage에 별도 저장
 * 4. 관리자 대시보드 리다이렉트 → 관리자 영역 이동
 *
 * 🎨 UI/UX 특징:
 * - 관리자 전용 디자인 (Shield 아이콘)
 * - 일반 로그인과 구분되는 시각적 차별화
 * - 비밀번호 표시/숨김 토글
 * - 로딩 상태 시각적 피드백
 * - 실시간 에러 메시지 표시
 *
 * 🎯 이벤트 처리:
 * - onSubmit: 폼 제출 시 관리자 인증 요청
 * - showPassword 토글: 비밀번호 가시성 제어
 */
export default function AdminLogin() {
  // 🎛️ 컴포넌트 로컬 상태 관리
  const [showPassword, setShowPassword] = useState(false)        // 비밀번호 표시/숨김 토글
  const [isLoading, setIsLoading] = useState(false)              // 로딩 상태 (API 요청 중)

  // 🧭 라우팅 훅
  const navigate = useNavigate()                                 // React Router 네비게이션

  // 📝 React Hook Form 설정 - 폼 상태 관리 및 검증
  const {
    register,                                                      // 📋 입력 필드 등록 함수
    handleSubmit,                                                  // 🚀 폼 제출 핸들러
    formState: { errors },                                         // ❌ 검증 에러 상태
    setError,                                                      // 🎯 수동 에러 설정 함수
  } = useForm<AdminLoginForm>({
    resolver: zodResolver(adminLoginSchema),                       // 🔍 Zod 스키마를 통한 검증 연결
  })

  /**
   * 📤 관리자 로그인 폼 제출 처리 함수 (모크 인증)
   *
   * @param data - Zod 스키마로 검증된 관리자 로그인 폼 데이터
   *
   * 🔄 처리 흐름:
   * 1. 로딩 상태 활성화 → 버튼 비활성화 및 스피너 표시
   * 2. 모크 인증 처리 → 항상 성공으로 처리 (인증 없이)
   * 3. 모크 토큰 저장 → localStorage에 모크 토큰 저장
   * 4. 페이지 이동 → 관리자 대시보드로 리다이렉트
   *
   * 🎯 모크 기능:
   * - 실제 API 호출 없이 항상 로그인 성공
   * - 모크 토큰을 localStorage에 저장
   * - 관리자 대시보드로 즉시 이동
   */
  const onSubmit = async (data: AdminLoginForm) => {
    setIsLoading(true)                                             // 🔄 로딩 상태 시작

    // 모크 로딩 시간 (0.5초 대기)
    await new Promise(resolve => setTimeout(resolve, 500))

    try {
      // 🔓 모크 인증: 항상 성공으로 처리
      // 모크 토큰을 localStorage에 저장
      localStorage.setItem('adminToken', 'mock-admin-token-12345')
      localStorage.setItem('adminRefreshToken', 'mock-admin-refresh-token-12345')

      // 모크 관리자 정보 저장
      const mockAdminUser = {
        id: 1,
        email: data.email,
        name: '관리자',
        userType: 'ADMIN',
        role: 'ADMIN'
      }
      localStorage.setItem('adminUser', JSON.stringify(mockAdminUser))

      // 🏠 관리자 대시보드로 이동
      navigate('/admin')
    } catch (error: any) {
      // 🚨 모크에서는 거의 발생하지 않지만 안전을 위한 예외 처리
      setError('root', { message: '관리자 로그인 중 오류가 발생했습니다' })
    } finally {
      setIsLoading(false)                                          // ⏹️ 로딩 상태 종료
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-gray-900 via-gray-800 to-gray-900 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full space-y-8">
        <div>
          <Link to="/" className="flex justify-center">
            <div className="flex items-center space-x-2">
              <Shield className="h-12 w-12 text-red-500" />
              <span className="text-4xl font-bold text-white">관리자</span>
            </div>
          </Link>
          <h2 className="mt-6 text-center text-3xl font-bold text-white">
            관리자 로그인
          </h2>
          <p className="mt-2 text-center text-sm text-gray-300">
            관리자 권한이 있는 계정으로 로그인해주세요
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
                className="appearance-none rounded-md relative block w-full px-3 py-2 border border-gray-600 placeholder-gray-400 text-white bg-gray-700 focus:outline-none focus:ring-red-500 focus:border-red-500 focus:z-10 sm:text-sm"
                placeholder="관리자 이메일 주소"
              />
              {errors.email && (
                <p className="mt-1 text-sm text-red-400">{errors.email.message}</p>
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
                className="appearance-none rounded-md relative block w-full px-3 py-2 pr-10 border border-gray-600 placeholder-gray-400 text-white bg-gray-700 focus:outline-none focus:ring-red-500 focus:border-red-500 focus:z-10 sm:text-sm"
                placeholder="관리자 비밀번호"
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
                <p className="mt-1 text-sm text-red-400">{errors.password.message}</p>
              )}
            </div>
          </div>

          {errors.root && (
            <div className="bg-red-900/50 border border-red-500 rounded-md p-3">
              <p className="text-sm text-red-200">{errors.root.message}</p>
            </div>
          )}

          <div>
            <button
              type="submit"
              disabled={isLoading}
              className="group relative w-full flex justify-center py-3 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-red-600 hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
            >
              <span className="absolute left-0 inset-y-0 flex items-center pl-3">
                <Shield className="h-5 w-5 text-red-300 group-hover:text-red-200" />
              </span>
              {isLoading ? '인증 중...' : '관리자 로그인'}
            </button>
          </div>

          <div className="text-center">
            <Link
              to="/login"
              className="font-medium text-gray-300 hover:text-white transition-colors"
            >
              일반 사용자 로그인으로 이동
            </Link>
          </div>
        </form>
      </div>
    </div>
  )
}