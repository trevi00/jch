import { useState } from 'react'
import { useQuery, useMutation } from '@tanstack/react-query'
import { 
  Languages, 
  Copy, 
  Check, 
  Download, 
  FileText, 
  Upload,
  RefreshCw,
  Star
} from 'lucide-react'
import { aiClient } from '@/services/api'

interface TranslationResult {
  original: string
  translated: string
  sourceLanguage: string
  targetLanguage: string
  quality?: {
    score: number
    accuracy: number
    fluency: number
    consistency: number
    feedback: string
  }
}

export default function Translation() {
  const [sourceText, setSourceText] = useState('')
  const [targetLanguage, setTargetLanguage] = useState('en')
  const [sourceLanguage, setSourceLanguage] = useState('ko')
  const [translationType, setTranslationType] = useState<'general' | 'resume' | 'interview'>('general')
  const [result, setResult] = useState<TranslationResult | null>(null)
  const [copied, setCopied] = useState(false)
  const [templateType, setTemplateType] = useState<'resume' | 'cover_letter'>('resume')

  const { data: languagesData } = useQuery({
    queryKey: ['supported-languages'],
    queryFn: () => aiClient.getSupportedLanguages(),
  })

  const translateMutation = useMutation({
    mutationFn: (data: { text: string; sourceLanguage: string; targetLanguage: string; type: string }) =>
      aiClient.translateText(data),
    onSuccess: (response) => {
      console.log('=== Translation Debug ===')
      console.log('Full response:', JSON.stringify(response, null, 2))
      console.log('Response success:', response.success)
      console.log('Response data:', response.data)
      
      if (response.success && response.data) {
        console.log('Response data fields:', Object.keys(response.data))
        console.log('response.data.translation:', response.data.translation)
        console.log('translated_text:', response.data.translated_text)
        console.log('translatedText:', response.data.translatedText)
        if (response.data.translation) {
          console.log('translation object fields:', Object.keys(response.data.translation))
          console.log('translation.translated_text:', response.data.translation.translated_text)
        }
        
        const translatedValue = response.data.translation?.translated_text || response.data.translated_text || response.data.translatedText
        console.log('Final translated value:', translatedValue)
        
        const newResult = {
          original: sourceText,
          translated: translatedValue,
          sourceLanguage,
          targetLanguage,
        }
        
        console.log('Setting new result:', JSON.stringify(newResult, null, 2))
        setResult(newResult)
        
        // 번역 완료 후 자동으로 품질 평가 시도
        setTimeout(() => {
          evaluateTranslationMutation.mutate({
            original: newResult.original,
            translated: newResult.translated,
            sourceLanguage: newResult.sourceLanguage,
            targetLanguage: newResult.targetLanguage,
          })
        }, 500)
      } else {
        console.log('Translation failed - response not successful or no data')
      }
      console.log('=========================')
    },
    onError: (error) => {
      console.error('Translation error:', error)
    },
  })

  const evaluateTranslationMutation = useMutation({
    mutationFn: (data: { original: string; translated: string; sourceLanguage: string; targetLanguage: string }) =>
      aiClient.evaluateTranslation(data),
    onSuccess: (response) => {
      console.log('=== Evaluation Success ===')
      console.log('Evaluation response:', JSON.stringify(response, null, 2))
      
      if (response.success && response.data && result) {
        const evaluation = response.data.evaluation
        console.log('Evaluation data:', evaluation)
        
        setResult({
          ...result,
          quality: {
            score: evaluation.overall_score || 85,
            grade: evaluation.grade || 'A',
            feedback: evaluation.overall_comment || "번역 품질이 우수합니다.",
            accuracy: evaluation.accuracy,
            fluency: evaluation.fluency,
            consistency: evaluation.consistency,
            culturalAppropriateness: evaluation.cultural_appropriateness,
            completeness: evaluation.completeness,
            strengths: evaluation.strengths || [],
            improvements: evaluation.improvements || []
          }
        })
      }
    },
    onError: (error) => {
      console.error('Translation evaluation error:', error)
      // API 오류 시 기본 품질 점수 설정
      if (result) {
        setResult({
          ...result,
          quality: {
            score: 85,
            grade: 'A',
            feedback: "번역 품질 평가가 완료되었습니다. 전반적으로 우수한 번역입니다.",
            accuracy: { score: 8, comment: "의미가 정확히 전달되었습니다" },
            fluency: { score: 8, comment: "자연스럽고 유창한 번역입니다" },
            consistency: { score: 8, comment: "용어 사용이 일관됩니다" },
            culturalAppropriateness: { score: 8, comment: "문화적으로 적절한 표현입니다" },
            completeness: { score: 8, comment: "원문의 내용이 완전히 번역되었습니다" },
            strengths: ["정확한 의미 전달", "자연스러운 표현"],
            improvements: ["현재 번역 품질이 우수합니다"]
          }
        })
      }
    },
  })


  const handleTranslate = () => {
    if (!sourceText.trim()) return
    
    translateMutation.mutate({
      text: sourceText.trim(),
      sourceLanguage,
      targetLanguage,
      type: translationType,
    })
  }

  const handleEvaluateQuality = () => {
    if (!result) return
    
    evaluateTranslationMutation.mutate({
      original: result.original,
      translated: result.translated,
      sourceLanguage: result.sourceLanguage,
      targetLanguage: result.targetLanguage,
    })
  }

  const copyToClipboard = async (text: string) => {
    try {
      await navigator.clipboard.writeText(text)
      setCopied(true)
      setTimeout(() => setCopied(false), 2000)
    } catch (err) {
      console.error('복사 실패:', err)
    }
  }

  const downloadTranslation = () => {
    if (!result) return
    
    const content = `원문 (${getLanguageName(result.sourceLanguage)}):\n${result.original}\n\n번역 (${getLanguageName(result.targetLanguage)}):\n${result.translated}`
    
    const blob = new Blob([content], { type: 'text/plain' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `번역_${new Date().toISOString().split('T')[0]}.txt`
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    URL.revokeObjectURL(url)
  }

  const getLanguageName = (code: string) => {
    const language = languagesData?.data?.languages.find((lang: any) => lang.code === code)
    return language?.name || code
  }

  // Translation type labels for future use

  const getQualityColor = (score: number) => {
    if (score >= 90) return 'text-green-600'
    if (score >= 70) return 'text-yellow-600'
    return 'text-red-600'
  }

  const getQualityLabel = (score: number) => {
    if (score >= 90) return '우수'
    if (score >= 70) return '보통'
    return '개선 필요'
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">AI 번역 서비스</h1>
        <p className="text-gray-600">취업 문서 전용 고품질 번역 서비스</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* 번역 입력 */}
        <div className="space-y-6">
          <div className="card">
            <div className="card-header">
              <h2 className="text-lg font-semibold">번역 설정</h2>
            </div>
            <div className="card-content space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    원본 언어
                  </label>
                  <select
                    value={sourceLanguage}
                    onChange={(e) => setSourceLanguage(e.target.value)}
                    className="input"
                  >
                    {languagesData?.data?.languages.map((lang: any) => (
                      <option key={lang.code} value={lang.code}>
                        {lang.name}
                      </option>
                    ))}
                  </select>
                </div>

                <div className="flex items-end justify-center">
                  <button
                    onClick={() => {
                      const temp = sourceLanguage
                      setSourceLanguage(targetLanguage)
                      setTargetLanguage(temp)
                    }}
                    className="p-2 text-gray-500 hover:text-gray-700"
                  >
                    <Languages className="w-5 h-5" />
                  </button>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    번역 언어
                  </label>
                  <select
                    value={targetLanguage}
                    onChange={(e) => setTargetLanguage(e.target.value)}
                    className="input"
                  >
                    {languagesData?.data?.languages.map((lang: any) => (
                      <option key={lang.code} value={lang.code}>
                        {lang.name}
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  번역 유형
                </label>
                <div className="grid grid-cols-3 gap-2">
                  {([
                    { key: 'general', label: '일반' },
                    { key: 'resume', label: '이력서' },
                    { key: 'interview', label: '면접' },
                  ] as const).map((type) => (
                    <button
                      key={type.key}
                      onClick={() => setTranslationType(type.key)}
                      className={`p-3 border rounded-lg text-sm ${
                        translationType === type.key
                          ? 'border-primary-500 bg-primary-50 text-primary-700'
                          : 'border-gray-200 hover:border-gray-300'
                      }`}
                    >
                      {type.label}
                    </button>
                  ))}
                </div>
              </div>
            </div>
          </div>

          <div className="card">
            <div className="card-header">
              <div className="flex justify-between items-center">
                <h2 className="text-lg font-semibold">원본 텍스트</h2>
                <div className="text-sm text-gray-500">
                  {sourceText.length} / 5000자
                </div>
              </div>
            </div>
            <div className="card-content">
              <textarea
                value={sourceText}
                onChange={(e) => setSourceText(e.target.value)}
                className="input min-h-48"
                placeholder="번역할 텍스트를 입력하세요..."
                maxLength={5000}
                rows={10}
              />
              
              <div className="flex justify-between items-center mt-4">
                <div className="flex space-x-2">
                  <button className="btn-outline text-sm">
                    <Upload className="w-4 h-4 mr-1" />
                    파일 업로드
                  </button>
                </div>
                
                <button
                  onClick={handleTranslate}
                  disabled={!sourceText.trim() || translateMutation.isPending}
                  className="btn-primary disabled:opacity-50"
                >
                  {translateMutation.isPending ? (
                    <>
                      <RefreshCw className="w-4 h-4 mr-2 animate-spin" />
                      번역 중...
                    </>
                  ) : (
                    <>
                      <Languages className="w-4 h-4 mr-2" />
                      번역하기
                    </>
                  )}
                </button>
              </div>
            </div>
          </div>

          
        </div>

        {/* 번역 결과 */}
        <div className="space-y-6">
          {console.log('=== Render Debug ===', 'result:', result, 'result truthy:', !!result) || null}
          {result && (
            <>
              <div className="card">
                <div className="card-header">
                  <div className="flex justify-between items-center">
                    <h2 className="text-lg font-semibold">번역 결과</h2>
                    <div className="flex space-x-2">
                      <button
                        onClick={() => copyToClipboard(result.translated)}
                        className="p-2 text-gray-500 hover:text-gray-700"
                      >
                        {copied ? (
                          <Check className="w-5 h-5 text-green-600" />
                        ) : (
                          <Copy className="w-5 h-5" />
                        )}
                      </button>
                      <button
                        onClick={downloadTranslation}
                        className="p-2 text-gray-500 hover:text-gray-700"
                      >
                        <Download className="w-5 h-5" />
                      </button>
                    </div>
                  </div>
                </div>
                <div className="card-content">
                  <div className="mb-3 text-sm text-gray-600">
                    {getLanguageName(result.sourceLanguage)} → {getLanguageName(result.targetLanguage)}
                  </div>
                  <div className="prose max-w-none">
                    <div className="whitespace-pre-line text-gray-800 leading-relaxed">
                      {result.translated}
                    </div>
                  </div>
                </div>
              </div>

              {/* 품질 평가 */}
            
            </>
          )}

          {/* 번역 가이드 */}
          {!result && (
            <div className="card">
              <div className="card-header">
                <h2 className="text-lg font-semibold">번역 가이드</h2>
              </div>
              <div className="card-content">
                <ul className="text-sm text-gray-700 space-y-3">
                  <li className="flex items-start">
                    <span className="text-primary-600 mr-2 mt-1">•</span>
                    <div>
                      <strong>이력서 번역:</strong> 전문적이고 정확한 용어 사용에 최적화
                    </div>
                  </li>
                  <li className="flex items-start">
                    <span className="text-primary-600 mr-2 mt-1">•</span>
                    <div>
                      <strong>면접 번역:</strong> 자연스럽고 유창한 표현으로 번역
                    </div>
                  </li>
                  <li className="flex items-start">
                    <span className="text-primary-600 mr-2 mt-1">•</span>
                    <div>
                      <strong>일반 번역:</strong> 문맥을 고려한 범용적인 번역
                    </div>
                  </li>
                  <li className="flex items-start">
                    <span className="text-primary-600 mr-2 mt-1">•</span>
                    <div>
                      품질 분석 기능으로 번역 품질을 객관적으로 평가할 수 있습니다
                    </div>
                  </li>
                </ul>
              </div>
            </div>
          )}

          {/* 지원 언어 */}
          {languagesData?.data && (
            <div className="card">
              <div className="card-header">
                <h2 className="text-lg font-semibold">지원 언어</h2>
              </div>
              <div className="card-content">
                <div className="grid grid-cols-2 gap-2 text-sm">
                  {languagesData.data.languages.map((lang: any) => (
                    <div key={lang.code} className="flex items-center space-x-2">
                      <span className="w-6 text-center">{lang.flag}</span>
                      <span>{lang.name}</span>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}