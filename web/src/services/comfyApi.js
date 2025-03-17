import axios from 'axios';

class ComfyApi {
  constructor() {
    this.baseUrl = localStorage.getItem('comfyui-api-url') || 'http://127.0.0.1:8188';
    this.client = axios.create({
      baseURL: this.baseUrl,
    });
    this.clientId = this.generateClientId();
    this.ws = null;
    this.wsCallbacks = {
      onProgress: null,
      onExecuted: null,
      onError: null
    };
    this.useWebSocket = true; // 是否使用WebSocket
    this.pollingInterval = null; // 轮询间隔ID
    this.pollingDelay = 1000; // 轮询延迟（毫秒）
    this.wsReconnectAttempts = 0; // 添加WebSocket重连尝试次数计数
    this.maxReconnectAttempts = 3; // 最大重连尝试次数
    this.forcePollingMode = false; // 是否强制使用轮询模式
    this.setupWebSocket();
  }

  // 生成唯一的客户端ID
  generateClientId() {
    return `web-ui-${Math.random().toString(36).substring(2, 15)}`;
  }

  setBaseUrl(url) {
    this.baseUrl = url;
    this.client = axios.create({
      baseURL: url,
    });
    localStorage.setItem('comfyui-api-url', url);
    // 重置WebSocket连接状态
    this.wsReconnectAttempts = 0;
    this.forcePollingMode = false;
    this.setupWebSocket();
  }

  getBaseUrl() {
    return this.baseUrl;
  }

  // 获取客户端ID
  getClientId() {
    return this.clientId;
  }

  // 设置WebSocket连接
  setupWebSocket() {
    // 如果强制使用轮询模式，则不尝试建立WebSocket连接
    if (this.forcePollingMode) {
      this.useWebSocket = false;
      return;
    }

    try {
      const wsUrl = this.baseUrl.replace('http://', 'ws://').replace('https://', 'wss://');
      this.ws = new WebSocket(`${wsUrl}/ws?clientId=${this.clientId}`);
      
      this.ws.onopen = () => {
        console.log('WebSocket连接已建立');
        this.useWebSocket = true;
        this.wsReconnectAttempts = 0; // 重置重连计数
        // 如果有轮询，停止轮询
        this.stopPolling();
      };
      
      this.ws.onmessage = (event) => {
        try {
          const message = JSON.parse(event.data);
          
          // 处理不同类型的消息
          if (message.type === 'progress') {
            // 进度更新
            if (this.wsCallbacks.onProgress) {
              this.wsCallbacks.onProgress(message.data);
            }
          } else if (message.type === 'executed') {
            // 执行完成
            if (this.wsCallbacks.onExecuted) {
              this.wsCallbacks.onExecuted(message.data);
            }
          } else if (message.type === 'execution_start') {
            console.log('开始执行工作流:', message.data);
            if (this.wsCallbacks.onExecuted) {
              this.wsCallbacks.onExecuted({
                type: 'execution_start',
                data: message.data
              });
            }
          } else if (message.type === 'execution_cached') {
            console.log('使用缓存结果:', message.data);
            if (this.wsCallbacks.onExecuted) {
              this.wsCallbacks.onExecuted(message.data);
            }
          } else if (message.type === 'executing') {
            console.log('正在执行节点:', message.data.node);
          } else if (message.type === 'execution_error') {
            console.error('执行出错:', message.data.exception_message);
            if (this.wsCallbacks.onError) {
              this.wsCallbacks.onError(new Error(message.data.exception_message));
            }
          } else if (message.type === 'status') {
            console.log('ComfyUI状态:', message.data);
          }
        } catch (error) {
          console.error('解析WebSocket消息失败:', error);
        }
      };
      
      this.ws.onerror = (error) => {
        console.error('WebSocket错误:', error);
        this.useWebSocket = false;
        
        // 增加重连尝试次数
        this.wsReconnectAttempts++;
        
        // 如果超过最大重连次数，切换到轮询模式并不再尝试WebSocket
        if (this.wsReconnectAttempts >= this.maxReconnectAttempts) {
          console.log(`WebSocket连接失败${this.maxReconnectAttempts}次，切换到HTTP轮询模式`);
          this.forcePollingMode = true;
        }
        
        // 只在第一次错误时通知用户
        if (this.wsReconnectAttempts === 1 && this.wsCallbacks.onError) {
          this.wsCallbacks.onError(error);
        }
      };
      
      this.ws.onclose = () => {
        console.log('WebSocket连接已关闭');
        this.useWebSocket = false;
        
        // 只有在未强制使用轮询模式且未超过最大重连次数时才尝试重连
        if (!this.forcePollingMode && this.wsReconnectAttempts < this.maxReconnectAttempts) {
          console.log(`尝试重新连接WebSocket (${this.wsReconnectAttempts + 1}/${this.maxReconnectAttempts})`);
          setTimeout(() => this.setupWebSocket(), 5000);
        } else if (this.wsReconnectAttempts >= this.maxReconnectAttempts) {
          console.log('已达到最大重连次数，将使用HTTP轮询模式');
          this.forcePollingMode = true;
        }
      };
    } catch (error) {
      console.error('设置WebSocket连接失败:', error);
      this.useWebSocket = false;
      this.wsReconnectAttempts++;
      
      // 如果超过最大重连次数，切换到轮询模式
      if (this.wsReconnectAttempts >= this.maxReconnectAttempts) {
        console.log('WebSocket连接失败多次，切换到HTTP轮询模式');
        this.forcePollingMode = true;
      }
    }
  }

