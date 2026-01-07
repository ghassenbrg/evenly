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
  variant?: 'balance' | 'list-item' | 'expense-item' | 'category-item' | 'chart' | 'expense-snapshot-list' | 'button' | 'dropdown' | 'text' | 'custom'
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
</script>

