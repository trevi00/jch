import { useState, useEffect } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { 
  MessageSquare, 
  Search, 
  Filter,
  Plus,
  Eye,
  Edit,
  Trash2,
  Hash,
  Calendar,
  User,
  Heart,
  MessageCircle,
  TrendingUp,
  AlertCircle,
  Image,
  Smile,
  Frown,
  Meh
} from 'lucide-react'
import { apiClient } from '@/services/api'

interface Post {
  id: number
  title: string
  content: string
  authorName: string
  authorEmail: string
  categoryName: string
  categoryId: number
  createdAt: string
  updatedAt: string
  viewCount: number
  commentCount: number
  imageUrl?: string
  sentimentLabel?: 'positive' | 'negative' | 'neutral'
  sentimentScore?: number
}

interface Category {
  id: number
  name: string
  description: string
  postCount: number
  isActive: boolean
  createdAt: string
}

interface CommunityStatistics {
  totalPosts: number
  totalComments: number
  totalCategories: number
  positivePosts: number
  negativePosts: number
  neutralPosts: number
  postsWithImages: number
  avgSentimentScore: number
}

export default function CommunityManagement() {
  const [isAdmin, setIsAdmin] = useState(false)

  // Check admin authentication
  useEffect(() => {
    const adminToken = localStorage.getItem('adminToken')
    const isAdminStatus = localStorage.getItem('isAdmin')
    setIsAdmin(adminToken && isAdminStatus === 'true')
  }, [])
  const queryClient = useQueryClient()
  const [activeTab, setActiveTab] = useState<'posts' | 'categories'>('posts')
  const [searchTerm, setSearchTerm] = useState('')
  const [categoryFilter, setCategoryFilter] = useState<string>('ALL')
  const [sentimentFilter, setSentimentFilter] = useState<string>('ALL')
  const [selectedPost, setSelectedPost] = useState<Post | null>(null)
  const [newCategory, setNewCategory] = useState({ name: '', description: '' })
  const [showCategoryModal, setShowCategoryModal] = useState(false)

  const { data: posts, isLoading: postsLoading } = useQuery({
    queryKey: ['admin-posts'],
    queryFn: async () => {
      const response = await apiClient.getPosts({ page: 0, size: 100 })
      return response.data.content || []
    },
    enabled: isAdmin
  })

  const { data: categories, isLoading: categoriesLoading } = useQuery({
    queryKey: ['admin-categories'],
    queryFn: async () => {
      const response = await apiClient.getCategories()
      return response.data as Category[]
    },
    enabled: isAdmin
  })

  const { data: statistics } = useQuery({
    queryKey: ['community-statistics'],
    queryFn: async () => {
      // Mock statistics based on posts data
      if (!posts) return null
      
      const positivePosts = posts.filter(p => p.sentimentLabel === 'positive').length
      const negativePosts = posts.filter(p => p.sentimentLabel === 'negative').length
      const neutralPosts = posts.filter(p => p.sentimentLabel === 'neutral').length
      const postsWithImages = posts.filter(p => p.imageUrl).length
      const totalComments = posts.reduce((acc, post) => acc + (post.commentCount || 0), 0)
      const avgSentiment = posts.reduce((acc, post) => acc + (post.sentimentScore || 0), 0) / Math.max(posts.length, 1)

      return {
        totalPosts: posts.length,
        totalComments,
        totalCategories: categories?.length || 0,
        positivePosts,
        negativePosts,
        neutralPosts,
        postsWithImages,
        avgSentimentScore: Math.round(avgSentiment * 100) / 100
      } as CommunityStatistics
    },
    enabled: !!posts && !!categories
  })

  const deletePostMutation = useMutation({
    mutationFn: (postId: number) => apiClient.deletePost(postId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-posts'] })
      alert('게시글이 삭제되었습니다.')
    },
    onError: (error) => {
      console.error('게시글 삭제 실패:', error)
      alert('게시글 삭제에 실패했습니다.')
    }
  })

  const createCategoryMutation = useMutation({
    mutationFn: (category: { name: string; description: string }) => 
      apiClient.createCategory(category),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-categories'] })
      setShowCategoryModal(false)
      setNewCategory({ name: '', description: '' })
      alert('카테고리가 생성되었습니다.')
    },
    onError: (error) => {
      console.error('카테고리 생성 실패:', error)
      alert('카테고리 생성에 실패했습니다.')
    }
  })

  const deleteCategoryMutation = useMutation({
    mutationFn: (categoryId: number) => apiClient.deleteCategory(categoryId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-categories'] })
      alert('카테고리가 삭제되었습니다.')
    },
    onError: (error) => {
      console.error('카테고리 삭제 실패:', error)
      alert('카테고리 삭제에 실패했습니다.')
    }
  })

  if (!isAdmin) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <AlertCircle className="w-16 h-16 text-red-500 mx-auto mb-4" />
          <h2 className="text-xl font-semibold mb-2">접근 권한이 없습니다</h2>
          <p className="text-gray-600">관리자만 접근할 수 있는 페이지입니다.</p>
        </div>
      </div>
    )
  }

  if (postsLoading || categoriesLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-500"></div>
      </div>
    )
  }

  const filteredPosts = posts?.filter((post) => {
    const matchesSearch = post.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         post.content.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         post.authorName.toLowerCase().includes(searchTerm.toLowerCase())
    const matchesCategory = categoryFilter === 'ALL' || post.categoryId.toString() === categoryFilter
    const matchesSentiment = sentimentFilter === 'ALL' || post.sentimentLabel === sentimentFilter
    
    return matchesSearch && matchesCategory && matchesSentiment
  }) || []

  const handleDeletePost = (postId: number) => {
    if (confirm('정말로 이 게시글을 삭제하시겠습니까?')) {
      deletePostMutation.mutate(postId)
    }
  }

  const handleDeleteCategory = (categoryId: number) => {
    if (confirm('정말로 이 카테고리를 삭제하시겠습니까? 연관된 게시글도 영향을 받을 수 있습니다.')) {
      deleteCategoryMutation.mutate(categoryId)
    }
  }

  const getSentimentIcon = (sentiment?: string) => {
    switch (sentiment) {
      case 'positive':
        return <Smile className="w-4 h-4 text-green-600" />
      case 'negative':
        return <Frown className="w-4 h-4 text-red-600" />
      case 'neutral':
        return <Meh className="w-4 h-4 text-yellow-600" />
      default:
        return <Meh className="w-4 h-4 text-gray-400" />
    }
  }

  const getSentimentLabel = (sentiment?: string) => {
    switch (sentiment) {
      case 'positive':
        return '긍정'
      case 'negative':
        return '부정'
      case 'neutral':
        return '중립'
      default:
        return '분석안됨'
    }
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">커뮤니티 관리</h1>
        <p className="text-gray-600">커뮤니티 게시글과 카테고리를 관리합니다</p>
      </div>

      {/* 통계 카드 */}
      <div className="grid grid-cols-1 md:grid-cols-4 lg:grid-cols-8 gap-4">
        <div className="card">
          <div className="card-content text-center">
            <MessageSquare className="w-8 h-8 text-blue-500 mx-auto mb-2" />
            <p className="text-xs font-medium text-gray-600">전체 게시글</p>
            <p className="text-xl font-bold text-gray-900">{statistics?.totalPosts || 0}</p>
          </div>
        </div>
        <div className="card">
          <div className="card-content text-center">
            <MessageCircle className="w-8 h-8 text-purple-500 mx-auto mb-2" />
            <p className="text-xs font-medium text-gray-600">전체 댓글</p>
            <p className="text-xl font-bold text-gray-900">{statistics?.totalComments || 0}</p>
          </div>
        </div>
        <div className="card">
          <div className="card-content text-center">
            <Hash className="w-8 h-8 text-orange-500 mx-auto mb-2" />
            <p className="text-xs font-medium text-gray-600">카테고리</p>
            <p className="text-xl font-bold text-gray-900">{statistics?.totalCategories || 0}</p>
          </div>
        </div>
        <div className="card">
          <div className="card-content text-center">
            <Smile className="w-8 h-8 text-green-500 mx-auto mb-2" />
            <p className="text-xs font-medium text-gray-600">긍정 게시글</p>
            <p className="text-xl font-bold text-gray-900">{statistics?.positivePosts || 0}</p>
          </div>
        </div>
        <div className="card">
          <div className="card-content text-center">
            <Frown className="w-8 h-8 text-red-500 mx-auto mb-2" />
            <p className="text-xs font-medium text-gray-600">부정 게시글</p>
            <p className="text-xl font-bold text-gray-900">{statistics?.negativePosts || 0}</p>
          </div>
        </div>
        <div className="card">
          <div className="card-content text-center">
            <Meh className="w-8 h-8 text-yellow-500 mx-auto mb-2" />
            <p className="text-xs font-medium text-gray-600">중립 게시글</p>
            <p className="text-xl font-bold text-gray-900">{statistics?.neutralPosts || 0}</p>
          </div>
        </div>
        <div className="card">
          <div className="card-content text-center">
            <Image className="w-8 h-8 text-pink-500 mx-auto mb-2" />
            <p className="text-xs font-medium text-gray-600">이미지 게시글</p>
            <p className="text-xl font-bold text-gray-900">{statistics?.postsWithImages || 0}</p>
          </div>
        </div>
        <div className="card">
          <div className="card-content text-center">
            <TrendingUp className="w-8 h-8 text-indigo-500 mx-auto mb-2" />
            <p className="text-xs font-medium text-gray-600">평균 감정점수</p>
            <p className="text-xl font-bold text-gray-900">{statistics?.avgSentimentScore || 0}</p>
          </div>
        </div>
      </div>

      {/* 탭 네비게이션 */}
      <div className="border-b border-gray-200">
        <nav className="-mb-px flex space-x-8">
          <button
            onClick={() => setActiveTab('posts')}
            className={`py-2 px-1 border-b-2 font-medium text-sm ${
              activeTab === 'posts'
                ? 'border-blue-500 text-blue-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
            }`}
          >
            <MessageSquare className="w-4 h-4 inline mr-2" />
            게시글 관리
          </button>
          <button
            onClick={() => setActiveTab('categories')}
            className={`py-2 px-1 border-b-2 font-medium text-sm ${
              activeTab === 'categories'
                ? 'border-blue-500 text-blue-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
            }`}
          >
            <Hash className="w-4 h-4 inline mr-2" />
            카테고리 관리
          </button>
        </nav>
      </div>

      {activeTab === 'posts' && (
        <>
          {/* 게시글 필터 및 검색 */}
          <div className="card">
            <div className="card-content">
              <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                <div className="relative">
                  <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-4 h-4" />
                  <input
                    type="text"
                    placeholder="제목, 내용, 작성자로 검색..."
                    className="pl-10 pr-4 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent w-full"
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                  />
                </div>
                
                <div>
                  <select
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    value={categoryFilter}
                    onChange={(e) => setCategoryFilter(e.target.value)}
                  >
                    <option value="ALL">전체 카테고리</option>
                    {categories?.map((category) => (
                      <option key={category.id} value={category.id.toString()}>
                        {category.name}
                      </option>
                    ))}
                  </select>
                </div>

                <div>
                  <select
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    value={sentimentFilter}
                    onChange={(e) => setSentimentFilter(e.target.value)}
                  >
                    <option value="ALL">전체 감정</option>
                    <option value="positive">긍정</option>
                    <option value="neutral">중립</option>
                    <option value="negative">부정</option>
                  </select>
                </div>

                <div className="text-sm text-gray-600 flex items-center">
                  <Filter className="w-4 h-4 mr-2" />
                  {filteredPosts.length}개 결과
                </div>
              </div>
            </div>
          </div>

          {/* 게시글 목록 */}
          <div className="card">
            <div className="card-content">
              <div className="overflow-x-auto">
                <table className="min-w-full">
                  <thead>
                    <tr className="border-b border-gray-200">
                      <th className="text-left py-3 px-3 text-sm font-medium text-gray-600">게시글</th>
                      <th className="text-left py-3 px-3 text-sm font-medium text-gray-600">작성자</th>
                      <th className="text-left py-3 px-3 text-sm font-medium text-gray-600">카테고리</th>
                      <th className="text-left py-3 px-3 text-sm font-medium text-gray-600">감정 분석</th>
                      <th className="text-left py-3 px-3 text-sm font-medium text-gray-600">통계</th>
                      <th className="text-left py-3 px-3 text-sm font-medium text-gray-600">작성일</th>
                      <th className="text-left py-3 px-3 text-sm font-medium text-gray-600">작업</th>
                    </tr>
                  </thead>
                  <tbody>
                    {filteredPosts.map((post) => (
                      <tr key={post.id} className="border-b border-gray-100">
                        <td className="py-3 px-3">
                          <div className="flex items-start space-x-3">
                            {post.imageUrl && (
                              <Image className="w-4 h-4 text-pink-500 mt-1" />
                            )}
                            <div>
                              <p className="text-sm font-medium text-gray-900 line-clamp-2">
                                {post.title}
                              </p>
                              <p className="text-xs text-gray-500 line-clamp-1 mt-1">
                                {post.content.substring(0, 100)}...
                              </p>
                            </div>
                          </div>
                        </td>
                        <td className="py-3 px-3">
                          <div className="flex items-center">
                            <User className="w-4 h-4 text-gray-400 mr-1" />
                            <span className="text-sm text-gray-900">{post.authorName}</span>
                          </div>
                        </td>
                        <td className="py-3 px-3">
                          <span className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                            <Hash className="w-3 h-3 mr-1" />
                            {post.categoryName}
                          </span>
                        </td>
                        <td className="py-3 px-3">
                          <div className="flex items-center space-x-2">
                            {getSentimentIcon(post.sentimentLabel)}
                            <span className="text-xs text-gray-600">
                              {getSentimentLabel(post.sentimentLabel)}
                            </span>
                            {post.sentimentScore && (
                              <span className="text-xs text-gray-500">
                                ({post.sentimentScore.toFixed(2)})
                              </span>
                            )}
                          </div>
                        </td>
                        <td className="py-3 px-3">
                          <div className="flex items-center space-x-3 text-xs text-gray-500">
                            <div className="flex items-center">
                              <Eye className="w-3 h-3 mr-1" />
                              {post.viewCount}
                            </div>
                            <div className="flex items-center">
                              <MessageCircle className="w-3 h-3 mr-1" />
                              {post.commentCount}
                            </div>
                          </div>
                        </td>
                        <td className="py-3 px-3 text-sm text-gray-500">
                          {new Date(post.createdAt).toLocaleDateString('ko-KR')}
                        </td>
                        <td className="py-3 px-3">
                          <div className="flex space-x-2">
                            <button
                              onClick={() => setSelectedPost(post)}
                              className="text-blue-600 hover:text-blue-800"
                              title="상세 보기"
                            >
                              <Eye className="w-4 h-4" />
                            </button>
                            <button
                              onClick={() => handleDeletePost(post.id)}
                              className="text-red-600 hover:text-red-800"
                              title="삭제"
                            >
                              <Trash2 className="w-4 h-4" />
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
                
                {filteredPosts.length === 0 && (
                  <div className="text-center py-8">
                    <MessageSquare className="w-16 h-16 text-gray-300 mx-auto mb-4" />
                    <p className="text-gray-500">조건에 맞는 게시글이 없습니다.</p>
                  </div>
                )}
              </div>
            </div>
          </div>
        </>
      )}

      {activeTab === 'categories' && (
        <>
          {/* 카테고리 관리 헤더 */}
          <div className="flex justify-between items-center">
            <div>
              <h2 className="text-lg font-semibold text-gray-900">카테고리 목록</h2>
              <p className="text-sm text-gray-600">커뮤니티 카테고리를 관리하세요</p>
            </div>
            <button
              onClick={() => setShowCategoryModal(true)}
              className="btn-primary flex items-center"
            >
              <Plus className="w-4 h-4 mr-2" />
              새 카테고리
            </button>
          </div>

          {/* 카테고리 목록 */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {categories?.map((category) => (
              <div key={category.id} className="card">
                <div className="card-content">
                  <div className="flex justify-between items-start mb-3">
                    <div className="flex items-center">
                      <Hash className="w-5 h-5 text-blue-500 mr-2" />
                      <h3 className="text-lg font-semibold text-gray-900">{category.name}</h3>
                    </div>
                    <button
                      onClick={() => handleDeleteCategory(category.id)}
                      className="text-red-600 hover:text-red-800"
                      title="카테고리 삭제"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>
                  <p className="text-sm text-gray-600 mb-3">{category.description}</p>
                  <div className="flex justify-between items-center text-sm text-gray-500">
                    <span>{category.postCount}개 게시글</span>
                    <span>{new Date(category.createdAt).toLocaleDateString('ko-KR')}</span>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </>
      )}

      {/* 게시글 상세 모달 */}
      {selectedPost && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-lg max-w-2xl w-full max-h-[80vh] overflow-y-auto">
            <div className="p-6">
              <div className="flex justify-between items-start mb-4">
                <div>
                  <h2 className="text-xl font-bold text-gray-900">{selectedPost.title}</h2>
                  <div className="flex items-center mt-2 space-x-4">
                    <div className="flex items-center">
                      <User className="w-4 h-4 text-gray-400 mr-1" />
                      <span className="text-sm text-gray-600">{selectedPost.authorName}</span>
                    </div>
                    <div className="flex items-center">
                      <Hash className="w-4 h-4 text-gray-400 mr-1" />
                      <span className="text-sm text-gray-600">{selectedPost.categoryName}</span>
                    </div>
                    <div className="flex items-center">
                      {getSentimentIcon(selectedPost.sentimentLabel)}
                      <span className="text-sm text-gray-600 ml-1">
                        {getSentimentLabel(selectedPost.sentimentLabel)}
                      </span>
                    </div>
                  </div>
                </div>
                <button
                  onClick={() => setSelectedPost(null)}
                  className="text-gray-400 hover:text-gray-600"
                >
                  <AlertCircle className="w-6 h-6" />
                </button>
              </div>

              <div className="space-y-4">
                {selectedPost.imageUrl && (
                  <div>
                    <img 
                      src={selectedPost.imageUrl.startsWith('/') ? `http://localhost:8001${selectedPost.imageUrl}` : selectedPost.imageUrl} 
                      alt="게시글 이미지"
                      className="w-full max-w-md mx-auto rounded-lg"
                      onError={(e) => {
                        const target = e.target as HTMLImageElement;
                        target.style.display = 'none';
                        const container = target.parentElement;
                        if (container && !container.querySelector('.error-message')) {
                          const errorDiv = document.createElement('div');
                          errorDiv.className = 'error-message text-center p-4 border border-gray-300 rounded-lg bg-gray-50';
                          errorDiv.innerHTML = '<div class="flex items-center justify-center text-gray-500"><svg class="w-8 h-8 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"></path></svg>이미지를 불러올 수 없습니다 (URL 만료)</div>';
                          container.appendChild(errorDiv);
                        }
                      }}
                    />
                  </div>
                )}

                <div>
                  <p className="text-sm text-gray-900 whitespace-pre-wrap">
                    {selectedPost.content}
                  </p>
                </div>

                <div className="grid grid-cols-2 gap-4 pt-4 border-t border-gray-200">
                  <div>
                    <p className="text-sm font-medium text-gray-700">조회수</p>
                    <p className="text-sm text-gray-900">{selectedPost.viewCount}</p>
                  </div>
                  <div>
                    <p className="text-sm font-medium text-gray-700">댓글 수</p>
                    <p className="text-sm text-gray-900">{selectedPost.commentCount}</p>
                  </div>
                  <div>
                    <p className="text-sm font-medium text-gray-700">작성일</p>
                    <p className="text-sm text-gray-900">
                      {new Date(selectedPost.createdAt).toLocaleDateString('ko-KR')}
                    </p>
                  </div>
                  <div>
                    <p className="text-sm font-medium text-gray-700">수정일</p>
                    <p className="text-sm text-gray-900">
                      {new Date(selectedPost.updatedAt).toLocaleDateString('ko-KR')}
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* 카테고리 생성 모달 */}
      {showCategoryModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-lg max-w-md w-full">
            <div className="p-6">
              <div className="flex justify-between items-center mb-4">
                <h2 className="text-lg font-bold text-gray-900">새 카테고리 생성</h2>
                <button
                  onClick={() => setShowCategoryModal(false)}
                  className="text-gray-400 hover:text-gray-600"
                >
                  <AlertCircle className="w-6 h-6" />
                </button>
              </div>

              <div className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    카테고리 이름
                  </label>
                  <input
                    type="text"
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    placeholder="카테고리 이름을 입력하세요"
                    value={newCategory.name}
                    onChange={(e) => setNewCategory({ ...newCategory, name: e.target.value })}
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    설명
                  </label>
                  <textarea
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    rows={3}
                    placeholder="카테고리 설명을 입력하세요"
                    value={newCategory.description}
                    onChange={(e) => setNewCategory({ ...newCategory, description: e.target.value })}
                  />
                </div>

                <div className="flex space-x-3 pt-4">
                  <button
                    onClick={() => setShowCategoryModal(false)}
                    className="flex-1 px-4 py-2 text-gray-700 bg-gray-100 rounded-md hover:bg-gray-200"
                  >
                    취소
                  </button>
                  <button
                    onClick={() => createCategoryMutation.mutate(newCategory)}
                    disabled={!newCategory.name.trim()}
                    className="flex-1 px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    생성
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}