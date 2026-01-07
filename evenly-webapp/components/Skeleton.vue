<template>
  <!-- Balance Card Skeleton -->
  <div v-if="variant === 'balance'" class="space-y-4">
    <div class="flex items-center justify-between">
      <div class="flex flex-col space-y-2 flex-1">
        <div class="h-4 w-24 bg-slate-700/50 rounded animate-pulse"></div>
        <div class="h-9 w-40 bg-slate-600/50 rounded animate-pulse"></div>
        <div class="h-4 w-32 bg-slate-700/50 rounded animate-pulse"></div>
      </div>
      <div class="w-30 h-20 bg-slate-700/30 rounded-lg animate-pulse"></div>
    </div>
    <div class="space-y-2">
      <div class="h-4 w-20 bg-slate-700/50 rounded animate-pulse"></div>
      <div class="w-full h-2.5 bg-slate-800 rounded-full overflow-hidden">
        <div class="h-full w-1/3 bg-emerald-500/30 rounded-full animate-pulse"></div>
      </div>
      <div class="h-3 w-32 bg-slate-700/50 rounded animate-pulse ml-auto"></div>
    </div>
    <div class="w-full h-12 bg-slate-700/30 rounded-xl animate-pulse"></div>
  </div>

  <!-- List Item Skeleton -->
  <div v-else-if="variant === 'list-item'" class="flex items-center justify-between py-3">
    <div class="flex items-center gap-3 flex-1">
      <div class="w-11 h-11 rounded-full bg-slate-700/50 animate-pulse"></div>
      <div class="flex-1 space-y-2">
        <div class="h-4 w-32 bg-slate-700/50 rounded animate-pulse"></div>
        <div class="h-3 w-20 bg-slate-700/30 rounded animate-pulse"></div>
      </div>
    </div>
    <div class="h-4 w-20 bg-slate-700/50 rounded animate-pulse"></div>
  </div>

  <!-- Expense List Item Skeleton -->
  <div v-else-if="variant === 'expense-item'" class="flex items-center justify-between py-3">
    <div class="flex items-center gap-3 flex-1">
      <div class="w-11 h-11 rounded-full bg-slate-700/50 animate-pulse"></div>
      <div class="flex-1 space-y-2 min-w-0">
        <div class="h-5 w-32 bg-slate-700/50 rounded animate-pulse"></div>
        <div class="h-3 w-40 bg-slate-700/30 rounded animate-pulse"></div>
        <div class="h-3 w-24 bg-slate-700/20 rounded animate-pulse"></div>
      </div>
    </div>
    <div class="h-5 w-20 bg-slate-700/50 rounded animate-pulse flex-shrink-0"></div>
  </div>

  <!-- Category List Item Skeleton (with separator) -->
  <template v-else-if="variant === 'category-item'">
    <div class="flex items-center justify-between py-3">
      <div class="flex items-center gap-3 flex-1">
        <div class="w-11 h-11 rounded-full bg-slate-700/50 animate-pulse"></div>
        <div class="flex-1 space-y-2">
          <div class="h-4 w-32 bg-slate-700/50 rounded animate-pulse"></div>
          <div class="h-3 w-20 bg-slate-700/30 rounded animate-pulse"></div>
        </div>
      </div>
      <div class="h-4 w-20 bg-slate-700/50 rounded animate-pulse"></div>
    </div>
    <div v-if="showSeparator" class="h-px bg-white/10 ml-14 mr-0"></div>
  </template>

  <!-- Chart Skeleton -->
  <div v-else-if="variant === 'chart'" class="flex-shrink-0 flex flex-col items-center">
    <div class="w-44 h-44 rounded-full bg-slate-700/30 animate-pulse"></div>
    <div class="mt-3 h-4 w-16 bg-slate-700/50 rounded animate-pulse"></div>
  </div>

  <!-- Expense Snapshot List Skeleton -->
  <div v-else-if="variant === 'expense-snapshot-list'" class="flex-1 space-y-3">
    <div v-for="i in count" :key="i" class="flex items-center gap-3">
      <div class="w-11 h-11 rounded-full bg-slate-700/50 animate-pulse"></div>
      <div class="w-px h-8 bg-slate-700/30"></div>
      <div class="flex-1 space-y-2">
        <div class="h-4 w-24 bg-slate-700/50 rounded animate-pulse"></div>
        <div class="h-3 w-12 bg-slate-700/30 rounded animate-pulse"></div>
      </div>
    </div>
  </div>

  <!-- Expenses Total Card Skeleton -->
  <div v-else-if="variant === 'expenses-total-card'" class="space-y-3">
    <!-- Total Amount -->
    <div class="mb-3">
      <div class="h-9 w-32 bg-slate-700/50 rounded animate-pulse"></div>
    </div>

    <!-- Indicators Row -->
    <div class="grid grid-cols-3 gap-3 mb-3">
      <div class="space-y-1">
        <div class="h-3 w-16 bg-slate-700/40 rounded animate-pulse"></div>
        <div class="h-5 w-12 bg-slate-700/50 rounded animate-pulse"></div>
      </div>
      <div class="space-y-1">
        <div class="h-3 w-20 bg-slate-700/40 rounded animate-pulse"></div>
        <div class="h-5 w-16 bg-slate-700/50 rounded animate-pulse"></div>
      </div>
      <div class="space-y-1">
        <div class="h-3 w-16 bg-slate-700/40 rounded animate-pulse"></div>
        <div class="h-5 w-14 bg-slate-700/50 rounded animate-pulse"></div>
      </div>
    </div>

    <!-- Chart Section with wavy line pattern -->
    <div class="w-full" style="height: 80px; position: relative; overflow: hidden;">
      <!-- Wavy line pattern to simulate chart -->
      <svg class="w-full h-full" viewBox="0 0 300 80" preserveAspectRatio="none">
        <defs>
          <linearGradient :id="`skeletonGradient-${skeletonId}`" x1="0%" y1="0%" x2="100%" y2="0%">
            <stop offset="0%" style="stop-color:#ef4444;stop-opacity:0.4" />
            <stop offset="100%" style="stop-color:#14b8a6;stop-opacity:0.4" />
          </linearGradient>
          <linearGradient :id="`skeletonGradientFill-${skeletonId}`" x1="0%" y1="0%" x2="100%" y2="0%">
            <stop offset="0%" style="stop-color:#ef4444;stop-opacity:0.15" />
            <stop offset="100%" style="stop-color:#14b8a6;stop-opacity:0.15" />
          </linearGradient>
        </defs>
        <!-- Area fill -->
        <path
          d="M 0 72 Q 75 40, 150 50 T 300 60 L 300 80 L 0 80 Z"
          :fill="`url(#skeletonGradientFill-${skeletonId})`"
          class="animate-pulse"
        />
        <!-- Line -->
        <path
          d="M 0 72 Q 75 40, 150 50 T 300 60"
          fill="none"
          :stroke="`url(#skeletonGradient-${skeletonId})`"
          stroke-width="2"
          stroke-linecap="round"
          stroke-linejoin="round"
          class="animate-pulse"
        />
      </svg>
    </div>
  </div>

  <!-- Button Skeleton -->
  <div v-else-if="variant === 'button'" class="w-full h-12 bg-slate-700/30 rounded-xl animate-pulse"></div>

  <!-- Dropdown Skeleton -->
  <div v-else-if="variant === 'dropdown'" class="h-8 w-24 bg-slate-700/50 rounded-lg animate-pulse"></div>

  <!-- Text Skeleton -->
  <div v-else-if="variant === 'text'" :class="['bg-slate-700/50 rounded animate-pulse', textClass]"></div>

  <!-- Custom/Default Skeleton -->
  <div v-else :class="['bg-slate-700/50 rounded animate-pulse', customClass]"></div>
</template>

<script setup lang="ts">
interface Props {
  variant?: 'balance' | 'list-item' | 'expense-item' | 'category-item' | 'chart' | 'expense-snapshot-list' | 'expenses-total-card' | 'button' | 'dropdown' | 'text' | 'custom'
  count?: number
  showSeparator?: boolean
  textClass?: string
  customClass?: string
}

withDefaults(defineProps<Props>(), {
  variant: 'custom',
  count: 4,
  showSeparator: true,
  textClass: 'h-4 w-24',
  customClass: 'h-4 w-full'
})

// Unique ID for gradients (to avoid conflicts if multiple instances)
const skeletonId = Math.random().toString(36).substring(7)
</script>

