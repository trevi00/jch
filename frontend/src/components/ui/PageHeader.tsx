import { ReactNode } from 'react'
import { cn } from '@/lib/utils'
import { Button } from '@/components/ui/Button'

interface PageHeaderProps {
  title: string
  description?: string
  children?: ReactNode
  actions?: ReactNode
  className?: string
  gradient?: boolean
}

export default function PageHeader({
  title,
  description,
  children,
  actions,
  className,
  gradient = false
}: PageHeaderProps) {
  return (
    <div className={cn('space-y-6 pb-8', className)}>
      <div className="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
        <div className="space-y-2 flex-1">
          <h1
            className={cn(
              'text-3xl sm:text-4xl font-bold tracking-tight',
              gradient
                ? 'bg-gradient-to-r from-primary-600 to-purple-600 bg-clip-text text-transparent dark:from-primary-400 dark:to-purple-400'
                : 'text-gray-900 dark:text-gray-100'
            )}
          >
            {title}
          </h1>
          {description && (
            <p className="text-lg text-gray-600 dark:text-gray-400 max-w-2xl">
              {description}
            </p>
          )}
        </div>
        {actions && (
          <div className="flex flex-col sm:flex-row gap-2 shrink-0">
            {actions}
          </div>
        )}
      </div>
      {children && <div>{children}</div>}
    </div>
  )
}