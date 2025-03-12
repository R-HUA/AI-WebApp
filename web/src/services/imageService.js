import axios from 'axios'

const API_BASE_URL = 'http://localhost:8080/api/images'

export const imageService = {
  // 扫描图片目录
  scanImages(basePath) {
    return axios.post(`${API_BASE_URL}/scan`, null, {
      params: { basePath }
    })
  },
  
  // 按日期获取图片
  getImagesByDate(page = 0, size = 20) {
    return axios.get(`${API_BASE_URL}/by-date`, {
      params: { page, size }
    })
  },
  
  // 按标签获取图片
  getImagesByTag(page = 0, size = 20) {
    return axios.get(`${API_BASE_URL}/by-tag`, {
      params: { page, size }
    })
  },
  
  // 按画师获取图片
  getImagesByArtist(page = 0, size = 20) {
    return axios.get(`${API_BASE_URL}/by-artist`, {
      params: { page, size }
    })
  },
  
  // 获取所有标签
  getAllTags() {
    return axios.get(`${API_BASE_URL}/tags`)
  },
  
  // 获取所有画师
  getAllArtists() {
    return axios.get(`${API_BASE_URL}/artists`)
  },
  
  // 按标签搜索图片
  searchByTags(tags, page = 0, size = 20) {
    return axios.get(`${API_BASE_URL}/search`, {
      params: {
        tags: tags.join(','),
        page,
        size
      }
    })
  },
  
  // 获取图片详情
  getImageDetail(id) {
    return axios.get(`${API_BASE_URL}/${id}`)
  },
  
  // 获取图片URL（通过ID）
  getImageUrl(id) {
    return `${API_BASE_URL}/${id}/file`
  },
  
  // 获取缩略图URL（通过ID）
  getThumbnailUrl(id) {
    return `${API_BASE_URL}/${id}/thumbnail`
  },
  
  // 将路径转换为安全的URL（兼容旧代码，但建议使用上面的方法）
  getImageUrlFromPath(path) {
    if (!path) return ''
    // 使用图片ID方式获取
    if (path.startsWith('/images/')) {
      const imageId = this.extractImageIdFromPath(path)
      if (imageId) {
        return this.getImageUrl(imageId)
      }
    }
    // 如果无法提取ID，提供一个基本的路径（不推荐）
    return `http://localhost:8080${path}`
  },
  
  // 将路径转换为缩略图URL
  getThumbnailUrlFromPath(path) {
    if (!path) return ''
    // 使用图片ID方式获取
    if (path.startsWith('/thumbnails/')) {
      const imageId = this.extractImageIdFromPath(path.replace('/thumbnails/', '/images/'))
      if (imageId) {
        return this.getThumbnailUrl(imageId)
      }
    }
    // 如果无法提取ID，提供一个基本的路径（不推荐）
    return `http://localhost:8080${path}`
  },
  
  // 从路径中提取图片ID（辅助方法）
  extractImageIdFromPath(pathString) {
    if (!pathString) return null
    
    try {
      // 尝试查找路径中的图片ID
      // 这需要与后端约定好命名规则
      // 例如，如果文件名包含图片ID信息，可以在这里解析
      
      // 这里假设图片已经在数据库中存储，且需要根据文件名查询
      // 由于我们无法直接从前端查询数据库，这个方法暂时只能返回null
      // 实际应用中，可以考虑在文件名中嵌入ID信息，或者提供一个API端点通过文件名查询ID
    } catch (e) {
      console.error('Failed to extract image ID from path:', e)
    }
    return null
  },
  
  // 获取所有扫描目录
  getAllDirectories() {
    return axios.get(`${API_BASE_URL}/directories`)
  },
  
  // 添加扫描目录
  addScanDirectory(path, description = '') {
    return axios.post(`${API_BASE_URL}/directories`, null, {
      params: { path, description }
    })
  },
  
  // 删除扫描目录
  removeScanDirectory(id) {
    return axios.delete(`${API_BASE_URL}/directories/${id}`)
  },
  
  // 重新扫描目录
  rescanDirectory(id) {
    return axios.post(`${API_BASE_URL}/directories/${id}/rescan`)
  }
} 