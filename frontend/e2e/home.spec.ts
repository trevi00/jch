import { test, expect } from '@playwright/test';

test.describe('Home Page', () => {
  test('should display home page', async ({ page }) => {
    await page.goto('/');
    
    // 페이지가 로드되는지 확인
    await expect(page).toHaveTitle(/잡았다/);
    
    // React 앱의 root 요소가 있는지 확인
    const root = page.locator('#root');
    await expect(root).toBeAttached();
    
    // React 컴포넌트가 렌더링될 때까지 기다리기
    // "잡았다" 텍스트가 나타날 때까지 기다림 (홈페이지 제목)
    await page.waitForSelector('h1:has-text("잡았다")', { timeout: 10000 });
    
    // 페이지 제목이 표시되는지 확인
    await expect(page.locator('h1')).toContainText('잡았다');
  });

  test('should navigate to login page', async ({ page }) => {
    await page.goto('/');
    
    // 로그인 링크를 먼저 시도 (첫 번째 우선순위)
    const loginLink = page.getByRole('link', { name: '로그인' }).first();
    
    if (await loginLink.isVisible()) {
      await loginLink.click();
      await expect(page).toHaveURL('/login');
    } else {
      // 링크가 없으면 버튼 시도
      const loginButton = page.getByRole('button', { name: '로그인' }).first();
      if (await loginButton.isVisible()) {
        await loginButton.click();
        await expect(page).toHaveURL('/login');
      }
    }
  });
});