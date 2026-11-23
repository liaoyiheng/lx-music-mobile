import { memo } from 'react'
import { ScrollView, TouchableOpacity, View } from 'react-native'
import { IonIcon } from '@/components/common/Icon'
import Text from '@/components/common/Text'
import { useI18n } from '@/lang'
import { useStatusbarHeight } from '@/store/common/hook'
import { useApple, fontSize, fontWeight, hairline, pagePadding, size, spacing } from '@/theme/apple'
import { createStyle } from '@/utils/tools'
import { SettingContent, type SettingScreenIds } from './screens'

/**
 * 设置二级页面：顶部导航栏（左上角「‹ 设置」返回 + 居中标题）+ 分组内容
 * onBack 不传时（宽屏右侧分栏）不显示返回按钮
 */
export default memo(({ id, onBack }: {
  id: SettingScreenIds
  onBack?: () => void
}) => {
  const apple = useApple()
  const t = useI18n()
  const statusBarHeight = useStatusbarHeight()

  return (
    <View style={{ ...styles.container, backgroundColor: apple.groupedBackground }}>
      <View style={{
        ...styles.navBar,
        paddingTop: statusBarHeight,
        backgroundColor: apple.groupedBackground,
        borderBottomColor: apple.separator,
      }}>
        <View style={styles.navInner}>
          <Text style={styles.navTitle} size={fontSize.headline} color={apple.label} numberOfLines={1}>{t(`setting_${id}`)}</Text>
          {onBack
            ? (
                <TouchableOpacity style={styles.backBtn} activeOpacity={0.5} onPress={onBack}>
                  <IonIcon name="chevron-back" rawSize={24} color={apple.accent} style={styles.backIcon} />
                  <Text size={fontSize.body} color={apple.accent} numberOfLines={1}>{t('nav_setting')}</Text>
                </TouchableOpacity>
              )
            : null}
        </View>
      </View>
      <ScrollView
        key={id}
        contentContainerStyle={styles.content}
        keyboardShouldPersistTaps="always"
      >
        <SettingContent id={id} />
      </ScrollView>
    </View>
  )
})

const styles = createStyle({
  container: {
    flex: 1,
  },
  navBar: {
    borderBottomWidth: hairline,
  },
  navInner: {
    height: size.hitTarget,
    justifyContent: 'center',
  },
  navTitle: {
    textAlign: 'center',
    fontWeight: fontWeight.semibold,
    paddingHorizontal: 96,
  },
  backBtn: {
    position: 'absolute',
    left: spacing.xs,
    top: 0,
    bottom: 0,
    flexDirection: 'row',
    alignItems: 'center',
    paddingRight: spacing.sm,
  },
  backIcon: {
    marginRight: -2,
  },
  content: {
    paddingHorizontal: pagePadding,
    paddingTop: spacing.xl,
    paddingBottom: spacing.xxxl + spacing.lg,
  },
})
