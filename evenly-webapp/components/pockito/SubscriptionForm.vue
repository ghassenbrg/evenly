<template>
  <form class="space-y-5" @submit.prevent="handleSubmit">
    <!-- Basic Information -->
    <div class="space-y-4">
      <h3 class="text-sm font-semibold text-white/90 uppercase tracking-wide flex items-center gap-2">
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
        </svg>
        {{ t('subscriptions.form.basicInfo') || 'Basic Information' }}
      </h3>
      
      <div class="space-y-3">
        <div class="space-y-2">
          <label class="text-sm font-medium text-white/90 flex items-center gap-2">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z" />
            </svg>
            {{ t('subscriptions.name') || 'Name' }}
            <span class="text-red-400">*</span>
          </label>
          <input
            v-model="form.name"
            type="text"
            class="w-full bg-slate-800/50 border transition-colors rounded-2xl px-4 py-4 text-base text-white placeholder:text-white/40 focus:outline-none focus:ring-2 focus:ring-emerald-500/50 focus:border-emerald-500/50 min-h-[56px] touch-manipulation"
            :class="errors.name ? 'border-red-500/50' : 'border-slate-700/50'"
            maxlength="100"
            placeholder="e.g. Netflix, Spotify"
            required
          />
          <p v-if="errors.name" class="text-xs text-red-400 flex items-center gap-1">
            <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            {{ errors.name }}
          </p>
        </div>

        <div class="space-y-2">
          <label class="text-sm font-medium text-white/90 flex items-center gap-2">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
            </svg>
            {{ t('subscriptions.icon') || 'Icon URL' }}
            <span class="text-xs text-white/50 font-normal">({{ t('common.optional') }})</span>
          </label>
          <input
            v-model="form.iconUrl"
            type="url"
            class="w-full bg-slate-800/50 border transition-colors rounded-xl px-4 py-3 text-white placeholder:text-white/40 focus:outline-none focus:ring-2 focus:ring-emerald-500/50 focus:border-emerald-500/50"
            :class="errors.iconUrl ? 'border-red-500/50' : 'border-slate-700/50'"
            placeholder="https://example.com/icon.png"
          />
          <p v-if="errors.iconUrl" class="text-xs text-red-400">{{ errors.iconUrl }}</p>
          <p v-else class="text-xs text-white/50">{{ t('subscriptions.form.iconUrlHelp') || 'Optional: URL to subscription icon' }}</p>
        </div>
      </div>
    </div>

    <!-- Payment Details -->
    <div class="space-y-4">
      <h3 class="text-sm font-semibold text-white/90 uppercase tracking-wide flex items-center gap-2">
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
        {{ t('subscriptions.form.payment') || 'Payment Details' }}
      </h3>
      
      <div class="grid grid-cols-2 gap-3">
        <div class="space-y-2">
          <AmountInput
            v-model="form.amount"
            :currency="form.currency"
            :label="t('subscriptions.amount') || 'Amount'"
            :hint="errors.amount"
            placeholder="0.00"
            :input-class="`w-full pl-8 pr-4 py-3 bg-slate-800/50 border transition-colors rounded-xl text-white placeholder:text-white/40 focus:outline-none focus:ring-2 focus:ring-emerald-500/50 focus:border-emerald-500/50 ${errors.amount ? 'border-red-500/50' : 'border-slate-700/50'}`"
          />
        </div>
        <div class="space-y-2">
          <label class="text-sm font-medium text-white/90 flex items-center gap-2">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z" />
            </svg>
            {{ t('subscriptions.currency') || 'Currency' }}
            <span class="text-red-400">*</span>
          </label>
          <select
            v-model="form.currency"
            class="w-full bg-slate-800/50 border transition-colors rounded-2xl px-4 py-4 text-base text-white focus:outline-none focus:ring-2 focus:ring-emerald-500/50 focus:border-emerald-500/50 min-h-[56px] touch-manipulation"
            :class="errors.currency ? 'border-red-500/50' : 'border-slate-700/50'"
          >
            <option value="" disabled>{{ t('subscriptions.selectCurrency') || 'Select currency' }}</option>
            <option v-for="code in currencyOptions" :key="code" :value="code">
              {{ code }}
            </option>
          </select>
          <p v-if="errors.currency" class="text-xs text-red-400">{{ errors.currency }}</p>
        </div>
      </div>
    </div>

    <!-- Schedule Settings -->
    <div class="space-y-4">
      <h3 class="text-sm font-semibold text-white/90 uppercase tracking-wide flex items-center gap-2">
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
        </svg>
        {{ t('subscriptions.form.schedule') || 'Schedule' }}
      </h3>
      
      <div class="grid grid-cols-2 gap-3">
        <div class="space-y-2">
          <label class="text-sm font-medium text-white/90 flex items-center gap-2">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
            </svg>
            {{ t('subscriptions.frequency') || 'Frequency' }}
            <span class="text-red-400">*</span>
          </label>
          <select
            v-model="form.frequency"
            class="w-full bg-slate-800/50 border transition-colors rounded-2xl px-4 py-4 text-base text-white focus:outline-none focus:ring-2 focus:ring-emerald-500/50 focus:border-emerald-500/50 min-h-[56px] touch-manipulation"
            :class="errors.frequency ? 'border-red-500/50' : 'border-slate-700/50'"
            @change="onFrequencyChange"
          >
            <option v-for="freq in frequencies" :key="freq" :value="freq">
              {{ freq }}
            </option>
          </select>
          <p v-if="errors.frequency" class="text-xs text-red-400">{{ errors.frequency }}</p>
        </div>
        <div class="space-y-2">
          <label class="text-sm font-medium text-white/90 flex items-center gap-2">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 20l4-16m2 16l4-16M6 9h14M4 15h14" />
            </svg>
            {{ t('subscriptions.interval') || 'Interval' }}
            <span class="text-red-400">*</span>
          </label>
          <input
            v-model.number="form.interval"
            type="number"
            min="1"
            class="w-full bg-slate-800/50 border transition-colors rounded-xl px-4 py-3 text-white placeholder:text-white/40 focus:outline-none focus:ring-2 focus:ring-emerald-500/50 focus:border-emerald-500/50"
            :class="errors.interval ? 'border-red-500/50' : 'border-slate-700/50'"
            placeholder="1"
          />
          <p v-if="errors.interval" class="text-xs text-red-400">{{ errors.interval }}</p>
        </div>
      </div>

      <!-- Conditional Schedule Fields -->
      <div v-if="showDayOfMonth" class="space-y-2">
        <label class="text-sm font-medium text-white/90 flex items-center gap-2">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
          </svg>
          {{ t('subscriptions.dayOfMonth') || 'Day of month' }}
        </label>
        <input
          v-model.number="form.dayOfMonth"
          type="number"
          min="1"
          max="31"
          class="w-full bg-slate-800/50 border transition-colors rounded-xl px-4 py-3 text-white placeholder:text-white/40 focus:outline-none focus:ring-2 focus:ring-emerald-500/50 focus:border-emerald-500/50"
          :class="errors.dayOfMonth ? 'border-red-500/50' : 'border-slate-700/50'"
          placeholder="1-31"
        />
        <p v-if="errors.dayOfMonth" class="text-xs text-red-400">{{ errors.dayOfMonth }}</p>
      </div>

      <div v-if="showDayOfWeek" class="space-y-2">
        <label class="text-sm font-medium text-white/90 flex items-center gap-2">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
          </svg>
          {{ t('subscriptions.dayOfWeek') || 'Day of week' }}
        </label>
        <select
          v-model="form.dayOfWeek"
          class="w-full bg-slate-800/50 border transition-colors rounded-xl px-4 py-3 text-white focus:outline-none focus:ring-2 focus:ring-emerald-500/50 focus:border-emerald-500/50"
          :class="errors.dayOfWeek ? 'border-red-500/50' : 'border-slate-700/50'"
        >
          <option value="" disabled>{{ t('subscriptions.selectDay') || 'Select day' }}</option>
          <option v-for="day in daysOfWeek" :key="day" :value="day">{{ day }}</option>
        </select>
        <p v-if="errors.dayOfWeek" class="text-xs text-red-400">{{ errors.dayOfWeek }}</p>
      </div>

      <div v-if="showMonthOfYear" class="space-y-2">
        <label class="text-sm font-medium text-white/90 flex items-center gap-2">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
          </svg>
          {{ t('subscriptions.monthOfYear') || 'Month' }}
        </label>
        <select
          v-model="form.monthOfYear"
          class="w-full bg-slate-800/50 border transition-colors rounded-xl px-4 py-3 text-white focus:outline-none focus:ring-2 focus:ring-emerald-500/50 focus:border-emerald-500/50"
          :class="errors.monthOfYear ? 'border-red-500/50' : 'border-slate-700/50'"
        >
          <option value="" disabled>{{ t('subscriptions.selectMonth') || 'Select month' }}</option>
          <option v-for="month in monthsOfYear" :key="month" :value="month">{{ month }}</option>
        </select>
        <p v-if="errors.monthOfYear" class="text-xs text-red-400">{{ errors.monthOfYear }}</p>
      </div>
    </div>

    <!-- Additional Settings -->
    <div class="space-y-4">
      <h3 class="text-sm font-semibold text-white/90 uppercase tracking-wide flex items-center gap-2">
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
        </svg>
        {{ t('subscriptions.form.settings') || 'Settings' }}
      </h3>
      
      <div class="space-y-3">
        <div class="space-y-2">
          <label class="text-sm font-medium text-white/90 flex items-center gap-2">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z" />
            </svg>
            {{ t('subscriptions.category') || 'Category' }}
            <span class="text-red-400">*</span>
          </label>
          <select
            v-model="form.categoryId"
            class="w-full bg-slate-800/50 border transition-colors rounded-xl px-4 py-3 text-white focus:outline-none focus:ring-2 focus:ring-emerald-500/50 focus:border-emerald-500/50"
            :class="errors.categoryId ? 'border-red-500/50' : 'border-slate-700/50'"
          >
            <option value="" disabled>{{ t('subscriptions.selectCategory') || 'Select category' }}</option>
            <option v-for="category in categories" :key="category.id" :value="category.id">
              {{ category.name }}
            </option>
          </select>
          <p v-if="errors.categoryId" class="text-xs text-red-400">{{ errors.categoryId }}</p>
        </div>

        <div class="space-y-2">
          <label class="text-sm font-medium text-white/90 flex items-center gap-2">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z" />
            </svg>
            {{ t('subscriptions.defaultWallet') || 'Default wallet' }}
            <span class="text-red-400">*</span>
          </label>
          <select
            v-model="form.defaultWalletId"
            class="w-full bg-slate-800/50 border transition-colors rounded-xl px-4 py-3 text-white focus:outline-none focus:ring-2 focus:ring-emerald-500/50 focus:border-emerald-500/50"
            :class="errors.defaultWalletId ? 'border-red-500/50' : 'border-slate-700/50'"
          >
            <option value="" disabled>{{ t('subscriptions.selectWallet') || 'Select wallet' }}</option>
            <option v-for="wallet in wallets" :key="wallet.id" :value="wallet.id">
              {{ wallet.name }} ({{ wallet.currency }})
            </option>
          </select>
          <p v-if="errors.defaultWalletId" class="text-xs text-red-400">{{ errors.defaultWalletId }}</p>
        </div>

        <div class="grid grid-cols-2 gap-3">
          <div class="space-y-2">
            <label class="text-sm font-medium text-white/90 flex items-center gap-2">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
              {{ t('subscriptions.startDate') || 'Start date' }}
              <span class="text-red-400">*</span>
            </label>
            <div class="relative">
              <input
                v-model="form.startDate"
                type="date"
                class="w-full bg-slate-800/50 border transition-colors rounded-xl px-4 py-3 pl-10 text-white focus:outline-none focus:ring-2 focus:ring-emerald-500/50 focus:border-emerald-500/50"
                :class="errors.startDate ? 'border-red-500/50' : 'border-slate-700/50'"
              />
              <svg class="w-5 h-5 text-white/40 absolute left-3 top-1/2 -translate-y-1/2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
            </div>
            <p v-if="errors.startDate" class="text-xs text-red-400">{{ errors.startDate }}</p>
          </div>
          <div class="space-y-2">
            <label class="text-sm font-medium text-white/90 flex items-center gap-2">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
              {{ t('subscriptions.endDate') || 'End date' }}
              <span class="text-xs text-white/50 font-normal">({{ t('common.optional') }})</span>
            </label>
            <div class="relative">
              <input
                v-model="form.endDate"
                type="date"
                class="w-full bg-slate-800/50 border transition-colors rounded-xl px-4 py-3 pl-10 text-white focus:outline-none focus:ring-2 focus:ring-emerald-500/50 focus:border-emerald-500/50 border-slate-700/50"
              />
              <svg class="w-5 h-5 text-white/40 absolute left-3 top-1/2 -translate-y-1/2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
            </div>
          </div>
        </div>

        <div class="space-y-2">
          <label class="text-sm font-medium text-white/90 flex items-center gap-2">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
            </svg>
            {{ t('subscriptions.note') || 'Note' }}
            <span class="text-xs text-white/50 font-normal">({{ t('common.optional') }})</span>
          </label>
          <textarea
            v-model="form.note"
            rows="2"
            maxlength="500"
            class="w-full bg-slate-800/50 border transition-colors rounded-xl px-4 py-3 text-white placeholder:text-white/40 focus:outline-none focus:ring-2 focus:ring-emerald-500/50 focus:border-emerald-500/50 resize-none"
            :class="errors.note ? 'border-red-500/50' : 'border-slate-700/50'"
            placeholder="Add a note..."
          />
          <div class="flex items-center justify-between">
            <p v-if="errors.note" class="text-xs text-red-400">{{ errors.note }}</p>
            <p v-else class="text-xs text-white/50">{{ form.note.length }}/500</p>
          </div>
        </div>

        <label class="flex items-center gap-3 p-3 rounded-xl border border-slate-700/50 hover:border-slate-600/50 transition-colors cursor-pointer group">
          <input
            v-model="form.enabled"
            type="checkbox"
            class="w-5 h-5 rounded border-slate-600 bg-slate-800/50 text-emerald-500 focus:ring-2 focus:ring-emerald-500/50 cursor-pointer"
          />
          <div class="flex-1">
            <span class="text-sm font-medium text-white/90 block flex items-center gap-2">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              {{ t('subscriptions.enabled') || 'Enabled' }}
            </span>
            <span class="text-xs text-white/50">{{ t('subscriptions.form.enabledHelp') || 'Active subscriptions will create transactions automatically' }}</span>
          </div>
        </label>
      </div>
    </div>

    <!-- Action Buttons -->
    <div class="flex gap-3 pt-4 pb-safe">
      <button
        type="button"
        class="flex-1 h-14 rounded-2xl border-2 border-slate-700/50 text-white/90 hover:text-white hover:border-slate-600/50 hover:bg-white/5 active:bg-white/10 active:scale-95 transition-all font-semibold flex items-center justify-center gap-2 touch-manipulation"
        @click="emit('cancelled')"
      >
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
        </svg>
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
        {{ submitting ? (t('common.saving') || 'Saving...') : (props.subscription ? t('subscriptions.update') || 'Update' : t('subscriptions.create') || 'Create') }}
      </button>
    </div>
  </form>
