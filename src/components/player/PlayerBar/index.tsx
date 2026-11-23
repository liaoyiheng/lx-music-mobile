// Modified for Apple Music style UI. Original: lyswhut/lx-music-mobile (Apache-2.0)
import { memo, useCallback, useState } from 'react'
import { View } from 'react-native'
import { useKeyboard } from '@/utils/hooks'
import { createStyle } from '@/utils/tools'
import { useApple, radius, hairline, size, spacing } from '@/theme/apple'
import { useSettingValue } from '@/store/setting/hook'
import { usePageVisible } from '@/store/common/hook'
import { COMPONENT_IDS } from '@/config/constant'

import Artwork from './components/Artwork'
import Info from './components/Info'
import ControlBtn from './components/ControlBtn'
import MiniProgress from './components/MiniProgress'

/**
 * Apple Music 风格迷你播放条：悬浮圆角卡片 + 圆角封面 + 歌名/歌手（播放时显示歌词）+ 播放暂停/下一首 + 底部细进度线
 * 位于底部标签栏上方（宽屏时位于内容区底部）
 * 交互保持不变：点击封面/歌名打开全屏播放页，长按定位到列表；可按设置拖动进度、键盘弹出时自动隐藏
 */
export default memo(({ isHome = false }: { isHome?: boolean }) => {
  const { keyboardShown } = useKeyboard()
  const apple = useApple()
  const autoHidePlayBar = useSettingValue('common.autoHidePlayBar')
  const [autoUpdate, setAutoUpdate] = useState(true)

  usePageVisible([COMPONENT_IDS.home], useCallback((visible) => {
    if (isHome) setAutoUpdate(visible)
  }, [isHome]))

  if (autoHidePlayBar && keyboardShown) return null

  return (
    <View style={styles.wrapper}>
      <View style={{
        ...styles.card,
        backgroundColor: apple.sheetBackground,
        borderColor: apple.separator,
        shadowColor: apple.mask,
      }}>
        <Artwork isHome={isHome} />
        <Info autoUpdate={autoUpdate} />
        <View style={styles.controls}>
          <ControlBtn />
        </View>
        <MiniProgress autoUpdate={autoUpdate} />
      </View>
    </View>
  )
})

const styles = createStyle({
  wrapper: {
    width: '100%',
    paddingHorizontal: spacing.sm,
    paddingTop: spacing.xs,
    paddingBottom: spacing.sm,
  },
  card: {
    width: '100%',
    height: size.miniPlayer,
    paddingLeft: (size.miniPlayer - size.songArtwork) / 2,
    paddingRight: spacing.xs,
    borderRadius: radius.alert,
    borderWidth: hairline,
    flexDirection: 'row',
    alignItems: 'center',
    overflow: 'hidden',
    elevation: 8,
    shadowOpacity: 1,
    shadowRadius: 12,
    shadowOffset: { width: 0, height: 4 },
  },
  controls: {
    flexDirection: 'row',
    alignItems: 'center',
    flexGrow: 0,
    flexShrink: 0,
  },
})
