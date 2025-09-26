import { useState, useEffect } from 'react'
import { useQuery, useMutation } from '@tanstack/react-query'
import { Clock, Star, FileText, Eye, Calendar, Target, TrendingUp, Award, Bot, X, CheckCircle, AlertTriangle, Briefcase, BookOpen } from 'lucide-react'
import { aiClient } from '@/services/api'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/Card'
import { Badge } from '@/components/ui/Badge'
import { Button } from '@/components/ui/Button'
import { useAuthStore } from '@/hooks/useAuthStore'

interface InterviewRecord {
  interviewId: number
  overallScore: number
  totalQuestions: number
  answeredQuestions: number
  completedAt: string
  jobRole: string
  category?: string  // 카테고리 추가
  experienceLevel?: string  // 경험 수준 추가
  questions: Array<{
    id: string
    question: string
    type: string
  }>
  answers: Array<{
    questionId: string
    answer: string
    feedback?: {
      score: number
      detailed_feedback: string  // 상세 피드백 추가
      model_answer: string  // 모범 답변 추가
      strengths: string[]
      improvements: string[]
      evaluation_criteria?: string[]  // 평가기준 추가
    }
  }>
}

interface InterviewHistoryResponse {
  success: boolean
  data: {
    interviews: InterviewRecord[]
    total: number
  }
}

interface AIReviewResponse {
  success: boolean
  data: {
    overallAssessment: string
    detailedAnalysis: Array<{
      questionId: string
      question: string
      userAnswer: string
      score: number
      strengths: string[]
      weaknesses: string[]
      improvements: string[]
      modelAnswer: string
      recommendation: string
    }>
    totalScore: number
    recommendations: string[]
  }
}

const getCategoryBadgeColor = (category: string) => {
  switch(category?.toUpperCase()) {
    case 'IT':
      return 'bg-blue-100 text-blue-800'
    case '건축':
    case 'ARCHITECTURE':
      return 'bg-gray-100 text-gray-800'
    case '뷰티':
    case 'BEAUTY':
      return 'bg-pink-100 text-pink-800'
    case '미디어':
    case 'MEDIA':
      return 'bg-purple-100 text-purple-800'
    case '금융':
    case 'FINANCE':
      return 'bg-green-100 text-green-800'
    default:
      return 'bg-gray-100 text-gray-800'
  }
}

const getExperienceLevelBadgeColor = (level: string) => {
  switch(level?.toUpperCase()) {
    case 'BEGINNER':
    case '초급':
      return 'bg-green-100 text-green-800'
    case 'INTERMEDIATE':
    case '중급':
      return 'bg-yellow-100 text-yellow-800'
    case 'ADVANCED':
    case '고급':
      return 'bg-red-100 text-red-800'
    default:
      return 'bg-gray-100 text-gray-800'
  }
}

