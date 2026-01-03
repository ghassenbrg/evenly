<template>
  <BottomSheet :model-value="modelValue" @update:model-value="(val) => emit('update:modelValue', val)" :title="t('dashboard.settleUp')">
    <!-- Loading State -->
    <div v-if="loading" class="flex items-center justify-center py-12">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-emerald-500"></div>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="bg-red-500/10 border border-red-500/20 rounded-xl p-4">
      <p class="text-red-400 text-sm">{{ error.message || 'Failed to load balances' }}</p>
      <button
        @click="loadBalances"
        class="mt-2 text-sm text-red-400 hover:text-red-300 underline"
      >
        Retry
      </button>
    </div>

    <!-- Content -->
    <div v-else class="space-y-4">
      <p class="text-sm text-gray-400">{{ t('dashboard.settleUpDescription') }}</p>

      <!-- Member Balances List -->
      <div class="space-y-3">
        <div
          v-for="balance in otherMemberBalances"
          :key="balance.userId"
          class="flex items-center justify-between p-4 bg-slate-800/50 rounded-xl border border-slate-700/50"
        >
          <div class="flex-1">
            <p class="text-base font-medium text-white">{{ balance.user?.displayName || balance.userId }}</p>
            <p class="text-xs text-gray-400 mt-1">
              {{ t('dashboard.paid') }}: {{ formatCurrency(balance.paid) }} • 
              {{ t('dashboard.expected') }}: {{ formatCurrency(balance.expected) }}
            </p>
          </div>
          
          <div class="flex items-center gap-3">
            <div class="text-right">
              <p 
                :class="balance.balance < 0 ? 'text-rose-400' : balance.balance > 0 ? 'text-emerald-400' : 'text-gray-400'"
                class="text-lg font-semibold"
              >
                {{ formatCurrency(balance.balance) }}
              </p>
            </div>
            
            <button
              @click="handlePay(balance)"
              :disabled="settling"
              class="px-4 py-2 bg-emerald-500 hover:bg-emerald-600 disabled:bg-slate-700 disabled:text-gray-500 text-white font-medium rounded-lg transition-colors text-sm"
            >
              {{ t('dashboard.pay') }}
            </button>
          </div>
        </div>

        <!-- Empty State -->
        <div v-if="otherMemberBalances.length === 0 && !loading" class="text-center py-8">
          <p class="text-gray-400">{{ t('dashboard.noBalancesToSettle') }}</p>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="flex gap-3">
        <button
          @click="emit('update:modelValue', false)"
          class="flex-1 bg-slate-700 hover:bg-slate-600 text-white font-medium py-3 rounded-xl transition-colors"
        >
          {{ t('common.close') }}
        </button>
        <button
          v-if="otherMemberBalances.length > 0 && hasOutstandingBalances"
          @click="handleSettleAll"
          :disabled="settling"
          class="flex-1 bg-emerald-500 hover:bg-emerald-600 disabled:bg-slate-700 disabled:text-gray-500 text-white font-medium py-3 rounded-xl transition-colors"
        >
          <span v-if="!settling">{{ t('dashboard.settleAll') }}</span>
          <span v-else>{{ t('dashboard.settling') }}</span>
        </button>
      </div>
    </template>
  </BottomSheet>
</template>

<script setup lang="ts">
import { useBalance } from '~/composables/useBalance'
import { useSettlements } from '~/composables/useSettlements'
import { useAuth } from '~/composables/useAuth'
import { useWorkspaceMembers } from '~/composables/useWorkspaceMembers'
import { useFormatting } from '~/composables/useFormatting'
import type { Balance } from '~/types/api'

interface Props {
  modelValue: boolean
  workspaceId: string
}

const props = defineProps<Props>()
const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'settled': []
  'open-payment': [balance: Balance, currentUserBalance?: Balance | null]
}>()

const { t } = useI18n()
const { formatCurrency } = useFormatting()
const { user } = useAuth()
const { balances, loading, error, fetchBalances } = useBalance()
const { members, fetchMembers } = useWorkspaceMembers()
const { createSettlement, loading: settling } = useSettlements()
const { success, error: showError } = useToast()

// Load balances and members when sheet opens
watch(() => props.modelValue, (isOpen) => {
  if (isOpen && props.workspaceId) {
    loadBalances()
    fetchMembers(props.workspaceId)
  }
})

const loadBalances = async () => {
  if (props.workspaceId) {
    await fetchBalances(props.workspaceId)
  }
}

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

// Get current user's balance
const currentUserBalance = computed(() => {
  if (!currentUserId.value || balances.value.length === 0) return null
  
  // Find by userId
  let balance = balances.value.find(b => b.userId === currentUserId.value)
  if (balance) return balance
  
  // Fallback: find by user.id or user.email or user.username
  if (user.value) {
    balance = balances.value.find(b => 
      b.user?.id === user.value?.id ||
      b.user?.email === user.value?.email ||
      b.user?.username === user.value?.username
    )
    if (balance) return balance
  }
  
  return null
})

// Filter out current user's balance and show only other members
// Show all members even if balance is 0
const otherMemberBalances = computed(() => {
  if (!currentUserId.value) {
    // If we can't identify current user, show all balances
    return balances.value
  }
  return balances.value.filter(b => b.userId !== currentUserId.value)
})

const hasOutstandingBalances = computed(() => {
  return otherMemberBalances.value.some(b => b.balance !== 0)
})

const handlePay = (balance: Balance) => {
  // Close settle up sheet and open payment sheet
  emit('update:modelValue', false)
  // Get current user's balance to pass along
  const currentUserBal = currentUserBalance.value
  // Emit event to parent to open payment sheet with both balances
  emit('open-payment', balance, currentUserBal)
}

const handleSettleAll = async () => {
  if (!props.workspaceId) return
  
  try {
    // Create a settlement for all outstanding balances
    await createSettlement(props.workspaceId, {})
    success(t('dashboard.settlementCreated'))
    // Reload balances
    await loadBalances()
    emit('settled')
  } catch (err: any) {
    showError(err.message || t('dashboard.settlementFailed'))
  }
}
</script>

