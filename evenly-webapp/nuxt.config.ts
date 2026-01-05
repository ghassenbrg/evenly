import { defineNuxtConfig } from 'nuxt/config'

export default defineNuxtConfig({
  compatibilityDate: '2026-01-03',
  ssr: false,
  devtools: { enabled: false },
  modules: ['@pinia/nuxt', '@nuxtjs/tailwindcss', '@vite-pwa/nuxt', '@nuxtjs/i18n'],
  postcss: {
    plugins: {
      tailwindcss: {},
      autoprefixer: {}
    }
  },
  app: {
    head: {
      title: 'Evenly',
      meta: [
        { name: 'viewport', content: 'width=device-width, initial-scale=1, maximum-scale=1' },
        { name: 'theme-color', content: '#1e293b' },
        { name: 'description', content: 'Evenly - Smart expense splitting made simple' },
        { name: 'mobile-web-app-capable', content: 'yes' },
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
    pageTransition: false
  },
  runtimeConfig: {
    public: {
      apiBase: process.env.NUXT_PUBLIC_API_BASE || 'http://localhost:8080',
      pockitoApiBase: process.env.NUXT_PUBLIC_POCKITO_API_BASE || process.env.NUXT_PUBLIC_API_BASE,
      keycloak: {
        url: process.env.NUXT_PUBLIC_KEYCLOAK_URL || 'http://localhost:9090',
        realm: process.env.NUXT_PUBLIC_KEYCLOAK_REALM || 'evenly',
        clientId: process.env.NUXT_PUBLIC_KEYCLOAK_CLIENT_ID || 'evenly-web'
      }
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
      navigateFallbackDenylist: [/^\/api/, /^\/_nuxt/],
      globPatterns: ['**/*.{js,css,html,png,svg,jpg,jpeg,gif,webp,woff,woff2}'],
      cleanupOutdatedCaches: true,
      skipWaiting: true,
      clientsClaim: true,
      // Suppress non-precached-url errors for navigation routes
      // The root route is handled by navigateFallback
      runtimeCaching: [
        {
          urlPattern: ({ url }) => url.pathname.startsWith('/api'),
          handler: 'NetworkFirst',
          options: {
            cacheName: 'api-cache',
            networkTimeoutSeconds: 5
          }
        },
        {
          urlPattern: ({ url }) => url.pathname === '/',
          handler: 'NetworkFirst',
          options: {
            cacheName: 'root-cache'
          }
        }
      ],
      // Customize the service worker to handle root route properly
      importScripts: []
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
