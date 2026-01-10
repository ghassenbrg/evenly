<template>
  <div class="icon-picker">
    <!-- Search Input -->
    <div class="relative mb-4">
      <input
        v-model="searchQuery"
        type="text"
        :placeholder="placeholder || t('settings.categories.searchIcons') || 'Search icons...'"
        class="w-full px-4 py-3 pl-10 bg-slate-800 border border-slate-700 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-emerald-500 text-base"
        @input="handleSearch"
      />
      <svg
        class="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-slate-400"
        fill="none"
        stroke="currentColor"
        viewBox="0 0 24 24"
      >
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
      </svg>
      <button
        v-if="searchQuery"
        @click="clearSearch"
        class="absolute right-3 top-1/2 -translate-y-1/2 p-1 text-slate-400 hover:text-white transition-colors"
      >
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
        </svg>
      </button>
    </div>

    <!-- Icon Grid -->
    <div :class="['overflow-y-auto', maxHeightClass]">
      <!-- Filtered Icons -->
      <div v-if="filteredIcons.length > 0" :class="gridClass">
        <button
          v-for="icon in filteredIcons"
          :key="icon.value"
          @click="selectIcon(icon.value)"
          :class="[
            'relative p-3 rounded-xl border-2 transition-all hover:scale-105 active:scale-95',
            selectedIcon === icon.value
              ? 'border-emerald-500 bg-emerald-500/10'
              : 'border-slate-700 bg-slate-800 hover:border-slate-600'
          ]"
          :title="icon.name"
        >
          <div class="flex flex-col items-center gap-1.5">
            <FontAwesomeIcon
              :icon="getFontAwesomeIcon(icon.value)"
              class="w-5 h-5 sm:w-6 sm:h-6 text-white"
            />
            <span class="text-[10px] sm:text-xs text-slate-400 truncate w-full text-center leading-tight">{{ icon.name }}</span>
          </div>
          <!-- Checkmark for selected icon -->
          <div
            v-if="selectedIcon === icon.value"
            class="absolute top-1 right-1 w-5 h-5 bg-emerald-500 rounded-full flex items-center justify-center"
          >
            <svg class="w-3 h-3 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M5 13l4 4L19 7" />
            </svg>
          </div>
        </button>
      </div>

      <!-- No Results -->
      <div v-else class="text-center py-8">
        <p class="text-slate-400">{{ t('settings.categories.noIconsFound') || 'No icons found' }}</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useFontAwesome } from '~/composables/useFontAwesome'

interface Props {
  modelValue?: string
  placeholder?: string
  maxHeight?: string
  columns?: number
}

interface IconOption {
  name: string
  value: string
  keywords: string[]
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: '',
  placeholder: undefined,
  maxHeight: '400px',
  columns: 6
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
  'select': [value: string]
}>()

const { t } = useI18n()
const { parseIconClass } = useFontAwesome()

const searchQuery = ref('')
const selectedIcon = computed(() => props.modelValue)

const maxHeightClass = computed(() => {
  return `max-h-[${props.maxHeight}]`
})

const gridClass = computed(() => {
  const cols = props.columns
  if (cols === 4) return 'grid grid-cols-4 gap-3'
  if (cols === 5) return 'grid grid-cols-4 sm:grid-cols-5 gap-3'
  if (cols === 6) return 'grid grid-cols-4 sm:grid-cols-5 md:grid-cols-6 gap-3'
  if (cols === 8) return 'grid grid-cols-4 sm:grid-cols-6 md:grid-cols-8 gap-3'
  return `grid grid-cols-${cols} gap-3`
})

