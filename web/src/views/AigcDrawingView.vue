<template>
  <div class="drawing-view">
    <el-container class="main-container">
      <!-- 左侧图像预览区域 -->
      <el-main class="preview-container">
          <ImagePreview 
            :generated-image="generatedImage"
            :image-history="imageHistory"
            :is-generating="isGenerating"
            :generation-progress="generationProgress"
            :image-params="imageParams"
            @select-history-image="selectHistoryImage"
            @clear-history="clearHistory"
            @retry="retryGeneration"
          />
      </el-main>
      
      <!-- 右侧控制面板 -->
      <el-aside width="400px" class="control-container">
        <ControlPanel
          v-model:prompt="prompt"
          v-model:negativePrompt="negativePrompt"
          :imageParams="imageParams"
          @update:imageParams="handleImageParamsUpdate"
          :workflow="selectedWorkflow"
          v-model:initImage="initImage"
          :available-models="availableModels"
          :available-samplers="availableSamplers"
          :is-collapsed="isControlPanelCollapsed"
          @toggle="toggleControlPanel"
        />
        
        <!-- 底部生成按钮 -->
        <div class="action-footer">
          <div class="param-info">
            <span class="size-info">{{ imageParams.width }}×{{ imageParams.height }}</span>
            <span class="model-info">{{ imageParams.model.slice(0, 10) }}</span>
          </div>
          <el-button 
            type="primary" 
            size="large" 
            @click="generateImage" 
            :loading="isGenerating"
            :disabled="!prompt.trim()"
          >
            {{ isGenerating ? '生成中...' : '生成图像' }}
          </el-button>
        </div>
      </el-aside>
    </el-container>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute } from 'vue-router'
import comfyApi from '../services/comfyApi'
import storageService from '../services/storageService'
import ImagePreview from '../components/ImagePreview.vue'
import ControlPanel from '../components/ControlPanel.vue'

// 获取路由
const route = useRoute()

// 状态变量
const prompt = ref('')
const negativePrompt = ref('')
const generatedImage = ref(null)
const imageHistory = ref([])
const isGenerating = ref(false)
const generationProgress = ref(0)
const isControlPanelCollapsed = ref(true)
const selectedWorkflow = ref(localStorage.getItem('default-workflow') || 'txt2img') // 从设置中读取默认工作流
const initImage = ref(null) // 图生图的初始图片

// 图像参数
const imageParams = reactive({
  model: localStorage.getItem('default-model') || 'Stable Diffusion XL',
  width: 1024,
  height: 1024,
  seed: Math.floor(Math.random() * 1000000000),
  samplingMethod: localStorage.getItem('default-sampler') || 'euler',
  steps: 28,
  cfgScale: 6
})

// 可用模型和采样器
const availableModels = ref([])
const availableSamplers = ref([])

// 处理图像参数更新
const handleImageParamsUpdate = (newParams) => {
  // 使用 Object.assign 更新 reactive 对象，保持响应性
  Object.assign(imageParams, newParams)
}

// 从历史记录中选择图像
const selectHistoryImage = (img) => {
  generatedImage.value = img.imageUrl
  
  // 如果有参数，也恢复参数
  if (img.params) {
    Object.assign(imageParams, img.params)
  }
  
  // 如果有提示词，也恢复提示词
  if (img.prompt) {
    prompt.value = img.prompt
  }
  
  if (img.negativePrompt) {
    negativePrompt.value = img.negativePrompt
  }
}

// 清空历史记录
const clearHistory = () => {
  imageHistory.value = []
  storageService.saveImageHistory([])
  ElMessage.success('历史记录已清空')
}

// 获取模型和采样器列表
const fetchModelAndSamplers = async () => {
  try {
    const models = await comfyApi.getModels()
    if (models && models.length > 0) {
      availableModels.value = models
      // 更新当前模型为可用模型中的第一个
      if (!imageParams.model || !models.includes(imageParams.model)) {
        imageParams.model = models[0]
      }
    }
    
    const samplers = await comfyApi.getSamplers()
    if (samplers && samplers.length > 0) {
      availableSamplers.value = samplers
      // 更新当前采样器为可用采样器中的第一个
      if (!imageParams.samplingMethod || !samplers.includes(imageParams.samplingMethod)) {
        imageParams.samplingMethod = samplers[0]
      }
    }
  } catch (error) {
    console.error('获取模型和采样器列表失败:', error)
  }
}

// 重试生成图像
const retryGeneration = () => {
  generateImage()
}

// 生成图像
const generateImage = async () => {
  if (!prompt.value.trim()) {
    ElMessage.warning('请输入提示词')
    return
  }
  
  if (selectedWorkflow.value === 'img2img' && !initImage.value) {
    ElMessage.warning('请上传初始图片')
    return
  }
  
  try {
    isGenerating.value = true
    generationProgress.value = 0
    
    // 设置WebSocket回调
    comfyApi.setWebSocketCallbacks({
      onProgress: (data) => {
        // 更新进度
        if (data.value && data.max) {
          const progress = Math.round((data.value / data.max) * 100)
          generationProgress.value = progress
        }
      },
      onExecuted: (data) => {
        console.log('执行完成:', data)
      },
      onError: (error) => {
        console.error('WebSocket错误:', error)
        ElMessage.error('生成过程中发生错误')
      }
    })
    
    // 添加提示词到历史
    storageService.addPromptToHistory(prompt.value)
    
    // 准备参数
    const params = {
      prompt: prompt.value,
      negativePrompt: negativePrompt.value,
      ...imageParams,
      workflow: selectedWorkflow.value
    }

    // 如果是图生图，添加初始图片
    if (selectedWorkflow.value === 'img2img' && initImage.value) {
      params.initImage = initImage.value
    }
    
    // 调用API生成图像
    try {
      // 尝试连接API
      const promptId = await comfyApi.generateImage(params)

      await new Promise(resolve => setTimeout(resolve, 1000))
      
      // 获取结果
      const imageUrl = await comfyApi.getImageResult(promptId)
      
      generationProgress.value = 100
      
      // 显示生成的图像
      generatedImage.value = imageUrl
      
      // 添加到历史记录
      const imageData = {
        imageUrl,
        prompt: prompt.value,
        negativePrompt: negativePrompt.value,
        params: { ...imageParams },
        workflow: selectedWorkflow.value,
        initImage: initImage.value ? URL.createObjectURL(initImage.value) : null,
        timestamp: new Date().toISOString()
      }
      
      imageHistory.value = storageService.addImageToHistory(imageData)
      
      // 生成成功后更新 seed
      imageParams.seed = Math.floor(Math.random() * 1000000000)
      
      ElMessage.success('图像生成成功')
    } catch (error) {
      ElMessage({
        message: 'API连接失败:' + error.message,
        type: 'warning',
        duration: 5000
      })
    }
  } catch (error) {
    ElMessage.error('生成图像失败: ' + error.message)
    console.error('生成图像失败:', error)
  } finally {
    isGenerating.value = false
  }
}

