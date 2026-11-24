import { useCallback, useRef, forwardRef, useImperativeHandle, useState } from 'react'
import { TouchableOpacity } from 'react-native'
import Input, { type InputType, type InputProps } from '@/components/common/Input'
import { GlassSurface } from '@/components/apple/LiquidGlass'
import { IonIcon } from '@/components/common/Icon'
import { useI18n } from '@/lang'
import { createStyle } from '@/utils/tools'
import { useApple, radius, spacing } from '@/theme/apple'

export interface SearchInputProps {
  onChangeText: (text: string) => void
  onSubmit: (text: string) => void
  onClear?: () => void
  onFocus?: () => void
  onBlur: () => void
  onTouchStart: () => void
}

export interface SearchInputType {
  setText: (text: string) => void
  focus: () => void
  blur: () => void
}

/**
 * iOS 26 液态玻璃搜索框：圆角 14dp 玻璃容器 + search 图标 + 输入框 + close-circle 清空键
 */
export default forwardRef<SearchInputType, SearchInputProps>(({
  onChangeText,
  onSubmit,
  onClear,
  onFocus,
  onBlur,
  onTouchStart,
}, ref) => {
  const [text, setText] = useState('')
  const inputRef = useRef<InputType>(null)
  const apple = useApple()
  const t = useI18n()

  useImperativeHandle(ref, () => ({
    setText(nextText) {
      setText(nextText)
    },
    focus() {
      inputRef.current?.focus()
    },
    blur() {
      inputRef.current?.blur()
    },
  }))

  const handleChangeText = useCallback((nextText: string) => {
    setText(nextText)
    onChangeText(nextText.trim())
  }, [onChangeText])

  const handleClearText = useCallback(() => {
    setText('')
    onChangeText('')
    if (onClear) onClear()
    else onSubmit('')
  }, [onChangeText, onClear, onSubmit])

  const handleSubmit = useCallback<NonNullable<InputProps['onSubmitEditing']>>(({ nativeEvent: { text: value } }) => {
    onSubmit(value)
  }, [onSubmit])

  return (
    <GlassSurface borderRadius={radius.alert} style={styles.outer} innerStyle={styles.inner}>
      <IonIcon name="search" rawSize={17} color={apple.secondaryLabel} />
      <Input
        ref={inputRef}
        placeholder={t('apple_search_placeholder')}
        value={text}
        onChangeText={handleChangeText}
        style={styles.input}
        size={15.5}
        onFocus={onFocus}
        onBlur={onBlur}
        onSubmitEditing={handleSubmit}
        onTouchStart={onTouchStart}
        returnKeyType="search"
        clearBtn={false}
      />
      {text ? (
        <TouchableOpacity
          activeOpacity={0.6}
          onPress={handleClearText}
          hitSlop={{ top: 8, bottom: 8, left: 8, right: 8 }}
          style={styles.clearBtn}
        >
          <IonIcon name="close-circle" rawSize={18} color={apple.tertiaryLabel} />
        </TouchableOpacity>
      ) : null}
    </GlassSurface>
  )
})

const styles = createStyle({
  outer: {
    flex: 1,
  },
  inner: {
    height: 40,
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 11,
    gap: spacing.xs + 2,
  },
  input: {
    backgroundColor: 'transparent',
    paddingLeft: 2,
    paddingRight: 0,
    height: 38,
  },
  clearBtn: {
    alignItems: 'center',
    justifyContent: 'center',
  },
})