// Comprehensive list of Font Awesome icons - 200+ icons
const icons: IconOption[] = [
  // Food & Dining
  { name: 'Utensils', value: 'fa-solid fa-utensils', keywords: ['food', 'dining', 'restaurant', 'eat', 'meal', 'fork', 'knife'] },
  { name: 'Bowl Food', value: 'fa-solid fa-bowl-food', keywords: ['food', 'bowl', 'meal', 'dining', 'soup'] },
  { name: 'Burger', value: 'fa-solid fa-burger', keywords: ['food', 'burger', 'fast food', 'hamburger', 'cheeseburger'] },
  { name: 'Pizza Slice', value: 'fa-solid fa-pizza-slice', keywords: ['food', 'pizza', 'slice', 'italian'] },
  { name: 'Coffee', value: 'fa-solid fa-mug-hot', keywords: ['coffee', 'drink', 'cafe', 'beverage', 'hot'] },
  { name: 'Wine Glass', value: 'fa-solid fa-wine-glass', keywords: ['wine', 'drink', 'alcohol', 'bar', 'beverage'] },
  { name: 'Wine Bottle', value: 'fa-solid fa-wine-bottle', keywords: ['wine', 'bottle', 'alcohol'] },
  { name: 'Beer', value: 'fa-solid fa-beer-mug-empty', keywords: ['beer', 'drink', 'alcohol', 'bar', 'mug'] },
  { name: 'Cocktail', value: 'fa-solid fa-martini-glass', keywords: ['cocktail', 'drink', 'alcohol', 'bar'] },
  { name: 'Cake', value: 'fa-solid fa-cake-candles', keywords: ['cake', 'dessert', 'sweet', 'birthday', 'celebration'] },
  { name: 'Cookie', value: 'fa-solid fa-cookie', keywords: ['cookie', 'dessert', 'sweet', 'snack'] },
  { name: 'Ice Cream', value: 'fa-solid fa-ice-cream', keywords: ['ice cream', 'dessert', 'sweet', 'frozen'] },
  { name: 'Fish', value: 'fa-solid fa-fish', keywords: ['fish', 'seafood', 'food', 'meal'] },
  { name: 'Drumstick', value: 'fa-solid fa-drumstick-bite', keywords: ['chicken', 'meat', 'food', 'drumstick'] },
  { name: 'Apple', value: 'fa-solid fa-apple-whole', keywords: ['apple', 'fruit', 'food', 'healthy'] },
  { name: 'Carrot', value: 'fa-solid fa-carrot', keywords: ['carrot', 'vegetable', 'food', 'healthy'] },
  { name: 'Bread', value: 'fa-solid fa-bread-slice', keywords: ['bread', 'food', 'bakery'] },
  { name: 'Cheese', value: 'fa-solid fa-cheese', keywords: ['cheese', 'dairy', 'food'] },
  { name: 'Egg', value: 'fa-solid fa-egg', keywords: ['egg', 'food', 'breakfast'] },
  { name: 'Bacon', value: 'fa-solid fa-bacon', keywords: ['bacon', 'meat', 'food', 'breakfast'] },
  
  // Shopping
  { name: 'Shopping Cart', value: 'fa-solid fa-cart-shopping', keywords: ['shopping', 'cart', 'store', 'buy', 'purchase'] },
  { name: 'Shopping Bag', value: 'fa-solid fa-bag-shopping', keywords: ['shopping', 'bag', 'store', 'buy', 'purchase'] },
  { name: 'Basket Shopping', value: 'fa-solid fa-basket-shopping', keywords: ['shopping', 'basket', 'store', 'buy'] },
  { name: 'Store', value: 'fa-solid fa-store', keywords: ['store', 'shop', 'retail', 'market'] },
  { name: 'Shop', value: 'fa-solid fa-shop', keywords: ['shop', 'store', 'retail'] },
  { name: 'Gift', value: 'fa-solid fa-gift', keywords: ['gift', 'present', 'celebration', 'birthday'] },
  { name: 'Tag', value: 'fa-solid fa-tag', keywords: ['tag', 'price', 'discount', 'sale'] },
  { name: 'Tags', value: 'fa-solid fa-tags', keywords: ['tags', 'price', 'discount', 'sale'] },
  { name: 'Barcode', value: 'fa-solid fa-barcode', keywords: ['barcode', 'product', 'scan'] },
  { name: 'Receipt', value: 'fa-solid fa-receipt', keywords: ['receipt', 'bill', 'invoice', 'expense', 'purchase'] },
  
  // Transportation
  { name: 'Car', value: 'fa-solid fa-car', keywords: ['car', 'vehicle', 'transport', 'drive', 'automobile'] },
  { name: 'Taxi', value: 'fa-solid fa-taxi', keywords: ['taxi', 'cab', 'transport', 'ride'] },
  { name: 'Bus', value: 'fa-solid fa-bus', keywords: ['bus', 'transport', 'public transport', 'transit'] },
  { name: 'Train', value: 'fa-solid fa-train', keywords: ['train', 'railway', 'transport', 'rail'] },
  { name: 'Subway', value: 'fa-solid fa-train-subway', keywords: ['subway', 'metro', 'train', 'transport'] },
  { name: 'Plane', value: 'fa-solid fa-plane', keywords: ['plane', 'airplane', 'flight', 'travel', 'air'] },
  { name: 'Helicopter', value: 'fa-solid fa-helicopter', keywords: ['helicopter', 'aircraft', 'transport'] },
  { name: 'Ship', value: 'fa-solid fa-ship', keywords: ['ship', 'boat', 'transport', 'cruise'] },
  { name: 'Motorcycle', value: 'fa-solid fa-motorcycle', keywords: ['motorcycle', 'bike', 'transport'] },
  { name: 'Gas Pump', value: 'fa-solid fa-gas-pump', keywords: ['gas', 'fuel', 'petrol', 'car', 'station'] },
  { name: 'Bicycle', value: 'fa-solid fa-bicycle', keywords: ['bike', 'bicycle', 'cycling', 'transport'] },
  { name: 'Scooter', value: 'fa-solid fa-scooter', keywords: ['scooter', 'transport', 'electric'] },
  { name: 'Parking', value: 'fa-solid fa-square-parking', keywords: ['parking', 'car', 'space'] },
  { name: 'Traffic Light', value: 'fa-solid fa-traffic-light', keywords: ['traffic', 'light', 'road', 'signal'] },
  
  // Entertainment
  { name: 'Film', value: 'fa-solid fa-film', keywords: ['movie', 'film', 'cinema', 'entertainment'] },
  { name: 'TV', value: 'fa-solid fa-tv', keywords: ['tv', 'television', 'entertainment', 'watch'] },
  { name: 'Gamepad', value: 'fa-solid fa-gamepad', keywords: ['game', 'gaming', 'console', 'entertainment', 'play'] },
  { name: 'Dice', value: 'fa-solid fa-dice', keywords: ['dice', 'game', 'gaming', 'random'] },
  { name: 'Chess', value: 'fa-solid fa-chess', keywords: ['chess', 'game', 'board', 'strategy'] },
  { name: 'Music', value: 'fa-solid fa-music', keywords: ['music', 'song', 'entertainment', 'audio'] },
  { name: 'Headphones', value: 'fa-solid fa-headphones', keywords: ['headphones', 'music', 'audio', 'listen'] },
  { name: 'Microphone', value: 'fa-solid fa-microphone', keywords: ['microphone', 'music', 'sing', 'audio'] },
  { name: 'Guitar', value: 'fa-solid fa-guitar', keywords: ['guitar', 'music', 'instrument'] },
  { name: 'Drum', value: 'fa-solid fa-drum', keywords: ['drum', 'music', 'instrument'] },
  { name: 'Ticket', value: 'fa-solid fa-ticket', keywords: ['ticket', 'event', 'concert', 'show', 'entertainment'] },
  { name: 'Theater Masks', value: 'fa-solid fa-masks-theater', keywords: ['theater', 'drama', 'entertainment', 'play'] },
  { name: 'Book Open', value: 'fa-solid fa-book-open', keywords: ['book', 'reading', 'literature', 'entertainment'] },
  { name: 'Theater', value: 'fa-solid fa-theater-masks', keywords: ['theater', 'drama', 'entertainment'] },
  
  // Bills & Utilities
  { name: 'Lightbulb', value: 'fa-solid fa-lightbulb', keywords: ['electricity', 'light', 'utility', 'bill', 'energy'] },
  { name: 'Bolt', value: 'fa-solid fa-bolt', keywords: ['electricity', 'power', 'energy', 'bill', 'lightning'] },
  { name: 'Plug', value: 'fa-solid fa-plug', keywords: ['plug', 'electricity', 'power', 'outlet'] },
  { name: 'Droplet', value: 'fa-solid fa-droplet', keywords: ['water', 'utility', 'bill', 'liquid'] },
  { name: 'Fire', value: 'fa-solid fa-fire', keywords: ['gas', 'heating', 'utility', 'bill', 'flame'] },
  { name: 'Flame', value: 'fa-solid fa-fire-flame-curved', keywords: ['flame', 'gas', 'heating', 'fire'] },
  { name: 'Wifi', value: 'fa-solid fa-wifi', keywords: ['internet', 'wifi', 'network', 'bill', 'connection'] },
  { name: 'Router', value: 'fa-solid fa-router', keywords: ['router', 'internet', 'network', 'wifi'] },
  { name: 'Phone', value: 'fa-solid fa-phone', keywords: ['phone', 'mobile', 'telephone', 'bill', 'call'] },
  { name: 'Mobile', value: 'fa-solid fa-mobile-screen-button', keywords: ['mobile', 'phone', 'cell', 'bill', 'smartphone'] },
  { name: 'House', value: 'fa-solid fa-house', keywords: ['house', 'home', 'rent', 'housing', 'residence'] },
  { name: 'Building', value: 'fa-solid fa-building', keywords: ['building', 'office', 'rent', 'apartment'] },
  { name: 'Home', value: 'fa-solid fa-home', keywords: ['home', 'house', 'residence'] },
  { name: 'Cable', value: 'fa-solid fa-cable-car', keywords: ['cable', 'tv', 'internet', 'utility'] },
  { name: 'Satellite', value: 'fa-solid fa-satellite', keywords: ['satellite', 'tv', 'internet', 'communication'] },
  { name: 'Tower', value: 'fa-solid fa-tower-cell', keywords: ['tower', 'cell', 'phone', 'signal'] },
  
  // Health & Fitness
  { name: 'Heart Pulse', value: 'fa-solid fa-heart-pulse', keywords: ['health', 'medical', 'hospital', 'doctor', 'heart'] },
  { name: 'Heartbeat', value: 'fa-solid fa-heartbeat', keywords: ['heartbeat', 'health', 'medical', 'pulse'] },
  { name: 'Pills', value: 'fa-solid fa-pills', keywords: ['medicine', 'pharmacy', 'health', 'medication'] },
  { name: 'Prescription', value: 'fa-solid fa-prescription-bottle', keywords: ['prescription', 'medicine', 'pharmacy', 'health'] },
  { name: 'Syringe', value: 'fa-solid fa-syringe', keywords: ['syringe', 'medical', 'vaccine', 'injection'] },
  { name: 'Stethoscope', value: 'fa-solid fa-stethoscope', keywords: ['stethoscope', 'doctor', 'medical', 'health'] },
  { name: 'Hospital', value: 'fa-solid fa-hospital', keywords: ['hospital', 'medical', 'health', 'clinic'] },
  { name: 'Cross', value: 'fa-solid fa-cross', keywords: ['cross', 'medical', 'health', 'hospital'] },
  { name: 'Dumbbell', value: 'fa-solid fa-dumbbell', keywords: ['gym', 'fitness', 'workout', 'exercise', 'weight'] },
  { name: 'Running', value: 'fa-solid fa-person-running', keywords: ['running', 'fitness', 'exercise', 'sport'] },
  { name: 'Biking', value: 'fa-solid fa-person-biking', keywords: ['biking', 'cycling', 'fitness', 'exercise'] },
  { name: 'Swimming', value: 'fa-solid fa-person-swimming', keywords: ['swimming', 'fitness', 'exercise', 'sport'] },
  { name: 'Spa', value: 'fa-solid fa-spa', keywords: ['spa', 'wellness', 'relaxation', 'massage'] },
  { name: 'Tooth', value: 'fa-solid fa-tooth', keywords: ['tooth', 'dental', 'dentist', 'health'] },
  { name: 'Eye', value: 'fa-solid fa-eye', keywords: ['eye', 'vision', 'optometrist', 'health'] },
  { name: 'Glasses', value: 'fa-solid fa-glasses', keywords: ['glasses', 'eyewear', 'vision', 'optometrist'] },
  
  // Education
  { name: 'Graduation Cap', value: 'fa-solid fa-graduation-cap', keywords: ['education', 'school', 'university', 'study', 'graduate'] },
  { name: 'Book', value: 'fa-solid fa-book', keywords: ['book', 'reading', 'education', 'study', 'textbook'] },
  { name: 'Pencil', value: 'fa-solid fa-pencil', keywords: ['writing', 'education', 'study', 'pen'] },
  { name: 'Pen', value: 'fa-solid fa-pen', keywords: ['pen', 'writing', 'education'] },
  { name: 'Marker', value: 'fa-solid fa-marker', keywords: ['marker', 'highlight', 'education'] },
  { name: 'Chalkboard', value: 'fa-solid fa-chalkboard', keywords: ['chalkboard', 'blackboard', 'education', 'teaching'] },
  { name: 'School', value: 'fa-solid fa-school', keywords: ['school', 'education', 'learning'] },
  { name: 'University', value: 'fa-solid fa-building-columns', keywords: ['university', 'college', 'education'] },
  { name: 'Calculator', value: 'fa-solid fa-calculator', keywords: ['calculator', 'math', 'education', 'calculate'] },
  { name: 'Ruler', value: 'fa-solid fa-ruler', keywords: ['ruler', 'measure', 'education', 'math'] },
  { name: 'Compass', value: 'fa-solid fa-compass', keywords: ['compass', 'navigation', 'education', 'math'] },
  { name: 'Microscope', value: 'fa-solid fa-microscope', keywords: ['microscope', 'science', 'education', 'lab'] },
  { name: 'Flask', value: 'fa-solid fa-flask', keywords: ['flask', 'science', 'chemistry', 'education'] },
  
  // Personal Care & Fashion
  { name: 'Shirt', value: 'fa-solid fa-shirt', keywords: ['clothing', 'clothes', 'fashion', 'shopping', 'apparel'] },
  { name: 'Tshirt', value: 'fa-solid fa-shirt-tank-top', keywords: ['tshirt', 'clothing', 'fashion'] },
  { name: 'Socks', value: 'fa-solid fa-socks', keywords: ['socks', 'clothing', 'fashion'] },
  { name: 'Hat', value: 'fa-solid fa-hat-cowboy', keywords: ['hat', 'clothing', 'fashion', 'accessory'] },
  { name: 'Glasses', value: 'fa-solid fa-glasses', keywords: ['glasses', 'eyewear', 'fashion', 'accessory'] },
  { name: 'Sunglasses', value: 'fa-solid fa-sunglasses', keywords: ['sunglasses', 'eyewear', 'fashion'] },
  { name: 'Ring', value: 'fa-solid fa-ring', keywords: ['ring', 'jewelry', 'fashion', 'wedding'] },
  { name: 'Gem', value: 'fa-solid fa-gem', keywords: ['gem', 'jewelry', 'diamond', 'fashion'] },
  { name: 'Scissors', value: 'fa-solid fa-scissors', keywords: ['haircut', 'salon', 'beauty', 'cut'] },
  { name: 'Soap', value: 'fa-solid fa-soap', keywords: ['soap', 'cleaning', 'personal care', 'hygiene'] },
  { name: 'Spray', value: 'fa-solid fa-spray-can', keywords: ['spray', 'perfume', 'beauty', 'cosmetic'] },
  { name: 'Pump Soap', value: 'fa-solid fa-pump-soap', keywords: ['soap', 'hand soap', 'hygiene'] },
  { name: 'Hand', value: 'fa-solid fa-hand', keywords: ['hand', 'care', 'massage'] },
  { name: 'Nail Polish', value: 'fa-solid fa-hand-sparkles', keywords: ['nail', 'polish', 'beauty', 'manicure'] },
  
  // Finance & Money
  { name: 'Credit Card', value: 'fa-solid fa-credit-card', keywords: ['card', 'payment', 'finance', 'bank'] },
  { name: 'Wallet', value: 'fa-solid fa-wallet', keywords: ['wallet', 'money', 'finance', 'cash'] },
  { name: 'Money Bill', value: 'fa-solid fa-money-bill', keywords: ['money', 'cash', 'payment', 'dollar'] },
  { name: 'Bills', value: 'fa-solid fa-money-bills', keywords: ['bills', 'money', 'cash', 'payment'] },
  { name: 'Dollar Sign', value: 'fa-solid fa-dollar-sign', keywords: ['dollar', 'money', 'currency', 'usd'] },
  { name: 'Euro Sign', value: 'fa-solid fa-euro-sign', keywords: ['euro', 'money', 'currency', 'eur'] },
  { name: 'Yen Sign', value: 'fa-solid fa-yen-sign', keywords: ['yen', 'money', 'currency', 'jpy'] },
  { name: 'Pound Sign', value: 'fa-solid fa-pound-sign', keywords: ['pound', 'money', 'currency', 'gbp'] },
  { name: 'Piggy Bank', value: 'fa-solid fa-piggy-bank', keywords: ['savings', 'money', 'bank', 'piggy'] },
  { name: 'Chart Line', value: 'fa-solid fa-chart-line', keywords: ['chart', 'analytics', 'finance', 'graph'] },
  { name: 'Chart Bar', value: 'fa-solid fa-chart-bar', keywords: ['chart', 'bar', 'analytics', 'finance'] },
  { name: 'Chart Pie', value: 'fa-solid fa-chart-pie', keywords: ['chart', 'pie', 'analytics', 'finance'] },
  { name: 'Coins', value: 'fa-solid fa-coins', keywords: ['coins', 'money', 'currency', 'change'] },
  { name: 'Landmark', value: 'fa-solid fa-landmark', keywords: ['bank', 'landmark', 'finance', 'building'] },
  { name: 'Handshake', value: 'fa-solid fa-handshake', keywords: ['handshake', 'agreement', 'partnership', 'deal'] },
  { name: 'Briefcase', value: 'fa-solid fa-briefcase', keywords: ['work', 'business', 'office', 'briefcase'] },
  { name: 'Sack Dollar', value: 'fa-solid fa-sack-dollar', keywords: ['money', 'sack', 'dollar', 'finance'] },
  
  // Technology & Work
  { name: 'Laptop', value: 'fa-solid fa-laptop', keywords: ['laptop', 'computer', 'work', 'tech'] },
  { name: 'Desktop', value: 'fa-solid fa-desktop', keywords: ['desktop', 'computer', 'pc', 'work'] },
  { name: 'Tablet', value: 'fa-solid fa-tablet-screen-button', keywords: ['tablet', 'ipad', 'device', 'tech'] },
  { name: 'Mobile Phone', value: 'fa-solid fa-mobile-screen-button', keywords: ['mobile', 'phone', 'smartphone', 'device'] },
  { name: 'Keyboard', value: 'fa-solid fa-keyboard', keywords: ['keyboard', 'computer', 'typing', 'work'] },
  { name: 'Mouse', value: 'fa-solid fa-computer-mouse', keywords: ['mouse', 'computer', 'device', 'work'] },
  { name: 'Server', value: 'fa-solid fa-server', keywords: ['server', 'computer', 'tech', 'network'] },
  { name: 'Database', value: 'fa-solid fa-database', keywords: ['database', 'data', 'tech', 'storage'] },
  { name: 'Code', value: 'fa-solid fa-code', keywords: ['code', 'programming', 'developer', 'tech'] },
  { name: 'Bug', value: 'fa-solid fa-bug', keywords: ['bug', 'programming', 'debug', 'tech'] },
  { name: 'Print', value: 'fa-solid fa-print', keywords: ['print', 'printer', 'office', 'work'] },
  { name: 'Fax', value: 'fa-solid fa-fax', keywords: ['fax', 'office', 'document', 'work'] },
  { name: 'Copy', value: 'fa-solid fa-copy', keywords: ['copy', 'duplicate', 'document', 'work'] },
  { name: 'File', value: 'fa-solid fa-file', keywords: ['file', 'document', 'work', 'paper'] },
  { name: 'Folder', value: 'fa-solid fa-folder', keywords: ['folder', 'directory', 'work', 'organize'] },
  
  // Home & Maintenance
  { name: 'Wrench', value: 'fa-solid fa-wrench', keywords: ['repair', 'maintenance', 'tool', 'fix'] },
  { name: 'Hammer', value: 'fa-solid fa-hammer', keywords: ['repair', 'maintenance', 'tool', 'construction'] },
  { name: 'Screwdriver', value: 'fa-solid fa-screwdriver', keywords: ['screwdriver', 'tool', 'repair', 'maintenance'] },
  { name: 'Screwdriver Wrench', value: 'fa-solid fa-screwdriver-wrench', keywords: ['tool', 'repair', 'maintenance'] },
  { name: 'Paint Roller', value: 'fa-solid fa-paint-roller', keywords: ['paint', 'decoration', 'home', 'renovation'] },
  { name: 'Paintbrush', value: 'fa-solid fa-paintbrush', keywords: ['paintbrush', 'paint', 'art', 'decoration'] },
  { name: 'Brush', value: 'fa-solid fa-brush', keywords: ['brush', 'cleaning', 'paint', 'maintenance'] },
  { name: 'Broom', value: 'fa-solid fa-broom', keywords: ['broom', 'cleaning', 'sweep', 'maintenance'] },
  { name: 'Spray Can', value: 'fa-solid fa-spray-can', keywords: ['spray', 'paint', 'decoration'] },
  { name: 'Toolbox', value: 'fa-solid fa-toolbox', keywords: ['toolbox', 'tools', 'repair', 'maintenance'] },
  { name: 'Saw', value: 'fa-solid fa-saw', keywords: ['saw', 'tool', 'construction', 'cut'] },
  { name: 'Drill', value: 'fa-solid fa-drill', keywords: ['drill', 'tool', 'construction', 'repair'] },
  { name: 'Ladder', value: 'fa-solid fa-ladder', keywords: ['ladder', 'construction', 'maintenance', 'repair'] },
  { name: 'Shovel', value: 'fa-solid fa-shovel', keywords: ['shovel', 'tool', 'construction', 'dig'] },
  { name: 'Couch', value: 'fa-solid fa-couch', keywords: ['couch', 'sofa', 'furniture', 'home'] },
  { name: 'Bed', value: 'fa-solid fa-bed', keywords: ['bed', 'furniture', 'sleep', 'home'] },
  { name: 'Chair', value: 'fa-solid fa-chair', keywords: ['chair', 'furniture', 'seat', 'home'] },
  { name: 'Table', value: 'fa-solid fa-table', keywords: ['table', 'furniture', 'dining', 'home'] },
  { name: 'Lamp', value: 'fa-solid fa-lamp', keywords: ['lamp', 'light', 'furniture', 'home'] },
  { name: 'Couch', value: 'fa-solid fa-couch', keywords: ['couch', 'sofa', 'furniture', 'home'] },
  { name: 'Door', value: 'fa-solid fa-door-open', keywords: ['door', 'entrance', 'home'] },
  { name: 'Window', value: 'fa-solid fa-window-maximize', keywords: ['window', 'home', 'view'] },
  { name: 'Key', value: 'fa-solid fa-key', keywords: ['key', 'lock', 'security', 'door'] },
  { name: 'Lock', value: 'fa-solid fa-lock', keywords: ['lock', 'security', 'privacy', 'safe'] },
  { name: 'Unlock', value: 'fa-solid fa-lock-open', keywords: ['unlock', 'open', 'access'] },
  
  // Pets & Animals
  { name: 'Baby', value: 'fa-solid fa-baby', keywords: ['baby', 'child', 'kids', 'infant'] },
  { name: 'Dog', value: 'fa-solid fa-dog', keywords: ['pet', 'dog', 'animal', 'puppy'] },
  { name: 'Cat', value: 'fa-solid fa-cat', keywords: ['pet', 'cat', 'animal', 'kitten'] },
  { name: 'Paw', value: 'fa-solid fa-paw', keywords: ['pet', 'animal', 'paw', 'dog', 'cat'] },
  { name: 'Fish', value: 'fa-solid fa-fish', keywords: ['fish', 'pet', 'animal', 'aquarium'] },
  { name: 'Horse', value: 'fa-solid fa-horse', keywords: ['horse', 'animal', 'riding'] },
  { name: 'Crow', value: 'fa-solid fa-crow', keywords: ['crow', 'bird', 'animal'] },
  { name: 'Dove', value: 'fa-solid fa-dove', keywords: ['dove', 'bird', 'animal', 'peace'] },
  { name: 'Spider', value: 'fa-solid fa-spider', keywords: ['spider', 'insect', 'animal'] },
  { name: 'Bug', value: 'fa-solid fa-bug', keywords: ['bug', 'insect', 'animal'] },
  
  // Nature & Outdoors
  { name: 'Tree', value: 'fa-solid fa-tree', keywords: ['tree', 'nature', 'garden', 'outdoor'] },
  { name: 'Flower', value: 'fa-solid fa-seedling', keywords: ['plant', 'garden', 'nature', 'flower'] },
  { name: 'Leaf', value: 'fa-solid fa-leaf', keywords: ['leaf', 'nature', 'plant', 'green'] },
  { name: 'Mountain', value: 'fa-solid fa-mountain', keywords: ['mountain', 'nature', 'hiking', 'outdoor'] },
  { name: 'Sun', value: 'fa-solid fa-sun', keywords: ['sun', 'weather', 'vacation', 'summer'] },
  { name: 'Moon', value: 'fa-solid fa-moon', keywords: ['moon', 'night', 'sleep', 'dark'] },
  { name: 'Cloud', value: 'fa-solid fa-cloud', keywords: ['cloud', 'weather', 'sky'] },
  { name: 'Cloud Rain', value: 'fa-solid fa-cloud-rain', keywords: ['rain', 'weather', 'cloud'] },
  { name: 'Cloud Sun', value: 'fa-solid fa-cloud-sun', keywords: ['weather', 'cloud', 'sun'] },
  { name: 'Snowflake', value: 'fa-solid fa-snowflake', keywords: ['snow', 'winter', 'cold', 'weather'] },
  { name: 'Umbrella', value: 'fa-solid fa-umbrella', keywords: ['umbrella', 'rain', 'weather', 'protection'] },
  { name: 'Tent', value: 'fa-solid fa-tent', keywords: ['tent', 'camping', 'outdoor', 'nature'] },
  { name: 'Fire', value: 'fa-solid fa-fire', keywords: ['fire', 'campfire', 'outdoor', 'warmth'] },
  { name: 'Compass', value: 'fa-solid fa-compass', keywords: ['compass', 'navigation', 'outdoor', 'hiking'] },
  
  // Communication & Social
  { name: 'Envelope', value: 'fa-solid fa-envelope', keywords: ['mail', 'email', 'message', 'letter'] },
  { name: 'Paper Plane', value: 'fa-solid fa-paper-plane', keywords: ['send', 'message', 'communication', 'email'] },
  { name: 'Comment', value: 'fa-solid fa-comment', keywords: ['comment', 'message', 'chat', 'communication'] },
  { name: 'Comments', value: 'fa-solid fa-comments', keywords: ['comments', 'messages', 'chat', 'communication'] },
  { name: 'Phone', value: 'fa-solid fa-phone', keywords: ['phone', 'call', 'telephone', 'communication'] },
  { name: 'Video', value: 'fa-solid fa-video', keywords: ['video', 'camera', 'recording', 'call'] },
  { name: 'Camera', value: 'fa-solid fa-camera', keywords: ['camera', 'photo', 'photography', 'picture'] },
  { name: 'Image', value: 'fa-solid fa-image', keywords: ['image', 'photo', 'picture', 'photo'] },
  { name: 'User', value: 'fa-solid fa-user', keywords: ['user', 'person', 'profile', 'account'] },
  { name: 'Users', value: 'fa-solid fa-users', keywords: ['users', 'people', 'group', 'team'] },
  { name: 'User Group', value: 'fa-solid fa-user-group', keywords: ['group', 'users', 'people', 'team'] },
  { name: 'Handshake', value: 'fa-solid fa-handshake', keywords: ['handshake', 'agreement', 'partnership', 'deal'] },
  { name: 'Thumbs Up', value: 'fa-solid fa-thumbs-up', keywords: ['like', 'approve', 'good', 'positive'] },
  { name: 'Thumbs Down', value: 'fa-solid fa-thumbs-down', keywords: ['dislike', 'reject', 'bad', 'negative'] },
  { name: 'Heart', value: 'fa-solid fa-heart', keywords: ['heart', 'love', 'favorite', 'like'] },
  { name: 'Star', value: 'fa-solid fa-star', keywords: ['star', 'favorite', 'rating', 'review'] },
  { name: 'Smile', value: 'fa-regular fa-face-smile', keywords: ['smile', 'happy', 'emotion', 'positive'] },
  { name: 'Frown', value: 'fa-regular fa-face-frown', keywords: ['frown', 'sad', 'emotion', 'negative'] },
  { name: 'Meh', value: 'fa-regular fa-face-meh', keywords: ['meh', 'neutral', 'emotion'] },
  { name: 'Grin', value: 'fa-regular fa-face-grin', keywords: ['grin', 'happy', 'smile', 'emotion'] },
  
  // Time & Calendar
  { name: 'Calendar', value: 'fa-solid fa-calendar', keywords: ['calendar', 'date', 'event', 'schedule'] },
  { name: 'Calendar Days', value: 'fa-solid fa-calendar-days', keywords: ['calendar', 'date', 'days', 'schedule'] },
  { name: 'Clock', value: 'fa-solid fa-clock', keywords: ['time', 'clock', 'schedule', 'watch'] },
  { name: 'Stopwatch', value: 'fa-solid fa-stopwatch', keywords: ['stopwatch', 'time', 'timer', 'measure'] },
  { name: 'Hourglass', value: 'fa-solid fa-hourglass', keywords: ['hourglass', 'time', 'timer', 'wait'] },
  { name: 'Bell', value: 'fa-solid fa-bell', keywords: ['notification', 'alert', 'reminder', 'alarm'] },
  { name: 'Calendar Check', value: 'fa-solid fa-calendar-check', keywords: ['calendar', 'check', 'event', 'done'] },
  
  // Travel & Location
  { name: 'Location', value: 'fa-solid fa-location-dot', keywords: ['location', 'place', 'map', 'gps'] },
  { name: 'Map', value: 'fa-solid fa-map', keywords: ['map', 'location', 'navigation', 'travel'] },
  { name: 'Map Pin', value: 'fa-solid fa-map-pin', keywords: ['pin', 'location', 'map', 'marker'] },
  { name: 'Globe', value: 'fa-solid fa-globe', keywords: ['globe', 'world', 'internet', 'earth'] },
  { name: 'Flag', value: 'fa-solid fa-flag', keywords: ['flag', 'country', 'travel', 'nation'] },
  { name: 'Passport', value: 'fa-solid fa-passport', keywords: ['passport', 'travel', 'document', 'trip'] },
  { name: 'Suitcase', value: 'fa-solid fa-suitcase', keywords: ['suitcase', 'luggage', 'travel', 'trip'] },
  { name: 'Suitcase Rolling', value: 'fa-solid fa-suitcase-rolling', keywords: ['suitcase', 'luggage', 'travel', 'trip'] },
  { name: 'Hotel', value: 'fa-solid fa-hotel', keywords: ['hotel', 'accommodation', 'travel', 'stay'] },
  { name: 'Umbrella Beach', value: 'fa-solid fa-umbrella-beach', keywords: ['beach', 'vacation', 'travel', 'summer'] },
  
  // Security & Safety
  { name: 'Shield', value: 'fa-solid fa-shield', keywords: ['shield', 'security', 'protection', 'safety'] },
  { name: 'Shield Halved', value: 'fa-solid fa-shield-halved', keywords: ['shield', 'security', 'protection'] },
  { name: 'Lock', value: 'fa-solid fa-lock', keywords: ['lock', 'security', 'privacy', 'safe'] },
  { name: 'Unlock', value: 'fa-solid fa-lock-open', keywords: ['unlock', 'open', 'access', 'security'] },
  { name: 'Key', value: 'fa-solid fa-key', keywords: ['key', 'lock', 'security', 'access'] },
  { name: 'Fingerprint', value: 'fa-solid fa-fingerprint', keywords: ['fingerprint', 'security', 'biometric', 'lock'] },
  { name: 'Eye', value: 'fa-solid fa-eye', keywords: ['eye', 'view', 'watch', 'see'] },
  { name: 'Eye Slash', value: 'fa-solid fa-eye-slash', keywords: ['eye', 'hide', 'private', 'secret'] },
  
  // Miscellaneous
  { name: 'Box', value: 'fa-solid fa-box', keywords: ['box', 'package', 'delivery', 'shipping'] },
  { name: 'Boxes', value: 'fa-solid fa-boxes-stacked', keywords: ['boxes', 'packages', 'storage', 'inventory'] },
  { name: 'Truck', value: 'fa-solid fa-truck', keywords: ['truck', 'delivery', 'shipping', 'transport'] },
  { name: 'Truck Fast', value: 'fa-solid fa-truck-fast', keywords: ['truck', 'delivery', 'fast', 'shipping'] },
  { name: 'Trash', value: 'fa-solid fa-trash', keywords: ['trash', 'delete', 'waste', 'garbage'] },
  { name: 'Recycle', value: 'fa-solid fa-recycle', keywords: ['recycle', 'environment', 'green', 'eco'] },
  { name: 'Battery', value: 'fa-solid fa-battery-full', keywords: ['battery', 'power', 'energy', 'charge'] },
  { name: 'Plug', value: 'fa-solid fa-plug', keywords: ['plug', 'electricity', 'power', 'charge'] },
  { name: 'Lightbulb', value: 'fa-solid fa-lightbulb', keywords: ['lightbulb', 'idea', 'light', 'electricity'] },
  { name: 'Ellipsis', value: 'fa-solid fa-ellipsis', keywords: ['more', 'options', 'menu', 'dots'] },
  { name: 'Circle', value: 'fa-solid fa-circle', keywords: ['circle', 'dot', 'default', 'round'] },
  { name: 'Square', value: 'fa-solid fa-square', keywords: ['square', 'box', 'shape', 'rectangle'] },
  { name: 'Triangle', value: 'fa-solid fa-triangle-exclamation', keywords: ['triangle', 'warning', 'alert', 'danger'] },
  { name: 'Question', value: 'fa-solid fa-circle-question', keywords: ['question', 'help', 'support', 'info'] },
  { name: 'Info', value: 'fa-solid fa-circle-info', keywords: ['info', 'information', 'help', 'support'] },
  { name: 'Check', value: 'fa-solid fa-circle-check', keywords: ['check', 'done', 'success', 'complete'] },
  { name: 'X Mark', value: 'fa-solid fa-circle-xmark', keywords: ['x', 'close', 'cancel', 'error'] },
  { name: 'Exclamation', value: 'fa-solid fa-circle-exclamation', keywords: ['exclamation', 'warning', 'alert', 'important'] },
  { name: 'Plus', value: 'fa-solid fa-circle-plus', keywords: ['plus', 'add', 'new', 'create'] },
  { name: 'Minus', value: 'fa-solid fa-circle-minus', keywords: ['minus', 'remove', 'delete', 'subtract'] },
  { name: 'Gear', value: 'fa-solid fa-gear', keywords: ['gear', 'settings', 'preferences', 'config'] },
  { name: 'Gears', value: 'fa-solid fa-gears', keywords: ['gears', 'settings', 'preferences', 'config'] },
  { name: 'Wrench', value: 'fa-solid fa-wrench', keywords: ['wrench', 'settings', 'tool', 'repair'] },
  { name: 'Sliders', value: 'fa-solid fa-sliders', keywords: ['sliders', 'settings', 'controls', 'adjust'] },
  { name: 'Filter', value: 'fa-solid fa-filter', keywords: ['filter', 'sort', 'search', 'refine'] },
  { name: 'Magnifying Glass', value: 'fa-solid fa-magnifying-glass', keywords: ['search', 'magnify', 'find', 'look'] },
  { name: 'X Mark', value: 'fa-solid fa-xmark', keywords: ['close', 'cancel', 'delete', 'remove'] },
  { name: 'Check', value: 'fa-solid fa-check', keywords: ['check', 'done', 'success', 'approve'] },
  { name: 'Plus', value: 'fa-solid fa-plus', keywords: ['plus', 'add', 'new', 'create'] },
  { name: 'Minus', value: 'fa-solid fa-minus', keywords: ['minus', 'remove', 'delete', 'subtract'] },
  { name: 'Arrow Right', value: 'fa-solid fa-arrow-right', keywords: ['arrow', 'right', 'next', 'forward'] },
  { name: 'Arrow Left', value: 'fa-solid fa-arrow-left', keywords: ['arrow', 'left', 'back', 'previous'] },
  { name: 'Arrow Up', value: 'fa-solid fa-arrow-up', keywords: ['arrow', 'up', 'top', 'increase'] },
  { name: 'Arrow Down', value: 'fa-solid fa-arrow-down', keywords: ['arrow', 'down', 'bottom', 'decrease'] },
  { name: 'Download', value: 'fa-solid fa-download', keywords: ['download', 'save', 'get', 'file'] },
  { name: 'Upload', value: 'fa-solid fa-upload', keywords: ['upload', 'send', 'share', 'file'] },
  { name: 'Share', value: 'fa-solid fa-share', keywords: ['share', 'send', 'forward', 'social'] },
  { name: 'Copy', value: 'fa-solid fa-copy', keywords: ['copy', 'duplicate', 'clone', 'file'] },
  { name: 'Paste', value: 'fa-solid fa-paste', keywords: ['paste', 'insert', 'clipboard', 'file'] },
  { name: 'Cut', value: 'fa-solid fa-scissors', keywords: ['cut', 'remove', 'delete', 'trim'] },
  { name: 'Edit', value: 'fa-solid fa-pen-to-square', keywords: ['edit', 'modify', 'change', 'update'] },
  { name: 'Save', value: 'fa-solid fa-floppy-disk', keywords: ['save', 'store', 'disk', 'file'] },
  { name: 'Folder', value: 'fa-solid fa-folder', keywords: ['folder', 'directory', 'organize', 'file'] },
  { name: 'Folder Open', value: 'fa-solid fa-folder-open', keywords: ['folder', 'open', 'directory', 'file'] },
  { name: 'File', value: 'fa-solid fa-file', keywords: ['file', 'document', 'paper', 'page'] },
  { name: 'File Lines', value: 'fa-solid fa-file-lines', keywords: ['file', 'document', 'text', 'page'] },
  { name: 'File Image', value: 'fa-solid fa-file-image', keywords: ['file', 'image', 'photo', 'picture'] },
  { name: 'File Video', value: 'fa-solid fa-file-video', keywords: ['file', 'video', 'movie', 'media'] },
  { name: 'File Audio', value: 'fa-solid fa-file-audio', keywords: ['file', 'audio', 'music', 'sound'] },
  { name: 'File PDF', value: 'fa-solid fa-file-pdf', keywords: ['file', 'pdf', 'document', 'read'] },
  { name: 'File Word', value: 'fa-solid fa-file-word', keywords: ['file', 'word', 'document', 'text'] },
  { name: 'File Excel', value: 'fa-solid fa-file-excel', keywords: ['file', 'excel', 'spreadsheet', 'data'] },
  { name: 'File Powerpoint', value: 'fa-solid fa-file-powerpoint', keywords: ['file', 'powerpoint', 'presentation', 'slide'] },
  { name: 'File Archive', value: 'fa-solid fa-file-zipper', keywords: ['file', 'archive', 'zip', 'compress'] },
  { name: 'Link', value: 'fa-solid fa-link', keywords: ['link', 'url', 'connect', 'chain'] },
  { name: 'Paperclip', value: 'fa-solid fa-paperclip', keywords: ['paperclip', 'attach', 'file', 'attachment'] },
  { name: 'Bookmark', value: 'fa-solid fa-bookmark', keywords: ['bookmark', 'save', 'favorite', 'mark'] },
  { name: 'Tag', value: 'fa-solid fa-tag', keywords: ['tag', 'label', 'category', 'mark'] },
  { name: 'Tags', value: 'fa-solid fa-tags', keywords: ['tags', 'labels', 'categories', 'marks'] },
  { name: 'Flag', value: 'fa-solid fa-flag', keywords: ['flag', 'mark', 'important', 'country'] },
  { name: 'Star', value: 'fa-solid fa-star', keywords: ['star', 'favorite', 'rating', 'important'] },
  { name: 'Heart', value: 'fa-solid fa-heart', keywords: ['heart', 'love', 'favorite', 'like'] },
  { name: 'Thumbs Up', value: 'fa-solid fa-thumbs-up', keywords: ['thumbs', 'up', 'like', 'approve'] },
  { name: 'Thumbs Down', value: 'fa-solid fa-thumbs-down', keywords: ['thumbs', 'down', 'dislike', 'reject'] },
  { name: 'Share Nodes', value: 'fa-solid fa-share-nodes', keywords: ['share', 'nodes', 'network', 'connect'] },
  { name: 'Retweet', value: 'fa-solid fa-retweet', keywords: ['retweet', 'share', 'repeat', 'forward'] },
  { name: 'Comment', value: 'fa-solid fa-comment', keywords: ['comment', 'message', 'chat', 'talk'] },
  { name: 'Comments', value: 'fa-solid fa-comments', keywords: ['comments', 'messages', 'chat', 'discussion'] },
  { name: 'Reply', value: 'fa-solid fa-reply', keywords: ['reply', 'answer', 'respond', 'message'] },
  { name: 'Forward', value: 'fa-solid fa-forward', keywords: ['forward', 'send', 'next', 'skip'] },
  { name: 'Backward', value: 'fa-solid fa-backward', keywords: ['backward', 'previous', 'rewind', 'back'] },
  { name: 'Play', value: 'fa-solid fa-play', keywords: ['play', 'start', 'begin', 'media'] },
  { name: 'Pause', value: 'fa-solid fa-pause', keywords: ['pause', 'stop', 'wait', 'media'] },
  { name: 'Stop', value: 'fa-solid fa-stop', keywords: ['stop', 'end', 'media', 'player'] },
  { name: 'Volume', value: 'fa-solid fa-volume-high', keywords: ['volume', 'sound', 'audio', 'speaker'] },
  { name: 'Volume Mute', value: 'fa-solid fa-volume-xmark', keywords: ['volume', 'mute', 'silent', 'audio'] },
  { name: 'Volume Low', value: 'fa-solid fa-volume-low', keywords: ['volume', 'low', 'quiet', 'audio'] },
  { name: 'Volume High', value: 'fa-solid fa-volume-high', keywords: ['volume', 'high', 'loud', 'audio'] },
  { name: 'Shuffle', value: 'fa-solid fa-shuffle', keywords: ['shuffle', 'random', 'mix', 'music'] },
  { name: 'Repeat', value: 'fa-solid fa-repeat', keywords: ['repeat', 'loop', 'again', 'music'] },
  { name: 'Rotate', value: 'fa-solid fa-rotate', keywords: ['rotate', 'turn', 'spin', 'refresh'] },
  { name: 'Rotate Right', value: 'fa-solid fa-rotate-right', keywords: ['rotate', 'right', 'turn', 'refresh'] },
  { name: 'Rotate Left', value: 'fa-solid fa-rotate-left', keywords: ['rotate', 'left', 'turn', 'refresh'] },
  { name: 'Refresh', value: 'fa-solid fa-arrow-rotate-right', keywords: ['refresh', 'reload', 'update', 'sync'] },
  { name: 'Sync', value: 'fa-solid fa-arrows-rotate', keywords: ['sync', 'synchronize', 'update', 'refresh'] },
  { name: 'Undo', value: 'fa-solid fa-arrow-rotate-left', keywords: ['undo', 'back', 'reverse', 'cancel'] },
  { name: 'Redo', value: 'fa-solid fa-arrow-rotate-right', keywords: ['redo', 'forward', 'repeat', 'again'] },
  { name: 'Trash', value: 'fa-solid fa-trash', keywords: ['trash', 'delete', 'remove', 'waste'] },
  { name: 'Trash Can', value: 'fa-solid fa-trash-can', keywords: ['trash', 'can', 'delete', 'waste'] },
  { name: 'Archive', value: 'fa-solid fa-box-archive', keywords: ['archive', 'store', 'save', 'keep'] },
  { name: 'Inbox', value: 'fa-solid fa-inbox', keywords: ['inbox', 'mail', 'messages', 'receive'] },
  { name: 'Envelope', value: 'fa-solid fa-envelope', keywords: ['envelope', 'mail', 'email', 'message'] },
  { name: 'Envelope Open', value: 'fa-solid fa-envelope-open', keywords: ['envelope', 'open', 'mail', 'read'] },
  { name: 'Paper Plane', value: 'fa-solid fa-paper-plane', keywords: ['paper', 'plane', 'send', 'message'] },
  { name: 'At', value: 'fa-solid fa-at', keywords: ['at', 'email', 'mention', 'address'] },
  { name: 'Hashtag', value: 'fa-solid fa-hashtag', keywords: ['hashtag', 'tag', 'social', 'trend'] },
  { name: 'Bell', value: 'fa-solid fa-bell', keywords: ['bell', 'notification', 'alert', 'reminder'] },
  { name: 'Bell Slash', value: 'fa-solid fa-bell-slash', keywords: ['bell', 'slash', 'mute', 'notification'] },
  { name: 'Rss', value: 'fa-solid fa-rss', keywords: ['rss', 'feed', 'news', 'subscribe'] },
  { name: 'Wifi', value: 'fa-solid fa-wifi', keywords: ['wifi', 'internet', 'connection', 'network'] },
  { name: 'Signal', value: 'fa-solid fa-signal', keywords: ['signal', 'connection', 'network', 'strength'] },
  { name: 'Bluetooth', value: 'fa-brands fa-bluetooth', keywords: ['bluetooth', 'wireless', 'connection', 'device'] },
  { name: 'USB', value: 'fa-solid fa-usb', keywords: ['usb', 'port', 'connection', 'device'] },
  { name: 'Plug', value: 'fa-solid fa-plug', keywords: ['plug', 'electricity', 'power', 'charge'] },
  { name: 'Battery', value: 'fa-solid fa-battery-full', keywords: ['battery', 'power', 'energy', 'charge'] },
  { name: 'Battery Half', value: 'fa-solid fa-battery-half', keywords: ['battery', 'half', 'power', 'energy'] },
  { name: 'Battery Empty', value: 'fa-solid fa-battery-empty', keywords: ['battery', 'empty', 'power', 'low'] },
  { name: 'Power Off', value: 'fa-solid fa-power-off', keywords: ['power', 'off', 'shutdown', 'turn'] },
  { name: 'Toggle On', value: 'fa-solid fa-toggle-on', keywords: ['toggle', 'on', 'switch', 'enable'] },
  { name: 'Toggle Off', value: 'fa-solid fa-toggle-off', keywords: ['toggle', 'off', 'switch', 'disable'] },
  { name: 'Light Switch', value: 'fa-solid fa-light-switch', keywords: ['light', 'switch', 'on', 'off'] },
  { name: 'Fan', value: 'fa-solid fa-fan', keywords: ['fan', 'air', 'cooling', 'ventilation'] },
  { name: 'Temperature', value: 'fa-solid fa-temperature-high', keywords: ['temperature', 'hot', 'weather', 'thermometer'] },
  { name: 'Temperature Low', value: 'fa-solid fa-temperature-low', keywords: ['temperature', 'low', 'cold', 'thermometer'] },
  { name: 'Droplet', value: 'fa-solid fa-droplet', keywords: ['droplet', 'water', 'liquid', 'moisture'] },
  { name: 'Droplet Slash', value: 'fa-solid fa-droplet-slash', keywords: ['droplet', 'slash', 'dry', 'no water'] },
  { name: 'Wind', value: 'fa-solid fa-wind', keywords: ['wind', 'air', 'weather', 'breeze'] },
  { name: 'Cloud', value: 'fa-solid fa-cloud', keywords: ['cloud', 'weather', 'sky', 'overcast'] },
  { name: 'Cloud Rain', value: 'fa-solid fa-cloud-rain', keywords: ['cloud', 'rain', 'weather', 'precipitation'] },
  { name: 'Cloud Sun', value: 'fa-solid fa-cloud-sun', keywords: ['cloud', 'sun', 'weather', 'partly'] },
  { name: 'Cloud Moon', value: 'fa-solid fa-cloud-moon', keywords: ['cloud', 'moon', 'weather', 'night'] },
  { name: 'Sun', value: 'fa-solid fa-sun', keywords: ['sun', 'weather', 'day', 'bright'] },
  { name: 'Moon', value: 'fa-solid fa-moon', keywords: ['moon', 'weather', 'night', 'dark'] },
  { name: 'Star', value: 'fa-solid fa-star', keywords: ['star', 'sky', 'night', 'bright'] },
  { name: 'Umbrella', value: 'fa-solid fa-umbrella', keywords: ['umbrella', 'rain', 'weather', 'protection'] },
  { name: 'Umbrella Beach', value: 'fa-solid fa-umbrella-beach', keywords: ['umbrella', 'beach', 'vacation', 'summer'] },
  { name: 'Snowflake', value: 'fa-solid fa-snowflake', keywords: ['snowflake', 'snow', 'winter', 'cold'] },
  { name: 'Icicles', value: 'fa-solid fa-icicles', keywords: ['icicles', 'ice', 'winter', 'cold'] },
  { name: 'Fire', value: 'fa-solid fa-fire', keywords: ['fire', 'flame', 'hot', 'burn'] },
  { name: 'Flame', value: 'fa-solid fa-fire-flame-curved', keywords: ['flame', 'fire', 'hot', 'burn'] },
  { name: 'Smoking', value: 'fa-solid fa-smoking', keywords: ['smoking', 'cigarette', 'tobacco', 'bad'] },
  { name: 'Ban Smoking', value: 'fa-solid fa-ban-smoking', keywords: ['ban', 'smoking', 'no', 'prohibited'] },
  { name: 'Martini Glass', value: 'fa-solid fa-martini-glass', keywords: ['martini', 'glass', 'cocktail', 'drink'] },
  { name: 'Wine Glass', value: 'fa-solid fa-wine-glass', keywords: ['wine', 'glass', 'drink', 'alcohol'] },
  { name: 'Wine Bottle', value: 'fa-solid fa-wine-bottle', keywords: ['wine', 'bottle', 'alcohol', 'drink'] },
  { name: 'Beer Mug', value: 'fa-solid fa-beer-mug-empty', keywords: ['beer', 'mug', 'drink', 'alcohol'] },
  { name: 'Champagne Glasses', value: 'fa-solid fa-champagne-glasses', keywords: ['champagne', 'glasses', 'celebration', 'drink'] },
  { name: 'Whiskey Glass', value: 'fa-solid fa-whiskey-glass', keywords: ['whiskey', 'glass', 'drink', 'alcohol'] },
  { name: 'Cocktail', value: 'fa-solid fa-cocktail', keywords: ['cocktail', 'drink', 'alcohol', 'bar'] },
  { name: 'Coffee', value: 'fa-solid fa-mug-hot', keywords: ['coffee', 'mug', 'hot', 'drink'] },
  { name: 'Mug Saucer', value: 'fa-solid fa-mug-saucer', keywords: ['mug', 'saucer', 'coffee', 'tea'] },
  { name: 'Teapot', value: 'fa-solid fa-teapot', keywords: ['teapot', 'tea', 'drink', 'hot'] },
  { name: 'Jug', value: 'fa-solid fa-jug-detergent', keywords: ['jug', 'container', 'liquid'] },
  { name: 'Bottle', value: 'fa-solid fa-bottle-water', keywords: ['bottle', 'water', 'drink', 'container'] },
  { name: 'Glass Water', value: 'fa-solid fa-glass-water', keywords: ['glass', 'water', 'drink', 'liquid'] },
  { name: 'Ice', value: 'fa-solid fa-ice-cream', keywords: ['ice', 'cream', 'dessert', 'sweet'] },
  { name: 'Cookie', value: 'fa-solid fa-cookie', keywords: ['cookie', 'dessert', 'sweet', 'snack'] },
  { name: 'Cake', value: 'fa-solid fa-cake-candles', keywords: ['cake', 'candles', 'birthday', 'celebration'] },
  { name: 'Candy', value: 'fa-solid fa-candy-cane', keywords: ['candy', 'cane', 'sweet', 'christmas'] },
  { name: 'Lollipop', value: 'fa-solid fa-lollipop', keywords: ['lollipop', 'candy', 'sweet', 'treat'] },
  { name: 'Gift', value: 'fa-solid fa-gift', keywords: ['gift', 'present', 'celebration', 'birthday'] },
  { name: 'Party Horn', value: 'fa-solid fa-party-horn', keywords: ['party', 'horn', 'celebration', 'festive'] },
  { name: 'Confetti', value: 'fa-solid fa-party-horn', keywords: ['confetti', 'party', 'celebration', 'festive'] },
  { name: 'Balloon', value: 'fa-solid fa-balloon', keywords: ['balloon', 'party', 'celebration', 'birthday'] },
  { name: 'Crown', value: 'fa-solid fa-crown', keywords: ['crown', 'king', 'queen', 'royal'] },
  { name: 'Gem', value: 'fa-solid fa-gem', keywords: ['gem', 'diamond', 'jewelry', 'precious'] },
  { name: 'Ring', value: 'fa-solid fa-ring', keywords: ['ring', 'jewelry', 'wedding', 'engagement'] },
  { name: 'Necklace', value: 'fa-solid fa-necklace', keywords: ['necklace', 'jewelry', 'accessory', 'fashion'] },
  { name: 'Earrings', value: 'fa-solid fa-earrings', keywords: ['earrings', 'jewelry', 'accessory', 'fashion'] },
  { name: 'Watch', value: 'fa-solid fa-watch', keywords: ['watch', 'time', 'wrist', 'accessory'] },
  { name: 'Clock', value: 'fa-solid fa-clock', keywords: ['clock', 'time', 'watch', 'schedule'] },
  { name: 'Stopwatch', value: 'fa-solid fa-stopwatch', keywords: ['stopwatch', 'timer', 'time', 'measure'] },
  { name: 'Hourglass', value: 'fa-solid fa-hourglass', keywords: ['hourglass', 'time', 'timer', 'sand'] },
  { name: 'Calendar', value: 'fa-solid fa-calendar', keywords: ['calendar', 'date', 'event', 'schedule'] },
  { name: 'Calendar Days', value: 'fa-solid fa-calendar-days', keywords: ['calendar', 'days', 'date', 'schedule'] },
  { name: 'Calendar Check', value: 'fa-solid fa-calendar-check', keywords: ['calendar', 'check', 'event', 'done'] },
  { name: 'Calendar Plus', value: 'fa-solid fa-calendar-plus', keywords: ['calendar', 'plus', 'add', 'event'] },
  { name: 'Calendar Minus', value: 'fa-solid fa-calendar-minus', keywords: ['calendar', 'minus', 'remove', 'event'] },
  { name: 'Calendar X Mark', value: 'fa-solid fa-calendar-xmark', keywords: ['calendar', 'x', 'mark', 'cancel'] },
  { name: 'Clock', value: 'fa-solid fa-clock', keywords: ['clock', 'time', 'watch', 'schedule'] },
  { name: 'Bell', value: 'fa-solid fa-bell', keywords: ['bell', 'notification', 'alert', 'reminder'] },
  { name: 'Bell Slash', value: 'fa-solid fa-bell-slash', keywords: ['bell', 'slash', 'mute', 'notification'] },
  { name: 'Alarm Clock', value: 'fa-solid fa-clock', keywords: ['alarm', 'clock', 'wake', 'reminder'] },
  { name: 'Timer', value: 'fa-solid fa-hourglass', keywords: ['timer', 'hourglass', 'time', 'countdown'] },
  { name: 'Stopwatch', value: 'fa-solid fa-stopwatch', keywords: ['stopwatch', 'timer', 'time', 'measure'] },
  { name: 'History', value: 'fa-solid fa-clock-rotate-left', keywords: ['history', 'past', 'time', 'previous'] },
  { name: 'Future', value: 'fa-solid fa-clock', keywords: ['future', 'time', 'coming', 'next'] },
  { name: 'Today', value: 'fa-solid fa-calendar-day', keywords: ['today', 'date', 'current', 'now'] },
  { name: 'Week', value: 'fa-solid fa-calendar-week', keywords: ['week', 'calendar', 'days', 'schedule'] },
  { name: 'Month', value: 'fa-solid fa-calendar', keywords: ['month', 'calendar', 'date', 'schedule'] },
  { name: 'Year', value: 'fa-solid fa-calendar', keywords: ['year', 'calendar', 'date', 'schedule'] },
  { name: 'Schedule', value: 'fa-solid fa-calendar-days', keywords: ['schedule', 'calendar', 'plan', 'event'] },
  { name: 'Event', value: 'fa-solid fa-calendar-check', keywords: ['event', 'calendar', 'date', 'meeting'] },
  { name: 'Meeting', value: 'fa-solid fa-users', keywords: ['meeting', 'people', 'group', 'conference'] },
  { name: 'Conference', value: 'fa-solid fa-users-line', keywords: ['conference', 'meeting', 'people', 'group'] },
  { name: 'Presentation', value: 'fa-solid fa-presentation-screen', keywords: ['presentation', 'screen', 'slide', 'show'] },
  { name: 'Screen', value: 'fa-solid fa-tv', keywords: ['screen', 'tv', 'display', 'monitor'] },
  { name: 'Projector', value: 'fa-solid fa-projector', keywords: ['projector', 'presentation', 'screen', 'display'] },
  { name: 'Microphone', value: 'fa-solid fa-microphone', keywords: ['microphone', 'audio', 'sound', 'record'] },
  { name: 'Microphone Slash', value: 'fa-solid fa-microphone-slash', keywords: ['microphone', 'slash', 'mute', 'audio'] },
  { name: 'Headphones', value: 'fa-solid fa-headphones', keywords: ['headphones', 'audio', 'sound', 'listen'] },
  { name: 'Earbuds', value: 'fa-solid fa-earbuds', keywords: ['earbuds', 'headphones', 'audio', 'wireless'] },
  { name: 'Speaker', value: 'fa-solid fa-volume-high', keywords: ['speaker', 'audio', 'sound', 'volume'] },
  { name: 'Radio', value: 'fa-solid fa-radio', keywords: ['radio', 'audio', 'music', 'broadcast'] },
  { name: 'Music', value: 'fa-solid fa-music', keywords: ['music', 'song', 'audio', 'sound'] },
  { name: 'Guitar', value: 'fa-solid fa-guitar', keywords: ['guitar', 'music', 'instrument', 'play'] },
  { name: 'Drum', value: 'fa-solid fa-drum', keywords: ['drum', 'music', 'instrument', 'beat'] },
  { name: 'Piano', value: 'fa-solid fa-music', keywords: ['piano', 'music', 'instrument', 'keys'] },
  { name: 'Violin', value: 'fa-solid fa-music', keywords: ['violin', 'music', 'instrument', 'strings'] },
  { name: 'Saxophone', value: 'fa-solid fa-music', keywords: ['saxophone', 'music', 'instrument', 'jazz'] },
  { name: 'Trumpet', value: 'fa-solid fa-music', keywords: ['trumpet', 'music', 'instrument', 'brass'] },
  { name: 'Harmonica', value: 'fa-solid fa-music', keywords: ['harmonica', 'music', 'instrument', 'blues'] },
  { name: 'Accordion', value: 'fa-solid fa-music', keywords: ['accordion', 'music', 'instrument', 'folk'] },
  { name: 'Banjo', value: 'fa-solid fa-music', keywords: ['banjo', 'music', 'instrument', 'country'] },
  { name: 'Ukulele', value: 'fa-solid fa-music', keywords: ['ukulele', 'music', 'instrument', 'hawaii'] },
  { name: 'Mandolin', value: 'fa-solid fa-music', keywords: ['mandolin', 'music', 'instrument', 'strings'] },
  { name: 'Lute', value: 'fa-solid fa-music', keywords: ['lute', 'music', 'instrument', 'medieval'] },
  { name: 'Harp', value: 'fa-solid fa-music', keywords: ['harp', 'music', 'instrument', 'strings'] },
  { name: 'Xylophone', value: 'fa-solid fa-music', keywords: ['xylophone', 'music', 'instrument', 'percussion'] },
  { name: 'Maracas', value: 'fa-solid fa-music', keywords: ['maracas', 'music', 'instrument', 'percussion'] },
  { name: 'Tambourine', value: 'fa-solid fa-music', keywords: ['tambourine', 'music', 'instrument', 'percussion'] },
  { name: 'Triangle', value: 'fa-solid fa-triangle', keywords: ['triangle', 'music', 'instrument', 'percussion'] },
  { name: 'Cymbal', value: 'fa-solid fa-music', keywords: ['cymbal', 'music', 'instrument', 'percussion'] },
  { name: 'Bell', value: 'fa-solid fa-bell', keywords: ['bell', 'music', 'instrument', 'percussion'] },
  { name: 'Gong', value: 'fa-solid fa-music', keywords: ['gong', 'music', 'instrument', 'percussion'] },
  { name: 'Whistle', value: 'fa-solid fa-music', keywords: ['whistle', 'music', 'instrument', 'sound'] },
  { name: 'Horn', value: 'fa-solid fa-music', keywords: ['horn', 'music', 'instrument', 'brass'] },
  { name: 'Bugle', value: 'fa-solid fa-music', keywords: ['bugle', 'music', 'instrument', 'military'] },
  { name: 'Oboe', value: 'fa-solid fa-music', keywords: ['oboe', 'music', 'instrument', 'woodwind'] },
  { name: 'Clarinet', value: 'fa-solid fa-music', keywords: ['clarinet', 'music', 'instrument', 'woodwind'] },
  { name: 'Flute', value: 'fa-solid fa-music', keywords: ['flute', 'music', 'instrument', 'woodwind'] },
  { name: 'Piccolo', value: 'fa-solid fa-music', keywords: ['piccolo', 'music', 'instrument', 'woodwind'] },
  { name: 'Recorder', value: 'fa-solid fa-music', keywords: ['recorder', 'music', 'instrument', 'woodwind'] },
  { name: 'Bassoon', value: 'fa-solid fa-music', keywords: ['bassoon', 'music', 'instrument', 'woodwind'] },
  { name: 'Saxophone', value: 'fa-solid fa-music', keywords: ['saxophone', 'music', 'instrument', 'jazz'] },
  { name: 'Trombone', value: 'fa-solid fa-music', keywords: ['trombone', 'music', 'instrument', 'brass'] },
  { name: 'Tuba', value: 'fa-solid fa-music', keywords: ['tuba', 'music', 'instrument', 'brass'] },
  { name: 'French Horn', value: 'fa-solid fa-music', keywords: ['french', 'horn', 'music', 'instrument'] },
  { name: 'Euphonium', value: 'fa-solid fa-music', keywords: ['euphonium', 'music', 'instrument', 'brass'] },
  { name: 'Baritone', value: 'fa-solid fa-music', keywords: ['baritone', 'music', 'instrument', 'brass'] },
  { name: 'Cornet', value: 'fa-solid fa-music', keywords: ['cornet', 'music', 'instrument', 'brass'] },
  { name: 'Flugelhorn', value: 'fa-solid fa-music', keywords: ['flugelhorn', 'music', 'instrument', 'brass'] },
  { name: 'Mellophone', value: 'fa-solid fa-music', keywords: ['mellophone', 'music', 'instrument', 'brass'] },
  { name: 'Alto Horn', value: 'fa-solid fa-music', keywords: ['alto', 'horn', 'music', 'instrument'] },
  { name: 'Tenor Horn', value: 'fa-solid fa-music', keywords: ['tenor', 'horn', 'music', 'instrument'] },
  { name: 'Baritone Horn', value: 'fa-solid fa-music', keywords: ['baritone', 'horn', 'music', 'instrument'] },
  { name: 'Bass Trombone', value: 'fa-solid fa-music', keywords: ['bass', 'trombone', 'music', 'instrument'] },
  { name: 'Contrabass Trombone', value: 'fa-solid fa-music', keywords: ['contrabass', 'trombone', 'music', 'instrument'] },
  { name: 'Bass Tuba', value: 'fa-solid fa-music', keywords: ['bass', 'tuba', 'music', 'instrument'] },
  { name: 'Contrabass Tuba', value: 'fa-solid fa-music', keywords: ['contrabass', 'tuba', 'music', 'instrument'] },
  { name: 'Sousaphone', value: 'fa-solid fa-music', keywords: ['sousaphone', 'music', 'instrument', 'brass'] },
  { name: 'Helicon', value: 'fa-solid fa-music', keywords: ['helicon', 'music', 'instrument', 'brass'] },
  { name: 'Wagner Tuba', value: 'fa-solid fa-music', keywords: ['wagner', 'tuba', 'music', 'instrument'] },
  { name: 'Alphorn', value: 'fa-solid fa-music', keywords: ['alphorn', 'music', 'instrument', 'alpine'] },
  { name: 'Didgeridoo', value: 'fa-solid fa-music', keywords: ['didgeridoo', 'music', 'instrument', 'australian'] },
  { name: 'Shofar', value: 'fa-solid fa-music', keywords: ['shofar', 'music', 'instrument', 'jewish'] },
  { name: 'Conch', value: 'fa-solid fa-music', keywords: ['conch', 'music', 'instrument', 'shell'] },
  { name: 'Ocarina', value: 'fa-solid fa-music', keywords: ['ocarina', 'music', 'instrument', 'clay'] },
  { name: 'Recorder', value: 'fa-solid fa-music', keywords: ['recorder', 'music', 'instrument', 'woodwind'] },
  { name: 'Pan Flute', value: 'fa-solid fa-music', keywords: ['pan', 'flute', 'music', 'instrument'] },
  { name: 'Tin Whistle', value: 'fa-solid fa-music', keywords: ['tin', 'whistle', 'music', 'instrument'] },
  { name: 'Fife', value: 'fa-solid fa-music', keywords: ['fife', 'music', 'instrument', 'military'] },
  { name: 'Piccolo', value: 'fa-solid fa-music', keywords: ['piccolo', 'music', 'instrument', 'woodwind'] },
  { name: 'Alto Flute', value: 'fa-solid fa-music', keywords: ['alto', 'flute', 'music', 'instrument'] },
  { name: 'Bass Flute', value: 'fa-solid fa-music', keywords: ['bass', 'flute', 'music', 'instrument'] },
  { name: 'Contrabass Flute', value: 'fa-solid fa-music', keywords: ['contrabass', 'flute', 'music', 'instrument'] },
  { name: 'Hyperbass Flute', value: 'fa-solid fa-music', keywords: ['hyperbass', 'flute', 'music', 'instrument'] },
  { name: 'Treble Flute', value: 'fa-solid fa-music', keywords: ['treble', 'flute', 'music', 'instrument'] },
  { name: 'Soprano Flute', value: 'fa-solid fa-music', keywords: ['soprano', 'flute', 'music', 'instrument'] },
  { name: 'Mezzo Soprano Flute', value: 'fa-solid fa-music', keywords: ['mezzo', 'soprano', 'flute', 'music', 'instrument'] },
  { name: 'Alto Flute', value: 'fa-solid fa-music', keywords: ['alto', 'flute', 'music', 'instrument'] },
  { name: 'Tenor Flute', value: 'fa-solid fa-music', keywords: ['tenor', 'flute', 'music', 'instrument'] },
  { name: 'Bass Flute', value: 'fa-solid fa-music', keywords: ['bass', 'flute', 'music', 'instrument'] },
  { name: 'Contrabass Flute', value: 'fa-solid fa-music', keywords: ['contrabass', 'flute', 'music', 'instrument'] },
  { name: 'Hyperbass Flute', value: 'fa-solid fa-music', keywords: ['hyperbass', 'flute', 'music', 'instrument'] },
  { name: 'Subcontrabass Flute', value: 'fa-solid fa-music', keywords: ['subcontrabass', 'flute', 'music', 'instrument'] },
  { name: 'Double Contrabass Flute', value: 'fa-solid fa-music', keywords: ['double', 'contrabass', 'flute', 'music', 'instrument'] },
  { name: 'Hyperbass Flute', value: 'fa-solid fa-music', keywords: ['hyperbass', 'flute', 'music', 'instrument'] },
  { name: 'Subcontrabass Flute', value: 'fa-solid fa-music', keywords: ['subcontrabass', 'flute', 'music', 'instrument'] },
  { name: 'Double Contrabass Flute', value: 'fa-solid fa-music', keywords: ['double', 'contrabass', 'flute', 'music', 'instrument'] },
  { name: 'Hyperbass Flute', value: 'fa-solid fa-music', keywords: ['hyperbass', 'flute', 'music', 'instrument'] },
  { name: 'Subcontrabass Flute', value: 'fa-solid fa-music', keywords: ['subcontrabass', 'flute', 'music', 'instrument'] },
  { name: 'Double Contrabass Flute', value: 'fa-solid fa-music', keywords: ['double', 'contrabass', 'flute', 'music', 'instrument'] },
]

