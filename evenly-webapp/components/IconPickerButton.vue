<template>
  <div>
    <!-- Button to open icon picker -->
    <button
      type="button"
      @click="showPicker = true"
      :class="[
        'w-full px-4 py-4 bg-slate-800 border border-slate-700 rounded-2xl text-white focus:outline-none focus:ring-2 focus:ring-emerald-500 min-h-[56px] touch-manipulation text-base flex items-center justify-between',
        buttonClass
      ]"
    >
      <div class="flex items-center gap-3 flex-1 min-w-0">
        <div
          v-if="modelValue"
          class="w-10 h-10 rounded-full flex items-center justify-center flex-shrink-0"
          :style="{ background: color || 'linear-gradient(135deg, #64748b 0%, #475569 100%)' }"
        >
          <FontAwesomeIcon
            :icon="getFontAwesomeIcon(modelValue)"
            class="w-5 h-5 text-white"
          />
        </div>
        <div v-else class="w-10 h-10 rounded-full bg-slate-700/50 border-2 border-dashed border-slate-600 flex items-center justify-center flex-shrink-0">
          <svg class="w-5 h-5 text-slate-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
          </svg>
        </div>
        <span :class="[
          'text-left flex-1 truncate',
          modelValue ? 'text-white' : 'text-slate-500'
        ]">
          {{ modelValue ? getIconName(modelValue) : (placeholder || t('settings.categories.selectIcon') || 'Select an icon') }}
        </span>
      </div>
      <svg class="w-5 h-5 text-slate-400 flex-shrink-0 ml-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
      </svg>
    </button>

    <!-- Icon Picker in BottomSheet -->
    <BottomSheet v-model="showPicker" :title="title || t('settings.categories.selectIcon') || 'Select an Icon'">
      <IconPicker
        :model-value="modelValue"
        @update:model-value="handleIconSelect"
        placeholder="Search icons..."
        max-height="500px"
        :columns="6"
      />
    </BottomSheet>
  </div>
</template>

<script setup lang="ts">
import { useFontAwesome } from '~/composables/useFontAwesome'

interface Props {
  modelValue?: string
  placeholder?: string
  title?: string
  color?: string
  buttonClass?: string
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: '',
  placeholder: undefined,
  title: undefined,
  color: undefined,
  buttonClass: ''
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const { t } = useI18n()
const { parseIconClass } = useFontAwesome()

const showPicker = ref(false)

// Get Font Awesome icon from class string
const getFontAwesomeIcon = (iconClass: string | null | undefined) => {
  if (!iconClass) return ['fas', 'ellipsis']
  const parsed = parseIconClass(iconClass)
  if (!parsed) {
    return ['fas', 'ellipsis']
  }
  return [parsed.prefix, parsed.icon]
}

// Get icon name from class string (extract the icon name)
const getIconName = (iconClass: string) => {
  if (!iconClass) return ''
  const parts = iconClass.split(' ')
  const iconPart = parts.find(p => p.startsWith('fa-') && !p.includes('solid') && !p.includes('regular') && !p.includes('brands'))
  if (!iconPart) return iconClass
  return iconPart.replace('fa-', '').replace(/-/g, ' ').replace(/\b\w/g, l => l.toUpperCase())
}

// Handle icon selection
const handleIconSelect = (value: string) => {
  emit('update:modelValue', value)
  // Close picker after selection
  setTimeout(() => {
    showPicker.value = false
  }, 200)
}
</script>
