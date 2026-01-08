<template>
  <BottomSheet :model-value="modelValue" @update:model-value="(val) => emit('update:modelValue', val)">
    <template #header>
      <div class="flex items-center justify-between w-full">
        <h2 class="text-xl font-semibold text-white">{{ t('expenses.addExpense') || 'Add Expense' }}</h2>
        <button
          @click="emit('update:modelValue', false)"
          class="p-2 text-slate-400 hover:text-white transition-colors"
        >
          <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>
    </template>

    <!-- Content -->
    <div class="space-y-6">
      <!-- Category Selection -->
      <div>
        <label class="block text-sm font-medium text-slate-300 mb-2">
          {{ t('expenses.category') }} <span class="text-red-400">*</span>
        </label>
        <div class="grid grid-cols-2 gap-2">
          <button
            v-for="category in categories"
            :key="category.id"
            type="button"
            @click="selectedCategoryId = category.id"
            class="p-4 rounded-2xl border-2 transition-all active:scale-95 touch-manipulation"
            :class="selectedCategoryId === category.id
              ? 'border-emerald-500 bg-emerald-500/20'
              : 'border-slate-700/50 bg-slate-800/50 hover:border-slate-600/50'"
          >
            <div class="flex flex-col items-center gap-2">
              <div
                class="w-12 h-12 rounded-full flex items-center justify-center"
                :style="{ background: colorToGradient(category.color) }"
              >
                <FontAwesomeIcon
                  :icon="getFontAwesomeIcon(category.icon)"
                  class="w-6 h-6 text-white"
                />
              </div>
              <span class="text-xs font-medium text-white text-center">{{ category.name }}</span>
            </div>
          </button>
        </div>
        <p v-if="!selectedCategoryId && submitted" class="text-xs text-red-400 mt-2">
          {{ t('expenses.categoryRequired') || 'Please select a category' }}
        </p>
      </div>

      <!-- Amount Input -->
      <AmountInput
        v-model="expenseAmount"
        :label="t('expenses.amount')"
        :currency="workspaceCurrency"
        :placeholder="'0'"
      />

      <!-- Effective Date -->
      <div>
        <label class="block text-sm font-medium text-slate-300 mb-2">
          {{ t('expenses.effectiveDate') }} <span class="text-red-400">*</span>
        </label>
        <input
          v-model="effectiveDate"
          type="date"
          class="w-full px-4 py-4 bg-slate-800 border border-slate-700 rounded-2xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-emerald-500 min-h-[56px] touch-manipulation text-base"
        />
      </div>

      <!-- Note Field -->
      <div>
        <label class="block text-sm font-medium text-slate-300 mb-2">
          {{ t('expenses.note') }} <span class="text-slate-500 text-xs">({{ t('common.optional') }})</span>
        </label>
        <textarea
          v-model="expenseNote"
          rows="3"
          class="w-full px-4 py-4 bg-slate-800 border border-slate-700 rounded-2xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-emerald-500 resize-none min-h-[56px] touch-manipulation text-base"
          :placeholder="t('expenses.notePlaceholder')"
        />
      </div>
    </div>

    <template #footer>
      <div class="flex gap-3 pb-safe">
        <button
          @click="emit('update:modelValue', false)"
          class="flex-1 h-14 rounded-2xl border-2 border-slate-700/50 text-white/90 hover:text-white hover:border-slate-600/50 hover:bg-white/5 active:bg-white/10 active:scale-95 transition-all font-semibold touch-manipulation"
        >
          {{ t('common.cancel') }}
        </button>
        <button
          @click="handleSubmit"
          :disabled="!canSubmit || submitting"
          class="flex-1 h-14 rounded-2xl bg-emerald-500 hover:bg-emerald-600 active:bg-emerald-700 active:scale-95 disabled:bg-slate-700 disabled:text-gray-500 disabled:active:scale-100 text-white font-bold transition-all shadow-lg shadow-emerald-500/20 touch-manipulation"
        >
          <span v-if="!submitting">{{ t('expenses.create') || 'Create' }}</span>
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
</template>

<script setup lang="ts">
import type { Category } from '~/types/api'
import { useExpenses } from '~/composables/useExpenses'
import { useCategories } from '~/composables/useCategories'
import { useFormatting } from '~/composables/useFormatting'
import { useFontAwesome } from '~/composables/useFontAwesome'
import { useCategoryColor } from '~/composables/useCategoryColor'
import { useToast } from '~/composables/useToast'
import { useWorkspacesStore } from '~/stores/workspaces'
import { useAuth } from '~/composables/useAuth'

interface Props {
  modelValue: boolean
  workspaceId: string
}

const props = defineProps<Props>()
const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'expense-created': []
}>()

const { t } = useI18n()
const { createExpense, loading: submitting } = useExpenses()
const { categories, fetchCategories } = useCategories()
const { success, error: showError } = useToast()
const { parseIconClass } = useFontAwesome()
const { colorToGradient } = useCategoryColor()
const workspacesStore = useWorkspacesStore()
const { user: currentUser, loadUserProfile, getCurrentUserId } = useAuth()

const selectedCategoryId = ref<string | null>(null)
const expenseAmount = ref(0)
const effectiveDate = ref(new Date().toISOString().split('T')[0])
const expenseNote = ref('')
const submitted = ref(false)

const workspaceCurrency = computed(() => {
  return workspacesStore.activeWorkspace?.currency || 'JPY'
})

// Helper to get Font Awesome icon from class string
const getFontAwesomeIcon = (iconClass: string | null | undefined) => {
  const parsed = parseIconClass(iconClass)
  if (!parsed) {
    return ['fas', 'ellipsis']
  }
  return [parsed.prefix, parsed.icon]
}

// Load categories when sheet opens
watch(() => props.modelValue, async (isOpen) => {
  if (isOpen && props.workspaceId) {
    try {
      await fetchCategories(props.workspaceId)
      // Reset form when opening
      selectedCategoryId.value = null
      expenseAmount.value = 0
      effectiveDate.value = new Date().toISOString().split('T')[0]
      expenseNote.value = ''
      submitted.value = false
    } catch (err) {
      showError(t('expenses.loadFailed'))
    }
  }
})

const canSubmit = computed(() => {
  return selectedCategoryId.value !== null && expenseAmount.value > 0 && effectiveDate.value !== ''
})

const handleSubmit = async () => {
  submitted.value = true
  if (!canSubmit.value || !props.workspaceId) return
  
  // Try to load user profile if not available
  if (!currentUser.value?.id && process.client) {
    const { $keycloak } = useNuxtApp()
    if ($keycloak && $keycloak.authenticated) {
      try {
        await loadUserProfile()
        await nextTick() // Wait for reactive update
      } catch (err) {
        console.error('Failed to load user profile:', err)
      }
    }
  }
  
  const userId = getCurrentUserId()
  if (!userId) {
    showError(t('expenses.userNotFound') || 'User not found. Please try logging in again.')
    return
  }

  try {
    await createExpense(props.workspaceId, {
      amount: expenseAmount.value,
      paidByUserId: userId,
      categoryId: selectedCategoryId.value!,
      date: effectiveDate.value,
      note: expenseNote.value.trim() || undefined
    })
    success(t('expenses.created') || 'Expense created successfully')
    emit('expense-created')
    emit('update:modelValue', false)
    // Reset form
    selectedCategoryId.value = null
    expenseAmount.value = 0
    effectiveDate.value = new Date().toISOString().split('T')[0]
    expenseNote.value = ''
    submitted.value = false
  } catch (err: any) {
    showError(err.message || t('expenses.createFailed') || 'Failed to create expense')
  }
}
</script>
