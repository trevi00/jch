import { type ClassValue, clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

export function getCardClassName(variant: 'default' | 'elevated' = 'default') {
  const base = 'bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700'

  if (variant === 'elevated') {
    return cn(base, 'shadow-lg')
  }

  return cn(base, 'shadow-sm')
}

export function getButtonClassName(variant: 'primary' | 'secondary' | 'danger' = 'primary') {
  const base = 'px-4 py-2 rounded-lg font-medium transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-offset-2'

  switch (variant) {
    case 'primary':
      return cn(base, 'bg-primary-600 hover:bg-primary-700 text-white focus:ring-primary-500')
    case 'secondary':
      return cn(base, 'bg-gray-200 dark:bg-gray-700 hover:bg-gray-300 dark:hover:bg-gray-600 text-gray-900 dark:text-gray-100 focus:ring-gray-500')
    case 'danger':
      return cn(base, 'bg-red-600 hover:bg-red-700 text-white focus:ring-red-500')
    default:
      return cn(base, 'bg-primary-600 hover:bg-primary-700 text-white focus:ring-primary-500')
  }
}

export function getStatusColor(status: string) {
  const colors = {
    success: 'text-green-600 dark:text-green-400 bg-green-50 dark:bg-green-900/30',
    warning: 'text-orange-600 dark:text-orange-400 bg-orange-50 dark:bg-orange-900/30',
    error: 'text-red-600 dark:text-red-400 bg-red-50 dark:bg-red-900/30',
    info: 'text-blue-600 dark:text-blue-400 bg-blue-50 dark:bg-blue-900/30',
    pending: 'text-gray-600 dark:text-gray-400 bg-gray-50 dark:bg-gray-900/30',
  }
  return colors[status as keyof typeof colors] || colors.info
}

export function getGradientText(from = 'primary-600', to = 'purple-600') {
  return `bg-gradient-to-r from-${from} to-${to} bg-clip-text text-transparent dark:from-${from.replace('600', '400')} dark:to-${to.replace('600', '400')}`
}

export function getAnimationClasses() {
  return {
    fadeIn: 'animate-in fade-in duration-500',
    slideIn: 'animate-in slide-in-from-bottom-4 duration-500',
    scaleIn: 'animate-in zoom-in-95 duration-200',
    hover: 'transition-all duration-200 hover:scale-105',
    pulse: 'animate-pulse',
    spin: 'animate-spin',
    bounce: 'animate-bounce'
  }
}

export function formatDate(date: string | Date): string {
  const d = new Date(date);
  return d.toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  });
}

export function formatDateTime(date: string | Date): string {
  const d = new Date(date);
  return d.toLocaleString('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

export function formatRelativeTime(date: string | Date): string {
  const d = new Date(date);
  const now = new Date();
  const diff = now.getTime() - d.getTime();
  
  const seconds = Math.floor(diff / 1000);
  const minutes = Math.floor(seconds / 60);
  const hours = Math.floor(minutes / 60);
  const days = Math.floor(hours / 24);
  const weeks = Math.floor(days / 7);
  const months = Math.floor(days / 30);
  const years = Math.floor(days / 365);

  if (years > 0) return `${years}년 전`;
  if (months > 0) return `${months}개월 전`;
  if (weeks > 0) return `${weeks}주 전`;
  if (days > 0) return `${days}일 전`;
  if (hours > 0) return `${hours}시간 전`;
  if (minutes > 0) return `${minutes}분 전`;
  return '방금 전';
}

export function truncateText(text: string, maxLength: number): string {
  if (text.length <= maxLength) return text;
  return text.slice(0, maxLength) + '...';
}

export function slugify(text: string): string {
  return text
    .toLowerCase()
    .replace(/[^\w\s-]/g, '')
    .replace(/[\s_-]+/g, '-')
    .replace(/^-+|-+$/g, '');
}

export function debounce<T extends (...args: any[]) => void>(
  func: T,
  delay: number
): (...args: Parameters<T>) => void {
  let timeoutId: NodeJS.Timeout;
  return (...args: Parameters<T>) => {
    clearTimeout(timeoutId);
    timeoutId = setTimeout(() => func(...args), delay);
  };
}

export function throttle<T extends (...args: any[]) => void>(
  func: T,
  limit: number
): (...args: Parameters<T>) => void {
  let inThrottle: boolean;
  return (...args: Parameters<T>) => {
    if (!inThrottle) {
      func(...args);
      inThrottle = true;
      setTimeout(() => (inThrottle = false), limit);
    }
  };
}