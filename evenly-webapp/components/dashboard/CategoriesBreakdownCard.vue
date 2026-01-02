<template>
  <div class="rounded-2xl bg-gradient-to-br from-slate-900 to-slate-950 shadow-lg shadow-black/40 p-4 space-y-4">
    <!-- Header Row -->
    <div class="flex items-center justify-between">
      <h2 class="text-base font-semibold text-gray-100">{{ t('dashboard.categoriesBreakdown') }}</h2>
      <button
        type="button"
        @click="emit('selectFilter')"
        class="px-3 py-1.5 bg-slate-800/80 ring-1 ring-white/10 rounded-xl text-sm text-gray-200 font-medium flex items-center gap-1.5 hover:bg-slate-800 transition-colors"
      >
        <span>{{ filterLabel }}</span>
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
        </svg>
      </button>
    </div>

    <!-- Category List -->
    <div class="space-y-0">
      <template v-for="(item, index) in items" :key="item.id">
        <button
          type="button"
          @click="emit('openCategory', item.id)"
          class="w-full flex items-center justify-between py-3 transition-colors hover:bg-white/5 rounded-lg px-1 -mx-1 relative"
        >
          <!-- Left Icon -->
          <div
            class="w-11 h-11 rounded-full flex items-center justify-center flex-shrink-0 shadow-lg"
            :style="{ background: getCategoryGradient(item.accent) }"
          >
            <svg
              v-if="item.icon === 'groceries'"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
              class="w-5 h-5 text-white/80"
            >
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" />
            </svg>
            <svg
              v-else-if="item.icon === 'rent'"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
              class="w-5 h-5 text-white/80"
            >
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
            </svg>
            <svg
              v-else-if="item.icon === 'bills'"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
              class="w-5 h-5 text-white/80"
            >
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
            </svg>
            <svg
              v-else-if="item.icon === 'mobile'"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
              class="w-5 h-5 text-white/80"
            >
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 18h.01M8 21h8a2 2 0 002-2V5a2 2 0 00-2-2H8a2 2 0 00-2 2v14a2 2 0 002 2z" />
            </svg>
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
          v-if="index < items.length - 1"
          class="h-px bg-white/10 ml-14 mr-0"
        ></div>
      </template>
    </div>

    <!-- Bottom CTA -->
    <button
      type="button"
      @click="emit('openAllCategories')"
      class="w-full py-3 px-4 bg-slate-800/80 hover:bg-slate-800 rounded-xl text-gray-200 font-medium flex items-center justify-center gap-2 transition-colors ring-1 ring-white/10"
    >
      <span>{{ t('dashboard.allCategories') }} ({{ totalCategories }})</span>
      <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
      </svg>
    </button>
  </div>
</template>

<script setup lang="ts">
interface CategoryItem {
  id: string
  name: string
  icon: 'groceries' | 'rent' | 'bills' | 'mobile'
  expenseCount: number
  totalAmount: number
  accent: 'green' | 'rose' | 'sky' | 'indigo'
}

interface Props {
  filterLabel?: string
  items?: CategoryItem[]
  totalCategories?: number
}

const props = withDefaults(defineProps<Props>(), {
  filterLabel: 'ALL',
  totalCategories: 12,
  items: () => [
    {
      id: 'groceries',
      name: 'Groceries',
      icon: 'groceries' as const,
      expenseCount: 4,
      totalAmount: 100000,
      accent: 'green' as const
    },
    {
      id: 'rent',
      name: 'Rent',
      icon: 'rent' as const,
      expenseCount: 3,
      totalAmount: 260000,
      accent: 'rose' as const
    },
    {
      id: 'bills',
      name: 'Bills',
      icon: 'bills' as const,
      expenseCount: 1,
      totalAmount: 32000,
      accent: 'sky' as const
    },
    {
      id: 'mobile',
      name: 'Mobile',
      icon: 'mobile' as const,
      expenseCount: 1,
      totalAmount: 6280,
      accent: 'indigo' as const
    }
  ]
})

const emit = defineEmits<{
  selectFilter: []
  openAllCategories: []
  openCategory: [id: string]
}>()

const { t } = useI18n()
const { formatCurrency } = useFormatting()

const getCategoryGradient = (accent: string): string => {
  const gradients: Record<string, string> = {
    green: 'linear-gradient(135deg, #10b981 0%, #059669 100%)',
    rose: 'linear-gradient(135deg, #f43f5e 0%, #e11d48 100%)',
    sky: 'linear-gradient(135deg, #0ea5e9 0%, #0284c7 100%)',
    indigo: 'linear-gradient(135deg, #6366f1 0%, #4f46e5 100%)'
  }
  return gradients[accent] || gradients.green
}

</script>

