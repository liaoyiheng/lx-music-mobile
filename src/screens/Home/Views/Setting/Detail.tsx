import { memo } from 'react'
import { ScrollView, View } from 'react-native'
import { GlassCapsuleButton } from '@/components/apple/LiquidGlass'
import Text from '@/components/common/Text'
import { useI18n } from '@/lang'
import { useStatusbarHeight } from '@/store/common/hook'
import { useHorizontalMode } from '@/utils/hooks'
import { useApple, fontSize, fontWeight, pagePadding, size, spacing } from '@/theme/apple'
import { createStyle } from '@/utils/tools'
import { SettingContent, type SettingScreenIds } from './screens'

const NAV_HEIGHT = 48

const LIGHT_NAV_STOPS = [
  'rgba(242, 242, 247, 0.96)',
  'rgba(242, 242, 247, 0.86)',
  'rgba(242, 242, 247, 0.45)',
  'rgba(242, 242, 247, 0.0)',
] as const

const DARK_NAV_STOPS = [
  'rgba(0, 0, 0, 0.96)',
  'rgba(0, 0, 0, 0.86)',
  'rgba(0, 0, 0, 0.45)',
  'rgba(0, 0, 0, 0.0)',
] as const

/**
 * 设置二级页面：顶部悬浮液态玻璃导航栏（左侧胶囊返回按钮 + 居中 17pt Semibold 标题）+ 分组内容
 * onBack 不传时（宽屏右侧分栏）不显示返回按钮
 */
export default memo(({ id, onBack }: {
  id: SettingScreenIds
  onBack?: () => void
}) => {
  const apple = useApple()
  const t = useI18n()
  const statusBarHeight = useStatusbarHeight()
  const isWide = useHorizontalMode()
  const [s0, s1, s2, s3] = apple.isDark ? DARK_NAV_STOPS : LIGHT_NAV_STOPS
  const topInset = statusBarHeight + NAV_HEIGHT

  return (
    <View style={{ ...styles.container, backgroundColor: apple.groupedBackground }}>
      <ScrollView
        key={id}
        contentContainerStyle={{
          ...styles.content,
          paddingTop: topInset + spacing.md,
          paddingBottom: isWide ? size.miniPlayerClearance : size.dockClearance,
        }}
        keyboardShouldPersistTaps="always"
      >
        <SettingContent id={id} />
      </ScrollView>

      <View
        pointerEvents="box-none"
        style={[styles.navBar, { paddingTop: statusBarHeight }]}
      >
        <View pointerEvents="none" style={styles.navBackdrop}>
          <View style={{ flex: 2.2, backgroundColor: s0 }} />
          <View style={{ flex: 1, backgroundColor: s1 }} />
          <View style={{ flex: 0.8, backgroundColor: s2 }} />
          <View style={{ flex: 0.8, backgroundColor: s3 }} />
        </View>
        <View pointerEvents="box-none" style={styles.navInner}>
          <Text
            style={styles.navTitle}
            size={fontSize.headline}
            color={apple.label}
            numberOfLines={1}
          >
            {t(`setting_${id}`)}
          </Text>
          {onBack ? (
            <GlassCapsuleButton
              icon="chevron-back"
              label={t('nav_setting')}
              onPress={onBack}
              style={styles.backBtn}
            />
          ) : null}
        </View>
      </View>
    </View>
  )
})

const styles = createStyle({
  container: {
    flex: 1,
  },
  navBar: {
    position: 'absolute',
    top: 0,
    left: 0,
    right: 0,
    zIndex: 25,
    paddingBottom: spacing.sm,
  },
  navBackdrop: {
    position: 'absolute',
    top: 0,
    left: 0,
    right: 0,
    bottom: -10,
    flexDirection: 'column',
  },
  navInner: {
    height: NAV_HEIGHT,
    justifyContent: 'center',
  },
  navTitle: {
    textAlign: 'center',
    fontWeight: fontWeight.semibold,
    paddingHorizontal: 108,
    letterSpacing: -0.2,
  },
  backBtn: {
    position: 'absolute',
    left: spacing.lg,
  },
  content: {
    paddingHorizontal: pagePadding - spacing.xs,
  },
})
