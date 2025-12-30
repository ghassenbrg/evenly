import { defineNuxtConfig } from 'nuxt/config'

export default defineNuxtConfig({
  ssr: false,
  devtools: { enabled: false },
  modules: ['@pinia/nuxt', '@nuxtjs/tailwindcss', '@vite-pwa/nuxt'],
  app: {
    head: {
      title: 'Evenly',
      meta: [
        { name: 'viewport', content: 'width=device-width, initial-scale=1, maximum-scale=1' },
        { name: 'theme-color', content: '#1e293b' },
        { name: 'description', content: 'Evenly - Smart expense splitting made simple' }
      ],
      link: [
        { rel: 'manifest', href: '/manifest.webmanifest' }
      ]
    },
    pageTransition: { name: 'page', mode: 'out-in' }
  },
  runtimeConfig: {
    public: {
      apiBase: process.env.NUXT_PUBLIC_API_BASE || 'http://localhost:8080'
    }
  },
  css: ['~/assets/tailwind.css'],
  pwa: {
    registerType: 'autoUpdate',
    manifest: {
      name: 'Evenly',
      short_name: 'Evenly',
      start_url: '/',
      display: 'standalone',
      background_color: '#1e293b',
      theme_color: '#1e293b',
      icons: [
        { src: '/icons/icon-192.png', sizes: '192x192', type: 'image/png' },
        { src: '/icons/icon-512.png', sizes: '512x512', type: 'image/png' }
      ]
    },
    workbox: {
      navigateFallback: '/',
      runtimeCaching: [
        {
          urlPattern: ({ url }) => url.pathname.startsWith('/api'),
          handler: 'NetworkFirst',
          options: {
            cacheName: 'api-cache',
            networkTimeoutSeconds: 5
          }
        }
      ]
    }
  },
  tailwindcss: {
    exposeConfig: true
  }
})