</template>

<script setup lang="ts">
import type { Subscription, SubscriptionRequest } from '~/types/pockito'
import {
  CategoryType,
  Currency,
  DayOfWeek,
  MonthOfYear,
  SubscriptionFrequency
} from '~/types/pockito'
import { usePockitoSubscriptions } from '~/composables/usePockitoSubscriptions'
import { usePockitoWallets } from '~/composables/usePockitoWallets'
import { usePockitoCategories } from '~/composables/usePockitoCategories'
import { usePockitoUsers } from '~/composables/usePockitoUsers'
import AmountInput from '~/components/AmountInput.vue'

const props = defineProps<{
  subscription?: Subscription | null
}>()

const emit = defineEmits<{
  saved: [subscription: Subscription]
  cancelled: []
}>()

const { t } = useI18n()
const toast = useToast()
const { createSubscription, updateSubscription } = usePockitoSubscriptions()
const { wallets, loadWallets } = usePockitoWallets()
const { getCategoriesByType } = usePockitoCategories()
const { currentUser, getOrCreateCurrentUser } = usePockitoUsers()

const currencyOptions = Object.values(Currency)
const frequencies = Object.values(SubscriptionFrequency)
const daysOfWeek = Object.values(DayOfWeek)
const monthsOfYear = Object.values(MonthOfYear)

