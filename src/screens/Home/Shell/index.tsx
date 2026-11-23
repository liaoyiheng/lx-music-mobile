import { View } from 'react-native'
import StatusBar from '@/components/common/StatusBar'
import PlayerBar from '@/components/player/PlayerBar'
import { useNavActiveId } from '@/store/common/hook'
import { useHorizontalMode } from '@/utils/hooks'
import { useApple } from '@/theme/apple'
import { createStyle } from '@/utils/tools'
import KeepAlive from './KeepAlive'
import TabBar from './TabBar'
import Sidebar from './Sidebar'
import { LibraryPage, BrowsePage, SearchPage, SettingPage } from './Pages'
import { getTabId, isBrowseId, rememberBrowseId, getLastBrowseId } from './tabs'

/**
 * Apple Music 风格根布局
 * 竖屏：页面 + 迷你播放条 + 底部标签栏
 * 宽屏 / 横屏：左侧边栏 + 页面 + 迷你播放条
 */
export default () => {
  const apple = useApple()
  const navId = useNavActiveId()
  const isWide = useHorizontalMode()
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
          <PlayerBar isHome />
          {isWide ? null : <TabBar />}
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
  },
  pages: {
    flex: 1,
  },
})
