// Added for Apple Music iOS 26 Liquid Glass UI (based on lyswhut/lx-music-mobile, Apache-2.0)
import { memo } from 'react'
import { View } from 'react-native'
import Progress, { ProgressPlain } from '@/components/player/Progress'
import { useProgress } from '@/store/player/hook'
import { useBufferProgress } from '@/plugins/player'
import { useSettingValue } from '@/store/setting/hook'
import { spacing } from '@/theme/apple'
import { createStyle } from '@/utils/tools'

const LINE_HEIGHT = spacing.xxs
const HIT_PADDING = spacing.sm
const PILL_INSET = 24

/** 药丸底部的细进度线；左右缩进 24dp 贴合全圆角药丸曲率 */
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
    left: PILL_INSET,
    right: PILL_INSET,
    bottom: 0,
    height: LINE_HEIGHT + HIT_PADDING,
  },
})
