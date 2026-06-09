import { reactive } from 'vue'

const state = reactive({
  visible: false,
  title: '',
  text: '',
  emoji: '🌟'
})

export function useModal() {
  function showModal(title, text, emoji = '🌟') {
    state.title = title
    state.text = text
    state.emoji = emoji
    state.visible = true
  }

  function hideModal() {
    state.visible = false
  }

  return { state, showModal, hideModal }
}
