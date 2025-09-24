// API Configuration - 환경 변수 우선, fallback으로 기본값 사용
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081/api'
export const AI_SERVICE_BASE_URL = import.meta.env.VITE_AI_SERVICE_BASE_URL || 'http://localhost:8001/api'

// Application Configuration
export const APP_CONFIG = {
  TITLE: import.meta.env.VITE_APP_TITLE || '잡았다',
  DESCRIPTION: import.meta.env.VITE_APP_DESCRIPTION || '취업 준비 플랫폼',
  VERSION: import.meta.env.VITE_APP_VERSION || '1.0.0',
  ENABLE_DEBUG: import.meta.env.VITE_ENABLE_DEBUG === 'true',
  ENABLE_DEV_TOOLS: import.meta.env.VITE_ENABLE_DEV_TOOLS === 'true',
  ENABLE_ERROR_REPORTING: import.meta.env.VITE_ENABLE_ERROR_REPORTING !== 'false',
} as const

// Performance Configuration
export const API_CONFIG = {
  TIMEOUT: Number(import.meta.env.VITE_API_TIMEOUT) || 15000,
  AI_TIMEOUT: Number(import.meta.env.VITE_AI_API_TIMEOUT) || 30000,
  RETRY_COUNT: Number(import.meta.env.VITE_RETRY_COUNT) || 3,
  CACHE_TIME: Number(import.meta.env.VITE_CACHE_TIME) || 300000,
} as const

// OAuth Configuration
export const OAUTH_CONFIG = {
  GOOGLE_CLIENT_ID: import.meta.env.VITE_GOOGLE_CLIENT_ID || '',
  REDIRECT_URL: import.meta.env.VITE_OAUTH_REDIRECT_URL || 'http://localhost:3000/auth/callback',
} as const

// Analytics Configuration
export const ANALYTICS_CONFIG = {
  ANALYTICS_ID: import.meta.env.VITE_ANALYTICS_ID || '',
  SENTRY_DSN: import.meta.env.VITE_SENTRY_DSN || '',
} as const

export const LOCAL_STORAGE_KEYS = {
  AUTH_TOKEN: 'jbd_auth_token',
  REFRESH_TOKEN: 'jbd_refresh_token',
  USER: 'jbd_user',
} as const

export const QUERY_KEYS = {
  DASHBOARD: 'dashboard',
  PROFILE: 'profile',
  JOBS: 'jobs',
  JOB: 'job',
  APPLICATIONS: 'applications',
  APPLICATION: 'application',
  POSTS: 'posts',
  POST: 'post',
  COMMENTS: 'comments',
  CATEGORIES: 'categories',
  USER: 'user',
} as const

export const ROUTES = {
  HOME: '/',
  LOGIN: '/login',
  REGISTER: '/register',
  DASHBOARD: '/dashboard',
  JOBS: '/jobs',
  JOB_DETAIL: '/jobs/:id',
  COMMUNITY: '/community',
  POST_DETAIL: '/community/:id',
  AI_INTERVIEW: '/ai/interview',
  AI_COVER_LETTER: '/ai/cover-letter',
  AI_TRANSLATION: '/ai/translation',
  PROFILE: '/profile',
  SETTINGS: '/settings',
  CERTIFICATES: '/certificates',
} as const

export const JOB_TYPE_LABELS = {
  FULL_TIME: '정규직',
  PART_TIME: '파트타임',
  CONTRACT: '계약직',
  INTERN: '인턴십',
  FREELANCE: '프리랜서',
} as const

export const EXPERIENCE_LEVEL_LABELS = {
  ENTRY_LEVEL: '신입',
  JUNIOR: '주니어 (1-3년)',
  MID_LEVEL: '미들 (3-7년)',
  SENIOR: '시니어 (7년+)',
  EXPERT: '전문가',
  MANAGER: '매니저',
  DIRECTOR: '디렉터',
  ANY: '경력 무관',
} as const

export const APPLICATION_STATUS_LABELS = {
  PENDING: '서류 검토중',
  REVIEWING: '면접 진행중',
  INTERVIEW: '면접 진행중',
  APPROVED: '합격',
  ACCEPTED: '합격',
  REJECTED: '불합격',
} as const

export const USER_TYPE_LABELS = {
  GENERAL: '일반 사용자',
  COMPANY: '기업 사용자',
  ADMIN: '관리자',
} as const

export const SKILL_CATEGORIES = {
  PROGRAMMING: '프로그래밍 언어',
  FRAMEWORK: '프레임워크/라이브러리',
  DATABASE: '데이터베이스',
  TOOL: '개발 도구',
  LANGUAGE: '언어',
  CERTIFICATION: '자격증',
} as const

export const EDUCATION_LEVELS = {
  HIGH_SCHOOL: '고등학교',
  ASSOCIATE: '전문학사',
  BACHELOR: '학사',
  MASTER: '석사',
  DOCTORATE: '박사',
} as const

export const EMPLOYMENT_STATUSES = {
  EMPLOYED: '재직중',
  UNEMPLOYED: '구직중',
  JOB_SEEKING: '구직중',
  STUDENT: '학생',
  FREELANCER: '프리랜서',
} as const

export const PAGINATION = {
  DEFAULT_SIZE: 10,
  MAX_SIZE: 100,
} as const

export const FILE_UPLOAD = {
  MAX_SIZE: 10 * 1024 * 1024, // 10MB
  ALLOWED_TYPES: {
    IMAGE: ['image/jpeg', 'image/png', 'image/gif', 'image/webp'],
    DOCUMENT: ['application/pdf', 'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'],
  },
} as const

export const VALIDATION = {
  PASSWORD: {
    MIN_LENGTH: 8,
    MAX_LENGTH: 128,
  },
  EMAIL: {
    MAX_LENGTH: 255,
  },
  TEXT: {
    SHORT: 255,
    MEDIUM: 1000,
    LONG: 5000,
  },
} as const