import { useLrcPlay } from '@/plugins/lyric'
import { useIsPlay, useStatusText } from '@/store/player/hook'
import Text from '@/components/common/Text'
import { useApple } from '@/theme/apple'

export default ({ autoUpdate }: { autoUpdate: boolean }) => {
  const { text } = useLrcPlay(autoUpdate)
  const statusText = useStatusText()
  const isPlay = useIsPlay()
  const apple = useApple()

  const status = isPlay ? text : statusText

  return <Text numberOfLines={1} size={12} color={apple.secondaryLabel}>{status}</Text>
}
