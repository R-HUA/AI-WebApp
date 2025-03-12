<template>
  <el-drawer
    v-model="showDrawer"
    title="系统设置"
    size="400px"
    :direction="direction"
    :before-close="handleClose"
  >
    <el-tabs>
      <el-tab-pane label="API设置">
        <el-form label-position="top">
          <el-form-item label="ComfyUI API地址">
            <el-input v-model="localSettings.apiSettings.baseUrl" placeholder="例如: http://127.0.0.1:8188" />
          </el-form-item>

          <el-form-item>
            <el-button @click="testApiConnection">测试连接</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="界面设置">
        <el-form label-position="top">
          <el-form-item label="界面主题">
            <el-radio-group v-model="localSettings.uiSettings.theme">
              <el-radio label="light">浅色</el-radio>
              <el-radio label="dark">深色</el-radio>
              <el-radio label="auto">跟随系统</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="侧边栏位置">
            <el-radio-group v-model="localSettings.uiSettings.sidebarPosition">
              <el-radio label="left">左侧</el-radio>
              <el-radio label="right">右侧</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="默认图像尺寸">
            <el-select v-model="localSettings.uiSettings.defaultSize" placeholder="选择默认图像尺寸">
              <el-option label="1024 x 1024" value="1024x1024" />
            </el-select>
          </el-form-item>

          <el-form-item label="默认工作流">
            <el-radio-group v-model="localSettings.uiSettings.defaultWorkflow">
              <el-radio label="txt2img">文生图</el-radio>
              <el-radio label="img2img">图生图</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="存储设置">
        <el-form label-position="top">
          <el-form-item label="历史记录存储上限">
            <el-input-number
              v-model="localSettings.storageSettings.historyLimit"
              :min="10"
              :max="500"
              :step="10"
            />
            <div class="form-help">
              设置历史记录保存的最大图像数量
            </div>
          </el-form-item>

          <el-form-item label="数据存储位置">
            <el-radio-group v-model="localSettings.storageSettings.storageType">
              <el-radio label="localStorage">浏览器本地存储</el-radio>
              <el-radio label="indexedDB">IndexedDB</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item>
            <el-button type="warning" @click="showClearDataConfirm = true">清除所有数据</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>

    <template #footer>
      <div style="flex: auto">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="saveSettings">保存</el-button>
      </div>
    </template>

    <!-- 确认删除数据的对话框 -->
    <el-dialog
      v-model="showClearDataConfirm"
      title="确认清除数据"
      width="300px"
    >
      <span>确定要清除所有存储的数据吗？此操作不可恢复。</span>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showClearDataConfirm = false">取消</el-button>
          <el-button type="danger" @click="clearAllData">确认清除</el-button>
        </span>
      </template>
    </el-dialog>
  </el-drawer>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import comfyApi from '../services/comfyApi'
import storageService from '../services/storageService'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  direction: {
    type: String,
    default: 'rtl'
  }
})

const emit = defineEmits(['update:modelValue', 'settings-saved'])

// 计算属性用于双向绑定drawer的显示状态
const showDrawer = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

// 本地设置状态，用于表单编辑
const localSettings = reactive({
  apiSettings: {
    baseUrl: localStorage.getItem('comfyui-api-url') || `${window.location.protocol}//${window.location.hostname}:8188`
  },
  uiSettings: {
    theme: localStorage.getItem('ui-theme') || 'light',
    sidebarPosition: localStorage.getItem('sidebar-position') || 'left',
    defaultSize: localStorage.getItem('default-image-size') || '512x512',
    defaultWorkflow: localStorage.getItem('default-workflow') || 'txt2img'
  },
  storageSettings: {
    historyLimit: parseInt(localStorage.getItem('history-limit') || '50'),
    storageType: localStorage.getItem('storage-type') || 'localStorage'
  }
})

// 清除数据确认对话框
const showClearDataConfirm = ref(false)

// 测试API连接
const testApiConnection = async () => {
  try {
    // 临时设置API基础URL用于测试
    const originalUrl = comfyApi.getBaseUrl()
    comfyApi.setBaseUrl(localSettings.apiSettings.baseUrl)

    await comfyApi.getObjectInfo()
    ElMessage.success('API连接成功')

    // 恢复原始URL（如果不同）
    if (originalUrl !== localSettings.apiSettings.baseUrl) {
      comfyApi.setBaseUrl(originalUrl)
    }

    return true
  } catch (error) {
    ElMessage.error('API连接失败: ' + error.message)
    console.error('API连接失败:', error)
    return false
  }
}

