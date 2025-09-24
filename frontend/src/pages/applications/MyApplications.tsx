import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { Briefcase, Calendar, Eye, AlertCircle, ChevronLeft, ChevronRight } from 'lucide-react'
import { apiClient } from '@/services/api'
import { useAuthStore } from '@/hooks/useAuthStore'
import { ApplicationStatus, JobApplication } from '@/types/api'

export default function MyApplications() {
  const { user } = useAuthStore()
  const [currentPage, setCurrentPage] = useState(0)
  const [pageSize] = useState(20)

  const { data: applicationsData, isLoading } = useQuery({
    queryKey: ['my-applications', user?.id, currentPage],
    queryFn: () => apiClient.getMyApplications(currentPage, pageSize),
    enabled: !!user,
  })

  const getStatusBadge = (status: ApplicationStatus) => {
    const statusConfig = {
      PENDING: { color: 'bg-yellow-100 text-yellow-800', text: '검토 중' },
      REVIEWING: { color: 'bg-blue-100 text-blue-800', text: '서류검토' },
      INTERVIEW: { color: 'bg-purple-100 text-purple-800', text: '면접' },
      APPROVED: { color: 'bg-green-100 text-green-800', text: '합격' },
      ACCEPTED: { color: 'bg-green-100 text-green-800', text: '채용확정' },
      REJECTED: { color: 'bg-red-100 text-red-800', text: '불합격' },
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

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
      </div>
    )
  }

  const applications = applicationsData?.data?.content || []
  const totalPages = applicationsData?.data?.totalPages || 0
  const totalElements = applicationsData?.data?.totalElements || 0

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">지원 현황</h1>
          <p className="text-gray-600 mt-1">총 {totalElements}개의 지원 내역이 있습니다.</p>
        </div>
        <Link
          to="/profile"
          className="btn-outline"
        >
          프로필로 돌아가기
        </Link>
      </div>

      {applications.length > 0 ? (
        <>
          {/* Applications List */}
          <div className="bg-white shadow rounded-lg overflow-hidden">
            <div className="divide-y divide-gray-200">
              {applications.map((application: JobApplication) => (
                <div key={application.id} className="p-6 hover:bg-gray-50">
                  <div className="flex items-start justify-between">
                    <div className="flex-1">
                      <div className="flex items-center space-x-3 mb-2">
                        <Link
                          to={`/jobs/${application.jobPostingId}`}
                          className="text-lg font-semibold text-gray-900 hover:text-primary-600 hover:underline"
                        >
                          {application.jobPosting?.title || '채용공고'}
                        </Link>
                        {getStatusBadge(application.status)}
                      </div>
                      
                      <div className="flex items-center text-sm text-gray-600 space-x-4 mb-3">
                        <span className="flex items-center">
                          <Briefcase className="w-4 h-4 mr-1" />
                          {application.jobPosting?.companyName || '회사명'}
                        </span>
                        <span className="flex items-center">
                          <Calendar className="w-4 h-4 mr-1" />
                          지원일: {application.appliedAt ? formatDate(application.appliedAt[0] + '-' + String(application.appliedAt[1]).padStart(2, '0') + '-' + String(application.appliedAt[2]).padStart(2, '0')) : '날짜 없음'}
                        </span>
                      </div>

                      {application.coverLetter && (
                        <div className="bg-gray-50 rounded-lg p-3 mb-3">
                          <p className="text-sm text-gray-700">
                            <strong>자기소개서:</strong>
                          </p>
                          <p className="text-sm text-gray-600 mt-1 line-clamp-2">
                            {application.coverLetter}
                          </p>
                        </div>
                      )}

                      {application.interviewerNotes && (
                        <div className="bg-blue-50 rounded-lg p-3 mb-3">
                          <p className="text-sm text-blue-700">
                            <strong>면접관 메모:</strong>
                          </p>
                          <p className="text-sm text-blue-600 mt-1">
                            {application.interviewerNotes}
                          </p>
                        </div>
                      )}

                      {application.rejectionReason && (
                        <div className="bg-red-50 rounded-lg p-3">
                          <p className="text-sm text-red-700">
                            <strong>불합격 사유:</strong>
                          </p>
                          <p className="text-sm text-red-600 mt-1">
                            {application.rejectionReason}
                          </p>
                        </div>
                      )}
                    </div>

                    <div className="ml-4 flex-shrink-0">
                      <Link
                        to={`/jobs/${application.jobPostingId}`}
                        className="btn-outline btn-sm"
                      >
                        <Eye className="w-4 h-4 mr-1" />
                        채용공고 보기
                      </Link>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Pagination */}
          {totalPages > 1 && (
            <div className="flex items-center justify-between border-t border-gray-200 bg-white px-4 py-3 sm:px-6 rounded-lg">
              <div className="flex flex-1 justify-between sm:hidden">
                <button
                  onClick={() => setCurrentPage(Math.max(0, currentPage - 1))}
                  disabled={currentPage === 0}
                  className="relative inline-flex items-center rounded-md border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:bg-gray-100 disabled:text-gray-400"
                >
                  이전
                </button>
                <button
                  onClick={() => setCurrentPage(Math.min(totalPages - 1, currentPage + 1))}
                  disabled={currentPage === totalPages - 1}
                  className="relative ml-3 inline-flex items-center rounded-md border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:bg-gray-100 disabled:text-gray-400"
                >
                  다음
                </button>
              </div>
              <div className="hidden sm:flex sm:flex-1 sm:items-center sm:justify-between">
                <div>
                  <p className="text-sm text-gray-700">
                    <span className="font-medium">{currentPage * pageSize + 1}</span>
                    -
                    <span className="font-medium">{Math.min((currentPage + 1) * pageSize, totalElements)}</span>
                    {' '} 중 {' '}
                    <span className="font-medium">{totalElements}</span>
                    개 결과
                  </p>
                </div>
                <div>
                  <nav className="isolate inline-flex -space-x-px rounded-md shadow-sm" aria-label="Pagination">
                    <button
                      onClick={() => setCurrentPage(Math.max(0, currentPage - 1))}
                      disabled={currentPage === 0}
                      className="relative inline-flex items-center rounded-l-md px-2 py-2 text-gray-400 ring-1 ring-inset ring-gray-300 hover:bg-gray-50 focus:z-20 focus:outline-offset-0 disabled:bg-gray-100"
                    >
                      <ChevronLeft className="h-5 w-5" aria-hidden="true" />
                    </button>
                    
                    {Array.from({ length: Math.min(5, totalPages) }, (_, i) => {
                      const pageNum = Math.max(0, Math.min(totalPages - 5, currentPage - 2)) + i
                      return (
                        <button
                          key={pageNum}
                          onClick={() => setCurrentPage(pageNum)}
                          className={`relative inline-flex items-center px-4 py-2 text-sm font-semibold ${
                            pageNum === currentPage
                              ? 'bg-primary-600 text-white focus:z-20 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary-600'
                              : 'text-gray-900 ring-1 ring-inset ring-gray-300 hover:bg-gray-50 focus:z-20 focus:outline-offset-0'
                          }`}
                        >
                          {pageNum + 1}
                        </button>
                      )
                    })}
                    
                    <button
                      onClick={() => setCurrentPage(Math.min(totalPages - 1, currentPage + 1))}
                      disabled={currentPage === totalPages - 1}
                      className="relative inline-flex items-center rounded-r-md px-2 py-2 text-gray-400 ring-1 ring-inset ring-gray-300 hover:bg-gray-50 focus:z-20 focus:outline-offset-0 disabled:bg-gray-100"
                    >
                      <ChevronRight className="h-5 w-5" aria-hidden="true" />
                    </button>
                  </nav>
                </div>
              </div>
            </div>
          )}
        </>
      ) : (
        <div className="text-center py-12 bg-white rounded-lg shadow">
          <AlertCircle className="w-12 h-12 text-gray-400 mx-auto mb-4" />
          <h3 className="text-lg font-medium text-gray-900 mb-2">지원 내역이 없습니다</h3>
          <p className="text-gray-500 mb-6">아직 지원한 채용공고가 없습니다. 관심 있는 채용공고에 지원해보세요!</p>
          <Link
            to="/jobs"
            className="btn-primary"
          >
            채용공고 둘러보기
          </Link>
        </div>
      )}
    </div>
  )
}