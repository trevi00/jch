/**
 * Dashboard.tsx - 메인 대시보드 페이지 컴포넌트
 *
 * 🔧 사용 기술:
 * - React 18 (useState, useEffect, 함수형 컴포넌트)
 * - TypeScript (타입 안전성)
 * - Tanstack Query (서버 상태 관리, 캐싱, 재시도)
 * - Recharts (데이터 시각화 차트 라이브러리)
 * - Lucide React (아이콘 라이브러리)
 * - Tailwind CSS (스타일링)
 * - Zustand (클라이언트 상태 관리)
 *
 * 📋 주요 기능:
 * - 사용자 타입별 대시보드 분기 (일반/기업)
 * - 실시간 취업 활동 현황 시각화
 * - 인터랙티브 차트 및 그래프
 * - 취업 준비도 AI 분석 (정규분포 기반)
 * - 역량 분석 및 개선 제안
 * - 월별 진행상황 트래킹
 * - 빠른 액션 메뉴
 *
 * 🎯 이벤트 처리:
 * - 데이터 페칭: Tanstack Query의 자동 관리
 * - 차트 인터랙션: Recharts의 hover, click 이벤트
 * - 새로고침: 수동 데이터 리페치
 * - 네비게이션: React Router를 통한 페이지 이동
 */

// 🔍 데이터 페칭 및 상태 관리
import { useQuery } from '@tanstack/react-query'

// 🎨 아이콘 라이브러리 (Lucide React - 경량화된 Feather Icons)
import {
  Briefcase,          // 💼 채용공고/직무 관련
  TrendingUp,         // 📈 상승 트렌드/성장
  Calendar,           // 📅 면접 일정/날짜
  Award,              // 🏆 성취/인기 공고
  Target,             // 🎯 목표/타겟
  Clock,              // ⏰ 시간/마감일
  MessageSquare,      // 💬 채팅/메시지
  Languages,          // 🌐 언어/번역
  Zap,                // ⚡ 빠른 실행/액션
  Users,              // 👥 사용자/지원자
  FileText,           // 📄 문서/공고
  Building2,          // 🏢 기업/회사
  UserCheck,          // ✅ 사용자 승인/확인
  BarChart3,          // 📊 막대 차트
  PieChart,           // 🥧 원형 차트
  BookOpen,           // 📖 학습/추천
  CheckCircle,        // ✅ 완료/체크
  AlertCircle,        // ⚠️ 알림/경고
  RefreshCw,          // 🔄 새로고침
  Loader2,            // ⏳ 로딩 스피너
  WifiOff             // 📶 오프라인/연결 끊김
} from 'lucide-react'

// 🌐 API 통신 및 데이터 관리
import { apiClient } from '@/services/api'

// 🧩 UI 컴포넌트들
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/Card'
import { Badge } from '@/components/ui/Badge'
import { Button } from '@/components/ui/Button'
import LoadingCard from '@/components/ui/LoadingCard'
import ErrorMessage from '@/components/ui/ErrorMessage'

// 🎯 컨텍스트 및 상태 관리
import { useToast } from '@/contexts/ToastContext'
import { useAuthStore } from '@/hooks/useAuthStore'

// 🧭 네비게이션
import { Link } from 'react-router-dom'

// 🏷️ 타입 정의
import { UserType, GeneralUserDashboard } from '@/types/api'

// 📊 차트 라이브러리 (Recharts - React용 D3 기반 차트)
import {
  BarChart,              // 막대 차트
  Bar,                   // 막대 요소
  XAxis,                 // X축
  YAxis,                 // Y축
  CartesianGrid,         // 격자
  Tooltip,               // 툴팁
  ResponsiveContainer,   // 반응형 컨테이너
  LineChart,             // 선 차트
  Line,                  // 선 요소
  PieChart as RechartsPieChart, // 원형 차트 (이름 충돌 방지)
  Pie,                   // 원형 요소
  Cell,                  // 셀 요소
  AreaChart,             // 영역 차트
  Area                   // 영역 요소
} from 'recharts'

