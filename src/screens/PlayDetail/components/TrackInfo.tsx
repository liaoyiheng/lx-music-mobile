// Modified for Apple Music iOS 26 Liquid Glass UI. Original: lyswhut/lx-music-mobile (Apache-2.0)
import { memo, useCallback, useEffect, useState } from 'react'
import { TouchableOpacity, View } from 'react-native'
import { CoverArt, GlassCircleButton } from '@/components/apple/LiquidGlass'
import Text from '@/components/common/Text'
import { LIST_IDS } from '@/config/constant'
import { addListMusics, getListMusics, removeListMusics } from '@/core/list'
import { useI18n } from '@/lang'
import { usePlayerMusicInfo } from '@/store/player/hook'
import playerState from '@/store/player/state'
import settingState from '@/store/setting/state'
import { fontWeight, pagePadding, spacing } from '@/theme/apple'
import { createStyle, toast } from '@/utils/tools'

export interface TrackActionProps {
  onShowSetting?: () => void
  onShowMusicAdd?: () => void
}

/**
 * 同步的双液态玻璃圆按钮（34×34）：
 * 1. 收藏按钮（heart / heart-outline，在「我的收藏」中呈亮白底红心 active 态；点击直接加入/移出收藏，长按弹出添加到歌单）
 * 2. 更多菜单按钮（ellipsis-horizontal，打开暗色液态玻璃 SettingPopup）
 */
export const TrackActionButtons = memo(({ onShowSetting, onShowMusicAdd }: TrackActionProps) => {
  const musicInfo = usePlayerMusicInfo()
  const t = useI18n()
  const [isLoved, setIsLoved] = useState(false)

  const syncLoveState = useCallback(() => {
    if (!musicInfo.id) {
      setIsLoved(false)
      return
    }
    void getListMusics(LIST_IDS.LOVE).then((list) => {
      setIsLoved(list.some(m => m.id == musicInfo.id))
    })
  }, [musicInfo.id])

  useEffect(() => {
    syncLoveState()
    const handleUpdate = (ids: string[]) => {
      if (ids.includes(LIST_IDS.LOVE)) syncLoveState()
    }
    global.app_event.on('myListMusicUpdate', handleUpdate)
    return () => {
      global.app_event.off('myListMusicUpdate', handleUpdate)
    }
  }, [syncLoveState])

  const handleToggleLove = useCallback(() => {
    const rawInfo = playerState.playMusicInfo.musicInfo
    if (!rawInfo) return
    const targetInfo = 'progress' in rawInfo ? rawInfo.metadata.musicInfo : rawInfo
    if (isLoved) {
      void removeListMusics(LIST_IDS.LOVE, [targetInfo.id])
      setIsLoved(false)
    } else {
      void addListMusics(LIST_IDS.LOVE, [targetInfo], settingState.setting['list.addMusicLocationType'])
      setIsLoved(true)
    }
  }, [isLoved])

  return (
    <View style={styles.actions}>
      <GlassCircleButton
        icon={isLoved ? 'heart' : 'heart-outline'}
        iconSize={17}
        size={34}
        onDark
        active={isLoved}
        onPress={handleToggleLove}
        onLongPress={onShowMusicAdd}
        accessibilityLabel={t('list_name_love')}
      />
      <GlassCircleButton
        icon="ellipsis-horizontal"
        iconSize={17}
        size={34}
        onDark
        onPress={onShowSetting}
        accessibilityLabel={t('play_detail_setting_title')}
      />
    </View>
  )
})

/** 封面主控视图歌曲信息行：左侧 21pt Bold 纯白歌名 + 16.5pt 歌手名，右侧双玻璃圆按钮 */
export default memo(({ onShowSetting, onShowMusicAdd }: TrackActionProps) => {
  const musicInfo = usePlayerMusicInfo()
  const t = useI18n()
  const title = musicInfo.name || t('apple_player_not_playing')
  const singer = musicInfo.singer

  const handleInspectTrack = useCallback(() => {
    if (!musicInfo.name) return
    const details = [musicInfo.name, musicInfo.singer, musicInfo.album].filter(Boolean).join(' · ')
    toast(details)
  }, [musicInfo.album, musicInfo.name, musicInfo.singer])

  return (
    <View style={styles.container}>
      <TouchableOpacity
        style={styles.texts}
        activeOpacity={0.78}
        onPress={handleInspectTrack}
        accessibilityRole="button"
      >
        <Text numberOfLines={1} size={21} color="#FFFFFF" style={styles.title}>{title}</Text>
        {singer ? (
          <Text numberOfLines={1} size={16.5} color="rgba(255, 255, 255, 0.68)" style={styles.singer}>
            {singer}
          </Text>
        ) : null}
      </TouchableOpacity>
      <TrackActionButtons onShowSetting={onShowSetting} onShowMusicAdd={onShowMusicAdd} />
    </View>
  )
})

/** 歌词 / 待播清单顶部紧凑头图条：48×48 圆角 8dp 封面（点击一键回封面视图）+ 歌名歌手 + 双玻璃圆按钮 */
export const MiniTrackHeader = memo(({
  onBackToCover,
  onShowSetting,
  onShowMusicAdd,
}: TrackActionProps & { onBackToCover: () => void }) => {
  const musicInfo = usePlayerMusicInfo()
  const t = useI18n()

  return (
    <View style={styles.miniHeader}>
      <TouchableOpacity
        style={styles.miniMain}
        activeOpacity={0.78}
        onPress={onBackToCover}
        accessibilityRole="button"
      >
        <CoverArt
          uri={musicInfo.pic}
          size={48}
          borderRadius={8}
          style={styles.miniCover}
        />
        <View style={styles.miniTexts}>
          <Text numberOfLines={1} size={16} color="#FFFFFF" style={styles.miniTitle}>
            {musicInfo.name || t('apple_player_not_playing')}
          </Text>
          {musicInfo.singer ? (
            <Text numberOfLines={1} size={14} color="rgba(255, 255, 255, 0.68)" style={styles.miniSinger}>
              {musicInfo.singer}
            </Text>
          ) : null}
        </View>
      </TouchableOpacity>
      <TrackActionButtons onShowSetting={onShowSetting} onShowMusicAdd={onShowMusicAdd} />
    </View>
  )
})

const styles = createStyle({
  container: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: spacing.sm,
  },
  texts: {
    flex: 1,
    paddingRight: spacing.md,
  },
  title: {
    fontWeight: fontWeight.bold,
    letterSpacing: -0.3,
  },
  singer: {
    marginTop: 2,
    fontWeight: fontWeight.medium,
  },
  actions: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
    flexShrink: 0,
  },
  miniHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: pagePadding + 4,
    paddingTop: spacing.xs,
    paddingBottom: spacing.md,
    gap: 12,
  },
  miniMain: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
    minWidth: 0,
  },
  miniCover: {
    backgroundColor: 'rgba(255, 255, 255, 0.08)',
  },
  miniTexts: {
    flex: 1,
    marginLeft: 12,
    minWidth: 0,
  },
  miniTitle: {
    fontWeight: fontWeight.bold,
  },
  miniSinger: {
    marginTop: 2,
    fontWeight: fontWeight.medium,
  },
})
