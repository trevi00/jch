import { useState, useEffect } from 'react'
import { useMutation, useQuery } from '@tanstack/react-query'
import { Mail, Send, Inbox, Clock, Globe, BarChart3, FileText, Plus, Trash2, Eye, Download } from 'lucide-react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/Card'
import { Button } from '@/components/ui/Button'
import { Input } from '@/components/ui/Input'
import { Badge } from '@/components/ui/Badge'
import { useAuthStore } from '@/hooks/useAuthStore'
import apiClient from '@/services/api'

interface EmailHistory {
  id: number
  recipientEmail: string
  recipientName?: string
  subject: string
  content: string
  isTranslated: boolean
  status: 'SENT' | 'FAILED' | 'PENDING'
  createdAt: string
  originalLanguage?: string
  translatedLanguage?: string
}

interface EmailStats {
  totalSentCount: number
  translatedCount: number
  regularCount: number
}

interface SendEmailRequest {
  to: string
  from?: string
  subject: string
  content: string
  translationNeeded?: boolean
  sourceLanguage?: string
  targetLanguage?: string
  documentType?: string
}

export default function Webmail() {
  const { user, token } = useAuthStore()
  const [showCompose, setShowCompose] = useState(false)
  const [activeTab, setActiveTab] = useState<'compose' | 'sent' | 'translated' | 'stats'>('compose')
  const [showSuccessMessage, setShowSuccessMessage] = useState(false)
  const [showErrorMessage, setShowErrorMessage] = useState(false)
  const [errorMessage, setErrorMessage] = useState('')
  const [formData, setFormData] = useState<SendEmailRequest>({
    to: '',
    from: '',
    subject: '',
    content: '',
    translationNeeded: false,
    sourceLanguage: 'ko',
    targetLanguage: 'en',
    documentType: 'email'
  })


  const sendEmailMutation = useMutation({
    mutationFn: apiClient.sendEmail,
    onSuccess: (response) => {
      console.log('Email send response:', response);
      if (response && response.success) {
        setFormData({
          to: '',
          from: '',
          subject: '',
          content: '',
          translationNeeded: false,
          sourceLanguage: 'ko',
          targetLanguage: 'en',
          documentType: 'email'
        })
        setShowCompose(false)
        setShowSuccessMessage(true)
        // Refetch sent emails
        refetchSentEmails()
      } else {
        // Handle case where response exists but success is false
        const message = response?.message || '이메일 발송 중 오류가 발생했습니다.'
        console.error('Email send failed with response:', response);
        setErrorMessage(message)
        setShowErrorMessage(true)
      }
    },
    onError: (error: any) => {
      console.error('Email send error:', error);
      const message = error.response?.data?.message || error.message || '이메일 발송 중 오류가 발생했습니다.'
      setErrorMessage(message)
      setShowErrorMessage(true)
    }
  })

  const { data: sentEmailsData, refetch: refetchSentEmails } = useQuery({
    queryKey: ['sent-emails'],
    queryFn: () => apiClient.getSentEmails(),
    enabled: activeTab === 'sent'
  })

  const { data: translatedEmailsData } = useQuery({
    queryKey: ['translated-emails'],
    queryFn: apiClient.getTranslatedEmails,
    enabled: activeTab === 'translated'
  })

  const { data: statsData } = useQuery({
    queryKey: ['email-stats'],
    queryFn: apiClient.getEmailStats,
    enabled: activeTab === 'stats'
  })

  const handleInputChange = (field: keyof SendEmailRequest, value: string | boolean) => {
    setFormData(prev => ({ ...prev, [field]: value }))
  }

  const handleSendEmail = () => {
    if (!formData.to || !formData.subject || !formData.content) return
    
    // from 필드가 있으면 본문에 포함
    const emailData = { ...formData }
    if (formData.from && formData.from.trim()) {
      emailData.content = `From: ${formData.from}\n\n${formData.content}`
    }
    
    sendEmailMutation.mutate(emailData)
  }

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  }

  const getStatusBadge = (status: string) => {
    const statusConfig = {
      SENT: { label: '발송완료', className: 'bg-green-100 text-green-800' },
      FAILED: { label: '발송실패', className: 'bg-red-100 text-red-800' },
      PENDING: { label: '발송중', className: 'bg-yellow-100 text-yellow-800' }
    }
    const config = statusConfig[status as keyof typeof statusConfig]
    return (
      <Badge className={config?.className || 'bg-gray-100 text-gray-800'}>
        {config?.label || status}
      </Badge>
    )
  }

  return (
    <div className="container mx-auto px-4 py-8 max-w-6xl">
      {/* Success Message Modal */}
      {showSuccessMessage && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 animate-fadeIn">
          <div className="bg-white rounded-xl p-8 max-w-md mx-4 shadow-2xl animate-slideUp">
            <div className="text-center">
              <div className="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <svg className="w-8 h-8 text-green-600" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
                </svg>
              </div>
              <h3 className="text-xl font-semibold text-gray-900 mb-2">발송 완료!</h3>
              <p className="text-gray-600 mb-6">이메일이 성공적으로 발송되었습니다.</p>
              <button
                onClick={() => setShowSuccessMessage(false)}
                className="bg-green-600 text-white px-6 py-2 rounded-lg hover:bg-green-700 transition-colors font-medium"
              >
                확인
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Error Message Modal */}
      {showErrorMessage && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 animate-fadeIn">
          <div className="bg-white rounded-xl p-8 max-w-md mx-4 shadow-2xl animate-slideUp">
            <div className="text-center">
              <div className="w-16 h-16 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <svg className="w-8 h-8 text-red-600" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
                </svg>
              </div>
              <h3 className="text-xl font-semibold text-gray-900 mb-2">발송 실패</h3>
              <p className="text-gray-600 mb-6">{errorMessage}</p>
              <button
                onClick={() => setShowErrorMessage(false)}
                className="bg-red-600 text-white px-6 py-2 rounded-lg hover:bg-red-700 transition-colors font-medium"
              >
                확인
              </button>
            </div>
          </div>
        </div>
      )}

      <div className="text-center mb-8">
        <div className="flex items-center justify-center gap-3 mb-4">
          <div className="p-3 bg-gradient-to-br from-blue-500 to-indigo-600 rounded-full">
            <Mail className="h-8 w-8 text-white" />
          </div>
          <h1 className="text-3xl font-bold bg-gradient-to-r from-blue-600 to-indigo-600 bg-clip-text text-transparent">
            웹메일
          </h1>
        </div>
        <p className="text-gray-600">
          AI 번역 기능이 포함된 스마트 이메일 시스템
        </p>
      </div>

      {/* Tab Navigation */}
      <div className="flex flex-wrap gap-2 mb-6 p-1 bg-gray-100 rounded-lg">
        {[
          { key: 'compose', label: '새 메일', icon: Plus },
          { key: 'sent', label: '보낸편지함', icon: Inbox },
          { key: 'translated', label: '번역메일', icon: Globe },
          { key: 'stats', label: '통계', icon: BarChart3 }
        ].map(({ key, label, icon: Icon }) => (
          <button
            key={key}
            onClick={() => setActiveTab(key as any)}
            className={`flex items-center gap-2 px-4 py-2 rounded-md font-medium transition-colors ${
              activeTab === key
                ? 'bg-white text-blue-600 shadow-sm'
                : 'text-gray-600 hover:text-gray-900'
            }`}
          >
            <Icon className="w-4 h-4" />
            {label}
          </button>
        ))}
      </div>

      {/* Compose Email */}
      {activeTab === 'compose' && (
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Send className="w-5 h-5" />
              새 메일 작성
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                받는 사람 이메일 *
              </label>
              <Input
                type="email"
                value={formData.to}
                onChange={(e) => handleInputChange('to', e.target.value)}
                placeholder="recipient@example.com"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                보내는 사람 이메일 (선택)
              </label>
              <Input
                type="email"
                value={formData.from || ''}
                onChange={(e) => handleInputChange('from', e.target.value)}
                placeholder="sender@example.com"
              />
              <p className="text-xs text-gray-500 mt-1">
                입력하면 메일 본문 상단에 'From: 이메일' 형태로 포함됩니다
              </p>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                제목 *
              </label>
              <Input
                value={formData.subject}
                onChange={(e) => handleInputChange('subject', e.target.value)}
                placeholder="이메일 제목을 입력하세요"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                내용 *
              </label>
              <textarea
                rows={8}
                value={formData.content}
                onChange={(e) => handleInputChange('content', e.target.value)}
                placeholder="이메일 내용을 입력하세요..."
                className="w-full min-h-32 p-3 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>


            <div className="flex items-center gap-4">
              <label className="flex items-center gap-2">
                <input
                  type="checkbox"
                  checked={formData.translationNeeded || false}
                  onChange={(e) => handleInputChange('translationNeeded', e.target.checked)}
                  className="w-4 h-4 text-blue-600 rounded"
                />
                <span className="text-sm text-gray-700">AI 번역 사용</span>
              </label>
              {formData.translationNeeded && (
                <div className="flex gap-2">
                  <select
                    value={formData.sourceLanguage || 'ko'}
                    onChange={(e) => handleInputChange('sourceLanguage', e.target.value)}
                    className="px-3 py-1 border rounded text-sm"
                  >
                    <option value="ko">한국어</option>
                    <option value="en">English</option>
                  </select>
                  <span className="text-gray-500">→</span>
                  <select
                    value={formData.targetLanguage || 'en'}
                    onChange={(e) => handleInputChange('targetLanguage', e.target.value)}
                    className="px-3 py-1 border rounded text-sm"
                  >
                    <option value="en">English</option>
                    <option value="ko">한국어</option>
                  </select>
                </div>
              )}
            </div>

            <div className="flex justify-end">
              <Button
                onClick={handleSendEmail}
                disabled={!formData.to || !formData.subject || !formData.content || sendEmailMutation.isPending}
                className="flex items-center gap-2"
              >
                <Send className="w-4 h-4" />
                {sendEmailMutation.isPending ? '발송 중...' : '메일 발송'}
              </Button>
            </div>
          </CardContent>
        </Card>
      )}

      {/* Sent Emails */}
      {activeTab === 'sent' && (
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Inbox className="w-5 h-5" />
              보낸편지함
            </CardTitle>
          </CardHeader>
          <CardContent>
            {sentEmailsData?.success && sentEmailsData.data?.content ? (
              <div className="space-y-4">
                {sentEmailsData.data.content.map((email: EmailHistory) => (
                  <div key={email.id} className="border rounded-lg p-4 hover:bg-gray-50 transition-colors">
                    <div className="flex items-start justify-between">
                      <div className="flex-1">
                        <div className="flex items-center gap-2 mb-2">
                          <h3 className="font-semibold text-gray-900">{email.subject}</h3>
                          {getStatusBadge(email.status)}
                          {email.isTranslated && (
                            <Badge className="bg-blue-100 text-blue-800">
                              <Globe className="w-3 h-3 mr-1" />
                              번역됨
                            </Badge>
                          )}
                        </div>
                        <p className="text-sm text-gray-600 mb-2">
                          받는 사람: {email.recipientName ? `${email.recipientName} <${email.recipientEmail}>` : email.recipientEmail}
                        </p>
                        <p className="text-sm text-gray-500 line-clamp-2 mb-2">{email.content}</p>
                        <div className="flex items-center gap-4 text-xs text-gray-500">
                          <span className="flex items-center gap-1">
                            <Clock className="w-3 h-3" />
                            {formatDate(email.createdAt)}
                          </span>
                          {email.isTranslated && email.originalLanguage && email.translatedLanguage && (
                            <span>{email.originalLanguage} → {email.translatedLanguage}</span>
                          )}
                        </div>
                      </div>
                      <div className="flex gap-2 ml-4">
                        <Button variant="outline" size="sm">
                          <Eye className="w-3 h-3" />
                        </Button>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <div className="text-center py-8">
                <Mail className="w-12 h-12 text-gray-300 mx-auto mb-4" />
                <p className="text-gray-500">보낸 메일이 없습니다.</p>
              </div>
            )}
          </CardContent>
        </Card>
      )}

      {/* Translated Emails */}
      {activeTab === 'translated' && (
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Globe className="w-5 h-5" />
              번역된 메일
            </CardTitle>
          </CardHeader>
          <CardContent>
            {translatedEmailsData?.success && translatedEmailsData.data ? (
              <div className="space-y-4">
                {translatedEmailsData.data.map((email: EmailHistory) => (
                  <div key={email.id} className="border rounded-lg p-4 bg-blue-50">
                    <div className="flex items-start justify-between">
                      <div className="flex-1">
                        <div className="flex items-center gap-2 mb-2">
                          <h3 className="font-semibold text-gray-900">{email.subject}</h3>
                          <Badge className="bg-blue-100 text-blue-800">
                            {email.originalLanguage} → {email.translatedLanguage}
                          </Badge>
                        </div>
                        <p className="text-sm text-gray-600 mb-2">
                          받는 사람: {email.recipientName ? `${email.recipientName} <${email.recipientEmail}>` : email.recipientEmail}
                        </p>
                        <p className="text-sm text-gray-700 line-clamp-3 mb-2">{email.content}</p>
                        <span className="flex items-center gap-1 text-xs text-gray-500">
                          <Clock className="w-3 h-3" />
                          {formatDate(email.createdAt)}
                        </span>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <div className="text-center py-8">
                <Globe className="w-12 h-12 text-gray-300 mx-auto mb-4" />
                <p className="text-gray-500">번역된 메일이 없습니다.</p>
              </div>
            )}
          </CardContent>
        </Card>
      )}

      {/* Statistics */}
      {activeTab === 'stats' && (
        <div className="grid md:grid-cols-3 gap-6">
          <Card>
            <CardHeader className="pb-4">
              <CardTitle className="text-lg flex items-center gap-2">
                <FileText className="w-5 h-5 text-blue-500" />
                총 발송 메일
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="text-3xl font-bold text-blue-600 mb-2">
                {statsData?.data?.totalSentCount || 0}
              </div>
              <p className="text-gray-600 text-sm">전체 발송된 메일 수</p>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="pb-4">
              <CardTitle className="text-lg flex items-center gap-2">
                <Globe className="w-5 h-5 text-green-500" />
                번역 메일
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="text-3xl font-bold text-green-600 mb-2">
                {statsData?.data?.translatedCount || 0}
              </div>
              <p className="text-gray-600 text-sm">AI 번역을 사용한 메일</p>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="pb-4">
              <CardTitle className="text-lg flex items-center gap-2">
                <Mail className="w-5 h-5 text-purple-500" />
                일반 메일
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="text-3xl font-bold text-purple-600 mb-2">
                {statsData?.data?.regularCount || 0}
              </div>
              <p className="text-gray-600 text-sm">번역 없이 발송한 메일</p>
            </CardContent>
          </Card>
        </div>
      )}
    </div>
  )
}