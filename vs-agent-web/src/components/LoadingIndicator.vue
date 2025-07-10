<template>
  <div class="loading-indicator">
    <span class="dot"></span>
    <span class="dot"></span>
    <span class="dot"></span>
  </div>
</template>

<style scoped>
.loading-indicator {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  padding: 10px 14px;
  max-width: 60px;
  background-color: white;
  border-radius: 18px;
  border-top-left-radius: 0;
  margin-right: auto;
  margin-bottom: 16px;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #999;
  margin: 0 3px;
  opacity: 0.6;
  animation: pulse 1.5s infinite;
}

.dot:nth-child(2) {
  animation-delay: 0.5s;
}

.dot:nth-child(3) {
  animation-delay: 1s;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(0.8);
    opacity: 0.6;
  }
  50% {
    transform: scale(1.2);
    opacity: 1;
  }
}
</style> 