<template>
  <div class="p-4 space-y-4">
    <ExpensesMonthlyTotalCard 
      :expenses="demoExpenses" 
      @period-change="handlePeriodChange"
    />
    
    <!-- Expenses List by Day -->
    <div class="space-y-6">
      <template v-for="(group, groupIndex) in displayedGroups" :key="group.date">
        <!-- Day Header -->
        <div class="mb-1">
          <h3 class="text-xs font-medium text-white/50">{{ group.label }}</h3>
        </div>
        
        <!-- Expenses for this day -->
        <div class="space-y-2">
          <template v-for="(expense, expenseIndex) in group.expenses" :key="expense.id">
            <ExpensesExpenseItem
              :expense="expense"
              @click="handleExpenseClick"
            />
          </template>
        </div>
      </template>
    </div>
      
      <!-- Load More Trigger (for infinite scroll) -->
      <div
        v-if="hasMore"
        ref="loadMoreTrigger"
        class="flex justify-center pt-4 pb-4"
      >
        <div class="text-sm text-white/40">{{ t('expenses.loadingMore') }}</div>
      </div>
      
      <!-- End of List -->
      <div v-else-if="displayedGroups.length > 0" class="text-center py-8">
        <p class="text-sm text-white/40">{{ t('common.noMoreItems') }}</p>
      </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({
  middleware: 'auth'
})

const { t, locale } = useI18n()

