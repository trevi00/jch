import React, { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useParams, Link, useNavigate } from 'react-router-dom'
import {
  MapPin,
  Clock,
  DollarSign,
  Users,
  Calendar,
  Heart,
  Share2,
  ArrowLeft,
  X,
  FileText,
  Upload,
  Eye,
  Save,
  Lightbulb,
  ChevronRight,
  ChevronLeft,
  Check,
  Edit,
  Trash2,
  UserCheck,
  Mail,
  Phone,
  Building
} from 'lucide-react'
import { apiClient } from '@/services/api'
import { JobType, ExperienceLevel, ApplicationStatus } from '@/types/api'
import { useAuthStore } from '@/hooks/useAuthStore'
import JobPostingStats from '@/components/jobs/JobPostingStats'

export default function JobDetail() {
  const { id } = useParams<{ id: string }>()
  const { user } = useAuthStore()
  const queryClient = useQueryClient()
  const navigate = useNavigate()
  const [showApplicationModal, setShowApplicationModal] = useState(false)
  const [selectedApplicant, setSelectedApplicant] = useState<any>(null)
  const [showApplicantModal, setShowApplicantModal] = useState(false)

  const { data: jobData, isLoading, isError, error } = useQuery({
    queryKey: ['job', id],
    queryFn: () => apiClient.getJobPosting(Number(id)),
    enabled: !!id,
  })

  const { data: applicationData } = useQuery({
    queryKey: ['job-application', id, user?.id],
    queryFn: () => apiClient.getMyApplications(),
    enabled: !!id && !!user,
  })

  // 기업 유저가 자신의 공고에 대한 지원자 목록을 가져오는 쿼리
  const { data: applicantsData, isLoading: applicantsLoading, refetch: refetchApplicants } = useQuery({
    queryKey: ['job-applicants', id],
    queryFn: () => apiClient.getJobApplications(Number(id)),
    enabled: false, // 일단 비활성화하고 나중에 refetch로 호출
  })

  const applyMutation = useMutation({
    mutationFn: (data: { coverLetter: string; resume?: File }) =>
      apiClient.applyToJob({
        jobPostingId: Number(id)!,
        coverLetter: data.coverLetter,
      }),
    onSuccess: () => {
      console.log('✅ Application submitted successfully - invalidating queries')
      queryClient.invalidateQueries({ queryKey: ['job-application', id] })
      queryClient.invalidateQueries({ queryKey: ['job-applicants', id] }) // 지원자 목록도 새로고침
      setShowApplicationModal(false)
    },
  })

  const deleteMutation = useMutation({
    mutationFn: () => apiClient.deleteJobPosting(Number(id)),
    onSuccess: async () => {
      // Invalidate queries to trigger real-time update
      await queryClient.invalidateQueries({ queryKey: ['jobs'] })
      navigate('/jobs')
    },
  })

  const job = jobData?.data

  // 채용공고 작성자인지 확인
  const isJobAuthor = user?.userType === 'COMPANY' && user?.id === job?.companyUserId
  const isAdmin = user?.userType === 'ADMIN'
  const canEditJob = isJobAuthor || isAdmin

  // 작성자인 경우에만 지원자 데이터 fetch
  React.useEffect(() => {
    console.log('🔍 JobDetail - isJobAuthor:', isJobAuthor)
    if (isJobAuthor) {
      console.log('🔄 Fetching applicants for job:', id)
      refetchApplicants()
    }
  }, [isJobAuthor, id]) // refetchApplicants, user, job 제거하여 의존성 간소화

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
      </div>
    )
  }

  if (isError) {
    return (
      <div className="text-center py-12">
        <div className="text-red-500 mb-4">
          <h2 className="text-xl font-semibold mb-2">채용공고를 불러올 수 없습니다</h2>
          <p className="text-gray-600 mb-2">
            {error?.message || '네트워크 오류가 발생했습니다'}
          </p>
          <p className="text-sm text-gray-500">
            요청한 채용공고 ID: {id}
          </p>
        </div>
        <div className="space-x-4">
          <button
            onClick={() => window.location.reload()}
            className="px-4 py-2 bg-primary-600 text-white rounded hover:bg-primary-700"
          >
            다시 시도
          </button>
          <Link
            to="/jobs"
            className="px-4 py-2 bg-gray-200 text-gray-700 rounded hover:bg-gray-300"
          >
            채용공고 목록으로 돌아가기
          </Link>
        </div>
      </div>
    )
  }

  if (!job) {
    return (
      <div className="text-center py-12">
        <div className="text-gray-500 mb-2">채용공고를 찾을 수 없습니다</div>
        <Link to="/jobs" className="text-primary-600 hover:text-primary-500">
          채용공고 목록으로 돌아가기
        </Link>
      </div>
    )
  }

  const formatSalary = (min?: number, max?: number) => {
    if (!min && !max) return '연봉 정보 없음'
    if (min && max) return `${min.toLocaleString()}만원 - ${max.toLocaleString()}만원`
    if (min) return `${min.toLocaleString()}만원 이상`
    if (max) return `${max.toLocaleString()}만원 이하`
    return '연봉 정보 없음'
  }

  const getJobTypeLabel = (type: JobType) => {
    const labels = {
      [JobType.FULL_TIME]: '정규직',
      [JobType.PART_TIME]: '파트타임',
      [JobType.CONTRACT]: '계약직',
      [JobType.INTERN]: '인턴십',
      [JobType.FREELANCE]: '프리랜서'
    }
    return labels[type] || type
  }

  const getExperienceLevelLabel = (level: ExperienceLevel) => {
    const labels = {
      [ExperienceLevel.ENTRY_LEVEL]: '신입',
      [ExperienceLevel.JUNIOR]: '주니어',
      [ExperienceLevel.MID_LEVEL]: '미들',
      [ExperienceLevel.SENIOR]: '시니어',
      [ExperienceLevel.EXPERT]: '경력 10년+',
      [ExperienceLevel.MANAGER]: '관리자',
      [ExperienceLevel.DIRECTOR]: '임원',
      [ExperienceLevel.ANY]: '경력무관'
    }
    return labels[level] || level
  }

