import { useState, useRef, useEffect } from 'react'
import { useMutation, useQuery } from '@tanstack/react-query'
import { Send, Bot, User, MessageCircle, Sparkles, RefreshCw, ChevronDown, AlertCircle, Loader2, WifiOff } from 'lucide-react'
import { aiClient } from '@/services/api'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/Card'
import { Button } from '@/components/ui/Button'
import { Input } from '@/components/ui/Input'
import { Badge } from '@/components/ui/Badge'
import LoadingSpinner from '@/components/ui/LoadingSpinner'
import LoadingCard from '@/components/ui/LoadingCard'
import ErrorMessage from '@/components/ui/ErrorMessage'
import { useToast } from '@/contexts/ToastContext'
import type { ChatSuggestion } from '@/types/api'

interface Message {
  id: string
  type: 'user' | 'bot'
  content: string
  timestamp: Date
}

const DEFAULT_SUGGESTIONS: ChatSuggestion[] = [
  {
    category: "면접 준비",
    suggestions: [
      "백엔드 개발자 면접 질문을 만들어주세요",
      "면접에서 자주 나오는 질문들을 알려주세요",
      "기술 면접 준비 방법을 알려주세요"
    ]
  },
  {
    category: "자기소개서",
    suggestions: [
      "자기소개서 작성 팁을 알려주세요",
      "지원동기 작성법을 알려주세요",
      "성장과정을 어떻게 써야 할까요?"
    ]
  },
  {
    category: "취업 준비",
    suggestions: [
      "신입 개발자 취업 준비 방법을 알려주세요",
      "포트폴리오 작성 가이드를 알려주세요",
      "코딩테스트 준비 방법을 알려주세요"
    ]
  },
  {
    category: "일반 질문",
    suggestions: [
      "개발자 커리어 경로에 대해 알려주세요",
      "프로그래밍 언어 선택 기준을 알려주세요",
      "개발자로서의 성장 방법을 알려주세요"
    ]
  }
]

