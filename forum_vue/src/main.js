// Vue 應用程式進入點
// ============================================================
// 註冊順序：
//   1. createApp(App)  生 root instance
//   2. use(createPinia()) 狀態管理
//   3. use(router)        路由
//   4. use(ElementPlus)   UI 套件（雖然有 auto-import 元件，主題 / locale 還是要這行）
//   5. mount('#app')      掛到 index.html 的 <div id="app">

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import zhTw from 'element-plus/es/locale/lang/zh-tw'
import 'element-plus/dist/index.css'

import App from './App.vue'
import router from './router'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.use(ElementPlus, { locale: zhTw })
app.mount('#app')
