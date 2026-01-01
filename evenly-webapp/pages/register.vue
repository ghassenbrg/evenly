<template>
  <div class="min-h-screen flex flex-col items-center justify-center px-6 py-12 bg-slate-900">
    <LanguageSwitcher />
    <div class="w-full max-w-md space-y-8">
      <!-- Logo -->
      <div class="flex justify-center">
        <div class="w-20 h-20 rounded-2xl bg-emerald-500 flex items-center justify-center">
          <span class="text-4xl text-white">¥</span>
        </div>
      </div>

      <!-- Title -->
      <div class="text-center space-y-2">
        <h1 class="text-3xl font-bold text-white">{{ t('auth.register.title') }}</h1>
        <p class="text-slate-400">{{ t('auth.register.subtitle') }}</p>
      </div>

      <!-- Form -->
      <form @submit.prevent="handleRegister" class="space-y-4">
        <div>
          <label class="block text-sm font-medium text-slate-300 mb-2">{{ t('auth.register.displayName') }}</label>
          <input
            v-model="displayName"
            type="text"
            required
            class="w-full px-4 py-3 bg-slate-800 border border-slate-700 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-emerald-500"
            :placeholder="t('auth.register.displayNamePlaceholder')"
          />
        </div>

        <div>
          <label class="block text-sm font-medium text-slate-300 mb-2">{{ t('auth.register.username') }}</label>
          <input
            v-model="username"
            type="text"
            required
            class="w-full px-4 py-3 bg-slate-800 border border-slate-700 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-emerald-500"
            :placeholder="t('auth.register.usernamePlaceholder')"
          />
        </div>

        <div>
          <label class="block text-sm font-medium text-slate-300 mb-2">{{ t('auth.register.email') }}</label>
          <input
            v-model="email"
            type="email"
            required
            class="w-full px-4 py-3 bg-slate-800 border border-slate-700 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-emerald-500"
            :placeholder="t('auth.register.emailPlaceholder')"
          />
        </div>

        <div>
          <label class="block text-sm font-medium text-slate-300 mb-2">{{ t('auth.register.preferredCurrency') }}</label>
          <select
            v-model="preferredCurrency"
            required
            :disabled="currenciesLoading"
            class="w-full px-4 py-3 bg-slate-800 border border-slate-700 rounded-xl text-white focus:outline-none focus:ring-2 focus:ring-emerald-500 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <option value="">{{ currenciesLoading ? t('auth.register.loadingCurrencies') : t('auth.register.selectCurrency') }}</option>
            <option
              v-for="currency in currencies"
              :key="currency.code"
              :value="currency.code"
            >
              {{ currency.code }} - {{ currency.name }} ({{ currency.symbol }})
            </option>
          </select>
        </div>

        <div>
          <label class="block text-sm font-medium text-slate-300 mb-2">{{ t('auth.register.password') }}</label>
          <input
            v-model="password"
            type="password"
            required
            minlength="6"
            class="w-full px-4 py-3 bg-slate-800 border border-slate-700 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-emerald-500"
            :placeholder="t('auth.register.passwordPlaceholder')"
          />
        </div>

        <button
          type="submit"
          :disabled="loading"
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
import type { Currency } from '~/types/api'

definePageMeta({
  middleware: 'guest',
  layout: false
})

const authStore = useAuthStore()
const { success, error } = useToast()
const router = useRouter()
const api = useApi()

const displayName = ref('')
const username = ref('')
const email = ref('')
const preferredCurrency = ref('JPY')
const password = ref('')
const loading = ref(false)
const currencies = ref<Currency[]>([])
const currenciesLoading = ref(false)

// Fetch currencies on mount
onMounted(async () => {
  try {
    currenciesLoading.value = true
    currencies.value = await api.get<Currency[]>('/api/currencies')
    // Set default to JPY if available, otherwise first currency
    if (currencies.value.length > 0) {
      const jpyCurrency = currencies.value.find((c: Currency) => c.code === 'JPY')
      preferredCurrency.value = jpyCurrency?.code || currencies.value[0].code
    }
  } catch (err) {
    console.error('Failed to load currencies:', err)
    // Fallback to default list if API fails
    currencies.value = [
      { code: 'JPY', name: 'Japanese Yen', symbol: '¥' },
      { code: 'USD', name: 'US Dollar', symbol: '$' },
      { code: 'EUR', name: 'Euro', symbol: '€' }
    ]
  } finally {
    currenciesLoading.value = false
  }
})

const { t } = useI18n()

const handleRegister = async () => {
  try {
    loading.value = true
    await authStore.registerUser(email.value, password.value, displayName.value, username.value, preferredCurrency.value)
    success(t('auth.register.accountCreated'))
    
    // Wait for next tick and a small delay to ensure token cookie is set
    await nextTick()
    await new Promise(resolve => setTimeout(resolve, 50))
    
    // Pre-load workspaces
    const workspacesStore = useWorkspacesStore()
    await workspacesStore.fetchWorkspaces()
    
    await router.push('/dashboard')
  } catch (err: any) {
    error(err.message || t('auth.register.registrationFailed'))
  } finally {
    loading.value = false
  }
}
</script>

