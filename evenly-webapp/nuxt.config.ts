import { defineNuxtConfig } from 'nuxt/config'

export default defineNuxtConfig({
  ssr: false,
  devtools: { enabled: false },
  modules: ['@pinia/nuxt', '@nuxtjs/tailwindcss', '@vite-pwa/nuxt', '@nuxtjs/i18n'],
  app: {
    head: {
      title: 'Evenly',
      meta: [
        { name: 'viewport', content: 'width=device-width, initial-scale=1, maximum-scale=1' },
        { name: 'theme-color', content: '#1e293b' },
        { name: 'description', content: 'Evenly - Smart expense splitting made simple' },
        { name: 'apple-mobile-web-app-capable', content: 'yes' },
        { name: 'apple-mobile-web-app-status-bar-style', content: 'black-translucent' },
        { name: 'apple-mobile-web-app-title', content: 'Evenly' }
      ],
      link: [
        { rel: 'manifest', href: '/manifest.webmanifest' },
        { rel: 'icon', type: 'image/png', href: '/icons/favicon.png' },
        { rel: 'apple-touch-icon', href: '/icons/favicon.png' },
        { rel: 'apple-touch-icon', sizes: '180x180', href: '/icons/favicon.png' }
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
        { src: '/icons/favicon.png', sizes: '192x192', type: 'image/png', purpose: 'any' },
        { src: '/icons/favicon.png', sizes: '512x512', type: 'image/png', purpose: 'any' },
        { src: '/icons/favicon.png', sizes: '192x192', type: 'image/png', purpose: 'maskable' },
        { src: '/icons/favicon.png', sizes: '512x512', type: 'image/png', purpose: 'maskable' }
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
  },
  i18n: {
    locales: [
      {
        code: 'en',
        iso: 'en-US',
        name: 'English',
        file: 'en.json'
      },
      {
        code: 'ja',
        iso: 'ja-JP',
        name: '日本語',
        file: 'ja.json'
      }
    ],
    langDir: 'locales',
    defaultLocale: 'en',
    strategy: 'no_prefix',
    detectBrowserLanguage: {
      useCookie: true,
      cookieKey: 'i18n_redirected',
      redirectOn: 'root',
      alwaysRedirect: false,
      fallbackLocale: 'en'
    }
  }
})
