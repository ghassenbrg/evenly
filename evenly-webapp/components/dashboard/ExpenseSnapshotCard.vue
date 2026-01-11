<template>
  <div class="rounded-2xl bg-gradient-to-br from-slate-900 to-slate-950 shadow-lg shadow-black/40 p-4 space-y-4">
    <!-- Header Row -->
    <div class="flex items-center justify-between">
      <h2 class="text-base font-semibold text-gray-100">{{ t('dashboard.expenseSnapshot') }}</h2>
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
      <div class="flex justify-between items-start gap-4">
        <!-- Left Column Skeleton -->
        <div class="flex-1 space-y-3">
          <div v-for="i in 4" :key="i" class="flex items-center gap-3">
            <div class="w-11 h-11 rounded-full bg-slate-700/50 animate-pulse"></div>
            <div class="w-px h-8 bg-slate-700/30"></div>
            <div class="flex-1 flex flex-col space-y-2">
              <div class="h-4 w-24 bg-slate-700/50 rounded animate-pulse"></div>
              <div class="h-3 w-12 bg-slate-700/30 rounded animate-pulse"></div>
            </div>
          </div>
        </div>
        <!-- Right Column Skeleton -->
        <div class="flex-shrink-0 flex flex-col items-center">
          <div class="w-44 h-44 rounded-full bg-slate-700/30 animate-pulse"></div>
          <div class="mt-3 h-4 w-16 bg-slate-700/50 rounded animate-pulse"></div>
        </div>
      </div>
    </template>

    <!-- Content -->
    <template v-else>

    <!-- Two Column Layout -->
    <div ref="containerRef" class="flex justify-between items-start gap-4">
      <!-- Left Column: Category List -->
      <div ref="leftColumnRef" class="flex-1 space-y-3">
          <div
            v-for="item in displayItems"
            :key="item.key"
            class="flex items-center gap-3"
          >
          <!-- Icon Circle with Font Awesome -->
          <div
            class="w-11 h-11 rounded-full flex items-center justify-center flex-shrink-0"
            :style="{ background: item.color }"
          >
            <FontAwesomeIcon
              :icon="getFontAwesomeIcon(item.iconClass || 'fa-solid fa-ellipsis')"
              class="w-5 h-5 text-white"
            />
          </div>

          <!-- Separator Line -->
          <div class="w-px h-8 bg-slate-700/50 flex-shrink-0"></div>

          <!-- Text Block -->
          <div class="flex-1 min-w-0">
            <div class="flex flex-col">
              <span class="text-sm font-medium text-gray-100">{{ item.label }}</span>
              <span class="text-sm font-semibold text-gray-200">{{ item.percent }}%</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Right Column: Pie Chart -->
      <div class="flex-shrink-0 flex flex-col items-center">
        <svg
          :width="responsiveChartSize"
          :height="responsiveChartSize"
          viewBox="0 0 200 200"
          class="overflow-visible"
          style="max-width: 100%; height: auto;"
        >
          <defs>
            <filter id="shadow">
              <feDropShadow dx="0" dy="2" stdDeviation="3" flood-opacity="0.2" />
            </filter>
            <linearGradient
              v-for="(gradient, index) in gradients"
              :key="`gradient-${index}`"
              :id="`gradient-${index}`"
              x1="0%"
              y1="0%"
              x2="100%"
              y2="100%"
            >
              <stop offset="0%" :style="`stop-color:${gradient.start};stop-opacity:1`" />
              <stop offset="100%" :style="`stop-color:${gradient.end};stop-opacity:1`" />
            </linearGradient>
          </defs>
          <g transform="translate(100, 100)">
            <circle
              cx="0"
              cy="0"
              r="80"
              fill="none"
              stroke="rgba(0, 0, 0, 0.2)"
              stroke-width="2"
            />
            <path
              v-for="(segment, index) in chartSegments"
              :key="index"
              :d="segment.path"
              :fill="segment.fill"
              :filter="'url(#shadow)'"
              stroke="rgba(15, 23, 42, 0.8)"
              stroke-width="3"
              stroke-linejoin="round"
            />
          </g>
        </svg>

        <!-- Others Label -->
        <div v-if="displayOthersPercent > 0" class="mt-3 flex flex-col items-center gap-2">
          <button
            type="button"
            @click="handleOpenOthers"
            class="flex items-center gap-2 transition-opacity hover:opacity-80"
          >
            <div 
              class="w-3 h-3 rounded-sm"
              :style="{ background: displayOthersColor }"
            ></div>
            <span class="text-xs font-medium text-gray-200">{{ t('dashboard.otherCategories') }} {{ displayOthersPercent }}%</span>
          </button>
        </div>
      </div>
    </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { useWorkspacesStore } from '~/stores/workspaces'
