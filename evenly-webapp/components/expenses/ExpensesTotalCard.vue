<template>
  <div class="balance-card-bg rounded-2xl p-3 relative overflow-visible">
    <!-- Header with Dropdown -->
    <div class="flex items-center justify-between mb-2">
      <p class="text-xs text-white/60 font-medium">{{ periodLabel }}</p>
      <PeriodDropdown
        :model-value="selectedPeriod"
        :range="customRange"
        :settlement-scope="settlementScope"
        @update:model-value="(value) => emit('update:modelValue', value)"
        @update:range="(value) => emit('update:range', value)"
        @update:settlement-scope="(value) => emit('update:settlementScope', value)"
        @period-change="handlePeriodChange"
        @settlement-change="handleSettlementChange"
      />
    </div>
    
    <!-- Loading State -->
    <template v-if="summaryLoading && !summary">
      <Skeleton variant="expenses-total-card" />
    </template>
    
    <!-- Content -->
    <template v-else>
      <!-- Total Amount -->
      <div class="mb-3">
        <p class="text-3xl font-bold text-white">{{ formattedTotal }}</p>
      </div>

    <!-- Indicators Row -->
    <div class="grid grid-cols-3 gap-3 mb-3">
      <div>
        <p class="text-xs text-white/50 mb-0.5 font-medium">{{ t('expenses.expensesLabel') }}</p>
        <p class="text-base font-semibold text-white">{{ expenseCount }}</p>
      </div>
      <div>
        <p class="text-xs text-white/50 mb-0.5 font-medium">{{ t('expenses.avgPerDay') }}</p>
        <p class="text-base font-semibold text-white">{{ formattedAverage }}</p>
      </div>
      <div>
        <p class="text-xs text-white/50 mb-0.5 font-medium">{{ t('expenses.largest') }}</p>
        <p class="text-base font-semibold text-white">{{ formattedLargest }}</p>
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
            <stop offset="0%" style="stop-color:#34d399;stop-opacity:1" />
            <stop offset="50%" style="stop-color:#10b981;stop-opacity:1" />
            <stop offset="100%" style="stop-color:#06b6d4;stop-opacity:1" />
          </linearGradient>
          <linearGradient :id="`expenseGradientFill-${componentId}`" x1="0%" y1="0%" x2="100%" y2="0%">
            <stop offset="0%" style="stop-color:#34d399;stop-opacity:0.25" />
            <stop offset="50%" style="stop-color:#10b981;stop-opacity:0.25" />
            <stop offset="100%" style="stop-color:#06b6d4;stop-opacity:0.25" />
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
          stroke-width="2.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
      </svg>
    </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { endOfLocalDay, startOfLocalDay } from '~/utils/date'
import PeriodDropdown from '~/components/PeriodDropdown.vue'
import Skeleton from '~/components/Skeleton.vue'

import type { ExpenseSummary, SettlementScope } from '~/types/api'

type PeriodType = 'month' | 'week' | 'all' | 'custom'

interface Props {
  summary: ExpenseSummary | null
  summaryLoading?: boolean
  workspaceId?: string
  monthISO?: string
  modelValue?: PeriodType
  range?: { start: string | null; end: string | null }
  settlementScope?: SettlementScope
}

const props = withDefaults(defineProps<Props>(), {
  summary: null,
  summaryLoading: false,
  workspaceId: undefined,
  monthISO: undefined,
  modelValue: 'month',
  range: () => ({ start: null, end: null }),
  settlementScope: 'ALL'
})

const { formatCurrency } = useFormatting()

const chartWidth = 300
const chartHeight = 80

// Unique ID for gradients (to avoid conflicts if multiple instances)
const componentId = Math.random().toString(36).substring(7)

const emit = defineEmits<{
  'update:modelValue': [value: PeriodType]
  'update:range': [{ start: string | null; end: string | null }]
  'update:settlementScope': [value: SettlementScope]
  'period-change': [period: PeriodType, dateRange?: { start: string | null; end: string | null }]
  'settlement-change': [scope: SettlementScope]
}>()

