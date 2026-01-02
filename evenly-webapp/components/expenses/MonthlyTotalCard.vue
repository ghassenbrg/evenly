<template>
  <div class="balance-card-bg rounded-2xl p-3 relative overflow-hidden">
    <!-- Header with Dropdown -->
    <div class="flex items-center justify-between mb-2">
      <p class="text-xs text-white/50">{{ periodLabel }}</p>
      <div class="relative">
        <button
          type="button"
          @click="showDropdown = !showDropdown"
          class="px-2.5 py-1 bg-slate-800/80 ring-1 ring-white/10 rounded-lg text-xs text-gray-200 font-medium flex items-center gap-1 hover:bg-slate-800 transition-colors"
        >
          <span>{{ selectedPeriodLabel }}</span>
          <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
          </svg>
        </button>
        
        <!-- Dropdown Menu -->
        <div
          v-if="showDropdown"
          class="absolute right-0 top-full mt-1.5 w-36 bg-slate-800 ring-1 ring-white/10 rounded-lg shadow-lg z-10 overflow-hidden"
          @click.stop
        >
          <button
            v-for="option in periodOptions"
            :key="option.value"
            @click="selectPeriod(option.value)"
            class="w-full px-3 py-2 text-left text-xs text-gray-200 hover:bg-slate-700/50 transition-colors flex items-center justify-between"
            :class="{ 'bg-slate-700/30': selectedPeriod === option.value }"
          >
            <span>{{ option.label }}</span>
            <svg
              v-if="selectedPeriod === option.value"
              class="w-3.5 h-3.5 text-emerald-400"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
            </svg>
          </button>
        </div>
      </div>
    </div>
    
    <!-- Custom Date Range Picker -->
    <div v-if="selectedPeriod === 'custom'" class="mt-3">
      <DateRangePicker
        :model-value="{ start: customStartDate, end: customEndDate }"
        :max-date="new Date().toISOString().split('T')[0]"
        @apply="handleCustomRangeApply"
      />
    </div>

    <!-- Total Amount -->
    <div class="mb-3">
      <p class="text-2xl font-semibold text-white/90">{{ formattedTotal }}</p>
    </div>

    <!-- Indicators Row -->
    <div class="grid grid-cols-3 gap-3 mb-3">
      <div>
        <p class="text-[10px] text-white/40 mb-0.5">{{ t('expenses.expensesLabel') }}</p>
        <p class="text-sm font-semibold text-white/80">{{ expenseCount }}</p>
      </div>
      <div>
        <p class="text-[10px] text-white/40 mb-0.5">{{ t('expenses.avgPerDay') }}</p>
        <p class="text-sm font-semibold text-white/80">{{ formattedAverage }}</p>
      </div>
      <div>
        <p class="text-[10px] text-white/40 mb-0.5">{{ t('expenses.largest') }}</p>
        <p class="text-sm font-semibold text-white/80">{{ formattedLargest }}</p>
      </div>
    </div>

    <!-- Chart Section -->
    <div class="w-full" style="height: 80px;">
      <svg
        :width="chartWidth"
        :height="80"
        viewBox="0 0 300 80"
        preserveAspectRatio="none"
        class="w-full h-full"
      >
        <defs>
          <linearGradient :id="`expenseGradient-${componentId}`" x1="0%" y1="0%" x2="100%" y2="0%">
            <stop offset="0%" style="stop-color:#ef4444;stop-opacity:1" />
            <stop offset="100%" style="stop-color:#14b8a6;stop-opacity:1" />
          </linearGradient>
          <linearGradient :id="`expenseGradientFill-${componentId}`" x1="0%" y1="0%" x2="100%" y2="0%">
            <stop offset="0%" style="stop-color:#ef4444;stop-opacity:0.15" />
            <stop offset="100%" style="stop-color:#14b8a6;stop-opacity:0.15" />
          </linearGradient>
        </defs>
        
        <!-- Area fill -->
        <path
          :d="areaPath"
          :fill="`url(#expenseGradientFill-${componentId})`"
        />
        
        <!-- Line -->
        <path
          :d="linePath"
          fill="none"
          :stroke="`url(#expenseGradient-${componentId})`"
          stroke-width="2"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
      </svg>
    </div>
  </div>
</template>

<script setup lang="ts">
interface Expense {
  id: string
  workspaceId: string
  amount: number
  dateISO?: string
  date?: string
}

interface Props {
  expenses: Expense[]
  workspaceId?: string
  monthISO?: string
}

const props = withDefaults(defineProps<Props>(), {
  workspaceId: undefined,
  monthISO: undefined
})

const { formatCurrency } = useFormatting()

const chartWidth = 300
const chartHeight = 80

// Unique ID for gradients (to avoid conflicts if multiple instances)
const componentId = Math.random().toString(36).substring(7)

// Period options
type PeriodType = 'month' | 'week' | 'all' | 'custom'

