<script setup>
import HelloWorld from './components/HelloWorld.vue'
</script>

<template>
  <router-view />
</template>

<style>
html, body {
  font-family: 'Helvetica Neue', Arial, sans-serif;
  margin: 0;
  padding: 0;
  background-color: #f0f2f5;
  color: #333;
  width: 100%;
  height: 100%;
  overflow-x: hidden;
}

body {
  overflow-x: hidden;
}

#app {
  width:100%;
  min-height: 100vh;
}

* {
  box-sizing: border-box;
}
</style>
