<template>
  <form class="space-y-5" @submit.prevent="handleSubmit">
    <!-- Transaction Type Selection -->
    <div class="space-y-3">
      <label class="text-sm font-semibold text-white/90 uppercase tracking-wide flex items-center gap-2">
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
        </svg>
        {{ t('transactions.form.type') || 'Transaction Type' }}
        <span class="text-red-400">*</span>
      </label>
      <div class="grid grid-cols-3 gap-3">
        <button
          v-for="type in transactionTypes"
          :key="type"
          type="button"
          class="h-16 rounded-2xl border-2 transition-all flex flex-col items-center justify-center gap-2 touch-manipulation active:scale-95"
          :class="form.transactionType === type 
            ? 'border-emerald-500 bg-emerald-500/20 text-emerald-300 shadow-lg shadow-emerald-500/20' 
            : 'border-slate-700/50 text-white/60 hover:border-slate-600 hover:text-white hover:bg-white/5 active:bg-white/10'"
          @click="setTransactionType(type)"
        >
          <svg
            v-if="type === 'INCOME'"
            class="w-5 h-5"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6" />
          </svg>
          <svg
            v-else-if="type === 'EXPENSE'"
            class="w-5 h-5"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 17h8m0 0V9m0 8l-8-8-4 4-6-6" />
          </svg>
          <svg
            v-else
            class="w-5 h-5"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4" />
          </svg>
          <span class="text-xs font-semibold">{{ type }}</span>
        </button>
      </div>
      <p v-if="errors.transactionType" class="text-xs text-red-400 flex items-center gap-1">
        <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
        {{ errors.transactionType }}
      </p>
    </div>

    <!-- Wallet Selection -->
    <div class="grid grid-cols-1 sm:grid-cols-2 gap-3" v-if="showWalletFields">
      <div v-if="showFromWallet" class="space-y-2">
        <SearchableSelect
          v-model="form.walletFromId"
          :options="walletOptions"
          :label="t('transactions.fromWallet') || 'From'"
          :error="errors.walletFromId"
          :enable-search="wallets.length > 5"
          :get-option-label="(opt: any) => opt.label"
          :get-option-value="(opt: any) => opt.value"
        >
          <template #icon>
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z" />
            </svg>
          </template>
        </SearchableSelect>
      </div>

      <div v-if="showToWallet" class="space-y-2">
        <SearchableSelect
          v-model="form.walletToId"
          :options="walletOptions"
          :label="t('transactions.toWallet') || 'To'"
          :error="errors.walletToId"
          :enable-search="wallets.length > 5"
          :get-option-label="(opt: any) => opt.label"
          :get-option-value="(opt: any) => opt.value"
        >
          <template #icon>
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z" />
            </svg>
          </template>
        </SearchableSelect>
      </div>
    </div>

    <!-- Amount -->
    <div class="space-y-2">
      <AmountInput
        v-model="form.amount"
        :currency="amountCurrency"
        :label="t('transactions.amount') || 'Amount'"
        :hint="errors.amount"
        placeholder="0.00"
        :input-class="`w-full pl-8 pr-4 py-3 bg-slate-800/50 border transition-colors rounded-xl text-white placeholder:text-white/40 focus:outline-none focus:ring-2 focus:ring-emerald-500/50 focus:border-emerald-500/50 text-lg font-semibold ${errors.amount ? 'border-red-500/50' : 'border-slate-700/50'}`"
      />
    </div>

    <!-- Exchange Rate (if needed) -->
    <div v-if="shouldShowExchangeRate" class="rounded-xl bg-blue-500/10 border border-blue-500/20 p-4 space-y-2">
      <label class="text-sm font-medium text-blue-300 flex items-center gap-2">
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 21a4 4 0 01-4-4V5a2 2 0 012-2h4a2 2 0 012 2v12a4 4 0 01-4 4zm0 0h12a2 2 0 002-2v-4a2 2 0 00-2-2h-2.343M11 7.343l1.657-1.657a2 2 0 012.828 0l2.829 2.829a2 2 0 010 2.828l-8.486 8.485M7 17h.01" />
        </svg>
        {{ t('transactions.exchangeRate') || 'Exchange rate' }}
      </label>
      <input
        v-model.number="form.exchangeRate"
        type="number"
        step="0.0001"
        min="0.0001"
        class="w-full bg-slate-800/50 border border-slate-700/50 rounded-xl px-4 py-3 text-white placeholder:text-white/40 focus:outline-none focus:ring-2 focus:ring-blue-500/50 focus:border-blue-500/50"
        placeholder="1.0000"
      />
      <div class="flex items-center justify-between text-xs">
        <span class="text-white/60">{{ t('transactions.convertedAmount') || 'Converted amount' }}:</span>
        <span class="font-semibold text-blue-300">{{ convertedAmountDisplay }}</span>
      </div>
      <p v-if="errors.exchangeRate" class="text-xs text-red-400">{{ errors.exchangeRate }}</p>
    </div>

    <!-- Category -->
    <div v-if="showCategoryField" class="space-y-2">
      <SearchableSelect
        v-model="form.categoryId"
        :options="filteredCategories"
        :label="t('transactions.category') || 'Category'"
        :placeholder="t('transactions.selectCategory') || 'Select category'"
        :error="errors.categoryId"
        :required="true"
        :enable-search="filteredCategories.length > 5"
        :get-option-label="(cat: any) => cat.name"
        :get-option-value="(cat: any) => cat.id"
      >
        <template #icon>
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z" />
          </svg>
        </template>
      </SearchableSelect>
    </div>

    <!-- Date -->
    <div class="space-y-2">
      <label class="text-sm font-medium text-white/90 flex items-center gap-2">
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
        </svg>
        {{ t('transactions.date') || 'Date' }}
        <span class="text-red-400">*</span>
      </label>
      <div class="relative">
        <input
          v-model="form.effectiveDate"
          type="date"
          class="w-full bg-slate-800/50 border transition-colors rounded-2xl px-4 py-4 pl-12 text-white text-base focus:outline-none focus:ring-2 focus:ring-emerald-500/50 focus:border-emerald-500/50 min-h-[56px] touch-manipulation"
          :class="errors.effectiveDate ? 'border-red-500/50' : 'border-slate-700/50'"
          required
        />
        <svg class="w-6 h-6 text-white/40 absolute left-4 top-1/2 -translate-y-1/2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
        </svg>
      </div>
      <p v-if="errors.effectiveDate" class="text-xs text-red-400">{{ errors.effectiveDate }}</p>
    </div>

    <!-- Note -->
    <div class="space-y-2">
      <label class="text-sm font-medium text-white/90 flex items-center gap-2">
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
        </svg>
        {{ t('transactions.note') || 'Note' }}
        <span class="text-xs text-white/50 font-normal">({{ t('common.optional') }})</span>
      </label>
      <textarea
        v-model="form.note"
        rows="3"
        maxlength="500"
        class="w-full bg-slate-800/50 border transition-colors rounded-2xl px-4 py-4 text-white placeholder:text-white/40 focus:outline-none focus:ring-2 focus:ring-emerald-500/50 focus:border-emerald-500/50 resize-none min-h-[56px] touch-manipulation text-base"
        :class="errors.note ? 'border-red-500/50' : 'border-slate-700/50'"
        placeholder="Add a note..."
      />
      <div class="flex items-center justify-between">
        <p v-if="errors.note" class="text-xs text-red-400">{{ errors.note }}</p>
        <p v-else class="text-xs text-white/50">{{ form.note.length }}/500</p>
      </div>
    </div>

    <!-- Action Buttons -->
    <div class="flex gap-3 pt-4 pb-safe">
      <button
        type="button"
        class="flex-1 h-14 rounded-2xl border-2 border-slate-700/50 text-white/90 hover:text-white hover:border-slate-600/50 hover:bg-white/5 active:bg-white/10 active:scale-95 transition-all font-semibold flex items-center justify-center gap-2 touch-manipulation"
        @click="emit('cancelled')"
      >
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
        </svg>
        {{ t('common.cancel') }}
      </button>
      <button
        type="submit"
        class="flex-1 h-14 rounded-2xl bg-emerald-500 text-slate-900 font-bold hover:bg-emerald-400 active:bg-emerald-600 active:scale-95 disabled:opacity-50 disabled:cursor-not-allowed disabled:active:scale-100 transition-all shadow-lg shadow-emerald-500/20 flex items-center justify-center gap-2 touch-manipulation"
        :disabled="submitting"
      >
        <svg v-if="!submitting" class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
        </svg>
        <svg v-else class="animate-spin h-5 w-5" fill="none" viewBox="0 0 24 24">
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
          <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
        </svg>
        {{ submitting ? (t('common.saving') || 'Saving...') : (props.transaction ? t('transactions.update') || 'Update' : t('transactions.create') || 'Create') }}
      </button>
    </div>
  </form>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import type { TransactionDto, TransactionRequest } from '~/types/pockito'
