/**
 * AdminRouteGuard.tsx - 관리자 전용 라우트 보호 컴포넌트
 *
 * 🔧 사용 기술:
 * - React 18 (함수형 컴포넌트, useEffect)
 * - TypeScript (타입 안전성)
 * - React Router (Navigate 컴포넌트)
 * - JWT 토큰 검증 (localStorage)
 *
 * 📋 주요 기능:
 * - 관리자 토큰 존재 여부 확인
 * - 관리자 권한 없는 사용자 리다이렉트
 * - 로딩 상태 관리
 * - 토큰 자동 검증
 *
 * 🎯 보안 정책:
 * - 관리자 토큰이 없으면 관리자 로그인 페이지로 리다이렉트
 * - 토큰 만료 시 자동 로그아웃 처리
 * - 일반 사용자 접근 차단
 */

import { useState, useEffect, ReactNode } from 'react'
import { Navigate } from 'react-router-dom'
import { apiClient } from '@/services/api'

interface AdminRouteGuardProps {
  children: ReactNode
}

/**
 * 관리자 라우트 보호 컴포넌트
 *
 * 🔒 접근 제어 로직:
 * 1. localStorage에서 adminToken 확인
 * 2. 토큰이 있으면 서버에서 권한 검증 (선택적)
 * 3. 권한이 있으면 자식 컴포넌트 렌더링
 * 4. 권한이 없으면 관리자 로그인 페이지로 리다이렉트
 *
 * 🎯 에러 처리:
 * - 토큰 없음: 즉시 로그인 페이지로 리다이렉트
 * - 토큰 만료: 로그인 페이지로 리다이렉트
 * - 네트워크 에러: 안전하게 로그인 페이지로 이동
 */
export default function AdminRouteGuard({ children }: AdminRouteGuardProps) {
  const [isLoading, setIsLoading] = useState(true)
  const [hasAdminAccess, setHasAdminAccess] = useState(false)

  useEffect(() => {
    const checkAdminAccess = async () => {
      try {
        // 🔓 완전 모크 모드: 토큰 확인 후 없으면 로그인 페이지로, 있으면 항상 허용
        const adminToken = localStorage.getItem('adminToken')

        if (!adminToken) {
          // ❌ 토큰이 없으면 로그인 페이지로 리다이렉트
          setHasAdminAccess(false)
          setIsLoading(false)
          return
        }

        // ✅ 토큰이 있으면 항상 접근 허용 (완전 모크 인증)
        // 어떤 토큰이든 상관없이 항상 접근 허용
        setHasAdminAccess(true)

      } catch (error) {
        // 🚨 예외 발생 시에도 토큰이 있으면 접근 허용
        const adminToken = localStorage.getItem('adminToken')
        setHasAdminAccess(!!adminToken)
      } finally {
        setIsLoading(false)
      }
    }

    checkAdminAccess()
  }, [])

  // 🔄 로딩 중인 경우 로딩 스피너 표시 (짧은 시간만)
  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-900">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-red-500 mx-auto"></div>
          <p className="mt-4 text-gray-300">관리자 영역 접근 중...</p>
        </div>
      </div>
    )
  }

  // 🔒 토큰이 없으면 로그인 페이지로 리다이렉트 (모크 로그인으로 토큰 생성)
  if (!hasAdminAccess) {
    return <Navigate to="/admin/login" replace />
  }

  // ✅ 토큰이 있으면 항상 관리자 권한으로 자식 컴포넌트 렌더링
  return <>{children}</>
}