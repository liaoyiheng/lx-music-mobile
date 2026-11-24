import { memo, useMemo } from 'react'
import { View } from 'react-native'
import { GlassCircleButton } from '@/components/apple/LiquidGlass'
import { pop } from '@/navigation'
import commonState from '@/store/common/state'
import songlistState from '@/store/songlist/state'
import { useStatusbarHeight } from '@/store/common/hook'
import { useMyList } from '@/store/list/hook'
import { useI18n } from '@/lang'
import { spacing } from '@/theme/apple'
import { createStyle } from '@/utils/tools'
import { handleCollect } from './listAction'
import { useListInfo } from './state'

/**
 * 歌单详情悬浮液态玻璃导航栏：透明覆盖在头图上，左侧返回、右侧收藏操作按钮
 */
export default memo(() => {
  const t = useI18n()
  const info = useListInfo()
  const statusBarHeight = useStatusbarHeight()
  const lists = useMyList()

  const isCollected = useMemo(() => {
    const compositeId = `${info.source}__${info.id}`
    return lists.some(l => 'sourceListId' in l && (l.sourceListId == info.id || l.sourceListId == compositeId))
  }, [info.id, info.source, lists])

  const back = () => {
    void pop(commonState.componentIds.songlistDetail!)
  }

  const handleCollection = () => {
    if (!songlistState.listDetailInfo.info.name) return
    void handleCollect(info.id, info.source, songlistState.listDetailInfo.info.name || info.name)
  }

  return (
    <View
      pointerEvents="box-none"
      style={{
        ...styles.bar,
        paddingTop: statusBarHeight + 6,
      }}
    >
      <GlassCircleButton
        icon="chevron-back"
        iconSize={18}
        size={36}
        onPress={back}
        accessibilityLabel={t('back')}
      />
      <GlassCircleButton
        icon={isCollected ? 'heart' : 'heart-outline'}
        iconSize={18}
        size={36}
        active={isCollected}
        onPress={handleCollection}
        accessibilityLabel={t('collect_songlist')}
      />
    </View>
  )
})

const styles = createStyle({
  bar: {
    position: 'absolute',
    top: 0,
    left: 0,
    right: 0,
    zIndex: 33,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: spacing.lg,
    paddingBottom: spacing.xs,
  },
})