// 保存设置
const saveSettings = () => {
  // 保存API设置
  localStorage.setItem('comfyui-api-url', localSettings.apiSettings.baseUrl)
  comfyApi.setBaseUrl(localSettings.apiSettings.baseUrl)

  // 保存UI设置
  localStorage.setItem('ui-theme', localSettings.uiSettings.theme)
  localStorage.setItem('sidebar-position', localSettings.uiSettings.sidebarPosition)
  localStorage.setItem('default-image-size', localSettings.uiSettings.defaultSize)
  localStorage.setItem('default-workflow', localSettings.uiSettings.defaultWorkflow)

  // 保存存储设置
  localStorage.setItem('history-limit', localSettings.storageSettings.historyLimit.toString())
  localStorage.setItem('storage-type', localSettings.storageSettings.storageType)

  // 应用存储设置
  storageService.setHistoryLimit(localSettings.storageSettings.historyLimit)

  // 关闭抽屉并发送事件
  showDrawer.value = false
  emit('settings-saved', {
    apiSettings: { ...localSettings.apiSettings },
    uiSettings: { ...localSettings.uiSettings },
    storageSettings: { ...localSettings.storageSettings }
  })

  ElMessage.success('设置已保存')
}

// 清除所有数据
const clearAllData = () => {
  ElMessageBox.confirm(
    '此操作将清除所有本地存储的数据，包括图像历史记录、提示词历史和设置。确定要继续吗？',
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(() => {
      // 清除所有存储数据
      storageService.clearAllData()

      // 关闭对话框
      showClearDataConfirm.value = false

      ElMessage({
        type: 'success',
        message: '所有数据已清除',
      })
    })
    .catch(() => {
      // 用户取消
    })
}

// 关闭抽屉前的处理函数
const handleClose = () => {
  // 设置未保存时询问用户
  ElMessageBox.confirm(
    '有未保存的设置更改，确定要关闭吗？',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(() => {
      showDrawer.value = false
    })
    .catch(() => {
      // 用户取消关闭
    })
}
</script>

<style scoped>
.form-help {
  font-size: 12px;
  color: var(--text-color-light);
  margin-top: 4px;
}

.el-form-item {
  margin-bottom: 20px;
}

/* 深色主题下设置面板的样式 */
:deep(.dark-theme .el-drawer__header) {
  color: var(--text-color);
  border-bottom: 1px solid var(--border-color);
}

:deep(.dark-theme .el-drawer__body) {
  background-color: #252529;
  color: var(--text-color);
}

:deep(.dark-theme .el-tabs__item) {
  color: var(--text-color-light);
}

:deep(.dark-theme .el-tabs__item.is-active) {
  color: var(--primary-color);
}

:deep(.dark-theme .el-tabs__active-bar) {
  background-color: var(--primary-color);
}

:deep(.dark-theme .el-radio__label) {
  color: var(--text-color);
}

:deep(.dark-theme .el-input__inner),
:deep(.dark-theme .el-input-number__inner) {
  background-color: #333336;
  color: var(--text-color);
  border-color: var(--border-color);
}

:deep(.dark-theme .el-input__wrapper.is-focus),
:deep(.dark-theme .el-select .el-input.is-focus .el-input__wrapper) {
  box-shadow: 0 0 0 1px var(--primary-color) inset;
}

:deep(.dark-theme .el-select-dropdown) {
  background-color: #333336;
  border-color: var(--border-color);
}

:deep(.dark-theme .el-select-dropdown__item) {
  color: var(--text-color);
}

:deep(.dark-theme .el-select-dropdown__item.hover),
:deep(.dark-theme .el-select-dropdown__item:hover) {
  background-color: #252529;
}

:deep(.dark-theme .el-button) {
  background-color: #333336;
  border-color: var(--border-color);
  color: var(--text-color);
}

:deep(.dark-theme .el-button:hover) {
  color: var(--primary-color);
  border-color: var(--primary-color);
  background-color: #2a2a2e;
}

:deep(.dark-theme .el-button--primary) {
  background-color: var(--primary-color);
  border-color: var(--primary-color);
  color: white;
}

:deep(.dark-theme .el-button--danger) {
  background-color: #f56c6c;
  border-color: #f56c6c;
  color: white;
}

:deep(.dark-theme .el-dialog) {
  background-color: #252529;
}

:deep(.dark-theme .el-dialog__title) {
  color: var(--text-color);
}

:deep(.dark-theme .el-dialog__body) {
  color: var(--text-color);
}
</style>
