<template>
  <div class="p-4 space-y-6">
    <!-- Workspace Info -->
    <div v-if="workspace" class="bg-slate-800 rounded-2xl p-4 space-y-4">
      <h2 class="text-white font-semibold text-lg">{{ t('workspace.settings.title') }}</h2>
      <div class="space-y-2">
        <div>
          <p class="text-slate-400 text-sm">{{ t('workspace.name') }}</p>
          <p class="text-white font-medium">{{ workspace.isPersonal ? t('workspace.mySpace') : workspace.name }}</p>
        </div>
        <div v-if="!workspace.isPersonal">
          <p class="text-slate-400 text-sm">{{ t('workspace.splitMode') }}</p>
          <p class="text-white font-medium">
            {{ workspace.defaultSplitMode === 'EQUAL' ? t('workspace.splitModeEqual') : t('workspace.splitModeWeighted') }}
          </p>
        </div>
        <div>
          <p class="text-slate-400 text-sm">{{ t('workspace.currency') }}</p>
          <p class="text-white font-medium">{{ workspace.currency }}</p>
        </div>
        <div v-if="workspace.monthlySharedLimit">
          <p class="text-slate-400 text-sm">{{ t('workspace.monthlyBudget') }}</p>
          <p class="text-white font-medium">{{ formatCurrency(workspace.monthlySharedLimit, workspace.currency || '') }}</p>
        </div>
      </div>
    </div>

    <!-- Edit Workspace Sheet -->
    <BottomSheet :model-value="showEditSheet" @update:model-value="showEditSheet = $event" :title="t('workspace.edit')">
      <form @submit.prevent="handleUpdate" class="space-y-4">
        <!-- Name field - only for non-personal workspaces -->
        <div v-if="!workspace?.isPersonal">
          <label class="block text-sm font-medium text-slate-300 mb-2">{{ t('workspace.name') }}</label>
          <input
            v-model="editForm.name"
            type="text"
            required
            class="w-full px-4 py-3 bg-slate-800 border border-slate-700 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-emerald-500"
            :placeholder="t('workspace.namePlaceholder')"
          />
        </div>

        <!-- Split Mode field - only for non-personal workspaces -->
        <div v-if="!workspace?.isPersonal">
          <label class="block text-sm font-medium text-slate-300 mb-2">{{ t('workspace.splitMode') }}</label>
          <div class="grid grid-cols-2 gap-3">
            <button
              type="button"
              @click="editForm.defaultSplitMode = 'EQUAL'"
              :class="editForm.defaultSplitMode === 'EQUAL' ? 'bg-emerald-500 text-white' : 'bg-slate-800 text-slate-300'"
              class="px-4 py-3 rounded-xl font-medium transition-colors"
            >
              {{ t('workspace.splitModeEqual') }}
            </button>
            <button
              type="button"
              @click="editForm.defaultSplitMode = 'WEIGHTED'"
              :class="editForm.defaultSplitMode === 'WEIGHTED' ? 'bg-emerald-500 text-white' : 'bg-slate-800 text-slate-300'"
              class="px-4 py-3 rounded-xl font-medium transition-colors"
            >
              {{ t('workspace.splitModeWeighted') }}
            </button>
          </div>
        </div>

        <!-- Budget Limit - shown for all workspaces -->
        <div>
          <AmountInput
            v-model="editForm.monthlySharedLimit"
            :currency="workspace?.currency || ''"
            :label="`${t('workspace.monthlyBudget')} (${t('common.optional')})`"
            :placeholder="t('workspace.monthlyBudgetPlaceholder')"
          />
        </div>
      </form>

      <template #footer>
        <div class="flex space-x-3">
          <button
            @click="showEditSheet = false"
            class="flex-1 bg-slate-700 hover:bg-slate-600 text-white font-medium py-3 rounded-xl transition-colors"
          >
            {{ t('common.cancel') }}
          </button>
          <button
            @click="handleUpdate"
            :disabled="updateLoading"
            class="btn btn-green flex-1 py-3"
          >
            <span v-if="!updateLoading">{{ t('common.save') }}</span>
            <span v-else>{{ t('common.saving') }}</span>
          </button>
        </div>
      </template>
    </BottomSheet>

    <!-- Actions -->
    <div v-if="workspace" class="space-y-3">
      <!-- Edit Button - shown for all workspaces -->
      <button
        @click="openEditSheet"
        class="w-full bg-emerald-500 hover:bg-emerald-600 text-white font-medium py-3 rounded-xl transition-colors"
      >
        {{ t('workspace.edit') }}
      </button>

      <!-- Members Section - only for non-personal workspaces -->
      <div v-if="!workspace.isPersonal" class="bg-slate-800 rounded-2xl p-4 space-y-3">
        <div class="flex items-center justify-between">
          <h3 class="text-white font-semibold">{{ t('workspace.members') }}</h3>
          <button
            v-if="isOwner && workspace.defaultSplitMode === 'WEIGHTED'"
            @click="openEditWeightsSheet"
            class="text-emerald-500 hover:text-emerald-400 text-sm font-medium"
          >
            {{ t('workspace.editWeights') }}
          </button>
        </div>
        <div v-if="membersLoading" class="text-slate-400 text-sm">{{ t('common.loading') }}</div>
        <div v-else-if="members.length === 0" class="text-slate-400 text-sm">{{ t('workspace.noMembers') }}</div>
        <div v-else class="space-y-2">
          <div
            v-for="member in members"
            :key="member.userId"
            class="flex items-center justify-between p-3 bg-slate-700 rounded-xl"
          >
            <div>
              <p class="text-white font-medium">{{ member.user?.displayName || member.userId }}</p>
              <p class="text-slate-400 text-sm">
                {{ member.role === 'OWNER' ? t('workspace.owner') : t('workspace.member') }}
                <span v-if="workspace.defaultSplitMode === 'WEIGHTED'">
                  • {{ member.weightPercent }}%
                </span>
              </p>
            </div>
          </div>
        </div>
      </div>

      <!-- Invite Section - only for non-personal workspaces -->
      <div v-if="!workspace.isPersonal" class="bg-slate-800 rounded-2xl p-4 space-y-3">
        <h3 class="text-white font-semibold">{{ t('workspace.invite') }}</h3>
        <p class="text-slate-400 text-sm">{{ t('workspace.inviteDescription') }}</p>
        <button
          @click="handleCreateInvite"
          :disabled="inviteLoading"
          class="w-full bg-white/5 border border-slate-700 hover:border-emerald-500 text-white font-medium py-3 rounded-xl transition-colors"
        >
          <span v-if="!inviteLoading">{{ t('workspace.createInvite') }}</span>
          <span v-else>{{ t('workspace.creating') }}</span>
        </button>
      </div>

      <!-- Invite Code Display Sheet -->
      <BottomSheet 
        :model-value="showInviteSheet" 
        @update:model-value="showInviteSheet = $event"
        :title="t('workspace.inviteTitle')"
      >
        <div class="space-y-4">
          <p class="text-slate-300 text-sm">{{ t('workspace.inviteDescription') }}</p>
          
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
                {{ t('common.copy') }}
              </button>
            </div>
          </div>

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
                {{ t('common.copy') }}
              </button>
            </div>
          </div>
        </div>

        <template #footer>
          <button
            @click="showInviteSheet = false"
            class="w-full bg-emerald-500 hover:bg-emerald-600 text-white font-medium py-3 rounded-xl transition-colors"
          >
            {{ t('common.done') }}
          </button>
        </template>
      </BottomSheet>

      <!-- Delete Button - only for non-personal workspaces -->
      <button
        v-if="!workspace.isPersonal"
        @click="showDeleteConfirm = true"
        class="w-full bg-red-500 hover:bg-red-600 text-white font-medium py-3 rounded-xl transition-colors"
      >
        {{ t('workspace.delete') }}
      </button>
    </div>

    <!-- Edit Weights Sheet -->
    <BottomSheet 
      :model-value="showEditWeightsSheet" 
      @update:model-value="showEditWeightsSheet = $event"
      :title="t('workspace.editWeights')"
    >
      <form @submit.prevent="handleUpdateWeights" class="space-y-4">
        <p class="text-slate-300 text-sm">{{ t('workspace.editWeightsDescription') }}</p>
        <div class="space-y-3">
          <div
            v-for="(weight, index) in weightsForm"
            :key="weight.userId"
            class="space-y-2"
          >
            <label class="block text-sm font-medium text-slate-300">
              {{ members.find(m => m.userId === weight.userId)?.user?.displayName || weight.userId }}
            </label>
            <div class="flex items-center gap-3">
              <input
                v-model.number="weight.weightPercent"
                type="number"
                min="0"
                max="100"
                step="0.1"
                required
                class="flex-1 px-4 py-3 bg-slate-800 border border-slate-700 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-emerald-500"
                :placeholder="t('workspace.weightPlaceholder')"
              />
              <span class="text-slate-400 text-sm w-8">%</span>
            </div>
          </div>
        </div>
        <div v-if="weightsTotal !== 100" class="text-sm" :class="weightsTotal > 100 ? 'text-red-400' : 'text-yellow-400'">
          {{ t('workspace.weightsTotal') }}: {{ weightsTotal.toFixed(1) }}%
          <span v-if="weightsTotal !== 100" class="block mt-1">
            {{ weightsTotal > 100 ? t('workspace.weightsExceed100') : t('workspace.weightsLessThan100') }}
          </span>
        </div>
      </form>

      <template #footer>
        <div class="flex space-x-3">
          <button
            @click="showEditWeightsSheet = false"
            class="flex-1 bg-slate-700 hover:bg-slate-600 text-white font-medium py-3 rounded-xl transition-colors"
          >
            {{ t('common.cancel') }}
          </button>
          <button
            @click="handleUpdateWeights"
            :disabled="updateWeightsLoading || weightsTotal !== 100"
            class="flex-1 btn btn-green py-3"
          >
            <span v-if="!updateWeightsLoading">{{ t('common.save') }}</span>
            <span v-else>{{ t('common.saving') }}</span>
          </button>
        </div>
      </template>
    </BottomSheet>

    <!-- Delete Confirmation Sheet -->
    <BottomSheet 
      :model-value="showDeleteConfirm" 
      @update:model-value="showDeleteConfirm = $event"
      :title="t('workspace.deleteConfirm')"
    >
      <div class="space-y-4">
        <p class="text-slate-300">
          {{ t('workspace.deleteWarning') }}
        </p>
      </div>

      <template #footer>
        <div class="flex space-x-3">
          <button
            @click="showDeleteConfirm = false"
            class="flex-1 bg-slate-700 hover:bg-slate-600 text-white font-medium py-3 rounded-xl transition-colors"
          >
            {{ t('common.cancel') }}
          </button>
          <button
            @click="handleDelete"
            :disabled="deleteLoading"
            class="flex-1 bg-red-500 hover:bg-red-600 text-white font-medium py-3 rounded-xl transition-colors"
          >
            <span v-if="!deleteLoading">{{ t('workspace.delete') }}</span>
            <span v-else>{{ t('workspace.deleting') }}</span>
          </button>
        </div>
      </template>
    </BottomSheet>

  </div>
