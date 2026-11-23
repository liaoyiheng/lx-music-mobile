import { useCallback, useEffect, useMemo, useState } from 'react'
import { View, TouchableOpacity, Pressable } from 'react-native'

import { createStyle, tipDialog } from '@/utils/tools'
import { scaleSizeH, scaleSizeW } from '@/utils/pixelRatio'
import { useApple } from '@/theme/apple'
import Text from '../Text'
import { IonIcon } from '../Icon'

export interface CheckBoxProps {
  check: boolean
  label?: string
  children?: React.ReactNode
  onChange: (check: boolean) => void
  disabled?: boolean
  need?: boolean
  size?: number
  marginRight?: number
  marginBottom?: number

  helpTitle?: string
  helpDesc?: string
}

const CircleBox = ({
  checked,
  disabled,
  size = 1,
  onPress,
}: {
  checked: boolean
  disabled?: boolean
  size?: number
  onPress?: () => void
}) => {
  const apple = useApple()
  const diameter = scaleSizeW(22 * size)
  const iconSize = Math.max(12, Math.round(diameter * 0.64))

  return (
    <Pressable
      onPress={onPress}
      disabled={disabled}
      accessibilityRole="checkbox"
      accessibilityState={{ disabled, checked }}
      style={({ pressed }) => ({
        ...styles.boxWrap,
        opacity: pressed ? 0.6 : 1,
      })}
    >
      <View
        style={{
          width: diameter,
          height: diameter,
          borderRadius: diameter / 2,
          alignItems: 'center',
          justifyContent: 'center',
          backgroundColor: checked
            ? (disabled ? apple.accentFill : apple.accent)
            : 'transparent',
          borderWidth: checked ? 0 : 1.5,
          borderColor: disabled ? apple.quaternaryLabel : apple.separator,
        }}
      >
        {checked ? <IonIcon name="checkmark" rawSize={iconSize} color="#FFFFFF" /> : null}
      </View>
    </Pressable>
  )
}

export default ({ check, label, children, onChange, helpTitle, helpDesc, disabled = false, need = false, marginRight = 0, marginBottom = 0, size = 1 }: CheckBoxProps) => {
  const apple = useApple()
  const [isDisabled, setDisabled] = useState(false)

  useEffect(() => {
    if (need) {
      if (check) {
        if (!isDisabled) setDisabled(true)
      } else {
        if (isDisabled) setDisabled(false)
      }
    } else {
      isDisabled && setDisabled(false)
    }
  }, [check, need, isDisabled])

  const handleLabelPress = useCallback(() => {
    if (isDisabled) return
    onChange?.(!check)
  }, [isDisabled, onChange, check])

  const helpComponent = useMemo(() => {
    const handleShowHelp = () => {
      void tipDialog({
        title: helpTitle ?? '',
        message: helpDesc,
        btnText: global.i18n.t('understand'),
      })
    }
    return (helpTitle ?? helpDesc) ? (
      <TouchableOpacity style={styles.helpBtn} onPress={handleShowHelp}>
        <IonIcon rawSize={Math.round(scaleSizeW(16 * size))} name="help-circle-outline" color={apple.secondaryLabel} />
      </TouchableOpacity>
    ) : null
  }, [helpTitle, helpDesc, size, apple.secondaryLabel])

  const contentStyle = { ...styles.content, marginBottom: scaleSizeH(marginBottom) }
  const labelStyle = { ...styles.label, marginRight: scaleSizeW(marginRight) }

  return (
    disabled
      ? (
          <View style={contentStyle}>
            <CircleBox checked={check} disabled={true} size={size} />
            <View style={labelStyle}>
              {label ? <Text style={styles.name} color={apple.secondaryLabel} size={15 * size}>{label}</Text> : children}
            </View>
            {helpComponent}
          </View>
        )
      : (
          <View style={contentStyle}>
            <CircleBox checked={check} disabled={isDisabled} onPress={handleLabelPress} size={size} />
            <TouchableOpacity style={labelStyle} activeOpacity={0.5} onPress={handleLabelPress}>
              {label ? <Text style={styles.name} color={apple.label} size={15 * size}>{label}</Text> : children}
            </TouchableOpacity>
            {helpComponent}
          </View>
        )
  )
}

const styles = createStyle({
  content: {
    flexGrow: 0,
    flexShrink: 1,
    marginRight: 15,
    alignItems: 'center',
    flexDirection: 'row',
  },
  boxWrap: {
    padding: 4,
    marginLeft: -4,
    marginRight: 4,
    alignItems: 'center',
    justifyContent: 'center',
  },
  label: {
    flexGrow: 0,
    flexShrink: 1,
    paddingRight: 3,
  },
  name: {
    marginTop: 1,
  },
  helpBtn: {
    paddingHorizontal: 8,
    paddingVertical: 3,
  },
})
