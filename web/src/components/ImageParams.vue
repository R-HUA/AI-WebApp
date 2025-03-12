<script setup>
import { reactive, computed, watch } from 'vue'
import { RefreshRight, CopyDocument } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  modelValue: {
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
  }
})

const emit = defineEmits(['update:modelValue'])

// 本地状态
const localParams = reactive({ ...props.modelValue })

// 监听 props 变化，更新本地状态
watch(() => props.modelValue, (newVal) => {
  // 使用 Object.assign 而不是直接赋值，以保持响应性
  Object.assign(localParams, newVal)
}, { deep: true })

// 更新参数值
const updateParam = (key, value) => {
  localParams[key] = value
  // 创建一个新对象发送给父组件，确保引用变化
  emit('update:modelValue', { ...localParams })
}

// 设置图像尺寸
const setDimensions = (width, height) => {
  localParams.width = width
  localParams.height = height
  emit('update:modelValue', { ...localParams })
}

// 随机种子
const randomizeSeed = () => {
  updateParam('seed', Math.floor(Math.random() * 1000000000))
}

// 复制种子
const copySeed = () => {
  navigator.clipboard.writeText(localParams.seed.toString())
    .then(() => {
      ElMessage.success('种子已复制到剪贴板')
    })
    .catch(() => {
      ElMessage.error('复制失败')
    })
}

// 使用默认采样器
const defaultSamplers = computed(() => {
  if (props.availableSamplers.length > 0) {
    return props.availableSamplers
  }
  return [
    'Euler',
    'Euler A',
    'DPM++ 2M Karras',
    'DPM++ 2M',
    'LMS',
    'Heun',
    'DPM2',
    'DPM2 Karras',
    'DPM++ 2S Karras',
    'DPM++ 2M SDE Karras',
    'DPM++ 2M SDE',
    'DPM++ 3M SDE Karras',
    'DPM++ 3M SDE',
    'DPM++ 3M',
    'DPM++ 3M Karras'
  ]
})

// 使用默认模型
const defaultModels = computed(() => {
  if (props.availableModels.length > 0) {
    return props.availableModels
  }
  return ['Stable Diffusion XL']
})

// 监听 availableModels 变化并更新选中的模型
watch(() => props.availableModels, (newVal) => {
  if (newVal.length > 0 && !localParams.model) {
    updateParam('model', newVal[0])  
  }
})
</script>

