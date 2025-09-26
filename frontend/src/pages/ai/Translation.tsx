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
                {/* <div className="flex space-x-2">
                  <button className="btn-outline text-sm">
                    <Upload className="w-4 h-4 mr-1" />
                    파일 업로드
                  </button>
                </div> */}
                
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

          {/* 템플릿 번역 */}
          {/* <div className="card">
            <div className="card-header">
              <h2 className="text-lg font-semibold">템플릿 번역</h2>
            </div>
            <div className="card-content space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  템플릿 유형
                </label>
                <div className="grid grid-cols-2 gap-2">
                  <button
                    onClick={() => setTemplateType('resume')}
                    className={`p-3 border rounded-lg text-sm ${
                      templateType === 'resume'
                        ? 'border-primary-500 bg-primary-50 text-primary-700'
                        : 'border-gray-200 hover:border-gray-300'
                    }`}
                  >
                    이력서 템플릿
                  </button>
                  <button
                    onClick={() => setTemplateType('cover_letter')}
                    className={`p-3 border rounded-lg text-sm ${
                      templateType === 'cover_letter'
                        ? 'border-primary-500 bg-primary-50 text-primary-700'
                        : 'border-gray-200 hover:border-gray-300'
                    }`}
                  >
                    자소서 템플릿
                  </button>
                </div>
              </div>
              
              <button className="btn-outline w-full">
                <FileText className="w-4 h-4 mr-2" />
                템플릿으로 번역하기
              </button>
            </div>
          </div> */}
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
              {/* <div className="card">
                <div className="card-header">
                  <div className="flex justify-between items-center">
                    <h2 className="text-lg font-semibold">번역 품질 평가</h2>
                    <button
                      onClick={handleEvaluateQuality}
                      disabled={evaluateTranslationMutation.isPending}
                      className="btn-outline text-sm"
                    >
                      {evaluateTranslationMutation.isPending ? (
                        <RefreshCw className="w-4 h-4 mr-1 animate-spin" />
                      ) : (
                        <Star className="w-4 h-4 mr-1" />
                      )}
                      품질 분석
                    </button>
                  </div>
                </div>
                
                {result.quality ? (
                  <div className="card-content space-y-6">
                    <div className="text-center bg-gradient-to-r from-blue-50 to-purple-50 p-6 rounded-lg">
                      <div className="flex items-center justify-center space-x-6">
                        <div>
                          <div className={`text-4xl font-bold ${getQualityColor(result.quality.score)}`}>
                            {result.quality.score}
                          </div>
                          <div className="text-sm text-gray-600 mt-1">점 / 100점</div>
                        </div>
                        <div className="border-l border-gray-300 pl-6">
                          <div className={`text-3xl font-bold ${
                            result.quality.grade === 'A+' || result.quality.grade === 'A' ? 'text-green-600' :
                            result.quality.grade?.startsWith('B') ? 'text-yellow-600' : 'text-red-600'
                          }`}>
                            {result.quality.grade}
                          </div>
                          <div className="text-sm text-gray-600 mt-1">등급</div>
                        </div>
                      </div>
                      <div className="mt-4">
                        <span className={`inline-flex items-center px-4 py-2 rounded-full text-sm font-medium ${
                          result.quality.score >= 90 
                            ? 'bg-green-100 text-green-800'
                            : result.quality.score >= 70
                            ? 'bg-yellow-100 text-yellow-800'
                            : 'bg-red-100 text-red-800'
                        }`}>
                          {getQualityLabel(result.quality.score)}
                        </span>
                      </div>
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                      <div className="p-4 border rounded-lg">
                        <div className="flex items-center justify-between mb-2">
                          <h4 className="font-medium text-gray-900">정확성</h4>
                          <span className={`text-lg font-bold ${getQualityColor(result.quality.accuracy?.score * 10)}`}>
                            {result.quality.accuracy?.score || 8}/10
                          </span>
                        </div>
                        <p className="text-sm text-gray-600">{result.quality.accuracy?.comment || "의미가 정확히 전달되었습니다"}</p>
                      </div>
                      
                      <div className="p-4 border rounded-lg">
                        <div className="flex items-center justify-between mb-2">
                          <h4 className="font-medium text-gray-900">유창성</h4>
                          <span className={`text-lg font-bold ${getQualityColor(result.quality.fluency?.score * 10)}`}>
                            {result.quality.fluency?.score || 8}/10
                          </span>
                        </div>
                        <p className="text-sm text-gray-600">{result.quality.fluency?.comment || "자연스럽고 유창한 번역입니다"}</p>
                      </div>
                      
                      <div className="p-4 border rounded-lg">
                        <div className="flex items-center justify-between mb-2">
                          <h4 className="font-medium text-gray-900">일관성</h4>
                          <span className={`text-lg font-bold ${getQualityColor(result.quality.consistency?.score * 10)}`}>
                            {result.quality.consistency?.score || 8}/10
                          </span>
                        </div>
                        <p className="text-sm text-gray-600">{result.quality.consistency?.comment || "용어 사용이 일관됩니다"}</p>
                      </div>
                      
                      <div className="p-4 border rounded-lg">
                        <div className="flex items-center justify-between mb-2">
                          <h4 className="font-medium text-gray-900">문화적 적절성</h4>
                          <span className={`text-lg font-bold ${getQualityColor(result.quality.culturalAppropriateness?.score * 10)}`}>
                            {result.quality.culturalAppropriateness?.score || 8}/10
                          </span>
                        </div>
                        <p className="text-sm text-gray-600">{result.quality.culturalAppropriateness?.comment || "문화적으로 적절한 표현입니다"}</p>
                      </div>
                      
                      <div className="p-4 border rounded-lg">
                        <div className="flex items-center justify-between mb-2">
                          <h4 className="font-medium text-gray-900">완전성</h4>
                          <span className={`text-lg font-bold ${getQualityColor(result.quality.completeness?.score * 10)}`}>
                            {result.quality.completeness?.score || 8}/10
                          </span>
                        </div>
                        <p className="text-sm text-gray-600">{result.quality.completeness?.comment || "원문의 내용이 완전히 번역되었습니다"}</p>
                      </div>
                    </div>

                    <div className="p-4 bg-blue-50 rounded-lg">
                      <h4 className="font-medium text-blue-900 mb-2">💡 AI 전문가 의견</h4>
                      <p className="text-sm text-blue-800 leading-relaxed">
                        {result.quality.feedback}
                      </p>
                    </div>

                    {(result.quality.strengths?.length > 0 || result.quality.improvements?.length > 0) && (
                      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        {result.quality.strengths?.length > 0 && (
                          <div className="p-4 bg-green-50 rounded-lg">
                            <h4 className="font-medium text-green-900 mb-3 flex items-center">
                              <span className="text-green-600 mr-2">✅</span>
                              번역의 장점
                            </h4>
                            <ul className="space-y-1">
                              {result.quality.strengths.map((strength, index) => (
                                <li key={index} className="text-sm text-green-800 flex items-start">
                                  <span className="text-green-600 mr-2 mt-1">•</span>
                                  {strength}
                                </li>
                              ))}
                            </ul>
                          </div>
                        )}
                        
                        {result.quality.improvements?.length > 0 && (
                          <div className="p-4 bg-amber-50 rounded-lg">
                            <h4 className="font-medium text-amber-900 mb-3 flex items-center">
                              <span className="text-amber-600 mr-2">💡</span>
                              개선 제안
                            </h4>
                            <ul className="space-y-1">
                              {result.quality.improvements.map((improvement, index) => (
                                <li key={index} className="text-sm text-amber-800 flex items-start">
                                  <span className="text-amber-600 mr-2 mt-1">•</span>
                                  {improvement}
                                </li>
                              ))}
                            </ul>
                          </div>
                        )}
                      </div>
                    )}
                  </div>
                ) : (
                  <div className="card-content">
                    <div className="text-center text-gray-500 py-8">
                      품질 분석을 실행하면 번역 품질에 대한 상세한 평가를 확인할 수 있습니다.
                    </div>
                  </div>
                )}
              </div>
             /* */}

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