import { useState, useEffect } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { 
  FileText, 
  Clock, 
  CheckCircle, 
  XCircle, 
  Eye, 
  Filter,
  Search,
  Download,
  User,
  Calendar
} from 'lucide-react'
import { apiClient } from '@/services/api'

interface CertificateRequest {
  id: number
  userEmail: string
  userName: string
  certificateType: string
  certificateTypeDescription: string
  status: string
  statusDescription: string
  purpose: string
  adminNotes?: string
  createdAt: string
  processedAt?: string
}

type StatusFilter = 'ALL' | 'PENDING' | 'APPROVED' | 'REJECTED' | 'COMPLETED'

export default function CertificateManagement() {
  const [isAdmin, setIsAdmin] = useState(false)
  const [statusFilter, setStatusFilter] = useState<StatusFilter>('ALL')

  // Check admin authentication
  useEffect(() => {
    const adminToken = localStorage.getItem('adminToken')
    const isAdminStatus = localStorage.getItem('isAdmin')
    setIsAdmin(adminToken && isAdminStatus === 'true')
  }, [])
  const [searchTerm, setSearchTerm] = useState('')
  const [selectedRequest, setSelectedRequest] = useState<CertificateRequest | null>(null)
  const [showProcessModal, setShowProcessModal] = useState(false)
  const [adminNotes, setProcessNote] = useState('')

  const queryClient = useQueryClient()

  const { data: requests, isLoading } = useQuery({
    queryKey: ['admin-certificates', statusFilter],
    queryFn: async () => {
      const response = await apiClient.getAllCertificateRequests()
      return response.data?.content || []
    },
    enabled: isAdmin
  })

  const processMutation = useMutation({
    mutationFn: async ({ requestId, approved, note }: { requestId: number, approved: boolean, note: string }) => {
      return await apiClient.processCertificateRequest(requestId, { approved, adminNotes: note })
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-certificates'] })
      setShowProcessModal(false)
      setSelectedRequest(null)
      setProcessNote('')
      alert('요청이 처리되었습니다.')
    },
    onError: (error) => {
      alert('처리 중 오류가 발생했습니다: ' + error.message)
    }
  })

  const filteredRequests = requests?.filter(request => {
    const matchesSearch = request.userName.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         request.userEmail.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         request.certificateTypeDescription.toLowerCase().includes(searchTerm.toLowerCase())
    return matchesSearch
  }) || []

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'PENDING':
        return <Clock className="w-4 h-4 text-yellow-500" />
      case 'APPROVED':
      case 'COMPLETED':
        return <CheckCircle className="w-4 h-4 text-green-500" />
      case 'REJECTED':
        return <XCircle className="w-4 h-4 text-red-500" />
      default:
        return <FileText className="w-4 h-4 text-gray-500" />
    }
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'PENDING':
        return 'bg-yellow-100 text-yellow-800'
      case 'APPROVED':
      case 'COMPLETED':
        return 'bg-green-100 text-green-800'
      case 'REJECTED':
        return 'bg-red-100 text-red-800'
      default:
        return 'bg-gray-100 text-gray-800'
    }
  }

  const handleProcess = (request: CertificateRequest, status: 'APPROVED' | 'REJECTED') => {
    setSelectedRequest(request)
    setShowProcessModal(true)
  }

  const handleSubmitProcess = (status: 'APPROVED' | 'REJECTED') => {
    if (!selectedRequest) return
    if (!adminNotes.trim()) {
      alert('처리 메모를 입력해주세요.')
      return
    }

    processMutation.mutate({
      requestId: selectedRequest.id,
      approved: status === 'APPROVED',
      note: adminNotes
    })
  }

  if (!isAdmin) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <FileText className="w-16 h-16 text-red-500 mx-auto mb-4" />
          <h2 className="text-xl font-semibold mb-2">접근 권한이 없습니다</h2>
          <p className="text-gray-600">관리자만 접근할 수 있는 페이지입니다.</p>
        </div>
      </div>
    )
  }

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-96">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500"></div>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">증명서 관리</h1>
        <p className="text-gray-600">증명서 요청을 검토하고 승인/거부할 수 있습니다</p>
      </div>

      {/* 필터 및 검색 */}
      <div className="card">
        <div className="card-content">
          <div className="flex flex-col md:flex-row md:items-center md:justify-between space-y-4 md:space-y-0">
            <div className="flex items-center space-x-4">
              <div className="flex items-center space-x-2">
                <Filter className="w-4 h-4 text-gray-500" />
                <select
                  value={statusFilter}
                  onChange={(e) => setStatusFilter(e.target.value as StatusFilter)}
                  className="input text-sm"
                >
                  <option value="ALL">전체</option>
                  <option value="PENDING">대기중</option>
                  <option value="APPROVED">승인됨</option>
                  <option value="REJECTED">거부됨</option>
                  <option value="COMPLETED">완료됨</option>
                </select>
              </div>
            </div>

            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-gray-400" />
              <input
                type="text"
                placeholder="이름, 이메일, 증명서 타입으로 검색..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="input pl-10 w-full md:w-80"
              />
            </div>
          </div>
        </div>
      </div>

      {/* 요청 목록 */}
      <div className="card">
        <div className="card-content">
          <div className="overflow-x-auto">
            <table className="min-w-full">
              <thead>
                <tr className="border-b border-gray-200">
                  <th className="text-left py-3 px-4 font-medium text-gray-600">요청자</th>
                  <th className="text-left py-3 px-4 font-medium text-gray-600">증명서 타입</th>
                  <th className="text-left py-3 px-4 font-medium text-gray-600">용도</th>
                  <th className="text-left py-3 px-4 font-medium text-gray-600">요청일</th>
                  <th className="text-left py-3 px-4 font-medium text-gray-600">상태</th>
                  <th className="text-left py-3 px-4 font-medium text-gray-600">액션</th>
                </tr>
              </thead>
              <tbody>
                {filteredRequests.map((request) => (
                  <tr key={request.id} className="border-b border-gray-100 hover:bg-gray-50">
                    <td className="py-3 px-4">
                      <div className="flex items-center space-x-3">
                        <div className="w-8 h-8 bg-blue-100 rounded-full flex items-center justify-center">
                          <User className="w-4 h-4 text-blue-600" />
                        </div>
                        <div>
                          <p className="font-medium text-gray-900">{request.userName}</p>
                          <p className="text-sm text-gray-500">{request.userEmail}</p>
                        </div>
                      </div>
                    </td>
                    <td className="py-3 px-4">
                      <span className="font-medium text-gray-900">{request.certificateTypeDescription}</span>
                    </td>
                    <td className="py-3 px-4">
                      <span className="text-gray-600 text-sm">{request.purpose}</span>
                    </td>
                    <td className="py-3 px-4">
                      <div className="flex items-center space-x-2 text-sm text-gray-500">
                        <Calendar className="w-4 h-4" />
                        <span>{new Date(request.createdAt).toLocaleDateString('ko-KR')}</span>
                      </div>
                    </td>
                    <td className="py-3 px-4">
                      <div className="flex items-center space-x-2">
                        {getStatusIcon(request.status)}
                        <span className={`px-2 py-1 rounded-full text-xs font-medium ${getStatusColor(request.status)}`}>
                          {request.statusDescription}
                        </span>
                      </div>
                    </td>
                    <td className="py-3 px-4">
                      <div className="flex items-center space-x-2">
                        <button
                          onClick={() => setSelectedRequest(request)}
                          className="p-1 text-blue-600 hover:text-blue-800"
                          title="상세보기"
                        >
                          <Eye className="w-4 h-4" />
                        </button>
                        
                        {request.status === 'PENDING' && (
                          <>
                            <button
                              onClick={() => handleProcess(request, 'APPROVED')}
                              className="p-1 text-green-600 hover:text-green-800"
                              title="승인"
                            >
                              <CheckCircle className="w-4 h-4" />
                            </button>
                            <button
                              onClick={() => handleProcess(request, 'REJECTED')}
                              className="p-1 text-red-600 hover:text-red-800"
                              title="거부"
                            >
                              <XCircle className="w-4 h-4" />
                            </button>
                          </>
                        )}

                        {request.status === 'COMPLETED' && (
                          <button
                            onClick={() => window.open(`/api/certificates/requests/${request.id}/download`)}
                            className="p-1 text-purple-600 hover:text-purple-800"
                            title="다운로드"
                          >
                            <Download className="w-4 h-4" />
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>

            {filteredRequests.length === 0 && (
              <div className="text-center py-12">
                <FileText className="w-12 h-12 text-gray-400 mx-auto mb-4" />
                <p className="text-gray-500">조건에 맞는 증명서 요청이 없습니다.</p>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* 처리 모달 */}
      {showProcessModal && selectedRequest && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg max-w-md w-full mx-4">
            <div className="p-6">
              <h3 className="text-lg font-semibold mb-4">증명서 요청 처리</h3>
              
              <div className="space-y-4 mb-6">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">요청자</label>
                  <p className="text-sm text-gray-900">{selectedRequest.userName} ({selectedRequest.userEmail})</p>
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">증명서 타입</label>
                  <p className="text-sm text-gray-900">{selectedRequest.certificateTypeDescription}</p>
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">용도</label>
                  <p className="text-sm text-gray-900">{selectedRequest.purpose}</p>
                </div>

                {selectedRequest.adminNotes && (
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">추가 정보</label>
                    <p className="text-sm text-gray-900">{selectedRequest.adminNotes}</p>
                  </div>
                )}
              </div>

              <div className="mb-6">
                <label className="block text-sm font-medium text-gray-700 mb-2">처리 메모</label>
                <textarea
                  value={adminNotes}
                  onChange={(e) => setProcessNote(e.target.value)}
                  placeholder="처리 사유나 메모를 입력하세요..."
                  className="input w-full h-24 resize-none"
                  required
                />
              </div>

              <div className="flex space-x-3">
                <button
                  onClick={() => handleSubmitProcess('APPROVED')}
                  disabled={processMutation.isPending}
                  className="flex-1 btn-primary bg-green-600 hover:bg-green-700"
                >
                  승인
                </button>
                <button
                  onClick={() => handleSubmitProcess('REJECTED')}
                  disabled={processMutation.isPending}
                  className="flex-1 btn-primary bg-red-600 hover:bg-red-700"
                >
                  거부
                </button>
                <button
                  onClick={() => {
                    setShowProcessModal(false)
                    setSelectedRequest(null)
                    setProcessNote('')
                  }}
                  className="flex-1 btn-outline"
                >
                  취소
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* 상세보기 모달 */}
      {selectedRequest && !showProcessModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg max-w-lg w-full mx-4">
            <div className="p-6">
              <h3 className="text-lg font-semibold mb-4">증명서 요청 상세</h3>
              
              <div className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">요청자</label>
                  <p className="text-sm text-gray-900">{selectedRequest.userName} ({selectedRequest.userEmail})</p>
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">증명서 타입</label>
                  <p className="text-sm text-gray-900">{selectedRequest.certificateTypeDescription}</p>
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">용도</label>
                  <p className="text-sm text-gray-900">{selectedRequest.purpose}</p>
                </div>

                {selectedRequest.adminNotes && (
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">추가 정보</label>
                    <p className="text-sm text-gray-900">{selectedRequest.adminNotes}</p>
                  </div>
                )}

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">요청일</label>
                  <p className="text-sm text-gray-900">{new Date(selectedRequest.createdAt).toLocaleString('ko-KR')}</p>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">상태</label>
                  <div className="flex items-center space-x-2">
                    {getStatusIcon(selectedRequest.status)}
                    <span className={`px-2 py-1 rounded-full text-xs font-medium ${getStatusColor(selectedRequest.status)}`}>
                      {selectedRequest.statusDescription}
                    </span>
                  </div>
                </div>

                {selectedRequest.processedAt && (
                  <>
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">처리일</label>
                      <p className="text-sm text-gray-900">{new Date(selectedRequest.processedAt).toLocaleString('ko-KR')}</p>
                    </div>

                    {selectedRequest.adminNotes && (
                      <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">처리자</label>
                        <p className="text-sm text-gray-900">{selectedRequest.adminNotes}</p>
                      </div>
                    )}

                    {selectedRequest.adminNotes && (
                      <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">처리 메모</label>
                        <p className="text-sm text-gray-900">{selectedRequest.adminNotes}</p>
                      </div>
                    )}
                  </>
                )}
              </div>

              <div className="mt-6 flex justify-end space-x-3">
                {selectedRequest.status === 'COMPLETED' && (
                  <button
                    onClick={() => window.open(`/api/certificates/requests/${selectedRequest.id}/download`)}
                    className="btn-primary bg-purple-600 hover:bg-purple-700"
                  >
                    <Download className="w-4 h-4 mr-2" />
                    다운로드
                  </button>
                )}
                <button
                  onClick={() => setSelectedRequest(null)}
                  className="btn-outline"
                >
                  닫기
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}