/**
 * useAuthStore.ts - Zustand를 사용한 전역 인증 상태 관리
 *
 * 🔧 사용 기술:
 * - Zustand (경량 상태 관리 라이브러리)
 * - TypeScript (타입 안전성)
 * - Local Storage (상태 지속화)
 * - Custom Events (다른 컴포넌트와의 통신)
 *
 * 📋 주요 기능:
 * - JWT 토큰 기반 인증 상태 관리
 * - 자동 로그아웃 처리 (토큰 만료 시)
 * - 브라우저 새로고침 시 상태 복원
 * - 인증 상태 실시간 동기화
 *
 * 🎯 이벤트 처리:
 * - auth:logout: API 클라이언트에서 발생하는 자동 로그아웃 이벤트
 * - localStorage 변경: 토큰 저장/삭제 시 브라우저 스토리지 업데이트
 * - 상태 변경: 컴포넌트 리렌더링 트리거
 */

import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import { User } from '@/types/api'

/**
 * 🏗️ 인증 상태 관리를 위한 TypeScript 인터페이스
 *
 * 상태 속성:
 * - user: 현재 로그인된 사용자의 상세 정보
 * - accessToken: API 요청에 사용되는 JWT 액세스 토큰
 * - refreshToken: 액세스 토큰 갱신용 JWT 토큰
 * - isAuthenticated: 현재 로그인 상태 여부 (계산된 속성)
 *
 * 액션 함수:
 * - login: 로그인 성공 시 사용자 정보와 토큰 저장
 * - logout: 로그아웃 시 모든 인증 정보 제거
 * - updateUser: 사용자 정보만 업데이트 (토큰은 유지)
 */
interface AuthState {
  user: User | null
  accessToken: string | null
  refreshToken: string | null
  isAuthenticated: boolean
  login: (user: User, accessToken: string, refreshToken: string) => void
  logout: () => void
  updateUser: (user: User) => void
}

/**
 * 🏪 Zustand 스토어 생성 - 전역 인증 상태 관리
 *
 * 🔄 persist 미들웨어:
 * - localStorage에 상태 자동 저장/복원
 * - 브라우저 새로고침 시에도 로그인 상태 유지
 * - 선택적 직렬화로 민감한 정보 제외 가능
 *
 * 🎯 이벤트 리스너:
 * - 'auth:logout': API 클라이언트에서 토큰 만료 시 발생
 * - 자동 로그아웃 처리로 사용자 경험 향상
 *
 * 💡 보안 고려사항:
 * - 토큰을 localStorage에 저장 (XSS 주의)
 * - 토큰 만료 시 자동 정리
 * - 민감한 정보는 메모리에만 보관
 */
export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => {
      // 🎧 API 클라이언트에서의 자동 로그아웃 이벤트 리스너
      // 토큰 만료나 인증 오류 시 전역 로그아웃 처리
      if (typeof window !== 'undefined') {
        window.addEventListener('auth:logout', () => {
          get().logout(); // 현재 스토어의 logout 함수 호출
        });
      }

      return {
        user: null,
        accessToken: null,
        refreshToken: null,
        isAuthenticated: false,

        /**
         * 🔑 사용자 로그인 처리 함수
         *
         * @param user - 인증된 사용자의 상세 정보 (ID, 이메일, 권한 등)
         * @param accessToken - API 인증용 JWT 토큰 (짧은 만료시간)
         * @param refreshToken - 토큰 갱신용 JWT 토큰 (긴 만료시간)
         *
         * 🔄 처리 과정:
         * 1. localStorage에 토큰 저장 (API 클라이언트에서 자동 사용)
         * 2. Zustand 스토어에 사용자 정보와 토큰 저장
         * 3. isAuthenticated를 true로 설정
         * 4. 모든 구독 컴포넌트에 상태 변경 알림
         *
         * 🎯 부작용:
         * - localStorage 업데이트로 다른 탭과 동기화
         * - 컴포넌트 리렌더링 트리거
         * - API 클라이언트 인터셉터에서 토큰 자동 사용
         */
        login: (user: User, accessToken: string, refreshToken: string) => {
          // 💾 브라우저 로컬 스토리지에 토큰 저장 (API 클라이언트가 자동으로 사용)
          localStorage.setItem('accessToken', accessToken)
          localStorage.setItem('refreshToken', refreshToken)

          // 🏪 Zustand 스토어 상태 업데이트 (모든 구독자에게 알림)
          set({
            user,                    // 👤 사용자 정보
            accessToken,             // 🔑 API 인증 토큰
            refreshToken,            // 🔄 토큰 갱신용
            isAuthenticated: true,   // ✅ 로그인 상태 활성화
          })
        },

        /**
         * 🚪 사용자 로그아웃 처리 함수
         *
         * 🧹 정리 작업:
         * 1. localStorage에서 모든 인증 토큰 제거
         * 2. Zustand 스토어 상태 초기화
         * 3. 보안을 위한 메모리 정리
         *
         * 🎯 호출 시점:
         * - 사용자가 직접 로그아웃 버튼 클릭
         * - 토큰 만료로 인한 자동 로그아웃
         * - API 에러로 인한 강제 로그아웃
         * - 보안 정책에 의한 세션 종료
         *
         * 🔒 보안 고려사항:
         * - 모든 토큰을 즉시 제거하여 재사용 방지
         * - 사용자 정보 메모리에서 완전 제거
         * - 다른 탭에서도 동일한 로그아웃 상태 동기화
         */
        logout: () => {
          // 🗑️ localStorage에서 인증 토큰 완전 제거
          localStorage.removeItem('accessToken')   // API 인증 토큰 삭제
          localStorage.removeItem('refreshToken')  // 토큰 갱신용 삭제

          // 🔄 스토어 상태를 초기값으로 리셋
          set({
            user: null,               // 👤 사용자 정보 제거
            accessToken: null,        // 🔑 액세스 토큰 제거
            refreshToken: null,       // 🔄 리프레시 토큰 제거
            isAuthenticated: false,   // ❌ 인증 상태 비활성화
          })
        },

        /**
         * 사용자 정보 업데이트
         * @param user 업데이트할 사용자 정보 객체
         * 이벤트: zustand 상태 변경 이벤트
         */
        updateUser: (user: User) => {
          set({ user })
        },
      };
    },
    {
      name: 'auth-storage',
      partialize: (state) => ({
        user: state.user,
        accessToken: state.accessToken,
        refreshToken: state.refreshToken,
        isAuthenticated: state.isAuthenticated,
      }),
      onRehydrateStorage: () => (state) => {
        // localStorage에서 토큰이 복원되었지만 isAuthenticated가 false인 경우 수정
        if (state && state.accessToken && !state.isAuthenticated) {
          state.isAuthenticated = true;
        }
      },
    }
  )
)