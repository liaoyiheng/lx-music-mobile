import { memo } from 'react'
import { View, type StyleProp, type ViewStyle } from 'react-native'
import ActionButton from '@/components/apple/ActionButton'
import songlistState from '@/store/songlist/state'
import { useI18n } from '@/lang'
import { spacing } from '@/theme/apple'
import { createStyle } from '@/utils/tools'
import { handlePlay, handleShufflePlay } from './listAction'
import { useListInfo } from './state'

/**
 * 歌单详情头部的「播放 / 随机播放」按钮组
 * 播放 = 原版「播放全部」；随机播放 = 打乱后播放这一次，不改全局播放模式
 */
export default memo(({ style }: { style?: StyleProp<ViewStyle> }) => {
  const t = useI18n()
  const info = useListInfo()

  const handlePlayAll = () => {
    if (!songlistState.listDetailInfo.info.name) return
    void handlePlay(info.id, info.source, songlistState.listDetailInfo.list)
  }

  const handleShuffle = () => {
    if (!songlistState.listDetailInfo.info.name) return
    void handleShufflePlay(info.id, info.source, songlistState.listDetailInfo.list)
  }

  return (
    <View style={[styles.container, style]}>
      <ActionButton icon="play" label={t('apple_play')} onPress={handlePlayAll} />
      <View style={styles.gap} />
      <ActionButton icon="list-random" label={t('apple_shuffle')} onPress={handleShuffle} />
    </View>
  )
})

const styles = createStyle({
  container: {
    flexDirection: 'row',
    flexGrow: 0,
    flexShrink: 0,
  },
  gap: {
    width: spacing.md,
  },
})