  // 设置WebSocket回调
  setWebSocketCallbacks(callbacks) {
    this.wsCallbacks = { ...this.wsCallbacks, ...callbacks };
  }

  // 获取ComfyUI对象信息（模型、采样器等）
  async getObjectInfo() {
    try {
      const response = await this.client.get('/object_info');
      return response.data;
    } catch (error) {
      console.error('获取对象信息失败:', error);
      throw error;
    }
  }

  // 获取模型列表
  async getModels() {
    try {
      const objectInfo = await this.getObjectInfo();
      const models = objectInfo.CheckpointLoaderSimple.input.required.ckpt_name[0];
      
      // 如果有可用模型，存储第一个作为默认模型
      if (models && models.length > 0) {
        localStorage.setItem('default-model', models[0]);
      }
      
      return models;
    } catch (error) {
      console.error('获取模型列表失败:', error);
      throw error;
    }
  }

  // 获取采样器列表
  async getSamplers() {
    try {
      const objectInfo = await this.getObjectInfo();
      const samplers = objectInfo.KSampler.input.required.sampler_name[0];
      
      // 如果有可用采样器，存储第一个作为默认采样器
      if (samplers && samplers.length > 0) {
        localStorage.setItem('default-sampler', samplers[0]);
      }
      
      return samplers;
    } catch (error) {
      console.error('获取采样器列表失败:', error);
      throw error;
    }
  }

  // 获取当前队列状态
  async getQueue() {
    try {
      const response = await this.client.get('/queue');
      return response.data;
    } catch (error) {
      console.error('获取队列状态失败:', error);
      throw error;
    }
  }

  // 获取历史记录
  async getHistory() {
    try {
      const response = await this.client.get('/history');
      return response.data;
    } catch (error) {
      console.error('获取历史记录失败:', error);
      throw error;
    }
  }

  // 开始HTTP轮询
  startPolling(promptId) {
    if (this.pollingInterval) {
      this.stopPolling();
    }
    
    console.log('开始HTTP轮询获取进度和结果');
    
    // 记录上次进度
    let lastProgress = 0;
    
    this.pollingInterval = setInterval(async () => {
      try {
        // 获取队列状态
        const queueStatus = await this.getQueue();
        
        // 检查队列中是否有我们的任务
        const ourPrompt = queueStatus.queue_running.find(item => item.prompt_id === promptId);
        
        if (ourPrompt) {
          // 任务正在运行，获取进度
          if (ourPrompt.execution_status && ourPrompt.execution_status.processing_node) {
            const node = ourPrompt.execution_status.processing_node;
            const progress = {
              value: ourPrompt.execution_status.current_step || 0,
              max: ourPrompt.execution_status.total_steps || 1,
              node: node
            };
            
            // 只有当进度变化时才回调
            if (progress.value !== lastProgress) {
              lastProgress = progress.value;
              if (this.wsCallbacks.onProgress) {
                this.wsCallbacks.onProgress(progress);
              }
            }
          }
        } else {
          // 检查任务是否已完成
          const history = await this.getHistory();
          if (history[promptId]) {
            // 任务已完成
            if (this.wsCallbacks.onExecuted) {
              this.wsCallbacks.onExecuted({
                prompt_id: promptId,
                ...history[promptId]
              });
            }
            this.stopPolling();
          }
        }
      } catch (error) {
        console.error('轮询过程中出错:', error);
      }
    }, this.pollingDelay);
  }

  // 停止HTTP轮询
  stopPolling() {
    if (this.pollingInterval) {
      clearInterval(this.pollingInterval);
      this.pollingInterval = null;
    }
  }

