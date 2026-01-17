<template>
  <button
    type="button"
    @click="emit('click', payment.id)"
    class="w-full flex items-center justify-between py-3.5 px-2 transition-colors bg-white/5 hover:bg-white/8 rounded-lg relative"
  >
    <!-- Left Avatar/Icon -->
    <div class="flex items-center space-x-3 flex-shrink-0">
      <div
        class="w-11 h-11 rounded-full flex items-center justify-center flex-shrink-0 shadow-lg"
        :style="{ background: iconGradient }"
      >
        <!-- Direction icon: arrow up for received, arrow down for paid -->
        <svg
          v-if="isReceived"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
          class="w-5 h-5 text-white/90"
        >
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 10l7-7m0 0l7 7m-7-7v18" />
        </svg>
        <svg
          v-else
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
          class="w-5 h-5 text-white/90"
        >
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 14l-7 7m0 0l-7-7m7 7V3" />
        </svg>
      </div>
      
    </div>

    <!-- Middle Text Block -->
    <div class="flex-1 min-w-0 px-3 text-left">
      <div class="flex items-center space-x-2">
        <span class="text-base font-medium text-white/90">
          {{ isReceived ? t('payments.paymentReceivedFrom', { name: otherUserName }) : t('payments.paymentPaidTo', { name: otherUserName }) }}
        </span>
        <span
          class="px-2 py-0.5 rounded-full text-xs font-medium"
          :class="getStatusClass(payment.status)"
        >
          {{ getStatusLabel(payment.status) }}
        </span>
        <span
          v-if="isSettled"
          class="px-2 py-0.5 rounded-full text-xs font-medium text-amber-200/90 bg-amber-500/15 inline-flex items-center gap-1"
        >
          <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 11V7a4 4 0 00-8 0v4m1 0h14a2 2 0 012 2v5a2 2 0 01-2 2H5a2 2 0 01-2-2v-5a2 2 0 012-2z" />
          </svg>
          <span>{{ t('dashboard.settled') }}</span>
        </span>
      </div>
      <div class="text-sm text-white/55 mt-0.5">
        {{ formatDate(payment.effectiveDate) }}
      </div>
      <div v-if="payment.note" class="text-xs text-white/50 mt-1">
        {{ payment.note }}
      </div>
    </div>

    <!-- Right Amount with Sign -->
    <div 
      class="text-base font-semibold flex-shrink-0"
      :class="amountClass"
    >
      {{ formattedAmount }}
    </div>
  </button>
</template>

<script setup lang="ts">
import type { Payment } from '~/types/api'
import { useAuthStore } from '~/stores/auth'

interface Props {
  payment: Payment
}

const props = defineProps<Props>()

const emit = defineEmits<{
  click: [id: string]
}>()

const { formatCurrency, formatDate } = useFormatting()
const { t } = useI18n()
const authStore = useAuthStore()
const { getCurrentUserId } = useAuth()

// Determine if current user received money (is payee) or paid money (is payer)
const isReceived = computed(() => {
  const currentUser = authStore.currentUser
  if (!currentUser) return false
  
  // Get current user identifiers
  const currentUserId = getCurrentUserId() || ''
  const currentUserName = currentUser.username || currentUser.displayName || ''
  
  // Get payment identifiers
  const payeeUserId = props.payment.payeeUserId || ''
  const payeeUserName = props.payment.payeeUserName || ''
  const payerUserId = props.payment.paidByUserId || ''
  const payerUserName = props.payment.paidByUserName || ''
  
  // Check if current user is the payee (received money) - exact match first
  const isPayee = payeeUserId === currentUserId || payeeUserName === currentUserName
  
  // Check if current user is the payer (paid money) - exact match first
  const isPayer = payerUserId === currentUserId || payerUserName === currentUserName
  
  // If user is payee, they received money
  if (isPayee) return true
  
  // If user is payer, they paid money
  if (isPayer) return false
  
  // Default: assume user paid (conservative approach)
  return false
})

// Get the other user's name (not the current user)
const otherUserName = computed(() => {
  const currentUserId = getCurrentUserId()
  if (!currentUserId) {
    // Fallback: if we can't determine current user, default to showing payee
    return props.payment.payeeUserName || props.payment.payeeUserId || 'Unknown'
  }
  
  if (isReceived.value) {
    // Current user received money, show who paid
    return props.payment.paidByUserName || props.payment.paidByUserId || 'Unknown'
  } else {
    // Current user paid money, show who received
    return props.payment.payeeUserName || props.payment.payeeUserId || 'Unknown'
  }
})

// Icon gradient based on direction
const iconGradient = computed(() => {
  if (isReceived.value) {
    // Green gradient for received money
    return 'linear-gradient(135deg, #10b981 0%, #059669 100%)'
  } else {
    // Red gradient for paid money
    return 'linear-gradient(135deg, #ef4444 0%, #dc2626 100%)'
  }
})

// Amount class (color based on direction)
const amountClass = computed(() => {
  if (isReceived.value) {
    return 'text-emerald-400' // Green for received
  } else {
    return 'text-red-400' // Red for paid
  }
})

// Formatted amount with sign
const formattedAmount = computed(() => {
  const amount = formatCurrency(props.payment.amount, props.payment.currency)
  if (isReceived.value) {
    return `+${amount}` // Positive for received
  } else {
    return `-${amount}` // Negative for paid
  }
})

const isSettled = computed(() => !!props.payment.settlementId)

const getStatusLabel = (status: string) => {
  const labels: Record<string, string> = {
    COMPLETED: t('payments.status.completed'),
    PENDING: t('payments.status.pending'),
    FAILED: t('payments.status.failed')
  }
  return labels[status] || status
}

const getStatusClass = (status: string) => {
  const classes: Record<string, string> = {
    COMPLETED: 'bg-emerald-500/20 text-emerald-400',
    PENDING: 'bg-yellow-500/20 text-yellow-400',
    FAILED: 'bg-red-500/20 text-red-400'
  }
  return classes[status] || 'bg-slate-500/20 text-slate-400'
}
</script>
