// Added for Apple Music iOS 26 Liquid Glass UI (based on lyswhut/lx-music-mobile, Apache-2.0)
import { memo, useCallback, useEffect, useMemo, useState } from 'react'
import { FlatList, StyleSheet, TouchableOpacity, View, type ListRenderItem } from 'react-native'
import { CoverArt, EqualizerBars } from '@/components/apple/LiquidGlass'
import { IonIcon } from '@/components/common/Icon'
import Text from '@/components/common/Text'
import { LIST_IDS, MUSIC_TOGGLE_MODE } from '@/config/constant'
import { updateSetting } from '@/core/common'
import { getListMusics } from '@/core/list'
import { playList } from '@/core/player/player'
import { useI18n } from '@/lang'
import { useMyList } from '@/store/list/hook'
import { useIsPlay, usePlayInfo, usePlayMusicInfo } from '@/store/player/hook'
import { useSettingValue } from '@/store/setting/hook'
import { fontWeight, pagePadding, spacing } from '@/theme/apple'
import { getListMusicSync } from '@/utils/listManage'
import { createStyle, toast } from '@/utils/tools'

interface QueueRowItem {
  musicInfo: LX.Music.MusicInfo
  originalIndex: number
  isCurrent: boolean
}

const MODE_TOAST_KEYS: Record<string, string> = {
  [MUSIC_TOGGLE_MODE.listLoop]: 'play_list_loop',
  [MUSIC_TOGGLE_MODE.random]: 'play_list_random',
  [MUSIC_TOGGLE_MODE.list]: 'play_list_order',
  [MUSIC_TOGGLE_MODE.singleLoop]: 'play_single_loop',
}

const ModeSquareBtn = memo(({
  icon,
  active,
  badge,
  accessibilityLabel,
  onPress,
}: {
  icon: string
  active: boolean
  badge?: string
  accessibilityLabel: string
  onPress: () => void
}) => {
  return (
    <TouchableOpacity
      style={[styles.modeBtn, active ? styles.modeBtnActive : null]}
      activeOpacity={0.7}
      onPress={onPress}
      accessibilityRole="button"
      accessibilityLabel={accessibilityLabel}
    >
      <IonIcon
        name={icon}
        rawSize={16}
        color={active ? '#16161C' : 'rgba(255, 255, 255, 0.88)'}
      />
      {badge ? (
        <View style={styles.modeBadgeWrap} pointerEvents="none">
          <Text size={8.5} color={active ? '#16161C' : '#FFFFFF'} style={styles.modeBadgeText}>
            {badge}
          </Text>
        </View>
      ) : null}
    </TouchableOpacity>
  )
})

const QueueRow = memo(({
  item,
  isPlay,
  onSelect,
}: {
  item: QueueRowItem
  isPlay: boolean
  onSelect: (index: number) => void
}) => {
  const { musicInfo, originalIndex, isCurrent } = item
  const subText = musicInfo.interval
    ? `${musicInfo.singer} · ${musicInfo.interval}`
    : musicInfo.singer

  return (
    <TouchableOpacity
      style={[styles.row, isCurrent ? styles.rowCurrent : null]}
      activeOpacity={0.68}
      onPress={() => { onSelect(originalIndex) }}
      accessibilityRole="button"
    >
      <CoverArt
        uri={musicInfo.meta.picUrl}
        size={46}
        borderRadius={8}
        style={styles.rowCover}
      />
      <View style={styles.rowInfo}>
        <Text numberOfLines={1} size={15.5} color="#FFFFFF" style={styles.rowTitle}>
          {musicInfo.name}
        </Text>
        {subText ? (
          <Text numberOfLines={1} size={13} color="rgba(255, 255, 255, 0.65)" style={styles.rowSub}>
            {subText}
          </Text>
        ) : null}
      </View>
      {isCurrent ? (
        <View style={styles.eqContainer}>
          <EqualizerBars color="#FFFFFF" playing={isPlay} size={14} />
        </View>
      ) : null}
    </TouchableOpacity>
  )
})

/**
 * 视图 C：待播清单
 * - 列表头：左侧「待播清单」18pt Bold + 曲目数，右侧三颗 36×32 圆角 10dp 玻璃方按钮切换播放模式
 * - 歌曲列表：FlatList 渲染当前及后续待播曲目，当前曲目显示白色 EqualizerBars，点击直接切歌播放
 */
