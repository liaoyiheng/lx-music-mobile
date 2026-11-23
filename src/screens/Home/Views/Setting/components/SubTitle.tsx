import { memo } from 'react'
import { View } from 'react-native'
import { createStyle } from '@/utils/tools'
import { useApple, fontSize, fontWeight, hairline, spacing } from '@/theme/apple'
import Text from '@/components/common/Text'

/**
 * 分组卡片里的一行设置：上方标题 + 下方控件，底部带左缩进 hairline 分割线
 */
export default memo(({ title, children, showSeparator = true }: {
  title: string
  children: React.ReactNode | React.ReactNode[]
  showSeparator?: boolean
}) => {
  const apple = useApple()
  return (
    <View>
      <View style={styles.container}>
        <Text style={styles.title} size={fontSize.callout} color={apple.label}>{title}</Text>
        {children}
      </View>
      {showSeparator ? <View style={{ ...styles.sep, backgroundColor: apple.separator }} /> : null}
    </View>
  )
})

const styles = createStyle({
  container: {
    paddingHorizontal: spacing.lg,
    paddingVertical: spacing.md,
  },
  title: {
    fontWeight: fontWeight.medium,
    marginBottom: spacing.sm,
  },
  sep: {
    height: hairline,
    marginLeft: spacing.lg,
  },
})
