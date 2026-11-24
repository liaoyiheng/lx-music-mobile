// Modified for Apple Music iOS 26 Liquid Glass UI. Original: lyswhut/lx-music-mobile (Apache-2.0)
import { forwardRef, useCallback, useImperativeHandle, useMemo, useRef, useState } from 'react'
import { Platform, ScrollView, StyleSheet, Switch, TouchableOpacity, View } from 'react-native'
import { CoverArt, GlassCircleButton, GlassSurface } from '@/components/apple/LiquidGlass'
import { IonIcon } from '@/components/common/Icon'
import Modal, { type ModalType } from '@/components/common/Modal'
import Text from '@/components/common/Text'
import { MUSIC_TOGGLE_MODE, MUSIC_TOGGLE_MODE_LIST } from '@/config/constant'
import { updateSetting } from '@/core/common'
import { toggleDesktopLyricLock } from '@/core/desktopLyric'
import { toggleRoma, toggleTranslation } from '@/core/lyric'
import { useI18n } from '@/lang'
import { navigations } from '@/navigation'
import commonState from '@/store/common/state'
import { usePlayerMusicInfo } from '@/store/player/hook'
import { useSettingValue } from '@/store/setting/hook'
import settingState from '@/store/setting/state'
import { fontSize, fontWeight, pagePadding, radius, spacing } from '@/theme/apple'
import { createStyle, toast } from '@/utils/tools'

import SettingLrcAlign from './settings/SettingLrcAlign'
import SettingLrcFontSize from './settings/SettingLrcFontSize'
import SettingLyricProgress from './settings/SettingLyricProgress'
import SettingPlaybackRate from './settings/SettingPlaybackRate'
import SettingVolume from './settings/SettingVolume'
import settingStyles, {
  GLASS_DARK_ACCENT,
  GLASS_DARK_LABEL,
  GLASS_DARK_SUB_LABEL,
  GLASS_DARK_SWITCH_OFF,
  GLASS_DARK_SWITCH_ON,
} from './settings/style'

export interface SettingPopupProps {
  direction: 'vertical' | 'horizontal'
  timeoutActive?: boolean
  onToggleDesktopLyric?: (enable: boolean) => void
  onShowTimeoutExit?: () => void
  onShowMusicAdd?: () => void
}

export interface SettingPopupType {
  show: () => void
  hide: () => void
}

const MODE_INFO: Record<string, { icon: string, name: string }> = {
  [MUSIC_TOGGLE_MODE.listLoop]: { icon: 'repeat', name: 'play_list_loop' },
  [MUSIC_TOGGLE_MODE.random]: { icon: 'shuffle', name: 'play_list_random' },
  [MUSIC_TOGGLE_MODE.list]: { icon: 'reorder-three-outline', name: 'play_list_order' },
  [MUSIC_TOGGLE_MODE.singleLoop]: { icon: 'repeat-outline', name: 'play_single_loop' },
  [MUSIC_TOGGLE_MODE.none]: { icon: 'remove-circle-outline', name: 'play_single' },
}

