<template>
  <BottomSheet :model-value="modelValue" @update:model-value="(val) => emit('update:modelValue', val)" :title="t('dashboard.settleUp')">
    <!-- Loading State -->
    <div v-if="loading" class="flex items-center justify-center py-12">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-emerald-500"></div>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="bg-red-500/10 border border-red-500/20 rounded-xl p-4">
      <p class="text-red-400 text-sm">{{ error.message || t('dashboard.loadBalancesFailed') }}</p>
      <button
        @click="loadBalances"
        class="mt-2 text-sm text-red-400 hover:text-red-300 underline"
      >
        {{ t('common.retry') }}
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
              class="px-4 py-2 bg-emerald-500 hover:bg-emerald-600 text-white font-medium rounded-lg transition-colors text-sm"
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
  </BottomSheet>
</template>

<script setup lang="ts">
import { useSettleUp } from '~/composables/useSettleUp'
import { useAuth } from '~/composables/useAuth'
import { useFormatting } from '~/composables/useFormatting'
import type { Balance } from '~/types/api'

interface Props {
  modelValue: boolean
  workspaceId: string
}

const props = defineProps<Props>()
const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'open-payment': [balance: Balance, currentUserBalance?: Balance | null]
  'settled': []
}>()

const { t } = useI18n()
const { formatCurrency } = useFormatting()
const { user } = useAuth()
const { settleUpData, loading, error, fetchSettleUp } = useSettleUp()

// Load settle-up data when sheet opens
watch(() => props.modelValue, (isOpen) => {
  if (isOpen && props.workspaceId) {
    loadSettleUp()
  }
})

const loadSettleUp = async () => {
  if (props.workspaceId) {
    await fetchSettleUp(props.workspaceId)
  }
}

// Get current user's balance from settle-up data
const currentUserBalance = computed(() => {
  if (!settleUpData.value) return null
  
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
})

// Transform otherMembers from settle-up data to Balance objects
const otherMemberBalances = computed(() => {
  if (!settleUpData.value) return []
  
  return settleUpData.value.otherMembers.map(member => {
    const balance = member.paidAmount - member.expectedAmount
    return {
      userId: member.userId,
      paid: member.paidAmount,
      expected: member.expectedAmount,
      balance: balance,
      user: {
        id: member.userId,
        displayName: member.userFullName,
        email: '',
        createdAt: ''
      }
    } as Balance
  })
})

const handlePay = (balance: Balance) => {
  // Close settle up sheet and open payment sheet
  emit('update:modelValue', false)
  // Get current user's balance to pass along
  const currentUserBal = currentUserBalance.value
  // Emit event to parent to open payment sheet with both balances
  emit('open-payment', balance, currentUserBal)
}
</script>

