// Modified for Apple Music style UI. Original: lyswhut/lx-music-mobile (Apache-2.0)
import { TouchableOpacity } from 'react-native'
import { navigations } from '@/navigation'
import { usePlayerMusicInfo } from '@/store/player/hook'
import { useSettingValue } from '@/store/setting/hook'
import commonState from '@/store/common/state'
import playerState from '@/store/player/state'
import Text from '@/components/common/Text'
import { LIST_IDS } from '@/config/constant'
import { createStyle, formatMusicName } from '@/utils/tools'
import { useApple } from '@/theme/apple'

export default ({ isHome }: { isHome: boolean }) => {
  const musicInfo = usePlayerMusicInfo()
  const downloadFileName = useSettingValue('download.fileName')
  const apple = useApple()

  const handlePress = () => {
    if (!musicInfo.id) return
    navigations.pushPlayDetailScreen(commonState.componentIds.home!)
  }

  const handleLongPress = () => {
    const listId = playerState.playMusicInfo.listId
    if (!listId || listId == LIST_IDS.DOWNLOAD) return
    global.app_event.jumpListPosition()
  }

  const title = musicInfo.id
    ? musicInfo.singer
      ? formatMusicName(downloadFileName, musicInfo.name, musicInfo.singer)
      : musicInfo.name
    : ''
  return (
    <TouchableOpacity style={styles.container} onLongPress={handleLongPress} onPress={handlePress} activeOpacity={0.7} >
      <Text color={apple.label} size={15} style={styles.title} numberOfLines={1}>{title}</Text>
    </TouchableOpacity>
  )
}

const styles = createStyle({
  container: {
    width: '100%',
    paddingHorizontal: 2,
  },
  title: {
    fontWeight: '600',
  },
})
