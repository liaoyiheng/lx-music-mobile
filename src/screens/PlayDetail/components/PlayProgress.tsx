// Modified for Apple Music iOS 26 Liquid Glass UI. Original: lyswhut/lx-music-mobile (Apache-2.0)
import { memo, useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { Animated, PanResponder, StyleSheet, View } from 'react-native'
import { IonIcon } from '@/components/common/Icon'
import Text from '@/components/common/Text'
import { getPlayQuality } from '@/core/music/utils'
import { useI18n } from '@/lang'
import { useBufferProgress } from '@/plugins/player'
import { usePlayMusicInfo, useProgress, useStatusText } from '@/store/player/hook'
import { useSettingValue } from '@/store/setting/hook'
import { fontWeight, spacing } from '@/theme/apple'
import { formatPlayTime2 } from '@/utils'
import { useDrag } from '@/utils/hooks'
import { createStyle } from '@/utils/tools'

const TRACK_HEIGHT_NORMAL = 4
const TRACK_HEIGHT_DRAG = 7
const HIT_PADDING = 12
const HIT_HEIGHT = HIT_PADDING * 2 + TRACK_HEIGHT_DRAG
const TIME_COLOR = 'rgba(255, 255, 255, 0.58)'

const KnoblessProgressBar = memo(({
  progress,
  duration,
  buffered,
  dragging,
  dragProgress,
  onDragState,
  setDragProgress,
}: {
  progress: number
  duration: number
  buffered: number
  dragging: boolean
  dragProgress: number
  onDragState: (drag: boolean) => void
  setDragProgress: (progress: number) => void
}) => {
  const heightAnim = useRef(new Animated.Value(TRACK_HEIGHT_NORMAL)).current
  const durationRef = useRef(duration)

  useEffect(() => {
    durationRef.current = duration
  }, [duration])

  useEffect(() => {
    Animated.timing(heightAnim, {
      toValue: dragging ? TRACK_HEIGHT_DRAG : TRACK_HEIGHT_NORMAL,
      duration: 160,
      useNativeDriver: false,
    }).start()
  }, [dragging, heightAnim])

  const onSetProgress = useCallback((p: number) => {
    global.app_event.setProgress(p * durationRef.current)
  }, [])

  const {
    onLayout,
    onDragStart,
    onDragEnd,
    onDrag,
  } = useDrag(onSetProgress, onDragState, setDragProgress)

  const panResponder = useRef(
    PanResponder.create({
      onStartShouldSetPanResponderCapture: () => true,
      onMoveShouldSetPanResponderCapture: () => true,
      onPanResponderMove: (_, gestureState) => {
        onDrag(gestureState.dx)
      },
      onPanResponderGrant: (evt, gestureState) => {
        onDragStart(gestureState.dx, evt.nativeEvent.locationX)
      },
      onPanResponderRelease: () => {
        onDragEnd()
      },
      onPanResponderTerminate: () => {
        onDragEnd()
      },
    }),
  ).current

  const clampedProgress = Math.max(0, Math.min(1, progress))
  const clampedBuffered = Math.max(0, Math.min(1, buffered))
  const activeRatio = dragging ? Math.max(0, Math.min(1, dragProgress)) : clampedProgress
  const activePercent: `${number}%` = `${activeRatio * 100}%`
  const bufferedPercent: `${number}%` = `${clampedBuffered * 100}%`

  return (
    <View style={styles.progressWrap}>
      <Animated.View
        style={[
          styles.track,
          {
            height: heightAnim,
            backgroundColor: dragging ? 'rgba(255, 255, 255, 0.30)' : 'rgba(255, 255, 255, 0.22)',
          },
        ]}
      >
        <View style={[styles.bufferedBar, { width: bufferedPercent }]} />
        <View
          style={[
            styles.fillBar,
            {
              width: activePercent,
              backgroundColor: dragging ? '#FFFFFF' : 'rgba(255, 255, 255, 0.90)',
            },
          ]}
        />
      </Animated.View>
      <View onLayout={onLayout} style={styles.pressBar} {...panResponder.panHandlers} />
    </View>
  )
})

const useQualityLabel = () => {
  const playMusicInfo = usePlayMusicInfo()
  const playQuality = useSettingValue('player.playQuality')
  const t = useI18n()

  return useMemo(() => {
    const lossless = t('apple_player_lossless')
    const raw = playMusicInfo.musicInfo
    if (!raw) return lossless

    if ('progress' in raw) {
      const q = raw.metadata.quality
      if (q === 'flac24bit') return 'Hi-Res'
      if (q === 'flac' || q === 'ape' || q === 'wav') return lossless
      if (q === '320k' || q === '192k') return 'HQ'
      return 'SQ'
    }

    if (raw.source === 'local') {
      const ext = raw.meta.ext?.toLowerCase()
      if (ext === 'flac' || ext === 'wav' || ext === 'ape') return lossless
      return 'HQ'
    }

    const q = getPlayQuality(playQuality, raw)
    if (q === 'flac24bit') return 'Hi-Res'
    if (q === 'flac') return lossless
    if (q === '320k' || q === '192k') return 'HQ'
    return 'SQ'
  }, [playMusicInfo.musicInfo, playQuality, t])
}

/**
 * 无圆点胶囊进度条 + 11.5pt 等宽数字（已播 / 剩余倒计时 -MM:SS）+ 半透明玻璃音质徽章
 */
export default memo(() => {
  const { nowPlayTime, maxPlayTime, progress, nowPlayTimeStr, maxPlayTimeStr } = useProgress()
  const buffered = useBufferProgress()
  const statusText = useStatusText()
  const qualityLabel = useQualityLabel()

  const [dragging, setDragging] = useState(false)
  const [dragProgress, setDragProgress] = useState(0)

  const activeTime = dragging && maxPlayTime > 0 ? dragProgress * maxPlayTime : nowPlayTime
  const elapsedLabel = dragging && maxPlayTime > 0
    ? formatPlayTime2(activeTime)
    : nowPlayTimeStr
  const remainLabel = maxPlayTime > 0
    ? `-${formatPlayTime2(Math.max(0, maxPlayTime - activeTime))}`
    : maxPlayTimeStr

  return (
    <View style={styles.container}>
      <KnoblessProgressBar
        progress={progress}
        duration={maxPlayTime}
        buffered={buffered}
        dragging={dragging}
        dragProgress={dragProgress}
        onDragState={setDragging}
        setDragProgress={setDragProgress}
      />
      <View style={styles.infoRow}>
        <Text size={11.5} color={TIME_COLOR} style={styles.timeLeft}>{elapsedLabel}</Text>
        <View style={styles.centerSlot}>
          {statusText ? (
            <Text size={11} color="rgba(255, 255, 255, 0.72)" numberOfLines={1} style={styles.statusText}>
              {statusText}
            </Text>
          ) : (
            <View style={styles.qualityBadge}>
              <IonIcon name="pulse" rawSize={11} color="rgba(255, 255, 255, 0.88)" />
              <Text size={10.5} color="rgba(255, 255, 255, 0.88)" style={styles.qualityText}>
                {qualityLabel}
              </Text>
            </View>
          )}
        </View>
        <Text size={11.5} color={TIME_COLOR} style={styles.timeRight}>{remainLabel}</Text>
      </View>
    </View>
  )
})

const styles = createStyle({
  container: {
    marginTop: spacing.xs,
  },
  progressWrap: {
    width: '100%',
    height: HIT_HEIGHT,
    justifyContent: 'center',
    zIndex: 1,
  },
  track: {
    width: '100%',
    borderRadius: 999,
    overflow: 'hidden',
  },
  bufferedBar: {
    position: 'absolute',
    left: 0,
    top: 0,
    bottom: 0,
    backgroundColor: 'rgba(255, 255, 255, 0.14)',
  },
  fillBar: {
    position: 'absolute',
    left: 0,
    top: 0,
    bottom: 0,
    borderRadius: 999,
  },
  pressBar: {
    position: 'absolute',
    left: 0,
    top: 0,
    height: HIT_HEIGHT,
    width: '100%',
    zIndex: 6,
  },
  infoRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginTop: -4,
  },
  timeLeft: {
    flex: 1,
    fontVariant: ['tabular-nums'],
    fontWeight: fontWeight.semibold,
  },
  centerSlot: {
    flexShrink: 1,
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: spacing.sm,
  },
  timeRight: {
    flex: 1,
    textAlign: 'right',
    fontVariant: ['tabular-nums'],
    fontWeight: fontWeight.semibold,
  },
  statusText: {
    textAlign: 'center',
  },
  qualityBadge: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
    paddingHorizontal: 8,
    paddingVertical: 2.5,
    borderRadius: 6,
    backgroundColor: 'rgba(255, 255, 255, 0.14)',
    borderWidth: StyleSheet.hairlineWidth,
    borderColor: 'rgba(255, 255, 255, 0.22)',
  },
  qualityText: {
    fontWeight: fontWeight.bold,
    letterSpacing: 0.2,
  },
})