const { t } = useI18n()

// Sync with parent's period state
const selectedPeriod = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const customRange = computed({
  get: () => props.range,
  set: (value) => emit('update:range', value)
})

const settlementScope = computed(() => props.settlementScope)

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
  // Update parent state via emit
  emit('update:modelValue', period)

  if (period === 'custom') {
    // For custom period, only emit if we have a valid range with both start and end
    if (range && range.start && range.end) {
      emit('update:range', { start: range.start, end: range.end })
      emit('period-change', period, { start: range.start, end: range.end })
    }
    // If no valid range, don't emit - wait for user to select dates via applyCustomRange
    return
  }

  // For non-custom periods, emit immediately
  if (range) {
    emit('update:range', range)
  }
  emit('period-change', period, range)
}

const handleSettlementChange = (scope: SettlementScope) => {
  emit('update:settlementScope', scope)
  emit('settlement-change', scope)
}

// Get date range based on selected period
const dateRange = computed(() => {
  const now = new Date()
  let start: Date
  let end: Date = endOfLocalDay(now)

  switch (selectedPeriod.value) {
    case 'month':
      start = startOfLocalDay(new Date(now.getFullYear(), now.getMonth(), 1))
      end = endOfLocalDay(new Date(now.getFullYear(), now.getMonth() + 1, 0))
      break
    case 'week':
      const dayOfWeek = now.getDay()
      const diff = now.getDate() - dayOfWeek + (dayOfWeek === 0 ? -6 : 1) // Monday
      start = startOfLocalDay(new Date(now.getFullYear(), now.getMonth(), diff))
      end = endOfLocalDay(now)
      break
    case 'all':
      // Go back 2 months from now for reasonable chart display
      start = startOfLocalDay(new Date(now.getFullYear(), now.getMonth() - 2, 1))
      end = endOfLocalDay(new Date(now.getFullYear(), now.getMonth() + 1, 0))
      break
    case 'custom':
      if (customRange.value.start && customRange.value.end) {
        start = startOfLocalDay(customRange.value.start)
        end = endOfLocalDay(customRange.value.end)
      } else {
        // Default to current month if not set
        start = startOfLocalDay(new Date(now.getFullYear(), now.getMonth(), 1))
        end = endOfLocalDay(new Date(now.getFullYear(), now.getMonth() + 1, 0))
      }
      break
    default:
      start = startOfLocalDay(new Date(now.getFullYear(), now.getMonth(), 1))
      end = endOfLocalDay(new Date(now.getFullYear(), now.getMonth() + 1, 0))
  }

  return { start, end }
})

// Use summary data from API
const total = computed(() => {
  return props.summary?.totalAmount || 0
})

// Format total amount
const formattedTotal = computed(() => {
  return formatCurrency(total.value, props.summary?.currency)
})

// Expense count from API
const expenseCount = computed(() => {
  return props.summary?.expensesCount || 0
})

// Average per day from API
const formattedAverage = computed(() => {
  return formatCurrency(props.summary?.averagePerDay || 0, props.summary?.currency)
})

// Largest expense from API
const formattedLargest = computed(() => {
  return formatCurrency(props.summary?.largestExpenseAmount || 0, props.summary?.currency)
})

// Build trend data points from API linearChartData
const trendData = computed(() => {
  if (!props.summary?.linearChartData || props.summary.linearChartData.length === 0) {
    return []
  }
  
  const chartData = props.summary.linearChartData
  const numPoints = chartData.length
  const maxAmount = Math.max(...chartData.map(d => d.amount), total.value || 1)
  
  const points: Array<{ x: number; y: number }> = []
  
  for (let i = 0; i < numPoints; i++) {
    const value = chartData[i].amount || 0
    
    // Normalize to chart coordinates
    // Handle edge case when there's only one point
    const x = numPoints === 1 ? chartWidth / 2 : (i / (numPoints - 1)) * chartWidth
    const normalizedValue = maxAmount > 0 ? value / maxAmount : 0
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
