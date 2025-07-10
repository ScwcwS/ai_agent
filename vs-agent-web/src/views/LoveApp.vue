<template>
  <div class="chat-container">
    <div class="chat-header">
      <router-link to="/" class="back-link">
        <span>←</span> 返回首页
      </router-link>
      <h1>AI 情感大师</h1>
    </div>
    
    <div class="chat-messages" ref="messagesContainer">
      <div v-if="messages.length === 0" class="empty-state">
        <div class="empty-icon">❤️</div>
        <p>欢迎使用AI情感大师，请发送消息开始聊天</p>
      </div>
      
      <template v-else>
        <ChatMessage
          v-for="message in messages"
          :key="message.id"
          :content="message.content"
          :isUser="message.isUser"
          :timestamp="message.timestamp"
        />
        
        <!-- 加载动画 -->
        <LoadingIndicator v-if="loading" />
      </template>
    </div>
    
    <ChatInput :loading="loading" @send="sendMessage" />
    
    <div class="chat-footer">
      <div class="chat-options">
        <label :class="['rag-toggle', { active: useRag }]" @click="useRag = !useRag">
          <span class="rag-icon">RAG</span>
          <span class="rag-text"></span>
        </label>
        <span class="option-hint">有问题，尽管问，shift+enter换行</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, nextTick, onBeforeMount } from 'vue'
import { useChatStore } from '../stores/chat'
import { connectToLoveAppChat, connectToLoveAppRagChat } from '../services/api'
import ChatMessage from '../components/ChatMessage.vue'
import ChatInput from '../components/ChatInput.vue'
import LoadingIndicator from '../components/LoadingIndicator.vue'
import { useHead } from '@vueuse/head'

// SEO 设置
useHead({
  title: 'AI情感大师 - 专业情感分析与关系咨询 | AI Agent Platform',
  meta: [
    { name: 'description', content: 'AI情感大师提供专业的情感分析、关系咨询和心理建议，帮助解决感情困惑，提升人际关系质量。通过先进AI技术深入解析情感问题，提供个性化建议。' },
    { name: 'keywords', content: 'AI聊天,情感分析,情感咨询,情感问题,关系建议,心理分析,人工智能情感顾问,情感大师,AI情感辅导' },
    { property: 'og:title', content: 'AI情感大师 - 专业情感分析与关系咨询 | AI Agent Platform' },
    { property: 'og:description', content: '通过AI技术解析情感密码，获得专业建议和关系指导。AI情感大师随时倾听你的心声，提供深度情感分析和解决方案。' },
    { property: 'og:type', content: 'website' },
    { property: 'og:url', content: window.location.href },
    { property: 'og:site_name', content: 'AI Agent Platform' },
    { property: 'og:locale', content: 'zh_CN' },
    { name: 'twitter:card', content: 'summary_large_image' },
    { name: 'twitter:title', content: 'AI情感大师 - 专业情感分析与关系咨询' },
    { name: 'twitter:description', content: 'AI情感大师提供专业的情感分析与建议，帮助你解决感情困惑，提升人际关系质量。' },
    { name: 'robots', content: 'index, follow' },
    { name: 'canonical', content: window.location.href }
  ],
  script: [
    {
      type: 'application/ld+json',
      children: JSON.stringify({
        "@context": "https://schema.org",
        "@type": "SoftwareApplication",
        "name": "AI情感大师",
        "applicationCategory": "ChatApplication",
        "offers": {
          "@type": "Offer",
          "price": "0",
          "priceCurrency": "CNY"
        },
        "description": "AI情感大师是一款专业的情感分析与关系咨询AI应用，帮助用户解决情感困惑，提升人际关系质量。",
        "aggregateRating": {
          "@type": "AggregateRating",
          "ratingValue": "4.8",
          "ratingCount": "256"
        }
      })
    }
  ]
})

const chatStore = useChatStore()
const messagesContainer = ref(null)
const loading = ref(false)
const chatId = ref('')
const eventSource = ref(null)
const useRag = ref(false)

const messages = ref([])

// 创建聊天室ID
onMounted(() => {
  chatId.value = chatStore.createLoveAppChat()
  
  // 添加系统欢迎消息
  const welcomeMessage = "您好，我是AI情感大师。请告诉我您想聊些什么？我可以帮您分析情感问题、提供关系建议或者倾听您的心声。"
  chatStore.addLoveAppMessage(chatId.value, welcomeMessage, false)
  
  // 在组件挂载后同步消息
  syncMessagesFromStore()
})

// 从store同步消息
const syncMessagesFromStore = () => {
  if (chatId.value && chatStore.loveAppChats[chatId.value]) {
    messages.value = chatStore.loveAppChats[chatId.value].messages
  }
}

