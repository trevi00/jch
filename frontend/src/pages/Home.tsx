import { Link } from 'react-router-dom'
import { ArrowRight, CheckCircle, Users, Briefcase, Brain, Star, Award, Zap } from 'lucide-react'
import { Button } from '@/components/ui/Button'
import { Card, CardContent, CardTitle } from '@/components/ui/Card'
import { Badge } from '@/components/ui/Badge'

/**
 * 홈 페이지 컴포넌트
 * 서비스 소개, 주요 기능, 통계, CTA 섹션을 포함한 랜딩 페이지
 * 이벤트: 페이지 렌더링 이벤트, 네비게이션 클릭 이벤트, 회원가입/로그인 리다이렉트 이벤트
 */
export default function Home() {
  return (
    <div className="min-h-screen bg-gradient-to-br from-primary-50 to-blue-100">
      {/* Hero Section */}
      <div className="px-6 py-12">
        <div className="max-w-7xl mx-auto">
          <div className="text-center">
            <h1 className="text-4xl md:text-6xl font-bold text-gray-900 mb-6">
              <span className="text-primary-600">JOB았다</span>
              <br />
              취업 지원 솔루션
            </h1>
            <p className="text-xl text-gray-600 mb-8 max-w-3xl mx-auto">
              종합 취업 지원 플랫폼
              <br />
              AI 기반 면접 연습, 자소서 작성부터 채용공고까지 한 번에
            </p>
            <div className="flex flex-col sm:flex-row gap-4 justify-center items-center">
              <Button size="lg" className="text-lg px-8 shadow-lg hover:shadow-xl transition-all duration-300" asChild>
                <Link to="/register">
                  시작하기
                </Link>
              </Button>
              <Button variant="outline" size="lg" className="text-lg px-8 border-2 hover:bg-primary-50" asChild>
                <Link to="/login">
                  로그인
                </Link>
              </Button>
            </div>
          </div>
        </div>
      </div>

      {/* Features Section */}
      <section className="py-24 bg-white">
        <div className="max-w-7xl mx-auto px-6">
          <div className="text-center mb-16">
            <Badge variant="info" size="lg" className="mb-4">
              핵심 서비스
            </Badge>
            <h2 className="text-4xl font-bold text-gray-900 mb-4">
              주요 기능
            </h2>
            <p className="text-xl text-gray-600 max-w-2xl mx-auto">
              AI 기술과 데이터를 활용한 개인 맞춤형 취업 지원 서비스
            </p>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            <Card className="text-center group hover:shadow-xl transition-all duration-300 border-0 shadow-lg" hover="lift">
              <CardContent className="p-8">
                <div className="w-20 h-20 bg-gradient-to-r from-blue-500 to-purple-600 rounded-full flex items-center justify-center mx-auto mb-6 group-hover:scale-110 transition-transform duration-300">
                  <Brain className="w-10 h-10 text-white" />
                </div>
                <CardTitle className="text-2xl mb-4 text-gray-900">
                  AI 면접 연습
                </CardTitle>
                <p className="text-gray-600 leading-relaxed">
                  실시간 AI 피드백으로 면접 실력을 향상시키고 자신감을 기르세요
                </p>
                <div className="mt-6">
                  <Badge variant="success" size="sm">
                    <Star className="w-3 h-3 mr-1" />
                    인기 기능
                  </Badge>
                </div>
              </CardContent>
            </Card>
            
            <Card className="text-center group hover:shadow-xl transition-all duration-300 border-0 shadow-lg" hover="lift">
              <CardContent className="p-8">
                <div className="w-20 h-20 bg-gradient-to-r from-green-500 to-teal-600 rounded-full flex items-center justify-center mx-auto mb-6 group-hover:scale-110 transition-transform duration-300">
                  <Briefcase className="w-10 h-10 text-white" />
                </div>
                <CardTitle className="text-2xl mb-4 text-gray-900">
                  맞춤 채용공고
                </CardTitle>
                <p className="text-gray-600 leading-relaxed">
                  당신의 스킬과 경험에 맞는 최적의 채용공고를 추천해드립니다
                </p>
                <div className="mt-6">
                  <Badge variant="warning" size="sm">
                    <Zap className="w-3 h-3 mr-1" />
                    AI 추천
                  </Badge>
                </div>
              </CardContent>
            </Card>
            
            <Card className="text-center group hover:shadow-xl transition-all duration-300 border-0 shadow-lg" hover="lift">
              <CardContent className="p-8">
                <div className="w-20 h-20 bg-gradient-to-r from-orange-500 to-red-500 rounded-full flex items-center justify-center mx-auto mb-6 group-hover:scale-110 transition-transform duration-300">
                  <Users className="w-10 h-10 text-white" />
                </div>
                <CardTitle className="text-2xl mb-4 text-gray-900">
                  커뮤니티
                </CardTitle>
                <p className="text-gray-600 leading-relaxed">
                  동료 교육생들과 정보를 공유하고 함께 성장해나가세요
                </p>
                <div className="mt-6">
                  <Badge variant="info" size="sm">
                    <Award className="w-3 h-3 mr-1" />
                    활발한 소통
                  </Badge>
                </div>
              </CardContent>
            </Card>
          </div>
        </div>
      </section>

      {/* Statistics Section */}
      <section className="py-20 bg-gradient-to-r from-gray-50 to-blue-50">
        <div className="max-w-7xl mx-auto px-6">
          <div className="text-center mb-12">
            <h2 className="text-3xl font-bold text-gray-900 mb-4">
              신뢰할 수 있는 수치
            </h2>
            <p className="text-gray-600">
              실제 데이터로 증명하는 잡았다의 성과
            </p>
          </div>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-8">
            <Card className="text-center hover:shadow-lg transition-shadow duration-300 bg-white/80 backdrop-blur-sm border-0">
              <CardContent className="p-6">
                <div className="text-4xl font-bold text-primary-600 mb-2 bg-gradient-to-r from-primary-600 to-blue-700 bg-clip-text text-transparent">
                  1,200+
                </div>
                <div className="text-gray-700 font-medium">등록 기업</div>
                <div className="text-sm text-gray-500 mt-1">협력 파트너</div>
              </CardContent>
            </Card>
            
            <Card className="text-center hover:shadow-lg transition-shadow duration-300 bg-white/80 backdrop-blur-sm border-0">
              <CardContent className="p-6">
                <div className="text-4xl font-bold text-green-600 mb-2 bg-gradient-to-r from-green-600 to-emerald-700 bg-clip-text text-transparent">
                  5,000+
                </div>
                <div className="text-gray-700 font-medium">활성 사용자</div>
                <div className="text-sm text-gray-500 mt-1">월간 이용자</div>
              </CardContent>
            </Card>
            
            <Card className="text-center hover:shadow-lg transition-shadow duration-300 bg-white/80 backdrop-blur-sm border-0">
              <CardContent className="p-6">
                <div className="text-4xl font-bold text-orange-600 mb-2 bg-gradient-to-r from-orange-600 to-red-600 bg-clip-text text-transparent">
                  85%
                </div>
                <div className="text-gray-700 font-medium">취업 성공률</div>
                <div className="text-sm text-gray-500 mt-1">6개월 내</div>
              </CardContent>
            </Card>
            
            <Card className="text-center hover:shadow-lg transition-shadow duration-300 bg-white/80 backdrop-blur-sm border-0">
              <CardContent className="p-6">
                <div className="text-4xl font-bold text-purple-600 mb-2 bg-gradient-to-r from-purple-600 to-indigo-700 bg-clip-text text-transparent">
                  3,500+
                </div>
                <div className="text-gray-700 font-medium">취업 성공</div>
                <div className="text-sm text-gray-500 mt-1">누적 사례</div>
              </CardContent>
            </Card>
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="py-24 bg-gradient-to-r from-primary-600 to-blue-700 relative overflow-hidden">
        <div className="absolute inset-0 bg-black/10"></div>
        <div className="max-w-4xl mx-auto text-center px-6 relative">
          <div className="mb-8">
            <Badge variant="outline" className="bg-white/20 text-white border-white/30 mb-4">
              무료 서비스
            </Badge>
            <h2 className="text-4xl md:text-5xl font-bold text-white mb-6">
              지금 바로 취업 준비를 시작하세요
            </h2>
            <p className="text-xl text-primary-100 mb-8 max-w-2xl mx-auto leading-relaxed">
              무료로 회원가입하고 AI 기반 개인 맞춤형 취업 지원 서비스를 경험해보세요
            </p>
          </div>
          
          <div className="flex flex-col sm:flex-row gap-4 justify-center items-center">
            <Button 
              size="xl" 
              className="bg-white text-primary-600 hover:bg-gray-100 shadow-xl hover:shadow-2xl transition-all duration-300 text-lg px-10"
              asChild
            >
              <Link to="/register">
                무료 회원가입
                <ArrowRight className="ml-2 w-5 h-5" />
              </Link>
            </Button>
            
            <Button 
              variant="outline" 
              size="xl" 
              className="border-white/30 text-white hover:bg-white/10 backdrop-blur-sm text-lg px-10"
              asChild
            >
              <Link to="/about">
                서비스 둘러보기
              </Link>
            </Button>
          </div>
          
          <div className="mt-12 flex flex-wrap justify-center gap-8 text-white/80">
            <div className="flex items-center">
              <CheckCircle className="w-5 h-5 mr-2" />
              <span>무료 서비스</span>
            </div>
            <div className="flex items-center">
              <CheckCircle className="w-5 h-5 mr-2" />
              <span>AI 맞춤 추천</span>
            </div>
            <div className="flex items-center">
              <CheckCircle className="w-5 h-5 mr-2" />
              <span>실시간 피드백</span>
            </div>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="bg-gray-900 text-white py-12">
        <div className="max-w-7xl mx-auto px-6">
          <div className="text-center">
            <div className="text-2xl font-bold mb-4">잡았다</div>
            <p className="text-gray-400 mb-4">
              국비 학원 원생 취업 지원 솔루션
            </p>
            <div className="text-sm text-gray-500">
              © 2024 잡았다. All rights reserved.
            </div>
          </div>
        </div>
      </footer>
    </div>
  )
}