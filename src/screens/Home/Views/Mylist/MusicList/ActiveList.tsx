import { forwardRef, useEffect, useImperativeHandle, useState } from 'react'
import { TouchableOpacity, View } from 'react-native'

import { Icon } from '@/components/common/Icon'
import { useActiveListId, useListFetching } from '@/store/list/hook'
import { createStyle } from '@/utils/tools'
import { getListPrevSelectId } from '@/utils/data'
import { setActiveList } from '@/core/list'
import Text from '@/components/common/Text'
import Loading from '@/components/common/Loading'
import { useHorizontalMode } from '@/utils/hooks'
import { useI18n } from '@/lang'
import { useApple, fontSize, size, spacing } from '@/theme/apple'

export interface ActiveListProps {
  onShowSearchBar: () => void
  onScrollToTop: () => void
}
export interface ActiveListType {
  setVisibleBar: (visible: boolean) => void
}

export const NAV_BAR_HEIGHT = size.hitTarget

/**
 * 歌曲页导航栏：左侧「‹ 资料库」返回（宽屏不显示），右侧列表内搜索
 * 长按导航栏回到顶部（沿用原版）
 */
export default forwardRef<ActiveListType, ActiveListProps>(({ onShowSearchBar, onScrollToTop }, ref) => {
  const apple = useApple()
  const t = useI18n()
  const isWide = useHorizontalMode()
  const currentListId = useActiveListId()
  const fetching = useListFetching(currentListId)
  const [visibleBar, setVisibleBar] = useState(true)

  useImperativeHandle(ref, () => ({
    setVisibleBar(visible) {
      setVisibleBar(visible)
    },
  }))

  const showList = () => {
    global.app_event.changeLoveListVisible(true)
  }

  useEffect(() => {
    void getListPrevSelectId().then((id) => {
      setActiveList(id)
    })
  }, [])

  return (
    <TouchableOpacity
      activeOpacity={1}
      onLongPress={onScrollToTop}
      style={{ ...styles.bar, opacity: visibleBar ? 1 : 0 }}
    >
      {
        isWide
          ? <View style={styles.flex} />
          : (
              <TouchableOpacity style={styles.back} activeOpacity={0.5} onPress={showList}>
                <Icon name="chevron-left" size={18} color={apple.accent} />
                <Text style={styles.backText} size={fontSize.body} color={apple.accent} numberOfLines={1}>{t('apple_tab_library')}</Text>
              </TouchableOpacity>
            )
      }
      {fetching ? <Loading color={apple.secondaryLabel} style={styles.loading} /> : null}
      <TouchableOpacity style={styles.iconBtn} activeOpacity={0.5} onPress={onShowSearchBar}>
        <Icon color={apple.accent} name="search-2" size={18} />
      </TouchableOpacity>
    </TouchableOpacity>
  )
})

const styles = createStyle({
  bar: {
    flexDirection: 'row',
    height: NAV_BAR_HEIGHT,
    alignItems: 'center',
    paddingHorizontal: spacing.xs,
  },
  flex: {
    flex: 1,
  },
  back: {
    flex: 1,
    height: '100%',
    flexDirection: 'row',
    alignItems: 'center',
    paddingLeft: spacing.xs,
  },
  backText: {
    marginLeft: spacing.xxs,
  },
  loading: {
    marginRight: spacing.sm,
  },
  iconBtn: {
    width: size.hitTarget,
    height: '100%',
    justifyContent: 'center',
    alignItems: 'center',
  },
})
