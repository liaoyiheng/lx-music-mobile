import { memo } from 'react'
import { ScrollView, TouchableOpacity, View } from 'react-native'
import { Icon } from '@/components/common/Icon'
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
}
interface SidebarSection {
  title?: 'apple_tab_browse' | 'apple_tab_library'
  items: SidebarItem[]
}

const SECTIONS: SidebarSection[] = [
  { items: [{ id: 'nav_search', icon: 'search-2' }] },
  { title: 'apple_tab_browse', items: [{ id: 'nav_songlist', icon: 'album' }, { id: 'nav_top', icon: 'leaderboard' }] },
  { title: 'apple_tab_library', items: [{ id: 'nav_love', icon: 'love' }] },
  { items: [{ id: 'nav_setting', icon: 'setting' }] },
]

const Item = memo(({ item, active }: { item: SidebarItem, active: boolean }) => {
  const apple = useApple()
  const t = useI18n()
  return (
    <TouchableOpacity
      activeOpacity={0.6}
      style={{ ...styles.item, backgroundColor: active ? apple.fill : 'transparent' }}
      onPress={() => { setNavActiveId(item.id) }}
    >
      <View style={styles.icon}>
        <Icon name={item.icon} size={18} color={apple.accent} />
      </View>
      <Text
        style={{ ...styles.itemLabel, fontWeight: active ? fontWeight.semibold : fontWeight.regular }}
        size={fontSize.callout}
        color={apple.label}
        numberOfLines={1}
      >{t(item.id)}</Text>
    </TouchableOpacity>
  )
})

/**
 * 宽屏 / 横屏时替代底部标签栏的左侧边栏（类 iPad 版 Apple Music）
 */
export default memo(() => {
  const apple = useApple()
  const t = useI18n()
  const navId = useNavActiveId()
  const statusBarHeight = useStatusbarHeight()
  const windowSize = useWindowSize()
  const width = Math.min(size.sidebar, Math.max(200, windowSize.width * 0.28))

  return (
    <View style={{
      ...styles.container,
      width,
      paddingTop: statusBarHeight,
      backgroundColor: apple.isDark ? '#1C1C1E' : '#F2F2F7',
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
    height: '100%',
    borderRightWidth: hairline,
  },
  list: {
    paddingVertical: spacing.md,
  },
  appName: {
    fontWeight: fontWeight.bold,
    paddingHorizontal: spacing.xl,
    paddingBottom: spacing.md,
  },
  section: {
    marginBottom: spacing.lg,
  },
  sectionTitle: {
    fontWeight: fontWeight.semibold,
    paddingHorizontal: spacing.xl,
    paddingBottom: spacing.xs,
  },
  item: {
    flexDirection: 'row',
    alignItems: 'center',
    height: 40,
    marginHorizontal: spacing.sm,
    paddingHorizontal: spacing.md,
    borderRadius: radius.medium,
  },
  icon: {
    width: 24,
    alignItems: 'center',
  },
  itemLabel: {
    flex: 1,
    marginLeft: spacing.md,
  },
})
