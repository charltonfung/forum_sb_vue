// 使用者相關 API（已登入者自己編輯自己資料）
import request from './request'

export const getMe = () => request.get('/api/users/me')

/** 更新個人資料（目前只開放改名稱） */
export const updateProfileApi = (data) =>
  request.put('/api/users/me', data)

/** 變更密碼。成功後後端會推 credentials_changed_at → 舊 JWT 失效 → 401 → 自動踢回登入 */
export const changePasswordApi = (data) =>
  request.post('/api/users/me/password', data)

/** 申請變更 email（寄驗證信到新 email） */
export const requestEmailChangeApi = (data) =>
  request.post('/api/users/me/email-change', data)

/** 驗證 email 變更（信件連結頁呼叫，不需登入） */
export const verifyEmailChangeApi = (token) =>
  request.post('/api/users/verify-email-change', { token })