import { CategoryType, TransactionType } from '~/types/pockito'
import { usePockitoTransactions } from '~/composables/usePockitoTransactions'
import { usePockitoWallets } from '~/composables/usePockitoWallets'
import { usePockitoCategories } from '~/composables/usePockitoCategories'
import SearchableSelect from '~/components/pockito/SearchableSelect.vue'
import AmountInput from '~/components/AmountInput.vue'

const props = defineProps<{
  transaction?: TransactionDto | null
  initialWalletFromId?: string
  initialWalletToId?: string
}>()

const emit = defineEmits<{
  saved: [transaction: TransactionDto]
  cancelled: []
  walletChange?: [{ walletFromId: string | null; walletToId: string | null }]
}>()

const { t } = useI18n()
const toast = useToast()
const { createTransaction, updateTransaction } = usePockitoTransactions()
const { wallets, loadWallets } = usePockitoWallets()
const { getCategoriesByType } = usePockitoCategories()

const transactionTypes = Object.values(TransactionType)
type TransactionFormState = Omit<TransactionRequest, 'walletFromId' | 'walletToId' | 'categoryId' | 'effectiveDate'> & {
  walletFromId?: string | null
  walletToId?: string | null
  categoryId?: string | null
  effectiveDate?: string
}

const form = reactive<TransactionFormState>({
  transactionType: TransactionType.EXPENSE,
  walletFromId: undefined,
  walletToId: undefined,
  amount: 0,
  exchangeRate: 1,
  categoryId: undefined,
  note: '',
  effectiveDate: new Date().toISOString().split('T')[0]
})

