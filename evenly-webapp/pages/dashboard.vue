<template>
  <div class="p-4 space-y-4">
    <!-- Loading State -->
    <div v-if="loading" class="flex items-center justify-center py-12">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-emerald-500"></div>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="bg-red-500/10 border border-red-500/20 rounded-xl p-4">
      <p class="text-red-400 text-sm">{{ error.message || 'Failed to load dashboard data' }}</p>
      <button
        @click="() => loadDashboard()"
        class="mt-2 text-sm text-red-400 hover:text-red-300 underline"
      >
        Retry
      </button>
    </div>

    <!-- Content -->
    <template v-else>
      <DashboardBalanceCard
        v-if="summary"
        :balance="summary.total"
        :spent="summary.sharedBudgetUsage?.spent || 0"
        :limit="summary.sharedBudgetUsage?.limit || 0"
        :is-personal="activeWorkspace?.isPersonal || false"
      />
      
      <DashboardExpenseSnapshotCard
        :items="expenseSnapshotItems"
        :others-count="othersCount"
        :others-percent="othersPercent"
        :others-color="othersColor"
      />
      
      <DashboardCategoriesBreakdownCard
        :items="categoryItems"
        :total-categories="categories.length"
      />
      
      <DashboardRecentExpensesCard
        :expenses="recentExpenses as Expense[]"
      />
    </template>
  </div>
</template>

<script setup lang="ts">
definePageMeta({
  middleware: 'auth',
  scrollToTop: false
})

import { useWorkspacesStore } from '~/stores/workspaces'
import { useAnalytics } from '~/composables/useAnalytics'
import { useCategories } from '~/composables/useCategories'
import { useFormatting } from '~/composables/useFormatting'
import type { Expense } from '~/types/api'

const { t } = useI18n()
const workspacesStore = useWorkspacesStore()
const { activeWorkspace, activeWorkspaceId } = storeToRefs(workspacesStore)
const { summary, categoryAnalytics, recentExpenses, loading, error, fetchSummary, fetchCategoryAnalytics, fetchRecentExpenses, clear } = useAnalytics()
const { categories, fetchCategories } = useCategories()
const { formatCurrency } = useFormatting()

const getDateRange = (period: 'month' | 'week' | 'all' | 'custom', customRange?: { start: string | null; end: string | null }) => {
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

const loadDashboard = async (period: 'month' | 'week' | 'all' | 'custom' = 'month', customRange?: { start: string | null; end: string | null }) => {
  if (!activeWorkspaceId.value) return
  
  const { start, end } = getDateRange(period, customRange)
  await Promise.all([
    fetchSummary(activeWorkspaceId.value, start, end),
    fetchCategoryAnalytics(activeWorkspaceId.value, start, end),
    fetchRecentExpenses(activeWorkspaceId.value, 5),
    fetchCategories(activeWorkspaceId.value)
  ])
}


const expenseSnapshotItems = computed(() => {
  if (!categoryAnalytics.value.length) return []
  
  const total = categoryAnalytics.value.reduce((sum, item) => sum + item.total, 0)
  const topItems = categoryAnalytics.value
    .slice(0, 4)
    .map(item => ({
      key: item.categoryId,
      label: item.category?.name || 'Unknown',
      percent: total > 0 ? Math.round((item.total / total) * 100) : 0,
      count: item.count,
      color: item.category?.color ? `linear-gradient(135deg, ${item.category.color} 0%, ${item.category.color} 100%)` : 'linear-gradient(135deg, #64748b 0%, #475569 100%)',
      icon: (item.category?.icon || 'others') as 'groceries' | 'rent' | 'bills' | 'internet' | 'others'
    }))
  
  return topItems
})

const othersCount = computed(() => {
  if (categoryAnalytics.value.length <= 4) return 0
  return categoryAnalytics.value.slice(4).reduce((sum, item) => sum + item.count, 0)
})

const othersPercent = computed(() => {
  if (!categoryAnalytics.value.length) return 0
  const total = categoryAnalytics.value.reduce((sum, item) => sum + item.total, 0)
  const topTotal = categoryAnalytics.value.slice(0, 4).reduce((sum, item) => sum + item.total, 0)
  return total > 0 ? Math.round(((total - topTotal) / total) * 100) : 0
})

const othersColor = 'linear-gradient(135deg, #64748b 0%, #475569 100%)'

const categoryItems = computed(() => {
  return [...categoryAnalytics.value]
    .sort((a, b) => b.total - a.total)
    .slice(0, 4)
    .map(item => ({
      id: item.categoryId,
      name: item.category?.name || 'Unknown',
      icon: (item.category?.icon || 'other') as 'groceries' | 'rent' | 'bills' | 'mobile',
      expenseCount: item.count,
      totalAmount: item.total,
      accent: getAccentFromColor(item.category?.color || '#64748b')
    }))
})

const getAccentFromColor = (color: string): 'green' | 'rose' | 'sky' | 'indigo' => {
  if (color.includes('10b981') || color.includes('059669')) return 'green'
  if (color.includes('f43f5e') || color.includes('e11d48')) return 'rose'
  if (color.includes('0ea5e9') || color.includes('0284c7')) return 'sky'
  if (color.includes('6366f1') || color.includes('4f46e5')) return 'indigo'
  return 'green'
}

watch(activeWorkspaceId, () => {
  clear()
  loadDashboard()
}, { immediate: true })

onMounted(() => {
  if (!activeWorkspace.value) {
    workspacesStore.fetchWorkspaces().then(() => {
      loadDashboard()
    })
  } else {
    loadDashboard()
  }
})
</script>
