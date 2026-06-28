// Axios 實例 + 攔截器
// ============================================================
// 攔截器做兩件事：
//   1. request：自動帶 Authorization header（從 Pinia store 拿 token）
//   2. response：
//      - 成功且 code=200 → 直接回 data 給呼叫端（不用再 .data.data）
//      - code 非 200    → Element Plus 跳訊息 + reject
//      - HTTP 401      → 清 token 跳回登入頁
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import router from '@/router'

const service = axios.create({
  baseURL: '/',                // 直接用相對路徑（dev 走 vite proxy，prod 走反向代理）
  timeout: 10000
})

// ---------- Request ----------
service.interceptors.request.use(
  (config) => {
    // 不要在 main.js 之外的地方 import useAuthStore 到模組頂層 — pinia 還沒準備好
    // 改在攔截器執行時才取 store
    const auth = useAuthStore()
    if (auth.token) {
      config.headers.Authorization = `Bearer ${auth.token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// ---------- Response ----------
service.interceptors.response.use(
  (response) => {
    const body = response.data
    // 後端回的格式：{ code, message, data }
    if (body && typeof body.code === 'number') {
      if (body.code === 200) {
        return body.data
      }
      ElMessage.error(body.message || '操作失敗')
      return Promise.reject(new Error(body.message || 'Error'))
    }
    return body
  },
  (error) => {
    const status = error.response?.status
    const body = error.response?.data

    if (status === 401) {
      const auth = useAuthStore()
      auth.logout()
      router.push({ name: 'login', query: { redirect: router.currentRoute.value.fullPath } })
      ElMessage.warning(body?.message || '請重新登入')
    } else if (status === 403) {
      ElMessage.error(body?.message || '沒有權限')
    } else {
      ElMessage.error(body?.message || error.message || '網路錯誤')
    }
    return Promise.reject(error)
  }
)

export default service
