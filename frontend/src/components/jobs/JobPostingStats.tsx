import React from 'react'
import { useQuery } from '@tanstack/react-query'
import { TrendingUp, Eye, Users, Calendar, AlertTriangle, CheckCircle } from 'lucide-react'
import { apiClient } from '@/services/api'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/Card'
import { Badge } from '@/components/ui/Badge'

interface JobPostingStatsProps {
  jobId: number
}

interface JobPostingStatsData {
  jobPostingId: number
  title: string
  companyName: string
  status: string
  viewCount: number
  applicationCount: number
  viewToApplicationRatio?: number
  deadlineDate?: string
  daysUntilDeadline?: number
  isDeadlineApproaching: boolean
  createdAt: string
  publishedAt?: string
}

export default function JobPostingStats({ jobId }: JobPostingStatsProps) {
  const { data: statsData, isLoading, isError } = useQuery({
    queryKey: ['jobStats', jobId],
    queryFn: () => apiClient.getJobPostingStats(jobId),
    enabled: !!jobId,
  })

  if (isLoading) {
    return (
      <Card className="border-blue-200 bg-blue-50">
        <CardHeader>
          <CardTitle className="text-lg flex items-center">
            <TrendingUp className="w-5 h-5 mr-2" />
            📊 채용공고 통계
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="animate-pulse">
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
              {[...Array(4)].map((_, i) => (
                <div key={i} className="text-center">
                  <div className="h-8 bg-blue-200 rounded w-12 mx-auto mb-2"></div>
                  <div className="h-4 bg-blue-200 rounded w-16 mx-auto"></div>
                </div>
              ))}
            </div>
          </div>
        </CardContent>
      </Card>
    )
  }

  if (isError || !statsData?.data) {
    return null
  }

  const stats: JobPostingStatsData = statsData.data

  const getApplicationRate = () => {
    if (!stats.viewCount || stats.viewCount === 0) return 0
    return ((stats.applicationCount / stats.viewCount) * 100).toFixed(1)
  }

  const getDeadlineStatus = () => {
    if (!stats.daysUntilDeadline) return null

    if (stats.daysUntilDeadline <= 0) {
      return { text: '마감됨', color: 'text-red-600', icon: '⏰' }
    } else if (stats.isDeadlineApproaching) {
      return { text: `${stats.daysUntilDeadline}일 남음`, color: 'text-orange-600', icon: '⚠️' }
    } else {
      return { text: `${stats.daysUntilDeadline}일 남음`, color: 'text-green-600', icon: '✅' }
    }
  }

  const deadlineStatus = getDeadlineStatus()

  // return (
  //   <Card className="border-blue-200 bg-blue-50">
  //     <CardHeader>
  //       <CardTitle className="text-lg flex items-center text-blue-800">
  //         <TrendingUp className="w-5 h-5 mr-2" />
  //         📊 채용공고 통계
  //       </CardTitle>
  //       <p className="text-sm text-blue-700">
  //         실시간 조회수와 지원자 현황을 확인해보세요
  //       </p>
  //     </CardHeader>
  //     <CardContent>
  //       <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-4">
  //         <div className="text-center bg-white p-4 rounded-lg border border-blue-200">
  //           <div className="flex items-center justify-center mb-2">
  //             <Eye className="w-5 h-5 text-blue-600 mr-1" />
  //             <div className="text-2xl font-bold text-blue-600">{stats.viewCount}</div>
  //           </div>
  //           <div className="text-sm text-gray-600">조회수</div>
  //         </div>

  //         <div className="text-center bg-white p-4 rounded-lg border border-blue-200">
  //           <div className="flex items-center justify-center mb-2">
  //             <Users className="w-5 h-5 text-green-600 mr-1" />
  //             <div className="text-2xl font-bold text-green-600">{stats.applicationCount}</div>
  //           </div>
  //           <div className="text-sm text-gray-600">지원자수</div>
  //         </div>

  //         <div className="text-center bg-white p-4 rounded-lg border border-blue-200">
  //           <div className="flex items-center justify-center mb-2">
  //             <TrendingUp className="w-5 h-5 text-purple-600 mr-1" />
  //             <div className="text-2xl font-bold text-purple-600">{getApplicationRate()}%</div>
  //           </div>
  //           <div className="text-sm text-gray-600">지원율</div>
  //         </div>

  //         <div className="text-center bg-white p-4 rounded-lg border border-blue-200">
  //           <div className="flex items-center justify-center mb-2">
  //             {deadlineStatus ? (
  //               <>
  //                 <Calendar className="w-5 h-5 mr-1" />
  //                 <div className={`text-2xl font-bold ${deadlineStatus.color}`}>
  //                   {deadlineStatus.icon}
  //                 </div>
  //               </>
  //             ) : (
  //               <>
  //                 <CheckCircle className="w-5 h-5 text-gray-400 mr-1" />
  //                 <div className="text-2xl font-bold text-gray-400">∞</div>
  //               </>
  //             )}
  //           </div>
  //           <div className="text-sm text-gray-600">
  //             {deadlineStatus ? deadlineStatus.text : '상시모집'}
  //           </div>
  //         </div>
  //       </div>

  //       {/* 추가 정보 */}
  //       <div className="bg-white p-4 rounded-lg border border-blue-200">
  //         <h4 className="font-semibold text-gray-900 mb-3">상세 정보</h4>
  //         <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm">
  //           <div className="flex justify-between">
  //             <span className="text-gray-600">공고 상태:</span>
  //             <Badge
  //               variant={stats.status === 'PUBLISHED' ? 'success' : 'secondary'}
  //               size="sm"
  //             >
  //               {stats.status === 'PUBLISHED' ? '게시중' : stats.status}
  //             </Badge>
  //           </div>

  //           {stats.publishedAt && (
  //             <div className="flex justify-between">
  //               <span className="text-gray-600">게시일:</span>
  //               <span className="text-gray-900">
  //                 {new Date(stats.publishedAt).toLocaleDateString('ko-KR')}
  //               </span>
  //             </div>
  //           )}

  //           {stats.deadlineDate && (
  //             <div className="flex justify-between">
  //               <span className="text-gray-600">마감일:</span>
  //               <span className={`font-medium ${deadlineStatus?.color || 'text-gray-900'}`}>
  //                 {new Date(stats.deadlineDate).toLocaleDateString('ko-KR')}
  //               </span>
  //             </div>
  //           )}

  //           {stats.viewToApplicationRatio !== undefined && (
  //             <div className="flex justify-between">
  //               <span className="text-gray-600">전환율:</span>
  //               <span className="text-gray-900 font-medium">
  //                 {(stats.viewToApplicationRatio * 100).toFixed(2)}%
  //               </span>
  //             </div>
  //           )}
  //         </div>
  //       </div>

  //       {/* 인사이트 */}
  //       {stats.viewCount > 0 && (
  //         <div className="mt-4 p-3 bg-gradient-to-r from-blue-100 to-purple-100 rounded-lg border border-blue-200">
  //           <h5 className="font-medium text-blue-800 mb-2 flex items-center">
  //             💡 인사이트
  //           </h5>
  //           <div className="text-sm text-blue-700">
  //             {stats.applicationCount === 0 ? (
  //               "아직 지원자가 없습니다. 채용공고 내용을 더 매력적으로 작성해보세요."
  //             ) : parseFloat(getApplicationRate()) < 5 ? (
  //               "지원율이 낮습니다. 자격요건이나 혜택을 다시 검토해보세요."
  //             ) : parseFloat(getApplicationRate()) > 15 ? (
  //               "높은 지원율을 보이고 있습니다! 우수한 채용공고입니다."
  //             ) : (
  //               "적정한 지원율을 유지하고 있습니다."
  //             )}
  //             {stats.isDeadlineApproaching && (
  //               " 마감일이 임박했으니 지원자 검토를 서둘러 진행하세요."
  //             )}
  //           </div>
  //         </div>
  //       )}
  //     </CardContent>
  //   </Card>
  // )
}