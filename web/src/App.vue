<template>
  <router-view />
</template>

<script setup>
// 根组件仅作为路由视图承载
</script>

<style>
html, body, #app {
  margin: 0;
  padding: 0;
  width: 100%;
  height: 100%;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  box-sizing: border-box;
}
*, *::before, *::after {
  box-sizing: inherit;
}
</style>
