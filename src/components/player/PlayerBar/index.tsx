// Modified for Apple Music style UI. Original: lyswhut/lx-music-mobile (Apache-2.0)
import { memo, useCallback, useState } from 'react'
import { View } from 'react-native'
import { useKeyboard } from '@/utils/hooks'
import { createStyle } from '@/utils/tools'
import { useApple, spacing } from '@/theme/apple'
import { useSettingValue } from '@/store/setting/hook'
import { usePageVisible } from '@/store/common/hook'
import { COMPONENT_IDS } from '@/config/constant'

import Artwork from './components/Artwork'
import Info from './components/Info'
import ControlBtn from './components/ControlBtn'
import MiniProgress from './components/MiniProgress'

const BAR_HEIGHT = 56
const BAR_RADIUS = 14

/**
 * Apple Music 风格迷你播放条：轻盈悬浮圆角胶囊卡片 + 40x40 圆角封面 + 歌名/副标题 + 精致播控 + 底部细进度线
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
        backgroundColor: apple.isDark ? '#1C1C1E' : 'rgba(255, 255, 255, 0.96)',
        borderWidth: apple.isDark ? 1 : 0,
        borderColor: apple.isDark ? 'rgba(255, 255, 255, 0.08)' : 'transparent',
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
    paddingHorizontal: 10,
    paddingTop: spacing.xs,
    paddingBottom: 6,
  },
  card: {
    width: '100%',
    height: BAR_HEIGHT,
    paddingLeft: (BAR_HEIGHT - 40) / 2,
    paddingRight: spacing.xs,
    borderRadius: BAR_RADIUS,
    flexDirection: 'row',
    alignItems: 'center',
    overflow: 'hidden',
    shadowColor: '#000',
    shadowOpacity: 0.12,
    shadowRadius: 12,
    shadowOffset: { width: 0, height: 4 },
    elevation: 6,
  },
  controls: {
    flexDirection: 'row',
    alignItems: 'center',
    flexGrow: 0,
    flexShrink: 0,
  },
})
