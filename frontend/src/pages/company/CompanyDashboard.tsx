/**
 * CompanyDashboard.tsx - 통합 기업 대시보드 컴포넌트
 *
 * 🔧 사용 기술:
 * - React 18 (함수형 컴포넌트, Hooks)
 * - TypeScript (타입 안전성)
 * - React Query (데이터 페칭 및 캐싱)
 * - Zustand (상태 관리)
 * - Tailwind CSS (스타일링, 다크모드)
 * - Lucide React (아이콘)
 *
 * 📋 주요 기능:
 * - 기업 종합 통계 대시보드
 * - 실시간 지원자 현황
 * - 채용공고 관리 요약
 * - 최근 활동 및 알림
 * - 빠른 액션 버튼들
 *
 * 🎯 개선사항:
 * - 기존 CompanyProfile과 MyJobPostings의 중복 로직 통합
 * - 성능 최적화 (React Query 활용)
 * - 사용자 경험 개선 (실시간 업데이트)
 * - 반응형 디자인 강화
 */

import React, { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Link, useNavigate } from 'react-router-dom'
import {
  Building2,
  Users,
  Eye,
  TrendingUp,
  Calendar,
  Bell,
  Plus,
  FileText,
  UserCheck,
  Clock,
  AlertTriangle,
  BarChart3,
  Briefcase,
  MapPin,
  DollarSign,
  Edit3,
  Settings
} from 'lucide-react'

import { useAuthStore } from '@/hooks/useAuthStore'
import { apiClient } from '@/services/api'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/Card'
import { formatEstablishmentDate, formatDisplayDate } from '@/utils/dateUtils'
import { Button } from '@/components/ui/Button'
import { Badge } from '@/components/ui/Badge'

// 유틸리티 함수들
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

const formatDate = (dateInput: any): string => {
  const date = parseDate(dateInput);
  return date ? date.toLocaleDateString('ko-KR') : '날짜 없음';
};

const getStatusBadgeColor = (status: string) => {
  const statusMap: { [key: string]: string } = {
    'SUBMITTED': 'bg-blue-100 text-blue-800',
    'REVIEWED': 'bg-yellow-100 text-yellow-800',
    'DOCUMENT_PASSED': 'bg-green-100 text-green-800',
    'INTERVIEW_SCHEDULED': 'bg-purple-100 text-purple-800',
    'INTERVIEW_PASSED': 'bg-green-100 text-green-800',
    'HIRED': 'bg-green-100 text-green-800',
    'REJECTED': 'bg-red-100 text-red-800',
    'WITHDRAWN': 'bg-gray-100 text-gray-800'
  };
  return statusMap[status] || 'bg-gray-100 text-gray-800';
};

const getStatusLabel = (status: string) => {
  const labelMap: { [key: string]: string } = {
    'SUBMITTED': '지원완료',
    'REVIEWED': '검토중',
    'DOCUMENT_PASSED': '서류통과',
    'INTERVIEW_SCHEDULED': '면접예정',
    'INTERVIEW_PASSED': '면접통과',
    'HIRED': '최종합격',
    'REJECTED': '불합격',
    'WITHDRAWN': '지원취소'
  };
  return labelMap[status] || status;
};