import { useAnalytics } from '~/composables/useAnalytics'
import { useFontAwesome } from '~/composables/useFontAwesome'
import { useCategoryColor } from '~/composables/useCategoryColor'

interface ExpenseItem {
  key: string
  label: string
  percent: number
  count: number
  color: string
  iconClass?: string // Font Awesome icon class from API
  icon?: 'groceries' | 'rent' | 'bills' | 'internet' | 'others' // Legacy support
}

interface Props {
  filterLabel?: string
  items?: ExpenseItem[]
  othersCount?: number
  othersPercent?: number
  othersColor?: string
  loading?: boolean
}

type PeriodType = 'month' | 'week' | 'all' | 'custom'

const props = withDefaults(defineProps<Props>(), {
  filterLabel: undefined,
  othersCount: 0,
  othersPercent: 0,
  othersColor: 'linear-gradient(135deg, #64748b 0%, #475569 100%)',
  items: () => [],
  loading: false
})

const { t } = useI18n()
const emit = defineEmits<{
  'period-change': [period: PeriodType, range?: { start: string | null; end: string | null }]
  openAllCategories: [startDate?: string, endDate?: string]
}>()

const workspacesStore = useWorkspacesStore()
const { activeWorkspaceId } = storeToRefs(workspacesStore)
// Use shared analytics composable to access data (fetched by parent dashboard page)
const { categoryAnalytics, expenseSnapshot, loading: analyticsLoading, fetchCategoryAnalytics } = useAnalytics()
const { parseIconClass } = useFontAwesome()
const { colorToGradient } = useCategoryColor()

// Combine prop loading with analytics loading
// Note: Parent dashboard page handles initial data fetching, this component only fetches on period change
const isLoading = computed(() => props.loading || analyticsLoading.value)

// Helper to get Font Awesome icon from class string
const getFontAwesomeIcon = (iconClass: string) => {
  const parsed = parseIconClass(iconClass)
  if (!parsed) {
    // Fallback to ellipsis icon
    return ['fas', 'ellipsis']
  }
  return [parsed.prefix, parsed.icon]
}

const chartSize = 180
const radius = 80
const gapAngle = 2 // degrees gap between slices
const centerX = 0
const centerY = 0

// Refs to measure container dimensions
const containerRef = ref<HTMLElement | null>(null)
const leftColumnRef = ref<HTMLElement | null>(null)
const containerWidth = ref(0)
const leftColumnWidth = ref(0)

// Responsive chart size - based on remaining space
const responsiveChartSize = computed(() => {
  if (!process.client || containerWidth.value === 0 || leftColumnWidth.value === 0) {
    return chartSize // Default size on SSR or before measurement
  }
  
  // Calculate remaining space: container width - left column width - gap (16px = gap-4)
  const gap = 16 // gap-4 = 1rem = 16px
  const remainingSpace = containerWidth.value - leftColumnWidth.value - gap
  
  // Chart size should fit in remaining space (use 95% to leave some margin)
  const minSize = 120 // Minimum chart size
  const maxSize = chartSize // Maximum chart size (180px)
  
  // Use remaining space, but clamp between min and max
  const calculatedSize = Math.max(minSize, Math.min(maxSize, remainingSpace * 0.95))
  
  return calculatedSize
})

