// Modified for Apple Music style UI. Original: lyswhut/lx-music-mobile (Apache-2.0)
import { memo } from 'react'
import { TouchableOpacity } from 'react-native'
import Text from '@/components/common/Text'
import { useLrcPlay } from '@/plugins/lyric'
import { useIsPlay, usePlayerMusicInfo, useStatusText } from '@/store/player/hook'
import { useI18n } from '@/lang'
import { useApple, fontSize, fontWeight, spacing } from '@/theme/apple'
import { createStyle } from '@/utils/tools'
import { jumpToPlayingPosition, openPlayDetail } from '../utils'

/**
 * 歌名 + 副标题
 * 未播放时显示柔和灰色「未在播放」；播放时副标题优先显示状态，其次歌词或歌手
 */
export default memo(({ autoUpdate }: { autoUpdate: boolean }) => {
  const apple = useApple()
  const t = useI18n()
  const musicInfo = usePlayerMusicInfo()
  const { text: lyricText } = useLrcPlay(autoUpdate)
  const statusText = useStatusText()
  const isPlay = useIsPlay()

  const handlePress = () => {
    if (!musicInfo.id) return
    openPlayDetail()
  }

  const hasTrack = !!musicInfo.id
  const subtitle = hasTrack
    ? statusText || (isPlay && lyricText ? lyricText : musicInfo.singer)
    : ''

  return (
    <TouchableOpacity style={styles.container} onPress={handlePress} onLongPress={jumpToPlayingPosition} activeOpacity={0.7}>
      <Text
        numberOfLines={1}
        size={fontSize.subhead}
        color={hasTrack ? apple.label : apple.secondaryLabel}
        style={hasTrack ? styles.title : styles.idleTitle}
      >
        {hasTrack ? musicInfo.name : t('apple_player_not_playing')}
      </Text>
      {subtitle
        ? <Text numberOfLines={1} size={fontSize.caption1} color={apple.secondaryLabel} style={styles.subtitle}>{subtitle}</Text>
        : null}
    </TouchableOpacity>
  )
})

const styles = createStyle({
  container: {
    flex: 1,
    height: '100%',
    justifyContent: 'center',
    paddingHorizontal: spacing.md,
  },
  title: {
    fontWeight: fontWeight.semibold,
  },
  idleTitle: {
    fontWeight: fontWeight.medium,
  },
  subtitle: {
    marginTop: spacing.xxs,
  },
})
