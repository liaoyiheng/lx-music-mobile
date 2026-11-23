// Modified for Apple Music style UI. Original: lyswhut/lx-music-mobile (Apache-2.0)
import { memo, useCallback } from 'react'
import { TouchableOpacity, View } from 'react-native'
import Image from '@/components/common/Image'
import { IonIcon } from '@/components/common/Icon'
import { usePlayerMusicInfo } from '@/store/player/hook'
import { NAV_SHEAR_NATIVE_IDS } from '@/config/constant'
import { setLoadErrorPicUrl, setMusicInfo } from '@/core/player/playInfo'
import { useApple } from '@/theme/apple'
import { createStyle } from '@/utils/tools'
import { jumpToPlayingPosition, openPlayDetail } from '../utils'

const ARTWORK_SIZE = 40
const ARTWORK_RADIUS = 7

/** 迷你播放条圆角封面：点击打开播放页，长按（仅首页）定位到列表；无封面时用中性灰底 + 音符图标 */
export default memo(({ isHome }: { isHome: boolean }) => {
  const apple = useApple()
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
      {musicInfo.pic ? (
        <Image url={musicInfo.pic} nativeID={NAV_SHEAR_NATIVE_IDS.playDetail_pic} style={styles.image} onError={handleError} />
      ) : (
        <View
          nativeID={NAV_SHEAR_NATIVE_IDS.playDetail_pic}
          style={{
            ...styles.placeholder,
            backgroundColor: apple.isDark ? '#2C2C2E' : '#E5E5EA',
          }}
        >
          <IonIcon name="musical-notes" size={18} color="#8E8E93" />
        </View>
      )}
    </TouchableOpacity>
  )
})

const styles = createStyle({
  image: {
    width: ARTWORK_SIZE,
    height: ARTWORK_SIZE,
    borderRadius: ARTWORK_RADIUS,
  },
  placeholder: {
    width: ARTWORK_SIZE,
    height: ARTWORK_SIZE,
    borderRadius: ARTWORK_RADIUS,
    alignItems: 'center',
    justifyContent: 'center',
  },
})
