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
  position: relative;
}

.gallery-navigation {
  display: flex;
  padding: 0 24px;
  position: absolute;
  top: -30px; /* 进一步向上调整位置 */
  left: 0;
  right: 0;
  z-index: 100; /* 大幅提高z-index确保在所有元素上方 */
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
  background-color: var(--card-bg);
  border-radius: 8px 8px 0 0;
  box-shadow: 0 -2px 6px rgba(0, 0, 0, 0.1);
  transform: translateY(0); /* 确保没有向下位移 */
}

.nav-item.active {
  color: var(--primary-color);
  background-color: var(--primary-light);
  border-bottom: 2px solid var(--primary-color);
  z-index: 101; /* 确保活动标签在最上层 */
}

.nav-item .el-icon {
  margin-right: 8px;
  font-size: 16px;
}

.gallery-content {
  flex: 1;
  overflow: auto;
  padding: 16px 24px;
  margin-top: 48px; /* 为导航腾出空间 */
}

/* 响应式设计 */
@media (max-width: 768px) {
  .gallery-navigation {
    padding: 0 16px;
    top: -30px; /* 保持与主样式一致 */
  }
  
  .nav-item {
    padding: 10px 12px;
    font-size: 14px;
    transform: translateY(0); /* 移除向下位移 */
  }
  
  .nav-item.active {
    transform: translateY(0); /* 移除向下位移和缩放 */
    z-index: 101; /* 保持与主样式一致 */
  }
  
  .gallery-content {
    padding: 12px 16px;
    margin-top: 42px;
  }
}

@media (max-width: 480px) {
  .gallery-navigation {
    top: -30px; /* 保持与主样式一致 */
  }
  
  .nav-item {
    padding: 8px 10px;
    margin-right: 4px;
    transform: translateY(0); /* 移除向下位移 */
  }
  
  .nav-item.active {
    transform: translateY(0); /* 移除向下位移和缩放 */
    z-index: 101; /* 保持与主样式一致 */
  }
  
  .nav-item .el-icon {
    margin-right: 4px;
  }
  
  .gallery-content {
    margin-top: 38px; /* 调整小屏幕的间距 */
  }
}
</style>