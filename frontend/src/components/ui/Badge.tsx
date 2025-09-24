import React from 'react';
import { cva, type VariantProps } from 'class-variance-authority';
import { cn } from '@/lib/utils';

const badgeVariants = cva(
  'inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-semibold transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-primary-500 focus:ring-offset-2 dark:focus:ring-offset-gray-900',
  {
    variants: {
      variant: {
        default: 'border-transparent bg-primary-100 dark:bg-primary-900/30 text-primary-800 dark:text-primary-200',
        secondary: 'border-transparent bg-gray-100 dark:bg-gray-700 text-gray-800 dark:text-gray-200',
        destructive: 'border-transparent bg-red-100 dark:bg-red-900/30 text-red-800 dark:text-red-200',
        success: 'border-transparent bg-green-100 dark:bg-green-900/30 text-green-800 dark:text-green-200',
        warning: 'border-transparent bg-orange-100 dark:bg-orange-900/30 text-orange-800 dark:text-orange-200',
        outline: 'border-gray-300 dark:border-gray-600 text-gray-700 dark:text-gray-300 bg-transparent',
        info: 'border-transparent bg-blue-50 dark:bg-blue-900/30 text-blue-700 dark:text-blue-200',
      },
      size: {
        sm: 'px-2 py-0.5 text-xs',
        default: 'px-2.5 py-0.5 text-xs',
        lg: 'px-3 py-1 text-sm',
      },
    },
    defaultVariants: {
      variant: 'default',
      size: 'default',
    },
  }
);

export interface BadgeProps
  extends React.HTMLAttributes<HTMLDivElement>,
    VariantProps<typeof badgeVariants> {}

function Badge({ className, variant, size, ...props }: BadgeProps) {
  return (
    <div className={cn(badgeVariants({ variant, size }), className)} {...props} />
  );
}

export { Badge, badgeVariants };