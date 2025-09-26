/**
 * Header.tsx - 사이트 전체 공통 헤더 컴포넌트
 *
 * 🔧 사용 기술:
 * - React 18 (함수형 컴포넌트)
 * - TypeScript (Props 인터페이스, 타입 안전성)
 * - React Router (Link 컴포넌트, 네비게이션)
 * - Lucide React (SVG 아이콘)
 * - Tailwind CSS (스타일링, 다크모드 지원)
 * - Zustand (사용자 상태 관리)
 *
 * 📋 주요 기능:
 * - 반응형 내비게이션 (데스크탑/모바일)
 * - 통합 검색 기능 (채용공고, 기업명)
 * - 다크모드 토글
 * - 실시간 알림 표시
 * - 사용자별 드롭다운 메뉴
 * - 로그아웃 기능
 * - 사용자 타입별 프로필 링크
 *
 * 🎯 이벤트 처리:
 * - 모바일 메뉴 토글: props를 통한 상위 컴포넌트 콜백
 * - 검색: 입력 필드 onChange 이벤트
 * - 사용자 메뉴: CSS hover 및 focus 이벤트
 * - 로그아웃: Zustand 액션 호출
 */

// 🧭 네비게이션 및 라우팅
import { Link } from 'react-router-dom'

// 🎨 아이콘 (Lucide React - 경량화된 Feather Icons)
import {
  Bell,      // 🔔 알림
  Search,    // 🔍 검색
  User,      // 👤 사용자
  Menu       // 📱 모바일 메뉴
} from 'lucide-react'

// 🎯 상태 관리 및 타입
import { useAuthStore } from '@/hooks/useAuthStore'
import { UserType } from '@/types/api'

// 🧩 UI 컴포넌트
import DarkModeToggle from '@/components/ui/DarkModeToggle'

/**
 * Header 컴포넌트의 Props 인터페이스
 * @interface HeaderProps
 * @property onMenuClick - 모바일 메뉴 클릭 핸들러 함수
 */
interface HeaderProps {
  onMenuClick?: () => void
}

/**
 * 🏗️ 헤더 메인 컴포넌트
 *
 * 📱 반응형 디자인:
 * - 데스크탑: 풀 내비게이션 + 검색바
 * - 모바일: 축약된 UI + 햄버거 메뉴
 *
 * 🎨 다크모드 지원:
 * - Tailwind의 dark: 클래스로 테마 대응
 * - 시스템 설정 또는 사용자 선택에 따라 자동 전환
 *
 * ♿ 접근성:
 * - ARIA 레이블 및 역할 정의
 * - 키보드 네비게이션 지원
 * - 스크린 리더 호환성
 *
 * @param onMenuClick - 모바일 햄버거 메뉴 클릭 핸들러 (옵셔널)
 */
export default function Header({ onMenuClick }: HeaderProps) {
  // 🔍 Zustand에서 사용자 정보와 로그아웃 함수 가져오기
  const { user, logout } = useAuthStore()

  return (
    <header
      className="bg-white dark:bg-gray-900 border-b border-gray-200 dark:border-gray-700 sticky top-0 z-50"
      role="banner"
    >
      {/* 🏠 헤더 컨테이너: sticky 위치로 스크롤 시에도 상단 고정 */}
      <div className="flex items-center justify-between px-4 lg:px-6 py-4">

        {/* 🔲 왼쪽 영역: 로고 + 검색 + 모바일 메뉴 */}
        <div className="flex items-center space-x-4">

          {/* 📱 모바일 햄버거 메뉴 버튼 (lg: 이상에서는 숨김) */}
          <button
            onClick={onMenuClick}
            className="p-2 text-gray-600 hover:text-gray-900 dark:text-gray-300 dark:hover:text-gray-100 lg:hidden focus:outline-none focus:ring-2 focus:ring-primary-500 focus:ring-offset-2 dark:focus:ring-offset-gray-900 rounded-lg"
            aria-label="메뉴 열기"
            type="button"
          >
            <Menu className="w-6 h-6" />
          </button>

          {/* 🏷️ 브랜드 로고: 클릭 시 대시보드로 이동 */}
          <Link to="/dashboard" className="text-2xl font-bold text-primary-600 dark:text-primary-400">
            JOB았다
          </Link>

          {/* 🔍 통합 검색바 (md: 이상에서만 표시) */}
          <div className="relative hidden md:block">
            {/* 🎨 검색 아이콘: input 내부 좌측에 절대 위치 */}
            {/* <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 dark:text-gray-500 w-4 h-4" /> */}

            {/* 📝 검색 입력 필드: 채용공고 및 기업명 통합 검색
            <input
              type="text"
              placeholder="채용공고, 기업명 검색..."
              className="pl-10 pr-4 py-2 w-80 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100 placeholder-gray-500 dark:placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-primary-600 focus:border-transparent"
              aria-label="채용공고 및 기업명 검색"
              role="searchbox"
            /> */}
          </div>
        </div>

        <div className="flex items-center space-x-2">
          <DarkModeToggle />
          <button className="relative p-2 text-gray-400 hover:text-gray-600 dark:text-gray-500 dark:hover:text-gray-300 focus:outline-none focus:ring-2 focus:ring-primary-500 focus:ring-offset-2 dark:focus:ring-offset-gray-900 rounded-lg" aria-label="알림 3개" type="button">
            <Bell className="w-5 h-5" />
          </button>
          
          <div className="relative group">
            <button className="flex items-center space-x-2 p-2 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-800 focus:outline-none focus:ring-2 focus:ring-primary-500 focus:ring-offset-2 dark:focus:ring-offset-gray-900" aria-label="사용자 메뉴 열기" aria-expanded="false" type="button">
              <div className="w-8 h-8 bg-primary-600 rounded-full flex items-center justify-center">
                <User className="w-5 h-5 text-white" />
              </div>
              <span className="text-sm font-medium text-gray-700 dark:text-gray-300">
                {user?.name || '사용자'}
              </span>
            </button>
            
            <div className="absolute right-0 mt-2 w-48 bg-white dark:bg-gray-800 rounded-md shadow-lg border border-gray-200 dark:border-gray-600 py-1 opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all" role="menu" aria-label="사용자 메뉴">
              {/* 기업 사용자에게만 통합 대시보드 링크 표시 */}
              {user?.userType === UserType.COMPANY && (
                <Link
                  to="/company/dashboard"
                  className="block px-4 py-2 text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700 focus:outline-none focus:bg-gray-100 dark:focus:bg-gray-700"
                  role="menuitem"
                >
                  기업 대시보드
                </Link>
              )}
              <Link
                to={user?.userType === UserType.COMPANY ? "/company/profile" : "/profile"}
                className="block px-4 py-2 text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700 focus:outline-none focus:bg-gray-100 dark:focus:bg-gray-700"
                role="menuitem"
              >
                내 프로필
              </Link>
              <Link
                to="/settings"
                className="block px-4 py-2 text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700 focus:outline-none focus:bg-gray-100 dark:focus:bg-gray-700"
                role="menuitem"
              >
                설정
              </Link>
              <hr className="my-1" />
              <button
                onClick={logout}
                className="block w-full text-left px-4 py-2 text-sm text-red-600 dark:text-red-400 hover:bg-gray-100 dark:hover:bg-gray-700 focus:outline-none focus:bg-gray-100 dark:focus:bg-gray-700"
                role="menuitem"
                type="button"
              >
                로그아웃
              </button>
            </div>
          </div>
        </div>
      </div>
    </header>
  )
}