<template>
  <div class="space-y-2 relative">
    <label v-if="label" class="text-sm font-medium text-white/90 flex items-center gap-2">
      <slot name="icon">
        <component v-if="icon" :is="icon" class="w-4 h-4" />
      </slot>
      {{ label }}
      <span v-if="required" class="text-red-400">*</span>
    </label>
    
    <!-- Mobile: Use native select for small lists, custom for large lists -->
    <select
      v-if="!enableSearch || options.length <= 5"
      :value="modelValue === null ? '' : modelValue"
      :class="[inputClass, 'w-full bg-slate-800/50 border transition-colors rounded-xl px-4 py-3 text-white focus:outline-none focus:ring-2 focus:ring-emerald-500/50 focus:border-emerald-500/50 appearance-none min-h-[48px]', error ? 'border-red-500/50' : 'border-slate-700/50']"
      @change="handleSelectChange"
    >
      <option v-if="placeholder" value="">{{ placeholder }}</option>
      <option
        v-for="option in options"
        :key="getOptionValue(option)"
        :value="getOptionValue(option) === null ? '' : getOptionValue(option)"
      >
        {{ getOptionLabel(option) }}
      </option>
    </select>

    <!-- Desktop/Large lists: Custom searchable dropdown -->
    <div v-else class="relative">
      <button
        type="button"
        @click="toggleDropdown"
        :class="[inputClass, 'w-full bg-slate-800/50 border transition-colors rounded-xl px-4 py-3 text-left text-white focus:outline-none focus:ring-2 focus:ring-emerald-500/50 focus:border-emerald-500/50 flex items-center justify-between min-h-[48px]', error ? 'border-red-500/50' : 'border-slate-700/50']"
      >
        <span :class="!selectedLabel ? 'text-white/40' : ''">
          {{ selectedLabel || placeholder || 'Select...' }}
        </span>
        <svg
          class="w-5 h-5 text-white/40 transition-transform"
          :class="isOpen ? 'rotate-180' : ''"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
        </svg>
      </button>

      <!-- Dropdown Menu -->
      <Transition name="dropdown">
        <div
          v-if="isOpen"
          class="absolute z-50 w-full mt-2 bg-slate-800 border border-slate-700 rounded-xl shadow-xl max-h-64 overflow-hidden flex flex-col"
        >
          <!-- Search Input -->
          <div class="p-2 border-b border-slate-700/50">
            <div class="relative">
              <input
                v-model="searchQuery"
                type="text"
                placeholder="Search..."
                class="w-full bg-slate-900/50 border border-slate-700/50 rounded-lg px-3 py-2 pl-9 text-white text-sm placeholder:text-white/40 focus:outline-none focus:ring-2 focus:ring-emerald-500/50 focus:border-emerald-500/50"
                @click.stop
              />
              <svg
                class="w-4 h-4 text-white/40 absolute left-3 top-1/2 -translate-y-1/2"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
              </svg>
            </div>
          </div>

          <!-- Options List -->
          <div class="overflow-y-auto max-h-48">
        <button
          v-for="option in filteredOptions"
          :key="getOptionValue(option)"
          type="button"
          @click="selectOption(option)"
          class="w-full text-left px-4 py-3.5 hover:bg-slate-700/50 active:bg-slate-700 transition-colors flex items-center justify-between min-h-[48px] touch-manipulation"
          :class="isSelected(option) ? 'bg-emerald-500/20 text-emerald-300' : 'text-white'"
        >
              <span>{{ getOptionLabel(option) }}</span>
              <svg
                v-if="isSelected(option)"
                class="w-5 h-5 text-emerald-400"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
              </svg>
            </button>
            <div v-if="filteredOptions.length === 0" class="px-4 py-3 text-white/50 text-sm text-center">
              No results found
            </div>
          </div>
        </div>
      </Transition>
    </div>

    <p v-if="error" class="text-xs text-red-400 flex items-center gap-1">
      <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
      </svg>
      {{ error }}
    </p>
  </div>
</template>

<script setup lang="ts">
interface Props {
  modelValue: string | number | null | undefined
  options: any[]
  label?: string
  placeholder?: string
  error?: string
  required?: boolean
  enableSearch?: boolean
  getOptionLabel?: (option: any) => string
  getOptionValue?: (option: any) => string | number
  icon?: any
  inputClass?: string
}

const props = withDefaults(defineProps<Props>(), {
  label: undefined,
  placeholder: undefined,
  error: undefined,
  required: false,
  enableSearch: true,
  getOptionLabel: (option: any) => {
    if (typeof option === 'string' || typeof option === 'number') return String(option)
    return option.label || option.name || String(option)
  },
  getOptionValue: (option: any) => {
    if (typeof option === 'string' || typeof option === 'number') return option
    return option.value || option.id || String(option)
  },
  icon: undefined,
  inputClass: ''
})

const emit = defineEmits<{
  'update:modelValue': [value: string | number | null]
}>()

const isOpen = ref(false)
const searchQuery = ref('')

const selectedLabel = computed(() => {
  if (!props.modelValue) return null
  const selected = props.options.find(opt => props.getOptionValue(opt) === props.modelValue)
  return selected ? props.getOptionLabel(selected) : null
})

const filteredOptions = computed(() => {
  if (!searchQuery.value) return props.options
  const query = searchQuery.value.toLowerCase()
  return props.options.filter(option => {
    const label = props.getOptionLabel(option).toLowerCase()
    return label.includes(query)
  })
})

const isSelected = (option: any) => {
  return props.getOptionValue(option) === props.modelValue
}

const toggleDropdown = () => {
  isOpen.value = !isOpen.value
  if (isOpen.value) {
    searchQuery.value = ''
    // Focus search input on next tick
    nextTick(() => {
      const searchInput = document.querySelector('.searchable-select input') as HTMLInputElement
      if (searchInput) searchInput.focus()
    })
  }
}

const selectOption = (option: any) => {
  const value = props.getOptionValue(option)
  emit('update:modelValue', value)
  isOpen.value = false
  searchQuery.value = ''
}

const handleSelectChange = (event: Event) => {
  const target = event.target as HTMLSelectElement
  const value = target.value === '' || target.value === 'null' ? null : target.value
  emit('update:modelValue', value)
}

// Close dropdown when clicking outside
onMounted(() => {
  const handleClickOutside = (e: MouseEvent) => {
    const target = e.target as HTMLElement
    if (!target.closest('.relative')) {
      isOpen.value = false
    }
  }
  document.addEventListener('click', handleClickOutside)
  onUnmounted(() => {
    document.removeEventListener('click', handleClickOutside)
  })
})
</script>

<style scoped>
.dropdown-enter-active,
.dropdown-leave-active {
  transition: all 0.2s ease;
}

.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
</style>

