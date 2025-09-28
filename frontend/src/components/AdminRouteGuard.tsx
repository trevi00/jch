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
        const adminToken = localStorage.getItem('adminToken')
        console.log('AdminRouteGuard: Checking admin access, token:', adminToken)

        if (!adminToken) {
          // ❌ 토큰이 없으면 로그인 페이지로 리다이렉트
          console.log('AdminRouteGuard: No token found, redirecting to login')
          setHasAdminAccess(false)
          setIsLoading(false)
          return
        }

        console.log('AdminRouteGuard: Verifying token with backend...')

        // 🔍 백엔드 API를 통한 토큰 검증
        const response = await apiClient.api.get('/api/admin/verify', {
          headers: {
            Authorization: `Bearer ${adminToken}`
          }
        })

        console.log('AdminRouteGuard: Verification response:', response.data)

        if (response.data.success && response.data.data.admin) {
          // ✅ 관리자 권한 확인됨
          console.log('AdminRouteGuard: Admin access granted')
          setHasAdminAccess(true)
        } else {
          // ❌ 관리자 권한 없음
          console.log('AdminRouteGuard: Admin access denied:', response.data)
          localStorage.removeItem('adminToken')
          localStorage.removeItem('adminRefreshToken')
          localStorage.removeItem('adminUser')
          setHasAdminAccess(false)
        }

      } catch (error) {
        console.error('AdminRouteGuard: Admin verification failed:', error)
        // 🚨 검증 실패 시 토큰 제거
        localStorage.removeItem('adminToken')
        localStorage.removeItem('adminRefreshToken')
        localStorage.removeItem('adminUser')
        setHasAdminAccess(false)
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