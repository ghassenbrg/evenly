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
    <div class="space-y-6 w-full">
      <!-- Category Selection -->
      <CategoryPicker
        v-model="selectedCategoryId"
        :label="t('expenses.category')"
        :required="true"
        :show-error="submitted"
      />

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
        <div class="date-input-wrapper">
          <input
            v-model="effectiveDate"
            type="date"
            class="w-full px-4 py-4 bg-slate-800 border border-slate-700 rounded-2xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-emerald-500 min-h-[56px] touch-manipulation text-base"
          />
        </div>
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
import { useExpenses } from '~/composables/useExpenses'
import { useToast } from '~/composables/useToast'
import { useWorkspacesStore } from '~/stores/workspaces'

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
const { success, error: showError } = useToast()
const workspacesStore = useWorkspacesStore()

const selectedCategoryId = ref<string | null>(null)
const expenseAmount = ref(0)
const effectiveDate = ref(new Date().toISOString().split('T')[0])
const expenseNote = ref('')
const submitted = ref(false)

const workspaceCurrency = computed(() => {
  return workspacesStore.activeWorkspace?.currency || ''
})

// Reset form when sheet opens
watch(() => props.modelValue, (isOpen) => {
  if (isOpen) {
    selectedCategoryId.value = null
    expenseAmount.value = 0
    effectiveDate.value = new Date().toISOString().split('T')[0]
    expenseNote.value = ''
    submitted.value = false
  }
})

const canSubmit = computed(() => {
  return selectedCategoryId.value !== null && expenseAmount.value > 0 && effectiveDate.value !== ''
})

const handleSubmit = async () => {
  submitted.value = true
  if (!canSubmit.value || !props.workspaceId) return

  try {
    await createExpense(props.workspaceId, {
      amount: expenseAmount.value,
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

