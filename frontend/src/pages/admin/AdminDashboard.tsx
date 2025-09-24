import { useState, useEffect } from 'react'
import { useQuery } from '@tanstack/react-query'
import { 
  Users, 
  FileText, 
  Briefcase, 
  MessageSquare,
  TrendingUp,
  AlertCircle,
  CheckCircle,
  Clock,
  Bot,
  Image,
  Languages,
  Brain
} from 'lucide-react'
import { apiClient } from '@/services/api'
import { useAuthStore } from '@/hooks/useAuthStore'

interface AdminDashboardData {
  userStatistics: {
    totalUsers: number
    generalUsers: number
    companyUsers: number
    adminUsers: number
    activeUsers: number
  }
  newUserStatistics: {
    todayNewUsers: number
    thisWeekNewUsers: number
    thisMonthNewUsers: number
  }
  jobPostingStatistics: {
    totalJobPostings: number
    activeJobPostings: number
    thisWeekJobPostings: number
  }
  certificateRequests: {
    id: number
    userEmail: string
    userName: string
    certificateType: string
    requestedAt: string
    status: string
  }[]
  aiServiceStatistics: {
    totalInterviews: number
    totalCoverLetters: number
    totalTranslations: number
    totalImageGenerations: number
    totalChatInteractions: number
    totalSentimentAnalyses: number
  }
}