// Filter icons based on search query
const filteredIcons = computed(() => {
  if (!searchQuery.value.trim()) {
    return icons
  }
  
  const query = searchQuery.value.toLowerCase().trim()
  return icons.filter(icon => {
    const nameMatch = icon.name.toLowerCase().includes(query)
    const keywordMatch = icon.keywords.some(keyword => keyword.toLowerCase().includes(query))
    const valueMatch = icon.value.toLowerCase().includes(query)
    
    return nameMatch || keywordMatch || valueMatch
  })
})

// Get Font Awesome icon from class string
const getFontAwesomeIcon = (iconClass: string) => {
  const parsed = parseIconClass(iconClass)
  if (!parsed) {
    return ['fas', 'ellipsis']
  }
  return [parsed.prefix, parsed.icon]
}

// Select icon and emit update
const selectIcon = (iconValue: string) => {
  emit('update:modelValue', iconValue)
  emit('select', iconValue)
}

const handleSearch = () => {
  // Search is handled by computed property
}

const clearSearch = () => {
  searchQuery.value = ''
}
</script>

<style scoped>
.icon-picker {
  width: 100%;
}

/* Custom scrollbar */
.icon-picker :deep(.overflow-y-auto)::-webkit-scrollbar {
  width: 6px;
}

.icon-picker :deep(.overflow-y-auto)::-webkit-scrollbar-track {
  background: rgba(15, 23, 42, 0.5);
  border-radius: 3px;
}

.icon-picker :deep(.overflow-y-auto)::-webkit-scrollbar-thumb {
  background: rgba(148, 163, 184, 0.5);
  border-radius: 3px;
}

.icon-picker :deep(.overflow-y-auto)::-webkit-scrollbar-thumb:hover {
  background: rgba(148, 163, 184, 0.7);
}
</style>
