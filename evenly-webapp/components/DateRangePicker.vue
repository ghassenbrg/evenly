<template>
  <div class="relative" ref="triggerRef">
    <!-- Trigger Button -->
    <button
      @click="toggleCalendar"
      class="w-full px-3 py-2 bg-slate-800/80 ring-1 ring-white/10 rounded-lg text-xs text-gray-200 hover:bg-slate-800 transition-colors text-left flex items-center justify-between"
    >
      <span>{{ displayText }}</span>
      <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
      </svg>
    </button>
    
    <!-- Calendar Popup (Teleported to body) -->
    <Teleport to="body">
      <Transition name="fade">
        <div
          v-if="showCalendar"
          ref="calendarRef"
          class="fixed bg-slate-800 ring-1 ring-white/10 rounded-lg shadow-lg z-[9999] p-3"
          :style="calendarStyle"
        >
      <!-- Header -->
      <div class="flex items-center justify-between mb-3">
        <button
          @click="previousMonth"
          class="p-1.5 text-white/70 hover:text-white hover:bg-slate-700/50 rounded transition-colors"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
          </svg>
        </button>
        
        <h3 class="text-sm font-semibold text-white">
          {{ currentMonthYear }}
        </h3>
        
        <button
          @click="nextMonth"
          class="p-1.5 text-white/70 hover:text-white hover:bg-slate-700/50 rounded transition-colors"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
          </svg>
        </button>
      </div>
      
      <!-- Weekday Headers -->
      <div class="grid grid-cols-7 gap-1 mb-2">
        <div
          v-for="day in weekDays"
          :key="day"
          class="text-center text-[10px] text-white/50 font-medium py-1"
        >
          {{ day }}
        </div>
      </div>
      
      <!-- Calendar Grid -->
      <div class="grid grid-cols-7 gap-1">
        <div
          v-for="(day, index) in calendarDays"
          :key="index"
          @click="selectDate(day.date)"
          :class="[
            'relative h-9 flex items-center justify-center text-xs rounded transition-colors cursor-pointer',
            {
              'text-white/30': !day.isCurrentMonth,
              'text-white': day.isCurrentMonth && !day.isSelected && !day.isInRange,
              'bg-emerald-500 text-white font-semibold': day.isStart || day.isEnd,
              'bg-emerald-500/20 text-emerald-400': day.isInRange && !day.isStart && !day.isEnd,
              'hover:bg-slate-700/50': day.isCurrentMonth && !day.isSelected && !day.isInRange,
              'ring-2 ring-emerald-400': day.isStart || day.isEnd,
              'text-white/50': day.isDisabled
            }
          ]"
          :disabled="day.isDisabled"
        >
          {{ day.date.getDate() }}
        </div>
      </div>
      
      <!-- Selected Range Display -->
      <div v-if="startDate && endDate" class="mt-3 pt-3 border-t border-white/10">
        <div class="flex items-center justify-between text-xs">
          <div>
            <p class="text-white/50 mb-0.5">Start</p>
            <p class="text-white font-medium">{{ formatDate(startDate) }}</p>
          </div>
          <div class="text-white/30">→</div>
          <div>
            <p class="text-white/50 mb-0.5">End</p>
            <p class="text-white font-medium">{{ formatDate(endDate) }}</p>
          </div>
        </div>
      </div>
      
      <!-- Actions -->
      <div class="mt-3 flex gap-2">
        <button
          @click="clearSelection"
          class="flex-1 px-3 py-2 bg-slate-700/50 hover:bg-slate-700 text-xs text-white/70 rounded-lg transition-colors"
        >
          Clear
        </button>
        <button
          @click="applySelection"
          :disabled="!startDate || !endDate"
          class="flex-1 px-3 py-2 bg-emerald-500/80 hover:bg-emerald-500 disabled:bg-slate-700/50 disabled:text-white/40 text-xs font-medium text-white rounded-lg transition-colors"
        >
          Apply
        </button>
      </div>
      </div>
    </Transition>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
interface Props {
  modelValue?: { start: string | null; end: string | null }
  inline?: boolean
  minDate?: string
  maxDate?: string
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: undefined,
  inline: false,
  minDate: undefined,
  maxDate: undefined
})

const emit = defineEmits<{
  'update:modelValue': [{ start: string | null; end: string | null }]
  'apply': [{ start: string | null; end: string | null }]
}>()

const weekDays = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat']

const showCalendar = ref(false)
const currentMonth = ref(new Date().getMonth())
const currentYear = ref(new Date().getFullYear())
const startDate = ref<Date | null>(null)
const endDate = ref<Date | null>(null)
const selectingStart = ref(true)

// Initialize from modelValue
watch(() => props.modelValue, (newVal) => {
  if (newVal) {
    startDate.value = newVal.start ? new Date(newVal.start) : null
    endDate.value = newVal.end ? new Date(newVal.end) : null
  }
}, { immediate: true })

const currentMonthYear = computed(() => {
  return new Date(currentYear.value, currentMonth.value).toLocaleDateString('en-US', {
    month: 'long',
    year: 'numeric'
  })
})