</template>

<script setup lang="ts">
definePageMeta({
  middleware: 'auth',
  isMainPage: true
})

import type { UpdateWorkspaceSettingsRequest, CreateInviteRequest, UpdateMemberWeightsRequest } from '~/types/api'
import AmountInput from '~/components/AmountInput.vue'
import { useWorkspaceMembers } from '~/composables/useWorkspaceMembers'
import { useInvites } from '~/composables/useInvites'
import { useFormatting } from '~/composables/useFormatting'
import { useAuth } from '~/composables/useAuth'

const { t } = useI18n()
const workspacesStore = useWorkspacesStore()
const { activeWorkspace } = storeToRefs(workspacesStore)
const { success, error } = useToast()
const { members, loading: membersLoading, fetchMembers } = useWorkspaceMembers()
const { createInvite } = useInvites()
const { formatCurrency } = useFormatting()
const { getCurrentUserId } = useAuth()
const router = useRouter()

const workspace = computed(() => activeWorkspace.value)
const showEditSheet = ref(false)
const showInviteSheet = ref(false)
const showDeleteConfirm = ref(false)
const showEditWeightsSheet = ref(false)
const updateLoading = ref(false)
const inviteLoading = ref(false)
const deleteLoading = ref(false)
const updateWeightsLoading = ref(false)
const inviteCode = ref<string | null>(null)
const inviteLink = ref<string | null>(null)