const emit = defineEmits<{
  'period-change': [period: PeriodType, dateRange?: { start: string | null; end: string | null }]
}>()

const { t } = useI18n()

const periodOptions = computed(() => [
  { value: 'month' as PeriodType, label: t('expenses.thisMonth') },
  { value: 'week' as PeriodType, label: t('expenses.thisWeek') },
  { value: 'all' as PeriodType, label: t('expenses.allTime') },
  { value: 'custom' as PeriodType, label: t('expenses.custom') }
])

const selectedPeriod = ref<PeriodType>('month')
const showDropdown = ref(false)
const customStartDate = ref<string>('')
const customEndDate = ref<string>('')

const selectedPeriodLabel = computed(() => {
  return periodOptions.value.find(opt => opt.value === selectedPeriod.value)?.label || t('expenses.thisMonth')
})

const periodLabel = computed(() => {
  switch (selectedPeriod.value) {
    case 'month':
      return t('expenses.totalExpensesThisMonth')
    case 'week':
      return t('expenses.totalExpensesThisWeek')
    case 'all':
      return t('expenses.totalExpenses')
    case 'custom':
      return t('expenses.totalExpensesCustom')
    default:
      return t('expenses.totalExpenses')
  }
})

const selectPeriod = (period: PeriodType) => {
  selectedPeriod.value = period
  showDropdown.value = false
  
  if (period === 'custom') {
    // Initialize with current month if not set
    if (!customStartDate.value || !customEndDate.value) {
      const now = new Date()
      const start = new Date(now.getFullYear(), now.getMonth(), 1)
      const end = new Date(now.getFullYear(), now.getMonth() + 1, 0)
      customStartDate.value = start.toISOString().split('T')[0]
      customEndDate.value = end.toISOString().split('T')[0]
    }
  }
  
  // Emit period change with date range
  emit('period-change', period, {
    start: customStartDate.value,
    end: customEndDate.value
  })
}

const handleCustomRangeApply = (range: { start: string | null; end: string | null }) => {
  customStartDate.value = range.start || ''
  customEndDate.value = range.end || ''
  
  // Emit period change with custom date range
  emit('period-change', 'custom', range)
}

// Get date range based on selected period
const dateRange = computed(() => {
  const now = new Date()
  let start: Date
  let end: Date = new Date(now)

  switch (selectedPeriod.value) {
    case 'month':
      start = new Date(now.getFullYear(), now.getMonth(), 1)
      end = new Date(now.getFullYear(), now.getMonth() + 1, 0, 23, 59, 59)
      break
    case 'week':
      const dayOfWeek = now.getDay()
      const diff = now.getDate() - dayOfWeek + (dayOfWeek === 0 ? -6 : 1) // Monday
      start = new Date(now.getFullYear(), now.getMonth(), diff)
      start.setHours(0, 0, 0, 0)
      end = new Date(now)
      end.setHours(23, 59, 59, 999)
      break
    case 'all':
      // Go back 2 months from now for reasonable chart display
      start = new Date(now.getFullYear(), now.getMonth() - 2, 1)
      end = new Date(now.getFullYear(), now.getMonth() + 1, 0, 23, 59, 59, 999)
      break
    case 'custom':
      if (customStartDate.value && customEndDate.value) {
        start = new Date(customStartDate.value)
        start.setHours(0, 0, 0, 0)
        end = new Date(customEndDate.value)
        end.setHours(23, 59, 59, 999)
      } else {
        // Default to current month if not set
        start = new Date(now.getFullYear(), now.getMonth(), 1)
        end = new Date(now.getFullYear(), now.getMonth() + 1, 0, 23, 59, 59, 999)
      }
      break
    default:
      start = new Date(now.getFullYear(), now.getMonth(), 1)
      end = new Date(now.getFullYear(), now.getMonth() + 1, 0, 23, 59, 59)
  }

  return { start, end }
})

// Filter expenses by date range and workspace
const filteredExpenses = computed(() => {
  const { start, end } = dateRange.value
  
  let filtered = props.expenses.filter(expense => {
    const expenseDate = expense.dateISO || expense.date
    if (!expenseDate) return false
    
    const expenseDateObj = new Date(expenseDate)
    if (expenseDateObj < start || expenseDateObj > end) return false
    
    if (props.workspaceId && expense.workspaceId !== props.workspaceId) return false
    
    return true
  })
  
  return filtered
})

// Calculate total
const total = computed(() => {
  return filteredExpenses.value.reduce((sum, expense) => sum + expense.amount, 0)
})

// Format total amount
const formattedTotal = computed(() => {
  return formatCurrency(total.value)
})

// Expense count
const expenseCount = computed(() => {
  return filteredExpenses.value.length
})

// Average per day
const averagePerDay = computed(() => {
  const { start, end } = dateRange.value
  const daysDiff = Math.max(1, Math.ceil((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24)))
  return total.value / daysDiff
})

const formattedAverage = computed(() => {
  return formatCurrency(Math.round(averagePerDay.value))
})

