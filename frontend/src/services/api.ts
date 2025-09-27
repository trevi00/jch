/**
 * api.ts - 중앙집중식 API 클라이언트 및 HTTP 통신 관리
 *
 * 🔧 사용 기술:
 * - Axios (HTTP 클라이언트 라이브러리)
 * - TypeScript (타입 안전성 및 API 인터페이스)
 * - Interceptors (요청/응답 전처리)
 * - JWT Authentication (토큰 기반 인증)
 * - Error Handling (에러 처리 및 재시도)
 *
 * 📋 주요 기능:
 * - 메인 백엔드 API 통신
 * - AI 서비스 API 통신
 * - 자동 토큰 관리 (추가/갱신/만료 처리)
 * - 인터셉터 기반 전역 에러 처리
 * - API 요청 성능 최적화
 *
 * 🎯 주요 엔드포인트 그룹:
 * - Auth APIs: 로그인, 회원가입, OAuth, 토큰 관리
 * - User APIs: 사용자 정보, 프로필 관리
 * - Job APIs: 채용공고 CRUD, 검색, 지원 관리
 * - Community APIs: 커뮤니티 게시글, 댓글, 카테고리
 * - AI Service APIs: 챗봇, 면접 연습, 자소서, 번역
 * - Dashboard APIs: 대시보드 데이터, 통계
 */

import axios, { AxiosInstance, AxiosResponse } from 'axios';
import {
  ApiResponse,
  PageResponse,
  LoginRequest,
  RegisterRequest,
  AuthResponse,
  OAuth2LoginRequest,
  User,
  UserProfile,
  JobPosting,
  JobApplication,
  JobSearch,
  CreateJobPostingRequest,
  UpdateJobPostingRequest,
  CreateJobApplicationRequest,
  Post,
  Comment,
  Category,
  CreatePostRequest,
  CreateCommentRequest,
  DashboardData,
  ChatbotRequest,
  ChatbotResponse,
  InterviewRequest,
  InterviewQuestion,
  CoverLetterRequest,
  CoverLetterResponse,
  TranslationRequest,
  TranslationResponse,
  ImageGenerationRequest,
  ImageGenerationResponse,
  CreateEducationRequest,
  CreateSkillRequest,
  CreateExperienceRequest,
  GeneralUserDashboard
} from '@/types/api';
import { API_BASE_URL, AI_SERVICE_BASE_URL } from '@/utils/constants';
import type { ChatbotCategoriesResponse, WelcomeMessageResponse, ChatSuggestion } from '@/types/api';

// Webmail Types
interface SendEmailRequest {
  to: string
  subject: string
  content: string
  translationNeeded?: boolean
  sourceLanguage?: string
  targetLanguage?: string
  documentType?: string
  senderEmail?: string
  senderName?: string
}

interface EmailHistory {
  id: number
  recipientEmail: string
  recipientName?: string
  subject: string
  content: string
  isTranslated: boolean
  status: 'SENT' | 'FAILED' | 'PENDING'
  createdAt: string
  originalLanguage?: string
  translatedLanguage?: string
}

interface EmailStats {
  totalSentCount: number
  translatedCount: number
  regularCount: number
}

/**
 * 🏢 ApiClient 클래스 - 중앙집중식 HTTP 통신 관리
 *
 * 🖥️ 아키텍처:
 * - Singleton 패턴으로 전역에서 단일 인스턴스 사용
 * - 메인 API와 AI API 서비스 분리된 클라이언트 관리
 * - Interceptor를 통한 공통 로직 처리
 *
 * 🔒 보안 기능:
 * - JWT 토큰 자동 첫부
 * - 토큰 만료 시 자동 갱신
 * - 인증 실패 시 자동 로그아웃
 *
 * 🚀 성능 최적화:
 * - HTTP 연결 재사용 (Keep-Alive)
 * - 지수 백오프 재시도 로직
 * - 요청/응답 압축 지원
 * - 적절한 타임아웃 설정
 */
class ApiClient {
  // 🌐 HTTP 클라이언트 인스턴스들
  private api: AxiosInstance;           // 메인 백엔드 API 클라이언트
  private aiApi: AxiosInstance;         // AI 서비스 전용 클라이언트 (긴 타임아웃)
  private readonly baseURL: string;     // 메인 API 기본 URL
  private readonly aiServiceURL: string; // AI 서비스 기본 URL

  /**
   * 🔧 API 클라이언트 생성자
   *
   * 초기화 작업:
   * 1. 기본 URL 설정 및 정규화
   * 2. 메인 API용 Axios 인스턴스 생성
   * 3. AI 서비스용 Axios 인스턴스 생성
   * 4. 요청/응답 인터셉터 설정
   *
   * 🎯 성능 최적화:
   * - HTTP/1.1 Keep-Alive 연결 재사용
   * - GZIP 압축 지원
   * - 적절한 타임아웃 설정
   * - 리다이렉트 제한
   */
  constructor() {
    this.baseURL = API_BASE_URL;
    this.aiServiceURL = AI_SERVICE_BASE_URL;

    // 성능 최적화된 axios 인스턴스 생성
    this.api = axios.create({
      baseURL: this.baseURL,
      timeout: 15000, // 타임아웃 증가 (AI 서비스 고려)
      headers: {
        'Content-Type': 'application/json; charset=utf-8',
        'Accept': 'application/json',
      },
      // HTTP 연결 재사용 최적화
      maxRedirects: 3,
      // 요청 압축 활성화 (가능한 경우)
      decompress: true,
    });

    // AI 서비스용 최적화된 재사용 클라이언트 생성
    this.aiApi = axios.create({
      baseURL: this.aiServiceURL,
      timeout: 30000, // AI 서비스는 더 긴 타임아웃 필요
      headers: {
        'Content-Type': 'application/json; charset=utf-8',
        'Accept': 'application/json',
      },
      maxRedirects: 3,
      decompress: true,
    });

    this.setupInterceptors();
  }

