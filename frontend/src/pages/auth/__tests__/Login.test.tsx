import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { BrowserRouter } from 'react-router-dom'
import Login from '../Login'
import { apiClient } from '@/services/api'
import { useAuthStore } from '@/hooks/useAuthStore'

// Mocks
vi.mock('@/services/api', () => ({
  apiClient: {
    login: vi.fn(),
  },
}))

vi.mock('@/hooks/useAuthStore', () => ({
  useAuthStore: vi.fn(),
}))

const mockNavigate = vi.fn()
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom')
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  }
})

const renderLogin = () => {
  return render(
    <BrowserRouter>
      <Login />
    </BrowserRouter>
  )
}

describe('Login Component', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    
    ;(useAuthStore as any).mockReturnValue({
      login: vi.fn(),
    })
  })

  it('renders login form with email and password fields', () => {
    renderLogin()
    
    expect(screen.getByPlaceholderText('이메일 주소')).toBeInTheDocument()
    expect(screen.getByPlaceholderText('비밀번호')).toBeInTheDocument()
    const loginButtons = screen.getAllByRole('button', { name: /^로그인$/ })
    const submitButton = loginButtons.find(button => button.getAttribute('type') === 'submit')
    expect(submitButton).toBeInTheDocument()
  })

  it('shows validation errors for invalid inputs', async () => {
    const user = userEvent
    renderLogin()
    
    const loginButtons = screen.getAllByRole('button', { name: /^로그인$/ })
    const submitButton = loginButtons.find(button => button.getAttribute('type') === 'submit')!
    
    // Submit empty form
    await user.click(submitButton)
    
    await waitFor(() => {
      // Look for validation error messages specifically
      const errorMessages = screen.getAllByText(/이메일/i)
      expect(errorMessages.length).toBeGreaterThan(0)
    }, { timeout: 3000 })
  })

  it('shows/hides password when eye icon is clicked', async () => {
    const user = userEvent
    renderLogin()
    
    const passwordInput = screen.getByPlaceholderText('비밀번호') as HTMLInputElement
    expect(passwordInput.type).toBe('password')
    
    // Click eye icon
    const toggleButton = passwordInput.parentElement?.querySelector('button')
    if (toggleButton) {
      await user.click(toggleButton)
    }
    
    expect(passwordInput.type).toBe('text')
  })

  it('submits form with valid credentials', async () => {
    const user = userEvent
    const mockLogin = vi.fn()
    
    ;(useAuthStore as any).mockReturnValue({
      login: mockLogin,
    })
    
    ;(apiClient.login as any).mockResolvedValue({
      success: true,
      data: {
        user: { id: 1, name: 'Test User', email: 'test@example.com' },
        access_token: 'mock-token',
        refresh_token: 'mock-refresh-token',
      },
    })
    
    renderLogin()
    
    const emailInput = screen.getByPlaceholderText('이메일 주소')
    const passwordInput = screen.getByPlaceholderText('비밀번호')
    const loginButtons = screen.getAllByRole('button', { name: /^로그인$/ })
    const submitButton = loginButtons.find(button => button.getAttribute('type') === 'submit')!
    
    await user.type(emailInput, 'test@example.com')
    await user.type(passwordInput, 'password123')
    await user.click(submitButton)
    
    await waitFor(() => {
      expect(apiClient.login).toHaveBeenCalledWith({
        email: 'test@example.com',
        password: 'password123',
      })
      expect(mockLogin).toHaveBeenCalled()
    }, { timeout: 3000 })
  })

  it('shows error message when login fails', async () => {
    const user = userEvent
    
    ;(apiClient.login as any).mockResolvedValue({
      success: false,
      message: '이메일 또는 비밀번호가 올바르지 않습니다',
    })
    
    renderLogin()
    
    const emailInput = screen.getByPlaceholderText('이메일 주소')
    const passwordInput = screen.getByPlaceholderText('비밀번호')
    const loginButtons = screen.getAllByRole('button', { name: /^로그인$/ })
    const submitButton = loginButtons.find(button => button.getAttribute('type') === 'submit')!
    
    await user.type(emailInput, 'test@example.com')
    await user.type(passwordInput, 'wrongpassword')
    await user.click(submitButton)
    
    await waitFor(() => {
      expect(screen.getByText(/올바르지 않습니다/)).toBeInTheDocument()
    }, { timeout: 3000 })
  })

  it('has link to register page', () => {
    renderLogin()
    
    const registerLink = screen.getByRole('link', { name: /새 계정 만들기/ })
    expect(registerLink).toHaveAttribute('href', '/register')
  })

  it('has link to forgot password page', () => {
    renderLogin()
    
    const forgotPasswordLink = screen.getByRole('link', { name: /비밀번호 찾기/ })
    expect(forgotPasswordLink).toHaveAttribute('href', '/forgot-password')
  })
})