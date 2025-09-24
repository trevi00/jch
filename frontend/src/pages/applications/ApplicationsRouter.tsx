/**
 * ApplicationsRouter.tsx - 사용자 타입에 따른 지원서 관리 라우터
 *
 * 일반 사용자: MyApplications (본인이 지원한 내역)
 * 기업 사용자: ApplicantManagement (회사로 지원한 지원자 관리)
 */

import { useAuthStore } from '@/hooks/useAuthStore'
import MyApplications from '@/pages/applications/MyApplications'
import ApplicantManagement from '@/pages/company/ApplicantManagement'

export default function ApplicationsRouter() {
  const { user } = useAuthStore()

  // 기업 사용자인 경우 지원자 관리 페이지 표시
  if (user?.userType === 'COMPANY') {
    return <ApplicantManagement />
  }

  // 일반 사용자인 경우 내 지원 현황 페이지 표시
  return <MyApplications />
}