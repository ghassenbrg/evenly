<template>
  <div class="rounded-2xl bg-gradient-to-br from-slate-900 to-slate-950 shadow-lg shadow-black/40 p-4 space-y-4">
    <!-- Header Row -->
    <div class="flex items-center justify-between">
      <h2 class="text-base font-semibold text-gray-100">{{ t('dashboard.categoriesBreakdown') }}</h2>
      <PeriodDropdown
        v-if="!isLoading"
        v-model="selectedPeriod"
        v-model:range="customRange"
        @period-change="handlePeriodChange"
      />
      <div v-else class="h-8 w-24 bg-slate-700/50 rounded-lg animate-pulse"></div>
    </div>

    <!-- Loading Skeleton -->
    <template v-if="isLoading">
      <div class="space-y-0">
        <template v-for="i in 4" :key="i">
          <div class="flex items-center justify-between py-3">
            <div class="flex items-center gap-3 flex-1">
              <div class="w-11 h-11 rounded-full bg-slate-700/50 animate-pulse"></div>
              <div class="flex-1 space-y-2">
                <div class="h-4 w-32 bg-slate-700/50 rounded animate-pulse"></div>
                <div class="h-3 w-20 bg-slate-700/30 rounded animate-pulse"></div>
              </div>
            </div>
            <div class="h-4 w-20 bg-slate-700/50 rounded animate-pulse"></div>
          </div>
          <div v-if="i < 4" class="h-px bg-white/10 ml-14 mr-0"></div>
        </template>
      </div>
      <div class="w-full h-12 bg-slate-700/30 rounded-xl animate-pulse"></div>
    </template>

    <!-- Content -->
    <template v-else>
    <!-- Category List -->
    <div class="space-y-0">
      <template v-for="(item, index) in displayItems" :key="item.id">
        <button
          type="button"
          @click="emit('openCategory', item.id)"
          class="w-full flex items-center justify-between py-3 transition-colors hover:bg-white/5 rounded-lg px-1 -mx-1 relative"
        >
          <!-- Left Icon with Font Awesome -->
          <div
            class="w-11 h-11 rounded-full flex items-center justify-center flex-shrink-0 shadow-lg"
            :style="{ background: colorToGradient(item.color) }"
          >
            <FontAwesomeIcon
              :icon="getFontAwesomeIcon(item.iconClass || 'fa-solid fa-ellipsis')"
              class="w-5 h-5 text-white/80"
            />
          </div>

          <!-- Middle Text Block -->
          <div class="flex-1 min-w-0 px-3">
            <div class="flex items-center gap-2">
              <span class="text-sm font-medium text-gray-100">{{ item.name }}</span>
              <span class="text-xs text-gray-400">
                {{ item.expenseCount }} {{ item.expenseCount === 1 ? t('dashboard.expense') : t('dashboard.expenses') }}
              </span>
            </div>
          </div>

          <!-- Right Amount -->
          <div class="text-sm font-semibold text-gray-200 flex-shrink-0">
            {{ formatCurrency(item.totalAmount) }}
          </div>
        </button>
        <!-- Separator Line (except last) -->
        <div
          v-if="index < displayItems.length - 1"
          class="h-px bg-white/10 ml-14 mr-0"
        ></div>
      </template>
    </div>

    <!-- Bottom CTA -->
    <button
      type="button"
      @click="handleOpenAllCategories"
      class="w-full py-3 px-4 bg-slate-800/80 hover:bg-slate-800 rounded-xl text-gray-200 font-medium flex items-center justify-center gap-2 transition-colors ring-1 ring-white/10"
    >
      <span>{{ t('dashboard.allCategories') }} ({{ displayTotalCategories }})</span>
      <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
      </svg>
    </button>
    </template>
  </div>
</template>

<script setup lang="ts">
import { endOfLocalDay, startOfLocalDay, toDateOnly } from '~/utils/date'
import type { ExpenseSnapshotResponse } from '~/types/api'
import { useWorkspacesStore } from '~/stores/workspaces'
import { useAnalytics } from '~/composables/useAnalytics'
import { useApi } from '~/utils/api'
import { useFontAwesome } from '~/composables/useFontAwesome'
import { useCategoryColor } from '~/composables/useCategoryColor'

