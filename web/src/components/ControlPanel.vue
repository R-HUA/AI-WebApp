<template>
  <div class="control-panel" :class="{ 'collapsed': isCollapsed }">
    <!-- 面板头部 只在移动端显示 -->
    <div class="panel-header" @click="$emit('toggle')" v-if="isMobile">
      <span class="panel-title">生成参数</span>
      <svg class="panel-toggle" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <polyline points="18 15 12 9 6 15"></polyline>
      </svg>
    </div>
    
    <div class="panel-content">
      <div class="content-inner">
        <!-- 提示词输入 -->
        <PromptInput 
          :model-value="prompt"
          @update:model-value="$emit('update:prompt', $event)"
          :negative="false"
        />
        
        <!-- 负面提示词 -->
        <PromptInput 
          :model-value="negativePrompt"
          @update:model-value="$emit('update:negativePrompt', $event)"
          :negative="true"
        />

        <!-- 图生图上传组件 -->
        <div v-if="isImageToImage" class="image-upload param-group">
          <div class="param-group-title">上传图片</div>
          <el-upload
            class="upload-demo"
            drag
            action="#"
            :auto-upload="false"
            :show-file-list="true"
            :on-change="handleImageChange"
          >
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">
              拖拽文件到此处或 <em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                支持 jpg/png 文件，且不超过 10MB
              </div>
            </template>
          </el-upload>
        </div>
          
        <ImageParams 
          :model-value="imageParams"
          @update:model-value="handleImageParamsUpdate"
          :available-models="availableModels"
          :available-samplers="availableSamplers"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { UploadFilled } from '@element-plus/icons-vue'
import PromptInput from './PromptInput.vue'
import ImageParams from './ImageParams.vue'

const props = defineProps({
  prompt: {
    type: String,
    required: true
  },
  negativePrompt: {
    type: String,
    required: true
  },
  imageParams: {
    type: Object,
    required: true
  },
  availableModels: {
    type: Array,
    default: () => []
  },
  availableSamplers: {
    type: Array,
    default: () => []
  },
  isCollapsed: {
    type: Boolean,
    default: true
  },
  workflow: {
    type: String,
    required: true
  }
})

const emit = defineEmits([
  'update:prompt',
  'update:negativePrompt',
  'update:imageParams',
  'toggle',
  'update:initImage'
])

const isImageToImage = computed(() => props.workflow === 'img2img')

// 添加移动端检测的计算属性
const isMobile = computed(() => {
  return window.innerWidth <= 768
})

// 处理图片上传
const handleImageChange = (file) => {
  if (file) {
    emit('update:initImage', file.raw)
  }
}

// 处理图像参数更新
const handleImageParamsUpdate = (newParams) => {
  emit('update:imageParams', newParams)
}
</script>

<style scoped>
.control-panel {
  display: flex;
  flex-direction: column;
  background-color: #fff;
  height: 100%;
  overflow: hidden;
  border-radius: 8px 8px 0 0;
  transition: all 0.3s ease-in-out;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  cursor: pointer;
  border-bottom: 1px solid var(--border-color);
  background-color: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
  position: sticky;
  top: 0;
  z-index: 2;
}

.panel-title {
  font-weight: 600;
  color: var(--text-color);
  font-size: 16px;
}

.panel-toggle {
  transition: transform 0.3s;
  transform: rotate(180deg);
}

.collapsed .panel-toggle {
  transform: rotate(0deg);
}

.panel-content {
  overflow: hidden;
  flex: 1;
  height: calc(100% - 52px);
  transition: all 0.3s ease-in-out;
  position: relative;
}

.content-inner {
  padding: 15px;
  height: 100%;
  overflow-y: auto;
  
  /* 自定义滚动条样式 */
  scrollbar-width: thin; /* Firefox */
  scrollbar-color: rgba(144, 147, 153, 0.3) transparent; /* Firefox */
}

/* Webkit浏览器的滚动条样式 (Chrome, Safari, Edge等) */
.content-inner::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

.content-inner::-webkit-scrollbar-thumb {
  background-color: rgba(144, 147, 153, 0.3);
  border-radius: 3px;
}

.content-inner::-webkit-scrollbar-thumb:hover {
  background-color: rgba(144, 147, 153, 0.5);
}

.content-inner::-webkit-scrollbar-track {
  background: transparent;
}

/* 深色主题下的滚动条样式 */
:deep(.dark-theme) .content-inner {
  scrollbar-color: rgba(100, 100, 100, 0.5) rgba(30, 30, 30, 0.1); /* Firefox */
}

:deep(.dark-theme) .content-inner::-webkit-scrollbar-thumb {
  background-color: rgba(100, 100, 100, 0.5);
}

:deep(.dark-theme) .content-inner::-webkit-scrollbar-thumb:hover {
  background-color: rgba(120, 120, 120, 0.7);
}

:deep(.dark-theme) .content-inner::-webkit-scrollbar-track {
  background: rgba(30, 30, 30, 0.1);
}

/* 深色主题下的控制面板背景 */
:deep(.dark-theme) .control-panel,
:deep(.dark-theme) .panel-content,
:deep(.dark-theme) .panel-header,
:deep(.dark-theme) .content-inner {
  background-color: #252529;
  color: var(--text-color);
  border-color: var(--border-color);
}

:deep(.dark-theme) .panel-header {
  background-color: rgba(37, 37, 41, 0.9);
  border-bottom-color: var(--border-color);
}

:deep(.dark-theme) .panel-header::before {
  background-color: rgba(255, 255, 255, 0.2);
}

.param-group {
  margin-bottom: 16px;
}

.param-group-title {
  font-weight: 600;
  margin-bottom: 8px;
  color: var(--text-color-secondary);
  font-size: 14px;
}

.image-upload {
  margin: 15px 0;
}

.upload-demo {
  width: 100%;
}

.el-upload {
  width: 100%;
}

.el-upload-dragger {
  width: 100%;
}

.el-upload__tip {
  color: var(--text-color-secondary);
  font-size: 12px;
  margin-top: 5px;
}

@media (max-width: 768px) {
  .control-panel {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    z-index: 100;
    height: auto;
    max-height: 80vh;
    transition: max-height 0.3s ease-in-out, transform 0.3s ease-in-out, bottom 0.3s ease-in-out;
    border-radius: 6px 6px 0 0;
    background-color: var(--bg-color);
    box-shadow: 0 -1px 10px 0 rgba(0, 0, 0, 0.1)
  }
  
  .control-panel.collapsed {
    max-height: 52px; /* 只显示面板头部 */
    transform: translateY(calc(100% - 52px)); /* 只露出面板头部 */
    bottom: 60px; /* 避免被底部导航栏遮挡 */
  }
  
  .panel-content {
    height: auto;
    max-height: calc(80vh - 52px);
  }
  
  .content-inner {
    max-height: calc(80vh - 52px);
    padding: 15px 15px 70px 15px; /* 增加底部内边距，防止内容被底部工具栏遮挡 */
  }
  
  .panel-header {
    border-radius: 12px 12px 0 0;
    position: relative;
    z-index: 3;
    background-color: var(--bg-color);
  }
}
</style> 