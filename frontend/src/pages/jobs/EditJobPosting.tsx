import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation } from '@tanstack/react-query'
import { ArrowLeft, Save, X } from 'lucide-react'
import { apiClient } from '@/services/api'
import { useAuthStore } from '@/hooks/useAuthStore'
import { JobType, ExperienceLevel } from '@/types/api'

export default function EditJobPosting() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const { user } = useAuthStore()

  const [formData, setFormData] = useState({
    title: '',
    companyName: '',
    location: '',
    jobType: '' as JobType,
    experienceLevel: '' as ExperienceLevel,
    description: '',
    qualifications: '',
    requiredSkills: '',
    benefits: '',
    minSalary: '',
    maxSalary: '',
    salaryNegotiable: false,
    workingHours: '',
    isRemotePossible: false,
    contactEmail: '',
    contactPhone: '',
    department: '',
    field: ''
  })

  // 기존 채용공고 데이터 조회
  const { data: jobData, isLoading: jobLoading } = useQuery({
    queryKey: ['job', id],
    queryFn: () => apiClient.getJobPosting(Number(id)),
    enabled: !!id,
  })

  const job = jobData?.data

  // 채용공고 데이터 로드 시 폼 초기화
  useEffect(() => {
    if (job) {
      setFormData({
        title: job.title || '',
        companyName: job.companyName || '',
        location: job.location || '',
        jobType: job.jobType || '' as JobType,
        experienceLevel: job.experienceLevel || '' as ExperienceLevel,
        description: job.description || '',
        qualifications: job.qualifications || '',
        requiredSkills: job.requiredSkills || '',
        benefits: job.benefits || '',
        minSalary: job.minSalary?.toString() || '',
        maxSalary: job.maxSalary?.toString() || '',
        salaryNegotiable: job.salaryNegotiable || false,
        workingHours: job.workingHours || '',
        isRemotePossible: job.isRemotePossible || false,
        contactEmail: job.contactEmail || '',
        contactPhone: job.contactPhone || '',
        department: job.department || '',
        field: job.field || ''
      })
    }
  }, [job])

  // 권한 확인
  useEffect(() => {
    if (job && user) {
      const isJobAuthor = user?.userType === 'COMPANY' && user?.id === job?.companyUserId
      const isAdmin = user?.userType === 'ADMIN'

      if (!isJobAuthor && !isAdmin) {
        alert('수정 권한이 없습니다.')
        navigate(`/jobs/${id}`)
      }
    }
  }, [job, user, id, navigate])

  const updateMutation = useMutation({
    mutationFn: (data: any) => apiClient.updateJobPosting(Number(id), data),
    onSuccess: () => {
      navigate(`/jobs/${id}`)
    },
    onError: (error) => {
      console.error('채용공고 수정 실패:', error)
      alert('채용공고 수정에 실패했습니다.')
    }
  })

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    const { name, value, type } = e.target
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? (e.target as HTMLInputElement).checked : value
    }))
  }

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()

    if (!formData.title.trim()) {
      alert('제목을 입력해주세요.')
      return
    }

    if (!formData.description.trim()) {
      alert('직무 설명을 입력해주세요.')
      return
    }

    // API 요청 데이터 준비
    const updateData = {
      title: formData.title.trim(),
      companyName: formData.companyName.trim(),
      location: formData.location.trim(),
      jobType: formData.jobType,
      experienceLevel: formData.experienceLevel,
      description: formData.description.trim(),
      qualifications: formData.qualifications.trim() || undefined,
      requiredSkills: formData.requiredSkills.trim() || undefined,
      benefits: formData.benefits.trim() || undefined,
      minSalary: formData.minSalary ? parseInt(formData.minSalary) : undefined,
      maxSalary: formData.maxSalary ? parseInt(formData.maxSalary) : undefined,
      salaryNegotiable: formData.salaryNegotiable,
      workingHours: formData.workingHours.trim() || undefined,
      isRemotePossible: formData.isRemotePossible,
      contactEmail: formData.contactEmail.trim() || undefined,
      contactPhone: formData.contactPhone.trim() || undefined,
      department: formData.department.trim() || undefined,
      field: formData.field.trim() || undefined
    }

    updateMutation.mutate(updateData)
  }

  if (jobLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
      </div>
    )
  }

  if (!job) {
    return (
      <div className="text-center py-12">
        <div className="text-gray-500 mb-2">채용공고를 찾을 수 없습니다</div>
        <button
          onClick={() => navigate('/jobs')}
          className="text-primary-600 hover:text-primary-500"
        >
          채용공고 목록으로 돌아가기
        </button>
      </div>
    )
  }

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      {/* 헤더 */}
      <div className="flex items-center justify-between">
        <div className="flex items-center space-x-4">
          <button
            onClick={() => navigate(`/jobs/${id}`)}
            className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
          >
            <ArrowLeft className="w-5 h-5" />
          </button>
          <div>
            <h1 className="text-2xl font-bold text-gray-900">채용공고 수정</h1>
            <p className="text-gray-600">채용공고 정보를 수정해보세요</p>
          </div>
        </div>
      </div>

      {/* 수정 폼 */}
      <div className="card">
        <div className="card-content">
          <form onSubmit={handleSubmit} className="space-y-6">
            {/* 기본 정보 */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  제목 *
                </label>
                <input
                  type="text"
                  name="title"
                  value={formData.title}
                  onChange={handleInputChange}
                  className="input"
                  placeholder="채용공고 제목"
                  required
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  회사명 *
                </label>
                <input
                  type="text"
                  name="companyName"
                  value={formData.companyName}
                  onChange={handleInputChange}
                  className="input"
                  placeholder="회사명"
                  required
                />
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  근무지 *
                </label>
                <input
                  type="text"
                  name="location"
                  value={formData.location}
                  onChange={handleInputChange}
                  className="input"
                  placeholder="예: 서울 강남구"
                  required
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  고용 형태 *
                </label>
                <select
                  name="jobType"
                  value={formData.jobType}
                  onChange={handleInputChange}
                  className="input"
                  required
                >
                  <option value="">선택하세요</option>
                  <option value={JobType.FULL_TIME}>정규직</option>
                  <option value={JobType.PART_TIME}>파트타임</option>
                  <option value={JobType.CONTRACT}>계약직</option>
                  <option value={JobType.INTERN}>인턴십</option>
                  <option value={JobType.FREELANCE}>프리랜서</option>
                </select>
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  경력 요건 *
                </label>
                <select
                  name="experienceLevel"
                  value={formData.experienceLevel}
                  onChange={handleInputChange}
                  className="input"
                  required
                >
                  <option value="">선택하세요</option>
                  <option value={ExperienceLevel.ENTRY_LEVEL}>신입</option>
                  <option value={ExperienceLevel.JUNIOR}>주니어 (1-3년)</option>
                  <option value={ExperienceLevel.MID_LEVEL}>미들 (3-7년)</option>
                  <option value={ExperienceLevel.SENIOR}>시니어 (7년+)</option>
                  <option value={ExperienceLevel.EXPERT}>전문가 (10년+)</option>
                  <option value={ExperienceLevel.MANAGER}>관리자</option>
                  <option value={ExperienceLevel.DIRECTOR}>임원</option>
                  <option value={ExperienceLevel.ANY}>경력무관</option>
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  부서
                </label>
                <input
                  type="text"
                  name="department"
                  value={formData.department}
                  onChange={handleInputChange}
                  className="input"
                  placeholder="예: 개발팀"
                />
              </div>
            </div>

            {/* 직무 설명 */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                직무 설명 *
              </label>
              <textarea
                name="description"
                value={formData.description}
                onChange={handleInputChange}
                rows={6}
                className="input resize-none"
                placeholder="직무에 대한 상세한 설명을 입력해주세요"
                required
              />
            </div>

            {/* 자격 요건 및 우대 사항 */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  자격 요건
                </label>
                <textarea
                  name="qualifications"
                  value={formData.qualifications}
                  onChange={handleInputChange}
                  rows={4}
                  className="input resize-none"
                  placeholder="필수 자격 요건을 입력해주세요"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  필요 기술
                </label>
                <textarea
                  name="requiredSkills"
                  value={formData.requiredSkills}
                  onChange={handleInputChange}
                  rows={4}
                  className="input resize-none"
                  placeholder="필요한 기술 스택을 입력해주세요"
                />
              </div>
            </div>

            {/* 급여 정보 */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                급여 정보
              </label>
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div>
                  <input
                    type="number"
                    name="minSalary"
                    value={formData.minSalary}
                    onChange={handleInputChange}
                    className="input"
                    placeholder="최소 급여 (만원)"
                  />
                </div>
                <div>
                  <input
                    type="number"
                    name="maxSalary"
                    value={formData.maxSalary}
                    onChange={handleInputChange}
                    className="input"
                    placeholder="최대 급여 (만원)"
                  />
                </div>
                <div className="flex items-center">
                  <input
                    type="checkbox"
                    name="salaryNegotiable"
                    checked={formData.salaryNegotiable}
                    onChange={handleInputChange}
                    className="mr-2"
                  />
                  <label className="text-sm text-gray-700">협의 가능</label>
                </div>
              </div>
            </div>

            {/* 근무 조건 */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  근무 시간
                </label>
                <input
                  type="text"
                  name="workingHours"
                  value={formData.workingHours}
                  onChange={handleInputChange}
                  className="input"
                  placeholder="예: 09:00 - 18:00"
                />
              </div>
              <div className="flex items-center pt-6">
                <input
                  type="checkbox"
                  name="isRemotePossible"
                  checked={formData.isRemotePossible}
                  onChange={handleInputChange}
                  className="mr-2"
                />
                <label className="text-sm text-gray-700">원격 근무 가능</label>
              </div>
            </div>

            {/* 혜택 및 복리후생 */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                혜택 및 복리후생
              </label>
              <textarea
                name="benefits"
                value={formData.benefits}
                onChange={handleInputChange}
                rows={4}
                className="input resize-none"
                placeholder="제공되는 혜택과 복리후생을 입력해주세요"
              />
            </div>

            {/* 연락처 정보 */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  담당자 이메일
                </label>
                <input
                  type="email"
                  name="contactEmail"
                  value={formData.contactEmail}
                  onChange={handleInputChange}
                  className="input"
                  placeholder="contact@company.com"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  담당자 연락처
                </label>
                <input
                  type="tel"
                  name="contactPhone"
                  value={formData.contactPhone}
                  onChange={handleInputChange}
                  className="input"
                  placeholder="010-1234-5678"
                />
              </div>
            </div>

            {/* 버튼들 */}
            <div className="flex justify-end space-x-3 pt-6 border-t border-gray-200">
              <button
                type="button"
                onClick={() => navigate(`/jobs/${id}`)}
                className="btn-outline"
              >
                <X className="w-4 h-4 mr-2" />
                취소
              </button>
              <button
                type="submit"
                disabled={updateMutation.isPending}
                className="btn-primary disabled:opacity-50"
              >
                <Save className="w-4 h-4 mr-2" />
                {updateMutation.isPending ? '수정 중...' : '수정하기'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  )
}