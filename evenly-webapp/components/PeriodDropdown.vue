<template>
  <div class="relative" ref="dropdownRef">
    <button
      type="button"
      @click="toggleDropdown"
      class="px-3 py-1.5 bg-slate-800/80 ring-1 ring-white/10 rounded-xl text-sm text-gray-200 font-medium flex items-center gap-1.5 hover:bg-slate-800 transition-colors"
    >
      <span>{{ selectedLabel }}</span>
      <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
      </svg>
    </button>

    <div
      v-if="open"
      class="absolute right-0 top-full mt-1.5 w-56 bg-slate-800 ring-1 ring-white/10 rounded-lg shadow-lg z-20 overflow-hidden"
      @click.stop
    >
      <div class="py-1">
        <button
          v-for="option in periodOptions"
          :key="option.value"
          @click="select(option.value)"
          class="w-full px-3 py-2.5 text-left text-sm text-gray-200 hover:bg-slate-700/50 transition-colors flex items-center justify-between"
          :class="{ 'bg-slate-700/30': modelValue === option.value }"
        >
          <span>{{ option.label }}</span>
          <svg
            v-if="modelValue === option.value"
            class="w-4 h-4 text-emerald-400"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
          </svg>
        </button>
      </div>

      <div v-if="modelValue === 'custom'" class="border-t border-white/10 bg-slate-900/60 p-3">
        <DateRangePicker
          :model-value="{ start: internalRange.start, end: internalRange.end }"
          :max-date="maxDate"
          :auto-open="true"
          @apply="applyCustomRange"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
type PeriodType = 'month' | 'week' | 'all' | 'custom'

interface Props {
  modelValue?: PeriodType
  range?: { start: string | null; end: string | null }
  maxDate?: string
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: 'month',
  range: () => ({ start: null, end: null }),
  maxDate: () => new Date().toISOString().split('T')[0]
})

const emit = defineEmits<{
  'update:modelValue': [value: PeriodType]
  'update:range': [{ start: string | null; end: string | null }]
  'period-change': [period: PeriodType, range?: { start: string | null; end: string | null }]
}>()

const { t } = useI18n()

const open = ref(false)
const dropdownRef = ref<HTMLElement | null>(null)
const internalRange = reactive<{ start: string | null; end: string | null }>({
  start: props.range.start,
  end: props.range.end
})

watch(() => props.range, (val) => {
  internalRange.start = val.start
  internalRange.end = val.end
}, { deep: true })

const periodOptions = computed(() => [
  { value: 'month' as PeriodType, label: t('expenses.thisMonth') },
  { value: 'week' as PeriodType, label: t('expenses.thisWeek') },
  { value: 'all' as PeriodType, label: t('expenses.allTime') },
  { value: 'custom' as PeriodType, label: t('expenses.custom') }
])

const selectedLabel = computed(() => {
  if (props.modelValue === 'custom') {
    const start = internalRange.start ? new Date(internalRange.start).toLocaleDateString() : '...'
    const end = internalRange.end ? new Date(internalRange.end).toLocaleDateString() : '...'
    return `${start} - ${end}`
  }
  return periodOptions.value.find(opt => opt.value === props.modelValue)?.label || t('expenses.thisMonth')
})

const toggleDropdown = () => {
  open.value = !open.value
}

const closeDropdown = () => {
  open.value = false
}

const select = (period: PeriodType) => {
  emit('update:modelValue', period)
  emit('period-change', period, period === 'custom' ? internalRange : undefined)
  if (period !== 'custom') {
    closeDropdown()
  }
}

const applyCustomRange = (range: { start: string | null; end: string | null }) => {
  internalRange.start = range.start
  internalRange.end = range.end
  emit('update:range', { ...range })
  emit('period-change', 'custom', range)
  closeDropdown()
}

if (process.client) {
  onMounted(() => {
    const handleClickOutside = (e: MouseEvent) => {
      const target = e.target as HTMLElement
      
      // Check if click is within the dropdown container
      if (dropdownRef.value && dropdownRef.value.contains(target)) {
        return
      }
      
      // Check if click is within the DateRangePicker calendar (teleported to body)
      // The calendar has z-index 9999 and is positioned fixed
      let element: HTMLElement | null = target
      while (element && element !== document.body) {
        const styles = window.getComputedStyle(element)
        const zIndex = styles.zIndex
        
        // Check if this element is the calendar (has z-index 9999, is fixed, and has calendar-like classes)
        if (zIndex === '9999' && 
            styles.position === 'fixed' && 
            (element.classList.contains('bg-slate-800') || element.querySelector('.grid.grid-cols-7'))) {
          return
        }
        element = element.parentElement
      }
      
      // If none of the above, close the dropdown
      closeDropdown()
    }
    document.addEventListener('click', handleClickOutside)
    onUnmounted(() => {
      document.removeEventListener('click', handleClickOutside)
    })
  })
}
</script>
