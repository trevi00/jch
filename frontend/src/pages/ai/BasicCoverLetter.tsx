import { useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { FileText, Download, RefreshCw, Copy, Check, Wand2 } from 'lucide-react'
import { aiClient } from '@/services/api'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/Card'
import { Button } from '@/components/ui/Button'
import { Input } from '@/components/ui/Input'
import { Textarea } from '@/components/ui/Textarea'

interface GeneratedSection {
  type: 'introduction' | 'motivation' | 'experience' | 'conclusion'
  title: string
  content: string
}

export default function BasicCoverLetter() {
  const [formData, setFormData] = useState({
    companyName: '',
    jobTitle: '',
    applicantName: '',
    experience: '',
    skills: '',
    motivation: '',
    achievements: '',
  })
  
  const [generatedContent, setGeneratedContent] = useState<GeneratedSection[]>([])
  const [completeLetter, setCompleteLetter] = useState('')
  const [currentSection, setCurrentSection] = useState<string | null>(null)
  const [copiedSection, setCopiedSection] = useState<string | null>(null)
  const [selectedTone, setSelectedTone] = useState<'professional' | 'friendly' | 'passionate'>('professional')

  // 섹션별 생성 mutation
  const generateSectionMutation = useMutation({
    mutationFn: (sectionType: string) => 
      aiClient.generateCoverLetterSection({
        section: sectionType,
        companyName: formData.companyName,
        position: formData.jobTitle,
        applicantInfo: {
          name: formData.applicantName,
          experience: formData.experience,
          skills: formData.skills,
          motivation: formData.motivation,
          achievements: formData.achievements
        },
        tone: selectedTone
      }),
    onSuccess: (response, sectionType) => {
      if (response.success && response.data) {
        const newSection: GeneratedSection = {
          type: sectionType as any,
          title: getSectionTitle(sectionType),
          content: response.data.content
        }
        
        setGeneratedContent(prev => {
          const filtered = prev.filter(s => s.type !== sectionType)
          return [...filtered, newSection].sort((a, b) => 
            getSectionOrder(a.type) - getSectionOrder(b.type)
          )
        })
        setCurrentSection(null)
      }
    },
    onError: (error) => {
      console.error('섹션 생성 실패:', error)
      alert('섹션 생성에 실패했습니다. 다시 시도해주세요.')
    }
  })

  // 전체 자소서 생성 mutation
  const generateCompleteMutation = useMutation({
    mutationFn: () => 
      aiClient.generateCompleteCoverLetter({
        companyName: formData.companyName,
        position: formData.jobTitle,
        applicantInfo: {
          name: formData.applicantName,
          experience: formData.experience,
          skills: formData.skills,
          motivation: formData.motivation,
          achievements: formData.achievements
        },
        tone: selectedTone
      }),
    onSuccess: (response) => {
      if (response.success && response.data) {
        setCompleteLetter(response.data.content)
      }
    },
    onError: (error) => {
      console.error('전체 자소서 생성 실패:', error)
      alert('자소서 생성에 실패했습니다. 다시 시도해주세요.')
    }
  })

  const getSectionTitle = (type: string) => {
    const titles = {
      introduction: '자기소개',
      motivation: '지원동기',
      experience: '경험 및 역량',
      conclusion: '포부 및 다짐'
    }
    return titles[type as keyof typeof titles] || type
  }

  const getSectionOrder = (type: string) => {
    const orders = { introduction: 1, motivation: 2, experience: 3, conclusion: 4 }
    return orders[type as keyof typeof orders] || 5
  }

  const handleInputChange = (field: string, value: string) => {
    setFormData(prev => ({ ...prev, [field]: value }))
  }

  const generateSection = (sectionType: string) => {
    if (!formData.companyName || !formData.jobTitle) {
      alert('회사명과 직무를 입력해주세요.')
      return
    }
    setCurrentSection(sectionType)
    generateSectionMutation.mutate(sectionType)
  }

  const generateComplete = () => {
    if (!formData.companyName || !formData.jobTitle) {
      alert('회사명과 직무를 입력해주세요.')
      return
    }
    generateCompleteMutation.mutate()
  }

  const copyToClipboard = async (content: string, sectionId: string) => {
    try {
      await navigator.clipboard.writeText(content)
      setCopiedSection(sectionId)
      setTimeout(() => setCopiedSection(null), 2000)
    } catch (error) {
      console.error('복사 실패:', error)
    }
  }

  const downloadAsText = (content: string, filename: string) => {
    const blob = new Blob([content], { type: 'text/plain;charset=utf-8' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = filename
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    URL.revokeObjectURL(url)
  }

  const isFormValid = formData.companyName && formData.jobTitle

  return (
    <div className="space-y-8">
      {/* 폼 입력 영역 */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <FileText className="w-5 h-5" />
            기본 정보 입력
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium mb-2">회사명 *</label>
              <Input
                value={formData.companyName}
                onChange={(e) => handleInputChange('companyName', e.target.value)}
                placeholder="지원하는 회사명을 입력하세요"
              />
            </div>
            <div>
              <label className="block text-sm font-medium mb-2">직무/포지션 *</label>
              <Input
                value={formData.jobTitle}
                onChange={(e) => handleInputChange('jobTitle', e.target.value)}
                placeholder="지원하는 직무를 입력하세요"
              />
            </div>
            <div>
              <label className="block text-sm font-medium mb-2">지원자명</label>
              <Input
                value={formData.applicantName}
                onChange={(e) => handleInputChange('applicantName', e.target.value)}
                placeholder="성함을 입력하세요"
              />
            </div>
            <div>
              <label className="block text-sm font-medium mb-2">문체 선택</label>
              <select
                value={selectedTone}
                onChange={(e) => setSelectedTone(e.target.value as any)}
                className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
              >
                <option value="professional">전문적</option>
                <option value="friendly">친근한</option>
                <option value="passionate">열정적</option>
              </select>
            </div>
          </div>
          
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium mb-2">경력/경험</label>
              <Textarea
                value={formData.experience}
                onChange={(e) => handleInputChange('experience', e.target.value)}
                placeholder="관련 경력이나 경험을 입력하세요"
                rows={3}
              />
            </div>
            <div>
              <label className="block text-sm font-medium mb-2">보유 기술/역량</label>
              <Textarea
                value={formData.skills}
                onChange={(e) => handleInputChange('skills', e.target.value)}
                placeholder="보유하고 있는 기술이나 역량을 입력하세요"
                rows={3}
              />
            </div>
            <div>
              <label className="block text-sm font-medium mb-2">지원동기</label>
              <Textarea
                value={formData.motivation}
                onChange={(e) => handleInputChange('motivation', e.target.value)}
                placeholder="해당 회사/직무에 지원하는 이유를 입력하세요"
                rows={3}
              />
            </div>
            <div>
              <label className="block text-sm font-medium mb-2">주요 성과</label>
              <Textarea
                value={formData.achievements}
                onChange={(e) => handleInputChange('achievements', e.target.value)}
                placeholder="주요 성과나 프로젝트 경험을 입력하세요"
                rows={3}
              />
            </div>
          </div>
        </CardContent>
      </Card>

      {/* 생성 버튼 영역 */}
      <div className="space-y-4">
        <h3 className="text-lg font-semibold text-gray-800">섹션별 생성</h3>
        <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
          {[
            { key: 'introduction', label: '자기소개', icon: '👋' },
            { key: 'motivation', label: '지원동기', icon: '🎯' },
            { key: 'experience', label: '경험/역량', icon: '💪' },
            { key: 'conclusion', label: '포부/다짐', icon: '🌟' }
          ].map(section => (
            <Button
              key={section.key}
              onClick={() => generateSection(section.key)}
              disabled={!isFormValid || generateSectionMutation.isPending}
              variant="outline"
              className="h-16 flex flex-col items-center gap-1 hover:bg-blue-50 border-blue-200"
            >
              {currentSection === section.key && generateSectionMutation.isPending ? (
                <RefreshCw className="w-4 h-4 animate-spin" />
              ) : (
                <span className="text-lg">{section.icon}</span>
              )}
              <span className="text-xs font-medium">{section.label}</span>
            </Button>
          ))}
        </div>
        
        <div className="pt-2">
          <Button
            onClick={generateComplete}
            disabled={!isFormValid || generateCompleteMutation.isPending}
            className="w-full bg-blue-600 hover:bg-blue-700 h-12 flex items-center justify-center gap-2"
          >
            {generateCompleteMutation.isPending ? (
              <RefreshCw className="w-4 h-4 animate-spin" />
            ) : (
              <span className="text-lg">📝</span>
            )}
            <span className="font-medium">전체 자소서 생성</span>
          </Button>
        </div>
      </div>

      {/* 섹션별 결과 */}
      {generatedContent.length > 0 && (
        <div className="space-y-4">
          <h3 className="text-lg font-semibold">생성된 섹션</h3>
          {generatedContent.map((section) => (
            <Card key={section.type}>
              <CardHeader className="flex flex-row items-center justify-between">
                <CardTitle className="text-base">{section.title}</CardTitle>
                <div className="flex gap-2">
                  <Button
                    onClick={() => copyToClipboard(section.content, section.type)}
                    variant="ghost"
                    size="sm"
                  >
                    {copiedSection === section.type ? (
                      <Check className="w-4 h-4 text-green-600" />
                    ) : (
                      <Copy className="w-4 h-4" />
                    )}
                  </Button>
                  <Button
                    onClick={() => downloadAsText(section.content, `${section.title}.txt`)}
                    variant="ghost"
                    size="sm"
                  >
                    <Download className="w-4 h-4" />
                  </Button>
                </div>
              </CardHeader>
              <CardContent>
                <div className="whitespace-pre-wrap text-sm leading-relaxed">
                  {section.content}
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      {/* 전체 자소서 결과 */}
      {completeLetter && (
        <Card>
          <CardHeader className="flex flex-row items-center justify-between">
            <CardTitle>완성된 자기소개서</CardTitle>
            <div className="flex gap-2">
              <Button
                onClick={() => copyToClipboard(completeLetter, 'complete')}
                variant="ghost"
                size="sm"
              >
                {copiedSection === 'complete' ? (
                  <Check className="w-4 h-4 text-green-600" />
                ) : (
                  <Copy className="w-4 h-4" />
                )}
              </Button>
              <Button
                onClick={() => downloadAsText(completeLetter, '자기소개서.txt')}
                variant="ghost"
                size="sm"
              >
                <Download className="w-4 h-4" />
              </Button>
            </div>
          </CardHeader>
          <CardContent>
            <div className="whitespace-pre-wrap text-sm leading-relaxed border rounded-lg p-4 bg-gray-50">
              {completeLetter}
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  )
}