// Demo data - replace with actual expenses from API/store
// Generate data for last 2 months covering every week
const generateDemoExpenses = (): Array<{
  id: string
  workspaceId: string
  category: 'groceries' | 'dining' | 'transportation' | 'rent' | 'bills' | 'internet' | 'mobile' | 'other'
  title: string
  dateISO: string
  paidBy: string
  amount: number
  note?: string
}> => {
  const expenses: Array<{
    id: string
    workspaceId: string
    category: 'groceries' | 'dining' | 'transportation' | 'rent' | 'bills' | 'internet' | 'mobile' | 'other'
    title: string
    dateISO: string
    paidBy: string
    amount: number
    note?: string
  }> = []
  
  const categories: Array<'groceries' | 'dining' | 'transportation' | 'rent' | 'bills' | 'internet' | 'mobile' | 'other'> = [
    'groceries', 'dining', 'transportation', 'rent', 'bills', 'internet', 'mobile', 'other'
  ]
  const titles: Record<string, string> = {
    groceries: t('expenses.category.groceries'),
    dining: t('expenses.category.dining'),
    transportation: t('expenses.category.transportation'),
    rent: t('expenses.category.rent'),
    bills: t('expenses.category.bills'),
    internet: t('expenses.category.internet'),
    mobile: t('expenses.category.mobile'),
    other: t('expenses.category.other')
  }
  const paidByOptions = ['Ghassen', 'Kana']
  const now = new Date()
  let id = 1
  
  // Generate data for current month
  const currentMonth = now.getMonth()
  const currentYear = now.getFullYear()
  const currentDate = now.getDate()
  
  // Helper function to create expense
  const createExpense = (
    amount: number,
    dateISO: string,
    category: typeof categories[number] = categories[Math.floor(Math.random() * categories.length)],
    paidBy: string = paidByOptions[Math.floor(Math.random() * paidByOptions.length)]
  ) => ({
    id: String(id++),
    workspaceId: 'ws1',
    category,
    title: titles[category],
    dateISO,
    paidBy,
    amount,
    note: Math.random() > 0.7 ? t('expenses.monthlyPayment') : undefined
  })
  
  // Current month - Week 1 (days 1-7) - Multiple expenses on same days
  if (currentDate >= 1) {
    const day2 = `${currentYear}-${String(currentMonth + 1).padStart(2, '0')}-02`
    expenses.push(
      createExpense(45000, day2, 'groceries', 'Kana'),
      createExpense(12000, day2, 'dining', 'Ghassen'), // Same day
      createExpense(32000, `${currentYear}-${String(currentMonth + 1).padStart(2, '0')}-04`, 'dining'),
      createExpense(28000, `${currentYear}-${String(currentMonth + 1).padStart(2, '0')}-06`, 'transportation'),
      createExpense(15000, `${currentYear}-${String(currentMonth + 1).padStart(2, '0')}-06`, 'bills', 'Kana') // Same day
    )
  }
  
  // Current month - Week 2 (days 8-14) - Multiple expenses on same days
  if (currentDate >= 8) {
    const day9 = `${currentYear}-${String(currentMonth + 1).padStart(2, '0')}-09`
    expenses.push(
      createExpense(38000, day9, 'bills', 'Ghassen'),
      createExpense(22000, day9, 'groceries', 'Kana'), // Same day
      createExpense(42000, `${currentYear}-${String(currentMonth + 1).padStart(2, '0')}-11`, 'groceries'),
      createExpense(35000, `${currentYear}-${String(currentMonth + 1).padStart(2, '0')}-13`, 'dining'),
      createExpense(18000, `${currentYear}-${String(currentMonth + 1).padStart(2, '0')}-13`, 'transportation', 'Ghassen') // Same day
    )
  }
  
  // Current month - Week 3 (days 15-21) - Multiple expenses on same days
  if (currentDate >= 15) {
    const day16 = `${currentYear}-${String(currentMonth + 1).padStart(2, '0')}-16`
    expenses.push(
      createExpense(55000, day16, 'rent', 'Ghassen'),
      createExpense(25000, day16, 'bills', 'Kana'), // Same day
      createExpense(48000, `${currentYear}-${String(currentMonth + 1).padStart(2, '0')}-18`, 'internet'),
      createExpense(31000, `${currentYear}-${String(currentMonth + 1).padStart(2, '0')}-20`, 'mobile'),
      createExpense(19000, `${currentYear}-${String(currentMonth + 1).padStart(2, '0')}-20`, 'groceries', 'Kana') // Same day
    )
  }
  
  // Current month - Week 4 (days 22-28) - Multiple expenses on same days
  if (currentDate >= 22) {
    const day23 = `${currentYear}-${String(currentMonth + 1).padStart(2, '0')}-23`
    expenses.push(
      createExpense(47000, day23, 'groceries', 'Kana'),
      createExpense(21000, day23, 'dining', 'Ghassen'), // Same day
      createExpense(39000, `${currentYear}-${String(currentMonth + 1).padStart(2, '0')}-25`, 'transportation'),
      createExpense(31280, `${currentYear}-${String(currentMonth + 1).padStart(2, '0')}-27`, 'bills'),
      createExpense(16000, `${currentYear}-${String(currentMonth + 1).padStart(2, '0')}-27`, 'mobile', 'Ghassen') // Same day
    )
  }
  
  // Current month - Week 5 (days 29-31)
  if (currentDate >= 29) {
    expenses.push(
      createExpense(25000, `${currentYear}-${String(currentMonth + 1).padStart(2, '0')}-29`, 'dining'),
      createExpense(18000, `${currentYear}-${String(currentMonth + 1).padStart(2, '0')}-30`, 'transportation')
    )
  }
  
  // Previous month (1 month ago)
  const prevMonth = currentMonth === 0 ? 11 : currentMonth - 1
  const prevYear = currentMonth === 0 ? currentYear - 1 : currentYear
  
  // Previous month - Week 1
  expenses.push(
    createExpense(52000, `${prevYear}-${String(prevMonth + 1).padStart(2, '0')}-02`, 'rent'),
    createExpense(38000, `${prevYear}-${String(prevMonth + 1).padStart(2, '0')}-04`, 'groceries'),
    createExpense(29000, `${prevYear}-${String(prevMonth + 1).padStart(2, '0')}-06`, 'bills')
  )
  
  // Previous month - Week 2
  expenses.push(
    createExpense(41000, `${prevYear}-${String(prevMonth + 1).padStart(2, '0')}-09`, 'dining'),
    createExpense(44000, `${prevYear}-${String(prevMonth + 1).padStart(2, '0')}-11`, 'groceries'),
    createExpense(33000, `${prevYear}-${String(prevMonth + 1).padStart(2, '0')}-13`, 'transportation')
  )
  
  // Previous month - Week 3
  expenses.push(
    createExpense(49000, `${prevYear}-${String(prevMonth + 1).padStart(2, '0')}-16`, 'internet'),
    createExpense(36000, `${prevYear}-${String(prevMonth + 1).padStart(2, '0')}-18`, 'mobile'),
    createExpense(27000, `${prevYear}-${String(prevMonth + 1).padStart(2, '0')}-20`, 'bills')
  )
  
  // Previous month - Week 4
  expenses.push(
    createExpense(56000, `${prevYear}-${String(prevMonth + 1).padStart(2, '0')}-23`, 'rent'),
    createExpense(43000, `${prevYear}-${String(prevMonth + 1).padStart(2, '0')}-25`, 'groceries'),
    createExpense(34000, `${prevYear}-${String(prevMonth + 1).padStart(2, '0')}-27`, 'dining')
  )
  
  // Previous month - Week 5
  expenses.push(
    createExpense(51000, `${prevYear}-${String(prevMonth + 1).padStart(2, '0')}-29`, 'transportation'),
    createExpense(22000, `${prevYear}-${String(prevMonth + 1).padStart(2, '0')}-30`, 'bills')
  )
  
  // 2 months ago
  const twoMonthsAgo = prevMonth === 0 ? 11 : prevMonth - 1
  const twoMonthsAgoYear = prevMonth === 0 ? prevYear - 1 : prevYear
  
  // 2 months ago - Week 1
  expenses.push(
    createExpense(47000, `${twoMonthsAgoYear}-${String(twoMonthsAgo + 1).padStart(2, '0')}-02`, 'groceries'),
    createExpense(39000, `${twoMonthsAgoYear}-${String(twoMonthsAgo + 1).padStart(2, '0')}-04`, 'dining'),
    createExpense(31000, `${twoMonthsAgoYear}-${String(twoMonthsAgo + 1).padStart(2, '0')}-06`, 'transportation')
  )
  
  // 2 months ago - Week 2
  expenses.push(
    createExpense(42000, `${twoMonthsAgoYear}-${String(twoMonthsAgo + 1).padStart(2, '0')}-09`, 'bills'),
    createExpense(45000, `${twoMonthsAgoYear}-${String(twoMonthsAgo + 1).padStart(2, '0')}-11`, 'groceries'),
    createExpense(36000, `${twoMonthsAgoYear}-${String(twoMonthsAgo + 1).padStart(2, '0')}-13`, 'rent')
  )
  
  // 2 months ago - Week 3
  expenses.push(
    createExpense(51000, `${twoMonthsAgoYear}-${String(twoMonthsAgo + 1).padStart(2, '0')}-16`, 'internet'),
    createExpense(38000, `${twoMonthsAgoYear}-${String(twoMonthsAgo + 1).padStart(2, '0')}-18`, 'mobile'),
    createExpense(29000, `${twoMonthsAgoYear}-${String(twoMonthsAgo + 1).padStart(2, '0')}-20`, 'bills')
  )
  
  // 2 months ago - Week 4
  expenses.push(
    createExpense(54000, `${twoMonthsAgoYear}-${String(twoMonthsAgo + 1).padStart(2, '0')}-23`, 'rent'),
    createExpense(41000, `${twoMonthsAgoYear}-${String(twoMonthsAgo + 1).padStart(2, '0')}-25`, 'groceries'),
    createExpense(33000, `${twoMonthsAgoYear}-${String(twoMonthsAgo + 1).padStart(2, '0')}-27`, 'dining')
  )
  
  // 2 months ago - Week 5
  expenses.push(
    createExpense(48000, `${twoMonthsAgoYear}-${String(twoMonthsAgo + 1).padStart(2, '0')}-29`, 'transportation'),
    createExpense(24000, `${twoMonthsAgoYear}-${String(twoMonthsAgo + 1).padStart(2, '0')}-30`, 'bills')
  )
  
  return expenses
}

