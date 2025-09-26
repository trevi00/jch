import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { ArrowLeft, Upload, Sparkles } from 'lucide-react'
import { apiClient } from '@/services/api'
import { useAuthStore } from '@/hooks/useAuthStore'
import { CreatePostRequest } from '@/types/api'

export default function CreatePost() {
  const navigate = useNavigate()
  const { user } = useAuthStore()
  const queryClient = useQueryClient()
  const [formData, setFormData] = useState<CreatePostRequest>({
    title: '',
    content: '',
    categoryId: 1, // 기본값: 취업정보
    imagePrompt: '', // AI 이미지 생성 프롬프트
  })
  const [generatingImage, setGeneratingImage] = useState(false)
  const [generatedImageUrl, setGeneratedImageUrl] = useState<string>('')

  // 카테고리 목록 가져오기
  const { data: categoriesData } = useQuery({
    queryKey: ['categories'],
    queryFn: () => apiClient.getCategories(),
  })

  // AI 이미지 생성
  const generateImageMutation = useMutation({
    mutationFn: (prompt: string) => apiClient.generateImage({ 
      prompt, 
      userId: user?.id 
    }),
    onSuccess: (response) => {
      if (response.success && response.data?.image?.image_url) {
        setGeneratedImageUrl(response.data.image.image_url)
      }
    },
    onError: (error) => {
      console.error('Image generation failed:', error)
      alert('이미지 생성에 실패했습니다. 다시 시도해주세요.')
    }
  })

  // 게시글 생성
  const createPostMutation = useMutation({
    mutationFn: (data: CreatePostRequest) => apiClient.createPost(data),
    onSuccess: (response) => {
      if (response.success) {
        // 게시글 목록 캐시 무효화 - 모든 posts 쿼리를 무효화하여 새로고침
        queryClient.invalidateQueries({ queryKey: ['posts'] })
        // 카테고리 목록도 postCount가 변경될 수 있으므로 무효화
        queryClient.invalidateQueries({ queryKey: ['categories'] })
        navigate('/community')
      }
    },
    onError: (error) => {
      console.error('Post creation failed:', error)
      alert('게시글 작성에 실패했습니다. 다시 시도해주세요.')
    }
  })

  // 카테고리별 접근 권한 확인
  const canAccessCategory = (categoryId: number) => {
    const category = categoriesData?.data?.find(c => c.id === categoryId)
    if (!category) return false
    
    if (category.name === '기업게시판') {
      return user?.userType !== 'GENERAL'
    }
    if (category.name === '공지') {
      return user?.userType === 'ADMIN'
    }

    if (category.name === '면접정보') {
      return user?.userType !== 'COMPANY'
    }
    return true
  }

  const handleGenerateImage = async () => {
    if (!formData.imagePrompt.trim()) {
      alert('이미지 생성을 위한 프롬프트를 입력해주세요.')
      return
    }
    
    setGeneratingImage(true)
    try {
      await generateImageMutation.mutateAsync(formData.imagePrompt)
    } finally {
      setGeneratingImage(false)
    }
  }

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    
    if (!formData.title.trim() || !formData.content.trim()) {
      alert('제목과 내용을 모두 입력해주세요.')
      return
    }

    const selectedCategory = categoriesData?.data?.find(c => c.id === formData.categoryId)
    if (!canAccessCategory(formData.categoryId)) {
      alert('해당 카테고리에 글을 작성할 권한이 없습니다.')
      return
    }

    // 생성된 이미지가 있으면 추가
    const postData: CreatePostRequest = {
      ...formData,
      ...(generatedImageUrl && { imageUrl: generatedImageUrl })
    }

    createPostMutation.mutate(postData)
  }

  // 접근 가능한 카테고리만 필터링
  const availableCategories = categoriesData?.data?.filter(category => 
    canAccessCategory(category.id)
  ) || []

  return (
    <div className="space-y-6">
      <div className="flex items-center space-x-4">
        <button
          onClick={() => navigate('/community')}
          className="flex items-center text-gray-600 hover:text-gray-900"
        >
          <ArrowLeft className="w-5 h-5 mr-2" />
          커뮤니티로 돌아가기
        </button>
      </div>

      <div className="card">
        <div className="card-header">
          <h1 className="text-2xl font-bold text-gray-900">글 작성</h1>
        </div>

        <div className="card-content">
          <form onSubmit={handleSubmit} className="space-y-6">
            {/* 카테고리 선택 */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                카테고리
              </label>
              <select
                value={formData.categoryId}
                onChange={(e) => setFormData({ ...formData, categoryId: Number(e.target.value) })}
                className="w-full input"
                required
              >
                {availableCategories.map((category) => (
                  <option key={category.id} value={category.id}>
                    {category.name}
                  </option>
                ))}
              </select>
            </div>

            {/* 제목 입력 */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                제목
              </label>
              <input
                type="text"
                value={formData.title}
                onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                className="w-full input"
                placeholder="제목을 입력하세요"
                required
              />
            </div>

            {/* AI 이미지 생성 (자유게시판에서만) */}
            {formData.categoryId === availableCategories.find(c => c.name === '자유게시판')?.id && (
              <div className="bg-blue-50 p-4 rounded-lg">
                <div className="flex items-center mb-3">
                  <Sparkles className="w-5 h-5 text-blue-600 mr-2" />
                  <h3 className="text-lg font-medium text-blue-900">AI 이미지 생성</h3>
                </div>
                
                <div className="space-y-3">
                  <div>
                    <label className="block text-sm font-medium text-blue-700 mb-1">
                      이미지 생성 프롬프트
                    </label>
                    <input
                      type="text"
                      value={formData.imagePrompt}
                      onChange={(e) => setFormData({ ...formData, imagePrompt: e.target.value })}
                      className="w-full input"
                      placeholder="생성하고 싶은 이미지를 설명해주세요 (예: 아름다운 일몰이 있는 산 풍경)"
                    />
                  </div>
                  
                  <button
                    type="button"
                    onClick={handleGenerateImage}
                    disabled={generatingImage || !formData.imagePrompt.trim()}
                    className="btn-secondary disabled:opacity-50"
                  >
                    {generatingImage ? (
                      <>
                        <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-blue-600 mr-2"></div>
                        이미지 생성 중...
                      </>
                    ) : (
                      <>
                        <Sparkles className="w-4 h-4 mr-2" />
                        이미지 생성
                      </>
                    )}
                  </button>

                  {/* 생성된 이미지 미리보기 */}
                  {generatedImageUrl && (
                    <div className="mt-4">
                      <p className="text-sm text-blue-700 mb-2">생성된 이미지:</p>
                      <img 
                        src={generatedImageUrl.startsWith('/') ? `http://localhost:8001${generatedImageUrl}` : generatedImageUrl} 
                        alt="AI 생성 이미지" 
                        className="max-w-md rounded-lg shadow-md"
                      />
                    </div>
                  )}
                </div>
              </div>
            )}

            {/* 내용 입력 */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                내용
              </label>
              <textarea
                value={formData.content}
                onChange={(e) => setFormData({ ...formData, content: e.target.value })}
                rows={12}
                className="w-full input resize-vertical"
                placeholder="내용을 입력하세요"
                required
              />
            </div>

            {/* 작성 버튼 */}
            <div className="flex justify-end space-x-3">
              <button
                type="button"
                onClick={() => navigate('/community')}
                className="btn-secondary"
              >
                취소
              </button>
              <button
                type="submit"
                disabled={createPostMutation.isPending}
                className="btn-primary disabled:opacity-50"
              >
                {createPostMutation.isPending ? (
                  <>
                    <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
                    작성 중...
                  </>
                ) : (
                  '작성하기'
                )}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  )
}