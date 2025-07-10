<template>
  <div class="chat-input-container">
    <textarea 
      ref="inputRef"
      class="chat-input" 
      v-model="message" 
      placeholder="请输入您的消息..." 
      @keydown.enter.prevent="onSubmit"
    ></textarea>
    <button class="send-button" @click="onSubmit" :disabled="!message.trim()">
      发送
    </button>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  loading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['send'])

const message = ref('')
const inputRef = ref(null)

const onSubmit = () => {
  if (!message.value.trim() || props.loading) return
  
  emit('send', message.value)
  message.value = ''
  
  // 自动聚焦输入框
  setTimeout(() => {
    inputRef.value?.focus()
  }, 0)
}
</script>

<style scoped>
.chat-input-container {
  display: flex;
  padding: 10px;
  background-color: #fff;
  border-top: 1px solid #e0e0e0;
  position: sticky;
  bottom: 0;
}

.chat-input {
  flex: 1;
  height: 44px;
  padding: 12px;
  border: 1px solid #e0e0e0;
  border-radius: 22px;
  outline: none;
  resize: none;
  font-family: inherit;
  font-size: 16px;
}

.send-button {
  margin-left: 10px;
  padding: 0 20px;
  height: 44px;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 22px;
  cursor: pointer;
  font-weight: bold;
}

.send-button:hover {
  background-color: #45a049;
}

.send-button:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .chat-input-container {
    padding: 8px;
  }
  
  .chat-input {
    font-size: 15px;
    padding: 10px;
  }
}

/* 小屏幕移动设备适配 */
@media (max-width: 480px) {
  .chat-input-container {
    padding: 6px;
  }
  
  .chat-input {
    height: 40px;
    font-size: 14px;
    padding: 8px 12px;
  }
  
  .send-button {
    padding: 0 15px;
    height: 40px;
    font-size: 14px;
  }
}
</style> 