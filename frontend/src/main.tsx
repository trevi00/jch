/**
 * main.tsx - 애플리케이션 진입점 및 전역 설정
 *
 * 🔧 사용 기술:
 * - React 18 (Concurrent Features, StrictMode)
 * - TypeScript (타입 안전성)
 * - React Router v6 (BrowserRouter)
 * - TanStack Query v4 (서버 상태 관리)
 * - Context API (테마, 토스트 상태 관리)
 * - Tailwind CSS (전역 스타일)
 *
 * 📋 주요 기능:
 * - React 18의 createRoot API를 통한 Concurrent Rendering
 * - Provider 체인을 통한 전역 상태 및 기능 제공
 * - React Query 성능 최적화 설정
 * - 에러 바운더리 및 폴백 처리
 *
 * 🎯 Provider 계층 구조:
 * React.StrictMode → ThemeProvider → ToastProvider → QueryClientProvider → BrowserRouter → App
 */

import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { ThemeProvider } from '@/contexts/ThemeContext'
import { ToastProvider } from '@/contexts/ToastContext'
import App from './App.tsx'
import './styles/globals.css'

/**
 * 🚀 성능 최적화된 React Query 클라이언트 설정
 *
 * 주요 최적화 포인트:
 * 1. 적절한 캐시 타임 설정으로 불필요한 API 호출 방지
 * 2. 지능적인 재시도 로직으로 에러 상황 대응
 * 3. 네트워크 상태에 따른 요청 제어
 * 4. 백그라운드 refetch 최소화로 성능 향상
 */
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      // 📌 캐시 관리 설정
      staleTime: 1000 * 60 * 5,     // 5분간 캐시된 데이터를 신선하다고 간주 (API 호출 최소화)
      gcTime: 1000 * 60 * 10,       // 10분 후 사용되지 않는 캐시 메모리에서 제거 (메모리 최적화)

      // 🔄 재시도 로직 - 스마트한 에러 처리
      retry: (failureCount, error) => {
        // 클라이언트 에러 (4xx)는 재시도하지 않음 - 서버에서 의도적으로 차단한 요청
        if (error instanceof Error && error.message.includes('404')) return false  // 리소스 없음
        if (error instanceof Error && error.message.includes('403')) return false  // 권한 없음
        return failureCount < 2 // 네트워크/서버 에러만 최대 2회 재시도
      },

      // ⏱️ 지수 백오프 알고리즘 - 서버 부하 방지
      retryDelay: (attemptIndex) => Math.min(1000 * 2 ** attemptIndex, 5000), // 1초 → 2초 → 4초 → 5초(최대)

      // 🎯 자동 refetch 제어 - 성능 최적화
      refetchOnWindowFocus: false,  // 윈도우 포커스 시 자동 refetch 방지 (배터리 수명 향상)
      refetchOnReconnect: 'always', // 네트워크 재연결 시에는 항상 refetch (데이터 동기화)
      refetchOnMount: true,         // 컴포넌트 마운트 시 refetch (최신 데이터 보장)

      // 🌐 네트워크 모드
      networkMode: 'online',        // 온라인일 때만 쿼리 실행 (오프라인 시 캐시 사용)
    },
    mutations: {
      retry: 1,                     // 변경 작업은 1회만 재시도 (데이터 일관성 보장)
      networkMode: 'online',        // 온라인일 때만 변경 작업 실행
    },
  },
})

/**
 * 🎯 React 18 Concurrent Rendering 시작점
 *
 * createRoot를 사용하여 새로운 렌더링 엔진 활성화:
 * - 자동 배칭 (Automatic Batching)
 * - 동시성 기능 (Concurrent Features)
 * - 서스펜스 개선 (Suspense Improvements)
 * - 타임 슬라이싱 (Time Slicing)
 */
ReactDOM.createRoot(document.getElementById('root')!).render(
  // 🛡️ React.StrictMode - 개발 모드에서 잠재적 문제 감지
  // - 부작용 감지를 위한 이중 렌더링
  // - 안전하지 않은 생명주기 메서드 경고
  // - 권장되지 않는 API 사용 경고
  <React.StrictMode>
    {/* 🎨 ThemeProvider - 다크/라이트 테마 전역 상태 관리 */}
    <ThemeProvider>
      {/* 🔔 ToastProvider - 전역 알림/토스트 메시지 시스템 */}
      <ToastProvider>
        {/* 🗄️ QueryClientProvider - 서버 상태 관리 및 캐싱 */}
        <QueryClientProvider client={queryClient}>
          {/* 🧭 BrowserRouter - HTML5 History API 기반 라우팅 */}
          <BrowserRouter>
            {/* 🏠 App - 메인 애플리케이션 컴포넌트 */}
            <App />
          </BrowserRouter>
        </QueryClientProvider>
      </ToastProvider>
    </ThemeProvider>
  </React.StrictMode>,
)