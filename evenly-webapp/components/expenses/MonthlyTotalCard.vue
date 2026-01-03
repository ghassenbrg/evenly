<template>
  <div class="balance-card-bg rounded-2xl p-3 relative overflow-visible">
    <!-- Header with Dropdown -->
    <div class="flex items-center justify-between mb-2">
      <p class="text-xs text-white/50">{{ periodLabel }}</p>
      <PeriodDropdown
        v-model="selectedPeriod"
        v-model:range="customRange"
        @period-change="handlePeriodChange"
      />
    </div>
    
    <!-- Total Amount -->
    <div class="mb-3">
      <p class="text-3xl font-semibold text-white/90">{{ formattedTotal }}</p>
    </div>

    <!-- Indicators Row -->
    <div class="grid grid-cols-3 gap-3 mb-3">
      <div>
        <p class="text-xs text-white/40 mb-0.5">{{ t('expenses.expensesLabel') }}</p>
        <p class="text-base font-semibold text-white/80">{{ expenseCount }}</p>
      </div>
      <div>
        <p class="text-xs text-white/40 mb-0.5">{{ t('expenses.avgPerDay') }}</p>
        <p class="text-base font-semibold text-white/80">{{ formattedAverage }}</p>
      </div>
      <div>
        <p class="text-xs text-white/40 mb-0.5">{{ t('expenses.largest') }}</p>
        <p class="text-base font-semibold text-white/80">{{ formattedLargest }}</p>
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
import PeriodDropdown from '~/components/PeriodDropdown.vue'

interface Expense {
  id: string
  workspaceId: string
  amount: number
  dateISO?: string
  date?: string
}

type PeriodType = 'month' | 'week' | 'all' | 'custom'

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

const emit = defineEmits<{
  'period-change': [period: PeriodType, dateRange?: { start: string | null; end: string | null }]
}>()

const { t } = useI18n()

const selectedPeriod = ref<PeriodType>('month')
const customRange = ref<{ start: string | null; end: string | null }>({ start: null, end: null })

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

const handlePeriodChange = (period: PeriodType, range?: { start: string | null; end: string | null }) => {
  selectedPeriod.value = period

  if (period === 'custom') {
    const existing = range || customRange.value
    let start = existing.start
    let end = existing.end
    if (!start || !end) {
      const now = new Date()
      const defaultStart = new Date(now.getFullYear(), now.getMonth(), 1)
      const defaultEnd = new Date(now.getFullYear(), now.getMonth() + 1, 0)
      start = defaultStart.toISOString().split('T')[0]
      end = defaultEnd.toISOString().split('T')[0]
    }
    customRange.value = { start, end }
    emit('period-change', period, { start, end })
    return
  }

  if (range) {
    customRange.value = range
  }
  emit('period-change', period, range)
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
      if (customRange.value.start && customRange.value.end) {
        start = new Date(customRange.value.start)
        start.setHours(0, 0, 0, 0)
        end = new Date(customRange.value.end)
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

</script>