  // 创建工作流并提交
  async generateImage(params) {
    try {
      // 构建ComfyUI工作流
      const workflow = this.buildWorkflow(params);
      
      // 构建正确的请求格式
      const requestData = {
        prompt: workflow,
        client_id: this.clientId
      };
      
      // 提交工作流
      const response = await this.client.post('/prompt', requestData);
      const promptId = response.data.prompt_id;
      
      // 如果WebSocket不可用，启动HTTP轮询
      if (!this.useWebSocket || this.forcePollingMode) {
        this.startPolling(promptId);
      }
      
      // 返回提示ID，用于后续查询结果
      return promptId;
    } catch (error) {
      console.error('生成图像失败:', error);
      throw error;
    }
  }

  // 构建ComfyUI工作流
  buildWorkflow(params) {
    const {
      prompt,
      negativePrompt,
      model,
      width,
      height,
      seed,
      samplingMethod,
      steps,
      cfgScale,
      workflow,
      initImage
    } = params;

    // 基础节点配置
    const baseNodes = {
      "4": {
        "inputs": {
          "ckpt_name": model
        },
        "class_type": "CheckpointLoaderSimple"
      },
      "6": {
        "inputs": {
          "text": prompt,
          "clip": ["4", 1]
        },
        "class_type": "CLIPTextEncode"
      },
      "7": {
        "inputs": {
          "text": negativePrompt,
          "clip": ["4", 1]
        },
        "class_type": "CLIPTextEncode"
      }
    };

    // 文生图工作流
    if (workflow === 'txt2img') {

      return {
        "3": {
          "inputs": {
            "seed": seed,
            "steps": steps,
            "cfg": cfgScale,
            "sampler_name": samplingMethod,
            "scheduler": "normal",
            "denoise": 1,
            "model": [
              "4",
              0
            ],
            "positive": [
              "6",
              0
            ],
            "negative": [
              "7",
              0
            ],
            "latent_image": [
              "5",
              0
            ]
          },
          "class_type": "KSampler",
          "_meta": {
            "title": "K采样器"
          }
        },
        "4": {
          "inputs": {
            "ckpt_name": model
          },
          "class_type": "CheckpointLoaderSimple",
          "_meta": {
            "title": "Checkpoint加载器"
          }
        },
        "5": {
          "inputs": {
            "width": width,
            "height": height,
            "batch_size": 1
          },
          "class_type": "EmptyLatentImage",
          "_meta": {
            "title": "空Latent图像"
          }
        },
        "6": {
          "inputs": {
            "text": prompt,
            "clip": [
              "4",
              1
            ]
          },
          "class_type": "CLIPTextEncode",
          "_meta": {
            "title": "CLIP文本编码"
          }
        },
        "7": {
          "inputs": {
            "text": negativePrompt ? negativePrompt :"(worst quality, low quality, sketch:1.1), nsfw, error, bad anatomy, bad hands, watermark, ugly, distorted, nipples, pussy, lowres, abstract, signature, bkub, jewelry, blush, hat, simple background, earrings, hair ornament, fur trim, dark, multiple views, 4koma, censored, poorly drawn hands,poorly drawn feet,poorly drawn face,out of frame, mutation,mutated,extra limbs,extra legs,extra arms,disfigured,deformed,cross-eye,bad art,bad anatomy,blurred,three fingers, four fingers, six fingers, seven fingers, extra fingers, missing fingers, fused fingers, deformed fingers, ugly fingers,deformed hands, bad hands, worst time, worst hands, wrong hands, twisted hands, ugly hands,deformed toes, fused toes, missing toes,wrong feet, deformed feet, ugly feet",
            "clip": [
              "4",
              1
            ]
          },
          "class_type": "CLIPTextEncode",
          "_meta": {
            "title": "CLIP文本编码"
          }
        },
        "8": {
          "inputs": {
            "samples": [
              "3",
              0
            ],
            "vae": [
              "4",
              2
            ]
          },
          "class_type": "VAEDecode",
          "_meta": {
            "title": "VAE解码"
          }
        },
        "9": {
          "inputs": {
            "filename_prefix": "ComfyUI_",
            "images": [
              "8",
              0
            ]
          },
          "class_type": "SaveImage",
          "_meta": {
            "title": "保存图像"
          }
        }
      }
      
    }
    // 图生图工作流
    else if (workflow === 'img2img' && initImage) {
      return {
        ...baseNodes,
        "1": {
          "inputs": {
            "image": initImage
          },
          "class_type": "LoadImage"
        },
        "2": {
          "inputs": {
            "image": ["1", 0],
            "vae": ["4", 2]
          },
          "class_type": "VAEEncode"
        },
        "3": {
          "inputs": {
            "seed": seed,
            "steps": steps,
            "cfg": cfgScale,
            "sampler_name": samplingMethod,
            "scheduler": "normal",
            "denoise": 0.8, // 图生图默认降噪强度
            "model": ["4", 0],
            "positive": ["6", 0],
            "negative": ["7", 0],
            "latent_image": ["2", 0]
          },
          "class_type": "KSampler"
        },
        "8": {
          "inputs": {
            "samples": ["3", 0],
            "vae": ["4", 2]
          },
          "class_type": "VAEDecode"
        },
        "9": {
          "inputs": {
            "filename_prefix": "output",
            "images": ["8", 0]
          },
          "class_type": "SaveImage"
        }
      };
    }

    throw new Error('Invalid workflow configuration');
  }

