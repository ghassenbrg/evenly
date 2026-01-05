#!/usr/bin/env node
/**
 * Contract Test / Smoke Test for WireMock API
 * 
 * Verifies that critical endpoints match the endpoints.json contract
 * and return expected response structures.
 */

const BASE_URL = process.env.API_BASE_URL || 'http://localhost:8080'

const endpoints = [
  {
    method: 'GET',
    path: '/api/workspaces',
    expectedFields: ['data'],
    description: 'List workspaces'
  },
  {
    method: 'GET',
    path: '/api/workspaces/ws-couple',
    expectedFields: ['data'],
    description: 'Get workspace by ID'
  },
  {
    method: 'GET',
    path: '/api/workspaces/ws-couple/analytics/balance-summary',
    expectedFields: ['data'],
    description: 'Get balance summary'
  },
  {
    method: 'GET',
    path: '/api/workspaces/ws-couple/analytics/expenses-snapshot?startDate=2025-10-31&endDate=2026-01-31',
    expectedFields: ['data', 'categoriesCount', 'remainingCategoriesCount'],
    description: 'Get expenses snapshot'
  },
  {
    method: 'GET',
    path: '/api/workspaces/ws-couple/analytics/recent-expenses?size=3',
    expectedFields: ['data'],
    description: 'Get recent expenses'
  },
  {
    method: 'GET',
    path: '/api/workspaces/ws-couple/expenses?page=0&size=5&sort=effectiveDate,DESC',
    expectedFields: ['data', 'page', 'sort'],
    description: 'Get paginated expenses'
  },
  {
    method: 'GET',
    path: '/api/workspaces/ws-couple/payments?page=0&size=5&sort=effectiveDate,DESC',
    expectedFields: ['data', 'page', 'sort'],
    description: 'Get paginated payments'
  },
  {
    method: 'GET',
    path: '/api/notifications',
    expectedFields: ['data', 'unreadCount'],
    description: 'Get notifications'
  },
  {
    method: 'GET',
    path: '/api/notifications/unread-count',
    expectedFields: ['data'],
    description: 'Get unread count'
  }
]

async function testEndpoint(endpoint) {
  try {
    const url = `${BASE_URL}${endpoint.path}`
    const response = await fetch(url, {
      method: endpoint.method,
      headers: {
        'Content-Type': 'application/json'
      }
    })

    if (!response.ok) {
      throw new Error(`HTTP ${response.status}: ${response.statusText}`)
    }

    const data = await response.json()

    // Check expected fields
    const missingFields = endpoint.expectedFields.filter(field => !(field in data))
    if (missingFields.length > 0) {
      throw new Error(`Missing expected fields: ${missingFields.join(', ')}`)
    }

    console.log(`✅ ${endpoint.description}`)
    return { success: true, endpoint: endpoint.description }
  } catch (error) {
    console.error(`❌ ${endpoint.description}: ${error.message}`)
    return { success: false, endpoint: endpoint.description, error: error.message }
  }
}

async function runTests() {
  console.log(`\n🧪 Running contract tests against ${BASE_URL}\n`)
  console.log('=' .repeat(60))

  const results = []
  for (const endpoint of endpoints) {
    const result = await testEndpoint(endpoint)
    results.push(result)
    // Small delay to avoid overwhelming the server
    await new Promise(resolve => setTimeout(resolve, 100))
  }

  console.log('\n' + '='.repeat(60))
  const passed = results.filter(r => r.success).length
  const failed = results.filter(r => !r.success).length

  console.log(`\n📊 Results: ${passed} passed, ${failed} failed\n`)

  if (failed > 0) {
    console.log('Failed tests:')
    results.filter(r => !r.success).forEach(r => {
      console.log(`  - ${r.endpoint}: ${r.error}`)
    })
    process.exit(1)
  } else {
    console.log('✅ All tests passed!\n')
    process.exit(0)
  }
}

// Run tests
runTests().catch(error => {
  console.error('Fatal error:', error)
  process.exit(1)
})