interface CategoryItem {
  id: string
  name: string
  iconClass?: string // Font Awesome icon class from API
  icon?: 'groceries' | 'rent' | 'bills' | 'mobile' // Legacy support
  expenseCount: number
  totalAmount: number
  accent: 'green' | 'rose' | 'sky' | 'indigo'
  color?: string // Category color from API
}

interface Props {
  filterLabel?: string
  items?: CategoryItem[]
  totalCategories?: number
  loading?: boolean
}

type PeriodType = 'month' | 'week' | 'all' | 'custom'

const props = withDefaults(defineProps<Props>(), {
  filterLabel: undefined,
  totalCategories: 0,
  items: () => [],
  loading: false
})

const emit = defineEmits<{
  selectFilter: []
  'period-change': [period: PeriodType, range?: { start: string | null; end: string | null }]
  openAllCategories: [startDate?: string, endDate?: string]
  openCategory: [id: string]
}>()

const { t } = useI18n()
const { formatCurrency } = useFormatting()
const { parseIconClass } = useFontAwesome()
const { colorToGradient } = useCategoryColor()

const workspacesStore = useWorkspacesStore()
const { activeWorkspaceId } = storeToRefs(workspacesStore)
// Use shared analytics composable to access data (fetched by parent dashboard page)
// Note: Parent dashboard page handles initial data fetching, this component only fetches on period change
const { expenseSnapshot: sharedExpenseSnapshot, loading: analyticsLoading, fetchCategoryAnalytics } = useAnalytics()

// Local snapshot state - this component maintains its own snapshot independent of ExpenseSnapshotCard
const localExpenseSnapshot = ref<ExpenseSnapshotResponse | null>(null)

// Use local snapshot if available, otherwise fall back to shared snapshot (from parent initial load)
const expenseSnapshot = computed(() => localExpenseSnapshot.value || sharedExpenseSnapshot.value)

// Combine prop loading with analytics loading
const isLoading = computed(() => props.loading || analyticsLoading.value)

// Helper to get Font Awesome icon from class string
const getFontAwesomeIcon = (iconClass: string | null | undefined) => {
  const parsed = parseIconClass(iconClass)
  if (!parsed) {
    // Fallback to ellipsis icon
    return ['fas', 'ellipsis']
  }
  return [parsed.prefix, parsed.icon]
}

const selectedPeriod = ref<PeriodType>('month')
const customRange = ref<{ start: string | null; end: string | null }>({ start: null, end: null })

const getDateRange = (period: PeriodType, customRange?: { start: string | null; end: string | null }) => {
  const now = new Date()
  let start: Date
  let end: Date = endOfLocalDay(now)

  switch (period) {
    case 'month':
      start = startOfLocalDay(new Date(now.getFullYear(), now.getMonth(), 1))
      end = endOfLocalDay(new Date(now.getFullYear(), now.getMonth() + 1, 0))
      break
    case 'week':
      const dayOfWeek = now.getDay()
      const diff = now.getDate() - dayOfWeek + (dayOfWeek === 0 ? -6 : 1)
      start = startOfLocalDay(new Date(now.getFullYear(), now.getMonth(), diff))
      end = endOfLocalDay(now)
      break
    case 'all':
      start = startOfLocalDay(new Date(now.getFullYear(), now.getMonth() - 2, 1))
      end = endOfLocalDay(new Date(now.getFullYear(), now.getMonth() + 1, 0))
      break
    case 'custom':
      if (customRange?.start && customRange?.end) {
        start = startOfLocalDay(customRange.start)
        end = endOfLocalDay(customRange.end)
      } else {
        start = startOfLocalDay(new Date(now.getFullYear(), now.getMonth(), 1))
        end = endOfLocalDay(new Date(now.getFullYear(), now.getMonth() + 1, 0))
      }
      break
    default:
      start = startOfLocalDay(new Date(now.getFullYear(), now.getMonth(), 1))
      end = endOfLocalDay(new Date(now.getFullYear(), now.getMonth() + 1, 0))
  }

  return {
    start: toDateOnly(start),
    end: toDateOnly(end)
  }
}