const userApplication = applicationData?.data?.content?.find(
  app => app.jobPostingId === Number(id)
)

  const getApplicationStatusLabel = (status: ApplicationStatus) => {
    const labels: Record<ApplicationStatus, string> = {
      [ApplicationStatus.SUBMITTED]: '지원 완료',
      [ApplicationStatus.REVIEWED]: '검토 중',
      [ApplicationStatus.DOCUMENT_PASSED]: '서류 합격',
      [ApplicationStatus.INTERVIEW_SCHEDULED]: '면접 예정',
      [ApplicationStatus.INTERVIEW_PASSED]: '면접 합격',
      [ApplicationStatus.HIRED]: '최종 합격',
      [ApplicationStatus.REJECTED]: '불합격',
      [ApplicationStatus.WITHDRAWN]: '지원 철회'
    }
    return labels[status] || status
  }

  const handleApply = (data: { coverLetter: string; resume?: File }) => {
    applyMutation.mutate(data)
  }

  const formatAppliedDate = (appliedAt: any): string => {
    try {
      // 배열 형태인 경우 (백엔드 LocalDateTime 직렬화): [year, month, day, hour, minute, second, nano]
      if (Array.isArray(appliedAt)) {
        const [year, month, day] = appliedAt
        return new Date(year, month - 1, day).toLocaleDateString('ko-KR')
      }

      // 문자열 형태인 경우
      if (typeof appliedAt === 'string') {
        return new Date(appliedAt).toLocaleDateString('ko-KR')
      }

      // 숫자 형태인 경우 (timestamp)
      if (typeof appliedAt === 'number') {
        return new Date(appliedAt).toLocaleDateString('ko-KR')
      }

      return '정보 없음'
    } catch (error) {
      console.warn('Date parsing error:', error, 'appliedAt:', appliedAt)
      return '정보 없음'
    }
  }

  const handleDeleteJob = () => {
    if (window.confirm('정말로 이 채용공고를 삭제하시겠습니까?')) {
      deleteMutation.mutate()
    }
  }

  return (
    <div className="space-y-6">
      {/* 헤더 */}
      <div className="flex items-center space-x-4">
        <Link
          to="/jobs"
          className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
        >
          <ArrowLeft className="w-5 h-5" />
        </Link>
        <div className="flex-1">
          <h1 className="text-2xl font-bold text-gray-900">{job.title}</h1>
          <p className="text-lg text-gray-700">{job.companyName}</p>
        </div>
        <div className="flex space-x-2">
          {canEditJob && (
            <>
              <Link
                to={`/jobs/edit/${job.id}`}
                className="p-2 text-gray-600 hover:text-gray-800 hover:bg-gray-100 rounded-lg transition-colors"
              >
                <Edit className="w-5 h-5" />
              </Link>
              <button
                onClick={handleDeleteJob}
                className="p-2 text-red-600 hover:text-red-800 hover:bg-red-50 rounded-lg transition-colors"
              >
                <Trash2 className="w-5 h-5" />
              </button>
            </>
          )}
          <button className="p-2 border border-gray-300 rounded-lg hover:bg-gray-50">
            <Heart className="w-5 h-5" />
          </button>
          <button className="p-2 border border-gray-300 rounded-lg hover:bg-gray-50">
            <Share2 className="w-5 h-5" />
          </button>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* 메인 콘텐츠 */}
        <div className="lg:col-span-2 space-y-6">
          {/* 기본 정보 */}
          <div className="bg-white border border-gray-200 rounded-lg shadow-sm">
            <div className="p-6">
              <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
                <div className="flex items-center space-x-2">
                  <MapPin className="w-5 h-5 text-gray-400" />
                  <span className="text-sm text-gray-600">{job.location}</span>
                </div>
                <div className="flex items-center space-x-2">
                  <Clock className="w-5 h-5 text-gray-400" />
                  <span className="text-sm text-gray-600">{getJobTypeLabel(job.jobType)}</span>
                </div>
                <div className="flex items-center space-x-2">
                  <DollarSign className="w-5 h-5 text-gray-400" />
                  <span className="text-sm text-gray-600">{formatSalary(job.minSalary, job.maxSalary)}</span>
                </div>
                <div className="flex items-center space-x-2">
                  <Users className="w-5 h-5 text-gray-400" />
                  <span className="text-sm text-gray-600">{getExperienceLevelLabel(job.experienceLevel)}</span>
                </div>
              </div>

              {job.deadlineDate && (
                <div className="flex items-center space-x-2 p-3 bg-orange-50 rounded-lg">
                  <Calendar className="w-5 h-5 text-orange-600" />
                  <span className="text-sm text-orange-800">
                    지원 마감: {Array.isArray(job.deadlineDate) ? new Date(job.deadlineDate[0], job.deadlineDate[1] - 1, job.deadlineDate[2]).toLocaleDateString() : new Date(job.deadlineDate).toLocaleDateString()}
                  </span>
                </div>
              )}
            </div>
          </div>

          {/* 직무 소개 */}
          <div className="bg-white border border-gray-200 rounded-lg shadow-sm">
            <div className="px-6 py-4 border-b border-gray-200">
              <h2 className="text-lg font-semibold">직무 소개</h2>
            </div>
            <div className="p-6">
              <div className="whitespace-pre-line text-gray-700">
                {job.description}
              </div>
            </div>
          </div>

          {/* 자격 요건 */}
          {job.qualifications && (
            <div className="bg-white border border-gray-200 rounded-lg shadow-sm">
              <div className="px-6 py-4 border-b border-gray-200">
                <h2 className="text-lg font-semibold">자격 요건</h2>
              </div>
              <div className="p-6">
                <div className="whitespace-pre-line text-gray-700">
                  {job.qualifications}
                </div>
              </div>
            </div>
          )}

          {/* 우대 사항 */}
          {job.preferredQualifications && (
            <div className="bg-white border border-gray-200 rounded-lg shadow-sm">
              <div className="px-6 py-4 border-b border-gray-200">
                <h2 className="text-lg font-semibold">우대 사항</h2>
              </div>
              <div className="p-6">
                <div className="whitespace-pre-line text-gray-700">
                  {job.preferredQualifications}
                </div>
              </div>
            </div>
          )}

          {/* 혜택 */}
          {job.benefits && (
            <div className="bg-white border border-gray-200 rounded-lg shadow-sm">
              <div className="px-6 py-4 border-b border-gray-200">
                <h2 className="text-lg font-semibold">혜택 및 복리후생</h2>
              </div>
              <div className="p-6">
                <div className="whitespace-pre-line text-gray-700">
                  {job.benefits}
                </div>
              </div>
            </div>
          )}
        </div>

        {/* 사이드바 */}
        <div className="space-y-6">
          {/* 지원 버튼 - 기업 유저는 지원할 수 없음 */}
          {user?.userType !== 'COMPANY' && (
            <div className="bg-white border border-gray-200 rounded-lg shadow-sm">
              <div className="p-6">
                {userApplication ? (
                  <div className="text-center">
                    <div className="mb-4">
                      <span className="inline-block px-3 py-1 bg-blue-100 text-blue-800 rounded-full">
                        {getApplicationStatusLabel(userApplication.status)}
                      </span>
                    </div>
                    <p className="text-sm text-gray-600 mb-2">
                      지원일: {new Date(userApplication.appliedAt).toLocaleDateString()}
                    </p>
                    <Link
                      to="/applications"
                      className="w-full inline-flex items-center justify-center px-4 py-2 border border-gray-300 text-gray-700 bg-white rounded-lg hover:bg-gray-50 transition-colors"
                    >
                      지원 현황 보기
                    </Link>
                  </div>
                ) : (
                  <button
                    onClick={() => setShowApplicationModal(true)}
                    className="w-full px-4 py-3 bg-blue-600 hover:bg-blue-700 disabled:bg-gray-400 text-white font-medium rounded-lg transition-colors disabled:cursor-not-allowed"
                    disabled={job.deadlineDate ? (Array.isArray(job.deadlineDate) ? new Date(job.deadlineDate[0], job.deadlineDate[1] - 1, job.deadlineDate[2]) < new Date() : new Date(job.deadlineDate) < new Date()) : false}
                  >
                    {job.deadlineDate && (Array.isArray(job.deadlineDate) ? new Date(job.deadlineDate[0], job.deadlineDate[1] - 1, job.deadlineDate[2]) < new Date() : new Date(job.deadlineDate) < new Date()) ? '마감된 공고' : '지원하기'}
                  </button>
                )}
              </div>
            </div>
          )}

          {/* 필요 기술 */}
          {job.requiredSkills && job.requiredSkills.length > 0 && (
            <div className="bg-white border border-gray-200 rounded-lg shadow-sm">
              <div className="px-6 py-4 border-b border-gray-200">
                <h3 className="text-lg font-semibold">필요 기술</h3>
              </div>
              <div className="p-6">
                <div className="flex flex-wrap gap-2">
                  {job.requiredSkills.map((skill, index) => (
                    <span
                      key={index}
                      className="px-3 py-1 bg-blue-100 text-blue-800 text-sm rounded-full"
                    >
                      {skill}
                    </span>
                  ))}
                </div>
              </div>
            </div>
          )}

          {/* 회사 정보 */}
          <div className="bg-white border border-gray-200 rounded-lg shadow-sm">
            <div className="px-6 py-4 border-b border-gray-200">
              <h3 className="text-lg font-semibold">회사 정보</h3>
            </div>
            <div className="p-6 space-y-3">
              <div>
                <dt className="text-sm font-medium text-gray-500">회사명</dt>
                <dd className="text-gray-900">{job.companyName}</dd>
              </div>
              <div>
                <dt className="text-sm font-medium text-gray-500">위치</dt>
                <dd className="text-gray-900">{job.location}</dd>
              </div>
              <div>
                <dt className="text-sm font-medium text-gray-500">등록일</dt>
                <dd className="text-gray-900">{Array.isArray(job.createdAt) ? new Date(job.createdAt[0], job.createdAt[1] - 1, job.createdAt[2]).toLocaleDateString() : new Date(job.createdAt).toLocaleDateString()}</dd>
              </div>
            </div>
          </div>

          {/* 채용공고 통계 - 작성자나 관리자만 볼 수 있음 */}
          {(isJobAuthor || isAdmin) && (
            <JobPostingStats jobId={job.id} />
          )}

          {/* 지원자 목록 - 작성자만 볼 수 있음 */}
          {isJobAuthor && (
            <div className="bg-white border border-gray-200 rounded-lg shadow-sm">
              <div className="px-6 py-4 border-b border-gray-200">
                <div className="flex items-center justify-between">
                  <h3 className="text-lg font-semibold flex items-center gap-2">
                    <UserCheck className="w-5 h-5 text-blue-600" />
                    지원자 목록
                  </h3>
                  <div className="text-sm text-gray-600">
                    총 {applicantsData?.data?.content?.length || applicantsData?.data?.length || 0}명 지원
                  </div>
                </div>
              </div>
              <div className="p-6">
                {applicantsLoading ? (
                  <div className="flex items-center justify-center py-8">
                    <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
                  </div>
                ) : (applicantsData?.data?.content?.length || applicantsData?.data?.length) > 0 ? (
                  <div className="space-y-4">
                    {(applicantsData?.data?.content || applicantsData?.data || []).map((applicant: any) => (
                      <div key={applicant.id} className="border rounded-lg p-4 hover:shadow-md transition-shadow">
                        <div className="space-y-3">
                          {/* 상단: 프로필과 지원서 보기 버튼 */}
                          <div className="flex items-start justify-between">
                            <div className="flex items-center gap-3">
                              <div className="w-10 h-10 bg-blue-100 rounded-full flex items-center justify-center">
                                <UserCheck className="w-5 h-5 text-blue-600" />
                              </div>
                              <div>
                                <h4 className="font-medium text-gray-900">
                                  {applicant.userName || '이름 없음'}
                                </h4>
                                <p className="text-sm text-gray-600">
                                  {applicant.userEmail || '이메일 없음'}
                                </p>
                              </div>
                            </div>
                            <button
                              onClick={() => {
                                setSelectedApplicant(applicant)
                                setShowApplicantModal(true)
                              }}
                              className="px-3 py-1.5 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors flex items-center gap-1.5 text-sm"
                            >
                              <Eye className="w-3.5 h-3.5" />
                              지원서 보기
                            </button>
                          </div>

                          {/* 하단: 지원일과 상태 */}
                          <div className="flex items-center gap-6 pl-13">
                            <div className="flex items-center gap-2">
                              <div className="w-6 h-6 bg-gray-100 rounded-full flex items-center justify-center">
                                <Calendar className="w-3.5 h-3.5 text-gray-500" />
                              </div>
                              <span className="text-sm text-gray-500">지원날짜:</span>
                              <span className="text-sm text-gray-900 font-medium">
                                {applicant.appliedAt ? formatAppliedDate(applicant.appliedAt) : '정보 없음'}
                              </span>
                            </div>
                            <div className="flex items-center gap-2">
                              <span className="text-sm text-gray-500">상태:</span>
                              <div className={`px-2 py-1 rounded-full text-xs font-medium ${
                                applicant.status === 'SUBMITTED' ? 'bg-blue-100 text-blue-700' :
                                applicant.status === 'REVIEWED' ? 'bg-yellow-100 text-yellow-700' :
                                applicant.status === 'DOCUMENT_PASSED' ? 'bg-green-100 text-green-700' :
                                applicant.status === 'REJECTED' ? 'bg-red-100 text-red-700' :
                                'bg-gray-100 text-gray-700'
                              }`}>
                                {applicant.status ? getApplicationStatusLabel(applicant.status) : '정보 없음'}
                              </div>
                            </div>
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                ) : (
                  <div className="text-center py-8 text-gray-500">
                    <UserCheck className="w-12 h-12 mx-auto mb-3 text-gray-300" />
                    <p>아직 지원자가 없습니다.</p>
                  </div>
                )}
              </div>
            </div>
          )}
        </div>
      </div>

      {/* 지원 모달 */}
      {showApplicationModal && (
        <ApplicationModal
          jobTitle={job.title}
          onClose={() => setShowApplicationModal(false)}
          onSubmit={handleApply}
          isLoading={applyMutation.isPending}
        />
      )}

      {/* 지원자 상세보기 모달 */}
      {showApplicantModal && selectedApplicant && (
        <ApplicantDetailModal
          applicant={selectedApplicant}
          jobTitle={job.title}
          onClose={() => {
            setShowApplicantModal(false)
            setSelectedApplicant(null)
          }}
        />
      )}
    </div>
  )
}

interface ApplicationModalProps {
  jobTitle: string
  onClose: () => void
  onSubmit: (data: { coverLetter: string; resume?: File }) => void
  isLoading: boolean
}

function ApplicationModal({ jobTitle, onClose, onSubmit, isLoading }: ApplicationModalProps) {
  const [currentStep, setCurrentStep] = useState(1)
  const [coverLetter, setCoverLetter] = useState('')
  const [resume, setResume] = useState<File | null>(null)
  const [showPreview, setShowPreview] = useState(false)
  const [dragActive, setDragActive] = useState(false)
  const [useTemplate, setUseTemplate] = useState(false)

  // 임시 저장 (localStorage)
  React.useEffect(() => {
    const savedData = localStorage.getItem(`application-${jobTitle}`)
    if (savedData) {
      const { coverLetter: saved } = JSON.parse(savedData)
      setCoverLetter(saved || '')
    }
  }, [jobTitle])

  // 자동 저장
  React.useEffect(() => {
    if (coverLetter.trim()) {
      localStorage.setItem(`application-${jobTitle}`, JSON.stringify({ coverLetter }))
    }
  }, [coverLetter, jobTitle])

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    console.log('handleSubmit called', { coverLetter, resume })
    console.log('coverLetter length:', coverLetter.length)
    console.log('coverLetter bytes:', new Blob([coverLetter]).size)
    localStorage.removeItem(`application-${jobTitle}`)
    onSubmit({ coverLetter, resume: resume || undefined })
  }

  const handleDrag = (e: React.DragEvent) => {
    e.preventDefault()
    e.stopPropagation()
    if (e.type === "dragenter" || e.type === "dragover") {
      setDragActive(true)
    } else if (e.type === "dragleave") {
      setDragActive(false)
    }
  }

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault()
    e.stopPropagation()
    setDragActive(false)
    
    const files = Array.from(e.dataTransfer.files)
    const file = files[0]
    if (file && (file.type === 'application/pdf' || file.name.endsWith('.doc') || file.name.endsWith('.docx'))) {
      setResume(file)
    }
  }

  const coverLetterTemplate = `안녕하세요. ${jobTitle} 포지션에 지원하게 된 [귀하의 이름]입니다.

【지원 동기】
이 회사와 포지션에 관심을 갖게 된 이유를 구체적으로 작성해주세요.

【보유 역량】
관련 기술 스택이나 경험을 구체적인 사례와 함께 설명해주세요.

【성장 계획】
이 포지션을 통해 어떤 성장을 하고 싶은지 작성해주세요.

감사합니다.`

  const steps = [
    { id: 1, title: '자기소개서 작성', icon: FileText },
    { id: 2, title: '이력서 첨부', icon: Upload },
    { id: 3, title: '지원서 검토', icon: Eye }
  ]

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-xl max-w-4xl w-full mx-4 max-h-[90vh] overflow-hidden shadow-2xl">
        
        {/* Header */}
        <div className="px-6 py-4 border-b border-gray-200">
          <div className="flex justify-between items-center">
            <div>
              <h2 className="text-2xl font-bold text-gray-900 flex items-center">
                <FileText className="w-6 h-6 mr-2 text-primary-600" />
                지원서 작성
              </h2>
              <p className="text-gray-600 mt-1">{jobTitle}</p>
            </div>
            <button
              onClick={onClose}
              className="text-gray-400 hover:text-gray-600 transition-colors p-2 hover:bg-gray-100 rounded-lg"
            >
              <X className="w-6 h-6" />
            </button>
          </div>
          
          {/* Progress Steps */}
          <div className="flex items-center mt-6 space-x-4">
            {steps.map((step, index) => (
              <div key={step.id} className="flex items-center">
                <div className={`flex items-center justify-center w-10 h-10 rounded-full transition-all ${
                  step.id < currentStep 
                    ? 'bg-green-100 text-green-600' 
                    : step.id === currentStep 
                    ? 'bg-primary-600 text-white' 
                    : 'bg-gray-100 text-gray-400'
                }`}>
                  {step.id < currentStep ? <Check className="w-5 h-5" /> : <step.icon className="w-5 h-5" />}
                </div>
                <span className={`ml-2 text-sm font-medium ${
                  step.id === currentStep ? 'text-primary-600' : 'text-gray-500'
                }`}>
                  {step.title}
                </span>
                {index < steps.length - 1 && (
                  <ChevronRight className="w-4 h-4 mx-2 text-gray-300" />
                )}
              </div>
            ))}
          </div>
        </div>

        {/* Content */}
        <div className="flex-1 overflow-y-auto" style={{ maxHeight: 'calc(90vh - 140px)' }}>
          <form onSubmit={handleSubmit}>
            
            {/* Step 1: Cover Letter */}
            {currentStep === 1 && (
              <div className="p-6 space-y-6">
                <div className="flex justify-between items-start">
                  <div>
                    <h3 className="text-lg font-semibold text-gray-900 mb-2">자기소개서를 작성해 주세요</h3>
                    <p className="text-gray-600">본인의 경험과 역량을 구체적으로 어필해 보세요.</p>
                  </div>
                  <div className="flex items-center space-x-2">
                    <button
                      type="button"
                      onClick={() => setUseTemplate(!useTemplate)}
                      className={`px-3 py-1 text-sm rounded-lg flex items-center transition-colors ${
                        useTemplate 
                          ? 'bg-primary-100 text-primary-700' 
                          : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                      }`}
                    >
                      <Lightbulb className="w-4 h-4 mr-1" />
                      템플릿 {useTemplate ? '해제' : '사용'}
                    </button>
                    <button
                      type="button"
                      onClick={() => {
                        localStorage.setItem(`application-${jobTitle}`, JSON.stringify({ coverLetter }))
                        alert('임시 저장되었습니다!')
                      }}
                      className="px-3 py-1 text-sm bg-gray-100 text-gray-600 rounded-lg hover:bg-gray-200 flex items-center transition-colors"
                    >
                      <Save className="w-4 h-4 mr-1" />
                      저장
                    </button>
                  </div>
                </div>

                <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4">
                  <div className="flex">
                    <Lightbulb className="w-5 h-5 text-yellow-600 mt-0.5 mr-2" />
                    <div>
                      <h4 className="text-sm font-medium text-yellow-800">작성 팁</h4>
                      <p className="text-sm text-yellow-700 mt-1">
                        구체적인 경험과 성과 위주로 작성하시고, 해당 포지션과 연관된 내용을 포함해 주세요.
                      </p>
                    </div>
                  </div>
                </div>

                <div className="space-y-2">
                  <div className="flex items-center justify-between">
                    <label className="block text-sm font-medium text-gray-700">
                      자기소개서 *
                    </label>
                    <span className="text-sm text-gray-500">
                      {coverLetter.length}/2000자
                    </span>
                  </div>
                  
                  <textarea
                    value={useTemplate && !coverLetter ? coverLetterTemplate : coverLetter}
                    onChange={(e) => setCoverLetter(e.target.value)}
                    rows={15}
                    maxLength={2000}
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 resize-none"
                    placeholder={useTemplate ? '' : "지원 동기, 보유 역량, 성장 계획 등을 구체적으로 작성해 주세요..."}
                    required
                  />
                </div>
              </div>
            )}

            {/* Step 2: Resume Upload */}
            {currentStep === 2 && (
              <div className="p-6 space-y-6">
                <div>
                  <h3 className="text-lg font-semibold text-gray-900 mb-2">이력서를 첨부해 주세요</h3>
                  <p className="text-gray-600">PDF, DOC, DOCX 파일을 업로드할 수 있습니다. (선택사항)</p>
                </div>

                <div
                  className={`border-2 border-dashed rounded-lg p-8 text-center transition-all ${
                    dragActive 
                      ? 'border-primary-400 bg-primary-50' 
                      : resume 
                      ? 'border-green-400 bg-green-50' 
                      : 'border-gray-300 hover:border-gray-400'
                  }`}
                  onDragEnter={handleDrag}
                  onDragLeave={handleDrag}
                  onDragOver={handleDrag}
                  onDrop={handleDrop}
                >
                  {resume ? (
                    <div className="space-y-4">
                      <div className="w-16 h-16 bg-green-100 rounded-lg flex items-center justify-center mx-auto">
                        <FileText className="w-8 h-8 text-green-600" />
                      </div>
                      <div>
                        <p className="text-lg font-medium text-gray-900">{resume.name}</p>
                        <p className="text-sm text-gray-500">{(resume.size / 1024 / 1024).toFixed(2)}MB</p>
                      </div>
                      <button
                        type="button"
                        onClick={() => setResume(null)}
                        className="text-red-600 hover:text-red-700 text-sm font-medium"
                      >
                        파일 제거
                      </button>
                    </div>
                  ) : (
                    <div className="space-y-4">
                      <div className="w-16 h-16 bg-gray-100 rounded-lg flex items-center justify-center mx-auto">
                        <Upload className="w-8 h-8 text-gray-400" />
                      </div>
                      <div>
                        <p className="text-lg font-medium text-gray-900">
                          파일을 여기로 드래그하거나 클릭해서 업로드
                        </p>
                        <p className="text-sm text-gray-500 mt-1">
                          PDF, DOC, DOCX 파일 (최대 10MB)
                        </p>
                      </div>
                      <input
                        type="file"
                        accept=".pdf,.doc,.docx"
                        onChange={(e) => setResume(e.target.files?.[0] || null)}
                        className="sr-only"
                        id="resume-upload"
                      />
                      <label
                        htmlFor="resume-upload"
                        className="inline-flex items-center px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 cursor-pointer transition-colors"
                      >
                        <Upload className="w-4 h-4 mr-2" />
                        파일 선택
                      </label>
                    </div>
                  )}
                </div>

                <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
                  <h4 className="text-sm font-medium text-blue-800 mb-2">💡 이력서 작성 팁</h4>
                  <ul className="text-sm text-blue-700 space-y-1">
                    <li>• 최신 경력과 프로젝트를 포함해 주세요</li>
                    <li>• 해당 포지션과 관련된 기술 스택을 강조해 주세요</li>
                    <li>• 구체적인 성과나 결과물이 있다면 함께 기재해 주세요</li>
                  </ul>
                </div>
              </div>
            )}

            {/* Step 3: Preview */}
            {currentStep === 3 && (
              <div className="p-6 space-y-6">
                <div>
                  <h3 className="text-lg font-semibold text-gray-900 mb-2">지원서를 확인해 주세요</h3>
                  <p className="text-gray-600">제출하기 전에 작성한 내용을 검토해 보세요.</p>
                </div>

                {/* Job Info */}
                <div className="bg-gray-50 rounded-lg p-4">
                  <h4 className="font-medium text-gray-900 mb-2">지원 공고</h4>
                  <p className="text-gray-700">{jobTitle}</p>
                </div>

                {/* Cover Letter Preview */}
                <div className="bg-white border rounded-lg">
                  <div className="px-4 py-3 bg-gray-50 border-b rounded-t-lg">
                    <h4 className="font-medium text-gray-900 flex items-center">
                      <FileText className="w-4 h-4 mr-2" />
                      자기소개서
                    </h4>
                  </div>
                  <div className="p-4">
                    <div className="whitespace-pre-wrap text-gray-700 max-h-64 overflow-y-auto">
                      {coverLetter || '작성된 자기소개서가 없습니다.'}
                    </div>
                  </div>
                </div>

                {/* Resume Preview */}
                <div className="bg-white border rounded-lg">
                  <div className="px-4 py-3 bg-gray-50 border-b rounded-t-lg">
                    <h4 className="font-medium text-gray-900 flex items-center">
                      <Upload className="w-4 h-4 mr-2" />
                      첨부 이력서
                    </h4>
                  </div>
                  <div className="p-4">
                    {resume ? (
                      <div className="flex items-center space-x-3">
                        <FileText className="w-6 h-6 text-gray-400" />
                        <div>
                          <p className="text-gray-900 font-medium">{resume.name}</p>
                          <p className="text-sm text-gray-500">{(resume.size / 1024 / 1024).toFixed(2)}MB</p>
                        </div>
                      </div>
                    ) : (
                      <p className="text-gray-500">첨부된 이력서가 없습니다.</p>
                    )}
                  </div>
                </div>

                <div className="bg-green-50 border border-green-200 rounded-lg p-4">
                  <div className="flex">
                    <Check className="w-5 h-5 text-green-600 mt-0.5 mr-2" />
                    <div>
                      <h4 className="text-sm font-medium text-green-800">지원 준비 완료!</h4>
                      <p className="text-sm text-green-700 mt-1">
                        작성하신 내용으로 지원서가 제출됩니다. 확인 후 지원하기 버튼을 클릭해 주세요.
                      </p>
                    </div>
                  </div>
                </div>
              </div>
            )}

            {/* Footer */}
            <div className="px-6 py-4 bg-gray-50 border-t flex justify-between items-center">
              <div className="flex space-x-3">
                {currentStep > 1 && (
                  <button
                    type="button"
                    onClick={() => setCurrentStep(currentStep - 1)}
                    className="inline-flex items-center px-4 py-2 text-gray-600 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors"
                  >
                    <ChevronLeft className="w-4 h-4 mr-1" />
                    이전
                  </button>
                )}
                <button
                  type="button"
                  onClick={onClose}
                  className="px-4 py-2 text-gray-600 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors"
                >
                  취소
                </button>
              </div>

              <div className="flex space-x-3">
                {currentStep < 3 && (
                  <button
                    type="button"
                    onClick={() => setCurrentStep(currentStep + 1)}
                    disabled={currentStep === 1 && !coverLetter.trim()}
                    className="inline-flex items-center px-6 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                  >
                    다음
                    <ChevronRight className="w-4 h-4 ml-1" />
                  </button>
                )}
                {currentStep === 3 && (
                  <button
                    type="submit"
                    disabled={isLoading || !coverLetter.trim()}
                    className="inline-flex items-center px-6 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                    onClick={(e) => {
                      console.log('Submit button clicked', e)
                    }}
                  >
                    {isLoading ? (
                      <>
                        <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin mr-2"></div>
                        지원 중...
                      </>
                    ) : (
                      <>
                        <Check className="w-4 h-4 mr-2" />
                        지원하기
                      </>
                    )}
                  </button>
                )}
              </div>
            </div>
          </form>
        </div>
      </div>
    </div>
  )
}

