<template>
  <div class="history-view">
    <el-tabs v-model="activeTab" class="history-tabs">
      <el-tab-pane label="按日期查看" name="date">
        <div class="date-view">
          <el-collapse v-model="expandedDates">
            <el-collapse-item
              v-for="(images, date) in imagesByDate"
              :key="date"
              :title="date"
              :name="date"
            >
              <div class="image-grid">
                <div
                  v-for="image in images"
                  :key="image.id"
                  class="image-item"
                  @click="showImageDetail(image)"
                >
                  <el-image
                    :src="getThumbnailUrl(image.id)"
                    :alt="image.fileName"
                    fit="cover"
                  >
                    <template #error>
                      <div class="image-error">
                        <el-icon><Picture /></el-icon>
                      </div>
                    </template>
                  </el-image>
                </div>
              </div>
            </el-collapse-item>
          </el-collapse>
          
          <div v-if="loading" class="loading-more">
            <el-spinner />
          </div>
          
          <div v-if="hasMore" class="load-more">
            <el-button @click="loadMore">加载更多</el-button>
          </div>
        </div>
      </el-tab-pane>
      
      <el-tab-pane label="按标签查看" name="tag">
        <div class="tag-view">
          <div class="tag-cloud">
            <el-tag
              v-for="tag in allTags"
              :key="tag"
              :type="selectedTags.includes(tag) ? 'primary' : ''"
              @click="toggleTag(tag)"
              class="tag-item"
            >
              {{ tag }}
            </el-tag>
          </div>
          
          <div class="image-grid" v-if="selectedTags.length > 0">
            <div
              v-for="image in imagesByTag"
              :key="image.id"
              class="image-item"
              @click="showImageDetail(image)"
            >
              <el-image
                :src="getThumbnailUrl(image.id)"
                :alt="image.fileName"
                fit="cover"
              >
                <template #error>
                  <div class="image-error">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
            </div>
          </div>
        </div>
      </el-tab-pane>
      
      <el-tab-pane label="按画师查看" name="artist">
        <div class="artist-view">
          <el-collapse v-model="expandedArtists">
            <el-collapse-item
              v-for="(images, artist) in imagesByArtist"
              :key="artist"
              :title="artist"
              :name="artist"
            >
              <div class="image-grid">
                <div
                  v-for="image in images"
                  :key="image.id"
                  class="image-item"
                  @click="showImageDetail(image)"
                >
                  <el-image
                    :src="getThumbnailUrl(image.id)"
                    :alt="image.fileName"
                    fit="cover"
                  >
                    <template #error>
                      <div class="image-error">
                        <el-icon><Picture /></el-icon>
                      </div>
                    </template>
                  </el-image>
                </div>
              </div>
            </el-collapse-item>
          </el-collapse>
        </div>
      </el-tab-pane>
    </el-tabs>
    
    <!-- 图片详情对话框 -->
    <el-dialog
      v-model="showDetail"
      :title="currentImage?.fileName"
      width="80%"
      class="image-detail-dialog"
    >
      <div class="image-detail" v-if="currentImage">
        <div class="detail-image">
          <el-image
            :src="currentImage.imageUrl"
            :alt="currentImage.fileName"
            fit="contain"
          />
        </div>
        
        <div class="detail-info">
          <div class="info-section">
            <h4>提示词</h4>
            <p>{{ currentImage.prompt || '无' }}</p>
          </div>
          
          <div class="info-section">
            <h4>负面提示词</h4>
            <p>{{ currentImage.negativePrompt || '无' }}</p>
          </div>
          
          <div class="info-section">
            <h4>标签</h4>
            <div class="tag-list">
              <el-tag
                v-for="tag in currentImage.tags"
                :key="tag"
                size="small"
                class="tag-item"
              >
                {{ tag }}
              </el-tag>
            </div>
          </div>
          
          <div class="info-section">
            <h4>画师</h4>
            <div class="tag-list">
              <el-tag
                v-for="artist in currentImage.artists"
                :key="artist"
                type="success"
                size="small"
                class="tag-item"
              >
                {{ artist }}
              </el-tag>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { Picture } from '@element-plus/icons-vue'
import { imageService } from '../services/imageService'

