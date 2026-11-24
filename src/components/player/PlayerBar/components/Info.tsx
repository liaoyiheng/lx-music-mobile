// Modified for Apple Music iOS 26 Liquid Glass UI. Original: lyswhut/lx-music-mobile (Apache-2.0)
import { memo } from 'react'
import { TouchableOpacity } from 'react-native'
import Text from '@/components/common/Text'
import { useLrcPlay } from '@/plugins/lyric'
import { useIsPlay, usePlayerMusicInfo, useStatusText } from '@/store/player/hook'
import { useI18n } from '@/lang'
import { useApple, fontWeight } from '@/theme/apple'
import { createStyle } from '@/utils/tools'
import { jumpToPlayingPosition, openPlayDetail } from '../utils'

/**
 * 悬浮药丸播放条歌名 + 副标题（14.5pt 粗体歌名 + 12pt 副标题）
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
    <TouchableOpacity style={styles.container} onPress={handlePress} onLongPress={jumpToPlayingPosition} activeOpacity={0.72}>
      <Text
        numberOfLines={1}
        size={14.5}
        color={hasTrack ? apple.label : apple.secondaryLabel}
        style={hasTrack ? styles.title : styles.idleTitle}
      >
        {hasTrack ? musicInfo.name : t('apple_player_not_playing')}
      </Text>
      {subtitle
        ? <Text numberOfLines={1} size={12} color={apple.secondaryLabel} style={styles.subtitle}>{subtitle}</Text>
        : null}
    </TouchableOpacity>
  )
})

const styles = createStyle({
  container: {
    flex: 1,
    height: '100%',
    justifyContent: 'center',
    paddingHorizontal: 11,
  },
  title: {
    fontWeight: fontWeight.semibold,
    letterSpacing: -0.15,
  },
  idleTitle: {
    fontWeight: fontWeight.medium,
  },
  subtitle: {
    marginTop: 1,
  },
})
