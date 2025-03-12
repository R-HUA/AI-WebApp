class StorageService {
  constructor() {
    this.storagePrefix = 'aigc-app-';
  }

  // 保存设置
  saveSettings(settings) {
    localStorage.setItem(`${this.storagePrefix}settings`, JSON.stringify(settings));
  }

  // 获取设置
  getSettings() {
    const settings = localStorage.getItem(`${this.storagePrefix}settings`);
    return settings ? JSON.parse(settings) : null;
  }

  // 保存图像历史记录
  saveImageHistory(imageHistory) {
    localStorage.setItem(`${this.storagePrefix}image-history`, JSON.stringify(imageHistory));
  }

  // 获取图像历史记录
  getImageHistory() {
    const history = localStorage.getItem(`${this.storagePrefix}image-history`);
    return history ? JSON.parse(history) : [];
  }

  // 添加图像到历史记录
  addImageToHistory(imageData) {
    const history = this.getImageHistory();
    
    // 添加到历史记录开头
    history.unshift({
      ...imageData,
      timestamp: new Date().toISOString()
    });
    
    // 获取历史记录限制数量
    const historyLimit = parseInt(localStorage.getItem('history-limit') || '20');
    
    // 限制历史记录数量
    if (history.length > historyLimit) {
      history.splice(historyLimit);
    }
    
    this.saveImageHistory(history);
    return history;
  }

  // 设置历史记录上限
  setHistoryLimit(limit) {
    const historyLimit = parseInt(limit) || 20;
    localStorage.setItem('history-limit', historyLimit.toString());
    
    // 应用到现有历史记录
    const history = this.getImageHistory();
    if (history.length > historyLimit) {
      history.splice(historyLimit);
      this.saveImageHistory(history);
    }
  }

  // 保存提示词历史
  savePromptHistory(prompts) {
    localStorage.setItem(`${this.storagePrefix}prompt-history`, JSON.stringify(prompts));
  }

  // 获取提示词历史
  getPromptHistory() {
    const prompts = localStorage.getItem(`${this.storagePrefix}prompt-history`);
    return prompts ? JSON.parse(prompts) : [];
  }

  // 添加提示词到历史
  addPromptToHistory(prompt) {
    if (!prompt.trim()) return;
    
    const prompts = this.getPromptHistory();
    
    // 如果已存在相同提示词，则移除旧的
    const filteredPrompts = prompts.filter(p => p !== prompt);
    
    // 添加到历史记录开头
    filteredPrompts.unshift(prompt);
    
    // 限制历史记录数量为20条
    if (filteredPrompts.length > 20) {
      filteredPrompts.pop();
    }
    
    this.savePromptHistory(filteredPrompts);
    return filteredPrompts;
  }

  // 清除所有数据
  clearAllData() {
    localStorage.removeItem(`${this.storagePrefix}settings`);
    localStorage.removeItem(`${this.storagePrefix}image-history`);
    localStorage.removeItem(`${this.storagePrefix}prompt-history`);
  }
}

export default new StorageService(); 