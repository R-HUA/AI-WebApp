<template>
  <div class="scan-directories">
    <div class="header">
      <h2>扫描目录管理</h2>
      <el-button type="primary" @click="showAddDialog">添加目录</el-button>
    </div>
    
    <!-- 目录列表 -->
    <el-table :data="directories" v-loading="loading">
      <el-table-column prop="path" label="目录路径" />
      <el-table-column prop="description" label="描述" />
      <el-table-column prop="imageCount" label="图片数量" width="100" />
      <el-table-column prop="lastScanTime" label="上次扫描时间" width="180">
        <template #default="{ row }">
          {{ formatTime(row.lastScanTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button 
            type="primary" 
            :icon="Refresh"
            circle
            :loading="row.scanning"
            @click="handleRescan(row)"
          />
          <el-button 
            type="danger" 
            :icon="Delete"
            circle
            @click="handleDelete(row)"
          />
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 添加目录对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="添加扫描目录"
      width="500px"
    >
      <el-form :model="form" label-width="100px">
        <el-form-item label="目录路径" required>
          <el-input v-model="form.path" placeholder="请输入目录完整路径" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" placeholder="请输入目录描述（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleAdd" :loading="adding">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Delete, Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { imageService } from '../services/imageService'

const directories = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const adding = ref(false)
const form = ref({
  path: '',
  description: ''
})

// 格式化时间
const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString()
}

// 加载目录列表
const loadDirectories = async () => {
  loading.value = true
  try {
    const response = await imageService.getAllDirectories()
    directories.value = response.data
  } catch (error) {
    ElMessage.error('加载目录列表失败')
  } finally {
    loading.value = false
  }
}

// 显示添加对话框
const showAddDialog = () => {
  form.value = {
    path: '',
    description: ''
  }
  dialogVisible.value = true
}

// 添加目录
const handleAdd = async () => {
  if (!form.value.path) {
    ElMessage.warning('请输入目录路径')
    return
  }
  
  adding.value = true
  try {
    await imageService.addScanDirectory(form.value.path, form.value.description)
    ElMessage.success('添加成功')
    dialogVisible.value = false
    loadDirectories()
  } catch (error) {
    if (error.response?.data?.error) {
      ElMessage.error(error.response.data.error)
    } else {
      ElMessage.error('添加失败')
    }
  } finally {
    adding.value = false
  }
}

// 删除目录
const handleDelete = async (directory) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除该扫描目录吗？此操作不会删除实际的图片文件。',
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await imageService.removeScanDirectory(directory.id)
    ElMessage.success('删除成功')
    loadDirectories()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 重新扫描
const handleRescan = async (directory) => {
  directory.scanning = true
  try {
    const response = await imageService.rescanDirectory(directory.id)
    ElMessage.success(`扫描完成，更新了 ${response.data.updatedCount} 个图片`)
    loadDirectories()
  } catch (error) {
    if (error.response?.data?.error) {
      ElMessage.error(error.response.data.error)
    } else {
      ElMessage.error('扫描失败')
    }
  } finally {
    directory.scanning = false
  }
}

onMounted(() => {
  loadDirectories()
})
</script>

<style scoped>
.scan-directories {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header h2 {
  margin: 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style> 