import { createList, setTempList } from '@/core/list'
import { playList } from '@/core/player/player'
import { getListDetail, getListDetailAll } from '@/core/songlist'
import { LIST_IDS } from '@/config/constant'
import listState from '@/store/list/state'
import syncSourceList from '@/core/syncSourceList'
import { confirmDialog, toMD5, toast } from '@/utils/tools'
import { type Source } from '@/store/songlist/state'
import { shuffleList } from '@/utils/shuffle'

const getListId = (id: string, source: LX.OnlineSource) => `${source}__${id}`

export const handlePlay = async(id: string, source: Source, list?: LX.Music.MusicInfoOnline[], index = 0) => {
  const listId = getListId(id, source)
  let isPlayingList = false
  // console.log(list)
  if (!list?.length) list = (await getListDetail(id, source, 1)).list
  if (list?.length) {
    await setTempList(listId, [...list])
    void playList(LIST_IDS.TEMP, index)
    isPlayingList = true
  }
  const fullList = await getListDetailAll(source, id)
  if (!fullList.length) return
  if (isPlayingList) {
    if (listState.tempListMeta.id == listId) {
      await setTempList(listId, [...fullList])
    }
  } else {
    await setTempList(listId, [...fullList])
    void playList(LIST_IDS.TEMP, index)
  }
}

export const handleCollect = async(id: string, source: Source, name: string) => {
  const listId = getListId(id, source)

  const targetList = listState.userList.find(l => l.sourceListId == listId)
  if (targetList) {
    const confirm = await confirmDialog({
      message: global.i18n.t('duplicate_list_tip', { name: targetList.name }),
      cancelButtonText: global.i18n.t('list_import_part_button_cancel'),
      confirmButtonText: global.i18n.t('confirm_button_text'),
    })
    if (!confirm) return
    void syncSourceList(targetList)
    return
  }

  const list = await getListDetailAll(source, id)
  await createList({
    name,
    id: `${source}_${toMD5(listId)}`,
    list,
    source,
    sourceListId: id,
  })
  toast(global.i18n.t('collect_success'))
}

/**
 * 随机播放（只对这一次生效）：把歌单打乱后从第一首开始顺序播放，不修改全局播放模式
 */
export const handleShufflePlay = async(id: string, source: Source, list?: LX.Music.MusicInfoOnline[]) => {
  const listId = getListId(id, source)
  let firstId: string | null = null
  if (!list?.length) list = (await getListDetail(id, source, 1)).list
  if (list?.length) {
    const shuffled = shuffleList(list)
    firstId = shuffled[0].id
    await setTempList(listId, shuffled)
    void playList(LIST_IDS.TEMP, 0)
  }
  const fullList = await getListDetailAll(source, id)
  if (!fullList.length) return
  if (firstId) {
    // 用户已切到别的列表就不再覆盖
    if (listState.tempListMeta.id != listId) return
    // 完整列表到达后重新打乱，正在播的那首保持在最前
    const first = fullList.find(m => m.id == firstId)
    const rest = shuffleList(fullList.filter(m => m.id != firstId))
    await setTempList(listId, first ? [first, ...rest] : rest)
  } else {
    await setTempList(listId, shuffleList(fullList))
    void playList(LIST_IDS.TEMP, 0)
  }
}
