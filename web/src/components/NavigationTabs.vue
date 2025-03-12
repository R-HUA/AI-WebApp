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
      
      <button class="icon-button" @click="handleMenuSelect('gallery-images')">
        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"></path>
        </svg>
      </button>
      
      <button class="icon-button" @click="toggleSettings">
        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="12" cy="12" r="3"></circle>
          <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"></path>
        </svg>
      </button>
    </div>
  </header>
  
  <!-- 设置面板 -->
  <SettingsPanel 
    v-model="showSettings" 
    :direction="settingsPanelDirection"
    @settings-saved="handleSettingsSaved"
  />
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import SettingsPanel from './SettingsPanel.vue'

const router = useRouter()

// 状态
const showSettings = ref(false)

// 计算设置面板方向 - 根据侧边栏位置决定
const settingsPanelDirection = computed(() => {
  const sidebarPosition = localStorage.getItem('sidebar-position') || 'left'
  return sidebarPosition === 'left' ? 'rtl' : 'ltr'
})

// 打开设置面板
const toggleSettings = () => {
  showSettings.value = true
}

// 处理设置保存
const handleSettingsSaved = (settings) => {
  console.log('设置已保存:', settings)
  
  // 应用主题设置
  if (settings.uiSettings && settings.uiSettings.theme) {
    applyTheme(settings.uiSettings.theme)
  }
  
  // 应用侧边栏位置设置 - 需要刷新页面以生效
  if (settings.uiSettings && settings.uiSettings.sidebarPosition) {
    // 通过事件触发主应用更新
    window.dispatchEvent(new StorageEvent('storage', {
      key: 'sidebar-position',
      newValue: settings.uiSettings.sidebarPosition
    }))
  }
}

// 应用主题
const applyTheme = (themeName) => {
  if (themeName === 'dark') {
    document.documentElement.classList.add('dark-theme')
  } else if (themeName === 'light') {
    document.documentElement.classList.remove('dark-theme')
  } else if (themeName === 'auto') {
    // 跟随系统
    const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
    if (prefersDark) {
      document.documentElement.classList.add('dark-theme')
    } else {
      document.documentElement.classList.remove('dark-theme')
    }
  }
  
  // 通过事件触发主应用更新
  window.dispatchEvent(new StorageEvent('storage', {
    key: 'ui-theme',
    newValue: themeName
  }))
}

// 处理菜单项选择
const handleMenuSelect = (index) => {
  if (index === 'gallery') {
    // 如果点击了图库主菜单，导航到默认子页面
    router.push({ name: 'gallery-images' })
  } else {
    router.push({ name: index })
  }
}
</script>

<style scoped>
.app-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  max-width: 100%;
  overflow: hidden;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.4vw 20px;
  background-color: var(--card-bg, #fff);
  z-index: 10;
}

.header h1 {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-color);
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.icon-button {
  background: none;
  border: none;
  cursor: pointer;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.2s;
  color: var(--text-color-light);
}

.icon-button:hover {
  background-color: rgba(0, 0, 0, 0.05);
  color: var(--primary-color);
}

/* 深色主题样式 */
:deep(.dark-theme) .header {
  background-color: #252529;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

:deep(.dark-theme) .icon-button:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

/* 响应式设计 */
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