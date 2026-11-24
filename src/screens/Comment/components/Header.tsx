import { memo } from 'react'
import { TouchableOpacity, View } from 'react-native'

import { GlassCircleButton } from '@/components/apple/LiquidGlass'
import { IonIcon } from '@/components/common/Icon'
import { pop } from '@/navigation'
import StatusBar from '@/components/common/StatusBar'
import { useI18n } from '@/lang'
import { createStyle } from '@/utils/tools'
import Text from '@/components/common/Text'
import commonState from '@/store/common/state'
import { useStatusbarHeight } from '@/store/common/hook'
import { useApple, fontSize, fontWeight, hairline, radius, size, spacing } from '@/theme/apple'

const formatCount = (n: number) => {
  if (n >= 10000) return `${(n / 10000).toFixed(1)}w`
  if (n >= 1000) return `${(n / 1000).toFixed(1)}k`
  return String(n)
}

/**
 * 评论页吸顶液态玻璃导航栏：左侧返回胶囊圆形按钮，居中双行标题（「评论」+ 歌曲名·歌手），右侧评论数徽章
 */
export default memo(({ musicInfo, totalCount = 0, onRefresh }: {
  musicInfo: LX.Music.MusicInfo
  totalCount?: number
  onRefresh?: () => void
}) => {
  const t = useI18n()
  const apple = useApple()
  const statusBarHeight = useStatusbarHeight()

  const back = () => {
    void pop(commonState.componentIds.comment!)
  }

  const heading = t('comment_title', { name: '' }).includes('Comments') ? 'Comments' : '评论'
  const subtitle = [musicInfo.name, musicInfo.singer].filter(Boolean).join(' · ')

  return (
    <View
      style={{
        paddingTop: statusBarHeight,
        backgroundColor: apple.glassBgDense,
        borderBottomColor: apple.separator,
        borderBottomWidth: hairline,
      }}
    >
      <StatusBar />
      <View style={styles.bar}>
        <GlassCircleButton
          icon="chevron-back"
          iconSize={18}
          size={34}
          onPress={back}
          accessibilityLabel={t('back')}
        />

        <View style={styles.titleWrap} pointerEvents="none">
          <Text numberOfLines={1} size={fontSize.callout} color={apple.label} style={styles.title}>
            {heading}
          </Text>
          {subtitle ? (
            <Text numberOfLines={1} size={fontSize.caption1} color={apple.secondaryLabel} style={styles.subtitle}>
              {subtitle}
            </Text>
          ) : null}
        </View>

        <View style={styles.rightWrap}>
          {totalCount > 0 ? (
            <TouchableOpacity
              activeOpacity={onRefresh ? 0.65 : 1}
              onPress={onRefresh}
              disabled={!onRefresh}
              style={[styles.badge, { backgroundColor: apple.fill }]}
            >
              <Text size={fontSize.caption1} color={apple.secondaryLabel} style={styles.badgeText}>
                {formatCount(totalCount)}
              </Text>
            </TouchableOpacity>
          ) : onRefresh ? (
            <TouchableOpacity style={styles.refreshBtn} activeOpacity={0.6} onPress={onRefresh}>
              <IonIcon name="refresh" rawSize={18} color={apple.accent} />
            </TouchableOpacity>
          ) : (
            <View style={styles.placeholder} />
          )}
        </View>
      </View>
    </View>
  )
})

const styles = createStyle({
  bar: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    height: size.hitTarget + 4,
    paddingHorizontal: spacing.lg,
  },
  titleWrap: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: spacing.sm,
  },
  title: {
    fontWeight: fontWeight.semibold,
    letterSpacing: -0.25,
    textAlign: 'center',
  },
  subtitle: {
    textAlign: 'center',
    marginTop: 1,
  },
  rightWrap: {
    minWidth: 34,
    alignItems: 'flex-end',
    justifyContent: 'center',
  },
  badge: {
    paddingHorizontal: 8,
    paddingVertical: 3,
    borderRadius: radius.pill,
  },
  badgeText: {
    fontWeight: fontWeight.semibold,
    fontVariant: ['tabular-nums'],
  },
  refreshBtn: {
    width: 34,
    height: 34,
    alignItems: 'center',
    justifyContent: 'center',
  },
  placeholder: {
    width: 34,
  },
})