const calendarDays = computed(() => {
  const firstDay = new Date(currentYear.value, currentMonth.value, 1)
  const lastDay = new Date(currentYear.value, currentMonth.value + 1, 0)
  const startDateOfWeek = new Date(firstDay)
  startDateOfWeek.setDate(startDateOfWeek.getDate() - startDateOfWeek.getDay())
  
  const days: Array<{
    date: Date
    isCurrentMonth: boolean
    isSelected: boolean
    isStart: boolean
    isEnd: boolean
    isInRange: boolean
    isDisabled: boolean
  }> = []
  
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  
  const minDateObj = props.minDate ? new Date(props.minDate) : null
  const maxDateObj = props.maxDate ? new Date(props.maxDate) : new Date()
  maxDateObj.setHours(23, 59, 59, 999)
  
  for (let i = 0; i < 42; i++) {
    const date = new Date(startDateOfWeek)
    date.setDate(startDateOfWeek.getDate() + i)
    date.setHours(0, 0, 0, 0)
    
    const isCurrentMonth = date.getMonth() === currentMonth.value
    const isStart = !!(startDate.value && date.getTime() === startDate.value.getTime())
    const isEnd = !!(endDate.value && date.getTime() === endDate.value.getTime())
    const isInRange = !!(startDate.value && endDate.value && 
      date > startDate.value && date < endDate.value)
    const isSelected = isStart || isEnd || isInRange
    const isDisabled = !!(minDateObj && date < minDateObj) || !!(maxDateObj && date > maxDateObj)
    
    days.push({
      date,
      isCurrentMonth,
      isSelected,
      isStart,
      isEnd,
      isInRange,
      isDisabled
    })
  }
  
  return days
})

const selectDate = (date: Date) => {
  const dateObj = new Date(date)
  dateObj.setHours(0, 0, 0, 0)
  
  const minDateObj = props.minDate ? new Date(props.minDate) : null
  const maxDateObj = props.maxDate ? new Date(props.maxDate) : new Date()
  maxDateObj.setHours(23, 59, 59, 999)
  
  if ((minDateObj && dateObj < minDateObj) || (maxDateObj && dateObj > maxDateObj)) {
    return
  }
  
  if (selectingStart.value || !startDate.value) {
    // Start selecting
    startDate.value = dateObj
    endDate.value = null
    selectingStart.value = false
  } else {
    // End selecting
    if (dateObj < startDate.value!) {
      // If selected date is before start, make it the new start
      endDate.value = startDate.value
      startDate.value = dateObj
    } else {
      endDate.value = dateObj
    }
    selectingStart.value = true
  }
  
  emit('update:modelValue', {
    start: startDate.value ? startDate.value.toISOString().split('T')[0] : null,
    end: endDate.value ? endDate.value.toISOString().split('T')[0] : null
  })
}

const previousMonth = () => {
  if (currentMonth.value === 0) {
    currentMonth.value = 11
    currentYear.value--
  } else {
    currentMonth.value--
  }
}

const nextMonth = () => {
  if (currentMonth.value === 11) {
    currentMonth.value = 0
    currentYear.value++
  } else {
    currentMonth.value++
  }
}

const clearSelection = () => {
  startDate.value = null
  endDate.value = null
  selectingStart.value = true
  emit('update:modelValue', { start: null, end: null })
}

const applySelection = () => {
  if (startDate.value && endDate.value) {
    emit('apply', {
      start: startDate.value.toISOString().split('T')[0],
      end: endDate.value.toISOString().split('T')[0]
    })
    showCalendar.value = false
  }
}

const formatDate = (date: Date) => {
  return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })
}

const displayText = computed(() => {
  if (startDate.value && endDate.value) {
    return `${formatDate(startDate.value)} - ${formatDate(endDate.value)}`
  }
  if (startDate.value) {
    return `${formatDate(startDate.value)} - ...`
  }
  return 'Select Date Range'
})

const calendarRef = ref<HTMLElement | null>(null)
const triggerRef = ref<HTMLElement | null>(null)
const calendarStyle = ref({ top: '0px', left: '0px', width: '288px' })

const toggleCalendar = () => {
  showCalendar.value = !showCalendar.value
  if (showCalendar.value && process.client) {
    nextTick(() => {
      updateCalendarPosition()
    })
  }
}

const updateCalendarPosition = () => {
  if (!triggerRef.value || !calendarRef.value) return
  
  const triggerRect = triggerRef.value.getBoundingClientRect()
  const calendarWidth = 288 // w-72 = 18rem = 288px
  const calendarHeight = calendarRef.value.offsetHeight
  const viewportWidth = window.innerWidth
  const viewportHeight = window.innerHeight
  
  let left = triggerRect.left
  let top = triggerRect.bottom + 8 // mt-2 = 8px
  
  // Adjust if calendar goes off right edge
  if (left + calendarWidth > viewportWidth) {
    left = viewportWidth - calendarWidth - 16
  }
  
  // Adjust if calendar goes off bottom edge
  if (top + calendarHeight > viewportHeight) {
    top = triggerRect.top - calendarHeight - 8
  }
  
  // Ensure calendar doesn't go off left edge
  if (left < 16) {
    left = 16
  }
  
  // Ensure calendar doesn't go off top edge
  if (top < 16) {
    top = 16
  }
  
  calendarStyle.value = {
    top: `${top}px`,
    left: `${left}px`,
    width: `${calendarWidth}px`
  }
}

// Click outside handler
if (process.client) {
  onMounted(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (calendarRef.value && triggerRef.value) {
        const target = event.target as HTMLElement
        if (!calendarRef.value.contains(target) && !triggerRef.value.contains(target)) {
          showCalendar.value = false
        }
      }
    }
    document.addEventListener('click', handleClickOutside)
    
    // Update position on scroll/resize
    const handleResize = () => {
      if (showCalendar.value) {
        updateCalendarPosition()
      }
    }
    window.addEventListener('scroll', handleResize, true)
    window.addEventListener('resize', handleResize)
    
    onUnmounted(() => {
      document.removeEventListener('click', handleClickOutside)
      window.removeEventListener('scroll', handleResize, true)
      window.removeEventListener('resize', handleResize)
    })
  })
}
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
  transform-origin: top;
}

.fade-enter-from {
  opacity: 0;
  transform: scale(0.95) translateY(-10px);
}

.fade-leave-to {
  opacity: 0;
  transform: scale(0.95) translateY(-10px);
}
</style>

