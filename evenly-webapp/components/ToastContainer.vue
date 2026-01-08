<template>
  <div class="fixed top-0 left-0 right-0 z-[100] pointer-events-none pt-safe">
    <div class="max-w-md mx-auto px-4 pt-4 space-y-2">
      <TransitionGroup name="toast">
        <div
          v-for="toast in toasts"
          :key="toast.id"
          class="pointer-events-auto rounded-xl shadow-lg p-4 backdrop-blur-sm"
          :class="{
            'bg-emerald-500/90 text-white': toast.type === 'success',
            'bg-red-500/90 text-white': toast.type === 'error',
            'bg-blue-500/90 text-white': toast.type === 'info'
          }"
        >
          <div class="flex items-center justify-between">
            <p class="text-sm font-medium">{{ toast.message }}</p>
            <button
              @click="remove(toast.id)"
              class="ml-4 text-white/80 hover:text-white"
            >
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>
        </div>
      </TransitionGroup>
    </div>
  </div>
</template>

<script setup lang="ts">
const { toasts, remove } = useToast()
</script>

<style scoped>
.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s ease;
}

.toast-enter-from {
  opacity: 0;
  transform: translateY(-20px);
}

.toast-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}
</style>

