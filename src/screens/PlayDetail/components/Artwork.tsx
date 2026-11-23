// Added for Apple Music style UI (based on lyswhut/lx-music-mobile, Apache-2.0)
import { memo, useEffect, useRef, useState } from 'react'
import { Animated, View } from 'react-native'
import Image from '@/components/common/Image'
import { usePlayerMusicInfo, useIsPlay } from '@/store/player/hook'
import { NAV_SHEAR_NATIVE_IDS } from '@/config/constant'
import { useNavigationComponentDidAppear } from '@/navigation'
import commonState from '@/store/common/state'
import { radius } from '@/theme/apple'
import { createStyle } from '@/utils/tools'

/** 暂停时封面缩小的比例（Apple Music 的效果） */
const PAUSE_SCALE = 0.82

/**
 * 大圆角封面：播放时放大、暂停时缩小
 * 进入动画（共享元素）结束前不切换图片，避免过渡时闪烁
 */
export default memo(({ componentId, imageSize }: { componentId: string, imageSize: number }) => {
  const musicInfo = usePlayerMusicInfo()
  const isPlay = useIsPlay()
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
      bounciness: 6,
      speed: 10,
    }).start()
  }, [isPlay, scaleAnim])

  return (
    <View style={styles.container}>
      <Animated.View style={{
        ...styles.content,
        elevation: animated && isPlay ? 18 : 0,
        transform: [{ scale: scaleAnim }],
      }}>
        <Image url={pic} nativeID={NAV_SHEAR_NATIVE_IDS.playDetail_pic} style={{ width: imageSize, height: imageSize, borderRadius: radius.large }} />
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
    backgroundColor: 'rgba(0,0,0,0)',
    borderRadius: radius.large,
    shadowColor: '#000',
    shadowOpacity: 0.3,
    shadowRadius: 20,
  },
})
