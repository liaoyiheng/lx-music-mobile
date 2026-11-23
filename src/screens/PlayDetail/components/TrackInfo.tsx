// Added for Apple Music style UI (based on lyswhut/lx-music-mobile, Apache-2.0)
import { memo, useRef } from 'react'
import { TouchableOpacity, View } from 'react-native'
import Text from '@/components/common/Text'
import { Icon } from '@/components/common/Icon'
import MusicAddModal, { type MusicAddModalType } from '@/components/MusicAddModal'
import playerState from '@/store/player/state'
import { usePlayerMusicInfo } from '@/store/player/hook'
import { useApple, fontSize, fontWeight, radius, spacing } from '@/theme/apple'
import { createStyle } from '@/utils/tools'

const ADD_BTN_SIZE = 32

/** 添加到列表（圆形浅色按钮） */
const MusicAddBtn = () => {
  const apple = useApple()
  const musicAddModalRef = useRef<MusicAddModalType>(null)

  const handleShow = () => {
    const musicInfo = playerState.playMusicInfo.musicInfo
    if (!musicInfo) return
    musicAddModalRef.current?.show({
      musicInfo: 'progress' in musicInfo ? musicInfo.metadata.musicInfo : musicInfo,
      isMove: false,
      listId: playerState.playMusicInfo.listId!,
    })
  }

  return (
    <>
      <TouchableOpacity style={{ ...styles.addBtn, backgroundColor: apple.fill }} activeOpacity={0.5} onPress={handleShow} accessibilityRole="button">
        <Icon name="add-music" color={apple.label} size={16} />
      </TouchableOpacity>
      <MusicAddModal ref={musicAddModalRef} />
    </>
  )
}

/** 歌名（粗体）+ 歌手，左对齐；右侧添加到列表 */
export default memo(() => {
  const apple = useApple()
  const musicInfo = usePlayerMusicInfo()

  return (
    <View style={styles.container}>
      <View style={styles.texts}>
        <Text numberOfLines={1} size={fontSize.title3} color={apple.label} style={styles.title}>{musicInfo.name}</Text>
        <Text numberOfLines={1} size={fontSize.title3} color={apple.secondaryLabel}>{musicInfo.singer}</Text>
      </View>
      <MusicAddBtn />
    </View>
  )
})

const styles = createStyle({
  container: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  texts: {
    flex: 1,
    paddingRight: spacing.md,
  },
  title: {
    fontWeight: fontWeight.semibold,
  },
  addBtn: {
    width: ADD_BTN_SIZE,
    height: ADD_BTN_SIZE,
    borderRadius: radius.pill,
    justifyContent: 'center',
    alignItems: 'center',
  },
})
