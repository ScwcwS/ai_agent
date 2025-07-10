import { defineStore } from 'pinia'
import { v4 as uuidv4 } from 'uuid'

export const useChatStore = defineStore('chat', {
  state: () => ({
    loveAppChats: {},
    manusAppChats: []
  }),
  
  actions: {
    createLoveAppChat() {
      const chatId = uuidv4()
      this.loveAppChats[chatId] = {
        id: chatId,
        messages: []
      }
      return chatId
    },
    
    addLoveAppMessage(chatId, message, isUser = true) {
      if (!this.loveAppChats[chatId]) {
        this.loveAppChats[chatId] = {
          id: chatId,
          messages: []
        }
      }
      
      this.loveAppChats[chatId].messages.push({
        id: uuidv4(),
        content: message,
        isUser,
        timestamp: new Date()
      })
    },
    
    addManusAppMessage(message, isUser = true) {
      this.manusAppChats.push({
        id: uuidv4(),
        content: message,
        isUser,
        timestamp: new Date()
      })
    },
    
    clearManusChat() {
      this.manusAppChats = []
    }
  }
}) 