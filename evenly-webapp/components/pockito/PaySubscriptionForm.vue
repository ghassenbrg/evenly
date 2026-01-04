<template>
  <form class="space-y-5" @submit.prevent="handleSubmit">
    <!-- Subscription Info Card -->
    <div class="rounded-xl bg-gradient-to-br from-emerald-500/10 to-emerald-600/5 border border-emerald-500/20 p-4">
      <div class="flex items-center gap-3">
        <div class="w-12 h-12 rounded-xl bg-emerald-500/20 flex items-center justify-center">
          <svg class="w-6 h-6 text-emerald-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
          </svg>
        </div>
        <div class="flex-1">
          <p class="text-sm font-medium text-emerald-300 mb-1">{{ props.subscription.name }}</p>
          <p class="text-lg font-bold text-white">
            {{ formatCurrency(props.subscription.amount, props.subscription.currency) }}
          </p>
        </div>
      </div>
    </div>

    <!-- Wallet Selection -->
    <div class="space-y-2">
      <label class="text-sm font-medium text-white/90 flex items-center gap-2">
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z" />
        </svg>
        {{ t('subscriptions.pay.wallet') || 'Wallet' }}
        <span v-if="!form.skip" class="text-red-400">*</span>
      </label>
      <select
        v-model="form.walletId"
        class="w-full bg-slate-800/50 border transition-colors rounded-xl px-4 py-3 text-white focus:outline-none focus:ring-2 focus:ring-emerald-500/50 focus:border-emerald-500/50 disabled:opacity-50 disabled:cursor-not-allowed"
        :class="errors.walletId ? 'border-red-500/50' : 'border-slate-700/50'"
        :disabled="form.skip"
      >
        <option :value="null">{{ t('common.outOfPockito') || 'Out of app' }}</option>
        <option v-for="wallet in wallets" :key="wallet.id" :value="wallet.id">
          {{ wallet.name }} ({{ wallet.currency }})
        </option>
      </select>
      <p v-if="errors.walletId" class="text-xs text-red-400 flex items-center gap-1">
        <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
        {{ errors.walletId }}
      </p>
    </div>

    <!-- Exchange Rate (if needed) -->
    <div v-if="showExchangeRate" class="rounded-xl bg-blue-500/10 border border-blue-500/20 p-4 space-y-2">
      <label class="text-sm font-medium text-blue-300 flex items-center gap-2">
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 21a4 4 0 01-4-4V5a2 2 0 012-2h4a2 2 0 012 2v12a4 4 0 01-4 4zm0 0h12a2 2 0 002-2v-4a2 2 0 00-2-2h-2.343M11 7.343l1.657-1.657a2 2 0 012.828 0l2.829 2.829a2 2 0 010 2.828l-8.486 8.485M7 17h.01" />
        </svg>
        {{ t('subscriptions.pay.exchangeRate') || 'Exchange rate' }}
      </label>
      <input
        v-model.number="form.exchangeRate"
        type="number"
        step="0.0001"
        min="0.0001"
        class="w-full bg-slate-800/50 border border-slate-700/50 rounded-xl px-4 py-3 text-white placeholder:text-white/40 focus:outline-none focus:ring-2 focus:ring-blue-500/50 focus:border-blue-500/50"
        placeholder="1.0000"
      />
      <p v-if="errors.exchangeRate" class="text-xs text-red-400">{{ errors.exchangeRate }}</p>
      <p v-else class="text-xs text-blue-300/80">
        {{ formatCurrency(props.subscription.amount * form.exchangeRate, selectedWallet?.currency || props.subscription.currency) }}
        {{ t('subscriptions.pay.willBeDeducted') || 'will be deducted' }}
      </p>
    </div>

    <!-- Skip Option -->
    <label class="flex items-center gap-3 p-4 rounded-xl border border-slate-700/50 hover:border-slate-600/50 transition-colors cursor-pointer group">
      <input
        v-model="form.skip"
        type="checkbox"
        class="w-5 h-5 rounded border-slate-600 bg-slate-800/50 text-emerald-500 focus:ring-2 focus:ring-emerald-500/50 cursor-pointer"
      />
      <div class="flex-1">
        <span class="text-sm font-medium text-white/90 block flex items-center gap-2">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
          {{ t('subscriptions.pay.skip') || 'Skip this cycle' }}
        </span>
        <span class="text-xs text-white/50">{{ t('subscriptions.pay.skipHelp') || 'Skip payment for this billing cycle' }}</span>
      </div>
    </label>

    <!-- Action Buttons -->
    <div class="flex gap-3 pt-2">
      <button
        type="button"
        class="flex-1 h-12 rounded-xl border border-slate-700/50 text-white/80 hover:text-white hover:border-slate-600/50 hover:bg-white/5 transition-colors font-medium flex items-center justify-center gap-2"
        @click="emit('cancelled')"
      >
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
        </svg>
        {{ t('common.cancel') }}
      </button>
      <button
        type="submit"
        class="flex-1 h-12 rounded-xl bg-emerald-500 text-slate-900 font-semibold hover:bg-emerald-400 active:bg-emerald-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors shadow-lg shadow-emerald-500/20 flex items-center justify-center gap-2"
        :disabled="submitting || (form.skip ? false : !form.walletId)"
      >
        <svg v-if="!submitting" class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
        <svg v-else class="animate-spin h-5 w-5" fill="none" viewBox="0 0 24 24">
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
          <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
        </svg>
        {{ submitting ? (t('subscriptions.pay.processing') || 'Processing...') : (t('subscriptions.pay.confirm') || 'Pay') }}
      </button>
    </div>
  </form>
