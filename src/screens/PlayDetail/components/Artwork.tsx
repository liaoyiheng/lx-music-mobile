// Modified for Apple Music iOS 26 Liquid Glass UI. Original: lyswhut/lx-music-mobile (Apache-2.0)
import { memo, useEffect, useRef, useState } from 'react'
import { Animated, TouchableOpacity, View } from 'react-native'
import { CoverArt } from '@/components/apple/LiquidGlass'
import { NAV_SHEAR_NATIVE_IDS } from '@/config/constant'
import { togglePlay } from '@/core/player/player'
import { useI18n } from '@/lang'
import { useNavigationComponentDidAppear } from '@/navigation'
import commonState from '@/store/common/state'
import { useIsPlay, usePlayerMusicInfo } from '@/store/player/hook'
import { createStyle } from '@/utils/tools'

/** 暂停时封面平滑微缩比例 */
const PAUSE_SCALE = 0.84
const ARTWORK_RADIUS = 14

/**
 * 大圆角封面主控：
 * - 14dp 圆角 CoverArt（自带 0.5px 内描边环）
 * - 播放时 scale(1) + 深邃呼吸投影，暂停时弹簧缩放到 scale(0.84)
 * - 点击封面可切换播放/暂停
 * - 进入过渡动画结束前锁定图片源，避免共享元素闪烁
 */
export default memo(({ componentId, imageSize }: { componentId: string, imageSize: number }) => {
  const musicInfo = usePlayerMusicInfo()
  const isPlay = useIsPlay()
  const t = useI18n()
  const [animated, setAnimated] = useState(!!commonState.componentIds.playDetail)
  const [pic, setPic] = useState(musicInfo.pic)
  const scaleAnim = useRef(new Animated.Value(isPlay ? 1 : PAUSE_SCALE)).current

  useEffect(() => {
    if (animated) setPic(musicInfo.pic)
  }, [musicInfo.pic, animated])

  useNavigationComponentDidAppear(componentId, () => {
    setAnimated(true)
  })

  useEffect(() => {
    Animated.spring(scaleAnim, {
      toValue: isPlay ? 1 : PAUSE_SCALE,
      useNativeDriver: true,
      bounciness: 5,
      speed: 11,
    }).start()
  }, [isPlay, scaleAnim])

  return (
    <View style={styles.container}>
      <Animated.View
        style={[
          styles.content,
          {
            elevation: animated && isPlay ? 20 : 7,
            shadowOpacity: animated && isPlay ? 0.48 : 0.28,
            shadowRadius: animated && isPlay ? 28 : 14,
            transform: [{ scale: scaleAnim }],
          },
        ]}
      >
        <TouchableOpacity
          activeOpacity={0.92}
          onPress={togglePlay}
          accessibilityRole="button"
          accessibilityLabel={t(isPlay ? 'apple_player_pause' : 'apple_player_play')}
        >
          <CoverArt
            uri={pic}
            size={imageSize}
            borderRadius={ARTWORK_RADIUS}
            nativeID={NAV_SHEAR_NATIVE_IDS.playDetail_pic}
            style={styles.coverBg}
          />
        </TouchableOpacity>
      </Animated.View>
    </View>
  )
})

const styles = createStyle({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  content: {
    backgroundColor: 'rgba(0, 0, 0, 0)',
    borderRadius: ARTWORK_RADIUS,
    shadowColor: '#000000',
    shadowOffset: { width: 0, height: 14 },
  },
  coverBg: {
    backgroundColor: 'rgba(255, 255, 255, 0.08)',
  },
})
