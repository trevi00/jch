import { useState } from 'react'
import { FileText, Brain, Zap, ArrowLeft } from 'lucide-react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/Card'
import { Button } from '@/components/ui/Button'
import BasicCoverLetter from './BasicCoverLetter'
import InteractiveCoverLetter from './InteractiveCoverLetter'

type CoverLetterMode = 'selection' | 'basic' | 'interactive'

export default function CoverLetter() {
  const [mode, setMode] = useState<CoverLetterMode>('selection')

  // 모드 선택 화면
  if (mode === 'selection') {
    return (
      <div className="space-y-8">
        <div className="text-center space-y-4">
          <h1 className="text-3xl font-bold">자기소개서 생성</h1>
          <p className="text-gray-600">원하는 방식을 선택하여 맞춤형 자기소개서를 생성해보세요</p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-1 gap-6 max-w-4xl mx-auto">
          {/* 기본 생성 모드 */}
          {/* <Card className="cursor-pointer hover:shadow-lg transition-all duration-200 hover:scale-105">
            <CardHeader className="text-center">
              <div className="mx-auto w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center mb-4">
                <FileText className="w-8 h-8 text-blue-600" />
              </div>
              <CardTitle className="text-xl">기본 생성</CardTitle>
              <p className="text-sm text-gray-600">
                간단한 정보 입력으로 빠르게 자소서 생성
              </p>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2 text-sm">
                <div className="flex items-center gap-2">
                  <div className="w-2 h-2 bg-green-500 rounded-full"></div>
                  <span>빠른 생성 (3-5분)</span>
                </div>
                <div className="flex items-center gap-2">
                  <div className="w-2 h-2 bg-green-500 rounded-full"></div>
                  <span>섹션별 개별 생성 가능</span>
                </div>
                <div className="flex items-center gap-2">
                  <div className="w-2 h-2 bg-green-500 rounded-full"></div>
                  <span>전체 자소서 한번에 생성</span>
                </div>
                <div className="flex items-center gap-2">
                  <div className="w-2 h-2 bg-green-500 rounded-full"></div>
                  <span>문체 선택 가능</span>
                </div>
              </div>
              <Button 
                onClick={() => setMode('basic')}
                className="w-full bg-blue-600 hover:bg-blue-700"
              >
                <Zap className="w-4 h-4 mr-2" />
                기본 생성 시작
              </Button>
            </CardContent>
          </Card> */}

          {/* 인터랙티브 생성 모드 */}
          <Card className="cursor-pointer hover:shadow-lg transition-all duration-200 hover:scale-105">
            <CardHeader className="text-center">
              <div className="mx-auto w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center mb-4">
                <Brain className="w-8 h-8 text-blue-600" />
              </div>
              <CardTitle className="text-xl">AI 자기소개서 생성</CardTitle>
              <p className="text-sm text-gray-600">
                AI와 대화하며 맞춤형 자소서 생성
              </p>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2 text-sm">
                <div className="flex items-center gap-2">
                  <div className="w-2 h-2 bg-blue-500 rounded-full"></div>
                  <span>단계별 질문 응답</span>
                </div>
                <div className="flex items-center gap-2">
                  <div className="w-2 h-2 bg-blue-500 rounded-full"></div>
                  <span>개인화된 맞춤 생성</span>
                </div>
                <div className="flex items-center gap-2">
                  <div className="w-2 h-2 bg-blue-500 rounded-full"></div>
                  <span>섹션별 전문 분석</span>
                </div>
                <div className="flex items-center gap-2">
                  <div className="w-2 h-2 bg-blue-500 rounded-full"></div>
                  <span>고품질 결과물</span>
                </div>
              </div>
              <Button 
                onClick={() => setMode('interactive')}
                className="w-full bg-blue-600 hover:bg-purple-700"
              >
                자기소개서 생성 시작
              </Button>
            </CardContent>
          </Card>
        </div>

        {/* 비교 표 */}
        <Card className="max-w-4xl mx-auto">
          <CardHeader>
            <CardTitle className="text-center">생성 방식 비교</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="overflow-x-auto">
              <table className="w-full border-collapse">
                <thead>
                  <tr className="border-b">
                    <th className="text-left p-2">구분</th>
                    <th className="text-center p-2 text-blue-600">자기소개서 생성</th>
                  </tr>
                </thead>
                <tbody className="text-sm">
                  <tr className="border-b">
                    <td className="p-3 font-medium">소요 시간</td>
                    <td className="p-3 text-center">10-15분</td>
                  </tr>
                  <tr className="border-b">
                    <td className="p-3 font-medium">맞춤화 정도</td>
                    <td className="p-3 text-center">높음</td>
                  </tr>
                  <tr className="border-b">
                    <td className="p-3 font-medium">사용 편의성</td>
                    <td className="p-3 text-center">쉬움</td>
                  </tr>
                  <tr className="border-b">
                    <td className="p-3 font-medium">결과물 품질</td>
                    <td className="p-3 text-center">매우 좋음</td>
                  </tr>
                  <tr>
                    <td className="p-3 font-medium">추천 대상</td>
                    <td className="p-3 text-center">고품질 자소서 필요</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </CardContent>
        </Card>
      </div>
    )
  }

  // 기본 생성 모드
  if (mode === 'basic') {
    return (
      <div className="space-y-6">
        <div className="flex items-center gap-4">
          <Button
            onClick={() => setMode('selection')}
            variant="ghost"
            size="sm"
          >
            <ArrowLeft className="w-4 h-4 mr-2" />
            모드 선택으로 돌아가기
          </Button>
          <div>
            <h1 className="text-2xl font-bold">기본 자소서 생성</h1>
            <p className="text-gray-600">정보를 입력하고 원하는 섹션을 생성해보세요</p>
          </div>
        </div>
        <BasicCoverLetter />
      </div>
    )
  }

  // 인터랙티브 생성 모드
  if (mode === 'interactive') {
    return (
      <div className="space-y-6">
        <div className="flex items-center gap-4">
          <Button
            onClick={() => setMode('selection')}
            variant="ghost"
            size="sm"
          >
            <ArrowLeft className="w-4 h-4 mr-2" />
            모드 선택으로 돌아가기
          </Button>
          <div>
            <h1 className="text-2xl font-bold">AI 자기소서개 생성</h1>
            <p className="text-gray-600">AI와 대화하며 맞춤형 자기소개서를 만들어보세요</p>
          </div>
        </div>
        <InteractiveCoverLetter />
      </div>
    )
  }

  return null
}