import { z } from 'zod'
import { VALIDATION, FILE_UPLOAD } from './constants'

// 기본 검증 스키마
export const emailSchema = z
  .string()
  .email('올바른 이메일 주소를 입력해주세요')
  .max(VALIDATION.EMAIL.MAX_LENGTH, '이메일 주소가 너무 깁니다')

export const passwordSchema = z
  .string()
  .min(VALIDATION.PASSWORD.MIN_LENGTH, `비밀번호는 ${VALIDATION.PASSWORD.MIN_LENGTH}자 이상이어야 합니다`)
  .max(VALIDATION.PASSWORD.MAX_LENGTH, `비밀번호는 ${VALIDATION.PASSWORD.MAX_LENGTH}자 이하여야 합니다`)
  .regex(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/, '비밀번호는 대소문자, 숫자를 포함해야 합니다')

export const phoneNumberSchema = z
  .string()
  .regex(/^(01[0-9]|02|0[3-9][0-9])-?[0-9]{3,4}-?[0-9]{4}$/, '올바른 전화번호를 입력해주세요')

export const nameSchema = z
  .string()
  .min(2, '이름은 2자 이상이어야 합니다')
  .max(50, '이름은 50자 이하여야 합니다')
  .regex(/^[가-힣a-zA-Z\s]+$/, '이름은 한글, 영문, 공백만 입력 가능합니다')

// 사용자 검증 스키마
export const loginSchema = z.object({
  email: emailSchema,
  password: z.string().min(1, '비밀번호를 입력해주세요'),
})

export const registerSchema = z.object({
  name: nameSchema,
  email: emailSchema,
  password: passwordSchema,
  confirmPassword: z.string(),
  phoneNumber: phoneNumberSchema,
  userType: z.enum(['GENERAL', 'COMPANY']),
  academy: z.string().optional(),
  companyName: z.string().optional(),
  terms: z.boolean().refine(val => val, '이용약관에 동의해주세요'),
}).refine((data) => data.password === data.confirmPassword, {
  message: '비밀번호가 일치하지 않습니다',
  path: ['confirmPassword'],
})

// 프로필 검증 스키마
export const profileUpdateSchema = z.object({
  name: nameSchema.optional(),
  phoneNumber: phoneNumberSchema.optional(),
  location: z.string().max(100, '위치는 100자 이하여야 합니다').optional(),
  website: z.string().url('올바른 URL을 입력해주세요').optional().or(z.literal('')),
  summary: z.string().max(VALIDATION.TEXT.LONG, `자기소개는 ${VALIDATION.TEXT.LONG}자 이하여야 합니다`).optional(),
})

export const experienceSchema = z.object({
  companyName: z.string().min(1, '회사명을 입력해주세요').max(100, '회사명은 100자 이하여야 합니다'),
  position: z.string().min(1, '직책을 입력해주세요').max(100, '직책은 100자 이하여야 합니다'),
  startDate: z.string().min(1, '시작일을 입력해주세요'),
  endDate: z.string().optional(),
  description: z.string().max(VALIDATION.TEXT.LONG, `업무 설명은 ${VALIDATION.TEXT.LONG}자 이하여야 합니다`).optional(),
})

export const educationSchema = z.object({
  schoolName: z.string().min(1, '학교명을 입력해주세요').max(100, '학교명은 100자 이하여야 합니다'),
  major: z.string().min(1, '전공을 입력해주세요').max(100, '전공은 100자 이하여야 합니다'),
  degree: z.enum(['HIGH_SCHOOL', 'ASSOCIATE', 'BACHELOR', 'MASTER', 'DOCTORATE']),
  startDate: z.string().min(1, '입학일을 입력해주세요'),
  endDate: z.string().optional(),
  gpa: z.number().min(0).max(4.5).optional(),
})

export const skillSchema = z.object({
  name: z.string().min(1, '기술명을 입력해주세요').max(50, '기술명은 50자 이하여야 합니다'),
  category: z.enum(['PROGRAMMING', 'FRAMEWORK', 'DATABASE', 'TOOL', 'SOFT_SKILL']),
  proficiency: z.number().min(1).max(5),
})

export const certificationSchema = z.object({
  name: z.string().min(1, '자격증명을 입력해주세요').max(100, '자격증명은 100자 이하여야 합니다'),
  issuer: z.string().min(1, '발급기관을 입력해주세요').max(100, '발급기관은 100자 이하여야 합니다'),
  obtainedDate: z.string().min(1, '취득일을 입력해주세요'),
  expirationDate: z.string().optional(),
  credentialId: z.string().max(100, '자격증 번호는 100자 이하여야 합니다').optional(),
})

