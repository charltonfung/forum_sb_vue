// 驗證相關 API
import request from './request'

export const registerApi = (data) =>
  request.post('/api/auth/register', data)

export const loginApi = (data) =>
  request.post('/api/auth/login', data)

export const forgotPasswordApi = (email) =>
  request.post('/api/auth/forgot-password', { email })

export const resetPasswordApi = (data) =>
  request.post('/api/auth/reset-password', data)
