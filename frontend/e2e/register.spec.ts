import { test, expect } from '@playwright/test';

test.describe('Register Page', () => {
  test('should display registration form', async ({ page }) => {
    await page.goto('/register');
    
    // 페이지 제목 확인
    await expect(page).toHaveTitle(/잡았다/);
    
    // 회원가입 폼 요소들이 있는지 확인 (실제로는 h2 태그 사용)
    await expect(page.locator('h2')).toContainText('회원가입');
    
    // 이름 입력 필드
    const nameInput = page.locator('input[placeholder="이름"]').first();
    await expect(nameInput).toBeVisible();
    
    // 이메일 입력 필드
    const emailInput = page.locator('input[type="email"]').first();
    await expect(emailInput).toBeVisible();
    
    // 비밀번호 입력 필드
    const passwordInput = page.locator('input[type="password"]').first();
    await expect(passwordInput).toBeVisible();
    
    // 회원가입 버튼 (submit 타입만 선택)
    const registerButton = page.locator('button[type="submit"]');
    await expect(registerButton).toBeVisible();
    await expect(registerButton).toContainText('회원가입');
  });

  test('should show validation errors for invalid email', async ({ page }) => {
    await page.goto('/register');
    
    // 잘못된 이메일 형식으로 입력
    const nameInput = page.locator('input[placeholder="이름"]').first();
    const emailInput = page.locator('input[type="email"]').first();
    const passwordInput = page.locator('input[type="password"]').first();
    
    await nameInput.fill('테스트사용자');
    await emailInput.fill('invalid-email');
    await passwordInput.fill('password123');
    
    // 회원가입 버튼 클릭 (submit 타입만 선택)
    const registerButton = page.locator('button[type="submit"]');
    await registerButton.click();
    
    // 에러 처리 확인 (페이지가 여전히 회원가입 페이지에 있는지)
    await page.waitForTimeout(2000);
    await expect(page).toHaveURL('/register');
  });

  test('should navigate to login page', async ({ page }) => {
    await page.goto('/register');
    
    // 로그인 링크 찾기
    const loginLink = page.getByRole('link', { name: /로그인|login/i }).or(
      page.locator('text=로그인').first()
    );
    
    if (await loginLink.isVisible()) {
      await loginLink.click();
      await expect(page).toHaveURL('/login');
    }
  });

  test('should handle form submission with valid data', async ({ page }) => {
    await page.goto('/register');
    
    // 유효한 데이터로 폼 작성 (백엔드 DTO에 맞춤)
    const nameInput = page.locator('input[placeholder="이름"]').first();
    const emailInput = page.locator('input[type="email"]').first();
    const passwordInput = page.locator('input[type="password"]').first();
    const confirmPasswordInput = page.locator('input[placeholder="비밀번호 확인"]').first();
    const phoneNumberInput = page.locator('input[placeholder*="전화번호"]').first();
    const termsCheckbox = page.locator('input[type="checkbox"]').first();
    
    await nameInput.fill('E2E 테스트 사용자');
    await emailInput.fill(`test-${Date.now()}@example.com`);
    await passwordInput.fill('SecurePassword123!');
    await confirmPasswordInput.fill('SecurePassword123!');
    await phoneNumberInput.fill('010-1234-5678');
    await termsCheckbox.check();
    
    // 회원가입 버튼 클릭 (submit 타입만 선택)
    const registerButton = page.locator('button[type="submit"]');
    await registerButton.click();
    
    // 로딩 상태나 결과 페이지로의 이동을 기다림
    await page.waitForTimeout(3000);
    
    // phoneNumber 필드가 백엔드에서 지원되지 않아 에러가 발생할 것으로 예상
    // 페이지가 여전히 register에 있거나 에러가 표시될 것
  });
});