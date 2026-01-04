import type {
  Category,
  CategoryList,
  CategoryRequest,
  CategoryType
} from '~/types/pockito'
import { useApi } from '~/utils/api'

export const usePockitoCategories = () => {
  const config = useRuntimeConfig()
  const api = useApi(config.public.pockitoApiBase || config.public.apiBase)
  const categories = useState<Category[] | null>('pockito:categories', () => null)
  const loading = useState<boolean>('pockito:categories:loading', () => false)
  const error = useState<Error | null>('pockito:categories:error', () => null)

  const begin = () => {
    loading.value = true
    error.value = null
  }
  const end = () => {
    loading.value = false
  }

  const loadCategories = async () => {
    begin()
    try {
      const response = await api.get<CategoryList>('/api/categories')
      categories.value = [...(response.categories || [])]
      return categories.value
    } catch (err) {
      error.value = err as Error
      throw err
    } finally {
      end()
    }
  }

  const createCategory = async (request: CategoryRequest) => {
    begin()
    try {
      const created = await api.post<Category>('/api/categories', request)
      categories.value = [created, ...(categories.value || [])]
      return created
    } catch (err) {
      error.value = err as Error
      throw err
    } finally {
      end()
    }
  }

  const getCategory = async (categoryId: string) => {
    begin()
    try {
      const category = await api.get<Category>(`/api/categories/${categoryId}`)
      const list = categories.value ?? []
      const idx = list.findIndex(c => c.id === category.id)
      if (idx >= 0) {
        const next = [...list]
        next[idx] = category
        categories.value = next
      } else {
        categories.value = [category, ...list]
      }
      return category
    } catch (err) {
      error.value = err as Error
      throw err
    } finally {
      end()
    }
  }

  const updateCategory = async (categoryId: string, request: CategoryRequest) => {
    begin()
    try {
      const updated = await api.put<Category>(`/api/categories/${categoryId}`, request)
      const list = categories.value ?? []
      const idx = list.findIndex(c => c.id === updated.id)
      if (idx >= 0) {
        const next = [...list]
        next[idx] = updated
        categories.value = next
      }
      return updated
    } catch (err) {
      error.value = err as Error
      throw err
    } finally {
      end()
    }
  }

  const deleteCategory = async (categoryId: string) => {
    begin()
    try {
      await api.delete(`/api/categories/${categoryId}`)
      categories.value = (categories.value || []).filter(c => c.id !== categoryId)
    } catch (err) {
      error.value = err as Error
      throw err
    } finally {
      end()
    }
  }

  const getChildCategories = async (parentCategoryId: string) => {
    return api.get<CategoryList>(`/api/categories/${parentCategoryId}/children`)
  }

  const getCategoriesByType = async (type: CategoryType) => {
    return api.get<CategoryList>(`/api/categories/type/${type}`)
  }

  const getRootCategories = async () => {
    return api.get<CategoryList>('/api/categories/root')
  }

  const getHierarchicalCategories = async () => {
    return api.get<CategoryList>('/api/categories/hierarchical')
  }

  const getHierarchicalCategoriesByType = async (type: CategoryType) => {
    return api.get<CategoryList>(`/api/categories/hierarchical/type/${type}`)
  }

  const getCategoriesByColor = async (color: string) => {
    return api.get<CategoryList>(`/api/categories/color/${color}`)
  }

  return {
    categories: readonly(categories),
    loading: readonly(loading),
    error: readonly(error),
    loadCategories,
    createCategory,
    getCategory,
    updateCategory,
    deleteCategory,
    getChildCategories,
    getCategoriesByType,
    getRootCategories,
    getHierarchicalCategories,
    getHierarchicalCategoriesByType,
    getCategoriesByColor
  }
}
