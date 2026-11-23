import { forwardRef, useEffect, useImperativeHandle, useRef, useState } from 'react'
import { View } from 'react-native'
import { type Source, type InitState } from '@/store/hotSearch/state'
import Button from '@/components/common/Button'
import { getList } from '@/core/hotSearch'
import Text from '@/components/common/Text'
import { createStyle } from '@/utils/tools'
import { useI18n } from '@/lang'
import { useApple, fontSize, fontWeight, radius, spacing } from '@/theme/apple'

interface ListProps {
  onSearch: (keyword: string) => void
}
export interface HotSearchType {
  show: (source: Source) => void
}

export type List = NonNullable<InitState['sourceList'][keyof InitState['sourceList']]>

const ListItem = ({ keyword, onSearch }: {
  keyword: string
  onSearch: (keyword: string) => void
}) => {
  const apple = useApple()
  return (
    <Button style={{ ...styles.chip, backgroundColor: apple.fill }} onPress={() => { onSearch(keyword) }}>
      <Text color={apple.label} size={fontSize.subhead} numberOfLines={1}>{keyword}</Text>
    </Button>
  )
}

/**
 * 热门搜索：小标题 + 胶囊关键词
 */
export default forwardRef<HotSearchType, ListProps>((props, ref) => {
  const [list, setList] = useState<List>([])
  const t = useI18n()
  const apple = useApple()

  const isUnmountedRef = useRef(false)
  useEffect(() => {
    isUnmountedRef.current = false
    return () => {
      isUnmountedRef.current = true
    }
  }, [])

  useImperativeHandle(ref, () => ({
    show(source) {
      void getList(source).then((list) => {
        if (isUnmountedRef.current) return
        setList(list)
      })
    },
  }), [])

  return (
    list.length
      ? (
          <View style={styles.section}>
            <Text style={styles.title} size={fontSize.title3} color={apple.label}>{t('search_hot_search')}</Text>
            <View style={styles.list}>
              {
                list.map(keyword => <ListItem keyword={keyword} key={keyword} onSearch={props.onSearch} />)
              }
            </View>
          </View>
        )
      : null
  )
})

const styles = createStyle({
  section: {
    paddingTop: spacing.xl,
  },
  title: {
    fontWeight: fontWeight.bold,
  },
  list: {
    flexDirection: 'row',
    flexWrap: 'wrap',
  },
  chip: {
    height: 32,
    maxWidth: '100%',
    paddingHorizontal: 14,
    borderRadius: radius.pill,
    justifyContent: 'center',
    marginRight: spacing.sm,
    marginTop: spacing.sm + 2,
  },
})
