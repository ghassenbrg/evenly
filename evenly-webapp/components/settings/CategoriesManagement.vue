<template>
  <div class="space-y-4">
    <!-- Search and Create Button -->
    <div class="space-y-3">
      <!-- Search Input -->
      <div class="relative">
        <input
          v-model="searchQuery"
          type="text"
          :placeholder="t('settings.categories.searchPlaceholder') || 'Search categories...'"
          class="w-full px-4 py-3 pl-10 bg-slate-800 border border-slate-700 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-emerald-500 text-base"
        />
        <svg
          class="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-slate-400"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
        </svg>
      </div>

      <!-- Add Category Button -->
      <button
        @click="openCreateSheet"
        class="w-full bg-emerald-500 hover:bg-emerald-600 text-white font-medium py-3 rounded-xl transition-colors flex items-center justify-center gap-2"
      >
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
        </svg>
        {{ t('settings.categories.addCategory') }}
      </button>
    </div>

    <!-- Categories List -->
    <div v-if="!loading && filteredCategories.length > 0" class="space-y-2">
      <div
        v-for="category in filteredCategories"
        :key="category.id"
        class="bg-slate-800 rounded-xl p-4 flex items-center gap-4"
      >
        <!-- Category Icon -->
        <div
          class="w-12 h-12 rounded-full flex items-center justify-center flex-shrink-0"
          :style="{ background: colorToGradient(category.color || '#64748b') }"
        >
          <FontAwesomeIcon
            :icon="getFontAwesomeIcon(category.icon || 'fa-solid fa-ellipsis')"
            class="w-6 h-6 text-white"
          />
        </div>

        <!-- Category Info -->
        <div class="flex-1 min-w-0">
          <h3 class="text-white font-medium text-base">{{ category.name }}</h3>
          <p class="text-slate-400 text-sm truncate">{{ category.icon }}</p>
        </div>

        <!-- Actions -->
        <div class="flex items-center gap-2">
          <button
            @click="openEditSheet(category)"
            class="p-2 text-slate-400 hover:text-emerald-400 transition-colors"
            :title="t('common.edit')"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
            </svg>
          </button>
          <button
            @click="confirmDelete(category)"
            class="p-2 text-slate-400 hover:text-red-400 transition-colors"
            :title="t('common.delete')"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
            </svg>
          </button>
        </div>
      </div>
    </div>

    <!-- Empty State -->
    <div v-else-if="!loading && categories.length === 0" class="text-center py-8">
      <p class="text-slate-400">{{ t('settings.categories.noCategories') }}</p>
    </div>

    <!-- No Results State -->
    <div v-else-if="!loading && categories.length > 0 && filteredCategories.length === 0" class="text-center py-8">
      <p class="text-slate-400">{{ t('settings.categories.noResults') || 'No categories found' }}</p>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="text-center py-8">
      <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-emerald-500"></div>
      <p class="text-slate-400 mt-2">{{ t('common.loading') }}</p>
    </div>

    <!-- Create/Edit Category Sheet -->
    <BottomSheet v-model="showSheet" :title="isEditing ? t('settings.categories.editCategory') : t('settings.categories.createCategory')">
      <div class="space-y-6">
        <!-- Category Name -->
        <div>
          <label class="block text-sm font-medium text-slate-300 mb-2">
            {{ t('settings.categories.name') }} <span class="text-red-400">*</span>
          </label>
          <input
            v-model="formData.name"
            type="text"
            class="w-full px-4 py-4 bg-slate-800 border border-slate-700 rounded-2xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-emerald-500 min-h-[56px] touch-manipulation text-base"
            :placeholder="t('settings.categories.namePlaceholder')"
          />
        </div>

        <!-- Category Icon -->
        <div>
          <label class="block text-sm font-medium text-slate-300 mb-2">
            {{ t('settings.categories.icon') }} <span class="text-red-400">*</span>
          </label>
          <input
            v-model="formData.icon"
            type="text"
            class="w-full px-4 py-4 bg-slate-800 border border-slate-700 rounded-2xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-emerald-500 min-h-[56px] touch-manipulation text-base"
            :placeholder="t('settings.categories.iconPlaceholder')"
          />
          <p class="text-xs text-slate-400 mt-2">{{ t('settings.categories.iconHint') }}</p>
        </div>

        <!-- Category Color -->
        <div>
          <label class="block text-sm font-medium text-slate-300 mb-2">
            {{ t('settings.categories.color') }} <span class="text-slate-500 text-xs">({{ t('common.optional') }})</span>
          </label>
          <div class="flex items-center gap-3">
            <input
              v-model="formData.color"
              type="color"
              class="w-16 h-16 rounded-xl border-2 border-slate-700 cursor-pointer"
            />
            <input
              v-model="formData.color"
              type="text"
              class="flex-1 px-4 py-4 bg-slate-800 border border-slate-700 rounded-2xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-emerald-500 min-h-[56px] touch-manipulation text-base"
              placeholder="#f43f5e"
            />
          </div>
          <p class="text-xs text-slate-400 mt-2">{{ t('settings.categories.colorHint') }}</p>
        </div>

        <!-- Preview -->
        <div v-if="formData.name || formData.icon" class="bg-slate-800 rounded-xl p-4">
          <p class="text-sm text-slate-400 mb-3">{{ t('settings.categories.preview') }}</p>
          <div class="flex items-center gap-4">
            <div
              class="w-12 h-12 rounded-full flex items-center justify-center flex-shrink-0"
              :style="{ background: colorToGradient(formData.color || '#64748b') }"
            >
              <FontAwesomeIcon
                :icon="getFontAwesomeIcon(formData.icon || 'fa-solid fa-ellipsis')"
                class="w-6 h-6 text-white"
              />
            </div>
            <div>
              <p class="text-white font-medium">{{ formData.name || t('settings.categories.categoryName') }}</p>
              <p class="text-slate-400 text-sm">{{ formData.icon || 'fa-solid fa-ellipsis' }}</p>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="flex gap-3 pb-safe">
          <button
            @click="closeSheet"
            class="flex-1 h-14 rounded-2xl border-2 border-slate-700/50 text-white/90 hover:text-white hover:border-slate-600/50 hover:bg-white/5 active:bg-white/10 active:scale-95 transition-all font-semibold touch-manipulation"
          >
            {{ t('common.cancel') }}
          </button>
          <button
            @click="handleSubmit"
            :disabled="!canSubmit || submitting"
            class="flex-1 h-14 rounded-2xl bg-emerald-500 hover:bg-emerald-600 active:bg-emerald-700 active:scale-95 disabled:bg-slate-700 disabled:text-gray-500 disabled:active:scale-100 text-white font-bold transition-all shadow-lg shadow-emerald-500/20 touch-manipulation"
          >
            <span v-if="!submitting">{{ isEditing ? t('common.save') : t('common.create') }}</span>
            <span v-else class="flex items-center justify-center gap-2">
              <svg class="animate-spin h-5 w-5" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
              {{ t('common.saving') }}
            </span>
          </button>
        </div>
      </template>
    </BottomSheet>
  </div>
