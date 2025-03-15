import { createRouter, createWebHistory } from 'vue-router'
import DrawingView from '../views/AigcDrawingView.vue'
import HistoryView from '../views/HistoryView.vue'
import HistoryGalleryView from '../views/HistoryGalleryView.vue'
import ScanDirectories from '../components/ScanDirectories.vue'
import SettingsView from '../views/SettingsView.vue'
import GalleryLayout from '../views/GalleryLayout.vue' // 添加导入

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/drawing'
    },
    {
      path: '/drawing',
      name: 'drawing',
      component: DrawingView,
      meta: {
        module: 'drawing'
      }
    },
    {
      path: '/gallery',
      name: 'gallery',
      component: GalleryLayout, // 添加组件
      redirect: '/gallery/images', // 确保默认重定向到images子路由
      meta: {
        module: 'gallery'
      },
      children: [
        {
          path: 'images',
          name: 'gallery-images',
          component: HistoryGalleryView
        },
        {
          path: 'history',
          name: 'gallery-history',
          component: HistoryView
        },
        {
          path: 'directories',
          name: 'gallery-directories',
          component: ScanDirectories
        }
      ]
    },
    {
      path: '/settings',
      name: 'settings',
      component: SettingsView,
      meta: {
        module: 'settings'
      }
    }
  ]
})

export default router