const errors = reactive<Record<string, string>>({})
const submitting = ref(false)
const categories = ref<{ [type in CategoryType]?: any[] }>({})

const showWalletFields = computed(() => !!form.transactionType)
const showFromWallet = computed(() => form.transactionType === TransactionType.EXPENSE || form.transactionType === TransactionType.TRANSFER)
const showToWallet = computed(() => form.transactionType === TransactionType.INCOME || form.transactionType === TransactionType.TRANSFER)
const showCategoryField = computed(() => form.transactionType !== TransactionType.TRANSFER)

const filteredCategories = computed(() => {
  if (!form.transactionType || form.transactionType === TransactionType.TRANSFER) return []
  const type = form.transactionType === TransactionType.INCOME ? CategoryType.INCOME : CategoryType.EXPENSE
  return categories.value[type] || []
})

// Get currency for amount input based on selected wallet
const amountCurrency = computed(() => {
  if (form.transactionType === TransactionType.EXPENSE && form.walletFromId) {
    const wallet = wallets.value.find(w => w.id === form.walletFromId)
    return wallet?.currency || 'USD'
  }
  if (form.transactionType === TransactionType.INCOME && form.walletToId) {
    const wallet = wallets.value.find(w => w.id === form.walletToId)
    return wallet?.currency || 'USD'
  }
  if (form.transactionType === TransactionType.TRANSFER && form.walletFromId) {
    const wallet = wallets.value.find(w => w.id === form.walletFromId)
    return wallet?.currency || 'USD'
  }
  // Default to first wallet currency or USD
  return wallets.value[0]?.currency || 'USD'
})

const walletOptions = computed(() => {
  const options = [
    { value: null, label: t('transactions.outside') || 'Out of app' }
  ]
  wallets.value.forEach(wallet => {
    options.push({
      value: wallet.id,
      label: `${wallet.name} (${wallet.currency})`
    })
  })
  return options
})

const shouldShowExchangeRate = computed(() => {
  if (!form.walletFromId || !form.walletToId) return false
  const from = wallets.value.find(w => w.id === form.walletFromId)
  const to = wallets.value.find(w => w.id === form.walletToId)
  return !!(from && to && from.currency !== to.currency)
})

const convertedAmountDisplay = computed(() => {
  if (!shouldShowExchangeRate.value) {
    return form.amount ? form.amount.toFixed(2) : '0.00'
  }
  const converted = (form.amount || 0) * (form.exchangeRate || 1)
  return converted.toFixed(2)
})

const loadCategories = async () => {
  try {
    const income = await getCategoriesByType(CategoryType.INCOME)
    const expense = await getCategoriesByType(CategoryType.EXPENSE)
    categories.value = {
      [CategoryType.INCOME]: income.categories || [],
      [CategoryType.EXPENSE]: expense.categories || []
    }
  } catch (err) {
    toast.error((err as Error)?.message || (t('transactions.loadingCategoriesError') as string) || 'Unable to load categories')
  }
}

