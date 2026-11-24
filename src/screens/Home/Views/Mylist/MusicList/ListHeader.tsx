import { memo, useEffect, useMemo, useState } from 'react'
import { View } from 'react-native'
import ActionButton from '@/components/apple/ActionButton'
import Text from '@/components/common/Text'
import { useI18n } from '@/lang'
import { LIST_IDS } from '@/config/constant'
import listState from '@/store/list/state'
import { useActiveListId, useMyList } from '@/store/list/hook'
import { useSettingValue } from '@/store/setting/hook'
import { getListMusics, setTempList } from '@/core/list'
import { playList } from '@/core/player/player'
import { useApple, fontSize, fontWeight, pagePadding, size, spacing } from '@/theme/apple'
import { createStyle } from '@/utils/tools'
import { shuffleList } from '@/utils/shuffle'

/** 头部固定高度（列表的 getItemLayout 依赖这个值） */
export const LIST_HEADER_HEIGHT = 140

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

/**
 * 歌曲页头部：居中清晰歌单名 + 歌曲数 + 并排等宽播放 / 随机播放 ActionButton
 */
export default memo(() => {
  const apple = useApple()
  const t = useI18n()
  const activeListId = useActiveListId()
  const allList = useMyList()
  const langId = useSettingValue('common.langId')
  const [songCount, setSongCount] = useState(0)

  useEffect(() => {
    let cancelled = false
    const updateCount = () => {
      if (!activeListId) {
        setSongCount(0)
        return
      }
      void getListMusics(activeListId).then((list) => {
        if (!cancelled) setSongCount(list.length)
      })
    }
    updateCount()

    const handleUpdate = (ids: string[]) => {
      if (ids.includes(activeListId)) updateCount()
    }
    global.app_event.on('myListMusicUpdate', handleUpdate)
    return () => {
      cancelled = true
      global.app_event.off('myListMusicUpdate', handleUpdate)
    }
  }, [activeListId])

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
      <Text style={styles.title} size={fontSize.title1} color={apple.label} numberOfLines={1}>
        {name}
      </Text>
      <Text style={styles.count} size={fontSize.footnote} color={apple.secondaryLabel} numberOfLines={1}>
        {t('apple_library_songs_count', { num: songCount })}
      </Text>
      <View style={styles.btns}>
        <ActionButton icon="play" ionIcon="play" label={t('apple_play')} onPress={() => { void handlePlay(false) }} />
        <View style={styles.gap} />
        <ActionButton icon="list-random" ionIcon="shuffle" label={t('apple_shuffle')} onPress={() => { void handlePlay(true) }} />
      </View>
    </View>
  )
})

const styles = createStyle({
  container: {
    height: LIST_HEADER_HEIGHT,
    paddingHorizontal: pagePadding,
    paddingTop: spacing.xs,
    alignItems: 'center',
    overflow: 'hidden',
  },
  title: {
    fontWeight: fontWeight.bold,
    letterSpacing: -0.45,
    textAlign: 'center',
  },
  count: {
    marginTop: 4,
    textAlign: 'center',
    fontWeight: fontWeight.medium,
  },
  btns: {
    flexDirection: 'row',
    marginTop: spacing.lg,
    width: '100%',
    maxWidth: size.actionBarMaxWidth,
  },
  gap: {
    width: spacing.md,
  },
})
