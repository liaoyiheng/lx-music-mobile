import { useCallback, useEffect, useRef, useState } from 'react'
import { View } from 'react-native'
import MusicList from './MusicList'
import MyList from './MyList'
import { useHorizontalMode } from '@/utils/hooks'
import { useBackHandler } from '@/utils/hooks/useBackHandler'
import commonState from '@/store/common/state'
import { useStatusbarHeight } from '@/store/common/hook'
import { useApple, hairline } from '@/theme/apple'
import { createStyle } from '@/utils/tools'

type Mode = 'lists' | 'songs'

const LISTS_PANE_WIDTH = 320

/**
 * 资料库
 * 竖屏：先显示「我的列表」，点进某个列表后显示歌曲，返回键 / 左上角返回回到列表
 * 宽屏：左边列表、右边歌曲
 *
 * 歌曲页始终保持布局（列表页盖在上面），保证「定位到当前歌曲」能正常滚动
 */
export default () => {
  const apple = useApple()
  const isWide = useHorizontalMode()
  const statusBarHeight = useStatusbarHeight()
  const [mode, setMode] = useState<Mode>(global.lx.jumpMyListPosition ? 'songs' : 'lists')
  const modeRef = useRef(mode)
  modeRef.current = mode

  useEffect(() => {
    // 沿用原版事件：true = 显示列表，false = 显示歌曲
    const changeVisible = (visible: boolean) => {
      setMode(visible ? 'lists' : 'songs')
    }
    const handleJump = () => {
      setMode('songs')
    }
    global.app_event.on('changeLoveListVisible', changeVisible)
    global.app_event.on('jumpListPosition', handleJump)
    return () => {
      global.app_event.off('changeLoveListVisible', changeVisible)
      global.app_event.off('jumpListPosition', handleJump)
    }
  }, [])

  useBackHandler(useCallback(() => {
    if (isWide || modeRef.current != 'songs') return false
    if (Object.keys(commonState.componentIds).length != 1 || commonState.navActiveId != 'nav_love') return false
    setMode('lists')
    return true
  }, [isWide]))

  if (isWide) {
    return (
      <View style={styles.row}>
        <View style={{ ...styles.listsPane, borderRightColor: apple.separator, backgroundColor: apple.background }}>
          <MyList />
        </View>
        <View style={{ ...styles.songsPane, paddingTop: statusBarHeight }}>
          <MusicList />
        </View>
      </View>
    )
  }

  return (
    <View style={styles.container}>
      <View style={{ ...styles.container, paddingTop: statusBarHeight }}>
        <MusicList />
      </View>
      <View
        style={mode == 'lists' ? { ...styles.overlay, backgroundColor: apple.background } : styles.hidden}
        pointerEvents={mode == 'lists' ? 'auto' : 'none'}
      >
        <MyList />
      </View>
    </View>
  )
}

const styles = createStyle({
  container: {
    flex: 1,
  },
  row: {
    flex: 1,
    flexDirection: 'row',
  },
  listsPane: {
    width: LISTS_PANE_WIDTH,
    borderRightWidth: hairline,
  },
  songsPane: {
    flex: 1,
  },
  overlay: {
    position: 'absolute',
    left: 0,
    top: 0,
    right: 0,
    bottom: 0,
    zIndex: 5,
  },
  hidden: {
    display: 'none',
  },
})
