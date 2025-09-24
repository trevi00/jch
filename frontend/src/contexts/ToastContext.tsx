import { createContext, useContext, useState, useCallback } from 'react'
import Toast, { ToastType } from '@/components/ui/Toast'

/**
 * Toast 데이터 인터페이스
 * @interface ToastData
 * @property id - 고유 식별자
 * @property type - Toast 타입 (success, error, info, warning)
 * @property title - Toast 제목
 * @property message - Toast 메시지
 * @property duration - 표시 시간
 */
interface ToastData {
  id: string
  type: ToastType
  title: string
  message?: string
  duration?: number
}

/**
 * Toast Context 타입 인터페이스
 * @interface ToastContextType
 * @property addToast - Toast 추가 함수
 * @property removeToast - Toast 제거 함수
 * @property showSuccess - 성공 Toast 표시 함수
 * @property showError - 에러 Toast 표시 함수
 * @property showInfo - 정보 Toast 표시 함수
 * @property showWarning - 경고 Toast 표시 함수
 */
interface ToastContextType {
  addToast: (toast: Omit<ToastData, 'id'>) => void
  removeToast: (id: string) => void
  showSuccess: (title: string, message?: string) => void
  showError: (title: string, message?: string) => void
  showInfo: (title: string, message?: string) => void
  showWarning: (title: string, message?: string) => void
}

const ToastContext = createContext<ToastContextType | undefined>(undefined)

/**
 * Toast Context Provider 컴포넌트
 * 전역 Toast 알림 시스템을 제공
 * @param children - 자식 컴포넌트
 * 이벤트: Toast 추가/제거 이벤트, 자동 숨김 이벤트
 */
export function ToastProvider({ children }: { children: React.ReactNode }) {
  const [toasts, setToasts] = useState<ToastData[]>([])

  /**
   * Toast 추가 함수
   * @param toast Toast 데이터 (id 제외)
   * 이벤트: 새로운 Toast 생성 이벤트, 고유 ID 생성 이벤트
   */
  const addToast = useCallback((toast: Omit<ToastData, 'id'>) => {
    const id = Math.random().toString(36).substring(2, 9)
    setToasts(prev => [...prev, { ...toast, id }])
  }, [])

  /**
   * Toast 제거 함수
   * @param id 제거할 Toast의 ID
   * 이벤트: Toast 제거 이벤트, 상태 업데이트 이벤트
   */
  const removeToast = useCallback((id: string) => {
    setToasts(prev => prev.filter(toast => toast.id !== id))
  }, [])

  const showSuccess = useCallback((title: string, message?: string) => {
    addToast({ type: 'success', title, message })
  }, [addToast])

  const showError = useCallback((title: string, message?: string) => {
    addToast({ type: 'error', title, message })
  }, [addToast])

  const showInfo = useCallback((title: string, message?: string) => {
    addToast({ type: 'info', title, message })
  }, [addToast])

  const showWarning = useCallback((title: string, message?: string) => {
    addToast({ type: 'warning', title, message })
  }, [addToast])

  return (
    <ToastContext.Provider value={{ addToast, removeToast, showSuccess, showError, showInfo, showWarning }}>
      {children}

      {/* Toast Container */}
      <div
        aria-live="assertive"
        className="fixed inset-0 flex items-end justify-center px-4 py-6 pointer-events-none sm:p-6 sm:items-start sm:justify-end z-50"
      >
        <div className="w-full flex flex-col items-center space-y-4 sm:items-end">
          {toasts.map((toast) => (
            <Toast
              key={toast.id}
              id={toast.id}
              type={toast.type}
              title={toast.title}
              message={toast.message}
              duration={toast.duration}
              onClose={removeToast}
            />
          ))}
        </div>
      </div>
    </ToastContext.Provider>
  )
}

/**
 * Toast Context를 사용하기 위한 훅
 * ToastProvider 내부에서만 사용 가능
 * @returns Toast 관리 함수들을 포함한 객체
 * 이벤트: Context 연결 이벤트, 에러 발생 이벤트
 */
export function useToast() {
  const context = useContext(ToastContext)
  if (context === undefined) {
    throw new Error('useToast must be used within a ToastProvider')
  }
  return context
}