<template>
  <header class="header">
    <h1> </h1>
    <div class="header-actions">
      <button class="icon-button" @click="handleMenuSelect('drawing')">
        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
          <circle cx="8.5" cy="8.5" r="1.5"></circle>
          <polyline points="21 15 16 10 5 21"></polyline>
        </svg>
      </button>
      
      <button class="icon-button" @click="handleMenuSelect('gallery')">
        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"></path>
        </svg>
      </button>
      
      <button class="icon-button" @click="handleMenuSelect('settings')">
        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="12" cy="12" r="3"></circle>
          <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"></path>
        </svg>
      </button>
    </div>
  </header>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

// 处理菜单选择
const handleMenuSelect = (index) => {
  // 直接导航到对应路由
  router.push({ name: index })
}

// 应用主题
const applyTheme = (themeName) => {
  if (themeName === 'dark') {
    document.documentElement.classList.add('dark-theme')
  } else if (themeName === 'light') {
    document.documentElement.classList.remove('dark-theme')
  } else if (themeName === 'auto') {
    // 自动模式 - 根据系统偏好设置
    if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {
      document.documentElement.classList.add('dark-theme')
    } else {
      document.documentElement.classList.remove('dark-theme')
    }
  }
}

// 监听主题变化
window.addEventListener('storage', (event) => {
  if (event.key === 'ui-theme') {
    applyTheme(event.newValue)
  }
})
</script>

<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.4vw 20px;
  background-color: var(--card-bg, #fff);
  z-index: 20; /* 增加z-index确保在导航标签之上 */
  position: relative; /* 添加相对定位 */
}

.header h1 {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
  color: var(--text-color);
}

.header-actions {
  display: flex;
  gap: 10px;
}

.icon-button {
  background: none;
  border: none;
  cursor: pointer;
  padding: 8px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.2s;
}

.icon-button:hover {
  background-color: var(--hover-color);
}

.icon-button svg {
  width: 20px;
  height: 20px;
}

@media (max-width: 768px) {
  .header {
    padding: 10px 16px;
  }
  
  .header h1 {
    font-size: 16px;
  }
  
  .icon-button {
    width: 32px;
    height: 32px;
  }
}

@media (max-width: 480px) {
  .header {
    padding: 8px 12px;
  }
  
  .header-actions {
    gap: 8px;
  }
  
  .icon-button {
    width: 28px;
    height: 28px;
  }
  
  .icon-button svg {
    width: 18px;
    height: 18px;
  }
}
</style>