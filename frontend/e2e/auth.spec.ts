import { test, expect } from '@playwright/test';

test.describe('Authentication', () => {
  test('should display login form', async ({ page }) => {
    await page.goto('/login');
    
    await expect(page.getByRole('heading', { name: '로그인' })).toBeVisible();
    await expect(page.getByPlaceholder('이메일 주소')).toBeVisible();
    await expect(page.getByPlaceholder('비밀번호')).toBeVisible();
    await expect(page.getByRole('button', { name: '로그인', exact: true })).toBeVisible();
  });

  test('should display signup form', async ({ page }) => {
    await page.goto('/register');
    
    await expect(page.getByRole('heading', { name: '회원가입' })).toBeVisible();
    await expect(page.getByPlaceholder('이름')).toBeVisible();
    await expect(page.getByPlaceholder('이메일 주소')).toBeVisible();
    await expect(page.getByPlaceholder('전화번호 (010-1234-5678)')).toBeVisible();
    await expect(page.getByRole('button', { name: '회원가입', exact: true })).toBeVisible();
  });

  test('should show validation errors for invalid login', async ({ page }) => {
    await page.goto('/login');
    
    await page.getByRole('button', { name: '로그인', exact: true }).click();
    
    // 이메일과 비밀번호 필드 검증 오류 메시지 확인
    await expect(page.getByText('올바른 이메일 주소를 입력해주세요')).toBeVisible();
    await expect(page.getByText('비밀번호는 8자 이상이어야 합니다')).toBeVisible();
  });

  test('should navigate between login and signup', async ({ page }) => {
    await page.goto('/login');
    
    await page.getByRole('link', { name: '새 계정 만들기' }).click();
    await expect(page).toHaveURL('/register');
    
    // 회원가입 페이지에서 로그인 페이지로 돌아가는 링크 찾기
    const backToLogin = page.getByRole('link', { name: '로그인' }).or(
      page.getByRole('link', { name: '기존 계정으로 로그인' })
    );
    
    if (await backToLogin.isVisible()) {
      await backToLogin.click();
      await expect(page).toHaveURL('/login');
    }
  });
});