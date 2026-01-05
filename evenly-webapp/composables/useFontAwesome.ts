/**
 * Utility to parse Font Awesome icon class names from API
 * API provides icons like "fa-solid fa-utensils" or "fa-regular fa-user"
 */
export const useFontAwesome = () => {
  /**
   * Parse Font Awesome icon class string and return icon name
   * @param iconClass - Icon class from API (e.g., "fa-solid fa-utensils")
   * @returns Object with prefix and icon name, or null if invalid
   */
  const parseIconClass = (iconClass: string | null | undefined): { prefix: 'fas' | 'far' | 'fab', icon: string } | null => {
    if (!iconClass) return null
    
    // Remove any extra whitespace
    const clean = iconClass.trim()
    
    // Parse format: "fa-solid fa-utensils" or "fa-regular fa-user" or "fa-brands fa-github"
    const parts = clean.split(' ')
    
    if (parts.length < 2) return null
    
    // Get prefix (fa-solid, fa-regular, fa-brands)
    const prefixPart = parts.find(p => p.startsWith('fa-') && (p.includes('solid') || p.includes('regular') || p.includes('brands')))
    if (!prefixPart) return null
    
    // Get icon name (fa-utensils -> utensils)
    const iconPart = parts.find(p => p.startsWith('fa-') && !p.includes('solid') && !p.includes('regular') && !p.includes('brands'))
    if (!iconPart) return null
    
    // Map prefix
    let prefix: 'fas' | 'far' | 'fab' = 'fas'
    if (prefixPart.includes('regular')) prefix = 'far'
    else if (prefixPart.includes('brands')) prefix = 'fab'
    
    // Extract icon name (remove 'fa-' prefix)
    const iconName = iconPart.replace('fa-', '')
    
    return { prefix, icon: iconName }
  }

  return {
    parseIconClass
  }
}

