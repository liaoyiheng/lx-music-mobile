import { memo } from 'react'

import Button, { type BtnProps } from '@/components/common/Button'
import Text from '@/components/common/Text'
import { useApple, radius } from '@/theme/apple'
import { createStyle } from '@/utils/tools'

type ButtonProps = BtnProps

export default memo(({ disabled, onPress, children }: ButtonProps) => {
  const apple = useApple()

  return (
    <Button style={{ ...styles.button, backgroundColor: apple.accentFill }} onPress={onPress} disabled={disabled}>
      <Text size={14} color={apple.accent} style={styles.text}>{children}</Text>
    </Button>
  )
})

const styles = createStyle({
  button: {
    paddingHorizontal: 14,
    paddingVertical: 7,
    borderRadius: radius.medium,
    marginRight: 10,
    marginBottom: 8,
    alignItems: 'center',
    justifyContent: 'center',
  },
  text: {
    fontWeight: '600',
  },
})
