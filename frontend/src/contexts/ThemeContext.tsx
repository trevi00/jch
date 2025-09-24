/**
 * ThemeContext.tsx - 다크/라이트 테마 전역 상태 관리
 *
 * 🔧 사용 기술:
 * - React Context API (전역 상태 관리)
 * - TypeScript (타입 안전성)
 * - CSS Classes (다니엘 다크모드 구현)
 * - Local Storage (사용자 선호도 지속화)
 * - Media Query (시스템 테마 감지)
 *
 * 📋 주요 기능:
 * - 다크/라이트 테마 토글
 * - 사용자 선호도 자동 저장/복원
 * - 시스템 테마 자동 감지
 * - CSS 클래스 자동 전환
 *
 * 🎯 이벤트 처리:
 * - toggleTheme: 사용자 클릭 시 테마 전환
 * - 브라우저 테마 변경: prefers-color-scheme 미디어 쿼리 반영
 * - localStorage 변경: 사용자 설정 지속화
 */

import { createContext, useContext, useEffect, useState } from 'react'

// 🞨 테마 타입 정의 - 리터럴 타입으로 엄격한 타입 체크
type Theme = 'light' | 'dark'

/**
 * 🏗️ 테마 컨텍스트 타입 인터페이스
 *
 * - theme: 현재 활성화된 테마 (라이트/다크)
 * - toggleTheme: 테마를 전환하는 함수
 */
interface ThemeContextType {
  theme: Theme
  toggleTheme: () => void
}

// 🏪 React Context 생성 - undefined로 초기화하여 에러 감지 가능
const ThemeContext = createContext<ThemeContextType | undefined>(undefined)

/**
 * 🌍 ThemeProvider 컴포넌트 - 전역 테마 제공자
 *
 * 🔄 초기화 로직:
 * 1. localStorage에서 저장된 사용자 선호도 확인
 * 2. 없으면 시스템 선호도 감지 (prefers-color-scheme)
 * 3. 기본값은 라이트 모드
 *
 * 🎨 적용 방식:
 * - CSS 클래스 기반 (Tailwind CSS 다크 모드)
 * - document.documentElement에 'dark' 클래스 추가/제거
 * - localStorage에 사용자 선호도 자동 저장
 */
export function ThemeProvider({ children }: { children: React.ReactNode }) {
  // 🎨 테마 상태 초기화 - 지연 초기화로 성능 최적화
  const [theme, setTheme] = useState<Theme>(() => {
    // 🌐 SSR 환경 고려 - window 객체 존재 여부 확인
    if (typeof window !== 'undefined') {
      // 1순위: localStorage에서 사용자 설정 확인
      const savedTheme = localStorage.getItem('theme') as Theme
      if (savedTheme) return savedTheme

      // 2순위: 브라우저/OS 시스템 테마 설정 감지
      return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
    }
    // 3순위: SSR 환경에서는 라이트 모드 기본값
    return 'light'
  })

  // 🎯 테마 변경 시 부작용 처리 - DOM 조작 및 localStorage 업데이트
  useEffect(() => {
    // 🌐 HTML 루트 요소 참조 - Tailwind CSS 다크 모드 클래스 적용 대상
    const root = window.document.documentElement

    // 🌙 다크 모드 적용/해제 - CSS 클래스 기반 스타일링
    if (theme === 'dark') {
      root.classList.add('dark')     // Tailwind 다크 모드 활성화
    } else {
      root.classList.remove('dark')  // 라이트 모드로 되돌리기
    }

    // 💾 사용자 선호도를 localStorage에 지속적으로 저장
    localStorage.setItem('theme', theme)
  }, [theme]) // theme 값이 변경될 때마다 실행

  /**
   * 🔄 테마 토글 함수
   *
   * 현재 테마에 따라 반대 테마로 전환:
   * - 라이트 → 다크
   * - 다크 → 라이트
   *
   * 🎯 호출 시점:
   * - 사용자가 테마 전환 버튼 클릭
   * - 단축키 사용 (Ctrl+Shift+D 등)
   * - 자동 전환 로직 (예: 시간대별 자동 전환)
   */
  const toggleTheme = () => {
    setTheme(prev => prev === 'light' ? 'dark' : 'light') // 현재 테마의 반대값으로 설정
  }

  return (
    <ThemeContext.Provider value={{ theme, toggleTheme }}>
      {children}
    </ThemeContext.Provider>
  )
}

/**
 * 🦅 useTheme 커스텀 훅
 *
 * ThemeContext에서 테마 상태와 제어 함수를 가져옵니다.
 *
 * 🔒 안전 가드:
 * - ThemeProvider로 감싸지지 않은 컴포넌트에서 사용 시 에러 발생
 * - 개발 시점에 에러를 발견하여 버그 방지
 *
 * 📝 사용 예시:
 * ```tsx
 * const { theme, toggleTheme } = useTheme()
 *
 * return (
 *   <button onClick={toggleTheme}>
 *     {theme === 'dark' ? '🌙' : '☀️'} {theme}
 *   </button>
 * )
 * ```
 *
 * @returns {ThemeContextType} 테마 상태와 제어 함수
 * @throws {Error} ThemeProvider 외부에서 사용 시 에러
 */
export function useTheme() {
  const context = useContext(ThemeContext)

  // 🛑 에러 가드 - Provider 누락 감지
  if (context === undefined) {
    throw new Error('useTheme must be used within a ThemeProvider')
  }

  return context
}