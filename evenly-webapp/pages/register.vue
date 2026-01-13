<template>
  <div class="min-h-screen flex flex-col items-center justify-center px-6 py-12 bg-slate-900">
    <LanguageSwitcher />
    <div class="w-full max-w-md space-y-8">
      <!-- Boarding Image -->
      <div class="flex justify-center">
        <img
          src="/images/boarding.png"
          alt="Evenly"
          class="w-full max-w-sm h-auto object-contain"
        />
      </div>

      <!-- Title -->
      <div class="text-center space-y-2">
        <h1 class="text-3xl font-bold text-white">{{ t('auth.register.title') }}</h1>
        <p class="text-slate-400">{{ t('auth.register.subtitle') }}</p>
      </div>

      <!-- Register Form -->
      <form @submit.prevent="handleRegister" class="space-y-4">
        <div>
          <label for="email" class="block text-sm font-medium text-slate-300 mb-2">
            {{ t('auth.register.email') || 'Email' }}
          </label>
          <input
            id="email"
            v-model="form.email"
            type="email"
            required
            autocomplete="email"
            class="w-full px-4 py-3 bg-slate-800 border border-slate-700 rounded-lg text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
            :placeholder="t('auth.register.emailPlaceholder') || 'Enter your email'"
            :disabled="loading"
          />
        </div>

        <div>
          <label for="username" class="block text-sm font-medium text-slate-300 mb-2">
            {{ t('auth.register.username') || 'Username' }}
          </label>
          <input
            id="username"
            v-model="form.username"
            type="text"
            required
            autocomplete="username"
            class="w-full px-4 py-3 bg-slate-800 border border-slate-700 rounded-lg text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
            :placeholder="t('auth.register.usernamePlaceholder') || 'Choose a username'"
            :disabled="loading"
          />
        </div>

        <div>
          <label for="displayName" class="block text-sm font-medium text-slate-300 mb-2">
            {{ t('auth.register.displayName') || 'Display Name' }}
          </label>
          <input
            id="displayName"
            v-model="form.displayName"
            type="text"
            required
            autocomplete="name"
            class="w-full px-4 py-3 bg-slate-800 border border-slate-700 rounded-lg text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
            :placeholder="t('auth.register.displayNamePlaceholder') || 'Enter your display name'"
            :disabled="loading"
          />
        </div>

        <div>
          <label for="password" class="block text-sm font-medium text-slate-300 mb-2">
            {{ t('auth.register.password') || 'Password' }}
          </label>
          <input
            id="password"
            v-model="form.password"
            type="password"
            required
            autocomplete="new-password"
            class="w-full px-4 py-3 bg-slate-800 border border-slate-700 rounded-lg text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
            :placeholder="t('auth.register.passwordPlaceholder') || 'Choose a password'"
            :disabled="loading"
          />
        </div>

        <div>
          <label for="preferredCurrency" class="block text-sm font-medium text-slate-300 mb-2">
            {{ t('auth.register.preferredCurrency') || 'Preferred Currency' }}
          </label>
          <select
            id="preferredCurrency"
            v-model="form.preferredCurrency"
            required
            class="w-full px-4 py-3 bg-slate-800 border border-slate-700 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
            :disabled="loading || loadingCurrencies"
          >
            <option value="" disabled>{{ t('auth.register.selectCurrency') || 'Select currency' }}</option>
            <option v-for="currency in currencies" :key="currency.code" :value="currency.code">
              {{ currency.code }} - {{ getCurrencyName(currency.code) }} ({{ currency.symbol }})
            </option>
          </select>
          <p v-if="loadingCurrencies" class="text-sm text-slate-400 mt-1">
            {{ t('auth.register.loadingCurrencies') || 'Loading currencies...' }}
          </p>
        </div>

        <div v-if="error" class="text-red-400 text-sm">
          {{ error }}
        </div>

        <!-- Register Button -->
        <button
          type="submit"
          :disabled="loading || loadingCurrencies"
          class="btn btn-green w-full py-3"
        >
          <span v-if="!loading">{{ t('auth.register.submit') }}</span>
          <span v-else>{{ t('auth.register.submitting') }}</span>
        </button>
      </form>

      <!-- Login Link -->
      <div class="text-center">
        <p class="text-slate-400">
          {{ t('auth.register.hasAccount') }}
          <NuxtLink to="/login" class="text-emerald-500 hover:text-emerald-400 font-medium">
            {{ t('auth.register.signIn') }}
          </NuxtLink>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { RegisterRequest } from '~/types/api'

definePageMeta({
  middleware: 'guest',
  layout: false
})

const { register } = useAuth()
const { error: showError } = useToast()
const { currencies, fetchCurrencies, loading: currenciesLoading } = useCurrencies()
const loading = ref(false)
const error = ref<string | null>(null)

const loadingCurrencies = computed(() => currenciesLoading.value)

const { t } = useI18n()

const getCurrencyName = (code: string) => {
  return t(`currencies.${code}`) || code
}

const form = ref<RegisterRequest>({
  email: '',
  username: '',
  displayName: '',
  password: '',
  preferredCurrency: ''
})

// Fetch currencies on mount
onMounted(async () => {
  try {
    await fetchCurrencies()
    // Set default currency to USD if available
    if (currencies.value.length > 0 && !form.value.preferredCurrency) {
      const usd = currencies.value.find(c => c.code === 'USD')
      form.value.preferredCurrency = usd?.code || currencies.value[0].code
    }
  } catch (err) {
    console.error('Failed to load currencies:', err)
  }
})

const handleRegister = async () => {
  try {
    loading.value = true
    error.value = null
    
    await register(form.value)
  } catch (err: any) {
    console.error('Registration error:', err)
    const errorMessage = err.message || err.error?.message || (err.errors && err.errors.length > 0 
      ? err.errors.map((e: any) => e.message || `${e.field}: ${e.message}`).join(', ')
      : t('auth.register.error') || 'Registration failed. Please check your information.')
    error.value = errorMessage
    showError(errorMessage)
    loading.value = false
  }
}
</script>

