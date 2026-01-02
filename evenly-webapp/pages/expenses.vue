<template>
  <div class="p-4 space-y-4">
    <ExpensesMonthlyTotalCard :expenses="demoExpenses" />
    
    <div class="flex flex-col items-center justify-center py-12 text-center">
      <p class="text-slate-400">{{ t('placeholders.expenses') }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({
  middleware: 'auth'
})

const { t } = useI18n()

// Demo data - replace with actual expenses from API/store
// Generate data for last 2 months covering every week
const generateDemoExpenses = () => {
  const expenses: Array<{ id: string; workspaceId: string; amount: number; dateISO: string }> = []
  const now = new Date()
  let id = 1
  
  // Generate data for current month
  const currentMonth = now.getMonth()
  const currentYear = now.getFullYear()
  const currentDate = now.getDate()
  
  // Current month - Week 1 (days 1-7)
  if (currentDate >= 1) {
    expenses.push(
      { id: String(id++), workspaceId: 'ws1', amount: 45000, dateISO: `${currentYear}-${String(currentMonth + 1).padStart(2, '0')}-02` },
      { id: String(id++), workspaceId: 'ws1', amount: 32000, dateISO: `${currentYear}-${String(currentMonth + 1).padStart(2, '0')}-04` },
      { id: String(id++), workspaceId: 'ws1', amount: 28000, dateISO: `${currentYear}-${String(currentMonth + 1).padStart(2, '0')}-06` }
    )
  }
  
  // Current month - Week 2 (days 8-14)
  if (currentDate >= 8) {
    expenses.push(
      { id: String(id++), workspaceId: 'ws1', amount: 38000, dateISO: `${currentYear}-${String(currentMonth + 1).padStart(2, '0')}-09` },
      { id: String(id++), workspaceId: 'ws1', amount: 42000, dateISO: `${currentYear}-${String(currentMonth + 1).padStart(2, '0')}-11` },
      { id: String(id++), workspaceId: 'ws1', amount: 35000, dateISO: `${currentYear}-${String(currentMonth + 1).padStart(2, '0')}-13` }
    )
  }
  
  // Current month - Week 3 (days 15-21)
  if (currentDate >= 15) {
    expenses.push(
      { id: String(id++), workspaceId: 'ws1', amount: 55000, dateISO: `${currentYear}-${String(currentMonth + 1).padStart(2, '0')}-16` },
      { id: String(id++), workspaceId: 'ws1', amount: 48000, dateISO: `${currentYear}-${String(currentMonth + 1).padStart(2, '0')}-18` },
      { id: String(id++), workspaceId: 'ws1', amount: 31000, dateISO: `${currentYear}-${String(currentMonth + 1).padStart(2, '0')}-20` }
    )
  }
  
  // Current month - Week 4 (days 22-28)
  if (currentDate >= 22) {
    expenses.push(
      { id: String(id++), workspaceId: 'ws1', amount: 47000, dateISO: `${currentYear}-${String(currentMonth + 1).padStart(2, '0')}-23` },
      { id: String(id++), workspaceId: 'ws1', amount: 39000, dateISO: `${currentYear}-${String(currentMonth + 1).padStart(2, '0')}-25` },
      { id: String(id++), workspaceId: 'ws1', amount: 31280, dateISO: `${currentYear}-${String(currentMonth + 1).padStart(2, '0')}-27` }
    )
  }
  
  // Current month - Week 5 (days 29-31)
  if (currentDate >= 29) {
    expenses.push(
      { id: String(id++), workspaceId: 'ws1', amount: 25000, dateISO: `${currentYear}-${String(currentMonth + 1).padStart(2, '0')}-29` },
      { id: String(id++), workspaceId: 'ws1', amount: 18000, dateISO: `${currentYear}-${String(currentMonth + 1).padStart(2, '0')}-30` }
    )
  }
  
  // Previous month (1 month ago)
  const prevMonth = currentMonth === 0 ? 11 : currentMonth - 1
  const prevYear = currentMonth === 0 ? currentYear - 1 : currentYear
  
  // Previous month - Week 1
  expenses.push(
    { id: String(id++), workspaceId: 'ws1', amount: 52000, dateISO: `${prevYear}-${String(prevMonth + 1).padStart(2, '0')}-02` },
    { id: String(id++), workspaceId: 'ws1', amount: 38000, dateISO: `${prevYear}-${String(prevMonth + 1).padStart(2, '0')}-04` },
    { id: String(id++), workspaceId: 'ws1', amount: 29000, dateISO: `${prevYear}-${String(prevMonth + 1).padStart(2, '0')}-06` }
  )
  
  // Previous month - Week 2
  expenses.push(
    { id: String(id++), workspaceId: 'ws1', amount: 41000, dateISO: `${prevYear}-${String(prevMonth + 1).padStart(2, '0')}-09` },
    { id: String(id++), workspaceId: 'ws1', amount: 44000, dateISO: `${prevYear}-${String(prevMonth + 1).padStart(2, '0')}-11` },
    { id: String(id++), workspaceId: 'ws1', amount: 33000, dateISO: `${prevYear}-${String(prevMonth + 1).padStart(2, '0')}-13` }
  )
  
  // Previous month - Week 3
  expenses.push(
    { id: String(id++), workspaceId: 'ws1', amount: 49000, dateISO: `${prevYear}-${String(prevMonth + 1).padStart(2, '0')}-16` },
    { id: String(id++), workspaceId: 'ws1', amount: 36000, dateISO: `${prevYear}-${String(prevMonth + 1).padStart(2, '0')}-18` },
    { id: String(id++), workspaceId: 'ws1', amount: 27000, dateISO: `${prevYear}-${String(prevMonth + 1).padStart(2, '0')}-20` }
  )
  
  // Previous month - Week 4
  expenses.push(
    { id: String(id++), workspaceId: 'ws1', amount: 56000, dateISO: `${prevYear}-${String(prevMonth + 1).padStart(2, '0')}-23` },
    { id: String(id++), workspaceId: 'ws1', amount: 43000, dateISO: `${prevYear}-${String(prevMonth + 1).padStart(2, '0')}-25` },
    { id: String(id++), workspaceId: 'ws1', amount: 34000, dateISO: `${prevYear}-${String(prevMonth + 1).padStart(2, '0')}-27` }
  )
  
  // Previous month - Week 5
  expenses.push(
    { id: String(id++), workspaceId: 'ws1', amount: 51000, dateISO: `${prevYear}-${String(prevMonth + 1).padStart(2, '0')}-29` },
    { id: String(id++), workspaceId: 'ws1', amount: 22000, dateISO: `${prevYear}-${String(prevMonth + 1).padStart(2, '0')}-30` }
  )
  
  // 2 months ago
  const twoMonthsAgo = prevMonth === 0 ? 11 : prevMonth - 1
  const twoMonthsAgoYear = prevMonth === 0 ? prevYear - 1 : prevYear
  
  // 2 months ago - Week 1
  expenses.push(
    { id: String(id++), workspaceId: 'ws1', amount: 47000, dateISO: `${twoMonthsAgoYear}-${String(twoMonthsAgo + 1).padStart(2, '0')}-02` },
    { id: String(id++), workspaceId: 'ws1', amount: 39000, dateISO: `${twoMonthsAgoYear}-${String(twoMonthsAgo + 1).padStart(2, '0')}-04` },
    { id: String(id++), workspaceId: 'ws1', amount: 31000, dateISO: `${twoMonthsAgoYear}-${String(twoMonthsAgo + 1).padStart(2, '0')}-06` }
  )
  
  // 2 months ago - Week 2
  expenses.push(
    { id: String(id++), workspaceId: 'ws1', amount: 42000, dateISO: `${twoMonthsAgoYear}-${String(twoMonthsAgo + 1).padStart(2, '0')}-09` },
    { id: String(id++), workspaceId: 'ws1', amount: 45000, dateISO: `${twoMonthsAgoYear}-${String(twoMonthsAgo + 1).padStart(2, '0')}-11` },
    { id: String(id++), workspaceId: 'ws1', amount: 36000, dateISO: `${twoMonthsAgoYear}-${String(twoMonthsAgo + 1).padStart(2, '0')}-13` }
  )
  
  // 2 months ago - Week 3
  expenses.push(
    { id: String(id++), workspaceId: 'ws1', amount: 51000, dateISO: `${twoMonthsAgoYear}-${String(twoMonthsAgo + 1).padStart(2, '0')}-16` },
    { id: String(id++), workspaceId: 'ws1', amount: 38000, dateISO: `${twoMonthsAgoYear}-${String(twoMonthsAgo + 1).padStart(2, '0')}-18` },
    { id: String(id++), workspaceId: 'ws1', amount: 29000, dateISO: `${twoMonthsAgoYear}-${String(twoMonthsAgo + 1).padStart(2, '0')}-20` }
  )
  
  // 2 months ago - Week 4
  expenses.push(
    { id: String(id++), workspaceId: 'ws1', amount: 54000, dateISO: `${twoMonthsAgoYear}-${String(twoMonthsAgo + 1).padStart(2, '0')}-23` },
    { id: String(id++), workspaceId: 'ws1', amount: 41000, dateISO: `${twoMonthsAgoYear}-${String(twoMonthsAgo + 1).padStart(2, '0')}-25` },
    { id: String(id++), workspaceId: 'ws1', amount: 33000, dateISO: `${twoMonthsAgoYear}-${String(twoMonthsAgo + 1).padStart(2, '0')}-27` }
  )
  
  // 2 months ago - Week 5
  expenses.push(
    { id: String(id++), workspaceId: 'ws1', amount: 48000, dateISO: `${twoMonthsAgoYear}-${String(twoMonthsAgo + 1).padStart(2, '0')}-29` },
    { id: String(id++), workspaceId: 'ws1', amount: 24000, dateISO: `${twoMonthsAgoYear}-${String(twoMonthsAgo + 1).padStart(2, '0')}-30` }
  )
  
  return expenses
}

const demoExpenses = generateDemoExpenses()
</script>
