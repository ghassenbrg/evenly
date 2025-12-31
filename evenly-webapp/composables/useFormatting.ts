export const useFormatting = () => {
  const formatCurrency = (amount: number, currency: string = 'JPY') => {
    if (currency === 'JPY') {
      return `¥${amount.toLocaleString('ja-JP')}`
    }
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency
    }).format(amount)
  }

  const formatDate = (date: string | Date, format: 'short' | 'long' | 'month' = 'short') => {
    const d = typeof date === 'string' ? new Date(date) : date
    if (format === 'month') {
      return d.toLocaleDateString('en-US', { month: 'long', year: 'numeric' })
    }
    if (format === 'long') {
      return d.toLocaleDateString('en-US', { month: 'long', day: 'numeric', year: 'numeric' })
    }
    return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric' })
  }

  const formatMonthYear = (date: string | Date) => {
    const d = typeof date === 'string' ? new Date(date) : date
    return d.toLocaleDateString('en-US', { month: 'long', year: 'numeric' })
  }

  return {
    formatCurrency,
    formatDate,
    formatMonthYear
  }
}

