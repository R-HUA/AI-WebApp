<template>
  <div class="gallery-layout">
    <div class="gallery-navigation">
      <div 
        v-for="tab in tabs" 
        :key="tab.name"
        class="nav-item"
        :class="{ active: activeTab === tab.name }"
        @click="handleTabClick(tab)"
      >
        <el-icon v-if="tab.icon"><component :is="tab.icon" /></el-icon>
        <span>{{ tab.label }}</span>
      </div>
    </div>
    
    <div class="gallery-content">
      <router-view />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()
const activeTab = ref('images')

// 定义标签页
const tabs = [
  { name: 'images', label: '图片库', icon: 'Picture', path: 'gallery-images' },
  { name: 'history', label: '历史记录', icon: 'List', path: 'gallery-history' },
  { name: 'directories', label: '扫描目录', icon: 'FolderOpened', path: 'gallery-directories' }
]

// 处理标签页切换
const handleTabClick = (tab) => {
  activeTab.value = tab.name
  router.push({ name: tab.path })
}

// 根据当前路由设置活动标签
onMounted(() => {
  const currentRouteName = route.name
  if (currentRouteName) {
    const tabName = currentRouteName.replace('gallery-', '')
    if (tabs.some(tab => tab.name === tabName)) {
      activeTab.value = tabName
    }
  }
})
</script>

<style scoped>
.gallery-layout {
  display: flex;
  flex-direction: column;
  height: 100%;
  background-color: var(--bg-color);
}

.gallery-header {
  padding: 16px 24px;
  background-color: var(--card-bg);
  border-bottom: 1px solid var(--border-color);
}

.gallery-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: var(--text-color);
}

.gallery-navigation {
  display: flex;
  padding: 0 24px;
  background-color: var(--card-bg);
  border-bottom: 1px solid var(--border-color);
}

.nav-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  margin-right: 8px;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  color: var(--text-color-light);
  transition: all 0.3s ease;
}

.nav-item:hover {
  color: var(--primary-color);
}

.nav-item.active {
  color: var(--primary-color);
  border-bottom-color: var(--primary-color);
}

.nav-item .el-icon {
  margin-right: 8px;
  font-size: 16px;
}

.gallery-content {
  flex: 1;
  overflow: auto;
  padding: 16px 24px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .gallery-navigation {
    padding: 0 16px;
  }
  
  .nav-item {
    padding: 10px 12px;
    font-size: 14px;
  }
  
  .gallery-content {
    padding: 12px 16px;
  }
}

@media (max-width: 480px) {
  .gallery-header {
    padding: 12px 16px;
  }
  
  .gallery-header h2 {
    font-size: 18px;
  }
  
  .nav-item {
    padding: 8px 10px;
    margin-right: 4px;
  }
  
  .nav-item .el-icon {
    margin-right: 4px;
  }
}
</style>