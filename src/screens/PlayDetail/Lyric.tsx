// Modified for Apple Music iOS 26 Liquid Glass UI. Original: lyswhut/lx-music-mobile (Apache-2.0)
import { memo, useCallback, useEffect, useMemo, useRef, useState } from 'react'
import {
  FlatList,
  TouchableOpacity,
  View,
  type FlatListProps,
  type LayoutChangeEvent,
  type ListRenderItem,
  type NativeScrollEvent,
  type NativeSyntheticEvent,
} from 'react-native'
import { IonIcon } from '@/components/common/Icon'
import Text, { AnimatedColorText } from '@/components/common/Text'
import { useI18n } from '@/lang'
import { type Line, useLrcPlay, useLrcSet } from '@/plugins/lyric'
import playerState from '@/store/player/state'
import { useSettingValue } from '@/store/setting/hook'
import { fontWeight, pagePadding, spacing } from '@/theme/apple'
import { setSpText } from '@/utils/pixelRatio'
import { scrollTo } from '@/utils/scroll'
import { createStyle } from '@/utils/tools'
import PlayLine, { type PlayLineType } from './components/PlayLine'

type FlatListType = FlatListProps<Line>
export type LyricDirection = 'vertical' | 'horizontal'

type DepthBucket = 'past' | 'active' | 'next1' | 'next2'

const getDepthBucket = (lineNum: number, activeLine: number): DepthBucket => {
  if (lineNum === activeLine) return 'active'
  if (lineNum < activeLine) return 'past'
  if (lineNum === activeLine + 1) return 'next1'
  return 'next2'
}

const BUCKET_OPACITY: Record<DepthBucket, number> = {
  active: 1,
  past: 0.38,
  next1: 0.50,
  next2: 0.38,
}

interface LineProps {
  line: Line
  lineNum: number
  depthBucket: DepthBucket
  direction: LyricDirection
  onLayout: (lineNum: number, height: number, width: number) => void
  onSeek: (timeMs: number) => void
}

const LrcLine = memo(({ line, lineNum, depthBucket, direction, onLayout, onSeek }: LineProps) => {
  const lrcFontSize = useSettingValue(
    direction == 'horizontal'
      ? 'playDetail.horizontal.style.lrcFontSize'
      : 'playDetail.vertical.style.lrcFontSize',
  )
  const textAlign = useSettingValue('playDetail.style.align')
  // 默认 210 -> 26pt（当前行 ~28pt Bold），随设置字号比例缩放
  const baseSize = Math.round((lrcFontSize / 10) * 1.24)
  const active = depthBucket === 'active'
  const size = active ? Math.round(baseSize * 1.07) : baseSize
  const lineHeight = setSpText(size) * 1.32
  const opacity = BUCKET_OPACITY[depthBucket]

  const handleLayout = ({ nativeEvent }: LayoutChangeEvent) => {
    onLayout(lineNum, nativeEvent.layout.height, nativeEvent.layout.width)
  }

  return (
    <TouchableOpacity
      activeOpacity={0.72}
      onPress={() => { onSeek(line.time) }}
      onLayout={handleLayout}
      style={styles.line}
    >
      <AnimatedColorText
        style={[
          styles.lineText,
          {
            fontWeight: fontWeight.bold,
            textAlign,
            lineHeight,
          },
          active ? styles.activeGlow : null,
        ]}
        textBreakStrategy="simple"
        color="#FFFFFF"
        opacity={opacity}
        size={size}
      >
        {line.text}
      </AnimatedColorText>
      {line.extendedLyrics.map((lrc, index) => (
        <AnimatedColorText
          key={index}
          style={{
            ...styles.lineTranslationText,
            textAlign,
            lineHeight: setSpText(Math.max(13, Math.round(baseSize * 0.58))) * 1.4,
          }}
          textBreakStrategy="simple"
          color="#FFFFFF"
          opacity={active ? 0.78 : 0.38}
          size={Math.max(13, Math.round(baseSize * 0.58))}
        >
          {lrc}
        </AnimatedColorText>
      ))}
    </TouchableOpacity>
  )
}, (prevProps, nextProps) => {
  return prevProps.line === nextProps.line &&
    prevProps.depthBucket === nextProps.depthBucket &&
    prevProps.direction === nextProps.direction
})

const wait = async() => new Promise(resolve => setTimeout(resolve, 100))

