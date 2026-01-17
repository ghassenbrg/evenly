<template>
  <BottomSheet v-model="isOpen" :title="t('dashboard.allCategories')">
    <!-- Loading State -->
    <template v-if="isLoading">
      <div class="space-y-0">
        <template v-for="i in 5" :key="i">
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
          <div v-if="i < 5" class="h-px bg-white/10 ml-14 mr-0"></div>
        </template>
      </div>
    </template>

    <!-- Content -->
    <template v-else>
      <div class="space-y-0">
        <template v-for="(item, index) in allCategories" :key="item.id || 'others'">
          <div class="flex items-center justify-between py-3">
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
                <span 
                  v-if="item.id === null" 
                  class="text-xs px-2 py-0.5 bg-slate-700/50 text-slate-300 rounded-full"
                >
                  {{ t('dashboard.remaining') }}
                </span>
                <span class="text-xs text-gray-400">
                  {{ item.expenseCount }} {{ item.expenseCount === 1 ? t('dashboard.expense') : t('dashboard.expenses') }}
                </span>
              </div>
              <div class="mt-1">
                <div class="text-xs text-gray-400">{{ item.percentage }}%</div>
              </div>
            </div>

            <!-- Right Amount -->
            <div class="text-sm font-semibold text-gray-200 flex-shrink-0">
              {{ formatCurrency(item.totalAmount) }}
            </div>
          </div>
          <!-- Separator Line (except last) -->
          <div
            v-if="index < allCategories.length - 1"
            class="h-px bg-white/10 ml-14 mr-0"
          ></div>
        </template>
      </div>

      <!-- Empty State -->
      <div v-if="allCategories.length === 0" class="text-center py-8">
        <p class="text-gray-400 text-sm">{{ t('expenses.noExpenses') }}</p>
      </div>
    </template>
  </BottomSheet>
</template>

<script setup lang="ts">
import { useWorkspacesStore } from '~/stores/workspaces'
import { useAnalytics } from '~/composables/useAnalytics'
import { useFontAwesome } from '~/composables/useFontAwesome'
import { useCategoryColor } from '~/composables/useCategoryColor'
import type { SettlementScope } from '~/types/api'

interface CategoryItem {
  id: string | null
  name: string
  iconClass?: string
  color?: string
  expenseCount: number
  totalAmount: number
  percentage: number
}

interface Props {
  modelValue: boolean
  workspaceId?: string
  startDate?: string
  endDate?: string
  settlementScope?: SettlementScope
}

const props = defineProps<Props>()
const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()

const { t } = useI18n()
const { formatCurrency } = useFormatting()
const { parseIconClass } = useFontAwesome()
const { colorToGradient } = useCategoryColor()

const workspacesStore = useWorkspacesStore()
const { activeWorkspaceId } = storeToRefs(workspacesStore)
const { expenseSnapshot, loading: analyticsLoading, fetchCategoryAnalytics } = useAnalytics()

const isLoading = computed(() => analyticsLoading.value)

const isOpen = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

// Helper to get Font Awesome icon from class string
const getFontAwesomeIcon = (iconClass: string | null | undefined) => {
  const parsed = parseIconClass(iconClass)
  if (!parsed) {
    return ['fas', 'ellipsis']
  }
  return [parsed.prefix, parsed.icon]
}

// Compute all categories with percentages
const allCategories = computed<CategoryItem[]>(() => {
  if (!expenseSnapshot.value || !expenseSnapshot.value.data.length) return []
  
  // Map all categories (including "Remaining Categories" if present - categoryId: null)
  // Use spentPercentage from API which represents the percentage of total expenses
  return expenseSnapshot.value.data.map(item => ({
    id: item.categoryId,
    name: item.categoryId === null 
      ? t('dashboard.otherCategories') 
      : (item.categoryName || t('common.unknown')),
    iconClass: item.categoryIcon || 'fa-solid fa-ellipsis',
    color: item.categoryColor || '#64748b',
    expenseCount: item.expensesCount || 0,
    totalAmount: item.totalAmount,
    percentage: item.spentPercentage || 0
  }))
})

// Watch for sheet opening to fetch data
watch(isOpen, async (newValue) => {
  if (newValue) {
    const workspaceId = props.workspaceId || activeWorkspaceId.value
    if (workspaceId) {
      // Fetch all categories (size = 0 means all)
      await fetchCategoryAnalytics(workspaceId, props.startDate, props.endDate, 0, props.settlementScope)
    }
  }
})

// Also watch for date changes when sheet is open
watch([() => props.startDate, () => props.endDate, () => props.settlementScope], async () => {
  if (isOpen.value) {
    const workspaceId = props.workspaceId || activeWorkspaceId.value
    if (workspaceId) {
      await fetchCategoryAnalytics(workspaceId, props.startDate, props.endDate, 0, props.settlementScope)
    }
  }
})
</script>