const demoExpenses = generateDemoExpenses()

// Period filter state (synced with MonthlyTotalCard)
const selectedPeriod = ref<'month' | 'week' | 'all' | 'custom'>('month')
const customDateRange = ref<{ start: string | null; end: string | null }>({ start: null, end: null })

// Handle period change from MonthlyTotalCard
const handlePeriodChange = (period: 'month' | 'week' | 'all' | 'custom', dateRange?: { start: string | null; end: string | null }) => {
  selectedPeriod.value = period
  if (dateRange) {
    customDateRange.value = dateRange
  }
}

// Get date range based on selected period (same logic as MonthlyTotalCard)
const dateRange = computed(() => {
  const now = new Date()
  let start: Date
  let end: Date = new Date(now)

  switch (selectedPeriod.value) {
    case 'month':
      start = new Date(now.getFullYear(), now.getMonth(), 1)
      end = new Date(now.getFullYear(), now.getMonth() + 1, 0, 23, 59, 59, 999)
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
      start = new Date(now.getFullYear(), now.getMonth() - 2, 1)
      end = new Date(now.getFullYear(), now.getMonth() + 1, 0, 23, 59, 59, 999)
      break
    case 'custom':
      if (customDateRange.value.start && customDateRange.value.end) {
        start = new Date(customDateRange.value.start)
        start.setHours(0, 0, 0, 0)
        end = new Date(customDateRange.value.end)
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

  return { start, end }
})

// Filter expenses by date range
const filteredExpenses = computed(() => {
  const { start, end } = dateRange.value
  
  return demoExpenses.filter(expense => {
    const expenseDate = new Date(expense.dateISO)
    return expenseDate >= start && expenseDate <= end
  })
})

// Sort expenses by date (newest first)
const sortedExpenses = computed(() => {
  return [...filteredExpenses.value].sort((a, b) => {
    return new Date(b.dateISO).getTime() - new Date(a.dateISO).getTime()
  })
})

// Group expenses by day
const groupedExpenses = computed(() => {
  const groups: Record<string, typeof sortedExpenses.value> = {}
  
  sortedExpenses.value.forEach(expense => {
    const date = new Date(expense.dateISO)
    const dateKey = date.toISOString().split('T')[0] // YYYY-MM-DD
    
    if (!groups[dateKey]) {
      groups[dateKey] = []
    }
    groups[dateKey].push(expense)
  })
  
  return groups
})

// Format day label (Today, Yesterday, or date)
const formatDayLabel = (dateISO: string): string => {
  const date = new Date(dateISO)
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  
  const expenseDate = new Date(date)
  expenseDate.setHours(0, 0, 0, 0)
  
  const diffTime = today.getTime() - expenseDate.getTime()
  const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24))
  
  // Get locale from i18n (e.g., 'en' or 'ja')
  const currentLocale = locale.value === 'ja' ? 'ja-JP' : 'en-US'
  
  if (diffDays === 0) {
    return t('common.today')
  } else if (diffDays === 1) {
    return t('common.yesterday')
  } else if (diffDays < 7) {
    return date.toLocaleDateString(currentLocale, { weekday: 'long' })
  } else {
    return date.toLocaleDateString(currentLocale, { month: 'long', day: 'numeric', year: 'numeric' })
  }
}

