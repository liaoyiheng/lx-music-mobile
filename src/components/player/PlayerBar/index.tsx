// Modified for Apple Music iOS 26 Liquid Glass UI. Original: lyswhut/lx-music-mobile (Apache-2.0)
import { memo, useCallback, useState } from 'react'
import { View } from 'react-native'
import { GlassSurface } from '@/components/apple/LiquidGlass'
import { useKeyboard } from '@/utils/hooks'
import { createStyle } from '@/utils/tools'
import { radius, size } from '@/theme/apple'
import { useSettingValue } from '@/store/setting/hook'
import { usePageVisible } from '@/store/common/hook'
import { usePlayerMusicInfo } from '@/store/player/hook'
import { COMPONENT_IDS } from '@/config/constant'

import Artwork from './components/Artwork'
import Info from './components/Info'
import ControlBtn from './components/ControlBtn'
import MiniProgress from './components/MiniProgress'

/**
 * Apple Music iOS 26 悬浮液态玻璃药丸迷你播放条：
 * 54dp 高、27dp 全圆角药丸 + 封面透色弥散玻璃 + 38×38 圆角封面 + 歌名/副标题 + 精致播控 + 底部细进度线
 */
export default memo(({ isHome = false }: { isHome?: boolean }) => {
  const { keyboardShown } = useKeyboard()
  const autoHidePlayBar = useSettingValue('common.autoHidePlayBar')
  const musicInfo = usePlayerMusicInfo()
  const [autoUpdate, setAutoUpdate] = useState(true)

  usePageVisible([COMPONENT_IDS.home], useCallback((visible) => {
    if (isHome) setAutoUpdate(visible)
  }, [isHome]))

  if (autoHidePlayBar && keyboardShown) return null

  const backdropUri = typeof musicInfo.pic === 'string' && musicInfo.pic ? musicInfo.pic : null

  return (
    <GlassSurface
      borderRadius={radius.miniPlayer}
      backdropUri={backdropUri}
      innerStyle={styles.card}
    >
      <Artwork isHome={isHome} />
      <Info autoUpdate={autoUpdate} />
      <View style={styles.controls}>
        <ControlBtn />
      </View>
      <MiniProgress autoUpdate={autoUpdate} />
    </GlassSurface>
  )
})

const styles = createStyle({
  card: {
    width: '100%',
    height: size.miniPlayer,
    paddingLeft: 8,
    paddingRight: 10,
    flexDirection: 'row',
    alignItems: 'center',
    position: 'relative',
  },
  controls: {
    flexDirection: 'row',
    alignItems: 'center',
    flexGrow: 0,
    flexShrink: 0,
  },
})
