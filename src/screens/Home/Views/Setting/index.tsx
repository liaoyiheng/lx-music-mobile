import { useCallback, useRef, useState } from 'react'
import { View } from 'react-native'
import LargeTitle from '@/components/apple/LargeTitle'
import { useI18n } from '@/lang'
import { useHorizontalMode } from '@/utils/hooks'
import { useBackHandler } from '@/utils/hooks/useBackHandler'
import commonState from '@/store/common/state'
import { setNavActiveId } from '@/core/common'
import { useApple, hairline } from '@/theme/apple'
import { createStyle } from '@/utils/tools'
import CategoryList from './CategoryList'
import Detail from './Detail'
import type { SettingScreenIds } from './screens'

export type { SettingScreenIds } from './screens'

const LIST_PANE_WIDTH = 320

/**
 * 设置
 * 竖屏：首页是 iOS 分组列表，点某一项进入二级页面，返回键 / 左上角「‹ 设置」回到列表
 * 宽屏：左边分类列表，右边当前分类的内容
 * 所有设置项的读写沿用原版组件（settings/ 目录）
 */
export default () => {
  const apple = useApple()
  const t = useI18n()
  const isWide = useHorizontalMode()
  // 宽屏当前选中的分类（沿用原版全局记录）
  const [activeId, setActiveId] = useState<SettingScreenIds>(global.lx.settingActiveId)
  // 竖屏当前打开的二级页面，null = 在列表首页
  const [detailId, setDetailId] = useState<SettingScreenIds | null>(null)
  const detailIdRef = useRef(detailId)
  detailIdRef.current = detailId

  const handleSelect = useCallback((id: SettingScreenIds) => {
    global.lx.settingActiveId = id
    setActiveId(id)
    setDetailId(id)
  }, [])
  const handleBack = useCallback(() => {
    setDetailId(null)
  }, [])

  useBackHandler(useCallback(() => {
    if (Object.keys(commonState.componentIds).length != 1 || commonState.navActiveId != 'nav_setting') return false
    if (!isWide && detailIdRef.current) {
      setDetailId(null)
      return true
    }
    setNavActiveId(commonState.lastNavActiveId)
    return true
  }, [isWide]))

  if (isWide) {
    return (
      <View style={styles.row}>
        <View style={{ ...styles.listPane, borderRightColor: apple.separator, backgroundColor: apple.groupedBackground }}>
          <LargeTitle title={t('nav_setting')} />
          <CategoryList activeId={activeId} onSelect={handleSelect} />
        </View>
        <View style={styles.detailPane}>
          <Detail id={activeId} />
        </View>
      </View>
    )
  }

  return (
    <View style={{ ...styles.container, backgroundColor: apple.groupedBackground }}>
      {detailId
        ? <Detail id={detailId} onBack={handleBack} />
        : (
            <>
              <LargeTitle title={t('nav_setting')} />
              <CategoryList onSelect={handleSelect} />
            </>
          )}
    </View>
  )
}

const styles = createStyle({
  container: {
    flex: 1,
  },
  row: {
    flex: 1,
    flexDirection: 'row',
  },
  listPane: {
    width: LIST_PANE_WIDTH,
    borderRightWidth: hairline,
  },
  detailPane: {
    flex: 1,
  },
})
