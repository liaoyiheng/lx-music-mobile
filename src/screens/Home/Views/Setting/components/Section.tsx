import { View } from 'react-native'

import { createStyle } from '@/utils/tools'
import { useApple, radius } from '@/theme/apple'
import Text from '@/components/common/Text'

interface Props {
  title: string
  children: React.ReactNode | React.ReactNode[]
}

/**
 * iOS 分组列表样式：灰色小标题 + 圆角白色卡片
 */
export default ({ title, children }: Props) => {
  const apple = useApple()

  return (
    <View style={styles.container}>
      <Text style={styles.title} size={13} color={apple.secondaryLabel}>{title}</Text>
      <View style={{ ...styles.card, backgroundColor: apple.secondaryBackground }}>
        {children}
      </View>
    </View>
  )
}

const styles = createStyle({
  container: {
    marginBottom: 22,
  },
  title: {
    paddingLeft: 16,
    marginBottom: 7,
    textTransform: 'uppercase',
    letterSpacing: 0.2,
  },
  card: {
    borderRadius: radius.large,
    paddingTop: 14,
    paddingBottom: 2,
    paddingRight: 12,
    overflow: 'hidden',
  },
})
