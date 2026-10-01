import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

const mocks = vi.hoisted(() => ({
  create: vi.fn(),
  get: vi.fn(),
  post: vi.fn(),
  put: vi.fn(),
  delete: vi.fn(),
  error: vi.fn(),
  success: vi.fn(),
  warning: vi.fn(),
}))

vi.mock('axios', () => ({
  default: { create: mocks.create },
}))
vi.mock('element-plus', () => ({
  ElMessage: { error: mocks.error, success: mocks.success, warning: mocks.warning },
}))

function storage() {
  const values = new Map<string, string>()
  return {
    getItem: (key: string) => values.get(key) ?? null,
    setItem: (key: string, value: string) => values.set(key, value),
    removeItem: (key: string) => values.delete(key),
  }
}

function token(exp = Math.floor(Date.now() / 1000) + 3600) {
  return `header.${btoa(JSON.stringify({ exp }))}.signature`
}

const response = { data: { code: 200, data: null, message: '成功' } }

beforeEach(() => {
  vi.resetModules()
  vi.clearAllMocks()
  vi.stubEnv('VITE_API_BASE_URL', '')
  vi.stubGlobal('localStorage', storage())
  vi.stubGlobal('sessionStorage', storage())
  mocks.create.mockReturnValue(mocks)
  for (const method of [mocks.get, mocks.post, mocks.put, mocks.delete]) {
    method.mockResolvedValue(response)
  }
})

afterEach(() => {
  vi.unstubAllGlobals()
  vi.unstubAllEnvs()
  vi.restoreAllMocks()
})

describe('Gateway 请求兼容', () => {
  it('默认连接 8080 Gateway', async () => {
    await import('../src/net')
    expect(mocks.create).toHaveBeenCalledWith({ baseURL: 'http://localhost:8080', timeout: 10000 })
  })

  it('允许通过环境变量指定 Gateway', async () => {
    vi.stubEnv('VITE_API_BASE_URL', 'http://localhost:18080')
    await import('../src/net')
    expect(mocks.create).toHaveBeenCalledWith({ baseURL: 'http://localhost:18080', timeout: 10000 })
  })

  it('登录沿用 Spring Security 表单格式', async () => {
    const net = await import('../src/net')
    const onSuccess = vi.fn()
    net.login('用户', 'password', onSuccess)
    const [url, form, config] = mocks.post.mock.calls[0]!
    expect(url).toBe('/api/auth/login')
    expect(form.get('username')).toBe('用户')
    expect(form.get('password')).toBe('password')
    expect(config.headers).toEqual({ 'Content-Type': 'application/x-www-form-urlencoded' })
    await vi.waitFor(() => expect(onSuccess).toHaveBeenCalledWith(response))
  })

  it.each(['get', 'post', 'put', 'remove'] as const)('%s 请求携带用户 JWT', async method => {
    const net = await import('../src/net')
    const jwt = token()
    net.storeAccessToken(jwt, true)
    const onSuccess = vi.fn()
    if (method === 'remove') net.remove('/api/post/1', onSuccess)
    else net[method]('/api/post/1', {}, onSuccess)
    const mockMethod = method === 'remove' ? mocks.delete : mocks[method]
    const args = mockMethod.mock.calls[0]!
    expect(args.at(-1).headers).toEqual({ Authorization: `Bearer ${jwt}` })
    await vi.waitFor(() => expect(onSuccess).toHaveBeenCalledWith(response))
  })

  it('AI 请求保留 60 秒等待时间和 JWT', async () => {
    const net = await import('../src/net')
    const jwt = token()
    net.storeAccessToken(jwt, false)
    net.aiChat({ message: '查找帖子' }, vi.fn())
    expect(mocks.post).toHaveBeenCalledWith('/api/ai/chat', { message: '查找帖子' }, {
      headers: { Authorization: `Bearer ${jwt}` }, timeout: 60000,
    })
  })

  it('验证码接口不发送内部服务凭证', async () => {
    const net = await import('../src/net')
    net.askCode('test@example.com', 'register', vi.fn())
    expect(mocks.get).toHaveBeenCalledWith('/api/auth/ask-code', {
      params: { email: 'test@example.com', type: 'register' }, headers: undefined,
    })
  })

  it('RestBean 业务失败调用失败回调', async () => {
    const failure = { data: { code: 400, data: null, message: '请求失败' } }
    mocks.get.mockResolvedValueOnce(failure)
    const net = await import('../src/net')
    const onFailure = vi.fn()
    net.get('/api/post/list', {}, vi.fn(), onFailure)
    await vi.waitFor(() => expect(onFailure).toHaveBeenCalledWith(failure))
  })

  it('HTTP 429/503 错误显示 RestBean 提示', async () => {
    const error = { response: { status: 429, data: { code: 429, data: null, message: '请求过于频繁' } } }
    mocks.get.mockRejectedValueOnce(error)
    vi.spyOn(console, 'error').mockImplementation(() => {})
    const net = await import('../src/net')
    net.get('/api/post/list', {}, vi.fn())
    await vi.waitFor(() => expect(mocks.error).toHaveBeenCalledWith('请求过于频繁'))
  })

  it('过期 JWT 清理两种存储并回到未登录状态', async () => {
    const net = await import('../src/net')
    net.storeAccessToken(token(1), true)
    net.storeAccessToken(token(1), false)
    expect(net.unauthorized()).toBe(true)
    expect(localStorage.getItem('access_token')).toBeNull()
    expect(sessionStorage.getItem('access_token')).toBeNull()
    expect(mocks.warning).toHaveBeenCalled()
  })

  it('退出成功后清除 JWT', async () => {
    const net = await import('../src/net')
    net.storeAccessToken(token(), true)
    const onSuccess = vi.fn()
    net.logout(onSuccess)
    await vi.waitFor(() => expect(onSuccess).toHaveBeenCalledWith(response))
    expect(net.unauthorized()).toBe(true)
  })
})
