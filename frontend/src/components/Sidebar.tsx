import { Link, useLocation } from 'react-router-dom'
import {
  LayoutDashboard,
  Briefcase,
  Users,
  MessageSquare,
  Brain,
  FileText,
  Languages,
  Award,
  Settings,
  Bot,
  Mail,
  Shield,
  ChevronDown,
  X,
  Plus,
  Building2
} from 'lucide-react'
import { useAuthStore } from '@/hooks/useAuthStore'
import { UserType } from '@/types/api'
import { useState, useEffect } from 'react'

const menuItems = [
  {
    title: '대시보드',
    icon: LayoutDashboard,
    path: '/dashboard',
  },
  {
    title: '채용공고',
    icon: Briefcase,
    path: '/jobs',
  },
  {
    title: '커뮤니티',
    icon: Users,
    path: '/community',
  },
  {
    title: '웹메일',
    icon: Mail,
    path: '/webmail',
  },
  {
    title: 'AI 서비스',
    icon: Brain,
    subItems: [
      { title: 'AI 챗봇', icon: Bot, path: '/ai/chatbot' },
      { title: 'AI 모의면접', icon: MessageSquare, path: '/ai/interview' },
      { title: '자기소개서 생성', icon: FileText, path: '/ai/cover-letter' },
      { title: 'AI 번역', icon: Languages, path: '/ai/translation' },
    ],
  },
  {
    title: '증명서',
    icon: Award,
    path: '/certificates',
  },
  {
    title: '설정',
    icon: Settings,
    path: '/settings',
  },
]

interface SidebarProps {
  isOpen?: boolean
  onClose?: () => void
}

