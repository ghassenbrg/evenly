<template>
  <button
    type="button"
    @click="emit('click', expense.id)"
    class="w-full flex items-center justify-between py-3.5 px-2 transition-colors bg-white/5 hover:bg-white/8 rounded-lg relative"
  >
      <!-- Left Icon with Font Awesome -->
      <div
        class="w-11 h-11 rounded-full flex items-center justify-center flex-shrink-0 shadow-lg"
        :style="{ background: categoryGradient }"
      >
        <FontAwesomeIcon
          :icon="getFontAwesomeIcon(props.expense.categoryIcon)"
          class="w-5 h-5 text-white/80"
        />
      </div>

      <!-- Middle Text Block -->
      <div class="flex-1 min-w-0 px-3 text-left">
        <div class="text-base font-medium text-white/90 text-left">{{ categoryName }}</div>
        <div class="text-sm text-white/55 mt-0.5 text-left">
          {{ formatDate(expenseDate) }} · {{ t('dashboard.paidBy') }} {{ paidByName }}
        </div>
        <div v-if="expense.note" class="text-xs text-white/50 mt-1 text-left">
          {{ expense.note }}
        </div>
      </div>

      <!-- Right Amount -->
      <div class="text-base font-semibold text-white/85 flex-shrink-0 text-right">
        <div>{{ formatCurrency(expense.amount) }}</div>
        <div
          v-if="isSettled"
          class="mt-1 inline-flex items-center gap-1 text-[11px] font-medium text-amber-200/90 bg-amber-500/15 px-2 py-0.5 rounded-full"
        >
          <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 11V7a4 4 0 00-8 0v4m1 0h14a2 2 0 012 2v5a2 2 0 01-2 2H5a2 2 0 01-2-2v-5a2 2 0 012-2z" />
          </svg>
          <span>{{ t('dashboard.settled') }}</span>
        </div>
      </div>
  </button>
</template>

<script setup lang="ts">
import type { Expense } from '~/types/api'

interface Props {
  expense: Expense
}

const props = defineProps<Props>()

const emit = defineEmits<{
  click: [id: string]
}>()

const { formatCurrency, formatDate } = useFormatting()
const { t } = useI18n()
const { parseIconClass } = useFontAwesome()
const { colorToGradient } = useCategoryColor()

// Use API field names from endpoints.json
const categoryName = computed(() => props.expense.categoryName || t('common.other'))
const paidByName = computed(() => props.expense.paidByUserName || props.expense.paidByUserId)
const expenseDate = computed(() => props.expense.effectiveDate)
const isSettled = computed(() => props.expense.status === 'SETTLED' || !!props.expense.settlementId)

// Use categoryColor from API, fallback to default
const categoryGradient = computed(() => {
  return colorToGradient(props.expense.categoryColor)
})

// Helper to get Font Awesome icon from class string
const getFontAwesomeIcon = (iconClass: string | null | undefined) => {
  const parsed = parseIconClass(iconClass)
  if (!parsed) {
    // Fallback to ellipsis icon
    return ['fas', 'ellipsis']
  }
  return [parsed.prefix, parsed.icon]
}
</script>