const getAccentFromColor = (color: string): 'green' | 'rose' | 'sky' | 'indigo' => {
  if (color.includes('10b981') || color.includes('059669')) return 'green'
  if (color.includes('f43f5e') || color.includes('e11d48')) return 'rose'
  if (color.includes('0ea5e9') || color.includes('0284c7')) return 'sky'
  if (color.includes('6366f1') || color.includes('4f46e5')) return 'indigo'
  return 'green'
}

const handlePeriodChange = async (period: PeriodType, range?: { start: string | null; end: string | null }) => {
  selectedPeriod.value = period
  
  // For custom period, only proceed if we have a valid range with both start and end
  if (period === 'custom') {
    if (range && range.start && range.end) {
      customRange.value = range
      // Fetch data for this card only and store in local state
      if (activeWorkspaceId.value) {
        const { start, end } = getDateRange(period, range)
        const api = useApi()
        const queryParams = new URLSearchParams()
        if (start) queryParams.append('startDate', start)
        if (end) queryParams.append('endDate', end)
        queryParams.append('size', '4')
        const query = queryParams.toString()
        const path = `/api/workspaces/${activeWorkspaceId.value}/analytics/expenses-snapshot${query ? `?${query}` : ''}`
        localExpenseSnapshot.value = await api.get<ExpenseSnapshotResponse>(path)
      }
      emit('period-change', period, range)
    }
    // If no valid range, just update the selected period but don't fetch data
    return
  }
  
  // For non-custom periods, proceed immediately
  if (range) {
    customRange.value = range
  }
  
  // Fetch data for this card only and store in local state
  if (activeWorkspaceId.value) {
    const { start, end } = getDateRange(period, range)
    const api = useApi()
    const queryParams = new URLSearchParams()
    if (start) queryParams.append('startDate', start)
    if (end) queryParams.append('endDate', end)
    queryParams.append('size', '4')
    const query = queryParams.toString()
    const path = `/api/workspaces/${activeWorkspaceId.value}/analytics/expenses-snapshot${query ? `?${query}` : ''}`
    localExpenseSnapshot.value = await api.get<ExpenseSnapshotResponse>(path)
  }
  
  emit('period-change', period, range)
}

// Compute items from fetched expenseSnapshot - use real API data
// The API returns data sorted by totalAmount, with "Others" category (categoryId: null) at the end
const computedItems = computed(() => {
  if (!expenseSnapshot.value || !expenseSnapshot.value.data.length) return []
  
  // Filter out "Others" category (categoryId: null) and take top 4
  return expenseSnapshot.value.data
    .filter(item => item.categoryId !== null)
    .slice(0, 4)
    .map(item => ({
      id: item.categoryId || '',
      name: item.categoryName || t('common.unknown'),
      iconClass: item.categoryIcon || 'fa-solid fa-ellipsis', // Use real API categoryIcon
      expenseCount: item.expensesCount || 0,
      totalAmount: item.totalAmount,
      accent: getAccentFromColor(item.categoryColor || '#64748b'), // Use real API categoryColor
      color: item.categoryColor || '#64748b' // Store color for gradient
    }))
})

// Use computed values if available, otherwise fall back to props
const displayItems = computed(() => computedItems.value.length > 0 ? computedItems.value : props.items)
// Simply use categoriesCount from expenses-snapshot API
const displayTotalCategories = computed(() => {
  if (!expenseSnapshot.value) return 0
  return expenseSnapshot.value.categoriesCount || 0
})

const getCategoryGradient = (accent: string): string => {
  const gradients: Record<string, string> = {
    green: 'linear-gradient(135deg, #10b981 0%, #059669 100%)',
    rose: 'linear-gradient(135deg, #f43f5e 0%, #e11d48 100%)',
    sky: 'linear-gradient(135deg, #0ea5e9 0%, #0284c7 100%)',
    indigo: 'linear-gradient(135deg, #6366f1 0%, #4f46e5 100%)'
  }
  return gradients[accent] || gradients.green
}

// Note: Data is fetched by parent dashboard page, no need to fetch on mount
// Only fetch when user changes period (user-initiated action)
// Parent dashboard will reload data when workspace changes

// Watch for workspace changes - reset local state so it uses shared state from parent
watch(activeWorkspaceId, () => {
  localExpenseSnapshot.value = null
})

const handleOpenAllCategories = () => {
  const { start, end } = getDateRange(selectedPeriod.value, customRange.value)
  emit('openAllCategories', start, end)
}
</script>
