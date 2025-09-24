import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { FileText, Download, Clock, CheckCircle, XCircle } from 'lucide-react'
import { apiClient } from '@/services/api'

export default function Certificates() {
  const [requestType, setRequestType] = useState('')
  const [description, setDescription] = useState('')

  const { data: certificateRequests, isLoading, refetch } = useQuery({
    queryKey: ['certificate-requests'],
    queryFn: () => apiClient.getMyCertificateRequests(),
  })

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    
    if (!requestType || !description.trim()) {
      alert('증명서 종류와 사용 용도를 모두 입력해주세요.')
      return
    }

    try {
      const certificateTypeMap: { [key: string]: string } = {
        'enrollment': 'ENROLLMENT_CERTIFICATE',
        'transcript': 'TRANSCRIPT', 
        'completion': 'COMPLETION_CERTIFICATE',
        'course': 'COURSE_COMPLETION_CERTIFICATE',
        'attendance': 'ATTENDANCE_CERTIFICATE'
      }

      await apiClient.createCertificateRequest({
        certificateType: certificateTypeMap[requestType] || requestType,
        purpose: description.trim()
      })

      alert('증명서 요청이 성공적으로 등록되었습니다.')
      setRequestType('')
      setDescription('')
      refetch() // Refresh the list
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || '증명서 요청 중 오류가 발생했습니다.'
      alert(errorMessage)
    }
  }

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'COMPLETED':
        return <CheckCircle className="w-5 h-5 text-green-500" />
      case 'APPROVED':
        return <CheckCircle className="w-5 h-5 text-blue-500" />
      case 'PENDING':
        return <Clock className="w-5 h-5 text-yellow-500" />
      case 'REJECTED':
        return <XCircle className="w-5 h-5 text-red-500" />
      default:
        return <Clock className="w-5 h-5 text-gray-500" />
    }
  }

  const getStatusText = (status: string) => {
    switch (status) {
      case 'COMPLETED':
        return '완료됨'
      case 'APPROVED':
        return '승인됨'
      case 'PENDING':
        return '처리 대기중'
      case 'REJECTED':
        return '거부됨'
      default:
        return '대기중'
    }
  }

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">증명서 발급</h1>
        <p className="text-gray-600">각종 증명서를 온라인으로 신청하고 발급받으세요</p>
      </div>

      {/* Certificate Request Form */}
      <form onSubmit={handleSubmit} className="card">
        <div className="card-header">
          <h2 className="text-lg font-semibold">증명서 신청</h2>
        </div>
        <div className="card-content space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              증명서 종류
            </label>
            <select 
              value={requestType} 
              onChange={(e) => setRequestType(e.target.value)}
              className="input"
              required
            >
              <option value="">증명서를 선택하세요</option>
              <option value="enrollment">재학증명서</option>
              <option value="transcript">성적증명서</option>
              <option value="completion">수료증</option>
              <option value="course">이수증명서</option>
              <option value="attendance">출석증명서</option>
            </select>
          </div>
          
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              사용 용도
            </label>
            <textarea 
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="증명서 사용 용도를 입력하세요"
              className="input min-h-20"
              rows={3}
              required
            />
          </div>

          <button type="submit" className="btn-primary">
            <FileText className="w-4 h-4 mr-2" />
            신청하기
          </button>
        </div>
      </form>

      {/* Certificate Requests History */}
      <div className="card">
        <div className="card-header">
          <h2 className="text-lg font-semibold">신청 내역</h2>
        </div>
        <div className="card-content">
          {certificateRequests?.data?.length > 0 ? (
            <div className="space-y-4">
              {certificateRequests.data.map((request: any) => (
                <div key={request.id} className="flex items-center justify-between p-4 border border-gray-200 rounded-lg">
                  <div className="flex items-center space-x-4">
                    {getStatusIcon(request.status)}
                    <div>
                      <h3 className="font-semibold text-gray-900">
                        {request.certificateTypeDescription || request.certificateType}
                      </h3>
                      <p className="text-sm text-gray-600">{request.purpose}</p>
                      <p className="text-xs text-gray-500">
                        신청일: {new Date(request.createdAt).toLocaleDateString('ko-KR')}
                      </p>
                      {request.adminNotes && (
                        <p className="text-xs text-gray-500">관리자 메모: {request.adminNotes}</p>
                      )}
                    </div>
                  </div>
                  
                  <div className="flex items-center space-x-4">
                    <span className={`px-3 py-1 rounded-full text-sm font-medium ${
                      request.status === 'COMPLETED' 
                        ? 'bg-green-100 text-green-800'
                        : request.status === 'APPROVED'
                        ? 'bg-blue-100 text-blue-800'
                        : request.status === 'PENDING'
                        ? 'bg-yellow-100 text-yellow-800'
                        : 'bg-red-100 text-red-800'
                    }`}>
                      {request.statusDescription || getStatusText(request.status)}
                    </span>
                    
                    {request.status === 'COMPLETED' && (
                      <button className="btn-outline btn-sm">
                        <Download className="w-4 h-4 mr-1" />
                        다운로드
                      </button>
                    )}
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="text-center text-gray-500 py-8">
              아직 신청한 증명서가 없습니다
            </div>
          )}
        </div>
      </div>
    </div>
  )
}