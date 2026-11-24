import { forwardRef, useImperativeHandle, useMemo, useState } from 'react'
import { TouchableOpacity, View } from 'react-native'
import songlistState, { type SortInfo, type Source } from '@/store/songlist/state'
import { useI18n } from '@/lang'
import Text from '@/components/common/Text'
import { createStyle } from '@/utils/tools'
import { useApple, fontWeight, radius, spacing } from '@/theme/apple'

export interface SortTabProps {
  onSortChange: (id: string) => void
}

export interface SortTabType {
  setSource: (source: Source, activeTab: SortInfo['id']) => void
}

export default forwardRef<SortTabType, SortTabProps>(({ onSortChange }, ref) => {
  const [sortList, setSortList] = useState<SortInfo[]>([])
  const [activeId, setActiveId] = useState<SortInfo['id']>('')
  const t = useI18n()
  const apple = useApple()

  useImperativeHandle(ref, () => ({
    setSource(source, activeTab) {
      setSortList(songlistState.sortList[source]!)
      setActiveId(activeTab)
    },
  }))

  const sorts = useMemo(() => {
    return sortList.map(s => ({ label: t(`songlist_${s.tid}`), id: s.id }))
  }, [sortList, t])

  const handleSortChange = (id: string) => {
    onSortChange(id)
    setActiveId(id)
  }

  return (
    <View style={styles.row}>
      {
        sorts.map(s => {
          const active = activeId == s.id
          return (
            <TouchableOpacity
              key={s.id}
              activeOpacity={0.68}
              style={{ ...styles.chip, backgroundColor: active ? apple.accent : apple.fill }}
              onPress={() => { if (!active) handleSortChange(s.id) }}
            >
              <Text
                size={13.5}
                color={active ? apple.onAccent : apple.label}
                style={{ fontWeight: active ? fontWeight.semibold : fontWeight.medium }}
              >{s.label}</Text>
            </TouchableOpacity>
          )
        })
      }
    </View>
  )
})

const styles = createStyle({
  row: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  chip: {
    height: 30,
    paddingHorizontal: 13,
    marginRight: spacing.sm,
    borderRadius: radius.pill,
    justifyContent: 'center',
  },
})
