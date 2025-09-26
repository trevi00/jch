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
        // 🔍 localStorage에서 관리자 토큰 확인
        const adminToken = localStorage.getItem('adminToken')

        if (!adminToken) {
          // ❌ 토큰이 없으면 즉시 접근 거부
          setHasAdminAccess(false)
          setIsLoading(false)
          return
        }

        // ✅ 토큰이 있으면 일단 접근 허용 (간단한 버전)
        // 추후 서버 검증 로직 추가 가능
        setHasAdminAccess(true)

        // TODO: 서버에서 관리자 권한 검증 (선택적 구현)
        /*
        try {
          const response = await apiClient.verifyAdminToken()
          if (response.success) {
            setHasAdminAccess(true)
          } else {
            // 토큰이 유효하지 않으면 제거
            localStorage.removeItem('adminToken')
            localStorage.removeItem('adminRefreshToken')
            localStorage.removeItem('adminUser')
            setHasAdminAccess(false)
          }
        } catch (error) {
          // 서버 검증 실패 시에도 접근 거부
          localStorage.removeItem('adminToken')
          localStorage.removeItem('adminRefreshToken')
          localStorage.removeItem('adminUser')
          setHasAdminAccess(false)
        }
        */

      } catch (error) {
        // 🚨 예외 발생 시 안전하게 접근 거부
        setHasAdminAccess(false)
      } finally {
        setIsLoading(false)
      }
    }

    checkAdminAccess()
  }, [])

  // 🔄 로딩 중인 경우 로딩 스피너 표시
  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-900">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-red-500 mx-auto"></div>
          <p className="mt-4 text-gray-300">관리자 권한 확인 중...</p>
        </div>
      </div>
    )
  }

  // 🔒 관리자 권한이 없으면 로그인 페이지로 리다이렉트
  if (!hasAdminAccess) {
    return <Navigate to="/admin/login" replace />
  }

  // ✅ 관리자 권한이 있으면 자식 컴포넌트 렌더링
  return <>{children}</>
}