// 채용공고 검증 스키마
export const jobPostingSchema = z.object({
  title: z.string().min(1, '제목을 입력해주세요').max(200, '제목은 200자 이하여야 합니다'),
  description: z.string().min(1, '설명을 입력해주세요').max(VALIDATION.TEXT.LONG, `설명은 ${VALIDATION.TEXT.LONG}자 이하여야 합니다`),
  requirements: z.string().max(VALIDATION.TEXT.LONG, `자격요건은 ${VALIDATION.TEXT.LONG}자 이하여야 합니다`).optional(),
  preferredQualifications: z.string().max(VALIDATION.TEXT.LONG, `우대사항은 ${VALIDATION.TEXT.LONG}자 이하여야 합니다`).optional(),
  benefits: z.string().max(VALIDATION.TEXT.LONG, `혜택은 ${VALIDATION.TEXT.LONG}자 이하여야 합니다`).optional(),
  location: z.string().min(1, '근무지를 입력해주세요').max(100, '근무지는 100자 이하여야 합니다'),
  jobType: z.enum(['FULL_TIME', 'PART_TIME', 'CONTRACT', 'INTERNSHIP', 'FREELANCE']),
  experienceLevel: z.enum(['ENTRY', 'JUNIOR', 'MIDDLE', 'SENIOR', 'LEAD']),
  minSalary: z.number().min(0).optional(),
  maxSalary: z.number().min(0).optional(),
  deadline: z.string().optional(),
  requiredSkills: z.array(z.string()).optional(),
})

export const jobApplicationSchema = z.object({
  jobPostingId: z.number().min(1, '채용공고를 선택해주세요'),
  coverLetter: z.string().min(1, '자기소개서를 작성해주세요').max(VALIDATION.TEXT.LONG, `자기소개서는 ${VALIDATION.TEXT.LONG}자 이하여야 합니다`),
})

// 커뮤니티 검증 스키마
export const postSchema = z.object({
  title: z.string().min(1, '제목을 입력해주세요').max(200, '제목은 200자 이하여야 합니다'),
  content: z.string().min(1, '내용을 입력해주세요').max(VALIDATION.TEXT.LONG, `내용은 ${VALIDATION.TEXT.LONG}자 이하여야 합니다`),
  categoryId: z.number().min(1, '카테고리를 선택해주세요').optional(),
})

export const commentSchema = z.object({
  postId: z.number().min(1, '게시글을 선택해주세요'),
  content: z.string().min(1, '댓글을 입력해주세요').max(VALIDATION.TEXT.MEDIUM, `댓글은 ${VALIDATION.TEXT.MEDIUM}자 이하여야 합니다`),
})

// AI 서비스 검증 스키마
export const interviewGenerateSchema = z.object({
  jobRole: z.string().min(1, '지원 직무를 입력해주세요').max(100, '직무는 100자 이하여야 합니다'),
  experienceLevel: z.enum(['entry', 'junior', 'senior']),
  interviewType: z.enum(['general', 'technical']),
})

export const coverLetterGenerateSchema = z.object({
  companyName: z.string().min(1, '회사명을 입력해주세요').max(100, '회사명은 100자 이하여야 합니다'),
  jobTitle: z.string().min(1, '직무를 입력해주세요').max(100, '직무는 100자 이하여야 합니다'),
  applicantName: z.string().max(50, '이름은 50자 이하여야 합니다').optional(),
  experience: z.string().max(VALIDATION.TEXT.LONG, `경험은 ${VALIDATION.TEXT.LONG}자 이하여야 합니다`).optional(),
  skills: z.string().max(VALIDATION.TEXT.MEDIUM, `기술은 ${VALIDATION.TEXT.MEDIUM}자 이하여야 합니다`).optional(),
  motivation: z.string().max(VALIDATION.TEXT.MEDIUM, `지원동기는 ${VALIDATION.TEXT.MEDIUM}자 이하여야 합니다`).optional(),
  achievements: z.string().max(VALIDATION.TEXT.MEDIUM, `성과는 ${VALIDATION.TEXT.MEDIUM}자 이하여야 합니다`).optional(),
})

