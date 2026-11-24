import { useRef, useImperativeHandle, forwardRef, useState } from 'react'
import ConfirmAlert, { type ConfirmAlertType } from '@/components/common/ConfirmAlert'
import Text from '@/components/common/Text'
import { View } from 'react-native'
import Input, { type InputType } from '@/components/common/Input'
import { createStyle } from '@/utils/tools'
import { useApple, fontSize, radius } from '@/theme/apple'
import { useI18n } from '@/lang'
import { type Source } from '@/store/songlist/state'

interface IdInputType {
  setText: (text: string) => void
  getText: () => string
  focus: () => void
}
const IdInput = forwardRef<IdInputType, {}>((props, ref) => {
  const apple = useApple()
  const t = useI18n()
  const [text, setText] = useState('')
  const inputRef = useRef<InputType>(null)

  useImperativeHandle(ref, () => ({
    getText() {
      return text.trim()
    },
    setText(text) {
      setText(text)
    },
    focus() {
      inputRef.current?.focus()
    },
  }))

  return (
    <Input
      ref={inputRef}
      placeholder={t('songlist_open_input_placeholder')}
      value={text}
      onChangeText={setText}
      style={{ ...styles.input, backgroundColor: apple.fill }}
    />
  )
})


export interface ModalProps {
  onOpenId: (id: string) => void
}
export interface ModalType {
  show: (source: Source) => void
}

export default forwardRef<ModalType, ModalProps>(({ onOpenId }, ref) => {
  const alertRef = useRef<ConfirmAlertType>(null)
  const inputRef = useRef<IdInputType>(null)
  const [visible, setVisible] = useState(false)
  const apple = useApple()
  const t = useI18n()

  const handleShow = (source: Source) => {
    alertRef.current?.setVisible(true)
    requestAnimationFrame(() => {
      inputRef.current?.setText('')
      setTimeout(() => {
        inputRef.current?.focus()
      }, 300)
    })
  }
  useImperativeHandle(ref, () => ({
    show(source) {
      if (visible) handleShow(source)
      else {
        setVisible(true)
        requestAnimationFrame(() => {
          handleShow(source)
        })
      }
    },
  }))

  const handleConfirm = () => {
    let id = inputRef.current?.getText() ?? ''
    if (!id.length) return
    if (id.length > 500) id = id.substring(0, 500)
    alertRef.current?.setVisible(false)
    onOpenId(id)
  }

  return (
    visible
      ? <ConfirmAlert
          ref={alertRef}
          onConfirm={handleConfirm}
        >
          <View style={styles.content}>
            <View style={styles.col}>
              <IdInput ref={inputRef} />
            </View>
            <Text style={styles.inputTipText} size={fontSize.footnote} color={apple.secondaryLabel}>{t('songlist_open_input_tip')}</Text>
          </View>
        </ConfirmAlert>
      : null
  )
})


const styles = createStyle({
  content: {
    flexGrow: 1,
    flexShrink: 1,
    flexDirection: 'column',
  },
  col: {
    flexDirection: 'row',
    height: 38,
  },
  input: {
    flexGrow: 1,
    flexShrink: 1,
    minWidth: 290,
    borderRadius: radius.medium,
    height: '100%',
  },
  inputTipText: {
    marginTop: 15,
  },
})
