import { describe, it, expect } from 'vitest'
import {
  formatJobType,
  formatExperienceLevel,
  formatSalary,
  formatDate,
  formatRelativeDate,
  formatFileSize,
  formatPhoneNumber,
  truncateText,
  formatDuration,
} from '../formatters'
import { JobType, ExperienceLevel } from '@/types/api'

describe('Formatter Functions', () => {
  describe('formatJobType', () => {
    it('formats job types correctly', () => {
      expect(formatJobType(JobType.FULL_TIME)).toBe('정규직')
      expect(formatJobType(JobType.PART_TIME)).toBe('파트타임')
      expect(formatJobType(JobType.CONTRACT)).toBe('계약직')
      expect(formatJobType(JobType.INTERN)).toBe('인턴십')
      expect(formatJobType(JobType.FREELANCE)).toBe('프리랜서')
    })

    it('returns original value for unknown types', () => {
      expect(formatJobType('UNKNOWN' as JobType)).toBe('UNKNOWN')
    })
  })

  describe('formatExperienceLevel', () => {
    it('formats experience levels correctly', () => {
      expect(formatExperienceLevel(ExperienceLevel.ENTRY_LEVEL)).toBe('신입')
      expect(formatExperienceLevel(ExperienceLevel.JUNIOR)).toBe('주니어 (1-3년)')
      expect(formatExperienceLevel(ExperienceLevel.MID_LEVEL)).toBe('미들 (3-7년)')
      expect(formatExperienceLevel(ExperienceLevel.SENIOR)).toBe('시니어 (7년+)')
      expect(formatExperienceLevel(ExperienceLevel.EXPERT)).toBe('전문가')
    })
  })

  describe('formatSalary', () => {
    it('formats salary ranges correctly', () => {
      expect(formatSalary(3000, 5000)).toBe('3,000만원 - 5,000만원')
      expect(formatSalary(3000)).toBe('3,000만원 이상')
      expect(formatSalary(undefined, 5000)).toBe('5,000만원 이하')
      expect(formatSalary()).toBe('연봉 정보 없음')
    })
  })

  describe('formatDate', () => {
    it('formats dates correctly', () => {
      const date = '2024-01-15T10:30:00'
      
      expect(formatDate(date, 'short')).toContain('2024')
      expect(formatDate(date, 'short')).toContain('1월')
      expect(formatDate(date, 'short')).toContain('15')
    })

    it('formats relative dates', () => {
      const now = new Date()
      const oneHourAgo = new Date(now.getTime() - 60 * 60 * 1000)
      const oneDayAgo = new Date(now.getTime() - 24 * 60 * 60 * 1000)
      
      expect(formatRelativeDate(now)).toBe('방금 전')
      expect(formatRelativeDate(oneHourAgo)).toBe('1시간 전')
      expect(formatRelativeDate(oneDayAgo)).toBe('1일 전')
    })
  })

  describe('formatFileSize', () => {
    it('formats file sizes correctly', () => {
      expect(formatFileSize(0)).toBe('0 Bytes')
      expect(formatFileSize(1024)).toBe('1 KB')
      expect(formatFileSize(1024 * 1024)).toBe('1 MB')
      expect(formatFileSize(1024 * 1024 * 1024)).toBe('1 GB')
      expect(formatFileSize(1536)).toBe('1.5 KB')
    })
  })

  describe('formatPhoneNumber', () => {
    it('formats Korean phone numbers correctly', () => {
      expect(formatPhoneNumber('01012345678')).toBe('010-1234-5678')
      expect(formatPhoneNumber('010-1234-5678')).toBe('010-1234-5678')
      expect(formatPhoneNumber('0212345678')).toBe('02-1234-5678')
    })

    it('returns original for invalid formats', () => {
      expect(formatPhoneNumber('123')).toBe('123')
      expect(formatPhoneNumber('invalid')).toBe('invalid')
    })
  })

  describe('truncateText', () => {
    it('truncates long text', () => {
      const longText = 'This is a very long text that needs to be truncated'
      expect(truncateText(longText, 20)).toBe('This is a very long ...')
    })

    it('returns original text if shorter than max length', () => {
      const shortText = 'Short text'
      expect(truncateText(shortText, 20)).toBe('Short text')
    })
  })

  describe('formatDuration', () => {
    it('formats duration between dates', () => {
      const start = '2023-01-01'
      const end = '2024-03-15'
      
      expect(formatDuration(start, end)).toBe('1년 2개월')
    })

    it('formats duration without end date', () => {
      const twoYearsAgo = new Date()
      twoYearsAgo.setFullYear(twoYearsAgo.getFullYear() - 2)
      
      const result = formatDuration(twoYearsAgo.toISOString())
      expect(result).toContain('2년')
    })

    it('formats short durations', () => {
      const start = '2024-01-01'
      const end = '2024-01-15'
      
      expect(formatDuration(start, end)).toBe('1개월 미만')
    })
  })
})