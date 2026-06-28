// 文章 API
import request from './request'

export const listArticles = (page = 1, pageSize = 10) =>
  request.get('/api/articles', { params: { page, pageSize } })

export const getArticle = (id) =>
  request.get(`/api/articles/${id}`)

export const createArticle = (data) =>
  request.post('/api/articles', data)

export const updateArticle = (id, data) =>
  request.put(`/api/articles/${id}`, data)

export const deleteArticle = (id) =>
  request.delete(`/api/articles/${id}`)
