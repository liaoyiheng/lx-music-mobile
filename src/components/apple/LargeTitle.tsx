import { memo } from 'react'
import { View } from 'react-native'
import Text from '@/components/common/Text'
import { useStatusbarHeight } from '@/store/common/hook'
import { useApple, fontSize, fontWeight, pagePadding, spacing } from '@/theme/apple'
import { createStyle } from '@/utils/tools'

interface Props {
  title: string
  /** 标题右侧的附加内容（液态玻璃胶囊/圆形按钮等） */
  right?: React.ReactNode
  /** 是否在顶部留出状态栏高度 */
  withStatusBar?: boolean
  /** 自定义背景色（例如设置页分组背景） */
  backgroundColor?: string
}

/**
 * Apple Music iOS 26 风格页面大标题：34pt 粗体、紧凑字距、20pt 边距
 */
export default memo(({ title, right, withStatusBar = true, backgroundColor }: Props) => {
  const apple = useApple()
  const statusBarHeight = useStatusbarHeight()

  return (
    <View style={{
      ...styles.container,
      paddingTop: (withStatusBar ? statusBarHeight : 0) + spacing.md,
      backgroundColor: backgroundColor ?? apple.background,
    }}>
      <Text style={styles.title} size={fontSize.largeTitle} color={apple.label} numberOfLines={1}>{title}</Text>
      {right ? <View style={styles.right}>{right}</View> : null}
    </View>
  )
})

const styles = createStyle({
  container: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: pagePadding,
    paddingBottom: 10,
  },
  title: {
    flex: 1,
    fontWeight: fontWeight.bold,
    letterSpacing: -0.6,
  },
  right: {
    marginLeft: spacing.sm,
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.sm,
  },
})
