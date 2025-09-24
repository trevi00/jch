/**
 * formatters.ts - 데이터 포맷팅 및 변환 유틸리티 함수 모음
 *
 * 🔧 사용 기술:
 * - TypeScript (타입 안전성)
 * - Intl API (국제화 및 지역화)
 * - JavaScript 내장 Date 객체
 * - 정규표현식 (전화번호 포맷팅)
 *
 * 📋 주요 기능:
 * - Enum 값을 사용자 친화적 텍스트로 변환
 * - 다양한 날짜 포맷 (절대/상대/full/short)
 * - 숫자 포맷팅 (통화, 퍼센트, 그룹핑)
 * - 한국식 전화번호 포맷팅
 * - 파일 크기 변환 (Bytes → KB/MB/GB)
 * - 텍스트 자르기 및 생략
 * - 상태별 색상 매핑
 * - 기간 계산 (년/월 단위)
 *
 * 🎯 사용 목적:
 * - UI에서 일관된 데이터 표시
 * - 사용자 경험 개선 (가독성)
 * - 국제화 대응 (한국어 기본)
 * - 접근성 향상 (명확한 텍스트)
 */

// 📊 상수 및 레이블 매핑 (다국어 지원 가능)
import {
  JOB_TYPE_LABELS,           // 채용 유형 라벨
  EXPERIENCE_LEVEL_LABELS,   // 경력 수준 라벨
  APPLICATION_STATUS_LABELS, // 지원 상태 라벨
  USER_TYPE_LABELS,          // 사용자 타입 라벨
  SKILL_CATEGORIES,          // 기술 카테고리 라벨
  EDUCATION_LEVELS,          // 학력 수준 라벨
  EMPLOYMENT_STATUSES        // 재직 상태 라벨
} from './constants'

// 🏷️ TypeScript 타입 정의 import
import {
  JobType,           // 채용 유형 enum
  ExperienceLevel,   // 경력 수준 enum
  ApplicationStatus, // 지원 상태 enum
  UserType,          // 사용자 타입 enum
  SkillCategory,     // 기술 카테고리 enum
  EducationLevel,    // 학력 수준 enum
  EmploymentStatus   // 재직 상태 enum
} from '@/types/api'

export const formatJobType = (type: JobType): string => {
  return JOB_TYPE_LABELS[type] || type
}

export const formatExperienceLevel = (level: ExperienceLevel): string => {
  return EXPERIENCE_LEVEL_LABELS[level] || level
}

export const formatApplicationStatus = (status: ApplicationStatus): string => {
  return APPLICATION_STATUS_LABELS[status] || status
}

export const formatUserType = (type: UserType): string => {
  return USER_TYPE_LABELS[type] || type
}

export const formatSkillCategory = (category: SkillCategory): string => {
  return SKILL_CATEGORIES[category] || category
}

export const formatEducationLevel = (level: EducationLevel): string => {
  return EDUCATION_LEVELS[level] || level
}

export const formatEmploymentStatus = (status: EmploymentStatus): string => {
  return EMPLOYMENT_STATUSES[status] || status
}

export const formatSalary = (min?: number, max?: number): string => {
  if (!min && !max) return '연봉 정보 없음'
  if (min && max) return `${min.toLocaleString()}만원 - ${max.toLocaleString()}만원`
  if (min) return `${min.toLocaleString()}만원 이상`
  if (max) return `${max.toLocaleString()}만원 이하`
  return '연봉 정보 없음'
}

/**
 * 📅 다양한 형식의 날짜 포맷팅 함수
 *
 * @param date - 포맷팅할 날짜 (string 또는 Date 객체)
 * @param format - 포맷 타입 ('full' | 'short' | 'relative')
 * @returns 포맷된 날짜 문자열
 *
 * 🔄 처리 로직:
 * 1. 문자열인 경우 Date 객체로 변환
 * 2. format 타입에 따른 분기 처리
 * 3. Intl.DateTimeFormat API 사용 (한국어 로케일)
 *
 * 📝 포맷 옵션:
 * - 'full': 2024년 1월 15일 오후 2:30 (시간 포함)
 * - 'short': 2024년 1월 15일 (날짜만)
 * - 'relative': 5분 전, 2시간 전 등 (상대적 시간)
 */
