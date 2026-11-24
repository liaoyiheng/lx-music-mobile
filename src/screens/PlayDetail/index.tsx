// Modified for Apple Music iOS 26 Liquid Glass UI. Original: lyswhut/lx-music-mobile (Apache-2.0)
import { memo, useCallback, useEffect, useMemo, useRef, useState } from 'react'
import {
  Animated,
  AppState,
  PanResponder,
  StatusBar,
  StyleSheet,
  TouchableOpacity,
  View,
  type LayoutChangeEvent,
} from 'react-native'
import { Navigation } from 'react-native-navigation'
import PagerView, { type PagerViewOnPageSelectedEvent } from 'react-native-pager-view'
import DesktopLyricEnable, { type DesktopLyricEnableType } from '@/components/DesktopLyricEnable'
import MusicAddModal, { type MusicAddModalType } from '@/components/MusicAddModal'
import PageContent from '@/components/PageContent'
import TimeoutExitEditModal, { type TimeoutExitEditModalType, useTimeInfo } from '@/components/TimeoutExitEditModal'
import { IonIcon } from '@/components/common/Icon'
import Text from '@/components/common/Text'
import { COMPONENT_IDS, NAV_SHEAR_NATIVE_IDS } from '@/config/constant'
import { setComponentId, updateSetting } from '@/core/common'
import { toggleTranslation } from '@/core/lyric'
import { togglePlay } from '@/core/player/player'
import { useI18n } from '@/lang'
import { pop } from '@/navigation'
import { useStatusbarHeight } from '@/store/common/hook'
import commonState, { type InitState as CommonState } from '@/store/common/state'
import { useIsPlay } from '@/store/player/hook'
import playerState from '@/store/player/state'
import { useSettingValue } from '@/store/setting/hook'
import { fontWeight, pagePadding, radius, spacing } from '@/theme/apple'
import { useHorizontalMode, useWindowSize } from '@/utils/hooks'
import { screenUnkeepAwake, screenkeepAwake } from '@/utils/nativeModules/utils'
import { createStyle } from '@/utils/tools'

import Lyric from './Lyric'
import ActionBar, { type PlayerViewMode } from './components/ActionBar'
import Artwork from './components/Artwork'
import Background from './components/Background'
import ControlBtns from './components/ControlBtns'
import Header from './components/Header'
import PlayProgress from './components/PlayProgress'
import SettingPopup, { type SettingPopupType } from './components/SettingPopup'
import TrackInfo, { MiniTrackHeader } from './components/TrackInfo'
import UpNextQueue from './components/UpNextQueue'

/** 下拉超过此距离（或快速下滑）即关闭 */
const CLOSE_DISTANCE = 120
const CLOSE_VELOCITY = 1

const VIEW_ORDER: readonly PlayerViewMode[] = ['cover', 'lyric', 'queue']

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
      if (ids.comment) screenkeepAwake()
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

/** 根据容器宽度和高度计算响应式大封面尺寸：min(screenW - 64, 326, height * 0.92) */
const useArtworkSize = (maxSize = 326) => {
  const [imageSize, setImageSize] = useState(0)
  const onLayout = useCallback(({ nativeEvent }: LayoutChangeEvent) => {
    const { width, height } = nativeEvent.layout
    const target = Math.floor(Math.min(width - 64, height * 0.92, maxSize))
    setImageSize(Math.max(target, 0))
  }, [maxSize])
  return { imageSize, onLayout }
}

