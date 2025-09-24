import { useState, useEffect } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { 
  Briefcase, 
  Search, 
  Filter,
  Eye,
  Edit,
  CheckCircle,
  XCircle,
  Clock,
  MapPin,
  Building,
  DollarSign,
  Users,
  AlertCircle,
  Trash2
} from 'lucide-react'
import { apiClient } from '@/services/api'

interface JobPosting {
  id: number
  title: string
  companyName: string
  location: string
  jobType: 'FULL_TIME' | 'PART_TIME' | 'CONTRACT' | 'INTERNSHIP'
  experienceLevel: 'ENTRY_LEVEL' | 'MID_LEVEL' | 'SENIOR_LEVEL' | 'EXECUTIVE'
  description: string
  minSalary?: number
  maxSalary?: number
  qualifications?: string
  requiredSkills?: string
  status: 'DRAFT' | 'PUBLISHED' | 'CLOSED' | 'EXPIRED'
  createdAt: string
  publishedAt?: string
  closedAt?: string
  applicationCount?: number
  views?: number
}

interface JobStatistics {
  totalJobPostings: number
  publishedJobs: number
  closedJobs: number
  draftJobs: number
  totalApplications: number
  avgApplicationsPerJob: number
}

export default function JobManagement() {
  const queryClient = useQueryClient()
  const [isAdmin, setIsAdmin] = useState(false)

  // Check admin authentication
  useEffect(() => {
    const adminToken = localStorage.getItem('adminToken')
    const isAdminStatus = localStorage.getItem('isAdmin')
    setIsAdmin(adminToken && isAdminStatus === 'true')
  }, [])
  const [searchTerm, setSearchTerm] = useState('')
  const [statusFilter, setStatusFilter] = useState<string>('ALL')
  const [jobTypeFilter, setJobTypeFilter] = useState<string>('ALL')
  const [selectedJob, setSelectedJob] = useState<JobPosting | null>(null)

  // Mock data for demonstration - in reality this would come from an admin API
  const { data: jobPostings, isLoading: jobsLoading } = useQuery({
    queryKey: ['admin-job-postings'],
    queryFn: async () => {
      // Using the existing job postings API - in a real scenario there would be an admin-specific endpoint
      const response = await apiClient.getJobPostings({ page: 0, size: 100 })
      return response.data.content as JobPosting[]
    },
    enabled: isAdmin
  })

  const { data: statistics } = useQuery({
    queryKey: ['job-statistics'],
    queryFn: async () => {
      // Mock statistics - would be from admin dashboard API
      return {
        totalJobPostings: jobPostings?.length || 0,
        publishedJobs: jobPostings?.filter(j => j.status === 'PUBLISHED').length || 0,
        closedJobs: jobPostings?.filter(j => j.status === 'CLOSED').length || 0,
        draftJobs: jobPostings?.filter(j => j.status === 'DRAFT').length || 0,
        totalApplications: jobPostings?.reduce((acc, job) => acc + (job.applicationCount || 0), 0) || 0,
        avgApplicationsPerJob: Math.round((jobPostings?.reduce((acc, job) => acc + (job.applicationCount || 0), 0) || 0) / Math.max(jobPostings?.length || 1, 1))
      } as JobStatistics
    },
    enabled: !!jobPostings
  })

  const deleteJobMutation = useMutation({
    mutationFn: async (jobId: number) => {
      await apiClient.deleteJobPosting(jobId)
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-job-postings'] })
      alert('채용공고가 삭제되었습니다.')
    },
    onError: (error: any) => {
      alert(`삭제 실패: ${error.message || '오류가 발생했습니다.'}`)
    }
  })

  const handleDeleteJob = (job: JobPosting) => {
    if (window.confirm(`정말로 "${job.title}" 채용공고를 삭제하시겠습니까?`)) {
      deleteJobMutation.mutate(job.id)
    }
  }

  if (!isAdmin) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <AlertCircle className="w-16 h-16 text-red-500 mx-auto mb-4" />
          <h2 className="text-xl font-semibold mb-2">접근 권한이 없습니다</h2>
          <p className="text-gray-600">관리자만 접근할 수 있는 페이지입니다.</p>
        </div>
      </div>
    )
  }

  if (jobsLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-500"></div>
      </div>
    )
  }

  const filteredJobs = jobPostings?.filter((job) => {
    const matchesSearch = job.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         job.companyName.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         job.location.toLowerCase().includes(searchTerm.toLowerCase())
    const matchesStatus = statusFilter === 'ALL' || job.status === statusFilter
    const matchesJobType = jobTypeFilter === 'ALL' || job.jobType === jobTypeFilter
    
    return matchesSearch && matchesStatus && matchesJobType
  }) || []

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'PUBLISHED':
        return 'bg-green-100 text-green-800'
      case 'DRAFT':
        return 'bg-yellow-100 text-yellow-800'
      case 'CLOSED':
        return 'bg-red-100 text-red-800'
      case 'EXPIRED':
        return 'bg-gray-100 text-gray-800'
      default:
        return 'bg-gray-100 text-gray-800'
    }
  }

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'PUBLISHED':
        return <CheckCircle className="w-3 h-3" />
      case 'DRAFT':
        return <Clock className="w-3 h-3" />
      case 'CLOSED':
      case 'EXPIRED':
        return <XCircle className="w-3 h-3" />
      default:
        return <Clock className="w-3 h-3" />
    }
  }

  const getStatusLabel = (status: string) => {
    switch (status) {
      case 'PUBLISHED':
        return '게시중'
      case 'DRAFT':
        return '임시저장'
      case 'CLOSED':
        return '마감'
      case 'EXPIRED':
        return '만료'
      default:
        return status
    }
  }

  const getJobTypeLabel = (jobType: string) => {
    switch (jobType) {
      case 'FULL_TIME':
        return '정규직'
      case 'PART_TIME':
        return '계약직'
      case 'CONTRACT':
        return '계약직'
      case 'INTERNSHIP':
        return '인턴'
      default:
        return jobType
    }
  }

  const getExperienceLevelLabel = (level: string) => {
    switch (level) {
      case 'ENTRY_LEVEL':
        return '신입'
      case 'MID_LEVEL':
        return '경력'
      case 'SENIOR_LEVEL':
        return '시니어'
      case 'EXECUTIVE':
        return '임원급'
      default:
        return level
    }
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">채용공고 관리</h1>
        <p className="text-gray-600">시스템에 등록된 모든 채용공고를 관리합니다</p>
      </div>

      {/* 통계 카드 */}
      <div className="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-6 gap-4">
        <div className="card">
          <div className="card-content text-center">
            <Briefcase className="w-8 h-8 text-blue-500 mx-auto mb-2" />
            <p className="text-sm font-medium text-gray-600">전체 공고</p>
            <p className="text-2xl font-bold text-gray-900">{statistics?.totalJobPostings || 0}</p>
          </div>
        </div>
        <div className="card">
          <div className="card-content text-center">
            <CheckCircle className="w-8 h-8 text-green-500 mx-auto mb-2" />
            <p className="text-sm font-medium text-gray-600">게시 중</p>
            <p className="text-2xl font-bold text-gray-900">{statistics?.publishedJobs || 0}</p>
          </div>
        </div>
        <div className="card">
          <div className="card-content text-center">
            <Clock className="w-8 h-8 text-yellow-500 mx-auto mb-2" />
            <p className="text-sm font-medium text-gray-600">임시저장</p>
            <p className="text-2xl font-bold text-gray-900">{statistics?.draftJobs || 0}</p>
          </div>
        </div>
        <div className="card">
          <div className="card-content text-center">
            <XCircle className="w-8 h-8 text-red-500 mx-auto mb-2" />
            <p className="text-sm font-medium text-gray-600">마감된 공고</p>
            <p className="text-2xl font-bold text-gray-900">{statistics?.closedJobs || 0}</p>
          </div>
        </div>
        <div className="card">
          <div className="card-content text-center">
            <Users className="w-8 h-8 text-purple-500 mx-auto mb-2" />
            <p className="text-sm font-medium text-gray-600">총 지원자</p>
            <p className="text-2xl font-bold text-gray-900">{statistics?.totalApplications || 0}</p>
          </div>
        </div>
        <div className="card">
          <div className="card-content text-center">
            <DollarSign className="w-8 h-8 text-emerald-500 mx-auto mb-2" />
            <p className="text-sm font-medium text-gray-600">공고당 평균 지원</p>
            <p className="text-2xl font-bold text-gray-900">{statistics?.avgApplicationsPerJob || 0}</p>
          </div>
        </div>
      </div>

      {/* 필터 및 검색 */}
      <div className="card">
        <div className="card-content">
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-4 h-4" />
              <input
                type="text"
                placeholder="제목, 회사명, 지역으로 검색..."
                className="pl-10 pr-4 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent w-full"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
            </div>
            
            <div>
              <select
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                value={statusFilter}
                onChange={(e) => setStatusFilter(e.target.value)}
              >
                <option value="ALL">전체 상태</option>
                <option value="PUBLISHED">게시 중</option>
                <option value="DRAFT">임시저장</option>
                <option value="CLOSED">마감</option>
                <option value="EXPIRED">만료</option>
              </select>
            </div>

            <div>
              <select
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                value={jobTypeFilter}
                onChange={(e) => setJobTypeFilter(e.target.value)}
              >
                <option value="ALL">전체 고용형태</option>
                <option value="FULL_TIME">정규직</option>
                <option value="PART_TIME">파트타임</option>
                <option value="CONTRACT">계약직</option>
                <option value="INTERNSHIP">인턴</option>
              </select>
            </div>

            <div className="text-sm text-gray-600 flex items-center">
              <Filter className="w-4 h-4 mr-2" />
              {filteredJobs.length}개 결과
            </div>
          </div>
        </div>
      </div>

      {/* 채용공고 목록 */}
      <div className="card">
        <div className="card-content">
          <div className="overflow-x-auto">
            <table className="min-w-full">
              <thead>
                <tr className="border-b border-gray-200">
                  <th className="text-left py-3 px-3 text-sm font-medium text-gray-600">채용공고</th>
                  <th className="text-left py-3 px-3 text-sm font-medium text-gray-600">회사</th>
                  <th className="text-left py-3 px-3 text-sm font-medium text-gray-600">위치</th>
                  <th className="text-left py-3 px-3 text-sm font-medium text-gray-600">고용형태</th>
                  <th className="text-left py-3 px-3 text-sm font-medium text-gray-600">상태</th>
                  <th className="text-left py-3 px-3 text-sm font-medium text-gray-600">지원자 수</th>
                  <th className="text-left py-3 px-3 text-sm font-medium text-gray-600">등록일</th>
                  <th className="text-left py-3 px-3 text-sm font-medium text-gray-600">작업</th>
                </tr>
              </thead>
              <tbody>
                {filteredJobs.map((job) => (
                  <tr key={job.id} className="border-b border-gray-100">
                    <td className="py-3 px-3">
                      <div>
                        <p className="text-sm font-medium text-gray-900">{job.title}</p>
                        <p className="text-xs text-gray-500">{getExperienceLevelLabel(job.experienceLevel)}</p>
                        {job.minSalary && job.maxSalary && (
                          <p className="text-xs text-green-600">
                            {job.minSalary}만원 - {job.maxSalary}만원
                          </p>
                        )}
                      </div>
                    </td>
                    <td className="py-3 px-3">
                      <div className="flex items-center">
                        <Building className="w-4 h-4 text-gray-400 mr-1" />
                        <span className="text-sm text-gray-900">{job.companyName}</span>
                      </div>
                    </td>
                    <td className="py-3 px-3">
                      <div className="flex items-center">
                        <MapPin className="w-4 h-4 text-gray-400 mr-1" />
                        <span className="text-sm text-gray-700">{job.location}</span>
                      </div>
                    </td>
                    <td className="py-3 px-3">
                      <span className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                        {getJobTypeLabel(job.jobType)}
                      </span>
                    </td>
                    <td className="py-3 px-3">
                      <span className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium ${getStatusColor(job.status)}`}>
                        {getStatusIcon(job.status)}
                        <span className="ml-1">{getStatusLabel(job.status)}</span>
                      </span>
                    </td>
                    <td className="py-3 px-3 text-center">
                      <div className="flex items-center justify-center">
                        <Users className="w-4 h-4 text-gray-400 mr-1" />
                        <span className="text-sm font-medium text-gray-900">
                          {job.applicationCount || 0}
                        </span>
                      </div>
                    </td>
                    <td className="py-3 px-3 text-sm text-gray-500">
                      {new Date(job.createdAt).toLocaleDateString('ko-KR')}
                    </td>
                    <td className="py-3 px-3">
                      <div className="flex space-x-2">
                        <button
                          onClick={() => setSelectedJob(job)}
                          className="text-blue-600 hover:text-blue-800"
                          title="상세 보기"
                        >
                          <Eye className="w-4 h-4" />
                        </button>
                        <button
                          onClick={() => handleDeleteJob(job)}
                          className="text-red-600 hover:text-red-800"
                          title="삭제"
                          disabled={deleteJobMutation.isPending}
                        >
                          <Trash2 className="w-4 h-4" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            
            {filteredJobs.length === 0 && (
              <div className="text-center py-8">
                <Briefcase className="w-16 h-16 text-gray-300 mx-auto mb-4" />
                <p className="text-gray-500">조건에 맞는 채용공고가 없습니다.</p>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* 채용공고 상세 모달 */}
      {selectedJob && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-lg max-w-2xl w-full max-h-[80vh] overflow-y-auto">
            <div className="p-6">
              <div className="flex justify-between items-start mb-4">
                <div>
                  <h2 className="text-xl font-bold text-gray-900">{selectedJob.title}</h2>
                  <div className="flex items-center mt-2 space-x-4">
                    <div className="flex items-center">
                      <Building className="w-4 h-4 text-gray-400 mr-1" />
                      <span className="text-sm text-gray-600">{selectedJob.companyName}</span>
                    </div>
                    <div className="flex items-center">
                      <MapPin className="w-4 h-4 text-gray-400 mr-1" />
                      <span className="text-sm text-gray-600">{selectedJob.location}</span>
                    </div>
                    <span className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium ${getStatusColor(selectedJob.status)}`}>
                      {getStatusIcon(selectedJob.status)}
                      <span className="ml-1">{getStatusLabel(selectedJob.status)}</span>
                    </span>
                  </div>
                </div>
                <button
                  onClick={() => setSelectedJob(null)}
                  className="text-gray-400 hover:text-gray-600"
                >
                  <XCircle className="w-6 h-6" />
                </button>
              </div>

              <div className="space-y-4">
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <p className="text-sm font-medium text-gray-700">고용 형태</p>
                    <p className="text-sm text-gray-900">{getJobTypeLabel(selectedJob.jobType)}</p>
                  </div>
                  <div>
                    <p className="text-sm font-medium text-gray-700">경력 수준</p>
                    <p className="text-sm text-gray-900">{getExperienceLevelLabel(selectedJob.experienceLevel)}</p>
                  </div>
                </div>

                {(selectedJob.minSalary || selectedJob.maxSalary) && (
                  <div>
                    <p className="text-sm font-medium text-gray-700">급여</p>
                    <p className="text-sm text-gray-900">
                      {selectedJob.minSalary}만원 - {selectedJob.maxSalary}만원
                    </p>
                  </div>
                )}

                <div>
                  <p className="text-sm font-medium text-gray-700">업무 내용</p>
                  <p className="text-sm text-gray-900 whitespace-pre-wrap">{selectedJob.description}</p>
                </div>

                {selectedJob.qualifications && (
                  <div>
                    <p className="text-sm font-medium text-gray-700">자격 요건</p>
                    <p className="text-sm text-gray-900 whitespace-pre-wrap">{selectedJob.qualifications}</p>
                  </div>
                )}

                {selectedJob.requiredSkills && (
                  <div>
                    <p className="text-sm font-medium text-gray-700">필요 기술</p>
                    <p className="text-sm text-gray-900">{selectedJob.requiredSkills}</p>
                  </div>
                )}

                <div className="grid grid-cols-2 gap-4 pt-4 border-t border-gray-200">
                  <div>
                    <p className="text-sm font-medium text-gray-700">지원자 수</p>
                    <p className="text-sm text-gray-900">{selectedJob.applicationCount || 0}명</p>
                  </div>
                  <div>
                    <p className="text-sm font-medium text-gray-700">등록일</p>
                    <p className="text-sm text-gray-900">
                      {new Date(selectedJob.createdAt).toLocaleDateString('ko-KR')}
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}