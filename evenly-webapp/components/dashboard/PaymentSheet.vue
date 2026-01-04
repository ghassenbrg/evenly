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
          <span v-else-if="loading || balancesLoadingState || balances.length === 0" class="text-gray-500 text-xs">{{ t('common.loading') }}</span>
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
import { useSettlements } from '~/composables/useSettlements'
import { useFormatting } from '~/composables/useFormatting'
import { useBalance } from '~/composables/useBalance'
import { useAuth } from '~/composables/useAuth'
import { useWorkspaceMembers } from '~/composables/useWorkspaceMembers'
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
const { createSettlement, loading: submitting } = useSettlements()
const { success, error: showError } = useToast()
const { balances, loading: balancesLoadingState, fetchBalances } = useBalance()
const { user } = useAuth()
const { members, fetchMembers } = useWorkspaceMembers()

const paymentAmount = ref(0)
const paymentNote = ref('')
const loading = ref(false)

// Load balances and members when sheet opens
watch(() => props.modelValue, async (isOpen) => {
  if (isOpen && props.workspaceId) {
    loading.value = true
    try {
      await Promise.all([
        fetchBalances(props.workspaceId),
        fetchMembers(props.workspaceId)
      ])
    } finally {
      loading.value = false
    }
  }
})

// Get current user's ID - try multiple matching strategies
const currentUserId = computed(() => {
  if (!user.value) return null
  
  // Strategy 1: Match by email in balances (most reliable)
  if (user.value.email && balances.value.length > 0) {
    const balance = balances.value.find(b => b.user?.email === user.value?.email)
    if (balance) return balance.userId
  }
  
  // Strategy 2: Match by email in members
  if (user.value.email && members.value.length > 0) {
    const member = members.value.find(m => m.user?.email === user.value?.email)
    if (member) return member.userId
  }
  
  // Strategy 3: Match by username in balances
  if (user.value.username && balances.value.length > 0) {
    const balance = balances.value.find(b => b.user?.username === user.value?.username)
    if (balance) return balance.userId
  }
  
  // Strategy 4: Match by username in members
  if (user.value.username && members.value.length > 0) {
    const member = members.value.find(m => m.user?.username === user.value?.username)
    if (member) return member.userId
  }
  
  // Strategy 5: Match by user ID
  if (user.value.id && balances.value.length > 0) {
    const balance = balances.value.find(b => b.userId === user.value?.id || b.user?.id === user.value?.id)
    if (balance) return balance.userId
  }
  
  return user.value.id
})

// Get current user's balance - use summary.total (same as total balance card) for consistency
const currentUserBalance = computed(() => {
  // Priority 1: Use total balance from summary (same source as total balance card) for consistency
  if (props.currentUserTotalBalance !== undefined && props.currentUserTotalBalance !== null) {
    return {
      userId: currentUserId.value || user.value?.id || 'current-user',
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
  
  // If balances haven't loaded yet, return null (will show loading)
  if (balances.value.length === 0) {
    return null
  }
  
  // Priority 3: Try to find it from balances using multiple strategies
  if (user.value) {
    // Strategy 1: Find by user email (most reliable)
    if (user.value.email) {
      const balance = balances.value.find(b => b.user?.email === user.value?.email)
      if (balance) return balance
    }
    
    // Strategy 2: Find by username
    if (user.value.username) {
      const balance = balances.value.find(b => b.user?.username === user.value?.username)
      if (balance) return balance
    }
    
    // Strategy 3: Find by user ID
    if (user.value.id) {
      const balance = balances.value.find(b => 
        b.userId === user.value?.id || 
        b.user?.id === user.value?.id
      )
      if (balance) return balance
    }
  }
  
  // Strategy 4: Use currentUserId if available
  if (currentUserId.value) {
    const balance = balances.value.find(b => b.userId === currentUserId.value)
    if (balance) return balance
  }
  
  return null
})

// Check if balances are still loading
const balancesLoading = computed(() => {
  return loading.value || balancesLoadingState.value || (balances.value.length === 0 && !props.currentUserBalance && members.value.length === 0)
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
  if (!canSubmit.value || !props.workspaceId) return
  
  try {
    // Create a settlement for this payment
    await createSettlement(props.workspaceId, {
      note: paymentNote.value.trim() || undefined
    })
    success(`${t('dashboard.paymentCompleted')}: ${formatCurrency(paymentAmount.value)}`)
    emit('payment-completed')
    emit('update:modelValue', false)
  } catch (err: any) {
    showError(err.message || t('dashboard.paymentFailed'))
  }
}
</script>

