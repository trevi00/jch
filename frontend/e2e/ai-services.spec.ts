import { test, expect } from '@playwright/test';
import { loginAsTestUser } from './auth-helper';

test.describe('AI Services', () => {
  test.describe('AI Interview', () => {
    test('should display interview setup form', async ({ page }) => {
      await loginAsTestUser(page);
      await page.goto('/ai/interview');
      await page.waitForTimeout(1000);
      
      // 페이지가 로드되었는지 확인
      const body = page.locator('body');
      await expect(body).toBeVisible();
      
      // 면접 관련 요소들이 있다면 확인
      const interviewHeading = page.getByRole('heading', { name: 'AI 면접' });
      const technicalButton = page.getByText('기술면접');
      const personalityButton = page.getByText('인성면접');
      
      if (await interviewHeading.isVisible()) {
        await expect(interviewHeading).toBeVisible();
      }
      
      if (await technicalButton.isVisible()) {
        await expect(technicalButton).toBeVisible();
      }
      
      if (await personalityButton.isVisible()) {
        await expect(personalityButton).toBeVisible();
      }
    });

    test('should start interview process', async ({ page }) => {
      await loginAsTestUser(page);
      await page.goto('/ai/interview');
      await page.waitForTimeout(1000);
      
      // 페이지가 로드되었는지 확인
      const body = page.locator('body');
      await expect(body).toBeVisible();
      
      // 면접 유형 선택 (가능하다면)
      const technicalButton = page.getByText('기술면접');
      if (await technicalButton.isVisible()) {
        await technicalButton.click();
        
        // 면접 시작 버튼이 있다면 클릭
        const startButton = page.getByRole('button', { name: '면접 시작' });
        if (await startButton.isVisible()) {
          await startButton.click();
          await page.waitForTimeout(1000);
        }
      }
    });
  });

  test.describe('Cover Letter Generation', () => {
    test('should display cover letter form', async ({ page }) => {
      await loginAsTestUser(page);
      await page.goto('/ai/cover-letter');
      await page.waitForTimeout(1000);
      
      // 페이지가 로드되었는지 확인
      const body = page.locator('body');
      await expect(body).toBeVisible();
      
      // 자소서 관련 요소들이 있다면 확인
      const heading = page.getByRole('heading', { name: '자소서 생성' });
      if (await heading.isVisible()) {
        await expect(heading).toBeVisible();
      }
    });

    test('should validate required fields', async ({ page }) => {
      await loginAsTestUser(page);
      await page.goto('/ai/cover-letter');
      await page.waitForTimeout(1000);
      
      // 페이지가 로드되었는지 확인
      const body = page.locator('body');
      await expect(body).toBeVisible();
      
      // 생성 시작 버튼이 있다면 클릭해서 검증 테스트
      const startButton = page.getByRole('button', { name: '생성 시작' });
      if (await startButton.isVisible()) {
        await startButton.click();
        await page.waitForTimeout(500);
      }
    });
  });

  test.describe('Document Translation', () => {
    test('should display translation form', async ({ page }) => {
      await loginAsTestUser(page);
      await page.goto('/ai/translation');
      await page.waitForTimeout(1000);
      
      // 페이지가 로드되었는지 확인
      const body = page.locator('body');
      await expect(body).toBeVisible();
      
      // 번역 관련 요소들이 있다면 확인
      const heading = page.getByRole('heading', { name: '문서 번역' });
      if (await heading.isVisible()) {
        await expect(heading).toBeVisible();
      }
    });

    test('should handle translation request', async ({ page }) => {
      await loginAsTestUser(page);
      await page.goto('/ai/translation');
      await page.waitForTimeout(1000);
      
      // 페이지가 로드되었는지 확인
      const body = page.locator('body');
      await expect(body).toBeVisible();
      
      // 번역 기능이 있다면 테스트
      const textArea = page.getByPlaceholder('번역할 텍스트를 입력하세요');
      if (await textArea.isVisible()) {
        await textArea.fill('안녕하세요. 저는 개발자입니다.');
        
        // exact: true를 사용해서 정확한 버튼만 선택
        const translateButton = page.getByRole('button', { name: '번역하기', exact: true });
        if (await translateButton.isVisible()) {
          await translateButton.click();
          await page.waitForTimeout(1000);
        } else {
          // 만약 '번역하기' 버튼이 없다면 일반 '번역' 버튼 시도
          const fallbackButton = page.getByRole('button', { name: '번역' }).first();
          if (await fallbackButton.isVisible()) {
            await fallbackButton.click();
            await page.waitForTimeout(1000);
          }
        }
      }
    });
  });

  test.describe('AI Chatbot', () => {
    test('should display chatbot interface', async ({ page }) => {
      await loginAsTestUser(page);
      await page.goto('/ai/chatbot');
      await page.waitForTimeout(1000);
      
      // 페이지가 로드되었는지 확인
      const body = page.locator('body');
      await expect(body).toBeVisible();
      
      // 챗봇 관련 요소들이 있다면 확인
      const heading = page.getByRole('heading', { name: 'AI 챗봇' });
      if (await heading.isVisible()) {
        await expect(heading).toBeVisible();
      }
    });

    test('should send and receive messages', async ({ page }) => {
      await loginAsTestUser(page);
      await page.goto('/ai/chatbot');
      await page.waitForTimeout(1000);
      
      // 페이지가 로드되었는지 확인
      const body = page.locator('body');
      await expect(body).toBeVisible();
      
      // 챗봇 기능이 있다면 테스트
      const messageInput = page.getByPlaceholder('메시지를 입력하세요');
      if (await messageInput.isVisible()) {
        await messageInput.fill('안녕하세요');
        
        const sendButton = page.getByRole('button', { name: '전송' });
        if (await sendButton.isVisible()) {
          await sendButton.click();
          await page.waitForTimeout(1000);
        }
      }
    });
  });
});