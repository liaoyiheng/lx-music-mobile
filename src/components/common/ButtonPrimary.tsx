import { memo } from 'react'

import Button, { type BtnProps } from '@/components/common/Button'
import Text from '@/components/common/Text'
import { useApple, radius } from '@/theme/apple'
import { createStyle } from '@/utils/tools'

export interface ButtonProps extends BtnProps {
  size?: number
  /**
   * filled：红底白字（主要操作）
   * tinted：淡红底红字（默认，次要操作）
   * plain：只有红字
   */
  variant?: 'filled' | 'tinted' | 'plain'
}

export default memo(({ disabled, size = 14, onPress, children, variant = 'tinted' }: ButtonProps) => {
  const apple = useApple()

  const backgroundColor = variant == 'filled' ? apple.accent : variant == 'tinted' ? apple.accentFill : 'transparent'
  const color = variant == 'filled' ? apple.onAccent : apple.accent

  return (
    <Button style={{ ...styles.button, backgroundColor }} onPress={onPress} disabled={disabled}>
      <Text size={size} color={color} style={styles.text}>{children}</Text>
    </Button>
  )
})

const styles = createStyle({
  button: {
    paddingHorizontal: 14,
    paddingVertical: 7,
    borderRadius: radius.medium,
    marginRight: 10,
    alignItems: 'center',
    justifyContent: 'center',
  },
  text: {
    fontWeight: '600',
  },
})