  // 获取图像结果
  async getImageResult(promptId) {
    return new Promise((resolve, reject) => {
      try {
        // 设置超时
        const timeout = setTimeout(() => {
          reject(new Error('Timeout waiting for image generation'));
        }, 60000); // 60秒超时
        
        // 如果WebSocket不可用或强制使用轮询模式，使用HTTP轮询获取结果
        if (!this.useWebSocket || this.forcePollingMode) {
          console.log('使用HTTP轮询获取结果');
          
          const checkResult = async () => {
            try {
              const history = await this.getHistory();
              const result = history[promptId];
              
              if (result && result.outputs) {
                for (const nodeId in result.outputs) {
                  const nodeOutput = result.outputs[nodeId];
                  if (nodeOutput.images && nodeOutput.images.length > 0) {
                    const image = nodeOutput.images[0];
                    const imageUrl = `${this.baseUrl}/view?filename=${image.filename}&subfolder=${encodeURIComponent(image.subfolder || '')}&type=${image.type || 'output'}`;
                    
                    clearTimeout(timeout);
                    resolve(imageUrl);
                    return;
                  }
                }
              }
              
              // 如果还没有结果，继续轮询
              setTimeout(checkResult, 2000);
            } catch (error) {
              console.error('HTTP轮询获取结果失败:', error);
              clearTimeout(timeout);
              reject(error);
            }
          };
          
          // 开始轮询检查结果
          checkResult();
          return;
        }
        
        // 保存原有的回调
        const originalCallbacks = { ...this.wsCallbacks };
        
        // 设置新的WebSocket回调
        this.setWebSocketCallbacks({
          onProgress: (data) => {
            // 继续调用原有的进度回调
            if (originalCallbacks.onProgress) {
              originalCallbacks.onProgress(data);
            }
          },
          onExecuted: async (data) => {
            // 检查是否执行完成 - 修复：应该检查是否是相关的promptId
            if (data.prompt_id === promptId) {
              try {
                console.log('工作流执行完成，获取结果');
                // 获取历史记录中的结果
                const history = await this.getHistory();
                const result = history[promptId];
                
                if (result && result.outputs) {
                  for (const nodeId in result.outputs) {
                    const nodeOutput = result.outputs[nodeId];
                    if (nodeOutput.images && nodeOutput.images.length > 0) {
                      const image = nodeOutput.images[0];
                      const imageUrl = `${this.baseUrl}/view?filename=${image.filename}&subfolder=${encodeURIComponent(image.subfolder || '')}&type=${image.type || 'output'}`;
                      
                      // 恢复原有回调
                      this.setWebSocketCallbacks(originalCallbacks);
                      clearTimeout(timeout);
                      resolve(imageUrl);
                      return;
                    }
                  }
                }
                
                // 如果执行完成但没有找到图像，可能是出错了
                console.error('未找到生成的图像:', result);
                reject(new Error('No image found in generation result'));
              } catch (error) {
                console.error('获取图像结果失败:', error);
                reject(error);
              } finally {
                // 恢复原有回调
                this.setWebSocketCallbacks(originalCallbacks);
                clearTimeout(timeout);
              }
            }
          },
          onError: (error) => {
            // 发生错误时
            clearTimeout(timeout);
            this.setWebSocketCallbacks(originalCallbacks);
            reject(error);
            
            // 调用原有的错误回调
            if (originalCallbacks.onError) {
              originalCallbacks.onError(error);
            }
          }
        });
      } catch (error) {
        reject(error);
      }
    });
  }
}

export default new ComfyApi();