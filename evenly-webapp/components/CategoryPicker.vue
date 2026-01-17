<template>
  <div class="relative" ref="dropdownRef">
    <label v-if="label" class="block text-sm font-medium text-slate-300 mb-2">
      {{ label }}
      <span v-if="required" class="text-red-400">*</span>
    </label>
    
    <!-- Dropdown Button -->
    <button
      type="button"
      @click="toggleDropdown"
      :disabled="loading || props.disabled"
      class="w-full px-4 py-4 bg-slate-800 border border-slate-700 rounded-2xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-emerald-500 min-h-[56px] touch-manipulation text-base flex items-center justify-between transition-colors"
      :class="{
        'border-red-500': showError && required && !modelValue,
        'hover:border-slate-600': !loading && !props.disabled,
        'opacity-50 cursor-not-allowed': loading || props.disabled
      }"
    >
      <div class="flex items-center gap-3 flex-1 min-w-0">
        <div v-if="selectedCategory" class="flex items-center gap-3 flex-1 min-w-0">
          <div
            class="w-8 h-8 rounded-full flex items-center justify-center flex-shrink-0"
            :style="{ background: colorToGradient(selectedCategory.color || '#64748b') }"
          >
            <FontAwesomeIcon
              :icon="getFontAwesomeIcon(selectedCategory.icon || 'fa-solid fa-ellipsis')"
              class="w-4 h-4 text-white"
            />
          </div>
          <span class="text-white font-medium truncate">{{ selectedCategory.name }}</span>
        </div>
        <span v-else class="text-slate-400">{{ placeholder || t('categories.selectCategory') || 'Select a category' }}</span>
      </div>
      <div class="flex items-center gap-2 flex-shrink-0">
        <div v-if="loading" class="animate-spin rounded-full h-4 w-4 border-b-2 border-emerald-500"></div>
        <svg class="w-5 h-5 text-slate-400 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
        </svg>
      </div>
    </button>

    <!-- Error Message -->
    <p v-if="showError && required && !modelValue" class="text-xs text-red-400 mt-2">
      {{ errorMessage || t('expenses.categoryRequired') || 'Please select a category' }}
    </p>

    <!-- Dropdown Menu -->
    <Transition name="dropdown">
      <div
        v-if="open && !loading"
        class="absolute left-0 right-0 top-full mt-2 bg-slate-800 border border-slate-700 rounded-2xl shadow-lg z-50 overflow-hidden"
        @click.stop
      >
        <!-- Search Input -->
        <div class="p-3 border-b border-slate-700">
          <div class="relative">
            <input
              v-model="searchQuery"
              type="text"
              :placeholder="t('categories.searchPlaceholder') || 'Search categories...'"
              class="w-full px-4 py-2.5 pl-10 bg-slate-900 border border-slate-700 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-emerald-500 text-sm"
              @click.stop
            />
            <svg
              class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
          </div>
        </div>

        <!-- Categories List -->
        <div class="max-h-64 overflow-y-auto">
          <div v-if="filteredCategories.length > 0" class="py-1">
            <button
              v-for="category in filteredCategories"
              :key="category.id"
              type="button"
              @click="handleCategorySelect(category.id)"
              class="w-full px-4 py-3 text-left hover:bg-slate-700/50 transition-colors flex items-center gap-3"
              :class="{ 'bg-slate-700/30': modelValue === category.id }"
            >
              <div
                class="w-10 h-10 rounded-full flex items-center justify-center flex-shrink-0"
                :style="{ background: colorToGradient(category.color || '#64748b') }"
              >
                <FontAwesomeIcon
                  :icon="getFontAwesomeIcon(category.icon || 'fa-solid fa-ellipsis')"
                  class="w-5 h-5 text-white"
                />
              </div>
              <span class="text-white font-medium flex-1">{{ category.name }}</span>
              <svg
                v-if="modelValue === category.id"
                class="w-5 h-5 text-emerald-400 flex-shrink-0"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
              </svg>
            </button>
          </div>
          
          <!-- No Results -->
          <div v-else class="px-4 py-8 text-center text-slate-400">
            <p>{{ t('categories.noResults') || 'No categories found' }}</p>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup lang="ts">
import type { Category } from '~/types/api'
import { useCategories } from '~/composables/useCategories'
import { useFontAwesome } from '~/composables/useFontAwesome'
import { useCategoryColor } from '~/composables/useCategoryColor'

interface Props {
  modelValue: string | null
  categories?: Category[]
  label?: string
  required?: boolean
  showError?: boolean
  errorMessage?: string
  autoLoad?: boolean
  placeholder?: string
  disabled?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  required: false,
  showError: false,
  autoLoad: true
})

const emit = defineEmits<{
  'update:modelValue': [value: string | null]
}>()

const { t } = useI18n()
const { parseIconClass } = useFontAwesome()
const { colorToGradient } = useCategoryColor()
const { categories: fetchedCategories, loading, fetchCategories } = useCategories()

const open = ref(false)
const searchQuery = ref('')
const dropdownRef = ref<HTMLElement | null>(null)

// Use provided categories or fetch them
const categories = computed(() => {
  if (props.categories && props.categories.length > 0) {
    return props.categories
  }
  return fetchedCategories.value
})

// Filter categories based on search query
const filteredCategories = computed(() => {
  if (!searchQuery.value.trim()) {
    return categories.value
  }
  const query = searchQuery.value.toLowerCase().trim()
  return categories.value.filter(category =>
    category.name.toLowerCase().includes(query)
  )
})

// Get selected category
const selectedCategory = computed(() => {
  if (!props.modelValue) return null
  return categories.value.find(c => c.id === props.modelValue) || null
})

// Load categories when autoLoad is true
watch(() => props.autoLoad, async (autoLoad) => {
  if (autoLoad && !props.categories) {
    try {
      await fetchCategories()
    } catch (err) {
      console.error('Failed to fetch categories:', err)
    }
  }
}, { immediate: true })

// Helper to get Font Awesome icon from class string
const getFontAwesomeIcon = (iconClass: string | null | undefined) => {
  const parsed = parseIconClass(iconClass)
  if (!parsed) {
    return ['fas', 'ellipsis']
  }
  return [parsed.prefix, parsed.icon]
}

const toggleDropdown = () => {
  if (loading.value || props.disabled) return
  open.value = !open.value
  if (open.value) {
    searchQuery.value = ''
  }
}

const closeDropdown = () => {
  open.value = false
  searchQuery.value = ''
}

const handleCategorySelect = (categoryId: string) => {
  emit('update:modelValue', categoryId)
  closeDropdown()
}

// Close dropdown when clicking outside
if (process.client) {
  onMounted(() => {
    const handleClickOutside = (e: MouseEvent) => {
      const target = e.target as HTMLElement
      if (dropdownRef.value && !dropdownRef.value.contains(target)) {
        closeDropdown()
      }
    }
    document.addEventListener('click', handleClickOutside)
    onUnmounted(() => {
      document.removeEventListener('click', handleClickOutside)
    })
  })
}
</script>

<style scoped>
.dropdown-enter-active,
.dropdown-leave-active {
  transition: all 0.2s ease;
}

.dropdown-enter-from {
  opacity: 0;
  transform: translateY(-10px);
}

.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>
