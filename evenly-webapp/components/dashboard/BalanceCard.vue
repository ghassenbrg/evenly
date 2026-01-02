<template>
  <div class="balance-card-bg rounded-2xl p-4 space-y-4 relative overflow-hidden">
    <!-- Total Balance Section -->
    <div class="flex items-center justify-between">
      <div class="flex flex-col">
        <span class="text-sm text-gray-400 mb-1">{{ t('dashboard.totalBalance') }}</span>
        <span :class="getBalanceColorClass" class="text-3xl font-semibold">
          {{ formatCurrency(balance) }}
        </span>
      </div>
      <img
        src="/images/wallet.png"
        :alt="t('dashboard.totalBalance')"
        class="w-30 h-20 object-contain"
      />
    </div>

    <!-- Budget Section -->
    <div class="space-y-2">
      <div>
        <span class="text-sm text-gray-300">{{ t('dashboard.budget') }}</span>
      </div>

      <!-- Progress Bar -->
      <div class="w-full h-2.5 bg-slate-800 rounded-full overflow-hidden">
        <div
          class="h-full bg-emerald-500 rounded-full transition-all duration-300"
          :style="{ width: `${progressPercentage}%` }"
        ></div>
      </div>

      <!-- Budget Text -->
      <div class="text-right">
        <span class="text-xs text-gray-400">
          {{ formatCurrency(spent) }} / {{ formatCurrency(limit) }}
        </span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
interface Props {
  balance: number
  spent: number
  limit: number
  isPersonal?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  isPersonal: false
})

const { formatCurrency } = useFormatting()
const { t } = useI18n()

const progressPercentage = computed(() => {
  if (props.limit <= 0) return 0
  return Math.min((props.spent / props.limit) * 100, 100)
})

const getBalanceColorClass = computed(() => {
  if (props.balance < 0) {
    return 'text-rose-400'
  }
  if (!props.isPersonal && props.balance > 0) {
    return 'text-emerald-400'
  }
  return 'text-white'
})
</script>

