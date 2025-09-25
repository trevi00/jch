import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Link, useNavigate } from 'react-router-dom'
import { Search, MapPin, Clock, DollarSign, X, Briefcase, Building2, Calendar, TrendingUp, Plus } from 'lucide-react'
import { apiClient } from '@/services/api'
import { JobType, ExperienceLevel, UserType } from '@/types/api'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/Card'
import { Badge } from '@/components/ui/Badge'
import { Input } from '@/components/ui/Input'
import { Button } from '@/components/ui/Button'
import LoadingCard from '@/components/ui/LoadingCard'
import ErrorMessage from '@/components/ui/ErrorMessage'
import { useToast } from '@/contexts/ToastContext'
import { useAuthStore } from '@/hooks/useAuthStore'
import DeadlineApproachingJobs from '@/components/jobs/DeadlineApproachingJobs'
import QuickSearch from '@/components/jobs/QuickSearch'

/**
 * 백엔드에서 오는 배열 형태의 날짜를 파싱하는 유틸 함수
 * @param dateArray 백엔드에서 전달받는 날짜 데이터 (배열 또는 문자열)
 * 이벤트: 날짜 파싱 이벤트, 날짜 형식 변환 이벤트
 */
const parseDate = (dateArray: number[] | string | null): Date | null => {
  if (!dateArray) return null;

  if (typeof dateArray === 'string') {
    return new Date(dateArray);
  }

  if (Array.isArray(dateArray)) {
    // [year, month, day, hour, minute, second, nanosecond] 형태
    // month는 0-based이므로 1을 빼야 함
    const [year, month, day, hour = 0, minute = 0, second = 0] = dateArray;
    return new Date(year, month - 1, day, hour, minute, second);
  }

  return null;
};

/**
 * 채용공고 목록 페이지 컴포넌트
 * 채용공고 검색, 필터링, 목록 조회 기능을 제공
 * 이벤트: 검색 이벤트, 필터 적용 이벤트, 페이지네이션 이벤트, 공고 상세 조회 이벤트
 */
