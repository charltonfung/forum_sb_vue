import request from './request'

export const getMe = () => request.get('/api/users/me')
