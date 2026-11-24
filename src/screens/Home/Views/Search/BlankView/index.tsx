import { forwardRef, useImperativeHandle, useRef, useState } from 'react'
import { ScrollView, View } from 'react-native'
import { useSettingValue } from '@/store/setting/hook'
import { useHorizontalMode } from '@/utils/hooks'
import { pagePadding, size } from '@/theme/apple'
import { createStyle } from '@/utils/tools'
import HistorySearch, { type HistorySearchType } from './HistorySearch'
import HotSearch, { type HotSearchType } from './HotSearch'
import BrowseCategories from './BrowseCategories'

interface BlankViewProps {
  onSearch: (keyword: string) => void
}
type Source = LX.OnlineSource | 'all'

export interface BlankViewType {
  show: (source: Source) => void
}

export default forwardRef<BlankViewType, BlankViewProps>(({ onSearch }, ref) => {
  const [visible, setVisible] = useState(false)
  const hotSearchRef = useRef<HotSearchType>(null)
  const historySearchRef = useRef<HistorySearchType>(null)
  const isShowHotSearch = useSettingValue('search.isShowHotSearch')
  const isShowHistorySearch = useSettingValue('search.isShowHistorySearch')
  const isWide = useHorizontalMode()

  const handleShow = (source: Source) => {
    hotSearchRef.current?.show(source)
    historySearchRef.current?.show()
  }

  useImperativeHandle(ref, () => ({
    show(source) {
      if (visible) handleShow(source)
      else {
        setVisible(true)
        requestAnimationFrame(() => {
          handleShow(source)
        })
      }
    },
  }), [visible])

  if (!visible) return null

  return (
    <ScrollView
      keyboardShouldPersistTaps="handled"
      showsVerticalScrollIndicator={false}
      contentContainerStyle={{
        ...styles.content,
        paddingBottom: isWide ? size.miniPlayerClearance : size.dockClearance,
      }}
    >
      {isShowHotSearch ? <HotSearch ref={hotSearchRef} onSearch={onSearch} /> : null}
      {isShowHistorySearch ? <HistorySearch ref={historySearchRef} onSearch={onSearch} /> : null}
      <View>
        <BrowseCategories onSearch={onSearch} />
      </View>
    </ScrollView>
  )
})

const styles = createStyle({
  content: {
    paddingHorizontal: pagePadding,
  },
})
