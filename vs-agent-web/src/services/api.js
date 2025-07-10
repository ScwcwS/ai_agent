import axios from 'axios'


const baseURL = 'your url'
const api = axios.create({
  baseURL,
  headers: {
    'Content-Type': 'application/json'
  }
})

export const connectToLoveAppChat = (message, chatId) => {
  const url = `${baseURL}/api/ai/love_app/chat/sse?message=${encodeURIComponent(message)}&chatId=${encodeURIComponent(chatId)}`
  return new EventSource(url)
}

export const connectToLoveAppRagChat = (message, chatId) => {
  const url = `${baseURL}/api/ai/love_app/chat_rag/sse?message=${encodeURIComponent(message)}&chatId=${encodeURIComponent(chatId)}`
  return new EventSource(url)
}

export const connectToManusChat = (message,contentText) => {
  const url = `${baseURL}/api/ai/manus/chat?message=${encodeURIComponent(message)}&contentText=${encodeURIComponent(contentText)}`
  return new EventSource(url)
}

export default api
