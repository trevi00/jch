import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Eye, EyeOff } from 'lucide-react'
import { apiClient } from '@/services/api'
import { UserType } from '@/types/api'

const registerSchema = z.object({
  name: z.string().min(2, '이름은 2자 이상이어야 합니다'),
  email: z.string().email('올바른 이메일 주소를 입력해주세요'),
  password: z.string().min(8, '비밀번호는 8자 이상이어야 합니다'),
  confirmPassword: z.string(),
  phoneNumber: z.string().min(10, '올바른 전화번호를 입력해주세요'),
  userType: z.nativeEnum(UserType),
  academy: z.string().optional(),
  companyName: z.string().optional(),
  terms: z.boolean().refine(val => val, '이용약관에 동의해주세요'),
}).refine((data) => data.password === data.confirmPassword, {
  message: '비밀번호가 일치하지 않습니다',
  path: ['confirmPassword'],
})

type RegisterForm = z.infer<typeof registerSchema>

export default function Register() {
  const [showPassword, setShowPassword] = useState(false)
  const [showConfirmPassword, setShowConfirmPassword] = useState(false)
  const [isLoading, setIsLoading] = useState(false)
  const navigate = useNavigate()

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors },
    setError,
  } = useForm<RegisterForm>({
    resolver: zodResolver(registerSchema),
    defaultValues: {
      userType: UserType.GENERAL,
    },
  })

  const userType = watch('userType')

  const onSubmit = async (data: RegisterForm) => {
    setIsLoading(true)
    try {
      const response = await apiClient.register({
        name: data.name,
        email: data.email,
        password: data.password,
        phoneNumber: data.phoneNumber,
        userType: data.userType,
        academy: data.academy,
        companyName: data.companyName,
      })
      
      if (response.success) {
        navigate('/login', { 
          state: { message: '회원가입이 완료되었습니다. 로그인해주세요.' } 
        })
      } else {
        setError('root', { message: response.message || '회원가입에 실패했습니다' })
      }
    } catch (error: any) {
      setError('root', { message: '회원가입 중 오류가 발생했습니다' })
    } finally {
      setIsLoading(false)
    }
  }

  const handleGoogleSignUp = async () => {
    try {
      const redirectUri = `${window.location.origin}/auth/callback`
      const response = await apiClient.getGoogleAuthUrl(redirectUri, 'GENERAL', 'SIGNUP')
      
      if (response.success && response.data) {
        // Google OAuth URL로 리다이렉트
        window.location.href = response.data
      } else {
        setError('root', { message: 'Google 인증 URL을 가져오는데 실패했습니다' })
      }
    } catch (error) {
      setError('root', { message: 'Google 인증 중 오류가 발생했습니다' })
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full space-y-8">
        <div>
          <Link to="/" className="flex justify-center">
            <span className="text-6xl font-bold text-primary-600">JOB았다</span>
          </Link>
          <h2 className="mt-6 text-center text-3xl font-bold text-gray-900">
            회원가입
          </h2>
          <p className="mt-2 text-center text-sm text-gray-600">
            또는{' '}
            <Link
              to="/login"
              className="font-medium text-primary-600 hover:text-primary-500"
            >
              기존 계정으로 로그인
            </Link>
          </p>
        </div>

        <form className="mt-8 space-y-6" onSubmit={handleSubmit(onSubmit)}>
          <div className="space-y-4">
            {/* 이름 */}
            <div>
              <label htmlFor="name" className="sr-only">
                이름
              </label>
              <input
                {...register('name')}
                type="text"
                autoComplete="name"
                className="input"
                placeholder="이름"
              />
              {errors.name && (
                <p className="mt-1 text-sm text-red-600">{errors.name.message}</p>
              )}
            </div>

            {/* 이메일 */}
            <div>
              <label htmlFor="email" className="sr-only">
                이메일
              </label>
              <input
                {...register('email')}
                type="email"
                autoComplete="email"
                className="input"
                placeholder="이메일 주소"
              />
              {errors.email && (
                <p className="mt-1 text-sm text-red-600">{errors.email.message}</p>
              )}
            </div>

            {/* 전화번호 */}
            <div>
              <label htmlFor="phoneNumber" className="sr-only">
                전화번호
              </label>
              <input
                {...register('phoneNumber')}
                type="tel"
                autoComplete="tel"
                className="input"
                placeholder="전화번호 (010-1234-5678)"
              />
              {errors.phoneNumber && (
                <p className="mt-1 text-sm text-red-600">{errors.phoneNumber.message}</p>
              )}
            </div>

            {/* 사용자 유형 */}
            <div>
              <label htmlFor="userType" className="block text-sm font-medium text-gray-700 mb-2">
                사용자 유형
              </label>
              <select {...register('userType')} className="input">
                <option value={UserType.GENERAL}>일반 사용자 (취업 준비생)</option>
                <option value={UserType.COMPANY}>기업 사용자</option>
              </select>
              {errors.userType && (
                <p className="mt-1 text-sm text-red-600">{errors.userType.message}</p>
              )}
            </div>

            {/* 조건부 필드 */}
            {userType === UserType.GENERAL && (
              <div>
                <label htmlFor="academy" className="sr-only">
                  학원명
                </label>
                <input
                  {...register('academy')}
                  type="text"
                  className="input"
                  placeholder="학원명 (선택사항)"
                />
                {errors.academy && (
                  <p className="mt-1 text-sm text-red-600">{errors.academy.message}</p>
                )}
              </div>
            )}

            {userType === UserType.COMPANY && (
              <div>
                <label htmlFor="companyName" className="sr-only">
                  회사명
                </label>
                <input
                  {...register('companyName')}
                  type="text"
                  className="input"
                  placeholder="회사명"
                />
                {errors.companyName && (
                  <p className="mt-1 text-sm text-red-600">{errors.companyName.message}</p>
                )}
              </div>
            )}

            {/* 비밀번호 */}
            <div className="relative">
              <label htmlFor="password" className="sr-only">
                비밀번호
              </label>
              <input
                {...register('password')}
                type={showPassword ? 'text' : 'password'}
                autoComplete="new-password"
                className="input pr-10"
                placeholder="비밀번호 (8자 이상)"
              />
              <button
                type="button"
                className="absolute inset-y-0 right-0 pr-3 flex items-center"
                onClick={() => setShowPassword(!showPassword)}
              >
                {showPassword ? (
                  <EyeOff className="h-5 w-5 text-gray-400" />
                ) : (
                  <Eye className="h-5 w-5 text-gray-400" />
                )}
              </button>
              {errors.password && (
                <p className="mt-1 text-sm text-red-600">{errors.password.message}</p>
              )}
            </div>

            {/* 비밀번호 확인 */}
            <div className="relative">
              <label htmlFor="confirmPassword" className="sr-only">
                비밀번호 확인
              </label>
              <input
                {...register('confirmPassword')}
                type={showConfirmPassword ? 'text' : 'password'}
                autoComplete="new-password"
                className="input pr-10"
                placeholder="비밀번호 확인"
              />
              <button
                type="button"
                className="absolute inset-y-0 right-0 pr-3 flex items-center"
                onClick={() => setShowConfirmPassword(!showConfirmPassword)}
              >
                {showConfirmPassword ? (
                  <EyeOff className="h-5 w-5 text-gray-400" />
                ) : (
                  <Eye className="h-5 w-5 text-gray-400" />
                )}
              </button>
              {errors.confirmPassword && (
                <p className="mt-1 text-sm text-red-600">{errors.confirmPassword.message}</p>
              )}
            </div>

            {/* 이용약관 동의 */}
            <div className="flex items-center">
              <input
                {...register('terms')}
                id="terms"
                type="checkbox"
                className="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded"
              />
              <label htmlFor="terms" className="ml-2 block text-sm text-gray-900">
                <Link to="/terms" className="text-primary-600 hover:text-primary-500">
                  이용약관
                </Link>
                에 동의합니다
              </label>
            </div>
            {errors.terms && (
              <p className="mt-1 text-sm text-red-600">{errors.terms.message}</p>
            )}
          </div>

          {errors.root && (
            <div className="bg-red-50 border border-red-200 rounded-md p-3">
              <p className="text-sm text-red-600">{errors.root.message}</p>
            </div>
          )}

          <div>
            <button
              type="submit"
              disabled={isLoading}
              className="w-full btn-primary py-3 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {isLoading ? '가입 중...' : '회원가입'}
            </button>
          </div>

          <div className="mt-6">
            <div className="relative">
              <div className="absolute inset-0 flex items-center">
                <div className="w-full border-t border-gray-300" />
              </div>
              <div className="relative flex justify-center text-sm">
                <span className="px-2 bg-gray-50 text-gray-500">또는</span>
              </div>
            </div>

            <div className="mt-6">
              <button
                type="button"
                onClick={handleGoogleSignUp}
                className="w-full flex justify-center items-center px-4 py-3 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 transition-colors"
              >
                <svg className="w-5 h-5 mr-2" viewBox="0 0 24 24">
                  <path
                    fill="#4285F4"
                    d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"
                  />
                  <path
                    fill="#34A853"
                    d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
                  />
                  <path
                    fill="#FBBC05"
                    d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"
                  />
                  <path
                    fill="#EA4335"
                    d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"
                  />
                </svg>
                Google로 회원가입
              </button>
            </div>
          </div>
        </form>
      </div>
    </div>
  )
}