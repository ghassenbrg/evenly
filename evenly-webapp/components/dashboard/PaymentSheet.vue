<template>
  <BottomSheet :model-value="modelValue" @update:model-value="(val) => emit('update:modelValue', val)" :title="t('dashboard.makePayment')">
    <!-- Loading State -->
    <div v-if="loading" class="flex items-center justify-center py-12">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-emerald-500"></div>
    </div>

    <!-- Content -->
    <div v-else class="space-y-6">
      <!-- Recipient Info -->
      <div class="bg-slate-800/50 rounded-xl p-4 border border-slate-700/50">
        <p class="text-sm text-gray-400 mb-1">{{ t('dashboard.payingTo') }}</p>
        <p class="text-lg font-semibold text-white">{{ balance.user?.displayName || balance.userId }}</p>
        <div class="mt-2 flex items-center justify-between text-sm">
          <span class="text-gray-400">{{ t('dashboard.outstandingBalance') }}</span>
          <span 
            :class="balance.balance < 0 ? 'text-rose-400' : 'text-emerald-400'"
            class="font-semibold"
          >
            {{ formatCurrency(balance.balance) }}
          </span>
        </div>
      </div>

      <!-- Amount Input -->
      <AmountInput
        v-model="paymentAmount"
        :label="t('dashboard.paymentAmount')"
        :hint="t('dashboard.enterAmount')"
        :placeholder="'0'"
      />

      <!-- Quick Amount Buttons -->
      <div>
        <p class="text-sm text-gray-400 mb-2">{{ t('dashboard.quickAmounts') }}</p>
        <div class="grid grid-cols-4 gap-2">
          <button
            v-for="quickAmount in quickAmounts"
            :key="quickAmount"
            @click="paymentAmount = quickAmount"
            :class="paymentAmount === quickAmount ? 'bg-emerald-500 text-white' : 'bg-slate-800 text-gray-300 hover:bg-slate-700'"
            class="px-3 py-2 rounded-lg text-sm font-medium transition-colors"
          >
            {{ formatCurrency(quickAmount) }}
          </button>
        </div>
      </div>

      <!-- Note Field (Optional) -->
      <div>
        <label class="block text-sm font-medium text-slate-300 mb-2">
          {{ t('dashboard.paymentNote') }} <span class="text-slate-500 text-xs">({{ t('common.optional') }})</span>
        </label>
        <textarea
          v-model="paymentNote"
          rows="3"
          class="w-full px-4 py-3 bg-slate-800 border border-slate-700 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-emerald-500 resize-none"
          :placeholder="t('dashboard.paymentNotePlaceholder')"
        />
      </div>

      <!-- Payment Summary -->
      <div v-if="paymentAmount > 0" class="bg-emerald-500/10 border border-emerald-500/20 rounded-xl p-4 space-y-3">
        <div class="flex items-center justify-between">
          <span class="text-sm text-gray-300">{{ t('dashboard.paymentAmount') }}</span>
          <span class="text-lg font-semibold text-emerald-400">{{ formatCurrency(paymentAmount) }}</span>
        </div>
        
        <!-- Recipient's Remaining Balance -->
        <div class="flex items-center justify-between text-sm pt-2 border-t border-emerald-500/20">
          <span class="text-gray-400">{{ balance.user?.displayName || balance.userId }} {{ t('dashboard.remainingBalance') }}</span>
          <span 
            :class="(balance.balance - paymentAmount) < 0 ? 'text-rose-400' : (balance.balance - paymentAmount) > 0 ? 'text-emerald-400' : 'text-gray-400'"
            class="font-medium"
          >
            {{ (balance.balance - paymentAmount) < 0 ? '-' : (balance.balance - paymentAmount) > 0 ? '+' : '' }}{{ formatCurrency(Math.abs(balance.balance - paymentAmount)) }}
          </span>
        </div>
        
        <!-- Current User's Remaining Balance -->
        <div class="flex items-center justify-between text-sm">
          <span class="text-gray-400">{{ t('dashboard.myRemainingBalance') }}</span>
          <span 
            v-if="currentUserBalance"
            :class="(currentUserBalance.balance + paymentAmount) < 0 ? 'text-rose-400' : (currentUserBalance.balance + paymentAmount) > 0 ? 'text-emerald-400' : 'text-gray-400'"
            class="font-medium"
          >
            {{ (currentUserBalance.balance + paymentAmount) < 0 ? '-' : (currentUserBalance.balance + paymentAmount) > 0 ? '+' : '' }}{{ formatCurrency(Math.abs(currentUserBalance.balance + paymentAmount)) }}
          </span>
          <span v-else-if="balancesLoading" class="text-gray-500 text-xs">{{ t('common.loading') }}</span>
          <span v-else class="text-gray-500 text-xs">—</span>
        </div>
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
          <span v-if="!submitting">{{ t('dashboard.confirmPayment') }}</span>
          <span v-else>{{ t('dashboard.processing') }}</span>
        </button>
      </div>
    </template>
  </BottomSheet>
