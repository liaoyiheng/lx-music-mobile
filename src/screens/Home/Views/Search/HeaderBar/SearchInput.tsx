import { useCallback, useRef, forwardRef, useImperativeHandle, useState } from 'react'
import { View } from 'react-native'
import Input, { type InputType, type InputProps } from '@/components/common/Input'
import { IonIcon } from '@/components/common/Icon'
import { useI18n } from '@/lang'
import { createStyle } from '@/utils/tools'
import { useApple, fontSize, radius, spacing } from '@/theme/apple'

export interface SearchInputProps {
  onChangeText: (text: string) => void
  onSubmit: (text: string) => void
  onBlur: () => void
  onTouchStart: () => void
}

export interface SearchInputType {
  setText: (text: string) => void
  focus: () => void
  blur: () => void
}

/**
 * Apple 风格搜索框：左侧放大镜，整体圆角填充底，有内容时右侧显示清除按钮
 */
export default forwardRef<SearchInputType, SearchInputProps>(({ onChangeText, onSubmit, onBlur, onTouchStart }, ref) => {
  const [text, setText] = useState('')
  const inputRef = useRef<InputType>(null)
  const apple = useApple()
  const t = useI18n()

  useImperativeHandle(ref, () => ({
    setText(text) {
      setText(text)
    },
    focus() {
      inputRef.current?.focus()
    },
    blur() {
      inputRef.current?.blur()
    },
  }))

  const handleChangeText = (text: string) => {
    setText(text)
    onChangeText(text.trim())
  }

  const handleClearText = useCallback(() => {
    setText('')
    onChangeText('')
    onSubmit('')
  }, [onChangeText, onSubmit])

  const handleSubmit = useCallback<NonNullable<InputProps['onSubmitEditing']>>(({ nativeEvent: { text } }) => {
    onSubmit(text)
  }, [onSubmit])

  return (
    <View style={{ ...styles.container, backgroundColor: apple.fill }}>
      <IonIcon name="search" rawSize={16} color={apple.secondaryLabel} />
      <Input
        ref={inputRef}
        placeholder={t('apple_search_placeholder')}
        value={text}
        onChangeText={handleChangeText}
        style={styles.input}
        size={fontSize.callout}
        onBlur={onBlur}
        onSubmitEditing={handleSubmit}
        onClearText={handleClearText}
        onTouchStart={onTouchStart}
        returnKeyType="search"
        clearBtn={!!text}
      />
    </View>
  )
})

const styles = createStyle({
  container: {
    flex: 1,
    height: 36,
    flexDirection: 'row',
    alignItems: 'center',
    borderRadius: radius.medium,
    paddingLeft: spacing.sm,
    overflow: 'hidden',
  },
  input: {
    backgroundColor: 'transparent',
    paddingLeft: spacing.sm - 2,
  },
})
