<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login, storeAccessToken } from '@/net'

const router = useRouter()
const username = ref('')
const password = ref('')
const rememberMe = ref(false)
const errorMessage = ref('')
const loading = ref(false)

function handleLogin() {
  if (!username.value.trim() || !password.value.trim()) {
    errorMessage.value = '用户名或密码不能为空'
    return
  }
  errorMessage.value = ''
  loading.value = true

  login(
    username.value,
    password.value,
    // 成功回调
    (response) => {
      loading.value = false
      const data = response.data?.data
      const token = data?.token
      const name = data?.username || username.value
      if (token) {
        storeAccessToken(token, rememberMe.value)
        ElMessage.success(`登录成功，欢迎 ${name} 进入系统`)
        router.push('/index')
      }
    },
    // 失败回调
    () => {
      loading.value = false
      ElMessage.error('用户名或密码错误')
    },
    // 错误回调（网络异常由 defaultError 自动弹出）
    () => {
      loading.value = false
    },
  )
}
</script>

<template>
  <div class="login-wrapper">
    <div class="login-grid"></div><div class="login-glow"></div>
    <div class="login-orbit"><span></span><i></i><b>01<br><small>资源站</small></b></div>
    <div class="login-side"><span>01 / ENTRY POINT</span><strong>连接<br>资源<br><em>社区</em></strong><small>RESOURCE SHARING COMMUNITY<br>EST. 2026</small></div>
    <div class="login-card">
      <div class="card-kicker">登录 / 账号验证</div>
      <h1 class="login-title">欢迎回来<span>_</span></h1>
      <p class="login-desc">输入凭证，接入资源信号网络。</p>

      <form class="login-form" @submit.prevent>
        <div class="form-group">
          <input
            v-model="username"
            type="text"
            class="form-input"
            placeholder="用户名 / 邮箱"
            autocomplete="username"
          />
        </div>

        <div class="form-group">
          <input
            v-model="password"
            type="password"
            class="form-input"
            placeholder="访问密码"
            autocomplete="current-password"
          />
        </div>

        <div class="form-extra">
          <label class="remember-me">
            <input v-model="rememberMe" type="checkbox" />
            <span>保持连接</span>
          </label>
          <a href="#" class="forgot-link" @click.prevent="router.push('/reset')">找回凭证 ↗</a>
        </div>

        <p v-if="errorMessage" class="error-msg">{{ errorMessage }}</p>

        <button type="submit" class="login-btn" :disabled="loading" @click="handleLogin">
          {{ loading ? 'VERIFYING...' : '接入网络  ↗' }}
        </button>
      </form>

      <p class="register-link">
        还没账号？
        <a href="#" @click.prevent="router.push('/register')">创建账号 ↗</a>
      </p>
    </div>
  </div>
</template>

