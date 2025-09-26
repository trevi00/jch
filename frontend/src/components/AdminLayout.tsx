/**
 * AdminLayout.tsx - 관리자 전용 레이아웃 관리 컴포넌트
 *
 * 🔧 사용 기술:
 * - React 18 (함수형 컴포넌트, Hooks)
 * - React Router v6 (Outlet 컴포넌트로 중첩 라우팅)
 * - TypeScript (타입 안전성)
 * - Tailwind CSS (반응형 스타일링, 다크모드)
 * - Lucide React (아이콘)
 *
 * 📋 주요 기능:
 * - 관리자 전용 네비게이션
 * - 관리자 사이드바
 * - 관리자 헤더 (로그아웃 기능)
 * - 반응형 레이아웃
 *
 * 🎯 이벤트 처리:
 * - 관리자 로그아웃: adminToken 제거 후 로그인 페이지로 이동
 * - 사이드바 토글: 모바일에서 햄버거 메뉴
 */

import { Outlet, useNavigate, useLocation, Link } from 'react-router-dom'
import { useState } from 'react'
import {
  Shield,
  Users,
  Briefcase,
  MessageSquare,
  Award,
  BarChart3,
  Settings,
  LogOut,
  Menu,
  X
} from 'lucide-react'

/**
 * 관리자 네비게이션 메뉴 항목 정의
 */
const adminNavItems = [
  {
    name: '대시보드',
    href: '/admin',
    icon: BarChart3,
    exact: true
  },
  {
    name: '사용자 관리',
    href: '/admin/users',
    icon: Users
  },
  {
    name: '채용공고 관리',
    href: '/admin/jobs',
    icon: Briefcase
  },
  {
    name: '커뮤니티 관리',
    href: '/admin/community',
    icon: MessageSquare
  },
  {
    name: '자격증 관리',
    href: '/admin/certificates',
    icon: Award
  }
]

/**
 * 관리자 레이아웃 메인 컴포넌트
 *
 * 📐 레이아웃 구조:
 * - AdminHeader: 관리자 전용 상단 헤더
 * - AdminSidebar: 관리자 네비게이션 사이드바
 * - Main: 메인 콘텐츠 영역 (Outlet으로 중첩 라우팅)
 *
 * 🔒 보안 고려사항:
 * - 관리자 전용 토큰 사용
 * - 일반 사용자 세션과 분리
 * - 로그아웃 시 관리자 토큰 완전 제거
 */
