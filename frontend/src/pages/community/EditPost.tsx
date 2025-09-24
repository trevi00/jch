import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation } from '@tanstack/react-query'
import { ArrowLeft, Save, X } from 'lucide-react'
import { apiClient } from '@/services/api'
import { useAuthStore } from '@/hooks/useAuthStore'

export default function EditPost() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const { user } = useAuthStore()

  const [title, setTitle] = useState('')
  const [content, setContent] = useState('')
  const [categoryId, setCategoryId] = useState<number>()
  const [imageUrl, setImageUrl] = useState('')

  // 기존 게시글 데이터 조회
  const { data: postData, isLoading: postLoading } = useQuery({
    queryKey: ['post', id],
    queryFn: () => apiClient.getPost(Number(id)),
    enabled: !!id,
  })

  // 카테고리 목록 조회
  const { data: categoriesData } = useQuery({
    queryKey: ['categories'],
    queryFn: () => apiClient.getCategories(),
  })

  const post = postData?.data
  const categories = categoriesData?.data || []

  // 게시글 데이터 로드 시 폼 초기화
  useEffect(() => {
    if (post && categories.length > 0) {
      setTitle(post.title)
      setContent(post.content)
      // categoryName을 통해 categoryId를 찾기
      const category = categories.find(cat => cat.name === post.categoryName)
      setCategoryId(category?.id)
      setImageUrl(post.imageUrl || '')
    }
  }, [post, categories])

  // 권한 확인
  useEffect(() => {
    if (post && user && post.authorId !== user.id && user.userType !== 'ADMIN') {
      alert('수정 권한이 없습니다.')
      navigate(`/community/${id}`)
    }
  }, [post, user, id, navigate])

  const updateMutation = useMutation({
    mutationFn: (data: { title: string; content: string; categoryId?: number; imageUrl?: string }) =>
      apiClient.updatePost(Number(id), data),
    onSuccess: () => {
      navigate(`/community/${id}`)
    },
    onError: (error) => {
      console.error('게시글 수정 실패:', error)
      alert('게시글 수정에 실패했습니다.')
    }
  })

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()

    if (!title.trim()) {
      alert('제목을 입력해주세요.')
      return
    }

    if (!content.trim()) {
      alert('내용을 입력해주세요.')
      return
    }

    updateMutation.mutate({
      title: title.trim(),
      content: content.trim(),
      categoryId,
      imageUrl: imageUrl.trim() || undefined
    })
  }

  if (postLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
      </div>
    )
  }

  if (!post) {
    return (
      <div className="text-center py-12">
        <div className="text-gray-500 mb-2">게시글을 찾을 수 없습니다</div>
        <button
          onClick={() => navigate('/community')}
          className="text-primary-600 hover:text-primary-500"
        >
          커뮤니티로 돌아가기
        </button>
      </div>
    )
  }

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      {/* 헤더 */}
      <div className="flex items-center justify-between">
        <div className="flex items-center space-x-4">
          <button
            onClick={() => navigate(`/community/${id}`)}
            className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
          >
            <ArrowLeft className="w-5 h-5" />
          </button>
          <div>
            <h1 className="text-2xl font-bold text-gray-900">게시글 수정</h1>
            <p className="text-gray-600">게시글을 수정해보세요</p>
          </div>
        </div>
      </div>

      {/* 수정 폼 */}
      <div className="card">
        <div className="card-content">
          <form onSubmit={handleSubmit} className="space-y-6">
            {/* 카테고리 선택 */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                카테고리
              </label>
              <select
                value={categoryId || ''}
                onChange={(e) => setCategoryId(e.target.value ? Number(e.target.value) : undefined)}
                className="input"
              >
                <option value="">카테고리 선택</option>
                {categories.map((category) => (
                  <option key={category.id} value={category.id}>
                    {category.name}
                  </option>
                ))}
              </select>
            </div>

            {/* 제목 */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                제목 *
              </label>
              <input
                type="text"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                className="input"
                placeholder="게시글 제목을 입력하세요"
                required
              />
            </div>

            {/* 이미지 URL */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                이미지 URL (선택사항)
              </label>
              <input
                type="url"
                value={imageUrl}
                onChange={(e) => setImageUrl(e.target.value)}
                className="input"
                placeholder="이미지 URL을 입력하세요"
              />
              {imageUrl && (
                <div className="mt-2">
                  <img
                    src={imageUrl.startsWith('/') ? `http://localhost:8001${imageUrl}` : imageUrl}
                    alt="미리보기"
                    className="w-32 h-32 object-cover rounded-lg"
                    onError={(e) => {
                      e.currentTarget.style.display = 'none'
                    }}
                  />
                </div>
              )}
            </div>

            {/* 내용 */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                내용 *
              </label>
              <textarea
                value={content}
                onChange={(e) => setContent(e.target.value)}
                rows={15}
                className="input resize-none"
                placeholder="게시글 내용을 입력하세요"
                required
              />
            </div>

            {/* 버튼들 */}
            <div className="flex justify-end space-x-3 pt-6 border-t border-gray-200">
              <button
                type="button"
                onClick={() => navigate(`/community/${id}`)}
                className="btn-outline"
              >
                <X className="w-4 h-4 mr-2" />
                취소
              </button>
              <button
                type="submit"
                disabled={updateMutation.isPending}
                className="btn-primary disabled:opacity-50"
              >
                <Save className="w-4 h-4 mr-2" />
                {updateMutation.isPending ? '수정 중...' : '수정하기'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  )
}