export default memo(() => {
  const t = useI18n()
  const playInfo = usePlayInfo()
  const playMusicInfo = usePlayMusicInfo()
  const isPlay = useIsPlay()
  const myLists = useMyList()
  const togglePlayMethod = useSettingValue('player.togglePlayMethod')

  const targetListId = playInfo.playerListId ?? playMusicInfo.listId
  const [listMusics, setListMusics] = useState<LX.Music.MusicInfo[]>(() => getListMusicSync(targetListId))

  useEffect(() => {
    if (!targetListId) {
      setListMusics([])
      return
    }
    setListMusics([...getListMusicSync(targetListId)])
    void getListMusics(targetListId).then((musics) => {
      setListMusics([...musics])
    })

    const handleListUpdate = (ids: string[]) => {
      if (!ids.includes(targetListId)) return
      void getListMusics(targetListId).then((musics) => {
        setListMusics([...musics])
      })
    }
    global.app_event.on('myListMusicUpdate', handleListUpdate)
    return () => {
      global.app_event.off('myListMusicUpdate', handleListUpdate)
    }
  }, [targetListId])

  const listTitle = useMemo(() => {
    if (!targetListId) return ''
    if (targetListId === LIST_IDS.TEMP) return t('list_name_temp')
    return myLists.find(l => l.id === targetListId)?.name ?? ''
  }, [myLists, t, targetListId])

  const currentMusicId = useMemo(() => {
    const raw = playMusicInfo.musicInfo
    if (!raw) return null
    return 'progress' in raw ? raw.metadata.musicInfo.id : raw.id
  }, [playMusicInfo.musicInfo])

  const queueItems = useMemo<QueueRowItem[]>(() => {
    if (!listMusics.length) return []

    let currentIdx = -1
    if (currentMusicId) {
      currentIdx = listMusics.findIndex(m => m.id === currentMusicId)
    }
    if (currentIdx < 0 && playInfo.playerPlayIndex >= 0 && playInfo.playerPlayIndex < listMusics.length) {
      currentIdx = playInfo.playerPlayIndex
    }

    if (currentIdx < 0) {
      return listMusics.map((musicInfo, originalIndex) => ({
        musicInfo,
        originalIndex,
        isCurrent: false,
      }))
    }

    const head = listMusics.slice(currentIdx).map((musicInfo, offset) => ({
      musicInfo,
      originalIndex: currentIdx + offset,
      isCurrent: offset === 0,
    }))
    const tail = listMusics.slice(0, currentIdx).map((musicInfo, originalIndex) => ({
      musicInfo,
      originalIndex,
      isCurrent: false,
    }))
    return [...head, ...tail]
  }, [currentMusicId, listMusics, playInfo.playerPlayIndex])

  const setPlayMethod = useCallback((mode: LX.AppSetting['player.togglePlayMethod']) => {
    const nextMode = togglePlayMethod === mode && mode !== MUSIC_TOGGLE_MODE.listLoop
      ? MUSIC_TOGGLE_MODE.listLoop
      : mode
    updateSetting({ 'player.togglePlayMethod': nextMode })
    const key = MODE_TOAST_KEYS[nextMode]
    if (key) toast(t(key as any))
  }, [t, togglePlayMethod])

  const handleSelectTrack = useCallback((originalIndex: number) => {
    if (!targetListId) return
    void playList(targetListId, originalIndex)
  }, [targetListId])

  const renderItem = useCallback<ListRenderItem<QueueRowItem>>(({ item }) => (
    <QueueRow item={item} isPlay={isPlay} onSelect={handleSelectTrack} />
  ), [handleSelectTrack, isPlay])

  const keyExtractor = useCallback((item: QueueRowItem) => `${item.musicInfo.id}_${item.originalIndex}`, [])

  const subtitle = listTitle
    ? `${listTitle} · ${t('apple_library_songs_count', { num: queueItems.length })}`
    : t('apple_library_songs_count', { num: queueItems.length })

  return (
    <View style={styles.container}>
      <View style={styles.headerBar}>
        <View style={styles.titleWrap}>
          <Text size={18} color="#FFFFFF" style={styles.title}>
            {t('apple_player_up_next')}
          </Text>
          <Text numberOfLines={1} size={13} color="rgba(255, 255, 255, 0.65)" style={styles.subtitle}>
            {subtitle}
          </Text>
        </View>
        <View style={styles.modeBtns}>
          <ModeSquareBtn
            icon="shuffle"
            active={togglePlayMethod === MUSIC_TOGGLE_MODE.random}
            accessibilityLabel={t('play_list_random')}
            onPress={() => { setPlayMethod(MUSIC_TOGGLE_MODE.random) }}
          />
          <ModeSquareBtn
            icon="repeat"
            active={togglePlayMethod === MUSIC_TOGGLE_MODE.listLoop}
            accessibilityLabel={t('play_list_loop')}
            onPress={() => { setPlayMethod(MUSIC_TOGGLE_MODE.listLoop) }}
          />
          <ModeSquareBtn
            icon="repeat"
            badge="1"
            active={togglePlayMethod === MUSIC_TOGGLE_MODE.singleLoop}
            accessibilityLabel={t('play_single_loop')}
            onPress={() => { setPlayMethod(MUSIC_TOGGLE_MODE.singleLoop) }}
          />
        </View>
      </View>

      {queueItems.length ? (
        <FlatList
          data={queueItems}
          renderItem={renderItem}
          keyExtractor={keyExtractor}
          showsVerticalScrollIndicator={false}
          contentContainerStyle={styles.listContent}
          initialNumToRender={16}
          maxToRenderPerBatch={16}
          windowSize={9}
        />
      ) : (
        <View style={styles.emptyWrap}>
          <IonIcon name="musical-notes-outline" rawSize={36} color="rgba(255, 255, 255, 0.34)" />
          <Text size={15} color="rgba(255, 255, 255, 0.56)" style={styles.emptyText}>
            {t('apple_player_queue_empty')}
          </Text>
        </View>
      )}
    </View>
  )
})

