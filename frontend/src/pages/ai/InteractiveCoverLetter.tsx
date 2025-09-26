import { useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { MessageCircle, ArrowRight, ArrowLeft, RefreshCw, Copy, Check, Download, Brain, CheckCircle } from 'lucide-react'
import { aiClient } from '@/services/api'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/Card'
import { Button } from '@/components/ui/Button'
import { Input } from '@/components/ui/Input'
import { Textarea } from '@/components/ui/Textarea'

interface QuestionData {
  type: 'text' | 'selection'
  question: string
  options?: Array<{ value: string; label: string }>
  max_selections?: number
  placeholder?: string
}

interface InteractiveSession {
  sessionId: string
  currentStep: number
  totalSteps: number
  currentQuestion: QuestionData | null
  responses: string[]
  selections: string[][]
  isCompleted: boolean
  generatedContent?: string
  section: string
  processType: string
}

export default function InteractiveCoverLetter() {
  const [session, setSession] = useState<InteractiveSession | null>(null)
  const [currentAnswer, setCurrentAnswer] = useState('')
  const [selectedOptions, setSelectedOptions] = useState<string[]>([])
  const [companyName, setCompanyName] = useState('')
  const [position, setPosition] = useState('')
  const [selectedSection, setSelectedSection] = useState<string>('')
  const [copiedContent, setCopiedContent] = useState(false)
  const [completedSections, setCompletedSections] = useState<Set<string>>(new Set())

  // 인터랙티브 세션 시작
  const startInteractiveMutation = useMutation({
    mutationFn: (data: { companyName: string; position: string; section: string }) =>
      aiClient.startInteractiveCoverLetter(data),
    onSuccess: (response) => {
      if (response.success && response.data) {
        setSession({
          sessionId: response.data.session_id,
          currentStep: response.data.current_step,
          totalSteps: response.data.total_steps,
          currentQuestion: response.data.question, // 백엔드 응답에서는 'question'으로 옴
          responses: [],
          selections: [],
          isCompleted: false,
          section: selectedSection,
          processType: response.data.process_type || 'interactive'
        })
        setCurrentAnswer('')
        setSelectedOptions([])
      }
    },
    onError: (error) => {
      console.error('인터랙티브 세션 시작 실패:', error)
      alert('인터랙티브 자소서 생성을 시작할 수 없습니다. 다시 시도해주세요.')
    }
  })

  // 답변 제출
  const submitAnswerMutation = useMutation({
    mutationFn: (data: { sessionId: string; answer: string; selections?: string[] }) =>
      aiClient.submitInteractiveAnswer(data),
    onSuccess: (response) => {
      if (response.success && response.data) {
        if (response.data.is_completed) {
          // 완료된 섹션을 추가
          setCompletedSections(prev => new Set([...prev, selectedSection]))
          setSession(prev => prev ? {
            ...prev,
            isCompleted: true,
            generatedContent: response.data.generated_content
          } : null)
        } else {
          setSession(prev => prev ? {
            ...prev,
            currentStep: response.data.current_step,
            currentQuestion: response.data.question, // 백엔드 응답에서는 'question'으로 옴
            responses: [...prev.responses, currentAnswer],
            selections: [...prev.selections, selectedOptions]
          } : null)
          setCurrentAnswer('')
          setSelectedOptions([])
        }
      }
    },
    onError: (error) => {
      console.error('답변 제출 실패:', error)
      alert('답변 제출에 실패했습니다. 다시 시도해주세요.')
    }
  })

  const startSession = () => {
    if (!companyName || !position || !selectedSection) {
      alert('회사명, 포지션, 섹션을 모두 선택해주세요.')
      return
    }
    startInteractiveMutation.mutate({
      companyName,
      position,
      section: selectedSection
    })
  }

  const submitAnswer = () => {
    if (!session) return
    
    const answer = session.currentQuestion?.type === 'text' ? currentAnswer : ''
    const selections = session.currentQuestion?.type === 'selection' ? selectedOptions : undefined
    
    if (session.currentQuestion?.type === 'text' && !answer.trim()) {
      alert('답변을 입력해주세요.')
      return
    }
    
    if (session.currentQuestion?.type === 'selection' && selections?.length === 0) {
      alert('옵션을 선택해주세요.')
      return
    }

    submitAnswerMutation.mutate({
      sessionId: session.sessionId,
      answer,
      selections
    })
  }

  const goBackToPrevious = () => {
    if (!session || session.currentStep <= 1) return
    
    // 이전 단계로 돌아가는 로직 (실제 구현에서는 API 호출이 필요할 수 있음)
    const prevStep = session.currentStep - 1
    setSession(prev => prev ? {
      ...prev,
      currentStep: prevStep,
      responses: prev.responses.slice(0, prevStep - 1),
      selections: prev.selections.slice(0, prevStep - 1)
    } : null)
  }

  const resetSession = () => {
    setSession(null)
    setCurrentAnswer('')
    setSelectedOptions([])
    setCompanyName('')
    setPosition('')
    setSelectedSection('')
    setCompletedSections(new Set())
  }

  const startNewSection = (section: string) => {
    setSession(null)
    setCurrentAnswer('')
    setSelectedOptions([])
    setSelectedSection(section)
  }

  const toggleSelection = (value: string) => {
    const currentQuestion = session?.currentQuestion
    if (!currentQuestion) return

    const maxSelections = currentQuestion.max_selections || 1
    
    if (selectedOptions.includes(value)) {
      setSelectedOptions(prev => prev.filter(opt => opt !== value))
    } else {
      if (maxSelections === 1) {
        setSelectedOptions([value])
      } else {
        if (selectedOptions.length < maxSelections) {
          setSelectedOptions(prev => [...prev, value])
        }
      }
    }
  }

  const copyToClipboard = async (content: string) => {
    try {
      await navigator.clipboard.writeText(content)
      setCopiedContent(true)
      setTimeout(() => setCopiedContent(false), 2000)
    } catch (error) {
      console.error('복사 실패:', error)
    }
  }

  const downloadAsText = (content: string) => {
    const blob = new Blob([content], { type: 'text/plain;charset=utf-8' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `${selectedSection}_자소서.txt`
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    URL.revokeObjectURL(url)
  }

  const getSectionTitle = (section: string) => {
    // 이제 한국어 섹션명을 바로 사용하므로 그대로 반환
    return section
  }

  // 세션이 시작되지 않은 경우
  if (!session) {
    return (
      <div className="space-y-6">
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Brain className="w-5 h-5" />
              AI 자기소개서 생성
            </CardTitle>
            <p className="text-sm text-gray-600">
              AI가 단계별로 질문하며 맞춤형 자기소개서를 생성해드립니다.
            </p>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium mb-2">회사명 *</label>
                <Input
                  value={companyName}
                  onChange={(e) => setCompanyName(e.target.value)}
                  placeholder="지원하는 회사명을 입력하세요"
                />
              </div>
              <div>
                <label className="block text-sm font-medium mb-2">포지션 *</label>
                <Input
                  value={position}
                  onChange={(e) => setPosition(e.target.value)}
                  placeholder="지원하는 포지션을 입력하세요"
                />
              </div>
            </div>
            
            <div>
              <label className="block text-sm font-medium mb-2">생성할 섹션 선택 *</label>
              <div className="grid grid-cols-2 md:grid-cols-4 gap-2">
                {[
                  { value: '성장과정', label: '성장과정', icon: '🌱' },
                  { value: '나의 장점', label: '나의 장점', icon: '💪' },
                  { value: '커뮤니케이션', label: '커뮤니케이션', icon: '💬' },
                  { value: '기업 분석', label: '기업 분석', icon: '🏢' }
                ].map((section) => {
                  const isCompleted = completedSections.has(section.value)
                  const isSelected = selectedSection === section.value
                  
                  return (
                    <Button
                      key={section.value}
                      onClick={() => setSelectedSection(section.value)}
                      variant={isSelected ? "default" : "outline"}
                      className={`h-14 flex flex-col items-center gap-1 relative ${
                        isCompleted ? 'bg-green-50 border-green-200 hover:bg-green-100' : 
                        isSelected ? '' : 'hover:bg-blue-50'
                      }`}
                    >
                      <span className="text-base">{section.icon}</span>
                      <span className="text-xs">{section.label}</span>
                      {isCompleted && (
                        <span className="absolute -top-1 -right-1 text-xs text-green-600">✅</span>
                      )}
                    </Button>
                  )
                })}
              </div>
            </div>

            <Button
              onClick={startSession}
              disabled={!companyName || !position || !selectedSection || startInteractiveMutation.isPending}
              className="w-full bg-blue-600 hover:bg-blue-700"
            >
              {startInteractiveMutation.isPending ? (
                <RefreshCw className="w-4 h-4 mr-2 animate-spin" />
              ) : (
                <MessageCircle className="w-4 h-4 mr-2" />
              )}
              자기소개서 생성 시작
            </Button>
          </CardContent>
        </Card>
      </div>
    )
  }

  // 완료된 경우
  if (session.isCompleted && session.generatedContent) {
    return (
      <div className="space-y-6">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between">
            <div>
              <CardTitle className="flex items-center gap-2">
                <CheckCircle className="w-5 h-5 text-green-600" />
                {getSectionTitle(session.section)} 생성 완료
              </CardTitle>
              <p className="text-sm text-gray-600 mt-1">
                {companyName} {position} 포지션을 위한 맞춤형 자기소개서가 완성되었습니다.
              </p>
            </div>
            <div className="flex gap-2">
              <Button
                onClick={() => copyToClipboard(session.generatedContent!)}
                variant="ghost"
                size="sm"
              >
                {copiedContent ? (
                  <Check className="w-4 h-4 text-green-600" />
                ) : (
                  <Copy className="w-4 h-4" />
                )}
              </Button>
              <Button
                onClick={() => downloadAsText(session.generatedContent!)}
                variant="ghost"
                size="sm"
              >
                <Download className="w-4 h-4" />
              </Button>
            </div>
          </CardHeader>
          <CardContent>
            <div className="whitespace-pre-wrap text-sm leading-relaxed border rounded-lg p-4 bg-gray-50">
              {session.generatedContent}
            </div>
            <div className="mt-6 space-y-4">
              <div className="flex gap-2">
                <Button onClick={resetSession} variant="outline">
                  새로 작성하기
                </Button>
              </div>
              
              <div>
                <h4 className="text-sm font-medium mb-3 text-gray-700">다른 섹션도 작성해보세요</h4>
                <div className="grid grid-cols-2 md:grid-cols-3 gap-2">
                  {[
                    { value: '성장과정', label: '성장과정', icon: '🌱' },
                    { value: '나의 장점', label: '나의 장점', icon: '💪' },
                    { value: '커뮤니케이션', label: '커뮤니케이션', icon: '💬' },
                    { value: '기업 분석', label: '기업 분석', icon: '🏢' }
                  ]
                    .filter(section => !completedSections.has(section.value))
                    .map((section) => (
                    <Button
                      key={section.value}
                      onClick={() => startNewSection(section.value)}
                      variant="outline"
                      className="h-auto p-3 flex flex-col items-center gap-1 hover:bg-blue-50 border-blue-200"
                    >
                      <span className="text-lg">{section.icon}</span>
                      <span className="text-xs">{section.label}</span>
                    </Button>
                  ))}
                </div>
                
                {completedSections.size > 0 && (
                  <div className="mt-3">
                    <p className="text-xs text-green-600">
                      ✅ 완료된 섹션: {Array.from(completedSections).join(', ')}
                    </p>
                  </div>
                )}
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    )
  }

  // 진행 중인 경우
  return (
    <div className="space-y-6">
      {/* 진행 상황 표시 */}
      <Card>
        <CardContent className="pt-6">
          <div className="flex items-center justify-between mb-4">
            <div className="text-sm text-gray-600">
              {getSectionTitle(session.section)} 생성 진행 중
            </div>
            <div className="text-sm font-medium">
              {session.currentStep} / {session.totalSteps}
            </div>
          </div>
          <div className="w-full bg-gray-200 rounded-full h-2">
            <div
              className="bg-blue-600 h-2 rounded-full transition-all duration-300"
              style={{
                width: `${(session.currentStep / session.totalSteps) * 100}%`
              }}
            />
          </div>
        </CardContent>
      </Card>

      {/* 현재 질문 */}
      {session.currentQuestion && (
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <MessageCircle className="w-5 h-5" />
              질문 {session.currentStep}
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="text-sm leading-relaxed whitespace-pre-wrap">
              {session.currentQuestion.question}
            </div>

            {session.currentQuestion.type === 'text' && (
              <Textarea
                value={currentAnswer}
                onChange={(e) => setCurrentAnswer(e.target.value)}
                placeholder={session.currentQuestion.placeholder || '답변을 입력해주세요'}
                rows={4}
                className="w-full"
              />
            )}

            {session.currentQuestion.type === 'selection' && session.currentQuestion.options && (
              <div className="space-y-2">
                <div className="text-xs text-gray-500">
                  {session.currentQuestion.max_selections === 1
                    ? '하나를 선택해주세요'
                    : `최대 ${session.currentQuestion.max_selections}개까지 선택 가능`}
                </div>
                <div className="grid grid-cols-1 gap-2">
                  {session.currentQuestion.options.map((option) => (
                    <Button
                      key={option.value}
                      onClick={() => toggleSelection(option.value)}
                      variant={selectedOptions.includes(option.value) ? "default" : "outline"}
                      className="justify-start h-auto p-3 text-left"
                    >
                      {option.label}
                    </Button>
                  ))}
                </div>
                {selectedOptions.length > 0 && (
                  <div className="text-xs text-gray-600">
                    선택됨: {selectedOptions.length}개
                  </div>
                )}
              </div>
            )}

            <div className="flex justify-between pt-4">
              <Button
                onClick={goBackToPrevious}
                variant="outline"
                disabled={session.currentStep <= 1}
              >
                <ArrowLeft className="w-4 h-4 mr-2" />
                이전
              </Button>
              <Button
                onClick={submitAnswer}
                disabled={submitAnswerMutation.isPending}
                className="bg-blue-600 hover:bg-blue-700"
              >
                {submitAnswerMutation.isPending ? (
                  <>
                    <RefreshCw className="w-4 h-4 mr-2 animate-spin" />
                    {session.currentStep === session.totalSteps ? '자소서 생성 중...' : '처리 중...'}
                  </>
                ) : (
                  <>
                    <ArrowRight className="w-4 h-4 mr-2" />
                    {session.currentStep === session.totalSteps ? '자소서 생성 완료' : '다음'}
                  </>
                )}
              </Button>
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  )
}