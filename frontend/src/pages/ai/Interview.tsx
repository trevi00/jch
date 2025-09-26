import { useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { Mic, MicOff, RefreshCw, MessageCircle, Brain, Play, CheckCircle, AlertCircle, Target, Users, Code, Lightbulb, History, BookOpen, Briefcase, Palette, Camera, DollarSign } from 'lucide-react'
import axios from 'axios'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/Card'
import { Badge } from '@/components/ui/Badge'
import { Input } from '@/components/ui/Input'
import { Button } from '@/components/ui/Button'
import { useAuthStore } from '@/hooks/useAuthStore'
import { AI_SERVICE_BASE_URL } from '@/utils/constants'
import { aiClient } from '@/services/api'

interface Question {
  id: string
  question: string
  type: 'technical' | 'behavioral' | 'situational'
}

interface InterviewSession {
  questions: Question[]
  currentQuestionIndex: number
  answers: Array<{
    questionId: string
    answer: string
    feedback?: {
      score: number
      detailed_feedback: string
      model_answer: string
      strengths: string[]
      improvements: string[]
      evaluation_criteria: string[]
    }
  }>
}

export default function Interview() {
  const navigate = useNavigate()
  const { user } = useAuthStore()
  const [session, setSession] = useState<InterviewSession | null>(null)
  const [isRecording, setIsRecording] = useState(false)
  const [currentAnswer, setCurrentAnswer] = useState('')
  const [interviewType, setInterviewType] = useState<'personality' | 'technical'>('technical')
  const [jobRole, setJobRole] = useState('')
  const [experienceLevel, setExperienceLevel] = useState<'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED'>('BEGINNER')
  const [category, setCategory] = useState<'IT' | '건축' | '뷰티' | '미디어' | '금융'>('IT')

  // 사용자 ID를 가져옴 - 로그인한 사용자의 ID 또는 'guest'
  const userId = user?.id ? String(user.id) : 'guest'

  const generateQuestionsMutation = useMutation({
    mutationFn: async (data: { category: string; experienceLevel: string; interviewType: 'technical' | 'personality'; jobRole: string }) => {
      const aiApi = axios.create({
        baseURL: `${AI_SERVICE_BASE_URL}/v1`,
        timeout: 30000,
      });
      const response = await aiApi.post('/interview/category/generate-questions', {
        category: data.category,
        experience_level: data.experienceLevel,
        interview_type: data.interviewType,
        job_position: data.jobRole
      });
      return response.data;
    },
    onSuccess: (response) => {
      if (response.success && response.data) {
        const questions: Question[] = response.data.questions.map((q: any, index: number) => ({
          id: q.id ? q.id.toString() : index.toString(),
          question: q.question,
          type: 'technical'
        }))

        setSession({
          questions,
          currentQuestionIndex: 0,
          answers: []
        })
      }
    },
  })

  const evaluateAnswerMutation = useMutation({
    mutationFn: async (data: { question: string; answer: string; jobRole: string }) => {
      const aiApi = axios.create({
        baseURL: `${AI_SERVICE_BASE_URL}/v1`,
        timeout: 30000,
      });
      const response = await aiApi.post('/interview/category/evaluate-answer', {
        category: category,
        experience_level: experienceLevel,
        interview_type: interviewType,
        job_position: data.jobRole,
        question: data.question,
        answer: data.answer
      });
      return response.data;
    },
    onSuccess: (response) => {
      if (response.success && response.data && session) {
        const currentQuestion = session.questions[session.currentQuestionIndex]
        const updatedAnswers = [...session.answers]
        const existingAnswerIndex = updatedAnswers.findIndex(a => a.questionId === currentQuestion.id)

        if (existingAnswerIndex >= 0) {
          updatedAnswers[existingAnswerIndex].feedback = response.data.feedback
        } else {
          updatedAnswers.push({
            questionId: currentQuestion.id,
            answer: currentAnswer,
            feedback: response.data.feedback
          })
        }

        setSession({ ...session, answers: updatedAnswers })
      }
    },
  })

  const completeInterviewMutation = useMutation({
    mutationFn: (data: {
      jobRole: string
      questions: Question[]
      answers: Array<{ questionId: string; answer: string; feedback: any }>
    }) => {
      return aiClient.completeInterview({
        job_role: data.jobRole,
        questions: data.questions.map((q, index) => ({
          id: `question_${index + 1}`,
          question: q.question,
          type: q.category || 'technical'
        })),
        answers: data.answers.map((a, index) => {
          const questionIndex = data.questions.findIndex(q => q.id === a.questionId);
          const questionId = questionIndex >= 0 ? `question_${questionIndex + 1}` : `question_${index + 1}`;

          return {
            question_id: questionId,
            answer: a.answer,
            feedback: a.feedback ? {
              score: a.feedback.score || 0,
              strengths: a.feedback.strengths || [],
              improvements: a.feedback.improvements || [],
              suggestion: a.feedback.suggestion || ''
            } : null
          }
        })
      })
    },
    onSuccess: (response) => {
      if (response.success) {
        alert(`면접이 성공적으로 완료되었습니다!\n\n전체 점수: ${response.data?.overallScore || '계산중'}점\n답변한 질문: ${response.data?.totalQuestions || session?.questions.length || 0}개\n\n결과가 저장되어 나중에 확인할 수 있습니다.`)
        resetInterview()
      } else {
        alert(`면접 완료 처리 중 오류가 발생했습니다: ${response.message || '알 수 없는 오류'}`)
      }
    },
    onError: (error: any) => {
      console.error('Interview completion error:', error)
      alert(`면접 완료 중 오류가 발생했습니다: ${error.message || '네트워크 오류'}`)
    }
  })

  const startInterview = () => {
    if (!jobRole.trim()) return

    generateQuestionsMutation.mutate({
      category: category,
      experienceLevel: experienceLevel,
      interviewType: interviewType,
      jobRole: jobRole.trim()
    })
  }

  const submitAnswer = () => {
    if (!currentAnswer.trim() || !session) return

    const currentQuestion = session.questions[session.currentQuestionIndex]

    evaluateAnswerMutation.mutate({
      question: currentQuestion.question,
      answer: currentAnswer.trim(),
      jobRole
    })
  }

  const nextQuestion = () => {
    if (!session) return

    const updatedAnswers = [...session.answers]
    const currentQuestion = session.questions[session.currentQuestionIndex]
    const existingAnswerIndex = updatedAnswers.findIndex(a => a.questionId === currentQuestion.id)

    if (existingAnswerIndex === -1) {
      updatedAnswers.push({
        questionId: currentQuestion.id,
        answer: currentAnswer,
      })
    }

    setSession({
      ...session,
      currentQuestionIndex: session.currentQuestionIndex + 1,
      answers: updatedAnswers
    })
    setCurrentAnswer('')
  }

  const previousQuestion = () => {
    if (!session || session.currentQuestionIndex === 0) return

    const prevIndex = session.currentQuestionIndex - 1
    const prevAnswer = session.answers.find(a => a.questionId === session.questions[prevIndex].id)

    setSession({
      ...session,
      currentQuestionIndex: prevIndex
    })
    setCurrentAnswer(prevAnswer?.answer || '')
  }

  const finishInterview = () => {
    if (!session) return

    completeInterviewMutation.mutate({
      jobRole,
      questions: session.questions,
      answers: session.answers.map(a => ({
        ...a,
        feedback: a.feedback || null
      })),
      user_id: userId  // 사용자 ID 추가
    })
  }

  const resetInterview = () => {
    setSession(null)
    setCurrentAnswer('')
    setIsRecording(false)
  }

  const toggleRecording = () => {
    setIsRecording(!isRecording)
    // 실제 음성 녹음 기능은 Web Speech API 등을 사용하여 구현 필요
  }

  const currentQuestion = session?.questions[session.currentQuestionIndex]
  const currentQuestionAnswer = session?.answers.find(a => a.questionId === currentQuestion?.id)
  const isLastQuestion = session && session.currentQuestionIndex === session.questions.length - 1
  const canProceed = currentAnswer.trim().length > 0

  if (!session) {
    return (
      <div className="space-y-8 p-6">
        <div className="text-center">
          <div className="flex items-center justify-between mb-6">
            <div className="flex items-center">
              <div className="p-4 bg-gradient-to-r from-purple-500 to-indigo-600 rounded-xl shadow-lg mr-4">
                <Brain className="w-10 h-10 text-white" />
              </div>
              <div className="text-left">
                <h1 className="text-4xl font-bold text-gray-900 mb-2">AI 면접 연습</h1>
                <p className="text-xl text-gray-600">실제 면접과 같은 환경에서 연습하고 피드백을 받아보세요</p>
              </div>
            </div>
            <Button
              onClick={() => navigate('/ai/interview/history')}
              variant="outline"
              className="flex items-center"
            >
              <History className="w-4 h-4 mr-2" />
              면접 기록
            </Button>
          </div>
          <div className="flex flex-wrap gap-4 justify-center mb-8">
            <Badge variant="success" size="lg">
              <CheckCircle className="w-4 h-4 mr-1" />
              실시간 피드백
            </Badge>
            <Badge variant="info" size="lg">
              <Target className="w-4 h-4 mr-1" />
              맞춤형 질문
            </Badge>
            <Badge variant="warning" size="lg">
              <Lightbulb className="w-4 h-4 mr-1" />
              개선 제안
            </Badge>
          </div>
        </div>

        <Card className="max-w-4xl mx-auto shadow-xl border-0">
          <CardContent className="p-8 space-y-8">
            <div>
              <Input
                type="text"
                value={jobRole}
                onChange={(e) => setJobRole(e.target.value)}
                placeholder="예: 프론트엔드 개발자, 백엔드 개발자, 데이터 분석가"
                label="지원 직무 *"
                leftIcon={<Target className="h-4 w-4" />}
                size="lg"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-3">
                면접 분야
              </label>
              <div className="grid grid-cols-5 gap-3">
                {[
                  { value: 'IT', label: 'IT', icon: <Code className="w-5 h-5" />, color: 'from-blue-500 to-indigo-600' },
                  { value: '건축', label: '건축', icon: <BookOpen className="w-5 h-5" />, color: 'from-green-500 to-teal-600' },
                  { value: '뷰티', label: '뷰티', icon: <Palette className="w-5 h-5" />, color: 'from-pink-500 to-rose-600' },
                  { value: '미디어', label: '미디어', icon: <Camera className="w-5 h-5" />, color: 'from-purple-500 to-violet-600' },
                  { value: '금융', label: '금융', icon: <DollarSign className="w-5 h-5" />, color: 'from-yellow-500 to-orange-600' }
                ].map((cat) => (
                  <Button
                    key={cat.value}
                    variant={category === cat.value ? 'default' : 'outline'}
                    onClick={() => setCategory(cat.value as any)}
                    className="h-auto p-4 flex-col"
                  >
                    <div className={`w-10 h-10 bg-gradient-to-r ${cat.color} rounded-xl flex items-center justify-center text-white mb-2`}>
                      {cat.icon}
                    </div>
                    <div className="font-semibold text-sm">{cat.label}</div>
                  </Button>
                ))}
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-3">
                경력 수준
              </label>
              <div className="grid grid-cols-3 gap-3">
                {[
                  { value: 'BEGINNER', label: '초급', desc: '0-2년', icon: <Users className="w-5 h-5" /> },
                  { value: 'INTERMEDIATE', label: '중급', desc: '2-5년', icon: <Code className="w-5 h-5" /> },
                  { value: 'ADVANCED', label: '고급', desc: '5년+', icon: <Target className="w-5 h-5" /> }
                ].map((level) => (
                  <Button
                    key={level.value}
                    variant={experienceLevel === level.value ? 'default' : 'outline'}
                    onClick={() => setExperienceLevel(level.value as any)}
                    className="h-auto p-4 flex-col"
                  >
                    {level.icon}
                    <div className="mt-2 text-center">
                      <div className="font-semibold">{level.label}</div>
                      <div className="text-xs opacity-70">{level.desc}</div>
                    </div>
                  </Button>
                ))}
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-3">
                면접 유형
              </label>
              <div className="grid grid-cols-2 gap-4">
                <Card
                  className={`cursor-pointer transition-all duration-200 hover:shadow-lg ${
                    interviewType === 'personality'
                      ? 'border-2 border-primary-500 bg-primary-50 shadow-md'
                      : 'border border-gray-200 hover:border-primary-300'
                  }`}
                  onClick={() => setInterviewType('personality')}
                >
                  <CardContent className="p-6 text-center">
                    <div className="w-12 h-12 bg-gradient-to-r from-blue-500 to-purple-600 rounded-xl flex items-center justify-center mx-auto mb-4">
                      <Users className="w-6 h-6 text-white" />
                    </div>
                    <CardTitle className="text-lg mb-2">일반 면접</CardTitle>
                    <p className="text-sm text-gray-600">인성, 경험, 동기 중심의 질문</p>
                  </CardContent>
                </Card>

                <Card
                  className={`cursor-pointer transition-all duration-200 hover:shadow-lg ${
                    interviewType === 'technical'
                      ? 'border-2 border-primary-500 bg-primary-50 shadow-md'
                      : 'border border-gray-200 hover:border-primary-300'
                  }`}
                  onClick={() => setInterviewType('technical')}
                >
                  <CardContent className="p-6 text-center">
                    <div className="w-12 h-12 bg-gradient-to-r from-green-500 to-teal-600 rounded-xl flex items-center justify-center mx-auto mb-4">
                      <Code className="w-6 h-6 text-white" />
                    </div>
                    <CardTitle className="text-lg mb-2">기술 면접</CardTitle>
                    <p className="text-sm text-gray-600">기술적 지식과 문제해결 능력</p>
                  </CardContent>
                </Card>
              </div>
            </div>

            <Button
              onClick={startInterview}
              disabled={!jobRole.trim() || generateQuestionsMutation.isPending}
              size="xl"
              className="w-full py-4 text-lg shadow-lg hover:shadow-xl transition-all duration-300"
            >
              {generateQuestionsMutation.isPending ? (
                <>
                  <RefreshCw className="w-5 h-5 mr-2 animate-spin" />
                  면접 문제 생성 중...
                </>
              ) : (
                <>
                  면접 시작하기
                </>
              )}
            </Button>
          </CardContent>
        </Card>
      </div>
    )
  }

  return (
    <div className="space-y-8 p-6">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div className="flex items-center">
          <div className="p-3 bg-gradient-to-r from-purple-500 to-indigo-600 rounded-xl shadow-lg mr-4">
            <Brain className="w-8 h-8 text-white" />
          </div>
          <div>
            <h1 className="text-3xl font-bold text-gray-900">AI 면접 진행 중</h1>
            <div className="flex items-center gap-4 mt-2">
              <Badge variant="info" size="lg">{category} - {jobRole}</Badge>
              <Badge variant="outline" size="lg">
                질문 {session.currentQuestionIndex + 1} / {session.questions.length}
              </Badge>
              <Badge variant="success" size="lg">{experienceLevel}</Badge>
            </div>
          </div>
        </div>
        <Button
          onClick={resetInterview}
          variant="outline"
          size="lg"
        >
          면접 종료
        </Button>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* 메인 면접 영역 */}
        <div className="lg:col-span-2 space-y-6">
          {/* 질문 카드 */}
          <Card className="shadow-lg border-0" hover="glow">
            <CardHeader className="pb-4">
              <div className="flex justify-between items-center">
                <CardTitle className="text-2xl flex items-center">
                  <MessageCircle className="w-6 h-6 mr-2 text-primary-600" />
                  질문 {session.currentQuestionIndex + 1}
                </CardTitle>
                <Badge
                  variant={currentQuestion?.type === 'technical' ? 'info' : 'success'}
                  size="lg"
                >
                  {currentQuestion?.type === 'technical' ? '기술' :
                   currentQuestion?.type === 'behavioral' ? '인성' : '상황'}
                </Badge>
              </div>
            </CardHeader>
            <CardContent>
              <div className="p-6 bg-gradient-to-r from-blue-50 to-indigo-50 rounded-xl">
                <p className="text-xl text-gray-900 leading-relaxed font-medium">
                  {currentQuestion?.question}
                </p>
              </div>
            </CardContent>
          </Card>

          {/* 답변 입력 */}
          <Card className="shadow-lg border-0">
            <CardHeader className="pb-4">
              <div className="flex justify-between items-center">
                <CardTitle className="text-xl flex items-center">
                  <Target className="w-5 h-5 mr-2 text-primary-600" />
                  답변 작성
                </CardTitle>
                {/* <Button
                  onClick={toggleRecording}
                  variant={isRecording ? 'destructive' : 'outline'}
                  size="sm"
                  className="p-2"
                >
                  {isRecording ? <MicOff className="w-5 h-5" /> : <Mic className="w-5 h-5" />}
                </Button> */}
              </div>
            </CardHeader>
            <CardContent className="space-y-4">
              <textarea
                value={currentAnswer}
                onChange={(e) => setCurrentAnswer(e.target.value)}
                className="flex w-full rounded-lg border border-gray-300 bg-white px-4 py-3 text-sm placeholder:text-gray-500 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 min-h-40 resize-none"
                placeholder="답변을 입력하세요..."
                rows={8}
              />

              <div className="flex justify-between items-center">
                <div className="text-sm text-gray-500 flex items-center">
                  <AlertCircle className="w-4 h-4 mr-1" />
                  {currentAnswer.length} / 1000자
                </div>
                <Button
                  onClick={submitAnswer}
                  disabled={!canProceed || evaluateAnswerMutation.isPending}
                  variant="outline"
                  size="lg"
                >
                  {evaluateAnswerMutation.isPending ? (
                    <RefreshCw className="w-4 h-4 mr-2 animate-spin" />
                  ) : (
                    <Brain className="w-4 h-4 mr-2" />
                  )}
                  AI 피드백 받기
                </Button>
              </div>
            </CardContent>
          </Card>

          {/* 피드백 */}
          {currentQuestionAnswer?.feedback && (
            <Card className="shadow-lg border-0 bg-gradient-to-r from-green-50 to-blue-50">
              <CardHeader className="pb-4">
                <CardTitle className="text-xl flex items-center">
                  <Lightbulb className="w-6 h-6 mr-2 text-amber-500" />
                  AI 피드백
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-6">
                <div className="flex justify-center">
                  <Card className="text-center bg-white shadow-md border-2 border-primary-200">
                    <CardContent className="p-6">
                      <div className="text-4xl font-bold text-primary-600 mb-2">
                        {currentQuestionAnswer.feedback.score}/100
                      </div>
                      <Badge variant="success" size="lg">
                        평가 점수
                      </Badge>
                    </CardContent>
                  </Card>
                </div>

                <Card className="bg-white shadow-sm border border-blue-200">
                  <CardHeader className="pb-3">
                    <CardTitle className="text-lg text-blue-700 flex items-center">
                      <MessageCircle className="w-5 h-5 mr-2" />
                      상세 피드백
                    </CardTitle>
                  </CardHeader>
                  <CardContent>
                    <p className="text-gray-700 leading-relaxed">{currentQuestionAnswer.feedback.detailed_feedback}</p>
                  </CardContent>
                </Card>

                <Card className="bg-gradient-to-r from-indigo-500 to-purple-600 text-white">
                  <CardContent className="p-6">
                    <h4 className="font-semibold text-lg mb-3 flex items-center">
                      <CheckCircle className="w-5 h-5 mr-2" />
                      모범 답변
                    </h4>
                    <p className="text-indigo-100 leading-relaxed">
                      {currentQuestionAnswer.feedback.model_answer}
                    </p>
                  </CardContent>
                </Card>

                <div className="grid md:grid-cols-2 gap-6">
                  <Card className="bg-white shadow-sm border border-green-200">
                    <CardHeader className="pb-3">
                      <CardTitle className="text-lg text-green-700 flex items-center">
                        <CheckCircle className="w-5 h-5 mr-2" />
                        강점
                      </CardTitle>
                    </CardHeader>
                    <CardContent>
                      <ul className="space-y-2">
                        {currentQuestionAnswer.feedback.strengths.map((strength, index) => (
                          <li key={index} className="flex items-start text-sm">
                            <Badge variant="success" size="sm" className="mr-2 mt-0.5 w-2 h-2 rounded-full p-0" />
                            <span className="text-gray-700">{strength}</span>
                          </li>
                        ))}
                      </ul>
                    </CardContent>
                  </Card>

                  <Card className="bg-white shadow-sm border border-orange-200">
                    <CardHeader className="pb-3">
                      <CardTitle className="text-lg text-orange-700 flex items-center">
                        <AlertCircle className="w-5 h-5 mr-2" />
                        개선사항
                      </CardTitle>
                    </CardHeader>
                    <CardContent>
                      <ul className="space-y-2">
                        {currentQuestionAnswer.feedback.improvements.map((improvement, index) => (
                          <li key={index} className="flex items-start text-sm">
                            <Badge variant="warning" size="sm" className="mr-2 mt-0.5 w-2 h-2 rounded-full p-0" />
                            <span className="text-gray-700">{improvement}</span>
                          </li>
                        ))}
                      </ul>
                    </CardContent>
                  </Card>
                </div>

                <Card className="bg-white shadow-sm border border-purple-200">
                  <CardHeader className="pb-3">
                    <CardTitle className="text-lg text-purple-700 flex items-center">
                      <Target className="w-5 h-5 mr-2" />
                      평가기준
                    </CardTitle>
                  </CardHeader>
                  <CardContent>
                    <ul className="space-y-2">
                      {currentQuestionAnswer.feedback.evaluation_criteria.map((criterion, index) => (
                        <li key={index} className="flex items-start text-sm">
                          <Badge variant="info" size="sm" className="mr-2 mt-0.5 w-2 h-2 rounded-full p-0" />
                          <span className="text-gray-700">{criterion}</span>
                        </li>
                      ))}
                    </ul>
                  </CardContent>
                </Card>
              </CardContent>
            </Card>
          )}

          {/* 네비게이션 */}
          <div className="flex justify-between items-center">
            <Button
              onClick={previousQuestion}
              disabled={session.currentQuestionIndex === 0}
              variant="outline"
              size="lg"
            >
              이전 질문
            </Button>

            <div className="flex space-x-3">
              {isLastQuestion ? (
                <Button
                  onClick={finishInterview}
                  disabled={!canProceed || completeInterviewMutation.isPending}
                  size="xl"
                  className="px-8 shadow-lg"
                >
                  {completeInterviewMutation.isPending ? (
                    <>
                      <RefreshCw className="w-5 h-5 mr-2 animate-spin" />
                      처리중...
                    </>
                  ) : (
                    <>
                      <CheckCircle className="w-5 h-5 mr-2" />
                      면접 완료
                    </>
                  )}
                </Button>
              ) : (
                <Button
                  onClick={nextQuestion}
                  disabled={!canProceed}
                  size="xl"
                  className="px-8 shadow-lg"
                >
                  다음 질문
                  <Target className="w-5 h-5 ml-2" />
                </Button>
              )}
            </div>
          </div>
        </div>

        {/* 사이드바 - 진행 상황 */}
        <div className="space-y-6">
          <Card className="shadow-lg border-0" hover="glow">
            <CardHeader>
              <CardTitle className="flex items-center">
                <Target className="w-5 h-5 mr-2 text-primary-600" />
                진행 상황
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-3">
                {session.questions.map((q, index) => (
                  <Card
                    key={q.id}
                    className={`transition-all duration-200 ${
                      index === session.currentQuestionIndex
                        ? 'border-2 border-primary-400 bg-primary-50 shadow-md'
                        : index < session.currentQuestionIndex
                        ? 'border border-green-300 bg-green-50'
                        : 'border border-gray-200 bg-gray-50'
                    }`}
                  >
                    <CardContent className="p-4">
                      <div className="flex justify-between items-start mb-2">
                        <span className="font-medium text-sm">질문 {index + 1}</span>
                        <div className="flex flex-wrap gap-1">
                          {index < session.currentQuestionIndex && (
                            <Badge variant="success" size="sm">
                              <CheckCircle className="w-3 h-3 mr-1" />
                              완료
                            </Badge>
                          )}
                          {index === session.currentQuestionIndex && (
                            <Badge variant="info" size="sm">
                              <Play className="w-3 h-3 mr-1" />
                              진행중
                            </Badge>
                          )}
                          {session.answers.find(a => a.questionId === q.id)?.feedback && (
                            <Badge variant="warning" size="sm">
                              <Brain className="w-3 h-3 mr-1" />
                              피드백
                            </Badge>
                          )}
                        </div>
                      </div>
                      <p className="text-xs text-gray-600 line-clamp-2">
                        {q.question}
                      </p>
                    </CardContent>
                  </Card>
                ))}
              </div>
            </CardContent>
          </Card>

          {/* 면접 가이드 */}
          <Card className="shadow-lg border-0 bg-gradient-to-br from-amber-50 to-orange-50">
            <CardHeader>
              <CardTitle className="flex items-center text-amber-700">
                <Lightbulb className="w-5 h-5 mr-2" />
                면접 가이드
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-3">
                {[
                  { icon: <Target className="w-4 h-4" />, text: '구체적인 경험과 사례를 포함하세요' },
                  { icon: <CheckCircle className="w-4 h-4" />, text: 'STAR 방법(상황-과제-행동-결과)을 활용하세요' },
                  { icon: <MessageCircle className="w-4 h-4" />, text: '간결하고 명확하게 답변하세요' },
                  { icon: <Users className="w-4 h-4" />, text: '긍정적인 어조를 유지하세요' }
                ].map((tip, index) => (
                  <div key={index} className="flex items-start p-3 bg-white rounded-lg shadow-sm">
                    <div className="text-primary-600 mr-3 mt-0.5">
                      {tip.icon}
                    </div>
                    <span className="text-sm text-gray-700">{tip.text}</span>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  )
}