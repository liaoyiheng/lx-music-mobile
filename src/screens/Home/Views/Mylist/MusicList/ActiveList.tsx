import { forwardRef, useEffect, useImperativeHandle, useState } from 'react'
import { TouchableOpacity, View } from 'react-native'

import { GlassCapsuleButton, GlassCircleButton } from '@/components/apple/LiquidGlass'
import { useActiveListId, useListFetching } from '@/store/list/hook'
import { createStyle } from '@/utils/tools'
import { getListPrevSelectId } from '@/utils/data'
import { setActiveList } from '@/core/list'
import Loading from '@/components/common/Loading'
import { useHorizontalMode } from '@/utils/hooks'
import { useI18n } from '@/lang'
import { useApple, pagePadding, size, spacing } from '@/theme/apple'

export interface ActiveListProps {
  onShowSearchBar: () => void
  onScrollToTop: () => void
}
export interface ActiveListType {
  setVisibleBar: (visible: boolean) => void
}

export const NAV_BAR_HEIGHT = size.hitTarget

/**
 * 歌曲页顶部液态玻璃导航栏：左侧「‹ 资料库」胶囊按钮（宽屏隐藏），右侧列表内搜索圆形玻璃按钮
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
    <View pointerEvents={visibleBar ? 'auto' : 'none'} style={{ opacity: visibleBar ? 1 : 0 }}>
      <TouchableOpacity
        activeOpacity={1}
        onLongPress={onScrollToTop}
        style={styles.bar}
      >
        {
          isWide
            ? <View style={styles.flex} />
            : (
                <GlassCapsuleButton
                  icon="chevron-back"
                  label={t('apple_tab_library')}
                  onPress={showList}
                />
              )
        }
        {!isWide ? <View style={styles.flex} /> : null}
        <View style={styles.right}>
          {fetching ? <Loading color={apple.secondaryLabel} style={styles.loading} /> : null}
          <GlassCircleButton
            icon="search"
            iconSize={16}
            size={34}
            onPress={onShowSearchBar}
            accessibilityLabel={t('nav_search')}
          />
        </View>
      </TouchableOpacity>
    </View>
  )
})

const styles = createStyle({
  bar: {
    flexDirection: 'row',
    height: NAV_BAR_HEIGHT,
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: pagePadding,
  },
  flex: {
    flex: 1,
  },
  right: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  loading: {
    marginRight: spacing.sm,
  },
})
