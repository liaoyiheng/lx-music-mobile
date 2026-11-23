import { memo } from 'react'
import { TouchableOpacity, View } from 'react-native'
import { Icon } from '@/components/common/Icon'
import Text from '@/components/common/Text'
import { useI18n } from '@/lang'
import { useNavActiveId } from '@/store/common/hook'
import { useApple, hairline, size } from '@/theme/apple'
import { createStyle } from '@/utils/tools'
import { TABS, getTabId, selectTab } from './tabs'

/**
 * Apple Music 风格底部标签栏：半透明底、细分割线、选中项为强调红
 */
export default memo(() => {
  const apple = useApple()
  const t = useI18n()
  const activeTab = getTabId(useNavActiveId())
  const inactiveColor = apple.isDark ? '#8E8E93' : '#999999'

  return (
    <View style={{
      ...styles.container,
      backgroundColor: apple.isDark ? 'rgba(22, 22, 22, 0.96)' : 'rgba(249, 249, 249, 0.96)',
      borderTopColor: apple.separator,
    }}>
      {TABS.map(tab => {
        const color = tab.id == activeTab ? apple.accent : inactiveColor
        return (
          <TouchableOpacity key={tab.id} style={styles.item} activeOpacity={0.6} onPress={() => { selectTab(tab.id) }}>
            <Icon name={tab.icon} size={22} color={color} />
            <Text style={styles.label} size={10} color={color} numberOfLines={1}>{t(tab.label)}</Text>
          </TouchableOpacity>
        )
      })}
    </View>
  )
})

const styles = createStyle({
  container: {
    flexDirection: 'row',
    height: size.tabBar,
    borderTopWidth: hairline,
  },
  item: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    paddingTop: 2,
  },
  label: {
    marginTop: 3,
    fontWeight: '500',
  },
})
