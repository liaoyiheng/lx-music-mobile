import { memo, type ComponentProps } from 'react'
import { View } from 'react-native'
import { Icon } from '@/components/common/Icon'
import { GlassCapsuleButton } from '@/components/apple/LiquidGlass'
import Text from '@/components/common/Text'
import Loading from '@/components/common/Loading'
import { useApple, fontSize, spacing } from '@/theme/apple'
import { createStyle } from '@/utils/tools'

interface Props {
  icon?: ComponentProps<typeof Icon>['name']
  title: string
  loading?: boolean
  actionLabel?: string
  onAction?: () => void
}

/**
 * 评论页的空状态 / 加载中 / 加载失败占位视图（居中图标 + 说明文字 + 液态玻璃胶囊重试按钮）
 */
export default memo(({ icon, title, loading, actionLabel, onAction }: Props) => {
  const apple = useApple()

  return (
    <View style={styles.container}>
      {
        loading
          ? <Loading color={apple.secondaryLabel} size={fontSize.title3} />
          : icon
            ? <Icon name={icon} size={fontSize.largeTitle} color={apple.tertiaryLabel} />
            : null
      }
      <Text style={styles.title} size={fontSize.callout} color={apple.secondaryLabel}>{title}</Text>
      {
        actionLabel && onAction
          ? (
              <GlassCapsuleButton
                label={actionLabel}
                icon="refresh"
                onPress={onAction}
                style={styles.action}
              />
            )
          : null
      }
    </View>
  )
})

const styles = createStyle({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: spacing.xxxl,
    paddingVertical: spacing.xxxl,
  },
  title: {
    marginTop: spacing.md,
    textAlign: 'center',
  },
  action: {
    marginTop: spacing.lg,
  },
})
