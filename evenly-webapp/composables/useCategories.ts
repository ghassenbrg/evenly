import type { Category, CreateCategoryRequest, UpdateCategoryRequest } from '~/types/api'
import { useApi } from '~/utils/api'

export const useCategories = () => {
  const api = useApi()
  const categories = ref<Category[]>([])
  const loading = ref(false)
  const error = ref<Error | null>(null)

  const fetchCategories = async () => {
    loading.value = true
    error.value = null
    try {
      categories.value = await api.get<Category[]>('/api/categories')
    } catch (err) {
      error.value = err as Error
      throw err
    } finally {
      loading.value = false
    }
  }

  const getCategory = async (categoryId: string) => {
    loading.value = true
    error.value = null
    try {
      return await api.get<Category>(`/api/categories/${categoryId}`)
    } catch (err) {
      error.value = err as Error
      throw err
    } finally {
      loading.value = false
    }
  }

  const createCategory = async (data: CreateCategoryRequest) => {
    loading.value = true
    error.value = null
    try {
      const category = await api.post<Category>('/api/categories', data)
      categories.value.push(category)
      return category
    } catch (err) {
      error.value = err as Error
      throw err
    } finally {
      loading.value = false
    }
  }

  const updateCategory = async (categoryId: string, data: UpdateCategoryRequest) => {
    loading.value = true
    error.value = null
    try {
      const category = await api.put<Category>(`/api/categories/${categoryId}`, data)
      const index = categories.value.findIndex(c => c.id === categoryId)
      if (index !== -1) {
        categories.value[index] = category
      }
      return category
    } catch (err) {
      error.value = err as Error
      throw err
    } finally {
      loading.value = false
    }
  }

  const deleteCategory = async (categoryId: string) => {
    loading.value = true
    error.value = null
    try {
      await api.delete(`/api/categories/${categoryId}`)
      categories.value = categories.value.filter(c => c.id !== categoryId)
    } catch (err) {
      error.value = err as Error
      throw err
    } finally {
      loading.value = false
    }
  }

  const clearCategories = () => {
    categories.value = []
    error.value = null
  }

  return {
    categories: readonly(categories),
    loading: readonly(loading),
    error: readonly(error),
    fetchCategories,
    getCategory,
    createCategory,
    updateCategory,
    deleteCategory,
    clearCategories
  }
}