// Update container dimensions
const updateDimensions = () => {
  if (!process.client || !containerRef.value || !leftColumnRef.value) return
  
  containerWidth.value = containerRef.value.offsetWidth
  leftColumnWidth.value = leftColumnRef.value.offsetWidth
}

// Watch for container and left column changes
watch([containerRef, leftColumnRef], () => {
  if (process.client) {
    nextTick(() => {
      updateDimensions()
    })
  }
})

const selectedPeriod = ref<PeriodType>('month')
const customRange = ref<{ start: string | null; end: string | null }>({ start: null, end: null })

const getDateRange = (period: PeriodType, customRange?: { start: string | null; end: string | null }) => {
  const now = new Date()
  let start: Date
  let end: Date = new Date(now)

  switch (period) {
    case 'month':
      start = new Date(now.getFullYear(), now.getMonth(), 1)
      end = new Date(now.getFullYear(), now.getMonth() + 1, 0, 23, 59, 59, 999)
      break
    case 'week':
      const dayOfWeek = now.getDay()
      const diff = now.getDate() - dayOfWeek + (dayOfWeek === 0 ? -6 : 1)
      start = new Date(now.getFullYear(), now.getMonth(), diff)
      start.setHours(0, 0, 0, 0)
      end = new Date(now)
      end.setHours(23, 59, 59, 999)
      break
    case 'all':
      start = new Date(now.getFullYear(), now.getMonth() - 2, 1)
      end = new Date(now.getFullYear(), now.getMonth() + 1, 0, 23, 59, 59, 999)
      break
    case 'custom':
      if (customRange?.start && customRange?.end) {
        start = new Date(customRange.start)
        start.setHours(0, 0, 0, 0)
        end = new Date(customRange.end)
        end.setHours(23, 59, 59, 999)
      } else {
        start = new Date(now.getFullYear(), now.getMonth(), 1)
        end = new Date(now.getFullYear(), now.getMonth() + 1, 0, 23, 59, 59, 999)
      }
      break
    default:
      start = new Date(now.getFullYear(), now.getMonth(), 1)
      end = new Date(now.getFullYear(), now.getMonth() + 1, 0, 23, 59, 59, 999)
  }

  return {
    start: start.toISOString().split('T')[0],
    end: end.toISOString().split('T')[0]
  }
}

