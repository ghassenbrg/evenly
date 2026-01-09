<template>
  <BottomSheet :model-value="modelValue" @update:model-value="(val) => emit('update:modelValue', val)">
    <template #header>
      <div class="flex items-center justify-between w-full">
        <h2 class="text-xl font-semibold text-white">{{ t('expenses.editExpense') }}</h2>
        <div class="flex items-center gap-2">
          <button
            @click="handleDelete"
            :disabled="deleting"
            class="p-2 text-red-400 hover:text-red-300 transition-colors disabled:opacity-50"
            :title="t('common.delete')"
          >
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
            </svg>
          </button>
          <button
            @click="emit('update:modelValue', false)"
            class="p-2 text-slate-400 hover:text-white transition-colors"
          >
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>
      </div>
    </template>

    <!-- Loading State -->
    <div v-if="loading" class="flex items-center justify-center py-12">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-emerald-500"></div>
    </div>

    <!-- Content -->
    <div v-else-if="expense" class="space-y-6">
      <!-- Category Selection -->
      <CategoryPicker
        v-model="selectedCategoryId"
        :label="t('expenses.category')"
        :required="true"
      />

      <!-- Paid By Info (Read-only) -->
      <div class="bg-slate-800/50 rounded-xl p-4 border border-slate-700/50">
        <div class="flex items-center justify-between text-sm">
          <span class="text-gray-400">{{ t('dashboard.paidBy') }}</span>
          <span class="text-white">{{ expense.paidByUserName || expense.paidByUserId }}</span>
        </div>
      </div>

      <!-- Amount Input -->
      <AmountInput
        v-model="expenseAmount"
        :label="t('expenses.amount')"
        :currency="expense.currency"
        :placeholder="'0'"
      />

      <!-- Effective Date -->
      <div>
        <label class="block text-sm font-medium text-slate-300 mb-2">
          {{ t('expenses.effectiveDate') }}
        </label>
        <input
          v-model="effectiveDate"
          type="date"
          class="w-full px-4 py-3 bg-slate-800 border border-slate-700 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-emerald-500"
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
          class="w-full px-4 py-3 bg-slate-800 border border-slate-700 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-emerald-500 resize-none"
          :placeholder="t('expenses.notePlaceholder')"
        />
      </div>
    </div>

    <template #footer>
      <div class="flex gap-3">
        <button
          @click="emit('update:modelValue', false)"
          class="flex-1 bg-slate-700 hover:bg-slate-600 text-white font-medium py-3 rounded-xl transition-colors"
        >
          {{ t('common.cancel') }}
        </button>
        <button
          @click="handleSubmit"
          :disabled="!canSubmit || submitting"
          class="flex-1 bg-emerald-500 hover:bg-emerald-600 disabled:bg-slate-700 disabled:text-gray-500 text-white font-medium py-3 rounded-xl transition-colors"
        >
          <span v-if="!submitting">{{ t('common.save') }}</span>
          <span v-else>{{ t('common.saving') }}</span>
        </button>
      </div>
    </template>
  </BottomSheet>
</template>

<script setup lang="ts">
import type { Expense } from '~/types/api'
import { useExpenses } from '~/composables/useExpenses'
import { useFormatting } from '~/composables/useFormatting'
import { useToast } from '~/composables/useToast'

interface Props {
  modelValue: boolean
  workspaceId: string
  expense: Expense | null
}

const props = defineProps<Props>()
const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'expense-updated': []
  'expense-deleted': []
}>()

const { t } = useI18n()
const { formatCurrency } = useFormatting()
const { updateExpense, deleteExpense, loading: submitting, getExpense } = useExpenses()
const { success, error: showError } = useToast()

const loading = ref(false)
const deleting = ref(false)
const expenseAmount = ref(0)
const effectiveDate = ref('')
const expenseNote = ref('')
const selectedCategoryId = ref<string | null>(null)

// Load expense details when sheet opens
watch(() => props.modelValue, async (isOpen) => {
  if (isOpen && props.expense && props.workspaceId) {
    loading.value = true
    try {
      // Fetch fresh expense data
      const freshExpense = await getExpense(props.workspaceId, props.expense.id)
      if (freshExpense) {
        expenseAmount.value = freshExpense.amount
        effectiveDate.value = freshExpense.effectiveDate
        expenseNote.value = freshExpense.note || ''
        selectedCategoryId.value = freshExpense.categoryId || null
      }
    } catch (err) {
      showError(t('expenses.loadFailed'))
    } finally {
      loading.value = false
    }
  }
})

// Initialize form when expense changes
watch(() => props.expense, (expense) => {
  if (expense) {
    expenseAmount.value = expense.amount
    effectiveDate.value = expense.effectiveDate
    expenseNote.value = expense.note || ''
    selectedCategoryId.value = expense.categoryId || null
  }
}, { immediate: true })

const canSubmit = computed(() => {
  return expenseAmount.value > 0 && effectiveDate.value !== '' && selectedCategoryId.value !== null
})

const handleSubmit = async () => {
  if (!canSubmit.value || !props.workspaceId || !props.expense) return
  
  try {
    await updateExpense(props.workspaceId, props.expense.id, {
      amount: expenseAmount.value,
      categoryId: selectedCategoryId.value!,
      date: effectiveDate.value,
      note: expenseNote.value.trim() || undefined
    })
    success(t('expenses.updated'))
    emit('expense-updated')
    emit('update:modelValue', false)
  } catch (err: any) {
    showError(err.message || t('expenses.updateFailed'))
  }
}

const handleDelete = async () => {
  if (!props.workspaceId || !props.expense) return
  
  if (!confirm(t('expenses.confirmDelete'))) return
  
  deleting.value = true
  try {
    await deleteExpense(props.workspaceId, props.expense.id)
    success(t('expenses.deleted'))
    emit('expense-deleted')
    emit('update:modelValue', false)
  } catch (err: any) {
    showError(err.message || t('expenses.deleteFailed'))
  } finally {
    deleting.value = false
  }
}
</script>