</template>

<script setup lang="ts">
import type { Category, CreateCategoryRequest, UpdateCategoryRequest } from '~/types/api'
import { useCategories } from '~/composables/useCategories'
import { useToast } from '~/composables/useToast'
import { useFontAwesome } from '~/composables/useFontAwesome'
import { useCategoryColor } from '~/composables/useCategoryColor'

const { t } = useI18n()
const { categories, loading, fetchCategories, createCategory, updateCategory, deleteCategory } = useCategories()
const { success, error: showError } = useToast()
const { parseIconClass } = useFontAwesome()
const { colorToGradient } = useCategoryColor()

const showSheet = ref(false)
const isEditing = ref(false)
const editingCategory = ref<Category | null>(null)
const submitting = ref(false)
const searchQuery = ref('')

const formData = ref({
  name: '',
  icon: '',
  color: '#64748b'
})

const canSubmit = computed(() => {
  return formData.value.name.trim() !== '' && formData.value.icon.trim() !== ''
})

// Filter categories based on search query
const filteredCategories = computed(() => {
  if (!searchQuery.value.trim()) {
    return categories.value
  }
  const query = searchQuery.value.toLowerCase().trim()
  return categories.value.filter(category =>
    category.name.toLowerCase().includes(query) ||
    category.icon.toLowerCase().includes(query)
  )
})

// Load categories on mount
onMounted(async () => {
  try {
    await fetchCategories()
  } catch (err) {
    showError(t('settings.categories.loadError') || 'Failed to load categories')
  }
})

const getFontAwesomeIcon = (iconClass: string | null | undefined) => {
  const parsed = parseIconClass(iconClass)
  if (!parsed) {
    return ['fas', 'ellipsis']
  }
  return [parsed.prefix, parsed.icon]
}

const openCreateSheet = () => {
  isEditing.value = false
  editingCategory.value = null
  formData.value = {
    name: '',
    icon: '',
    color: '#64748b'
  }
  showSheet.value = true
}

const openEditSheet = (category: Category) => {
  isEditing.value = true
  editingCategory.value = category
  formData.value = {
    name: category.name,
    icon: category.icon,
    color: category.color || '#64748b'
  }
  showSheet.value = true
}

const closeSheet = () => {
  showSheet.value = false
  setTimeout(() => {
    formData.value = {
      name: '',
      icon: '',
      color: '#64748b'
    }
    editingCategory.value = null
  }, 300)
}

const handleSubmit = async () => {
  if (!canSubmit.value) return

  submitting.value = true
  try {
    if (isEditing.value && editingCategory.value) {
      const updateData: UpdateCategoryRequest = {
        name: formData.value.name.trim(),
        icon: formData.value.icon.trim(),
        color: formData.value.color || undefined
      }
      await updateCategory(editingCategory.value.id, updateData)
      success(t('settings.categories.updateSuccess') || 'Category updated successfully')
    } else {
      const createData: CreateCategoryRequest = {
        name: formData.value.name.trim(),
        icon: formData.value.icon.trim(),
        color: formData.value.color || undefined
      }
      await createCategory(createData)
      success(t('settings.categories.createSuccess') || 'Category created successfully')
    }
    closeSheet()
  } catch (err: any) {
    const errorMessage = err?.message || (isEditing.value 
      ? t('settings.categories.updateError') || 'Failed to update category'
      : t('settings.categories.createError') || 'Failed to create category')
    showError(errorMessage)
  } finally {
    submitting.value = false
  }
}

const confirmDelete = async (category: Category) => {
  if (!confirm(t('settings.categories.confirmDelete') || `Are you sure you want to delete "${category.name}"?`)) {
    return
  }

  try {
    await deleteCategory(category.id)
    success(t('settings.categories.deleteSuccess') || 'Category deleted successfully')
  } catch (err: any) {
    const errorMessage = err?.message || t('settings.categories.deleteError') || 'Failed to delete category'
    showError(errorMessage)
  }
}
</script>