export const formatDate = (date: string | Date, format: 'full' | 'short' | 'relative' = 'short'): string => {
  // 🔄 문자열을 Date 객체로 안전하게 변환
  const dateObj = typeof date === 'string' ? new Date(date) : date

  // 🕐 상대적 시간 포맷 요청 시 별도 함수 호출
  if (format === 'relative') {
    return formatRelativeDate(dateObj)
  }

  // 🌐 Intl API를 사용한 한국어 날짜 포맷팅
  const options: Intl.DateTimeFormatOptions = format === 'full'
    ? { year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit' }
    : { year: 'numeric', month: 'long', day: 'numeric' }

  return dateObj.toLocaleDateString('ko-KR', options)
}

/**
 * ⏰ 상대적 시간 표시 함수 (페이스북, 트위터 스타일)
 *
 * @param date - 기준이 되는 날짜
 * @returns 상대적 시간 문자열 (예: "5분 전", "2시간 전")
 *
 * 🧮 알고리즘:
 * 1. 현재 시간과의 차이를 초 단위로 계산
 * 2. 시간 단위별로 조건 분기
 * 3. 가장 적절한 단위로 표시
 *
 * ⏱️ 시간 단위 상수:
 * - 60초 = 1분
 * - 3600초 = 1시간 (60 * 60)
 * - 86400초 = 1일 (60 * 60 * 24)
 * - 2592000초 = 1개월 (60 * 60 * 24 * 30)
 * - 31536000초 = 1년 (60 * 60 * 24 * 365)
 */
export const formatRelativeDate = (date: Date): string => {
  const now = new Date()
  // 🔢 밀리초 차이를 초 단위로 변환 (Math.floor로 소수점 제거)
  const diffInSeconds = Math.floor((now.getTime() - date.getTime()) / 1000)

  // 📊 시간 차이에 따른 단계별 포맷팅
  if (diffInSeconds < 60) return '방금 전'                                    // 1분 미만
  if (diffInSeconds < 3600) return `${Math.floor(diffInSeconds / 60)}분 전`    // 1시간 미만
  if (diffInSeconds < 86400) return `${Math.floor(diffInSeconds / 3600)}시간 전` // 1일 미만
  if (diffInSeconds < 2592000) return `${Math.floor(diffInSeconds / 86400)}일 전` // 1개월 미만
  if (diffInSeconds < 31536000) return `${Math.floor(diffInSeconds / 2592000)}개월 전` // 1년 미만
  return `${Math.floor(diffInSeconds / 31536000)}년 전`                        // 1년 이상
}

export const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 Bytes'
  
  const k = 1024
  const sizes = ['Bytes', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

export const formatPhoneNumber = (phone: string): string => {
  // Remove all non-digits
  const cleaned = phone.replace(/\D/g, '')
  
  // Format as Korean phone number
  if (cleaned.length === 11) {
    // Mobile numbers: 010-1234-5678
    if (cleaned.startsWith('010')) {
      return cleaned.replace(/(\d{3})(\d{4})(\d{4})/, '$1-$2-$3')
    }
    // Other 11-digit numbers
    return cleaned.replace(/(\d{3})(\d{4})(\d{4})/, '$1-$2-$3')
  } else if (cleaned.length === 10) {
    // Seoul area code: 02-1234-5678
    if (cleaned.startsWith('02')) {
      return cleaned.replace(/(\d{2})(\d{4})(\d{4})/, '$1-$2-$3')
    }
    // Other area codes: 031-123-4567
    return cleaned.replace(/(\d{3})(\d{3})(\d{4})/, '$1-$2-$3')
  } else if (cleaned.length === 9) {
    // Some area codes with shorter numbers: 02-123-4567
    if (cleaned.startsWith('02')) {
      return cleaned.replace(/(\d{2})(\d{3})(\d{4})/, '$1-$2-$3')
    }
  }
  
  return phone
}

export const formatPercentage = (value: number, decimals: number = 1): string => {
  return `${value.toFixed(decimals)}%`
}

export const formatNumber = (num: number, useGrouping: boolean = true): string => {
  return num.toLocaleString('ko-KR', { useGrouping })
}

export const truncateText = (text: string, maxLength: number): string => {
  if (text.length <= maxLength) return text
  return text.slice(0, maxLength) + '...'
}

export const formatSkillLevel = (level: number): string => {
  const levels = ['입문', '초급', '중급', '고급', '전문가']
  return levels[level - 1] || '알 수 없음'
}

export const getApplicationStatusColor = (status: ApplicationStatus): string => {
  const colors: Record<ApplicationStatus, string> = {
    [ApplicationStatus.PENDING]: 'text-yellow-600 bg-yellow-100',
    [ApplicationStatus.REVIEWING]: 'text-blue-600 bg-blue-100',
    [ApplicationStatus.INTERVIEW]: 'text-purple-600 bg-purple-100',
    [ApplicationStatus.APPROVED]: 'text-green-600 bg-green-100',
    [ApplicationStatus.ACCEPTED]: 'text-green-600 bg-green-100',
    [ApplicationStatus.REJECTED]: 'text-red-600 bg-red-100',
  }
  return colors[status] || 'text-gray-600 bg-gray-100'
}

export const getJobTypeColor = (type: JobType): string => {
  const colors = {
    [JobType.FULL_TIME]: 'text-green-600 bg-green-100',
    [JobType.PART_TIME]: 'text-blue-600 bg-blue-100',
    [JobType.CONTRACT]: 'text-purple-600 bg-purple-100',
    [JobType.INTERN]: 'text-orange-600 bg-orange-100',
    [JobType.FREELANCE]: 'text-pink-600 bg-pink-100',
  }
  return colors[type] || 'text-gray-600 bg-gray-100'
}

export const getExperienceLevelColor = (level: ExperienceLevel): string => {
  const colors = {
    [ExperienceLevel.ENTRY_LEVEL]: 'text-green-600 bg-green-100',
    [ExperienceLevel.JUNIOR]: 'text-blue-600 bg-blue-100',
    [ExperienceLevel.MID_LEVEL]: 'text-purple-600 bg-purple-100',
    [ExperienceLevel.SENIOR]: 'text-orange-600 bg-orange-100',
    [ExperienceLevel.EXPERT]: 'text-red-600 bg-red-100',
    [ExperienceLevel.MANAGER]: 'text-indigo-600 bg-indigo-100',
    [ExperienceLevel.DIRECTOR]: 'text-pink-600 bg-pink-100',
    [ExperienceLevel.ANY]: 'text-gray-600 bg-gray-100',
  }
  return colors[level] || 'text-gray-600 bg-gray-100'
}

export const formatDuration = (startDate: string, endDate?: string): string => {
  const start = new Date(startDate)
  const end = endDate ? new Date(endDate) : new Date()
  
  const diffInMonths = (end.getFullYear() - start.getFullYear()) * 12 + (end.getMonth() - start.getMonth())
  
  if (diffInMonths < 1) return '1개월 미만'
  if (diffInMonths < 12) return `${diffInMonths}개월`
  
  const years = Math.floor(diffInMonths / 12)
  const months = diffInMonths % 12
  
  if (months === 0) return `${years}년`
  return `${years}년 ${months}개월`
}