// Vite 設定
// ============================================================
// 重點：
//   1. server.proxy 把 /api 轉發到 Spring Boot（8090），開發時不需要處理 CORS
//      （生產環境就靠 SecurityConfig 的 CORS 白名單 + nginx 反向代理）
//   2. unplugin-auto-import + unplugin-vue-components 自動 import Element Plus，
//      不用每個檔案 import { ElButton } from 'element-plus'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import path from 'node:path'

export default defineConfig({
  plugins: [
    vue(),
    AutoImport({ resolvers: [ElementPlusResolver()] }),
    Components({ resolvers: [ElementPlusResolver()] })
  ],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  server: {
    host: '0.0.0.0',    // 讓 container 內的 Vite 能被 host 連到（Docker 必須）
    port: 5173,
    proxy: {
      // 前端打 /api/* → 自動轉到後端
      // 本機跑：BACKEND_URL 沒設 → localhost:8090
      // Docker 跑：docker-compose 設 BACKEND_URL=http://backend:8090
      '/api': {
        target: process.env.BACKEND_URL || 'http://localhost:8090',
        changeOrigin: true
      }
    }
  }
})
