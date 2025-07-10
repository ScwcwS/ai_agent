<template>
  <div class="chat-container">
    <div class="chat-header">
      <router-link to="/" class="back-link">
        <span>←</span> 返回首页
      </router-link>
      <h1>AI 超级智能体</h1>
    </div>

    <div class="chat-messages" ref="messagesContainer">
      <div v-if="messages.length === 0" class="empty-state">
        <div class="empty-icon">🧠</div>
        <p>欢迎使用AI超级智能体，请发送消息开始聊天</p>
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
  </div>
</template>

<script setup>
import { ref, onMounted, watch, nextTick } from 'vue'
import { useChatStore } from '../stores/chat'
import { connectToManusChat } from '../services/api'
import ChatMessage from '../components/ChatMessage.vue'
import ChatInput from '../components/ChatInput.vue'
import LoadingIndicator from '../components/LoadingIndicator.vue'
import { useHead } from '@vueuse/head'

// SEO 设置
useHead({
  title: 'AI超级智能体 - 高级知识问答与解决方案 | AI Agent Platform',
  meta: [
    { name: 'description', content: 'AI超级智能体是一个拥有强大思考能力的AI系统，能够解答各领域复杂问题，提供深度分析和解决方案，帮助用户获取专业知识与见解。' },
    { name: 'keywords', content: 'AI智能体,人工智能问答,知识问答,AI助手,智能问答系统,深度思考,AI解决方案,复杂问题分析,智能体技术,AI顾问' },
    { property: 'og:title', content: 'AI超级智能体 - 高级知识问答与解决方案 | AI Agent Platform' },
    { property: 'og:description', content: '探索AI超级智能体的强大思考能力，获取专业领域知识解答与深度分析。让AI智能体帮你突破知识边界，解决复杂问题。' },
    { property: 'og:type', content: 'website' },
    { property: 'og:url', content: window.location.href },
    { property: 'og:site_name', content: 'AI Agent Platform' },
    { property: 'og:locale', content: 'zh_CN' },
    { name: 'twitter:card', content: 'summary_large_image' },
    { name: 'twitter:title', content: 'AI超级智能体 - 高级知识问答与解决方案' },
    { name: 'twitter:description', content: '体验前沿AI技术，让AI超级智能体为你解答各领域问题，提供专业见解与解决方案。' },
    { name: 'robots', content: 'index, follow' },
    { name: 'canonical', content: window.location.href }
  ],
  script: [
    {
      type: 'application/ld+json',
      children: JSON.stringify({
        "@context": "https://schema.org",
        "@type": "SoftwareApplication",
        "name": "AI超级智能体",
        "applicationCategory": "AIAssistant",
        "offers": {
          "@type": "Offer",
          "price": "0",
          "priceCurrency": "CNY"
        },
        "description": "AI超级智能体是一款拥有强大思考能力的AI问答系统，能够解答各领域复杂问题，提供深度分析和解决方案。",
        "aggregateRating": {
          "@type": "AggregateRating",
          "ratingValue": "4.9",
          "ratingCount": "328"
        }
      })
    }
  ]
})

const chatStore = useChatStore()
const messagesContainer = ref(null)
const loading = ref(false)
const eventSource = ref(null)

// 从store获取消息
const messages = ref([])

onMounted(() => {
  // 清空之前的聊天记录
  chatStore.clearManusChat()

  // 添加系统欢迎消息
  chatStore.addManusAppMessage(
    "您好，我是AI超级智能体。我拥有强大的思考和问题解决能力，可以回答您各领域的问题，请告诉我您想了解什么？",
    false
  )

  // 同步消息
  syncMessagesFromStore()
})

// 同步store中的消息
const syncMessagesFromStore = () => {
  messages.value = chatStore.manusAppChats
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


let contentText = []
// 发送消息
const sendMessage = async (message) => {
  if (loading.value) return
  contentText.push({"user":message})
  // 添加用户消息
  chatStore.addManusAppMessage(message, true)
  syncMessagesFromStore()

  // 设置加载状态
  loading.value = true

  try {
    // 关闭之前的连接
    if (eventSource.value) {
      eventSource.value.close()
    }

    // 创建新的SSE连接
    eventSource.value = connectToManusChat(message,contentText === [] ? "" : JSON.stringify(contentText))

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
          chatStore.addManusAppMessage(aiResponse, false)
          messageAdded = true
        } else {
          // 更新最后一条AI消息
          const lastMessage = chatStore.manusAppChats.slice(-1)[0]
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
      contentText.push({"assistant":aiResponse})
      // console.log(contentText)
      loading.value = false
    }
    // 设置结束事件
    eventSource.value.addEventListener('complete', () => {
      eventSource.value.close()

      contentText.push({"assistant":aiResponse})
      // console.log("complete")
      loading.value = false
    })
  } catch (error) {
    console.error('连接聊天服务失败:', error)
    loading.value = false

    // 添加错误消息
    chatStore.addManusAppMessage(
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
  background-color: #4a6fa5;
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
}

/* 小屏幕移动设备适配 */
@media (max-width: 480px) {
  .chat-header {
    padding: 8px 12px;
  }
  
  .chat-header h1 {
    font-size: 1.1rem;
  }
  
  .chat-messages {
    padding: 10px 8px;
  }
  
  .empty-icon {
    font-size: 3rem;
  }
}
</style>
