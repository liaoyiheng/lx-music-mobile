import { forwardRef, memo, useEffect, useImperativeHandle, useRef, useState } from 'react'
import { StyleSheet, TouchableOpacity, View } from 'react-native'
import { type Source, type InitState } from '@/store/hotSearch/state'
import { getList } from '@/core/hotSearch'
import Text from '@/components/common/Text'
import { createStyle } from '@/utils/tools'
import { useI18n } from '@/lang'
import { useSettingValue } from '@/store/setting/hook'
import { useApple, fontSize, fontWeight, radius, spacing } from '@/theme/apple'

interface ListProps {
  onSearch: (keyword: string) => void
  onEmptyChange?: (isEmpty: boolean) => void
}
export interface HotSearchType {
  show: (source: Source) => void
}

export type List = NonNullable<InitState['sourceList'][keyof InitState['sourceList']]>

const HotChip = memo(({ keyword, onSearch }: {
  keyword: string
  onSearch: (keyword: string) => void
}) => {
  const apple = useApple()
  return (
    <TouchableOpacity
      activeOpacity={0.65}
      style={[
        styles.chip,
        {
          backgroundColor: apple.fill,
          borderColor: apple.isDark ? 'rgba(255, 255, 255, 0.10)' : 'rgba(0, 0, 0, 0.06)',
        },
      ]}
      onPress={() => { onSearch(keyword) }}
    >
      <Text color={apple.label} size={13.5} style={styles.chipText} numberOfLines={1}>
        {keyword}
      </Text>
    </TouchableOpacity>
  )
})

/**
 * 热门搜索：紧凑标题行（含当前音源提示）+ 轻量玻璃胶囊云
 */
export default forwardRef<HotSearchType, ListProps>(({ onSearch, onEmptyChange }, ref) => {
  const [list, setList] = useState<List>([])
  const [currentSource, setCurrentSource] = useState<Source>('kw')
  const t = useI18n()
  const apple = useApple()
  const sourceNameType = useSettingValue('common.sourceNameType')

  const isUnmountedRef = useRef(false)
  useEffect(() => {
    isUnmountedRef.current = false
    return () => {
      isUnmountedRef.current = true
    }
  }, [])

  useImperativeHandle(ref, () => ({
    show(source) {
      setCurrentSource(source)
      void getList(source).then((nextList) => {
        if (isUnmountedRef.current) return
        setList(nextList)
        onEmptyChange?.(nextList.length === 0)
      })
    },
  }), [onEmptyChange])

  if (!list.length) return null

  return (
    <View style={styles.section}>
      <View style={styles.header}>
        <Text style={styles.title} size={fontSize.footnote} color={apple.secondaryLabel}>
          {t('search_hot_search')}
        </Text>
        <Text style={styles.sourceHint} size={11.5} color={apple.tertiaryLabel} numberOfLines={1}>
          {t(`source_${sourceNameType}_${currentSource}`)}
        </Text>
      </View>
      <View style={styles.list}>
        {list.map(keyword => <HotChip keyword={keyword} key={keyword} onSearch={onSearch} />)}
      </View>
    </View>
  )
})

const styles = createStyle({
  section: {
    paddingTop: spacing.xs,
    paddingBottom: spacing.xs,
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: spacing.sm,
  },
  title: {
    fontWeight: fontWeight.semibold,
  },
  sourceHint: {
    fontWeight: fontWeight.medium,
  },
  list: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 7,
  },
  chip: {
    height: 30,
    maxWidth: '100%',
    paddingHorizontal: 13,
    borderRadius: radius.pill,
    borderWidth: StyleSheet.hairlineWidth,
    justifyContent: 'center',
    alignItems: 'center',
  },
  chipText: {
    fontWeight: fontWeight.medium,
  },
})
