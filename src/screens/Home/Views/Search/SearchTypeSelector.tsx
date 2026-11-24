import { memo, useMemo } from 'react'
import { View } from 'react-native'

import { createStyle } from '@/utils/tools'
import { type SearchType } from '@/store/search/state'
import { useI18n } from '@/lang'
import SegmentedControl, { type SegmentItem } from '@/components/apple/SegmentedControl'
import SourceSelector, { type SearchSource, type SearchSources } from './SourceSelector'
import { pagePadding, spacing } from '@/theme/apple'

const SEARCH_TYPE_LIST = [
  'music',
  'songlist',
] as const

export interface SearchTypeSelectorProps {
  type: SearchType
  sourceList: SearchSources
  source: SearchSource
  onSourceChange: (source: SearchSource) => void
  onTypeChange?: (type: SearchType) => void
}

/**
 * 搜索类型（歌曲 / 歌单）液态玻璃分段控件 + 右侧紧凑音源胶囊
 */
export default memo(({
  type,
  sourceList,
  source,
  onSourceChange,
  onTypeChange,
}: SearchTypeSelectorProps) => {
  const t = useI18n()

  const items = useMemo<Array<SegmentItem<SearchType>>>(() => [
    { id: 'music', label: t('apple_search_songs') },
    { id: 'songlist', label: t('apple_search_playlists') },
  ], [t])

  const handleTypeChange = (nextType: SearchType) => {
    if (onTypeChange) onTypeChange(nextType)
    else global.app_event.searchTypeChanged(nextType)
  }

  return (
    <View style={styles.container}>
      <SegmentedControl
        items={items}
        value={type}
        onChange={handleTypeChange}
        style={styles.seg}
      />
      <SourceSelector
        compact
        list={sourceList}
        source={source}
        onSourceChange={onSourceChange}
      />
    </View>
  )
})

const styles = createStyle({
  container: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: pagePadding,
    paddingBottom: spacing.sm + 2,
    gap: spacing.sm,
  },
  seg: {
    flex: 1,
  },
})