const toggleControlPanel = () => {
  isControlPanelCollapsed.value = !isControlPanelCollapsed.value
}

// 处理参考图片
const handleReferenceImage = () => {
  if (route.params.referenceImage) {
    const timestamp = route.params.referenceImage
    const history = storageService.getImageHistory() || []
    const referenceImg = history.find(img => img.timestamp.toString() === timestamp.toString())
    
    if (referenceImg) {
      // 设置图像和参数
      generatedImage.value = referenceImg.imageUrl
      
      if (referenceImg.params) {
        // 复制参数
        Object.assign(imageParams, referenceImg.params)
      }
      
      // 设置提示词
      if (referenceImg.prompt) {
        prompt.value = referenceImg.prompt
      }
      
      if (referenceImg.negativePrompt) {
        negativePrompt.value = referenceImg.negativePrompt
      }
      
      ElMessage.success('已加载参考图片')
    }
  }
}

// 生命周期钩子
onMounted(async () => {
  // 加载图片历史记录
  imageHistory.value = storageService.getImageHistory()

  // 自动清除24h以上图片历史
  cleanupOldImages()

  prompt.value = storageService.getPromptHistory().slice(-1)[0] || '' // slice 返回的是一个新数组，取第一个元素
  
  // 尝试获取模型和采样器列表
  fetchModelAndSamplers()
  
  // 加载设置
  const settings = storageService.getSettings()
  if (settings) {
    // 应用保存的设置
    if (settings.imageParams) {
      Object.assign(imageParams, settings.imageParams)
    }
    // 应用工作流设置
    if (settings.uiSettings && settings.uiSettings.defaultWorkflow) {
      selectedWorkflow.value = settings.uiSettings.defaultWorkflow
    }
  }
  
  // 加载默认尺寸设置
  const defaultSize = localStorage.getItem('default-image-size')
  if (defaultSize) {
    const [width, height] = defaultSize.split('x').map(Number)
    if (width && height) {
      imageParams.width = width
      imageParams.height = height
    }
  }
  
  // 加载参考图片（如果有）
  handleReferenceImage()
})

// 清理24小时以上的历史图片
const cleanupOldImages = () => {
  const history = storageService.getImageHistory() || []
  if (history.length === 0) return
  
  const now = new Date().getTime()
  const oneDayMs = 24 * 60 * 60 * 1000 // 24小时的毫秒数
  
  // 过滤出24小时内的图片
  const recentImages = history.filter(img => {
    const imgTime = new Date(img.timestamp).getTime()
    return (now - imgTime) < oneDayMs
  })
  
  // 如果有图片被过滤掉，则更新历史记录
  if (recentImages.length < history.length) {
    console.log(`清理了 ${history.length - recentImages.length} 张超过24小时的历史图片`)
    imageHistory.value = recentImages
    storageService.saveImageHistory(recentImages)
  }
}

// 监听路由变化以处理参考图片
watch(() => route.params.referenceImage, (newRefImg) => {
  if (newRefImg) {
    handleReferenceImage()
  }
})
</script>

<style scoped>
.drawing-view {
  height: 100%;
  width: 100%;
  display: flex;
  flex-direction: column;
}

.main-container {
  height: 100%;
  width: 100%;
  overflow: hidden;
}

.preview-container {
  padding: 0px;
  margin-right: 15px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  box-shadow: none;
  background-color: #fff;
  overflow-y: hidden;
  border-radius: 6px;
}

.preview-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  height: 100%;
  box-shadow: none;
}

.control-container {
  display: flex;
  flex-direction: column;
  width: 380px !important;
  height: 100%;
  overflow: hidden;
}

.action-footer {
  background-color: #fff;
  padding: 12px 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 60px;
  flex-shrink: 0;
  z-index: 101;
  border-radius: 0 0 6px 6px;
}

.param-info {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.size-info {
  font-weight: 500;
  color: var(--text-color);
}

.model-info {
  font-size: 12px;
  color: var(--text-color-light);
  margin-top: 4px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  
  .main-container {
    flex-direction: column;
  }
  
  .preview-container {
    padding: 0;
    flex: 1;
    min-height: 0;
  }
  
  .preview-card {
    height: 100%;
    border-radius: 0;
  }
  
  .control-container {
    width: 100% !important;
    height: auto;
  }
  
  .action-footer {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    border-radius: 0;
    background-color: var(--bg-color);
  }
}

@media (max-width: 480px) {
  .preview-container {
    height: calc(100vh - 60px);
  }
}
</style>