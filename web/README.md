# AIGC 绘图工具

基于 ComfyUI API 构建的现代化 AIGC 绘图 Web 应用。

## 功能特点

- 基于 Vue 3 和 Element Plus 构建的现代化界面
- 支持 ComfyUI API 接口，利用 Stable Diffusion 模型生成图像
- 提供丰富的参数控制，满足各类创作需求
- 支持提示词和负面提示词输入
- 保存图像生成历史记录
- 支持图像下载和参数复用

## 开发设置

### 安装依赖

```bash
npm install
```

### 本地开发

```bash
npm run dev
```

### 生产构建

```bash
npm run build
```

## 使用说明

1. 确保您已安装并运行 ComfyUI 服务器，默认地址为 `http://127.0.0.1:8188`
2. 在应用设置中配置 ComfyUI API 地址
3. 在提示词框中输入您想要生成图像的描述
4. 可选：在负面提示词框中输入想要避免的元素
5. 调整参数（尺寸、种子、采样方法等）
6. 点击"生成图像"按钮

## ComfyUI 连接

此应用需要连接到 ComfyUI 服务器才能生成图像。如果您还没有安装 ComfyUI，可以按照以下步骤操作：

1. 克隆 ComfyUI 仓库：`git clone https://github.com/comfyanonymous/ComfyUI`
2. 按照 ComfyUI 官方文档进行安装和配置
3. 运行 ComfyUI 服务器：`python main.py`
4. 配置本应用的 API 地址为您的 ComfyUI 服务器地址

## 项目介绍

AIGC绘图应用是一个用户友好的AI绘图工具，它通过简洁直观的界面，使用户能够轻松地利用AI技术创建高质量图像。该应用基于ComfyUI的API构建，提供了更加友好的用户界面，使AI绘图变得简单易用。

### 主要特点

- 简洁直观的用户界面
- 支持多种AI模型和参数调整
- 提示词和负面提示词输入
- 图像历史记录和参数保存
- 与ComfyUI的无缝集成

## 技术栈

- **前端框架：** Vue.js 3
- **UI组件库：** Element Plus
- **HTTP客户端：** Axios
- **构建工具：** Vite
- **API：** ComfyUI API

## 安装与运行

### 前提条件

- Node.js 16+
- 已安装并运行的ComfyUI服务

### 安装步骤

1. 克隆仓库

```bash
git clone <repository-url>
cd web
```

2. 安装依赖

```bash
npm install
```

3. 启动开发服务器

```bash
npm run dev
```

4. 构建生产版本

```bash
npm run build
```

## ComfyUI简介

ComfyUI是一个功能强大的稳定扩散工作流程创建工具，它提供了灵活的节点编辑界面，允许用户创建复杂的图像生成工作流。与其他AI绘图工具相比，ComfyUI的主要优势在于其高度的可定制性和灵活性。

要使用本应用，您需要先安装并运行ComfyUI服务。详细的安装说明可以在[ComfyUI的GitHub仓库](https://github.com/comfyanonymous/ComfyUI)中找到。

## 许可证

[MIT](LICENSE)
