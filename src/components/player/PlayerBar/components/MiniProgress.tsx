// Added for Apple Music style UI (based on lyswhut/lx-music-mobile, Apache-2.0)
import { memo } from 'react'
import { View } from 'react-native'
import Progress, { ProgressPlain } from '@/components/player/Progress'
import { useProgress } from '@/store/player/hook'
import { useBufferProgress } from '@/plugins/player'
import { useSettingValue } from '@/store/setting/hook'
import { spacing } from '@/theme/apple'
import { createStyle } from '@/utils/tools'

/** 进度线粗细 */
const LINE_HEIGHT = spacing.xxs
/** 线上方的可拖动区域 */
const HIT_PADDING = spacing.sm
const BAR_RADIUS = 14

/** 卡片底部的细进度线；开启「允许拖动进度条」时可拖动调整进度 */
export default memo(({ autoUpdate }: { autoUpdate: boolean }) => {
  const { progress, maxPlayTime } = useProgress(autoUpdate)
  const buffered = useBufferProgress()
  const allowProgressBarSeek = useSettingValue('common.allowProgressBarSeek')

  return (
    <View style={styles.container} pointerEvents={allowProgressBarSeek ? 'box-none' : 'none'}>
      {
        allowProgressBarSeek
          ? <Progress progress={progress} duration={maxPlayTime} buffered={buffered} paddingTop={HIT_PADDING} />
          : <ProgressPlain progress={progress} duration={maxPlayTime} buffered={buffered} paddingTop={HIT_PADDING} />
      }
    </View>
  )
})

const styles = createStyle({
  container: {
    position: 'absolute',
    left: BAR_RADIUS,
    right: BAR_RADIUS,
    bottom: 0,
    height: LINE_HEIGHT + HIT_PADDING,
  },
})
