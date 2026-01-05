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
        :style="{ background: getStatusGradient(payment.status) }"
      >
        <!-- Payment icon - API doesn't provide type, so use default -->
        <svg
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
          class="w-5 h-5 text-white/80"
        >
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
      </div>
      
      <!-- Member Info -->
      <div class="flex flex-col items-start">
        <div class="flex items-center space-x-2">
          <span class="text-xs font-medium text-white/70">{{ getInitials(payment.paidByUserName) }}</span>
          <svg class="w-4 h-4 text-white/50" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 8l4 4m0 0l-4 4m4-4H3" />
          </svg>
          <span class="text-xs font-medium text-white/70">{{ getInitials(payment.payeeUserName) }}</span>
        </div>
        <div class="text-xs text-white/50 mt-0.5">
          {{ payerName }} → {{ payeeName }}
        </div>
      </div>
    </div>

    <!-- Middle Text Block -->
    <div class="flex-1 min-w-0 px-3 text-left">
      <div class="flex items-center space-x-2">
        <span class="text-base font-medium text-white/90">{{ t('payments.payment') }}</span>
        <span
          class="px-2 py-0.5 rounded-full text-xs font-medium"
          :class="getStatusClass(payment.status)"
        >
          {{ getStatusLabel(payment.status) }}
        </span>
      </div>
      <div class="text-sm text-white/55 mt-0.5">
        {{ formatDate(payment.effectiveDate) }}
      </div>
      <div v-if="payment.note" class="text-xs text-white/50 mt-1">
        {{ payment.note }}
      </div>
    </div>

    <!-- Right Amount -->
    <div class="text-base font-semibold text-white/85 flex-shrink-0">
      {{ formatCurrency(payment.amount, payment.currency) }}
    </div>
  </button>
</template>

<script setup lang="ts">
import type { Payment, User } from '~/types/api'

interface Props {
  payment: Payment
}

const props = defineProps<Props>()

const emit = defineEmits<{
  click: [id: string]
}>()

const { formatCurrency, formatDate } = useFormatting()
const { t } = useI18n()

// Use API field names from endpoints.json
const payerName = computed(() => props.payment.paidByUserName || props.payment.paidByUserId)
const payeeName = computed(() => props.payment.payeeUserName || props.payment.payeeUserId)

const getInitials = (name?: string) => {
  if (!name) return '?'
  return name
    .split(' ')
    .map(n => n[0])
    .join('')
    .toUpperCase()
    .slice(0, 2) || '??'
}

// Payment type not in API response, removed

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

const getStatusGradient = (status: string) => {
  const gradients: Record<string, string> = {
    COMPLETED: 'linear-gradient(135deg, #10b981 0%, #059669 100%)',
    PENDING: 'linear-gradient(135deg, #f59e0b 0%, #d97706 100%)',
    FAILED: 'linear-gradient(135deg, #ef4444 0%, #dc2626 100%)'
  }
  return gradients[status] || 'linear-gradient(135deg, #64748b 0%, #475569 100%)'
}
</script>

