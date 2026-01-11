<template>
  <div class="rounded-2xl bg-gradient-to-br from-slate-900 to-slate-950 shadow-lg shadow-black/40 p-4 space-y-4">
    <!-- Header Row -->
    <div class="flex items-center justify-between">
      <h2 class="text-lg font-semibold text-white/90">{{ t('dashboard.recentExpenses') }}</h2>
      <NuxtLink
        v-if="!isLoading"
        to="/expenses"
        class="text-sm text-white/60 hover:text-white/80 font-medium flex items-center gap-1 transition-colors"
      >
        <span>{{ t('dashboard.viewAll') }}</span>
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
        </svg>
      </NuxtLink>
      <div v-else class="h-5 w-20 bg-slate-700/50 rounded animate-pulse"></div>
    </div>

    <!-- Loading Skeleton -->
    <template v-if="isLoading">
      <div class="space-y-0">
        <template v-for="i in 3" :key="i">
          <div class="flex items-center justify-between py-3">
            <div class="flex items-center gap-3 flex-1">
              <div class="w-11 h-11 rounded-full bg-slate-700/50 animate-pulse"></div>
              <div class="flex-1 space-y-2 min-w-0">
                <div class="h-5 w-32 bg-slate-700/50 rounded animate-pulse"></div>
                <div class="h-3 w-40 bg-slate-700/30 rounded animate-pulse"></div>
                <div class="h-3 w-24 bg-slate-700/20 rounded animate-pulse"></div>
              </div>
            </div>
            <div class="h-5 w-20 bg-slate-700/50 rounded animate-pulse flex-shrink-0"></div>
          </div>
          <div v-if="i < 3" class="h-px bg-white/10 ml-14 mr-0"></div>
        </template>
      </div>
    </template>

    <!-- Content -->
    <template v-else>
    <!-- Expense List -->
    <div class="space-y-0">
      <template v-for="(expense, index) in displayedExpenses" :key="expense.id">
        <button
          type="button"
          @click="emit('openExpense', expense.id)"
          class="w-full flex items-center justify-between py-3 transition-colors hover:bg-white/5 rounded-lg px-1 -mx-1 relative"
        >
          <!-- Left Icon with Font Awesome -->
          <div
            class="w-11 h-11 rounded-full flex items-center justify-center flex-shrink-0 shadow-lg"
            :style="{ background: expense.gradient }"
          >
            <FontAwesomeIcon
              :icon="getFontAwesomeIcon(expense.iconClass)"
              class="w-5 h-5 text-white/80"
            />
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
        <!-- Separator Line (except last) -->
        <div
          v-if="index < displayedExpenses.length - 1"
          class="h-px bg-white/10 ml-14 mr-0"
        ></div>
      </template>
    </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import type { Expense } from '~/types/api'
import { useWorkspacesStore } from '~/stores/workspaces'
import { useAnalytics } from '~/composables/useAnalytics'

interface Props {
  expenses?: Expense[]
  loading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  expenses: () => [],
  loading: false
})

const emit = defineEmits<{
  openExpense: [id: string]
}>()

const { t } = useI18n()
const { formatCurrency } = useFormatting()
const { parseIconClass } = useFontAwesome()
const { colorToGradient } = useCategoryColor()

const workspacesStore = useWorkspacesStore()
const { activeWorkspaceId } = storeToRefs(workspacesStore)
// Use shared analytics composable to access data (fetched by parent dashboard page)
// Note: Parent dashboard page handles data fetching, this component just displays the data
const { recentExpenses, loading: analyticsLoading } = useAnalytics()

// Combine prop loading with analytics loading
const isLoading = computed(() => props.loading || analyticsLoading.value)

const formatDate = (dateISO: string): string => {
  const date = new Date(dateISO)
  return new Intl.DateTimeFormat('en-US', { month: 'short', day: 'numeric' }).format(date)
}

// Helper to get Font Awesome icon from class string
const getFontAwesomeIcon = (iconClass: string | null | undefined) => {
  const parsed = parseIconClass(iconClass)
  if (!parsed) {
    // Fallback to ellipsis icon
    return ['fas', 'ellipsis']
  }
  return [parsed.prefix, parsed.icon]
}

// Use API data from recentExpenses if available, otherwise fall back to props
const displayedExpenses = computed(() => {
  // Use API data if available
  const expensesToUse = recentExpenses.value.length > 0 ? recentExpenses.value : props.expenses
  
  return [...expensesToUse]
    .sort((a, b) => new Date(b.effectiveDate).getTime() - new Date(a.effectiveDate).getTime())
    .slice(0, 3)
    .map(expense => ({
      id: expense.id,
      title: expense.categoryName || t('common.other'),
      dateISO: expense.effectiveDate,
      paidBy: expense.paidByUserName || expense.paidByUserId,
      amount: expense.amount,
      note: expense.note,
      iconClass: expense.categoryIcon || 'fa-solid fa-ellipsis', // Use real API icon class
      gradient: colorToGradient(expense.categoryColor) // Use real API categoryColor
    }))
})

// Note: Data is fetched by parent dashboard page via fetchRecentExpenses(workspaceId, 5)
// No need to fetch here - parent passes data as props
// The component will use props.expenses or recentExpenses from the shared composable
</script>