const categories = ref<any[]>([])
const errors = reactive<Record<string, string>>({})
const submitting = ref(false)

const form = reactive<SubscriptionRequest>({
  name: '',
  iconUrl: '',
  frequency: SubscriptionFrequency.MONTHLY,
  interval: 1,
  amount: 0,
  currency: Currency.USD,
  startDate: new Date().toISOString().split('T')[0],
  endDate: undefined,
  enabled: true,
  categoryId: '',
  dayOfMonth: undefined,
  dayOfWeek: undefined,
  monthOfYear: undefined,
  defaultWalletId: '',
  note: ''
})

const showDayOfMonth = computed(() => form.frequency === SubscriptionFrequency.MONTHLY || form.frequency === SubscriptionFrequency.YEARLY)
const showDayOfWeek = computed(() => form.frequency === SubscriptionFrequency.WEEKLY)
const showMonthOfYear = computed(() => form.frequency === SubscriptionFrequency.YEARLY)

const loadCategories = async () => {
  try {
    const expense = await getCategoriesByType(CategoryType.EXPENSE)
    categories.value = expense.categories || []
  } catch (err) {
    toast.error((err as Error)?.message || (t('subscriptions.loadingCategoriesError') as string) || 'Unable to load categories')
  }
}

const setDefaultsFromUser = () => {
  if (!props.subscription && currentUser.value?.defaultCurrency) {
    form.currency = currentUser.value.defaultCurrency
  }
}

