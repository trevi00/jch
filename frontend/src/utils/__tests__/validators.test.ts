import { describe, it, expect } from 'vitest'
import {
  emailSchema,
  passwordSchema,
  phoneNumberSchema,
  nameSchema,
  loginSchema,
  registerSchema,
  isValidKoreanPhoneNumber,
  isValidEmail,
  isValidPassword,
  getPasswordStrength,
  validateFile,
} from '../validators'
import { FILE_UPLOAD } from '../constants'

describe('Validator Functions', () => {
  describe('Email Validation', () => {
    it('validates correct email addresses', () => {
      expect(emailSchema.safeParse('user@example.com').success).toBe(true)
      expect(emailSchema.safeParse('test.user@domain.co.kr').success).toBe(true)
    })

    it('rejects invalid email addresses', () => {
      expect(emailSchema.safeParse('invalid').success).toBe(false)
      expect(emailSchema.safeParse('@example.com').success).toBe(false)
      expect(emailSchema.safeParse('user@').success).toBe(false)
      expect(emailSchema.safeParse('').success).toBe(false)
    })

    it('validates email helper function', () => {
      expect(isValidEmail('user@example.com')).toBe(true)
      expect(isValidEmail('invalid')).toBe(false)
    })
  })

  describe('Password Validation', () => {
    it('validates strong passwords', () => {
      expect(passwordSchema.safeParse('StrongPass123').success).toBe(true)
      expect(passwordSchema.safeParse('AnotherGood1').success).toBe(true)
    })

    it('rejects weak passwords', () => {
      expect(passwordSchema.safeParse('short').success).toBe(false) // Too short
      expect(passwordSchema.safeParse('alllowercase123').success).toBe(false) // No uppercase
      expect(passwordSchema.safeParse('ALLUPPERCASE123').success).toBe(false) // No lowercase
      expect(passwordSchema.safeParse('NoNumbers').success).toBe(false) // No numbers
    })

    it('validates password helper function', () => {
      expect(isValidPassword('StrongPass123')).toBe(true)
      expect(isValidPassword('weak')).toBe(false)
    })

    it('calculates password strength correctly', () => {
      const weak = getPasswordStrength('pass')
      expect(weak.label).toBe('약함')
      expect(weak.color).toBe('text-red-600')

      const medium = getPasswordStrength('Password1')
      expect(medium.label).toBe('보통')
      expect(medium.color).toBe('text-yellow-600')

      const strong = getPasswordStrength('StrongP@ssw0rd!')
      expect(strong.label).toBe('강함')
      expect(strong.color).toBe('text-green-600')
    })
  })

  describe('Phone Number Validation', () => {
    it('validates Korean phone numbers', () => {
      expect(phoneNumberSchema.safeParse('010-1234-5678').success).toBe(true)
      expect(phoneNumberSchema.safeParse('01012345678').success).toBe(true)
      expect(phoneNumberSchema.safeParse('011-234-5678').success).toBe(true)
    })

    it('rejects invalid phone numbers', () => {
      expect(phoneNumberSchema.safeParse('123-456-7890').success).toBe(false)
      expect(phoneNumberSchema.safeParse('010-12-5678').success).toBe(false)
      expect(phoneNumberSchema.safeParse('abc-def-ghij').success).toBe(false)
      expect(phoneNumberSchema.safeParse('010-1234').success).toBe(false)
    })

    it('validates phone number helper function', () => {
      expect(isValidKoreanPhoneNumber('01012345678')).toBe(true)
      expect(isValidKoreanPhoneNumber('010-1234-5678')).toBe(true)
      expect(isValidKoreanPhoneNumber('021234567')).toBe(true) // Seoul area code is valid
      expect(isValidKoreanPhoneNumber('12345')).toBe(false)
      expect(isValidKoreanPhoneNumber('abc123')).toBe(false)
    })
  })

  describe('Name Validation', () => {
    it('validates correct names', () => {
      expect(nameSchema.safeParse('홍길동').success).toBe(true)
      expect(nameSchema.safeParse('John Doe').success).toBe(true)
      expect(nameSchema.safeParse('김 철수').success).toBe(true)
    })

    it('rejects invalid names', () => {
      expect(nameSchema.safeParse('A').success).toBe(false) // Too short
      expect(nameSchema.safeParse('123').success).toBe(false) // Numbers
      expect(nameSchema.safeParse('name@123').success).toBe(false) // Special chars
    })
  })

  describe('Login Form Validation', () => {
    it('validates complete login form', () => {
      const validForm = {
        email: 'user@example.com',
        password: 'password123',
      }
      expect(loginSchema.safeParse(validForm).success).toBe(true)
    })

    it('rejects incomplete login form', () => {
      const invalidForm = {
        email: 'invalid',
        password: '',
      }
      expect(loginSchema.safeParse(invalidForm).success).toBe(false)
    })
  })

  describe('Register Form Validation', () => {
    it('validates complete registration form', () => {
      const validForm = {
        name: '홍길동',
        email: 'user@example.com',
        password: 'StrongPass123',
        confirmPassword: 'StrongPass123',
        phoneNumber: '010-1234-5678',
        userType: 'GENERAL',
        terms: true,
      }
      expect(registerSchema.safeParse(validForm).success).toBe(true)
    })

    it('rejects mismatched passwords', () => {
      const invalidForm = {
        name: '홍길동',
        email: 'user@example.com',
        password: 'StrongPass123',
        confirmPassword: 'DifferentPass123',
        phoneNumber: '010-1234-5678',
        userType: 'GENERAL',
        terms: true,
      }
      expect(registerSchema.safeParse(invalidForm).success).toBe(false)
    })

    it('rejects without terms agreement', () => {
      const invalidForm = {
        name: '홍길동',
        email: 'user@example.com',
        password: 'StrongPass123',
        confirmPassword: 'StrongPass123',
        phoneNumber: '010-1234-5678',
        userType: 'GENERAL',
        terms: false,
      }
      expect(registerSchema.safeParse(invalidForm).success).toBe(false)
    })
  })

  describe('File Validation', () => {
    it('validates image files', () => {
      const validImage = new File([''], 'test.jpg', { type: 'image/jpeg' })
      expect(validateFile(validImage, 'image')).toBe(null)

      const validPng = new File([''], 'test.png', { type: 'image/png' })
      expect(validateFile(validPng, 'image')).toBe(null)
    })

    it('validates document files', () => {
      const validPdf = new File([''], 'test.pdf', { type: 'application/pdf' })
      expect(validateFile(validPdf, 'document')).toBe(null)

      const validDoc = new File([''], 'test.docx', {
        type: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
      })
      expect(validateFile(validDoc, 'document')).toBe(null)
    })

    it('rejects invalid file types', () => {
      const invalidFile = new File([''], 'test.exe', { type: 'application/exe' })
      expect(validateFile(invalidFile, 'image')).toContain('지원하는 파일 형식')
      expect(validateFile(invalidFile, 'document')).toContain('지원하는 파일 형식')
    })

    it('rejects files that are too large', () => {
      const largeFile = new File(
        [new ArrayBuffer(FILE_UPLOAD.MAX_SIZE + 1)],
        'large.jpg',
        { type: 'image/jpeg' }
      )
      expect(validateFile(largeFile, 'image')).toContain('10MB 이하')
    })
  })
})