const styles = createStyle({
  container: {
    flex: 1,
    minHeight: 0,
  },
  headerBar: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: pagePadding + 4,
    paddingTop: spacing.xs,
    paddingBottom: spacing.md,
    gap: spacing.md,
  },
  titleWrap: {
    flex: 1,
    minWidth: 0,
  },
  title: {
    fontWeight: fontWeight.bold,
    letterSpacing: -0.2,
  },
  subtitle: {
    marginTop: 2,
    fontVariant: ['tabular-nums'],
  },
  modeBtns: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
    flexShrink: 0,
  },
  modeBtn: {
    width: 36,
    height: 32,
    borderRadius: 10,
    backgroundColor: 'rgba(255, 255, 255, 0.14)',
    borderWidth: StyleSheet.hairlineWidth,
    borderColor: 'rgba(255, 255, 255, 0.22)',
    justifyContent: 'center',
    alignItems: 'center',
    position: 'relative',
  },
  modeBtnActive: {
    backgroundColor: 'rgba(255, 255, 255, 0.94)',
    borderColor: 'rgba(255, 255, 255, 0.98)',
    shadowColor: '#000000',
    shadowOpacity: 0.22,
    shadowRadius: 10,
    shadowOffset: { width: 0, height: 4 },
    elevation: 4,
  },
  modeBadgeWrap: {
    position: 'absolute',
    bottom: 3,
    right: 4,
  },
  modeBadgeText: {
    fontWeight: fontWeight.heavy,
    lineHeight: 9,
  },
  listContent: {
    paddingHorizontal: pagePadding,
    paddingBottom: spacing.lg,
  },
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 8,
    paddingVertical: 8,
    borderRadius: 12,
    gap: 12,
  },
  rowCurrent: {
    backgroundColor: 'rgba(255, 255, 255, 0.14)',
  },
  rowCover: {
    backgroundColor: 'rgba(255, 255, 255, 0.08)',
  },
  rowInfo: {
    flex: 1,
    minWidth: 0,
  },
  rowTitle: {
    fontWeight: fontWeight.semibold,
  },
  rowSub: {
    marginTop: 2,
  },
  eqContainer: {
    paddingLeft: spacing.xs,
    paddingRight: spacing.xs,
  },
  emptyWrap: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    gap: spacing.sm,
    paddingBottom: spacing.xxxl,
  },
  emptyText: {
    fontWeight: fontWeight.medium,
  },
})
