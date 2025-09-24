import { api } from './index';

export interface TranslationRequest {
  sourceText: string;
  sourceLanguage: string;
  targetLanguage: string;
  documentType: string;
}

export interface TranslationResponse {
  id: number;
  sourceText: string;
  translatedText: string;
  sourceLanguage: string;
  targetLanguage: string;
  documentType: string;
  status: 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'FAILED';
  errorMessage?: string;
  characterCount: number;
  cost?: number;
  createdAt: string;
  processedAt?: string;
}

export const translationAPI = {
  // 번역 실행
  translate: (data: TranslationRequest) =>
    api.post<TranslationResponse>('/api/v1/translation/translate', data),

  // 번역 기록 조회
  getHistory: (page = 0, size = 20) =>
    api.get<TranslationResponse[]>('/api/v1/translation/history', {
      params: { page, size }
    }),

  // 특정 번역 요청 조회
  getTranslation: (id: number) =>
    api.get<TranslationResponse>(`/api/v1/translation/${id}`),

  // 지원 언어 목록 조회
  getSupportedLanguages: () =>
    api.get<{ code: string; name: string; nativeName: string }[]>('/api/v1/translation/supported-languages'),

  // 문서 템플릿 조회
  getTemplates: (documentType: string, targetLanguage: string) =>
    api.get<{ template: string; placeholders: string[] }>('/api/v1/translation/templates', {
      params: { document_type: documentType, target_language: targetLanguage }
    }),

  // 번역 비용 예상
  estimateCost: (data: Omit<TranslationRequest, 'sourceText'> & { characterCount: number }) =>
    api.post<{ estimatedCost: number; currency: string }>('/api/v1/translation/estimate-cost', data),

  // 번역 삭제
  deleteTranslation: (id: number) =>
    api.delete(`/api/v1/translation/${id}`),
};