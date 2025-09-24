import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import {
  Users,
  Calendar,
  Eye,
  AlertCircle,
  ChevronLeft,
  ChevronRight,
  Briefcase,
  MapPin,
  Clock
} from 'lucide-react'
import { apiClient } from '@/services/api'
import { useAuthStore } from '@/hooks/useAuthStore'
import { ApplicationStatus } from '@/types/api'
import { JobApplicationResponse } from '@/types/jobApplication'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/Card'
import { Button } from '@/components/ui/Button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'

export default function ApplicantManagement() {
  const { user } = useAuthStore()
  const queryClient = useQueryClient()
  const [currentPage, setCurrentPage] = useState(0)
  const [pageSize] = useState(20)
  const [selectedApplication, setSelectedApplication] = useState<JobApplicationResponse | null>(null)
  const [isDetailModalOpen, setIsDetailModalOpen] = useState(false)

  // 회사의 채용공고 목록 조회
  const { data: jobPostingsData, isLoading: jobPostingsLoading } = useQuery({
    queryKey: ['my-job-postings', user?.id],
    queryFn: () => apiClient.getMyJobPostings(0, 100), // 모든 채용공고 가져오기
    enabled: !!user,
  })

  // 모든 지원자 데이터 조회
  const { data: applicantsData, isLoading: applicantsLoading } = useQuery({
    queryKey: ['company-applicants', user?.id, currentPage],
    queryFn: async () => {
      if (!jobPostingsData?.data || !Array.isArray(jobPostingsData.data)) return { content: [], totalElements: 0 }

      let allApplications: JobApplicationResponse[] = []

      for (const jobPosting of jobPostingsData.data) {
        try {
          const applicationsResponse = await apiClient.getJobApplications(jobPosting.id, 0, 100)
          if (applicationsResponse.data?.content) {
            allApplications = [...allApplications, ...applicationsResponse.data.content]
          }
        } catch (error) {
          console.warn(`Failed to fetch applications for job posting ${jobPosting.id}:`, error)
        }
      }

      // 지원일 기준 최신순 정렬
      allApplications.sort((a, b) => new Date(b.appliedAt).getTime() - new Date(a.appliedAt).getTime())

      // 페이지네이션 적용
      const startIndex = currentPage * pageSize
      const endIndex = startIndex + pageSize
      const paginatedApplications = allApplications.slice(startIndex, endIndex)

      return {
        content: paginatedApplications,
        totalElements: allApplications.length
      }
    },
    enabled: !!jobPostingsData?.data && Array.isArray(jobPostingsData.data),
  })

  // 서류 합격 처리 mutation
  const passDocumentMutation = useMutation({
    mutationFn: (applicationId: number) => apiClient.passDocumentReview(applicationId),
    onSuccess: (response) => {
      // 성공 시 데이터 새로고침
      queryClient.invalidateQueries({ queryKey: ['company-applicants'] })
      setIsDetailModalOpen(false)
      alert(response.message || '서류 합격 처리가 완료되었습니다.')
    },
    onError: (error: any) => {
      console.error('서류 합격 처리 실패:', error)
      const errorMessage = error?.response?.data?.message || '서류 합격 처리 중 오류가 발생했습니다.'
      alert(errorMessage)
    }
  })

  // 불합격 처리 mutation
  const rejectApplicationMutation = useMutation({
    mutationFn: ({ applicationId, reason }: { applicationId: number, reason?: string }) =>
      apiClient.rejectApplication(applicationId, reason),
    onSuccess: (response) => {
      // 성공 시 데이터 새로고침
      queryClient.invalidateQueries({ queryKey: ['company-applicants'] })
      setIsDetailModalOpen(false)
      alert(response.message || '불합격 처리가 완료되었습니다.')
    },
    onError: (error: any) => {
      console.error('불합격 처리 실패:', error)
      const errorMessage = error?.response?.data?.message || '불합격 처리 중 오류가 발생했습니다.'
      alert(errorMessage)
    }
  })

  const getStatusBadge = (status: ApplicationStatus) => {
    const statusConfig = {
      SUBMITTED: { color: 'bg-yellow-100 text-yellow-800', text: '지원완료' },
      REVIEWED: { color: 'bg-blue-100 text-blue-800', text: '검토중' },
      DOCUMENT_PASSED: { color: 'bg-green-100 text-green-800', text: '서류통과' },
      INTERVIEW_SCHEDULED: { color: 'bg-purple-100 text-purple-800', text: '면접예정' },
      INTERVIEW_PASSED: { color: 'bg-green-100 text-green-800', text: '면접통과' },
      HIRED: { color: 'bg-green-100 text-green-800', text: '최종합격' },
      REJECTED: { color: 'bg-red-100 text-red-800', text: '불합격' },
      WITHDRAWN: { color: 'bg-gray-100 text-gray-800', text: '지원취소' },
    }

    const config = statusConfig[status] || { color: 'bg-gray-100 text-gray-800', text: status }

    return (
      <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${config.color}`}>
        {config.text}
      </span>
    )
  }

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    })
  }

  if (jobPostingsLoading || applicantsLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
      </div>
    )
  }

  const applications = applicantsData?.content || []
  const totalElements = applicantsData?.totalElements || 0
  const totalPages = Math.ceil(totalElements / pageSize)

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900 mb-2">지원자 관리</h1>
        <p className="text-gray-600">회사의 모든 채용공고에 지원한 지원자들을 관리할 수 있습니다.</p>
      </div>

      {/* 통계 카드 */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
        <Card>
          <CardContent className="p-6">
            <div className="flex items-center">
              <Users className="h-8 w-8 text-blue-500" />
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-500">총 지원자</p>
                <p className="text-2xl font-bold text-gray-900">{totalElements}명</p>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6">
            <div className="flex items-center">
              <Clock className="h-8 w-8 text-yellow-500" />
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-500">검토 중</p>
                <p className="text-2xl font-bold text-gray-900">
                  {applications.filter(app => app.status === 'SUBMITTED').length}명
                </p>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6">
            <div className="flex items-center">
              <Briefcase className="h-8 w-8 text-green-500" />
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-500">합격</p>
                <p className="text-2xl font-bold text-gray-900">
                  {applications.filter(app => app.status === 'HIRED' || app.status === 'INTERVIEW_PASSED' || app.status === 'DOCUMENT_PASSED').length}명
                </p>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6">
            <div className="flex items-center">
              <AlertCircle className="h-8 w-8 text-red-500" />
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-500">불합격</p>
                <p className="text-2xl font-bold text-gray-900">
                  {applications.filter(app => app.status === 'REJECTED').length}명
                </p>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* 지원자 목록 */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Users className="w-5 h-5" />
            지원자 목록
          </CardTitle>
        </CardHeader>
        <CardContent>
          {applications.length === 0 ? (
            <div className="text-center py-12">
              <Users className="mx-auto h-12 w-12 text-gray-400" />
              <h3 className="mt-2 text-sm font-medium text-gray-900">지원자가 없습니다</h3>
              <p className="mt-1 text-sm text-gray-500">
                아직 지원한 사람이 없습니다. 채용공고를 확인해보세요.
              </p>
              <div className="mt-6">
                <Link to="/jobs/create" className="inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-primary-600 hover:bg-primary-700">
                  새 채용공고 작성
                </Link>
              </div>
            </div>
          ) : (
            <>
              <div className="space-y-4">
                {applications.map((application) => (
                  <div key={application.id} className="border border-gray-200 rounded-lg p-4 hover:bg-gray-50 transition-colors">
                    <div className="flex items-start justify-between">
                      <div className="flex-1">
                        <div className="flex items-center gap-3 mb-2">
                          <h3 className="text-lg font-medium text-gray-900">
                            {application.userName || '이름 없음'}
                          </h3>
                          {getStatusBadge(application.status)}
                        </div>

                        <div className="flex items-center gap-4 text-sm text-gray-500 mb-2">
                          <div className="flex items-center gap-1">
                            <Briefcase className="w-4 h-4" />
                            <span>{application.jobPostingTitle || '채용공고'}</span>
                          </div>
                          <div className="flex items-center gap-1">
                            <MapPin className="w-4 h-4" />
                            <span>{application.companyName || '회사명'}</span>
                          </div>
                          <div className="flex items-center gap-1">
                            <Calendar className="w-4 h-4" />
                            <span>지원일: {formatDate(application.appliedAt)}</span>
                          </div>
                        </div>

                        {application.coverLetter && (
                          <p className="text-sm text-gray-600 line-clamp-2">
                            {application.coverLetter}
                          </p>
                        )}
                      </div>

                      <div className="ml-4">
                        <Button
                          variant="outline"
                          size="sm"
                          className="flex items-center gap-2"
                          onClick={() => {
                            setSelectedApplication(application)
                            setIsDetailModalOpen(true)
                          }}
                        >
                          <Eye className="w-4 h-4" />
                          상세보기
                        </Button>
                      </div>
                    </div>
                  </div>
                ))}
              </div>

              {/* 페이지네이션 */}
              {totalPages > 1 && (
                <div className="flex items-center justify-between mt-6">
                  <div className="text-sm text-gray-700">
                    총 {totalElements}명 중 {currentPage * pageSize + 1}-{Math.min((currentPage + 1) * pageSize, totalElements)}명 표시
                  </div>

                  <div className="flex items-center gap-2">
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => setCurrentPage(prev => Math.max(0, prev - 1))}
                      disabled={currentPage === 0}
                    >
                      <ChevronLeft className="w-4 h-4" />
                      이전
                    </Button>

                    <span className="text-sm text-gray-700">
                      {currentPage + 1} / {totalPages}
                    </span>

                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => setCurrentPage(prev => Math.min(totalPages - 1, prev + 1))}
                      disabled={currentPage >= totalPages - 1}
                    >
                      다음
                      <ChevronRight className="w-4 h-4" />
                    </Button>
                  </div>
                </div>
              )}
            </>
          )}
        </CardContent>
      </Card>

      {/* 지원서 상세보기 모달 */}
      <Dialog open={isDetailModalOpen} onOpenChange={setIsDetailModalOpen}>
        <DialogContent className="max-w-3xl max-h-[80vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle className="text-xl font-bold">지원서 상세 정보</DialogTitle>
            <DialogDescription>
              지원자의 상세 정보와 지원 내용을 확인할 수 있습니다.
            </DialogDescription>
          </DialogHeader>

          {selectedApplication && (
            <div className="space-y-6">
              {/* 지원자 정보 섹션 */}
              <div className="bg-gray-50 rounded-lg p-4">
                <h3 className="font-semibold text-gray-900 mb-3 flex items-center gap-2">
                  <Users className="w-5 h-5" />
                  지원자 정보
                </h3>
                <div className="space-y-2">
                  <div className="flex gap-2">
                    <span className="text-gray-500 w-24">이름:</span>
                    <span className="text-gray-900 font-medium">{selectedApplication.userName || '정보 없음'}</span>
                  </div>
                  <div className="flex gap-2">
                    <span className="text-gray-500 w-24">이메일:</span>
                    <span className="text-gray-900">{selectedApplication.userEmail || '정보 없음'}</span>
                  </div>
                  <div className="flex gap-2">
                    <span className="text-gray-500 w-24">지원일:</span>
                    <span className="text-gray-900">{formatDate(selectedApplication.appliedAt)}</span>
                  </div>
                  <div className="flex gap-2">
                    <span className="text-gray-500 w-24">상태:</span>
                    {getStatusBadge(selectedApplication.status)}
                  </div>
                </div>
              </div>

              {/* 채용공고 정보 섹션 */}
              <div className="bg-blue-50 rounded-lg p-4">
                <h3 className="font-semibold text-gray-900 mb-3 flex items-center gap-2">
                  <Briefcase className="w-5 h-5" />
                  채용공고 정보
                </h3>
                <div className="space-y-2">
                  <div className="flex gap-2">
                    <span className="text-gray-500 w-24">직무:</span>
                    <span className="text-gray-900 font-medium">{selectedApplication.jobPostingTitle}</span>
                  </div>
                  <div className="flex gap-2">
                    <span className="text-gray-500 w-24">회사:</span>
                    <span className="text-gray-900">{selectedApplication.companyName}</span>
                  </div>
                </div>
              </div>

              {/* 자기소개서 섹션 */}
              <div className="border rounded-lg p-4">
                <h3 className="font-semibold text-gray-900 mb-3">자기소개서</h3>
                <p className="text-gray-700 whitespace-pre-wrap">
                  {selectedApplication.coverLetter || '자기소개서가 없습니다.'}
                </p>
              </div>

              {/* 액션 버튼들 */}
              <div className="flex gap-3 justify-end pt-4 border-t">
                <Button
                  variant="outline"
                  onClick={() => setIsDetailModalOpen(false)}
                >
                  닫기
                </Button>
                {selectedApplication.status === 'SUBMITTED' && (
                  <>
                    <Button
                      variant="outline"
                      className="text-red-600 hover:bg-red-50"
                      disabled={rejectApplicationMutation.isPending}
                      onClick={() => {
                        if (confirm('이 지원자를 불합격 처리하시겠습니까?')) {
                          rejectApplicationMutation.mutate({
                            applicationId: selectedApplication.id,
                            reason: '서류 심사 불합격'
                          })
                        }
                      }}
                    >
                      {rejectApplicationMutation.isPending ? '처리 중...' : '불합격'}
                    </Button>
                    <Button
                      variant="primary"
                      disabled={passDocumentMutation.isPending}
                      onClick={() => {
                        if (confirm('이 지원자를 서류 합격 처리하시겠습니까?')) {
                          passDocumentMutation.mutate(selectedApplication.id)
                        }
                      }}
                    >
                      {passDocumentMutation.isPending ? '처리 중...' : '서류 합격'}
                    </Button>
                  </>
                )}
              </div>
            </div>
          )}
        </DialogContent>
      </Dialog>
    </div>
  )
}