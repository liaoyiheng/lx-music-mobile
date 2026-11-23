import { memo } from 'react'
import { TouchableOpacity, View } from 'react-native'
import { IonIcon } from '@/components/common/Icon'
import Text from '@/components/common/Text'
import { useI18n } from '@/lang'
import { useNavActiveId } from '@/store/common/hook'
import { useApple, hairline, size } from '@/theme/apple'
import { createStyle } from '@/utils/tools'
import { TABS, getTabId, selectTab, type TabId } from './tabs'

const TAB_ICONS: Record<TabId, { active: string, inactive: string }> = {
  library: { active: 'albums', inactive: 'albums-outline' },
  browse: { active: 'grid', inactive: 'grid-outline' },
  search: { active: 'search', inactive: 'search-outline' },
  setting: { active: 'settings', inactive: 'settings-outline' },
}

/**
 * Apple Music 风格底部标签栏：半透明底、细分割线、选中项为强调红
 */
export default memo(() => {
  const apple = useApple()
  const t = useI18n()
  const activeTab = getTabId(useNavActiveId())

  return (
    <View style={{
      ...styles.container,
      backgroundColor: apple.isDark ? 'rgba(22, 22, 22, 0.96)' : 'rgba(249, 249, 249, 0.96)',
      borderTopColor: apple.separator,
    }}>
      {TABS.map(tab => {
        const isActive = tab.id == activeTab
        const color = isActive ? apple.accent : apple.secondaryLabel
        const iconName = isActive ? TAB_ICONS[tab.id].active : TAB_ICONS[tab.id].inactive
        return (
          <TouchableOpacity key={tab.id} style={styles.item} activeOpacity={0.6} onPress={() => { selectTab(tab.id) }}>
            <IonIcon name={iconName} size={23} color={color} />
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
    marginTop: 2,
    fontWeight: '500',
  },
})
