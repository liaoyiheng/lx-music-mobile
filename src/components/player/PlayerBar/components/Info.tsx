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
 * 副标题优先显示播放状态（加载中、出错等），播放时显示当前歌词，否则显示歌手
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

  const subtitle = musicInfo.id
    ? statusText || (isPlay && lyricText ? lyricText : musicInfo.singer)
    : ''

  return (
    <TouchableOpacity style={styles.container} onPress={handlePress} onLongPress={jumpToPlayingPosition} activeOpacity={0.7}>
      <Text numberOfLines={1} size={fontSize.subhead} color={apple.label} style={styles.title}>
        {musicInfo.id ? musicInfo.name : t('apple_player_not_playing')}
      </Text>
      {subtitle
        ? <Text numberOfLines={1} size={fontSize.footnote} color={apple.secondaryLabel} style={styles.subtitle}>{subtitle}</Text>
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
  subtitle: {
    marginTop: spacing.xxs,
  },
})
