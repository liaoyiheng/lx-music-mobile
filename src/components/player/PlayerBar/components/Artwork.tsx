// Modified for Apple Music style UI. Original: lyswhut/lx-music-mobile (Apache-2.0)
import { memo, useCallback } from 'react'
import { TouchableOpacity } from 'react-native'
import Image from '@/components/common/Image'
import { usePlayerMusicInfo } from '@/store/player/hook'
import { NAV_SHEAR_NATIVE_IDS } from '@/config/constant'
import { setLoadErrorPicUrl, setMusicInfo } from '@/core/player/playInfo'
import { radius, size } from '@/theme/apple'
import { createStyle } from '@/utils/tools'
import { jumpToPlayingPosition, openPlayDetail } from '../utils'

/** 迷你播放条圆角封面：点击打开播放页，长按（仅首页）定位到列表 */
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
    <TouchableOpacity onPress={handlePress} onLongPress={handleLongPress} activeOpacity={0.7}>
      <Image url={musicInfo.pic} nativeID={NAV_SHEAR_NATIVE_IDS.playDetail_pic} style={styles.image} onError={handleError} />
    </TouchableOpacity>
  )
})

const styles = createStyle({
  image: {
    width: size.songArtwork,
    height: size.songArtwork,
    borderRadius: radius.small,
  },
})
