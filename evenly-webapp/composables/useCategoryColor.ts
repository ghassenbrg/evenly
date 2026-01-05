/**
 * Utility to convert category color from API to gradient
 */
export const useCategoryColor = () => {
  /**
   * Convert hex color to gradient string
   * @param color - Hex color from API (e.g., "#10b981")
   * @returns Gradient string for CSS
   */
  const colorToGradient = (color: string | null | undefined): string => {
    if (!color) {
      return 'linear-gradient(135deg, #64748b 0%, #475569 100%)' // Default gray
    }
    
    // If already a gradient, return as is
    if (color.includes('gradient')) {
      return color
    }
    
    // Convert hex to RGB for darker shade
    const hex = color.replace('#', '')
    const r = parseInt(hex.substring(0, 2), 16)
    const g = parseInt(hex.substring(2, 4), 16)
    const b = parseInt(hex.substring(4, 6), 16)
    
    // Create darker shade (reduce by ~15%)
    const darkerR = Math.max(0, Math.floor(r * 0.85))
    const darkerG = Math.max(0, Math.floor(g * 0.85))
    const darkerB = Math.max(0, Math.floor(b * 0.85))
    
    const darkerHex = `#${darkerR.toString(16).padStart(2, '0')}${darkerG.toString(16).padStart(2, '0')}${darkerB.toString(16).padStart(2, '0')}`
    
    return `linear-gradient(135deg, ${color} 0%, ${darkerHex} 100%)`
  }

  return {
    colorToGradient
  }
}

