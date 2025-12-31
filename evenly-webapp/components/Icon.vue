<template>
  <div
    class="inline-flex items-center justify-center"
    :style="{ width: size + 'px', height: size + 'px', color: color }"
  >
    <!-- Simple icon mapping for common icons -->
    <svg
      v-if="name === 'cart' || name === 'mdi:cart'"
      fill="none"
      stroke="currentColor"
      viewBox="0 0 24 24"
      class="w-full h-full"
    >
      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" />
    </svg>
    <svg
      v-else-if="name === 'lightning' || name === 'mdi:lightning-bolt'"
      fill="none"
      stroke="currentColor"
      viewBox="0 0 24 24"
      class="w-full h-full"
    >
      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
    </svg>
    <svg
      v-else-if="name === 'restaurant' || name === 'mdi:silverware-fork-knife'"
      fill="none"
      stroke="currentColor"
      viewBox="0 0 24 24"
      class="w-full h-full"
    >
      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" />
    </svg>
    <svg
      v-else-if="name === 'car' || name === 'mdi:car'"
      fill="none"
      stroke="currentColor"
      viewBox="0 0 24 24"
      class="w-full h-full"
    >
      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7v8a2 2 0 002 2h6M8 7V5a2 2 0 012-2h4.586a1 1 0 01.707.293l4.414 4.414a1 1 0 01.293.707V15a2 2 0 01-2 2h-2M8 7H6a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2v-2" />
    </svg>
    <svg
      v-else-if="name === 'movie' || name === 'mdi:movie'"
      fill="none"
      stroke="currentColor"
      viewBox="0 0 24 24"
      class="w-full h-full"
    >
      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 10l4.553-2.276A1 1 0 0121 8.618v6.764a1 1 0 01-1.447.894L15 14M5 18h8a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v8a2 2 0 002 2z" />
    </svg>
    <svg
      v-else-if="name === 'dots' || name === 'mdi:dots-horizontal'"
      fill="none"
      stroke="currentColor"
      viewBox="0 0 24 24"
      class="w-full h-full"
    >
      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 12h.01M12 12h.01M19 12h.01M6 12a1 1 0 11-2 0 1 1 0 012 0zm7 0a1 1 0 11-2 0 1 1 0 012 0zm7 0a1 1 0 11-2 0 1 1 0 012 0z" />
    </svg>
    <svg
      v-else-if="name === 'home' || name === 'mdi:home'"
      fill="none"
      stroke="currentColor"
      viewBox="0 0 24 24"
      class="w-full h-full"
    >
      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
    </svg>
    <svg
      v-else-if="name === 'wifi' || name === 'mdi:wifi'"
      fill="none"
      stroke="currentColor"
      viewBox="0 0 24 24"
      class="w-full h-full"
    >
      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8.111 16.404a5.5 5.5 0 017.778 0M12 20h.01m-7.08-7.071c3.904-3.905 10.236-3.905 14.141 0M1.394 9.393c5.857-5.857 15.355-5.857 21.213 0" />
    </svg>
    <span v-else class="text-xs">{{ name }}</span>
  </div>
</template>

<script setup lang="ts">
interface Props {
  name: string
  size?: number
  color?: string
}

withDefaults(defineProps<Props>(), {
  size: 24,
  color: 'currentColor'
})
</script>

