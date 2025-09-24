import { getCardClassName } from '@/lib/utils'

interface LoadingCardProps {
  title?: string
  description?: string
  variant?: 'default' | 'elevated'
}

export default function LoadingCard({ title = '로딩 중...', description, variant = 'default' }: LoadingCardProps) {
  return (
    <div className={getCardClassName(variant)}>
      <div className="p-6">
        <div className="animate-pulse space-y-4">
          <div className="flex items-center space-x-3">
            <div className="w-8 h-8 bg-gray-300 dark:bg-gray-600 rounded-full"></div>
            <div className="flex-1 space-y-2">
              <div className="h-4 bg-gray-300 dark:bg-gray-600 rounded w-3/4"></div>
              {description && <div className="h-3 bg-gray-300 dark:bg-gray-600 rounded w-1/2"></div>}
            </div>
          </div>
          <div className="space-y-2">
            <div className="h-3 bg-gray-300 dark:bg-gray-600 rounded"></div>
            <div className="h-3 bg-gray-300 dark:bg-gray-600 rounded w-5/6"></div>
            <div className="h-3 bg-gray-300 dark:bg-gray-600 rounded w-4/6"></div>
          </div>
        </div>
      </div>
    </div>
  )
}