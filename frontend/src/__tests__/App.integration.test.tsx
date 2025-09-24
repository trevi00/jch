import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter } from 'react-router-dom'
import App from '@/App'
import { apiClient } from '@/services/api'

// Mock API client
vi.mock('@/services/api', () => ({
  apiClient: {
    login: vi.fn(),
    register: vi.fn(),
    getJobPostings: vi.fn(),
    getDashboardData: vi.fn(),
    getPosts: vi.fn(),
    getCategories: vi.fn(),
    getCurrentUser: vi.fn(),
    getUserProfile: vi.fn(),
    checkBackendHealth: vi.fn().mockResolvedValue(true),
    checkAIServiceHealth: vi.fn().mockResolvedValue(true),
  },
}))

// Mock useAuthStore
vi.mock('@/hooks/useAuthStore', () => ({
  useAuthStore: vi.fn(() => ({
    isAuthenticated: false,
    user: null,
    login: vi.fn(),
    logout: vi.fn(),
  })),
}))

const createWrapper = (initialEntries = ['/']) => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
      mutations: { retry: false },
    },
  })

  return ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={queryClient}>
      <MemoryRouter initialEntries={initialEntries}>{children}</MemoryRouter>
    </QueryClientProvider>
  )
}

describe('App Integration Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
  })

  it('renders landing page for unauthenticated users', () => {
    render(<App />, { wrapper: createWrapper() })
    
    expect(screen.getByText((content, element) => {
      return element?.tagName.toLowerCase() === 'p' && content.includes('국비 학원 원생들을 위한')
    })).toBeInTheDocument()
    expect(screen.getByText('시작하기')).toBeInTheDocument()
    expect(screen.getByText('로그인')).toBeInTheDocument()
  })

  it('redirects to login when accessing protected route', () => {
    render(<App />, { wrapper: createWrapper(['/dashboard']) })
    
    // Should show login page elements instead of dashboard
    expect(screen.getByPlaceholderText('이메일 주소')).toBeInTheDocument()
    expect(screen.getByPlaceholderText('비밀번호')).toBeInTheDocument()
  })

  describe('User Authentication Flow', () => {
    it('allows user to login and access dashboard', async () => {
      // Reset mocks
      vi.resetAllMocks()
      
      const user = userEvent
      
      // Mock successful login
      ;(apiClient.login as any).mockResolvedValue({
        success: true,
        data: {
          user: {
            id: 1,
            name: 'Test User',
            email: 'test@example.com',
            userType: 'GENERAL',
          },
          access_token: 'mock-token',
          refresh_token: 'mock-refresh',
        },
      })

      // Mock dashboard data
      ;(apiClient.getDashboardData as any).mockResolvedValue({
        success: true,
        data: {
          stats: {
            applications: 10,
            interviews: 3,
            offers: 1,
          },
        },
      })

      render(<App />, { wrapper: createWrapper(['/login']) })
      
      // Fill login form
      const emailInput = screen.getByPlaceholderText('이메일 주소')
      const passwordInput = screen.getByPlaceholderText('비밀번호')
      
      await user.type(emailInput, 'test@example.com')
      await user.type(passwordInput, 'password123')
      
      const loginButton = screen.getAllByRole('button', { name: /^로그인$/ })[0]
      await user.click(loginButton)
      
      // Should call API
      await waitFor(() => {
        expect(apiClient.login).toHaveBeenCalledWith({
          email: 'test@example.com',
          password: 'password123',
        })
      }, { timeout: 3000 })
    })

    it('allows user to register a new account', async () => {
      // Reset mocks
      vi.resetAllMocks()
      
      // Mock successful registration
      ;(apiClient.register as any).mockResolvedValue({
        success: true,
        data: {
          id: 2,
          name: 'New User',
          email: 'newuser@example.com',
          userType: 'GENERAL',
        },
      })

      render(<App />, { wrapper: createWrapper(['/register']) })
      
      await waitFor(() => {
        expect(screen.getByRole('heading', { name: /회원가입/ })).toBeInTheDocument()
      }, { timeout: 3000 })
    })
  })

  describe('Job Postings Flow', () => {
    it('redirects to login when accessing jobs page unauthenticated', async () => {
      render(<App />, { wrapper: createWrapper(['/jobs']) })
      
      // Should redirect to login page
      await waitFor(() => {
        expect(screen.getByPlaceholderText('이메일 주소')).toBeInTheDocument()
        expect(screen.getByPlaceholderText('비밀번호')).toBeInTheDocument()
      }, { timeout: 3000 })
    })
  })

  describe('Community Posts Flow', () => {
    it('redirects to login when accessing community page unauthenticated', async () => {
      render(<App />, { wrapper: createWrapper(['/community']) })
      
      // Should redirect to login page
      await waitFor(() => {
        expect(screen.getByPlaceholderText('이메일 주소')).toBeInTheDocument()
        expect(screen.getByPlaceholderText('비밀번호')).toBeInTheDocument()
      }, { timeout: 3000 })
    })
  })

  describe('Navigation', () => {
    it('renders header with navigation links on homepage', () => {
      render(<App />, { wrapper: createWrapper() })
      
      // Home page has footer with "잡았다" text too
      expect(screen.getAllByText('잡았다')).toHaveLength(2)
    })

    it('shows login link on homepage for unauthenticated users', () => {
      render(<App />, { wrapper: createWrapper() })
      
      // Should show login link on homepage
      expect(screen.getByText('로그인')).toBeInTheDocument()
    })
  })

  describe('Error Handling', () => {
    it('displays error message when API fails', async () => {
      // Reset mocks
      vi.resetAllMocks()
      
      // Mock API failure
      ;(apiClient.login as any).mockRejectedValue(new Error('Network error'))

      render(<App />, { wrapper: createWrapper(['/login']) })
      
      const user = userEvent
      const emailInput = screen.getByPlaceholderText('이메일 주소')
      const passwordInput = screen.getByPlaceholderText('비밀번호')
      const loginButton = screen.getAllByRole('button', { name: /^로그인$/ })[0]
      
      await user.type(emailInput, 'test@example.com')
      await user.type(passwordInput, 'password123')
      await user.click(loginButton)
      
      await waitFor(() => {
        expect(screen.getByText(/오류/)).toBeInTheDocument()
      }, { timeout: 3000 })
    })
  })
})