const patchForm = (subscription: Subscription) => {
  form.name = subscription.name
  form.iconUrl = subscription.iconUrl || ''
  form.frequency = subscription.frequency
  form.interval = subscription.interval
  form.amount = subscription.amount
  form.currency = subscription.currency
  form.startDate = subscription.startDate?.split('T')[0] || ''
  form.endDate = subscription.endDate?.split('T')[0]
  form.enabled = subscription.enabled
  form.categoryId = subscription.categoryId
  form.dayOfMonth = subscription.dayOfMonth
  form.dayOfWeek = subscription.dayOfWeek
  form.monthOfYear = subscription.monthOfYear
  form.defaultWalletId = subscription.defaultWalletId
  form.note = subscription.note || ''
}

const resetForm = () => {
  form.name = ''
  form.iconUrl = ''
  form.frequency = SubscriptionFrequency.MONTHLY
  form.interval = 1
  form.amount = 0
  form.currency = Currency.USD
  form.startDate = new Date().toISOString().split('T')[0]
  form.endDate = undefined
  form.enabled = true
  form.categoryId = ''
  form.dayOfMonth = undefined
  form.dayOfWeek = undefined
  form.monthOfYear = undefined
  form.defaultWalletId = ''
  form.note = ''
  Object.keys(errors).forEach((key) => delete errors[key])
}