// Largest expense
const largestExpense = computed(() => {
  if (filteredExpenses.value.length === 0) return 0
  return Math.max(...filteredExpenses.value.map(e => e.amount))
})

const formattedLargest = computed(() => {
  return formatCurrency(largestExpense.value)
})

// Build trend data points
const trendData = computed(() => {
  const { start, end } = dateRange.value
  const daysDiff = Math.ceil((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24)) + 1
  // Cap at 90 days for reasonable chart, but ensure minimum of 7 days
  const numDays = Math.max(7, Math.min(daysDiff, 90))
  
  // Group expenses by day and calculate cumulative sum
  const dailyTotals = new Map<number, number>()
  
  filteredExpenses.value.forEach(expense => {
    const expenseDate = expense.dateISO || expense.date
    if (!expenseDate) return
    
    const expenseDateObj = new Date(expenseDate)
    const dayIndex = Math.floor((expenseDateObj.getTime() - start.getTime()) / (1000 * 60 * 60 * 24))
    
    if (dayIndex >= 0 && dayIndex < numDays) {
      const current = dailyTotals.get(dayIndex) || 0
      dailyTotals.set(dayIndex, current + expense.amount)
    }
  })
  
  // Build cumulative sum array
  const cumulative = new Array(numDays).fill(0)
  let runningTotal = 0
  for (let day = 0; day < numDays; day++) {
    runningTotal += dailyTotals.get(day) || 0
    cumulative[day] = runningTotal
  }
  
  // Sample 14 points across the period (evenly distributed)
  const numPoints = 14
  const points: Array<{ x: number; y: number }> = []
  
  // Ensure we have at least some data points even if no expenses
  const maxCumulative = Math.max(...cumulative, total.value || 1)
  
  for (let i = 0; i < numPoints; i++) {
    const dayIndex = Math.round((i / (numPoints - 1)) * (numDays - 1))
    const value = cumulative[dayIndex] || 0
    
    // Normalize to chart coordinates
    const x = (i / (numPoints - 1)) * chartWidth
    const normalizedValue = maxCumulative > 0 ? value / maxCumulative : 0
    // Add padding: 8px top, 8px bottom
    const y = chartHeight - 8 - (normalizedValue * (chartHeight - 16))
    
    points.push({ x, y })
  }
  
  return points
})

// Generate smooth path using cubic bezier
const linePath = computed(() => {
  const points = trendData.value
  if (points.length === 0) {
    // Return flat line at bottom if no data
    return `M 0 ${chartHeight - 8} L ${chartWidth} ${chartHeight - 8}`
  }
  
  if (points.length === 1) {
    return `M ${points[0].x} ${points[0].y}`
  }
  
  let path = `M ${points[0].x} ${points[0].y}`
  
  for (let i = 0; i < points.length - 1; i++) {
    const current = points[i]
    const next = points[i + 1]
    
    // Control points for smooth curve
    const cp1x = current.x + (next.x - current.x) / 3
    const cp1y = current.y
    const cp2x = current.x + (next.x - current.x) * 2 / 3
    const cp2y = next.y
    
    path += ` C ${cp1x} ${cp1y}, ${cp2x} ${cp2y}, ${next.x} ${next.y}`
  }
  
  return path
})

// Generate area path (line + bottom line)
const areaPath = computed(() => {
  const points = trendData.value
  if (points.length === 0) {
    return `M 0 ${chartHeight - 8} L ${chartWidth} ${chartHeight - 8} Z`
  }
  
  if (points.length === 1) {
    return `M ${points[0].x} ${points[0].y} L ${points[0].x} ${chartHeight - 8} L 0 ${chartHeight - 8} Z`
  }
  
  // Build area path with same curve as line
  let path = `M ${points[0].x} ${points[0].y}`
  
  for (let i = 0; i < points.length - 1; i++) {
    const current = points[i]
    const next = points[i + 1]
    
    // Control points for smooth curve (same as line)
    const cp1x = current.x + (next.x - current.x) / 3
    const cp1y = current.y
    const cp2x = current.x + (next.x - current.x) * 2 / 3
    const cp2y = next.y
    
    path += ` C ${cp1x} ${cp1y}, ${cp2x} ${cp2y}, ${next.x} ${next.y}`
  }
  
  // Close the area by going to bottom-right, then bottom-left, then back to start
  const lastPoint = points[points.length - 1]
  path += ` L ${lastPoint.x} ${chartHeight - 8} L 0 ${chartHeight - 8} Z`
  
  return path
})

// Close dropdown when clicking outside
onMounted(() => {
  const handleClickOutside = (e: MouseEvent) => {
    const target = e.target as HTMLElement
    if (!target.closest('.relative')) {
      showDropdown.value = false
    }
  }
  document.addEventListener('click', handleClickOutside)
  onUnmounted(() => {
    document.removeEventListener('click', handleClickOutside)
  })
})
</script>