export default memo(({ direction }: { direction: LyricDirection }) => {
  const lyricLines = useLrcSet()
  const { line } = useLrcPlay()
  const t = useI18n()
  const flatListRef = useRef<FlatList>(null)
  const playLineRef = useRef<PlayLineType>(null)
  const isPauseScrollRef = useRef(true)
  const scrollTimoutRef = useRef<NodeJS.Timeout | null>(null)
  const delayScrollTimeout = useRef<NodeJS.Timeout | null>(null)
  const lineRef = useRef({ line: 0, prevLine: 0 })
  const isFirstSetLrc = useRef(true)
  const scrollInfoRef = useRef<NativeSyntheticEvent<NativeScrollEvent>['nativeEvent'] | null>(null)
  const listLayoutInfoRef = useRef<{ spaceHeight: number, lineHeights: number[] }>({ spaceHeight: 0, lineHeights: [] })
  const scrollCancelRef = useRef<(() => void) | null>(null)
  const isShowLyricProgressSetting = useSettingValue('playDetail.isShowLyricProgressSetting')
  const [spaceHeight, setSpaceHeight] = useState(220)

  const handleScrollToActive = (index = lineRef.current.line) => {
    if (index < 0) return
    if (flatListRef.current) {
      if (scrollInfoRef.current && lineRef.current.line - lineRef.current.prevLine == 1) {
        let offset = listLayoutInfoRef.current.spaceHeight
        for (let i = 0; i < index; i++) {
          offset += listLayoutInfoRef.current.lineHeights[i] ?? 0
        }
        offset += (listLayoutInfoRef.current.lineHeights[index] ?? 0) / 2
        offset -= scrollInfoRef.current.layoutMeasurement.height * 0.38
        try {
          scrollCancelRef.current?.()
          scrollCancelRef.current = scrollTo(
            flatListRef.current,
            scrollInfoRef.current,
            offset,
            600,
            () => {
              scrollCancelRef.current = null
            },
          )
        } catch {}
      } else {
        if (scrollCancelRef.current) {
          scrollCancelRef.current()
          scrollCancelRef.current = null
        }
        try {
          flatListRef.current.scrollToIndex({
            index,
            animated: true,
            viewPosition: 0.38,
          })
        } catch {}
      }
    }
  }

  const handleScroll = ({ nativeEvent }: NativeSyntheticEvent<NativeScrollEvent>) => {
    scrollInfoRef.current = nativeEvent
    if (isPauseScrollRef.current) {
      playLineRef.current?.updateScrollInfo(nativeEvent)
    }
  }

  const handleScrollBeginDrag = () => {
    isPauseScrollRef.current = true
    playLineRef.current?.setVisible(true)
    if (scrollTimoutRef.current) {
      clearTimeout(scrollTimoutRef.current)
      scrollTimoutRef.current = null
    }
    if (delayScrollTimeout.current) {
      clearTimeout(delayScrollTimeout.current)
      delayScrollTimeout.current = null
    }
    if (scrollCancelRef.current) {
      scrollCancelRef.current()
      scrollCancelRef.current = null
    }
  }

  const onScrollEndDrag = () => {
    if (!isPauseScrollRef.current) return
    if (scrollTimoutRef.current) clearTimeout(scrollTimoutRef.current)
    scrollTimoutRef.current = setTimeout(() => {
      playLineRef.current?.setVisible(false)
      scrollTimoutRef.current = null
      isPauseScrollRef.current = false
      if (!playerState.isPlay) return
      handleScrollToActive()
    }, 3000)
  }

  useEffect(() => {
    return () => {
      if (delayScrollTimeout.current) {
        clearTimeout(delayScrollTimeout.current)
        delayScrollTimeout.current = null
      }
      if (scrollTimoutRef.current) {
        clearTimeout(scrollTimoutRef.current)
        scrollTimoutRef.current = null
      }
      scrollCancelRef.current?.()
    }
  }, [])

  useEffect(() => {
    listLayoutInfoRef.current.lineHeights = []
    lineRef.current.prevLine = 0
    lineRef.current.line = 0
    if (!flatListRef.current) return
    flatListRef.current.scrollToOffset({
      offset: 0,
      animated: false,
    })
    if (!lyricLines.length) return
    playLineRef.current?.updateLyricLines(lyricLines)
    requestAnimationFrame(() => {
      if (isFirstSetLrc.current) {
        isFirstSetLrc.current = false
        setTimeout(() => {
          isPauseScrollRef.current = false
          handleScrollToActive()
        }, 100)
      } else {
        if (delayScrollTimeout.current) clearTimeout(delayScrollTimeout.current)
        delayScrollTimeout.current = setTimeout(() => {
          handleScrollToActive(0)
        }, 100)
      }
    })
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [lyricLines])

  useEffect(() => {
    if (line < 0) return
    lineRef.current.prevLine = lineRef.current.line
    lineRef.current.line = line
    if (!flatListRef.current || isPauseScrollRef.current) return

    if (line - lineRef.current.prevLine != 1) {
      handleScrollToActive()
      return
    }

    delayScrollTimeout.current = setTimeout(() => {
      delayScrollTimeout.current = null
      handleScrollToActive()
    }, 600)
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [line])

  useEffect(() => {
    requestAnimationFrame(() => {
      playLineRef.current?.updateLayoutInfo(listLayoutInfoRef.current)
      playLineRef.current?.updateLyricLines(lyricLines)
    })
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isShowLyricProgressSetting])

  const handleScrollToIndexFailed: FlatListType['onScrollToIndexFailed'] = (info) => {
    void wait().then(() => {
      handleScrollToActive(info.index)
    })
  }

  const handleLineLayout = useCallback<LineProps['onLayout']>((lineNum, height) => {
    listLayoutInfoRef.current.lineHeights[lineNum] = height
    playLineRef.current?.updateLayoutInfo(listLayoutInfoRef.current)
  }, [])

  const handleContainerLayout = useCallback(({ nativeEvent }: LayoutChangeEvent) => {
    const h = Math.max(120, Math.round(nativeEvent.layout.height * 0.36))
    setSpaceHeight(h)
    listLayoutInfoRef.current.spaceHeight = h
    playLineRef.current?.updateLayoutInfo(listLayoutInfoRef.current)
  }, [])

  const handlePlayLine = useCallback((time: number) => {
    playLineRef.current?.setVisible(false)
    global.app_event.setProgress(time)
  }, [])

  const handleSeekByLine = useCallback((timeMs: number) => {
    global.app_event.setProgress(timeMs / 1000)
  }, [])

  const renderItem = useCallback<ListRenderItem<Line>>(({ item, index }) => {
    return (
      <LrcLine
        line={item}
        lineNum={index}
        depthBucket={getDepthBucket(index, line)}
        direction={direction}
        onLayout={handleLineLayout}
        onSeek={handleSeekByLine}
      />
    )
  }, [direction, handleLineLayout, handleSeekByLine, line])

  const getKey: FlatListType['keyExtractor'] = useCallback((item: Line, index: number) => `${index}${item.text}`, [])

  const spaceComponent = useMemo(() => (
    <View style={{ height: spaceHeight }} />
  ), [spaceHeight])

  if (!lyricLines.length) {
    return (
      <View style={styles.emptyWrap} onLayout={handleContainerLayout}>
        <IonIcon name="chatbubble-ellipses-outline" rawSize={34} color="rgba(255, 255, 255, 0.34)" />
        <Text size={16} color="rgba(255, 255, 255, 0.56)" style={styles.emptyText}>
          {t('apple_player_lyrics')}
        </Text>
      </View>
    )
  }

  return (
    <View style={styles.wrapper} onLayout={handleContainerLayout}>
      <FlatList
        data={lyricLines}
        renderItem={renderItem}
        keyExtractor={getKey}
        style={styles.container}
        ref={flatListRef}
        showsVerticalScrollIndicator={false}
        ListHeaderComponent={spaceComponent}
        ListFooterComponent={spaceComponent}
        onScrollBeginDrag={handleScrollBeginDrag}
        onScrollEndDrag={onScrollEndDrag}
        fadingEdgeLength={120}
        initialNumToRender={Math.max(line + 10, 12)}
        onScrollToIndexFailed={handleScrollToIndexFailed}
        onScroll={handleScroll}
      />
      {isShowLyricProgressSetting ? <PlayLine ref={playLineRef} onPlayLine={handlePlayLine} /> : null}
    </View>
  )
})

const styles = createStyle({
  wrapper: {
    flex: 1,
    minHeight: 0,
  },
  container: {
    flex: 1,
    paddingHorizontal: pagePadding + 6,
  },
  line: {
    paddingVertical: 11,
  },
  lineText: {
    letterSpacing: -0.35,
  },
  activeGlow: {
    textShadowColor: 'rgba(255, 255, 255, 0.35)',
    textShadowOffset: { width: 0, height: 0 },
    textShadowRadius: 12,
  },
  lineTranslationText: {
    paddingTop: 5,
    fontWeight: fontWeight.medium,
  },
  emptyWrap: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    gap: spacing.sm,
  },
  emptyText: {
    fontWeight: fontWeight.medium,
  },
})
