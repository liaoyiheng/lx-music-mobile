// Added for Apple Music style UI (based on lyswhut/lx-music-mobile, Apache-2.0)
import { memo } from 'react'
import { View } from 'react-native'
import Progress from '@/components/player/ProgressBar'
import Text from '@/components/common/Text'
import { useProgress, useStatusText } from '@/store/player/hook'
import { useBufferProgress } from '@/plugins/player'
import { fontSize, spacing } from '@/theme/apple'
import { createStyle } from '@/utils/tools'

const TIME_COLOR = 'rgba(255, 255, 255, 0.58)'

/** 可拖动进度条 + 11pt 等宽数字时间标签 */
export default memo(() => {
  const { maxPlayTimeStr, nowPlayTimeStr, progress, maxPlayTime } = useProgress()
  const buffered = useBufferProgress()
  const statusText = useStatusText()

  return (
    <View style={styles.container}>
      <Progress progress={progress} duration={maxPlayTime} buffered={buffered} />
      <View style={styles.info}>
        <Text size={fontSize.caption2} color={TIME_COLOR} style={styles.time}>{nowPlayTimeStr}</Text>
        <Text size={fontSize.caption2} color={TIME_COLOR} numberOfLines={1} style={styles.status}>{statusText}</Text>
        <Text size={fontSize.caption2} color={TIME_COLOR} style={styles.time}>{maxPlayTimeStr}</Text>
      </View>
    </View>
  )
})

const styles = createStyle({
  container: {
    marginTop: spacing.sm,
  },
  info: {
    flexDirection: 'row',
    alignItems: 'center',
    marginTop: -2,
  },
  time: {
    fontVariant: ['tabular-nums'],
    fontWeight: '500',
  },
  status: {
    flex: 1,
    textAlign: 'center',
    paddingHorizontal: spacing.sm,
  },
})
