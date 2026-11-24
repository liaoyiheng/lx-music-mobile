// Modified for Apple Music iOS 26 Liquid Glass UI. Original: lyswhut/lx-music-mobile (Apache-2.0)
import { memo, useCallback } from 'react'
import { TouchableOpacity, View } from 'react-native'
import { CoverArt } from '@/components/apple/LiquidGlass'
import { usePlayerMusicInfo } from '@/store/player/hook'
import { setLoadErrorPicUrl, setMusicInfo } from '@/core/player/playInfo'
import { NAV_SHEAR_NATIVE_IDS } from '@/config/constant'
import { createStyle } from '@/utils/tools'
import { jumpToPlayingPosition, openPlayDetail } from '../utils'

const ARTWORK_SIZE = 38
const ARTWORK_RADIUS = 9

/** 迷你播放条 38×38 圆角封面：自带 0.5px 内描边环与轻投影 */
export default memo(({ isHome }: { isHome: boolean }) => {
  const musicInfo = usePlayerMusicInfo()

  const handlePress = () => {
    if (!musicInfo.id) return
    openPlayDetail()
  }
  const handleLongPress = () => {
    if (!isHome) return
    jumpToPlayingPosition()
  }
  const handleError = useCallback((url: string | number) => {
    setLoadErrorPicUrl(url as string)
    setMusicInfo({
      pic: null,
    })
  }, [])

  return (
    <TouchableOpacity onPress={handlePress} onLongPress={handleLongPress} activeOpacity={0.75}>
      <View style={styles.shadowWrap}>
        <CoverArt
          uri={musicInfo.pic}
          size={ARTWORK_SIZE}
          borderRadius={ARTWORK_RADIUS}
          nativeID={NAV_SHEAR_NATIVE_IDS.playDetail_pic}
          onError={handleError}
        />
      </View>
    </TouchableOpacity>
  )
})

const styles = createStyle({
  shadowWrap: {
    borderRadius: ARTWORK_RADIUS,
    shadowColor: '#000',
    shadowOpacity: 0.20,
    shadowRadius: 6,
    shadowOffset: { width: 0, height: 2 },
    elevation: 3,
  },
})
