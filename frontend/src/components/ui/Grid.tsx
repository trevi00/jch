import { ReactNode } from 'react'
import { cn } from '@/lib/utils'

interface GridProps {
  children: ReactNode
  cols?: 1 | 2 | 3 | 4 | 5 | 6
  gap?: 2 | 3 | 4 | 5 | 6 | 8
  className?: string
  responsive?: {
    sm?: 1 | 2 | 3
    md?: 1 | 2 | 3 | 4
    lg?: 1 | 2 | 3 | 4 | 5 | 6
    xl?: 1 | 2 | 3 | 4 | 5 | 6
  }
}

export default function Grid({
  children,
  cols = 1,
  gap = 6,
  className,
  responsive
}: GridProps) {
  const colsClass = {
    1: 'grid-cols-1',
    2: 'grid-cols-2',
    3: 'grid-cols-3',
    4: 'grid-cols-4',
    5: 'grid-cols-5',
    6: 'grid-cols-6'
  }

  const gapClass = {
    2: 'gap-2',
    3: 'gap-3',
    4: 'gap-4',
    5: 'gap-5',
    6: 'gap-6',
    8: 'gap-8'
  }

  const responsiveClasses = responsive
    ? [
        responsive.sm && `sm:grid-cols-${responsive.sm}`,
        responsive.md && `md:grid-cols-${responsive.md}`,
        responsive.lg && `lg:grid-cols-${responsive.lg}`,
        responsive.xl && `xl:grid-cols-${responsive.xl}`
      ].filter(Boolean).join(' ')
    : ''

  return (
    <div
      className={cn(
        'grid',
        colsClass[cols],
        gapClass[gap],
        responsiveClasses,
        className
      )}
    >
      {children}
    </div>
  )
}