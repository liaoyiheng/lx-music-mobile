import { useRef, useImperativeHandle, forwardRef, useCallback } from 'react'
import { Pressable, type PressableProps, StyleSheet, type View, type ViewProps, type PressableStateCallbackType, type StyleProp, type ViewStyle } from 'react-native'

export interface BtnProps extends PressableProps {
  /**
   * Android 水波纹。Apple 风格默认不使用，只有显式传入时才启用
   */
  ripple?: PressableProps['android_ripple']
  /**
   * 按下时的不透明度（iOS 风格），默认 0.5
   */
  activeOpacity?: number
  style?: ViewProps['style']
  onChangeText?: (value: string) => void
  onClearText?: () => void
  children: React.ReactNode
}

export interface BtnType {
  measure: (callback: (x: number, y: number, width: number, height: number, pageX: number, pageY: number) => void) => void
}

export default forwardRef<BtnType, BtnProps>(({ ripple, activeOpacity = 0.5, disabled, children, style, ...props }, ref) => {
  const btnRef = useRef<View>(null)

  useImperativeHandle(ref, () => ({
    measure(callback) {
      btnRef.current?.measure(callback)
    },
  }))

  const getStyle = useCallback(({ pressed }: PressableStateCallbackType): StyleProp<ViewStyle> => {
    const opacity = disabled ? 0.35 : (pressed && !ripple) ? activeOpacity : 1
    return StyleSheet.compose({ opacity }, style)
  }, [disabled, ripple, activeOpacity, style])

  return (
    <Pressable
      android_ripple={ripple}
      disabled={disabled}
      style={getStyle}
      {...props}
      ref={btnRef}
    >
      {children}
    </Pressable>
  )
})
