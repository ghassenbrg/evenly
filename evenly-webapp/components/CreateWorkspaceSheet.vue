<template>
  <BottomSheet :model-value="modelValue" @update:model-value="(val) => emit('update:modelValue', val)" title="Create Workspace">
    <form @submit.prevent="handleSubmit" class="space-y-4">
      <div>
        <label class="block text-sm font-medium text-slate-300 mb-2">Workspace Name</label>
        <input
          v-model="form.name"
          type="text"
          required
          class="w-full px-4 py-3 bg-slate-800 border border-slate-700 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-emerald-500"
          placeholder="e.g. Apartment Share"
        />
      </div>

      <div>
        <label class="block text-sm font-medium text-slate-300 mb-2">Split Mode</label>
        <div class="grid grid-cols-2 gap-3">
          <button
            type="button"
            @click="form.defaultSplitMode = 'EQUAL'"
            :class="form.defaultSplitMode === 'EQUAL' ? 'bg-emerald-500 text-white' : 'bg-slate-800 text-slate-300'"
            class="px-4 py-3 rounded-xl font-medium transition-colors"
          >
            Equal
          </button>
          <button
            type="button"
            @click="form.defaultSplitMode = 'WEIGHTED'"
            :class="form.defaultSplitMode === 'WEIGHTED' ? 'bg-emerald-500 text-white' : 'bg-slate-800 text-slate-300'"
            class="px-4 py-3 rounded-xl font-medium transition-colors"
          >
            Weighted
          </button>
        </div>
      </div>

      <div>
        <label class="block text-sm font-medium text-slate-300 mb-2">Monthly Shared Budget (optional)</label>
        <input
          v-model.number="form.monthlySharedLimit"
          type="number"
          min="0"
          step="1000"
          class="w-full px-4 py-3 bg-slate-800 border border-slate-700 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-emerald-500"
          placeholder="100000"
        />
      </div>
    </form>

    <template #footer>
      <div class="flex space-x-3">
        <button
          @click="emit('update:modelValue', false)"
          class="flex-1 bg-slate-700 hover:bg-slate-600 text-white font-medium py-3 rounded-xl transition-colors"
        >
          Cancel
        </button>
        <button
          @click="handleSubmit"
          :disabled="loading"
          class="flex-1 bg-emerald-500 hover:bg-emerald-600 text-white font-medium py-3 rounded-xl transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
        >
          <span v-if="!loading">Create</span>
          <span v-else>Creating...</span>
        </button>
      </div>
    </template>
  </BottomSheet>
</template>

<script setup lang="ts">
import type { CreateWorkspaceRequest } from '~/types/api'

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

const loading = ref(false)

const form = ref<CreateWorkspaceRequest>({
  name: '',
  defaultSplitMode: 'EQUAL',
  monthlySharedLimit: null
})

watch(() => props.modelValue, (newVal) => {
  if (newVal) {
    form.value = {
      name: '',
      defaultSplitMode: 'EQUAL',
      monthlySharedLimit: null
    }
  }
})

const handleSubmit = async () => {
  try {
    loading.value = true
    await workspacesStore.createWorkspace(form.value)
    success('Workspace created!')
    emit('created')
    emit('update:modelValue', false)
  } catch (err: any) {
    error(err.message || 'Failed to create workspace')
  } finally {
    loading.value = false
  }
}
</script>

