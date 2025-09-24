import React, { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Search, MapPin, Briefcase, Lightbulb, TrendingUp, ChevronDown } from 'lucide-react'
import { apiClient } from '@/services/api'
import { JobType, ExperienceLevel } from '@/types/api'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/Card'
import { Input } from '@/components/ui/Input'
import { Badge } from '@/components/ui/Badge'

// 한국 지역 데이터
const KOREA_REGIONS = {
  '서울특별시': ['전체', '강남구', '강동구', '강북구', '강서구', '관악구', '광진구', '구로구', '금천구', '노원구', '도봉구', '동대문구', '동작구', '마포구', '서대문구', '서초구', '성동구', '성북구', '송파구', '양천구', '영등포구', '용산구', '은평구', '종로구', '중구', '중랑구'],
  '부산광역시': ['전체', '강서구', '금정구', '기장군', '남구', '동구', '동래구', '부산진구', '북구', '사상구', '사하구', '서구', '수영구', '연제구', '영도구', '중구', '해운대구'],
  '대구광역시': ['전체', '남구', '달서구', '달성군', '동구', '북구', '서구', '수성구', '중구'],
  '인천광역시': ['전체', '강화군', '계양구', '남동구', '동구', '미추홀구', '부평구', '서구', '연수구', '옹진군', '중구'],
  '광주광역시': ['전체', '광산구', '남구', '동구', '북구', '서구'],
  '대전광역시': ['전체', '대덕구', '동구', '서구', '유성구', '중구'],
  '울산광역시': ['전체', '남구', '동구', '북구', '울주군', '중구'],
  '세종특별자치시': ['전체'],
  '경기도': ['전체', '고양시', '과천시', '광명시', '광주시', '구리시', '군포시', '김포시', '남양주시', '동두천시', '부천시', '성남시', '수원시', '시흥시', '안산시', '안성시', '안양시', '양주시', '오산시', '용인시', '의왕시', '의정부시', '이천시', '파주시', '평택시', '포천시', '하남시', '화성시'],
  '강원도': ['전체', '강릉시', '고성군', '동해시', '삼척시', '속초시', '양구군', '양양군', '영월군', '원주시', '인제군', '정선군', '철원군', '춘천시', '태백시', '평창군', '홍천군', '화천군', '횡성군'],
  '충청북도': ['전체', '괴산군', '단양군', '보은군', '영동군', '옥천군', '음성군', '제천시', '진천군', '청주시', '충주시', '증평군'],
  '충청남도': ['전체', '계룡시', '공주시', '금산군', '논산시', '당진시', '보령시', '부여군', '서산시', '서천군', '아산시', '예산군', '천안시', '청양군', '태안군', '홍성군'],
  '전라북도': ['전체', '고창군', '군산시', '김제시', '남원시', '무주군', '부안군', '순창군', '완주군', '익산시', '임실군', '장수군', '전주시', '정읍시', '진안군'],
  '전라남도': ['전체', '강진군', '고흥군', '곡성군', '광양시', '구례군', '나주시', '담양군', '목포시', '무안군', '보성군', '순천시', '신안군', '여수시', '영광군', '영암군', '완도군', '장성군', '장흥군', '진도군', '함평군', '해남군', '화순군'],
  '경상북도': ['전체', '경산시', '경주시', '고령군', '구미시', '군위군', '김천시', '문경시', '봉화군', '상주시', '성주군', '안동시', '영덕군', '영양군', '영주시', '영천시', '예천군', '울릉군', '울진군', '의성군', '청도군', '청송군', '칠곡군', '포항시'],
  '경상남도': ['전체', '거제시', '거창군', '고성군', '김해시', '남해군', '밀양시', '사천시', '산청군', '양산시', '의령군', '진주시', '창녕군', '창원시', '통영시', '하동군', '함안군', '함양군', '합천군'],
  '제주특별자치도': ['전체', '서귀포시', '제주시']
}

interface QuickSearchProps {
  onResultsFound?: (count: number) => void
  onSearchApply?: (params: {
    keyword?: string
    location?: string
    jobType?: JobType
    experienceLevel?: ExperienceLevel
  }) => void
}

const popularKeywords = [
  'Backend', 'Frontend', 'Full Stack', 'React', 'Spring',
  'Java', 'Python', 'JavaScript', 'DevOps', 'AI/ML'
]

const popularLocations = [
  '서울', '판교', '강남', '가산', '마포', '성수', '부산', '대구', '인천'
]

