import { memo } from 'react'
import { ScrollView, TouchableHighlight, View } from 'react-native'
import { IonIcon } from '@/components/common/Icon'
import Text from '@/components/common/Text'
import { useI18n } from '@/lang'
import { useSettingValue } from '@/store/setting/hook'
import { useStatus as useSyncStatus } from '@/store/sync/hook'
import { useVersionInfo } from '@/store/version/hook'
import { useHorizontalMode } from '@/utils/hooks'
import { useApple, fontSize, hairline, radius, size, spacing } from '@/theme/apple'
import { createStyle } from '@/utils/tools'
import { SETTING_GROUPS, SETTING_ICONS, type SettingScreenIds } from './screens'

const ICON_BOX = 29

const useRowSummary = (id: SettingScreenIds): string | undefined => {
  const t = useI18n()
  const syncStatus = useSyncStatus()
  const isSyncEnable = useSettingValue('sync.enable')
  const versionInfo = useVersionInfo()

  switch (id) {
    case 'basic':
      return t('apple_setting_material_glass').replace(/\s*\([^)]*\)/, '')
    case 'sync':
      if (syncStatus.status) return t('setting_sync_status_enabled')
      return isSyncEnable ? syncStatus.message || undefined : undefined
    case 'version':
      if (!versionInfo.isLatest && versionInfo.newVersion?.version) {
        return `v${versionInfo.newVersion.version}`
      }
      return `v${process.versions.app}`
    default:
      return undefined
  }
}

const Row = memo(({ id, isLast, active, onPress }: {
  id: SettingScreenIds
  isLast: boolean
  active: boolean
  onPress: (id: SettingScreenIds) => void
}) => {
  const apple = useApple()
  const t = useI18n()
  const { icon, color } = SETTING_ICONS[id]
  const summary = useRowSummary(id)

  return (
    <TouchableHighlight
      underlayColor={apple.highlight}
      onPress={() => { onPress(id) }}
      style={active ? { backgroundColor: apple.accent } : null}
    >
      <View style={styles.row}>
        <View style={{ ...styles.iconBox, backgroundColor: color }}>
          <IonIcon name={icon} rawSize={17} color="#FFFFFF" />
        </View>
        <View style={{
          ...styles.rowMain,
          borderBottomWidth: isLast ? 0 : hairline,
          borderBottomColor: apple.separator,
        }}>
          <Text style={styles.label} size={fontSize.callout} color={active ? apple.onAccent : apple.label} numberOfLines={1}>
            {t(`setting_${id}`)}
          </Text>
          {summary ? (
            <Text
              style={styles.summary}
              size={fontSize.subhead}
              color={active ? apple.onAccent : apple.secondaryLabel}
              numberOfLines={1}
            >
              {summary}
            </Text>
          ) : null}
          <IonIcon name="chevron-forward" rawSize={16} color={active ? apple.onAccent : apple.tertiaryLabel} />
        </View>
      </View>
    </TouchableHighlight>
  )
})

/**
 * 设置首页：iOS 26 分组圆角卡片列表，每行左侧 29×29 高饱和图标、右侧摘要与箭头
 * activeId 只在宽屏（左右分栏）时传入，用于高亮当前分类
 */
export default memo(({ activeId, onSelect }: {
  activeId?: SettingScreenIds | null
  onSelect: (id: SettingScreenIds) => void
}) => {
  const apple = useApple()
  const isWide = useHorizontalMode()
  const cardBorderColor = apple.isDark ? 'rgba(255, 255, 255, 0.08)' : 'rgba(0, 0, 0, 0.04)'

  return (
    <ScrollView
      style={{ backgroundColor: apple.groupedBackground }}
      contentContainerStyle={{
        ...styles.content,
        paddingBottom: isWide ? size.miniPlayerClearance : size.dockClearance,
      }}
      keyboardShouldPersistTaps="always"
    >
      {SETTING_GROUPS.map((group, index) => (
        <View
          key={index}
          style={[
            styles.card,
            {
              backgroundColor: apple.secondaryBackground,
              borderColor: cardBorderColor,
              shadowOpacity: apple.isDark ? 0 : 0.04,
              elevation: apple.isDark ? 0 : 1,
            },
          ]}
        >
          {group.map((id, i) => (
            <Row key={id} id={id} isLast={i == group.length - 1} active={activeId == id} onPress={onSelect} />
          ))}
        </View>
      ))}
    </ScrollView>
  )
})

const styles = createStyle({
  content: {
    paddingHorizontal: spacing.lg,
    paddingTop: spacing.xs,
  },
  card: {
    borderRadius: radius.sheet,
    overflow: 'hidden',
    marginBottom: spacing.xl,
    borderWidth: hairline,
    shadowColor: '#000000',
    shadowRadius: 6,
    shadowOffset: { width: 0, height: 1 },
  },
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    minHeight: 48,
    paddingLeft: 16,
  },
  iconBox: {
    width: ICON_BOX,
    height: ICON_BOX,
    borderRadius: radius.xs,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 13,
    shadowColor: '#000000',
    shadowOpacity: 0.12,
    shadowRadius: 4,
    shadowOffset: { width: 0, height: 2 },
  },
  rowMain: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
    alignSelf: 'stretch',
    minHeight: 48,
    paddingRight: 14,
  },
  label: {
    flex: 1,
    marginRight: spacing.sm,
  },
  summary: {
    marginRight: 6,
    maxWidth: 120,
  },
})
