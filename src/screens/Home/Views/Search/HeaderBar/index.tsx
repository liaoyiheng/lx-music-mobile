import { useRef, forwardRef, useImperativeHandle } from 'react'
import { View } from 'react-native'

import SourceSelector, {
  type SourceSelectorType as _SourceSelectorType,
  type SourceSelectorProps as _SourceSelectorProps,
} from '@/components/SourceSelector'
import SearchInput, { type SearchInputType, type SearchInputProps } from './SearchInput'
import { createStyle } from '@/utils/tools'
import { type Source as MusicSource } from '@/store/search/music/state'
import { type Source as SonglistSource } from '@/store/search/songlist/state'
import { pagePadding, spacing } from '@/theme/apple'

type Sources = Readonly<Array<MusicSource | SonglistSource>>
type SourceSelectorProps = _SourceSelectorProps<Sources>
type SourceSelectorType = _SourceSelectorType<Sources>

export interface HeaderBarProps {
  onSourceChange: SourceSelectorProps['onSourceChange']
  onTipSearch: SearchInputProps['onChangeText']
  onSearch: SearchInputProps['onSubmit']
  onHideTipList: SearchInputProps['onBlur']
  onShowTipList: SearchInputProps['onTouchStart']
}

export interface HeaderBarType {
  setSourceList: SourceSelectorType['setSourceList']
  setText: SearchInputType['setText']
  blur: SearchInputType['blur']
}

/**
 * 搜索栏：圆角搜索框 + 右侧来源选择
 */
export default forwardRef<HeaderBarType, HeaderBarProps>(({ onSourceChange, onTipSearch, onSearch, onHideTipList, onShowTipList }, ref) => {
  const sourceSelectorRef = useRef<SourceSelectorType>(null)
  const searchInputRef = useRef<SearchInputType>(null)

  useImperativeHandle(ref, () => ({
    setSourceList(list, source) {
      sourceSelectorRef.current?.setSourceList(list, source)
    },
    setText(text) {
      searchInputRef.current?.setText(text)
    },
    blur() {
      searchInputRef.current?.blur()
    },
  }), [])

  return (
    <View style={styles.searchBar}>
      <View style={styles.input}>
        <SearchInput
          ref={searchInputRef}
          onChangeText={onTipSearch}
          onSubmit={onSearch}
          onBlur={onHideTipList}
          onTouchStart={onShowTipList}
        />
      </View>
      <View style={styles.selector}>
        <SourceSelector ref={sourceSelectorRef} onSourceChange={onSourceChange} center />
      </View>
    </View>
  )
})

const styles = createStyle({
  searchBar: {
    flexDirection: 'row',
    alignItems: 'center',
    height: 48,
    paddingLeft: pagePadding,
    paddingRight: spacing.sm,
    zIndex: 2,
  },
  input: {
    flex: 1,
    height: 36,
  },
  selector: {
    marginLeft: spacing.xs,
  },
})
