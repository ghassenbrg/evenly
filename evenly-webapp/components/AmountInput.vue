<template>
  <div>
    <label v-if="label" class="block text-sm font-medium text-gray-300 mb-2">
      {{ label }}
    </label>
    <div class="relative">
      <span class="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400">{{ currencySymbol }}</span>
      <input
        :value="displayValue"
        type="tel"
        inputmode="decimal"
        pattern="[0-9]*\.?[0-9]*"
        :placeholder="placeholder"
        :class="inputClass"
        @input="handleInput"
        @blur="handleBlur"
        @focus="handleFocus"
      />
    </div>
    <p v-if="hint" class="mt-1 text-xs text-gray-400">
      {{ hint }}
    </p>
  </div>
</template>

<script setup lang="ts">
import { useFormatting } from '~/composables/useFormatting'

interface Props {
  modelValue: number | null | undefined
  currency?: string
  label?: string
  hint?: string
  placeholder?: string
  inputClass?: string
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: 0,
  currency: 'USD',
  label: undefined,
  hint: undefined,
  placeholder: undefined,
  inputClass: 'w-full pl-8 pr-4 py-3 bg-slate-800 border border-slate-700 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent text-lg font-semibold'
})

const emit = defineEmits<{
  'update:modelValue': [value: number]
}>()

const { formatCurrency } = useFormatting()
const isFocused = ref(false)

// Get currency symbol
const currencySymbol = computed(() => {
  if (!props.currency) return '$'
  try {
    // Use Intl.NumberFormat to get the currency symbol
    const formatter = new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: props.currency,
      minimumFractionDigits: 0,
      maximumFractionDigits: 0
    })
    // Extract symbol from formatted string (e.g., "$1" -> "$")
    const parts = formatter.formatToParts(1)
    const symbolPart = parts.find(part => part.type === 'currency')
    return symbolPart?.value || '$'
  } catch {
    // Fallback for common currencies
    const symbols: Record<string, string> = {
      'USD': '$',
      'EUR': '€',
      'GBP': '£',
      'JPY': '¥',
      'CNY': '¥',
      'INR': '₹',
      'CAD': 'C$',
      'AUD': 'A$',
      'SGD': 'S$',
      'CHF': 'CHF'
    }
    return symbols[props.currency] || props.currency
  }
})

// Internal raw value for editing
const rawValue = ref('')

// Display value - show raw number when focused, formatted when not focused
const displayValue = computed(() => {
  if (isFocused.value) {
    // When focused, use raw value for editing
    return rawValue.value
  } else {
    // When not focused, show formatted number (without currency symbol, just commas)
    if (!props.modelValue || props.modelValue === 0) return ''
    const value = Number(props.modelValue)
    if (isNaN(value)) return ''
    // Format with decimals if needed
    const formatted = value % 1 === 0
      ? value.toLocaleString('en-US')
      : value.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
    return formatted
  }
})

const handleInput = (event: Event) => {
  const target = event.target as HTMLInputElement
  let value = target.value
  
  // Remove all non-numeric characters except decimal point
  value = value.replace(/[^0-9.]/g, '')
  
  // Store raw value for editing
  rawValue.value = value
  
  // Convert to number (support decimals)
  let numValue = 0
  if (value !== '') {
    // Support decimal input: allow one decimal point
    const parts = value.split('.')
    if (parts.length === 1) {
      numValue = parseInt(value, 10) || 0
    } else if (parts.length === 2) {
      // Limit decimal places to 2
      const decimalPart = parts[1].slice(0, 2)
      numValue = parseFloat(`${parts[0]}.${decimalPart}`) || 0
    } else {
      // Multiple decimal points, take first two parts
      numValue = parseFloat(`${parts[0]}.${parts[1]?.slice(0, 2) || '0'}`) || 0
    }
  }
  
  // Ensure it's a positive number
  const positiveValue = Math.max(0, numValue)
  
  // Emit the numeric value
  emit('update:modelValue', positiveValue)
}

const handleFocus = () => {
  isFocused.value = true
  // Set raw value when focusing (preserve decimals)
  if (!props.modelValue || props.modelValue === 0) {
    rawValue.value = ''
  } else {
    const value = Number(props.modelValue)
    if (isNaN(value)) {
      rawValue.value = ''
    } else {
      // Format to show up to 2 decimal places
      rawValue.value = value % 1 === 0 
        ? value.toString() 
        : value.toFixed(2).replace(/\.?0+$/, '')
    }
  }
}

const handleBlur = () => {
  isFocused.value = false
  // Clear raw value when blurring (will use formatted value)
  rawValue.value = ''
}
</script>