export default function InterviewHistory() {
  const [selectedInterview, setSelectedInterview] = useState<InterviewRecord | null>(null)
  const [showAIReview, setShowAIReview] = useState(false)
  const [aiReviewData, setAIReviewData] = useState<AIReviewResponse | null>(null)
  const { user } = useAuthStore()

  // 사용자 ID를 가져옴 - 로그인한 사용자의 ID 또는 'guest'
  const userId = user?.id ? String(user.id) : 'guest'

  const { data: historyData, isLoading, error } = useQuery<InterviewHistoryResponse>({
    queryKey: ['interviewHistory', userId],
    queryFn: () => aiClient.getInterviewHistory(userId),
    refetchOnWindowFocus: false
  })

  const interviews = historyData?.data?.content || historyData?.data?.interviews || []
  const totalInterviews = historyData?.data?.totalElements || historyData?.data?.total || 0

  const getScoreColor = (score: number) => {
    if (score >= 80) return 'text-green-600 bg-green-50'
    if (score >= 60) return 'text-yellow-600 bg-yellow-50'
    return 'text-red-600 bg-red-50'
  }

  const getScoreBadgeVariant = (score: number): "success" | "warning" | "error" => {
    if (score >= 80) return 'success'
    if (score >= 60) return 'warning'
    return 'error'
  }

  const formatDate = (dateString: string) => {
    const date = new Date(dateString)
    return date.toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  }

  const aiReviewMutation = useMutation({
    mutationFn: async (interviewId: number) => {
      const response = await aiClient.getAIReview(interviewId, userId)
      return response
    },
    onSuccess: (data) => {
      setAIReviewData(data)
      setShowAIReview(true)
    },
    onError: (error: any) => {
      console.error('AI 총평 요청 실패:', error)
      alert('AI 총평을 가져오는 중 오류가 발생했습니다.')
    }
  })

  const handleAIReview = (interviewId: number) => {
    aiReviewMutation.mutate(interviewId)
  }

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-purple-600 mx-auto mb-4"></div>
          <p className="text-gray-600">면접 기록을 불러오는 중...</p>
        </div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <FileText className="w-16 h-16 text-gray-400 mx-auto mb-4" />
          <p className="text-gray-600">면접 기록을 불러올 수 없습니다.</p>
        </div>
      </div>
    )
  }

  if (selectedInterview) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="mb-6">
          <Button
            onClick={() => setSelectedInterview(null)}
            variant="ghost"
            className="mb-4"
          >
            ← 목록으로 돌아가기
          </Button>

          <div className="flex items-center justify-between mb-6">
            <div>
              <h1 className="text-3xl font-bold text-gray-900 mb-2">면접 상세 보기</h1>
              <div className="flex items-center space-x-2 mb-2">
                <p className="text-gray-600">{selectedInterview.jobRole} 면접</p>
                {selectedInterview.category && (
                  <Badge className={getCategoryBadgeColor(selectedInterview.category)}>
                    {selectedInterview.category}
                  </Badge>
                )}
                {selectedInterview.experienceLevel && (
                  <Badge className={getExperienceLevelBadgeColor(selectedInterview.experienceLevel)}>
                    {selectedInterview.experienceLevel}
                  </Badge>
                )}
              </div>
            </div>
            <div className="text-right">
              <div className="flex items-center space-x-3 mb-3">
                <Button
                  onClick={() => handleAIReview(selectedInterview.interviewId)}
                  disabled={aiReviewMutation.isPending}
                  className="bg-blue-600 hover:bg-blue-700 text-white"
                >
                  <Bot className="w-4 h-4 mr-2" />
                  {aiReviewMutation.isPending ? 'AI 분석 중...' : 'AI 총평'}
                </Button>
              </div>
              <div className={`inline-flex items-center px-4 py-2 rounded-lg ${getScoreColor(selectedInterview.overallScore)}`}>
                <Award className="w-5 h-5 mr-2" />
                <span className="font-semibold">{selectedInterview.overallScore}점</span>
              </div>
              <p className="text-sm text-gray-500 mt-2">
                {formatDate(selectedInterview.completedAt)}
              </p>
            </div>
          </div>
        </div>

        <div className="space-y-6">
          {selectedInterview?.questions?.map((question, index) => {
            const answer = selectedInterview.answers?.find(a =>
              a.questionId === question.id ||
              a.questionId === `question_${index + 1}`
            )
            return (
              <Card key={question.id} className="p-6">
                <div className="mb-4">
                  <div className="flex items-center justify-between mb-3">
                    <Badge variant="outline" className="mb-2">
                      질문 {index + 1}
                    </Badge>
                    {answer?.feedback && (
                      <Badge variant={getScoreBadgeVariant(answer.feedback.score)}>
                        {answer.feedback.score}점
                      </Badge>
                    )}
                  </div>
                  <h3 className="text-lg font-semibold text-gray-900 mb-3">
                    {question.question}
                  </h3>
                </div>

                {answer && (
                  <>
                    <div className="mb-4">
                      <h4 className="font-medium text-gray-700 mb-2">답변:</h4>
                      <p className="text-gray-600 bg-gray-50 p-4 rounded-lg">
                        {answer.answer}
                      </p>
                    </div>

                    {answer.feedback && (
                      <div className="space-y-4">
                        {/* 상세 피드백 */}
                        {answer.feedback.detailed_feedback && (
                          <div>
                            <h4 className="font-medium text-gray-700 mb-2 flex items-center">
                              <FileText className="w-4 h-4 mr-1" />
                              상세 피드백
                            </h4>
                            <p className="text-sm text-gray-600 bg-blue-50 p-3 rounded-lg">
                              {answer.feedback.detailed_feedback}
                            </p>
                          </div>
                        )}

                        {/* 모범 답변 */}
                        {answer.feedback.model_answer && (
                          <div>
                            <h4 className="font-medium text-green-700 mb-2 flex items-center">
                              <BookOpen className="w-4 h-4 mr-1" />
                              모범 답변
                            </h4>
                            <p className="text-sm text-gray-600 bg-green-50 p-3 rounded-lg">
                              {answer.feedback.model_answer}
                            </p>
                          </div>
                        )}

                        <div className="grid md:grid-cols-2 gap-4">
                          {/* 강점 */}
                          <div>
                            <h4 className="font-medium text-green-700 mb-2 flex items-center">
                              <TrendingUp className="w-4 h-4 mr-1" />
                              강점
                            </h4>
                            <ul className="list-disc list-inside text-sm text-gray-600 space-y-1">
                              {answer.feedback.strengths.map((strength, i) => (
                                <li key={i}>{strength}</li>
                              ))}
                            </ul>
                          </div>

                          {/* 개선사항 */}
                          <div>
                            <h4 className="font-medium text-orange-700 mb-2 flex items-center">
                              <Target className="w-4 h-4 mr-1" />
                              개선사항
                            </h4>
                            <ul className="list-disc list-inside text-sm text-gray-600 space-y-1">
                              {answer.feedback.improvements.map((improvement, i) => (
                                <li key={i}>{improvement}</li>
                              ))}
                            </ul>
                          </div>
                        </div>

                        {/* 평가기준 */}
                        {answer.feedback.evaluation_criteria && answer.feedback.evaluation_criteria.length > 0 && (
                          <div>
                            <h4 className="font-medium text-purple-700 mb-2 flex items-center">
                              <CheckCircle className="w-4 h-4 mr-1" />
                              평가기준
                            </h4>
                            <ul className="list-disc list-inside text-sm text-gray-600 bg-purple-50 p-3 rounded-lg space-y-1">
                              {answer.feedback.evaluation_criteria.map((criteria, i) => (
                                <li key={i}>{criteria}</li>
                              ))}
                            </ul>
                          </div>
                        )}
                      </div>
                    )}
                  </>
                )}
              </Card>
            )
          })}
        </div>

        {/* AI 총평 모달 */}
        {showAIReview && aiReviewData && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
            <div className="bg-white rounded-lg max-w-4xl max-h-[90vh] overflow-y-auto w-full">
              <div className="sticky top-0 bg-white border-b px-6 py-4 flex items-center justify-between">
                <h2 className="text-2xl font-bold text-gray-900 flex items-center">
                  <Bot className="w-6 h-6 mr-2 text-blue-600" />
                  AI 면접 총평
                </h2>
                <Button
                  onClick={() => setShowAIReview(false)}
                  variant="ghost"
                  size="sm"
                >
                  <X className="w-5 h-5" />
                </Button>
              </div>

              <div className="p-6 space-y-6">
                {/* 전체 평가 */}
                <Card>
                  <CardHeader>
                    <CardTitle className="flex items-center">
                      <Award className="w-5 h-5 mr-2 text-yellow-600" />
                      종합 평가
                    </CardTitle>
                  </CardHeader>
                  <CardContent>
                    <div className="bg-blue-50 p-4 rounded-lg mb-4">
                      <p className="text-gray-700 leading-relaxed">{aiReviewData.data.overallAssessment}</p>
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="text-lg font-semibold">총 점수:</span>
                      <Badge variant={getScoreBadgeVariant(aiReviewData.data.totalScore)} className="text-lg px-3 py-1">
                        {aiReviewData.data.totalScore}점
                      </Badge>
                    </div>
                  </CardContent>
                </Card>

                {/* 질문별 상세 분석 */}
                <div className="space-y-4">
                  <h3 className="text-xl font-bold text-gray-900">질문별 상세 분석</h3>
                  {aiReviewData.data.detailedAnalysis.map((analysis, index) => (
                    <Card key={analysis.questionId} className="border-l-4 border-l-blue-500">
                      <CardContent className="p-6">
                        <div className="mb-4">
                          <div className="flex items-center justify-between mb-2">
                            <Badge variant="outline">질문 {index + 1}</Badge>
                            <Badge variant={getScoreBadgeVariant(analysis.score)}>
                              {analysis.score}점
                            </Badge>
                          </div>
                          <h4 className="font-semibold text-gray-900 mb-2">{analysis.question}</h4>
                          <div className="bg-gray-50 p-3 rounded-lg mb-4">
                            <h5 className="text-sm font-medium text-gray-700 mb-1">답변:</h5>
                            <p className="text-gray-600 text-sm">{analysis.userAnswer}</p>
                          </div>
                        </div>

                        <div className="grid md:grid-cols-2 gap-4 mb-4">
                          <div>
                            <h5 className="font-medium text-green-700 mb-2 flex items-center">
                              <CheckCircle className="w-4 h-4 mr-1" />
                              잘한 점
                            </h5>
                            <ul className="list-disc list-inside text-sm text-gray-600 space-y-1">
                              {analysis.strengths.map((strength, i) => (
                                <li key={i}>{strength}</li>
                              ))}
                            </ul>
                          </div>

                          <div>
                            <h5 className="font-medium text-red-700 mb-2 flex items-center">
                              <AlertTriangle className="w-4 h-4 mr-1" />
                              부족한 점
                            </h5>
                            <ul className="list-disc list-inside text-sm text-gray-600 space-y-1">
                              {analysis.weaknesses.map((weakness, i) => (
                                <li key={i}>{weakness}</li>
                              ))}
                            </ul>
                          </div>
                        </div>

                        <div className="mb-4">
                          <h5 className="font-medium text-blue-700 mb-2">개선 방안</h5>
                          <ul className="list-disc list-inside text-sm text-gray-600 space-y-1">
                            {analysis.improvements.map((improvement, i) => (
                              <li key={i}>{improvement}</li>
                            ))}
                          </ul>
                        </div>

                        <div className="bg-green-50 p-4 rounded-lg mb-4">
                          <h5 className="font-medium text-green-800 mb-2">모범 답안</h5>
                          <p className="text-green-700 text-sm leading-relaxed">{analysis.modelAnswer}</p>
                        </div>

                        <div className="bg-yellow-50 p-3 rounded-lg">
                          <h5 className="font-medium text-yellow-800 mb-1">추천사항</h5>
                          <p className="text-yellow-700 text-sm">{analysis.recommendation}</p>
                        </div>
                      </CardContent>
                    </Card>
                  ))}
                </div>

                {/* 전체 추천사항 */}
                <Card>
                  <CardHeader>
                    <CardTitle className="flex items-center">
                      <Target className="w-5 h-5 mr-2 text-purple-600" />
                      전체 추천사항
                    </CardTitle>
                  </CardHeader>
                  <CardContent>
                    <ul className="list-disc list-inside text-gray-700 space-y-2">
                      {aiReviewData.data.recommendations.map((recommendation, i) => (
                        <li key={i}>{recommendation}</li>
                      ))}
                    </ul>
                  </CardContent>
                </Card>
              </div>
            </div>
          </div>
        )}
      </div>
    )
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900 mb-2">면접 기록</h1>
        <p className="text-gray-600">지금까지 진행한 AI 면접 기록을 확인하세요</p>
      </div>

      {totalInterviews === 0 ? (
        <Card className="text-center py-12">
          <CardContent>
            <FileText className="w-16 h-16 text-gray-400 mx-auto mb-4" />
            <h3 className="text-xl font-semibold text-gray-900 mb-2">아직 면접 기록이 없습니다</h3>
            <p className="text-gray-600 mb-6">
              AI 면접을 진행하고 나서 기록을 확인해보세요.
            </p>
            <Button
              onClick={() => window.location.href = '/ai/interview'}
              className="bg-purple-600 hover:bg-purple-700"
            >
              면접 시작하기
            </Button>
          </CardContent>
        </Card>
      ) : (
        <>
          <div className="mb-6">
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
              <Card>
                <CardContent className="p-4">
                  <div className="flex items-center">
                    <FileText className="w-8 h-8 text-purple-600 mr-3" />
                    <div>
                      <p className="text-sm font-medium text-gray-600">총 면접 수</p>
                      <p className="text-2xl font-bold text-gray-900">{totalInterviews}</p>
                    </div>
                  </div>
                </CardContent>
              </Card>

              <Card>
                <CardContent className="p-4">
                  <div className="flex items-center">
                    <Star className="w-8 h-8 text-yellow-600 mr-3" />
                    <div>
                      <p className="text-sm font-medium text-gray-600">평균 점수</p>
                      <p className="text-2xl font-bold text-gray-900">
                        {Math.round(interviews.reduce((acc, interview) => acc + interview.overallScore, 0) / interviews.length)}점
                      </p>
                    </div>
                  </div>
                </CardContent>
              </Card>

              <Card>
                <CardContent className="p-4">
                  <div className="flex items-center">
                    <Target className="w-8 h-8 text-green-600 mr-3" />
                    <div>
                      <p className="text-sm font-medium text-gray-600">최고 점수</p>
                      <p className="text-2xl font-bold text-gray-900">
                        {Math.max(...interviews.map(i => i.overallScore))}점
                      </p>
                    </div>
                  </div>
                </CardContent>
              </Card>
            </div>
          </div>

          <div className="space-y-4">
            {interviews.map((interview, index) => (
              <Card key={interview.interviewId || interview.id || index} className="hover:shadow-lg transition-shadow">
                <CardContent className="p-6">
                  <div className="flex items-center justify-between">
                    <div className="flex-1">
                      <div className="flex items-center mb-2">
                        <h3 className="text-lg font-semibold text-gray-900 mr-4">
                          {interview.jobRole} 면접
                        </h3>
                        <Badge variant={getScoreBadgeVariant(interview.overallScore)}>
                          {interview.overallScore}점
                        </Badge>
                        {interview.category && (
                          <Badge className={`ml-2 ${getCategoryBadgeColor(interview.category)}`}>
                            <Briefcase className="w-3 h-3 mr-1" />
                            {interview.category}
                          </Badge>
                        )}
                        {interview.experienceLevel && (
                          <Badge className={`ml-2 ${getExperienceLevelBadgeColor(interview.experienceLevel)}`}>
                            {interview.experienceLevel}
                          </Badge>
                        )}
                      </div>

                      <div className="flex items-center text-sm text-gray-600 space-x-4 mb-2">
                        <div className="flex items-center">
                          <Calendar className="w-4 h-4 mr-1" />
                          {formatDate(interview.completedAt)}
                        </div>
                        <div className="flex items-center">
                          <FileText className="w-4 h-4 mr-1" />
                          {interview.totalQuestions}개 질문
                        </div>
                        <div className="flex items-center">
                          <Clock className="w-4 h-4 mr-1" />
                          {interview.answeredQuestions}/{interview.totalQuestions} 답변 완료
                        </div>
                      </div>
                    </div>

                    <Button
                      onClick={() => setSelectedInterview(interview)}
                      className="ml-4"
                      variant="outline"
                    >
                      <Eye className="w-4 h-4 mr-2" />
                      상세 보기
                    </Button>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        </>
      )}
    </div>
  )
}