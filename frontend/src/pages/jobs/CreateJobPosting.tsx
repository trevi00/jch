import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useNavigate } from 'react-router-dom'
import { useQueryClient } from '@tanstack/react-query'
import { apiClient } from '@/services/api'
import { JobType, ExperienceLevel } from '@/types/api'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/Card'
import { Button } from '@/components/ui/Button'
import { Badge } from '@/components/ui/Badge'
import { FileText, MapPin, DollarSign, Calendar, Users, ArrowLeft, Save } from 'lucide-react'

const createJobPostingSchema = z.object({
  title: z.string().min(1, '제목을 입력해주세요'),
  companyName: z.string().min(1, '회사명을 입력해주세요'),
  location: z.string().min(1, '근무지를 입력해주세요'),
  jobType: z.nativeEnum(JobType, { errorMap: () => ({ message: '고용 형태를 선택해주세요' }) }),
  experienceLevel: z.nativeEnum(ExperienceLevel, { errorMap: () => ({ message: '경력을 선택해주세요' }) }),
  department: z.string().optional(),
  description: z.string().min(1, '상세 내용을 입력해주세요'),
  minSalary: z.number().optional(),
  maxSalary: z.number().optional(),
  qualifications: z.string().optional(),
  requiredSkills: z.string().optional(),
})

type CreateJobPostingForm = z.infer<typeof createJobPostingSchema>

