// Added for Apple Music style UI (based on lyswhut/lx-music-mobile, Apache-2.0)
import { memo } from 'react'
import { View, Image, StyleSheet } from 'react-native'
import { usePlayerMusicInfo } from '@/store/player/hook'

/**
 * 播放页背景：无论系统浅色/深色，永远使用深色高饱和模糊封面背景
 * 有封面时：#16161C 深色衬底 + blurRadius=32 放大 1.35x 避免白边 + rgba(12,12,18,0.48) 暗色遮罩
 * 无封面时：#1C1C22 深炭底 + 柔和深紫氛围光晕
 */
export default memo(() => {
  const musicInfo = usePlayerMusicInfo()
  const pic = musicInfo.pic
  const source = typeof pic == 'number'
    ? pic
    : pic
      ? { uri: pic.startsWith('/') ? 'file://' + pic : pic }
      : null

  return (
    <View style={StyleSheet.absoluteFill} pointerEvents="none">
      <View style={[StyleSheet.absoluteFillObject, { backgroundColor: source ? '#16161C' : '#1C1C22' }]} />
      {source ? (
        <>
          <Image
            source={source}
            blurRadius={32}
            resizeMode="cover"
            style={[StyleSheet.absoluteFillObject, styles.blurImage]}
          />
          <View style={[StyleSheet.absoluteFillObject, styles.mask]} />
        </>
      ) : (
        <View style={styles.fallbackGlow} />
      )}
    </View>
  )
})

const styles = StyleSheet.create({
  blurImage: {
    transform: [{ scale: 1.35 }],
  },
  mask: {
    backgroundColor: 'rgba(12, 12, 18, 0.48)',
  },
  fallbackGlow: {
    position: 'absolute',
    top: '-15%',
    left: '-10%',
    right: '-10%',
    height: '65%',
    borderRadius: 999,
    backgroundColor: 'rgba(88, 62, 138, 0.24)',
  },
})