const handlePeriodChange = async (period: PeriodType, range?: { start: string | null; end: string | null }) => {
  selectedPeriod.value = period
  
  // For custom period, only proceed if we have a valid range with both start and end
  if (period === 'custom') {
    if (range && range.start && range.end) {
      customRange.value = range
      // Fetch data for this card only
      if (activeWorkspaceId.value) {
        const { start, end } = getDateRange(period, range)
        await fetchCategoryAnalytics(activeWorkspaceId.value, start, end)
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
  
  // Fetch data for this card only
  if (activeWorkspaceId.value) {
    const { start, end } = getDateRange(period, range)
    await fetchCategoryAnalytics(activeWorkspaceId.value, start, end)
  }
  
  emit('period-change', period, range)
}

// Compute items from fetched expenseSnapshot - use real API data
// The API returns data sorted by totalAmount, with "Others" category (categoryId: null) at the end
const computedItems = computed(() => {
  if (!expenseSnapshot.value || !expenseSnapshot.value.data.length) return []
  
  // Filter out "Others" category (categoryId: null) from main items
  // It will be handled separately
  const mainItems = expenseSnapshot.value.data
    .filter(item => item.categoryId !== null)
    .slice(0, 4) // Take top 4 categories
    .map(item => ({
      key: item.categoryId || 'others',
      label: item.categoryName || t('common.unknown'),
      percent: item.spentPercentage, // Use spentPercentage from API
      count: item.expensesCount || 0,
      color: colorToGradient(item.categoryColor), // Use real API categoryColor
      iconClass: item.categoryIcon || 'fa-solid fa-ellipsis' // Use real API icon class
    }))
  
  return mainItems
})

// Find the "Others" category (categoryId: null) from API response
const othersItem = computed(() => {
  if (!expenseSnapshot.value || !expenseSnapshot.value.data.length) return null
  return expenseSnapshot.value.data.find(item => item.categoryId === null)
})

const computedOthersCount = computed(() => {
  if (othersItem.value) {
    return othersItem.value.expensesCount || 0
  }
  // If no "Others" item in API, calculate from remaining items
  if (!expenseSnapshot.value || expenseSnapshot.value.data.length <= 4) return 0
  const mainItems = expenseSnapshot.value.data.filter(item => item.categoryId !== null)
  if (mainItems.length <= 4) return 0
  return mainItems.slice(4).reduce((sum, item) => sum + (item.expensesCount || 0), 0)
})

const computedOthersPercent = computed(() => {
  if (othersItem.value) {
    return othersItem.value.spentPercentage // Use spentPercentage from API
  }
  // If no "Others" item in API, calculate from remaining items
  if (!expenseSnapshot.value || !expenseSnapshot.value.data.length) return 0
  const mainItems = expenseSnapshot.value.data.filter(item => item.categoryId !== null)
  if (mainItems.length <= 4) return 0
  const total = expenseSnapshot.value.data.reduce((sum, item) => sum + item.totalAmount, 0)
  const topTotal = mainItems.slice(0, 4).reduce((sum, item) => sum + item.totalAmount, 0)
  return total > 0 ? Math.round(((total - topTotal) / total) * 100) : 0
})

const computedOthersColor = computed(() => {
  if (othersItem.value && othersItem.value.categoryColor) {
    return colorToGradient(othersItem.value.categoryColor)
  }
  return props.othersColor
})

// Use computed values from API if available, otherwise fall back to props
const displayItems = computed(() => computedItems.value.length > 0 ? computedItems.value : props.items)
const displayOthersCount = computed(() => computedItems.value.length > 0 ? computedOthersCount.value : props.othersCount)
const displayOthersPercent = computed(() => computedItems.value.length > 0 ? computedOthersPercent.value : props.othersPercent)
const displayOthersColor = computed(() => computedItems.value.length > 0 ? computedOthersColor.value : props.othersColor)

// Update dimensions when data changes (items might change width)
watch([displayItems, isLoading], () => {
  if (process.client && !isLoading.value) {
    nextTick(() => {
      updateDimensions()
    })
  }
})

// Store resize observer for cleanup
let resizeObserver: ResizeObserver | null = null

// Set up resize observer
onMounted(async () => {
  // Set up ResizeObserver to watch container and left column sizes
  if (process.client) {
    // Wait for next tick to ensure DOM is ready
    await nextTick()
    updateDimensions()
    
    // Use ResizeObserver for better performance than window resize
    resizeObserver = new ResizeObserver(() => {
      updateDimensions()
    })
    
    if (containerRef.value) {
      resizeObserver.observe(containerRef.value)
    }
    if (leftColumnRef.value) {
      resizeObserver.observe(leftColumnRef.value)
    }
    
    // Fallback to window resize as well
    window.addEventListener('resize', updateDimensions)
  }
  
  // Note: Data is fetched by parent dashboard page, no need to fetch here
})

// Clean up resize observer
onUnmounted(() => {
  if (process.client) {
    if (resizeObserver) {
      resizeObserver.disconnect()
    }
    window.removeEventListener('resize', updateDimensions)
  }
})

// Watch for workspace changes - parent will handle data fetching
// Only fetch when period changes (user interaction)
watch(activeWorkspaceId, () => {
  // Parent dashboard will reload data when workspace changes
  // Only need to update dimensions if needed
  if (process.client) {
    nextTick(() => {
      updateDimensions()
    })
  }
})

const handleOpenOthers = () => {
  const { start, end } = getDateRange(selectedPeriod.value, customRange.value)
  emit('openAllCategories', start, end)
}

const gradients = computed(() => {
  // Use displayItems (from API) if available, otherwise use props.items
  const itemsToUse = displayItems.value.length > 0 ? displayItems.value : props.items
  const itemGradients = itemsToUse.map((item, index) => {
    if (item.color.startsWith('linear-gradient')) {
      const matches = item.color.match(/#[0-9a-fA-F]{6}/g)
      return {
        start: matches?.[0] || '#10b981',
        end: matches?.[1] || matches?.[0] || '#059669'
      }
    }
    return {
      start: item.color,
      end: item.color
    }
  })
  
  // Add Others gradient
  const othersColorToUse = displayOthersColor.value
  if (othersColorToUse.startsWith('linear-gradient')) {
    const matches = othersColorToUse.match(/#[0-9a-fA-F]{6}/g)
    itemGradients.push({
      start: matches?.[0] || '#64748b',
      end: matches?.[1] || matches?.[0] || '#475569'
    })
  } else {
    itemGradients.push({
      start: othersColorToUse,
      end: othersColorToUse
    })
  }
  
  return itemGradients
})

const chartSegments = computed(() => {
  let currentAngle = -90 // Start from top
  // Use actual percentages without normalization to match displayed values
  const items = [...displayItems.value]
  
  // Add Others as a segment
  const allSegments = items.map((item, index) => {
    // Calculate slice angle based on actual percentage
    const sliceAngle = (item.percent / 100) * 360
    const startAngle = currentAngle
    const endAngle = currentAngle + sliceAngle - gapAngle
    currentAngle += sliceAngle

    const startAngleRad = (startAngle * Math.PI) / 180
    const endAngleRad = (endAngle * Math.PI) / 180

    const x1 = centerX + radius * Math.cos(startAngleRad)
    const y1 = centerY + radius * Math.sin(startAngleRad)
    const x2 = centerX + radius * Math.cos(endAngleRad)
    const y2 = centerY + radius * Math.sin(endAngleRad)

    const largeArcFlag = sliceAngle > 180 ? 1 : 0

    const path = `
      M ${centerX} ${centerY}
      L ${x1} ${y1}
      A ${radius} ${radius} 0 ${largeArcFlag} 1 ${x2} ${y2}
      Z
    `

    // Use gradient if available, otherwise solid color
    const fill = item.color.startsWith('linear-gradient')
      ? `url(#gradient-${index})`
      : item.color

    return {
      path: path.trim(),
      fill,
      percent: item.percent
    }
  })
  
  // Add Others segment
  if (displayOthersPercent.value > 0) {
    const sliceAngle = (displayOthersPercent.value / 100) * 360
    const startAngle = currentAngle
    const endAngle = currentAngle + sliceAngle - gapAngle

    const startAngleRad = (startAngle * Math.PI) / 180
    const endAngleRad = (endAngle * Math.PI) / 180

    const x1 = centerX + radius * Math.cos(startAngleRad)
    const y1 = centerY + radius * Math.sin(startAngleRad)
    const x2 = centerX + radius * Math.cos(endAngleRad)
    const y2 = centerY + radius * Math.sin(endAngleRad)

    const largeArcFlag = sliceAngle > 180 ? 1 : 0

    const path = `
      M ${centerX} ${centerY}
      L ${x1} ${y1}
      A ${radius} ${radius} 0 ${largeArcFlag} 1 ${x2} ${y2}
      Z
    `

    const othersColorToUse = displayOthersColor.value
    const fill = othersColorToUse.startsWith('linear-gradient')
      ? `url(#gradient-${items.length})`
      : othersColorToUse

    allSegments.push({
      path: path.trim(),
      fill,
      percent: displayOthersPercent.value
    })
  }
  
  return allSegments
})

</script>
