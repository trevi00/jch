import { useState, useEffect } from 'react'
import { useQuery } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Link, useNavigate } from 'react-router-dom'
import { useAuthStore } from '@/hooks/useAuthStore'
import { apiClient } from '@/services/api'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/Card'
import { formatEstablishmentDate, formatDisplayDate } from '@/utils/dateUtils'
import { Button } from '@/components/ui/Button'
import { Badge } from '@/components/ui/Badge'
import { 
  Building2, 
  MapPin, 
  Users, 
  Globe, 
  Phone, 
  Mail, 
  Edit3, 
  Save, 
  X, 
  Calendar,
  DollarSign,
  FileText,
  Eye,
  UserCheck,
  TrendingUp,
  Briefcase,
  Clock,
  Bell,
  Activity,
  AlertCircle,
  CheckCircle2,
  Plus
} from 'lucide-react'

const companyProfileSchema = z.object({
  companyName: z.string().min(1, '회사명을 입력해주세요'),
  industry: z.string().min(1, '업종을 선택해주세요'),
  employeeCount: z.number().min(1, '직원 수를 입력해주세요'),
  location: z.string().min(1, '주소를 입력해주세요'),
  websiteUrl: z.string().url('올바른 URL을 입력해주세요').optional().or(z.literal('')),
  phoneNumber: z.string().min(1, '연락처를 입력해주세요'),
  description: z.string().min(1, '회사 소개를 입력해주세요'),
  establishmentDate: z.string().optional(), // LocalDate format: YYYY-MM-DD
  businessNumber: z.string().min(1, '사업자번호를 입력해주세요'),
  revenue: z.number().optional(), // BigDecimal as number
})

type CompanyProfileForm = z.infer<typeof companyProfileSchema>

// Date parsing utility function
const parseDate = (dateArray: number[] | string | null): Date | null => {
  if (!dateArray) return null;
  if (typeof dateArray === 'string') {
    return new Date(dateArray);
  }
  if (Array.isArray(dateArray)) {
    const [year, month, day, hour = 0, minute = 0, second = 0] = dateArray;
    return new Date(year, month - 1, day, hour, minute, second);
  }
  return null;
};

