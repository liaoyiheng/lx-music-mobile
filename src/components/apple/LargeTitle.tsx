import { memo } from 'react'
import { View } from 'react-native'
import Text from '@/components/common/Text'
import { useStatusbarHeight } from '@/store/common/hook'
import { useApple, fontSize, fontWeight, pagePadding, spacing } from '@/theme/apple'
import { createStyle } from '@/utils/tools'

interface Props {
  title: string
  /** 标题右侧的附加内容（按钮等） */
  right?: React.ReactNode
  /** 是否在顶部留出状态栏高度 */
  withStatusBar?: boolean
}

/**
 * Apple Music 风格页面大标题：左对齐、粗体、34 号字
 */
export default memo(({ title, right, withStatusBar = true }: Props) => {
  const apple = useApple()
  const statusBarHeight = useStatusbarHeight()

  return (
    <View style={{
      ...styles.container,
      paddingTop: (withStatusBar ? statusBarHeight : 0) + spacing.md,
      backgroundColor: apple.background,
    }}>
      <Text style={styles.title} size={fontSize.largeTitle} color={apple.label} numberOfLines={1}>{title}</Text>
      {right ? <View style={styles.right}>{right}</View> : null}
    </View>
  )
})

const styles = createStyle({
  container: {
    flexDirection: 'row',
    alignItems: 'flex-end',
    paddingHorizontal: pagePadding,
    paddingBottom: spacing.sm,
  },
  title: {
    flex: 1,
    fontWeight: fontWeight.bold,
    letterSpacing: 0.2,
  },
  right: {
    marginLeft: spacing.sm,
    marginBottom: spacing.xs,
    flexDirection: 'row',
    alignItems: 'center',
  },
})
