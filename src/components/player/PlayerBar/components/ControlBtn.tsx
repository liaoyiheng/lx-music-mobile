// Modified for Apple Music style UI. Original: lyswhut/lx-music-mobile (Apache-2.0)
import { memo } from 'react'
import { TouchableOpacity } from 'react-native'
import { Icon } from '@/components/common/Icon'
import { useIsPlay } from '@/store/player/hook'
import { useI18n } from '@/lang'
import { useApple, size } from '@/theme/apple'
import { playNext, playPrev, togglePlay } from '@/core/player/player'
import { createStyle } from '@/utils/tools'
import { useHorizontalMode } from '@/utils/hooks'

const ICON_SIZE = 24

const handlePlayPrev = () => {
  void playPrev()
}
const handlePlayNext = () => {
  void playNext()
}

const Btn = ({ icon, label, onPress }: {
  icon: string
  label: string
  onPress: () => void
}) => {
  const apple = useApple()
  return (
    <TouchableOpacity style={styles.btn} activeOpacity={0.5} onPress={onPress} accessibilityRole="button" accessibilityLabel={label}>
      <Icon name={icon} color={apple.label} size={ICON_SIZE} />
    </TouchableOpacity>
  )
}

/** 播放 / 暂停、下一首；宽屏额外显示上一首 */
export default memo(() => {
  const isWide = useHorizontalMode()
  const isPlay = useIsPlay()
  const t = useI18n()

  return (
    <>
      {isWide ? <Btn icon="prevMusic" label={t('apple_player_prev')} onPress={handlePlayPrev} /> : null}
      <Btn icon={isPlay ? 'pause' : 'play'} label={t(isPlay ? 'apple_player_pause' : 'apple_player_play')} onPress={togglePlay} />
      <Btn icon="nextMusic" label={t('apple_player_next')} onPress={handlePlayNext} />
    </>
  )
})

const styles = createStyle({
  btn: {
    width: size.hitTarget,
    height: size.hitTarget,
    justifyContent: 'center',
    alignItems: 'center',
  },
})
