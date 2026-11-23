// Added for Apple Music style UI (based on lyswhut/lx-music-mobile, Apache-2.0)
import { memo } from 'react'
import { View } from 'react-native'
import Progress from '@/components/player/ProgressBar'
import Text from '@/components/common/Text'
import { useProgress, useStatusText } from '@/store/player/hook'
import { useBufferProgress } from '@/plugins/player'
import { useApple, fontSize, spacing } from '@/theme/apple'
import { createStyle } from '@/utils/tools'

/** 可拖动进度条 + 已播放 / 总时长，中间显示播放状态（加载中、出错等） */
export default memo(() => {
  const apple = useApple()
  const { maxPlayTimeStr, nowPlayTimeStr, progress, maxPlayTime } = useProgress()
  const buffered = useBufferProgress()
  const statusText = useStatusText()

  return (
    <View style={styles.container}>
      <Progress progress={progress} duration={maxPlayTime} buffered={buffered} />
      <View style={styles.info}>
        <Text size={fontSize.caption1} color={apple.secondaryLabel}>{nowPlayTimeStr}</Text>
        <Text size={fontSize.caption1} color={apple.secondaryLabel} numberOfLines={1} style={styles.status}>{statusText}</Text>
        <Text size={fontSize.caption1} color={apple.secondaryLabel}>{maxPlayTimeStr}</Text>
      </View>
    </View>
  )
})

const styles = createStyle({
  container: {
    marginTop: spacing.md,
  },
  info: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  status: {
    flex: 1,
    textAlign: 'center',
    paddingHorizontal: spacing.sm,
  },
})