export default function CreateJobPosting() {
  const [isLoading, setIsLoading] = useState(false)
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  const {
    register,
    handleSubmit,
    formState: { errors },
    setError,
    watch,
  } = useForm<CreateJobPostingForm>({
    resolver: zodResolver(createJobPostingSchema),
    defaultValues: {
      title: '',
      companyName: '',
      location: '',
      jobType: JobType.FULL_TIME,
      experienceLevel: ExperienceLevel.ANY,
      department: '',
      description: '',
      minSalary: undefined,
      maxSalary: undefined,
      qualifications: '',
      requiredSkills: '',
    }
  })

  const onSubmit = async (data: CreateJobPostingForm) => {
    setIsLoading(true)
    try {
      // Process data to match backend expectations
      const processedData = {
        ...data,
        // Ensure empty strings are sent as empty strings (not undefined)
        qualifications: data.qualifications || '',
        requiredSkills: data.requiredSkills || '',
        department: data.department || '',
      }

      const response = await apiClient.createJobPosting(processedData)
      
      if (response.success) {
        // 생성 후 바로 발행하여 목록에 표시되도록 함
        try {
          // 1년 후를 기본 마감일로 설정
          const defaultDeadline = new Date()
          defaultDeadline.setFullYear(defaultDeadline.getFullYear() + 1)
          const deadlineDate = defaultDeadline.toISOString().split('T')[0]
          
          await apiClient.publishJobPosting(response.data.id, deadlineDate)
        } catch (publishError) {
          console.warn('Job posting published as draft - manual publish needed:', publishError)
        }

        // Invalidate queries to trigger real-time update
        await queryClient.invalidateQueries({ queryKey: ['jobs'] })
        navigate('/jobs')
      } else {
        setError('root', { message: response.message || '채용공고 등록에 실패했습니다' })
      }
    } catch (error: any) {
      console.error('Job posting creation error:', error)
      setError('root', { message: error.response?.data?.message || '채용공고 등록 중 오류가 발생했습니다' })
    } finally {
      setIsLoading(false)
    }
  }

  const jobTypeOptions = [
    { value: JobType.FULL_TIME, label: '정규직' },
    { value: JobType.PART_TIME, label: '계약직' },
    { value: JobType.CONTRACT, label: '파견직' },
    { value: JobType.INTERN, label: '인턴' },
    { value: JobType.FREELANCE, label: '프리랜서' },
  ]

  const experienceLevelOptions = [
    { value: ExperienceLevel.ANY, label: '경력무관' },
    { value: ExperienceLevel.ENTRY_LEVEL, label: '신입' },
    { value: ExperienceLevel.JUNIOR, label: '주니어 (1-3년)' },
    { value: ExperienceLevel.MID_LEVEL, label: '미들 (3-5년)' },
    { value: ExperienceLevel.SENIOR, label: '시니어 (5년 이상)' },
    { value: ExperienceLevel.MANAGER, label: '매니저급' },
    { value: ExperienceLevel.DIRECTOR, label: '임원급' },
  ]

  return (
    <div className="max-w-4xl mx-auto space-y-8 p-6">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Button
            variant="ghost"
            onClick={() => navigate(-1)}
            className="flex items-center gap-2"
          >
            <ArrowLeft className="w-4 h-4" />
            뒤로가기
          </Button>
          <div>
            <h1 className="text-3xl font-bold text-gray-900">채용공고 작성</h1>
            <p className="text-gray-600 mt-2">새로운 채용공고를 등록하세요</p>
          </div>
        </div>
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-8">
        {/* 기본 정보 */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <FileText className="w-5 h-5" />
              기본 정보
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  채용공고 제목 *
                </label>
                <input
                  {...register('title')}
                  className="input w-full"
                  placeholder="백엔드 개발자 채용"
                />
                {errors.title && (
                  <p className="mt-1 text-sm text-red-600">{errors.title.message}</p>
                )}
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  회사명 *
                </label>
                <input
                  {...register('companyName')}
                  className="input w-full"
                  placeholder="테크 스타트업 (주)"
                />
                {errors.companyName && (
                  <p className="mt-1 text-sm text-red-600">{errors.companyName.message}</p>
                )}
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  근무지 *
                </label>
                <input
                  {...register('location')}
                  className="input w-full"
                  placeholder="서울 강남구"
                />
                {errors.location && (
                  <p className="mt-1 text-sm text-red-600">{errors.location.message}</p>
                )}
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  부서
                </label>
                <input
                  {...register('department')}
                  className="input w-full"
                  placeholder="개발팀"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  고용 형태 *
                </label>
                <select {...register('jobType')} className="input w-full">
                  {jobTypeOptions.map(option => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </select>
                {errors.jobType && (
                  <p className="mt-1 text-sm text-red-600">{errors.jobType.message}</p>
                )}
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  경력 *
                </label>
                <select {...register('experienceLevel')} className="input w-full">
                  {experienceLevelOptions.map(option => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </select>
                {errors.experienceLevel && (
                  <p className="mt-1 text-sm text-red-600">{errors.experienceLevel.message}</p>
                )}
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                채용 상세 내용 *
              </label>
              <textarea
                {...register('description')}
                rows={8}
                className="input w-full resize-none"
                placeholder="채용 공고의 상세 내용을 작성해주세요. 담당 업무, 자격 요건, 우대 사항 등을 포함해주세요."
              />
              {errors.description && (
                <p className="mt-1 text-sm text-red-600">{errors.description.message}</p>
              )}
            </div>
          </CardContent>
        </Card>

        {/* 근무 조건 */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <DollarSign className="w-5 h-5" />
              근무 조건
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  최소 연봉 (만원)
                </label>
                <input
                  {...register('minSalary', { valueAsNumber: true })}
                  type="number"
                  className="input w-full"
                  placeholder="3000"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  최대 연봉 (만원)
                </label>
                <input
                  {...register('maxSalary', { valueAsNumber: true })}
                  type="number"
                  className="input w-full"
                  placeholder="5000"
                />
              </div>
            </div>

          </CardContent>
        </Card>

        {/* 자격 요건 */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Users className="w-5 h-5" />
              자격 요건 및 우대 사항
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                자격 요건
              </label>
              <textarea
                {...register('qualifications')}
                rows={3}
                className="input w-full resize-none"
                placeholder="컴퓨터공학 또는 관련 분야 학위, 3년 이상 백엔드 개발 경험 (쉼표로 구분)"
              />
              <p className="text-xs text-gray-500 mt-1">쉼표(,)로 구분하여 입력해주세요</p>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                필수 기술 스택
              </label>
              <textarea
                {...register('requiredSkills')}
                rows={3}
                className="input w-full resize-none"
                placeholder="Java, Spring Boot, MySQL, AWS (쉼표로 구분)"
              />
              <p className="text-xs text-gray-500 mt-1">쉼표(,)로 구분하여 입력해주세요</p>
            </div>
          </CardContent>
        </Card>

        {errors.root && (
          <div className="bg-red-50 border border-red-200 rounded-md p-3">
            <p className="text-sm text-red-600">{errors.root.message}</p>
          </div>
        )}

        <div className="flex justify-end gap-4">
          <Button
            type="button"
            variant="secondary"
            onClick={() => navigate(-1)}
          >
            취소
          </Button>
          <Button
            type="submit"
            disabled={isLoading}
            className="flex items-center gap-2"
          >
            <Save className="w-4 h-4" />
            {isLoading ? '등록 중...' : '채용공고 등록'}
          </Button>
        </div>
      </form>
    </div>
  )
}