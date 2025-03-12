<template>
  <div class="history-gallery-view">
    <el-container class="gallery-container">
      <el-header class="gallery-header">
        <h2>历史图片库</h2>
        <div class="header-actions">
          <el-button type="danger" size="small" @click="confirmClearAll" :disabled="!imageHistory.length">
            <el-icon><Delete /></el-icon>清空全部
          </el-button>
        </div>
      </el-header>
      
      <el-main class="gallery-main">
        <!-- 空状态 -->
        <div v-if="!imageHistory.length" class="empty-gallery">
          <el-icon :size="64"><Picture /></el-icon>
          <p>暂无历史图片</p>
          <el-button type="primary" @click="goToDrawing">去创作</el-button>
        </div>
        
        <!-- 图片网格 -->
        <div v-else class="gallery-grid">
          <el-row :gutter="16">
            <el-col v-for="(img, index) in imageHistory" :key="index" :xs="24" :sm="12" :md="8" :lg="6" :xl="4">
              <el-card class="gallery-item" shadow="hover">
                <div class="gallery-image-wrapper">
                  <el-image 
                    :src="img.imageUrl" 
                    fit="cover"
                    :preview-src-list="imageHistory.map(item => item.imageUrl)" 
                    :initial-index="index"
                  >
                    <template #error>
                      <div class="image-error">
                        <el-icon><Picture /></el-icon>
                      </div>
                    </template>
                  </el-image>
                </div>
                <div class="gallery-item-info">
                  <div class="creation-time">
                    <el-tooltip :content="new Date(img.timestamp).toLocaleString()" placement="top">
                      <span>{{ formatTime(img.timestamp) }}</span>
                    </el-tooltip>
                  </div>
                  <div class="image-meta">
                    <span class="image-size" v-if="img.params">{{ img.params.width }}×{{ img.params.height }}</span>
                    <span class="image-model" v-if="img.params">{{ img.params.model }}</span>
                  </div>
                  <div class="item-actions">
                    <el-button size="small" type="primary" @click="downloadImage(img)" circle>
                      <el-icon><Download /></el-icon>
                    </el-button>
                    <el-button size="small" type="success" @click="useAsReference(img)" circle>
                      <el-icon><RefreshLeft /></el-icon>
                    </el-button>
                    <el-button size="small" type="danger" @click="confirmDelete(img)" circle>
                      <el-icon><Delete /></el-icon>
                    </el-button>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Download, RefreshLeft, Delete, Picture } from '@element-plus/icons-vue'
import storageService from '../services/storageService'

const router = useRouter()
const imageHistory = ref([])

// 获取历史记录
const loadHistoryImages = () => {
  imageHistory.value = storageService.getImageHistory() || []
}

// 格式化时间
const formatTime = (timestamp) => {
  const date = new Date(timestamp)
  const now = new Date()
  const diffMs = now - date
  
  // 小于1分钟
  if (diffMs < 60000) {
    return '刚刚'
  }
  
  // 小于1小时
  if (diffMs < 3600000) {
    return `${Math.floor(diffMs / 60000)}分钟前`
  }
  
  // 小于24小时
  if (diffMs < 86400000) {
    return `${Math.floor(diffMs / 3600000)}小时前`
  }
  
  // 大于24小时
  return `${date.getMonth() + 1}月${date.getDate()}日 ${date.getHours()}:${date.getMinutes().toString().padStart(2, '0')}`
}

// 下载图像
const downloadImage = (img) => {
  if (!img || !img.imageUrl) return
  
  const link = document.createElement('a')
  link.href = img.imageUrl
  link.download = `aigc-image-${Date.now()}.png`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

// 确认删除单张图片
const confirmDelete = async (img) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这张图片吗？',
      '删除图片',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const updatedHistory = imageHistory.value.filter(item => item.timestamp !== img.timestamp)
    storageService.saveImageHistory(updatedHistory)
    imageHistory.value = updatedHistory
    ElMessage.success('图片已删除')
  } catch {
    // 用户取消操作
  }
}

// 确认清空所有历史
const confirmClearAll = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要清空所有历史记录吗？此操作不可撤销。',
      '清空历史记录',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    storageService.saveImageHistory([])
    imageHistory.value = []
    ElMessage.success('历史记录已清空')
  } catch {
    // 用户取消操作
  }
}

// 使用该图片作为参考，前往绘图页面
const useAsReference = (img) => {
  router.push({
    name: 'drawing',
    params: { referenceImage: img.timestamp }
  })
}

// 前往绘图页面
const goToDrawing = () => {
  router.push({ name: 'drawing' })
}

onMounted(() => {
  loadHistoryImages()
})
</script>

<style scoped>
.history-gallery-view {
  height: 100%;
}

.gallery-container {
  height: 100%;
}

.gallery-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  border-bottom: 1px solid var(--border-color);
}

.gallery-main {
  padding: 24px;
  background-color: var(--bg-color);
}

.empty-gallery {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 60vh;
  color: var(--text-color-light);
}

.empty-gallery p {
  margin: 16px 0;
  font-size: 16px;
}

.gallery-grid {
  margin-bottom: 24px;
}

.gallery-item {
  margin-bottom: 16px;
  transition: transform 0.3s;
}

.gallery-item:hover {
  transform: translateY(-5px);
}

.gallery-image-wrapper {
  height: 200px;
  overflow: hidden;
}

.gallery-image-wrapper .el-image {
  width: 100%;
  height: 100%;
}

.image-error {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  background-color: #f5f7fa;
  color: #909399;
}

.gallery-item-info {
  padding: 12px;
}

.creation-time {
  font-size: 14px;
  color: var(--text-color);
  margin-bottom: 8px;
}

.image-meta {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  font-size: 12px;
  color: var(--text-color-light);
}

.item-actions {
  display: flex;
  justify-content: space-around;
}
</style> 