import { memo } from 'react'

import { View } from 'react-native'
import { createStyle } from '@/utils/tools'
import { useApple, hairline } from '@/theme/apple'
import Text from '@/components/common/Text'

/**
 * 分组卡片里的一行设置：左侧标题 + 下方控件，行与行之间用细分割线隔开
 */
export default memo(({ title, children }: {
  title: string
  children: React.ReactNode | React.ReactNode[]
}) => {
  const apple = useApple()
  return (
    <View style={{ ...styles.container, borderBottomColor: apple.separator }}>
      <Text style={styles.title} size={16} color={apple.label}>{title}</Text>
      {children}
    </View>
  )
})

const styles = createStyle({
  container: {
    paddingLeft: 16,
    paddingBottom: 12,
    marginBottom: 12,
    borderBottomWidth: hairline,
  },
  title: {
    marginBottom: 10,
  },
})
