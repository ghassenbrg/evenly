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
      <div class="text-base font-semibold text-white/85 flex-shrink-0">
        {{ formatCurrency(expense.amount) }}
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

