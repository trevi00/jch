import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useParams, Link, useNavigate } from 'react-router-dom'
import { 
  ArrowLeft, 
  Heart, 
  MessageCircle, 
  Eye, 
  Clock,
  Edit,
  Trash2
} from 'lucide-react'
import { apiClient } from '@/services/api'
import { useAuthStore } from '@/hooks/useAuthStore'

export default function PostDetail() {
  const { id } = useParams<{ id: string }>()
  const { user } = useAuthStore()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [newComment, setNewComment] = useState('')

  const { data: postData, isLoading } = useQuery({
    queryKey: ['post', id],
    queryFn: () => apiClient.getPost(Number(id)),
    enabled: !!id,
  })

  const { data: commentsData } = useQuery({
    queryKey: ['comments', id],
    queryFn: () => apiClient.getComments(Number(id)),
    enabled: !!id,
  })

  const likeMutation = useMutation({
    mutationFn: () => apiClient.likePost(Number(id)!),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['post', id] })
    },
  })

  const commentMutation = useMutation({
    mutationFn: (content: string) =>
      apiClient.createComment({
        postId: Number(id)!,
        content,
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['comments', id] })
      setNewComment('')
    },
  })

  const deletePostMutation = useMutation({
    mutationFn: () => apiClient.deletePost(Number(id)!),
    onSuccess: () => {
      alert('게시글이 삭제되었습니다.')
      navigate('/community')
    },
    onError: (error: any) => {
      console.error('Delete error:', error)
      alert(error?.response?.data?.message || '게시글 삭제에 실패했습니다.')
    },
  })

  const deleteCommentMutation = useMutation({
    mutationFn: (commentId: number) => apiClient.deleteComment(commentId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['comments', id] })
    },
  })

  const post = postData?.data
  const comments = commentsData?.data

  if (isLoading) {
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
        <Link to="/community" className="text-primary-600 hover:text-primary-500">
          커뮤니티로 돌아가기
        </Link>
      </div>
    )
  }

  // 일반 사용자가 기업게시판 글에 접근하려고 할 때 차단
  if (user?.userType === 'GENERAL' && post.categoryName === '기업게시판') {
    return (
      <div className="text-center py-12">
        <div className="text-red-500 mb-2">접근 권한이 없습니다</div>
        <p className="text-gray-600 mb-4">기업게시판은 기업 회원만 이용할 수 있습니다.</p>
        <Link to="/community" className="text-primary-600 hover:text-primary-500">
          커뮤니티로 돌아가기
        </Link>
      </div>
    )
  }

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

  const handleLike = () => {
    likeMutation.mutate()
  }

  const handleCommentSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    if (!newComment.trim()) return
    commentMutation.mutate(newComment.trim())
  }

  const handleDeletePost = async () => {
    if (window.confirm('정말로 이 게시글을 삭제하시겠습니까?')) {
      console.log('Attempting to delete post:', id)
      try {
        deletePostMutation.mutate()
      } catch (error) {
        console.error('Delete post error:', error)
      }
    }
  }

  const handleDeleteComment = (commentId: number) => {
    if (window.confirm('정말로 이 댓글을 삭제하시겠습니까?')) {
      deleteCommentMutation.mutate(commentId)
    }
  }

  const isPostAuthor = user?.id === post.authorId

  // 디버깅용 로그
  console.log('Debug PostDetail:', {
    userId: user?.id,
    postAuthorId: post?.authorId,
    userEmail: user?.email,
    isPostAuthor,
    user: user,
    post: post
  })

  const getSentimentEmoji = (sentimentLabel: string | undefined) => {
    switch (sentimentLabel?.toLowerCase()) {
      case 'positive':
        return '😊'
      case 'negative':
        return '😢'
      case 'neutral':
      default:
        return '😐'
    }
  }

  const getSentimentColor = (sentimentLabel: string | undefined) => {
    switch (sentimentLabel?.toLowerCase()) {
      case 'positive':
        return 'text-green-600 bg-green-50 border-green-200'
      case 'negative':
        return 'text-red-600 bg-red-50 border-red-200'
      case 'neutral':
      default:
        return 'text-gray-600 bg-gray-50 border-gray-200'
    }
  }

  const getSentimentText = (sentimentLabel: string | undefined) => {
    switch (sentimentLabel?.toLowerCase()) {
      case 'positive':
        return '긍정적'
      case 'negative':
        return '부정적'
      case 'neutral':
      default:
        return '중립적'
    }
  }

  return (
    <div className="space-y-6">
      {/* 헤더 */}
      <div className="flex items-center justify-between">
        <div className="flex items-center space-x-4">
          <Link
            to="/community"
            className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
          >
            <ArrowLeft className="w-5 h-5" />
          </Link>
          <div>
            <div className="flex items-center space-x-2">
              {post.categoryName && (
                <span className="px-2 py-1 bg-blue-100 text-blue-800 text-xs rounded-full">
                  {post.categoryName}
                </span>
              )}
            </div>
          </div>
        </div>
        {isPostAuthor && (
          <div className="flex space-x-2">
            <Link
              to={`/community/edit/${post.id}`}
              className="p-2 text-gray-600 hover:text-gray-800 hover:bg-gray-100 rounded-lg transition-colors"
            >
              <Edit className="w-5 h-5" />
            </Link>
            <button
              onClick={handleDeletePost}
              className="p-2 text-red-600 hover:text-red-800 hover:bg-red-50 rounded-lg transition-colors"
            >
              <Trash2 className="w-5 h-5" />
            </button>
          </div>
        )}
      </div>

      {/* 게시글 내용 */}
      <div className="card">
        <div className="card-content">
          <div className="flex items-center justify-between mb-4">
            <h1 className="text-2xl font-bold text-gray-900">{post.title}</h1>
            {post.sentimentLabel && (
              <div className={`flex items-center space-x-2 px-3 py-1 rounded-full border text-sm font-medium ${getSentimentColor(post.sentimentLabel)}`}>
                <span className="text-lg">{getSentimentEmoji(post.sentimentLabel)}</span>
                <span>{getSentimentText(post.sentimentLabel)}</span>
                {post.sentimentScore !== undefined && (
                  <span className="text-xs opacity-75">
                    ({Math.round(Math.abs(post.sentimentScore) * 100)}%)
                  </span>
                )}
              </div>
            )}
          </div>
          
          <div className="flex items-center justify-between text-sm text-gray-500 mb-6 pb-6 border-b border-gray-200">
            <div className="flex items-center space-x-4">
              <span className="font-medium text-gray-700">{post.authorName}</span>
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
            </div>
          </div>

          {post.imageUrl && (
            <div className="mb-6">
              <img 
                src={post.imageUrl.startsWith('/') ? `http://localhost:8001${post.imageUrl}` : post.imageUrl} 
                alt={`${post.title} 이미지`}
                className="w-full max-w-2xl mx-auto rounded-lg shadow-md"
                onError={(e) => {
                  e.currentTarget.style.display = 'none'
                }}
              />
            </div>
          )}

          <div className="prose max-w-none">
            <div className="whitespace-pre-line text-gray-800">
              {post.content}
            </div>
          </div>

          <div className="flex items-center justify-between pt-6 mt-6 border-t border-gray-200">
            <button
              onClick={handleLike}
              className={`flex items-center space-x-2 px-4 py-2 rounded-lg transition-colors ${
                post.isLiked
                  ? 'bg-red-50 text-red-600'
                  : 'bg-gray-50 text-gray-600 hover:bg-red-50 hover:text-red-600'
              }`}
            >
              <Heart className={`w-5 h-5 ${post.isLiked ? 'fill-current' : ''}`} />
              <span>{post.likeCount}</span>
            </button>
          </div>
        </div>
      </div>

      {/* 댓글 섹션 */}
      <div className="card">
        <div className="card-header">
          <h2 className="text-lg font-semibold flex items-center">
            <MessageCircle className="w-5 h-5 mr-2" />
            댓글 {comments?.length || 0}개
          </h2>
        </div>
        <div className="card-content">
          {/* 댓글 작성 폼 */}
          {user && (
            <form onSubmit={handleCommentSubmit} className="mb-6">
              <div className="flex space-x-3">
                <div className="flex-1">
                  <textarea
                    value={newComment}
                    onChange={(e) => setNewComment(e.target.value)}
                    className="input resize-none"
                    rows={3}
                    placeholder="댓글을 작성해주세요..."
                  />
                </div>
                <button
                  type="submit"
                  disabled={!newComment.trim() || commentMutation.isPending}
                  className="btn-primary self-end px-6 disabled:opacity-50"
                >
                  {commentMutation.isPending ? '작성 중...' : '댓글 작성'}
                </button>
              </div>
            </form>
          )}

          {/* 댓글 목록 */}
          <div className="space-y-4">
            {comments?.map((comment) => (
              <div key={comment.id} className="border-l-2 border-gray-100 pl-4">
                <div className="flex items-start justify-between">
                  <div className="flex-1">
                    <div className="flex items-center space-x-2 mb-2">
                      <span className="font-medium text-gray-900">{comment.authorName}</span>
                      <span className="text-sm text-gray-500">{formatDate(comment.createdAt)}</span>
                    </div>
                    <div className="text-gray-800 whitespace-pre-line">
                      {comment.content}
                    </div>
                  </div>
                  {user?.id === comment.authorId && (
                    <button
                      onClick={() => handleDeleteComment(comment.id)}
                      className="ml-2 p-1 text-gray-400 hover:text-red-600 transition-colors"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  )}
                </div>
              </div>
            ))}

            {comments?.length === 0 && (
              <div className="text-center py-8 text-gray-500">
                아직 댓글이 없습니다. 첫 번째 댓글을 작성해보세요!
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}