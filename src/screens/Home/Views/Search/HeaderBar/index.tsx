import { useRef, forwardRef, useImperativeHandle, useState } from 'react'
import { View } from 'react-native'

import DorpDownMenu, { type DorpDownMenuProps as _DorpDownMenuProps } from '@/components/common/DorpDownMenu'
import { useSourceListI18n } from '@/components/SourceSelector'
import Text from '@/components/common/Text'
import { IonIcon } from '@/components/common/Icon'
import { useI18n } from '@/lang'
import { useSettingValue } from '@/store/setting/hook'
import SearchInput, { type SearchInputType, type SearchInputProps } from './SearchInput'
import { createStyle } from '@/utils/tools'
import { type Source as MusicSource } from '@/store/search/music/state'
import { type Source as SonglistSource } from '@/store/search/songlist/state'
import { useApple, fontSize, fontWeight, pagePadding, radius, spacing } from '@/theme/apple'

type Sources = Readonly<Array<MusicSource | SonglistSource>>
type Source = Sources[number]

export interface HeaderBarProps {
  onSourceChange: (source: Source) => void
  onTipSearch: SearchInputProps['onChangeText']
  onSearch: SearchInputProps['onSubmit']
  onHideTipList: SearchInputProps['onBlur']
  onShowTipList: SearchInputProps['onTouchStart']
}

export interface HeaderBarType {
  setSourceList: (list: Sources, activeSource: Source) => void
  setText: SearchInputType['setText']
  blur: SearchInputType['blur']
}

/**
 * 搜索栏：圆角搜索框 + 右侧来源切换胶囊
 */
export default forwardRef<HeaderBarType, HeaderBarProps>(({ onSourceChange, onTipSearch, onSearch, onHideTipList, onShowTipList }, ref) => {
  const apple = useApple()
  const t = useI18n()
  const sourceNameType = useSettingValue('common.sourceNameType')
  const searchInputRef = useRef<SearchInputType>(null)
  const [list, setList] = useState<Sources>([])
  const [source, setSource] = useState<Source>('kw')

  useImperativeHandle(ref, () => ({
    setSourceList(newList, activeSource) {
      setList(newList)
      setSource(activeSource)
    },
    setText(text) {
      searchInputRef.current?.setText(text)
    },
    blur() {
      searchInputRef.current?.blur()
    },
  }), [])

  const sourceList_t = useSourceListI18n(list)
  type DorpDownMenuProps = _DorpDownMenuProps<typeof sourceList_t>

  const handleChangeSource: DorpDownMenuProps['onPress'] = ({ action }) => {
    onSourceChange(action as Source)
    setSource(action as Source)
  }

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
        <DorpDownMenu
          menus={sourceList_t}
          onPress={handleChangeSource}
          fontSize={fontSize.subhead}
          activeId={source}
          center
        >
          <View style={{ ...styles.pill, backgroundColor: apple.fill }}>
            <Text
              style={styles.pillText}
              size={fontSize.footnote}
              color={apple.accent}
              numberOfLines={1}
            >
              {t(`source_${sourceNameType}_${source}`)}
            </Text>
            <IonIcon name="chevron-down" rawSize={12} color={apple.accent} />
          </View>
        </DorpDownMenu>
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
    paddingRight: pagePadding,
    zIndex: 2,
  },
  input: {
    flex: 1,
    height: 36,
  },
  selector: {
    marginLeft: spacing.sm,
    flexShrink: 0,
  },
  pill: {
    height: 32,
    paddingLeft: 10,
    paddingRight: 8,
    borderRadius: radius.pill,
    flexDirection: 'row',
    alignItems: 'center',
  },
  pillText: {
    marginRight: spacing.xxs + 2,
    fontWeight: fontWeight.medium,
  },
})
