import { Page } from '@playwright/test';

// 테스트용 목업 사용자 데이터
export const mockUser = {
  id: 1,
  email: 'test@example.com',
  name: '테스트 사용자',
  phoneNumber: '010-1234-5678',
  userType: 'GENERAL',
  createdAt: new Date().toISOString(),
  updatedAt: new Date().toISOString(),
};

export const mockTokens = {
  accessToken: 'mock-access-token',
  refreshToken: 'mock-refresh-token',
};

/**
 * 테스트용으로 인증 상태를 설정하는 함수
 */
export async function loginAsTestUser(page: Page) {
  // localStorage에 인증 정보 설정
  await page.addInitScript(() => {
    const authData = {
      user: {
        id: 1,
        email: 'test@example.com',
        name: '테스트 사용자',
        phoneNumber: '010-1234-5678',
        userType: 'GENERAL',
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      },
      accessToken: 'mock-access-token',
      refreshToken: 'mock-refresh-token',
      isAuthenticated: true,
    };

    // Zustand 스토어에 저장
    localStorage.setItem('auth-storage', JSON.stringify({
      state: authData,
      version: 0,
    }));

    // 개별 토큰도 저장 (API 호출용)
    localStorage.setItem('accessToken', 'mock-access-token');
    localStorage.setItem('refreshToken', 'mock-refresh-token');
  });
}

/**
 * 로그아웃 상태로 설정하는 함수
 */
export async function logoutTestUser(page: Page) {
  await page.addInitScript(() => {
    localStorage.removeItem('auth-storage');
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
  });
}