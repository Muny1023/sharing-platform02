<script setup lang="ts">
</script>

<template>
  <div class="not-found">
    <h1>404</h1>
    <p>页面不存在</p>
    <router-link to="/index">返回帖子流 ↗</router-link>
  </div>
</template>

<style scoped>
.not-found {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  color: #739092;
}
.not-found h1 {
  font-size: 72px;
  margin: 0;
}
.not-found p {
  font-size: 16px;
  margin-top: 12px;
}
</style>

<style scoped src="../styles/page-shell.css"></style>
