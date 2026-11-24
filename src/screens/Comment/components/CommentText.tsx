import { memo, useMemo, useState } from 'react'
import { TouchableOpacity, View } from 'react-native'
import { createStyle } from '@/utils/tools'
import Text from '@/components/common/Text'
import { useApple, fontSize as appleFontSize, fontWeight, spacing } from '@/theme/apple'

const TEXT_LIMIT = 160
const SUB_TEXT_LIMIT = TEXT_LIMIT * 1.2

/**
 * 评论正文：15pt 行高 22，超长时折叠，点击「展开 / 收起」切换
 */
export default memo(({ text, size = appleFontSize.subhead }: { text: string, size?: number }) => {
  const [show, setShow] = useState(false)
  const apple = useApple()

  const length = useMemo(() => {
    let count = 0
    let bCount = 0
    let subLength = 0
    for (let i = 0; i < text.length; i++) {
      let char = text.charAt(i)
      if (char == '\n') {
        count += bCount * 24 + 20
        bCount++
      } else {
        count++
        if (char.trim() != '') bCount &&= 0
      }
      if (!subLength && count > TEXT_LIMIT) subLength = i
      if (count >= SUB_TEXT_LIMIT) return subLength
    }
    return 0
  }, [text])

  const lineHeight = size === 15 ? 22 : Math.round(size * 1.45)
  const textStyle = { ...styles.text, lineHeight }

  return (
    length ? (
      <View>
        {
          show
            ? <Text selectable style={textStyle} size={size} color={apple.label}>{text}</Text>
            : <Text selectable style={textStyle} size={size} color={apple.label}>{text.substring(0, length)} <Text size={size} color={apple.secondaryLabel}>……</Text></Text>
        }
        <TouchableOpacity style={styles.toggle} activeOpacity={0.5} onPress={() => { setShow(!show) }}>
          <Text size={appleFontSize.footnote} color={apple.accent} style={styles.toggleText}>{show ? global.i18n.t('comment_hide_text') : global.i18n.t('comment_show_text')}</Text>
        </TouchableOpacity>
      </View>
    ) : <Text selectable style={textStyle} size={size} color={apple.label}>{text}</Text>
  )
})

const styles = createStyle({
  text: {
    marginTop: spacing.xs,
  },
  toggle: {
    marginTop: spacing.xs,
    paddingVertical: spacing.xs,
    alignSelf: 'flex-start',
  },
  toggleText: {
    fontWeight: fontWeight.medium,
  },
})
