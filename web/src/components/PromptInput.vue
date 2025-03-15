<script setup>
import { ref, computed } from 'vue'
import { Delete } from '@element-plus/icons-vue'
import storageService from '../services/storageService'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  negative: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue'])

const inputValue = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const promptHistory = ref([])
const showHistory = ref(false)

// 加载提示词历史
const loadPromptHistory = () => {
  promptHistory.value = storageService.getPromptHistory()
}

// 使用历史提示词
const useHistoryPrompt = (prompt) => {
  inputValue.value = prompt
  showHistory.value = false
}

// 移除历史提示词
const removePromptFromHistory = (index, event) => {
  event.stopPropagation()
  promptHistory.value.splice(index, 1)
  storageService.savePromptHistory(promptHistory.value)
}

// 初始化
loadPromptHistory()
</script>

<template>
  <div class="prompt-input-component">
    <div class="input-header">
      <div class="input-title">
        <span>{{ negative ? '负面提示词' : '提示词' }}</span>
        <!-- <el-tooltip content="点击显示历史提示词" placement="top">
          <el-badge :value="promptHistory.length" :hidden="promptHistory.length === 0" type="info">
            <el-button 
              type="primary" 
              size="small" 
              :icon="CollectionTag" 
              circle 
              plain
              @click="showHistory = !showHistory"
            />
          </el-badge>
        </el-tooltip> -->
      </div>
      <div class="input-actions">
        <el-button text @click="inputValue = ''" :icon="Delete"></el-button>
      </div>
    </div>
    
    <el-input
      v-model="inputValue"
      type="textarea"
      :rows="4"
      :placeholder="negative ? '输入想要避免在图像中出现的元素...' : '输入详细描述你想要生成的图像...'"
      resize="none"
      spellcheck="false"
    />
    
    <!-- 提示词历史 -->
    <div v-if="showHistory && promptHistory.length > 0" class="prompt-history">
      <div class="history-header">
        <h4>提示词历史</h4>
      </div>
      <div class="history-list">
        <div 
          v-for="(prompt, index) in promptHistory" 
          :key="index"
          class="history-item"
          @click="useHistoryPrompt(prompt)"
        >
          <div class="prompt-text">{{ prompt }}</div>
          <el-button 
            type="danger" 
            size="small" 
            icon="Delete" 
            circle 
            plain
            @click="removePromptFromHistory(index, $event)"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.prompt-input-component {
  position: relative;
  margin-bottom: 20px;
}

.input-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  padding: 0 2px;
}

.input-title {
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-color);
  font-size: 14px;
}

.input-title :deep(.el-badge__content) {
  z-index: 1;
}

.input-actions {
  display: flex;
  gap: 8px;
}

.input-actions :deep(.el-button) {
  padding: 4px 8px;
  height: 28px;
  font-size: 13px;
}

.input-actions :deep(.el-button .el-icon) {
  margin-right: 4px;
  font-size: 14px;
}

.prompt-input-component :deep(.el-textarea__inner) {
  min-height: 100px;
  padding: 8px 12px;
  font-size: 14px;
  line-height: 1.6;
  border-radius: 4px;
  transition: all 0.3s ease;
}

.prompt-input-component :deep(.el-textarea__inner:focus) {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px var(--primary-light);
}

.prompt-history {
  position: absolute;
  top: calc(100% + 5px);
  left: 0;
  right: 0;
  background: white;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  z-index: 10;
  max-height: 240px;
  overflow-y: auto;
}

.history-header {
  padding: 12px 16px;
  border-bottom: 1px solid var(--border-color);
  background-color: var(--bg-color);
  position: sticky;
  top: 0;
  z-index: 1;
}

.history-header h4 {
  margin: 0;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-color);
}

.history-list {
  padding: 8px;
}

.history-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.history-item:hover {
  background-color: var(--primary-light);
}

.history-item :deep(.el-button) {
  padding: 4px;
  height: 24px;
  width: 24px;
}

.prompt-text {
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-right: 8px;
  font-size: 13px;
  color: var(--text-color);
}

/* 自定义滚动条样式 */
.prompt-history::-webkit-scrollbar {
  width: 6px;
}

.prompt-history::-webkit-scrollbar-track {
  background: var(--bg-color);
  border-radius: 3px;
}

.prompt-history::-webkit-scrollbar-thumb {
  background: var(--border-color);
  border-radius: 3px;
}

.prompt-history::-webkit-scrollbar-thumb:hover {
  background: var(--text-color-light);
}
</style> 