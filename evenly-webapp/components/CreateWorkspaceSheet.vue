<template>
  <!-- Create Workspace Sheet -->
  <BottomSheet :model-value="modelValue" @update:model-value="(val) => emit('update:modelValue', val)" :title="t('workspace.create')">
    <form @submit.prevent="handleSubmit" class="space-y-4">
      <div>
        <label class="block text-sm font-medium text-slate-300 mb-2">{{ t('workspace.name') }}</label>
        <input
          v-model="form.name"
          type="text"
          required
          class="w-full px-4 py-3 bg-slate-800 border border-slate-700 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-emerald-500"
          :placeholder="t('workspace.namePlaceholder')"
        />
      </div>

      <div>
        <label class="block text-sm font-medium text-slate-300 mb-2">{{ t('workspace.splitMode') }}</label>
        <div class="grid grid-cols-2 gap-3">
          <button
            type="button"
            @click="form.defaultSplitMode = 'EQUAL'"
            :class="form.defaultSplitMode === 'EQUAL' ? 'bg-emerald-500 text-white' : 'bg-slate-800 text-slate-300'"
            class="px-4 py-3 rounded-xl font-medium transition-colors"
          >
            {{ t('workspace.splitModeEqual') }}
          </button>
          <button
            type="button"
            @click="form.defaultSplitMode = 'WEIGHTED'"
            :class="form.defaultSplitMode === 'WEIGHTED' ? 'bg-emerald-500 text-white' : 'bg-slate-800 text-slate-300'"
            class="px-4 py-3 rounded-xl font-medium transition-colors"
          >
            {{ t('workspace.splitModeWeighted') }}
          </button>
        </div>
      </div>

      <div>
        <label class="block text-sm font-medium text-slate-300 mb-2">{{ t('workspace.currency') }}</label>
        <select
          v-model="form.currency"
          required
          class="w-full px-4 py-3 bg-slate-800 border border-slate-700 rounded-xl text-white focus:outline-none focus:ring-2 focus:ring-emerald-500"
        >
          <option value="" disabled>{{ t('workspace.selectCurrency') }}</option>
          <option v-for="currency in currencies" :key="currency.code" :value="currency.code">
            {{ currency.code }} - {{ getCurrencyName(currency.code) }}
          </option>
        </select>
      </div>

      <div>
        <AmountInput
          v-model="form.monthlySharedLimit"
          :currency="form.currency || ''"
          :label="`${t('workspace.monthlyBudget')} (${t('common.optional')})`"
          :placeholder="t('workspace.monthlyBudgetPlaceholder')"
        />
      </div>
    </form>

    <template #footer>
      <div class="flex space-x-3">
        <button
          @click="emit('update:modelValue', false)"
          class="flex-1 bg-slate-700 hover:bg-slate-600 text-white font-medium py-3 rounded-xl transition-colors"
        >
          {{ t('common.cancel') }}
        </button>
        <button
          @click="handleSubmit"
          :disabled="loading"
          class="btn btn-green flex-1 py-3"
        >
          <span v-if="!loading">{{ t('common.create') }}</span>
          <span v-else>{{ t('workspace.creating') }}</span>
        </button>
      </div>
    </template>
  </BottomSheet>

  <!-- Invitation Sheet -->
  <BottomSheet 
    :model-value="showInviteSheet" 
    @update:model-value="showInviteSheet = $event"
    :title="t('workspace.inviteTitle')"
  >
    <div class="space-y-4">
      <p class="text-slate-300 text-sm">{{ t('workspace.inviteDescription') }}</p>
      
      <!-- Invite Code -->
      <div>
        <label class="block text-sm font-medium text-slate-300 mb-2">{{ t('workspace.inviteCode') }}</label>
        <div class="flex gap-2">
          <input
            :value="inviteCode"
            readonly
            class="flex-1 px-4 py-3 bg-slate-800 border border-slate-700 rounded-xl text-white font-mono text-lg text-center"
          />
          <button
            @click="copyInviteCode"
            class="px-4 py-3 bg-emerald-500 hover:bg-emerald-600 text-white rounded-xl font-medium transition-colors"
          >
            {{ t('common.copy') || 'Copy' }}
          </button>
        </div>
      </div>

      <!-- Invite Link -->
      <div>
        <label class="block text-sm font-medium text-slate-300 mb-2">{{ t('workspace.inviteLink') }}</label>
        <div class="flex gap-2">
          <input
            :value="inviteLink"
            readonly
            class="flex-1 px-4 py-3 bg-slate-800 border border-slate-700 rounded-xl text-white text-sm"
          />
          <button
            @click="copyInviteLink"
            class="px-4 py-3 bg-emerald-500 hover:bg-emerald-600 text-white rounded-xl font-medium transition-colors"
          >
            {{ t('common.copy') || 'Copy' }}
          </button>
        </div>
      </div>
    </div>

    <template #footer>
      <button
        @click="handleInviteSheetDone"
        class="w-full bg-emerald-500 hover:bg-emerald-600 text-white font-medium py-3 rounded-xl transition-colors"
      >
        {{ t('common.done') || 'Done' }}
      </button>
    </template>
  </BottomSheet>