export default function AdminLayout() {
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const navigate = useNavigate()
  const location = useLocation()

  /**
   * 관리자 로그아웃 처리 함수
   *
   * 🔄 처리 과정:
   * 1. localStorage에서 관리자 관련 토큰 모두 제거
   * 2. 관리자 로그인 페이지로 리다이렉트
   * 3. 사이드바 닫기
   */
  const handleAdminLogout = () => {
    // 🗑️ 관리자 관련 모든 데이터 제거
    localStorage.removeItem('adminToken')
    localStorage.removeItem('adminRefreshToken')
    localStorage.removeItem('adminUser')

    // 🚪 관리자 로그인 페이지로 이동
    navigate('/admin/login')
  }

  /**
   * 현재 경로가 활성 메뉴인지 확인하는 함수
   *
   * @param href 메뉴 경로
   * @param exact 정확히 일치해야 하는지 여부
   * @returns 활성 상태 여부
   */
  const isActiveRoute = (href: string, exact: boolean = false) => {
    if (exact) {
      return location.pathname === href
    }
    return location.pathname.startsWith(href)
  }

  /**
   * 관리자 정보 가져오기
   */
  const getAdminUser = () => {
    try {
      const adminUserStr = localStorage.getItem('adminUser')
      return adminUserStr ? JSON.parse(adminUserStr) : null
    } catch {
      return null
    }
  }

  const adminUser = getAdminUser()

  return (
    <div className="min-h-screen bg-gray-50 flex">
      {/* 🎨 사이드바 */}
      <div className={`
        fixed inset-y-0 left-0 z-50 w-64 bg-gray-900 transform transition-transform duration-300 ease-in-out lg:translate-x-0 lg:static lg:inset-0
        ${sidebarOpen ? 'translate-x-0' : '-translate-x-full'}
      `}>
        <div className="flex items-center justify-between h-16 px-4 bg-gray-800">
          <div className="flex items-center space-x-2">
            <Shield className="h-8 w-8 text-red-500" />
            <span className="text-xl font-bold text-white">관리자</span>
          </div>
          <button
            onClick={() => setSidebarOpen(false)}
            className="lg:hidden text-gray-300 hover:text-white"
          >
            <X className="h-6 w-6" />
          </button>
        </div>

        <nav className="mt-8">
          <div className="px-4">
            <ul className="space-y-2">
              {adminNavItems.map((item) => {
                const Icon = item.icon
                const isActive = isActiveRoute(item.href, item.exact)

                return (
                  <li key={item.name}>
                    <Link
                      to={item.href}
                      onClick={() => setSidebarOpen(false)}
                      className={`
                        flex items-center px-4 py-2 text-sm font-medium rounded-md transition-colors
                        ${isActive
                          ? 'bg-red-700 text-white'
                          : 'text-gray-300 hover:bg-gray-700 hover:text-white'
                        }
                      `}
                    >
                      <Icon className="mr-3 h-5 w-5" />
                      {item.name}
                    </Link>
                  </li>
                )
              })}
            </ul>
          </div>

          <div className="mt-8 pt-8 border-t border-gray-700">
            <div className="px-4">
              <button
                onClick={handleAdminLogout}
                className="w-full flex items-center px-4 py-2 text-sm font-medium text-gray-300 rounded-md hover:bg-gray-700 hover:text-white transition-colors"
              >
                <LogOut className="mr-3 h-5 w-5" />
                로그아웃
              </button>
            </div>
          </div>
        </nav>
      </div>

      {/* 📱 모바일 오버레이 */}
      {sidebarOpen && (
        <div
          className="fixed inset-0 z-40 lg:hidden"
          onClick={() => setSidebarOpen(false)}
        >
          <div className="absolute inset-0 bg-gray-600 opacity-75"></div>
        </div>
      )}

      {/* 📄 메인 콘텐츠 영역 */}
      <div className="flex-1 flex flex-col overflow-hidden">
        {/* 🎨 헤더 */}
        <header className="bg-white shadow-sm lg:static lg:overflow-y-visible">
          <div className="mx-auto px-4 sm:px-6 lg:px-8">
            <div className="relative flex justify-between h-16">
              <div className="relative z-10 px-2 flex items-center lg:px-0">
                <button
                  onClick={() => setSidebarOpen(true)}
                  className="lg:hidden inline-flex items-center justify-center p-2 rounded-md text-gray-400 hover:text-gray-500 hover:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-inset focus:ring-red-500"
                >
                  <Menu className="h-6 w-6" />
                </button>
              </div>

              <div className="relative z-0 flex-1 px-2 flex items-center justify-center sm:absolute sm:inset-0">
                <div className="w-full sm:max-w-xs">
                  <h1 className="text-lg font-medium text-gray-900">
                    관리자 대시보드
                  </h1>
                </div>
              </div>

              <div className="relative z-10 flex items-center lg:w-auto lg:static lg:inset-auto lg:ml-6 lg:pr-0">
                <div className="flex items-center space-x-4">
                  <span className="text-sm text-gray-700">
                    {adminUser?.name || adminUser?.email || '관리자'}님
                  </span>
                  <button
                    onClick={handleAdminLogout}
                    className="bg-red-600 hover:bg-red-700 text-white px-3 py-2 rounded-md text-sm font-medium transition-colors"
                  >
                    로그아웃
                  </button>
                </div>
              </div>
            </div>
          </div>
        </header>

        {/* 📋 메인 콘텐츠 */}
        <main className="flex-1 relative overflow-y-auto focus:outline-none">
          <div className="py-6">
            <div className="mx-auto px-4 sm:px-6 md:px-8">
              {/* 🎯 중첩 라우팅된 페이지 컴포넌트가 여기에 렌더링됩니다 */}
              <Outlet />
            </div>
          </div>
        </main>
      </div>
    </div>
  )
}