export default function Jobs() {
  const navigate = useNavigate()
  const { user } = useAuthStore()
  const [searchTerm, setSearchTerm] = useState('')
  const [page, setPage] = useState(0)

  // 새로운 간단한 필터 상태 (QuickSearch와 호환)
  const [appliedFilters, setAppliedFilters] = useState({
    keyword: '',
    location: '',
    jobType: undefined as JobType | undefined,
    experienceLevel: undefined as ExperienceLevel | undefined,
  })

  const { data: jobsData, isLoading } = useQuery({
    queryKey: ['jobs', searchTerm, appliedFilters, page],
    queryFn: () => {
      // 검색어가 있으면 키워드로, 없으면 필터의 키워드 사용
      const keyword = searchTerm || appliedFilters.keyword || undefined
      return apiClient.searchJobsSimple(
        keyword,
        appliedFilters.location || undefined,
        appliedFilters.jobType,
        appliedFilters.experienceLevel,
        page,
        10
      )
    },
  })

  // QuickSearch 핸들러들
  const handleQuickSearchApply = (params: {
    keyword?: string
    location?: string
    jobType?: JobType
    experienceLevel?: ExperienceLevel
  }) => {
    setAppliedFilters({
      keyword: params.keyword || '',
      location: params.location || '',
      jobType: params.jobType,
      experienceLevel: params.experienceLevel,
    })
    setSearchTerm('') // 기존 검색어 클리어
    setPage(0)
  }

  // 기본 검색 핸들러
  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault()
    setAppliedFilters({
      keyword: searchTerm,
      location: '',
      jobType: undefined,
      experienceLevel: undefined,
    })
    setPage(0)
  }

  // 필터 초기화
  const clearFilters = () => {
    setAppliedFilters({
      keyword: '',
      location: '',
      jobType: undefined,
      experienceLevel: undefined,
    })
    setSearchTerm('')
    setPage(0)
  }

  // 활성화된 필터 개수 계산
  const getActiveFilterCount = () => {
    let count = 0
    if (appliedFilters.keyword) count++
    if (appliedFilters.location) count++
    if (appliedFilters.jobType) count++
    if (appliedFilters.experienceLevel) count++
    return count
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

  if (isLoading) {
    return (
      <div className="space-y-6">
        <LoadingCard />
        <LoadingCard />
        <LoadingCard />
      </div>
    )
  }

  return (
    <div className="space-y-6">
      {/* 헤더 */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold text-gray-900 mb-2">채용공고</h1>
          <p className="text-gray-600">당신에게 맞는 완벽한 기회를 찾아보세요</p>
        </div>

        <div className="flex flex-wrap gap-2">
          <Badge variant="info" size="lg">
            <TrendingUp className="w-4 h-4 mr-1" />
            AI 맞춤 추천
          </Badge>
          <Badge variant="success" size="lg">
            <Building2 className="w-4 h-4 mr-1" />
            {jobsData?.data?.totalElements || 0}개 공고
          </Badge>
        </div>
      </div>

      {/* 마감 임박 채용공고 */}
      <DeadlineApproachingJobs days={7} maxItems={5} />

      {/* 빠른 검색 */}
      <QuickSearch
        onSearchApply={handleQuickSearchApply}
      />

      {/* 간단한 검색 바 */}
      <Card className="border border-gray-200">
        <CardContent className="p-6">
          <form onSubmit={handleSearch} className="space-y-4">
            <div className="flex gap-3">
              <div className="flex-1">
                <Input
                  type="text"
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  placeholder="직무, 회사명, 기술스택으로 검색"
                  leftIcon={<Search className="h-4 w-4 text-gray-400" />}
                  className="py-4 border border-gray-300 focus:border-blue-500 focus:ring-1 focus:ring-blue-500 transition-colors duration-200"
                />
              </div>
              <Button
                type="submit"
                className="px-8 py-4 bg-blue-600 hover:bg-blue-700 text-white font-medium transition-colors duration-200"
              >
                검색
              </Button>

              {/* 기업 사용자를 위한 빠른 등록 버튼 */}
              {user?.userType === UserType.COMPANY && (
                <Button
                  type="button"
                  onClick={() => navigate('/jobs/create')}
                  className="px-6 py-4 bg-gray-900 hover:bg-gray-800 text-white font-medium transition-colors duration-200 flex items-center gap-2"
                >
                  등록
                </Button>
              )}

              {getActiveFilterCount() > 0 && (
                <Button
                  type="button"
                  variant="outline"
                  onClick={clearFilters}
                  className="px-6 py-4 border border-gray-300 hover:border-gray-400 text-gray-700 hover:text-gray-900 font-medium transition-colors duration-200"
                >
                  <X className="h-4 w-4 mr-2" />
                  필터 해제 ({getActiveFilterCount()})
                </Button>
              )}
            </div>
          </form>
        </CardContent>
      </Card>

      {/* 검색 결과 */}
      <div className="space-y-6">
        <div className="grid grid-cols-1 gap-6">
          {jobsData?.data?.content.map((job) => (
            <Link key={job.id} to={`/jobs/${job.id}`}>
              <Card className="hover:shadow-xl transition-all duration-300 border-0 shadow-md group" hover="lift">
                <CardContent className="p-6">
                  <div className="flex justify-between items-start">
                    <div className="flex-1">
                      <div className="flex items-start justify-between mb-4">
                        <div className="flex-1">
                          <h3 className="text-2xl font-bold text-gray-900 mb-2 group-hover:text-primary-600 transition-colors">
                            {job.title}
                          </h3>
                          <div className="flex items-center mb-3">
                            <Building2 className="w-5 h-5 mr-2 text-gray-500" />
                            <p className="text-lg font-semibold text-gray-700">{job.companyName}</p>
                          </div>
                        </div>
                        <Badge variant={job.experienceLevel === ExperienceLevel.ENTRY_LEVEL ? 'success' : 'default'} size="lg">
                          {getExperienceLevelLabel(job.experienceLevel)}
                        </Badge>
                      </div>

                      <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
                        <div className="flex items-center text-gray-600">
                          <MapPin className="w-5 h-5 mr-2 text-primary-500" />
                          <span className="font-medium">{job.location}</span>
                        </div>
                        <div className="flex items-center text-gray-600">
                          <Clock className="w-5 h-5 mr-2 text-primary-500" />
                          <span className="font-medium">{getJobTypeLabel(job.jobType)}</span>
                        </div>
                        <div className="flex items-center text-gray-600">
                          <DollarSign className="w-5 h-5 mr-2 text-primary-500" />
                          <span className="font-medium">{formatSalary(job.minSalary, job.maxSalary)}</span>
                        </div>
                      </div>

                      {job.requiredSkills && job.requiredSkills.length > 0 && (
                        <div className="flex flex-wrap gap-2 mb-4">
                          {job.requiredSkills.slice(0, 6).map((skill, index) => (
                            <Badge key={index} variant="outline" size="sm">
                              {skill}
                            </Badge>
                          ))}
                          {job.requiredSkills.length > 6 && (
                            <Badge variant="secondary" size="sm">
                              +{job.requiredSkills.length - 6}
                            </Badge>
                          )}
                        </div>
                      )}

                      <div className="flex items-center justify-between text-sm text-gray-500">
                        <div className="flex items-center">
                          <Calendar className="w-4 h-4 mr-1" />
                          <span>
                            마감일: {job.deadline ? parseDate(job.deadline)?.toLocaleDateString('ko-KR') || '상시모집' : '상시모집'}
                          </span>
                        </div>
                        <Badge variant="info" size="sm">
                          지원하기
                        </Badge>
                      </div>
                    </div>
                  </div>
                </CardContent>
              </Card>
            </Link>
          ))}
        </div>

        {jobsData?.data?.content.length === 0 && (
          <Card className="text-center py-16 border-0 shadow-lg">
            <CardContent>
              <div className="mb-6">
                <div className="w-20 h-20 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
                  <Search className="w-10 h-10 text-gray-400" />
                </div>
                <h3 className="text-xl font-semibold text-gray-700 mb-2">검색 결과가 없습니다</h3>
                <p className="text-gray-500">다른 키워드나 필터 옵션으로 다시 검색해보세요</p>
              </div>
              <Button variant="outline" onClick={clearFilters}>
                필터 초기화
              </Button>
            </CardContent>
          </Card>
        )}

        {/* 페이지네이션 */}
        {jobsData?.data && jobsData.data.totalPages > 1 && (
          <div className="flex justify-center items-center space-x-4">
            <Button
              variant="outline"
              onClick={() => setPage(Math.max(0, page - 1))}
              disabled={page === 0}
              size="lg"
            >
              이전
            </Button>
            <div className="flex items-center space-x-2">
              <span className="text-sm text-gray-600">
                {page + 1} / {jobsData.data.totalPages}
              </span>
            </div>
            <Button
              variant="outline"
              onClick={() => setPage(Math.min(jobsData.data.totalPages - 1, page + 1))}
              disabled={page >= jobsData.data.totalPages - 1}
              size="lg"
            >
              다음
            </Button>
          </div>
        )}
      </div>

    </div>
  )
}