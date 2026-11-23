import { useEffect, useMemo, useState } from 'react'
import { View } from 'react-native'

import { createStyle } from '@/utils/tools'
import { type SearchType } from '@/store/search/state'
import { useI18n } from '@/lang'
import { getSearchSetting } from '@/utils/data'
import SegmentedControl, { type SegmentItem } from '@/components/apple/SegmentedControl'
import { pagePadding, spacing } from '@/theme/apple'

const SEARCH_TYPE_LIST = [
  'music',
  'songlist',
] as const

/**
 * 搜索类型（歌曲 / 歌单）分段控件，放在搜索框下方
 */
export default () => {
  const t = useI18n()
  const [type, setType] = useState<SearchType>('music')

  useEffect(() => {
    void getSearchSetting().then(info => {
      setType(info.type)
    })
  }, [])

  const items = useMemo<Array<SegmentItem<SearchType>>>(() => {
    return SEARCH_TYPE_LIST.map(type => ({ label: t(`search_type_${type}`), id: type }))
  }, [t])

  const handleTypeChange = (type: SearchType) => {
    setType(type)
    global.app_event.searchTypeChanged(type)
  }

  return (
    <View style={styles.container}>
      <SegmentedControl items={items} value={type} onChange={handleTypeChange} />
    </View>
  )
}

const styles = createStyle({
  container: {
    paddingHorizontal: pagePadding,
    paddingBottom: spacing.sm,
  },
})