export default function Sidebar({ isOpen = true, onClose }: SidebarProps) {
  const location = useLocation()
  const { user } = useAuthStore()
  const [expandedItems, setExpandedItems] = useState<string[]>(['AI 서비스'])
  const [isMobile, setIsMobile] = useState(false)

  // 사용자 타입에 따른 메뉴 활성화/비활성화 상태 확인
  const isMenuItemEnabled = (path: string) => {
    if (path === '/ai/interview') {
      return user?.userType === UserType.GENERAL
    }
    if (path === '/ai/cover-letter') {
      return user?.userType === UserType.GENERAL
    }
    if (path === '/certificates') {
      return user?.userType === UserType.GENERAL
    }
    return true
  }

  // 비활성화된 메뉴에 대한 툴팁 메시지
  const getDisabledMessage = (path: string) => {
    if (path === '/ai/interview') {
      return '개인회원만 이용 가능한 서비스입니다'
    }
    if (path === '/ai/cover-letter') {
      return '개인회원만 이용 가능한 서비스입니다'
    }
    if (path === '/certificates') {
      return '개인회원만 이용 가능한 서비스입니다'
    }
    return ''
  }

  const displayMenuItems = menuItems

  useEffect(() => {
    const checkIsMobile = () => {
      setIsMobile(window.innerWidth < 1024)
    }

    checkIsMobile()
    window.addEventListener('resize', checkIsMobile)
    return () => window.removeEventListener('resize', checkIsMobile)
  }, [])

  const toggleExpanded = (title: string) => {
    setExpandedItems(prev =>
      prev.includes(title)
        ? prev.filter(item => item !== title)
        : [...prev, title]
    )
  }

  const handleKeyDown = (event: React.KeyboardEvent, callback: () => void) => {
    if (event.key === 'Enter' || event.key === ' ') {
      event.preventDefault()
      callback()
    }
  }

  const sidebarClasses = `
    fixed left-0 top-0 h-full w-64 bg-white dark:bg-gray-900 border-r border-gray-200 dark:border-gray-700 pt-16 z-40
    transform transition-transform duration-300 ease-in-out
    ${isMobile ? (isOpen ? 'translate-x-0' : '-translate-x-full') : 'translate-x-0'}
  `

  return (
    <>
      {/* Mobile overlay */}
      {isMobile && isOpen && (
        <div
          className="fixed inset-0 bg-black bg-opacity-50 z-30 lg:hidden"
          onClick={onClose}
        />
      )}

      <aside className={sidebarClasses}>
        {/* Mobile close button */}
        {isMobile && (
          <button
            onClick={onClose}
            className="absolute top-4 right-4 p-2 text-gray-400 hover:text-gray-600 dark:text-gray-500 dark:hover:text-gray-300 lg:hidden"
            aria-label="사이드바 닫기"
          >
            <X className="w-5 h-5" />
          </button>
        )}

        <nav
          className="p-4 space-y-2 overflow-y-auto h-full pb-20"
          role="navigation"
          aria-label="주요 메뉴"
        >
          {displayMenuItems.map((item) => (
            <div key={item.title}>
              {item.subItems ? (
                <div>
                  <button
                    onClick={() => toggleExpanded(item.title)}
                    onKeyDown={(e) => handleKeyDown(e, () => toggleExpanded(item.title))}
                    className="w-full flex items-center justify-between px-3 py-2 text-gray-600 dark:text-gray-300 font-medium hover:bg-gray-50 dark:hover:bg-gray-800 rounded-lg transition-colors focus:outline-none focus:ring-2 focus:ring-primary-500 focus:ring-offset-2 dark:focus:ring-offset-gray-900"
                    aria-expanded={expandedItems.includes(item.title)}
                    aria-controls={`submenu-${item.title.replace(/\s+/g, '-')}`}
                    type="button"
                  >
                    <div className="flex items-center">
                      <item.icon className="w-5 h-5 mr-3" />
                      {item.title}
                    </div>
                    <ChevronDown
                      className={`w-4 h-4 transition-transform duration-200 ${
                        expandedItems.includes(item.title) ? 'rotate-180' : ''
                      }`}
                    />
                  </button>

                  <div
                    id={`submenu-${item.title.replace(/\s+/g, '-')}`}
                    className={`ml-8 space-y-1 overflow-hidden transition-all duration-200 ${
                      expandedItems.includes(item.title) ? 'max-h-96 opacity-100' : 'max-h-0 opacity-0'
                    }`}
                    role="menu"
                    aria-label={`${item.title} 하위 메뉴`}
                  >
                    {item.subItems.map((subItem) => {
                      const isEnabled = isMenuItemEnabled(subItem.path)
                      const disabledMessage = getDisabledMessage(subItem.path)
                      
                      return isEnabled ? (
                        <Link
                          key={subItem.path}
                          to={subItem.path}
                          onClick={isMobile ? onClose : undefined}
                          className={`flex items-center px-3 py-2 text-sm rounded-lg transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-primary-500 focus:ring-offset-2 dark:focus:ring-offset-gray-900 ${
                            location.pathname === subItem.path
                              ? 'bg-primary-50 dark:bg-primary-900/30 text-primary-600 dark:text-primary-400 border-r-2 border-primary-600 dark:border-primary-400 font-medium'
                              : 'text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800 hover:text-gray-900 dark:hover:text-gray-100'
                          }`}
                          role="menuitem"
                          aria-current={location.pathname === subItem.path ? 'page' : undefined}
                        >
                          <subItem.icon className="w-4 h-4 mr-3" />
                          {subItem.title}
                        </Link>
                      ) : (
                        <div
                          key={subItem.path}
                          className="flex items-center px-3 py-2 text-sm rounded-lg text-gray-400 dark:text-gray-500 cursor-not-allowed opacity-60"
                          role="menuitem"
                          aria-disabled="true"
                          title={disabledMessage}
                        >
                          <subItem.icon className="w-4 h-4 mr-3" />
                          <span>{subItem.title}</span>
                          <span className="ml-auto text-xs text-gray-400 dark:text-gray-500">
                            개인회원 전용
                          </span>
                        </div>
                      )
                    })}
                  </div>
                </div>
              ) : (
                // 일반 메뉴 항목에 대한 활성화/비활성화 처리
                isMenuItemEnabled(item.path) ? (
                  <Link
                    to={item.path}
                    onClick={isMobile ? onClose : undefined}
                    className={`flex items-center px-3 py-2 rounded-lg transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-primary-500 focus:ring-offset-2 dark:focus:ring-offset-gray-900 ${
                      location.pathname === item.path
                        ? 'bg-primary-50 dark:bg-primary-900/30 text-primary-600 dark:text-primary-400 border-r-2 border-primary-600 dark:border-primary-400 font-medium'
                        : 'text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800 hover:text-gray-900 dark:hover:text-gray-100'
                    }`}
                    aria-current={location.pathname === item.path ? 'page' : undefined}
                  >
                    <item.icon className="w-5 h-5 mr-3" />
                    {item.title}
                  </Link>
                ) : (
                  <div
                    className="flex items-center px-3 py-2 rounded-lg text-gray-400 dark:text-gray-500 cursor-not-allowed opacity-60"
                    aria-disabled="true"
                    title={getDisabledMessage(item.path)}
                  >
                    <item.icon className="w-5 h-5 mr-3" />
                    <span>{item.title}</span>
                    <span className="ml-auto text-xs text-gray-400 dark:text-gray-500">
                      개인회원 전용
                    </span>
                  </div>
                )
              )}
            </div>
          ))}
        </nav>
      </aside>
    </>
  )
}