export default function CompanyDashboard() {
  const { user } = useAuthStore()
  const navigate = useNavigate()

  // 기업 프로필 조회
  const { data: companyProfileData, isLoading: profileLoading } = useQuery({
    queryKey: ['company-profile'],
    queryFn: () => apiClient.getCompanyProfile(),
    enabled: !!user && user.userType === 'COMPANY',
  })

  // 채용공고 목록 조회
  const { data: jobPostingsData, isLoading: jobPostingsLoading } = useQuery({
    queryKey: ['my-job-postings'],
    queryFn: () => apiClient.getMyJobPostings(),
    enabled: !!user,
  })

  // 지원자 현황 조회 (기존 MyApplications API 활용)
  const { data: applicationsData, isLoading: applicationsLoading } = useQuery({
    queryKey: ['my-applications'],
    queryFn: () => apiClient.getMyApplications(),
    enabled: !!user,
  })

  const companyProfile = companyProfileData?.data || {}
  const jobPostings = jobPostingsData?.data?.content || []
  const applications = applicationsData?.data || []

  // 통계 계산
  const stats = {
    totalJobPostings: jobPostings.length,
    activeJobPostings: jobPostings.filter(job => !job.isDeleted).length,
    totalApplications: applications.length,
    newApplicationsToday: applications.filter(app => {
      const today = new Date().toDateString();
      const appDate = parseDate(app.appliedAt);
      return appDate && appDate.toDateString() === today;
    }).length,
    pendingReview: applications.filter(app =>
      app.status === 'SUBMITTED' || app.status === 'REVIEWED'
    ).length,
    interviewScheduled: applications.filter(app =>
      app.status === 'INTERVIEW_SCHEDULED'
    ).length,
    hired: applications.filter(app => app.status === 'HIRED').length,
  }

  // 최근 지원자 (최근 5명)
  const recentApplicants = applications
    .sort((a, b) => {
      const dateA = parseDate(a.appliedAt);
      const dateB = parseDate(b.appliedAt);
      if (!dateA || !dateB) return 0;
      return dateB.getTime() - dateA.getTime();
    })
    .slice(0, 5);

  // 최근 채용공고 (최근 3개)
  const recentJobPostings = jobPostings
    .sort((a, b) => {
      const dateA = parseDate(a.createdAt);
      const dateB = parseDate(b.createdAt);
      if (!dateA || !dateB) return 0;
      return dateB.getTime() - dateA.getTime();
    })
    .slice(0, 3);

  // 마감 임박 공고
  const deadlineApproachingJobs = jobPostings.filter(job => {
    if (!job.deadline) return false;
    const deadline = parseDate(job.deadline);
    if (!deadline) return false;
    const now = new Date();
    const diffTime = deadline.getTime() - now.getTime();
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    return diffDays <= 7 && diffDays >= 0;
  });

  if (profileLoading || jobPostingsLoading || applicationsLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-4 border-primary-200 border-t-primary-600 shadow-lg"></div>
      </div>
    )
  }

  return (
    <div className="max-w-7xl mx-auto space-y-8 p-6">
      {/* 헤더 */}
      <div className="flex items-center justify-between">
        <div className="flex items-center">
          <div className="p-3 bg-gradient-to-r from-blue-500 to-purple-600 rounded-xl shadow-lg mr-4">
            <Building2 className="w-8 h-8 text-white" />
          </div>
          <div>
            <h1 className="text-4xl font-bold text-gray-900 dark:text-gray-100">
              {companyProfile?.companyName || '기업 대시보드'}
            </h1>
            <p className="text-xl text-gray-600 dark:text-gray-400 mt-2">
              채용 현황을 한눈에 확인하고 관리하세요
            </p>
          </div>
        </div>
        <div className="flex items-center space-x-3">
          <Button
            onClick={() => navigate('/jobs/create')}
            className="flex items-center gap-2"
            size="lg"
          >
            <Plus className="w-5 h-5" />
            새 채용공고
          </Button>
          <Button
            onClick={() => navigate('/company/profile')}
            variant="outline"
            className="flex items-center gap-2"
          >
            <Edit3 className="w-5 h-5" />
            프로필 편집
          </Button>
        </div>
      </div>

      {/* 알림 영역 */}
      {(stats.newApplicationsToday > 0 || deadlineApproachingJobs.length > 0) && (
        <Card className="border-orange-200 bg-orange-50 dark:bg-orange-900/20">
          <CardContent className="p-4">
            <div className="flex items-center space-x-4">
              <Bell className="w-6 h-6 text-orange-600" />
              <div className="flex-1">
                <h3 className="font-semibold text-orange-800 dark:text-orange-200">알림</h3>
                <div className="space-y-1 mt-2">
                  {stats.newApplicationsToday > 0 && (
                    <p className="text-sm text-orange-700 dark:text-orange-300">
                      • 오늘 {stats.newApplicationsToday}명의 새로운 지원자가 있습니다
                    </p>
                  )}
                  {deadlineApproachingJobs.length > 0 && (
                    <p className="text-sm text-orange-700 dark:text-orange-300">
                      • {deadlineApproachingJobs.length}개의 채용공고가 마감 임박입니다
                    </p>
                  )}
                </div>
              </div>
            </div>
          </CardContent>
        </Card>
      )}

      {/* 통계 대시보드 */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <Card className="border-blue-200 bg-blue-50 dark:bg-blue-900/20">
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-blue-600 dark:text-blue-400">전체 공고</p>
                <p className="text-3xl font-bold text-blue-900 dark:text-blue-100">{stats.totalJobPostings}</p>
                <p className="text-xs text-blue-700 dark:text-blue-300 mt-1">
                  활성: {stats.activeJobPostings}개
                </p>
              </div>
              <Briefcase className="w-8 h-8 text-blue-500" />
            </div>
          </CardContent>
        </Card>

        <Card className="border-green-200 bg-green-50 dark:bg-green-900/20">
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-green-600 dark:text-green-400">총 지원자</p>
                <p className="text-3xl font-bold text-green-900 dark:text-green-100">{stats.totalApplications}</p>
                <p className="text-xs text-green-700 dark:text-green-300 mt-1">
                  오늘: +{stats.newApplicationsToday}명
                </p>
              </div>
              <Users className="w-8 h-8 text-green-500" />
            </div>
          </CardContent>
        </Card>

        <Card className="border-purple-200 bg-purple-50 dark:bg-purple-900/20">
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-purple-600 dark:text-purple-400">검토 대기</p>
                <p className="text-3xl font-bold text-purple-900 dark:text-purple-100">{stats.pendingReview}</p>
                <p className="text-xs text-purple-700 dark:text-purple-300 mt-1">
                  면접예정: {stats.interviewScheduled}명
                </p>
              </div>
              <UserCheck className="w-8 h-8 text-purple-500" />
            </div>
          </CardContent>
        </Card>

        <Card className="border-orange-200 bg-orange-50 dark:bg-orange-900/20">
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-orange-600 dark:text-orange-400">채용 완료</p>
                <p className="text-3xl font-bold text-orange-900 dark:text-orange-100">{stats.hired}</p>
                <p className="text-xs text-orange-700 dark:text-orange-300 mt-1">
                  성공률: {stats.totalApplications > 0 ? Math.round((stats.hired / stats.totalApplications) * 100) : 0}%
                </p>
              </div>
              <TrendingUp className="w-8 h-8 text-orange-500" />
            </div>
          </CardContent>
        </Card>
      </div>

      {/* 메인 콘텐츠 영역 */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* 왼쪽: 최근 지원자 */}
        <div className="lg:col-span-2 space-y-6">
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Clock className="w-5 h-5" />
                최근 지원자
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                {recentApplicants.length > 0 ? (
                  recentApplicants.map((applicant: any) => (
                    <div key={applicant.id} className="flex items-center justify-between p-4 border border-gray-200 dark:border-gray-700 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-800 transition-colors">
                      <div className="flex-1">
                        <div className="flex items-center justify-between mb-2">
                          <h4 className="font-medium text-gray-900 dark:text-gray-100">
                            {applicant.jobPosting?.title || '채용공고'}
                          </h4>
                          <Badge className={getStatusBadgeColor(applicant.status)}>
                            {getStatusLabel(applicant.status)}
                          </Badge>
                        </div>
                        <p className="text-sm text-gray-600 dark:text-gray-400">
                          지원자: {applicant.applicant?.name || '이름 없음'}
                        </p>
                        <p className="text-xs text-gray-500 dark:text-gray-500 mt-1">
                          지원일: {formatDate(applicant.appliedAt)}
                        </p>
                      </div>
                      <div className="ml-4">
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => navigate(`/applications/${applicant.id}`)}
                        >
                          <Eye className="w-4 h-4" />
                        </Button>
                      </div>
                    </div>
                  ))
                ) : (
                  <div className="text-center py-8 text-gray-500 dark:text-gray-400">
                    아직 지원자가 없습니다.
                  </div>
                )}
                <div className="text-center pt-4">
                  <Link
                    to="/applications"
                    className="text-primary-600 hover:text-primary-500 text-sm font-medium"
                  >
                    전체 지원자 보기 →
                  </Link>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* 오른쪽: 채용공고 요약 */}
        <div className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <FileText className="w-5 h-5" />
                최근 채용공고
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                {recentJobPostings.length > 0 ? (
                  recentJobPostings.map((job: any) => (
                    <Link
                      key={job.id}
                      to={`/jobs/${job.id}`}
                      className="block p-3 border border-gray-200 dark:border-gray-700 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-800 hover:border-primary-300 transition-all cursor-pointer"
                    >
                      <div className="flex justify-between items-start mb-2">
                        <h4 className="font-medium text-gray-900 dark:text-gray-100 hover:text-primary-600 text-sm">
                          {job.title}
                        </h4>
                        <Badge variant="success" size="sm">진행중</Badge>
                      </div>
                      <div className="space-y-1">
                        <p className="text-xs text-gray-600 dark:text-gray-400">
                          조회: {job.viewCount || 0} • 지원: {job.applicationCount || 0}명
                        </p>
                        <p className="text-xs text-gray-500 dark:text-gray-500">
                          등록: {formatDate(job.createdAt)}
                        </p>
                      </div>
                    </Link>
                  ))
                ) : (
                  <div className="text-center py-4 text-gray-500 dark:text-gray-400 text-sm">
                    등록된 채용공고가 없습니다.
                  </div>
                )}
                <Button
                  onClick={() => navigate('/jobs')}
                  variant="outline"
                  className="w-full"
                  size="sm"
                >
                  채용공고 관리
                </Button>
              </div>
            </CardContent>
          </Card>

          {/* 빠른 액션 */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Settings className="w-5 h-5" />
                빠른 액션
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-3">
                <Button
                  onClick={() => navigate('/jobs/create')}
                  className="w-full flex items-center gap-2"
                >
                  <Plus className="w-4 h-4" />
                  새 채용공고 작성
                </Button>
                <Button
                  onClick={() => navigate('/applications')}
                  variant="outline"
                  className="w-full flex items-center gap-2"
                >
                  <Users className="w-4 h-4" />
                  지원자 관리
                </Button>
                <Button
                  onClick={() => navigate('/company/profile')}
                  variant="outline"
                  className="w-full flex items-center gap-2"
                >
                  <Building2 className="w-4 h-4" />
                  기업 정보 수정
                </Button>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>

      {/* 마감 임박 공고 알림 */}
      {deadlineApproachingJobs.length > 0 && (
        <Card className="border-red-200 bg-red-50 dark:bg-red-900/20">
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-red-800 dark:text-red-200">
              <AlertTriangle className="w-5 h-5" />
              마감 임박 채용공고
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-3">
              {deadlineApproachingJobs.map((job: any) => {
                const deadline = parseDate(job.deadline);
                const daysLeft = deadline ? Math.ceil((deadline.getTime() - new Date().getTime()) / (1000 * 60 * 60 * 24)) : 0;

                return (
                  <div key={job.id} className="flex items-center justify-between p-3 bg-white dark:bg-gray-800 rounded-lg">
                    <div>
                      <h4 className="font-medium text-gray-900 dark:text-gray-100">{job.title}</h4>
                      <p className="text-sm text-red-600 dark:text-red-400">
                        {daysLeft === 0 ? '오늘 마감' : `${daysLeft}일 후 마감`}
                      </p>
                    </div>
                    <Button
                      onClick={() => navigate(`/jobs/edit/${job.id}`)}
                      size="sm"
                      variant="outline"
                    >
                      수정
                    </Button>
                  </div>
                );
              })}
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  )
}