onMounted(async () => {
  if (!currentUser.value) {
    try {
      await getOrCreateCurrentUser()
    } catch {
      // ignore user fetch failure
    }
  }
  await loadWallets()
  await loadCategories()
  setDefaultsFromUser()
  const defaultWallet = wallets.value.find(w => w.isDefault)
  if (!form.defaultWalletId && defaultWallet) {
    form.defaultWalletId = defaultWallet.id
  }
  if (props.subscription) {
    patchForm(props.subscription)
  }
})

watch(
  () => props.subscription,
  (sub) => {
    if (sub) {
      patchForm(sub)
    } else {
      resetForm()
      setDefaultsFromUser()
    }
  }
)

const onFrequencyChange = () => {
  if (!showDayOfMonth.value) {
    form.dayOfMonth = undefined
  }
  if (!showDayOfWeek.value) {
    form.dayOfWeek = undefined
  }
  if (!showMonthOfYear.value) {
    form.monthOfYear = undefined
  }
}

const validate = () => {
  Object.keys(errors).forEach((key) => delete errors[key])
  if (!form.name?.trim()) errors.name = t('subscriptions.form.errors.nameRequired') || 'Name is required'
  if (!form.amount || form.amount < 0) errors.amount = t('subscriptions.form.errors.amountMin') || 'Amount must be 0 or more'
  if (!form.currency) errors.currency = t('subscriptions.form.errors.currencyRequired') || 'Currency required'
  if (!form.frequency) errors.frequency = t('subscriptions.form.errors.frequencyRequired') || 'Frequency required'
  if (!form.interval || form.interval < 1) errors.interval = t('subscriptions.form.errors.intervalMin') || 'Interval must be >= 1'
  if (!form.categoryId) errors.categoryId = t('subscriptions.form.errors.categoryRequired') || 'Category required'
  if (!form.defaultWalletId) errors.defaultWalletId = t('subscriptions.form.errors.defaultWalletRequired') || 'Wallet required'
  if (!form.startDate) errors.startDate = t('subscriptions.form.errors.startDateRequired') || 'Start date required'
  if (form.iconUrl && !/^https?:\/\/.+\.(jpg|jpeg|png|gif|svg|webp)(\?.*)?$/i.test(form.iconUrl)) {
    errors.iconUrl = t('subscriptions.form.errors.iconUrlPattern') || 'Invalid image URL'
  }
  if (form.note && form.note.length > 500) errors.note = t('subscriptions.form.errors.noteMaxLength') || 'Note too long'
  if (showDayOfMonth.value && (!form.dayOfMonth || form.dayOfMonth < 1 || form.dayOfMonth > 31)) {
    errors.dayOfMonth = t('subscriptions.form.errors.dayOfMonthRange') || 'Enter a valid day'
  }
  if (showDayOfWeek.value && !form.dayOfWeek) {
    errors.dayOfWeek = t('subscriptions.form.errors.dayOfWeekRequired') || 'Select a day'
  }
  if (showMonthOfYear.value && !form.monthOfYear) {
    errors.monthOfYear = t('subscriptions.form.errors.monthOfYearRequired') || 'Select a month'
  }
  return Object.keys(errors).length === 0
}

const handleSubmit = async () => {
  if (!validate()) return
  submitting.value = true
  try {
    const payload: SubscriptionRequest = {
      ...form,
      endDate: form.endDate || undefined
    }
    let saved: Subscription
    if (props.subscription) {
      saved = await updateSubscription(props.subscription.id, payload)
      toast.success(t('subscriptions.updateSuccess') || 'Subscription updated')
    } else {
      saved = await createSubscription(payload)
      toast.success(t('subscriptions.createSuccess') || 'Subscription created')
    }
    emit('saved', saved)
  } catch (err) {
    toast.error((err as Error)?.message || (t('subscriptions.saveFailed') as string) || 'Unable to save subscription')
  } finally {
    submitting.value = false
  }
}
</script>
