import { View } from 'react-native'

import { createStyle } from '@/utils/tools'
import { useApple, fontSize, fontWeight, hairline, radius, spacing } from '@/theme/apple'
import Text from '@/components/common/Text'

interface Props {
  title: string
  children: React.ReactNode | React.ReactNode[]
}

/**
 * iOS 26 分组列表样式：灰色大写小标题 + 16dp 圆角卡片（带极细高光描边）
 */
export default ({ title, children }: Props) => {
  const apple = useApple()
  const cardBorderColor = apple.isDark ? 'rgba(255, 255, 255, 0.08)' : 'rgba(0, 0, 0, 0.04)'

  return (
    <View style={styles.container}>
      <Text style={styles.title} size={fontSize.footnote} color={apple.secondaryLabel}>{title}</Text>
      <View
        style={[
          styles.card,
          {
            backgroundColor: apple.secondaryBackground,
            borderColor: cardBorderColor,
            shadowOpacity: apple.isDark ? 0 : 0.03,
            elevation: apple.isDark ? 0 : 1,
          },
        ]}
      >
        {children}
      </View>
    </View>
  )
}

const styles = createStyle({
  container: {
    marginBottom: spacing.xl,
  },
  title: {
    paddingLeft: 14,
    marginBottom: 6,
    fontWeight: fontWeight.medium,
    textTransform: 'uppercase',
    letterSpacing: 0.3,
  },
  card: {
    borderRadius: radius.sheet,
    overflow: 'hidden',
    borderWidth: hairline,
    shadowColor: '#000000',
    shadowRadius: 4,
    shadowOffset: { width: 0, height: 1 },
  },
})
