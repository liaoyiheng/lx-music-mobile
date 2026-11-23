import { forwardRef, useImperativeHandle, useRef } from 'react'
import { View } from 'react-native'

import SortTab, { type SortTabProps, type SortTabType } from './SortTab'
import { createStyle } from '@/utils/tools'
import SourceSelector, {
  type SourceSelectorType,
  type SourceSelectorProps,
} from './SourceSelector'
import { type Source } from '@/store/songlist/state'
import Tag, { type TagType, type TagProps } from './Tag'
import OpenList, { type OpenListType } from './OpenList'
import { pagePadding, spacing } from '@/theme/apple'

export interface HeaderBarProps {
  onSortChange: SortTabProps['onSortChange']
  onTagChange: TagProps['onTagChange']
  onSourceChange: SourceSelectorProps['onSourceChange']
}

export interface HeaderBarType {
  setSource: (source: Source, sortId: string, tagName: string, tagId: string) => void
}

/**
 * 歌单单行紧凑工具条
 * 左侧：分类标签胶囊 + 排序胶囊
 * 右侧：打开歌单图标按钮 + 来源胶囊
 */
export default forwardRef<HeaderBarType, HeaderBarProps>(({ onSortChange, onTagChange, onSourceChange }, ref) => {
  const sortTabRef = useRef<SortTabType>(null)
  const tagRef = useRef<TagType>(null)
  const openListRef = useRef<OpenListType>(null)
  const sourceSelectorRef = useRef<SourceSelectorType>(null)

  useImperativeHandle(ref, () => ({
    setSource(source, sortId, tagName, tagId) {
      sortTabRef.current?.setSource(source, sortId)
      tagRef.current?.setSelectedTagInfo(source, tagName, tagId)
      sourceSelectorRef.current?.setSource(source)
      openListRef.current?.setInfo(source)
    },
  }), [])

  return (
    <View style={styles.container}>
      <Tag ref={tagRef} onTagChange={onTagChange} />
      <View style={styles.sortWrap}>
        <SortTab ref={sortTabRef} onSortChange={onSortChange} />
      </View>
      <View style={styles.right}>
        <OpenList ref={openListRef} />
        <View style={styles.gap} />
        <SourceSelector ref={sourceSelectorRef} onSourceChange={onSourceChange} />
      </View>
    </View>
  )
})

const styles = createStyle({
  container: {
    zIndex: 2,
    flexDirection: 'row',
    alignItems: 'center',
    height: 40,
    paddingHorizontal: pagePadding,
  },
  sortWrap: {
    flex: 1,
    marginHorizontal: spacing.sm,
    justifyContent: 'center',
  },
  right: {
    flexDirection: 'row',
    alignItems: 'center',
    flexShrink: 0,
  },
  gap: {
    width: spacing.xs,
  },
})
