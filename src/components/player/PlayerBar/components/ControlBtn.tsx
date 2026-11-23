// Modified for Apple Music style UI. Original: lyswhut/lx-music-mobile (Apache-2.0)
import { memo } from 'react'
import { TouchableOpacity } from 'react-native'
import { IonIcon } from '@/components/common/Icon'
import { useIsPlay } from '@/store/player/hook'
import { useI18n } from '@/lang'
import { useApple } from '@/theme/apple'
import { playNext, playPrev, togglePlay } from '@/core/player/player'
import { createStyle } from '@/utils/tools'
import { useHorizontalMode } from '@/utils/hooks'

const handlePlayPrev = () => {
  void playPrev()
}
const handlePlayNext = () => {
  void playNext()
}

const Btn = ({ icon, label, iconSize, onPress }: {
  icon: string
  label: string
  iconSize: number
  onPress: () => void
}) => {
  const apple = useApple()
  return (
    <TouchableOpacity style={styles.btn} activeOpacity={0.5} onPress={onPress} accessibilityRole="button" accessibilityLabel={label}>
      <IonIcon name={icon} color={apple.label} size={iconSize} />
    </TouchableOpacity>
  )
}

/** 播放 / 暂停（22pt）、下一首（20pt）；宽屏额外显示上一首（20pt） */
export default memo(() => {
  const isWide = useHorizontalMode()
  const isPlay = useIsPlay()
  const t = useI18n()

  return (
    <>
      {isWide ? <Btn icon="play-back" label={t('apple_player_prev')} iconSize={20} onPress={handlePlayPrev} /> : null}
      <Btn icon={isPlay ? 'pause' : 'play'} label={t(isPlay ? 'apple_player_pause' : 'apple_player_play')} iconSize={22} onPress={togglePlay} />
      <Btn icon="play-forward" label={t('apple_player_next')} iconSize={20} onPress={handlePlayNext} />
    </>
  )
})

const styles = createStyle({
  btn: {
    width: 40,
    height: 44,
    justifyContent: 'center',
    alignItems: 'center',
  },
})
