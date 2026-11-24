// Modified for Apple Music iOS 26 Liquid Glass UI. Original: lyswhut/lx-music-mobile (Apache-2.0)
import { memo, useCallback, useRef } from 'react'
import { Animated, TouchableWithoutFeedback, View } from 'react-native'
import { IonIcon } from '@/components/common/Icon'
import { playNext, playPrev, togglePlay } from '@/core/player/player'
import { useI18n } from '@/lang'
import { useIsPlay } from '@/store/player/hook'
import { spacing } from '@/theme/apple'
import { createStyle } from '@/utils/tools'

const SIDE_BTN = 64
const MAIN_BTN = 76

const handlePlayPrev = () => {
  void playPrev()
}
const handlePlayNext = () => {
  void playNext()
}

const TransportBtn = memo(({ icon, label, btnSize, iconSize, onPress }: {
  icon: string
  label: string
  btnSize: number
  iconSize: number
  onPress: () => void
}) => {
  const scaleAnim = useRef(new Animated.Value(1)).current

  const handlePressIn = useCallback(() => {
    Animated.spring(scaleAnim, {
      toValue: 0.86,
      useNativeDriver: true,
      speed: 36,
      bounciness: 4,
    }).start()
  }, [scaleAnim])

  const handlePressOut = useCallback(() => {
    Animated.spring(scaleAnim, {
      toValue: 1,
      useNativeDriver: true,
      speed: 22,
      bounciness: 8,
    }).start()
  }, [scaleAnim])

  return (
    <TouchableWithoutFeedback
      onPress={onPress}
      onPressIn={handlePressIn}
      onPressOut={handlePressOut}
      accessibilityRole="button"
      accessibilityLabel={label}
    >
      <Animated.View
        style={[
          styles.btn,
          {
            width: btnSize,
            height: btnSize,
            borderRadius: btnSize / 2,
            transform: [{ scale: scaleAnim }],
          },
        ]}
      >
        <IonIcon name={icon} color="#FFFFFF" rawSize={iconSize} />
      </Animated.View>
    </TouchableWithoutFeedback>
  )
})

/** 极简三键播控：上一首（34pt）/ 播放暂停（48pt）/ 下一首（34pt），带柔和缩放反馈 */
export default memo(() => {
  const isPlay = useIsPlay()
  const t = useI18n()

  return (
    <View style={styles.container}>
      <TransportBtn
        icon="play-back"
        label={t('apple_player_prev')}
        btnSize={SIDE_BTN}
        iconSize={34}
        onPress={handlePlayPrev}
      />
      <TransportBtn
        icon={isPlay ? 'pause' : 'play'}
        label={t(isPlay ? 'apple_player_pause' : 'apple_player_play')}
        btnSize={MAIN_BTN}
        iconSize={48}
        onPress={togglePlay}
      />
      <TransportBtn
        icon="play-forward"
        label={t('apple_player_next')}
        btnSize={SIDE_BTN}
        iconSize={34}
        onPress={handlePlayNext}
      />
    </View>
  )
})

const styles = createStyle({
  container: {
    flexDirection: 'row',
    justifyContent: 'space-evenly',
    alignItems: 'center',
    paddingTop: spacing.md,
    paddingBottom: spacing.sm,
  },
  btn: {
    justifyContent: 'center',
    alignItems: 'center',
  },
})
