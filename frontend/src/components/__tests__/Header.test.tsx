import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, fireEvent } from '@testing-library/react'
import { BrowserRouter } from 'react-router-dom'
import Header from '../Header'
import { useAuthStore } from '@/hooks/useAuthStore'

// Mock useAuthStore
vi.mock('@/hooks/useAuthStore', () => ({
  useAuthStore: vi.fn(),
}))

const renderHeader = () => {
  return render(
    <BrowserRouter>
      <Header />
    </BrowserRouter>
  )
}

describe('Header Component', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renders logo and navigation elements', () => {
    (useAuthStore as any).mockReturnValue({
      user: {
        id: 1,
        name: '테스트 사용자',
        email: 'test@example.com',
      },
      logout: vi.fn(),
    })

    renderHeader()
    
    expect(screen.getByText('잡았다')).toBeInTheDocument()
    expect(screen.getByPlaceholderText('채용공고, 기업명 검색...')).toBeInTheDocument()
  })

  it('shows user menu with user name and menu items', () => {
    (useAuthStore as any).mockReturnValue({
      user: {
        id: 1,
        name: '테스트 사용자',
        email: 'test@example.com',
      },
      logout: vi.fn(),
    })

    renderHeader()
    
    expect(screen.getByText('테스트 사용자')).toBeInTheDocument()
    expect(screen.getByText('내 프로필')).toBeInTheDocument()
    expect(screen.getByText('설정')).toBeInTheDocument()
    expect(screen.getByText('로그아웃')).toBeInTheDocument()
  })

  it('shows default user name when no user name provided', () => {
    (useAuthStore as any).mockReturnValue({
      user: {
        id: 1,
        email: 'test@example.com',
      },
      logout: vi.fn(),
    })

    renderHeader()
    
    expect(screen.getByText('사용자')).toBeInTheDocument()
  })

  it('shows default user name when user is null', () => {
    (useAuthStore as any).mockReturnValue({
      user: null,
      logout: vi.fn(),
    })

    renderHeader()
    
    expect(screen.getByText('사용자')).toBeInTheDocument()
  })

  it('calls logout when logout button is clicked', () => {
    const mockLogout = vi.fn()
    
    ;(useAuthStore as any).mockReturnValue({
      user: {
        id: 1,
        name: '테스트 사용자',
        email: 'test@example.com',
      },
      logout: mockLogout,
    })

    renderHeader()
    
    // Click logout button
    const logoutButton = screen.getByText('로그아웃')
    fireEvent.click(logoutButton)
    
    expect(mockLogout).toHaveBeenCalled()
  })

  it('renders notification bell with count badge', () => {
    (useAuthStore as any).mockReturnValue({
      user: {
        id: 1,
        name: '테스트 사용자',
        email: 'test@example.com',
      },
      logout: vi.fn(),
    })

    renderHeader()
    
    // The bell button includes the badge text in its accessible name
    const bellButton = screen.getByRole('button', { name: '3' })
    expect(bellButton).toBeInTheDocument()
    expect(screen.getByText('3')).toBeInTheDocument()
  })

  it('has correct navigation links', () => {
    (useAuthStore as any).mockReturnValue({
      user: {
        id: 1,
        name: '테스트 사용자',
        email: 'test@example.com',
      },
      logout: vi.fn(),
    })

    renderHeader()
    
    // Check logo link
    const logoLink = screen.getByRole('link', { name: '잡았다' })
    expect(logoLink).toHaveAttribute('href', '/dashboard')
    
    // Check profile menu link
    const profileLink = screen.getByRole('link', { name: '내 프로필' })
    expect(profileLink).toHaveAttribute('href', '/profile')
    
    // Check settings menu link
    const settingsLink = screen.getByRole('link', { name: '설정' })
    expect(settingsLink).toHaveAttribute('href', '/settings')
  })
})