// 點讚 API
import request from './request'

export const likeArticle    = (id) => request.post(`/api/articles/${id}/like`)
export const unlikeArticle  = (id) => request.delete(`/api/articles/${id}/like`)

export const likeComment    = (id) => request.post(`/api/comments/${id}/like`)
export const unlikeComment  = (id) => request.delete(`/api/comments/${id}/like`)
