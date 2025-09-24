import { AlertCircle, RefreshCw } from 'lucide-react'
import { getButtonClassName } from '@/lib/utils'

interface ErrorMessageProps {
  title?: string
  message?: string
  onRetry?: () => void
  retryLabel?: string
  showIcon?: boolean
}

export default function ErrorMessage({
  title = '오류가 발생했습니다',
  message = '잠시 후 다시 시도해주세요.',
  onRetry,
  retryLabel = '다시 시도',
  showIcon = true
}: ErrorMessageProps) {
  return (
    <div className="flex flex-col items-center justify-center p-8 text-center">
      {showIcon && (
        <AlertCircle className="w-12 h-12 text-red-500 dark:text-red-400 mb-4" />
      )}
      <h3 className="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-2">
        {title}
      </h3>
      <p className="text-gray-600 dark:text-gray-400 mb-6 max-w-md">
        {message}
      </p>
      {onRetry && (
        <button
          onClick={onRetry}
          className={getButtonClassName('secondary')}
        >
          <RefreshCw className="w-4 h-4 mr-2" />
          {retryLabel}
        </button>
      )}
    </div>
  )
}