<template>
  <div class="relative" ref="dropdownRef">
    <button
      type="button"
      @click="toggleDropdown"
      class="px-3 py-1.5 bg-slate-800/80 ring-1 ring-white/10 rounded-xl text-sm text-gray-200 font-medium flex items-center gap-1.5 hover:bg-slate-800 transition-colors"
    >
      <span>{{ selectedLabel }}</span>
      <font-awesome-icon
        v-if="direction === 'ASC'"
        icon="fa-solid fa-arrow-up-1-9"
        class="w-4 h-4 text-white/60"
      />
      <font-awesome-icon
        v-else
        icon="fa-solid fa-arrow-down-9-1"
        class="w-4 h-4 text-white/60"
      />
    </button>

    <div
      v-if="open"
      class="absolute left-0 top-full mt-1.5 w-56 bg-slate-800 ring-1 ring-white/10 rounded-lg shadow-lg z-20 overflow-hidden"
      @click.stop
    >
      <div class="py-1">
        <!-- Sort By Section -->
        <div class="px-3 py-2 border-b border-white/10">
          <p class="text-xs font-semibold text-white/50 uppercase">{{ t('payments.sortBy') }}</p>
        </div>
        <button
          v-for="option in sortByOptions"
          :key="option.value"
          @click="selectSortBy(option.value)"
          class="w-full px-3 py-2.5 text-left text-sm text-gray-200 hover:bg-slate-700/50 transition-colors flex items-center justify-between"
          :class="{ 'bg-slate-700/30': sortBy === option.value }"
        >
          <span>{{ option.label }}</span>
          <svg
            v-if="sortBy === option.value"
            class="w-4 h-4 text-emerald-400"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
          </svg>
        </button>
        
        <!-- Direction Section -->
        <div class="px-3 py-2 border-t border-white/10 mt-1">
          <p class="text-xs font-semibold text-white/50 uppercase">{{ t('payments.direction') }}</p>
        </div>
        <button
          v-for="option in directionOptions"
          :key="option.value"
          @click="selectDirection(option.value)"
          class="w-full px-3 py-2.5 text-left text-sm text-gray-200 hover:bg-slate-700/50 transition-colors flex items-center justify-between"
          :class="{ 'bg-slate-700/30': direction === option.value }"
        >
          <div class="flex items-center gap-2">
            <font-awesome-icon
              v-if="option.value === 'ASC'"
              icon="fa-solid fa-arrow-up-1-9"
              class="w-4 h-4 text-white/80"
            />
            <font-awesome-icon
              v-else
              icon="fa-solid fa-arrow-down-9-1"
              class="w-4 h-4 text-white/80"
            />
            <span>{{ option.label }}</span>
          </div>
          <svg
            v-if="direction === option.value"
            class="w-4 h-4 text-emerald-400"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
          </svg>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
type SortBy = 'effectiveDate' | 'amount'
type Direction = 'ASC' | 'DESC'

interface Props {
  sortBy?: SortBy
  direction?: Direction
}

const props = withDefaults(defineProps<Props>(), {
  sortBy: 'effectiveDate',
  direction: 'DESC'
})

const emit = defineEmits<{
  'update:sortBy': [value: SortBy]
  'update:direction': [value: Direction]
  'sort-change': [sortBy: SortBy, direction: Direction]
}>()

const { t } = useI18n()

const open = ref(false)
const dropdownRef = ref<HTMLElement | null>(null)

const sortBy = ref<SortBy>(props.sortBy)
const direction = ref<Direction>(props.direction)

watch(() => props.sortBy, (val) => {
  sortBy.value = val
})

watch(() => props.direction, (val) => {
  direction.value = val
})

const sortByOptions = computed(() => [
  { value: 'effectiveDate' as SortBy, label: t('payments.sortByDate') },
  { value: 'amount' as SortBy, label: t('payments.sortByAmount') }
])

const directionOptions = computed(() => [
  { value: 'ASC' as Direction, label: t('payments.ascending') },
  { value: 'DESC' as Direction, label: t('payments.descending') }
])

const selectedLabel = computed(() => {
  const sortLabel = sortByOptions.value.find(opt => opt.value === sortBy.value)?.label || ''
  return sortLabel
})

const toggleDropdown = () => {
  open.value = !open.value
}

const closeDropdown = () => {
  open.value = false
}

const selectSortBy = (value: SortBy) => {
  sortBy.value = value
  emit('update:sortBy', value)
  emit('sort-change', value, direction.value)
  closeDropdown()
}

const selectDirection = (value: Direction) => {
  direction.value = value
  emit('update:direction', value)
  emit('sort-change', sortBy.value, value)
  closeDropdown()
}

if (process.client) {
  onMounted(() => {
    const handleClickOutside = (e: MouseEvent) => {
      const target = e.target as HTMLElement
      if (dropdownRef.value && dropdownRef.value.contains(target)) {
        return
      }
      closeDropdown()
    }
    document.addEventListener('click', handleClickOutside)
    onUnmounted(() => {
      document.removeEventListener('click', handleClickOutside)
    })
  })
}
</script>

