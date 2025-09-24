import { Sun, Moon } from 'lucide-react'
import { useTheme } from '@/contexts/ThemeContext'

export default function DarkModeToggle() {
  const { theme, toggleTheme } = useTheme()

  return (
    <button
      onClick={toggleTheme}
      className="relative p-2 text-gray-600 hover:text-gray-900 dark:text-gray-300 dark:hover:text-gray-100 rounded-lg transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-primary-500 focus:ring-offset-2 dark:focus:ring-offset-gray-900"
      aria-label={`현재 ${theme === 'light' ? '라이트' : '다크'} 모드입니다. ${theme === 'light' ? '다크' : '라이트'} 모드로 전환하려면 클릭하세요`}
      aria-pressed={theme === 'dark'}
      type="button"
    >
      <div className="relative w-5 h-5">
        <Sun
          className={`absolute inset-0 w-5 h-5 transition-transform duration-300 ${
            theme === 'dark' ? 'rotate-90 scale-0' : 'rotate-0 scale-100'
          }`}
        />
        <Moon
          className={`absolute inset-0 w-5 h-5 transition-transform duration-300 ${
            theme === 'light' ? '-rotate-90 scale-0' : 'rotate-0 scale-100'
          }`}
        />
      </div>
    </button>
  )
}