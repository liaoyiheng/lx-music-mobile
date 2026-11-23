// Added for Apple Music style UI (based on lyswhut/lx-music-mobile, Apache-2.0)
import { memo } from 'react'
import { View, Image, StyleSheet } from 'react-native'
import { usePlayerMusicInfo } from '@/store/player/hook'
import { useApple } from '@/theme/apple'

/** 播放页背景：当前封面高斯模糊铺满 + 半透明遮罩（深色/浅色自适应，保证文字可读） */
export default memo(() => {
  const musicInfo = usePlayerMusicInfo()
  const apple = useApple()
  const pic = musicInfo.pic
  const source = typeof pic == 'number'
    ? pic
    : pic
      ? { uri: pic.startsWith('/') ? 'file://' + pic : pic }
      : null

  return (
    <View style={StyleSheet.absoluteFill} pointerEvents="none">
      <View style={{ ...StyleSheet.absoluteFillObject, backgroundColor: apple.background }} />
      {source ? <Image source={source} blurRadius={60} resizeMode="cover" style={{ ...StyleSheet.absoluteFillObject, opacity: 0.9 }} /> : null}
      <View style={{ ...StyleSheet.absoluteFillObject, backgroundColor: apple.isDark ? 'rgba(0,0,0,0.5)' : 'rgba(255,255,255,0.55)' }} />
    </View>
  )
})