/** 歌词视图底部紧凑控制条：居中 52×52 播放/暂停圆钮 + 右侧「译」切换胶囊 */
const LyricCompactTransport = memo(() => {
  const isPlay = useIsPlay()
  const t = useI18n()
  const isShowTranslation = useSettingValue('player.isShowLyricTranslation')

  const handleToggleTrans = useCallback(() => {
    const next = !isShowTranslation
    updateSetting({ 'player.isShowLyricTranslation': next })
    void toggleTranslation(next)
  }, [isShowTranslation])

  return (
    <View style={styles.lyricTransportRow}>
      <TouchableOpacity
        style={styles.lyricPlayBtn}
        activeOpacity={0.72}
        onPress={togglePlay}
        accessibilityRole="button"
        accessibilityLabel={t(isPlay ? 'apple_player_pause' : 'apple_player_play')}
      >
        <IonIcon name={isPlay ? 'pause' : 'play'} rawSize={24} color="#FFFFFF" />
      </TouchableOpacity>
      <TouchableOpacity
        style={[styles.transPillBtn, isShowTranslation ? styles.transPillBtnActive : null]}
        activeOpacity={0.72}
        onPress={handleToggleTrans}
        accessibilityRole="button"
        accessibilityLabel={t('apple_player_translation')}
      >
        <Text
          size={12}
          color={isShowTranslation ? '#16161C' : 'rgba(255, 255, 255, 0.88)'}
          style={styles.transPillText}
        >
          {t('apple_player_translation')}
        </Text>
      </TouchableOpacity>
    </View>
  )
})

/** 竖屏 / 窄屏：三视图无缝切换（封面主控 / 逐字大字歌词 / 待播清单） */
const VerticalLayout = memo(({
  componentId,
  panHandlers,
  onShowSetting,
  onShowMusicAdd,
}: {
  componentId: string
  panHandlers: ReturnType<typeof usePullToClose>['panHandlers']
  onShowSetting: () => void
  onShowMusicAdd: () => void
}) => {
  const { imageSize, onLayout } = useArtworkSize(326)
  const pagerRef = useRef<PagerView>(null)
  const [activeView, setActiveView] = useState<PlayerViewMode>('cover')
  const [lyricInited, setLyricInited] = useState(false)
  const [queueInited, setQueueInited] = useState(false)

  useLyricKeepAwake(activeView === 'lyric')

  const switchToView = useCallback((view: PlayerViewMode) => {
    const idx = VIEW_ORDER.indexOf(view)
    if (idx < 0) return
    if (view === 'lyric') setLyricInited(true)
    if (view === 'queue') setQueueInited(true)
    setActiveView(view)
    pagerRef.current?.setPage(idx)
  }, [])

  const onPageSelected = useCallback(({ nativeEvent }: PagerViewOnPageSelectedEvent) => {
    const view = VIEW_ORDER[nativeEvent.position] ?? 'cover'
    if (view === 'lyric') setLyricInited(true)
    if (view === 'queue') setQueueInited(true)
    setActiveView(view)
  }, [])

  const handleToggleLyric = useCallback(() => {
    switchToView(activeView === 'lyric' ? 'cover' : 'lyric')
  }, [activeView, switchToView])

  const handleToggleQueue = useCallback(() => {
    switchToView(activeView === 'queue' ? 'cover' : 'queue')
  }, [activeView, switchToView])

  const handleBackToCover = useCallback(() => {
    switchToView('cover')
  }, [switchToView])

  const lyricView = useMemo(() => <Lyric direction="vertical" />, [])
  const queueView = useMemo(() => <UpNextQueue />, [])

  return (
    <View style={styles.verticalRoot}>
      <PagerView
        ref={pagerRef}
        style={styles.pager}
        initialPage={0}
        onPageSelected={onPageSelected}
      >
        {/* 视图 A：封面主控 */}
        <View collapsable={false} style={styles.coverPage}>
          <View style={styles.artworkWrap} onLayout={onLayout} {...panHandlers}>
            {imageSize ? <Artwork componentId={componentId} imageSize={imageSize} /> : null}
          </View>
          <View style={styles.playerControls} nativeID={NAV_SHEAR_NATIVE_IDS.playDetail_player}>
            <TrackInfo onShowSetting={onShowSetting} onShowMusicAdd={onShowMusicAdd} />
            <PlayProgress />
            <ControlBtns />
          </View>
        </View>

        {/* 视图 B：大字沉浸歌词 */}
        <View collapsable={false} style={styles.subPage}>
          <MiniTrackHeader
            onBackToCover={handleBackToCover}
            onShowSetting={onShowSetting}
            onShowMusicAdd={onShowMusicAdd}
          />
          <View style={styles.subBody}>
            {lyricInited ? lyricView : null}
          </View>
          <LyricCompactTransport />
        </View>

        {/* 视图 C：待播清单 */}
        <View collapsable={false} style={styles.subPage}>
          <MiniTrackHeader
            onBackToCover={handleBackToCover}
            onShowSetting={onShowSetting}
            onShowMusicAdd={onShowMusicAdd}
          />
          <View style={styles.subBody}>
            {queueInited ? queueView : null}
          </View>
        </View>
      </PagerView>

      <View style={styles.bottomBarWrap}>
        <ActionBar
          activeView={activeView}
          onToggleLyric={handleToggleLyric}
          onShowSetting={onShowSetting}
          onToggleQueue={handleToggleQueue}
        />
      </View>
    </View>
  )
})