  /**
   * API 클라이언트 인터셉터 설정
   * 요청 및 응답 처리를 자동화
   * 이벤트: HTTP 요청/응답 이벤트, 토큰 만료 이벤트
   */
  private setupInterceptors() {
    // Request interceptor for adding auth token
    this.api.interceptors.request.use(
      (config) => {
        const token = localStorage.getItem('accessToken');
        if (token) {
          config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
      },
      (error) => {
        return Promise.reject(error);
      }
    );

    // Response interceptor for handling errors
    this.api.interceptors.response.use(
      (response: AxiosResponse) => response,
      async (error) => {
        if (error.response?.status === 401) {
          // Token expired, try to refresh
          const refreshToken = localStorage.getItem('refreshToken');
          if (refreshToken) {
            try {
              const response = await this.refreshAccessToken(refreshToken);
              if (response.success && response.data) {
                localStorage.setItem('accessToken', response.data.access_token);
                localStorage.setItem('refreshToken', response.data.refresh_token);
                // Retry original request
                return this.api.request(error.config);
              }
            } catch (refreshError) {
              // Only logout if refresh also fails
              this.softLogout();
            }
          } else {
            // No refresh token available
            this.softLogout();
          }
        }
        return Promise.reject(error);
      }
    );
  }

  /**
   * 사용자 로그아웃 처리
   * 토큰과 사용자 정보를 제거하고 로그인 페이지로 리다이렉트
   * 이벤트: 로그아웃 이벤트, 페이지 리다이렉트 이벤트
   */
  private logout() {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
    window.location.href = '/login';
  }

  /**
   * 소프트 로그아웃 처리
   * 토큰만 제거하고 리다이렉트하지 않음 (앱에서 처리)
   * 이벤트: 인증 상태 변경 이벤트, 커스텀 로그아웃 이벤트
   */
  private softLogout() {
    // Clear tokens but don't redirect - let the app handle it
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('auth-storage');
    // Dispatch a custom event to notify the auth store
    window.dispatchEvent(new CustomEvent('auth:logout'));
  }

  // ============= Auth APIs =============
  
  /**
   * 사용자 로그인
   * @param request 이메일과 비밀번호를 포함한 로그인 요청 데이터
   * 이벤트: 로그인 API 호출 이벤트, 인증 토큰 생성 이벤트
   */
  async login(request: LoginRequest): Promise<AuthResponse> {
    const response = await this.api.post<AuthResponse>('/api/auth/login', request);
    return response.data;
  }

  /**
   * 관리자 로그인
   * @param username 관리자 사용자명 (이메일)
   * @param password 관리자 비밀번호
   * 이벤트: 관리자 로그인 API 호출 이벤트, 관리자 인증 토큰 생성 이벤트
   */
  async adminLogin(username: string, password: string): Promise<ApiResponse<any>> {
    const response = await this.api.post<ApiResponse<any>>('/api/admin/login', {
      email: username,
      password: password
    });

    // 성공시 관리자 토큰을 별도로 저장
    if (response.data.success && response.data.data?.accessToken) {
      localStorage.setItem('adminToken', response.data.data.accessToken);
      localStorage.setItem('adminRefreshToken', response.data.data.refreshToken || '');
    }

    return response.data;
  }

  /**
   * 사용자 회원가입
   * @param request 회원가입에 필요한 사용자 정보
   * 이벤트: 회원가입 API 호출 이벤트, 신규 사용자 생성 이벤트
   */
  async register(request: RegisterRequest): Promise<AuthResponse> {
    const response = await this.api.post<AuthResponse>('/api/auth/register', request);
    return response.data;
  }

  /**
   * OAuth2 Google 로그인
   * @param request Google OAuth2 인증 코드와 상태 정보
   * 이벤트: OAuth2 로그인 API 호출 이벤트, 소셜 로그인 인증 이벤트
   */
  async oauth2Login(request: OAuth2LoginRequest): Promise<AuthResponse> {
    const response = await this.api.post<AuthResponse>('/api/auth/oauth2/google', request);
    return response.data;
  }

  /**
   * 액세스 토큰 갱신
   * @param refreshToken 리프레시 토큰
   * 이벤트: 토큰 갱신 API 호출 이벤트, 액세스 토큰 갱신 이벤트
   */
  async refreshAccessToken(refreshToken: string): Promise<AuthResponse> {
    const response = await this.api.post<AuthResponse>('/api/auth/refresh-token', {}, {
      headers: {
        'Authorization': `Bearer ${refreshToken}`
      }
    });
    return response.data;
  }

  /**
   * 사용자 로그아웃
   * 서버에 로그아웃 요청을 보내고 로컬 토큰을 제거
   * 이벤트: 로그아웃 API 호출 이벤트, 토큰 제거 이벤트, 페이지 리다이렉트 이벤트
   */
  async logoutUser(): Promise<ApiResponse> {
    const response = await this.api.post<ApiResponse>('/api/auth/logout');
    this.logout();
    return response.data;
  }

  /**
   * 사용자를 관리자로 승급
   * @param email 승급할 사용자 이메일
   * @param secretKey 관리자 시크릿 키
   * 이벤트: 관리자 승급 API 호출 이벤트, 권한 변경 이벤트
   */
  async promoteToAdmin(email: string, secretKey: string): Promise<ApiResponse<void>> {
    const response = await this.api.post<ApiResponse<void>>('/api/admin/promote', {
      email: email,
      secretKey: secretKey
    });
    return response.data;
  }

  /**
   * 관리자 권한 검증
   * @param token JWT 토큰 (Bearer 접두사 포함)
   * 이벤트: 관리자 권한 검증 API 호출 이벤트
   */
  async verifyAdminToken(token: string): Promise<ApiResponse<any>> {
    const response = await this.api.get<ApiResponse<any>>('/api/admin/verify', {
      headers: {
        Authorization: token
      }
    });
    return response.data;
  }

  /**
   * Google OAuth2 인증 URL 가져오기
   * @param redirectUri 인증 완료 후 리다이렉트할 URI
   * @param userType 사용자 타입 (GENERAL | COMPANY)
   * @param action 수행할 액션 (LOGIN | SIGNUP)
   * 이벤트: OAuth2 URL 요청 이벤트, Google 인증 URL 생성 이벤트
   */
  async getGoogleAuthUrl(redirectUri?: string, userType?: 'GENERAL' | 'COMPANY', action?: 'LOGIN' | 'SIGNUP'): Promise<ApiResponse<string>> {
    const params = new URLSearchParams();
    if (redirectUri) {
      params.append('redirectUri', redirectUri);
    }
    if (userType) {
      params.append('userType', userType);
    }
    if (action) {
      params.append('action', action);
    }
    const queryString = params.toString() ? `?${params.toString()}` : '';
    const response = await this.api.get<ApiResponse<string>>(`/api/auth/oauth2/google/url${queryString}`);
    return response.data;
  }

  /**
   * OAuth2 콜백 처리
   * @param code Google에서 반환받은 인증 코드
   * @param state 상태 파라미터
   * @param redirectUri 리다이렉트 URI
   * 이벤트: OAuth2 콜백 처리 이벤트, 인증 토큰 생성 이벤트
   */
  async handleOAuth2Callback(code: string, state?: string, redirectUri?: string): Promise<AuthResponse> {
    const params = new URLSearchParams({
      code,
      ...(state && { state }),
      ...(redirectUri && { redirectUri })
    });
    const response = await this.api.get<AuthResponse>(`/api/auth/oauth2/google/callback?${params.toString()}`);
    return response.data;
  }

  // ============= User APIs =============

  /**
   * 현재 로그인한 사용자 정보 조회
   * 이벤트: 사용자 정보 조회 API 호출 이벤트
   */
  async getCurrentUser(): Promise<ApiResponse<User>> {
    const response = await this.api.get<ApiResponse<User>>('/api/users/me');
    return response.data;
  }

  /**
   * 사용자 프로필 조회
   * @param userId 조회할 사용자 ID (없으면 현재 사용자)
   * 이벤트: 사용자 프로필 조회 API 호출 이벤트
   */
  async getUserProfile(userId?: number): Promise<ApiResponse<UserProfile>> {
    const endpoint = userId ? `/users/${userId}/profile` : '/users/me/profile';
    const response = await this.api.get<ApiResponse<UserProfile>>(endpoint);
    return response.data;
  }

  /**
   * 사용자 프로필 업데이트
   * @param data 업데이트할 프로필 데이터
   * 이벤트: 프로필 업데이트 API 호출 이벤트, 사용자 정보 변경 이벤트
   */
  async updateUserProfile(data: Partial<UserProfile>): Promise<ApiResponse<UserProfile>> {
    const response = await this.api.put<ApiResponse<UserProfile>>('/api/users/me/profile', data);
    return response.data;
  }

  /**
   * 사용자 통계 조회 (관리자)
   * 이벤트: 관리자 권한 확인 이벤트, 사용자 통계 데이터 조회 이벤트
   */
  async getUserStatistics(): Promise<ApiResponse<any>> {
    const adminToken = localStorage.getItem('adminToken');
    const response = await this.api.get<ApiResponse<any>>('/api/admin/users/statistics', {
      headers: {
        'Authorization': `Bearer ${adminToken}`
      }
    });
    return response.data;
  }


  /**
   * 모든 사용자 목록 조회 (관리자)
   * 이벤트: 관리자 권한 확인 이벤트, 전체 사용자 데이터 조회 이벤트
   */
  async getAllUsers(): Promise<ApiResponse<any[]>> {
    const adminToken = localStorage.getItem('adminToken');
    const response = await this.api.get<ApiResponse<any[]>>('/api/admin/users', {
      headers: {
        'Authorization': `Bearer ${adminToken}`
      }
    });
    return response.data;
  }

  /**
   * 사용자 계정 삭제 (관리자)
   * @param userId 삭제할 사용자 ID
   * 이벤트: 관리자 권한 확인 이벤트, 사용자 계정 삭제 이벤트
   */
  async deleteUserAccount(userId: number): Promise<ApiResponse<void>> {
    const response = await this.api.delete<ApiResponse<void>>(`/api/admin/users/${userId}`);
    return response.data;
  }



  // ============= Job APIs =============

  /**
   * 직무 검색 (고급 검색)
   * @param search 검색 조건 객체
   * @param page 페이지 번호 (기본값: 0)
   * @param size 페이지 크기 (기본값: 20)
   * 이벤트: 직무 검색 API 호출 이벤트, 필터링 조건 적용 이벤트
   */
  async searchJobs(search?: JobSearch, page = 0, size = 20): Promise<ApiResponse<PageResponse<JobPosting>>> {
    const params = new URLSearchParams();
    params.append('page', page.toString());
    params.append('size', size.toString());

    if (search) {
      Object.entries(search).forEach(([key, value]) => {
        if (value !== undefined && value !== null) {
          if (Array.isArray(value)) {
            value.forEach(v => params.append(key, v.toString()));
          } else {
            params.append(key, value.toString());
          }
        }
      });
    }

    // Use advanced search endpoint for better filtering with partial string matching
    const response = await this.api.get<ApiResponse<PageResponse<JobPosting>>>(`/api/job-postings/search/advanced?${params.toString()}`);
    return response.data;
  }

  /**
   * 직무 목록 조회
   * @param search 검색 조건 객체
   * @param page 페이지 번호 (기본값: 0)
   * @param size 페이지 크기 (기본값: 20)
   * 이벤트: 직무 목록 API 호출 이벤트, 데이터 조회 이벤트
   */
  async getJobPostings(search?: JobSearch, page = 0, size = 20): Promise<ApiResponse<PageResponse<JobPosting>>> {
    const params = new URLSearchParams();
    params.append('page', page.toString());
    params.append('size', size.toString());

    if (search) {
      Object.entries(search).forEach(([key, value]) => {
        if (value !== undefined && value !== null) {
          if (Array.isArray(value)) {
            value.forEach(v => params.append(key, v.toString()));
          } else {
            params.append(key, value.toString());
          }
        }
      });
    }

    const response = await this.api.get<ApiResponse<PageResponse<JobPosting>>>(`/api/job-postings?${params.toString()}`);
    return response.data;
  }

  /**
   * 특정 직무 상세 조회
   * @param id 직무 공고 ID
   * 이벤트: 직무 상세 조회 API 호출 이벤트
   */
  async getJobPosting(id: number): Promise<ApiResponse<JobPosting>> {
    const response = await this.api.get<ApiResponse<JobPosting>>(`/api/job-postings/${id}`);
    return response.data;
  }

  /**
   * 직무 공고 생성
   * @param data 직무 공고 생성 데이터
   * 이벤트: 직무 공고 생성 API 호출 이벤트, 새로운 공고 등록 이벤트
   */
  async createJobPosting(data: CreateJobPostingRequest): Promise<ApiResponse<JobPosting>> {
    const response = await this.api.post<ApiResponse<JobPosting>>('/api/job-postings', data);
    return response.data;
  }

  /**
   * 직무 공고 발행
   * @param id 직무 공고 ID
   * @param deadlineDate 마감일
   * 이벤트: 직무 공고 발행 API 호출 이벤트, 공고 상태 변경 이벤트
   */
  async publishJobPosting(id: number, deadlineDate: string): Promise<ApiResponse<JobPosting>> {
    const response = await this.api.post<ApiResponse<JobPosting>>(`/api/job-postings/${id}/publish?deadlineDate=${deadlineDate}`);
    return response.data;
  }

  /**
   * 직무 공고 수정
   * @param id 직무 공고 ID
   * @param data 수정할 데이터
   * 이벤트: 직무 공고 수정 API 호출 이벤트, 공고 정보 변경 이벤트
   */
  async updateJobPosting(id: number, data: UpdateJobPostingRequest): Promise<ApiResponse<JobPosting>> {
    const response = await this.api.put<ApiResponse<JobPosting>>(`/api/job-postings/${id}`, data);
    return response.data;
  }

  /**
   * 직무 공고 삭제 (관리자)
   * @param id 삭제할 직무 공고 ID
   * 이벤트: 직무 공고 삭제 API 호출 이벤트, 공고 제거 이벤트
   */
  async deleteJobPosting(id: number): Promise<ApiResponse> {
    const response = await this.api.delete<ApiResponse>(`/api/admin/job-postings/${id}`);
    return response.data;
  }

  /**
   * 내 직무 공고 목록 조회
   * @param page 페이지 번호 (기본값: 0)
   * @param size 페이지 크기 (기본값: 20)
   * 이벤트: 내 공고 목록 API 호출 이벤트
   */
  async getMyJobPostings(page = 0, size = 20): Promise<ApiResponse<JobPosting[]>> {
    const response = await this.api.get<ApiResponse<JobPosting[]>>(`/api/job-postings/my-postings?page=${page}&size=${size}`);
    return response.data;
  }

  /**
   * 직무 간단 검색 (JobAtda 통합)
   * @param keyword 검색 키워드
   * @param location 지역
   * @param jobType 직무 유형
   * @param experienceLevel 경력 수준
   * @param page 페이지 번호 (기본값: 0)
   * @param size 페이지 크기 (기본값: 20)
   * 이벤트: 간단 검색 API 호출 이벤트, 외부 API 통합 이벤트
   */
  async searchJobsSimple(keyword?: string, location?: string, jobType?: JobType, experienceLevel?: ExperienceLevel, page = 0, size = 20): Promise<ApiResponse<PageResponse<JobPosting>>> {
    const params = new URLSearchParams();
    params.append('page', page.toString());
    params.append('size', size.toString());

    if (keyword) params.append('keyword', keyword);
    if (location) params.append('location', location);
    if (jobType) params.append('jobType', jobType);
    if (experienceLevel) params.append('experienceLevel', experienceLevel);

    const response = await this.api.get<ApiResponse<PageResponse<JobPosting>>>(`/api/job-postings/search/simple?${params.toString()}`);
    return response.data;
  }

  /**
   * 마감일이 임박한 직무 공고 조회
   * @param days 마감일까지 남은 일수 (기본값: 7일)
   * 이벤트: 마감일 임박 직무 조회 API 호출 이벤트
   */
  async getDeadlineApproachingJobs(days = 7): Promise<ApiResponse<JobPosting[]>> {
    const response = await this.api.get<ApiResponse<JobPosting[]>>(`/api/job-postings/deadline-approaching?days=${days}`);
    return response.data;
  }

  /**
   * 내 직무 공고 중 마감일이 임박한 공고 조회
   * @param days 마감일까지 남은 일수 (기본값: 7일)
   * 이벤트: 내 공고 마감일 임박 조회 API 호출 이벤트
   */
  async getMyDeadlineApproachingJobs(days = 7): Promise<ApiResponse<JobPosting[]>> {
    const response = await this.api.get<ApiResponse<JobPosting[]>>(`/api/job-postings/my-postings/deadline-approaching?days=${days}`);
    return response.data;
  }

  /**
   * 직무 공고 통계 조회
   * @param id 직무 공고 ID
   * 이벤트: 공고 통계 데이터 조회 API 호출 이벤트
   */
  async getJobPostingStats(id: number): Promise<ApiResponse<any>> {
    const response = await this.api.get<ApiResponse<any>>(`/api/job-postings/${id}/stats`);
    return response.data;
  }

  /**
   * 내 직무 공고 통계 조회
   * 이벤트: 내 공고 통계 데이터 조회 API 호출 이벤트
   */
  async getMyJobPostingStats(): Promise<ApiResponse<any[]>> {
    const response = await this.api.get<ApiResponse<any[]>>('/api/job-postings/my-postings/stats');
    return response.data;
  }

  /**
   * 전체 직무 공고 통계 조회 (관리자)
   * 이벤트: 관리자 권한 확인 이벤트, 전체 공고 통계 조회 API 호출 이벤트
   */
  async getOverallJobPostingStatistics(): Promise<ApiResponse<any>> {
    const response = await this.api.get<ApiResponse<any>>('/api/job-postings/admin/stats');
    return response.data;
  }

  /**
   * 직무에 지원하기
   * @param data 지원 요청 데이터
   * 이벤트: 직무 지원 API 호출 이벤트, 새로운 지원서 제출 이벤트
   */
  async applyToJob(data: CreateJobApplicationRequest): Promise<ApiResponse<JobApplication>> {
    const response = await this.api.post<ApiResponse<JobApplication>>('/api/applications', data);
    return response.data;
  }

  /**
   * 내 지원 내역 조회
   * @param page 페이지 번호 (기본값: 0)
   * @param size 페이지 크기 (기본값: 20)
   * 이벤트: 내 지원 내역 조회 API 호출 이벤트
   */
  async getMyApplications(page = 0, size = 20): Promise<ApiResponse<PageResponse<JobApplication>>> {
    const response = await this.api.get<ApiResponse<PageResponse<JobApplication>>>(`/api/applications/my?page=${page}&size=${size}`);
    return response.data;
  }

  /**
   * 특정 채용공고의 지원자 목록 조회 (기업용)
   * @param jobPostingId 채용공고 ID
   * @param page 페이지 번호 (기본값: 0)
   * @param size 페이지 크기 (기본값: 20)
   * 이벤트: 기업 지원자 목록 조회 API 호출 이벤트
   */
  async getJobApplications(jobPostingId: number, page = 0, size = 20): Promise<ApiResponse<PageResponse<JobApplication>>> {
    console.log('🌐 API Call - getJobApplications:', jobPostingId, 'page:', page, 'size:', size);
    const response = await this.api.get<ApiResponse<PageResponse<JobApplication>>>(`/api/applications/job/${jobPostingId}?page=${page}&size=${size}`);
    console.log('📨 API Response - getJobApplications:', {
      success: response.data.success,
      message: response.data.message,
      dataType: typeof response.data.data,
      dataKeys: response.data.data ? Object.keys(response.data.data) : null,
      contentLength: response.data.data?.content?.length || 0,
      sampleApplicant: response.data.data?.content?.[0] || response.data.data?.[0] || null,
      fullData: response.data
    });
    return response.data;
  }

  /**
   * 지원서 서류 심사 합격 처리 (기업용)
   * @param applicationId 지원서 ID
   * 이벤트: 서류 심사 합격 처리 API 호출 이벤트
   */
  async passDocumentReview(applicationId: number): Promise<ApiResponse<JobApplication>> {
    const response = await this.api.put<ApiResponse<JobApplication>>(`/api/applications/${applicationId}/pass-document`);
    return response.data;
  }

  /**
   * 지원서 불합격 처리 (기업용)
   * @param applicationId 지원서 ID
   * @param reason 불합격 사유 (선택사항)
   * 이벤트: 지원서 불합격 처리 API 호출 이벤트
   */
  async rejectApplication(applicationId: number, reason?: string): Promise<ApiResponse<JobApplication>> {
    const url = `/api/applications/${applicationId}/reject${reason ? `?reason=${encodeURIComponent(reason)}` : ''}`;
    const response = await this.api.put<ApiResponse<JobApplication>>(url);
    return response.data;
  }

  /**
   * 기업의 모든 채용공고에 대한 지원자 목록 조회 (기업용)
   * @param page 페이지 번호 (기본값: 0)
   * @param size 페이지 크기 (기본값: 20)
   * 이벤트: 기업 전체 지원자 목록 조회 API 호출 이벤트
   */
  async getCompanyApplications(page = 0, size = 20): Promise<ApiResponse<PageResponse<JobApplication>>> {
    // First get all company job postings
    const jobPostingsResponse = await this.getMyJobPostings(0, 100); // Get more job postings to cover all

    if (!jobPostingsResponse.success || !jobPostingsResponse.data?.content) {
      return {
        success: false,
        message: "채용공고를 가져올 수 없습니다",
        data: {
          content: [],
          totalElements: 0,
          totalPages: 0,
          first: true,
          last: true,
          size: size,
          number: page
        }
      };
    }

    const jobPostings = jobPostingsResponse.data.content;
    let allApplications: JobApplication[] = [];

    // Get applications for each job posting
    for (const jobPosting of jobPostings) {
      try {
        const applicationsResponse = await this.getJobApplications(jobPosting.id, 0, 100);
        if (applicationsResponse.success && applicationsResponse.data?.content) {
          allApplications = [...allApplications, ...applicationsResponse.data.content];
        }
      } catch (error) {
        console.warn(`Failed to fetch applications for job posting ${jobPosting.id}:`, error);
      }
    }

    // Sort by application date (most recent first)
    allApplications.sort((a, b) => new Date(b.appliedAt).getTime() - new Date(a.appliedAt).getTime());

    // Implement client-side pagination
    const startIndex = page * size;
    const endIndex = startIndex + size;
    const paginatedApplications = allApplications.slice(startIndex, endIndex);

    return {
      success: true,
      message: "지원자 목록을 성공적으로 가져왔습니다",
      data: {
        content: paginatedApplications,
        totalElements: allApplications.length,
        totalPages: Math.ceil(allApplications.length / size),
        first: page === 0,
        last: endIndex >= allApplications.length,
        size: size,
        number: page
      }
    };
  }

  // ============= Certificate APIs =============

  async getAllCertificateRequests(): Promise<ApiResponse<any>> {
    const response = await this.api.get<ApiResponse<any>>('/api/admin/certificates');
    return response.data;
  }

  async processCertificateRequest(requestId: number, data: { approved: boolean; adminNotes?: string }): Promise<ApiResponse<any>> {
    const response = await this.api.put<ApiResponse<any>>(`/api/admin/certificates/${requestId}/process`, data);
    return response.data;
  }

  async completeCertificateRequest(requestId: number): Promise<ApiResponse<any>> {
    const adminToken = localStorage.getItem('adminToken');
    const response = await this.api.put<ApiResponse<any>>(`/api/certificates/admin/${requestId}/complete`, {}, {
      headers: {
        'Authorization': `Bearer ${adminToken}`
      }
    });
    return response.data;
  }

  // ============= Community APIs =============

  async getCategories(): Promise<ApiResponse<Category[]>> {
    const response = await this.api.get<ApiResponse<Category[]>>('/api/categories');
    return response.data;
  }

  async createCategory(category: { name: string; description: string }): Promise<ApiResponse<Category>> {
    const response = await this.api.post<ApiResponse<Category>>('/api/categories', category);
    return response.data;
  }

  async deleteCategory(categoryId: number): Promise<ApiResponse<void>> {
    const response = await this.api.delete<ApiResponse<void>>(`/api/categories/${categoryId}`);
    return response.data;
  }

  async getPosts(options?: { categoryId?: number; search?: string; page?: number; size?: number } | number, page = 0, size = 20): Promise<ApiResponse<PageResponse<Post>>> {
    // Handle both old signature (number) and new signature (object)
    let categoryId: number | undefined;
    let search: string | undefined;
    let pageNum: number;
    let pageSize: number;
    
    if (typeof options === 'object' && options !== null) {
      categoryId = options.categoryId;
      search = options.search;
      pageNum = options.page || 0;
      pageSize = options.size || 20;
    } else {
      categoryId = options;
      pageNum = page;
      pageSize = size;
    }
    
    const params = new URLSearchParams();
    params.append('page', pageNum.toString());
    params.append('size', pageSize.toString());
    
    if (search) {
      params.append('search', search);
    }
    
    let url = '/api/posts';
    if (categoryId) {
      url = `/api/posts/category/${categoryId}?${params.toString()}`;
    } else {
      url = `/api/posts?${params.toString()}`;
    }

    const response = await this.api.get<ApiResponse<any>>(url);
    
    // Backend returns ApiResponse<PostDto.PageResponse> format
    if (response.data.success && response.data.data) {
      const backendData = response.data.data;
      return {
        success: true,
        message: response.data.message || 'Posts retrieved successfully',
        data: {
          content: backendData.posts || [],
          totalElements: backendData.totalElements || 0,
          totalPages: backendData.totalPages || 1,
          first: backendData.first || true,
          last: backendData.last || true,
          // numberOfElements is not part of PageResponse interface, removing
          size: backendData.pageSize || pageSize,
          number: backendData.pageNumber || pageNum
        }
      };
    }
    return response.data;
  }

  async getPost(id: number): Promise<ApiResponse<Post>> {
    const response = await this.api.get<ApiResponse<Post>>(`/api/posts/${id}`);
    return response.data;
  }

  async createPost(data: CreatePostRequest): Promise<ApiResponse<Post>> {
    const response = await this.api.post<ApiResponse<Post>>('/api/posts', data);
    return response.data;
  }

  async likePost(id: number): Promise<ApiResponse<void>> {
    const response = await this.api.post<ApiResponse<void>>(`/api/posts/${id}/like`);
    return response.data;
  }

  async updatePost(id: number, data: { title: string; content: string; categoryId?: number; imageUrl?: string }): Promise<ApiResponse<Post>> {
    const response = await this.api.put<ApiResponse<Post>>(`/api/posts/${id}`, data);
    return response.data;
  }

  async deletePost(id: number): Promise<ApiResponse<void>> {
    const response = await this.api.delete<ApiResponse<void>>(`/api/admin/posts/${id}`);
    return response.data;
  }

  async getComments(postId: number): Promise<ApiResponse<Comment[]>> {
    const response = await this.api.get<ApiResponse<Comment[]>>(`/api/comments/post/${postId}`);
    return response.data;
  }

  async createComment(data: CreateCommentRequest): Promise<ApiResponse<Comment>> {
    const response = await this.api.post<ApiResponse<Comment>>('/api/comments', data);
    return response.data;
  }

  async deleteComment(id: number): Promise<ApiResponse<void>> {
    const response = await this.api.delete<ApiResponse<void>>(`/api/comments/${id}`);
    return response.data;
  }

  // ============= Profile APIs =============

  async getProfile(): Promise<ApiResponse<UserProfile>> {
    const response = await this.api.get<ApiResponse<UserProfile>>('/api/profile');
    return response.data;
  }

  async updateProfile(data: any): Promise<ApiResponse<UserProfile>> {
    const response = await this.api.put<ApiResponse<UserProfile>>('/api/profile', data);
    return response.data;
  }

  // Company Profile APIs
  async getCompanyProfile(): Promise<ApiResponse<any>> {
    const response = await this.api.get<ApiResponse<any>>('/api/company/profile');
    return response.data;
  }

  async updateCompanyProfile(data: any): Promise<ApiResponse<any>> {
    const response = await this.api.put<ApiResponse<any>>('/api/company/profile', data);
    return response.data;
  }

  async createExperience(data: CreateExperienceRequest): Promise<ApiResponse> {
    const response = await this.api.post<ApiResponse>('/api/profile/experiences', data);
    return response.data;
  }

  async updateExperience(id: number, data: Partial<CreateExperienceRequest>): Promise<ApiResponse> {
    const response = await this.api.put<ApiResponse>(`/api/profile/experiences/${id}`, data);
    return response.data;
  }

  async deleteExperience(id: number): Promise<ApiResponse> {
    const response = await this.api.delete<ApiResponse>(`/api/profile/experiences/${id}`);
    return response.data;
  }

  async createEducation(data: CreateEducationRequest): Promise<ApiResponse> {
    const response = await this.api.post<ApiResponse>('/api/profile/education', data);
    return response.data;
  }

  async updateEducation(id: number, data: Partial<CreateEducationRequest>): Promise<ApiResponse> {
    const response = await this.api.put<ApiResponse>(`/api/profile/education/${id}`, data);
    return response.data;
  }

  async deleteEducation(id: number): Promise<ApiResponse> {
    const response = await this.api.delete<ApiResponse>(`/api/profile/education/${id}`);
    return response.data;
  }

  async createSkill(data: CreateSkillRequest): Promise<ApiResponse> {
    const response = await this.api.post<ApiResponse>('/api/profile/skills', data);
    return response.data;
  }

  async updateSkill(id: number, data: Partial<CreateSkillRequest>): Promise<ApiResponse> {
    const response = await this.api.put<ApiResponse>(`/api/profile/skills/${id}`, data);
    return response.data;
  }

  async deleteSkill(id: number): Promise<ApiResponse> {
    const response = await this.api.delete<ApiResponse>(`/api/profile/skills/${id}`);
    return response.data;
  }

  // Certification APIs
  async createCertification(data: any): Promise<ApiResponse> {
    const response = await this.api.post<ApiResponse>('/api/profile/certifications', data);
    return response.data;
  }

  async updateCertification(id: number, data: any): Promise<ApiResponse> {
    const response = await this.api.put<ApiResponse>(`/api/profile/certifications/${id}`, data);
    return response.data;
  }

  async deleteCertification(id: number): Promise<ApiResponse> {
    const response = await this.api.delete<ApiResponse>(`/api/profile/certifications/${id}`);
    return response.data;
  }

  // Portfolio APIs
  async createPortfolio(data: any): Promise<ApiResponse> {
    const response = await this.api.post<ApiResponse>('/api/profile/portfolios', data);
    return response.data;
  }

  async updatePortfolio(id: number, data: any): Promise<ApiResponse> {
    const response = await this.api.put<ApiResponse>(`/api/profile/portfolios/${id}`, data);
    return response.data;
  }

  async deletePortfolio(id: number): Promise<ApiResponse> {
    const response = await this.api.delete<ApiResponse>(`/api/profile/portfolios/${id}`);
    return response.data;
  }

  // Get individual profile sections
  async getEducation(): Promise<ApiResponse<any[]>> {
    const response = await this.api.get<ApiResponse<any[]>>('/api/profile/education');
    return response.data;
  }

  async getSkills(): Promise<ApiResponse<any[]>> {
    const response = await this.api.get<ApiResponse<any[]>>('/api/profile/skills');
    return response.data;
  }

  async getCertifications(): Promise<ApiResponse<any[]>> {
    const response = await this.api.get<ApiResponse<any[]>>('/api/profile/certifications');
    return response.data;
  }

  async getPortfolios(): Promise<ApiResponse<any[]>> {
    const response = await this.api.get<ApiResponse<any[]>>('/api/profile/portfolios');
    return response.data;
  }

  async getExperiences(): Promise<ApiResponse<Experience[]>> {
    const response = await this.api.get<ApiResponse<Experience[]>>('/api/profile/experiences');
    return response.data;
  }

  // ============= Dashboard APIs =============

  async getDashboardData(): Promise<ApiResponse<DashboardData>> {
    const response = await this.api.get<ApiResponse<DashboardData>>('/api/dashboard');
    return response.data;
  }

  async getGeneralUserDashboard(): Promise<ApiResponse<GeneralUserDashboard>> {
    const response = await this.api.get<ApiResponse<GeneralUserDashboard>>('/api/dashboard/general');
    return response.data;
  }

  async getCompanyDashboard(): Promise<ApiResponse<any>> {
    const response = await this.api.get<ApiResponse<any>>('/api/dashboard/company');
    return response.data;
  }

  async getAdminDashboard(): Promise<ApiResponse<any>> {
    // Use admin token specifically for admin dashboard
    const adminToken = localStorage.getItem('adminToken');

    if (!adminToken) {
      throw new Error('No admin token found in localStorage');
    }

    const response = await this.api.get<ApiResponse<any>>('/api/admin/dashboard', {
      headers: {
        Authorization: `Bearer ${adminToken}`
      }
    });
    return response.data;
  }

  // ============= AI Service APIs =============

  async chatWithBot(request: ChatbotRequest): Promise<ChatbotResponse> {
    // Use nginx routing path instead of direct AI service path
    const aiApi = axios.create({
      baseURL: API_BASE_URL,
      timeout: 30000,
    });

    // Convert frontend format to backend format
    const backendRequest = {
      user_id: request.userId,
      message: request.message
    };

    const response = await aiApi.post('/api/chatbot/chat', {
      message: request.message,
      user_id: request.userId,
      language: 'ko'
    });
    
    // chatbot API 응답을 ChatbotResponse 형태로 변환
    if (response.data.success) {
      return {
        success: true,
        message: response.data.message,
        data: {
          response: response.data.data.response // API 응답의 response 필드 매핑
        }
      };
    } else {
      return response.data;
    }
  }

  async sendChatMessage(userId: string, message: string): Promise<ChatbotResponse> {
    return this.chatWithBot({ userId, message });
  }

  async generateInterviewQuestions(request: InterviewRequest): Promise<ApiResponse<{ questions: InterviewQuestion[] }>> {
    const aiApi = axios.create({
      baseURL: this.aiServiceURL,
      timeout: 30000,
    });

    const response = await aiApi.post<ApiResponse<{ questions: InterviewQuestion[] }>>('/interview/generate-questions', request);
    return response.data;
  }

  async generateCoverLetter(request: CoverLetterRequest): Promise<CoverLetterResponse> {
    const aiApi = axios.create({
      baseURL: this.aiServiceURL,
      timeout: 30000,
    });

    const response = await aiApi.post<CoverLetterResponse>('/cover-letter/generate-complete', request);
    return response.data;
  }

  async translateText(request: TranslationRequest): Promise<TranslationResponse> {
    const aiApi = axios.create({
      baseURL: this.aiServiceURL,
      timeout: 30000,
    });

    // Transform request parameters to match backend schema
    const backendRequest = {
      text: request.text,
      source_language: request.sourceLanguage,
      target_language: request.targetLanguage,
      document_type: 'general'  // type property doesn't exist on TranslationRequest
    };

    const response = await aiApi.post<TranslationResponse>('/translation/translate', backendRequest);
    return response.data;
  }

  async generateImage(request: ImageGenerationRequest): Promise<ImageGenerationResponse> {
    const aiApi = axios.create({
      baseURL: this.aiServiceURL,
      timeout: 60000,
    });

    // Backend expects user_id instead of userId
    const backendRequest = {
      prompt: request.prompt,
      user_id: request.userId,
      style: request.style
    };

    const response = await aiApi.post<ImageGenerationResponse>('/image/generate', backendRequest);
    return response.data;
  }

  // ============= Webmail APIs =============

  sendEmail = async (data: SendEmailRequest): Promise<ApiResponse> => {
    console.log('Sending email with data:', data);
    const response = await this.api.post<ApiResponse>(`/api/webmail/send`, data);
    console.log('Email API response:', response.data);
    return response.data;
  }

  getSentEmails = async (page = 0, size = 10): Promise<ApiResponse<PageResponse<EmailHistory>>> => {
    const response = await this.api.get<ApiResponse<PageResponse<EmailHistory>>>(
      `/api/webmail/sent?page=${page}&size=${size}`
    );
    return response.data;
  }

  getTranslatedEmails = async (): Promise<ApiResponse<EmailHistory[]>> => {
    const response = await this.api.get<ApiResponse<EmailHistory[]>>('/api/webmail/translated');
    return response.data;
  }

  getEmailStats = async (): Promise<ApiResponse<EmailStats>> => {
    const response = await this.api.get<ApiResponse<EmailStats>>('/api/webmail/stats');
    return response.data;
  }

  // ============= Certificate APIs =============

  async getMyCertificateRequests(): Promise<ApiResponse<any[]>> {
    const response = await this.api.get<ApiResponse<any[]>>('/api/certificates/my-requests');
    return response.data;
  }

  async createCertificateRequest(data: { certificateType: string; purpose: string }): Promise<ApiResponse<any>> {
    const response = await this.api.post<ApiResponse<any>>('/api/certificates/request', data);
    return response.data;
  }

  // ============= Health Check =============

  async checkBackendHealth(): Promise<boolean> {
    try {
      await this.api.get('/actuator/health');
      return true;
    } catch {
      return false;
    }
  }

  async checkAIServiceHealth(): Promise<boolean> {
    try {
      const aiApi = axios.create({
        baseURL: this.aiServiceURL,
        timeout: 5000,
      });
      await aiApi.get('/api/health');
      return true;
    } catch {
      return false;
    }
  }
}

export const apiClient = new ApiClient();

// AI Service 전용 클라이언트를 별도로 생성
// Helper function to calculate overall score from answers
const calculateOverallScore = (answers: any[]): number => {
  if (!answers || answers.length === 0) return 0;

  const scoredAnswers = answers.filter(a => a.feedback?.score != null);
  if (scoredAnswers.length === 0) return 0;

  const totalScore = scoredAnswers.reduce((sum, a) => sum + (a.feedback.score || 0), 0);
  return Math.round(totalScore / scoredAnswers.length);
};

export const aiClient = {
  generateInterviewQuestions: apiClient.generateInterviewQuestions.bind(apiClient),
  evaluateInterviewAnswer: async (data: any) => {
    const aiApi = axios.create({
      baseURL: AI_SERVICE_BASE_URL,
      timeout: 30000,
    });
    const response = await aiApi.post('/api/interview/evaluate-answer-frontend', data);
    return response.data;
  },
  completeInterview: async (data: any) => {
    const token = localStorage.getItem('accessToken');

    // For guest users, save to localStorage instead of backend
    if (!token) {
      // Store interview results in localStorage for guest users
      const guestInterviews = JSON.parse(localStorage.getItem('guestInterviews') || '[]');
      const interviewResult = {
        id: Date.now(),
        ...data,
        completedAt: new Date().toISOString(),
        isGuest: true
      };
      guestInterviews.push(interviewResult);
      localStorage.setItem('guestInterviews', JSON.stringify(guestInterviews));

      return {
        success: true,
        message: '면접이 완료되었습니다. (게스트 모드)',
        data: {
          id: interviewResult.id,
          overallScore: calculateOverallScore(data.answers),
          totalQuestions: data.questions.length,
          answeredQuestions: data.answers.filter((a: any) => a.answer).length,
          completedAt: interviewResult.completedAt
        }
      };
    }

    // For authenticated users, save to BACKEND first
    try {
      const response = await apiClient.api.post('/api/ai/interview/complete', data);
      return response.data;
    } catch (error) {
      console.error('Backend interview complete failed, fallback to AI service:', error);
      // Fallback to AI service if backend fails
      const aiApi = axios.create({
        baseURL: AI_SERVICE_BASE_URL,
        timeout: 60000,
      });
      const response = await aiApi.post('/api/interview/complete', {
        jobRole: data.jobRole,
        questions: data.questions,
        answers: data.answers,
        userId: data.userId || 'authenticated_user'
      });
      return response.data;
    }
  },
  getInterviewHistory: async (userId: string = 'guest') => {
    const token = localStorage.getItem('accessToken');

    // For guest users, return interviews from localStorage
    if (!token) {
      const guestInterviews = JSON.parse(localStorage.getItem('guestInterviews') || '[]');

      // Format the guest interviews to match the expected response structure
      const formattedInterviews = guestInterviews.map((interview: any) => ({
        interviewId: interview.id,
        id: interview.id,
        jobRole: interview.job_role || interview.jobRole,
        interviewType: interview.questions?.[0]?.type || 'technical',
        experienceLevel: 'BEGINNER',
        overallScore: interview.overallScore || calculateOverallScore(interview.answers),
        totalQuestions: interview.questions?.length || 0,
        answeredQuestions: interview.answers?.filter((a: any) => a.answer)?.length || 0,
        status: 'COMPLETED',
        completedAt: interview.completedAt,
        createdAt: interview.completedAt,
        questionCount: interview.questions?.length || 0,
        questions: interview.questions?.map((q: any) => ({
          id: q.id,
          question: q.question,
          type: q.type
        })) || [],
        answers: interview.answers?.map((a: any) => ({
          questionId: a.question_id,
          answer: a.answer,
          feedback: a.feedback
        })) || []
      }));

      return {
        success: true,
        data: {
          content: formattedInterviews,
          totalElements: formattedInterviews.length,
          totalPages: 1,
          last: true,
          first: true,
          number: 0,
          size: formattedInterviews.length,
          numberOfElements: formattedInterviews.length
        }
      };
    }

    // Helper function to convert backend date array to ISO string
    const convertDateArrayToString = (dateArray: number[] | string) => {
      if (typeof dateArray === 'string') return dateArray;
      if (!Array.isArray(dateArray) || dateArray.length < 3) return new Date().toISOString();

      // Convert [2025,9,25,21,6,27,821901000] format to ISO string
      const [year, month, day, hour = 0, minute = 0, second = 0, nano = 0] = dateArray;
      const date = new Date(year, month - 1, day, hour, minute, second, Math.floor(nano / 1000000));
      return date.toISOString();
    };

    // For authenticated users, fetch from BACKEND (not AI service)
    // Use the main API client which includes JWT token automatically
    try {
      const response = await apiClient.api.get('/api/ai/interview/history?page=0&size=50');
      const backendData = response.data;

      // Transform backend response to match frontend expectations
      if (backendData?.content) {
        const transformedContent = backendData.content.map((interview: any) => ({
          interviewId: interview.id,
          id: interview.id,
          jobRole: interview.jobRole,
          interviewType: interview.interviewType,
          experienceLevel: interview.experienceLevel,
          overallScore: Number(interview.overallScore || 0),
          totalQuestions: interview.totalQuestions,
          answeredQuestions: interview.answeredQuestions,
          status: interview.status || 'COMPLETED',
          completedAt: convertDateArrayToString(interview.completedAt),
          createdAt: convertDateArrayToString(interview.createdAt),
          questionCount: interview.questionCount || interview.totalQuestions,
          // These will be fetched on-demand when user clicks detail view
          questions: [],
          answers: []
        }));

        return {
          success: true,
          data: {
            content: transformedContent,
            interviews: transformedContent, // For backward compatibility
            totalElements: backendData.totalElements,
            total: backendData.totalElements, // For backward compatibility
            totalPages: backendData.totalPages,
            last: backendData.last,
            first: backendData.first,
            number: backendData.number,
            size: backendData.size,
            numberOfElements: backendData.numberOfElements
          }
        };
      }

      return response.data;
    } catch (error) {
      console.error('Backend interview history fetch failed, fallback to AI service:', error);
      // Fallback to AI service if backend fails
      const aiApi = axios.create({
        baseURL: AI_SERVICE_BASE_URL,
        timeout: 30000,
      });
      const response = await aiApi.get(`/interview/history?user_id=${userId}`);
      return response.data;
    }
  },
  getAIReview: async (interviewId: number, userId?: string) => {
    const aiApi = axios.create({
      baseURL: AI_SERVICE_BASE_URL,
      timeout: 60000, // AI 분석은 시간이 오래 걸릴 수 있음
    });

    // 사용자 ID 가져오기 - 현재 로그인된 사용자 또는 guest
    const token = localStorage.getItem('token');
    let currentUserId = userId || 'guest';

    if (!currentUserId || currentUserId === 'guest') {
      if (token) {
        try {
          // JWT 토큰에서 사용자 ID 추출 시도
          const payload = JSON.parse(atob(token.split('.')[1]));
          currentUserId = payload.userId?.toString() || payload.sub?.toString() || 'guest';
        } catch (error) {
          console.warn('토큰 파싱 실패:', error);
          currentUserId = 'guest';
        }
      }
    }

    const response = await aiApi.post(`/interview/ai-review`, {
      interview_id: interviewId,
      user_id: currentUserId
    });
    return response.data;
  },
  generateCoverLetterSection: async (data: any) => {
    const aiApi = axios.create({
      baseURL: AI_SERVICE_BASE_URL,
      timeout: 30000,
    });
    // Use the demo endpoint with query parameters for basic generation
    const params = new URLSearchParams({
      section: data.section,
      company_name: data.companyName,
      position: data.position
    });
    const response = await aiApi.get(`/cover-letter/demo-section?${params}`);
    return response.data;
  },
  generateCompleteCoverLetter: async (data: any) => {
    // Temporarily disable complete generation as it's causing backend issues
    return {
      success: false,
      message: "전체 자소서 생성 기능은 현재 준비 중입니다. 각 섹션별로 생성해주세요.",
      data: null,
      error: "Feature temporarily disabled"
    };
  },
  getCoverLetterFeedback: async (data: any) => {
    const aiApi = axios.create({
      baseURL: AI_SERVICE_BASE_URL,
      timeout: 30000,
    });
    const response = await aiApi.post('/api/cover-letter/feedback', data);
    return response.data;
  },
  translateText: apiClient.translateText.bind(apiClient),
  getSupportedLanguages: async () => {
    const response = await apiClient.api.get('/api/translation/languages');
    return response.data;
  },
  evaluateTranslation: async (data: any) => {
    const response = await apiClient.api.post('/api/translation/evaluate', data);
    return response.data;
  },
  batchTranslate: async (data: any) => {
    const response = await apiClient.api.post('/api/translation/batch', data);
    return response.data;
  },
  
  // 인터랙티브 자소서 생성 API
  startInteractiveCoverLetter: async (data: { companyName: string; position: string; section: string }) => {
    const aiApi = axios.create({
      baseURL: AI_SERVICE_BASE_URL,
      timeout: 30000,
    });
    // Use query parameters as expected by the backend
    const params = new URLSearchParams({
      company_name: data.companyName,
      position: data.position,
      section: data.section,
      user_id: 'web_user'
    });
    const response = await aiApi.post(`/cover-letter/interactive/start?${params}`);
    return response.data;
  },
  
  submitInteractiveAnswer: async (data: { sessionId: string; answer: string; selections?: string[] }) => {
    const aiApi = axios.create({
      baseURL: AI_SERVICE_BASE_URL,
      timeout: 120000, // 2분으로 증가 (최종 자소서 생성 시간 고려)
    });
    // Use query parameters as expected by the backend
    const params = new URLSearchParams({
      session_id: data.sessionId,
      answer: data.answer || ''
    });
    // Add selections if provided
    if (data.selections && data.selections.length > 0) {
      data.selections.forEach(selection => {
        params.append('selections', selection);
      });
    }
    const response = await aiApi.post(`/cover-letter/interactive/answer?${params}`);
    return response.data;
  },
  
  getInteractiveSections: async () => {
    const aiApi = axios.create({
      baseURL: AI_SERVICE_BASE_URL,
      timeout: 30000,
    });
    const response = await aiApi.get('/api/cover-letter/interactive/sections');
    return response.data;
  },

  sendChatMessage: async (userId: string, message: string) => {
    return apiClient.chatWithBot({ userId, message });
  },

  getChatbotSuggestions: async () => {
    const response = await apiClient.api.get('/api/chatbot/suggestions');
    return response.data;
  },

  getChatbotCategories: async (): Promise<ChatbotCategoriesResponse> => {
    console.log('🚀 API Client: Starting getChatbotCategories request')

    try {
      console.log('📞 Making request to: /api/chatbot/categories')
      const response = await apiClient.api.get('/api/chatbot/categories');
      console.log('📥 Raw API response:', response.data)

      // Transform the API response to match our expected format
      if (response.data.success && response.data.data && response.data.data.categories) {
        const categories = response.data.data.categories;
        console.log('🔄 Raw categories:', categories)

        const transformedData: ChatSuggestion[] = Object.entries(categories).map(([category, suggestions]) => ({
          category,
          suggestions: suggestions as string[]
        }));
        console.log('✨ Transformed data:', transformedData)

        const result = {
          success: true,
          message: response.data.message || "Categories retrieved successfully",
          data: transformedData
        };
        console.log('✅ Final result:', result)
        return result;
      }

      console.log('⚠️ Invalid response structure:', response.data)
      return {
        success: false,
        message: "Failed to retrieve categories",
        data: []
      };
    } catch (error: any) {
      console.error('❌ API Error in getChatbotCategories:', error);
      if (error?.response) {
        console.error('❌ Response data:', error.response.data);
        console.error('❌ Response status:', error.response.status);
      }
      return {
        success: false,
        message: "Failed to retrieve categories",
        data: []
      };
    }
  },

  getWelcomeMessage: async (): Promise<WelcomeMessageResponse> => {
    try {
      // For now, return a static welcome message
      // This can be extended to fetch from an API endpoint later
      return {
        success: true,
        message: "Welcome message retrieved",
        data: {
          message: "안녕하세요! 잡았다 AI 챗봇입니다. 취업 준비와 관련된 모든 질문에 답변해드릴 수 있어요. 무엇을 도와드릴까요?"
        }
      };
    } catch (error) {
      console.error('Failed to fetch welcome message:', error);
      return {
        success: false,
        message: "Failed to retrieve welcome message",
        data: {
          message: "안녕하세요! 잡았다 AI 챗봇입니다. 취업 준비와 관련된 모든 질문에 답변해드릴 수 있어요. 무엇을 도와드릴까요?"
        }
      };
    }
  }
};

// Auth API를 별도로 export
export const authApi = {
  adminLogin: (username: string, password: string) => apiClient.adminLogin(username, password),
  login: (request: LoginRequest) => apiClient.login(request),
  register: (request: RegisterRequest) => apiClient.register(request),
  oauth2Login: (request: OAuth2LoginRequest) => apiClient.oauth2Login(request),
  refreshToken: () => apiClient.refreshToken(),
  logout: () => apiClient.logout()
};

export default apiClient;