export const translationSchema = z.object({
  text: z.string().min(1, '번역할 텍스트를 입력해주세요').max(VALIDATION.TEXT.LONG, `텍스트는 ${VALIDATION.TEXT.LONG}자 이하여야 합니다`),
  sourceLanguage: z.string().min(2, '원본 언어를 선택해주세요'),
  targetLanguage: z.string().min(2, '번역 언어를 선택해주세요'),
  type: z.enum(['general', 'resume', 'interview']),
})

/**
 * 파일 검증 함수
 * @param file 검증할 File 객체
 * @param type 파일 타입 ('image' | 'document')
 * @returns 검증 오류 메시지 또는 null (성공 시)
 * 이벤트: 파일 크기 검증 이벤트, 파일 타입 검증 이벤트
 */
export const validateFile = (file: File, type: 'image' | 'document' = 'document'): string | null => {
  if (file.size > FILE_UPLOAD.MAX_SIZE) {
    return `파일 크기는 ${FILE_UPLOAD.MAX_SIZE / 1024 / 1024}MB 이하여야 합니다`
  }

  const allowedTypes = type === 'image'
    ? FILE_UPLOAD.ALLOWED_TYPES.IMAGE
    : FILE_UPLOAD.ALLOWED_TYPES.DOCUMENT

  if (!(allowedTypes as readonly string[]).includes(file.type)) {
    const typeNames = type === 'image'
      ? 'JPEG, PNG, GIF, WebP'
      : 'PDF, DOC, DOCX'
    return `지원하는 파일 형식: ${typeNames}`
  }

  return null
}

/**
 * 한국 전화번호 유효성 검사
 * @param phone 검사할 전화번호 문자열
 * @returns 유효한 전화번호인지 여부
 * 이벤트: 전화번호 형식 검증 이벤트
 */
export const isValidKoreanPhoneNumber = (phone: string): boolean => {
  const cleaned = phone.replace(/[^0-9]/g, '')
  // Mobile: 010, 011, 016, 017, 018, 019 (11 digits)
  // Seoul area: 02 (9-10 digits)
  // Other areas: 031, 032, etc. (10 digits)
  return /^(01[0-9]\d{7,8}|02\d{7,8}|0[3-9]\d\d{6,7})$/.test(cleaned)
}

/**
 * 이메일 주소 유효성 검사
 * @param email 검사할 이메일 주소
 * @returns 유효한 이메일인지 여부
 * 이벤트: 이메일 형식 검증 이벤트
 */
export const isValidEmail = (email: string): boolean => {
  return emailSchema.safeParse(email).success
}

/**
 * 비밀번호 유효성 검사
 * @param password 검사할 비밀번호
 * @returns 유효한 비밀번호인지 여부
 * 이벤트: 비밀번호 강도 검증 이벤트
 */
export const isValidPassword = (password: string): boolean => {
  return passwordSchema.safeParse(password).success
}

/**
 * 비밀번호 강도 측정
 * @param password 측정할 비밀번호
 * @returns 비밀번호 강도 정보 (점수, 라벨, 색상)
 * 이벤트: 비밀번호 강도 계산 이벤트, UI 색상 변경 이벤트
 */
export const getPasswordStrength = (password: string): { score: number; label: string; color: string } => {
  let score = 0

  if (password.length >= 8) score += 1
  if (password.length >= 12) score += 1
  if (/[a-z]/.test(password)) score += 1
  if (/[A-Z]/.test(password)) score += 1
  if (/[0-9]/.test(password)) score += 1
  if (/[^A-Za-z0-9]/.test(password)) score += 1

  if (score <= 2) return { score, label: '약함', color: 'text-red-600' }
  if (score <= 4) return { score, label: '보통', color: 'text-yellow-600' }
  return { score, label: '강함', color: 'text-green-600' }
}

/**
 * 폼 검증 헬퍼 함수
 * @param schema Zod 검증 스키마
 * @param data 검증할 데이터
 * @returns 검증 결과 (성공/실패, 데이터, 에러)
 * 이벤트: 폼 검증 이벤트, 에러 메시지 생성 이벤트
 */
export const validateForm = <T>(schema: z.ZodSchema<T>, data: unknown): { success: boolean; data?: T; errors?: Record<string, string> } => {
  const result = schema.safeParse(data)

  if (result.success) {
    return { success: true, data: result.data }
  }

  const errors: Record<string, string> = {}
  result.error.issues.forEach((issue) => {
    const path = issue.path.join('.')
    errors[path] = issue.message
  })

  return { success: false, errors }
}