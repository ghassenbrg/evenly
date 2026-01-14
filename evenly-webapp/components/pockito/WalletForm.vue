<template>
  <form class="space-y-5" @submit.prevent="handleSubmit">
    <!-- Basic Information Section -->
    <div class="space-y-4">
      <h3 class="text-sm font-semibold text-white/90 uppercase tracking-wide">{{ t('wallets.form.basicInfo') || 'Basic Information' }}</h3>
      
      <div class="space-y-3">
        <div class="space-y-2">
          <label class="text-sm font-medium text-white/90 flex items-center gap-1">
            {{ t('wallets.name') || 'Name' }}
            <span class="text-red-400">*</span>
          </label>
          <input
            v-model="form.name"
            type="text"
            class="w-full bg-slate-800/50 border transition-colors rounded-2xl px-4 py-4 text-base text-white placeholder:text-white/40 focus:outline-none focus:ring-2 focus:ring-emerald-500/50 focus:border-emerald-500/50 min-h-[56px] touch-manipulation"
            :class="errors.name ? 'border-red-500/50' : 'border-slate-700/50'"
            maxlength="100"
            placeholder="e.g. Main Wallet"
            required
          />
          <p v-if="errors.name" class="text-xs text-red-400 flex items-center gap-1">
            <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            {{ errors.name }}
          </p>
        </div>

        <div class="grid grid-cols-2 gap-3">
          <div class="space-y-2">
            <label class="text-sm font-medium text-white/90 flex items-center gap-1">
              {{ t('wallets.currency') || 'Currency' }}
              <span class="text-red-400">*</span>
            </label>
            <select
              v-model="form.currency"
              class="w-full bg-slate-800/50 border transition-colors rounded-2xl px-4 py-4 text-base text-white focus:outline-none focus:ring-2 focus:ring-emerald-500/50 focus:border-emerald-500/50 min-h-[56px] touch-manipulation"
              :class="errors.currency ? 'border-red-500/50' : 'border-slate-700/50'"
              required
            >
              <option value="" disabled>{{ t('wallets.selectCurrency') || 'Select currency' }}</option>
              <option v-for="code in currencyOptions" :key="code" :value="code">
                {{ code }}
              </option>
            </select>
            <p v-if="errors.currency" class="text-xs text-red-400">{{ errors.currency }}</p>
          </div>

          <div class="space-y-2">
            <label class="text-sm font-medium text-white/90 flex items-center gap-1">
              {{ t('wallets.type') || 'Type' }}
              <span class="text-red-400">*</span>
            </label>
            <select
              v-model="form.type"
              class="w-full bg-slate-800/50 border transition-colors rounded-2xl px-4 py-4 text-base text-white focus:outline-none focus:ring-2 focus:ring-emerald-500/50 focus:border-emerald-500/50 min-h-[56px] touch-manipulation"
              :class="errors.type ? 'border-red-500/50' : 'border-slate-700/50'"
              required
            >
              <option value="" disabled>{{ t('wallets.selectType') || 'Select type' }}</option>
              <option v-for="type in walletTypes" :key="type" :value="type">
                {{ type }}
              </option>
            </select>
            <p v-if="errors.type" class="text-xs text-red-400">{{ errors.type }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Financial Settings Section -->
    <div class="space-y-4">
      <h3 class="text-sm font-semibold text-white/90 uppercase tracking-wide">{{ t('wallets.form.financial') || 'Financial Settings' }}</h3>
      
      <div class="space-y-3">
        <div class="space-y-2">
          <AmountInput
            v-model="form.initialBalance"
            :currency="form.currency"
            :label="t('wallets.initialBalance') || 'Initial balance'"
            :hint="errors.initialBalance"
            placeholder="0.00"
            :input-class="`w-full pl-8 pr-4 py-3 bg-slate-800/50 border transition-colors rounded-xl text-white placeholder:text-white/40 focus:outline-none focus:ring-2 focus:ring-emerald-500/50 focus:border-emerald-500/50 ${errors.initialBalance ? 'border-red-500/50' : 'border-slate-700/50'}`"
          />
          <p v-if="!errors.initialBalance" class="text-xs text-white/50">{{ t('wallets.form.initialBalanceHelp') || 'Starting balance for this wallet' }}</p>
        </div>
      </div>
    </div>

    <!-- Appearance Section -->
    <div class="space-y-4">
      <h3 class="text-sm font-semibold text-white/90 uppercase tracking-wide">{{ t('wallets.form.appearance') || 'Appearance' }}</h3>
      
      <div class="space-y-3">
        <div class="grid grid-cols-2 gap-3">
          <div class="space-y-2">
            <label class="text-sm font-medium text-white/90">{{ t('wallets.color') || 'Color' }}</label>
            <div class="flex items-center gap-3">
              <input
                v-model="form.color"
                type="color"
                class="w-16 h-12 rounded-xl border border-slate-700/50 cursor-pointer"
              />
              <div class="flex-1 h-12 rounded-xl border border-slate-700/50 flex items-center justify-center" :style="{ background: form.color }">
                <span class="text-xs text-white/80 font-medium">{{ form.color }}</span>
              </div>
            </div>
          </div>

          <div class="space-y-2">
            <label class="text-sm font-medium text-white/90">{{ t('wallets.icon') || 'Icon URL' }}</label>
            <input
              v-model="form.iconUrl"
              type="url"
              class="w-full bg-slate-800/50 border transition-colors rounded-xl px-4 py-3 text-white placeholder:text-white/40 focus:outline-none focus:ring-2 focus:ring-emerald-500/50 focus:border-emerald-500/50"
              :class="errors.iconUrl ? 'border-red-500/50' : 'border-slate-700/50'"
              placeholder="https://example.com/icon.png"
            />
            <p v-if="errors.iconUrl" class="text-xs text-red-400">{{ errors.iconUrl }}</p>
            <p v-else class="text-xs text-white/50">{{ t('wallets.form.iconUrlHelp') || 'Optional: URL to wallet icon' }}</p>
          </div>
        </div>

        <div class="space-y-2">
          <label class="text-sm font-medium text-white/90">{{ t('wallets.description') || 'Description' }}</label>
          <textarea
            v-model="form.description"
            class="w-full bg-slate-800/50 border transition-colors rounded-2xl px-4 py-4 text-base text-white placeholder:text-white/40 focus:outline-none focus:ring-2 focus:ring-emerald-500/50 focus:border-emerald-500/50 resize-none min-h-[56px] touch-manipulation"
            :class="errors.description ? 'border-red-500/50' : 'border-slate-700/50'"
            rows="3"
            maxlength="500"
            placeholder="Optional description..."
          />
          <div class="flex items-center justify-between">
            <p v-if="errors.description" class="text-xs text-red-400">{{ errors.description }}</p>
            <p v-else class="text-xs text-white/50">{{ (form.description?.length || 0) }}/500</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Options Section -->
    <div class="space-y-3">
      <label class="flex items-center gap-3 p-3 rounded-xl border border-slate-700/50 hover:border-slate-600/50 transition-colors cursor-pointer group">
        <input
          v-model="form.isDefault"
          type="checkbox"
          class="w-5 h-5 rounded border-slate-600 bg-slate-800/50 text-emerald-500 focus:ring-2 focus:ring-emerald-500/50 cursor-pointer"
        />
        <div class="flex-1">
          <span class="text-sm font-medium text-white/90 block">{{ t('wallets.makeDefault') || 'Set as default wallet' }}</span>
          <span class="text-xs text-white/50">{{ t('wallets.form.defaultHelp') || 'Use this wallet for new transactions by default' }}</span>
        </div>
      </label>
    </div>

    <!-- Action Buttons -->
    <div class="flex gap-3 pt-4 pb-safe">
      <button
        type="button"
        class="flex-1 h-14 rounded-2xl border-2 border-slate-700/50 text-white/90 hover:text-white hover:border-slate-600/50 hover:bg-white/5 active:bg-white/10 active:scale-95 transition-all font-semibold touch-manipulation"
        @click="emit('cancelled')"
      >
        {{ t('common.cancel') }}
      </button>
      <button
        type="submit"
        class="flex-1 h-14 rounded-2xl bg-emerald-500 text-slate-900 font-bold hover:bg-emerald-400 active:bg-emerald-600 active:scale-95 disabled:opacity-50 disabled:cursor-not-allowed disabled:active:scale-100 transition-all shadow-lg shadow-emerald-500/20 flex items-center justify-center gap-2 touch-manipulation"
        :disabled="submitting"
      >
        <svg v-if="!submitting" class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
        </svg>
        <svg v-else class="animate-spin h-5 w-5" fill="none" viewBox="0 0 24 24">
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
          <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
        </svg>
        {{ submitting ? (t('common.saving') || 'Saving...') : (props.wallet ? t('wallets.update') || 'Update' : t('wallets.create') || 'Create') }}
      </button>
    </div>
  </form>
</template>

<script setup lang="ts">
import type { Wallet, WalletRequest } from '~/types/pockito'
import { Currency, WalletType } from '~/types/pockito'
import { usePockitoWallets } from '~/composables/usePockitoWallets'
import { usePockitoUsers } from '~/composables/usePockitoUsers'
import { useCurrencies } from '~/composables/useCurrencies'
import AmountInput from '~/components/AmountInput.vue'

const props = defineProps<{
  wallet?: Wallet | null
}>()

const emit = defineEmits<{
  saved: [wallet: Wallet]
  cancelled: []
}>()

const { t } = useI18n()
const toast = useToast()
const { createWallet, updateWallet } = usePockitoWallets()
const { getOrCreateCurrentUser, currentUser } = usePockitoUsers()

const currencyOptions = Object.values(Currency)
const walletTypes = Object.values(WalletType)

const form = reactive<WalletRequest>({
  name: '',
  description: '',
  color: '#0ea5e9',
  initialBalance: 0,
  currency: Currency.USD as Currency, // Will be set from user defaultCurrency
  iconUrl: '',
  goalAmount: 0,
  type: WalletType.CASH,
  isDefault: false
})

const errors = reactive<Record<string, string>>({})
const submitting = ref(false)

const setDefaultsFromUser = () => {
  if (!props.wallet) {
    if (currentUser.value?.defaultCurrency) {
      form.currency = currentUser.value.defaultCurrency
    } else {
      // If no user currency, fetch currencies from API and use first available
      const { currencies, fetchCurrencies } = useCurrencies()
      fetchCurrencies().then(() => {
        if (currencies.value.length > 0) {
          form.currency = currencies.value[0].code as Currency
        }
      })
    }
  }
}

onMounted(async () => {
  if (!currentUser.value) {
    try {
      await getOrCreateCurrentUser()
    } catch {
      // user fetch failure is non-blocking for the form
    }
  }
  setDefaultsFromUser()
  if (props.wallet) {
    patchForm(props.wallet)
  }
})

watch(
  () => props.wallet,
  (wallet) => {
    if (wallet) {
      patchForm(wallet)
    } else {
      resetForm()
      setDefaultsFromUser()
    }
  }
)

const patchForm = (wallet: Wallet) => {
  form.name = wallet.name
  form.description = wallet.description || ''
  form.color = wallet.color || '#0ea5e9'
  form.initialBalance = wallet.initialBalance
  form.currency = wallet.currency
  form.iconUrl = wallet.iconUrl || ''
  form.goalAmount = wallet.goalAmount || 0
  form.type = wallet.type
  form.isDefault = wallet.isDefault
}

const resetForm = () => {
  form.name = ''
  form.description = ''
  form.color = '#0ea5e9'
  form.initialBalance = 0
  // Currency will be set from user's defaultCurrency in setDefaultsFromUser
  form.currency = Currency.USD as Currency
  form.iconUrl = ''
  form.goalAmount = 0
  form.type = WalletType.CASH
  form.isDefault = false
  Object.keys(errors).forEach((key) => delete errors[key])
}

const validate = () => {
  Object.keys(errors).forEach((key) => delete errors[key])
  if (!form.name?.trim()) {
    errors.name = t('wallets.form.errors.nameRequired') || 'Name is required'
  }
  if (!form.currency) {
    errors.currency = t('wallets.form.errors.currencyRequired') || 'Currency is required'
  }
  if (!form.type) {
    errors.type = t('wallets.form.errors.typeRequired') || 'Type is required'
  }
  if (form.initialBalance < 0) {
    errors.initialBalance = t('wallets.form.errors.initialBalanceMin') || 'Balance must be 0 or more'
  }
  if (form.iconUrl && !/^https?:\/\/.+\.(jpg|jpeg|png|gif|svg|webp)(\?.*)?$/i.test(form.iconUrl)) {
    errors.iconUrl = t('wallets.form.errors.iconUrlPattern') || 'Enter a valid image URL'
  }
  if (form.description && form.description.length > 500) {
    errors.description = t('wallets.form.errors.descriptionMax') || 'Description too long'
  }
  return Object.keys(errors).length === 0
}

const handleSubmit = async () => {
  if (!validate()) return
  submitting.value = true
  try {
    const payload: WalletRequest = { ...form }
    let saved: Wallet
    if (props.wallet) {
      saved = await updateWallet(props.wallet.id, payload)
      toast.success(t('wallets.updateWalletSuccess') || 'Wallet updated')
    } else {
      saved = await createWallet(payload)
      toast.success(t('wallets.createWalletSuccess') || 'Wallet created')
    }
    emit('saved', saved)
  } catch (err) {
    toast.error((err as Error)?.message || (t('wallets.saveFailed') as string) || 'Unable to save wallet')
  } finally {
    submitting.value = false
  }
}
</script>
