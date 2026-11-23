import { memo, useCallback, useEffect, useRef, useState } from 'react'
import { View, PanResponder } from 'react-native'
import { createStyle } from '@/utils/tools'
import { useDrag } from '@/utils/hooks'

const TRACK_HEIGHT = 6
const TRACK_HEIGHT_ACTIVE = 9
const HIT_PADDING = 12
const HIT_HEIGHT = HIT_PADDING * 2 + TRACK_HEIGHT_ACTIVE

const PreassBar = memo(({ onDragState, setDragProgress, onSetProgress }: {
  onDragState: (drag: boolean) => void
  setDragProgress: (progress: number) => void
  onSetProgress: (progress: number) => void
}) => {
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

  return <View onLayout={onLayout} style={styles.pressBar} {...panResponder.panHandlers} />
})

/**
 * Apple Music 风格无圆点圆角胶囊进度条
 * 常态高 6px（拖动时 9px），纯白/半透明白配色，保留完整触摸拖动 seek 交互
 */
const Progress = ({ progress, duration, buffered }: {
  progress: number
  duration: number
  buffered: number
}) => {
  const [draging, setDraging] = useState(false)
  const [dragProgress, setDragProgress] = useState(0)

  const clampedProgress = Math.max(0, Math.min(1, progress))
  const clampedBuffered = Math.max(0, Math.min(1, buffered))
  const activeRatio = draging ? Math.max(0, Math.min(1, dragProgress)) : clampedProgress
  const activePercent: `${number}%` = `${activeRatio * 100}%`
  const bufferedPercent: `${number}%` = `${clampedBuffered * 100}%`

  const durationRef = useRef(duration)
  useEffect(() => {
    durationRef.current = duration
  }, [duration])

  const onSetProgress = useCallback((p: number) => {
    global.app_event.setProgress(p * durationRef.current)
  }, [])

  return (
    <View style={styles.progress}>
      <View style={[styles.track, draging ? styles.trackActive : styles.trackNormal]}>
        <View style={[styles.bufferedBar, { width: bufferedPercent }]} />
        <View
          style={[
            styles.fillBar,
            {
              width: activePercent,
              backgroundColor: draging ? '#FFFFFF' : 'rgba(255, 255, 255, 0.88)',
            },
          ]}
        />
      </View>
      <PreassBar onDragState={setDraging} setDragProgress={setDragProgress} onSetProgress={onSetProgress} />
    </View>
  )
}

const styles = createStyle({
  progress: {
    width: '100%',
    height: HIT_HEIGHT,
    justifyContent: 'center',
    zIndex: 1,
  },
  track: {
    width: '100%',
    backgroundColor: 'rgba(255, 255, 255, 0.24)',
    borderRadius: 4,
    overflow: 'hidden',
  },
  trackNormal: {
    height: TRACK_HEIGHT,
  },
  trackActive: {
    height: TRACK_HEIGHT_ACTIVE,
    borderRadius: 4.5,
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
    borderRadius: 4,
  },
  pressBar: {
    position: 'absolute',
    left: 0,
    top: 0,
    height: HIT_HEIGHT,
    width: '100%',
    zIndex: 6,
  },
})

export default Progress
