import { useEffect, useState, useRef, forwardRef, useImperativeHandle } from 'react'
import { View, ScrollView } from 'react-native'

import { createStyle } from '@/utils/tools'
import TagGroup, { type TagGroupProps } from './TagGroup'
import { useI18n } from '@/lang'
import { type TagInfo, type Source } from '@/store/songlist/state'
import { getTags } from '@/core/songlist'
import { useHorizontalMode } from '@/utils/hooks'
import { useApple, fontSize, pagePadding, size, spacing } from '@/theme/apple'
import Text from '@/components/common/Text'

export interface ListProps {
  onTagChange: TagGroupProps['onTagChange']
}

export interface ListType {
  loadTag: (source: Source, activeId: string) => void
}

export default forwardRef<ListType, ListProps>(({ onTagChange }, ref) => {
  const apple = useApple()
  const isWide = useHorizontalMode()
  const [activeId, setActiveId] = useState('')
  const [list, setList] = useState<TagInfo['tags']>([])
  const t = useI18n()
  const prevSource = useRef('')

  const isUnmountedRef = useRef(false)
  useEffect(() => {
    isUnmountedRef.current = false
    return () => {
      isUnmountedRef.current = true
    }
  }, [])

  useImperativeHandle(ref, () => ({
    loadTag(source, id) {
      if (id != activeId) setActiveId(id)
      if (source != prevSource.current) {
        setList([{ name: '', list: [{ name: t('songlist_tag_default'), id: '', parent_id: '', parent_name: '', source }] }])
        void getTags(source).then(tagInfo => {
          if (isUnmountedRef.current) return
          prevSource.current = source
          setList([
            { name: '', list: [{ name: t('songlist_tag_default'), id: '', parent_id: '', parent_name: '', source }] },
            { name: t('songlist_tag_hot'), list: [...tagInfo.hotTag] },
            ...tagInfo.tags,
          ].filter(item => item.list.length))
        })
      }
    },
  }))

  return (
    <ScrollView
      style={styles.scroll}
      contentContainerStyle={{
        ...styles.tagContainer,
        paddingBottom: isWide ? size.miniPlayerClearance : size.dockClearance,
      }}
      keyboardShouldPersistTaps="always"
    >
      <View onStartShouldSetResponder={() => true}>
        {
          list.map((type, index) => (
            <TagGroup
              key={index}
              name={type.name}
              list={type.list}
              activeId={activeId}
              onTagChange={onTagChange}
            />
          ))
        }
        {
          list.length == 1
            ? (
                <View style={styles.blankView}>
                  <Text size={fontSize.subhead} color={apple.secondaryLabel}>{t('list_loading')}</Text>
                </View>
              )
            : null
        }
      </View>
    </ScrollView>
  )
})

const styles = createStyle({
  scroll: {
    flex: 1,
  },
  tagContainer: {
    paddingTop: spacing.lg,
    paddingHorizontal: pagePadding,
  },
  blankView: {
    paddingTop: '15%',
    paddingBottom: '15%',
    alignItems: 'center',
    justifyContent: 'center',
  },
})
