/**
 * App.tsx - 메인 애플리케이션 라우팅 관리 컴포넌트
 *
 * 🔧 사용 기술:
 * - React 18 (함수형 컴포넌트, Hooks)
 * - TypeScript (타입 안전성)
 * - React Router v6 (클라이언트 사이드 라우팅)
 * - Zustand (인증 상태 관리)
 *
 * 📋 주요 기능:
 * - 전체 애플리케이션 라우팅 구조 정의
 * - 인증 기반 접근 권한 제어 (Public/Protected Routes)
 * - 관리자 전용 라우트 분리 및 보호
 * - 사용자 타입별 라우트 구분 (일반/기업)
 *
 * 🎯 이벤트 처리:
 * - 라우트 변경: React Router의 자동 처리
 * - 인증 상태 변경: Zustand store 구독을 통한 자동 렌더링
 * - 권한 없는 접근: Navigate 컴포넌트를 통한 리다이렉트
 */

// React Router 관련 imports - 클라이언트 사이드 라우팅
import { Routes, Route, Navigate } from 'react-router-dom'

// 레이아웃 컴포넌트들 - 페이지 구조 및 권한 관리
import Layout from '@/components/Layout'           // 일반 사용자용 공통 레이아웃
import AdminLayout from '@/components/AdminLayout' // 관리자용 전용 레이아웃
import AdminRouteGuard from '@/components/AdminRouteGuard' // 관리자 권한 검증 컴포넌트

// 페이지 컴포넌트들 - 각 라우트에 매핑되는 실제 페이지들
// 🏠 공개 페이지 (인증 불필요)
import Home from '@/pages/Home'

// 🔐 인증 관련 페이지
import Login from '@/pages/auth/Login'
import Register from '@/pages/auth/Register'
import OAuthCallback from '@/pages/auth/OAuthCallback'
import UserTypeSelection from '@/pages/auth/UserTypeSelection'
import CompleteProfile from '@/pages/auth/CompleteProfile'

// 📊 대시보드 및 메인 기능
import Dashboard from '@/pages/Dashboard'

// 💼 채용공고 관련 페이지
import Jobs from '@/pages/jobs/Jobs'
import JobDetail from '@/pages/jobs/JobDetail'
import CreateJobPosting from '@/pages/jobs/CreateJobPosting'
import EditJobPosting from '@/pages/jobs/EditJobPosting'
import MyJobPostings from '@/pages/jobs/MyJobPostings'

// 👤 프로필 관련 페이지
import MyProfile from '@/pages/profile/MyProfile'
import CompanyProfile from '@/pages/company/CompanyProfile'
import CompanyDashboard from '@/pages/company/CompanyDashboard'

// 💬 커뮤니티 관련 페이지
import Community from '@/pages/community/Community'
import CreatePost from '@/pages/community/CreatePost'
import EditPost from '@/pages/community/EditPost'
import PostDetail from '@/pages/community/PostDetail'

// 🤖 AI 기능 관련 페이지
import Interview from '@/pages/ai/Interview'
import InterviewHistory from '@/pages/ai/InterviewHistory'
import CoverLetter from '@/pages/ai/CoverLetter'
import Translation from '@/pages/ai/Translation'
import TranslationPage from '@/pages/translation/TranslationPage'
import Chatbot from '@/pages/ai/Chatbot'

// 📧 기타 서비스 페이지
import Webmail from '@/pages/webmail/Webmail'
import Support from '@/pages/support/SupportPage'
import Certificates from '@/pages/certificates/Certificates'
import Settings from '@/pages/settings/Settings'
import MyApplications from '@/pages/applications/MyApplications'
import ApplicantManagement from '@/pages/company/ApplicantManagement'
import ApplicationsRouter from '@/pages/applications/ApplicationsRouter'

// 👑 관리자 전용 페이지
import AdminDashboard from '@/pages/admin/AdminDashboard'
import UserManagement from '@/pages/admin/UserManagement'
import JobManagement from '@/pages/admin/JobManagement'
import CommunityManagement from '@/pages/admin/CommunityManagement'
import CertificateManagement from '@/pages/admin/CertificateManagement'
import AdminLogin from '@/pages/admin/AdminLogin'
import AdminPromote from '@/pages/admin/AdminPromote'

// 상태 관리 훅
import { useAuthStore } from '@/hooks/useAuthStore'

/**
 * 메인 애플리케이션 컴포넌트
 *
 * 📍 라우팅 구조:
 * 1. Public Routes: 인증 없이 접근 가능 (/, /login, /register 등)
 * 2. Protected Routes: 인증 필요 (/dashboard, /jobs, /profile 등)
 * 3. Admin Routes: 관리자 권한 필요 (/admin/*)
 *
 * 🔐 접근 제어 로직:
 * - 비인증 사용자 → 로그인 페이지로 리다이렉트
 * - 관리자 권한 없는 사용자 → AdminRouteGuard에서 차단
 * - 존재하지 않는 라우트 → 홈으로 리다이렉트
 *
 * 🎯 이벤트 처리:
 * - 라우트 변경: React Router의 자동 처리 및 브라우저 히스토리 관리
 * - 인증 상태 변경: useAuthStore 구독을 통한 실시간 UI 업데이트
 * - 권한 없는 접근: Navigate 컴포넌트의 선언적 리다이렉트
 */
