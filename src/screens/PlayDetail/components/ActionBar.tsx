// Added for Apple Music style UI (based on lyswhut/lx-music-mobile, Apache-2.0)
import { memo, useMemo, useRef } from 'react'
import { TouchableOpacity, View } from 'react-native'
import { Icon } from '@/components/common/Icon'
import DesktopLyricEnable, { type DesktopLyricEnableType } from '@/components/DesktopLyricEnable'
import TimeoutExitEditModal, { type TimeoutExitEditModalType, useTimeInfo } from '@/components/TimeoutExitEditModal'
import { MUSIC_TOGGLE_MODE_LIST, MUSIC_TOGGLE_MODE } from '@/config/constant'
import { useSettingValue } from '@/store/setting/hook'
import settingState from '@/store/setting/state'
import commonState from '@/store/common/state'
import { updateSetting } from '@/core/common'
import { toggleDesktopLyricLock } from '@/core/desktopLyric'
import { navigations } from '@/navigation'
import { useI18n } from '@/lang'
import { useApple, size, spacing } from '@/theme/apple'
import { createStyle, toast } from '@/utils/tools'

const Btn = ({ icon, active = false, onPress, onLongPress }: {
  icon: string
  active?: boolean
  onPress: () => void
  onLongPress?: () => void
}) => {
  const apple = useApple()
  return (
    <TouchableOpacity style={styles.btn} activeOpacity={0.5} onPress={onPress} onLongPress={onLongPress} accessibilityRole="button">
      <Icon name={icon} color={active ? apple.accent : apple.secondaryLabel} size={20} />
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
      <Btn icon={enabledLyric ? 'lyric-on' : 'lyric-off'} active={enabledLyric} onPress={update} onLongPress={updateLock} />
      <DesktopLyricEnable ref={desktopLyricEnableRef} />
    </>
  )
})

const MODE_INFO = {
  [MUSIC_TOGGLE_MODE.listLoop]: { icon: 'list-loop', name: 'play_list_loop' },
  [MUSIC_TOGGLE_MODE.random]: { icon: 'list-random', name: 'play_list_random' },
  [MUSIC_TOGGLE_MODE.list]: { icon: 'list-order', name: 'play_list_order' },
  [MUSIC_TOGGLE_MODE.singleLoop]: { icon: 'single-loop', name: 'play_single_loop' },
} as const
const DEFAULT_MODE_INFO = { icon: 'single', name: 'play_single' } as const
const getModeInfo = (mode: string) => MODE_INFO[mode as keyof typeof MODE_INFO] ?? DEFAULT_MODE_INFO

/** 播放模式：依次切换并提示 */
const PlayModeBtn = memo(() => {
  const togglePlayMethod = useSettingValue('player.togglePlayMethod')
  const t = useI18n()
  const icon = useMemo(() => getModeInfo(togglePlayMethod).icon, [togglePlayMethod])

  const toggleNextPlayMode = () => {
    let index = MUSIC_TOGGLE_MODE_LIST.indexOf(togglePlayMethod)
    if (++index >= MUSIC_TOGGLE_MODE_LIST.length) index = 0
    const mode = MUSIC_TOGGLE_MODE_LIST[index]
    updateSetting({ 'player.togglePlayMethod': mode })
    toast(t(getModeInfo(mode).name))
  }

  return <Btn icon={icon} onPress={toggleNextPlayMode} />
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
      <Btn icon="music_time" active={timeInfo.active} onPress={handleShow} />
      <TimeoutExitEditModal ref={modalRef} timeInfo={timeInfo} />
    </>
  )
})

const handleShowCommentScreen = () => {
  navigations.pushCommentScreen(commonState.componentIds.playDetail!)
}

/** 底部功能行：桌面歌词、播放模式、定时停止、评论 */
export default memo(() => {
  return (
    <View style={styles.container}>
      <DesktopLyricBtn />
      <PlayModeBtn />
      <TimeoutExitBtn />
      <Btn icon="comment" onPress={handleShowCommentScreen} />
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
    width: size.hitTarget,
    height: size.hitTarget,
    justifyContent: 'center',
    alignItems: 'center',
  },
})
