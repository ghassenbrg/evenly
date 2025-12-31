<template>
  <div class="p-4 space-y-6">
    <!-- Account Section -->
    <div class="bg-slate-800 rounded-2xl p-4 space-y-4">
      <h2 class="text-white font-semibold text-lg">Account</h2>
      <div v-if="currentUser" class="space-y-2">
        <div>
          <p class="text-slate-400 text-sm">Name</p>
          <p class="text-white font-medium">{{ currentUser.displayName }}</p>
        </div>
        <div>
          <p class="text-slate-400 text-sm">Email</p>
          <p class="text-white font-medium">{{ currentUser.email }}</p>
        </div>
      </div>
      <button
        @click="handleLogout"
        class="w-full bg-red-500 hover:bg-red-600 text-white font-medium py-3 rounded-xl transition-colors"
      >
        Logout
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({
  middleware: 'auth'
})

const authStore = useAuthStore()
const { currentUser } = storeToRefs(authStore)
const router = useRouter()

const handleLogout = () => {
  authStore.logout()
  router.push('/login')
}
</script>

