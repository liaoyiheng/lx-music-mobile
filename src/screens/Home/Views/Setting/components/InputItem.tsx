import { memo, useState, useEffect, useRef } from 'react'
import { StyleSheet, View, Keyboard } from 'react-native'
import type { InputType, InputProps } from '@/components/common/Input'
import Input from '@/components/common/Input'
import { useApple, fontSize, hairline, radius, spacing } from '@/theme/apple'
import Text from '@/components/common/Text'

export interface InputItemProps extends InputProps {
  value: string
  label: string
  onChanged: (text: string, callback: (vlaue: string) => void) => void
  showSeparator?: boolean
}

export default memo(({ value, label, onChanged, showSeparator = true, ...props }: InputItemProps) => {
  const [text, setText] = useState(value)
  const textRef = useRef(value)
  const isMountRef = useRef(false)
  const inputRef = useRef<InputType>(null)
  const apple = useApple()

  const saveValue = () => {
    onChanged?.(text, (value: string) => {
      if (!isMountRef.current) return
      const newValue = String(value)
      setText(newValue)
      textRef.current = newValue
    })
  }
  useEffect(() => {
    isMountRef.current = true
    return () => {
      isMountRef.current = false
    }
  }, [])
  useEffect(() => {
    const handleKeyboardDidHide = () => {
      if (!inputRef.current?.isFocused()) return
      onChanged?.(textRef.current, value => {
        if (!isMountRef.current) return
        const newValue = String(value)
        setText(newValue)
        textRef.current = newValue
      })
    }
    const keyboardDidHide = Keyboard.addListener('keyboardDidHide', handleKeyboardDidHide)

    return () => {
      keyboardDidHide.remove()
    }
  }, [onChanged])
  useEffect(() => {
    if (value != text) {
      const newValue = String(value)
      setText(newValue)
      textRef.current = newValue
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [value])
  const handleSetSelectMode = (text: string) => {
    setText(text)
    textRef.current = text
  }

  return (
    <View>
      <View style={styles.container}>
        <Text style={styles.label} size={fontSize.subhead} color={apple.secondaryLabel}>{label}</Text>
        <Input
          value={text}
          ref={inputRef}
          onChangeText={handleSetSelectMode}
          placeholderTextColor={apple.tertiaryLabel}
          selectionColor={apple.accent}
          style={{
            ...styles.input,
            backgroundColor: apple.fill,
            color: apple.label,
          }}
          {...props}
          onBlur={saveValue}
        />
      </View>
      {showSeparator ? <View style={{ ...styles.sep, backgroundColor: apple.separator }} /> : null}
    </View>
  )
})

const styles = StyleSheet.create({
  container: {
    paddingHorizontal: spacing.lg,
    paddingVertical: spacing.md,
  },
  label: {
    marginBottom: spacing.sm,
  },
  input: {
    flexGrow: 1,
    flexShrink: 1,
    borderRadius: radius.medium,
    paddingHorizontal: spacing.md,
    paddingVertical: 10,
    fontSize: fontSize.callout,
    maxWidth: 360,
  },
  sep: {
    height: hairline,
    marginLeft: spacing.lg,
  },
})