<style scoped>
.login-wrapper { position:relative;display:flex;justify-content:center;align-items:center;min-height:100vh;overflow:hidden;color:#edf9f5;background:#070b10; }
.login-wrapper::before { content:"";position:absolute;inset:0;opacity:.2;background-image:linear-gradient(rgba(121,242,229,.08) 1px,transparent 1px),linear-gradient(90deg,rgba(121,242,229,.06) 1px,transparent 1px);background-size:42px 42px;mask-image:linear-gradient(110deg,#000,transparent 75%); }.login-grid { position:absolute;inset:0;opacity:.1;background:repeating-linear-gradient(0deg,transparent 0 5px,#fff 6px); }.login-glow { position:absolute;width:48vw;height:48vw;right:-18vw;top:-17vw;border-radius:50%;background:#1dc8b7;filter:blur(110px);opacity:.19; }.login-side { position:absolute;left:clamp(28px,9vw,150px);top:15vh;display:flex;flex-direction:column;gap:25px;color:#648083; }.login-side span,.card-kicker { color:#75d9d0;font-size:10px;letter-spacing:.2em; }.login-side strong { color:#e8f5f2;font-size:clamp(36px,5vw,68px);line-height:.89;font-weight:400;letter-spacing:-.07em; }.login-side em { color:#76e9db;font-style:normal; }.login-side small { font-size:8px;line-height:1.7;letter-spacing:.18em; }.login-orbit { position:absolute;right:10vw;bottom:11vh;width:180px;height:180px;border:1px solid #477a7a;border-radius:50%;opacity:.7; }.login-orbit::before,.login-orbit::after { content:"";position:absolute;inset:18px;border:1px dashed #4d9f99;border-radius:50%; }.login-orbit::after { inset:40px;border-style:solid;border-color:#70e7d9 transparent;animation:spin 12s linear infinite; }.login-orbit span { position:absolute;top:18px;right:29px;width:7px;height:7px;background:#7de8d9;border-radius:50%;box-shadow:0 0 12px #7de8d9; }.login-orbit i { position:absolute;bottom:21px;left:30px;width:5px;height:5px;background:#fb9977;border-radius:50%; }.login-orbit b { position:absolute;inset:0;display:grid;place-items:center;text-align:center;color:#c7f5ee;font-size:22px;font-weight:400; }.login-orbit small { color:#6b9a9a;font-size:7px;letter-spacing:.14em; }.login-card { position:relative;z-index:1;width:360px;margin-left:35vw;padding:39px 38px 32px;border:1px solid #315358;background:rgba(10,22,28,.84);box-shadow:0 25px 80px #0008, inset 0 0 40px #72dfd209; }.login-card::before { content:"";position:absolute;right:-1px;top:-1px;width:42px;height:42px;border-top:1px solid #8feee0;border-right:1px solid #8feee0; }.login-title { margin:22px 0 10px;color:#eef9f6;font-size:34px;font-weight:400;letter-spacing:-.05em; }.login-title span { color:#7ce9dc;animation:blink 1s steps(2) infinite; }.login-desc { margin:0 0 29px;color:#799395;font-size:12px; }.login-form { display:flex;flex-direction:column;gap:17px; }.form-group { position:relative; }.form-input { width:100%;box-sizing:border-box;padding:13px 14px;color:#d9efeb;background:#0d1b21;border:1px solid #29474c;outline:none;font-size:13px;transition:.25s; }.form-input::placeholder { color:#557174; }.form-input:focus { border-color:#78e4d6;box-shadow:0 0 0 3px #5edbd622;background:#10242a; }.form-extra { display:flex;justify-content:space-between;align-items:center;color:#688487;font-size:11px; }.remember-me { display:flex;align-items:center;gap:7px;cursor:pointer; }.remember-me input { accent-color:#76e6d8; }.forgot-link,.register-link a { color:#7de7da;text-decoration:none; }.forgot-link:hover,.register-link a:hover { text-decoration:underline; }.login-btn { width:100%;padding:14px;border:1px solid #72e7d8;background:#65d8ca;color:#071217;font-size:12px;font-weight:800;letter-spacing:.11em;cursor:pointer;transition:.25s; }.login-btn:hover { background:#94f6e8;box-shadow:0 0 25px #66dfd655; }.login-btn:disabled { opacity:.55;cursor:wait; }.error-msg { margin:0;padding:9px 12px;color:#ff9d87;background:#301d20;border:1px solid #914c49;font-size:11px; }.register-link { margin:25px 0 0;color:#668184;text-align:left;font-size:11px; }.register-link a { margin-left:5px; }.login-wrapper :global(.el-message) { background:#11272a;border-color:#3b7775;color:#bdf7ee; }
@keyframes spin { to { transform:rotate(360deg); } } @keyframes blink { 50% { opacity:0; } }
@media (max-width:800px) { .login-side { left:24px;top:9vh; }.login-side strong { font-size:34px; }.login-orbit { right:-50px;bottom:6vh;opacity:.35; }.login-card { margin:110px 24px 0 auto;width:min(360px,calc(100vw - 48px));padding:32px 26px 27px; } }
@media (max-width:520px) { .login-side { top:30px;left:20px; }.login-side strong { display:none; }.login-side small { display:none; }.login-card { margin:75px auto 0; } }
</style>
