import { useState, useEffect } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import {
  Users,
  Search,
  Filter,
  Shield,
  Trash2,
  Building,
  User,
  Crown,
  AlertCircle,
  CheckCircle
} from 'lucide-react'
import { apiClient } from '@/services/api'

interface UserData {
  id: number
  email: string
  name: string
  userType: 'GENERAL' | 'COMPANY' | 'ADMIN'
}

interface UserStatistics {
  totalUsers: number
  generalUsers: number
  companyUsers: number
  adminUsers: number
  activeUsers: number
}

export default function UserManagement() {
  const queryClient = useQueryClient()
  const [searchTerm, setSearchTerm] = useState('')
  const [userTypeFilter, setUserTypeFilter] = useState<string>('ALL')
  const [statusFilter, setStatusFilter] = useState<string>('ALL')
  const [isAdmin, setIsAdmin] = useState(false)

  // Check admin authentication
  useEffect(() => {
    const adminToken = localStorage.getItem('adminToken')
    const isAdminStatus = localStorage.getItem('isAdmin')
    setIsAdmin(adminToken && isAdminStatus === 'true')
  }, [])

  const { data: users, isLoading: usersLoading } = useQuery({
    queryKey: ['admin-users'],
    queryFn: async () => {
      const response = await apiClient.getAllUsers()
      return response.data as UserData[]
    },
    enabled: isAdmin
  })

  const { data: statistics, isLoading: statsLoading } = useQuery({
    queryKey: ['user-statistics'],
    queryFn: async () => {
      const response = await apiClient.getUserStatistics()
      return response.data as UserStatistics
    },
    enabled: isAdmin
  })

  const deleteAccountMutation = useMutation({
    mutationFn: (userId: number) => apiClient.deleteUserAccount(userId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-users'] })
      queryClient.invalidateQueries({ queryKey: ['user-statistics'] })
      alert('계정이 삭제되었습니다.')
    },
    onError: (error) => {
      console.error('계정 삭제 실패:', error)
      alert('계정 삭제에 실패했습니다.')
    }
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

  if (usersLoading || statsLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-500"></div>
      </div>
    )
  }

  const filteredUsers = users?.filter((user) => {
    const matchesSearch = user.email.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         user.name.toLowerCase().includes(searchTerm.toLowerCase())
    const matchesUserType = userTypeFilter === 'ALL' || user.userType === userTypeFilter

    return matchesSearch && matchesUserType
  }) || []

  const handleDeleteAccount = (userId: number) => {
    if (confirm('정말로 이 계정을 삭제하시겠습니까? 삭제된 계정은 복구할 수 없습니다.')) {
      deleteAccountMutation.mutate(userId)
    }
  }


  const getUserTypeIcon = (userType: string) => {
    switch (userType) {
      case 'ADMIN':
        return <Crown className="w-4 h-4 text-yellow-600" />
      case 'COMPANY':
        return <Building className="w-4 h-4 text-purple-600" />
      default:
        return <User className="w-4 h-4 text-blue-600" />
    }
  }

  const getUserTypeLabel = (userType: string) => {
    switch (userType) {
      case 'ADMIN':
        return '관리자'
      case 'COMPANY':
        return '기업'
      default:
        return '일반'
    }
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">회원 관리</h1>
        <p className="text-gray-600">시스템에 등록된 모든 회원을 관리합니다</p>
      </div>

      {/* 통계 카드 */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="card">
          <div className="card-content text-center">
            <Users className="w-8 h-8 text-blue-500 mx-auto mb-2" />
            <p className="text-sm font-medium text-gray-600">전체 회원</p>
            <p className="text-2xl font-bold text-gray-900">{statistics?.totalUsers || 0}</p>
          </div>
        </div>
        <div className="card">
          <div className="card-content text-center">
            <User className="w-8 h-8 text-green-500 mx-auto mb-2" />
            <p className="text-sm font-medium text-gray-600">일반 회원</p>
            <p className="text-2xl font-bold text-gray-900">{statistics?.generalUsers || 0}</p>
          </div>
        </div>
        <div className="card">
          <div className="card-content text-center">
            <Building className="w-8 h-8 text-purple-500 mx-auto mb-2" />
            <p className="text-sm font-medium text-gray-600">기업 회원</p>
            <p className="text-2xl font-bold text-gray-900">{statistics?.companyUsers || 0}</p>
          </div>
        </div>
        <div className="card">
          <div className="card-content text-center">
            <Crown className="w-8 h-8 text-yellow-500 mx-auto mb-2" />
            <p className="text-sm font-medium text-gray-600">관리자</p>
            <p className="text-2xl font-bold text-gray-900">{statistics?.adminUsers || 0}</p>
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
                placeholder="이메일 또는 이름으로 검색..."
                className="pl-10 pr-4 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent w-full"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
            </div>
            
            <div>
              <select
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                value={userTypeFilter}
                onChange={(e) => setUserTypeFilter(e.target.value)}
              >
                <option value="ALL">전체 유형</option>
                <option value="GENERAL">일반 회원</option>
                <option value="COMPANY">기업 회원</option>
                <option value="ADMIN">관리자</option>
              </select>
            </div>


            <div className="text-sm text-gray-600 flex items-center">
              <Filter className="w-4 h-4 mr-2" />
              {filteredUsers.length}개 결과
            </div>
          </div>
        </div>
      </div>

      {/* 사용자 목록 */}
      <div className="card">
        <div className="card-content">
          <div className="overflow-x-auto">
            <table className="min-w-full">
              <thead>
                <tr className="border-b border-gray-200">
                  <th className="text-left py-3 px-3 text-sm font-medium text-gray-600">사용자</th>
                  <th className="text-left py-3 px-3 text-sm font-medium text-gray-600">유형</th>
                  <th className="text-left py-3 px-3 text-sm font-medium text-gray-600">작업</th>
                </tr>
              </thead>
              <tbody>
                {filteredUsers.map((userData) => (
                  <tr key={userData.id} className="border-b border-gray-100">
                    <td className="py-3 px-3">
                      <div>
                        <p className="text-sm font-medium text-gray-900">{userData.name}</p>
                        <p className="text-xs text-gray-500">{userData.email}</p>
                      </div>
                    </td>
                    <td className="py-3 px-3">
                      <span className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-gray-100 text-gray-800">
                        {getUserTypeIcon(userData.userType)}
                        <span className="ml-1">{getUserTypeLabel(userData.userType)}</span>
                      </span>
                    </td>
                    <td className="py-3 px-3">
                      <div className="flex space-x-2">
                        <button
                          onClick={() => handleDeleteAccount(userData.id)}
                          className="text-red-600 hover:text-red-800"
                          title="계정 삭제"
                        >
                          <Trash2 className="w-4 h-4" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            
            {filteredUsers.length === 0 && (
              <div className="text-center py-8">
                <Users className="w-16 h-16 text-gray-300 mx-auto mb-4" />
                <p className="text-gray-500">조건에 맞는 사용자가 없습니다.</p>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}