export default function CompanyProfile() {
  const [isEditing, setIsEditing] = useState(false)
  const [isLoading, setIsLoading] = useState(false)
  const { user } = useAuthStore()
  const navigate = useNavigate()

  // 기업 프로필 조회
  const { data: companyProfileData, isLoading: profileLoading } = useQuery({
    queryKey: ['company-profile'],
    queryFn: () => apiClient.getCompanyProfile(),
    enabled: !!user && user.userType === 'COMPANY',
  })

  // 채용공고 목록 조회
  const { data: jobPostingsData } = useQuery({
    queryKey: ['my-job-postings'],
    queryFn: () => apiClient.getMyJobPostings(),
    enabled: !!user,
  })

  // 지원자 현황 조회 (기업용)
  const { data: applicationsData } = useQuery({
    queryKey: ['company-applications'],
    queryFn: () => apiClient.getCompanyApplications(0, 10),
    enabled: !!user && user.userType === 'COMPANY',
  })

  const companyProfile = companyProfileData?.data || {}

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
    setError,
  } = useForm<CompanyProfileForm>({
    resolver: zodResolver(companyProfileSchema),
    defaultValues: {
      companyName: companyProfile?.companyName || '',
      industry: companyProfile?.industry || '',
      employeeCount: companyProfile?.employeeCount || 0,
      location: companyProfile?.location || '',
      websiteUrl: companyProfile?.websiteUrl || '',
      phoneNumber: companyProfile?.phoneNumber || '',
      description: companyProfile?.description || '',
      establishmentDate: formatEstablishmentDate(companyProfile?.establishmentDate) || '',
      businessNumber: companyProfile?.businessNumber || '',
      revenue: companyProfile?.revenue || undefined,
    }
  })

  // 프로필 데이터가 로드되면 form을 업데이트
  useEffect(() => {
    if (companyProfileData?.data && !profileLoading) {
      const profile = companyProfileData.data;
      reset({
        companyName: profile.companyName || '',
        industry: profile.industry || '',
        employeeCount: profile.employeeCount || 0,
        location: profile.location || '',
        websiteUrl: profile.websiteUrl || '',
        phoneNumber: profile.phoneNumber || '',
        description: profile.description || '',
        establishmentDate: formatEstablishmentDate(profile.establishmentDate) || '',
        businessNumber: profile.businessNumber || '',
        revenue: profile.revenue || undefined,
      })
    }
  }, [companyProfileData?.data, profileLoading, reset])

  const onSubmit = async (data: CompanyProfileForm) => {
    setIsLoading(true)
    try {
      const response = await apiClient.updateCompanyProfile(data)

      if (response.success) {
        setIsEditing(false)
        // TODO: 실제 API 응답으로 데이터 업데이트
      } else {
        setError('root', { message: response.message || '프로필 업데이트에 실패했습니다' })
      }
    } catch (error: any) {
      setError('root', { message: '프로필 업데이트 중 오류가 발생했습니다' })
    } finally {
      setIsLoading(false)
    }
  }

  const handleEdit = () => {
    setIsEditing(true)
  }

  const handleCancel = () => {
    setIsEditing(false)
    reset() // 폼 초기화
  }

  const recentApplications = (applicationsData?.data && Array.isArray(applicationsData.data) ? applicationsData.data.slice(0, 3) : [])

  return (
    <div className="max-w-7xl mx-auto space-y-8 p-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">기업 프로필</h1>
          <p className="text-gray-600 mt-2">기업 정보를 관리하고 채용 현황을 확인하세요</p>
        </div>
        {!isEditing && (
          <Button onClick={handleEdit} className="flex items-center gap-2">
            <Edit3 className="w-4 h-4" />
            편집
          </Button>
        )}
      </div>

      <form onSubmit={handleSubmit(onSubmit)}>
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
          {/* 기본 정보 */}
          <div className="lg:col-span-8 space-y-6">
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Building2 className="w-5 h-5" />
                  기본 정보
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      기업명 *
                    </label>
                    {isEditing ? (
                      <input
                        {...register('companyName')}
                        className="input w-full"
                        placeholder="기업명을 입력해주세요"
                      />
                    ) : (
                      <p className="text-gray-900 font-medium">{companyProfile?.companyName || '회사명 없음'}</p>
                    )}
                    {errors.companyName && (
                      <p className="mt-1 text-sm text-red-600">{errors.companyName.message}</p>
                    )}
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      사업자번호 *
                    </label>
                    {isEditing ? (
                      <input
                        {...register('businessNumber')}
                        className="input w-full"
                        placeholder="123-45-67890"
                      />
                    ) : (
                      <p className="text-gray-900">{companyProfile?.businessNumber || '사업자번호 없음'}</p>
                    )}
                    {errors.businessNumber && (
                      <p className="mt-1 text-sm text-red-600">{errors.businessNumber.message}</p>
                    )}
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      산업분야 *
                    </label>
                    {isEditing ? (
                      <select {...register('industry')} className="input w-full">
                        <option value="">산업분야를 선택해주세요</option>
                        <option value="IT/소프트웨어">IT/소프트웨어</option>
                        <option value="제조업">제조업</option>
                        <option value="금융/보험">금융/보험</option>
                        <option value="서비스업">서비스업</option>
                        <option value="의료/제약">의료/제약</option>
                        <option value="교육">교육</option>
                        <option value="기타">기타</option>
                      </select>
                    ) : (
                      <p className="text-gray-900">{companyProfile?.industry || '산업분야 없음'}</p>
                    )}
                    {errors.industry && (
                      <p className="mt-1 text-sm text-red-600">{errors.industry.message}</p>
                    )}
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      설립일
                    </label>
                    {isEditing ? (
                      <input
                        {...register('establishmentDate')}
                        type="date"
                        className="input w-full"
                        placeholder="YYYY-MM-DD"
                      />
                    ) : (
                      <p className="text-gray-900 flex items-center gap-1">
                        <Calendar className="w-4 h-4 text-gray-500" />
                        {formatDisplayDate(companyProfile?.establishmentDate)}
                      </p>
                    )}
                    {errors.establishmentDate && (
                      <p className="mt-1 text-sm text-red-600">{errors.establishmentDate.message}</p>
                    )}
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      직원 수 *
                    </label>
                    {isEditing ? (
                      <input
                        {...register('employeeCount', { valueAsNumber: true })}
                        type="number"
                        min="1"
                        className="input w-full"
                        placeholder="직원 수를 입력해주세요"
                      />
                    ) : (
                      <p className="text-gray-900 flex items-center gap-1">
                        <Users className="w-4 h-4 text-gray-500" />
                        {companyProfile?.employeeCount ? `${companyProfile.employeeCount}명` : '직원 수 없음'}
                      </p>
                    )}
                    {errors.employeeCount && (
                      <p className="mt-1 text-sm text-red-600">{errors.employeeCount.message}</p>
                    )}
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      매출 (억원)
                    </label>
                    {isEditing ? (
                      <input
                        {...register('revenue', { valueAsNumber: true })}
                        type="number"
                        min="0"
                        step="0.01"
                        className="input w-full"
                        placeholder="100"
                      />
                    ) : (
                      <p className="text-gray-900 flex items-center gap-1">
                        <DollarSign className="w-4 h-4 text-gray-500" />
                        {companyProfile?.revenue ? `${companyProfile.revenue}억원` : '매출 정보 없음'}
                      </p>
                    )}
                    {errors.revenue && (
                      <p className="mt-1 text-sm text-red-600">{errors.revenue.message}</p>
                    )}
                  </div>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    지역 *
                  </label>
                  {isEditing ? (
                    <input
                      {...register('location')}
                      className="input w-full"
                      placeholder="서울특별시 강남구"
                    />
                  ) : (
                    <p className="text-gray-900 flex items-center gap-1">
                      <MapPin className="w-4 h-4 text-gray-500" />
                      {companyProfile?.location || '지역 정보 없음'}
                    </p>
                  )}
                  {errors.location && (
                    <p className="mt-1 text-sm text-red-600">{errors.location.message}</p>
                  )}
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      대표 전화번호 *
                    </label>
                    {isEditing ? (
                      <input
                        {...register('phoneNumber')}
                        type="tel"
                        className="input w-full"
                        placeholder="02-1234-5678"
                      />
                    ) : (
                      <p className="text-gray-900 flex items-center gap-1">
                        <Phone className="w-4 h-4 text-gray-500" />
                        {companyProfile?.phoneNumber || '연락처 없음'}
                      </p>
                    )}
                    {errors.phoneNumber && (
                      <p className="mt-1 text-sm text-red-600">{errors.phoneNumber.message}</p>
                    )}
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      이메일
                    </label>
                    <p className="text-gray-900 flex items-center gap-1">
                      <Mail className="w-4 h-4 text-gray-500" />
                      {user?.email}
                    </p>
                  </div>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    홈페이지
                  </label>
                  {isEditing ? (
                    <input
                      {...register('websiteUrl')}
                      type="url"
                      className="input w-full"
                      placeholder="https://company.com"
                    />
                  ) : (
                    <p className="text-gray-900 flex items-center gap-1">
                      <Globe className="w-4 h-4 text-gray-500" />
                      {companyProfile?.websiteUrl ? (
                        <a href={companyProfile.websiteUrl} target="_blank" rel="noopener noreferrer" className="text-primary-600 hover:underline">
                          {companyProfile.websiteUrl}
                        </a>
                      ) : (
                        '홈페이지 없음'
                      )}
                    </p>
                  )}
                  {errors.websiteUrl && (
                    <p className="mt-1 text-sm text-red-600">{errors.websiteUrl.message}</p>
                  )}
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    기업 소개 *
                  </label>
                  {isEditing ? (
                    <textarea
                      {...register('description')}
                      rows={4}
                      className="input w-full resize-none"
                      placeholder="기업에 대한 간단한 소개를 작성해주세요"
                    />
                  ) : (
                    <p className="text-gray-900 whitespace-pre-line">
                      {companyProfile?.description || '기업 소개가 등록되지 않았습니다.'}
                    </p>
                  )}
                  {errors.description && (
                    <p className="mt-1 text-sm text-red-600">{errors.description.message}</p>
                  )}
                </div>
              </CardContent>
            </Card>

            {/* 공고 관리 */}
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Briefcase className="w-5 h-5" />
                  공고 관리
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="flex justify-between items-center mb-4">
                  <p className="text-sm text-gray-600">등록된 채용공고 현황</p>
                  <Link to="/jobs/create">
                    <Button size="sm" className="flex items-center gap-2">
                      <FileText className="w-4 h-4" />
                      새 공고 작성
                    </Button>
                  </Link>
                </div>
                <div className="space-y-3">
                  {jobPostingsData?.data && Array.isArray(jobPostingsData.data?.content) && jobPostingsData.data.content.length > 0 ? (
                    jobPostingsData.data.content.slice(0, 3).map((job: any) => (
                      <div key={job.id} className="flex justify-between items-center p-3 border border-gray-200 rounded-lg">
                        <div>
                          <h4 className="font-medium">{job.title}</h4>
                          <p className="text-sm text-gray-600">
                            지원자 {job.applicationCount || 0}명 • 
                            {job.createdAt ? parseDate(job.createdAt)?.toLocaleDateString('ko-KR') || '등록일 없음' : '등록일 없음'} 등록
                          </p>
                        </div>
                        <div className="flex items-center gap-2">
                          <Badge variant="success">진행중</Badge>
                          <Link to={`/jobs/${job.id}`}>
                            <Button variant="outline" size="sm">
                              <Eye className="w-4 h-4" />
                            </Button>
                          </Link>
                        </div>
                      </div>
                    ))
                  ) : (
                    <div className="text-center py-4 text-gray-500">
                      등록된 채용공고가 없습니다.
                    </div>
                  )}
                  <div className="text-center py-4">
                    <Link to="/jobs" className="text-primary-600 hover:text-primary-500 text-sm font-medium">
                      전체 공고 보기 →
                    </Link>
                  </div>
                </div>
              </CardContent>
            </Card>
          </div>

          {/* 우측 사이드바 */}
          <div className="lg:col-span-4 space-y-6">
            {/* 기업 현황 요약 */}
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <TrendingUp className="w-5 h-5" />
                  기업 현황
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="grid grid-cols-2 gap-4">
                  <div className="text-center p-3 bg-primary-50 rounded-lg">
                    <div className="text-2xl font-bold text-primary-600">{(jobPostingsData?.data?.content && Array.isArray(jobPostingsData.data.content) ? jobPostingsData.data.content.length : 0)}</div>
                    <div className="text-sm text-gray-600">등록 공고</div>
                  </div>
                  <div className="text-center p-3 bg-green-50 rounded-lg">
                    <div className="text-2xl font-bold text-green-600">{(applicationsData?.data && Array.isArray(applicationsData.data) ? applicationsData.data.length : 0)}</div>
                    <div className="text-sm text-gray-600">총 지원자</div>
                  </div>
                  <div className="text-center p-3 bg-yellow-50 rounded-lg">
                    <div className="text-2xl font-bold text-yellow-600">
                      {(applicationsData?.data && Array.isArray(applicationsData.data) ? applicationsData.data.filter((app: any) => app.status === '면접진행' || app.status === 'INTERVIEW').length : 0)}
                    </div>
                    <div className="text-sm text-gray-600">면접 진행</div>
                  </div>
                  <div className="text-center p-3 bg-blue-50 rounded-lg">
                    <div className="text-2xl font-bold text-blue-600">
                      {(applicationsData?.data && Array.isArray(applicationsData.data) ? applicationsData.data.filter((app: any) => app.status === '채용완료' || app.status === 'HIRED').length : 0)}
                    </div>
                    <div className="text-sm text-gray-600">채용 완료</div>
                  </div>
                </div>
              </CardContent>
            </Card>

            {/* 최근 지원 현황 */}
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Clock className="w-5 h-5" />
                  최근 지원 현황
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-3">
                  {recentApplications.length > 0 ? (
                    recentApplications.map((application: any) => (
                      <div key={application.id} className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                        <div className="flex-1">
                          <h4 className="font-medium text-sm">{application.jobPosting?.title || '채용공고 제목 없음'}</h4>
                          <p className="text-xs text-gray-600">{application.applicant?.name || '지원자 이름 없음'}</p>
                          <p className="text-xs text-gray-500">{application.appliedAt ? parseDate(application.appliedAt)?.toLocaleDateString('ko-KR') || '지원일 없음' : '지원일 없음'}</p>
                        </div>
                        <Badge variant="info" size="sm">{application.status || '상태 없음'}</Badge>
                      </div>
                    ))
                  ) : (
                    <div className="text-center py-4 text-gray-500">
                      최근 지원자가 없습니다.
                    </div>
                  )}
                </div>
                <div className="text-center mt-4">
                  <Link to="/applications" className="text-primary-600 hover:text-primary-500 text-sm font-medium">
                    전체 지원자 보기 →
                  </Link>
                </div>
              </CardContent>
            </Card>

            {/* 지원자 목록 */}
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <UserCheck className="w-5 h-5" />
                  지원자 목록
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-2">
                  <div className="flex justify-between items-center text-sm">
                    <span className="text-gray-600">신규 지원자 (오늘)</span>
                    <Badge variant="success">
                      {(applicationsData?.data && Array.isArray(applicationsData.data) ? applicationsData.data.filter((app: any) => {
                        const today = new Date().toDateString()
                        const appDate = app.appliedAt ? new Date(app.appliedAt).toDateString() : null
                        return appDate === today
                      }).length : 0)}명
                    </Badge>
                  </div>
                  <div className="flex justify-between items-center text-sm">
                    <span className="text-gray-600">서류 검토 대기</span>
                    <Badge variant="warning">
                      {(applicationsData?.data && Array.isArray(applicationsData.data) ? applicationsData.data.filter((app: any) => 
                        app.status === '서류검토' || app.status === 'DOCUMENT_REVIEW'
                      ).length : 0)}명
                    </Badge>
                  </div>
                  <div className="flex justify-between items-center text-sm">
                    <span className="text-gray-600">면접 예정</span>
                    <Badge variant="info">
                      {(applicationsData?.data && Array.isArray(applicationsData.data) ? applicationsData.data.filter((app: any) => 
                        app.status === '면접예정' || app.status === 'INTERVIEW_SCHEDULED'
                      ).length : 0)}명
                    </Badge>
                  </div>
                  <div className="flex justify-between items-center text-sm">
                    <span className="text-gray-600">최종 검토</span>
                    <Badge variant="secondary">
                      {(applicationsData?.data && Array.isArray(applicationsData.data) ? applicationsData.data.filter((app: any) => 
                        app.status === '최종검토' || app.status === 'FINAL_REVIEW'
                      ).length : 0)}명
                    </Badge>
                  </div>
                </div>
                <div className="mt-4">
                  <Button
                    className="w-full"
                    size="sm"
                    onClick={() => navigate('/applications')}
                  >
                    지원자 관리
                  </Button>
                </div>
              </CardContent>
            </Card>
          </div>
        </div>

        {errors.root && (
          <div className="bg-red-50 border border-red-200 rounded-md p-3 mt-6">
            <p className="text-sm text-red-600">{errors.root.message}</p>
          </div>
        )}

        {isEditing && (
          <div className="flex justify-end gap-4 mt-6">
            <Button
              type="button"
              variant="secondary"
              onClick={handleCancel}
              className="flex items-center gap-2"
            >
              <X className="w-4 h-4" />
              취소
            </Button>
            <Button
              type="submit"
              disabled={isLoading}
              className="flex items-center gap-2"
            >
              <Save className="w-4 h-4" />
              {isLoading ? '저장 중...' : '저장'}
            </Button>
          </div>
        )}
      </form>
    </div>
  )
}