const setTransactionType = (type: TransactionType) => {
  form.transactionType = type
  if (type === TransactionType.EXPENSE) {
    form.walletToId = undefined
  } else if (type === TransactionType.INCOME) {
    form.walletFromId = undefined
  }
  if (type === TransactionType.TRANSFER) {
    form.categoryId = undefined
  }
  validate()
}

const patchForm = (transaction: TransactionDto) => {
  form.transactionType = transaction.transactionType
  form.walletFromId = transaction.walletFromId ?? undefined
  form.walletToId = transaction.walletToId ?? undefined
  form.amount = transaction.amount
  form.exchangeRate = transaction.exchangeRate
  form.categoryId = transaction.categoryId
  form.note = transaction.note || ''
  form.effectiveDate = transaction.effectiveDate?.split('T')[0] || new Date().toISOString().split('T')[0]
}

const resetForm = () => {
  form.transactionType = TransactionType.EXPENSE
  form.walletFromId = props.initialWalletFromId
  form.walletToId = props.initialWalletToId
  form.amount = 0
  form.exchangeRate = 1
  form.categoryId = undefined
  form.note = ''
  form.effectiveDate = new Date().toISOString().split('T')[0]
  Object.keys(errors).forEach((key) => delete errors[key])
}

onMounted(async () => {
  await loadWallets()
  await loadCategories()
  if (props.transaction) {
    patchForm(props.transaction)
  } else {
    resetForm()
  }
})

watch(
  () => props.transaction,
  (tx) => {
    if (tx) {
      patchForm(tx)
    } else {
      resetForm()
    }
  }
)

watch(
  () => [form.walletFromId, form.walletToId],
  () => {
    emit('walletChange', { walletFromId: form.walletFromId ?? null, walletToId: form.walletToId ?? null })
  }
)

const validate = () => {
  Object.keys(errors).forEach((key) => delete errors[key])
  if (!form.transactionType) {
    errors.transactionType = t('transactions.form.errors.transactionTypeRequired') || 'Transaction type is required'
  }
  if (!form.amount || form.amount < 0.01) {
    errors.amount = t('transactions.form.errors.amountMin') || 'Enter an amount'
  }
  if (shouldShowExchangeRate.value && (!form.exchangeRate || form.exchangeRate < 0.0001)) {
    errors.exchangeRate = t('transactions.form.errors.exchangeRateMin') || 'Exchange rate required'
  }
  if (showCategoryField.value && !form.categoryId) {
    errors.categoryId = t('transactions.form.errors.categoryRequired') || 'Category required'
  }
  if (!form.effectiveDate) {
    errors.effectiveDate = t('transactions.form.errors.effectiveDateRequired') || 'Date required'
  }
  if (form.note && form.note.length > 500) {
    errors.note = t('transactions.form.errors.noteMaxLength') || 'Note too long'
  }
  if (form.transactionType === TransactionType.EXPENSE && !form.walletFromId) {
    errors.walletFromId = t('transactions.form.errors.walletFromRequired') || 'Select a wallet'
  }
  if (form.transactionType === TransactionType.INCOME && !form.walletToId) {
    errors.walletToId = t('transactions.form.errors.walletToRequired') || 'Select a wallet'
  }
  if (form.transactionType === TransactionType.TRANSFER) {
    const hasFrom = !!form.walletFromId
    const hasTo = !!form.walletToId
    if (!hasFrom && !hasTo) {
      errors.walletFromId = t('transactions.form.errors.walletFromRequired') || 'Select a wallet'
      errors.walletToId = t('transactions.form.errors.walletToRequired') || 'Select a wallet'
    }
    if (hasFrom && hasTo && form.walletFromId === form.walletToId) {
      errors.walletToId = t('transactions.form.errors.walletDistinct') || 'Choose a different wallet'
    }
  }
  return Object.keys(errors).length === 0
}

const handleSubmit = async () => {
  if (!validate()) return
  submitting.value = true
  try {
    const payload: TransactionRequest = {
      ...form,
      walletFromId: form.walletFromId ?? undefined,
      walletToId: form.walletToId ?? undefined,
      categoryId: form.categoryId ?? undefined
    }
    let saved: TransactionDto
    if (props.transaction) {
      saved = await updateTransaction(props.transaction.id, payload) as TransactionDto
      toast.success(t('transactions.updateTransactionSuccess') || 'Transaction updated')
    } else {
      saved = await createTransaction(payload) as TransactionDto
      toast.success(t('transactions.createTransactionSuccess') || 'Transaction created')
    }
    emit('saved', saved)
  } catch (err) {
    toast.error((err as Error)?.message || (t('transactions.saveFailed') as string) || 'Unable to save transaction')
  } finally {
    submitting.value = false
  }
}
</script>
