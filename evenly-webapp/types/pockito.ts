// Pockito domain types mirrored from the Angular app to keep API parity.
// Dates are represented as strings because the backend returns ISO strings.

// Enums
export enum Currency {
  JPY = 'JPY',
  CNY = 'CNY',
  HKD = 'HKD',
  PLN = 'PLN',
  CZK = 'CZK',
  HUF = 'HUF',
  RUB = 'RUB',
  TRY = 'TRY',
  KRW = 'KRW',
  SGD = 'SGD',
  TWD = 'TWD',
  THB = 'THB',
  MYR = 'MYR',
  IDR = 'IDR',
  PHP = 'PHP',
  VND = 'VND',
  INR = 'INR',
  PKR = 'PKR',
  BDT = 'BDT',
  LKR = 'LKR',
  NPR = 'NPR',
  MMK = 'MMK',
  KHR = 'KHR',
  LAK = 'LAK',
  BND = 'BND',
  MNT = 'MNT',
  KZT = 'KZT',
  UZS = 'UZS',
  KGS = 'KGS',
  TJS = 'TJS',
  TMT = 'TMT',
  AFN = 'AFN',
  IRR = 'IRR',
  IQD = 'IQD',
  SYP = 'SYP',
  LBP = 'LBP',
  JOD = 'JOD',
  EGP = 'EGP',
  LYD = 'LYD',
  TND = 'TND',
  DZD = 'DZD',
  MAD = 'MAD',
  SDG = 'SDG',
  SSP = 'SSP',
  ETB = 'ETB',
  ERN = 'ERN',
  DJF = 'DJF',
  SOS = 'SOS',
  KES = 'KES',
  UGX = 'UGX',
  TZS = 'TZS',
  RWF = 'RWF',
  BIF = 'BIF',
  CDF = 'CDF',
  XPF = 'XPF',
  NGN = 'NGN',
  XOF = 'XOF',
  ZAR = 'ZAR',
  NAD = 'NAD',
  BWP = 'BWP',
  ZWL = 'ZWL',
  ZMW = 'ZMW',
  MWK = 'MWK',
  MZN = 'MZN',
  MGA = 'MGA',
  MUR = 'MUR',
  SCR = 'SCR',
  KMF = 'KMF',
  STN = 'STN',
  CVE = 'CVE',
  AOA = 'AOA',
  XAF = 'XAF',
  SLL = 'SLL',
  LRD = 'LRD',
  MRO = 'MRO',
  LSL = 'LSL',
  SZL = 'SZL',
  GMD = 'GMD',
  GNF = 'GNF',
  GWP = 'GWP',
  MRT = 'MRT',
  AMD = 'AMD',
  AZN = 'AZN',
  GEL = 'GEL',
  UAH = 'UAH',
  BYN = 'BYN',
  MDL = 'MDL',
  RON = 'RON',
  BGN = 'BGN',
  HRK = 'HRK',
  RSD = 'RSD',
  MKD = 'MKD',
  ALL = 'ALL',
  BAM = 'BAM',
  ISK = 'ISK',
  NOK = 'NOK',
  DKK = 'DKK',
  SEK = 'SEK',
  CHF = 'CHF',
  GBP = 'GBP',
  EUR = 'EUR',
  USD = 'USD',
  CAD = 'CAD',
  MXN = 'MXN',
  GTQ = 'GTQ',
  BZD = 'BZD',
  SVC = 'SVC',
  HNL = 'HNL',
  NIO = 'NIO',
  CRC = 'CRC',
  PAB = 'PAB',
  CUP = 'CUP',
  JMD = 'JMD',
  HTG = 'HTG',
  DOP = 'DOP',
  TTD = 'TTD',
  BBD = 'BBD',
  XCD = 'XCD',
  AWG = 'AWG',
  ANG = 'ANG',
  SRD = 'SRD',
  GYD = 'GYD',
  VEF = 'VEF',
  COP = 'COP',
  PEN = 'PEN',
  CLP = 'CLP',
  ARS = 'ARS',
  UYU = 'UYU',
  PYG = 'PYG',
  BOB = 'BOB',
  VES = 'VES',
  BRL = 'BRL',
  AUD = 'AUD',
  NZD = 'NZD',
  FJD = 'FJD',
  PGK = 'PGK',
  SBD = 'SBD',
  VUV = 'VUV',
  WST = 'WST',
  TOP = 'TOP',
  KID = 'KID',
  TVD = 'TVD',
  NUD = 'NUD',
  MHD = 'MHD',
  FMD = 'FMD',
  PWD = 'PWD'
}

export enum WalletType {
  BANK_ACCOUNT = 'BANK_ACCOUNT',
  CASH = 'CASH',
  CREDIT_CARD = 'CREDIT_CARD',
  SAVINGS = 'SAVINGS',
  CUSTOM = 'CUSTOM'
}

export enum TransactionType {
  INCOME = 'INCOME',
  EXPENSE = 'EXPENSE',
  TRANSFER = 'TRANSFER'
}

export enum CategoryType {
  INCOME = 'INCOME',
  EXPENSE = 'EXPENSE'
}

export enum SubscriptionFrequency {
  DAILY = 'DAILY',
  WEEKLY = 'WEEKLY',
  MONTHLY = 'MONTHLY',
  YEARLY = 'YEARLY'
}