<template>
  <div class="image-params-component">
    <div class="params-section-title">
    </div>

    <div class="params-section">
      <!-- 模型选择 -->
      <div class="param-item">
        <div class="param-label">AI 模型</div>
        <el-select 
          v-model="localParams.model" 
          @change="(val) => updateParam('model', val)" 
          placeholder="选择模型" 
          class="full-width"
        >
          <el-option
            v-for="model in defaultModels"
            :key="model"
            :label="model"
            :value="model"
          />
        </el-select>
      </div>
      
      <!-- 图像尺寸 -->
      <div class="param-group">
        <div class="param-row">
          <div class="param-item half-width">
            <div class="param-label">宽度</div>
            <el-input-number 
              v-model="localParams.width" 
              :min="256" 
              :max="2048" 
              :step="64" 
              @change="(val) => updateParam('width', val)"
              :controls="false"
            />
            <div class="param-unit">px</div>
          </div>
          <div class="param-item half-width">
            <div class="param-label">高度</div>
            <el-input-number 
              v-model="localParams.height" 
              :min="256" 
              :max="2048" 
              :step="64" 
              @change="(val) => updateParam('height', val)"
              class="full-width"
              :controls="false"
            />
            <div class="param-unit">px</div>
          </div>
        </div>
        
        <div class="dimension-presets">
          <el-button 
            class="preset-button"
            @click="setDimensions(1024, 1024)"
            :class="{ active: localParams.width === 1024 && localParams.height === 1024 }"
          >
            1024×1024
          </el-button>
          <el-button 
            class="preset-button"
            @click="setDimensions(1216, 832)"
            :class="{ active: localParams.width === 1216 && localParams.height === 832 }"
          >
            1216×832
          </el-button>
          <el-button 
            class="preset-button"
            @click="setDimensions(832, 1216)"
            :class="{ active: localParams.width === 832 && localParams.height === 1216 }"
          >
            832×1216
          </el-button>
        </div>
      </div>
      
      <!-- 随机种子 -->
      <div class="param-item">
        <div class="param-label">种子 (Seed)</div>
        <div class="seed-input">
          <el-input 
            v-model.number="localParams.seed" 
            @change="(val) => updateParam('seed', Number(val) || 0)"
          />
          <el-button @click="randomizeSeed" :icon="RefreshRight"></el-button>
          <el-button @click="copySeed" :icon="CopyDocument"></el-button>
        </div>
      </div>
      
      <!-- 采样方法 -->
      <div class="param-item">
        <div class="param-label">采样算法</div>
        <el-select 
          v-model="localParams.samplingMethod" 
          @change="(val) => updateParam('samplingMethod', val)" 
          placeholder="选择采样方法" 
          class="full-width"
        >
          <el-option
            v-for="sampler in defaultSamplers"
            :key="sampler"
            :label="sampler"
            :value="sampler"
          />
        </el-select>
      </div>
      
      <!-- 采样步数 -->
      <div class="param-item">
        <div class="param-row">
          <div class="param-label">采样步数</div>
          <div class="param-value">{{ localParams.steps }}</div>
        </div>
        <el-slider 
          v-model="localParams.steps" 
          :min="10" 
          :max="50" 
          :step="1" 
          @change="(val) => updateParam('steps', val)"
          class="full-width-slider"
        />
      </div>
      
      <!-- CFG Scale -->
      <div class="param-item">
        <div class="param-row">
          <div class="param-label">引导比例 (CFG Scale)</div>
          <div class="param-value">{{ localParams.cfgScale.toFixed(1) }}</div>
        </div>
        <el-slider 
          v-model="localParams.cfgScale" 
          :min="1" 
          :max="20" 
          :step="0.1" 
          @change="(val) => updateParam('cfgScale', val)"
          class="full-width-slider"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.image-params-component {
  padding: 0;
  color: var(--text-color);
}

.params-section-title {
  display: flex;
  align-items: center;
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 12px;
  padding: 8px 0;
}

.params-section-title .el-icon {
  margin-right: 6px;
  font-size: 18px;
}

.params-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.param-item {
  position: relative;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.param-label {
  font-size: 14px;
  color: var(--text-color);
  margin-bottom: 6px;
}

.param-desc {
  font-size: 12px;
  color: var(--text-color-light);
  margin-top: 4px;
}

.param-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 6px;
  gap: 12px;
}

.param-value {
  font-size: 14px;
  color: var(--text-color);
  font-variant-numeric: tabular-nums;
}

.full-width {
  width: 100%;
}

.full-width-slider {
  width: 100%;
  margin: 0;
}

.param-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.dimension-presets {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.preset-button {
  flex: 1;
  padding: 6px 8px;
  font-size: 13px;
  min-width: 0;
}

.preset-button.active {
  background-color: var(--primary-light);
  color: var(--primary-color);
  border-color: var(--primary-color);
}

.seed-input {
  display: flex;
  gap: 8px;
  width: 100%;
}

.seed-input .el-input {
  flex: 1;
}

.param-unit {
  position: absolute;
  right: 8px;
  top: 32px;
  font-size: 14px;
  color: var(--text-color-light);
  pointer-events: none;
  z-index: 1;
}

.half-width {
  width: 100%;
}

:deep(.el-input-number .el-input__inner) {
  padding-right: 10%;
  padding-left: 33%;
  text-align: left;
}

:deep(.el-input-number) {
  width: 100%;
}

:deep(.el-input-number .el-input__wrapper) {
  padding-left: 8px;
}

</style> 