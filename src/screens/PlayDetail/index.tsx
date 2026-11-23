// Modified for Apple Music style UI. Original: lyswhut/lx-music-mobile (Apache-2.0)
import { memo, useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { Animated, AppState, PanResponder, View, type LayoutChangeEvent } from 'react-native'
import PagerView, { type PagerViewOnPageSelectedEvent } from 'react-native-pager-view'
import { Navigation } from 'react-native-navigation'
import PageContent from '@/components/PageContent'
import StatusBar from '@/components/common/StatusBar'
import { setComponentId } from '@/core/common'
import { COMPONENT_IDS, NAV_SHEAR_NATIVE_IDS } from '@/config/constant'
import { pop } from '@/navigation'
import commonState, { type InitState as CommonState } from '@/store/common/state'
import { useStatusbarHeight } from '@/store/common/hook'
import { useHorizontalMode, useWindowSize } from '@/utils/hooks'
import { screenkeepAwake, screenUnkeepAwake } from '@/utils/nativeModules/utils'
import { useApple, radius, spacing, pagePadding } from '@/theme/apple'
import { createStyle } from '@/utils/tools'

import Background from './components/Background'
import Header from './components/Header'
import Artwork from './components/Artwork'
import TrackInfo from './components/TrackInfo'
import PlayProgress from './components/PlayProgress'
import ControlBtns from './components/ControlBtns'
import ActionBar from './components/ActionBar'
import SettingPopup, { type SettingPopupType } from './components/SettingPopup'
import Lyric from './Lyric'

/** 下拉超过此距离（或快速下滑）即关闭 */
const CLOSE_DISTANCE = 120
const CLOSE_VELOCITY = 1

/** 歌词保持亮屏：歌词可见、应用在前台且没有打开评论页时 */
const useLyricKeepAwake = (lyricVisible: boolean) => {
  const visibleRef = useRef(lyricVisible)

  useEffect(() => {
    visibleRef.current = lyricVisible
    if (lyricVisible && !commonState.componentIds.comment) screenkeepAwake()
    else screenUnkeepAwake()
  }, [lyricVisible])

  useEffect(() => {
    const appstateListener = AppState.addEventListener('change', (state) => {
      switch (state) {
        case 'active':
          if (visibleRef.current && !commonState.componentIds.comment) screenkeepAwake()
          break
        case 'background':
          screenUnkeepAwake()
          break
      }
    })
    const handleComponentIdsChange = (ids: CommonState['componentIds']) => {
      if (!visibleRef.current) return
      if (ids.comment) screenUnkeepAwake()
      else if (AppState.currentState == 'active') screenkeepAwake()
    }
    global.state_event.on('componentIdsUpdated', handleComponentIdsChange)
    return () => {
      global.state_event.off('componentIdsUpdated', handleComponentIdsChange)
      appstateListener.remove()
      screenUnkeepAwake()
    }
  }, [])
}

/** 下拉关闭手势：返回整页位移动画值和手势处理 */
const usePullToClose = (componentId: string) => {
  const { height } = useWindowSize()
  const translateY = useRef(new Animated.Value(0)).current
  const heightRef = useRef(height)
  heightRef.current = height

  const panResponder = useMemo(() => PanResponder.create({
    onMoveShouldSetPanResponder: (_, { dx, dy }) => dy > 10 && dy > Math.abs(dx) * 1.5,
    onPanResponderMove: (_, { dy }) => {
      translateY.setValue(Math.max(dy, 0))
    },
    onPanResponderRelease: (_, { dy, vy }) => {
      if (dy > CLOSE_DISTANCE || vy > CLOSE_VELOCITY) {
        Animated.timing(translateY, {
          toValue: heightRef.current,
          duration: 200,
          useNativeDriver: true,
        }).start(() => {
          void Navigation.pop(componentId, { animations: { pop: { enabled: false } } })
        })
      } else {
        Animated.spring(translateY, { toValue: 0, useNativeDriver: true, bounciness: 4 }).start()
      }
    },
    onPanResponderTerminate: () => {
      Animated.spring(translateY, { toValue: 0, useNativeDriver: true }).start()
    },
  }), [componentId, translateY])

  return { translateY, panHandlers: panResponder.panHandlers }
}

/** 用容器尺寸算封面边长 */
const useArtworkSize = (ratio: number) => {
  const [imageSize, setImageSize] = useState(0)
  const onLayout = useCallback(({ nativeEvent }: LayoutChangeEvent) => {
    const { width, height } = nativeEvent.layout
    setImageSize(Math.max(Math.floor(Math.min(width * ratio, height * 0.92)), 0))
  }, [ratio])
  return { imageSize, onLayout }
}

const PageDots = ({ index }: { index: number }) => {
  const apple = useApple()
  return (
    <View style={styles.dots} pointerEvents="none">
      {[0, 1].map(i => <View key={i} style={{ ...styles.dot, backgroundColor: i == index ? apple.secondaryLabel : apple.quaternaryLabel }} />)}
    </View>
  )
}

/** 竖屏：封面页 / 歌词页左右滑动，下方固定播放控制 */
const VerticalLayout = memo(({ componentId, panHandlers }: {
  componentId: string
  panHandlers: ReturnType<typeof usePullToClose>['panHandlers']
}) => {
  const [pageIndex, setPageIndex] = useState(0)
  const [lyricInited, setLyricInited] = useState(false)
  const { imageSize, onLayout } = useArtworkSize(0.86)
  useLyricKeepAwake(pageIndex == 1)

  const onPageSelected = ({ nativeEvent }: PagerViewOnPageSelectedEvent) => {
    setPageIndex(nativeEvent.position)
    if (nativeEvent.position == 1) setLyricInited(true)
  }
  const lyric = useMemo(() => <Lyric direction="vertical" />, [])

  return (
    <>
      <PagerView style={styles.pager} onPageSelected={onPageSelected}>
        <View collapsable={false} style={styles.page} onLayout={onLayout} {...panHandlers}>
          {imageSize ? <Artwork componentId={componentId} imageSize={imageSize} /> : null}
        </View>
        <View collapsable={false} style={styles.page}>
          {lyricInited ? lyric : null}
        </View>
      </PagerView>
      <PageDots index={pageIndex} />
      <View style={styles.player} nativeID={NAV_SHEAR_NATIVE_IDS.playDetail_player}>
        <TrackInfo />
        <PlayProgress />
        <ControlBtns />
        <ActionBar />
      </View>
    </>
  )
})

/** 宽屏：左侧封面与播放控制，右侧歌词 */
const WideLayout = memo(({ componentId, panHandlers }: {
  componentId: string
  panHandlers: ReturnType<typeof usePullToClose>['panHandlers']
}) => {
  const { imageSize, onLayout } = useArtworkSize(0.9)
  useLyricKeepAwake(true)

  return (
    <View style={styles.wide}>
      <View style={styles.wideLeft}>
        <View style={styles.page} onLayout={onLayout} {...panHandlers}>
          {imageSize ? <Artwork componentId={componentId} imageSize={imageSize} /> : null}
        </View>
        <View style={styles.player} nativeID={NAV_SHEAR_NATIVE_IDS.playDetail_player}>
          <TrackInfo />
          <PlayProgress />
          <ControlBtns />
          <ActionBar />
        </View>
      </View>
      <View style={styles.wideRight}>
        <Lyric direction="horizontal" />
      </View>
    </View>
  )
})

export default ({ componentId }: { componentId: string }) => {
  const isWide = useHorizontalMode()
  const statusBarHeight = useStatusbarHeight()
  const popupRef = useRef<SettingPopupType>(null)
  const { translateY, panHandlers } = usePullToClose(componentId)

  useEffect(() => {
    setComponentId(COMPONENT_IDS.playDetail, componentId)
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  const handleClose = useCallback(() => {
    void pop(componentId)
  }, [componentId])
  const handleShowSetting = useCallback(() => {
    popupRef.current?.show()
  }, [])

  return (
    <PageContent>
      <StatusBar />
      <Animated.View style={{ ...styles.root, transform: [{ translateY }] }}>
        <Background />
        <View style={{ ...styles.container, paddingTop: statusBarHeight }}>
          <Header onClose={handleClose} onShowSetting={handleShowSetting} panHandlers={panHandlers} />
          {
            isWide
              ? <WideLayout componentId={componentId} panHandlers={panHandlers} />
              : <VerticalLayout componentId={componentId} panHandlers={panHandlers} />
          }
        </View>
      </Animated.View>
      <SettingPopup ref={popupRef} direction={isWide ? 'horizontal' : 'vertical'} />
    </PageContent>
  )
}

const styles = createStyle({
  root: {
    flex: 1,
    overflow: 'hidden',
    borderTopLeftRadius: radius.sheet,
    borderTopRightRadius: radius.sheet,
  },
  container: {
    flex: 1,
  },
  pager: {
    flex: 1,
  },
  page: {
    flex: 1,
  },
  dots: {
    flexDirection: 'row',
    justifyContent: 'center',
    paddingVertical: spacing.xs,
  },
  dot: {
    width: 6,
    height: 6,
    borderRadius: radius.pill,
    marginHorizontal: spacing.xs,
  },
  player: {
    paddingHorizontal: pagePadding * 1.5,
    paddingBottom: spacing.lg,
  },
  wide: {
    flex: 1,
    flexDirection: 'row',
  },
  wideLeft: {
    flex: 1,
    maxWidth: 520,
  },
  wideRight: {
    flex: 1,
  },
})
