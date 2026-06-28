// 留言 API
import request from './request'

export const listComments = (articleId) =>
  request.get(`/api/articles/${articleId}/comments`)

export const createComment = (articleId, content) =>
  request.post(`/api/articles/${articleId}/comments`, { content })

export const deleteComment = (commentId) =>
  request.delete(`/api/comments/${commentId}`)
