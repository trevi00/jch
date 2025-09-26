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
  WifiOff,            // 📶 오프라인/연결 끊김
  Settings            // ⚙️ 설정
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
  Area,                  // 영역 요소
  ReferenceLine          // 참조선 요소
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

  // 최신 채용공고 데이터 가져오기
  const { data: latestJobsData } = useQuery({
    queryKey: ['latestJobs'],
    queryFn: () => apiClient.getJobPostings(undefined, 0, 1), // 첫 번째 공고만 가져오기
    staleTime: 5 * 60 * 1000,
    retry: 2
  })

  // 사용자 프로필 상세 데이터 가져오기
  const { data: educationData } = useQuery({
    queryKey: ['profile-education'],
    queryFn: () => apiClient.api.get('/api/profile/education'),
    enabled: true,
    retry: 1
  })

  const { data: skillsData } = useQuery({
    queryKey: ['profile-skills'],
    queryFn: () => apiClient.api.get('/api/profile/skills'),
    enabled: true,
    retry: 1
  })

  const { data: certificationsData } = useQuery({
    queryKey: ['profile-certifications'],
    queryFn: () => apiClient.api.get('/api/profile/certifications'),
    enabled: true,
    retry: 1
  })

  const { data: portfoliosData } = useQuery({
    queryKey: ['profile-portfolios'],
    queryFn: () => apiClient.api.get('/api/profile/portfolios'),
    enabled: true,
    retry: 1
  })

  const { data: experiencesData } = useQuery({
    queryKey: ['profile-experiences'],
    queryFn: () => apiClient.api.get('/api/profile/experiences'),
    enabled: true,
    retry: 1
  })

  // 전체 프로필 데이터 조합
  const userProfileData = {
    data: {
      educations: educationData?.data?.data || [],
      skills: skillsData?.data?.data || [],
      certifications: certificationsData?.data?.data || [],
      portfolios: portfoliosData?.data?.data || [],
      experiences: experiencesData?.data?.data || []
    }
  }

  const profileError = null // 개별 에러 처리는 추후 추가 가능

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

  // 사용자 프로필 데이터 기반 역량 점수 계산
  const calculateUserSpecScores = (profile: any) => {
    if (!profile) return null;

    // 학력 점수 계산 (0-100)
    const calculateEducationScore = (educations: any[]) => {
      if (!educations || educations.length === 0) return 0;

      const maxDegree = Math.max(...educations.map(edu => {
        const degreeScores: { [key: string]: number } = {
          'HIGH_SCHOOL': 20,
          'ASSOCIATE': 40,
          'BACHELOR': 60,
          'MASTER': 80,
          'DOCTORAL': 100
        };
        return degreeScores[edu.degree] || 0;
      }));

      // GPA 보너스 (최대 15점)
      const maxGpa = Math.max(...educations.map(edu => {
        if (!edu.gpa || !edu.maxGpa) return 0;
        return (edu.gpa / edu.maxGpa) * 15;
      }));

      return Math.min(100, maxDegree + maxGpa);
    };

    // 스킬 점수 계산 (0-100)
    const calculateSkillScore = (skills: any[]) => {
      if (!skills || skills.length === 0) return 0;

      const levelScores: { [key: string]: number } = {
        'BEGINNER': 1,
        'INTERMEDIATE': 2,
        'ADVANCED': 3,
        'EXPERT': 4
      };

      const totalScore = skills.reduce((sum, skill) => {
        return sum + (levelScores[skill.level] || 0);
      }, 0);

      // 스킬 개수와 레벨을 종합적으로 평가 (최대 100점)
      const skillCount = Math.min(skills.length, 10); // 최대 10개까지
      const avgLevel = totalScore / skills.length;

      return Math.min(100, (skillCount * 5) + (avgLevel * 15));
    };

    // 자격증 점수 계산 (0-100)
    const calculateCertificationScore = (certifications: any[]) => {
      if (!certifications || certifications.length === 0) return 0;

      // 자격증 개수 기반 점수 (개당 15점, 최대 7개)
      const certCount = Math.min(certifications.length, 7);
      const baseScore = certCount * 15;

      // 최신 자격증 보너스 (5점)
      const recentCert = certifications.some(cert => {
        const issueYear = new Date(cert.issueDate).getFullYear();
        const currentYear = new Date().getFullYear();
        return currentYear - issueYear <= 2;
      });

      return Math.min(100, baseScore + (recentCert ? 5 : 0));
    };

    // 포트폴리오 점수 계산 (0-100)
    const calculatePortfolioScore = (portfolios: any[]) => {
      if (!portfolios || portfolios.length === 0) return 0;

      const portfolioCount = Math.min(portfolios.length, 5); // 최대 5개
      const baseScore = portfolioCount * 15;

      // 타입별 보너스
      const typeBonus = portfolios.reduce((bonus, portfolio) => {
        const typeBonuses: { [key: string]: number } = {
          'WEB': 5,
          'MOBILE': 5,
          'DESIGN': 3,
          'DATA': 4,
          'OTHER': 2
        };
        return bonus + (typeBonuses[portfolio.type] || 0);
      }, 0);

      return Math.min(100, baseScore + Math.min(typeBonus, 25));
    };

    // 경력 점수 계산 (0-100)
    const calculateExperienceScore = (experiences: any[]) => {
      if (!experiences || experiences.length === 0) return 0;

      const totalMonths = experiences.reduce((months, exp) => {
        const startDate = new Date(exp.startDate);
        const endDate = exp.endDate ? new Date(exp.endDate) : new Date();
        const diffMonths = (endDate.getFullYear() - startDate.getFullYear()) * 12 +
                          (endDate.getMonth() - startDate.getMonth());
        return months + Math.max(0, diffMonths);
      }, 0);

      // 경력 기간별 점수 (월별 2점, 최대 90점)
      const timeScore = Math.min(90, totalMonths * 2);

      // 현재 재직중 보너스 (10점)
      const currentJobBonus = experiences.some(exp => exp.isCurrentJob) ? 10 : 0;

      return Math.min(100, timeScore + currentJobBonus);
    };

    const educationScore = calculateEducationScore(profile.educations);
    const skillScore = calculateSkillScore(profile.skills);
    const certificationScore = calculateCertificationScore(profile.certifications);
    const portfolioScore = calculatePortfolioScore(profile.portfolios);
    const experienceScore = calculateExperienceScore(profile.experiences);

    return {
      education: educationScore,
      skills: skillScore,
      certificates: certificationScore,
      portfolio: portfolioScore,
      experience: experienceScore
    };
  };

  // 임시 테스트 데이터 (프로필 데이터가 없을 때 사용)
  const mockUserSpec = {
    education: 75,
    skills: 85,
    certificates: 60,
    portfolio: 70,
    experience: 55
  };

  // 스펙별 점수 계산 (학력 20%, 스킬 25%, 자격증 10%, 포트폴리오 20%, 경력 25%)
  const calculateSpecScores = (userSpec: any) => {
    // 실제 프로필 데이터가 있으면 계산, 없으면 테스트 데이터 사용
    const calculatedSpec = userProfileData?.data ?
      calculateUserSpecScores(userProfileData.data) : null;

    const spec = calculatedSpec || userSpec || mockUserSpec;

    // 디버깅 정보 출력
    console.log('=== 역량 점수 계산 디버깅 ===');
    console.log('userProfileData:', userProfileData);
    console.log('calculatedSpec:', calculatedSpec);
    console.log('spec (최종 사용 데이터):', spec);
    console.log('profileError:', profileError);

    if (!spec) return [];

    const educationScore = Math.round((spec.education || 0) * 0.2);
    const skillScore = Math.round((spec.skills || 0) * 0.25);
    const certificateScore = Math.round((spec.certificates || 0) * 0.1);
    const portfolioScore = Math.round((spec.portfolio || 0) * 0.2);
    const experienceScore = Math.round((spec.experience || 0) * 0.25);

    console.log('최종 점수:', {
      education: educationScore,
      skill: skillScore,
      certificate: certificateScore,
      portfolio: portfolioScore,
      experience: experienceScore,
      total: educationScore + skillScore + certificateScore + portfolioScore + experienceScore
    });

    return [
      { name: '학력', score: educationScore, percentage: 20, color: '#3b82f6' },
      { name: '스킬', score: skillScore, percentage: 25, color: '#10b981' },
      { name: '자격증', score: certificateScore, percentage: 10, color: '#f59e0b' },
      { name: '포트폴리오', score: portfolioScore, percentage: 20, color: '#8b5cf6' },
      { name: '경력', score: experienceScore, percentage: 25, color: '#ef4444' }
    ];
  };

  // 스펙별 점수 데이터
  const specScores = calculateSpecScores(dashboard?.userSpec);
  const totalScore = specScores.reduce((sum, item) => sum + item.score, 0);

  // 정규분포 기반 상위 퍼센트 계산 (평균 65, 표준편차 15 가정)
  const calculatePercentile = (score: number) => {
    const mean = 65;
    const stdDev = 15;
    const z = (score - mean) / stdDev;
    // 정규분포 CDF 근사값 계산
    const percentile = 100 - (50 * (1 + Math.sign(z) * Math.sqrt(1 - Math.exp(-2 * z * z / Math.PI))));
    return Math.max(0, Math.min(100, Math.round(percentile)));
  };

  const myPercentile = calculatePercentile(totalScore);

  // 맞춤 활동 추천 로직 (낮은 점수 기준 + 실용적 제안)
  const generateRecommendations = () => {
    if (!specScores.length) return [];

    const recommendations = [];
    const sortedSpecs = [...specScores].sort((a, b) => a.score - b.score);
    const lowestSpecs = sortedSpecs.slice(0, 3);

    const recommendationMap: { [key: string]: {
      title: string;
      detail: string;
      actionItems: string[];
      urgency: 'high' | 'medium' | 'low';
    } } = {
      '학력': {
        title: '학력 보강',
        detail: '온라인 교육과정이나 관련 자격증으로 학력 부족분을 보완하세요',
        actionItems: [
          '국비지원 교육과정 수강 신청',
          'Coursera, edX 등 온라인 강의 수강',
          '관련 분야 학위/학점은행제 검토'
        ],
        urgency: 'medium'
      },
      '스킬': {
        title: '기술 역량 강화',
        detail: '실무에서 요구되는 핵심 기술을 집중적으로 학습하고 연습하세요',
        actionItems: [
          'GitHub에 프로젝트 코드 업로드 및 관리',
          '프로그래밍 언어별 온라인 코딩테스트 연습',
          'Stack Overflow, 기술 블로그 활용한 학습'
        ],
        urgency: 'high'
      },
      '자격증': {
        title: '자격증 취득',
        detail: '직무 관련성이 높은 자격증을 우선순위에 따라 취득하세요',
        actionItems: [
          'IT 관련: 정보처리기사, AWS/Azure 자격증',
          '어학: TOEIC, OPIC 등 어학 자격증',
          '직무별 전문자격증: PMP, 컴활, 토익스피킹'
        ],
        urgency: 'medium'
      },
      '포트폴리오': {
        title: '포트폴리오 구축',
        detail: '실무 능력을 보여줄 수 있는 다양한 프로젝트를 완성하세요',
        actionItems: [
          '개인 웹사이트/GitHub Pages 제작',
          '팀 프로젝트 참여 (해커톤, 스터디)',
          '기업 과제나 실제 서비스 개발 경험'
        ],
        urgency: 'high'
      },
      '경력': {
        title: '실무 경험 확장',
        detail: '인턴십이나 계약직으로 실무 경험을 쌓고 네트워크를 확장하세요',
        actionItems: [
          '인턴십 프로그램 지원 (정부지원 포함)',
          '프리랜서/계약직 프로젝트 참여',
          '멘토링 프로그램이나 업계 네트워킹 행사 참석'
        ],
        urgency: 'high'
      }
    };

    lowestSpecs.forEach((spec, index) => {
      if (recommendationMap[spec.name]) {
        recommendations.push({
          ...recommendationMap[spec.name],
          priority: index + 1,
          currentScore: spec.score,
          targetImprovement: Math.min(20, 20 - spec.score) // 최대 20점까지 개선 목표
        });
      }
    });

    return recommendations;
  };

  const recommendations = generateRecommendations();

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
              {isRefetching ? '새로고침 중...' : '새로고침'}
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
      <section aria-labelledby="main-stats" className="grid grid-cols-1 md:grid-cols-1 xl:grid-cols-2 gap-6">
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

        {latestJobsData?.data?.content?.[0] ? (
          <Link to={`/jobs/${latestJobsData.data.content[0].id}`}>
            <Card className="hover:shadow-lg transition-all duration-300 border-0 shadow-md cursor-pointer">
              <CardContent className="p-6">
                <div className="flex items-center justify-between">
                  <div className="flex-1">
                    <p className="text-sm font-medium text-gray-600 mb-1">최신 공고</p>
                    <p className="text-lg font-bold text-gray-900 line-clamp-2">
                      {latestJobsData.data.content[0].title}
                    </p>
                    <p className="text-xs text-gray-500 mt-1">{latestJobsData.data.content[0].companyName}</p>
                    <div className="flex items-center gap-2 mt-2">
                      <Badge variant="outline" className="text-xs">
                        {latestJobsData.data.content[0].jobType}
                      </Badge>
                      <Badge variant="outline" className="text-xs">
                        {latestJobsData.data.content[0].location}
                      </Badge>
                    </div>
                  </div>
                  <div className="p-3 bg-gradient-to-r from-green-500 to-emerald-600 rounded-xl shadow-lg">
                    <Calendar className="w-8 h-8 text-white" />
                  </div>
                </div>
              </CardContent>
            </Card>
          </Link>
        ) : (
          <Card className="hover:shadow-lg transition-all duration-300 border-0 shadow-md opacity-50">
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <div className="flex-1">
                  <p className="text-sm font-medium text-gray-600 mb-1">최신 공고</p>
                  <p className="text-lg font-bold text-gray-900 line-clamp-2">
                    등록된 공고가 없습니다
                  </p>
                  <p className="text-xs text-gray-500 mt-1">새로운 공고를 기다려보세요</p>
                </div>
                <div className="p-3 bg-gradient-to-r from-gray-400 to-gray-500 rounded-xl shadow-lg">
                  <Calendar className="w-8 h-8 text-white" />
                </div>
              </div>
            </CardContent>
          </Card>
        )}

        {/* <Card className="hover:shadow-lg transition-all duration-300 border-0 shadow-md">
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
        </Card> */}

        {/* <Card className="hover:shadow-lg transition-all duration-300 border-0 shadow-md">
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
        </Card> */}
      </section>

      {/* 새로운 분석 섹션 */}
      <div className="grid grid-cols-1 xl:grid-cols-2 gap-6">
        
        {/* 1. 취업 준비도 분석 (정규분포 그래프) */}
        <Card className="shadow-lg">
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <BarChart3 className="w-5 h-5 text-blue-600" />
              나의 현 상황
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
                    isMyScore: Math.abs(x - totalScore) < 2,
                    isAverage: Math.abs(x - 65) < 1
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
                  {/* 내 점수 위치 표시를 위한 세로선 */}
                  <ReferenceLine x={totalScore} stroke="#ef4444" strokeWidth={3} strokeDasharray="5 5" />
                  <ReferenceLine x={65} stroke="#f59e0b" strokeWidth={2} strokeDasharray="3 3" />
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
            <div className="mt-6 space-y-4">
              {/* 통계 요약 카드들 */}
              <div className="grid grid-cols-3 gap-4">
                <div className="bg-red-50 p-3 rounded-lg text-center border border-red-200">
                  <div className="text-red-600 text-sm font-medium mb-1">내 점수</div>
                  <div className="text-2xl font-bold text-red-700">{totalScore}점</div>
                </div>
                <div className="bg-yellow-50 p-3 rounded-lg text-center border border-yellow-200">
                  <div className="text-yellow-600 text-sm font-medium mb-1">평균 점수</div>
                  <div className="text-2xl font-bold text-yellow-700">65점</div>
                </div>
                <div className="bg-green-50 p-3 rounded-lg text-center border border-green-200">
                  <div className="text-green-600 text-sm font-medium mb-1">상위 퍼센트</div>
                  <div className="text-2xl font-bold text-green-700">{100 - myPercentile}%</div>
                </div>
              </div>

              {/* 위치 분석 바 */}
              <div className="relative">
                <div className="flex items-center justify-between mb-2">
                  <span className="text-sm text-gray-600">0점</span>
                  <span className="text-sm text-gray-600">100점</span>
                </div>
                <div className="relative h-6 bg-gray-200 rounded-full">
                  {/* 정규분포 배경 */}
                  <div className="absolute inset-0 bg-gradient-to-r from-red-200 via-yellow-200 via-blue-200 to-green-200 rounded-full opacity-50"></div>

                  {/* 나의 점수 마커 */}
                  <div
                    className="absolute top-0 h-6 w-2 bg-red-600 rounded-full shadow-lg"
                    style={{ left: `${totalScore}%` }}
                  >
                    <div className="absolute -top-10 left-1/2 transform -translate-x-1/2">
                      <div className="bg-red-600 text-white text-xs px-2 py-1 rounded-full font-medium shadow-lg">
                        나: {totalScore}점
                      </div>
                    </div>
                  </div>

                  {/* 평균 점수 마커 */}
                  <div
                    className="absolute top-0 h-6 w-2 bg-yellow-500 rounded-full shadow-lg"
                    style={{ left: '65%' }}
                  >
                    <div className="absolute -bottom-10 left-1/2 transform -translate-x-1/2">
                      <div className="bg-yellow-500 text-white text-xs px-2 py-1 rounded-full font-medium shadow-lg">
                        평균: 65점
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              {/* 상세 분석 */}
              <div className="bg-gradient-to-r from-blue-50 to-purple-50 p-4 rounded-lg">
                <h4 className="font-semibold text-gray-900 mb-2 flex items-center gap-2">
                  <BarChart3 className="w-4 h-4" />
                  정규분포 분석
                </h4>
                <div className="space-y-2 text-sm text-gray-700">
                  <p>
                    • 내 점수 <strong>{totalScore}점</strong>은 평균 65점보다 {totalScore >= 65 ? '높습니다' : '낮습니다'}
                    ({totalScore >= 65 ? '+' : ''}{totalScore - 65}점)
                  </p>
                  <p>
                    • 전체 지원자 중 상위 <strong>{100 - myPercentile}%</strong>에 해당합니다
                  </p>
                  <p className={`font-medium ${totalScore >= 80 ? 'text-green-600' : totalScore >= 60 ? 'text-yellow-600' : 'text-red-600'}`}>
                    • 종합 평가: {totalScore >= 80 ? '우수한 경쟁력을 보유하고 있습니다' :
                                 totalScore >= 60 ? '평균 이상의 준비상태입니다' : '추가 준비가 필요합니다'}
                  </p>
                </div>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* 2. 스펙별 역량 분석 (원형 그래프) */}
        <Card className="shadow-lg">
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <PieChart className="w-5 h-5 text-purple-600" />
              내 역량 점수
            </CardTitle>
            <p className="text-sm text-gray-600 mt-1">
              스펙별 점수 비율을 원형 차트로 시각화합니다 (학력 20%, 스킬 25%, 자격증 10%, 포트폴리오 20%, 경력 25%)
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
                        data={specScores}
                        cx="50%"
                        cy="50%"
                        innerRadius={35}
                        outerRadius={80}
                        dataKey="score"
                        startAngle={90}
                        endAngle={450}
                      >
                        {specScores.map((entry, index) => (
                          <Cell key={`cell-${index}`} fill={entry.color} />
                        ))}
                      </Pie>
                      <Tooltip
                        formatter={(value: number, name: string) => [`${value}점 (${specScores.find(s => s.name === name)?.percentage}%)`, name]}
                      />
                    </RechartsPieChart>
                  </ResponsiveContainer>

                  {/* 중앙 총합 점수 */}
                  <div className="absolute inset-0 flex items-center justify-center">
                    <div className="text-center">
                      <div className="text-3xl font-bold text-purple-700">
                        {totalScore}
                      </div>
                      <div className="text-xs text-purple-600">총 점수</div>
                    </div>
                  </div>
                </div>

                {/* 평가 등급 */}
                <div className="mt-4 p-3 rounded-lg" style={{
                  backgroundColor: totalScore >= 80 ? '#dcfce7' : totalScore >= 60 ? '#fef3c7' : '#fee2e2'
                }}>
                  <p className={`text-sm font-medium ${
                    totalScore >= 80 ? 'text-green-800' : totalScore >= 60 ? 'text-yellow-800' : 'text-red-800'
                  }`}>
                    {totalScore >= 80 ? '🎉 우수 등급' : totalScore >= 60 ? '😊 양호 등급' : '📚 개선 필요'}
                  </p>
                  <p className={`text-xs mt-1 ${
                    totalScore >= 80 ? 'text-green-600' : totalScore >= 60 ? 'text-yellow-600' : 'text-red-600'
                  }`}>
                    상위 {100 - myPercentile}% • {totalScore >= 80 ? '경쟁력이 우수합니다' : totalScore >= 60 ? '평균 이상의 역량을 보유하고 있습니다' : '체계적인 역량 개발이 필요합니다'}
                  </p>
                </div>
              </div>

              {/* 스펙별 점수 및 색상 매칭 */}
              <div className="space-y-3">
                <h4 className="font-medium text-gray-900 mb-4">스펙별 상세 점수</h4>
                {specScores.map((spec, index) => (
                  <div key={index} className="flex items-center justify-between p-3 rounded-lg border">
                    <div className="flex items-center gap-3">
                      {/* 색상 인디케이터 */}
                      <div
                        className="w-4 h-4 rounded-full flex-shrink-0"
                        style={{ backgroundColor: spec.color }}
                      ></div>

                      {/* 스펙명 */}
                      <div className="flex-1">
                        <div className="font-medium text-gray-900">{spec.name}</div>

                        {/* 프로그레스 바 */}
                        <div className="mt-1">
                          <div className="h-2 bg-gray-200 rounded-full overflow-hidden">
                            <div
                              className="h-full rounded-full transition-all duration-500"
                              style={{
                                width: `${spec.score}%`,
                                backgroundColor: spec.color
                              }}
                            ></div>
                          </div>
                        </div>
                      </div>
                    </div>

                    {/* 점수 표시 */}
                    <div className="text-right ml-4">
                      <div className="text-lg font-bold" style={{ color: spec.color }}>
                        {spec.score}점
                      </div>
                      <div className="text-xs text-gray-500">
                        {spec.percentage}% 비중
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      <div className="grid grid-cols-1 xl:grid-cols-1 gap-6">
        {/* 3. 맞춤 활동 추천 */}
        <Card className="shadow-lg">
          <CardHeader>
            <CardTitle className="flex items-center gap-1">
              <BookOpen className="w-5 h-5 text-green-600" />
              맞춤 활동 추천
            </CardTitle>
            <p className="text-sm text-gray-600 mt-1">
              현재 상태를 분석하여 필요한 활동을 추천합니다
            </p>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {recommendations.map((recommendation, index) => (
                <div key={index} className="border border-gray-200 rounded-lg p-4 hover:shadow-md transition-shadow">
                  <div className="flex items-start justify-between mb-3">
                    <div className="flex items-center gap-2">
                      <div className={`px-2 py-1 rounded-full text-xs font-medium ${
                        recommendation.urgency === 'high' ? 'bg-red-100 text-red-800' :
                        recommendation.urgency === 'medium' ? 'bg-yellow-100 text-yellow-800' :
                        'bg-green-100 text-green-800'
                      }`}>
                        우선순위 {recommendation.priority}
                      </div>
                      <div className={`px-2 py-1 rounded-full text-xs font-medium ${
                        recommendation.urgency === 'high' ? 'bg-red-500 text-white' :
                        recommendation.urgency === 'medium' ? 'bg-yellow-500 text-white' :
                        'bg-green-500 text-white'
                      }`}>
                        {recommendation.urgency === 'high' ? '긴급' :
                         recommendation.urgency === 'medium' ? '중요' : '보통'}
                      </div>
                    </div>
                    <div className="text-right">
                      <div className="text-sm text-gray-600">현재 {recommendation.currentScore}점</div>
                      <div className="text-xs text-green-600">+{recommendation.targetImprovement}점 목표</div>
                    </div>
                  </div>

                  <h4 className="font-semibold text-gray-900 mb-2">{recommendation.title}</h4>
                  <p className="text-sm text-gray-600 mb-3">{recommendation.detail}</p>

                  <div className="space-y-2">
                    <h5 className="text-xs font-medium text-gray-700 mb-2">구체적 실행 방안:</h5>
                    {recommendation.actionItems.map((item, itemIndex) => (
                      <div key={itemIndex} className="flex items-center gap-2 text-xs text-gray-600">
                        <CheckCircle className="w-3 h-3 text-green-500 flex-shrink-0" />
                        <span>{item}</span>
                      </div>
                    ))}
                  </div>
                </div>
              ))}
            </div>

            {/* 프로필 API 오류 또는 데이터 부족 안내 */}
            {profileError ? (
              <div className="mt-6 p-4 bg-gradient-to-r from-red-500 to-pink-600 rounded-lg text-white">
                <div className="flex items-center gap-2 mb-2">
                  <AlertCircle className="w-5 h-5" />
                  <p className="font-semibold">⚠️ 프로필 데이터 로드 오류</p>
                </div>
                <p className="text-sm opacity-95 mb-3">
                  프로필 정보를 불러오는 중 오류가 발생했습니다.
                  현재 테스트 데이터로 역량 분석을 표시하고 있습니다.
                </p>
                <div className="text-xs opacity-80">
                  오류: {profileError?.message || '알 수 없는 오류'}
                </div>
              </div>
            ) : (!userProfileData?.data ||
              (!userProfileData.data.educations?.length &&
               !userProfileData.data.skills?.length &&
               !userProfileData.data.experiences?.length)) && (
              <div className="mt-6 p-4 bg-gradient-to-r from-blue-500 to-purple-600 rounded-lg text-white">
                <div className="flex items-center gap-2 mb-2">
                  <Settings className="w-5 h-5" />
                  <p className="font-semibold">📝 프로필 정보가 부족합니다</p>
                </div>
                <p className="text-sm opacity-95 mb-3">
                  더 정확한 역량 분석을 위해 프로필 정보를 입력해주세요.
                  학력, 스킬, 자격증, 경력 등의 정보가 필요합니다.
                </p>
                <Link to="/settings">
                  <Button variant="secondary" size="sm" className="bg-white text-blue-600 hover:bg-gray-100">
                    프로필 설정하기
                  </Button>
                </Link>
              </div>
            )}

            <div className="mt-6 p-4 bg-gradient-to-r from-green-500 to-emerald-600 rounded-lg text-white">
              <div className="flex items-center gap-2 mb-2">
                <Zap className="w-5 h-5" />
                <p className="font-semibold">💡 AI 종합 분석</p>
              </div>
              <p className="text-sm opacity-95">
                {userProfileData?.data ? (
                  <>
                    총 {recommendations.length}개 영역에서 개선이 필요합니다.
                    {recommendations.filter(r => r.urgency === 'high').length > 0 &&
                      ` 특히 ${recommendations.filter(r => r.urgency === 'high').length}개 긴급 항목을 우선 집중하세요.`}
                    차근차근 개선해나가시면 {recommendations.reduce((sum, r) => sum + r.targetImprovement, 0)}점 향상이 가능합니다!
                  </>
                ) : (
                  '프로필 정보를 입력하시면 개인 맞춤형 분석과 추천을 받으실 수 있습니다.'
                )}
              </p>
            </div>
          </CardContent>
        </Card>

        {/* 4. 지원 현황 파이 차트
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
        </Card> */}
      {/* </div> */}

{/*         
        직무별 취업률
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
        </Card> */}

        {/* 월별 진행 상황
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
        </Card> */}

      </div>

      {/* 빠른 실행 메뉴
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
      </Card> */}
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
      <section className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6">
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

        {/* <Card className="hover:shadow-lg transition-all duration-300 border-0 shadow-md">
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
        </Card> */}

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
      <div className="grid grid-cols-1 xl:grid-cols-1 gap-6">
        {/* 지원자 현황 분석
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
        </Card> */}

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
              <Link
                key={job.id}
                to={`/jobs/${job.id}`}
                className="block border rounded-lg p-4 hover:shadow-md hover:border-blue-300 transition-all cursor-pointer"
              >
                <div className="flex items-start justify-between">
                  <div className="flex-1">
                    <h3 className="font-semibold text-lg text-gray-900 mb-2 hover:text-blue-600 transition-colors">{job.title}</h3>
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
                      {/* <div>
                        <span className="text-gray-500">지원자:</span>
                        <span className="ml-2 font-medium text-blue-600">{job.applicationCount}명</span>
                      </div> */}
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
              </Link>
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

      {/* 빠른 실행 메뉴
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
      </Card> */}
    </div>
  )
}