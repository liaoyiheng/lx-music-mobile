import { memo } from 'react'
import { ScrollView, TouchableHighlight, View } from 'react-native'
import { Icon } from '@/components/common/Icon'
import Text from '@/components/common/Text'
import { useI18n } from '@/lang'
import { useApple, fontSize, hairline, pagePadding, radius, size, spacing } from '@/theme/apple'
import { createStyle } from '@/utils/tools'
import { SETTING_GROUPS, SETTING_ICONS, type SettingScreenIds } from './screens'

const ICON_BOX = 29

const Row = memo(({ id, isLast, active, onPress }: {
  id: SettingScreenIds
  isLast: boolean
  active: boolean
  onPress: (id: SettingScreenIds) => void
}) => {
  const apple = useApple()
  const t = useI18n()
  const { icon, color } = SETTING_ICONS[id]

  return (
    <TouchableHighlight
      underlayColor={apple.highlight}
      onPress={() => { onPress(id) }}
      style={active ? { backgroundColor: apple.accent } : null}
    >
      <View style={styles.row}>
        <View style={{ ...styles.iconBox, backgroundColor: color }}>
          <Icon name={icon} rawSize={16} color="#FFFFFF" />
        </View>
        <View style={{
          ...styles.rowMain,
          borderBottomWidth: isLast ? 0 : hairline,
          borderBottomColor: apple.separator,
        }}>
          <Text style={styles.label} size={fontSize.body} color={active ? apple.onAccent : apple.label} numberOfLines={1}>
            {t(`setting_${id}`)}
          </Text>
          <Icon name="chevron-right" rawSize={13} color={active ? apple.onAccent : apple.tertiaryLabel} />
        </View>
      </View>
    </TouchableHighlight>
  )
})

/**
 * 设置首页：iOS 风格分组列表，每组一张圆角卡片，每行左侧彩色图标、右侧箭头
 * activeId 只在宽屏（左右分栏）时传入，用于高亮当前分类
 */
export default memo(({ activeId, onSelect }: {
  activeId?: SettingScreenIds | null
  onSelect: (id: SettingScreenIds) => void
}) => {
  const apple = useApple()

  return (
    <ScrollView
      style={{ backgroundColor: apple.groupedBackground }}
      contentContainerStyle={styles.content}
      keyboardShouldPersistTaps="always"
    >
      {SETTING_GROUPS.map((group, index) => (
        <View key={index} style={{ ...styles.card, backgroundColor: apple.secondaryBackground }}>
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
    paddingHorizontal: pagePadding,
    paddingTop: spacing.sm,
    paddingBottom: spacing.xxxl,
  },
  card: {
    borderRadius: radius.large,
    overflow: 'hidden',
    marginBottom: spacing.xxl + spacing.xs,
  },
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    minHeight: size.settingRow,
    paddingLeft: spacing.lg,
  },
  iconBox: {
    width: ICON_BOX,
    height: ICON_BOX,
    borderRadius: 7,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: spacing.md,
  },
  rowMain: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
    alignSelf: 'stretch',
    minHeight: size.settingRow,
    paddingRight: spacing.lg,
  },
  label: {
    flex: 1,
    marginRight: spacing.sm,
  },
})

