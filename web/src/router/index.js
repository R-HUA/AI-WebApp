import { createRouter, createWebHistory } from 'vue-router'
import DrawingView from '../views/AigcDrawingView.vue'
import HistoryView from '../views/HistoryView.vue'
import HistoryGalleryView from '../views/HistoryGalleryView.vue'
import ScanDirectories from '../components/ScanDirectories.vue'

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
      redirect: '/gallery/images',
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
    }
  ]
})

export default router
