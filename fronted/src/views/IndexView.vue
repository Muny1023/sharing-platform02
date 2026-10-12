<script setup lang="ts">
import { onMounted, onUnmounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { get, logout } from '@/net'
import { offlinePreview } from '@/config/preview'
import { createPreviewPosts, createPreviewComments } from '@/preview/data'

interface PostItem {
  id: number
  authorId: number
  authorNickname: string
  title: string
  content: string
  resourceUrl: string
  likeCount: number
  commentCount: number
  favoriteCount: number
  createTime: string
  postId?: number
  postTitle?: string
}

interface AccountInfo {
  id: number
  username: string
  nickname: string
  avatar: string | null
  role: string
}

const router = useRouter()

const me = ref<AccountInfo | null>(null)
const posts = ref<PostItem[]>([])
const page = reactive({ current: 1, size: 10, total: 0 })
const sort = ref<'latest' | 'hot' | 'oldest'>('latest')
const loading = ref(false)
const keyword = ref('')
const searchMode = ref<'posts'|'comments'>('posts')
const searching = ref(false)
const unreadCount = ref(0)
let unreadTimer: number | undefined
function loadUnreadCount() { get('/api/notification/unread-count', {}, res => { unreadCount.value = Number(res.data?.data ?? 0) }) }

function loadPosts() {
  if (offlinePreview) { loadPreviewPosts(); return }
  loading.value = true
  get('/api/post/list',
    { page: page.current, size: page.size, sort: sort.value },
    (res) => {
      const data = res.data?.data
      posts.value = data?.items ?? []
      page.total = data?.total ?? 0
      loading.value = false
    },
    () => { loading.value = false },
    () => { loading.value = false },
  )
}

function search() {
  if (offlinePreview) { page.current = 1; loadPreviewPosts(keyword.value.trim()); return }
  if (!keyword.value.trim()) { loadPosts(); return }
  searching.value = true
  get(searchMode.value === 'posts' ? '/api/post/search' : '/api/post/search-comments', { keyword: keyword.value.trim(), page: page.current, size: page.size }, (res) => {
    const data = res.data?.data; posts.value = data?.items ?? []; page.total = data?.total ?? 0; searching.value = false
  }, () => { searching.value = false }, () => { searching.value = false })
}

function loadPreviewPosts(query = '') {
  let items = createPreviewPosts()
  if (query) {
    items = items.filter(item => (searchMode.value === 'comments'
      ? createPreviewComments(item.id).map(comment => comment.content).join(' ')
      : `${item.title} ${item.content}`).toLocaleLowerCase().includes(query.toLocaleLowerCase()))
  }
  items.sort((a, b) => sort.value === 'hot' ? b.likeCount - a.likeCount
    : sort.value === 'oldest' ? a.createTime.localeCompare(b.createTime) : b.createTime.localeCompare(a.createTime))
  page.total = items.length
  posts.value = items.slice((page.current - 1) * page.size, page.current * page.size)
}

function switchSort(value: 'latest' | 'hot' | 'oldest') {
  if (sort.value === value) return
  sort.value = value
  page.current = 1
  loadPosts()
}

function handlePageChange(newPage: number) {
  page.current = newPage
  loadPosts()
}

/** 列表卡片只展示正文摘要，避免长文撑爆卡片 */
function excerpt(content: string): string {
  return content.length > 120 ? content.slice(0, 120) + '…' : content
}

function handleLogout() {
  logout(() => {
    router.push('/')
  })
}

onMounted(() => {
  loadPosts()
  if (offlinePreview) return
  get('/api/user/me', {},
    (res) => { me.value = res.data?.data ?? null },
  )
  loadUnreadCount()
  unreadTimer = window.setInterval(loadUnreadCount, 15000)
})
onUnmounted(() => { if (unreadTimer) window.clearInterval(unreadTimer) })
</script>

<template>
  <div class="index-wrapper">
    <div class="ambient ambient-one"></div><div class="ambient ambient-two"></div>
    <header class="topbar">
      <div class="topbar-inner">
        <button class="brand" @click="router.push('/index')"><span class="brand-mark">✦</span><span>NEXUS</span><small>RESOURCE GRID</small></button>
        <div class="system-status"><i></i> SYSTEM ONLINE <span>·</span> {{ posts.length.toString().padStart(2, '0') }} SIGNALS</div>
        <span v-if="offlinePreview" class="system-status">本地预览 · 示例数据</span>
        <nav v-else class="topbar-right">
          <button class="nav-btn user-menu" @click="router.push('/my/posts')">{{ me?.nickname || me?.username || 'CONNECTING...' }}</button>
          <button class="nav-btn" @click="router.push('/notifications')">通知<span v-if="unreadCount > 0" class="badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</span></button>
          <button class="nav-btn" @click="router.push('/my/favorites')">收藏</button>
          <button class="nav-btn" @click="router.push('/ai')">AI LAB</button>
          <button class="new-post-btn" @click="router.push('/post/new')">+ 发布资源</button>
          <button class="logout-btn" @click="handleLogout">退出</button>
        </nav>
      </div>
    </header>

    <main class="feed">
      <div class="hero feed-inner">
        <div class="hero-copy">
          <div class="eyebrow"><span>01</span> COMMUNITY TRANSMISSION</div>
          <h1>资源<span>//</span><br><em>正在发生</em></h1>
          <p>发现、交换并保存来自社区的灵感信号。每一次分享，都会让知识网络更接近完整。</p>
          <div class="hero-line"><span></span><small>SCROLL TO EXPLORE</small></div>
        </div>
        <div class="hero-orbit" aria-hidden="true"><div class="orbit-ring"></div><div class="orbit-core">N<span>·</span>01</div><div class="orbit-dot dot-a"></div><div class="orbit-dot dot-b"></div></div>
        <div class="hero-stats"><div><strong>{{ page.total.toString().padStart(2, '0') }}</strong><span>POSTS INDEXED</span></div><div><strong>24<span>H</span></strong><span>LIVE ARCHIVE</span></div><div><strong>∞</strong><span>IDEAS CONNECTED</span></div></div>
      </div>

      <div class="feed-inner content-layout">
        <section class="stream-panel">
          <div class="section-heading"><div><span class="section-kicker">// SIGNAL STREAM</span><h2>最新资源</h2></div><div class="sort-bar"><button v-for="option in [{ value: 'latest', label: '最新' }, { value: 'hot', label: '最热' }, { value: 'oldest', label: '最早' }]" :key="option.value" class="sort-btn" :class="{ active: sort === option.value }" @click="switchSort(option.value as 'latest' | 'hot' | 'oldest')">{{ option.label }}</button></div></div>
          <div class="search-bar"><span class="search-icon">⌕</span><select v-model="searchMode"><option value="posts">帖子搜索</option><option value="comments">评论搜索</option></select><input v-model="keyword" placeholder="检索标题、内容或评论" @keyup.enter="search"><button @click="search">检索 <span>↗</span></button><button v-if="keyword" class="clear-btn" @click="keyword='';loadPosts()">清除</button></div>
          <p v-if="!loading && posts.length === 0" class="empty-tip">{{ offlinePreview ? '未找到匹配的示例资源。' : '暂无信号。点击右上角「发布资源」发送第一个资源。' }}</p>
          <article v-for="(post, index) in posts" :key="post.id" class="post-card" @click="router.push(`/post/${post.postId ?? post.id}`)">
            <div class="post-index">{{ String(index + 1).padStart(2, '0') }}</div><div class="post-body"><div class="post-card-top"><span class="signal-tag">SIGNAL / {{ post.authorNickname || 'ANONYMOUS' }}</span><span class="meta-time">{{ post.createTime }}</span></div><h2 class="post-title">{{ post.postTitle ?? post.title }}</h2><p class="post-excerpt">{{ excerpt(post.content) }}</p><div class="post-meta"><span>♥ {{ post.likeCount }}</span><span>◌ {{ post.commentCount }}</span><span>✦ {{ post.favoriteCount ?? 0 }}</span><span class="open-link">OPEN SIGNAL ↗</span></div></div><div class="post-arrow">↗</div>
          </article>
          <div v-if="page.total > page.size" class="pager"><button :disabled="page.current <= 1 || loading" @click="handlePageChange(page.current - 1)">←</button><span class="pager-info">{{ String(page.current).padStart(2, '0') }} / {{ String(Math.ceil(page.total / page.size)).padStart(2, '0') }}</span><button :disabled="page.current >= Math.ceil(page.total / page.size) || loading" @click="handlePageChange(page.current + 1)">→</button></div>
        </section>
        <aside class="side-panel"><div class="side-label">NEXUS / NOTE</div><h3>把你的<br><span>想法发射</span>出去</h3><p>一个开放的资源信号站。分享工具、灵感、文章和任何值得被看见的东西。</p><button class="side-cta" :disabled="offlinePreview" @click="router.push('/post/new')">{{ offlinePreview ? '示例资源 · 点击帖子查看详情' : '创建新信号' }} <span>↗</span></button><div class="side-footer"><span>COORDINATES</span><strong>31°14'52.4" N<br>121°28'06.7" E</strong></div></aside>
      </div>
    </main>
    <footer class="site-footer"><span>THE NETWORK IS ALIVE</span><span>SCROLL <b>›››</b></span></footer>
  </div>
</template>

<style scoped>
:global(body) { background:#070b10; }
.index-wrapper { position:relative; min-height:100vh; overflow:hidden; color:#eef6f4; background:radial-gradient(circle at 70% 10%,#15343a 0,#0b151b 34%,#070b10 72%); font-family:"Arial Narrow", "Microsoft YaHei", sans-serif; }
.index-wrapper::before { content:""; position:absolute; inset:0; pointer-events:none; opacity:.28; background-image:linear-gradient(rgba(121,242,229,.06) 1px,transparent 1px),linear-gradient(90deg,rgba(121,242,229,.04) 1px,transparent 1px); background-size:38px 38px; mask-image:linear-gradient(to bottom,#000,transparent 92%); }
.index-wrapper::after { content:""; position:absolute; inset:0; pointer-events:none; opacity:.13; background:repeating-linear-gradient(0deg,transparent 0 4px,rgba(255,255,255,.04) 5px); mix-blend-mode:screen; }
.ambient { position:absolute; border-radius:50%; filter:blur(4px); pointer-events:none; opacity:.24; animation:drift 12s ease-in-out infinite alternate; }.ambient-one { width:38vw;height:38vw;right:-12vw;top:9vh;background:#20d7c4;filter:blur(90px); }.ambient-two { width:26vw;height:26vw;left:-12vw;bottom:12vh;background:#5374ff;filter:blur(100px);animation-delay:-5s; }
.topbar { position:relative; z-index:2; border-bottom:1px solid rgba(155,220,215,.16); background:rgba(6,12,17,.72); backdrop-filter:blur(18px); }.topbar-inner { display:flex;align-items:center;gap:34px;max-width:1240px;height:78px;margin:auto;padding:0 28px; }.brand { display:flex;align-items:center;gap:10px;position:relative;padding:0;border:0;background:none;color:#f4fffd;cursor:pointer;font-weight:800;letter-spacing:.18em;font-size:16px; }.brand small { position:absolute;top:21px;left:30px;color:#6e9a9e;font-size:7px;letter-spacing:.28em;white-space:nowrap;font-weight:500; }.brand-mark { display:grid;place-items:center;width:28px;height:28px;border:1px solid #83f5e6;color:#83f5e6;font-size:14px;box-shadow:0 0 18px #36d3c266; }.system-status { margin-right:auto;color:#729398;font-size:10px;letter-spacing:.16em; }.system-status i { display:inline-block;width:6px;height:6px;margin-right:7px;border-radius:50%;background:#7af2d9;box-shadow:0 0 10px #7af2d9; }.system-status span { margin:0 8px;color:#3c6268; }.topbar-right { display:flex;align-items:center;gap:8px; }.nav-btn,.logout-btn { border:0;background:none;color:#a9c0c1;cursor:pointer;padding:9px 10px;font-size:11px;letter-spacing:.08em;transition:.25s; }.nav-btn:hover,.logout-btn:hover { color:#8cf8e8; }.badge { margin-left:5px;padding:2px 5px;border-radius:2px;background:#fa8068;color:#081014;font-size:9px; }.new-post-btn { padding:11px 15px;border:1px solid #6be4d3;background:#76e9da;color:#071217;font-size:11px;font-weight:800;letter-spacing:.08em;cursor:pointer;clip-path:polygon(8px 0,100% 0,100% calc(100% - 8px),calc(100% - 8px) 100%,0 100%,0 8px);transition:.25s; }.new-post-btn:hover { box-shadow:0 0 22px #62e8d455;transform:translateY(-2px); }.logout-btn { color:#759294; }
.feed { position:relative;z-index:1;padding:0 28px 40px; }.feed-inner { max-width:1240px;margin:0 auto; }.hero { display:grid;grid-template-columns:1fr 300px;min-height:335px;padding:70px 0 36px;position:relative; }.eyebrow,.section-kicker,.side-label { color:#79d5ce;font-size:10px;letter-spacing:.22em; }.eyebrow span { display:inline-block;margin-right:12px;color:#54777b; }.hero h1 { margin:22px 0 13px;font-size:clamp(48px,7vw,90px);line-height:.9;letter-spacing:-.06em;font-weight:700; }.hero h1 span { color:#76eadb;font-weight:300; }.hero h1 em { color:#ecf8f5;font-style:normal;text-shadow:0 0 26px rgba(126,241,221,.18); }.hero p { max-width:430px;margin:0;color:#8ca5a7;line-height:1.8;font-size:13px; }.hero-line { display:flex;align-items:center;gap:15px;margin-top:28px;color:#658488;font-size:9px;letter-spacing:.17em; }.hero-line span { width:72px;height:1px;background:#73e3d4;box-shadow:0 0 10px #73e3d4; }.hero-orbit { position:absolute;right:55px;top:45px;width:230px;height:230px;display:grid;place-items:center; }.orbit-ring { position:absolute;inset:16px;border:1px solid #70dbd166;border-radius:50%;box-shadow:inset 0 0 35px #5be7d515,0 0 25px #5be7d515;animation:spin 18s linear infinite; }.orbit-ring::before,.orbit-ring::after { content:"";position:absolute;inset:22px;border:1px dashed #70dbd144;border-radius:50%; }.orbit-ring::after { inset:44px;border-style:solid;border-color:#70dbd144 transparent;animation:spin 10s linear reverse infinite; }.orbit-core { position:relative;display:grid;place-items:center;width:83px;height:83px;border:1px solid #86f5e6;border-radius:50%;color:#dffff9;font-size:19px;letter-spacing:.08em;box-shadow:0 0 34px #47e8d555, inset 0 0 20px #47e8d522; }.orbit-core span { color:#70e2d5; }.orbit-dot { position:absolute;width:8px;height:8px;background:#82f2df;border-radius:50%;box-shadow:0 0 14px #82f2df; }.dot-a { top:26px;right:30px; }.dot-b { bottom:35px;left:20px;background:#fb9e74;box-shadow:0 0 14px #fb9e74; }.hero-stats { position:absolute;right:0;bottom:34px;display:flex;gap:30px; }.hero-stats div { display:flex;flex-direction:column;gap:4px; }.hero-stats strong { color:#d8eeeb;font-size:24px;font-weight:400; }.hero-stats strong span { color:#78dace;font-size:12px; }.hero-stats span { color:#638285;font-size:8px;letter-spacing:.15em; }
.content-layout { display:grid;grid-template-columns:minmax(0,1fr) 275px;gap:66px;align-items:start; }.stream-panel { min-width:0; }.section-heading { display:flex;align-items:end;justify-content:space-between;margin-bottom:18px;border-top:1px solid #315357;padding-top:18px; }.section-heading h2 { margin:8px 0 0;font-size:27px;font-weight:500;letter-spacing:.06em; }.sort-bar { display:flex;gap:4px; }.sort-btn { padding:7px 12px;border:1px solid transparent;background:none;color:#67878a;font-size:11px;cursor:pointer; }.sort-btn.active,.sort-btn:hover { color:#9df4e7;border-color:#3f7877;background:#143034; }.search-bar { display:flex;align-items:center;gap:9px;margin-bottom:14px;padding:3px 4px 3px 13px;border:1px solid #29474c;background:#0c1a20;box-shadow:0 8px 30px #02060844; }.search-icon { color:#76e8d9;font-size:21px;line-height:1; }.search-bar select,.search-bar input { border:0;outline:0;background:transparent;color:#99b8b8;font-size:12px; }.search-bar select { width:80px; }.search-bar input { flex:1;min-width:0;padding:11px 3px; }.search-bar option { color:#091014;background:#d9efeb; }.search-bar button { padding:10px 13px;border:1px solid #4c9c96;background:#173c3e;color:#a5f8ec;font-size:11px;cursor:pointer; }.search-bar button span { font-size:14px; }.search-bar .clear-btn { border:0;background:transparent;color:#68868a; }.post-card { display:flex;position:relative;margin-bottom:8px;padding:21px 22px 19px 16px;border:1px solid #1d363b;background:linear-gradient(105deg,rgba(17,35,40,.86),rgba(10,20,25,.7));cursor:pointer;transition:.3s;overflow:hidden; }.post-card::before { content:"";position:absolute;left:0;top:0;width:2px;height:100%;background:#6ee0d3;transform:scaleY(0);transition:.3s;transform-origin:bottom; }.post-card:hover { border-color:#477d7b;transform:translateX(4px);box-shadow:-15px 0 35px #5be1d30b; }.post-card:hover::before { transform:scaleY(1); }.post-index { width:45px;color:#5b777b;font-size:12px;letter-spacing:.08em; }.post-body { min-width:0;flex:1; }.post-card-top { display:flex;justify-content:space-between;gap:15px;margin-bottom:10px; }.signal-tag { color:#6bd3ca;font-size:9px;letter-spacing:.14em; }.meta-time { color:#5c7678;font-size:10px; }.post-title { margin:0 0 9px;color:#e9f6f3;font-size:20px;font-weight:500;letter-spacing:.01em; }.post-excerpt { max-width:680px;margin:0 0 18px;color:#87a3a4;font-size:12px;line-height:1.7;white-space:nowrap;overflow:hidden;text-overflow:ellipsis; }.post-meta { display:flex;align-items:center;gap:17px;color:#668588;font-size:10px; }.open-link { margin-left:auto;color:#7ee9db;letter-spacing:.13em; }.post-arrow { align-self:center;color:#5a8484;font-size:21px;transition:.3s; }.post-card:hover .post-arrow { color:#88f2e3;transform:translate(3px,-3px); }.empty-tip { padding:55px 20px;text-align:center;border:1px dashed #2a4b4e;color:#719092;font-size:13px; }.pager { display:flex;align-items:center;justify-content:center;gap:18px;margin:24px 0;color:#6e9192;font-size:11px; }.pager button { width:32px;height:32px;border:1px solid #3e6e70;background:#0e2227;color:#91ece1;cursor:pointer; }.pager button:disabled { opacity:.35;cursor:not-allowed; }
.side-panel { position:relative;margin-top:65px;padding:28px 24px 24px;border:1px solid #294a4c;background:linear-gradient(145deg,#122c31aa,#0a161cbb); }.side-panel::before { content:"";position:absolute;right:-1px;top:-1px;width:42px;height:42px;border-top:1px solid #8ae7dc;border-right:1px solid #8ae7dc; }.side-panel h3 { margin:22px 0 15px;font-size:29px;line-height:1.1;font-weight:400;letter-spacing:.01em; }.side-panel h3 span { color:#79ebdc; }.side-panel p { margin:0 0 25px;color:#819c9e;font-size:12px;line-height:1.8; }.side-cta { display:flex;justify-content:space-between;align-items:center;width:100%;padding:13px 14px;border:1px solid #5eaaa4;background:#16383b;color:#a3f9ed;font-size:11px;cursor:pointer; }.side-cta span { font-size:17px; }.side-footer { display:flex;flex-direction:column;gap:10px;margin-top:48px;padding-top:15px;border-top:1px solid #29474a;color:#567477;font-size:9px;letter-spacing:.15em; }.side-footer strong { color:#8babad;font-size:11px;line-height:1.6;letter-spacing:.08em;font-weight:400; }.site-footer { position:relative;z-index:1;display:flex;justify-content:space-between;max-width:1240px;margin:45px auto 0;padding:0 28px 24px;color:#557376;font-size:9px;letter-spacing:.28em; }.site-footer b { color:#88eee0;font-size:15px;letter-spacing:-.12em; }.user-menu { color:#91cfca; }.logout-btn { color:#708b8c; }
@keyframes spin { to { transform:rotate(360deg); } } @keyframes drift { to { transform:translate3d(25px,-18px,0) scale(1.06); } }
@media (max-width:900px) { .topbar-inner { gap:18px; }.system-status { display:none; }.topbar-right .nav-btn:nth-child(2),.topbar-right .nav-btn:nth-child(3) { display:none; }.hero { min-height:300px; }.hero-orbit { right:0;transform:scale(.76);transform-origin:top right; }.content-layout { grid-template-columns:1fr; }.side-panel { display:none; } }
@media (max-width:600px) { .topbar-inner,.feed,.site-footer { padding-left:16px;padding-right:16px; }.topbar-inner { height:68px; }.brand { font-size:14px; }.topbar-right { gap:0; }.topbar-right .nav-btn:first-child,.logout-btn { display:none; }.new-post-btn { padding:9px 10px;font-size:10px; }.hero { display:block;padding:50px 0 38px;min-height:385px; }.hero-orbit { right:-22px;top:75px;opacity:.5;transform:scale(.66); }.hero-copy { position:relative;z-index:1; }.hero h1 { font-size:62px; }.hero-stats { left:0;right:auto;bottom:20px;gap:22px; }.hero-stats strong { font-size:19px; }.section-heading { display:block; }.sort-bar { margin-top:16px; }.search-bar select { width:65px; }.search-bar button { padding:10px 8px; }.post-card { padding:17px 12px 16px 10px; }.post-index { width:31px; }.post-title { font-size:17px; }.post-excerpt { white-space:normal;display:-webkit-box;-webkit-line-clamp:2;-webkit-box-orient:vertical; }.post-meta { gap:10px; }.open-link { display:none; }.site-footer { margin-top:25px;font-size:8px; } }
</style>