export default forwardRef<SettingPopupType, SettingPopupProps>(({
  direction,
  timeoutActive = false,
  onToggleDesktopLyric,
  onShowTimeoutExit,
  onShowMusicAdd,
}, ref) => {
  const [visible, setVisible] = useState(false)
  const modalRef = useRef<ModalType>(null)
  const t = useI18n()
  const musicInfo = usePlayerMusicInfo()

  const desktopLyricEnable = useSettingValue('desktopLyric.enable')
  const desktopLyricLock = useSettingValue('desktopLyric.isLock')
  const togglePlayMethod = useSettingValue('player.togglePlayMethod')
  const isShowTranslation = useSettingValue('player.isShowLyricTranslation')
  const isShowRoma = useSettingValue('player.isShowLyricRoma')

  const hide = useCallback(() => {
    modalRef.current?.setVisible(false)
  }, [])

  useImperativeHandle(ref, () => ({
    show() {
      if (visible) modalRef.current?.setVisible(true)
      else {
        setVisible(true)
        requestAnimationFrame(() => {
          modalRef.current?.setVisible(true)
        })
      }
    },
    hide,
  }), [hide, visible])

  const modeMeta = useMemo(() => {
    return MODE_INFO[togglePlayMethod] ?? MODE_INFO[MUSIC_TOGGLE_MODE.listLoop]
  }, [togglePlayMethod])

  const handleTogglePlayMode = useCallback(() => {
    let index = MUSIC_TOGGLE_MODE_LIST.indexOf(togglePlayMethod)
    if (++index >= MUSIC_TOGGLE_MODE_LIST.length) index = 0
    const mode = MUSIC_TOGGLE_MODE_LIST[index]
    updateSetting({ 'player.togglePlayMethod': mode })
    const key = MODE_INFO[mode]?.name
    if (key) toast(t(key as any))
  }, [t, togglePlayMethod])

  const handleToggleDesktopLock = useCallback(() => {
    const isLock = !settingState.setting['desktopLyric.isLock']
    void toggleDesktopLyricLock(isLock).then(() => {
      updateSetting({ 'desktopLyric.isLock': isLock })
    })
  }, [])

  const handleToggleTranslation = useCallback((val: boolean) => {
    updateSetting({ 'player.isShowLyricTranslation': val })
    void toggleTranslation(val)
  }, [])

  const handleToggleRoma = useCallback((val: boolean) => {
    updateSetting({ 'player.isShowLyricRoma': val })
    void toggleRoma(val)
  }, [])

  const handleOpenComment = useCallback(() => {
    hide()
    requestAnimationFrame(() => {
      if (commonState.componentIds.playDetail) {
        navigations.pushCommentScreen(commonState.componentIds.playDetail)
      }
    })
  }, [hide])

  const handleOpenTimeout = useCallback(() => {
    hide()
    requestAnimationFrame(() => {
      onShowTimeoutExit?.()
    })
  }, [hide, onShowTimeoutExit])

  const handleOpenMusicAdd = useCallback(() => {
    hide()
    requestAnimationFrame(() => {
      onShowMusicAdd?.()
    })
  }, [hide, onShowMusicAdd])

  if (!visible) return null

  return (
    <Modal ref={modalRef} bgColor="rgba(0, 0, 0, 0.48)">
      <View style={styles.overlayWrap} pointerEvents="box-none">
        <View
          style={[
            styles.sheetFrame,
            direction === 'horizontal' ? styles.sheetWide : null,
          ]}
          onStartShouldSetResponder={() => true}
        >
          <GlassSurface
            dense
            onDark
            borderRadius={radius.glassSheet}
            backdropUri={musicInfo.pic}
            style={styles.glassOuter}
            innerStyle={styles.glassInner}
          >
            {/* 顶部把手与歌曲摘要 */}
            <View style={styles.grabberWrap} pointerEvents="none">
              <View style={styles.grabber} />
            </View>
            <View style={styles.sheetHeader}>
              <CoverArt
                uri={musicInfo.pic}
                size={44}
                borderRadius={9}
                style={styles.headerCover}
              />
              <View style={styles.headerMeta}>
                <Text numberOfLines={1} size={16} color="#FFFFFF" style={styles.headerTitle}>
                  {musicInfo.name || t('play_detail_setting_title')}
                </Text>
                <Text numberOfLines={1} size={13} color={GLASS_DARK_SUB_LABEL} style={styles.headerSub}>
                  {musicInfo.singer || t('play_detail_setting_title')}
                </Text>
              </View>
              <GlassCircleButton
                icon="close"
                iconSize={16}
                size={30}
                onDark
                onPress={hide}
                accessibilityLabel={t('close')}
              />
            </View>

            <View style={styles.headerDivider} />

            <ScrollView
              showsVerticalScrollIndicator={false}
              contentContainerStyle={styles.scrollContent}
            >
              <View onStartShouldSetResponder={() => true}>
                {/* 快捷播放功能区（桌面歌词、播放模式、定时关闭、评论、添加到歌单） */}
                <View style={settingStyles.section}>
                  <View style={settingStyles.card}>
                    {/* 桌面歌词开关 + 锁定 */}
                    <TouchableOpacity
                      activeOpacity={0.7}
                      onPress={() => { onToggleDesktopLyric?.(!desktopLyricEnable) }}
                      onLongPress={handleToggleDesktopLock}
                      style={settingStyles.row}
                    >
                      <IonIcon
                        name={desktopLyricEnable ? 'reader' : 'reader-outline'}
                        rawSize={20}
                        color={desktopLyricEnable ? GLASS_DARK_ACCENT : GLASS_DARK_LABEL}
                      />
                      <Text style={settingStyles.rowLabel} color={GLASS_DARK_LABEL} numberOfLines={1}>
                        {t('setting_lyric_desktop')}
                      </Text>
                      {desktopLyricEnable ? (
                        <TouchableOpacity
                          style={styles.lockChip}
                          activeOpacity={0.7}
                          onPress={handleToggleDesktopLock}
                          accessibilityRole="button"
                        >
                          <IonIcon
                            name={desktopLyricLock ? 'lock-closed' : 'lock-open-outline'}
                            rawSize={14}
                            color={desktopLyricLock ? GLASS_DARK_ACCENT : GLASS_DARK_SUB_LABEL}
                          />
                          <Text size={11.5} color={desktopLyricLock ? GLASS_DARK_ACCENT : GLASS_DARK_SUB_LABEL}>
                            {t('setting_lyric_desktop_lock')}
                          </Text>
                        </TouchableOpacity>
                      ) : null}
                      <Switch
                        value={desktopLyricEnable}
                        onValueChange={(val) => { onToggleDesktopLyric?.(val) }}
                        trackColor={{ false: GLASS_DARK_SWITCH_OFF, true: GLASS_DARK_SWITCH_ON }}
                        thumbColor={Platform.OS === 'android' ? '#FFFFFF' : undefined}
                        ios_backgroundColor={GLASS_DARK_SWITCH_OFF}
                      />
                    </TouchableOpacity>
                    <View style={settingStyles.rowSep} />

                    {/* 播放模式切换 */}
                    <TouchableOpacity
                      activeOpacity={0.7}
                      onPress={handleTogglePlayMode}
                      style={settingStyles.row}
                    >
                      <IonIcon name={modeMeta.icon} rawSize={20} color={GLASS_DARK_LABEL} />
                      <Text style={settingStyles.rowLabel} color={GLASS_DARK_LABEL} numberOfLines={1}>
                        {t(modeMeta.name as any)}
                      </Text>
                      <IonIcon name="chevron-forward" rawSize={16} color={GLASS_DARK_SUB_LABEL} />
                    </TouchableOpacity>
                    <View style={settingStyles.rowSep} />

                    {/* 定时停止播放 */}
                    <TouchableOpacity
                      activeOpacity={0.7}
                      onPress={handleOpenTimeout}
                      style={settingStyles.row}
                    >
                      <IonIcon
                        name="timer-outline"
                        rawSize={20}
                        color={timeoutActive ? GLASS_DARK_ACCENT : GLASS_DARK_LABEL}
                      />
                      <Text style={settingStyles.rowLabel} color={GLASS_DARK_LABEL} numberOfLines={1}>
                        {t('timeout_exit_tip_off')}
                      </Text>
                      {timeoutActive ? <View style={styles.activeDot} /> : null}
                      <IonIcon name="chevron-forward" rawSize={16} color={GLASS_DARK_SUB_LABEL} />
                    </TouchableOpacity>
                    <View style={settingStyles.rowSep} />

                    {/* 歌曲评论入口 */}
                    <TouchableOpacity
                      activeOpacity={0.7}
                      onPress={handleOpenComment}
                      style={settingStyles.row}
                    >
                      <IonIcon name="chatbubble-outline" rawSize={20} color={GLASS_DARK_LABEL} />
                      <Text style={settingStyles.rowLabel} color={GLASS_DARK_LABEL} numberOfLines={1}>
                        {t('comment_title', { name: musicInfo.name || '-' })}
                      </Text>
                      <IonIcon name="chevron-forward" rawSize={16} color={GLASS_DARK_SUB_LABEL} />
                    </TouchableOpacity>
                    <View style={settingStyles.rowSep} />

                    {/* 添加到歌单 */}
                    <TouchableOpacity
                      activeOpacity={0.7}
                      onPress={handleOpenMusicAdd}
                      style={settingStyles.row}
                    >
                      <IonIcon name="add-circle-outline" rawSize={20} color={GLASS_DARK_LABEL} />
                      <Text style={settingStyles.rowLabel} color={GLASS_DARK_LABEL} numberOfLines={1}>
                        {t('add_to')}
                      </Text>
                      <IonIcon name="chevron-forward" rawSize={16} color={GLASS_DARK_SUB_LABEL} />
                    </TouchableOpacity>
                  </View>
                </View>

                {/* 歌词译文 / 罗马音显示 */}
                <View style={settingStyles.section}>
                  <View style={settingStyles.card}>
                    <TouchableOpacity
                      activeOpacity={0.7}
                      onPress={() => { handleToggleTranslation(!isShowTranslation) }}
                      style={settingStyles.row}
                    >
                      <Text style={settingStyles.rowLabel} color={GLASS_DARK_LABEL} numberOfLines={2}>
                        {t('setting_play_show_translation')}
                      </Text>
                      <Switch
                        value={isShowTranslation}
                        onValueChange={handleToggleTranslation}
                        trackColor={{ false: GLASS_DARK_SWITCH_OFF, true: GLASS_DARK_SWITCH_ON }}
                        thumbColor={Platform.OS === 'android' ? '#FFFFFF' : undefined}
                        ios_backgroundColor={GLASS_DARK_SWITCH_OFF}
                      />
                    </TouchableOpacity>
                    <View style={settingStyles.rowSep} />
                    <TouchableOpacity
                      activeOpacity={0.7}
                      onPress={() => { handleToggleRoma(!isShowRoma) }}
                      style={settingStyles.row}
                    >
                      <Text style={settingStyles.rowLabel} color={GLASS_DARK_LABEL} numberOfLines={2}>
                        {t('setting_play_show_roma')}
                      </Text>
                      <Switch
                        value={isShowRoma}
                        onValueChange={handleToggleRoma}
                        trackColor={{ false: GLASS_DARK_SWITCH_OFF, true: GLASS_DARK_SWITCH_ON }}
                        thumbColor={Platform.OS === 'android' ? '#FFFFFF' : undefined}
                        ios_backgroundColor={GLASS_DARK_SWITCH_OFF}
                      />
                    </TouchableOpacity>
                  </View>
                </View>

                <SettingLyricProgress />
                <SettingVolume />
                <SettingPlaybackRate />
                <SettingLrcFontSize direction={direction} />
                <SettingLrcAlign />
              </View>
            </ScrollView>
          </GlassSurface>
        </View>
      </View>
    </Modal>
  )
})

