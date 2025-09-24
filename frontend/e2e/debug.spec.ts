import { test, expect } from '@playwright/test';

test.describe('Debug Test', () => {
  test('should load page and check console errors', async ({ page }) => {
    // 콘솔 에러를 캐치
    const errors: string[] = [];
    page.on('console', (msg) => {
      if (msg.type() === 'error') {
        errors.push(msg.text());
      }
    });

    // 네트워크 요청 실패를 캐치
    const failedRequests: string[] = [];
    page.on('response', (response) => {
      if (!response.ok()) {
        failedRequests.push(`${response.status()} ${response.url()}`);
      }
    });

    await page.goto('/');
    
    // 페이지가 로드될 때까지 잠시 기다리기
    await page.waitForTimeout(3000);

    // HTML 구조 확인
    const html = await page.content();
    console.log('Page HTML:', html);
    
    // Root 요소 내용 확인
    const rootContent = await page.evaluate(() => {
      const root = document.getElementById('root');
      return {
        exists: !!root,
        innerHTML: root?.innerHTML || '',
        style: root ? getComputedStyle(root).display : 'not found'
      };
    });
    console.log('Root element info:', rootContent);

    // JavaScript 파일이 로드되었는지 확인
    const scripts = await page.evaluate(() => {
      return Array.from(document.querySelectorAll('script')).map(s => s.src);
    });
    console.log('Scripts loaded:', scripts);

    // CSS 파일이 로드되었는지 확인
    const stylesheets = await page.evaluate(() => {
      return Array.from(document.querySelectorAll('link[rel="stylesheet"]')).map(l => l.href);
    });
    console.log('Stylesheets loaded:', stylesheets);

    // 에러가 있으면 로그 출력
    if (errors.length > 0) {
      console.log('Console errors:', errors);
    }
    
    if (failedRequests.length > 0) {
      console.log('Failed requests:', failedRequests);
    }

    // 기본적인 검증
    await expect(page).toHaveTitle(/잡았다/);
  });
});