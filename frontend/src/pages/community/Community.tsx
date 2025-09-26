import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { Plus, Search, MessageCircle, Heart, Eye, Clock, Smile, Frown, Meh } from 'lucide-react'
import { apiClient } from '@/services/api'
import { useAuthStore } from '@/hooks/useAuthStore'

export default function Community() {
  const { user } = useAuthStore()
  const [searchTerm, setSearchTerm] = useState('')
  const [selectedCategory, setSelectedCategory] = useState<number | null>(null)
  const [page, setPage] = useState(0)

  const { data: categoriesData } = useQuery({
    queryKey: ['categories'],
    queryFn: () => apiClient.getCategories(),
  })

  const { data: postsData, isLoading } = useQuery({
    queryKey: ['posts', selectedCategory, searchTerm, page, user?.userType],
    queryFn: () => {
      // 일반 사용자가 전체 글을 볼 때는 기업게시판을 제외
      let categoryId = selectedCategory;
      if (selectedCategory === null && user?.userType === 'GENERAL') {
        // 기업게시판 ID (5)를 제외한 모든 카테고리의 글을 가져오기 위해
        // 기업게시판이 아닌 다른 카테고리들만 표시하도록 필터링
        // 하지만 API가 excludeCategory를 지원하지 않으므로, 클라이언트에서 필터링
        categoryId = undefined;
      }
      
      return apiClient.getPosts({
        categoryId: categoryId || undefined,
        search: searchTerm || undefined,
        page,
        size: 10,
      });
    },
  })

  const formatDate = (date: string | number[]) => {
    const now = new Date()
    let postDate: Date
    
    if (Array.isArray(date)) {
      // Java LocalDateTime 배열 형태: [year, month, day, hour, minute, second, nanosecond]
      const [year, month, day, hour, minute, second, nanosecond] = date
      postDate = new Date(year, month - 1, day, hour, minute, second, Math.floor(nanosecond / 1000000))
    } else {
      postDate = new Date(date)
    }
    
    const diffInHours = Math.floor((now.getTime() - postDate.getTime()) / (1000 * 60 * 60))
    
    if (diffInHours < 1) return '방금 전'
    if (diffInHours < 24) return `${diffInHours}시간 전`
    if (diffInHours < 24 * 7) return `${Math.floor(diffInHours / 24)}일 전`
    return postDate.toLocaleDateString()
  }

  const getSentimentIcon = (sentimentLabel: string) => {
    switch (sentimentLabel?.toLowerCase()) {
      case 'positive':
      case 'positive_sentiment':
        return <Smile className="w-4 h-4 text-green-500" title="긍정적" />
      case 'negative':  
      case 'negative_sentiment':
        return <Frown className="w-4 h-4 text-red-500" title="부정적" />
      case 'neutral':
      case 'neutral_sentiment':
        return <Meh className="w-4 h-4 text-gray-500" title="중립적" />
      default:
        return null
    }
  }

  const canAccessCategory = (categoryName: string) => {
    if (categoryName === '기업게시판') {
      return user?.userType !== 'GENERAL'
    }
        if (categoryName === '면접정보') {
      return user?.userType !== 'COMPANY'
    }

    // 공지는 모든 사용자가 볼 수 있음 (읽기 권한)
    return true
  }

  const canCreatePost = (categoryName?: string) => {
    if (!categoryName) return true
    
    if (categoryName === '기업게시판') {
      return user?.userType !== 'GENERAL'
    }
    if (categoryName === '공지') {
      return user?.userType === 'ADMIN'
    }
    return true
  }

  const filteredCategories = categoriesData?.data?.filter(category => 
    canAccessCategory(category.name)
  ) || []

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">커뮤니티</h1>
          <p className="text-gray-600">동료들과 정보를 공유하고 소통하세요!</p>
        </div>
        {canCreatePost() && (
          <Link to="/community/new" className="btn-primary">
            <Plus className="w-5 h-4 mr-" />
            글 작성
          </Link>
        )}
      </div>

      {/* 검색 및 필터 */}
      <div className="space-y-4">
        {/* 검색 바 */}
        <div className="relative">
          <div className="absolute inset-y-0 left-0 pl-3 flex items-center">
            <Search className="h-5 w-5 text-gray-400" />
          </div>
          <input
            type="text"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="input pl-10"
            placeholder="제목, 내용으로 검색"
          />
        </div>

        {/* 카테고리 탭 */}
        <div className="flex space-x-1 bg-gray-100 p-1 rounded-lg overflow-x-auto">
          <button
            onClick={() => setSelectedCategory(null)}
            className={`px-4 py-2 rounded-lg text-sm font-medium whitespace-nowrap transition-colors ${
              selectedCategory === null
                ? 'bg-white text-primary-600 shadow-sm'
                : 'text-gray-600 hover:text-gray-900'
            }`}
          >
            전체
          </button>
          {filteredCategories.map((category) => (
            <button
              key={category.id}
              onClick={() => setSelectedCategory(category.id)}
              className={`px-4 py-2 rounded-lg text-sm font-medium whitespace-nowrap transition-colors ${
                selectedCategory === category.id
                  ? 'bg-white text-primary-600 shadow-sm'
                  : 'text-gray-600 hover:text-gray-900'
              }`}
            >
              {category.name}
            </button>
          ))}
        </div>
      </div>

      {/* 게시글 목록 */}
      <div className="space-y-4">
        {postsData?.data?.content
          .filter((post) => {
            // 일반 사용자는 기업게시판 글을 볼 수 없음
            if (user?.userType === 'GENERAL' && post.categoryName === '기업게시판') {
              return false;
            }
                       if (user?.userType === 'COMPANY' && post.categoryName === '면접정보') {
              return false;
            }
 
            return true;
          })
          .map((post) => (
          <Link
            key={post.id}
            to={`/community/${post.id}`}
            className="block card hover:shadow-md transition-shadow"
          >
            <div className="card-content">
              <div className="flex justify-between items-start mb-3">
                <div className="flex-1">
                  <div className="flex items-center gap-2 mb-1">
                    <h3 className="text-lg font-semibold text-gray-900">
                      {post.isNoticePost && (
                        <span className="inline-block px-2 py-1 mr-2 bg-red-100 text-red-800 text-xs rounded-full">
                          공지
                        </span>
                      )}
                      {post.title}
                    </h3>
                    {post.sentimentLabel && getSentimentIcon(post.sentimentLabel)}
                  </div>
                  <p className="text-gray-600 text-sm line-clamp-2">
                    {post.content}
                  </p>
                  {post.imageUrl && (
                    <div className="mt-2">
                      <img 
                        src={post.imageUrl.startsWith('/') ? `http://localhost:8001${post.imageUrl}` : post.imageUrl} 
                        alt="게시글 이미지"
                        className="max-w-xs max-h-32 rounded-lg object-cover"
                      />
                    </div>
                  )}
                </div>
                <div className="ml-4 flex flex-col gap-1">
                  {post.categoryName && (
                    <span className="px-2 py-1 bg-blue-100 text-blue-800 text-xs rounded-full whitespace-nowrap">
                      {post.categoryName}
                    </span>
                  )}
                  {post.isPinned && (
                    <span className="px-2 py-1 bg-yellow-100 text-yellow-800 text-xs rounded-full whitespace-nowrap">
                      고정
                    </span>
                  )}
                </div>
              </div>

              <div className="flex items-center justify-between text-sm text-gray-500">
                <div className="flex items-center space-x-4">
                  <span className="font-medium">{post.authorName}</span>
                  <div className="flex items-center space-x-1">
                    <Clock className="w-4 h-4" />
                    <span>{formatDate(post.createdAt)}</span>
                  </div>
                </div>

                <div className="flex items-center space-x-4">
                  <div className="flex items-center space-x-1">
                    <Eye className="w-4 h-4" />
                    <span>{post.viewCount}</span>
                  </div>
                  <div className="flex items-center space-x-1">
                    <MessageCircle className="w-4 h-4" />
                    <span>{post.commentCount}</span>
                  </div>
                  <div className="flex items-center space-x-1">
                    <Heart className="w-4 h-4" />
                    <span>{post.likeCount}</span>
                  </div>
                </div>
              </div>
            </div>
          </Link>
        ))}

        {postsData?.data?.content
          .filter((post) => {
            // 일반 사용자는 기업게시판 글을 볼 수 없음
            if (user?.userType === 'GENERAL' && post.categoryName === '기업게시판') {
              return false;
            }
                      if (user?.userType === 'COMPANY' && post.categoryName === '면접정보') {
              return false;
            }
  
            return true;
          }).length === 0 && (
          <div className="text-center py-12">
            <div className="text-gray-500 mb-2">게시글이 없습니다</div>
            <p className="text-sm text-gray-400">첫 번째 글을 작성해보세요!</p>
          </div>
        )}
      </div>

      {/* 페이지네이션 */}
      {postsData?.data && postsData.data.totalPages > 1 && (
        <div className="flex justify-center space-x-2">
          <button
            onClick={() => setPage(Math.max(0, page - 1))}
            disabled={page === 0}
            className="px-4 py-2 border border-gray-300 rounded-lg disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
          >
            이전
          </button>
          <span className="px-4 py-2 text-sm text-gray-700">
            {page + 1} / {postsData.data.totalPages}
          </span>
          <button
            onClick={() => setPage(Math.min(postsData.data?.totalPages ? postsData.data.totalPages - 1 : 0, page + 1))}
            disabled={page >= (postsData.data?.totalPages ? postsData.data.totalPages - 1 : 0)}
            className="px-4 py-2 border border-gray-300 rounded-lg disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
          >
            다음
          </button>
        </div>
      )}
    </div>
  )
}