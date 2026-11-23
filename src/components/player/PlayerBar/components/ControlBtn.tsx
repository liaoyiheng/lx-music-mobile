import { TouchableOpacity } from 'react-native'
import { Icon } from '@/components/common/Icon'
import { useIsPlay } from '@/store/player/hook'
import { useApple } from '@/theme/apple'
import { playNext, playPrev, togglePlay } from '@/core/player/player'
import { createStyle } from '@/utils/tools'
import { useHorizontalMode } from '@/utils/hooks'

const BTN_SIZE = 26
const handlePlayPrev = () => {
  void playPrev()
}
const handlePlayNext = () => {
  void playNext()
}

const PlayPrevBtn = () => {
  const apple = useApple()

  return (
    <TouchableOpacity style={styles.cotrolBtn} activeOpacity={0.5} onPress={handlePlayPrev}>
      <Icon name='prevMusic' color={apple.label} size={BTN_SIZE} />
    </TouchableOpacity>
  )
}

const PlayNextBtn = () => {
  const apple = useApple()

  return (
    <TouchableOpacity style={styles.cotrolBtn} activeOpacity={0.5} onPress={handlePlayNext}>
      <Icon name='nextMusic' color={apple.label} size={BTN_SIZE} />
    </TouchableOpacity>
  )
}

const TogglePlayBtn = () => {
  const isPlay = useIsPlay()
  const apple = useApple()

  return (
    <TouchableOpacity style={styles.cotrolBtn} activeOpacity={0.5} onPress={togglePlay}>
      <Icon name={isPlay ? 'pause' : 'play'} color={apple.label} size={BTN_SIZE} />
    </TouchableOpacity>
  )
}

export default () => {
  const isHorizontalMode = useHorizontalMode()
  return (
    <>
      {/* <TouchableOpacity activeOpacity={0.5} onPress={toggleNextPlayMode}>
        <Text style={{ ...styles.cotrolBtn }}>
          <Icon name={playModeIcon} style={{ color: theme.secondary10 }} size={18} />
        </Text>
      </TouchableOpacity>
    */}
      {/* {btnPrev} */}
      { isHorizontalMode ? <PlayPrevBtn /> : null }
      <TogglePlayBtn />
      <PlayNextBtn />
    </>
  )
}


const styles = createStyle({
  cotrolBtn: {
    width: 42,
    height: 42,
    justifyContent: 'center',
    alignItems: 'center',

    // backgroundColor: '#ccc',
    shadowOpacity: 1,
    textShadowRadius: 1,
  },
})