export default function AdminDashboard() {
  const [isAdmin, setIsAdmin] = useState(false)

  useEffect(() => {
    const checkAdminAuth = () => {
      const adminToken = localStorage.getItem('adminToken')
      const isAdminStatus = localStorage.getItem('isAdmin')

      if (adminToken && isAdminStatus === 'true') {
        setIsAdmin(true)
      } else {
        window.location.href = '/adminlogin'
      }
    }

    checkAdminAuth()
  }, [])

  const { data: dashboardData, isLoading } = useQuery({
    queryKey: ['admin-dashboard'],
    queryFn: async () => {
      // Set admin token in headers before making the request
      const adminToken = localStorage.getItem('adminToken')
      if (adminToken) {
        apiClient.setAuthToken(adminToken)
      }
      const response = await apiClient.getAdminDashboard()
      return response.data as AdminDashboardData
    },
    enabled: isAdmin
  })

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

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-500"></div>
      </div>
    )
  }

  const stats = [
    {
      title: '전체 회원수',
      value: dashboardData?.userStatistics.totalUsers || 0,
      icon: Users,
      color: 'bg-blue-500',
      change: `+${dashboardData?.newUserStatistics.thisWeekNewUsers || 0} (이번 주)`
    },
    {
      title: '일반 회원',
      value: dashboardData?.userStatistics.generalUsers || 0,
      icon: Users,
      color: 'bg-green-500',
      change: '활성 회원'
    },
    {
      title: '기업 회원',
      value: dashboardData?.userStatistics.companyUsers || 0,
      icon: Briefcase,
      color: 'bg-purple-500',
      change: '기업 계정'
    },
    {
      title: '채용공고',
      value: dashboardData?.jobPostingStatistics.totalJobPostings || 0,
      icon: FileText,
      color: 'bg-orange-500',
      change: `활성: ${dashboardData?.jobPostingStatistics.activeJobPostings || 0}개`
    }
  ]

  const aiStats = [
    {
      title: 'AI 면접',
      value: dashboardData?.aiServiceStatistics.totalInterviews || 0,
      icon: Brain,
      color: 'bg-indigo-500'
    },
    {
      title: '자소서 생성',
      value: dashboardData?.aiServiceStatistics.totalCoverLetters || 0,
      icon: FileText,
      color: 'bg-pink-500'
    },
    {
      title: '번역 서비스',
      value: dashboardData?.aiServiceStatistics.totalTranslations || 0,
      icon: Languages,
      color: 'bg-cyan-500'
    },
    {
      title: '이미지 생성',
      value: dashboardData?.aiServiceStatistics.totalImageGenerations || 0,
      icon: Image,
      color: 'bg-yellow-500'
    },
    {
      title: '챗봇 대화',
      value: dashboardData?.aiServiceStatistics.totalChatInteractions || 0,
      icon: Bot,
      color: 'bg-emerald-500'
    },
    {
      title: '감정 분석',
      value: dashboardData?.aiServiceStatistics.totalSentimentAnalyses || 0,
      icon: TrendingUp,
      color: 'bg-red-500'
    }
  ]

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">관리자 대시보드</h1>
        <p className="text-gray-600">시스템 전체 현황을 한눈에 확인하세요</p>
      </div>

      {/* 기본 통계 */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        {stats.map((stat) => {
          const IconComponent = stat.icon
          return (
            <div key={stat.title} className="card">
              <div className="card-content">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-sm font-medium text-gray-600">{stat.title}</p>
                    <p className="text-2xl font-bold text-gray-900">{stat.value.toLocaleString()}</p>
                    <p className="text-xs text-gray-500 mt-1">{stat.change}</p>
                  </div>
                  <div className={`${stat.color} rounded-full p-3`}>
                    <IconComponent className="w-6 h-6 text-white" />
                  </div>
                </div>
              </div>
            </div>
          )
        })}
      </div>

      {/* AI 서비스 통계 */}
      <div className="card">
        <div className="card-content">
          <h2 className="text-lg font-semibold mb-4 flex items-center">
            <Bot className="w-5 h-5 mr-2" />
            AI 서비스 이용 현황
          </h2>
          <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">
            {aiStats.map((stat) => {
              const IconComponent = stat.icon
              return (
                <div key={stat.title} className="text-center p-4 rounded-lg border border-gray-200">
                  <div className={`${stat.color} rounded-full p-2 mx-auto mb-2 w-fit`}>
                    <IconComponent className="w-4 h-4 text-white" />
                  </div>
                  <p className="text-xs font-medium text-gray-600">{stat.title}</p>
                  <p className="text-lg font-bold text-gray-900">{stat.value.toLocaleString()}</p>
                </div>
              )
            })}
          </div>
        </div>
      </div>

      {/* 최근 증명서 요청 */}
      <div className="card">
        <div className="card-content">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-lg font-semibold flex items-center">
              <FileText className="w-5 h-5 mr-2" />
              최근 증명서 요청
            </h2>
            <a href="/admin/certificates" className="text-blue-600 hover:text-blue-800 text-sm">
              전체 보기
            </a>
          </div>
          <div className="overflow-x-auto">
            <table className="min-w-full">
              <thead>
                <tr className="border-b border-gray-200">
                  <th className="text-left py-2 px-3 text-sm font-medium text-gray-600">요청자</th>
                  <th className="text-left py-2 px-3 text-sm font-medium text-gray-600">증명서 타입</th>
                  <th className="text-left py-2 px-3 text-sm font-medium text-gray-600">요청일</th>
                  <th className="text-left py-2 px-3 text-sm font-medium text-gray-600">상태</th>
                </tr>
              </thead>
              <tbody>
                {dashboardData?.certificateRequests.slice(0, 5).map((request) => (
                  <tr key={request.id} className="border-b border-gray-100">
                    <td className="py-2 px-3">
                      <div>
                        <p className="text-sm font-medium text-gray-900">{request.userName}</p>
                        <p className="text-xs text-gray-500">{request.userEmail}</p>
                      </div>
                    </td>
                    <td className="py-2 px-3 text-sm text-gray-900">{request.certificateType}</td>
                    <td className="py-2 px-3 text-sm text-gray-500">
                      {new Date(request.requestedAt).toLocaleDateString('ko-KR')}
                    </td>
                    <td className="py-2 px-3">
                      <span className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium ${
                        request.status === 'PENDING' ? 'bg-yellow-100 text-yellow-800' :
                        request.status === 'APPROVED' ? 'bg-green-100 text-green-800' :
                        'bg-red-100 text-red-800'
                      }`}>
                        {request.status === 'PENDING' ? (
                          <>
                            <Clock className="w-3 h-3 mr-1" />
                            대기중
                          </>
                        ) : request.status === 'APPROVED' ? (
                          <>
                            <CheckCircle className="w-3 h-3 mr-1" />
                            승인됨
                          </>
                        ) : (
                          <>
                            <AlertCircle className="w-3 h-3 mr-1" />
                            거부됨
                          </>
                        )}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>

      {/* 빠른 실행 */}
      <div className="card">
        <div className="card-content">
          <h2 className="text-lg font-semibold mb-4">빠른 실행</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            <a
              href="/admin/users"
              className="p-4 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors text-center"
            >
              <Users className="w-8 h-8 mx-auto mb-2 text-blue-600" />
              <p className="text-sm font-medium">회원 관리</p>
            </a>
            <a
              href="/admin/certificates"
              className="p-4 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors text-center"
            >
              <FileText className="w-8 h-8 mx-auto mb-2 text-green-600" />
              <p className="text-sm font-medium">증명서 관리</p>
            </a>
            <a
              href="/admin/jobs"
              className="p-4 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors text-center"
            >
              <Briefcase className="w-8 h-8 mx-auto mb-2 text-purple-600" />
              <p className="text-sm font-medium">채용공고 관리</p>
            </a>
            <a
              href="/admin/community"
              className="p-4 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors text-center"
            >
              <MessageSquare className="w-8 h-8 mx-auto mb-2 text-orange-600" />
              <p className="text-sm font-medium">커뮤니티 관리</p>
            </a>
          </div>
        </div>
      </div>
    </div>
  )
}