<script setup>
import { ref, onMounted, computed } from 'vue'
import { RouterView } from 'vue-router'
import { ElConfigProvider } from 'element-plus'
import NavigationTabs from './components/NavigationTabs.vue'

const language = ref('zh-cn')

// 主题设置
const theme = computed(() => {
  return localStorage.getItem('ui-theme') || 'light'
})

// 在组件挂载时应用主题
onMounted(() => {
  applyTheme(theme.value)
  
  // 监听主题变化
  window.addEventListener('storage', (event) => {
    if (event.key === 'ui-theme') {
      applyTheme(event.newValue || 'light')
    }
  })
  
  // 监听系统主题变化
  const systemDarkQuery = window.matchMedia('(prefers-color-scheme: dark)')
  systemDarkQuery.addEventListener('change', () => {
    if (theme.value === 'auto') {
      applyTheme('auto')
    }
  })
})

// 应用主题
function applyTheme(themeName) {
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
  } else {
    // 默认为浅色主题
    document.documentElement.classList.remove('dark-theme')
  }
}
</script>

<template>
  <ElConfigProvider :locale="language">
    <div class="app-container">
      <!-- 顶部导航栏 -->
      <NavigationTabs />
      
      <!-- 主内容区域 -->
      <main class="app-main">
        <RouterView />
      </main>
    </div>
  </ElConfigProvider>
</template>

<style>
/* 全局样式 */
:root {
  --primary-color: #409eff;
  --primary-light: #ecf5ff;
  --text-color: #303133;
  --text-color-light: #606266;
  --border-color: #ebeef5;
  --bg-color: #f5f7fa;
  --card-bg: #ffffff;
  --header-height: 60px;
  --box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  --padding: 20px;
}

/* 深色主题变量 */
.dark-theme {
  --primary-color: #409eff;
  --primary-light: #18222c;
  --text-color: #e5eaf3;
  --text-color-light: #a3a6ad;
  --border-color: #414243;
  --bg-color: #1e1e20;
  --card-bg: #252529;
  --box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.2);
}

/* 全局滚动条样式 */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-thumb {
  background-color: rgba(144, 147, 153, 0.3);
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background-color: rgba(144, 147, 153, 0.5);
}

::-webkit-scrollbar-track {
  background: transparent;
}

/* 深色主题下的全局滚动条样式 */
.dark-theme ::-webkit-scrollbar-thumb {
  background-color: rgba(100, 100, 100, 0.5);
}

.dark-theme ::-webkit-scrollbar-thumb:hover {
  background-color: rgba(120, 120, 120, 0.7);
}

.dark-theme ::-webkit-scrollbar-track {
  background: rgba(30, 30, 30, 0.1);
}

/* Firefox深色主题滚动条 */
.dark-theme * {
  scrollbar-color: rgba(100, 100, 100, 0.5) rgba(30, 30, 30, 0.1);
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  scrollbar-width: thin; /* Firefox 滚动条样式 */
  scrollbar-color: rgba(144, 147, 153, 0.3) transparent; /* Firefox 滚动条颜色 */
}

body {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: var(--text-color);
  background-color: var(--bg-color);
  font-size: 14px;
  line-height: 1.5;
}

.app-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  width: 100vw;
  overflow: hidden;
}

.app-main {
  flex: 1;
  overflow: auto;
  padding: var(--padding);
  position: relative;
}

/* 响应式设计 */
@media (max-width: 768px) {
  :root {
    --padding: 12px;
  }
  
  .app-main{
    width: 100vw;
    margin: 0 !important;
    padding: 0;
  }
  .preview-container{
    width: 100vw;
    margin: 0 !important;
    padding: 0;
  }
}

@media (max-width: 480px) {
  :root {
    --padding: 8px;
  }
}

/* 深色主题相关样式 */
.dark-theme .el-button--primary {
  --el-button-bg-color: var(--primary-color);
  --el-button-border-color: var(--primary-color);
}

.dark-theme .el-card {
  --el-card-bg-color: #252529;
  --el-card-border-color: var(--border-color);
}

.dark-theme .el-input__inner {
  background-color: #252529;
  border-color: var(--border-color);
  color: var(--text-color);
}

/* 深色主题下的遮罩层 */
.dark-theme .el-overlay {
  background-color: rgba(0, 0, 0, 0.7);
}
</style>
