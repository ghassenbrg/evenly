import type { Category, CreateCategoryRequest, UpdateCategoryRequest } from '~/types/api'
import { useApi } from '~/utils/api'

export const useCategories = () => {
  const api = useApi()
  const categories = ref<Category[]>([])
  const loading = ref(false)
  const error = ref<Error | null>(null)

  const fetchCategories = async (workspaceId: string) => {
    loading.value = true
    error.value = null
    try {
      categories.value = await api.get<Category[]>(`/api/workspaces/${workspaceId}/categories`)
    } catch (err) {
      error.value = err as Error
      throw err
    } finally {
      loading.value = false
    }
  }

  const getCategory = async (workspaceId: string, categoryId: string) => {
    loading.value = true
    error.value = null
    try {
      return await api.get<Category>(`/api/workspaces/${workspaceId}/categories/${categoryId}`)
    } catch (err) {
      error.value = err as Error
      throw err
    } finally {
      loading.value = false
    }
  }

  const createCategory = async (workspaceId: string, data: CreateCategoryRequest) => {
    loading.value = true
    error.value = null
    try {
      const category = await api.post<Category>(`/api/workspaces/${workspaceId}/categories`, data)
      categories.value.push(category)
      return category
    } catch (err) {
      error.value = err as Error
      throw err
    } finally {
      loading.value = false
    }
  }

  const updateCategory = async (workspaceId: string, categoryId: string, data: UpdateCategoryRequest) => {
    loading.value = true
    error.value = null
    try {
      const category = await api.put<Category>(`/api/workspaces/${workspaceId}/categories/${categoryId}`, data)
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

  const deleteCategory = async (workspaceId: string, categoryId: string) => {
    loading.value = true
    error.value = null
    try {
      await api.delete(`/api/workspaces/${workspaceId}/categories/${categoryId}`)
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