export enum DayOfWeek {
  MONDAY = 'MONDAY',
  TUESDAY = 'TUESDAY',
  WEDNESDAY = 'WEDNESDAY',
  THURSDAY = 'THURSDAY',
  FRIDAY = 'FRIDAY',
  SATURDAY = 'SATURDAY',
  SUNDAY = 'SUNDAY'
}

export enum MonthOfYear {
  JANUARY = 'JANUARY',
  FEBRUARY = 'FEBRUARY',
  MARCH = 'MARCH',
  APRIL = 'APRIL',
  MAY = 'MAY',
  JUNE = 'JUNE',
  JULY = 'JULY',
  AUGUST = 'AUGUST',
  SEPTEMBER = 'SEPTEMBER',
  OCTOBER = 'OCTOBER',
  NOVEMBER = 'NOVEMBER',
  DECEMBER = 'DECEMBER'
}

export type Country = string;

// Wallets
export interface WalletRequest {
  name: string;
  description?: string;
  color?: string;
  initialBalance: number;
  currency: Currency;
  iconUrl?: string;
  goalAmount?: number;
  type: WalletType;
  isDefault: boolean;
}

export interface Wallet {
  id: string;
  username: string;
  name: string;
  description?: string;
  color?: string;
  initialBalance: number;
  balance: number;
  currency: Currency;
  iconUrl?: string;
  goalAmount?: number;
  type: WalletType;
  isDefault: boolean;
  orderPosition: number;
  createdAt: string;
  updatedAt: string;
  active: boolean;
}

export interface WalletList {
  wallets: Wallet[];
  totalCount: number;
}

export interface ReorderWalletsRequest {
  walletIds: string[];
}

// Categories
export interface CategoryRequest {
  name: string;
  color: string;
  categoryType: CategoryType;
  iconUrl?: string;
  parentCategoryId?: string;
}

export interface Category {
  id: string;
  username: string;
  name: string;
  color: string;
  categoryType: CategoryType;
  iconUrl?: string;
  parentCategoryId?: string;
  parentCategoryName?: string;
  createdAt: string;
  updatedAt: string;
  active: boolean;
  childCount: number;
}

export interface CategoryList {
  categories: Category[];
  totalCount: number;
}

// Transactions
export interface TransactionRequest {
  transactionType: TransactionType;
  walletFromId?: string;
  walletToId?: string;
  amount: number;
  exchangeRate: number;
  categoryId?: string;
  note?: string;
  effectiveDate?: string;
}

export interface Transaction {
  id: string;
  username: string;
  transactionType: TransactionType;
  walletFromId?: string;
  walletFromName?: string;
  walletToId?: string;
  walletToName?: string;
  walletFromCurrency?: Currency;
  walletToCurrency?: Currency;
  amount: number;
  exchangeRate: number;
  walletToAmount?: number;
  categoryId?: string;
  categoryName?: string;
  iconUrl?: string;
  note?: string;
  effectiveDate: string;
  subscriptionId?: string;
  subscriptionName?: string;
  createdAt: string;
  updatedAt: string;
}

export interface TransactionDto extends Transaction {}

export interface TransactionList {
  transactions: Transaction[];
  totalCount: number;
}

export interface Pageable {
  page?: number;
  size?: number;
  sort?: string[];
}

export interface PageTransactionDto {
  totalElements: number;
  totalPages: number;
  size: number;
  content: TransactionDto[];
  number: number;
  sort?: Sort;
  numberOfElements: number;
  first: boolean;
  last: boolean;
  pageable?: PageableInfo;
  empty: boolean;
}

export interface Sort {
  empty: boolean;
  unsorted: boolean;
  sorted: boolean;
}

export interface PageableInfo {
  offset: number;
  sort?: Sort;
  unpaged: boolean;
  paged: boolean;
  pageNumber: number;
  pageSize: number;
}

// Subscriptions
export interface SubscriptionRequest {
  name: string;
  iconUrl?: string;
  frequency: SubscriptionFrequency;
  interval: number;
  amount: number;
  currency: Currency;
  startDate: string;
  endDate?: string;
  enabled: boolean;
  categoryId: string;
  dayOfMonth?: number;
  dayOfWeek?: DayOfWeek;
  monthOfYear?: MonthOfYear;
  defaultWalletId: string;
  note?: string;
}

export interface Subscription {
  id: string;
  username: string;
  name: string;
  iconUrl?: string;
  frequency: SubscriptionFrequency;
  interval: number;
  amount: number;
  currency: Currency;
  startDate: string;
  nextDueDate?: string;
  lastPaymentDate?: string;
  endDate?: string;
  enabled: boolean;
  isActive: boolean;
  categoryId: string;
  categoryName?: string;
  dayOfMonth?: number;
  dayOfWeek?: DayOfWeek;
  monthOfYear?: MonthOfYear;
  defaultWalletId: string;
  defaultWalletName?: string;
  note?: string;
  createdAt: string;
  updatedAt: string;
  monthlyEquivalentAmount?: number;
}

export interface PaySubscriptionRequest {
  walletId?: string;
  exchangeRate?: number;
  skip?: boolean;
}

// Users
export interface User {
  username: string;
  country?: Country;
  defaultCurrency?: Currency;
  createdAt: string;
  updatedAt: string;
}
