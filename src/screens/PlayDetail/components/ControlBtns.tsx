// Added for Apple Music style UI (based on lyswhut/lx-music-mobile, Apache-2.0)
import { memo } from 'react'
import { TouchableOpacity, View } from 'react-native'
import { IonIcon } from '@/components/common/Icon'
import { useIsPlay } from '@/store/player/hook'
import { playNext, playPrev, togglePlay } from '@/core/player/player'
import { useI18n } from '@/lang'
import { spacing } from '@/theme/apple'
import { createStyle } from '@/utils/tools'

const SIDE_BTN = 64
const MAIN_BTN = 80

const handlePlayPrev = () => {
  void playPrev()
}
const handlePlayNext = () => {
  void playNext()
}

const Btn = ({ icon, label, btnSize, iconSize, onPress }: {
  icon: string
  label: string
  btnSize: number
  iconSize: number
  onPress: () => void
}) => {
  return (
    <TouchableOpacity style={{ ...styles.btn, width: btnSize, height: btnSize }} activeOpacity={0.5} onPress={onPress} accessibilityRole="button" accessibilityLabel={label}>
      <IonIcon name={icon} color="#FFFFFF" rawSize={iconSize} />
    </TouchableOpacity>
  )
}

/** 上一首（34）/ 播放暂停（44）/ 下一首（34），纯白饱满 */
export default memo(() => {
  const isPlay = useIsPlay()
  const t = useI18n()

  return (
    <View style={styles.container}>
      <Btn icon="play-back" label={t('apple_player_prev')} btnSize={SIDE_BTN} iconSize={34} onPress={handlePlayPrev} />
      <Btn icon={isPlay ? 'pause' : 'play'} label={t(isPlay ? 'apple_player_pause' : 'apple_player_play')} btnSize={MAIN_BTN} iconSize={44} onPress={togglePlay} />
      <Btn icon="play-forward" label={t('apple_player_next')} btnSize={SIDE_BTN} iconSize={34} onPress={handlePlayNext} />
    </View>
  )
})

const styles = createStyle({
  container: {
    flexDirection: 'row',
    justifyContent: 'space-evenly',
    alignItems: 'center',
    paddingVertical: spacing.md,
  },
  btn: {
    justifyContent: 'center',
    alignItems: 'center',
  },
})
