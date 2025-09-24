import React from 'react'
import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { AlertTriangle, Calendar, Clock, MapPin, Building2 } from 'lucide-react'
import { apiClient } from '@/services/api'
import { JobPosting, JobType, ExperienceLevel } from '@/types/api'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/Card'
import { Badge } from '@/components/ui/Badge'

interface DeadlineApproachingJobsProps {
  days?: number
  maxItems?: number
  showForCompany?: boolean
}

// 백엔드에서 오는 배열 형태의 날짜를 파싱하는 유틸 함수
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

const getDaysUntilDeadline = (deadlineDate: number[] | string | null): number => {
  const deadline = parseDate(deadlineDate)
  if (!deadline) return 0

  const now = new Date()
  const diffTime = deadline.getTime() - now.getTime()
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
  return Math.max(0, diffDays)
}

export default function DeadlineApproachingJobs({
  days = 7,
  maxItems = 5,
  showForCompany = false
}: DeadlineApproachingJobsProps) {
  const { data: jobsData, isLoading } = useQuery({
    queryKey: ['deadlineApproachingJobs', days, showForCompany],
    queryFn: () => {
      if (showForCompany) {
        return apiClient.getMyDeadlineApproachingJobs(days)
      }
      return apiClient.getDeadlineApproachingJobs(days)
    },
  })

  if (isLoading) {
    return (
      <Card className="border-orange-200 bg-orange-50">
        <CardContent className="p-4">
          <div className="animate-pulse">
            <div className="h-4 bg-orange-200 rounded w-1/2 mb-2"></div>
            <div className="h-3 bg-orange-200 rounded w-full"></div>
          </div>
        </CardContent>
      </Card>
    )
  }

  const jobs = jobsData?.data || []
  const displayJobs = jobs.slice(0, maxItems)

  if (displayJobs.length === 0) {
    return null
  }

  return (
    <Card className="border-orange-200 bg-orange-50">
      <CardHeader className="pb-3">
        <CardTitle className="text-lg flex items-center text-orange-800">
          <AlertTriangle className="w-5 h-5 mr-2" />
          🚨 마감 임박 채용공고 ({days}일 이내)
        </CardTitle>
        <p className="text-sm text-orange-700">
          {showForCompany ? '내 채용공고 중 ' : ''}{displayJobs.length}개의 공고가 마감일이 다가오고 있습니다
        </p>
      </CardHeader>
      <CardContent className="pt-0">
        <div className="space-y-3">
          {displayJobs.map((job) => {
            const daysLeft = getDaysUntilDeadline(job.deadlineDate)
            return (
              <Link
                key={job.id}
                to={`/jobs/${job.id}`}
                className="block"
              >
                <div className="bg-white p-4 rounded-lg border border-orange-200 hover:border-orange-300 hover:shadow-md transition-all">
                  <div className="flex justify-between items-start mb-2">
                    <div className="flex-1">
                      <h4 className="font-semibold text-gray-900 hover:text-orange-700 transition-colors">
                        {job.title}
                      </h4>
                      <div className="flex items-center text-sm text-gray-600 mt-1">
                        <Building2 className="w-4 h-4 mr-1" />
                        <span className="mr-3">{job.companyName}</span>
                        <MapPin className="w-4 h-4 mr-1" />
                        <span>{job.location}</span>
                      </div>
                    </div>
                    <div className="flex flex-col items-end gap-1">
                      <Badge
                        variant={daysLeft <= 3 ? 'destructive' : daysLeft <= 7 ? 'warning' : 'secondary'}
                        size="sm"
                      >
                        <Clock className="w-3 h-3 mr-1" />
                        {daysLeft === 0 ? '오늘 마감' : `${daysLeft}일 남음`}
                      </Badge>
                      <Badge variant="outline" size="sm">
                        {getExperienceLevelLabel(job.experienceLevel)}
                      </Badge>
                    </div>
                  </div>

                  <div className="flex items-center justify-between text-xs text-gray-500">
                    <div className="flex items-center">
                      <Calendar className="w-3 h-3 mr-1" />
                      <span>
                        마감일: {job.deadlineDate ? parseDate(job.deadlineDate)?.toLocaleDateString('ko-KR') : '상시모집'}
                      </span>
                    </div>
                    <div className="flex items-center gap-2">
                      {job.viewCount !== undefined && (
                        <span>조회 {job.viewCount}회</span>
                      )}
                      {job.applicationCount !== undefined && (
                        <span>지원 {job.applicationCount}명</span>
                      )}
                    </div>
                  </div>
                </div>
              </Link>
            )
          })}
        </div>

        {jobs.length > maxItems && (
          <div className="mt-3 text-center">
            <Link
              to={showForCompany ? "/jobs/my-postings" : "/jobs"}
              className="text-sm text-orange-700 hover:text-orange-800 font-medium"
            >
              전체 {jobs.length}개 공고 보기 →
            </Link>
          </div>
        )}
      </CardContent>
    </Card>
  )
}