// 状态变量
const activeTab = ref('date')
const expandedDates = ref([])
const expandedArtists = ref([])
const selectedTags = ref([])
const currentPage = ref(0)
const pageSize = ref(20)
const loading = ref(false)
const hasMore = ref(true)
const showDetail = ref(false)
const currentImage = ref(null)

// 数据存储
const imagesByDate = ref({})
const imagesByTag = ref([])
const imagesByArtist = ref({})
const allTags = ref([])

// 加载初始数据
onMounted(async () => {
  await Promise.all([
    loadImagesByDate(),
    loadAllTags(),
    loadImagesByArtist()
  ])
})

// 加载日期分组的图片
async function loadImagesByDate() {
  try {
    loading.value = true
    const response = await imageService.getImagesByDate(currentPage.value, pageSize.value)
    const newImages = response.data
    
    // 合并新数据
    Object.entries(newImages).forEach(([date, images]) => {
      if (imagesByDate.value[date]) {
        imagesByDate.value[date].push(...images)
      } else {
        imagesByDate.value[date] = images
      }
    })
    
    hasMore.value = Object.keys(newImages).length === pageSize.value
    currentPage.value++
  } catch (error) {
    console.error('加载图片失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载所有标签
async function loadAllTags() {
  try {
    const response = await imageService.getAllTags()
    allTags.value = response.data
  } catch (error) {
    console.error('加载标签失败:', error)
  }
}

// 加载画师分组的图片
async function loadImagesByArtist() {
  try {
    const response = await imageService.getImagesByArtist(0, 100)
    imagesByArtist.value = response.data
  } catch (error) {
    console.error('加载画师图片失败:', error)
  }
}

// 切换标签选择
function toggleTag(tag) {
  const index = selectedTags.value.indexOf(tag)
  if (index === -1) {
    selectedTags.value.push(tag)
  } else {
    selectedTags.value.splice(index, 1)
  }
  
  if (selectedTags.value.length > 0) {
    searchByTags()
  } else {
    imagesByTag.value = []
  }
}

// 按标签搜索图片
async function searchByTags() {
  try {
    const response = await imageService.searchByTags(selectedTags.value)
    imagesByTag.value = response.data.content
  } catch (error) {
    console.error('搜索图片失败:', error)
  }
}

// 加载更多图片
function loadMore() {
  if (activeTab.value === 'date') {
    loadImagesByDate()
  }
}

// 获取缩略图URL
function getThumbnailUrl(imageId) {
  return imageService.getThumbnailUrl(imageId)
}

// 获取原图URL
function getImageUrl(imageId) {
  return imageService.getImageUrl(imageId)
}

// 显示图片详情
function showImageDetail(image) {
  currentImage.value = {
    ...image,
    imageUrl: getImageUrl(image.id),
    thumbnailUrl: getThumbnailUrl(image.id)
  }
  showDetail.value = true
}
</script>

<style scoped>
.history-view {
  padding: 20px;
  height: 100%;
  overflow: auto;
}

.history-tabs {
  height: 100%;
}

.image-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
  padding: 16px;
}

.image-item {
  aspect-ratio: 1;
  cursor: pointer;
  border-radius: 8px;
  overflow: hidden;
  transition: transform 0.2s;
  
  &:hover {
    transform: scale(1.05);
  }
}

.image-error {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  color: #909399;
}

.tag-cloud {
  padding: 16px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-item {
  cursor: pointer;
}

.loading-more,
.load-more {
  display: flex;
  justify-content: center;
  padding: 20px;
}

.image-detail-dialog {
  :deep(.el-dialog__body) {
    padding: 0;
  }
}

.image-detail {
  display: flex;
  gap: 20px;
  
  .detail-image {
    flex: 2;
    max-height: 80vh;
    overflow: hidden;
    
    .el-image {
      width: 100%;
      height: 100%;
    }
  }
  
  .detail-info {
    flex: 1;
    padding: 20px;
    overflow-y: auto;
    
    .info-section {
      margin-bottom: 20px;
      
      h4 {
        margin-bottom: 8px;
        color: var(--el-text-color-primary);
      }
      
      .tag-list {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;
      }
    }
  }
}

/* 深色主题适配 */
:deep(.dark-theme) {
  .image-error {
    background-color: #2a2a2e;
    color: #909399;
  }
  
  .image-detail {
    background-color: #252529;
    
    .detail-info {
      color: var(--el-text-color-regular);
    }
  }
}
</style> 