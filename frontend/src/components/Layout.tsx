/**
 * Layout.tsx - 애플리케이션 전체 레이아웃 관리 컴포넌트
 *
 * 🔧 사용 기술:
 * - React 18 (함수형 컴포넌트, Hooks)
 * - React Router v6 (Outlet 컴포넌트로 중첩 라우팅)
 * - TypeScript (타입 안전성)
 * - Tailwind CSS (반응형 스타일링, 다크모드)
 * - DOM Events API (윈도우 리사이즈 이벤트)
 *
 * 📋 주요 기능:
 * - 반응형 레이아웃 (데스크탑/모바일 대응)
 * - 사이드바 토글 기능
 * - 접근성 지원 (건너뛰기 링크, ARIA 레이블)
 * - 다크모드 지원
 * - 헤더와 사이드바 통합 관리
 *
 * 🎯 이벤트 처리:
 * - 윈도우 리사이즈: 화면 크기에 따른 레이아웃 자동 조정
 * - 사이드바 토글: 모바일에서 햄버거 메뉴 클릭 시 사이드바 열기/닫기
 * - 사이드바 오버레이: 모바일에서 배경 클릭 시 사이드바 닫기
 *
 * 📱 반응형 브레이크포인트:
 * - 1024px 이상: 데스크탑 레이아웃 (사이드바 항상 표시)
 * - 1024px 미만: 모바일 레이아웃 (사이드바 오버레이)
 */

// 🧭 네비게이션 및 라우팅
import { Outlet } from 'react-router-dom'

// ⚛️ React Hooks
import { useState, useEffect } from 'react'

// 🧩 레이아웃 컴포넌트들
import Header from './Header'      // 상단 헤더 (로고, 검색, 사용자 메뉴)
import Sidebar from './Sidebar'    // 사이드 네비게이션
import SkipLink from './ui/SkipLink' // 접근성을 위한 건너뛰기 링크

/**
 * 🏗️ 메인 레이아웃 컴포넌트
 *
 * 📐 레이아웃 구조:
 * - SkipLink: 접근성을 위한 메인 콘텐츠로 바로가기 링크
 * - Header: 전체 페이지 상단 고정
 * - Sidebar: 왼쪽 네비게이션 (반응형)
 * - Main: 메인 콘텐츠 영역 (Outlet으로 중첩 라우팅)
 *
 * 🎨 스타일링:
 * - min-h-screen: 최소 화면 높이 100vh 보장
 * - flex 레이아웃: 사이드바와 메인 콘텐츠 수평 배치
 * - 다크모드: bg-gray-50 (라이트) / bg-gray-900 (다크)
 * - transition-all: 부드러운 애니메이션 효과
 *
 * ♿ 접근성:
 * - role="main": 메인 콘텐츠 영역 명시
 * - aria-label: 스크린 리더를 위한 영역 설명
 * - id="main-content": 건너뛰기 링크 타겟
 */
export default function Layout() {
  // 📱 사이드바 열림/닫힘 상태 (모바일에서 오버레이 토글)
  const [sidebarOpen, setSidebarOpen] = useState(false)

  // 📱 모바일 화면 여부 상태 (1024px 미만)
  const [isMobile, setIsMobile] = useState(false)

  /**
   * 🔄 화면 크기 변화 감지 및 레이아웃 자동 조정 Effect
   *
   * 📊 처리 로직:
   * 1. 현재 윈도우 너비를 확인 (window.innerWidth)
   * 2. 1024px 기준으로 모바일/데스크탑 판단
   * 3. 데스크탑에서는 사이드바 자동 열기
   * 4. 모바일에서는 사이드바 상태 유지
   *
   * 🎯 이벤트:
   * - window.resize: 브라우저 크기 변경 시 실시간 감지
   * - 컴포넌트 마운트 시 초기 화면 크기 확인
   * - 컴포넌트 언마운트 시 이벤트 리스너 정리
   */
  useEffect(() => {
    /**
     * 📱 모바일 여부 확인 및 사이드바 상태 조정 함수
     * @description 1024px를 기준으로 모바일/데스크탑 레이아웃 결정
     * 이벤트: 화면 크기 계산 이벤트, 사이드바 상태 변경 이벤트
     */
    const checkIsMobile = () => {
      // 📏 현재 윈도우 너비 확인 (Tailwind의 lg 브레이크포인트와 일치)
      const mobile = window.innerWidth < 1024
      setIsMobile(mobile)

      // 🖥️ 데스크탑에서는 사이드바를 자동으로 열어서 UX 향상
      if (!mobile) {
        setSidebarOpen(true)
      }
    }

    // 🚀 컴포넌트 마운트 시 초기 화면 크기 확인
    checkIsMobile()

    // 👂 윈도우 리사이즈 이벤트 리스너 등록
    window.addEventListener('resize', checkIsMobile)

    // 🧹 컴포넌트 언마운트 시 이벤트 리스너 정리 (메모리 누수 방지)
    return () => window.removeEventListener('resize', checkIsMobile)
  }, [])

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900">
      {/* 🏠 최상위 컨테이너: 전체 화면 높이와 배경색 설정 */}

      {/* ♿ 접근성: 메인 콘텐츠로 바로가기 링크 */}
      <SkipLink />

      {/* 📋 상단 헤더: 로고, 검색, 사용자 메뉴, 다크모드 토글 */}
      {/* 📱 onMenuClick: 모바일에서 햄버거 메뉴 클릭 시 사이드바 열기 */}
      <Header onMenuClick={() => setSidebarOpen(true)} />

      {/* 🔄 메인 콘텐츠 영역: Flexbox로 사이드바와 콘텐츠 수평 배치 */}
      <div className="flex">

        {/* 📱 사이드 네비게이션: 반응형 오버레이/고정 레이아웃 */}
        <Sidebar
          isOpen={sidebarOpen}                          // 현재 열림/닫힘 상태
          onClose={() => setSidebarOpen(false)}         // 모바일에서 배경 클릭 시 닫기
        />

        {/* 📄 메인 콘텐츠 영역: React Router Outlet으로 중첩 라우팅 */}
        <main
          className={`flex-1 p-4 lg:p-6 transition-all duration-300 ${
            !isMobile ? 'ml-64' : ''  // 🖥️ 데스크탑에서만 사이드바 너비만큼 왼쪽 마진
          }`}
          role="main"
          aria-label="주요 콘텐츠 영역"
        >
          {/* 📏 콘텐츠 최대 너비 제한 및 중앙 정렬 */}
          <div id="main-content" className="max-w-7xl mx-auto">
            {/* 🎯 React Router Outlet: 중첩된 라우트 컴포넌트가 렌더링되는 위치 */}
            <Outlet />
          </div>
        </main>

      </div>
    </div>
  )
}