import React, { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Link, useNavigate } from 'react-router-dom'
import {
  Plus,
  Edit,
  Trash2,
  Eye,
  Users,
  Calendar,
  TrendingUp,
  AlertTriangle,
  BarChart3,
  Briefcase,
  CheckCircle,
  Clock,
  MapPin,
  DollarSign
} from 'lucide-react'
import { apiClient } from '@/services/api'
import { JobPosting, JobType, ExperienceLevel } from '@/types/api'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/Card'
import { Badge } from '@/components/ui/Badge'
import { Button } from '@/components/ui/Button'
import { useAuthStore } from '@/hooks/useAuthStore'
import { useToast } from '@/contexts/ToastContext'
import DeadlineApproachingJobs from '@/components/jobs/DeadlineApproachingJobs'

// 백엔드에서 오는 배열 형태의 날짜를 파싱하는 유틸 함수
const parseDate = (dateArray: number[] | string | null): Date | null => {
  if (!dateArray) return null;

  if (typeof dateArray === 'string') {
    return new Date(dateArray);
  }

  if (Array.isArray(dateArray)) {
    const [year, month, day, hour = 0, minute = 0, second = 0] = dateArray;
    return new Date(year, month - 1, day, hour, minute, second);
  }

  return null;
};

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

const formatSalary = (min?: number, max?: number) => {
  if (!min && !max) return '연봉 정보 없음'
  if (min && max) return `${min.toLocaleString()}만원 - ${max.toLocaleString()}만원`
  if (min) return `${min.toLocaleString()}만원 이상`
  if (max) return `${max.toLocaleString()}만원 이하`
  return '연봉 정보 없음'
}

