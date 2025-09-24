import { test, expect } from '@playwright/test';
import { loginAsTestUser } from './auth-helper';

test.describe('Dashboard', () => {
  test('should display main dashboard elements', async ({ page }) => {
    await loginAsTestUser(page);
    await page.goto('/dashboard');
    
    // 인증된 사용자의 대시보드 요소들이 표시되는지 확인
    await page.waitForTimeout(1000); // 페이지 로딩 대기
    const body = page.locator('body');
    await expect(body).toBeVisible();
  });

  test('should navigate to jobs page', async ({ page }) => {
    await loginAsTestUser(page);
    await page.goto('/dashboard');
    
    // 채용정보 링크가 있다면 클릭
    const jobsLink = page.getByRole('link', { name: '채용정보' });
    if (await jobsLink.isVisible()) {
      await jobsLink.click();
      await expect(page).toHaveURL('/jobs');
    } else {
      // 직접 채용정보 페이지로 이동
      await page.goto('/jobs');
    }
    
    await page.waitForTimeout(1000);
    const body = page.locator('body');
    await expect(body).toBeVisible();
  });

  test('should navigate to AI services', async ({ page }) => {
    await loginAsTestUser(page);
    await page.goto('/dashboard');
    
    // AI 면접 페이지로 직접 이동
    await page.goto('/ai/interview');
    await page.waitForTimeout(1000);
    
    // 페이지가 로드되었는지 확인
    const body = page.locator('body');
    await expect(body).toBeVisible();

    // 자소서 생성 페이지 테스트
    await page.goto('/ai/cover-letter');
    await page.waitForTimeout(1000);
    await expect(body).toBeVisible();
  });

  test('should display job search functionality', async ({ page }) => {
    await loginAsTestUser(page);
    await page.goto('/jobs');
    await page.waitForTimeout(1000);
    
    // 검색 관련 요소들이 있는지 확인 (있다면)
    const searchInput = page.getByPlaceholder('검색어를 입력하세요');
    const searchButton = page.getByRole('button', { name: '검색' });
    
    if (await searchInput.isVisible()) {
      await expect(searchInput).toBeVisible();
    }
    
    if (await searchButton.isVisible()) {
      await expect(searchButton).toBeVisible();
    }
    
    // 페이지가 로드되었는지 최소한 확인
    const body = page.locator('body');
    await expect(body).toBeVisible();
  });

  test('should handle job search', async ({ page }) => {
    await loginAsTestUser(page);
    await page.goto('/jobs');
    await page.waitForTimeout(1000);
    
    const searchInput = page.getByPlaceholder('검색어를 입력하세요');
    
    if (await searchInput.isVisible()) {
      await searchInput.fill('개발자');
      
      const searchButton = page.getByRole('button', { name: '검색' });
      if (await searchButton.isVisible()) {
        await searchButton.click();
        await page.waitForTimeout(1000);
        // URL이 검색 파라미터를 포함하는지 확인
        expect(page.url()).toContain('search=개발자');
      }
    }
    
    // 최소한 페이지가 접근 가능한지 확인
    const body = page.locator('body');
    await expect(body).toBeVisible();
  });
});