const isOwner = computed(() => {
  if (!workspace.value || !members.value.length) return false
  const currentUserId = getCurrentUserId()
  if (!currentUserId) return false
  const currentMember = members.value.find(m => m.userId === currentUserId)
  return currentMember?.role === 'OWNER'
})

const weightsForm = ref<Array<{ userId: string; weightPercent: number }>>([])

const weightsTotal = computed(() => {
  return weightsForm.value.reduce((sum, w) => sum + (w.weightPercent || 0), 0)
})

const editForm = ref<UpdateWorkspaceSettingsRequest>({
  name: '',
  defaultSplitMode: 'EQUAL',
  monthlySharedLimit: null
})

// Fetch members when workspace changes
watch(workspace, async (newWorkspace) => {
  if (newWorkspace && !newWorkspace.isPersonal) {
    await fetchMembers(newWorkspace.id)
  }
}, { immediate: true })

const openEditSheet = () => {
  if (!workspace.value) return
  editForm.value = {
    name: workspace.value.isPersonal ? t('workspace.mySpace') : workspace.value.name,
    defaultSplitMode: workspace.value.defaultSplitMode as 'EQUAL' | 'WEIGHTED',
    monthlySharedLimit: workspace.value.monthlySharedLimit
  }
  showEditSheet.value = true
}

