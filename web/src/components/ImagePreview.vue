<script setup>
import { ref, watch } from 'vue'
import { Delete, Picture } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'

const props = defineProps({
  generatedImage: {
    type: String,
    default: null
  },
  imageHistory: {
    type: Array,
    default: () => []
  },
  isGenerating: {
    type: Boolean,
    default: false
  },
  generationProgress: {
    type: Number,
    default: 0
  },
  imageParams: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['selectHistoryImage', 'clearHistory', 'retry'])

// 图片加载状态
const imageLoading = ref(true)
const handleImageLoad = () => {
  imageLoading.value = false
}

// 重置加载状态
const resetImageLoading = () => {
  imageLoading.value = true
}

// 监听图片变化
watch(() => props.generatedImage, () => {
  if (props.generatedImage) {
    resetImageLoading()
  }
})

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
  return `${date.getMonth() + 1}月${date.getDate()}日`
}

// 清空历史记录
const confirmClearHistory = async () => {
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
    emit('clearHistory')
  } catch {
    // 用户取消操作
  }
}
</script>

<template>
  <div class="image-preview-component">
    <div class="image-container" :class="{ 'loading': isGenerating || imageLoading }">
      <!-- 空状态 -->
      <div v-if="!generatedImage && !isGenerating" class="empty-image">
        <el-icon :size="64"><Picture /></el-icon>
        <p> </p>
      </div>
      
      <!-- 加载状态 -->
      <div v-if="isGenerating" class="generating-indicator">
        <el-progress 
          type="circle" 
          :percentage="generationProgress" 
          :status="generationProgress === 100 ? 'success' : ''"
        >
          <template #default="{ percentage }">
            <span class="progress-label">{{ percentage === 100 ? '处理中' : `${percentage}%` }}</span>
          </template>
        </el-progress>
        <p>正在生成图像...</p>
      </div>
      
      <!-- 图像预览 -->
      <div v-if="generatedImage && !isGenerating" class="image-display">
        <img 
          :src="generatedImage" 
          alt="生成的图像" 
          class="preview-image" 
          :class="{ 'loading': imageLoading, 'loaded': !imageLoading }"
          @load="handleImageLoad"
        />
        <div v-if="imageLoading" class="generating-indicator">
          <el-progress type="circle" :percentage="100" status="success">
            <template #default>
              <span class="progress-label">加载中</span>
            </template>
          </el-progress>
        </div>
      </div>
    </div>

    <!-- 历史记录 -->
    <div v-if="imageHistory.length > 0" class="image-history">
      <div class="history-header">
        <h3>历史记录</h3>
        <el-button text @click="confirmClearHistory" :icon="Delete"></el-button>
      </div>
      <div class="history-images-wrapper">
        <div class="history-images">
          <div 
            v-for="(img, index) in imageHistory" 
            :key="index" 
            class="history-image-item"
            @click="$emit('selectHistoryImage', img)"
          >
            <el-image 
              :src="img.imageUrl" 
              fit="cover"
            >
              <template #error>
                <div class="image-error">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
            <div class="image-item-info">
              <el-tooltip :content="new Date(img.timestamp).toLocaleString()" placement="top">
                <span>{{ formatTime(img.timestamp) }}</span>
              </el-tooltip>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.image-preview-component {
  display: flex;
  flex-direction: column;
  height: 100%;
  width: 100%;
  gap: 16px;
}

.image-container {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  /* background-color: var(--bg-color); */
  border-radius: 8px;
  overflow: hidden;
  position: relative;
  min-height: v-bind('imageHistory.length > 0 ? "calc((100vh - 100px) * 0.77)" : "calc((100vh - 100px) * 0.98)"');
  max-height: v-bind('imageHistory.length > 0 ? "calc((100vh - 100px) * 0.77)" : "calc((100vh - 100px) * 0.98)"');
  transition: height 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.empty-image {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: var(--text-color-light);
  height: 100%;
  width: 100%;
  position: absolute;
  top: 0;
  left: 0;
  transition: opacity 0.3s ease;
}

.empty-image p {
  margin-top: 16px;
}

.generating-indicator {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 2;
  transition: opacity 0.3s ease;
  background-color: var(--bg-color);
  padding: 24px;
  border-radius: 8px;
}

.progress-label {
  font-size: 14px;
  color: var(--text-color);
}

.image-display {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    opacity: 0.8;
    z-index: 1;
  }
}

.preview-image {
  position: relative;
  z-index: 2;
  max-width: 100%;
  height: 100%;
  width: auto;
  object-fit: contain;
  border-radius: 4px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  will-change: transform, opacity;
  
  &.loading {
    opacity: 0;
    transform: scale(0.95);
  }
  
  &.loaded {
    opacity: 1;
    transform: scale(1);
  }
}

.image-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border-radius: 8px;
  margin: 0;
}

.info-text {
  display: flex;
  align-items: center;
  gap: 12px;
  color: var(--text-color-light);
  font-size: 14px;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.image-history {
  margin: 0;
  padding: 16px;
  border-radius: 8px;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.history-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
  color: var(--text-color);
}

.history-images {
  display: flex;
  flex-wrap: nowrap;
  gap: 8px;
  max-height: unset;
  overflow-x: auto;
  overflow-y: hidden;
  padding: 4px 4px 12px 4px;
  scroll-behavior: smooth;
  -webkit-overflow-scrolling: touch;
  
  /* 隐藏滚动条但保持功能 */
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE and Edge */
  &::-webkit-scrollbar {
    display: none; /* Chrome, Safari, Opera */
  }
}

.history-image-item {
  flex: 0 0 100px; /* 固定宽度，不缩放 */
  aspect-ratio: 1;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  position: relative;
  border: 1px solid var(--border-color);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  
  &:hover {
    transform: translateY(-2px);
    border-color: var(--primary-color);
  }
  
  .el-image {
    width: 100%;
    height: 100%;
    
    :deep(img) {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.3s ease;
    }
    
    &:hover :deep(img) {
      transform: scale(1.05);
    }
  }
}

.image-item-info {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  color: #fff;
  font-size: 12px;
  padding: 4px;
  text-align: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  backdrop-filter: blur(4px);
}

.image-error {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-color-light);
}

/* 添加渐变遮罩，提示可以滚动 */
.history-images-wrapper {
  position: relative;
  
  
}

@media (max-width: 768px) {

  .image-container{
    min-height: v-bind('imageHistory.length > 0 ? "calc((100vh - 110px) * 0.7)" : "calc((100vh - 110px) * 0.9)"');
    max-height: v-bind('imageHistory.length > 0 ? "calc((100vh - 110px) * 0.7)" : "calc((100vh - 110px) * 0.9)"');
  }

  .image-history {
    padding: 0px;
  }

  .history-header {
    padding: 0 16px 0;
  }

  .history-images-wrapper {
    &::after {
    content: '';
    position: absolute;
    top: 4px;
    right: 0;
    bottom: 12px;
    width: 32px;
    background: linear-gradient(to right, transparent, var(--bg-color));
    pointer-events: none;
    }
  }
}
</style> 