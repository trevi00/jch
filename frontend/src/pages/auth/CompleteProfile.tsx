import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useAuthStore } from '@/hooks/useAuthStore'
import { apiClient } from '@/services/api'

const completeProfileSchema = z.object({
  phoneNumber: z.string().optional(),
  age: z.number().min(1, '나이를 입력해주세요').max(100, '올바른 나이를 입력해주세요').optional(),
  gender: z.enum(['MALE', 'FEMALE', 'OTHER'], {
    errorMap: () => ({ message: '성별을 선택해주세요' })
  }).optional(),
  residenceRegion: z.string().min(1, '거주지역을 입력해주세요').optional(),
  desiredJob: z.string().min(1, '희망 직업을 입력해주세요').optional(),
  desiredCompany: z.string().optional(),
  employmentStatus: z.enum(['JOB_SEEKING', 'EMPLOYED', 'STUDENT', 'PREPARING'], {
    errorMap: () => ({ message: '현재 상태를 선택해주세요' })
  })
})

type CompleteProfileForm = z.infer<typeof completeProfileSchema>

export default function CompleteProfile() {
  const [isLoading, setIsLoading] = useState(false)
  const navigate = useNavigate()
  const { user, updateUser } = useAuthStore()

  const {
    register,
    handleSubmit,
    formState: { errors },
    setError,
  } = useForm<CompleteProfileForm>({
    resolver: zodResolver(completeProfileSchema),
    defaultValues: {
      phoneNumber: user?.phoneNumber || '',
      age: user?.age || undefined,
      gender: user?.gender || undefined,
      residenceRegion: user?.residenceRegion || '',
      desiredJob: user?.desiredJob || '',
      desiredCompany: user?.desiredCompany || '',
      employmentStatus: user?.employmentStatus || 'JOB_SEEKING',
    }
  })

  const onSubmit = async (data: CompleteProfileForm) => {
    setIsLoading(true)
    try {
      // Convert age to number if it's provided
      const profileData = {
        ...data,
        age: data.age ? Number(data.age) : undefined
      }

      const response = await apiClient.updateProfile(profileData)
      
      if (response.success && response.data) {
        // Update auth store with new user data
        updateUser(response.data)
        navigate('/dashboard')
      } else {
        setError('root', { message: response.message || '프로필 업데이트에 실패했습니다' })
      }
    } catch (error: any) {
      setError('root', { message: '프로필 업데이트 중 오류가 발생했습니다' })
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-2xl w-full space-y-8">
        <div>
          <h2 className="mt-6 text-center text-3xl font-bold text-gray-900">
            프로필 완성하기
          </h2>
          <p className="mt-2 text-center text-sm text-gray-600">
            서비스 이용을 위해 추가 정보를 입력해주세요
          </p>
        </div>

        <form className="mt-8 space-y-6" onSubmit={handleSubmit(onSubmit)}>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {/* 전화번호 */}
            <div>
              <label htmlFor="phoneNumber" className="block text-sm font-medium text-gray-700">
                전화번호 (선택사항)
              </label>
              <input
                {...register('phoneNumber')}
                type="tel"
                className="input mt-1"
                placeholder="010-1234-5678"
              />
              {errors.phoneNumber && (
                <p className="mt-1 text-sm text-red-600">{errors.phoneNumber.message}</p>
              )}
            </div>

            {/* 나이 */}
            <div>
              <label htmlFor="age" className="block text-sm font-medium text-gray-700">
                나이 (선택사항)
              </label>
              <input
                {...register('age', { valueAsNumber: true })}
                type="number"
                min="1"
                max="100"
                className="input mt-1"
                placeholder="25"
              />
              {errors.age && (
                <p className="mt-1 text-sm text-red-600">{errors.age.message}</p>
              )}
            </div>

            {/* 성별 */}
            <div>
              <label htmlFor="gender" className="block text-sm font-medium text-gray-700">
                성별 (선택사항)
              </label>
              <select {...register('gender')} className="input mt-1">
                <option value="">선택해주세요</option>
                <option value="MALE">남성</option>
                <option value="FEMALE">여성</option>
                <option value="OTHER">기타</option>
              </select>
              {errors.gender && (
                <p className="mt-1 text-sm text-red-600">{errors.gender.message}</p>
              )}
            </div>

            {/* 현재 상태 */}
            <div>
              <label htmlFor="employmentStatus" className="block text-sm font-medium text-gray-700">
                현재 상태 *
              </label>
              <select {...register('employmentStatus')} className="input mt-1">
                <option value="JOB_SEEKING">구직 중</option>
                <option value="EMPLOYED">재직 중</option>
                <option value="STUDENT">학생</option>
                <option value="PREPARING">취업 준비</option>
              </select>
              {errors.employmentStatus && (
                <p className="mt-1 text-sm text-red-600">{errors.employmentStatus.message}</p>
              )}
            </div>
          </div>

          {/* 거주지역 */}
          <div>
            <label htmlFor="residenceRegion" className="block text-sm font-medium text-gray-700">
              거주지역 (선택사항)
            </label>
            <input
              {...register('residenceRegion')}
              type="text"
              className="input mt-1"
              placeholder="서울특별시 강남구"
            />
            {errors.residenceRegion && (
              <p className="mt-1 text-sm text-red-600">{errors.residenceRegion.message}</p>
            )}
          </div>

          {/* 희망 직업 */}
          <div>
            <label htmlFor="desiredJob" className="block text-sm font-medium text-gray-700">
              희망 직업 (선택사항)
            </label>
            <input
              {...register('desiredJob')}
              type="text"
              className="input mt-1"
              placeholder="백엔드 개발자"
            />
            {errors.desiredJob && (
              <p className="mt-1 text-sm text-red-600">{errors.desiredJob.message}</p>
            )}
          </div>

          {/* 희망 회사 */}
          <div>
            <label htmlFor="desiredCompany" className="block text-sm font-medium text-gray-700">
              희망 회사 (선택사항)
            </label>
            <input
              {...register('desiredCompany')}
              type="text"
              className="input mt-1"
              placeholder="네이버, 카카오 등"
            />
            {errors.desiredCompany && (
              <p className="mt-1 text-sm text-red-600">{errors.desiredCompany.message}</p>
            )}
          </div>

          {errors.root && (
            <div className="bg-red-50 border border-red-200 rounded-md p-3">
              <p className="text-sm text-red-600">{errors.root.message}</p>
            </div>
          )}

          <div className="flex space-x-4">
            <button
              type="button"
              onClick={() => navigate('/dashboard')}
              className="flex-1 py-3 px-4 border border-gray-300 rounded-md text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 transition-colors"
            >
              나중에 하기
            </button>
            <button
              type="submit"
              disabled={isLoading}
              className="flex-1 btn-primary py-3 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {isLoading ? '저장 중...' : '프로필 완성'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}