export default function Chatbot() {
  const [messages, setMessages] = useState<Message[]>([])
  const [inputMessage, setInputMessage] = useState('')
  const [showSuggestions, setShowSuggestions] = useState(true)
  const messagesEndRef = useRef<HTMLDivElement>(null)
  const inputRef = useRef<HTMLInputElement>(null)
  const { showError, showSuccess } = useToast()

  // Fetch suggestions from API - 향상된 에러 처리
  const {
    data: suggestionsData,
    isLoading: suggestionsLoading,
    error: suggestionsError,
    refetch: refetchSuggestions
  } = useQuery({
    queryKey: ['chatbot-categories'],
    queryFn: async () => {
      console.log('🔄 Fetching chatbot categories...')
      try {
        const result = await aiClient.getChatbotCategories()
        console.log('✅ Chatbot categories fetched successfully:', result)
        return result
      } catch (error) {
        console.error('❌ Failed to fetch chatbot categories:', error)
        throw error
      }
    },
    staleTime: 1000 * 60 * 10, // 10 minutes
    retry: 2,
    retryDelay: 1000,
    refetchOnWindowFocus: false,
  })

  // Fetch welcome message from API - 향상된 에러 처리
  const { 
    data: welcomeData, 
    isLoading: welcomeLoading, 
    error: welcomeError,
    refetch: refetchWelcome 
  } = useQuery({
    queryKey: ['chatbot-welcome'],
    queryFn: () => aiClient.getWelcomeMessage(),
    staleTime: 1000 * 60 * 30, // 30 minutes
    retry: 2,
    retryDelay: 1000,
    refetchOnWindowFocus: false,
  })

  const sendMessageMutation = useMutation({
    mutationFn: (message: string) => aiClient.sendChatMessage('user_' + Date.now(), message),
    onSuccess: (response) => {
      if (response.success && response.data) {
        const botMessage: Message = {
          id: Date.now().toString() + '_bot',
          type: 'bot',
          content: response.data.response,
          timestamp: new Date()
        }
        setMessages(prev => [...prev, botMessage])
      } else {
        const errorMessage: Message = {
          id: Date.now().toString() + '_error',
          type: 'bot',
          content: '응답을 받을 수 없습니다. 다시 시도해주세요.',
          timestamp: new Date()
        }
        setMessages(prev => [...prev, errorMessage])
      }
    },
    onError: (error: any) => {
      console.error('Chat error:', error)
      let errorText = '죄송합니다. 일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.'
      
      if (error?.response?.status === 503) {
        errorText = 'AI 서비스에 연결할 수 없습니다. 서비스 상태를 확인해주세요.'
      } else if (error?.response?.status === 400) {
        errorText = '메시지 처리 중 오류가 발생했습니다. 다시 시도해주세요.'
      } else if (error?.response?.status === 500) {
        errorText = 'AI 서비스에서 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요.'
      } else if (error?.code === 'NETWORK_ERROR' || error?.message?.includes('fetch')) {
        errorText = '네트워크 연결을 확인해주세요.'
      } else if (error?.code === 'ECONNREFUSED') {
        errorText = 'AI 서비스에 연결할 수 없습니다. 관리자에게 문의해주세요.'
      }
      
      const errorMessage: Message = {
        id: Date.now().toString() + '_error',
        type: 'bot',
        content: errorText,
        timestamp: new Date()
      }
      setMessages(prev => [...prev, errorMessage])
    }
  })

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }

  // Initialize messages with welcome message when component mounts
  useEffect(() => {
    if (!welcomeLoading && welcomeData) {
      const welcomeMessage = welcomeData.success && welcomeData.data ? 
        welcomeData.data.message : 
        '안녕하세요! 잡았다 AI 챗봇입니다. 취업 준비와 관련된 모든 질문에 답변해드릴 수 있어요. 무엇을 도와드릴까요?'
      
      setMessages([{
        id: '1',
        type: 'bot',
        content: welcomeMessage,
        timestamp: new Date()
      }])
    }
  }, [welcomeData, welcomeLoading])

  useEffect(() => {
    scrollToBottom()
  }, [messages])

  const handleSendMessage = () => {
    if (!inputMessage.trim() || sendMessageMutation.isPending) return

    const userMessage: Message = {
      id: Date.now().toString() + '_user',
      type: 'user',
      content: inputMessage.trim(),
      timestamp: new Date()
    }

    setMessages(prev => [...prev, userMessage])
    setShowSuggestions(false)
    const messageToSend = inputMessage.trim()
    setInputMessage('')
    sendMessageMutation.mutate(messageToSend)
    
    // Focus input after sending
    setTimeout(() => inputRef.current?.focus(), 100)
  }

  const handleSuggestionClick = (suggestion: string) => {
    if (sendMessageMutation.isPending) return
    
    setInputMessage(suggestion)
    setShowSuggestions(false)
    
    // Auto-send the suggestion
    const userMessage: Message = {
      id: Date.now().toString() + '_user',
      type: 'user',
      content: suggestion,
      timestamp: new Date()
    }

    setMessages(prev => [...prev, userMessage])
    sendMessageMutation.mutate(suggestion)
    
    setTimeout(() => inputRef.current?.focus(), 100)
  }

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      handleSendMessage()
    }
  }

  const clearChat = () => {
    const welcomeMessage = welcomeData?.success && welcomeData.data ? 
      welcomeData.data.message : 
      '안녕하세요! 잡았다 AI 챗봇입니다. 취업 준비와 관련된 모든 질문에 답변해드릴 수 있어요. 무엇을 도와드릴까요?'
    
    setMessages([{
      id: '1',
      type: 'bot',
      content: welcomeMessage,
      timestamp: new Date()
    }])
    setShowSuggestions(true)
  }

  return (
    <div className="container mx-auto px-4 py-8 max-w-6xl ">
      <div className="text-center mb-8">
        <div className="flex items-center justify-center gap-3 mb-4">
          <div className="p-3 bg-gradient-to-br from-primary-500 to-purple-600 rounded-full" aria-hidden="true">
            <Bot className="h-8 w-8 text-white" />
          </div>
          <h1 className="text-3xl font-bold bg-gradient-to-r from-primary-600 to-purple-600 bg-clip-text text-transparent dark:from-primary-400 dark:to-purple-400">
            AI 챗봇
          </h1>
        </div>
        <p className="text-gray-600 dark:text-gray-400" role="doc-subtitle">
          취업 준비의 모든 것을 도와드리는 AI 챗봇입니다
        </p>
      </div>

      <Card className="h-[800px] flex flex-col">
        <CardHeader className="flex-row items-center justify-between space-y-0 pb-4">
          <div className="flex items-center gap-2">
            <MessageCircle className="h-5 w-5 text-primary-500 dark:text-primary-400" />
            <CardTitle className="text-lg">대화</CardTitle>
            {(suggestionsError || welcomeError) && (
              <AlertCircle className="h-4 w-4 text-orange-500" title="일부 기능에서 오류가 발생했습니다" />
            )}
          </div>
          <div className="flex items-center gap-2">
            <Button
              variant="outline"
              size="sm"
              onClick={() => setShowSuggestions(!showSuggestions)}
            >
              <ChevronDown className={`h-4 w-4 transition-transform ${showSuggestions ? 'rotate-180' : ''}`} />
              제안
            </Button>
            {(suggestionsError || welcomeError) && (
              <Button
                variant="outline"
                size="sm"
                onClick={() => {
                  refetchSuggestions()
                  refetchWelcome()
                }}
                className="text-orange-600 hover:text-orange-700"
              >
                <RefreshCw className="h-4 w-4" />
                재시도
              </Button>
            )}
            <Button variant="outline" size="sm" onClick={clearChat}>
              <RefreshCw className="h-4 w-4" />
              초기화
            </Button>
          </div>
        </CardHeader>

        <CardContent className="flex-1 flex flex-col overflow-hidden">
          {/* 제안 섹션 */}
          {showSuggestions && (
            <div className="mb-4 p-4 bg-gray-50 rounded-lg">
              <h3 className="font-medium mb-3 flex items-center gap-2">
                <Sparkles className="h-4 w-4 text-yellow-500" />
                추천 질문
              </h3>
              <div className="space-y-3">
                {suggestionsLoading ? (
                  <div className="flex items-center justify-center gap-2 text-gray-500 py-4">
                    <Loader2 className="w-4 h-4 animate-spin" />
                    <span>추천 질문을 불러오는 중...</span>
                  </div>
                ) : suggestionsError ? (
                  <div className="text-center py-4">
                    <div className="flex flex-col items-center gap-3">
                      <WifiOff className="w-8 h-8 text-orange-500" />
                      <p className="text-sm text-orange-600">추천 질문을 불러올 수 없습니다</p>
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => refetchSuggestions()}
                        className="text-orange-600 hover:text-orange-700"
                      >
                        <RefreshCw className="w-4 h-4 mr-1" />
                        다시 시도
                      </Button>
                    </div>
                  </div>
                ) : (
                  (suggestionsData?.success && suggestionsData.data ? suggestionsData.data : DEFAULT_SUGGESTIONS).map((category: any, index: number) => (
                    <div key={index}>

                      <p className="text-1xl font-medium text-blue-600 mb-4">{category.category}</p>
                      
                      <div className="flex flex-wrap gap-4">
                        {category.suggestions.map((suggestion: string, sugIndex: number) => (
                          <Badge
                            key={sugIndex}
                            variant="secondary"
                            className="cursor-pointer hover:bg-blue-100 hover:text-blue-700 transition-colors
                                             px-4 py-1 text-sm font-medium" 
                            onClick={() => handleSuggestionClick(suggestion)}
                          >
                            {suggestion}
                          </Badge>
                        ))}
                      </div>
                    </div>
                  ))
                )}
                
                {/* API 연결 상태 표시 */}
                <div className="mt-4 flex items-center justify-center gap-2 text-xs">
                  {suggestionsError ? (
                    <>
                      <div className="w-2 h-2 bg-orange-500 rounded-full"></div>
                      <span className="text-orange-600">추천 서비스 연결 실패</span>
                    </>
                  ) : (
                    <>
                      <div className="w-2 h-2 bg-green-500 rounded-full animate-pulse"></div>
                      <span className="text-green-600">AI 서비스 연결됨</span>
                    </>
                  )}
                </div>
              </div>
            </div>
          )}

          {/* 메시지 영역 */}
          <div className="flex-1 overflow-y-auto space-y-4 pr-2">
            {messages.map((message) => (
              <div
                key={message.id}
                className={`flex gap-3 ${message.type === 'user' ? 'justify-end' : 'justify-start'}`}
              >
                {message.type === 'bot' && (
                  <div className="flex-shrink-0 w-8 h-8 bg-gradient-to-br from-blue-500 to-purple-600 rounded-full flex items-center justify-center">
                    <Bot className="h-4 w-4 text-white" />
                  </div>
                )}
                
                <div
                  className={`max-w-[80%] rounded-lg p-3 ${
                    message.type === 'user'
                      ? 'bg-blue-500 text-white ml-auto'
                      : 'bg-gray-100 text-gray-800'
                  }`}
                >
                  <p className="whitespace-pre-wrap">{message.content}</p>
                  <p className={`text-xs mt-2 ${
                    message.type === 'user' ? 'text-blue-100' : 'text-gray-500'
                  }`}>
                    {message.timestamp.toLocaleTimeString('ko-KR', { 
                      hour: '2-digit', 
                      minute: '2-digit' 
                    })}
                  </p>
                </div>

                {message.type === 'user' && (
                  <div className="flex-shrink-0 w-8 h-8 bg-gray-300 rounded-full flex items-center justify-center">
                    <User className="h-4 w-4 text-gray-600" />
                  </div>
                )}
              </div>
            ))}
            
            {sendMessageMutation.isPending && (
              <div className="flex gap-3 justify-start">
                <div className="flex-shrink-0 w-8 h-8 bg-gradient-to-br from-blue-500 to-purple-600 rounded-full flex items-center justify-center">
                  <Bot className="h-4 w-4 text-white" />
                </div>
                <div className="bg-gray-100 rounded-lg p-3">
                  <div className="flex items-center gap-2">
                    <div className="flex space-x-1">
                      <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce"></div>
                      <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style={{ animationDelay: '0.1s' }}></div>
                      <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style={{ animationDelay: '0.2s' }}></div>
                    </div>
                    <span className="text-sm text-gray-500">답변 중...</span>
                  </div>
                </div>
              </div>
            )}
            <div ref={messagesEndRef} />
          </div>

          {/* 입력 영역 */}
          <div className="flex gap-2 mt-4 border-t pt-4" role="form" aria-label="채팅 메시지 입력">
            <Input
              ref={inputRef}
              value={inputMessage}
              onChange={(e) => setInputMessage(e.target.value)}
              onKeyPress={handleKeyPress}
              placeholder="메시지를 입력하세요..."
              disabled={sendMessageMutation.isPending}
              className="flex-1"
              aria-label="채팅 메시지 입력"
              aria-describedby="chat-input-help"
            />
            <span id="chat-input-help" className="sr-only">
              Enter 키를 눌러서 메시지를 전송하거나 전송 버튼을 클릭하세요
            </span>
            <Button
              onClick={handleSendMessage}
              disabled={!inputMessage.trim() || sendMessageMutation.isPending}
              className="px-4"
              aria-label={sendMessageMutation.isPending ? '메시지 전송 중' : '메시지 전송'}
            >
              <Send className="h-4 w-4" aria-hidden="true" />
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}