// 监听消息变化，滚动到底部
watch(() => messages.value.length, async () => {
  await nextTick()
  scrollToBottom()
})

// 监听最后一条消息的内容变化，滚动到底部
watch(
  () => messages.value.length > 0 ? messages.value[messages.value.length - 1].content : '',
  async () => {
    await nextTick()
    scrollToBottom()
  }
)

// 滚动到底部
const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// 发送消息
const sendMessage = async (message) => {
  if (loading.value) return
  
  // 添加用户消息到store
  chatStore.addLoveAppMessage(chatId.value, message, true)
  syncMessagesFromStore()
  
  // 设置加载状态
  loading.value = true
  
  try {
    // 关闭之前的连接
    if (eventSource.value) {
      eventSource.value.close()
    }
    
    // 根据是否使用RAG选择不同的接口
    eventSource.value = useRag.value 
      ? connectToLoveAppRagChat(message, chatId.value)
      : connectToLoveAppChat(message, chatId.value)
    
    // 临时存储AI回复内容
    let aiResponse = ''
    let messageAdded = false
    
    // 监听SSE事件
    eventSource.value.onmessage = (event) => {
      if (event.data) {
        const data = event.data
        
        // 累加AI回复内容
        aiResponse += data
        
        // 收到第一个数据时，隐藏加载动画
        if (!messageAdded) {
          // 设置加载状态为false
          loading.value = false
          chatStore.addLoveAppMessage(chatId.value, aiResponse, false)
          messageAdded = true
        } else {
          // 更新最后一条AI消息
          const lastMessage = chatStore.loveAppChats[chatId.value].messages.slice(-1)[0]
          if (lastMessage && !lastMessage.isUser) {
            lastMessage.content = aiResponse
          }
        }
        
        syncMessagesFromStore()
        scrollToBottom() // 添加滚动到底部
      }
    }
    
    eventSource.value.onerror = () => {
      eventSource.value.close()
      loading.value = false
    }
    
    // 设置结束事件
    eventSource.value.addEventListener('complete', () => {
      eventSource.value.close()
      loading.value = false
    })
  } catch (error) {
    console.error('连接聊天服务失败:', error)
    loading.value = false
    
    // 添加错误消息
    chatStore.addLoveAppMessage(
      chatId.value, 
      '抱歉，连接服务器时出现问题，请稍后再试。', 
      false
    )
    syncMessagesFromStore()
  }
}
</script>

<style scoped>
.chat-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  width: 1200px; /* 固定宽度 */
  max-width: 100%; /* 确保不超过视口宽度 */
  margin: 0 auto;
  background-color: #f5f5f5;
  color: #333;
}

.chat-header {
  display: flex;
  align-items: center;
  padding: 10px 20px;
  background-color: #ff6b81;
  color: white;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.chat-header h1 {
  margin: 0 auto;
  font-size: 1.5rem;
}

.back-link {
  color: white;
  text-decoration: none;
  display: flex;
  align-items: center;
}

.back-link span {
  font-size: 1.2rem;
  margin-right: 5px;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  background-color: #f5f5f5;
}

.chat-footer {
  background-color: white;
  border-top: 1px solid #eaeaea;
}

.chat-options {
  display: flex;
  padding: 10px 16px;
  align-items: center;
  justify-content: space-between;
}

.rag-toggle {
  display: flex;
  align-items: center;
  background-color: #e7f1ff;
  color: #4a6fa5;
  border-radius: 18px;
  padding: 6px 12px;
  font-size: 14px;
  cursor: pointer;
  user-select: none;
  transition: all 0.2s ease;
  border: 1px solid transparent;
}

.rag-toggle.active {
  background-color: #3498db;
  color: white;
}

.rag-toggle:hover {
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.rag-icon {
  font-weight: bold;
}

.option-hint {
  color: #888;
  font-size: 12px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  opacity: 0.6;
  color: #666;
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .chat-container {
    width: 100%;
  }
  
  .chat-header h1 {
    font-size: 1.2rem;
  }
  
  .chat-messages {
    padding: 15px 10px;
  }
  
  .chat-options {
    padding: 8px 10px;
    flex-wrap: wrap;
  }
  
  .option-hint {
    margin-top: 5px;
    width: 100%;
    text-align: center;
    font-size: 11px;
  }
}

/* 小屏幕移动设备适配 */
@media (max-width: 480px) {
  .chat-header {
    padding: 8px 12px;
  }
  
  .chat-messages {
    padding: 10px 8px;
  }
  
  .empty-icon {
    font-size: 3rem;
  }
  
  .chat-options {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .rag-toggle {
    margin-bottom: 8px;
  }
}
</style> 