</template>

<script setup lang="ts">
import type { Subscription, PaySubscriptionRequest } from '~/types/pockito'
import { usePockitoWallets } from '~/composables/usePockitoWallets'
import { usePockitoSubscriptions } from '~/composables/usePockitoSubscriptions'

const props = defineProps<{
  subscription: Subscription
}>()

const emit = defineEmits<{
  completed: []
  cancelled: []
}>()

const { t } = useI18n()
const { formatCurrency } = useFormatting()
const toast = useToast()
const { wallets, loadWallets } = usePockitoWallets()
const { paySubscription } = usePockitoSubscriptions()

const selectedWallet = computed(() => {
  if (!form.walletId) return null
  return wallets.value.find(w => w.id === form.walletId)
})

type PayFormState = {
  walletId: string | null
  exchangeRate: number
  skip: boolean
}

const form = reactive<PayFormState>({
  walletId: null,
  exchangeRate: 1,
  skip: false
})
const errors = reactive<Record<string, string>>({})
const submitting = ref(false)

const showExchangeRate = computed(() => {
  if (!form.walletId) return false
  const wallet = wallets.value.find(w => w.id === form.walletId)
  return wallet && wallet.currency !== props.subscription.currency
})

onMounted(async () => {
  await loadWallets()
  const defaultWallet = wallets.value.find(w => w.id === props.subscription.defaultWalletId) || wallets.value.find(w => w.isDefault)
  if (defaultWallet) {
    form.walletId = defaultWallet.id
  }
})

watch(
  () => form.walletId,
  () => {
    if (!showExchangeRate.value) {
      form.exchangeRate = 1
    }
  }
)

const validate = () => {
  Object.keys(errors).forEach((key) => delete errors[key])
  if (!form.skip && !form.walletId) {
    errors.walletId = t('subscriptions.form.errors.defaultWalletRequired') || 'Wallet required'
  }
  if (showExchangeRate.value && (!form.exchangeRate || form.exchangeRate < 0.0001)) {
    errors.exchangeRate = t('subscriptions.form.errors.exchangeRateMin') || 'Exchange rate required'
  }
  return Object.keys(errors).length === 0
}

const handleSubmit = async () => {
  if (!validate()) return
  submitting.value = true
  try {
    const payload: PaySubscriptionRequest = {
      walletId: form.skip ? null : form.walletId || null,
      exchangeRate: showExchangeRate.value ? form.exchangeRate : 1,
      skip: form.skip || undefined
    }
    await paySubscription(props.subscription.id, payload)
    toast.success(t('subscriptions.pay.success') || 'Payment recorded')
    emit('completed')
  } catch (err) {
    toast.error((err as Error)?.message || (t('subscriptions.pay.error') as string) || 'Unable to process payment')
  } finally {
    submitting.value = false
  }
}
</script>
