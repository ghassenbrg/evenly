<template>
  <BottomSheet :model-value="modelValue" @update:model-value="(val) => emit('update:modelValue', val)">
    <template #header>
      <div class="flex items-center justify-between w-full">
        <h2 class="text-xl font-semibold text-white">{{ t('payments.editPayment') }}</h2>
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
    <div v-else-if="payment" class="space-y-6">
      <!-- Payment Info (Read-only) -->
      <div class="bg-slate-800/50 rounded-xl p-4 border border-slate-700/50">
        <p class="text-sm text-gray-400 mb-1">{{ isReceived ? t('payments.receivedFrom') : t('payments.paidTo') }}</p>
        <p class="text-lg font-semibold text-white">{{ otherUserName }}</p>
        <div class="mt-2 flex items-center justify-between text-sm">
          <span class="text-gray-400">{{ t('common.status') }}</span>
          <span
            class="px-2 py-0.5 rounded-full text-xs font-medium"
            :class="getStatusClass(payment.status)"
          >
            {{ getStatusLabel(payment.status) }}
          </span>
        </div>
      </div>

      <!-- Amount Input -->
      <AmountInput
        v-model="paymentAmount"
        :label="t('payments.amount')"
        :currency="payment.currency"
        :placeholder="'0'"
      />

      <!-- Effective Date -->
      <div>
        <label class="block text-sm font-medium text-slate-300 mb-2">
          {{ t('payments.effectiveDate') }}
        </label>
        <div class="date-input-wrapper">
          <input
            v-model="effectiveDate"
            type="date"
            class="w-full px-4 py-3 bg-slate-800 border border-slate-700 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-emerald-500"
          />
        </div>
      </div>

      <!-- Note Field -->
      <div>
        <label class="block text-sm font-medium text-slate-300 mb-2">
          {{ t('payments.note') }} <span class="text-slate-500 text-xs">({{ t('common.optional') }})</span>
        </label>
        <textarea
          v-model="paymentNote"
          rows="3"
          class="w-full px-4 py-3 bg-slate-800 border border-slate-700 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-emerald-500 resize-none"
          :placeholder="t('payments.notePlaceholder')"
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
import type { Payment } from '~/types/api'
import { usePayments } from '~/composables/usePayments'
import { useFormatting } from '~/composables/useFormatting'
import { useAuthStore } from '~/stores/auth'
import { useToast } from '~/composables/useToast'

interface Props {
  modelValue: boolean
  workspaceId: string
  payment: Payment | null
}

const props = defineProps<Props>()
const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'payment-updated': []
  'payment-deleted': []
}>()

const { t } = useI18n()
const { formatCurrency } = useFormatting()
const { updatePayment, deletePayment, loading: submitting, getPayment } = usePayments()
const { success, error: showError } = useToast()
const authStore = useAuthStore()
const { getCurrentUserId } = useAuth()

const loading = ref(false)
const deleting = ref(false)
const paymentAmount = ref(0)
const effectiveDate = ref('')
const paymentNote = ref('')

// Determine if current user received money (is payee) or paid money (is payer)
const isReceived = computed(() => {
  if (!props.payment) return false
  const currentUser = authStore.currentUser
  if (!currentUser) return false
  
  const currentUserId = getCurrentUserId() || ''
  const currentUserName = currentUser.username || currentUser.displayName || ''
  
  const payeeUserId = props.payment.payeeUserId || ''
  const payeeUserName = props.payment.payeeUserName || ''
  
  return payeeUserId === currentUserId || payeeUserName === currentUserName
})

// Get the other user's name
const otherUserName = computed(() => {
  if (!props.payment) return ''
  if (isReceived.value) {
    return props.payment.paidByUserName || props.payment.paidByUserId || 'Unknown'
  } else {
    return props.payment.payeeUserName || props.payment.payeeUserId || 'Unknown'
  }
})

// Load payment details when sheet opens
watch(() => props.modelValue, async (isOpen) => {
  if (isOpen && props.payment && props.workspaceId) {
    loading.value = true
    try {
      // Fetch fresh payment data
      const freshPayment = await getPayment(props.workspaceId, props.payment.id)
      if (freshPayment) {
        paymentAmount.value = freshPayment.amount
        effectiveDate.value = freshPayment.effectiveDate
        paymentNote.value = freshPayment.note || ''
      }
    } catch (err) {
      showError(t('payments.loadFailed'))
    } finally {
      loading.value = false
    }
  }
})

// Initialize form when payment changes
watch(() => props.payment, (payment) => {
  if (payment) {
    paymentAmount.value = payment.amount
    effectiveDate.value = payment.effectiveDate
    paymentNote.value = payment.note || ''
  }
}, { immediate: true })

const canSubmit = computed(() => {
  return paymentAmount.value > 0 && effectiveDate.value !== ''
})

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

const handleSubmit = async () => {
  if (!canSubmit.value || !props.workspaceId || !props.payment) return
  
  try {
    await updatePayment(props.workspaceId, props.payment.id, {
      amount: paymentAmount.value,
      effectiveDate: effectiveDate.value,
      note: paymentNote.value.trim() || undefined
    })
    success(t('payments.updated'))
    emit('payment-updated')
    emit('update:modelValue', false)
  } catch (err: any) {
    showError(err.message || t('payments.updateFailed'))
  }
}

const handleDelete = async () => {
  if (!props.workspaceId || !props.payment) return
  
  if (!confirm(t('payments.confirmDelete'))) return
  
  deleting.value = true
  try {
    await deletePayment(props.workspaceId, props.payment.id)
    success(t('payments.deleted'))
    emit('payment-deleted')
    emit('update:modelValue', false)
  } catch (err: any) {
    showError(err.message || t('payments.deleteFailed'))
  } finally {
    deleting.value = false
  }
}
</script>
