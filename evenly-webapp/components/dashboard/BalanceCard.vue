<template>
  <div class="balance-card-bg rounded-2xl p-4 space-y-4 relative overflow-hidden">
    <!-- Loading Skeleton -->
    <template v-if="loading || !balanceSummary">
      <div class="flex items-center justify-between">
        <div class="flex flex-col space-y-2 flex-1">
          <div class="h-4 w-24 bg-slate-700/50 rounded animate-pulse"></div>
          <div class="h-9 w-40 bg-slate-600/50 rounded animate-pulse"></div>
          <div class="h-4 w-32 bg-slate-700/50 rounded animate-pulse"></div>
        </div>
        <div class="w-30 h-20 bg-slate-700/30 rounded-lg animate-pulse"></div>
      </div>
      <div class="space-y-2">
        <div class="h-4 w-20 bg-slate-700/50 rounded animate-pulse"></div>
        <div class="w-full h-2.5 bg-slate-800 rounded-full overflow-hidden">
          <div class="h-full w-1/3 bg-emerald-500/30 rounded-full animate-pulse"></div>
        </div>
        <div class="h-3 w-32 bg-slate-700/50 rounded animate-pulse ml-auto"></div>
      </div>
      <div class="w-full h-12 bg-slate-700/30 rounded-xl animate-pulse"></div>
    </template>

    <!-- Content -->
    <template v-else>
      <!-- Total Paid Section -->
      <div class="flex items-center justify-between">
        <div class="flex flex-col">
          <span class="text-sm text-gray-400 mb-1">{{ t('dashboard.totalPaid') }}</span>
          <span class="text-3xl font-semibold text-white">
            {{ formatCurrency(balanceSummary?.userTotalPaidAmount || 0, balanceSummary?.currency) }}
          </span>
          <!-- You Owe / You Are Owed (only for non-personal workspaces) -->
          <div v-if="!isPersonal && balanceDifference !== 0" class="mt-1">
            <span :class="balanceDifferenceClass" class="text-sm font-medium">
              {{ balanceDifferenceText }}
            </span>
          </div>
        </div>
        <img
          src="/images/wallet.png"
          :alt="t('dashboard.totalPaid')"
          class="w-30 h-20 object-contain"
        />
      </div>

      <!-- Budget Section - Only show if budgetLimit exists and > 0 -->
      <div v-if="hasBudgetLimit" class="space-y-2">
        <div>
          <span class="text-sm text-gray-300">{{ t('dashboard.budget') }}</span>
        </div>

        <!-- Progress Bar -->
        <div class="w-full h-2.5 bg-slate-800 rounded-full overflow-hidden">
          <div
            class="h-full rounded-full transition-all duration-300"
            :class="progressBarColorClass"
            :style="{ width: `${progressBarWidth}%` }"
          ></div>
        </div>

        <!-- Budget Text -->
        <div class="text-right">
          <span class="text-xs text-gray-400">
            {{ formatCurrency(balanceSummary?.workspaceTotalPaidAmount || 0, balanceSummary?.currency) }} / {{ formatCurrency(balanceSummary?.budgetLimit || 0, balanceSummary?.currency) }}
          </span>
        </div>
      </div>

      <!-- Action Buttons (only for non-personal workspaces) -->
      <div v-if="!isPersonal && workspaceId" class="mt-4 flex gap-3">
        <button
          @click="emit('add-expense')"
          class="flex-1 py-3 bg-slate-700 hover:bg-slate-600 text-white font-medium rounded-xl transition-colors flex items-center justify-center gap-2"
        >
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
          </svg>
          {{ t('expenses.addExpense') }}
        </button>
        <button
          @click="emit('settle-up')"
          class="flex-1 py-3 bg-emerald-500 hover:bg-emerald-600 text-white font-medium rounded-xl transition-colors flex items-center justify-center gap-2"
        >
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          {{ t('dashboard.settleUp') }}
        </button>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import type { BalanceSummary } from '~/types/api'

interface Props {
  balanceSummary: BalanceSummary | null
  isPersonal?: boolean
  workspaceId?: string
  loading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  isPersonal: false,
  workspaceId: undefined,
  loading: false
})

const emit = defineEmits<{
  'settle-up': []
  'add-expense': []
}>()

const { formatCurrency } = useFormatting()
const { t } = useI18n()

// Check if budget limit exists and is greater than 0
const hasBudgetLimit = computed(() => {
  return props.balanceSummary?.budgetLimit != null && props.balanceSummary.budgetLimit > 0
})

// Progress bar width: if workspaceTotalPaidAmount exceeds limit, show full bar (100%)
const progressBarWidth = computed(() => {
  if (!props.balanceSummary?.budgetLimit || props.balanceSummary.budgetLimit <= 0) return 0
  
  // Calculate percentage based on workspaceTotalPaidAmount and budgetLimit
  const percentage = (props.balanceSummary.workspaceTotalPaidAmount / props.balanceSummary.budgetLimit) * 100
  
  // If workspaceTotalPaidAmount exceeds budgetLimit, show full bar
  if (percentage > 100) {
    return 100
  }
  
  return Math.min(percentage, 100)
})

// Progress bar color based on workspaceTotalPaidAmount vs budgetLimit
const progressBarColorClass = computed(() => {
  if (!props.balanceSummary?.budgetLimit || props.balanceSummary.budgetLimit <= 0) {
    return 'bg-emerald-500'
  }
  
  // Calculate percentage based on workspaceTotalPaidAmount and budgetLimit
  const percentage = (props.balanceSummary.workspaceTotalPaidAmount / props.balanceSummary.budgetLimit) * 100
  
  // Less than 60%: green
  if (percentage < 60) {
    return 'bg-emerald-500'
  }
  
  // Between 61% and 80%: yellow (warning)
  if (percentage >= 61 && percentage <= 80) {
    return 'bg-yellow-500'
  }
  
  // More than 80%: darker red (danger)
  return 'bg-red-600'
})

// Calculate balance difference for "You owe" / "You are owed"
const balanceDifference = computed(() => {
  if (!props.balanceSummary || props.isPersonal) return 0
  return (props.balanceSummary.userTotalPaidAmount || 0) - (props.balanceSummary.userTotalExpectedAmount || 0)
})

const balanceDifferenceText = computed(() => {
  const diff = balanceDifference.value
  if (diff > 0) {
    return `${t('dashboard.youAreOwed')}: ${formatCurrency(Math.abs(diff), props.balanceSummary?.currency)}`
  } else if (diff < 0) {
    return `${t('dashboard.youOwe')}: ${formatCurrency(Math.abs(diff), props.balanceSummary?.currency)}`
  }
  return ''
})

const balanceDifferenceClass = computed(() => {
  const diff = balanceDifference.value
  if (diff > 0) {
    return 'text-emerald-400'
  } else if (diff < 0) {
    return 'text-rose-400'
  }
  return 'text-gray-400'
})
</script>

