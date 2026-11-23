import { forwardRef, useImperativeHandle, useRef, useState } from 'react'
import { ScrollView, View } from 'react-native'
import { IonIcon } from '@/components/common/Icon'
import Text from '@/components/common/Text'
import { useI18n } from '@/lang'
import { useSettingValue } from '@/store/setting/hook'
import { useApple, fontSize, pagePadding, spacing } from '@/theme/apple'
import { createStyle } from '@/utils/tools'
import HistorySearch, { type HistorySearchType } from './HistorySearch'
import HotSearch, { type HotSearchType } from './HotSearch'

interface BlankViewProps {
  onSearch: (keyword: string) => void
}
type Source = LX.OnlineSource | 'all'

export interface BlankViewType {
  show: (source: Source) => void
}

export default forwardRef<BlankViewType, BlankViewProps>(({ onSearch }, ref) => {
  const [visible, setVisible] = useState(false)
  const [hotEmpty, setHotEmpty] = useState(true)
  const [historyEmpty, setHistoryEmpty] = useState(true)
  const hotSearchRef = useRef<HotSearchType>(null)
  const historySearchRef = useRef<HistorySearchType>(null)
  const isShowHotSearch = useSettingValue('search.isShowHotSearch')
  const isShowHistorySearch = useSettingValue('search.isShowHistorySearch')
  const t = useI18n()
  const apple = useApple()

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

  const isAllEmpty = (!isShowHotSearch || hotEmpty) && (!isShowHistorySearch || historyEmpty)

  return (
    <ScrollView contentContainerStyle={isAllEmpty ? styles.emptyScroll : undefined}>
      {
        isAllEmpty
          ? (
              <View style={styles.empty}>
                <IonIcon name="search" rawSize={44} color={apple.tertiaryLabel} />
                <Text size={fontSize.subhead} color={apple.secondaryLabel} style={styles.emptyText}>
                  {t('apple_search_placeholder')}
                </Text>
              </View>
            )
          : null
      }
      <View style={isAllEmpty ? styles.hidden : styles.content}>
        { isShowHotSearch ? <HotSearch ref={hotSearchRef} onSearch={onSearch} onEmptyChange={setHotEmpty} /> : null }
        { isShowHistorySearch ? <HistorySearch ref={historySearchRef} onSearch={onSearch} onEmptyChange={setHistoryEmpty} /> : null }
      </View>
    </ScrollView>
  )
})

const styles = createStyle({
  content: {
    paddingBottom: spacing.xxxl,
    paddingLeft: pagePadding,
    paddingRight: pagePadding,
  },
  emptyScroll: {
    flexGrow: 1,
  },
  empty: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: pagePadding,
  },
  emptyText: {
    marginTop: spacing.md,
  },
  hidden: {
    display: 'none',
  },
})
