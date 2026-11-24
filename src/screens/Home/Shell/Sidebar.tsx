import { memo } from 'react'
import { ScrollView, StyleSheet, TouchableOpacity, View } from 'react-native'
import { IonIcon } from '@/components/common/Icon'
import Text from '@/components/common/Text'
import { useI18n } from '@/lang'
import { useNavActiveId, useStatusbarHeight } from '@/store/common/hook'
import { setNavActiveId } from '@/core/common'
import type { NAV_ID_Type } from '@/config/constant'
import { useWindowSize } from '@/utils/hooks'
import { useApple, fontSize, fontWeight, hairline, radius, size, spacing } from '@/theme/apple'
import { createStyle } from '@/utils/tools'

interface SidebarItem {
  id: NAV_ID_Type
  icon: string
  activeIcon: string
}
interface SidebarSection {
  title?: 'apple_tab_browse' | 'apple_tab_library'
  items: SidebarItem[]
}

const SECTIONS: SidebarSection[] = [
  { items: [{ id: 'nav_search', icon: 'search-outline', activeIcon: 'search' }] },
  {
    title: 'apple_tab_browse',
    items: [
      { id: 'nav_songlist', icon: 'grid-outline', activeIcon: 'grid' },
      { id: 'nav_top', icon: 'podium-outline', activeIcon: 'podium' },
    ],
  },
  { title: 'apple_tab_library', items: [{ id: 'nav_love', icon: 'albums-outline', activeIcon: 'albums' }] },
  { items: [{ id: 'nav_setting', icon: 'settings-outline', activeIcon: 'settings' }] },
]

const Item = memo(({ item, active }: { item: SidebarItem, active: boolean }) => {
  const apple = useApple()
  const t = useI18n()
  return (
    <TouchableOpacity
      activeOpacity={0.68}
      style={[
        styles.item,
        active && {
          backgroundColor: apple.glassPillBg,
          borderColor: apple.glassPillBorder,
          borderWidth: StyleSheet.hairlineWidth,
        },
      ]}
      onPress={() => { setNavActiveId(item.id) }}
    >
      <View style={styles.icon}>
        <IonIcon name={active ? item.activeIcon : item.icon} rawSize={19} color={active ? apple.accent : apple.secondaryLabel} />
      </View>
      <Text
        style={{ ...styles.itemLabel, fontWeight: active ? fontWeight.semibold : fontWeight.regular }}
        size={fontSize.callout}
        color={active ? apple.accent : apple.label}
        numberOfLines={1}
      >
        {t(item.id)}
      </Text>
    </TouchableOpacity>
  )
})

/**
 * 宽屏 / 横屏时替代底部标签栏的左侧边栏（类 iPadOS 26 Liquid Glass Apple Music）
 */
export default memo(() => {
  const apple = useApple()
  const t = useI18n()
  const navId = useNavActiveId()
  const statusBarHeight = useStatusbarHeight()
  const windowSize = useWindowSize()
  const width = Math.min(size.sidebar, Math.max(208, windowSize.width * 0.26))

  return (
    <View style={{
      ...styles.container,
      width,
      paddingTop: statusBarHeight,
      backgroundColor: apple.isDark ? '#141416' : '#F2F2F7',
      borderRightColor: apple.separator,
    }}>
      <ScrollView contentContainerStyle={styles.list}>
        <Text style={styles.appName} size={fontSize.title2} color={apple.label}>LX Music</Text>
        {SECTIONS.map((section, index) => (
          <View key={index} style={styles.section}>
            {section.title
              ? <Text style={styles.sectionTitle} size={fontSize.footnote} color={apple.secondaryLabel}>{t(section.title)}</Text>
              : null}
            {section.items.map(item => <Item key={item.id} item={item} active={item.id == navId} />)}
          </View>
        ))}
      </ScrollView>
    </View>
  )
})

const styles = createStyle({
  container: {
    borderRightWidth: hairline,
  },
  list: {
    paddingHorizontal: spacing.md,
    paddingBottom: spacing.xl,
  },
  appName: {
    fontWeight: fontWeight.bold,
    paddingHorizontal: spacing.sm,
    paddingVertical: spacing.lg,
    letterSpacing: -0.4,
  },
  section: {
    marginBottom: spacing.lg,
  },
  sectionTitle: {
    fontWeight: fontWeight.semibold,
    paddingHorizontal: spacing.sm,
    paddingBottom: spacing.xs,
  },
  item: {
    flexDirection: 'row',
    alignItems: 'center',
    height: size.settingRow,
    paddingHorizontal: spacing.md,
    borderRadius: radius.large,
    marginBottom: 2,
  },
  icon: {
    width: 24,
    alignItems: 'center',
    marginRight: spacing.md,
  },
  itemLabel: {
    flex: 1,
  },
})