</template>

<script setup lang="ts">
import type { CreateWorkspaceRequest, Invite } from '~/types/api'
import AmountInput from '~/components/AmountInput.vue'
import { useCurrencies } from '~/composables/useCurrencies'
import { useInvites } from '~/composables/useInvites'
import { useAuth } from '~/composables/useAuth'

interface Props {
  modelValue: boolean
}

const props = defineProps<Props>()
const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  created: []
}>()

const workspacesStore = useWorkspacesStore()
const { success, error } = useToast()
const { currencies, fetchCurrencies } = useCurrencies()
const { createInvite } = useInvites()
const { user } = useAuth()

const loading = ref(false)
const showInviteSheet = ref(false)
const createdWorkspaceId = ref<string | null>(null)
const inviteCode = ref<string | null>(null)
const inviteLink = ref<string | null>(null)

const form = ref<CreateWorkspaceRequest>({
  name: '',
  defaultSplitMode: 'EQUAL',
  monthlySharedLimit: null,
  currency: ''
})

// Fetch currencies when sheet opens
watch(() => props.modelValue, async (newVal) => {
  if (newVal) {
    form.value = {
      name: '',
      defaultSplitMode: 'EQUAL',
      monthlySharedLimit: null,
      currency: user.value?.preferredCurrency || ''
    }
    await fetchCurrencies()
    // Set default currency from user preference or first available currency
    if (!form.value.currency && currencies.value.length > 0) {
      form.value.currency = user.value?.preferredCurrency || currencies.value[0].code
    }
  } else {
    // Only reset if invite sheet is not showing
    if (!showInviteSheet.value) {
      inviteCode.value = null
      inviteLink.value = null
      createdWorkspaceId.value = null
    }
  }
})

const { t } = useI18n()

const getCurrencyName = (code: string) => {
  return t(`currencies.${code}`) || code
}

const handleSubmit = async () => {
  if (!form.value.currency) {
    error(t('workspace.currencyRequired') || 'Please select a currency')
    return
  }

  try {
    loading.value = true
    // Convert 0 to null for optional monthlySharedLimit
    const payload = {
      ...form.value,
      monthlySharedLimit: form.value.monthlySharedLimit === 0 ? null : form.value.monthlySharedLimit
    }
    const workspace = await workspacesStore.createWorkspace(payload)
    createdWorkspaceId.value = workspace.id
    
    // Create an invite for the workspace
    try {
      const invite = await createInvite(workspace.id, {
        maxUses: 0, // Unlimited uses
        expiresInDays: 30
      })
      inviteCode.value = invite.code
      inviteLink.value = `${window.location.origin}/join?code=${invite.code}`
    } catch (inviteErr: any) {
      // If invite creation fails, still show success but without invite
      console.warn('Failed to create invite:', inviteErr)
    }
    
    success(t('workspace.created'))
    
    // Show invite sheet before closing create sheet
    if (inviteCode.value && inviteLink.value) {
      showInviteSheet.value = true
    } else {
      // If no invite was created, just close and emit
      emit('update:modelValue', false)
      emit('created')
    }
  } catch (err: any) {
    error(err.message || t('workspace.createFailed'))
  } finally {
    loading.value = false
  }
}

const copyInviteCode = async () => {
  if (inviteCode.value) {
    await navigator.clipboard.writeText(inviteCode.value)
    success(t('workspace.inviteCodeCopied') || 'Invite code copied!')
  }
}

const copyInviteLink = async () => {
  if (inviteLink.value) {
    await navigator.clipboard.writeText(inviteLink.value)
    success(t('workspace.inviteLinkCopied') || 'Invite link copied!')
  }
}

const handleInviteSheetDone = () => {
  showInviteSheet.value = false
  emit('update:modelValue', false)
  emit('created')
  // Reset form and invite data
  inviteCode.value = null
  inviteLink.value = null
  createdWorkspaceId.value = null
}
</script>

