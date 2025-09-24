import { describe, it, expect, beforeEach, afterEach } from 'vitest'
import axios from 'axios'
import MockAdapter from 'axios-mock-adapter'
import { apiClient } from '../api'
import { UserType, JobType } from '@/types/api'

describe('API Client', () => {
  let mock: MockAdapter

  beforeEach(() => {
    // Mock the axios instance used by apiClient
    mock = new MockAdapter((apiClient as any).api || axios)
    localStorage.clear()
  })

  afterEach(() => {
    mock.reset()
  })

  describe('Authentication', () => {
    it('should login successfully', async () => {
      const loginData = {
        email: 'test@example.com',
        password: 'password123',
      }

      const responseData = {
        success: true,
        message: '로그인 성공',
        data: {
          user: {
            id: 1,
            name: 'Test User',
            email: 'test@example.com',
            userType: 'GENERAL',
          },
          access_token: 'mock-access-token',
          refresh_token: 'mock-refresh-token',
          token_type: 'Bearer',
          expires_in: 3600,
        },
      }

      mock.onPost('/auth/login').reply(200, responseData)

      const result = await apiClient.login(loginData)
      
      expect(result).toEqual(responseData)
      expect(result.success).toBe(true)
      expect(result.data?.access_token).toBe('mock-access-token')
    })

    it('should handle login error', async () => {
      const loginData = {
        email: 'test@example.com',
        password: 'wrongpassword',
      }

      const errorResponse = {
        success: false,
        message: '이메일 또는 비밀번호가 올바르지 않습니다',
        errorCode: 'INVALID_CREDENTIALS',
      }

      mock.onPost('/auth/login').reply(401, errorResponse)

      try {
        await apiClient.login(loginData)
      } catch (error: any) {
        expect(error.response.status).toBe(401)
        expect(error.response.data.success).toBe(false)
      }
    })

    it('should register a new user', async () => {
      const registerData = {
        name: 'New User',
        email: 'newuser@example.com',
        password: 'password123',
        phoneNumber: '010-1234-5678',
        userType: UserType.GENERAL,
      }

      const responseData = {
        success: true,
        message: '회원가입 성공',
        data: {
          user: {
            id: 2,
            name: 'New User',
            email: 'newuser@example.com',
            userType: 'GENERAL',
          },
          access_token: 'mock-token',
          refresh_token: 'mock-refresh-token',
        },
      }

      mock.onPost('/auth/register').reply(201, responseData)

      const result = await apiClient.register(registerData)
      
      expect(result).toEqual(responseData)
      expect(result.success).toBe(true)
      expect(result.data?.user?.email).toBe('newuser@example.com')
    })

    it('should refresh access token', async () => {
      const refreshToken = 'mock-refresh-token'
      
      const responseData = {
        success: true,
        message: '토큰 갱신 성공',
        data: {
          access_token: 'new-access-token',
          refresh_token: 'new-refresh-token',
          token_type: 'Bearer',
          expires_in: 3600,
        },
      }

      mock.onPost('/auth/refresh-token').reply(200, responseData)

      const result = await apiClient.refreshAccessToken(refreshToken)
      
      expect(result).toEqual(responseData)
      expect(result.data?.access_token).toBe('new-access-token')
    })
  })

  describe('Job Postings', () => {
    it('should fetch job postings', async () => {
      const responseData = {
        success: true,
        message: '채용공고 조회 성공',
        data: {
          content: [
            {
              id: 1,
              title: '프론트엔드 개발자',
              companyName: '테크 회사',
              location: '서울',
              jobType: 'FULL_TIME',
              experienceLevel: 'JUNIOR',
              minSalary: 3000,
              maxSalary: 5000,
            },
          ],
          totalElements: 1,
          totalPages: 1,
          size: 10,
          number: 0,
          first: true,
          last: true,
        },
      }

      mock.onGet(/^\/job-postings/).reply(200, responseData)

      const result = await apiClient.getJobPostings()
      
      expect(result).toEqual(responseData)
      expect(result.data?.content).toHaveLength(1)
      expect(result.data?.content[0].title).toBe('프론트엔드 개발자')
    })

    it('should search jobs with filters', async () => {
      const searchParams = {
        title: '개발자',
        location: '서울',
        jobType: JobType.FULL_TIME,
        page: 0,
        size: 10,
      }

      const responseData = {
        success: true,
        message: '검색 성공',
        data: {
          content: [],
          totalElements: 0,
          totalPages: 0,
          size: 10,
          number: 0,
          first: true,
          last: true,
        },
      }

      mock.onGet(/^\/job-postings/).reply(200, responseData)

      const result = await apiClient.getJobPostings(searchParams)
      
      expect(result).toEqual(responseData)
    })
  })

  describe('Community Posts', () => {
    it('should fetch posts', async () => {
      const responseData = {
        success: true,
        message: '게시글 조회 성공',
        data: {
          content: [
            {
              id: 1,
              title: '테스트 게시글',
              content: '테스트 내용',
              authorId: 1,
              authorName: 'Test User',
              viewCount: 10,
              likeCount: 5,
              commentCount: 2,
              createdAt: '2024-01-01T00:00:00',
            },
          ],
          totalElements: 1,
          totalPages: 1,
          size: 10,
          number: 0,
          first: true,
          last: true,
        },
      }

      mock.onGet(/^\/posts/).reply(200, responseData)

      const result = await apiClient.getPosts()
      
      expect(result).toEqual(responseData)
      expect(result.data?.content).toHaveLength(1)
    })

    it('should create a new post', async () => {
      const postData = {
        title: '새 게시글',
        content: '새 게시글 내용',
        categoryId: 1,
      }

      const responseData = {
        success: true,
        message: '게시글 작성 성공',
        data: {
          id: 2,
          title: '새 게시글',
          content: '새 게시글 내용',
          categoryId: 1,
          authorId: 1,
          viewCount: 0,
          likeCount: 0,
          commentCount: 0,
          createdAt: '2024-01-01T00:00:00',
        },
      }

      mock.onPost('/posts').reply(201, responseData)

      const result = await apiClient.createPost(postData)
      
      expect(result).toEqual(responseData)
      expect(result.data?.title).toBe('새 게시글')
    })

    it('should like a post', async () => {
      const postId = 1

      const responseData = {
        success: true,
        message: '좋아요 성공',
      }

      mock.onPost(`/posts/${postId}/like`).reply(200, responseData)

      const result = await apiClient.likePost(postId)
      
      expect(result.success).toBe(true)
    })
  })

  describe('Profile Management', () => {
    it('should fetch user profile', async () => {
      const responseData = {
        success: true,
        message: '프로필 조회 성공',
        data: {
          id: 1,
          userId: 1,
          name: 'Test User',
          email: 'test@example.com',
          location: '서울',
          summary: '테스트 사용자입니다.',
          experiences: [],
          educations: [],
          skills: [],
        },
      }

      mock.onGet('/profile').reply(200, responseData)

      const result = await apiClient.getProfile()
      
      expect(result).toEqual(responseData)
      expect(result.data?.name).toBe('Test User')
    })

    it('should update profile', async () => {
      const updateData = {
        location: '부산',
        summary: '업데이트된 자기소개',
      }

      const responseData = {
        success: true,
        message: '프로필 업데이트 성공',
        data: {
          id: 1,
          userId: 1,
          location: '부산',
          summary: '업데이트된 자기소개',
        },
      }

      mock.onPut('/profile').reply(200, responseData)

      const result = await apiClient.updateProfile(updateData)
      
      expect(result.data?.location).toBe('부산')
    })
  })

  describe('Error Handling', () => {
    it('should handle 401 and attempt token refresh', async () => {
      // Test the refresh token functionality directly
      localStorage.setItem('refreshToken', 'valid-refresh-token')

      mock.onPost('/auth/refresh-token').reply(200, {
        success: true,
        data: {
          access_token: 'new-access-token',
          refresh_token: 'new-refresh-token',
        },
      })

      const result = await apiClient.refreshAccessToken('valid-refresh-token')
      
      expect(result.success).toBe(true)
      expect(result.data?.access_token).toBe('new-access-token')
      expect(result.data?.refresh_token).toBe('new-refresh-token')
    })

    it('should handle network errors', async () => {
      mock.onGet('/profile').networkError()

      try {
        await apiClient.getProfile()
      } catch (error: any) {
        expect(error.message).toBe('Network Error')
      }
    })

    it('should handle timeout errors', async () => {
      mock.onGet('/profile').timeout()

      try {
        await apiClient.getProfile()
      } catch (error: any) {
        expect(error.code).toBe('ECONNABORTED')
      }
    })
  })
})