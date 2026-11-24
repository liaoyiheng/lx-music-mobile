import { memo } from 'react'
import { StyleSheet, TouchableOpacity, View } from 'react-native'
import Section from '../../components/Section'
import { RadioRow } from '@/components/apple/RadioRow'
import Text from '@/components/common/Text'
import { useI18n } from '@/lang'
import { useApple, fontSize, fontWeight, hairline, radius, spacing } from '@/theme/apple'
import { toast } from '@/utils/tools'

const FutureMaterialRow = memo(({ label, onPress, showSeparator = true }: {
  label: string
  onPress: () => void
  showSeparator?: boolean
}) => {
  const apple = useApple()
  const t = useI18n()

  return (
    <View>
      <TouchableOpacity
        activeOpacity={0.65}
        onPress={onPress}
        style={styles.row}
      >
        <Text style={styles.label} size={fontSize.callout} color={apple.label} numberOfLines={1}>
          {label}
        </Text>
        <View style={[styles.badge, { backgroundColor: apple.fill }]}>
          <Text size={fontSize.caption2} color={apple.secondaryLabel} style={styles.badgeText}>
            {t('apple_setting_coming_soon')}
          </Text>
        </View>
      </TouchableOpacity>
      {showSeparator ? <View style={[styles.sep, { backgroundColor: apple.separator }]} /> : null}
    </View>
  )
})

/**
 * iOS 26 材质风格扩展位（当前默认启用液态玻璃，经典毛玻璃与固态哑光预留扩展）
 */
export default memo(() => {
  const t = useI18n()

  return (
    <Section title={t('apple_setting_material_title')}>
      <RadioRow
        label={t('apple_setting_material_glass')}
        description={t('apple_setting_material_glass_desc')}
        selected={true}
        onPress={() => {}}
        showSeparator={true}
      />
      <FutureMaterialRow
        label={t('apple_setting_material_frosted')}
        onPress={() => { toast(`${t('apple_setting_material_frosted')}：${t('apple_setting_coming_soon')}`) }}
        showSeparator={true}
      />
      <FutureMaterialRow
        label={t('apple_setting_material_matte')}
        onPress={() => { toast(`${t('apple_setting_material_matte')}：${t('apple_setting_coming_soon')}`) }}
        showSeparator={false}
      />
    </Section>
  )
})

const styles = StyleSheet.create({
  row: {
    minHeight: 48,
    paddingHorizontal: 16,
    paddingVertical: 10,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: spacing.md,
  },
  label: {
    flex: 1,
  },
  badge: {
    paddingHorizontal: 8,
    paddingVertical: 3,
    borderRadius: radius.pill,
  },
  badgeText: {
    fontWeight: fontWeight.semibold,
  },
  sep: {
    height: hairline,
    marginLeft: 16,
  },
})
