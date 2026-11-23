// Modified for Apple Music style UI. Original: lyswhut/lx-music-mobile (Apache-2.0)
import { memo, useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { Animated, AppState, PanResponder, StatusBar, TouchableOpacity, View, type LayoutChangeEvent } from 'react-native'
import PagerView, { type PagerViewOnPageSelectedEvent } from 'react-native-pager-view'
import { Navigation } from 'react-native-navigation'
import PageContent from '@/components/PageContent'
import Image from '@/components/common/Image'
import Text from '@/components/common/Text'
import { IonIcon } from '@/components/common/Icon'
import { setComponentId } from '@/core/common'
import { COMPONENT_IDS, NAV_SHEAR_NATIVE_IDS } from '@/config/constant'
import { pop } from '@/navigation'
import commonState, { type InitState as CommonState } from '@/store/common/state'
import { useStatusbarHeight } from '@/store/common/hook'
import { usePlayerMusicInfo } from '@/store/player/hook'
import { useHorizontalMode, useWindowSize } from '@/utils/hooks'
import { screenkeepAwake, screenUnkeepAwake } from '@/utils/nativeModules/utils'
import { fontSize, fontWeight, pagePadding, radius, size, spacing } from '@/theme/apple'
import { createStyle } from '@/utils/tools'

import Background from './components/Background'
import Header from './components/Header'
import Artwork from './components/Artwork'
import TrackInfo, { MusicAddBtn } from './components/TrackInfo'
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

/** 歌词视图顶部紧凑歌曲行：44x44 小封面 + 歌名（白粗体）+ 歌手（半透明白），点击切回封面视图 */
const LyricTrackHeader = memo(({ onBackToArtwork }: { onBackToArtwork: () => void }) => {
  const musicInfo = usePlayerMusicInfo()

  return (
    <View style={styles.lyricHeader}>
      <TouchableOpacity style={styles.lyricHeaderMain} activeOpacity={0.7} onPress={onBackToArtwork}>
        {musicInfo.pic ? (
          <Image url={musicInfo.pic} style={styles.miniArtwork} />
        ) : (
          <View style={styles.miniArtworkPlaceholder}>
            <IonIcon name="musical-notes" size={18} color="rgba(255, 255, 255, 0.35)" />
          </View>
        )}
        <View style={styles.miniTexts}>
          <Text numberOfLines={1} size={fontSize.headline} color="#FFFFFF" style={styles.miniTitle}>{musicInfo.name}</Text>
          <Text numberOfLines={1} size={fontSize.footnote} color="rgba(255, 255, 255, 0.68)" style={styles.miniSinger}>{musicInfo.singer}</Text>
        </View>
      </TouchableOpacity>
      <MusicAddBtn />
    </View>
  )
})

/** 竖屏：封面视图 / 歌词视图切换（支持底部歌词按钮或左右滑），下方固定播放控制 */
const VerticalLayout = memo(({ componentId, panHandlers }: {
  componentId: string
  panHandlers: ReturnType<typeof usePullToClose>['panHandlers']
}) => {
  const pagerRef = useRef<PagerView>(null)
  const [pageIndex, setPageIndex] = useState(0)
  const [lyricInited, setLyricInited] = useState(false)
  const { imageSize, onLayout } = useArtworkSize(0.86)
  useLyricKeepAwake(pageIndex == 1)

  const onPageSelected = useCallback(({ nativeEvent }: PagerViewOnPageSelectedEvent) => {
    setPageIndex(nativeEvent.position)
    if (nativeEvent.position == 1) setLyricInited(true)
  }, [])

  const handleToggleLyric = useCallback(() => {
    const next = pageIndex == 0 ? 1 : 0
    if (next == 1) setLyricInited(true)
    setPageIndex(next)
    pagerRef.current?.setPage(next)
  }, [pageIndex])

  const handleBackToArtwork = useCallback(() => {
    setPageIndex(0)
    pagerRef.current?.setPage(0)
  }, [])

  const lyric = useMemo(() => <Lyric direction="vertical" />, [])

  return (
    <>
      <PagerView ref={pagerRef} style={styles.pager} onPageSelected={onPageSelected}>
        <View collapsable={false} style={styles.page} onLayout={onLayout} {...panHandlers}>
          {imageSize ? <Artwork componentId={componentId} imageSize={imageSize} /> : null}
        </View>
        <View collapsable={false} style={styles.lyricPage}>
          <LyricTrackHeader onBackToArtwork={handleBackToArtwork} />
          <View style={styles.lyricBody}>
            {lyricInited ? lyric : null}
          </View>
        </View>
      </PagerView>
      <View style={styles.player} nativeID={NAV_SHEAR_NATIVE_IDS.playDetail_player}>
        {pageIndex == 0 ? <TrackInfo /> : null}
        <PlayProgress />
        <ControlBtns />
        <ActionBar lyricActive={pageIndex == 1} onToggleLyric={handleToggleLyric} />
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
      <StatusBar backgroundColor="rgba(0,0,0,0)" barStyle="light-content" translucent={true} />
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
  lyricPage: {
    flex: 1,
  },
  lyricHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: pagePadding * 1.5,
    paddingTop: spacing.xs,
    paddingBottom: spacing.sm,
  },
  lyricHeaderMain: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
    paddingRight: spacing.md,
  },
  miniArtwork: {
    width: size.songArtwork,
    height: size.songArtwork,
    borderRadius: radius.small,
  },
  miniArtworkPlaceholder: {
    width: size.songArtwork,
    height: size.songArtwork,
    borderRadius: radius.small,
    backgroundColor: 'rgba(255, 255, 255, 0.08)',
    justifyContent: 'center',
    alignItems: 'center',
  },
  miniTexts: {
    flex: 1,
    marginLeft: spacing.md,
  },
  miniTitle: {
    fontWeight: fontWeight.bold,
  },
  miniSinger: {
    marginTop: 2,
  },
  lyricBody: {
    flex: 1,
    overflow: 'hidden',
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