export default function MyJobPostings() {
  const { user } = useAuthStore()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const { showToast } = useToast()
  const [selectedTab, setSelectedTab] = useState<'all' | 'published' | 'draft'>('all')

  // 내 채용공고 목록 조회
  const { data: jobsData, isLoading: jobsLoading } = useQuery({
    queryKey: ['myJobPostings'],
    queryFn: () => apiClient.getMyJobPostings(0, 50),
    enabled: !!user
  })

  // 내 채용공고 통계 조회
  const { data: statsData, isLoading: statsLoading } = useQuery({
    queryKey: ['myJobPostingStats'],
    queryFn: () => apiClient.getMyJobPostingStats(),
    enabled: !!user
  })

  // 채용공고 삭제 뮤테이션
  const deleteMutation = useMutation({
    mutationFn: (id: number) => apiClient.deleteJobPosting(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['myJobPostings'] })
      queryClient.invalidateQueries({ queryKey: ['myJobPostingStats'] })
      showToast('채용공고가 삭제되었습니다', 'success')
    },
    onError: () => {
      showToast('채용공고 삭제에 실패했습니다', 'error')
    }
  })

  const handleDelete = (id: number, title: string) => {
    if (window.confirm(`'${title}' 채용공고를 정말 삭제하시겠습니까?`)) {
      deleteMutation.mutate(id)
    }
  }

  const jobs = jobsData?.data?.content || []
  const stats = statsData?.data || []

  // 탭별 필터링
  const filteredJobs = jobs.filter(job => {
    if (selectedTab === 'published') return job.status === 'PUBLISHED'
    if (selectedTab === 'draft') return job.status === 'DRAFT'
    return true
  })

  // 전체 통계 계산
  const totalStats = {
    totalJobs: jobs.length,
    publishedJobs: jobs.filter(job => job.status === 'PUBLISHED').length,
    draftJobs: jobs.filter(job => job.status === 'DRAFT').length,
    totalViews: stats.reduce((sum, stat) => sum + (stat.viewCount || 0), 0),
    totalApplications: stats.reduce((sum, stat) => sum + (stat.applicationCount || 0), 0),
    avgApplicationRate: stats.length > 0
      ? (stats.reduce((sum, stat) => sum + (stat.viewToApplicationRatio || 0), 0) / stats.length).toFixed(1)
      : '0.0'
  }

  if (jobsLoading || statsLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-4 border-primary-200 border-t-primary-600 shadow-lg"></div>
      </div>
    )
  }

  return (
    <div className="space-y-8 p-6">
      {/* 헤더 */}
      <div className="flex items-center justify-between">
        <div className="flex items-center">
          <div className="p-3 bg-gradient-to-r from-blue-500 to-purple-600 rounded-xl shadow-lg mr-4">
            <Briefcase className="w-8 h-8 text-white" />
          </div>
          <div>
            <h1 className="text-4xl font-bold text-gray-900 mb-2">내 채용공고 관리</h1>
            <p className="text-xl text-gray-600">채용공고 현황과 성과를 확인하고 관리하세요</p>
          </div>
        </div>
        <Button
          onClick={() => navigate('/jobs/create')}
          className="flex items-center gap-2"
          size="lg"
        >
          <Plus className="w-5 h-5" />
          새 채용공고 작성
        </Button>
      </div>

      {/* 마감 임박 채용공고 */}
      <DeadlineApproachingJobs days={15} maxItems={3} showForCompany={true} />

      {/* 통계 대시보드 */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <Card className="border-blue-200 bg-blue-50">
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-blue-600">전체 공고</p>
                <p className="text-3xl font-bold text-blue-900">{totalStats.totalJobs}</p>
              </div>
              <Briefcase className="w-8 h-8 text-blue-500" />
            </div>
          </CardContent>
        </Card>

        <Card className="border-green-200 bg-green-50">
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-green-600">게시 중</p>
                <p className="text-3xl font-bold text-green-900">{totalStats.publishedJobs}</p>
              </div>
              <CheckCircle className="w-8 h-8 text-green-500" />
            </div>
          </CardContent>
        </Card>

        <Card className="border-purple-200 bg-purple-50">
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-purple-600">총 조회수</p>
                <p className="text-3xl font-bold text-purple-900">{totalStats.totalViews.toLocaleString()}</p>
              </div>
              <Eye className="w-8 h-8 text-purple-500" />
            </div>
          </CardContent>
        </Card>

        <Card className="border-orange-200 bg-orange-50">
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-orange-600">총 지원자</p>
                <p className="text-3xl font-bold text-orange-900">{totalStats.totalApplications}</p>
              </div>
              <Users className="w-8 h-8 text-orange-500" />
            </div>
          </CardContent>
        </Card>
      </div>

      {/* 탭 네비게이션 */}
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <CardTitle className="text-xl">채용공고 목록</CardTitle>
            <div className="flex space-x-1 bg-gray-100 p-1 rounded-lg">
              <button
                onClick={() => setSelectedTab('all')}
                className={`px-4 py-2 text-sm rounded-md transition-colors ${
                  selectedTab === 'all'
                    ? 'bg-white text-gray-900 shadow-sm font-medium'
                    : 'text-gray-600 hover:text-gray-900'
                }`}
              >
                전체 ({totalStats.totalJobs})
              </button>
              <button
                onClick={() => setSelectedTab('published')}
                className={`px-4 py-2 text-sm rounded-md transition-colors ${
                  selectedTab === 'published'
                    ? 'bg-white text-gray-900 shadow-sm font-medium'
                    : 'text-gray-600 hover:text-gray-900'
                }`}
              >
                게시중 ({totalStats.publishedJobs})
              </button>
              <button
                onClick={() => setSelectedTab('draft')}
                className={`px-4 py-2 text-sm rounded-md transition-colors ${
                  selectedTab === 'draft'
                    ? 'bg-white text-gray-900 shadow-sm font-medium'
                    : 'text-gray-600 hover:text-gray-900'
                }`}
              >
                임시저장 ({totalStats.draftJobs})
              </button>
            </div>
          </div>
        </CardHeader>
        <CardContent>
          {filteredJobs.length === 0 ? (
            <div className="text-center py-12">
              <Briefcase className="w-16 h-16 text-gray-300 mx-auto mb-4" />
              <h3 className="text-lg font-medium text-gray-900 mb-2">
                {selectedTab === 'published' ? '게시된 채용공고가 없습니다' :
                 selectedTab === 'draft' ? '임시저장된 채용공고가 없습니다' :
                 '채용공고가 없습니다'}
              </h3>
              <p className="text-gray-600 mb-4">
                새로운 채용공고를 작성하여 우수한 인재를 찾아보세요
              </p>
              <Button onClick={() => navigate('/jobs/create')}>
                <Plus className="w-4 h-4 mr-2" />
                첫 채용공고 작성하기
              </Button>
            </div>
          ) : (
            <div className="space-y-4">
              {filteredJobs.map((job) => {
                const jobStats = stats.find(stat => stat.jobPostingId === job.id)
                const isDeadlineApproaching = jobStats?.isDeadlineApproaching || false
                const daysLeft = jobStats?.daysUntilDeadline || 0

                return (
                  <div
                    key={job.id}
                    className={`border rounded-lg p-6 hover:shadow-md transition-shadow ${
                      isDeadlineApproaching ? 'border-orange-200 bg-orange-50' : 'border-gray-200 bg-white'
                    }`}
                  >
                    <div className="flex justify-between items-start">
                      <div className="flex-1">
                        <div className="flex items-start justify-between mb-3">
                          <div>
                            <Link
                              to={`/jobs/${job.id}`}
                              className="text-xl font-semibold text-gray-900 hover:text-blue-600 transition-colors"
                            >
                              {job.title}
                            </Link>
                            {isDeadlineApproaching && (
                              <Badge variant="warning" size="sm" className="ml-2">
                                <AlertTriangle className="w-3 h-3 mr-1" />
                                마감 {daysLeft}일 전
                              </Badge>
                            )}
                            <div className="flex items-center mt-2 space-x-4 text-sm text-gray-600">
                              <div className="flex items-center">
                                <MapPin className="w-4 h-4 mr-1" />
                                {job.location}
                              </div>
                              <div className="flex items-center">
                                <Clock className="w-4 h-4 mr-1" />
                                {getJobTypeLabel(job.jobType)}
                              </div>
                              <div className="flex items-center">
                                <DollarSign className="w-4 h-4 mr-1" />
                                {formatSalary(job.minSalary, job.maxSalary)}
                              </div>
                            </div>
                          </div>
                          <Badge
                            variant={job.status === 'PUBLISHED' ? 'success' : 'secondary'}
                            size="lg"
                          >
                            {job.status === 'PUBLISHED' ? '게시중' :
                             job.status === 'DRAFT' ? '임시저장' : job.status}
                          </Badge>
                        </div>

                        {/* 통계 정보 */}
                        {jobStats && (
                          <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-4 p-4 bg-gray-50 rounded-lg">
                            <div className="text-center">
                              <div className="text-lg font-semibold text-blue-600">{jobStats.viewCount}</div>
                              <div className="text-xs text-gray-600">조회수</div>
                            </div>
                            <div className="text-center">
                              <div className="text-lg font-semibold text-green-600">{jobStats.applicationCount}</div>
                              <div className="text-xs text-gray-600">지원자</div>
                            </div>
                            <div className="text-center">
                              <div className="text-lg font-semibold text-purple-600">
                                {jobStats.viewToApplicationRatio ? `${(jobStats.viewToApplicationRatio * 100).toFixed(1)}%` : '0%'}
                              </div>
                              <div className="text-xs text-gray-600">지원율</div>
                            </div>
                            <div className="text-center">
                              <div className="text-lg font-semibold text-orange-600">
                                {jobStats.daysUntilDeadline || '∞'}
                              </div>
                              <div className="text-xs text-gray-600">마감까지</div>
                            </div>
                          </div>
                        )}

                        <div className="text-sm text-gray-500">
                          등록일: {parseDate(job.createdAt)?.toLocaleDateString('ko-KR')}
                          {job.publishedAt && (
                            <span className="ml-4">
                              게시일: {parseDate(job.publishedAt)?.toLocaleDateString('ko-KR')}
                            </span>
                          )}
                        </div>
                      </div>

                      <div className="flex flex-col gap-2 ml-4">
                        <Link
                          to={`/jobs/${job.id}`}
                          className="p-2 text-blue-600 hover:text-blue-800 hover:bg-blue-50 rounded-lg transition-colors"
                          title="상세보기"
                        >
                          <Eye className="w-5 h-5" />
                        </Link>
                        <Link
                          to={`/jobs/edit/${job.id}`}
                          className="p-2 text-gray-600 hover:text-gray-800 hover:bg-gray-50 rounded-lg transition-colors"
                          title="수정"
                        >
                          <Edit className="w-5 h-5" />
                        </Link>
                        {jobStats && (
                          <button
                            className="p-2 text-purple-600 hover:text-purple-800 hover:bg-purple-50 rounded-lg transition-colors"
                            title="통계 보기"
                          >
                            <BarChart3 className="w-5 h-5" />
                          </button>
                        )}
                        <button
                          onClick={() => handleDelete(job.id, job.title)}
                          className="p-2 text-red-600 hover:text-red-800 hover:bg-red-50 rounded-lg transition-colors"
                          title="삭제"
                          disabled={deleteMutation.isPending}
                        >
                          <Trash2 className="w-5 h-5" />
                        </button>
                      </div>
                    </div>
                  </div>
                )
              })}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  )
}