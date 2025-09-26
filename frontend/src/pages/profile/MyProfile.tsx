import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { User, Edit, Plus, Trash2, Save, X, Briefcase, Calendar, Eye, AlertCircle } from 'lucide-react'
import { apiClient } from '@/services/api'
import { useAuthStore } from '@/hooks/useAuthStore'
import { CreateExperienceRequest } from '@/types/api'

export default function MyProfile() {
  const { user } = useAuthStore()
  const queryClient = useQueryClient()
  const [editingSection, setEditingSection] = useState<string | null>(null)
  const [editingId, setEditingId] = useState<number | null>(null)

  const { data: profileData, isLoading } = useQuery({
    queryKey: ['profile', user?.id],
    queryFn: () => apiClient.getProfile(),
    enabled: !!user,
  })

  const { data: applicationsData, isLoading: applicationsLoading } = useQuery({
    queryKey: ['my-applications', user?.id],
    queryFn: () => apiClient.getMyApplications(),
    enabled: !!user,
  })

  // Individual profile section queries
  const { data: educationData } = useQuery({
    queryKey: ['education', user?.id],
    queryFn: () => apiClient.getEducation(),
    enabled: !!user,
  })

  const { data: skillsData } = useQuery({
    queryKey: ['skills', user?.id],
    queryFn: () => apiClient.getSkills(),
    enabled: !!user,
  })

  const { data: certificationsData } = useQuery({
    queryKey: ['certifications', user?.id],
    queryFn: () => apiClient.getCertifications(),
    enabled: !!user,
  })

  const { data: portfoliosData } = useQuery({
    queryKey: ['portfolios', user?.id],
    queryFn: () => apiClient.getPortfolios(),
    enabled: !!user,
  })

  const { data: experiencesData } = useQuery({
    queryKey: ['experiences', user?.id],
    queryFn: () => apiClient.getExperiences(),
    enabled: !!user,
  })

  const updateProfileMutation = useMutation({
    mutationFn: (data: any) => apiClient.updateProfile(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['profile', user?.id] })
      setEditingSection(null)
    },
  })

  const createExperienceMutation = useMutation({
    mutationFn: (data: CreateExperienceRequest) => apiClient.createExperience(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['experiences', user?.id] })
      queryClient.invalidateQueries({ queryKey: ['profile', user?.id] })
      setEditingSection(null)
    },
  })

  const updateExperienceMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: any }) =>
      apiClient.updateExperience(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['experiences', user?.id] })
      queryClient.invalidateQueries({ queryKey: ['profile', user?.id] })
      setEditingSection(null)
      setEditingId(null)
    },
  })

  const deleteExperienceMutation = useMutation({
    mutationFn: (id: number) => apiClient.deleteExperience(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['profile', user?.id] })
    },
  })

  const createEducationMutation = useMutation({
    mutationFn: (data: any) => apiClient.createEducation(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['education', user?.id] })
      queryClient.invalidateQueries({ queryKey: ['profile', user?.id] })
      setEditingSection(null)
    },
  })

  const updateEducationMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: any }) =>
      apiClient.updateEducation(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['education', user?.id] })
      queryClient.invalidateQueries({ queryKey: ['profile', user?.id] })
      setEditingSection(null)
      setEditingId(null)
    },
  })

  const deleteEducationMutation = useMutation({
    mutationFn: (id: number) => apiClient.deleteEducation(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['education', user?.id] })
      queryClient.invalidateQueries({ queryKey: ['profile', user?.id] })
    },
  })

  const createSkillMutation = useMutation({
    mutationFn: (data: any) => apiClient.createSkill(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['skills', user?.id] })
      queryClient.invalidateQueries({ queryKey: ['profile', user?.id] })
      setEditingSection(null)
    },
  })

  const updateSkillMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: any }) =>
      apiClient.updateSkill(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['skills', user?.id] })
      queryClient.invalidateQueries({ queryKey: ['profile', user?.id] })
      setEditingSection(null)
      setEditingId(null)
    },
  })

  const deleteSkillMutation = useMutation({
    mutationFn: (id: number) => apiClient.deleteSkill(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['skills', user?.id] })
      queryClient.invalidateQueries({ queryKey: ['profile', user?.id] })
    },
  })

  // Certification mutations
  const createCertificationMutation = useMutation({
    mutationFn: (data: any) => apiClient.createCertification(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['certifications', user?.id] })
      queryClient.invalidateQueries({ queryKey: ['profile', user?.id] })
      setEditingSection(null)
    },
  })

  const updateCertificationMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: any }) =>
      apiClient.updateCertification(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['certifications', user?.id] })
      queryClient.invalidateQueries({ queryKey: ['profile', user?.id] })
      setEditingSection(null)
      setEditingId(null)
    },
  })

  const deleteCertificationMutation = useMutation({
    mutationFn: (id: number) => apiClient.deleteCertification(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['certifications', user?.id] })
      queryClient.invalidateQueries({ queryKey: ['profile', user?.id] })
    },
  })

  // Portfolio mutations
  const createPortfolioMutation = useMutation({
    mutationFn: (data: any) => apiClient.createPortfolio(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['portfolios', user?.id] })
      queryClient.invalidateQueries({ queryKey: ['profile', user?.id] })
      setEditingSection(null)
    },
  })

  const updatePortfolioMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: any }) =>
      apiClient.updatePortfolio(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['portfolios', user?.id] })
      queryClient.invalidateQueries({ queryKey: ['profile', user?.id] })
      setEditingSection(null)
      setEditingId(null)
    },
  })

  const deletePortfolioMutation = useMutation({
    mutationFn: (id: number) => apiClient.deletePortfolio(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['portfolios', user?.id] })
      queryClient.invalidateQueries({ queryKey: ['profile', user?.id] })
    },
  })

  const profile = profileData?.data

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
      </div>
    )
  }

  if (!profile) {
    return (
      <div className="text-center py-12">
        <div className="text-gray-500 mb-2">프로필을 불러올 수 없습니다</div>
      </div>
    )
  }


  const handleDeleteItem = (type: string, id: number) => {
    if (!window.confirm('정말로 삭제하시겠습니까?')) return
    
    switch (type) {
      case 'experience':
        deleteExperienceMutation.mutate(id)
        break
      case 'education':
        deleteEducationMutation.mutate(id)
        break
      case 'skill':
        deleteSkillMutation.mutate(id)
        break
      case 'certification':
        deleteCertificationMutation.mutate(id)
        break
      case 'portfolio':
        deletePortfolioMutation.mutate(id)
        break
    }
  }

  const formatDate = (date: any) => {
    if (!date) return '현재'
    if (Array.isArray(date)) {
      return new Date(date[0], date[1] - 1, date[2]).toLocaleDateString('ko-KR', {
        year: 'numeric',
        month: 'long'
      })
    }
    return new Date(date).toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: 'long'
    })
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">내 프로필</h1>
        <p className="text-gray-600">프로필 정보를 관리하고 업데이트하세요</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* 왼쪽 사이드바 - 기본 정보 */}
        <div className="lg:col-span-1">
          <div className="card">
            <div className="card-content text-center">
              <div className="w-24 h-24 bg-primary-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <User className="w-12 h-12 text-primary-600" />
              </div>
              <h3 className="text-xl font-semibold text-gray-900 mb-1">
                {user?.name || 'Unknown User'}
              </h3>
              <p className="text-gray-600 mb-4">{user?.email}</p>
              
              <div className="space-y-2 text-sm">
                {/* <p className="text-gray-600">📊 사용자 ID: {profile.userId}</p>
                <p className="text-gray-600">✅ {profile.message}</p> */}
              </div>
              
              <button
                onClick={() => setEditingSection('basic')}
                className="btn-outline w-full mt-4"
              >
                기본 정보 수정
              </button>
            </div>
          </div>
          
          {profile.summary && (
            <div className="card mt-6">
              <div className="card-header">
                <div className="flex justify-between items-center">
                  <h3 className="text-lg font-semibold">자기소개</h3>
                  <button
                    onClick={() => setEditingSection('summary')}
                    className="text-gray-500 hover:text-gray-700"
                  >
                    <Edit className="w-4 h-4" />
                  </button>
                </div>
              </div>
              <div className="card-content">
                <p className="text-gray-700 text-sm leading-relaxed">
                  {profile.summary}
                </p>
              </div>
            </div>
          )}
        </div>

        {/* 메인 콘텐츠 */}
        <div className="lg:col-span-2 space-y-6">
          {/* 경력 */}
          <div className="card">
            <div className="card-header">
              <div className="flex justify-between items-center">
                <h3 className="text-lg font-semibold">경력</h3>
                <button
                  onClick={() => setEditingSection('experience')}
                  className="btn-outline btn-sm"
                >
                  <Plus className="w-4 h-4 mr-1" />
                  추가
                </button>
              </div>
            </div>
            <div className="card-content">
              {experiencesData?.data?.length > 0 ? (
                <div className="space-y-4">
                  {experiencesData.data.map((exp: any) => (
                    <div key={exp.id} className="border-l-4 border-blue-200 pl-4 py-3">
                      <div className="flex justify-between items-start">
                        <div className="flex-1">
                          <h4 className="font-medium text-gray-900">{exp.position}</h4>
                          <p className="text-sm text-blue-600 font-medium">{exp.companyName}</p>
                          <p className="text-sm text-gray-600">
                            {formatDate(exp.startDate)} - {formatDate(exp.endDate)}
                            {exp.employmentType && (
                              <span className="ml-2 text-xs bg-blue-100 text-blue-800 px-2 py-1 rounded">
                                {exp.employmentType === 'FULL_TIME' ? '정규직' :
                                 exp.employmentType === 'PART_TIME' ? '파트타임' :
                                 exp.employmentType === 'CONTRACT' ? '계약직' :
                                 exp.employmentType === 'FREELANCE' ? '프리랜서' :
                                 exp.employmentType === 'INTERNSHIP' ? '인턴십' :
                                 exp.employmentType === 'VOLUNTEER' ? '자원봉사' :
                                 exp.employmentType === 'PROJECT_BASED' ? '프로젝트 기반' :
                                 exp.employmentType === 'REMOTE' ? '원격근무' :
                                 exp.employmentType === 'HYBRID' ? '하이브리드' :
                                 exp.employmentType}
                              </span>
                            )}
                          </p>
                          {exp.description && (
                            <p className="text-sm text-gray-700 mt-2">{exp.description}</p>
                          )}
                          {exp.technologiesUsed && (
                            <div className="mt-2">
                              <span className="text-xs text-gray-500">기술: {exp.technologiesUsed}</span>
                            </div>
                          )}
                        </div>
                        <div className="flex space-x-1">
                          <button
                            onClick={() => {
                              setEditingSection('experience')
                              setEditingId(exp.id)
                            }}
                            className="text-gray-400 hover:text-gray-600"
                          >
                            <Edit className="w-4 h-4" />
                          </button>
                          <button
                            onClick={() => handleDeleteItem('experience', exp.id)}
                            className="text-gray-400 hover:text-red-600"
                          >
                            <Trash2 className="w-4 h-4" />
                          </button>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <div className="text-center text-gray-500 py-8">
                  <Briefcase className="w-12 h-12 text-gray-300 mx-auto mb-3" />
                  <p>경력 정보가 없습니다.</p>
                  <p className="text-sm mt-1">첫 번째 경력을 추가해보세요!</p>
                </div>
              )}
            </div>
          </div>

          {/* 학력 */}
          <div className="card">
            <div className="card-header">
              <div className="flex justify-between items-center">
                <h3 className="text-lg font-semibold">학력</h3>
                <button
                  onClick={() => setEditingSection('education')}
                  className="btn-outline btn-sm"
                >
                  <Plus className="w-4 h-4 mr-1" />
                  추가
                </button>
              </div>
            </div>
            <div className="card-content">
              {educationData?.data?.length > 0 ? (
                <div className="space-y-4">
                  {educationData.data.map((edu: any) => (
                    <div key={edu.id} className="border-l-4 border-green-200 pl-4 py-3">
                      <div className="flex justify-between items-start">
                        <div className="flex-1">
                          <h4 className="font-medium text-gray-900">{edu.schoolName}</h4>
                          <p className="text-sm text-green-600 font-medium">{edu.major}</p>
                          <p className="text-sm text-gray-600">
                            {edu.educationLevel} • {formatDate(edu.graduationDate)}
                          </p>
                          {edu.gpa && (
                            <p className="text-xs text-gray-500 mt-1">
                              GPA: {edu.gpa}/{edu.maxGpa}
                            </p>
                          )}
                        </div>
                        <div className="flex space-x-1">
                          <button
                            onClick={() => {
                              setEditingSection('education')
                              setEditingId(edu.id)
                            }}
                            className="text-gray-400 hover:text-gray-600"
                          >
                            <Edit className="w-4 h-4" />
                          </button>
                          <button
                            onClick={() => handleDeleteItem('education', edu.id)}
                            className="text-gray-400 hover:text-red-600"
                          >
                            <Trash2 className="w-4 h-4" />
                          </button>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <div className="text-center text-gray-500 py-8">
                  <User className="w-12 h-12 text-gray-300 mx-auto mb-3" />
                  <p>학력 정보가 없습니다.</p>
                  <p className="text-sm mt-1">첫 번째 학력을 추가해보세요!</p>
                </div>
              )}
            </div>
          </div>

          {/* 기술 스택 */}
          <div className="card">
            <div className="card-header">
              <div className="flex justify-between items-center">
                <h3 className="text-lg font-semibold">기술 스택</h3>
                <button
                  onClick={() => setEditingSection('skill')}
                  className="btn-outline btn-sm"
                >
                  <Plus className="w-4 h-4 mr-1" />
                  추가
                </button>
              </div>
            </div>
            <div className="card-content">
              {skillsData?.data?.length > 0 ? (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  {skillsData.data.map((skill: any) => (
                    <div key={skill.id} className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                      <div>
                        <h4 className="font-medium text-gray-900">{skill.skillName}</h4>
                        <p className="text-sm text-gray-600">{skill.skillCategory}</p>
                        <div className="flex items-center mt-1">
                          <div className="flex space-x-1">
                            {[1, 2, 3, 4, 5].map((level) => (
                              <div
                                key={level}
                                className={`w-2 h-2 rounded-full ${
                                  level <= (skill.skillLevel || 1)
                                    ? 'bg-primary-500'
                                    : 'bg-gray-300'
                                }`}
                              />
                            ))}
                          </div>
                          <span className="ml-2 text-xs text-gray-500">
                            {skill.skillLevel}/5
                          </span>
                        </div>
                        {skill.yearsOfExperience && (
                          <p className="text-xs text-gray-500 mt-1">
                            경험: {skill.yearsOfExperience}년
                          </p>
                        )}
                      </div>
                      <div className="flex space-x-1">
                        <button
                          onClick={() => {
                            setEditingSection('skill')
                            setEditingId(skill.id)
                          }}
                          className="text-gray-400 hover:text-gray-600"
                        >
                          <Edit className="w-4 h-4" />
                        </button>
                        <button
                          onClick={() => handleDeleteItem('skill', skill.id)}
                          className="text-gray-400 hover:text-red-600"
                        >
                          <Trash2 className="w-4 h-4" />
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <div className="text-center text-gray-500 py-8">
                  <AlertCircle className="w-12 h-12 text-gray-300 mx-auto mb-3" />
                  <p>기술 스택이 없습니다.</p>
                  <p className="text-sm mt-1">첫 번째 기술을 추가해보세요!</p>
                </div>
              )}
            </div>
          </div>

          {/* 최근 지원 현황 */}
          <div className="card">
            <div className="card-header">
              <div className="flex justify-between items-center">
                <h3 className="text-lg font-semibold">최근 지원 현황</h3>
                <Briefcase className="w-5 h-5 text-gray-400" />
              </div>
            </div>
            <div className="card-content">
              {applicationsLoading ? (
                <div className="flex items-center justify-center py-8">
                  <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-primary-600"></div>
                </div>
              ) : applicationsData?.data?.content?.length > 0 ? (
                <div className="space-y-4">
                  {applicationsData.data.content.slice(0, 5).map((application: any) => (
                    <div key={application.id} className="border-l-4 border-primary-200 pl-4 py-2">
                      <div className="flex justify-between items-start">
                        <div className="flex-1">
                          <Link 
                            to={`/jobs/${application.jobPostingId}`}
                            className="font-medium text-gray-900 hover:text-primary-600 block"
                          >
                            {application.jobPostingTitle}
                          </Link>
                          <p className="text-sm text-gray-600">{application.companyName}</p>
                          <div className="flex items-center mt-2 space-x-4">
                            <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                              application.status === 'SUBMITTED' ? 'bg-blue-100 text-blue-800' :
                              application.status === 'DOCUMENT_PASSED' ? 'bg-yellow-100 text-yellow-800' :
                              application.status === 'INTERVIEW_SCHEDULED' ? 'bg-purple-100 text-purple-800' :
                              application.status === 'HIRED' ? 'bg-green-100 text-green-800' :
                              'bg-red-100 text-red-800'
                            }`}>
                              {application.status === 'SUBMITTED' ? '지원완료' :
                               application.status === 'DOCUMENT_PASSED' ? '서류통과' :
                               application.status === 'INTERVIEW_SCHEDULED' ? '면접예정' :
                               application.status === 'HIRED' ? '최종합격' :
                               '불합격'}
                            </span>
                            <span className="text-xs text-gray-500 flex items-center">
                              <Calendar className="w-3 h-3 mr-1" />
                              {application.appliedAt ? 
                                new Date(
                                  application.appliedAt[0], 
                                  application.appliedAt[1] - 1, 
                                  application.appliedAt[2]
                                ).toLocaleDateString('ko-KR') : 
                                '날짜 없음'
                              }
                            </span>
                          </div>
                        </div>
                      </div>
                    </div>
                  ))}
                  {applicationsData.data.content.length > 5 && (
                    <div className="text-center pt-2">
                      <Link 
                        to="/applications"
                        className="text-primary-600 hover:text-primary-700 text-sm font-medium hover:underline"
                      >
                        전체보기 ({applicationsData.data.content.length - 5}개 더)
                      </Link>
                    </div>
                  )}
                </div>
              ) : (
                <div className="text-center text-gray-500 py-8">
                  <AlertCircle className="w-12 h-12 text-gray-300 mx-auto mb-3" />
                  <p>아직 지원한 공고가 없습니다.</p>
                  <p className="text-sm mt-1">관심있는 채용공고에 지원해보세요!</p>
                  <div className="mt-4">
                    <Link 
                      to="/applications"
                      className="text-primary-600 hover:text-primary-700 font-medium text-sm hover:underline"
                    >
                      전체보기
                    </Link>
                  </div>
                </div>
              )}
            </div>
          </div>

          {/* 자격증 */}
          <div className="card">
            <div className="card-header">
              <div className="flex justify-between items-center">
                <h3 className="text-lg font-semibold">자격증</h3>
                <button
                  onClick={() => setEditingSection('certification')}
                  className="btn-outline btn-sm"
                >
                  <Plus className="w-4 h-4 mr-1" />
                  추가
                </button>
              </div>
            </div>
            <div className="card-content">
              {certificationsData?.data?.length > 0 ? (
                <div className="space-y-4">
                  {certificationsData.data.map((cert: any) => {
                    const isExpired = cert.expiryDate && new Date(cert.expiryDate) < new Date()
                    return (
                      <div key={cert.id} className="border-l-4 border-blue-200 pl-4 py-3">
                        <div className="flex justify-between items-start">
                          <div className="flex-1">
                            <h4 className="font-medium text-gray-900">{cert.certificationName}</h4>
                            <p className="text-sm text-blue-600 font-medium">{cert.issuingOrganization}</p>
                            <div className="text-sm text-gray-600 mt-1">
                              <p>발급일: {formatDate(cert.issueDate)}</p>
                              {cert.expiryDate && (
                                <p className={isExpired ? 'text-red-600' : 'text-gray-600'}>
                                  만료일: {formatDate(cert.expiryDate)}
                                  {isExpired && ' (만료됨)'}
                                </p>
                              )}
                            </div>
                            {cert.credentialId && (
                              <p className="text-xs text-gray-500 mt-1">
                                증명서 ID: {cert.credentialId}
                              </p>
                            )}
                            {cert.description && (
                              <p className="text-sm text-gray-700 mt-2">{cert.description}</p>
                            )}
                          </div>
                          <div className="flex space-x-1">
                            <button
                              onClick={() => {
                                setEditingSection('certification')
                                setEditingId(cert.id)
                              }}
                              className="text-gray-400 hover:text-gray-600"
                            >
                              <Edit className="w-4 h-4" />
                            </button>
                            <button
                              onClick={() => handleDeleteItem('certification', cert.id)}
                              className="text-gray-400 hover:text-red-600"
                            >
                              <Trash2 className="w-4 h-4" />
                            </button>
                          </div>
                        </div>
                      </div>
                    )
                  })}
                </div>
              ) : (
                <div className="text-center text-gray-500 py-8">
                  <AlertCircle className="w-12 h-12 text-gray-300 mx-auto mb-3" />
                  <p>자격증 정보가 없습니다.</p>
                  <p className="text-sm mt-1">첫 번째 자격증을 추가해보세요!</p>
                </div>
              )}
            </div>
          </div>

          {/* 포트폴리오 */}
          <div className="card">
            <div className="card-header">
              <div className="flex justify-between items-center">
                <h3 className="text-lg font-semibold">포트폴리오</h3>
                <button
                  onClick={() => setEditingSection('portfolio')}
                  className="btn-outline btn-sm"
                >
                  <Plus className="w-4 h-4 mr-1" />
                  추가
                </button>
              </div>
            </div>
            <div className="card-content">
              {portfoliosData?.data?.length > 0 ? (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  {portfoliosData.data.map((portfolio: any) => (
                    <div key={portfolio.id} className="border rounded-lg overflow-hidden bg-white shadow-sm">
                      {portfolio.imageUrl && (
                        <div className="h-32 bg-gray-200 bg-cover bg-center" 
                             style={{backgroundImage: `url(${portfolio.imageUrl})`}} />
                      )}
                      <div className="p-4">
                        <div className="flex justify-between items-start mb-2">
                          <h4 className="font-medium text-gray-900">{portfolio.title}</h4>
                          <div className="flex space-x-1">
                            <button
                              onClick={() => {
                                setEditingSection('portfolio')
                                setEditingId(portfolio.id)
                              }}
                              className="text-gray-400 hover:text-gray-600"
                            >
                              <Edit className="w-4 h-4" />
                            </button>
                            <button
                              onClick={() => handleDeleteItem('portfolio', portfolio.id)}
                              className="text-gray-400 hover:text-red-600"
                            >
                              <Trash2 className="w-4 h-4" />
                            </button>
                          </div>
                        </div>
                        <p className="text-sm text-gray-600">{portfolio.portfolioType}</p>
                        {portfolio.description && (
                          <p className="text-sm text-gray-700 mt-2 line-clamp-2">{portfolio.description}</p>
                        )}
                        <div className="flex items-center justify-between mt-3">
                          <div className="text-xs text-gray-500">
                            {formatDate(portfolio.startDate)} - {formatDate(portfolio.endDate)}
                          </div>
                          <div className="flex space-x-2">
                            {portfolio.githubUrl && (
                              <a href={portfolio.githubUrl} target="_blank" rel="noopener noreferrer"
                                 className="text-xs text-blue-600 hover:underline">GitHub</a>
                            )}
                            {portfolio.demoUrl && (
                              <a href={portfolio.demoUrl} target="_blank" rel="noopener noreferrer"
                                 className="text-xs text-green-600 hover:underline">Demo</a>
                            )}
                          </div>
                        </div>
                        {portfolio.technologiesUsed && (
                          <div className="mt-2">
                            <span className="text-xs text-gray-500">기술: {portfolio.technologiesUsed}</span>
                          </div>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <div className="text-center text-gray-500 py-8">
                  <Eye className="w-12 h-12 text-gray-300 mx-auto mb-3" />
                  <p>포트폴리오가 없습니다.</p>
                  <p className="text-sm mt-1">첫 번째 포트폴리오를 추가해보세요!</p>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* 편집 모달들 */}
      {editingSection && (
        <EditModal
          section={editingSection}
          data={editingId ? (profile as any)[`${editingSection}s`]?.find((item: any) => item.id === editingId) : null}
          profile={profile}
          onClose={() => {
            setEditingSection(null)
            setEditingId(null)
          }}
          onSave={(data) => {
            switch (editingSection) {
              case 'basic':
                updateProfileMutation.mutate(data)
                break
              case 'summary':
                updateProfileMutation.mutate({ summary: data.summary })
                break
              case 'experience':
                if (editingId) {
                  updateExperienceMutation.mutate({ id: editingId, data })
                } else {
                  createExperienceMutation.mutate(data)
                }
                break
              case 'education':
                if (editingId) {
                  updateEducationMutation.mutate({ id: editingId, data })
                } else {
                  createEducationMutation.mutate(data)
                }
                break
              case 'skill':
                if (editingId) {
                  updateSkillMutation.mutate({ id: editingId, data })
                } else {
                  createSkillMutation.mutate(data)
                }
                break
              case 'certification':
                if (editingId) {
                  updateCertificationMutation.mutate({ id: editingId, data })
                } else {
                  createCertificationMutation.mutate(data)
                }
                break
              case 'portfolio':
                if (editingId) {
                  updatePortfolioMutation.mutate({ id: editingId, data })
                } else {
                  createPortfolioMutation.mutate(data)
                }
                break
            }
          }}
        />
      )}
    </div>
  )
}

interface EditModalProps {
  section: string
  data: any
  profile: any
  onClose: () => void
  onSave: (data: any) => void
}

function EditModal({ section, data, profile, onClose, onSave }: EditModalProps) {
  const [formData, setFormData] = useState(data || {})

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    onSave(formData)
  }

  const renderForm = () => {
    switch (section) {
      case 'basic':
        return (
          <div className="space-y-4">
            <input
              type="text"
              placeholder="이름"
              value={formData.name || profile.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              className="input"
            />
            <input
              type="tel"
              placeholder="전화번호"
              value={formData.phoneNumber || profile.phoneNumber || ''}
              onChange={(e) => setFormData({ ...formData, phoneNumber: e.target.value })}
              className="input"
            />
            <input
              type="text"
              placeholder="위치"
              value={formData.location || profile.location || ''}
              onChange={(e) => setFormData({ ...formData, location: e.target.value })}
              className="input"
            />
            <input
              type="url"
              placeholder="웹사이트"
              value={formData.website || profile.website || ''}
              onChange={(e) => setFormData({ ...formData, website: e.target.value })}
              className="input"
            />
          </div>
        )
        
      case 'summary':
        return (
          <textarea
            placeholder="자기소개를 입력하세요"
            value={formData.summary || profile.summary || ''}
            onChange={(e) => setFormData({ ...formData, summary: e.target.value })}
            className="input min-h-32"
            rows={6}
          />
        )
        
      case 'experience':
        return (
          <div className="space-y-4">
            <input
              type="text"
              placeholder="회사명 *"
              value={formData.companyName || ''}
              onChange={(e) => setFormData({ ...formData, companyName: e.target.value })}
              className="input"
              required
            />
            <input
              type="text"
              placeholder="직책 *"
              value={formData.position || ''}
              onChange={(e) => setFormData({ ...formData, position: e.target.value })}
              className="input"
              required
            />
            <select
              value={formData.employmentType || ''}
              onChange={(e) => setFormData({ ...formData, employmentType: e.target.value })}
              className="input"
              required
            >
              <option value="">고용 형태 선택 *</option>
              <option value="FULL_TIME">정규직</option>
              <option value="PART_TIME">파트타임</option>
              <option value="CONTRACT">계약직</option>
              <option value="FREELANCE">프리랜서</option>
              <option value="INTERNSHIP">인턴십</option>
              <option value="VOLUNTEER">자원봉사</option>
              <option value="PROJECT_BASED">프로젝트 기반</option>
              <option value="REMOTE">원격근무</option>
              <option value="HYBRID">하이브리드</option>
              <option value="OTHER">기타</option>
            </select>
            <div className="grid grid-cols-2 gap-4">
              <input
                type="month"
                placeholder="시작일 *"
                value={formData.startDate ? formData.startDate.substring(0, 7) : ''}
                onChange={(e) => setFormData({ ...formData, startDate: e.target.value + '-01' })}
                className="input"
                required
              />
              <input
                type="month"
                placeholder="종료일"
                value={formData.endDate ? formData.endDate.substring(0, 7) : ''}
                onChange={(e) => setFormData({ ...formData, endDate: e.target.value ? e.target.value + '-01' : null })}
                className="input"
              />
            </div>
            <textarea
              placeholder="업무 설명"
              value={formData.description || ''}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              className="input min-h-24"
              rows={3}
            />
          </div>
        )
        
      case 'education':
        return (
          <div className="space-y-4">
            <input
              type="text"
              placeholder="학교명 *"
              value={formData.schoolName || ''}
              onChange={(e) => setFormData({ ...formData, schoolName: e.target.value })}
              className="input"
              required
            />
            <input
              type="text"
              placeholder="전공 *"
              value={formData.major || ''}
              onChange={(e) => setFormData({ ...formData, major: e.target.value })}
              className="input"
              required
            />
            <select
              value={formData.educationLevel || ''}
              onChange={(e) => setFormData({ ...formData, educationLevel: e.target.value })}
              className="input"
              required
            >
              <option value="">학위 선택 *</option>
              <option value="HIGH_SCHOOL">고등학교</option>
              <option value="ASSOCIATE">전문학사</option>
              <option value="BACHELOR">학사</option>
              <option value="MASTER">석사</option>
              <option value="DOCTORATE">박사</option>
            </select>
            <div className="grid grid-cols-2 gap-4">
              <input
                type="number"
                placeholder="졸업년도"
                value={formData.graduationYear || ''}
                onChange={(e) => setFormData({ ...formData, graduationYear: parseInt(e.target.value) })}
                className="input"
              />
              <input
                type="date"
                placeholder="졸업일"
                value={formData.graduationDate || ''}
                onChange={(e) => setFormData({ ...formData, graduationDate: e.target.value })}
                className="input"
              />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <input
                type="number"
                step="0.01"
                placeholder="학점"
                value={formData.gpa || ''}
                onChange={(e) => setFormData({ ...formData, gpa: parseFloat(e.target.value) })}
                className="input"
              />
              <input
                type="number"
                step="0.01"
                placeholder="만점 학점"
                value={formData.maxGpa || ''}
                onChange={(e) => setFormData({ ...formData, maxGpa: parseFloat(e.target.value) })}
                className="input"
              />
            </div>
          </div>
        )
        
      case 'skill':
        return (
          <div className="space-y-4">
            <input
              type="text"
              placeholder="기술명 *"
              value={formData.skillName || ''}
              onChange={(e) => setFormData({ ...formData, skillName: e.target.value })}
              className="input"
              required
            />
            <select
              value={formData.skillCategory || ''}
              onChange={(e) => setFormData({ ...formData, skillCategory: e.target.value })}
              className="input"
              required
            >
              <option value="">카테고리 선택 *</option>
              <option value="PROGRAMMING_LANGUAGE">프로그래밍 언어</option>
              <option value="FRAMEWORK">프레임워크</option>
              <option value="DATABASE">데이터베이스</option>
              <option value="TOOL">도구</option>
              <option value="CLOUD">클라우드</option>
              <option value="OTHER">기타</option>
            </select>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                숙련도: {formData.skillLevel || 1}/5
              </label>
              <input
                type="range"
                min="1"
                max="5"
                value={formData.skillLevel || 1}
                onChange={(e) => setFormData({ ...formData, skillLevel: parseInt(e.target.value) })}
                className="w-full"
              />
            </div>
            <input
              type="number"
              placeholder="경험년수"
              value={formData.yearsOfExperience || ''}
              onChange={(e) => setFormData({ ...formData, yearsOfExperience: parseInt(e.target.value) })}
              className="input"
            />
            <textarea
              placeholder="설명"
              value={formData.description || ''}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              className="input min-h-24"
              rows={3}
            />
          </div>
        )
        
      case 'certification':
        return (
          <div className="space-y-4">
            <input
              type="text"
              placeholder="자격증명 *"
              value={formData.certificationName || ''}
              onChange={(e) => setFormData({ ...formData, certificationName: e.target.value })}
              className="input"
              required
            />
            <input
              type="text"
              placeholder="발급기관 *"
              value={formData.issuingOrganization || ''}
              onChange={(e) => setFormData({ ...formData, issuingOrganization: e.target.value })}
              className="input"
              required
            />
            <div className="grid grid-cols-2 gap-4">
              <input
                type="date"
                placeholder="발급일 *"
                value={formData.issueDate || ''}
                onChange={(e) => setFormData({ ...formData, issueDate: e.target.value })}
                className="input"
                required
              />
              <input
                type="date"
                placeholder="만료일"
                value={formData.expiryDate || ''}
                onChange={(e) => setFormData({ ...formData, expiryDate: e.target.value || null })}
                className="input"
              />
            </div>
            <input
              type="text"
              placeholder="증명서 ID"
              value={formData.credentialId || ''}
              onChange={(e) => setFormData({ ...formData, credentialId: e.target.value })}
              className="input"
            />
            <input
              type="url"
              placeholder="증명서 URL"
              value={formData.credentialUrl || ''}
              onChange={(e) => setFormData({ ...formData, credentialUrl: e.target.value })}
              className="input"
            />
            <textarea
              placeholder="설명"
              value={formData.description || ''}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              className="input min-h-24"
              rows={3}
            />
          </div>
        )
        
      case 'portfolio':
        return (
          <div className="space-y-4">
            <input
              type="text"
              placeholder="제목 *"
              value={formData.title || ''}
              onChange={(e) => setFormData({ ...formData, title: e.target.value })}
              className="input"
              required
            />
            <select
              value={formData.portfolioType || ''}
              onChange={(e) => setFormData({ ...formData, portfolioType: e.target.value })}
              className="input"
              required
            >
              <option value="">포트폴리오 타입 선택 *</option>
              <option value="WEB_APPLICATION">웹 애플리케이션</option>
              <option value="MOBILE_APPLICATION">모바일 애플리케이션</option>
              <option value="DESKTOP_APPLICATION">데스크톱 애플리케이션</option>
              <option value="API_BACKEND">API/백엔드</option>
              <option value="LIBRARY_FRAMEWORK">라이브러리/프레임워크</option>
              <option value="OTHER">기타</option>
            </select>
            <textarea
              placeholder="설명"
              value={formData.description || ''}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              className="input min-h-24"
              rows={3}
            />
            <div className="grid grid-cols-2 gap-4">
              <input
                type="date"
                placeholder="시작일"
                value={formData.startDate || ''}
                onChange={(e) => setFormData({ ...formData, startDate: e.target.value })}
                className="input"
              />
              <input
                type="date"
                placeholder="종료일"
                value={formData.endDate || ''}
                onChange={(e) => setFormData({ ...formData, endDate: e.target.value || null })}
                className="input"
              />
            </div>
            <input
              type="url"
              placeholder="프로젝트 URL"
              value={formData.projectUrl || ''}
              onChange={(e) => setFormData({ ...formData, projectUrl: e.target.value })}
              className="input"
            />
            <input
              type="url"
              placeholder="GitHub URL"
              value={formData.githubUrl || ''}
              onChange={(e) => setFormData({ ...formData, githubUrl: e.target.value })}
              className="input"
            />
            <input
              type="url"
              placeholder="데모 URL"
              value={formData.demoUrl || ''}
              onChange={(e) => setFormData({ ...formData, demoUrl: e.target.value })}
              className="input"
            />
            <input
              type="url"
              placeholder="이미지 URL"
              value={formData.imageUrl || ''}
              onChange={(e) => setFormData({ ...formData, imageUrl: e.target.value })}
              className="input"
            />
            <input
              type="text"
              placeholder="사용 기술"
              value={formData.technologiesUsed || ''}
              onChange={(e) => setFormData({ ...formData, technologiesUsed: e.target.value })}
              className="input"
            />
            <div className="grid grid-cols-2 gap-4">
              <input
                type="number"
                placeholder="팀 크기"
                value={formData.teamSize || ''}
                onChange={(e) => setFormData({ ...formData, teamSize: parseInt(e.target.value) })}
                className="input"
              />
              <input
                type="text"
                placeholder="나의 역할"
                value={formData.myRole || ''}
                onChange={(e) => setFormData({ ...formData, myRole: e.target.value })}
                className="input"
              />
            </div>
          </div>
        )
        
      default:
        return null
    }
  }

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg max-w-md w-full mx-4 max-h-[80vh] overflow-y-auto">
        <div className="p-6">
          <div className="flex justify-between items-start mb-6">
            <h2 className="text-xl font-bold text-gray-900">
              {data ? '수정' : '추가'} - {section === 'basic' ? '기본 정보' : 
                                      section === 'summary' ? '자기소개' :
                                      section === 'experience' ? '경력' :
                                      section === 'education' ? '학력' :
                                      section === 'skill' ? '기술' :
                                      section === 'certification' ? '자격증' :
                                      section === 'portfolio' ? '포트폴리오' : section}
            </h2>
            <button
              onClick={onClose}
              className="text-gray-400 hover:text-gray-600"
            >
              <X className="w-6 h-6" />
            </button>
          </div>

          <form onSubmit={handleSubmit} className="space-y-6">
            {renderForm()}

            <div className="flex justify-end space-x-4">
              <button
                type="button"
                onClick={onClose}
                className="btn-outline"
              >
                취소
              </button>
              <button
                type="submit"
                className="btn-primary"
              >
                <Save className="w-4 h-4 mr-2" />
                저장
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  )
}