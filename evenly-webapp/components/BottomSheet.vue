<template>
  <Teleport to="body">
    <Transition name="overlay">
      <div
        v-if="modelValue"
        class="fixed inset-0 z-50 bg-black/50 backdrop-blur-sm"
        @click.self="close"
      >
        <Transition name="sheet">
          <div
            v-if="modelValue"
            class="absolute bottom-0 left-0 right-0 bg-slate-900 rounded-t-3xl max-h-[90vh] overflow-hidden flex flex-col pb-safe"
          >
            <!-- Handle -->
            <div class="flex justify-center pt-3 pb-2">
              <div class="w-12 h-1.5 bg-slate-600 rounded-full"></div>
            </div>

            <!-- Header -->
            <div v-if="title || $slots.header" class="px-6 py-4 border-b border-slate-800">
              <slot name="header">
                <div v-if="title" class="flex items-center justify-between">
                  <h2 class="text-xl font-semibold text-white">{{ title }}</h2>
                  <button
                    @click="close"
                    class="p-2 text-slate-400 hover:text-white transition-colors"
                  >
                    <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                    </svg>
                  </button>
                </div>
              </slot>
            </div>

            <!-- Content -->
            <div class="flex-1 overflow-y-auto px-6 py-4 overflow-x-hidden">
              <slot />
            </div>

            <!-- Footer -->
            <div v-if="$slots.footer" class="px-6 py-4 border-t border-slate-800">
              <slot name="footer" />
            </div>
          </div>
        </Transition>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
interface Props {
  modelValue: boolean
  title?: string
}

const props = defineProps<Props>()
const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()

const close = () => {
  emit('update:modelValue', false)
}
</script>

<style scoped>
.overlay-enter-active,
.overlay-leave-active {
  transition: opacity 0.3s ease;
}

.overlay-enter-from,
.overlay-leave-to {
  opacity: 0;
}

.sheet-enter-active,
.sheet-leave-active {
  transition: transform 0.3s ease;
}

.sheet-enter-from,
.sheet-leave-to {
  transform: translateY(100%);
}
</style>

