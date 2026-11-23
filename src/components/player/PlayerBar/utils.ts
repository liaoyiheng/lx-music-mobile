// Added for Apple Music style UI (based on lyswhut/lx-music-mobile, Apache-2.0)
import { navigations } from '@/navigation'
import commonState from '@/store/common/state'
import playerState from '@/store/player/state'
import { LIST_IDS } from '@/config/constant'

/** 打开全屏播放页 */
export const openPlayDetail = () => {
  navigations.pushPlayDetailScreen(commonState.componentIds.home!)
}

/** 长按：定位到当前播放歌曲所在列表位置（下载列表除外） */
export const jumpToPlayingPosition = () => {
  const listId = playerState.playMusicInfo.listId
  if (!listId || listId == LIST_IDS.DOWNLOAD) return
  global.app_event.jumpListPosition()
}
