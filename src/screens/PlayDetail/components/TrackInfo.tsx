// Added for Apple Music style UI (based on lyswhut/lx-music-mobile, Apache-2.0)
import { memo, useRef } from 'react'
import { TouchableOpacity, View } from 'react-native'
import Text from '@/components/common/Text'
import { IonIcon } from '@/components/common/Icon'
import MusicAddModal, { type MusicAddModalType } from '@/components/MusicAddModal'
import playerState from '@/store/player/state'
import { usePlayerMusicInfo } from '@/store/player/hook'
import { fontSize, fontWeight, radius, spacing } from '@/theme/apple'
import { createStyle } from '@/utils/tools'

const ADD_BTN_SIZE = 32

/** 添加到歌单（半透明白圆形按钮） */
export const MusicAddBtn = memo(() => {
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
      <TouchableOpacity style={styles.addBtn} activeOpacity={0.5} onPress={handleShow} accessibilityRole="button">
        <IonIcon name="add" color="#FFFFFF" size={20} />
      </TouchableOpacity>
      <MusicAddModal ref={musicAddModalRef} />
    </>
  )
})

/** 歌名（纯白粗体）+ 歌手（半透明白），左对齐；右侧添加到歌单 */
export default memo(() => {
  const musicInfo = usePlayerMusicInfo()

  return (
    <View style={styles.container}>
      <View style={styles.texts}>
        <Text numberOfLines={1} size={fontSize.title3} color="#FFFFFF" style={styles.title}>{musicInfo.name}</Text>
        <Text numberOfLines={1} size={fontSize.body} color="rgba(255, 255, 255, 0.68)" style={styles.singer}>{musicInfo.singer}</Text>
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
    fontWeight: fontWeight.bold,
  },
  singer: {
    marginTop: 2,
  },
  addBtn: {
    width: ADD_BTN_SIZE,
    height: ADD_BTN_SIZE,
    borderRadius: radius.pill,
    backgroundColor: 'rgba(255, 255, 255, 0.16)',
    justifyContent: 'center',
    alignItems: 'center',
  },
})
