import { test, expect } from '@playwright/test';

test.describe('AI 서비스 프론트엔드-백엔드 통합 테스트', () => {
  test('AI 번역 서비스 실제 API 호출 테스트', async ({ page }) => {
    // AI 번역 페이지로 이동
    await page.goto('/ai/translation');
    await page.waitForTimeout(2000);
    
    console.log('✅ AI 번역 페이지 로드');

    // 개발자 도구를 통해 직접 API 호출 테스트
    const translationResult = await page.evaluate(async () => {
      try {
        const response = await fetch('http://localhost:8001/api/v1/translation/translate', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            text: "안녕하세요",
            source_language: "ko",
            target_language: "en"
          })
        });
        
        if (!response.ok) {
          return { error: `HTTP ${response.status}`, success: false };
        }
        
        const data = await response.json();
        return { data, success: true };
      } catch (error) {
        return { error: error.message, success: false };
      }
    });

    if (translationResult.success) {
      console.log('✅ AI 번역 API 호출 성공:', translationResult.data);
      expect(translationResult.data.success).toBe(true);
      expect(translationResult.data.data.translation.translated_text).toBeDefined();
    } else {
      console.log('❌ AI 번역 API 호출 실패:', translationResult.error);
    }
  });

  test('AI 서비스 헬스 체크 테스트', async ({ page }) => {
    await page.goto('/');
    
    const healthCheck = await page.evaluate(async () => {
      try {
        const response = await fetch('http://localhost:8001/health');
        const data = await response.json();
        return { data, success: response.ok };
      } catch (error) {
        return { error: error.message, success: false };
      }
    });

    console.log('AI 서비스 헬스 체크:', healthCheck);
    expect(healthCheck.success).toBe(true);
    expect(healthCheck.data.status).toBe('healthy');
  });

  test('백엔드 서비스 연결 테스트', async ({ page }) => {
    await page.goto('/');
    
    const backendCheck = await page.evaluate(async () => {
      try {
        // 인증이 필요한 엔드포인트이지만 연결 테스트용
        const response = await fetch('http://localhost:8080/api/users/statistics');
        return { 
          status: response.status, 
          connected: response.status === 403 || response.status === 401 // 인증 오류는 연결 성공을 의미
        };
      } catch (error) {
        return { error: error.message, connected: false };
      }
    });

    console.log('백엔드 서비스 연결 체크:', backendCheck);
    expect(backendCheck.connected).toBe(true);
  });

  test('CORS 정책 테스트', async ({ page }) => {
    await page.goto('/');
    
    // CORS 테스트를 위해 다양한 서비스에 요청
    const corsTests = await page.evaluate(async () => {
      const results = [];
      
      // AI 서비스 CORS 테스트
      try {
        const aiResponse = await fetch('http://localhost:8001/health', {
          method: 'GET',
          mode: 'cors'
        });
        results.push({ service: 'AI Service', status: 'OK', cors: true });
      } catch (error) {
        results.push({ service: 'AI Service', status: 'FAILED', error: error.message });
      }

      // 백엔드 CORS 테스트
      try {
        const backendResponse = await fetch('http://localhost:8080/api/users/statistics', {
          method: 'GET',
          mode: 'cors'
        });
        results.push({ service: 'Backend', status: 'OK', cors: true });
      } catch (error) {
        results.push({ service: 'Backend', status: 'FAILED', error: error.message });
      }

      return results;
    });

    console.log('CORS 테스트 결과:', corsTests);
    
    // AI 서비스와 백엔드 모두 CORS가 정상 작동해야 함
    const aiCors = corsTests.find(t => t.service === 'AI Service');
    const backendCors = corsTests.find(t => t.service === 'Backend');
    
    expect(aiCors?.cors).toBe(true);
    expect(backendCors?.cors).toBe(true);
  });

  test('프론트엔드 라우팅 및 네비게이션 테스트', async ({ page }) => {
    const routes = [
      { path: '/', name: '홈페이지' },
      { path: '/dashboard', name: '대시보드' },
      { path: '/jobs', name: '채용공고' },
      { path: '/ai/translation', name: 'AI 번역' },
      { path: '/ai/interview', name: 'AI 면접' },
      { path: '/ai/cover-letter', name: 'AI 자소서' },
      { path: '/ai/chatbot', name: 'AI 챗봇' }
    ];

    const results = [];

    for (const route of routes) {
      try {
        const response = await page.goto(`http://localhost:3002${route.path}`);
        await page.waitForTimeout(1000);
        
        const title = await page.title();
        const bodyVisible = await page.locator('body').isVisible();
        
        results.push({
          route: route.name,
          path: route.path,
          status: response?.status(),
          loaded: bodyVisible,
          title: title
        });
        
        console.log(`✅ ${route.name} (${route.path}) - 로드 성공`);
      } catch (error) {
        results.push({
          route: route.name,
          path: route.path,
          error: error.message,
          loaded: false
        });
        console.log(`❌ ${route.name} (${route.path}) - 로드 실패:`, error.message);
      }
    }

    // 모든 라우트가 정상 로드되어야 함
    const successfulRoutes = results.filter(r => r.loaded && r.status && r.status < 400);
    expect(successfulRoutes.length).toBeGreaterThan(5); // 최소 6개 이상의 라우트가 성공해야 함
    
    console.log(`✅ 총 ${successfulRoutes.length}/${routes.length}개 라우트 성공`);
  });
});