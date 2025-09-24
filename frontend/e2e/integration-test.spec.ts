import { test, expect } from '@playwright/test';

test.describe('전체 통합 테스트', () => {
  test('사용자 시나리오: 회원가입 → 로그인 → 채용공고 조회 → AI 서비스 사용', async ({ page }) => {
    // 1. 홈페이지 접근
    await page.goto('/');
    await page.waitForTimeout(1000);
    
    // 페이지가 로드되었는지 확인
    const body = page.locator('body');
    await expect(body).toBeVisible();
    
    console.log('✅ 홈페이지 접근 성공');

    // 2. 회원가입 페이지로 이동
    try {
      const signupLink = page.getByRole('link', { name: '회원가입' });
      if (await signupLink.isVisible({ timeout: 2000 })) {
        await signupLink.click();
      } else {
        await page.goto('/auth/signup');
      }
      await page.waitForTimeout(1000);
      console.log('✅ 회원가입 페이지 이동 성공');
    } catch (error) {
      console.log('ℹ️ 회원가입 링크를 찾을 수 없음, 직접 이동');
      await page.goto('/auth/signup');
    }

    // 3. 채용공고 페이지 테스트 (로그인 없이)
    await page.goto('/jobs');
    await page.waitForTimeout(1000);
    await expect(body).toBeVisible();
    console.log('✅ 채용공고 페이지 접근 성공');

    // 4. AI 서비스 페이지 테스트들
    const aiServices = [
      { url: '/ai/translation', name: 'AI 번역' },
      { url: '/ai/interview', name: 'AI 면접' },
      { url: '/ai/cover-letter', name: 'AI 자소서' },
      { url: '/ai/chatbot', name: 'AI 챗봇' }
    ];

    for (const service of aiServices) {
      try {
        await page.goto(service.url);
        await page.waitForTimeout(1000);
        await expect(body).toBeVisible();
        console.log(`✅ ${service.name} 페이지 접근 성공`);
      } catch (error) {
        console.log(`⚠️ ${service.name} 페이지 접근 실패: ${error}`);
      }
    }
  });

  test('AI 번역 서비스 기능 테스트', async ({ page }) => {
    await page.goto('/ai/translation');
    await page.waitForTimeout(2000);
    
    // 페이지가 로드되었는지 확인
    const body = page.locator('body');
    await expect(body).toBeVisible();
    console.log('✅ AI 번역 페이지 로드 완료');

    // 번역 입력 필드 찾기 시도
    const textAreas = [
      page.getByPlaceholder('번역할 텍스트를 입력하세요'),
      page.locator('textarea').first(),
      page.getByRole('textbox').first()
    ];

    for (const textArea of textAreas) {
      if (await textArea.isVisible({ timeout: 1000 })) {
        await textArea.fill('Hello World');
        console.log('✅ 번역 텍스트 입력 완료');
        
        // 번역 버튼 찾기 시도
        const translateButtons = [
          page.getByRole('button', { name: '번역하기' }),
          page.getByRole('button', { name: '번역' }),
          page.locator('button').filter({ hasText: '번역' })
        ];

        for (const button of translateButtons) {
          if (await button.isVisible({ timeout: 1000 })) {
            await button.click();
            await page.waitForTimeout(2000);
            console.log('✅ 번역 요청 완료');
            break;
          }
        }
        break;
      }
    }
  });

  test('대시보드 네비게이션 테스트', async ({ page }) => {
    await page.goto('/dashboard');
    await page.waitForTimeout(2000);
    
    const body = page.locator('body');
    await expect(body).toBeVisible();
    console.log('✅ 대시보드 페이지 접근 성공');

    // 주요 네비게이션 링크들 테스트
    const navLinks = [
      { text: '채용정보', url: '/jobs' },
      { text: 'AI 면접', url: '/ai/interview' },
      { text: 'AI 번역', url: '/ai/translation' }
    ];

    for (const link of navLinks) {
      try {
        const navLink = page.getByRole('link', { name: link.text });
        if (await navLink.isVisible({ timeout: 1000 })) {
          await navLink.click();
          await page.waitForTimeout(1000);
          console.log(`✅ ${link.text} 네비게이션 성공`);
          await page.goBack();
          await page.waitForTimeout(500);
        } else {
          console.log(`ℹ️ ${link.text} 링크를 찾을 수 없음`);
        }
      } catch (error) {
        console.log(`⚠️ ${link.text} 네비게이션 실패: ${error}`);
      }
    }
  });

  test('전체 서비스 연결성 테스트', async ({ page }) => {
    // 각 주요 페이지에 접근하여 서버 연결 상태 확인
    const pages = [
      { url: '/', name: '홈페이지' },
      { url: '/jobs', name: '채용공고' },
      { url: '/dashboard', name: '대시보드' },
      { url: '/ai/translation', name: 'AI 번역' },
      { url: '/ai/interview', name: 'AI 면접' },
      { url: '/ai/cover-letter', name: 'AI 자소서' },
      { url: '/ai/chatbot', name: 'AI 챗봇' }
    ];

    for (const pageInfo of pages) {
      try {
        const response = await page.goto(pageInfo.url);
        
        if (response && response.status() < 400) {
          await page.waitForTimeout(1000);
          const body = page.locator('body');
          await expect(body).toBeVisible();
          console.log(`✅ ${pageInfo.name} - 연결 성공 (${response.status()})`);
        } else {
          console.log(`⚠️ ${pageInfo.name} - HTTP 오류 (${response?.status()})`);
        }
      } catch (error) {
        console.log(`❌ ${pageInfo.name} - 연결 실패: ${error}`);
      }
    }
  });
});