</template>

<script setup lang="ts">
import { usePayments } from '~/composables/usePayments'
import { useFormatting } from '~/composables/useFormatting'
import { useSettleUp } from '~/composables/useSettleUp'
import { useAuth } from '~/composables/useAuth'
import type { Balance } from '~/types/api'

interface Props {
  modelValue: boolean
  workspaceId: string
  balance: Balance
  currentUserBalance?: Balance | null
  currentUserTotalBalance?: number
}

const props = defineProps<Props>()
const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'payment-completed': []
}>()

const { t } = useI18n()
const { formatCurrency } = useFormatting()
const { createPayment, loading: submitting } = usePayments()
const { success, error: showError } = useToast()
const { settleUpData, loading: settleUpLoading, fetchSettleUp } = useSettleUp()
const { user, getCurrentUserId } = useAuth()

const paymentAmount = ref(0)
const paymentNote = ref('')
const loading = ref(false)

// Load settle-up data when sheet opens to get current user balance
watch(() => props.modelValue, async (isOpen) => {
  if (isOpen && props.workspaceId) {
    loading.value = true
    try {
      await fetchSettleUp(props.workspaceId)
    } finally {
      loading.value = false
    }
  }
})

// Get current user's balance from settle-up data
const currentUserBalance = computed(() => {
  // Priority 1: Use total balance from summary (same source as total balance card) for consistency
  if (props.currentUserTotalBalance !== undefined && props.currentUserTotalBalance !== null) {
    return {
      userId: getCurrentUserId() || 'current-user',
      paid: 0,
      expected: 0,
      balance: props.currentUserTotalBalance,
      user: user.value || undefined
    } as Balance
  }
  
  // Priority 2: If passed as prop and it's not null/undefined, use it
  if (props.currentUserBalance) {
    return props.currentUserBalance
  }
  
  // Priority 3: Get from settle-up data
  if (settleUpData.value) {
    const currentUser = settleUpData.value.currentUser
    const balance = currentUser.paidAmount - currentUser.expectedAmount
    return {
      userId: currentUser.userId,
      paid: currentUser.paidAmount,
      expected: currentUser.expectedAmount,
      balance: balance,
      user: {
        id: currentUser.userId,
        displayName: currentUser.userFullName,
        email: '',
        createdAt: ''
      }
    } as Balance
  }
  
  return null
})

// Check if data is still loading
const balancesLoading = computed(() => {
  return loading.value || settleUpLoading.value || (!settleUpData.value && !props.currentUserBalance)
})

// Calculate quick amount options (25%, 50%, 75%, 100% of balance)
// If balance is 0, use default amounts (1000, 5000, 10000, 20000)
const quickAmounts = computed(() => {
  const maxAmount = Math.abs(props.balance.balance)
  if (maxAmount === 0) {
    // Default quick amounts when balance is 0
    return [1000, 5000, 10000, 20000]
  }
  return [
    Math.floor(maxAmount * 0.25),
    Math.floor(maxAmount * 0.5),
    Math.floor(maxAmount * 0.75),
    maxAmount
  ].filter(amount => amount > 0)
})

// Reset amount and note when modal opens/closes
watch(() => props.modelValue, (isOpen) => {
  if (isOpen) {
    paymentAmount.value = 0
    paymentNote.value = ''
  }
})

const canSubmit = computed(() => {
  // Allow submission if amount is greater than 0, no max limit
  return paymentAmount.value > 0
})

const handleSubmit = async () => {
  if (!canSubmit.value || !props.workspaceId || !props.balance) return
  
  try {
    // Create a payment using the pay endpoint
    await createPayment(props.workspaceId, {
      payeeUserId: props.balance.userId,
      amount: paymentAmount.value,
      note: paymentNote.value.trim() || undefined,
      effectiveDate: new Date().toISOString().split('T')[0] // Today's date
    })
    success(`${t('dashboard.paymentCompleted')}: ${formatCurrency(paymentAmount.value)}`)
    emit('payment-completed')
    emit('update:modelValue', false)
  } catch (err: any) {
    showError(err.message || t('dashboard.paymentFailed'))
  }
}
</script>