/** 宽屏 / 横屏：左侧封面主控，右侧在歌词与待播清单间切换 */
const WideLayout = memo(({
  componentId,
  panHandlers,
  onShowSetting,
  onShowMusicAdd,
}: {
  componentId: string
  panHandlers: ReturnType<typeof usePullToClose>['panHandlers']
  onShowSetting: () => void
  onShowMusicAdd: () => void
}) => {
  const { imageSize, onLayout } = useArtworkSize(320)
  const [rightView, setRightView] = useState<'lyric' | 'queue'>('lyric')

  useLyricKeepAwake(rightView === 'lyric')

  const handleToggleLyric = useCallback(() => {
    setRightView('lyric')
  }, [])

  const handleToggleQueue = useCallback(() => {
    setRightView(prev => prev === 'queue' ? 'lyric' : 'queue')
  }, [])

  return (
    <View style={styles.wide}>
      <View style={styles.wideLeft}>
        <View style={styles.artworkWrap} onLayout={onLayout} {...panHandlers}>
          {imageSize ? <Artwork componentId={componentId} imageSize={imageSize} /> : null}
        </View>
        <View style={styles.widePlayerControls} nativeID={NAV_SHEAR_NATIVE_IDS.playDetail_player}>
          <TrackInfo onShowSetting={onShowSetting} onShowMusicAdd={onShowMusicAdd} />
          <PlayProgress />
          <ControlBtns />
          <View style={styles.wideActionWrap}>
            <ActionBar
              activeView={rightView}
              onToggleLyric={handleToggleLyric}
              onShowSetting={onShowSetting}
              onToggleQueue={handleToggleQueue}
            />
          </View>
        </View>
      </View>
      <View style={styles.wideRight}>
        {rightView === 'lyric' ? <Lyric direction="horizontal" /> : <UpNextQueue />}
      </View>
    </View>
  )
})

