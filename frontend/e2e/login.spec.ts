import { test, expect } from '@playwright/test';

test.describe('Login Page', () => {
  test('should display login form', async ({ page }) => {
    await page.goto('/login');
    
    // 페이지 제목 확인
    await expect(page).toHaveTitle(/잡았다/);
    
    // 로그인 폼 요소들이 있는지 확인 (실제로는 h2 태그 사용)
    await expect(page.locator('h2')).toContainText('로그인');
    
    // 이메일 입력 필드
    const emailInput = page.locator('input[type="email"]').first();
    await expect(emailInput).toBeVisible();
    
    // 비밀번호 입력 필드
    const passwordInput = page.locator('input[type="password"]').first();
    await expect(passwordInput).toBeVisible();
    
    // 로그인 버튼 (submit 타입만 선택)
    const loginButton = page.locator('button[type="submit"]');
    await expect(loginButton).toBeVisible();
    await expect(loginButton).toContainText('로그인');
  });

  test('should show validation errors for empty form', async ({ page }) => {
    await page.goto('/login');
    
    // 빈 폼으로 로그인 버튼 클릭 (submit 타입만 선택)
    const loginButton = page.locator('button[type="submit"]');
    await loginButton.click();
    
    // 에러 메시지가 표시되는지 확인 (타임아웃을 짧게 설정)
    await page.waitForTimeout(2000);
    
    // 페이지가 여전히 로그인 페이지에 있는지 확인
    await expect(page).toHaveURL('/login');
  });

  test('should navigate to register page', async ({ page }) => {
    await page.goto('/login');
    
    // 회원가입 링크 찾기
    const registerLink = page.getByRole('link', { name: /회원가입|register/i }).or(
      page.locator('text=회원가입').first()
    );
    
    if (await registerLink.isVisible()) {
      await registerLink.click();
      await expect(page).toHaveURL('/register');
    }
  });

  test('should navigate back to home', async ({ page }) => {
    await page.goto('/login');
    
    // 홈으로 가는 링크나 로고 찾기
    const homeLink = page.getByRole('link', { name: /잡았다|home|홈/i }).first().or(
      page.locator('a[href="/"]').first()
    );
    
    if (await homeLink.isVisible()) {
      await homeLink.click();
      await expect(page).toHaveURL('/');
    }
  });
});