const handleUpdate = async () => {
  if (!workspace.value) return

  try {
    updateLoading.value = true
    // For personal workspaces, only send monthlySharedLimit
    const payload: UpdateWorkspaceSettingsRequest = workspace.value.isPersonal
      ? {
          monthlySharedLimit: editForm.value.monthlySharedLimit === 0 ? null : editForm.value.monthlySharedLimit
        }
      : {
          ...editForm.value,
          monthlySharedLimit: editForm.value.monthlySharedLimit === 0 ? null : editForm.value.monthlySharedLimit
        }
    await workspacesStore.updateWorkspaceSettings(workspace.value.id, payload)
    success(t('workspace.updated'))
    showEditSheet.value = false
  } catch (err: any) {
    error(err.message || t('workspace.updateFailed'))
  } finally {
    updateLoading.value = false
  }
}

const handleCreateInvite = async () => {
  if (!workspace.value) return

  try {
    inviteLoading.value = true
    const invite = await createInvite(workspace.value.id, {
      maxUses: 0, // Unlimited uses
      expiresInDays: 30
    })
    inviteCode.value = invite.code
    inviteLink.value = `${window.location.origin}/join?code=${invite.code}`
    showInviteSheet.value = true
    success(t('workspace.inviteCreated'))
  } catch (err: any) {
    error(err.message || t('workspace.inviteFailed'))
  } finally {
    inviteLoading.value = false
  }
}

const copyInviteCode = async () => {
  if (inviteCode.value) {
    await navigator.clipboard.writeText(inviteCode.value)
    success(t('workspace.inviteCodeCopied'))
  }
}

const copyInviteLink = async () => {
  if (inviteLink.value) {
    await navigator.clipboard.writeText(inviteLink.value)
    success(t('workspace.inviteLinkCopied'))
  }
}

const openEditWeightsSheet = () => {
  if (!workspace.value || !members.value.length) return
  weightsForm.value = members.value.map(m => ({
    userId: m.userId,
    weightPercent: m.weightPercent
  }))
  showEditWeightsSheet.value = true
}

const handleUpdateWeights = async () => {
  if (!workspace.value || weightsTotal.value !== 100) return

  try {
    updateWeightsLoading.value = true
    const payload: UpdateMemberWeightsRequest = {
      weights: weightsForm.value.map(w => ({
        userId: w.userId,
        weightPercent: w.weightPercent
      }))
    }
    await workspacesStore.updateMemberWeights(workspace.value.id, payload)
    success(t('workspace.weightsUpdated'))
    showEditWeightsSheet.value = false
    // Refresh members to show updated weights
    await fetchMembers(workspace.value.id)
  } catch (err: any) {
    error(err.message || t('workspace.weightsUpdateFailed'))
  } finally {
    updateWeightsLoading.value = false
  }
}

const handleDelete = async () => {
  if (!workspace.value) return

  try {
    deleteLoading.value = true
    await workspacesStore.deleteWorkspace(workspace.value.id)
    success(t('workspace.deleted'))
    showDeleteConfirm.value = false
    
    // Navigate to dashboard after deletion
    await router.push('/dashboard')
  } catch (err: any) {
    error(err.message || t('workspace.deleteFailed'))
  } finally {
    deleteLoading.value = false
  }
}
</script>
