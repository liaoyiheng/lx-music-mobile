import { View } from 'react-native'
import StatusBar from '@/components/common/StatusBar'
import PlayerBar from '@/components/player/PlayerBar'
import { BottomDockScrim } from '@/components/apple/LiquidGlass'
import { useNavActiveId } from '@/store/common/hook'
import { useHorizontalMode, useKeyboard } from '@/utils/hooks'
import { useApple, size } from '@/theme/apple'
import { createStyle } from '@/utils/tools'
import KeepAlive from './KeepAlive'
import TabBar from './TabBar'
import Sidebar from './Sidebar'
import { LibraryPage, BrowsePage, SearchPage, SettingPage } from './Pages'
import { getTabId, isBrowseId, rememberBrowseId, getLastBrowseId } from './tabs'

/**
 * Apple Music iOS 26 液态玻璃根布局
 * 竖屏：全屏内容 + 底部渐隐遮罩 + 悬浮液态玻璃 Dock（药丸 MiniPlayer + 胶囊 TabBar）
 * 宽屏 / 横屏：左侧边栏 + 全屏内容 + 悬浮药丸 MiniPlayer
 */
export default () => {
  const apple = useApple()
  const navId = useNavActiveId()
  const isWide = useHorizontalMode()
  const { keyboardShown } = useKeyboard()
  const tabId = getTabId(navId)
  if (isBrowseId(navId)) rememberBrowseId(navId)
  const browseId = getLastBrowseId()

  return (
    <View style={{ ...styles.container, backgroundColor: apple.background }}>
      <StatusBar />
      <View style={isWide ? styles.wide : styles.narrow}>
        {isWide ? <Sidebar /> : null}
        <View style={styles.content}>
          <View style={styles.pages}>
            <KeepAlive active={tabId == 'library'}><LibraryPage /></KeepAlive>
            <KeepAlive active={tabId == 'browse'}><BrowsePage browseId={browseId} isWide={isWide} /></KeepAlive>
            <KeepAlive active={tabId == 'search'}><SearchPage /></KeepAlive>
            <KeepAlive active={tabId == 'setting'}><SettingPage /></KeepAlive>
          </View>

          {!keyboardShown ? <BottomDockScrim height={isWide ? 96 : 148} /> : null}

          <View
            pointerEvents="box-none"
            style={[
              styles.dock,
              isWide && styles.dockWide,
            ]}
          >
            <PlayerBar isHome />
            {isWide || keyboardShown ? null : <TabBar />}
          </View>
        </View>
      </View>
    </View>
  )
}

const styles = createStyle({
  container: {
    flex: 1,
  },
  narrow: {
    flex: 1,
    flexDirection: 'column',
  },
  wide: {
    flex: 1,
    flexDirection: 'row',
  },
  content: {
    flex: 1,
    overflow: 'hidden',
    position: 'relative',
  },
  pages: {
    flex: 1,
  },
  dock: {
    position: 'absolute',
    left: size.dockMarginX,
    right: size.dockMarginX,
    bottom: size.dockMarginBottom,
    gap: size.dockGap,
    zIndex: 35,
  },
  dockWide: {
    left: 24,
    right: 24,
    bottom: 16,
  },
})