const styles = createStyle({
  overlayWrap: {
    flex: 1,
    justifyContent: 'flex-end',
    alignItems: 'center',
    paddingHorizontal: 12,
    paddingBottom: 14,
  },
  sheetFrame: {
    width: '100%',
    maxHeight: '80%',
  },
  sheetWide: {
    maxWidth: 520,
  },
  glassOuter: {
    width: '100%',
    maxHeight: '100%',
  },
  glassInner: {
    backgroundColor: 'rgba(22, 24, 32, 0.84)',
    maxHeight: '100%',
  },
  grabberWrap: {
    paddingTop: 8,
    paddingBottom: 4,
    alignItems: 'center',
  },
  grabber: {
    width: 36,
    height: 4.5,
    borderRadius: radius.pill,
    backgroundColor: 'rgba(255, 255, 255, 0.32)',
  },
  sheetHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: pagePadding,
    paddingTop: spacing.xs,
    paddingBottom: spacing.md,
    gap: 12,
  },
  headerCover: {
    backgroundColor: 'rgba(255, 255, 255, 0.08)',
  },
  headerMeta: {
    flex: 1,
    minWidth: 0,
  },
  headerTitle: {
    fontWeight: fontWeight.bold,
  },
  headerSub: {
    marginTop: 2,
  },
  headerDivider: {
    height: StyleSheet.hairlineWidth,
    backgroundColor: 'rgba(255, 255, 255, 0.14)',
  },
  scrollContent: {
    paddingHorizontal: pagePadding,
    paddingTop: spacing.md,
    paddingBottom: spacing.xl,
  },
  lockChip: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: radius.pill,
    backgroundColor: 'rgba(255, 255, 255, 0.12)',
  },
  activeDot: {
    width: 7,
    height: 7,
    borderRadius: 3.5,
    backgroundColor: GLASS_DARK_ACCENT,
  },
})
