<template>
  <button
    type="button"
    @click="emit('click', expense.id)"
    class="w-full flex items-center justify-between py-3.5 px-2 transition-colors bg-white/5 hover:bg-white/8 rounded-lg relative"
  >
      <!-- Left Icon -->
      <div
        class="w-11 h-11 rounded-full flex items-center justify-center flex-shrink-0 shadow-lg"
        :style="{ background: getCategoryGradient(expense.category) }"
      >
        <svg
          v-if="expense.category === 'groceries'"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
          class="w-5 h-5 text-white/80"
        >
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" />
        </svg>
        <svg
          v-else-if="expense.category === 'dining'"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
          class="w-5 h-5 text-white/80"
        >
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" />
        </svg>
        <svg
          v-else-if="expense.category === 'transportation'"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
          class="w-5 h-5 text-white/80"
        >
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 20l-5.447-2.724A1 1 0 013 16.382V5.618a1 1 0 011.447-.894L9 7m0 13l6-3m-6 3V7m6 10l4.553 2.276A1 1 0 0021 18.382V7.618a1 1 0 00-.553-.894L15 4m0 13V4m0 0L9 7" />
        </svg>
        <svg
          v-else-if="expense.category === 'rent'"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
          class="w-5 h-5 text-white/80"
        >
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
        </svg>
        <svg
          v-else-if="expense.category === 'bills'"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
          class="w-5 h-5 text-white/80"
        >
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
        </svg>
        <svg
          v-else-if="expense.category === 'internet'"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
          class="w-5 h-5 text-white/80"
        >
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8.111 16.404a5.5 5.5 0 017.778 0M12 20h.01m-7.08-7.071c3.904-3.905 10.236-3.905 14.141 0M1.394 9.393c5.857-5.857 15.355-5.857 21.213 0" />
        </svg>
        <svg
          v-else-if="expense.category === 'mobile'"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
          class="w-5 h-5 text-white/80"
        >
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 18h.01M8 21h8a2 2 0 002-2V5a2 2 0 00-2-2H8a2 2 0 00-2 2v14a2 2 0 002 2z" />
        </svg>
        <svg
          v-else
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
          class="w-5 h-5 text-white/80"
        >
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
        </svg>
      </div>

      <!-- Middle Text Block -->
      <div class="flex-1 min-w-0 px-3 text-left">
        <div class="text-base font-medium text-white/90 text-left">{{ expense.title }}</div>
        <div class="text-sm text-white/55 mt-0.5 text-left">
          {{ formatDate(expense.dateISO) }} · {{ t('dashboard.paidBy') }} {{ expense.paidBy }}
        </div>
        <div v-if="expense.note" class="text-xs text-white/50 mt-1 text-left">
          {{ expense.note }}
        </div>
      </div>

      <!-- Right Amount -->
      <div class="text-base font-semibold text-white/85 flex-shrink-0">
        {{ formatCurrency(expense.amount) }}
      </div>
  </button>
</template>

<script setup lang="ts">
interface Expense {
  id: string
  workspaceId: string
  category: 'groceries' | 'dining' | 'transportation' | 'rent' | 'bills' | 'internet' | 'mobile' | 'other'
  title: string
  dateISO: string
  paidBy: string
  amount: number
  note?: string
}

interface Props {
  expense: Expense
}

const props = defineProps<Props>()

const emit = defineEmits<{
  click: [id: string]
}>()

const { formatCurrency, formatDate } = useFormatting()
const { t } = useI18n()

const getCategoryGradient = (category: string) => {
  const gradients: Record<string, string> = {
    groceries: 'linear-gradient(135deg, #10b981 0%, #059669 100%)',
    dining: 'linear-gradient(135deg, #f43f5e 0%, #e11d48 100%)',
    transportation: 'linear-gradient(135deg, #3b82f6 0%, #2563eb 100%)',
    rent: 'linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%)',
    bills: 'linear-gradient(135deg, #f59e0b 0%, #d97706 100%)',
    internet: 'linear-gradient(135deg, #06b6d4 0%, #0891b2 100%)',
    mobile: 'linear-gradient(135deg, #6366f1 0%, #4f46e5 100%)',
    other: 'linear-gradient(135deg, #64748b 0%, #475569 100%)'
  }
  return gradients[category] || gradients.other
}
</script>

