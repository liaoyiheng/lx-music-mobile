// Added for Apple Music style UI (based on lyswhut/lx-music-mobile, Apache-2.0)
import { memo, useEffect, useRef, useState } from 'react'
import { Animated, View } from 'react-native'
import Image from '@/components/common/Image'
import { IonIcon } from '@/components/common/Icon'
import { usePlayerMusicInfo, useIsPlay } from '@/store/player/hook'
import { NAV_SHEAR_NATIVE_IDS } from '@/config/constant'
import { useNavigationComponentDidAppear } from '@/navigation'
import commonState from '@/store/common/state'
import { createStyle } from '@/utils/tools'

/** 暂停时封面轻柔微缩比例 */
const PAUSE_SCALE = 0.86
const ARTWORK_RADIUS = 12

/**
 * 大圆角封面：播放时舒展、暂停时轻柔微缩至 0.86
 * 进入动画（共享元素）结束前不切换图片，避免过渡时闪烁
 * 无封面时使用深炭圆角占位 + 居中音符图标
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
      bounciness: 5,
      speed: 10,
    }).start()
  }, [isPlay, scaleAnim])

  return (
    <View style={styles.container}>
      <Animated.View style={{
        ...styles.content,
        elevation: animated && isPlay ? 18 : 6,
        transform: [{ scale: scaleAnim }],
      }}>
        {pic ? (
          <Image
            url={pic}
            nativeID={NAV_SHEAR_NATIVE_IDS.playDetail_pic}
            style={{ width: imageSize, height: imageSize, borderRadius: ARTWORK_RADIUS }}
          />
        ) : (
          <View
            nativeID={NAV_SHEAR_NATIVE_IDS.playDetail_pic}
            style={{
              ...styles.placeholder,
              width: imageSize,
              height: imageSize,
              borderRadius: ARTWORK_RADIUS,
            }}
          >
            <IonIcon name="musical-notes" size={Math.round(imageSize * 0.28)} color="rgba(255, 255, 255, 0.35)" />
          </View>
        )}
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
    borderRadius: ARTWORK_RADIUS,
    shadowColor: '#000',
    shadowOpacity: 0.36,
    shadowRadius: 24,
    shadowOffset: { width: 0, height: 10 },
  },
  placeholder: {
    backgroundColor: 'rgba(255, 255, 255, 0.08)',
    justifyContent: 'center',
    alignItems: 'center',
  },
})
