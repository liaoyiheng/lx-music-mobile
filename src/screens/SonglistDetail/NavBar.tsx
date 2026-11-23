import { memo } from 'react'
import { TouchableOpacity, View } from 'react-native'
import { Icon } from '@/components/common/Icon'
import Text from '@/components/common/Text'
import { pop } from '@/navigation'
import commonState from '@/store/common/state'
import songlistState from '@/store/songlist/state'
import { useStatusbarHeight } from '@/store/common/hook'
import { useI18n } from '@/lang'
import { useApple, fontSize, fontWeight, size, spacing } from '@/theme/apple'
import { createStyle } from '@/utils/tools'
import { handleCollect } from './listAction'
import { useListInfo } from './state'

/**
 * 歌单详情导航栏：左侧「‹ 返回」，右侧「收藏歌单」
 */
export default memo(() => {
  const apple = useApple()
  const t = useI18n()
  const info = useListInfo()
  const statusBarHeight = useStatusbarHeight()

  const back = () => {
    void pop(commonState.componentIds.songlistDetail!)
  }

  const handleCollection = () => {
    if (!songlistState.listDetailInfo.info.name) return
    void handleCollect(info.id, info.source, songlistState.listDetailInfo.info.name || info.name)
  }

  return (
    <View style={{ ...styles.bar, paddingTop: statusBarHeight, backgroundColor: apple.background }}>
      <TouchableOpacity style={styles.back} activeOpacity={0.5} onPress={back}>
        <Icon name="chevron-left" size={fontSize.headline} color={apple.accent} />
        <Text style={styles.backText} size={fontSize.body} color={apple.accent} numberOfLines={1}>{t('back')}</Text>
      </TouchableOpacity>
      <TouchableOpacity style={styles.textBtn} activeOpacity={0.5} onPress={handleCollection}>
        <Text style={styles.textBtnLabel} size={fontSize.body} color={apple.accent} numberOfLines={1}>{t('collect_songlist')}</Text>
      </TouchableOpacity>
    </View>
  )
})

const styles = createStyle({
  bar: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: spacing.xs,
    flexGrow: 0,
    flexShrink: 0,
  },
  back: {
    flex: 1,
    height: size.hitTarget,
    flexDirection: 'row',
    alignItems: 'center',
    paddingLeft: spacing.xs,
  },
  backText: {
    marginLeft: spacing.xxs,
  },
  textBtn: {
    height: size.hitTarget,
    justifyContent: 'center',
    paddingHorizontal: spacing.md,
  },
  textBtnLabel: {
    fontWeight: fontWeight.semibold,
  },
})
