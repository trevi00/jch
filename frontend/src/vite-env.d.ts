/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_API_BASE_URL: string
  readonly VITE_AI_SERVICE_BASE_URL: string
  readonly VITE_APP_TITLE: string
  readonly VITE_APP_DESCRIPTION: string
  readonly VITE_APP_VERSION: string
  readonly VITE_ENABLE_DEBUG: string
  readonly VITE_ENABLE_DEV_TOOLS: string
  readonly VITE_ENABLE_ERROR_REPORTING: string
  readonly VITE_API_TIMEOUT: string
  readonly VITE_AI_API_TIMEOUT: string
  readonly VITE_RETRY_COUNT: string
  readonly VITE_CACHE_TIME: string
  readonly VITE_GOOGLE_CLIENT_ID: string
  readonly VITE_OAUTH_REDIRECT_URL: string
  readonly VITE_ANALYTICS_ID: string
  readonly VITE_SENTRY_DSN: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}