<template>
  <div class="settings-view">
    <div class="settings-container">
      <h1 class="settings-title">系统设置</h1>
      
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

      <div class="settings-actions">
        <el-button type="primary" @click="saveSettings">保存设置</el-button>
        <el-button @click="goBack">返回</el-button>
      </div>
    </div>

    <!-- 清除数据确认对话框 -->
    <el-dialog
      v-model="showClearDataConfirm"
      title="确认清除数据"
      width="90%"
      max-width="500px"
    >
      <span>此操作将清除所有本地存储的数据，包括图像历史记录、提示词历史和设置。确定要继续吗？</span>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showClearDataConfirm = false">取消</el-button>
          <el-button type="danger" @click="clearAllData">确认清除</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import comfyApi from '../services/comfyApi'
import storageService from '../services/storageService'

const router = useRouter()

// 本地设置
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
// 修复 ElementPlusError 警告（在保存设置方法中）
const saveSettings = () => {
  // 保存API设置
  localStorage.setItem('comfyui-api-url', localSettings.apiSettings.baseUrl)
  
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

  // 应用API设置
  comfyApi.setBaseUrl(localSettings.apiSettings.baseUrl)

  // 发送事件通知其他组件设置已更改
  window.dispatchEvent(new StorageEvent('storage', {
    key: 'ui-theme',
    newValue: localSettings.uiSettings.theme
  }))

  window.dispatchEvent(new StorageEvent('storage', {
    key: 'sidebar-position',
    newValue: localSettings.uiSettings.sidebarPosition
  }))

  // 修改消息提示方式
  ElMessage({
    type: 'success', // 或根据情况改为 'link'
    message: '设置已保存'
  })
}

// 在清除数据方法中修改提示方式
const clearAllData = () => {
  // 清除本地存储
  localStorage.clear()
  
  // 清除IndexedDB数据
  storageService.clearAllData()
    .then(() => {
      ElMessage({
        type: 'success',
        message: '所有数据已清除'
      })
      showClearDataConfirm.value = false
      
      // 重新加载页面以应用更改
      setTimeout(() => {
        window.location.reload()
      }, 1000)
    })
    .catch(error => {
      ElMessage.error('清除数据失败: ' + error.message)
      console.error('清除数据失败:', error)
    })
}

// 返回上一页
const goBack = () => {
  router.back()
}
</script>

<style scoped>
.settings-view {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.settings-container {
  background-color: var(--card-bg);
  border-radius: 8px;
  padding: 20px;
  box-shadow: var(--box-shadow);
}

.settings-title {
  font-size: 24px;
  margin-bottom: 20px;
  color: var(--text-color);
}

.settings-actions {
  margin-top: 30px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.form-help {
  font-size: 12px;
  color: var(--text-color-light);
  margin-top: 4px;
}

@media (max-width: 768px) {
  .settings-view {
    padding: 10px;
  }
  
  .settings-container {
    padding: 15px;
  }
}

/* 深色模式适配 */
:deep(.dark-theme .el-tabs__item) {
  color: var(--text-color-light);
}

:deep(.dark-theme .el-tabs__item.is-active) {
  color: var(--primary-color);
}

:deep(.dark-theme .el-tabs__active-bar) {
  background-color: var(--primary-color);
}

:deep(.dark-theme .el-form-item__label) {
  color: var(--text-color);
}

:deep(.dark-theme .el-input__inner) {
  background-color: var(--input-bg);
  border-color: var(--border-color);
  color: var(--text-color);
}

:deep(.dark-theme .el-input-number__decrease),
:deep(.dark-theme .el-input-number__increase) {
  background-color: var(--input-bg);
  border-color: var(--border-color);
  color: var(--text-color);
}

:deep(.dark-theme .el-radio__label) {
  color: var(--text-color);
}

:deep(.dark-theme .el-select .el-input__inner) {
  background-color: var(--input-bg);
}
</style>