interface ApplicantDetailModalProps {
  applicant: any
  jobTitle: string
  onClose: () => void
}

function ApplicantDetailModal({ applicant, jobTitle, onClose }: ApplicantDetailModalProps) {
  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString('ko-KR')
  }

  const formatAppliedDate = (appliedAt: any): string => {
    try {
      // 배열 형태인 경우 (백엔드 LocalDateTime 직렬화): [year, month, day, hour, minute, second, nano]
      if (Array.isArray(appliedAt)) {
        const [year, month, day] = appliedAt
        return new Date(year, month - 1, day).toLocaleDateString('ko-KR')
      }

      // 문자열 형태인 경우
      if (typeof appliedAt === 'string') {
        return new Date(appliedAt).toLocaleDateString('ko-KR')
      }

      // 숫자 형태인 경우 (timestamp)
      if (typeof appliedAt === 'number') {
        return new Date(appliedAt).toLocaleDateString('ko-KR')
      }

      return '정보 없음'
    } catch (error) {
      console.warn('Date parsing error:', error, 'appliedAt:', appliedAt)
      return '정보 없음'
    }
  }

  const getApplicationStatusLabel = (status: ApplicationStatus) => {
    const labels: Record<ApplicationStatus, string> = {
      [ApplicationStatus.SUBMITTED]: '지원 완료',
      [ApplicationStatus.REVIEWED]: '검토 중',
      [ApplicationStatus.DOCUMENT_PASSED]: '서류 합격',
      [ApplicationStatus.INTERVIEW_SCHEDULED]: '면접 예정',
      [ApplicationStatus.INTERVIEW_PASSED]: '면접 합격',
      [ApplicationStatus.HIRED]: '최종 합격',
      [ApplicationStatus.REJECTED]: '불합격',
      [ApplicationStatus.WITHDRAWN]: '지원 철회'
    }
    return labels[status] || status
  }

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-xl max-w-4xl w-full mx-4 max-h-[90vh] overflow-hidden shadow-2xl">

        {/* Header */}
        <div className="px-6 py-4 border-b border-gray-200">
          <div className="flex justify-between items-center">
            <div>
              <h2 className="text-2xl font-bold text-gray-900 flex items-center">
                <UserCheck className="w-6 h-6 mr-2 text-blue-600" />
                지원자 상세정보
              </h2>
              <p className="text-gray-600 mt-1">{jobTitle}</p>
            </div>
            <button
              onClick={onClose}
              className="text-gray-400 hover:text-gray-600 transition-colors p-2 hover:bg-gray-100 rounded-lg"
            >
              <X className="w-6 h-6" />
            </button>
          </div>
        </div>

        {/* Content */}
        <div className="flex-1 overflow-y-auto p-6 space-y-6" style={{ maxHeight: 'calc(90vh - 100px)' }}>

          {/* 지원자 기본 정보 */}
          <div className="bg-gradient-to-r from-blue-50 to-purple-50 rounded-lg p-6">
            <h3 className="text-lg font-semibold text-gray-900 mb-4 flex items-center gap-2">
              <UserCheck className="w-5 h-5 text-blue-600" />
              지원자 정보
            </h3>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="flex items-center gap-3">
                <div className="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center">
                  <UserCheck className="w-6 h-6 text-blue-600" />
                </div>
                <div>
                  <p className="text-sm text-gray-600">지원자명</p>
                  <p className="font-medium text-gray-900">
                    {applicant.userName || '이름 없음'}
                  </p>
                </div>
              </div>
              <div className="flex items-center gap-3">
                <div className="w-12 h-12 bg-green-100 rounded-full flex items-center justify-center">
                  <Mail className="w-6 h-6 text-green-600" />
                </div>
                <div>
                  <p className="text-sm text-gray-600">이메일</p>
                  <p className="font-medium text-gray-900">
                    {applicant.userEmail || '이메일 없음'}
                  </p>
                </div>
              </div>
              <div className="flex items-center gap-3">
                <div className="w-12 h-12 bg-orange-100 rounded-full flex items-center justify-center">
                  <Calendar className="w-6 h-6 text-orange-600" />
                </div>
                <div>
                  <p className="text-sm text-gray-600">지원일</p>
                  <p className="font-medium text-gray-900">
                    {applicant.appliedAt ? formatAppliedDate(applicant.appliedAt) : '정보 없음'}
                  </p>
                </div>
              </div>
              <div className="flex items-center gap-3">
                <div className="w-12 h-12 bg-purple-100 rounded-full flex items-center justify-center">
                  <Clock className="w-6 h-6 text-purple-600" />
                </div>
                <div>
                  <p className="text-sm text-gray-600">지원 상태</p>
                  <p className="font-medium text-gray-900">
                    {applicant.status ? getApplicationStatusLabel(applicant.status) : '정보 없음'}
                  </p>
                </div>
              </div>
            </div>
          </div>

          {/* 자기소개서 */}
          <div className="bg-white border border-gray-200 rounded-lg">
            <div className="px-4 py-3 bg-gray-50 border-b rounded-t-lg">
              <h4 className="font-medium text-gray-900 flex items-center">
                <FileText className="w-4 h-4 mr-2" />
                자기소개서
              </h4>
            </div>
            <div className="p-4">
              <div className="whitespace-pre-wrap text-gray-700 leading-relaxed">
                {applicant.coverLetter || '자기소개서가 제출되지 않았습니다.'}
              </div>
            </div>
          </div>

          {/* 첨부 파일 정보 */}
          {applicant.resumeUrl && (
            <div className="bg-white border border-gray-200 rounded-lg">
              <div className="px-4 py-3 bg-gray-50 border-b rounded-t-lg">
                <h4 className="font-medium text-gray-900 flex items-center">
                  <Upload className="w-4 h-4 mr-2" />
                  첨부 이력서
                </h4>
              </div>
              <div className="p-4">
                <div className="flex items-center space-x-3">
                  <FileText className="w-6 h-6 text-blue-400" />
                  <div>
                    <p className="text-gray-900 font-medium">이력서 파일</p>
                    <p className="text-sm text-gray-500">
                      URL: {applicant.resumeUrl}
                    </p>
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* 지원자 통계 */}
          <div className="bg-gray-50 rounded-lg p-4">
            <h4 className="font-medium text-gray-900 mb-3">지원 요약</h4>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div className="text-center">
                <p className="text-2xl font-bold text-blue-600">{applicant.id}</p>
                <p className="text-sm text-gray-600">지원 번호</p>
              </div>
              <div className="text-center">
                <p className="text-2xl font-bold text-green-600">
                  {applicant.coverLetter ? Math.ceil(applicant.coverLetter.length / 100) : 0}
                </p>
                <p className="text-sm text-gray-600">자기소개서 (100자 단위)</p>
              </div>
              <div className="text-center">
                <p className="text-2xl font-bold text-orange-600">
                  {applicant.resumeUrl ? 1 : 0}
                </p>
                <p className="text-sm text-gray-600">첨부 파일</p>
              </div>
            </div>
          </div>
        </div>

        {/* Footer */}
        <div className="px-6 py-4 bg-gray-50 border-t flex justify-end">
          <button
            onClick={onClose}
            className="px-6 py-2 bg-gray-600 text-white rounded-lg hover:bg-gray-700 transition-colors"
          >
            닫기
          </button>
        </div>
      </div>
    </div>
  )
}