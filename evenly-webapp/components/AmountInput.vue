<template>
  <div>
    <label v-if="label" class="block text-sm font-medium text-gray-300 mb-2">
      {{ label }}
    </label>
    <div class="relative">
      <span class="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400">¥</span>
      <input
        :value="displayValue"
        type="tel"
        inputmode="numeric"
        pattern="[0-9]*"
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
  modelValue: number
  label?: string
  hint?: string
  placeholder?: string
  inputClass?: string
}

const props = withDefaults(defineProps<Props>(), {
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

// Internal raw value for editing
const rawValue = ref('')

// Display value - show raw number when focused, formatted when not focused
const displayValue = computed(() => {
  if (isFocused.value) {
    // When focused, use raw value for editing
    return rawValue.value
  } else {
    // When not focused, show formatted number (without currency symbol, just commas)
    if (props.modelValue === 0) return ''
    return props.modelValue.toLocaleString('ja-JP')
  }
})

const handleInput = (event: Event) => {
  const target = event.target as HTMLInputElement
  let value = target.value
  
  // Remove all non-numeric characters (including commas)
  value = value.replace(/[^0-9]/g, '')
  
  // Store raw value for editing
  rawValue.value = value
  
  // Convert to number
  const numValue = value === '' ? 0 : parseInt(value, 10)
  
  // Ensure it's a positive number
  const positiveValue = Math.max(0, numValue)
  
  // Emit the numeric value
  emit('update:modelValue', positiveValue)
}

const handleFocus = () => {
  isFocused.value = true
  // Set raw value when focusing
  rawValue.value = props.modelValue === 0 ? '' : props.modelValue.toString()
}

const handleBlur = () => {
  isFocused.value = false
  // Clear raw value when blurring (will use formatted value)
  rawValue.value = ''
}
</script>

