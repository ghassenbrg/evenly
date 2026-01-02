<template>
  <div class="p-4 space-y-4">
    <DashboardBalanceCard
      :balance="-38500"
      :spent="88100"
      :limit="200000"
    />
    <button
      type="button"
      class="btn btn-green w-full py-3"
    >
      {{ t('dashboard.setUp') }}
    </button>
    <DashboardExpenseSnapshotCard />
    <DashboardCategoriesBreakdownCard />
  </div>
</template>

<script setup lang="ts">
definePageMeta({
  middleware: 'auth'
})

const { t } = useI18n()
</script>

