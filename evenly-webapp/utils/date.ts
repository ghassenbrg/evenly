// Keep date-only values in local time to avoid UTC shifts in UI and API filters.
const DATE_ONLY_REGEX = /^\d{4}-\d{2}-\d{2}$/

const pad = (value: number) => value.toString().padStart(2, '0')

export const isDateOnlyString = (value: string) => DATE_ONLY_REGEX.test(value)

export const toDateOnlyString = (date: Date) => {
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
}

export const toDateOnly = (value: string | Date) => {
  if (typeof value === 'string') {
    if (isDateOnlyString(value)) return value
    return toDateOnlyString(new Date(value))
  }
  return toDateOnlyString(value)
}

export const parseDateOnly = (value: string) => {
  const [year, month, day] = value.split('-').map(Number)
  return new Date(year, month - 1, day, 0, 0, 0, 0)
}

export const startOfLocalDay = (value: string | Date) => {
  const date = typeof value === 'string'
    ? (isDateOnlyString(value) ? parseDateOnly(value) : new Date(value))
    : new Date(value)
  date.setHours(0, 0, 0, 0)
  return date
}

export const endOfLocalDay = (value: string | Date) => {
  const date = typeof value === 'string'
    ? (isDateOnlyString(value) ? parseDateOnly(value) : new Date(value))
    : new Date(value)
  date.setHours(23, 59, 59, 999)
  return date
}

export const formatDateOnly = (
  value: string,
  locale?: string,
  options?: Intl.DateTimeFormatOptions
) => {
  return parseDateOnly(value).toLocaleDateString(locale, options)
}
