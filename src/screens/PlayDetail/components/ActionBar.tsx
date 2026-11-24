// Modified for Apple Music iOS 26 Liquid Glass UI. Original: lyswhut/lx-music-mobile (Apache-2.0)
import { memo } from 'react'
import { TouchableOpacity, View } from 'react-native'
import { IonIcon } from '@/components/common/Icon'
import { useTimeInfo } from '@/components/TimeoutExitEditModal'
import { useI18n } from '@/lang'
import { spacing } from '@/theme/apple'
import { createStyle } from '@/utils/tools'

export type PlayerViewMode = 'cover' | 'lyric' | 'queue'

const ICON_INACTIVE = 'rgba(255, 255, 255, 0.72)'
const ICON_ACTIVE_DARK = '#16161C'

const FootButton = memo(({
  icon,
  active = false,
  badgeDot = false,
  accessibilityLabel,
  onPress,
}: {
  icon: string
  active?: boolean
  badgeDot?: boolean
  accessibilityLabel?: string
  onPress: () => void
}) => {
  return (
    <TouchableOpacity
      style={[styles.btn, active ? styles.btnActiveLight : null]}
      activeOpacity={0.65}
      onPress={onPress}
      accessibilityRole="button"
      accessibilityLabel={accessibilityLabel}
    >
      <IonIcon
        name={icon}
        rawSize={21}
        color={active ? ICON_ACTIVE_DARK : ICON_INACTIVE}
      />
      {badgeDot ? <View style={styles.badgeDot} /> : null}
    </TouchableOpacity>
  )
})

/**
 * 底部三键直达切换栏（对齐 part3-player-settings.html `.player-bottom-bar`）：
 * - 左侧歌词键（chatbubble-ellipses-outline / chatbubble-ellipses，激活态白底反色圆角方块）
 * - 中间更多/功能键（ellipsis-horizontal-circle-outline，弹出 SettingPopup）
 * - 右侧待播清单键（list，激活态白底反色圆角方块）
 */
export default memo(({
  activeView,
  onToggleLyric,
  onShowSetting,
  onToggleQueue,
}: {
  activeView: PlayerViewMode
  onToggleLyric: () => void
  onShowSetting: () => void
  onToggleQueue: () => void
}) => {
  const t = useI18n()
  const timeInfo = useTimeInfo()
  const lyricActive = activeView === 'lyric'
  const queueActive = activeView === 'queue'

  return (
    <View style={styles.container}>
      <FootButton
        icon={lyricActive ? 'chatbubble-ellipses' : 'chatbubble-ellipses-outline'}
        active={lyricActive}
        accessibilityLabel={t('apple_player_lyrics')}
        onPress={onToggleLyric}
      />
      <FootButton
        icon="ellipsis-horizontal-circle-outline"
        badgeDot={timeInfo.active}
        accessibilityLabel={t('play_detail_setting_title')}
        onPress={onShowSetting}
      />
      <FootButton
        icon="list"
        active={queueActive}
        accessibilityLabel={t('apple_player_up_next')}
        onPress={onToggleQueue}
      />
    </View>
  )
})

const styles = createStyle({
  container: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 18,
    paddingTop: spacing.xs,
    paddingBottom: spacing.xs,
  },
  btn: {
    width: 42,
    height: 36,
    borderRadius: 10,
    justifyContent: 'center',
    alignItems: 'center',
    position: 'relative',
  },
  btnActiveLight: {
    backgroundColor: 'rgba(255, 255, 255, 0.90)',
    shadowColor: '#000000',
    shadowOpacity: 0.22,
    shadowRadius: 10,
    shadowOffset: { width: 0, height: 4 },
    elevation: 4,
  },
  badgeDot: {
    position: 'absolute',
    top: 5,
    right: 6,
    width: 6,
    height: 6,
    borderRadius: 3,
    backgroundColor: '#FF375F',
  },
})
