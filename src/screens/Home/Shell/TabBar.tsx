import { memo } from 'react'
import { StyleSheet, TouchableOpacity, View } from 'react-native'
import { IonIcon } from '@/components/common/Icon'
import Text from '@/components/common/Text'
import { GlassSurface } from '@/components/apple/LiquidGlass'
import { useI18n } from '@/lang'
import { useNavActiveId } from '@/store/common/hook'
import { useApple, fontWeight, radius, size } from '@/theme/apple'
import { createStyle } from '@/utils/tools'
import { TABS, getTabId, selectTab, type TabId } from './tabs'

const TAB_ICONS: Record<TabId, { active: string, inactive: string }> = {
  library: { active: 'albums', inactive: 'albums-outline' },
  browse: { active: 'grid', inactive: 'grid-outline' },
  search: { active: 'search', inactive: 'search-outline' },
  setting: { active: 'settings', inactive: 'settings-outline' },
}

/**
 * Apple Music iOS 26 悬浮液态玻璃胶囊底部标签栏：
 * 62dp 高、31dp 圆角玻璃外框 + 选中项亮面折射胶囊 Pill + 苹果红图标文字
 */
export default memo(() => {
  const apple = useApple()
  const t = useI18n()
  const activeTab = getTabId(useNavActiveId())

  return (
    <GlassSurface borderRadius={radius.tabBar} innerStyle={styles.bar}>
      {TABS.map(tab => {
        const isActive = tab.id == activeTab
        const color = isActive ? apple.accent : apple.secondaryLabel
        const iconName = isActive ? TAB_ICONS[tab.id].active : TAB_ICONS[tab.id].inactive
        return (
          <TouchableOpacity
            key={tab.id}
            style={[
              styles.item,
              isActive && {
                ...styles.activePill,
                backgroundColor: apple.glassPillBg,
                borderColor: apple.glassPillBorder,
                shadowOpacity: apple.isDark ? 0.34 : 0.10,
              },
            ]}
            activeOpacity={0.72}
            onPress={() => { selectTab(tab.id) }}
            accessibilityRole="tab"
            accessibilityState={{ selected: isActive }}
          >
            <IonIcon name={iconName} rawSize={22} color={color} />
            <Text style={styles.label} size={10} color={color} numberOfLines={1}>
              {t(tab.label)}
            </Text>
          </TouchableOpacity>
        )
      })}
    </GlassSurface>
  )
})

const styles = createStyle({
  bar: {
    flexDirection: 'row',
    alignItems: 'stretch',
    height: size.tabBar,
    padding: 5,
    gap: 2,
  },
  item: {
    flex: 1,
    borderRadius: 26,
    alignItems: 'center',
    justifyContent: 'center',
    gap: 2,
  },
  activePill: {
    borderWidth: StyleSheet.hairlineWidth,
    shadowColor: '#000',
    shadowRadius: 10,
    shadowOffset: { width: 0, height: 3 },
    elevation: 3,
  },
  label: {
    fontWeight: fontWeight.semibold,
    letterSpacing: -0.1,
  },
})
