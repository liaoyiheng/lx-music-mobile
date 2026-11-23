import { memo } from 'react'
import { View, TouchableOpacity } from 'react-native'

import { Icon, IonIcon } from '@/components/common/Icon'
import { pop } from '@/navigation'
import StatusBar from '@/components/common/StatusBar'
import { useI18n } from '@/lang'
import { createStyle } from '@/utils/tools'
import Text from '@/components/common/Text'
import commonState from '@/store/common/state'
import { useStatusbarHeight } from '@/store/common/hook'
import { useApple, fontSize, fontWeight, hairline, size, spacing } from '@/theme/apple'

/**
 * Apple Music 风格导航栏：左侧「‹ 返回」，居中歌曲名 + 歌手，右侧刷新按钮
 */
export default memo(({ musicInfo, onRefresh }: {
  musicInfo: LX.Music.MusicInfo
  onRefresh?: () => void
}) => {
  const t = useI18n()
  const apple = useApple()
  const statusBarHeight = useStatusbarHeight()

  const back = () => {
    void pop(commonState.componentIds.comment!)
  }

  return (
    <View style={{ paddingTop: statusBarHeight, backgroundColor: apple.background, borderBottomColor: apple.separator, borderBottomWidth: hairline }}>
      <StatusBar />
      <View style={styles.bar}>
        <View style={styles.titleWrap} pointerEvents="none">
          <Text numberOfLines={1} size={fontSize.headline} color={apple.label} style={styles.title}>{musicInfo.name}</Text>
          {
            musicInfo.singer
              ? <Text numberOfLines={1} size={fontSize.caption1} color={apple.secondaryLabel} style={styles.subtitle}>{musicInfo.singer}</Text>
              : null
          }
        </View>
        <TouchableOpacity style={styles.back} activeOpacity={0.5} onPress={back}>
          <IonIcon name="chevron-back" rawSize={24} color={apple.accent} style={styles.backIcon} />
          <Text size={17} color={apple.accent} numberOfLines={1}>{t('back')}</Text>
        </TouchableOpacity>
        <View style={styles.flex} />
        {
          onRefresh
            ? (
                <TouchableOpacity style={styles.iconBtn} activeOpacity={0.5} onPress={onRefresh}>
                  <Icon name="available_updates" size={20} color={apple.accent} />
                </TouchableOpacity>
              )
            : null
        }
      </View>
    </View>
  )
})

const styles = createStyle({
  bar: {
    flexDirection: 'row',
    alignItems: 'center',
    height: size.hitTarget,
    paddingHorizontal: spacing.xs,
  },
  titleWrap: {
    position: 'absolute',
    left: size.hitTarget * 2,
    right: size.hitTarget * 2,
    top: 0,
    bottom: 0,
    alignItems: 'center',
    justifyContent: 'center',
  },
  title: {
    fontWeight: fontWeight.semibold,
    textAlign: 'center',
  },
  subtitle: {
    textAlign: 'center',
  },
  back: {
    height: '100%',
    flexDirection: 'row',
    alignItems: 'center',
    paddingLeft: spacing.xs,
    paddingRight: spacing.sm,
  },
  backIcon: {
    marginRight: 2,
  },
  flex: {
    flex: 1,
  },
  iconBtn: {
    width: size.hitTarget,
    height: '100%',
    justifyContent: 'center',
    alignItems: 'center',
  },
})
