import { useEffect, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { useAuthStore } from '@/hooks/useAuthStore'
import { apiClient } from '@/services/api'

export default function OAuthCallback() {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const { login } = useAuthStore()
  const [status, setStatus] = useState<'loading' | 'success' | 'error'>('loading')
  const [error, setError] = useState<string>('')

  useEffect(() => {
    const handleOAuthCallback = async () => {
      const code = searchParams.get('code')
      const state = searchParams.get('state')
      const error = searchParams.get('error')

      if (error) {
        setStatus('error')
        setError('OAuth 인증이 취소되었습니다.')
        setTimeout(() => navigate('/login'), 3000)
        return
      }

      if (!code) {
        setStatus('error')
        setError('OAuth 코드를 받지 못했습니다.')
        setTimeout(() => navigate('/login'), 3000)
        return
      }

      try {
        // 백엔드 OAuth callback 엔드포인트 호출
        const response = await apiClient.handleOAuth2Callback(
          code,
          state || undefined,
          window.location.origin + '/auth/callback'
        )

        if (response.success) {
          const { accessToken, refreshToken, user } = response.data
          
          // 로그인 상태 업데이트
          login(user, accessToken, refreshToken)
          
          setStatus('success')
          
          // OAuth 유저의 프로필 완성도 체크
          const isProfileIncomplete = user.oauthProvider && (
            !user.phoneNumber || 
            !user.age || 
            !user.gender || 
            !user.residenceRegion || 
            !user.desiredJob
          )
          
          if (isProfileIncomplete) {
            // 프로필이 불완전한 경우 추가 정보 입력 페이지로 이동
            setTimeout(() => navigate('/auth/complete-profile'), 2000)
          } else {
            // 프로필이 완성된 경우 대시보드로 이동
            setTimeout(() => navigate('/dashboard'), 2000)
          }
        } else {
          setStatus('error')
          setError('OAuth 인증 처리에 실패했습니다.')
          setTimeout(() => navigate('/login'), 3000)
        }
      } catch (err: any) {
        console.error('OAuth callback error:', err)
        setStatus('error')
        
        // 계정이 없는 경우 회원가입 페이지로 리다이렉트
        if (err?.response?.data?.message?.includes('계정이 없습니다') || 
            err?.response?.data?.message?.includes('회원가입을 먼저 진행해주세요')) {
          setError('계정이 없습니다. 회원가입 페이지로 이동합니다.')
          setTimeout(() => navigate('/auth/user-type'), 3000)
        } 
        // 이미 가입된 계정인 경우 로그인 페이지로 리다이렉트  
        else if (err?.response?.data?.message?.includes('이미 가입된 계정입니다') ||
                 err?.response?.data?.message?.includes('로그인을 진행해주세요')) {
          setError('이미 가입된 계정입니다. 로그인 페이지로 이동합니다.')
          setTimeout(() => navigate('/login'), 3000)
        }
        // 일반적인 OAuth 오류
        else {
          setError('OAuth 처리 중 오류가 발생했습니다.')
          setTimeout(() => navigate('/login'), 3000)
        }
      }
    }

    handleOAuthCallback()
  }, [searchParams, navigate, login])

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="max-w-md w-full space-y-8 text-center">
        <div>
          <h2 className="text-2xl font-bold text-gray-900 mb-4">
            {status === 'loading' && 'Google 로그인 처리 중...'}
            {status === 'success' && '로그인 성공!'}
            {status === 'error' && '로그인 실패'}
          </h2>
          
          {status === 'loading' && (
            <div className="flex justify-center">
              <div className="animate-spin rounded-full h-12 w-12 border-4 border-primary-200 border-t-primary-600"></div>
            </div>
          )}
          
          {status === 'success' && (
            <div className="text-green-600">
              <p>Google 로그인이 완료되었습니다.</p>
              <p className="text-sm mt-2">잠시 후 대시보드로 이동합니다...</p>
            </div>
          )}
          
          {status === 'error' && (
            <div className="text-red-600">
              <p>{error}</p>
              <p className="text-sm mt-2">잠시 후 로그인 페이지로 이동합니다...</p>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}