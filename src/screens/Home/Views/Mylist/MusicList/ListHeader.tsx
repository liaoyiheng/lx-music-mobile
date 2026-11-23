import { memo, useMemo } from 'react'
import { TouchableOpacity, View } from 'react-native'
import { Icon } from '@/components/common/Icon'
import Text from '@/components/common/Text'
import { useI18n } from '@/lang'
import { LIST_IDS } from '@/config/constant'
import listState from '@/store/list/state'
import { useActiveListId, useMyList } from '@/store/list/hook'
import { useSettingValue } from '@/store/setting/hook'
import { getListMusics, setTempList } from '@/core/list'
import { playList } from '@/core/player/player'
import { useApple, fontSize, fontWeight, pagePadding, radius, spacing } from '@/theme/apple'
import { createStyle } from '@/utils/tools'
import { shuffleList } from '@/utils/shuffle'

/** 头部固定高度（列表的 getItemLayout 依赖这个值） */
export const LIST_HEADER_HEIGHT = 124

/**
 * 播放 = 从第一首顺序播放；
 * 随机播放 = 把列表打乱后放进临时列表播放这一次，不改全局播放模式，也不改动原列表顺序
 */
const handlePlay = async(shuffle: boolean) => {
  const listId = listState.activeListId
  if (!listId) return
  const list = await getListMusics(listId)
  if (!list.length) return
  if (shuffle) {
    await setTempList(`mylist__${listId}`, shuffleList(list) as LX.Music.MusicInfoOnline[])
    void playList(LIST_IDS.TEMP, 0)
  } else {
    void playList(listId, 0)
  }
}

const ActionBtn = ({ icon, label, onPress }: { icon: string, label: string, onPress: () => void }) => {
  const apple = useApple()
  return (
    <TouchableOpacity style={{ ...styles.btn, backgroundColor: apple.fill }} activeOpacity={0.6} onPress={onPress}>
      <Icon name={icon} size={15} color={apple.accent} />
      <Text style={styles.btnText} size={fontSize.callout} color={apple.accent}>{label}</Text>
    </TouchableOpacity>
  )
}

/**
 * 歌曲页头部：当前列表名（大标题）+ 播放 / 随机播放
 */
export default memo(() => {
  const apple = useApple()
  const t = useI18n()
  const activeListId = useActiveListId()
  const allList = useMyList()
  const langId = useSettingValue('common.langId')

  const name = useMemo(() => {
    switch (activeListId) {
      case LIST_IDS.TEMP: return global.i18n.t('list_name_temp')
      case LIST_IDS.DEFAULT: return global.i18n.t('list_name_default')
      case LIST_IDS.LOVE: return global.i18n.t('list_name_love')
      default: return allList.find(l => l.id === activeListId)?.name ?? ''
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [activeListId, allList, langId])

  return (
    <View style={styles.container}>
      <Text style={styles.title} size={fontSize.title1} color={apple.label} numberOfLines={1}>{name}</Text>
      <View style={styles.btns}>
        <ActionBtn icon="play" label={t('apple_play')} onPress={() => { void handlePlay(false) }} />
        <View style={styles.gap} />
        <ActionBtn icon="list-random" label={t('apple_shuffle')} onPress={() => { void handlePlay(true) }} />
      </View>
    </View>
  )
})

const styles = createStyle({
  container: {
    height: LIST_HEADER_HEIGHT,
    paddingHorizontal: pagePadding,
    paddingTop: spacing.xs,
    overflow: 'hidden',
  },
  title: {
    fontWeight: fontWeight.bold,
  },
  btns: {
    flexDirection: 'row',
    marginTop: spacing.lg,
  },
  btn: {
    flex: 1,
    height: 44,
    borderRadius: radius.medium,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
  },
  btnText: {
    marginLeft: spacing.sm,
    fontWeight: fontWeight.semibold,
  },
  gap: {
    width: spacing.md,
  },
})