export default ({ componentId }: { componentId: string }) => {
  const isWide = useHorizontalMode()
  const statusBarHeight = useStatusbarHeight()
  const popupRef = useRef<SettingPopupType>(null)
  const desktopLyricEnableRef = useRef<DesktopLyricEnableType>(null)
  const timeoutExitModalRef = useRef<TimeoutExitEditModalType>(null)
  const musicAddModalRef = useRef<MusicAddModalType>(null)
  const timeInfo = useTimeInfo()
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

  const handleToggleDesktopLyric = useCallback((enable: boolean) => {
    desktopLyricEnableRef.current?.setEnabled(enable)
  }, [])

  const handleShowTimeoutExit = useCallback(() => {
    timeoutExitModalRef.current?.show()
  }, [])

  const handleShowMusicAdd = useCallback(() => {
    const musicInfo = playerState.playMusicInfo.musicInfo
    if (!musicInfo || !playerState.playMusicInfo.listId) return
    musicAddModalRef.current?.show({
      musicInfo: 'progress' in musicInfo ? musicInfo.metadata.musicInfo : musicInfo,
      isMove: false,
      listId: playerState.playMusicInfo.listId,
    })
  }, [])

  return (
    <PageContent>
      <StatusBar backgroundColor="rgba(0,0,0,0)" barStyle="light-content" translucent={true} />
      <Animated.View style={{ ...styles.root, transform: [{ translateY }] }}>
        <Background />
        <View style={{ ...styles.container, paddingTop: statusBarHeight }}>
          <Header onClose={handleClose} panHandlers={panHandlers} />
          {isWide ? (
            <WideLayout
              componentId={componentId}
              panHandlers={panHandlers}
              onShowSetting={handleShowSetting}
              onShowMusicAdd={handleShowMusicAdd}
            />
          ) : (
            <VerticalLayout
              componentId={componentId}
              panHandlers={panHandlers}
              onShowSetting={handleShowSetting}
              onShowMusicAdd={handleShowMusicAdd}
            />
          )}
        </View>
      </Animated.View>
      <SettingPopup
        ref={popupRef}
        direction={isWide ? 'horizontal' : 'vertical'}
        timeoutActive={timeInfo.active}
        onToggleDesktopLyric={handleToggleDesktopLyric}
        onShowTimeoutExit={handleShowTimeoutExit}
        onShowMusicAdd={handleShowMusicAdd}
      />
      <DesktopLyricEnable ref={desktopLyricEnableRef} />
      <TimeoutExitEditModal ref={timeoutExitModalRef} timeInfo={timeInfo} />
      <MusicAddModal ref={musicAddModalRef} />
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
  verticalRoot: {
    flex: 1,
  },
  pager: {
    flex: 1,
  },
  coverPage: {
    flex: 1,
  },
  subPage: {
    flex: 1,
  },
  subBody: {
    flex: 1,
    minHeight: 0,
    overflow: 'hidden',
  },
  artworkWrap: {
    flex: 1,
  },
  playerControls: {
    paddingHorizontal: pagePadding + 12,
    paddingBottom: spacing.xs,
  },
  bottomBarWrap: {
    paddingHorizontal: pagePadding,
    paddingBottom: spacing.lg,
  },
  lyricTransportRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: pagePadding + 12,
    paddingTop: spacing.xs,
    paddingBottom: spacing.xs,
    position: 'relative',
  },
  lyricPlayBtn: {
    width: 52,
    height: 52,
    borderRadius: 26,
    backgroundColor: 'rgba(255, 255, 255, 0.14)',
    borderWidth: StyleSheet.hairlineWidth,
    borderColor: 'rgba(255, 255, 255, 0.22)',
    alignItems: 'center',
    justifyContent: 'center',
  },
  transPillBtn: {
    position: 'absolute',
    right: pagePadding + 12,
    height: 28,
    paddingHorizontal: 11,
    borderRadius: radius.pill,
    backgroundColor: 'rgba(255, 255, 255, 0.14)',
    borderWidth: StyleSheet.hairlineWidth,
    borderColor: 'rgba(255, 255, 255, 0.26)',
    alignItems: 'center',
    justifyContent: 'center',
  },
  transPillBtnActive: {
    backgroundColor: 'rgba(255, 255, 255, 0.90)',
    borderColor: 'rgba(255, 255, 255, 0.96)',
  },
  transPillText: {
    fontWeight: fontWeight.bold,
  },
  wide: {
    flex: 1,
    flexDirection: 'row',
  },
  wideLeft: {
    flex: 1,
    maxWidth: 500,
  },
  widePlayerControls: {
    paddingHorizontal: pagePadding + 12,
    paddingBottom: spacing.lg,
  },
  wideActionWrap: {
    marginTop: spacing.xs,
  },
  wideRight: {
    flex: 1,
  },
})
