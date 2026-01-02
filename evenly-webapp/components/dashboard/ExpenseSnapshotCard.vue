<template>
  <div class="rounded-2xl bg-gradient-to-br from-slate-900 to-slate-950 shadow-lg shadow-black/40 p-4 space-y-4">
    <!-- Header Row -->
    <div class="flex items-center justify-between">
      <h2 class="text-base font-semibold text-gray-100">{{ t('dashboard.expenseSnapshot') }}</h2>
      <button
        type="button"
        class="px-3 py-1.5 bg-slate-800/80 ring-1 ring-white/10 rounded-xl text-sm text-gray-200 font-medium flex items-center gap-1.5 hover:bg-slate-800 transition-colors"
      >
        <span>{{ filterLabel }}</span>
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
        </svg>
      </button>
    </div>

    <!-- Two Column Layout -->
    <div class="flex justify-between items-start gap-4">
      <!-- Left Column: Category List -->
      <div class="flex-1 space-y-3">
        <div
          v-for="item in items"
          :key="item.key"
          class="flex items-center gap-3"
        >
          <!-- Icon Circle -->
          <div
            class="w-11 h-11 rounded-full flex items-center justify-center flex-shrink-0"
            :style="{ background: item.color }"
          >
            <svg
              v-if="item.icon === 'groceries'"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
              class="w-5 h-5 text-white"
            >
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" />
            </svg>
            <svg
              v-else-if="item.icon === 'rent'"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
              class="w-5 h-5 text-white"
            >
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
            </svg>
            <svg
              v-else-if="item.icon === 'bills'"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
              class="w-5 h-5 text-white"
            >
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
            </svg>
            <svg
              v-else-if="item.icon === 'internet'"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
              class="w-5 h-5 text-white"
            >
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8.111 16.404a5.5 5.5 0 017.778 0M12 20h.01m-7.08-7.071c3.904-3.905 10.236-3.905 14.141 0M1.394 9.393c5.857-5.857 15.355-5.857 21.213 0" />
            </svg>
            <svg
              v-else
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
              class="w-5 h-5 text-white"
            >
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
            </svg>
          </div>

          <!-- Separator Line -->
          <div class="w-px h-8 bg-slate-700/50 flex-shrink-0"></div>

          <!-- Text Block -->
          <div class="flex-1 min-w-0">
            <div class="flex items-center gap-2">
              <span class="text-sm font-medium text-gray-100">{{ item.label }}</span>
              <span class="text-sm font-semibold text-gray-200">{{ item.percent }}%</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Right Column: Pie Chart -->
      <div class="flex-shrink-0 flex flex-col items-center">
        <svg
          :width="chartSize"
          :height="chartSize"
          viewBox="0 0 200 200"
          class="overflow-visible"
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
        <div class="mt-3 flex flex-col items-center gap-2">
          <div class="flex items-center gap-2">
            <div 
              class="w-3 h-3 rounded-sm"
              :style="{ background: othersColor }"
            ></div>
            <span class="text-xs font-medium text-gray-200">{{ t('dashboard.others') }} {{ othersPercent }}%</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
interface ExpenseItem {
  key: string
  label: string
  percent: number
  count: number
  color: string
  icon: 'groceries' | 'rent' | 'bills' | 'internet' | 'others'
}

interface Props {
  filterLabel?: string
  items?: ExpenseItem[]
  othersCount?: number
  othersPercent?: number
  othersColor?: string
}

const props = withDefaults(defineProps<Props>(), {
  filterLabel: 'All',
  othersCount: 2,
  othersPercent: 6,
  othersColor: 'linear-gradient(135deg, #64748b 0%, #475569 100%)',
  items: () => [
    {
      key: 'groceries',
      label: 'Groceries',
      percent: 53,
      count: 4,
      color: 'linear-gradient(135deg, #10b981 0%, #059669 100%)',
      icon: 'groceries' as const
    },
    {
      key: 'rent',
      label: 'Rent',
      percent: 18,
      count: 2,
      color: 'linear-gradient(135deg, #3b82f6 0%, #2563eb 100%)',
      icon: 'rent' as const
    },
    {
      key: 'bills',
      label: 'Bills',
      percent: 17,
      count: 3,
      color: 'linear-gradient(135deg, #f59e0b 0%, #d97706 100%)',
      icon: 'bills' as const
    },
    {
      key: 'internet',
      label: 'Internet',
      percent: 6,
      count: 1,
      color: 'linear-gradient(135deg, #ef4444 0%, #dc2626 100%)',
      icon: 'internet' as const
    }
  ]
})

const { t } = useI18n()

const chartSize = 180
const radius = 80
const gapAngle = 2 // degrees gap between slices
const centerX = 0
const centerY = 0

const gradients = computed(() => {
  const itemGradients = props.items.map((item, index) => {
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
  if (props.othersColor.startsWith('linear-gradient')) {
    const matches = props.othersColor.match(/#[0-9a-fA-F]{6}/g)
    itemGradients.push({
      start: matches?.[0] || '#64748b',
      end: matches?.[1] || matches?.[0] || '#475569'
    })
  } else {
    itemGradients.push({
      start: props.othersColor,
      end: props.othersColor
    })
  }
  
  return itemGradients
})

const chartSegments = computed(() => {
  let currentAngle = -90 // Start from top
  // Use actual percentages without normalization to match displayed values
  const items = [...props.items]
  
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
  if (props.othersPercent > 0) {
    const sliceAngle = (props.othersPercent / 100) * 360
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

    const fill = props.othersColor.startsWith('linear-gradient')
      ? `url(#gradient-${items.length})`
      : props.othersColor

    allSegments.push({
      path: path.trim(),
      fill,
      percent: props.othersPercent
    })
  }
  
  return allSegments
})
</script>

