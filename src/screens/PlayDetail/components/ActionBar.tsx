// Added for Apple Music style UI (based on lyswhut/lx-music-mobile, Apache-2.0)
import { memo, useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { TouchableOpacity, View } from 'react-native'
import { IonIcon } from '@/components/common/Icon'
import DesktopLyricEnable, { type DesktopLyricEnableType } from '@/components/DesktopLyricEnable'
import TimeoutExitEditModal, { type TimeoutExitEditModalType, useTimeInfo } from '@/components/TimeoutExitEditModal'
import { LIST_IDS, MUSIC_TOGGLE_MODE_LIST, MUSIC_TOGGLE_MODE } from '@/config/constant'
import { useSettingValue } from '@/store/setting/hook'
import { usePlayerMusicInfo } from '@/store/player/hook'
import settingState from '@/store/setting/state'
import commonState from '@/store/common/state'
import playerState from '@/store/player/state'
import { updateSetting } from '@/core/common'
import { addListMusics, getListMusics, removeListMusics } from '@/core/list'
import { toggleDesktopLyricLock } from '@/core/desktopLyric'
import { navigations } from '@/navigation'
import { useI18n } from '@/lang'
import { spacing } from '@/theme/apple'
import { createStyle, toast } from '@/utils/tools'

const ICON_INACTIVE = 'rgba(255, 255, 255, 0.68)'
const ICON_ACTIVE = '#FFFFFF'
const LOVE_ACTIVE = '#FF375F'

const Btn = ({ icon, active = false, activeColor = ICON_ACTIVE, pillBg = false, onPress, onLongPress }: {
  icon: string
  active?: boolean
  activeColor?: string
  pillBg?: boolean
  onPress: () => void
  onLongPress?: () => void
}) => {
  return (
    <TouchableOpacity
      style={[styles.btn, pillBg && active ? styles.btnPillActive : null]}
      activeOpacity={0.5}
      onPress={onPress}
      onLongPress={onLongPress}
      accessibilityRole="button"
    >
      <IonIcon name={icon} color={active ? activeColor : ICON_INACTIVE} size={22} />
    </TouchableOpacity>
  )
}

/** 桌面歌词：点击开关，长按锁定 / 解锁 */
const DesktopLyricBtn = memo(() => {
  const enabledLyric = useSettingValue('desktopLyric.enable')
  const desktopLyricEnableRef = useRef<DesktopLyricEnableType>(null)
  const update = () => {
    desktopLyricEnableRef.current?.setEnabled(!enabledLyric)
  }
  const updateLock = () => {
    const isLock = !settingState.setting['desktopLyric.isLock']
    void toggleDesktopLyricLock(isLock).then(() => {
      updateSetting({ 'desktopLyric.isLock': isLock })
    })
  }
  return (
    <>
      <Btn icon={enabledLyric ? 'reader' : 'reader-outline'} active={enabledLyric} onPress={update} onLongPress={updateLock} />
      <DesktopLyricEnable ref={desktopLyricEnableRef} />
    </>
  )
})

const MODE_INFO = {
  [MUSIC_TOGGLE_MODE.listLoop]: { icon: 'repeat', name: 'play_list_loop' },
  [MUSIC_TOGGLE_MODE.random]: { icon: 'shuffle', name: 'play_list_random' },
  [MUSIC_TOGGLE_MODE.list]: { icon: 'reorder-three-outline', name: 'play_list_order' },
  [MUSIC_TOGGLE_MODE.singleLoop]: { icon: 'repeat-outline', name: 'play_single_loop' },
} as const
const DEFAULT_MODE_INFO = { icon: 'repeat-outline', name: 'play_single' } as const
const getModeInfo = (mode: string) => MODE_INFO[mode as keyof typeof MODE_INFO] ?? DEFAULT_MODE_INFO

/** 播放模式：依次切换并提示 */
const PlayModeBtn = memo(() => {
  const togglePlayMethod = useSettingValue('player.togglePlayMethod')
  const t = useI18n()
  const icon = useMemo(() => getModeInfo(togglePlayMethod).icon, [togglePlayMethod])
  const isSpecial = togglePlayMethod == MUSIC_TOGGLE_MODE.random || togglePlayMethod == MUSIC_TOGGLE_MODE.singleLoop

  const toggleNextPlayMode = () => {
    let index = MUSIC_TOGGLE_MODE_LIST.indexOf(togglePlayMethod)
    if (++index >= MUSIC_TOGGLE_MODE_LIST.length) index = 0
    const mode = MUSIC_TOGGLE_MODE_LIST[index]
    updateSetting({ 'player.togglePlayMethod': mode })
    toast(t(getModeInfo(mode).name))
  }

  return <Btn icon={icon} active={isSpecial} onPress={toggleNextPlayMode} />
})

/** 定时停止播放 */
const TimeoutExitBtn = memo(() => {
  const modalRef = useRef<TimeoutExitEditModalType>(null)
  const timeInfo = useTimeInfo()
  const handleShow = () => {
    modalRef.current?.show()
  }
  return (
    <>
      <Btn icon="timer-outline" active={timeInfo.active} onPress={handleShow} />
      <TimeoutExitEditModal ref={modalRef} timeInfo={timeInfo} />
    </>
  )
})

/** 收藏到「我喜欢的音乐」 */
const LoveBtn = memo(() => {
  const musicInfo = usePlayerMusicInfo()
  const [isLoved, setIsLoved] = useState(false)

  const syncLoveState = useCallback(() => {
    if (!musicInfo.id) {
      setIsLoved(false)
      return
    }
    void getListMusics(LIST_IDS.LOVE).then(list => {
      setIsLoved(list.some(m => m.id == musicInfo.id))
    })
  }, [musicInfo.id])

  useEffect(() => {
    syncLoveState()
    const handleUpdate = (ids: string[]) => {
      if (ids.includes(LIST_IDS.LOVE)) syncLoveState()
    }
    global.app_event.on('myListMusicUpdate', handleUpdate)
    return () => {
      global.app_event.off('myListMusicUpdate', handleUpdate)
    }
  }, [syncLoveState])

  const handleToggle = () => {
    const rawInfo = playerState.playMusicInfo.musicInfo
    if (!rawInfo) return
    const targetInfo = 'progress' in rawInfo ? rawInfo.metadata.musicInfo : rawInfo
    if (isLoved) {
      void removeListMusics(LIST_IDS.LOVE, [targetInfo.id])
      setIsLoved(false)
    } else {
      void addListMusics(LIST_IDS.LOVE, [targetInfo], settingState.setting['list.addMusicLocationType'])
      setIsLoved(true)
    }
  }

  return <Btn icon={isLoved ? 'heart' : 'heart-outline'} active={isLoved} activeColor={LOVE_ACTIVE} onPress={handleToggle} />
})

const handleShowCommentScreen = () => {
  navigations.pushCommentScreen(commonState.componentIds.playDetail!)
}

/** 底部功能行：歌词切换（竖屏）、桌面歌词、播放模式、定时停止、评论、收藏 */
export default memo(({ lyricActive = false, onToggleLyric }: {
  lyricActive?: boolean
  onToggleLyric?: () => void
}) => {
  return (
    <View style={styles.container}>
      {onToggleLyric ? (
        <Btn
          icon={lyricActive ? 'chatbox-ellipses' : 'chatbox-ellipses-outline'}
          active={lyricActive}
          pillBg
          onPress={onToggleLyric}
        />
      ) : null}
      <DesktopLyricBtn />
      <PlayModeBtn />
      <TimeoutExitBtn />
      <Btn icon="chatbubble-outline" onPress={handleShowCommentScreen} />
      <LoveBtn />
    </View>
  )
})

const styles = createStyle({
  container: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-around',
    paddingTop: spacing.xs,
  },
  btn: {
    width: 40,
    height: 36,
    borderRadius: 10,
    justifyContent: 'center',
    alignItems: 'center',
  },
  btnPillActive: {
    backgroundColor: 'rgba(255, 255, 255, 0.22)',
  },
})
