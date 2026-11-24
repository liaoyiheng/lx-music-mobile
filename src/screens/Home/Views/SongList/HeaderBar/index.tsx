import { forwardRef, useEffect, useImperativeHandle, useRef } from 'react'
import { ScrollView } from 'react-native'

import SortTab, { type SortTabProps, type SortTabType } from './SortTab'
import { createStyle } from '@/utils/tools'
import { type Source } from '@/store/songlist/state'
import Tag, { type TagType, type TagProps } from './Tag'
import { pagePadding, spacing } from '@/theme/apple'

export interface HeaderBarProps {
  source?: Source
  sortId?: string
  tagName?: string
  tagId?: string
  onSortChange: SortTabProps['onSortChange']
  onTagChange: TagProps['onTagChange']
}

export interface HeaderBarType {
  setSource: (source: Source, sortId: string, tagName: string, tagId: string) => void
}

/**
 * 歌单广场轻量标签与排序胶囊行（单行横向滚动）
 */
export default forwardRef<HeaderBarType, HeaderBarProps>(({
  source,
  sortId,
  tagName,
  tagId,
  onSortChange,
  onTagChange,
}, ref) => {
  const sortTabRef = useRef<SortTabType>(null)
  const tagRef = useRef<TagType>(null)
  const scrollViewRef = useRef<ScrollView>(null)

  useEffect(() => {
    if (!source) return
    sortTabRef.current?.setSource(source, sortId ?? '')
    tagRef.current?.setSelectedTagInfo(source, tagName ?? '', tagId ?? '')
  }, [source, sortId, tagName, tagId])

  useImperativeHandle(ref, () => ({
    setSource(nextSource, nextSortId, nextTagName, nextTagId) {
      scrollViewRef.current?.scrollTo({ x: 0, animated: false })
      sortTabRef.current?.setSource(nextSource, nextSortId)
      tagRef.current?.setSelectedTagInfo(nextSource, nextTagName, nextTagId)
    },
  }), [])

  return (
    <ScrollView
      ref={scrollViewRef}
      horizontal
      showsHorizontalScrollIndicator={false}
      keyboardShouldPersistTaps="always"
      contentContainerStyle={styles.content}
    >
      <Tag ref={tagRef} onTagChange={onTagChange} />
      <SortTab ref={sortTabRef} onSortChange={onSortChange} />
    </ScrollView>
  )
})

const styles = createStyle({
  content: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: pagePadding,
    paddingTop: spacing.md + 2,
    paddingBottom: spacing.xs,
  },
})