function App() {
  // 🔍 Zustand store에서 인증 상태를 구독
  // isAuthenticated가 변경되면 컴포넌트가 자동으로 리렌더링됨
  const { isAuthenticated } = useAuthStore()

  return (
    <Routes>
      {/*
        🌐 PUBLIC ROUTES (인증 불필요)
        누구나 접근 가능한 페이지들
        인증 상태와 관계없이 렌더링됨
      */}
      <Route path="/" element={<Home />} />                                {/* 홈페이지 - 서비스 소개 */}
      <Route path="/login" element={<Login />} />                          {/* 로그인 */}
      <Route path="/register" element={<Register />} />                    {/* 회원가입 */}
      <Route path="/auth/user-type" element={<UserTypeSelection />} />     {/* 사용자 타입 선택 (일반/기업) */}
      <Route path="/auth/callback" element={<OAuthCallback />} />          {/* OAuth 콜백 처리 */}
      <Route path="/auth/complete-profile" element={<CompleteProfile />} /> {/* 프로필 완성 */}

      {/*
        👑 ADMIN ROUTES (관리자 전용)
        완전히 분리된 관리자 영역
        AdminRouteGuard를 통한 이중 보안 검증
      */}
      <Route path="/admin/login" element={<AdminLogin />} />
      <Route path="/admin/*" element={
        <AdminRouteGuard>
          <AdminLayout />
        </AdminRouteGuard>
      }>
        <Route path="users" element={<UserManagement />} />
        <Route path="jobs" element={<JobManagement />} />
        <Route path="community" element={<CommunityManagement />} />
        <Route path="certificates" element={<CertificateManagement />} />
        <Route path="promote" element={<AdminPromote />} />
        <Route index element={<AdminDashboard />} />
      </Route>

      {/*
        🔐 PROTECTED USER ROUTES (인증 필요)
        로그인한 사용자만 접근 가능
        조건부 렌더링: isAuthenticated ? Layout : 로그인페이지로 리다이렉트
      */}
      <Route path="/" element={
        isAuthenticated ? <Layout /> : <Navigate to="/login" />  // 🎯 인증 상태에 따른 조건부 렌더링
      }>
        {/* 📊 메인 기능 */}
        <Route path="dashboard" element={<Dashboard />} />                  {/* 사용자 대시보드 */}

        {/* 💼 채용공고 관련 */}
        <Route path="jobs" element={<Jobs />} />                            {/* 채용공고 목록 */}
        <Route path="jobs/create" element={<CreateJobPosting />} />         {/* 채용공고 작성 (기업용) */}
        <Route path="jobs/edit/:id" element={<EditJobPosting />} />         {/* 채용공고 수정 - URL 파라미터 :id */}
        <Route path="jobs/my-postings" element={<MyJobPostings />} />       {/* 내가 올린 공고 */}
        <Route path="jobs/:id" element={<JobDetail />} />                   {/* 채용공고 상세 - URL 파라미터 :id */}

        {/* 👤 프로필 관리 */}
        <Route path="profile" element={<MyProfile />} />                    {/* 개인 프로필 */}
        <Route path="company/profile" element={<CompanyProfile />} />       {/* 기업 프로필 (기존) */}
        <Route path="company/dashboard" element={<CompanyDashboard />} />   {/* 기업 통합 대시보드 */}
        <Route path="applications" element={<ApplicationsRouter />} />      {/* 지원서 관리 (사용자 타입별) */}

        {/* 💬 커뮤니티 */}
        <Route path="community" element={<Community />} />                  {/* 커뮤니티 목록 */}
        <Route path="community/new" element={<CreatePost />} />             {/* 게시글 작성 */}
        <Route path="community/edit/:id" element={<EditPost />} />          {/* 게시글 수정 - URL 파라미터 :id */}
        <Route path="community/:id" element={<PostDetail />} />             {/* 게시글 상세 - URL 파라미터 :id */}

        {/* 🤖 AI 기능들 */}
        <Route path="ai/chatbot" element={<Chatbot />} />                   {/* AI 챗봇 */}
        <Route path="ai/interview" element={<Interview />} />               {/* AI 면접 연습 */}
        <Route path="ai/interview/history" element={<InterviewHistory />} /> {/* AI 면접 이력 */}
        <Route path="ai/cover-letter" element={<CoverLetter />} />          {/* AI 자기소개서 */}
        <Route path="ai/translation" element={<Translation />} />           {/* AI 번역 */}

        {/* 📧 기타 서비스 */}
        <Route path="translation" element={<TranslationPage />} />          {/* 번역 페이지 */}
        <Route path="support" element={<Support />} />                      {/* 고객지원 */}
        <Route path="webmail" element={<Webmail />} />                      {/* 웹메일 */}
        <Route path="certificates" element={<Certificates />} />            {/* 자격증 관리 */}
        <Route path="settings" element={<Settings />} />                    {/* 설정 */}
      </Route>

      {/*
        🚫 FALLBACK ROUTE (존재하지 않는 경로)
        정의되지 않은 모든 경로를 홈으로 리다이렉트
        404 에러 대신 사용자 친화적인 처리
      */}
      <Route path="*" element={<Navigate to="/" />} />
    </Routes>
  )
}

export default App