// 🎨 차트에서 사용할 색상 팔레트 (Material Design 기반)
const COLORS = ['#3b82f6', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#06d6a0']

/**
 * 🏠 메인 대시보드 컴포넌트 (라우터 컴포넌트)
 *
 * 📍 역할:
 * - 사용자 타입에 따른 대시보드 분기 처리
 * - 일반 사용자 vs 기업 사용자 구분
 * - 적절한 하위 컴포넌트로 라우팅
 *
 * 🔄 처리 로직:
 * 1. Zustand에서 사용자 정보 조회
 * 2. userType 검사 (GENERAL | COMPANY)
 * 3. 타입별 전용 대시보드 컴포넌트 렌더링
 *
 * 🎯 이벤트:
 * - 사용자 인증 상태 변경 시 자동 리렌더링
 * - 사용자 타입 변경 시 적절한 대시보드 표시
 */
export default function Dashboard() {
  // 🔍 Zustand에서 현재 로그인된 사용자 정보 조회
  const { user } = useAuthStore()

  // 🏢 기업 사용자인 경우 → 기업 전용 대시보드 렌더링
  // 채용공고 관리, 지원자 현황, 기업 통계 등을 제공
  if (user?.userType === UserType.COMPANY) {
    return <CompanyDashboardComponent />
  }

  // 👤 일반 사용자인 경우 → 개인 취업 활동 대시보드 렌더링
  // 지원 현황, AI 분석, 취업 준비도, 추천 활동 등을 제공
  return <GeneralUserDashboardComponent />
}

/**
 * 일반 사용자 대시보드 컴포넌트
 * 취업 활동 현황, 통계, AI 분석 등을 제공
 * 이벤트: 대시보드 데이터 조회 이벤트, 차트 렌더링 이벤트, 새로고침 이벤트
 */
const GeneralUserDashboardComponent = () => {
  // 실제 데이터 가져오기 - 향상된 에러 처리 및 재시도 로직
  const { data: dashboardData, isLoading, error, refetch, isRefetching } = useQuery({
    queryKey: ['general-user-dashboard'],
    queryFn: () => apiClient.getGeneralUserDashboard(),
    staleTime: 5 * 60 * 1000, // 5분간 캐시 유지
    retry: 3,
    retryDelay: (attemptIndex) => Math.min(1000 * 2 ** attemptIndex, 30000), // 지수 백오프
    refetchOnWindowFocus: false, // 창 포커스 시 자동 갱신 비활성화
  })

  // 향상된 로딩 상태 UI
  if (isLoading) {
    return (
      <div className="space-y-8 p-6">
        <div className="text-center md:text-left">
          <h1 className="text-4xl font-bold text-gray-900 dark:text-gray-100 mb-2">대시보드</h1>
          <p className="text-xl text-gray-600 dark:text-gray-400">데이터를 불러오는 중...</p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          {Array.from({ length: 4 }).map((_, i) => (
            <LoadingCard key={i} variant="elevated" />
          ))}
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {Array.from({ length: 2 }).map((_, i) => (
            <LoadingCard key={i} variant="default" />
          ))}
        </div>
      </div>
    )
  }

  // 향상된 에러 상태 UI
  if (error) {
    return (
      <div className="space-y-8 p-6">
        <div className="text-center md:text-left">
          <h1 className="text-4xl font-bold text-gray-900 dark:text-gray-100 mb-2">대시보드</h1>
          <p className="text-xl text-gray-600 dark:text-gray-400">데이터를 불러오는 중 문제가 발생했습니다</p>
        </div>

        <Card variant="elevated" className="max-w-2xl mx-auto">
          <CardContent className="p-8">
            <ErrorMessage
              title="대시보드 로딩 실패"
              message="대시보드 데이터를 불러오는 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
              onRetry={() => refetch()}
              retryLabel={isRefetching ? "다시 시도 중..." : "다시 시도"}
            />
          </CardContent>
        </Card>
        
        {/* 대체 콘텐츠 또는 캐시된 데이터 표시 옵션 */}
        <Card className="max-w-2xl mx-auto shadow-sm border-yellow-200">
          <CardContent className="p-6 text-center bg-yellow-50">
            <div className="flex items-center justify-center gap-2 mb-3">
              <MessageSquare className="w-5 h-5 text-yellow-600" />
              <span className="font-medium text-yellow-700">대안 기능</span>
            </div>
            <p className="text-sm text-yellow-600 mb-4">
              데이터 로딩 중에도 다음 기능들을 이용하실 수 있습니다
            </p>
            <div className="flex flex-wrap justify-center gap-2">
              <Link to="/ai/interview">
                <Badge variant="outline" className="hover:bg-yellow-100 cursor-pointer">
                  AI 면접 연습
                </Badge>
              </Link>
              <Link to="/jobs">
                <Badge variant="outline" className="hover:bg-yellow-100 cursor-pointer">
                  채용 공고 보기
                </Badge>
              </Link>
              <Link to="/community">
                <Badge variant="outline" className="hover:bg-yellow-100 cursor-pointer">
                  커뮤니티
                </Badge>
              </Link>
            </div>
          </CardContent>
        </Card>
      </div>
    )
  }

  const dashboard = dashboardData?.data

  // 실제 데이터로 차트 데이터 구성
  const applicationData = dashboard ? [
    { name: '총 지원', value: dashboard.myApplicationStatus.totalApplications, color: '#3b82f6' },
    { name: '대기중', value: dashboard.myApplicationStatus.pendingApplications, color: '#f59e0b' },
    { name: '면접진행', value: dashboard.myApplicationStatus.interviewApplications, color: '#8b5cf6' },
    { name: '합격', value: dashboard.myApplicationStatus.acceptedApplications, color: '#10b981' },
    { name: '불합격', value: dashboard.myApplicationStatus.rejectedApplications, color: '#ef4444' }
  ] : []

  // 취업 준비도 분석 데이터 (API에서 가져온 실제 데이터)
  const jobPreparationData = dashboard?.jobPreparationAnalysis ? [
    { category: '나의 점수', score: dashboard.jobPreparationAnalysis.myScore },
    { category: '평균 점수', score: dashboard.jobPreparationAnalysis.averageScore },
    { category: '목표 점수', score: dashboard.jobPreparationAnalysis.targetScore },
  ] : []

  // 직무별 취업률 데이터
  const jobFieldData = dashboard?.jobFieldEmployments || []

  // 월별 진행 상황 데이터 (API에서 가져온 실제 데이터)
  const monthlyProgressData = dashboard?.monthlyProgress || []

  // 역량 분석 데이터 (API에서 가져온 실제 데이터)
  const capabilityData = dashboard?.capabilities || []
  const averageCapability = capabilityData.length > 0 ? 
    capabilityData.reduce((sum, item) => sum + item.level, 0) / capabilityData.length : 0

  // 맞춤 활동 추천 데이터 (API에서 가져온 실제 데이터)
  const recommendations = dashboard?.personalInsight?.recommendations || []

  return (
    <div className="space-y-8 p-6">
      <div className="text-center md:text-left">
        <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
          <div>
            <h1 className="text-4xl font-bold text-gray-900 dark:text-gray-100 mb-2" role="banner">대시보드</h1>
            <p className="text-xl text-gray-600 dark:text-gray-400" aria-describedby="dashboard-description">취업 현황과 활동을 한눈에 확인하세요</p>
          </div>
          <div className="flex items-center gap-3">
            <Button
              variant="outline"
              size="sm"
              onClick={() => refetch()}
              disabled={isRefetching}
              className="flex items-center gap-2"
              aria-label={isRefetching ? '대시보드 데이터를 새로고침하는 중입니다' : '대시보드 데이터 새로고침'}
              aria-describedby="refresh-status"
            >
              {isRefetching ? (
                <Loader2 className="w-4 h-4 animate-spin" aria-hidden="true" />
              ) : (
                <RefreshCw className="w-4 h-4" aria-hidden="true" />
              )}
              {isRefetching ? '새로고림 중...' : '새로고침'}
            </Button>
            {dashboard && (
              <Badge variant="secondary" className="text-xs">
                실시간 데이터
              </Badge>
            )}
          </div>
        </div>
      </div>

      {/* 메인 통계 카드 */}
      <section aria-labelledby="main-stats" className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-6">
        <h2 id="main-stats" className="sr-only">주요 통계 정보</h2>
        <Card className="hover:shadow-lg transition-all duration-300 border-0 shadow-md">
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600 mb-1">지원한 공고</p>
                <p className="text-3xl font-bold text-gray-900">{dashboard?.myApplicationStatus?.totalApplications || 0}</p>
              </div>
              <div className="p-3 bg-gradient-to-r from-blue-500 to-blue-600 rounded-xl shadow-lg">
                <Briefcase className="w-8 h-8 text-white" />
              </div>
            </div>
          </CardContent>
        </Card>

        <Card className="hover:shadow-lg transition-all duration-300 border-0 shadow-md">
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600 mb-1">면접 진행</p>
                <p className="text-3xl font-bold text-gray-900">{dashboard?.myApplicationStatus?.interviewApplications || 0}</p>
              </div>
              <div className="p-3 bg-gradient-to-r from-green-500 to-emerald-600 rounded-xl shadow-lg">
                <Calendar className="w-8 h-8 text-white" />
              </div>
            </div>
          </CardContent>
        </Card>

        <Card className="hover:shadow-lg transition-all duration-300 border-0 shadow-md">
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600 mb-1">취업 점수</p>
                <p className="text-3xl font-bold text-gray-900">{dashboard?.myJobScore || 0}</p>
              </div>
              <div className="p-3 bg-gradient-to-r from-purple-500 to-purple-600 rounded-xl shadow-lg">
                <Target className="w-8 h-8 text-white" />
              </div>
            </div>
          </CardContent>
        </Card>

        <Card className="hover:shadow-lg transition-all duration-300 border-0 shadow-md">
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600 mb-1">전체 취업률</p>
                <p className="text-3xl font-bold text-gray-900">{dashboard?.totalEmploymentRate?.toFixed(1) || '0.0'}%</p>
              </div>
              <div className="p-3 bg-gradient-to-r from-orange-500 to-red-500 rounded-xl shadow-lg">
                <TrendingUp className="w-8 h-8 text-white" />
              </div>
            </div>
          </CardContent>
        </Card>
      </section>

      {/* 새로운 분석 섹션 */}
      <div className="grid grid-cols-1 xl:grid-cols-2 gap-6">
        
        {/* 1. 취업 준비도 분석 (정규분포 그래프) */}
        <Card className="shadow-lg">
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <BarChart3 className="w-5 h-5 text-blue-600" />
              취업 준비도 분석
            </CardTitle>
            <p className="text-sm text-gray-600 mt-1">
              나의 취업 준비 점수를 정규분포로 분석하여 위치를 확인합니다
            </p>
          </CardHeader>
          <CardContent>
            <div className="h-64">
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart data={Array.from({ length: 100 }, (_, i) => {
                  const x = i + 1;
                  const mean = 65; // 평균 점수
                  const stdDev = 15; // 표준편차
                  // 정규분포 공식
                  const y = (1 / (stdDev * Math.sqrt(2 * Math.PI))) *
                           Math.exp(-0.5 * Math.pow((x - mean) / stdDev, 2));
                  return {
                    score: x,
                    probability: y * 1000, // 가시성을 위해 스케일 조정
                    isMyScore: Math.abs(x - (dashboard?.myJobScore || 0)) < 2,
                    isAverage: Math.abs(x - 65) < 1,
                    isTarget: Math.abs(x - 85) < 1
                  };
                })}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis
                    dataKey="score"
                    label={{ value: '점수', position: 'insideBottom', offset: -5 }}
                    domain={[0, 100]}
                  />
                  <YAxis
                    label={{ value: '분포 밀도', angle: -90, position: 'insideLeft' }}
                    hide
                  />
                  <Tooltip
                    formatter={(value: number) => [`${value.toFixed(2)}`, '분포 밀도']}
                    labelFormatter={(score: number) => `점수: ${score}점`}
                  />
                  <Area
                    type="monotone"
                    dataKey="probability"
                    stroke="#3b82f6"
                    fill="url(#normalDistributionGradient)"
                    strokeWidth={2}
                  />
                  <defs>
                    <linearGradient id="normalDistributionGradient" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#3b82f6" stopOpacity={0.8}/>
                      <stop offset="95%" stopColor="#3b82f6" stopOpacity={0.1}/>
                    </linearGradient>
                  </defs>
                </AreaChart>
              </ResponsiveContainer>
            </div>

            {/* 점수 표시 및 분석 */}
            <div className="mt-6 space-y-3">
              <div className="relative">
                <div className="flex items-center justify-between mb-2">
                  <span className="text-sm text-gray-600">0점</span>
                  <span className="text-sm text-gray-600">100점</span>
                </div>
                <div className="relative h-4 bg-gray-200 rounded-full">
                  {/* 정규분포 배경 */}
                  <div className="absolute inset-0 bg-gradient-to-r from-red-200 via-yellow-200 via-blue-200 to-green-200 rounded-full opacity-50"></div>

                  {/* 나의 점수 마커 */}
                  <div
                    className="absolute top-0 h-4 w-1 bg-blue-600 rounded-full"
                    style={{ left: `${(dashboard?.myJobScore || 0)}%` }}
                  >
                    <div className="absolute -top-8 left-1/2 transform -translate-x-1/2">
                      <div className="bg-blue-600 text-white text-xs px-2 py-1 rounded-full font-medium">
                        나: {dashboard?.myJobScore || 0}점
                      </div>
                    </div>
                  </div>

                  {/* 평균 점수 마커 */}
                  <div
                    className="absolute top-0 h-4 w-1 bg-yellow-500 rounded-full"
                    style={{ left: '65%' }}
                  >
                    <div className="absolute -bottom-8 left-1/2 transform -translate-x-1/2">
                      <div className="bg-yellow-500 text-white text-xs px-2 py-1 rounded-full font-medium">
                        평균: 65점
                      </div>
                    </div>
                  </div>

                  {/* 목표 점수 마커 */}
                  <div
                    className="absolute top-0 h-4 w-1 bg-green-500 rounded-full"
                    style={{ left: '85%' }}
                  >
                    <div className="absolute -top-8 left-1/2 transform -translate-x-1/2">
                      <div className="bg-green-500 text-white text-xs px-2 py-1 rounded-full font-medium">
                        목표: 85점
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              {/* 분석 결과 */}
              <div className="grid grid-cols-3 gap-3 mt-6">
                <div className="text-center p-3 bg-blue-50 rounded-lg">
                  <div className="text-2xl font-bold text-blue-600">{dashboard?.myJobScore || 0}</div>
                  <div className="text-xs text-blue-700">나의 점수</div>
                  <div className="text-xs text-blue-500 mt-1">
                    상위 {Math.max(0, 100 - (dashboard?.myJobScore || 0))}%
                  </div>
                </div>
                <div className="text-center p-3 bg-yellow-50 rounded-lg">
                  <div className="text-2xl font-bold text-yellow-600">65</div>
                  <div className="text-xs text-yellow-700">시장 평균</div>
                  <div className="text-xs text-yellow-500 mt-1">
                    {(dashboard?.myJobScore || 0) >= 65 ? '평균 이상' : '평균 이하'}
                  </div>
                </div>
                <div className="text-center p-3 bg-green-50 rounded-lg">
                  <div className="text-2xl font-bold text-green-600">85</div>
                  <div className="text-xs text-green-700">목표 점수</div>
                  <div className="text-xs text-green-500 mt-1">
                    {Math.max(0, 85 - (dashboard?.myJobScore || 0))}점 필요
                  </div>
                </div>
              </div>

              {/* AI 분석 피드백 */}
              <div className="mt-4 p-4 bg-gradient-to-r from-blue-500 to-blue-600 rounded-lg text-white">
                <div className="flex items-center gap-2 mb-2">
                  <Target className="w-4 h-4" />
                  <span className="font-medium">AI 분석 결과</span>
                </div>
                <p className="text-sm opacity-95">
                  {(dashboard?.myJobScore || 0) >= 85 ?
                    '훌륭합니다! 목표 점수에 도달했습니다. 이제 실전 면접 준비에 집중해보세요.' :
                    (dashboard?.myJobScore || 0) >= 65 ?
                    '평균 이상의 좋은 점수입니다. 조금 더 노력하면 목표에 도달할 수 있어요.' :
                    '아직 개선의 여지가 많습니다. 체계적인 준비를 통해 점수를 높여보세요.'
                  }
                </p>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* 2. 현재 역량 분석 (원형 그래프 + 색상별 점수 표시) */}
        <Card className="shadow-lg">
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <PieChart className="w-5 h-5 text-purple-600" />
              현재 역량 분석
            </CardTitle>
            <p className="text-sm text-gray-600 mt-1">
              6가지 핵심 역량을 시각화하여 강점과 약점을 파악합니다
            </p>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
              {/* 원형 그래프 */}
              <div className="flex flex-col items-center justify-center">
                <div className="h-48 w-48 relative">
                  <ResponsiveContainer width="100%" height="100%">
                    <RechartsPieChart>
                      <Pie
                        data={capabilityData}
                        cx="50%"
                        cy="50%"
                        innerRadius={35}
                        outerRadius={80}
                        dataKey="level"
                        startAngle={90}
                        endAngle={450}
                      >
                        {capabilityData.map((entry, index) => (
                          <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                        ))}
                      </Pie>
                      <Tooltip
                        formatter={(value: number, name: string) => [`${value}점`, name]}
                      />
                    </RechartsPieChart>
                  </ResponsiveContainer>

                  {/* 중앙 평균 점수 */}
                  <div className="absolute inset-0 flex items-center justify-center">
                    <div className="text-center">
                      <div className="text-3xl font-bold text-purple-700">
                        {averageCapability.toFixed(1)}
                      </div>
                      <div className="text-xs text-purple-600">종합 평균</div>
                    </div>
                  </div>
                </div>

                {/* 평가 등급 */}
                <div className="mt-4 p-3 rounded-lg" style={{
                  backgroundColor: averageCapability >= 80 ? '#dcfce7' : averageCapability >= 60 ? '#fef3c7' : '#fee2e2'
                }}>
                  <p className={`text-sm font-medium ${
                    averageCapability >= 80 ? 'text-green-800' : averageCapability >= 60 ? 'text-yellow-800' : 'text-red-800'
                  }`}>
                    {averageCapability >= 80 ? '🎉 우수 등급' : averageCapability >= 60 ? '😊 양호 등급' : '📚 개선 필요'}
                  </p>
                  <p className={`text-xs mt-1 ${
                    averageCapability >= 80 ? 'text-green-600' : averageCapability >= 60 ? 'text-yellow-600' : 'text-red-600'
                  }`}>
                    {averageCapability >= 80
                      ? '모든 역량이 균형있게 발달되어 있습니다'
                      : averageCapability >= 60
                      ? '전반적으로 양호하나 일부 개선이 필요합니다'
                      : '체계적인 역량 개발이 필요합니다'
                    }
                  </p>
                </div>
              </div>

              {/* 분야별 점수 및 색상 매칭 */}
              <div className="space-y-3">
                <h4 className="font-medium text-gray-900 mb-4">분야별 상세 점수</h4>
                {capabilityData.map((capability, index) => (
                  <div key={index} className="flex items-center justify-between p-3 rounded-lg border">
                    <div className="flex items-center gap-3">
                      {/* 색상 인디케이터 */}
                      <div
                        className="w-4 h-4 rounded-full flex-shrink-0"
                        style={{ backgroundColor: COLORS[index % COLORS.length] }}
                      ></div>

                      {/* 역량명 */}
                      <div className="flex-1">
                        <div className="font-medium text-gray-900">{capability.skill}</div>

                        {/* 프로그레스 바 */}
                        <div className="mt-1">
                          <div className="h-2 bg-gray-200 rounded-full overflow-hidden">
                            <div
                              className="h-full rounded-full transition-all duration-500"
                              style={{
                                width: `${capability.level}%`,
                                backgroundColor: COLORS[index % COLORS.length]
                              }}
                            ></div>
                          </div>
                        </div>
                      </div>
                    </div>

                    {/* 점수 표시 */}
                    <div className="text-right ml-4">
                      <div className="text-lg font-bold" style={{ color: COLORS[index % COLORS.length] }}>
                        {capability.level}점
                      </div>
                      <div className="text-xs text-gray-500">
                        {capability.level >= 80 ? '우수' : capability.level >= 60 ? '양호' : '개선필요'}
                      </div>
                    </div>
                  </div>
                ))}

                {/* 개선 제안 */}
                <div className="mt-6 p-4 bg-gradient-to-r from-purple-500 to-purple-600 rounded-lg text-white">
                  <div className="flex items-center gap-2 mb-2">
                    <Target className="w-4 h-4" />
                    <span className="font-medium">개선 제안</span>
                  </div>
                  <p className="text-sm opacity-95">
                    {capabilityData.length > 0 && (() => {
                      const lowestCapability = capabilityData.reduce((min, current) =>
                        current.level < min.level ? current : min
                      );
                      const highestCapability = capabilityData.reduce((max, current) =>
                        current.level > max.level ? current : max
                      );

                      if (lowestCapability.level < 60) {
                        return `${lowestCapability.skill} 역량이 ${lowestCapability.level}점으로 가장 낮습니다. 이 분야의 집중적인 학습을 추천합니다.`;
                      } else if (highestCapability.level >= 80) {
                        return `${highestCapability.skill} 역량이 ${highestCapability.level}점으로 우수합니다. 이 강점을 더욱 발전시켜보세요.`;
                      } else {
                        return '전반적으로 균형잡힌 역량을 보유하고 있습니다. 모든 영역을 고르게 발전시켜보세요.';
                      }
                    })()}
                  </p>
                </div>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* 3. 맞춤 활동 추천 */}
        <Card className="shadow-lg">
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <BookOpen className="w-5 h-5 text-green-600" />
              맞춤 활동 추천
            </CardTitle>
            <p className="text-sm text-gray-600 mt-1">
              현재 상태를 분석하여 필요한 활동을 추천합니다
            </p>
          </CardHeader>
          <CardContent>
            <div className="space-y-3">
              {recommendations.slice(0, 5).map((recommendation, index) => (
                <div key={index} className="flex items-center gap-3 p-3 bg-green-50 rounded-lg">
                  <CheckCircle className="w-5 h-5 text-green-600" />
                  <span className="text-sm font-medium text-green-800">{recommendation}</span>
                </div>
              ))}
            </div>
            <div className="mt-4 p-4 bg-gradient-to-r from-green-500 to-emerald-600 rounded-lg text-white">
              <div className="flex items-center gap-2 mb-2">
                <Zap className="w-5 h-5" />
                <p className="font-semibold">💡 AI 분석 피드백</p>
              </div>
              <p className="text-sm opacity-95">
                {dashboard?.personalInsight?.overallFeedback || '더 많은 활동을 통해 경쟁력을 높여보세요!'}
              </p>
            </div>
          </CardContent>
        </Card>

        {/* 4. 지원 현황 파이 차트 */}
        <Card className="shadow-lg">
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <PieChart className="w-5 h-5 text-orange-600" />
              지원 현황 분석
            </CardTitle>
            <p className="text-sm text-gray-600 mt-1">
              전체 지원 건수 대비 진행 상태를 한눈에 확인합니다
            </p>
          </CardHeader>
          <CardContent>
            <div className="h-64">
              <ResponsiveContainer width="100%" height="100%">
                <RechartsPieChart>
                  <Pie
                    data={applicationData}
                    cx="50%"
                    cy="50%"
                    outerRadius={80}
                    dataKey="value"
                    label={({ name, value, percent }) => value > 0 ? `${name}: ${value}건 (${(percent * 100).toFixed(0)}%)` : ''}
                  >
                    {applicationData.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={entry.color} />
                    ))}
                  </Pie>
                  <Tooltip 
                    formatter={(value: number) => [`${value}건`, '지원 건수']}
                    labelStyle={{ color: '#1f2937' }}
                  />
                </RechartsPieChart>
              </ResponsiveContainer>
            </div>
            <div className="mt-4 grid grid-cols-2 gap-2">
              {applicationData.map((item, index) => (
                <div key={index} className="flex items-center gap-2 p-2 rounded-lg" style={{ backgroundColor: `${item.color}15` }}>
                  <div 
                    className="w-3 h-3 rounded-full" 
                    style={{ backgroundColor: item.color }}
                  ></div>
                  <div className="flex-1">
                    <span className="text-sm font-medium" style={{ color: item.color }}>{item.name}</span>
                    <span className="text-sm font-bold ml-2">{item.value}건</span>
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      </div>

      {/* 직무별 취업률 및 월별 진행 상황 */}
      <div className="grid grid-cols-1 xl:grid-cols-2 gap-6">
        
        {/* 직무별 취업률 */}
        <Card className="shadow-lg">
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <TrendingUp className="w-5 h-5 text-blue-600" />
              직무별 취업률 비교
            </CardTitle>
            <p className="text-sm text-gray-600 mt-1">
              인기 직무의 시장 취업률을 비교 분석합니다
            </p>
          </CardHeader>
          <CardContent>
            <div className="h-64">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={jobFieldData}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="jobField" />
                  <YAxis label={{ value: '취업률 (%)', angle: -90, position: 'insideLeft' }} />
                  <Tooltip 
                    formatter={(value: number) => [`${value}%`, '취업률']}
                    labelStyle={{ color: '#1f2937' }}
                  />
                  <Bar dataKey="employmentRate" fill="#10b981" radius={[8, 8, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </div>
            <div className="mt-4 grid grid-cols-2 gap-4">
              {jobFieldData.map((field, index) => (
                <div key={index} className="p-3 bg-green-50 rounded-lg">
                  <div className="flex items-center justify-between">
                    <span className="text-sm font-medium text-green-900">{field.jobField}</span>
                    <span className="text-lg font-bold text-green-600">{field.employmentRate}%</span>
                  </div>
                  <div className="text-xs text-green-600 mt-1">
                    총 {field.totalApplicants}명 중 {field.employedCount}명 취업
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>

        {/* 월별 진행 상황 */}
        <Card className="shadow-lg">
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Calendar className="w-5 h-5 text-purple-600" />
              월별 취업 활동 동향
            </CardTitle>
            <p className="text-sm text-gray-600 mt-1">
              최근 5개월간 나의 취업 활동 비교 분석
            </p>
          </CardHeader>
          <CardContent>
            <div className="h-64">
              <ResponsiveContainer width="100%" height="100%">
                <LineChart data={monthlyProgressData}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="month" />
                  <YAxis label={{ value: '건수', angle: -90, position: 'insideLeft' }} />
                  <Tooltip 
                    formatter={(value: number, name: string) => [`${value}건`, name]}
                    labelStyle={{ color: '#1f2937' }}
                  />
                  <Line 
                    type="monotone" 
                    dataKey="applications" 
                    stroke="#3b82f6" 
                    strokeWidth={3}
                    name="지원 건수" 
                    dot={{ r: 4 }}
                  />
                  <Line 
                    type="monotone" 
                    dataKey="interviews" 
                    stroke="#8b5cf6" 
                    strokeWidth={3}
                    name="면접 건수" 
                    dot={{ r: 4 }}
                  />
                  <Line 
                    type="monotone" 
                    dataKey="offers" 
                    stroke="#10b981" 
                    strokeWidth={3}
                    name="합격 건수" 
                    dot={{ r: 4 }}
                  />
                </LineChart>
              </ResponsiveContainer>
            </div>
            <div className="mt-4 grid grid-cols-3 gap-4">
              <div className="p-3 bg-blue-50 rounded-lg text-center">
                <div className="flex items-center justify-center gap-1 mb-1">
                  <div className="w-3 h-3 bg-blue-500 rounded-full"></div>
                  <span className="text-sm font-medium text-blue-900">지원</span>
                </div>
                <p className="text-lg font-bold text-blue-600">
                  {monthlyProgressData[monthlyProgressData.length - 1]?.applications || 0}건
                </p>
                <p className="text-xs text-blue-500">이번 달</p>
              </div>
              <div className="p-3 bg-purple-50 rounded-lg text-center">
                <div className="flex items-center justify-center gap-1 mb-1">
                  <div className="w-3 h-3 bg-purple-500 rounded-full"></div>
                  <span className="text-sm font-medium text-purple-900">면접</span>
                </div>
                <p className="text-lg font-bold text-purple-600">
                  {monthlyProgressData[monthlyProgressData.length - 1]?.interviews || 0}건
                </p>
                <p className="text-xs text-purple-500">이번 달</p>
              </div>
              <div className="p-3 bg-green-50 rounded-lg text-center">
                <div className="flex items-center justify-center gap-1 mb-1">
                  <div className="w-3 h-3 bg-green-500 rounded-full"></div>
                  <span className="text-sm font-medium text-green-900">합격</span>
                </div>
                <p className="text-lg font-bold text-green-600">
                  {monthlyProgressData[monthlyProgressData.length - 1]?.offers || 0}건
                </p>
                <p className="text-xs text-green-500">이번 달</p>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* 빠른 실행 메뉴 */}
      <Card className="shadow-lg">
        <CardHeader>
          <CardTitle>빠른 실행</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <Link to="/ai/interview" className="group">
              <div className="p-4 rounded-lg bg-gradient-to-r from-blue-500 to-blue-600 text-white text-center hover:shadow-lg transition-all duration-300 group-hover:scale-105">
                <MessageSquare className="w-6 h-6 mx-auto mb-2" />
                <span className="text-sm font-medium">AI 면접</span>
              </div>
            </Link>
            <Link to="/jobs" className="group">
              <div className="p-4 rounded-lg bg-gradient-to-r from-green-500 to-emerald-600 text-white text-center hover:shadow-lg transition-all duration-300 group-hover:scale-105">
                <Briefcase className="w-6 h-6 mx-auto mb-2" />
                <span className="text-sm font-medium">맞춤 공고</span>
              </div>
            </Link>
            <Link to="/profile" className="group">
              <div className="p-4 rounded-lg bg-gradient-to-r from-purple-500 to-purple-600 text-white text-center hover:shadow-lg transition-all duration-300 group-hover:scale-105">
                <UserCheck className="w-6 h-6 mx-auto mb-2" />
                <span className="text-sm font-medium">프로필 수정</span>
              </div>
            </Link>
            <Link to="/ai/cover-letter" className="group">
              <div className="p-4 rounded-lg bg-gradient-to-r from-orange-500 to-red-500 text-white text-center hover:shadow-lg transition-all duration-300 group-hover:scale-105">
                <FileText className="w-6 h-6 mx-auto mb-2" />
                <span className="text-sm font-medium">자기소개서</span>
              </div>
            </Link>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}

/**
 * 기업 사용자 대시보드 컴포넌트
 * 채용공고 현황, 지원자 관리, 기업 통계 등을 제공
 * 이벤트: 기업 대시보드 데이터 조회 이벤트, 채용 통계 렌더링 이벤트, 지원자 현황 업데이트 이벤트
 */
const CompanyDashboardComponent = () => {
  const { data: dashboardData, isLoading, error, refetch, isRefetching } = useQuery({
    queryKey: ['company-dashboard'],
    queryFn: () => apiClient.getCompanyDashboard(),
    staleTime: 5 * 60 * 1000,
    retry: 3,
    retryDelay: (attemptIndex) => Math.min(1000 * 2 ** attemptIndex, 30000),
    refetchOnWindowFocus: false,
  })

  if (isLoading) {
    return (
      <div className="space-y-8 p-6">
        <div className="text-center md:text-left">
          <h1 className="text-4xl font-bold text-gray-900 mb-2">기업 대시보드</h1>
          <p className="text-xl text-gray-600">데이터를 불러오는 중...</p>
        </div>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          {Array.from({ length: 4 }).map((_, i) => (
            <LoadingCard key={i} variant="elevated" />
          ))}
        </div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="space-y-8 p-6">
        <div className="text-center md:text-left">
          <h1 className="text-4xl font-bold text-gray-900 mb-2">기업 대시보드</h1>
          <p className="text-xl text-gray-600">데이터를 불러오는 중 문제가 발생했습니다</p>
        </div>
        <Card variant="elevated" className="max-w-2xl mx-auto">
          <CardContent className="p-8">
            <ErrorMessage
              title="대시보드 로딩 실패"
              message="기업 대시보드 데이터를 불러오는 중 오류가 발생했습니다."
              onRetry={() => refetch()}
              retryLabel={isRefetching ? "다시 시도 중..." : "다시 시도"}
            />
          </CardContent>
        </Card>
      </div>
    )
  }

  const dashboard = dashboardData?.data

  return (
    <div className="space-y-8 p-6">
      {/* 헤더 */}
      <div className="text-center md:text-left">
        <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
          <div>
            <h1 className="text-4xl font-bold text-gray-900 mb-2">기업 대시보드</h1>
            <p className="text-xl text-gray-600">채용 현황과 기업 활동을 한눈에 확인하세요</p>
          </div>
          <div className="flex items-center gap-3">
            <Button
              variant="outline"
              size="sm"
              onClick={() => refetch()}
              disabled={isRefetching}
              className="flex items-center gap-2"
            >
              {isRefetching ? (
                <Loader2 className="w-4 h-4 animate-spin" />
              ) : (
                <RefreshCw className="w-4 h-4" />
              )}
              {isRefetching ? '새로고침 중...' : '새로고침'}
            </Button>
          </div>
        </div>
      </div>

      {/* 메인 통계 카드 */}
      <section className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-6">
        <Card className="hover:shadow-lg transition-all duration-300 border-0 shadow-md">
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600 mb-1">등록한 채용공고</p>
                <p className="text-3xl font-bold text-gray-900">{dashboard?.totalJobPostings || 0}</p>
                <p className="text-xs text-blue-600 mt-1">전체 공고</p>
              </div>
              <div className="p-3 bg-gradient-to-r from-blue-500 to-blue-600 rounded-xl shadow-lg">
                <Briefcase className="w-8 h-8 text-white" />
              </div>
            </div>
          </CardContent>
        </Card>

        <Card className="hover:shadow-lg transition-all duration-300 border-0 shadow-md">
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600 mb-1">활성 공고</p>
                <p className="text-3xl font-bold text-gray-900">{dashboard?.activeJobPostings || 0}</p>
                <p className="text-xs text-green-600 mt-1">현재 진행중</p>
              </div>
              <div className="p-3 bg-gradient-to-r from-green-500 to-emerald-600 rounded-xl shadow-lg">
                <TrendingUp className="w-8 h-8 text-white" />
              </div>
            </div>
          </CardContent>
        </Card>

        <Card className="hover:shadow-lg transition-all duration-300 border-0 shadow-md">
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600 mb-1">총 지원자</p>
                <p className="text-3xl font-bold text-gray-900">{dashboard?.totalApplications || 0}</p>
                <p className="text-xs text-purple-600 mt-1">누적 지원</p>
              </div>
              <div className="p-3 bg-gradient-to-r from-purple-500 to-purple-600 rounded-xl shadow-lg">
                <Users className="w-8 h-8 text-white" />
              </div>
            </div>
          </CardContent>
        </Card>

        <Card className="hover:shadow-lg transition-all duration-300 border-0 shadow-md">
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600 mb-1">신규 지원자</p>
                <p className="text-3xl font-bold text-gray-900">{dashboard?.newApplicationsThisWeek || 0}</p>
                <p className="text-xs text-orange-600 mt-1">이번 주</p>
              </div>
              <div className="p-3 bg-gradient-to-r from-orange-500 to-red-500 rounded-xl shadow-lg">
                <UserCheck className="w-8 h-8 text-white" />
              </div>
            </div>
          </CardContent>
        </Card>
      </section>

      {/* 지원자 현황 분석 및 인기 공고 */}
      <div className="grid grid-cols-1 xl:grid-cols-2 gap-6">
        {/* 지원자 현황 분석 */}
        <Card className="shadow-lg">
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <BarChart3 className="w-5 h-5 text-blue-600" />
              지원자 현황 분석
            </CardTitle>
            <p className="text-sm text-gray-600 mt-1">
              지원자들의 단계별 현황을 확인하세요
            </p>
          </CardHeader>
          <CardContent>
            {dashboard?.applicationStatistics && (
              <div className="space-y-4">
                <div className="grid grid-cols-2 gap-4">
                  <div className="p-4 bg-yellow-50 rounded-lg">
                    <div className="flex items-center justify-between">
                      <span className="text-sm font-medium text-yellow-900">검토 대기</span>
                      <span className="text-lg font-bold text-yellow-600">{dashboard.applicationStatistics.pendingReview}</span>
                    </div>
                  </div>
                  <div className="p-4 bg-blue-50 rounded-lg">
                    <div className="flex items-center justify-between">
                      <span className="text-sm font-medium text-blue-900">서류 통과</span>
                      <span className="text-lg font-bold text-blue-600">{dashboard.applicationStatistics.documentPassed}</span>
                    </div>
                  </div>
                  <div className="p-4 bg-purple-50 rounded-lg">
                    <div className="flex items-center justify-between">
                      <span className="text-sm font-medium text-purple-900">면접 예정</span>
                      <span className="text-lg font-bold text-purple-600">{dashboard.applicationStatistics.interviewScheduled}</span>
                    </div>
                  </div>
                  <div className="p-4 bg-green-50 rounded-lg">
                    <div className="flex items-center justify-between">
                      <span className="text-sm font-medium text-green-900">최종 합격</span>
                      <span className="text-lg font-bold text-green-600">{dashboard.applicationStatistics.finalPassed}</span>
                    </div>
                  </div>
                </div>
                <div className="p-4 bg-gray-50 rounded-lg">
                  <div className="flex items-center justify-between">
                    <span className="text-sm font-medium text-gray-700">평균 지원자 수</span>
                    <span className="text-lg font-bold text-gray-800">{dashboard.applicationStatistics.averageApplicationsPerPosting?.toFixed(1) || '0.0'}</span>
                  </div>
                </div>
              </div>
            )}
          </CardContent>
        </Card>

        {/* 인기 채용공고 */}
        <Card className="shadow-lg">
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Award className="w-5 h-5 text-gold-600" />
              인기 채용공고
            </CardTitle>
            <p className="text-sm text-gray-600 mt-1">
              조회수와 지원자 수가 많은 공고들을 확인하세요
            </p>
          </CardHeader>
          <CardContent>
            <div className="space-y-3">
              {dashboard?.popularJobPostings?.slice(0, 5).map((job: any, index: number) => (
                <div key={job.jobPostingId} className="flex items-center gap-3 p-3 bg-gray-50 rounded-lg">
                  <div className="flex items-center justify-center w-8 h-8 bg-blue-600 text-white rounded-full text-sm font-bold">
                    {index + 1}
                  </div>
                  <div className="flex-1">
                    <p className="font-medium text-gray-900 truncate">{job.title}</p>
                    <p className="text-sm text-gray-600">{job.companyName}</p>
                  </div>
                  <div className="text-right">
                    <p className="text-sm font-medium text-blue-600">{job.applicationCount}명 지원</p>
                    <p className="text-xs text-gray-500">{job.viewCount}회 조회</p>
                  </div>
                </div>
              ))}
              {(!dashboard?.popularJobPostings || dashboard.popularJobPostings.length === 0) && (
                <div className="text-center py-8 text-gray-500">
                  <Building2 className="w-12 h-12 mx-auto mb-3 text-gray-300" />
                  <p>아직 채용공고가 없습니다</p>
                  <p className="text-sm">첫 번째 채용공고를 등록해보세요!</p>
                </div>
              )}
            </div>
          </CardContent>
        </Card>
      </div>

      {/* 내가 올린 공고 리스트 */}
      <Card className="shadow-lg">
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <FileText className="w-5 h-5 text-green-600" />
            내가 올린 공고
          </CardTitle>
          <p className="text-sm text-gray-600 mt-1">
            등록한 채용공고들을 관리하고 현황을 확인하세요
          </p>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {dashboard?.myJobPostings?.map((job: any) => (
              <div key={job.id} className="border rounded-lg p-4 hover:shadow-md transition-shadow">
                <div className="flex items-start justify-between">
                  <div className="flex-1">
                    <h3 className="font-semibold text-lg text-gray-900 mb-2">{job.title}</h3>
                    <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
                      <div>
                        <span className="text-gray-500">위치:</span>
                        <span className="ml-2 font-medium">{job.location}</span>
                      </div>
                      <div>
                        <span className="text-gray-500">경력:</span>
                        <span className="ml-2 font-medium">{job.experienceLevel}</span>
                      </div>
                      <div>
                        <span className="text-gray-500">급여:</span>
                        <span className="ml-2 font-medium">{job.minSalary}-{job.maxSalary}만원</span>
                      </div>
                      <div>
                        <span className="text-gray-500">지원자:</span>
                        <span className="ml-2 font-medium text-blue-600">{job.applicationCount}명</span>
                      </div>
                    </div>
                  </div>
                  <div className="flex flex-col items-end gap-2">
                    <Badge
                      variant={job.status === 'PUBLISHED' ? 'success' : job.status === 'DRAFT' ? 'warning' : 'secondary'}
                    >
                      {job.status === 'PUBLISHED' ? '게시중' : job.status === 'DRAFT' ? '초안' : job.status}
                    </Badge>
                    <div className="text-sm text-gray-500">
                      {job.viewCount}회 조회
                    </div>
                  </div>
                </div>
              </div>
            ))}
            {(!dashboard?.myJobPostings || dashboard.myJobPostings.length === 0) && (
              <div className="text-center py-12 text-gray-500">
                <Briefcase className="w-16 h-16 mx-auto mb-4 text-gray-300" />
                <p className="text-lg font-medium mb-2">등록된 채용공고가 없습니다</p>
                <p className="text-sm mb-4">첫 번째 채용공고를 등록하여 우수한 인재를 찾아보세요!</p>
              </div>
            )}
          </div>
        </CardContent>
      </Card>

      {/* 빠른 실행 메뉴 */}
      <Card className="shadow-lg">
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Zap className="w-5 h-5 text-yellow-600" />
            빠른 실행
          </CardTitle>
          <p className="text-sm text-gray-600 mt-1">
            자주 사용하는 기능들을 빠르게 실행하세요
          </p>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <Link to="/jobs/create" className="group">
              <div className="p-4 rounded-lg bg-gradient-to-r from-blue-500 to-blue-600 text-white text-center hover:shadow-lg transition-all duration-300 group-hover:scale-105">
                <FileText className="w-6 h-6 mx-auto mb-2" />
                <span className="text-sm font-medium">채용공고 작성</span>
              </div>
            </Link>
            <Link to="/applications/manage" className="group">
              <div className="p-4 rounded-lg bg-gradient-to-r from-green-500 to-emerald-600 text-white text-center hover:shadow-lg transition-all duration-300 group-hover:scale-105">
                <Users className="w-6 h-6 mx-auto mb-2" />
                <span className="text-sm font-medium">지원자 관리</span>
              </div>
            </Link>
            <Link to="/company/profile" className="group">
              <div className="p-4 rounded-lg bg-gradient-to-r from-purple-500 to-purple-600 text-white text-center hover:shadow-lg transition-all duration-300 group-hover:scale-105">
                <Building2 className="w-6 h-6 mx-auto mb-2" />
                <span className="text-sm font-medium">기업 정보 관리</span>
              </div>
            </Link>
            <Link to="/company/statistics" className="group">
              <div className="p-4 rounded-lg bg-gradient-to-r from-orange-500 to-red-500 text-white text-center hover:shadow-lg transition-all duration-300 group-hover:scale-105">
                <BarChart3 className="w-6 h-6 mx-auto mb-2" />
                <span className="text-sm font-medium">통계 보기</span>
              </div>
            </Link>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}