export default function QuickSearch({ onResultsFound, onSearchApply }: QuickSearchProps) {
  const [keyword, setKeyword] = useState('')
  const [location, setLocation] = useState('')
  const [selectedJobType, setSelectedJobType] = useState<JobType | undefined>()
  const [selectedLevel, setSelectedLevel] = useState<ExperienceLevel | undefined>()

  // 지역 선택 관련 상태
  const [locationMode, setLocationMode] = useState<'search' | 'toggle'>('search')
  const [selectedProvince, setSelectedProvince] = useState('')
  const [selectedCity, setSelectedCity] = useState('')
  const [hoveredProvince, setHoveredProvince] = useState<string | null>(null)
  const [isLocationMenuOpen, setIsLocationMenuOpen] = useState(false)

  const { data: quickResults, isLoading } = useQuery({
    queryKey: ['quickSearch', keyword, location, selectedJobType, selectedLevel],
    queryFn: () => {
      if (!keyword && !location && !selectedJobType && !selectedLevel) return null
      return apiClient.searchJobsSimple(keyword, location, selectedJobType, selectedLevel, 0, 10)
    },
    enabled: !!(keyword || location || selectedJobType || selectedLevel)
  })

  React.useEffect(() => {
    if (quickResults?.data && onResultsFound) {
      onResultsFound(quickResults.data.totalElements)
    }
  }, [quickResults, onResultsFound])

  const handleApplySearch = () => {
    if (onSearchApply) {
      onSearchApply({
        keyword: keyword || undefined,
        location: location || undefined,
        jobType: selectedJobType,
        experienceLevel: selectedLevel
      })
    }
  }

  const handleKeywordClick = (selectedKeyword: string) => {
    setKeyword(selectedKeyword)
  }

  const handleLocationClick = (selectedLocation: string) => {
    setLocation(selectedLocation)
    setLocationMode('search') // 인기 지역 클릭 시 검색 모드로 전환
  }

  // 지역 모드 변경
  const handleLocationModeChange = (mode: 'search' | 'toggle') => {
    setLocationMode(mode)
    if (mode === 'search') {
      setSelectedProvince('')
      setSelectedCity('')
    } else {
      setLocation('')
    }
  }

  // 지역 선택 (도/시 토글 방식)
  const handleLocationSelect = (province: string, city?: string) => {
    setSelectedProvince(province)
    if (city && city !== '전체') {
      setSelectedCity(city)
      setLocation(`${province} ${city}`)
    } else {
      setSelectedCity('')
      setLocation(province)
    }
    setIsLocationMenuOpen(false)
  }

  const clearSearch = () => {
    setKeyword('')
    setLocation('')
    setSelectedJobType(undefined)
    setSelectedLevel(undefined)
    setSelectedProvince('')
    setSelectedCity('')
    setLocationMode('search')
  }

  const hasSearchTerms = keyword || location || selectedJobType || selectedLevel

  return (
    <Card className="border-blue-200 bg-gradient-to-r from-blue-50 to-indigo-50">
      <CardHeader className="pb-4">
        <CardTitle className="text-lg flex items-center text-blue-800">
          <Search className="w-5 h-5 mr-2" />
          🔍 빠른 검색
        </CardTitle>
        <p className="text-sm text-blue-700">
          키워드와 지역으로 원하는 채용공고를 빠르게 찾아보세요
        </p>
      </CardHeader>
      <CardContent className="space-y-4">
        {/* 검색 입력 필드 */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
          <div>
            <Input
              placeholder="키워드 (예: backend, java, react)"
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              leftIcon={<Search className="h-4 w-4" />}
              className="bg-white"
            />
          </div>
          <div>
            {/* 지역 필터 모드 토글 */}
            <div className="flex items-center gap-2 mb-2">
              <button
                type="button"
                onClick={() => handleLocationModeChange('search')}
                className={`px-3 py-1 text-xs rounded-full transition-colors ${
                  locationMode === 'search'
                    ? 'bg-green-500 text-white'
                    : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                }`}
              >
                직접 입력
              </button>
              <button
                type="button"
                onClick={() => handleLocationModeChange('toggle')}
                className={`px-3 py-1 text-xs rounded-full transition-colors ${
                  locationMode === 'toggle'
                    ? 'bg-green-500 text-white'
                    : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                }`}
              >
                도/시 선택
              </button>
            </div>

            {/* 직접 입력 모드 */}
            {locationMode === 'search' && (
              <Input
                placeholder="지역 (예: 서울, 판교, 강남)"
                value={location}
                onChange={(e) => setLocation(e.target.value)}
                leftIcon={<MapPin className="h-4 w-4" />}
                className="bg-white"
              />
            )}

            {/* 도/시 선택 모드 */}
            {locationMode === 'toggle' && (
              <div className="relative">
                <button
                  type="button"
                  onClick={() => setIsLocationMenuOpen(!isLocationMenuOpen)}
                  onBlur={() => setTimeout(() => setIsLocationMenuOpen(false), 200)}
                  className="flex w-full items-center justify-between rounded-lg border border-gray-300 bg-white px-4 py-2 text-sm hover:border-green-400 focus:border-green-500 focus:outline-none focus:ring-2 focus:ring-green-500 focus:ring-offset-2"
                >
                  <span className="flex items-center gap-2">
                    <MapPin className="h-4 w-4 text-gray-400" />
                    <span className={selectedProvince ? 'text-gray-900' : 'text-gray-500'}>
                      {selectedProvince && selectedCity && selectedCity !== '전체'
                        ? `${selectedProvince} ${selectedCity}`
                        : selectedProvince
                        ? selectedProvince
                        : '지역을 선택하세요'}
                    </span>
                  </span>
                  <ChevronDown className={`h-4 w-4 transition-transform ${isLocationMenuOpen ? 'rotate-180' : ''}`} />
                </button>

                {/* 드롭다운 메뉴 */}
                {isLocationMenuOpen && (
                  <div
                    className="absolute top-full left-0 z-50 mt-2 w-[500px] rounded-lg border border-gray-200 bg-white shadow-xl"
                    onMouseLeave={() => setHoveredProvince(null)}
                  >
                    <div className="flex h-[400px]">
                      {/* 도/특별시/광역시 목록 */}
                      <div className="w-1/2 border-r border-gray-200 overflow-y-auto">
                        <div className="sticky top-0 bg-gray-50 px-4 py-2 border-b border-gray-200">
                          <p className="text-xs font-semibold text-gray-600">도/특별시/광역시</p>
                        </div>
                        {Object.keys(KOREA_REGIONS).map(province => (
                          <button
                            key={province}
                            type="button"
                            onMouseEnter={() => setHoveredProvince(province)}
                            onClick={() => handleLocationSelect(province)}
                            className={`w-full px-4 py-2.5 text-left text-sm hover:bg-green-50 flex items-center justify-between group ${
                              selectedProvince === province ? 'bg-green-50 text-green-600 font-medium' : 'text-gray-700'
                            }`}
                          >
                            <span>{province}</span>
                            <ChevronDown className="h-3 w-3 rotate-[-90deg] opacity-0 group-hover:opacity-100 text-gray-400" />
                          </button>
                        ))}
                      </div>

                      {/* 시/구/군 목록 */}
                      <div className="w-1/2 overflow-y-auto">
                        {hoveredProvince && (
                          <>
                            <div className="sticky top-0 bg-gray-50 px-4 py-2 border-b border-gray-200">
                              <p className="text-xs font-semibold text-gray-600">{hoveredProvince}</p>
                            </div>
                            {KOREA_REGIONS[hoveredProvince]?.map(city => (
                              <button
                                key={city}
                                type="button"
                                onClick={() => handleLocationSelect(hoveredProvince, city)}
                                className={`w-full px-4 py-2.5 text-left text-sm hover:bg-green-50 ${
                                  selectedProvince === hoveredProvince && selectedCity === city ? 'bg-green-50 text-green-600 font-medium' : 'text-gray-700'
                                }`}
                              >
                                {city}
                              </button>
                            ))}
                          </>
                        )}
                      </div>
                    </div>
                  </div>
                )}
              </div>
            )}
          </div>
        </div>

        {/* 필터 선택 */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">고용형태</label>
            <select
              value={selectedJobType || ''}
              onChange={(e) => setSelectedJobType(e.target.value as JobType || undefined)}
              className="w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="">전체</option>
              <option value={JobType.FULL_TIME}>정규직</option>
              <option value={JobType.PART_TIME}>파트타임</option>
              <option value={JobType.CONTRACT}>계약직</option>
              <option value={JobType.INTERN}>인턴십</option>
              <option value={JobType.FREELANCE}>프리랜서</option>
            </select>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">경력</label>
            <select
              value={selectedLevel || ''}
              onChange={(e) => setSelectedLevel(e.target.value as ExperienceLevel || undefined)}
              className="w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="">전체</option>
              <option value={ExperienceLevel.ENTRY_LEVEL}>신입</option>
              <option value={ExperienceLevel.JUNIOR}>주니어</option>
              <option value={ExperienceLevel.MID_LEVEL}>미들</option>
              <option value={ExperienceLevel.SENIOR}>시니어</option>
              <option value={ExperienceLevel.ANY}>경력무관</option>
            </select>
          </div>
        </div>

        {/* 인기 키워드 */}
        <div>
          <div className="flex items-center mb-2">
            <TrendingUp className="w-4 h-4 mr-1 text-gray-600" />
            <label className="text-sm font-medium text-gray-700">인기 키워드</label>
          </div>
          <div className="flex flex-wrap gap-2">
            {popularKeywords.map((popularKeyword) => (
              <button
                key={popularKeyword}
                onClick={() => handleKeywordClick(popularKeyword)}
                className={`px-3 py-1 text-xs rounded-full transition-colors ${
                  keyword === popularKeyword
                    ? 'bg-blue-500 text-white'
                    : 'bg-white border border-blue-200 text-blue-700 hover:bg-blue-50'
                }`}
              >
                {popularKeyword}
              </button>
            ))}
          </div>
        </div>

        {/* 인기 지역 */}
        <div>
          <div className="flex items-center mb-2">
            <MapPin className="w-4 h-4 mr-1 text-gray-600" />
            <label className="text-sm font-medium text-gray-700">인기 지역</label>
          </div>
          <div className="flex flex-wrap gap-2">
            {popularLocations.map((popularLocation) => (
              <button
                key={popularLocation}
                onClick={() => handleLocationClick(popularLocation)}
                className={`px-3 py-1 text-xs rounded-full transition-colors ${
                  location === popularLocation
                    ? 'bg-green-500 text-white'
                    : 'bg-white border border-green-200 text-green-700 hover:bg-green-50'
                }`}
              >
                {popularLocation}
              </button>
            ))}
          </div>
        </div>

        {/* 검색 결과 미리보기 */}
        {hasSearchTerms && (
          <div className="bg-white p-4 rounded-lg border border-blue-200">
            {isLoading ? (
              <div className="flex items-center">
                <div className="w-4 h-4 border-2 border-blue-500 border-t-transparent rounded-full animate-spin mr-2"></div>
                <span className="text-sm text-gray-600">검색 중...</span>
              </div>
            ) : quickResults?.data ? (
              <div className="space-y-3">
                <div className="flex items-center justify-between">
                  <div className="flex items-center">
                    <Lightbulb className="w-4 h-4 mr-1 text-blue-500" />
                    <span className="text-sm font-medium text-blue-700">
                      💡 {quickResults.data.totalElements}개의 관련 공고를 찾았습니다
                    </span>
                  </div>
                  <Badge variant="info" size="sm">
                    실시간 결과
                  </Badge>
                </div>

                {/* 액션 버튼 */}
                <div className="flex gap-2">
                  <button
                    onClick={handleApplySearch}
                    className="flex-1 px-4 py-2 bg-blue-600 text-white text-sm rounded-lg hover:bg-blue-700 transition-colors flex items-center justify-center"
                  >
                    <Search className="w-4 h-4 mr-1" />
                    검색 결과 보기
                  </button>
                  <button
                    onClick={clearSearch}
                    className="px-4 py-2 border border-gray-300 text-gray-700 text-sm rounded-lg hover:bg-gray-50 transition-colors"
                  >
                    초기화
                  </button>
                </div>

                {/* 검색 요약 */}
                {(keyword || location || selectedJobType || selectedLevel) && (
                  <div className="text-xs text-gray-500 pt-2 border-t border-gray-200">
                    <span>검색 조건: </span>
                    {keyword && <span className="bg-blue-100 text-blue-800 px-2 py-1 rounded mr-1">키워드: {keyword}</span>}
                    {location && <span className="bg-green-100 text-green-800 px-2 py-1 rounded mr-1">지역: {location}</span>}
                    {selectedJobType && <span className="bg-purple-100 text-purple-800 px-2 py-1 rounded mr-1">고용형태: {selectedJobType}</span>}
                    {selectedLevel && <span className="bg-orange-100 text-orange-800 px-2 py-1 rounded mr-1">경력: {selectedLevel}</span>}
                  </div>
                )}
              </div>
            ) : (
              <div className="text-sm text-gray-500">
                검색 조건에 맞는 공고가 없습니다
              </div>
            )}
          </div>
        )}
      </CardContent>
    </Card>
  )
}