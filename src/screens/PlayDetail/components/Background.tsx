// Modified for Apple Music iOS 26 Liquid Glass UI. Original: lyswhut/lx-music-mobile (Apache-2.0)
import { memo, useEffect, useRef } from 'react'
import { Animated, Easing, Image, StyleSheet, View } from 'react-native'
import { usePlayerMusicInfo } from '@/store/player/hook'

/**
 * 动态封面流光 Mesh 背景：
 * - 铺满整个全屏播放页（延伸到状态栏之下）
 * - 有封面时叠加 3 层高模糊度（65 / 85 / 75）、放大至 1.8x–2.2x、偏移不同象限且带缓慢平滑呼吸漂移的封面图层
 * - 叠加深色沉浸暗角遮罩 rgba(10, 12, 18, 0.44)，确保白色前景文字与玻璃控件始终清晰
 * - 无封面时呈现深邃炭蓝黑渐变质感
 */
export default memo(() => {
  const musicInfo = usePlayerMusicInfo()
  const pic = musicInfo.pic
  const source = typeof pic == 'number'
    ? pic
    : pic
      ? { uri: pic.startsWith('/') ? 'file://' + pic : pic }
      : null

  const driftA = useRef(new Animated.Value(0)).current
  const driftB = useRef(new Animated.Value(0)).current
  const driftC = useRef(new Animated.Value(0)).current

  useEffect(() => {
    const loopA = Animated.loop(
      Animated.sequence([
        Animated.timing(driftA, {
          toValue: 1,
          duration: 14000,
          easing: Easing.inOut(Easing.sin),
          useNativeDriver: true,
        }),
        Animated.timing(driftA, {
          toValue: 0,
          duration: 14000,
          easing: Easing.inOut(Easing.sin),
          useNativeDriver: true,
        }),
      ]),
    )
    const loopB = Animated.loop(
      Animated.sequence([
        Animated.timing(driftB, {
          toValue: 1,
          duration: 18000,
          easing: Easing.inOut(Easing.sin),
          useNativeDriver: true,
        }),
        Animated.timing(driftB, {
          toValue: 0,
          duration: 18000,
          easing: Easing.inOut(Easing.sin),
          useNativeDriver: true,
        }),
      ]),
    )
    const loopC = Animated.loop(
      Animated.sequence([
        Animated.timing(driftC, {
          toValue: 1,
          duration: 21000,
          easing: Easing.inOut(Easing.sin),
          useNativeDriver: true,
        }),
        Animated.timing(driftC, {
          toValue: 0,
          duration: 21000,
          easing: Easing.inOut(Easing.sin),
          useNativeDriver: true,
        }),
      ]),
    )

    loopA.start()
    loopB.start()
    loopC.start()
    return () => {
      loopA.stop()
      loopB.stop()
      loopC.stop()
    }
  }, [driftA, driftB, driftC])

  const layerATransform = {
    transform: [
      { translateX: driftA.interpolate({ inputRange: [0, 1], outputRange: [-24, 20] }) },
      { translateY: driftA.interpolate({ inputRange: [0, 1], outputRange: [-18, 22] }) },
      { scale: driftA.interpolate({ inputRange: [0, 1], outputRange: [1.82, 1.98] }) },
    ],
  }

  const layerBTransform = {
    transform: [
      { translateX: driftB.interpolate({ inputRange: [0, 1], outputRange: [26, -22] }) },
      { translateY: driftB.interpolate({ inputRange: [0, 1], outputRange: [22, -18] }) },
      { scale: driftB.interpolate({ inputRange: [0, 1], outputRange: [2.04, 1.90] }) },
    ],
  }

  const layerCTransform = {
    transform: [
      { translateX: driftC.interpolate({ inputRange: [0, 1], outputRange: [14, -26] }) },
      { translateY: driftC.interpolate({ inputRange: [0, 1], outputRange: [-24, 16] }) },
      { scale: driftC.interpolate({ inputRange: [0, 1], outputRange: [2.15, 2.24] }) },
    ],
  }

  return (
    <View style={StyleSheet.absoluteFill} pointerEvents="none">
      <View style={[StyleSheet.absoluteFillObject, styles.baseBg]} />
      {source ? (
        <>
          <Animated.View style={[styles.meshLayerA, layerATransform]}>
            <Image
              source={source}
              blurRadius={65}
              resizeMode="cover"
              style={StyleSheet.absoluteFillObject}
            />
          </Animated.View>
          <Animated.View style={[styles.meshLayerB, layerBTransform]}>
            <Image
              source={source}
              blurRadius={85}
              resizeMode="cover"
              style={StyleSheet.absoluteFillObject}
            />
          </Animated.View>
          <Animated.View style={[styles.meshLayerC, layerCTransform]}>
            <Image
              source={source}
              blurRadius={75}
              resizeMode="cover"
              style={StyleSheet.absoluteFillObject}
            />
          </Animated.View>
        </>
      ) : (
        <>
          <Animated.View style={[styles.fallbackBlobPrimary, layerATransform]} />
          <Animated.View style={[styles.fallbackBlobSecondary, layerBTransform]} />
          <Animated.View style={[styles.fallbackBlobAccent, layerCTransform]} />
        </>
      )}
      <View style={[StyleSheet.absoluteFillObject, styles.immersiveMask]} />
      <View pointerEvents="none" style={styles.topVignette} />
      <View pointerEvents="none" style={styles.bottomVignette} />
    </View>
  )
})

const styles = StyleSheet.create({
  baseBg: {
    backgroundColor: '#0B0C12',
  },
  meshLayerA: {
    position: 'absolute',
    top: '-18%',
    left: '-18%',
    width: '120%',
    height: '110%',
    opacity: 0.92,
  },
  meshLayerB: {
    position: 'absolute',
    bottom: '-22%',
    right: '-18%',
    width: '122%',
    height: '112%',
    opacity: 0.72,
  },
  meshLayerC: {
    position: 'absolute',
    top: '-8%',
    right: '-24%',
    width: '115%',
    height: '115%',
    opacity: 0.52,
  },
  fallbackBlobPrimary: {
    position: 'absolute',
    top: '-18%',
    left: '-22%',
    width: '115%',
    height: '72%',
    borderRadius: 999,
    backgroundColor: 'rgba(38, 52, 108, 0.44)',
  },
  fallbackBlobSecondary: {
    position: 'absolute',
    bottom: '-18%',
    right: '-20%',
    width: '110%',
    height: '68%',
    borderRadius: 999,
    backgroundColor: 'rgba(78, 30, 68, 0.34)',
  },
  fallbackBlobAccent: {
    position: 'absolute',
    top: '22%',
    left: '10%',
    width: '88%',
    height: '58%',
    borderRadius: 999,
    backgroundColor: 'rgba(20, 38, 66, 0.38)',
  },
  immersiveMask: {
    backgroundColor: 'rgba(10, 12, 18, 0.44)',
  },
  topVignette: {
    position: 'absolute',
    top: 0,
    left: 0,
    right: 0,
    height: '22%',
    backgroundColor: 'rgba(0, 0, 0, 0.14)',
  },
  bottomVignette: {
    position: 'absolute',
    left: 0,
    right: 0,
    bottom: 0,
    height: '32%',
    backgroundColor: 'rgba(0, 0, 0, 0.24)',
  },
})