// Pagination
const itemsPerPage = 10
const visibleCount = ref(itemsPerPage)

// Get grouped expenses with labels
const expenseGroups = computed(() => {
  return Object.entries(groupedExpenses.value)
    .map(([date, expenses]) => ({
      date,
      label: formatDayLabel(date),
      expenses
    }))
    .sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime())
})

// Get displayed groups (lazy loaded)
const displayedGroups = computed(() => {
  let totalCount = 0
  const result: typeof expenseGroups.value = []
  
  for (const group of expenseGroups.value) {
    if (totalCount >= visibleCount.value) break
    
    const remaining = visibleCount.value - totalCount
    if (group.expenses.length <= remaining) {
      result.push(group)
      totalCount += group.expenses.length
    } else {
      result.push({
        ...group,
        expenses: group.expenses.slice(0, remaining)
      })
      totalCount += remaining
    }
  }
  
  return result
})

// Check if there are more expenses to load
const hasMore = computed(() => {
  const totalExpenses = sortedExpenses.value.length
  return visibleCount.value < totalExpenses
})

// Load more expenses
const loadMore = () => {
  visibleCount.value += itemsPerPage
}

const loadMoreTrigger = ref<HTMLElement | null>(null)

// Infinite scroll with Intersection Observer
if (process.client) {
  onMounted(() => {
    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting && hasMore.value) {
          loadMore()
        }
      },
      { rootMargin: '100px' }
    )
    
    watch(loadMoreTrigger, (el) => {
      if (el) {
        observer.observe(el)
      }
    }, { immediate: true })
    
    onUnmounted(() => {
      if (loadMoreTrigger.value) {
        observer.unobserve(loadMoreTrigger.value)
      }
      observer.disconnect()
    })
  })
}

const handleExpenseClick = (expenseId: string) => {
  // TODO: Navigate to expense detail page or open expense modal
  console.log('Expense clicked:', expenseId)
}
</script>
