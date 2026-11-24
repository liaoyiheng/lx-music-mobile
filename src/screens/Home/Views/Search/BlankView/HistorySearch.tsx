import { forwardRef, memo, useCallback, useEffect, useImperativeHandle, useRef, useState } from 'react'
import { TouchableOpacity, View } from 'react-native'
import { type InitState } from '@/store/hotSearch/state'
import Text from '@/components/common/Text'
import { createStyle } from '@/utils/tools'
import { useI18n } from '@/lang'
import { clearHistoryList, getSearchHistory, removeHistoryWord } from '@/core/search/search'
import { IonIcon } from '@/components/common/Icon'
import { useApple, fontSize, fontWeight, hairline, size, spacing } from '@/theme/apple'

export type List = NonNullable<InitState['sourceList'][keyof InitState['sourceList']]>

/**
 * 最近搜索行：搜索图标 + 关键词 + 右侧删除 + 左缩进 hairline 分割线
 */
const ListItem = memo(({ keyword, isLast, onSearch, onRemove }: {
  keyword: string
  isLast: boolean
  onSearch: (keyword: string) => void
  onRemove: (keyword: string) => void
}) => {
  const apple = useApple()
  return (
    <View style={styles.row}>
      <TouchableOpacity
        style={styles.rowMain}
        activeOpacity={0.6}
        onPress={() => { onSearch(keyword) }}
        onLongPress={() => { onRemove(keyword) }}
      >
        <IonIcon name="search" rawSize={15} color={apple.secondaryLabel} />
        <Text style={styles.keyword} size={fontSize.callout} color={apple.label} numberOfLines={1}>
          {keyword}
        </Text>
      </TouchableOpacity>
      <TouchableOpacity
        style={styles.removeBtn}
        activeOpacity={0.6}
        onPress={() => { onRemove(keyword) }}
        hitSlop={{ top: 6, bottom: 6, left: 6, right: 6 }}
      >
        <IonIcon name="close" rawSize={16} color={apple.tertiaryLabel} />
      </TouchableOpacity>
      {!isLast ? (
        <View pointerEvents="none" style={{ ...styles.separator, backgroundColor: apple.separator }} />
      ) : null}
    </View>
  )
})

interface HistorySearchProps {
  onSearch: (keyword: string) => void
  onEmptyChange?: (isEmpty: boolean) => void
}
export interface HistorySearchType {
  show: () => void
}

export default forwardRef<HistorySearchType, HistorySearchProps>(({ onSearch, onEmptyChange }, ref) => {
  const [list, setList] = useState<List>([])
  const isUnmountedRef = useRef(false)
  const t = useI18n()
  const apple = useApple()

  useEffect(() => {
    isUnmountedRef.current = false
    return () => {
      isUnmountedRef.current = true
    }
  }, [])

  useImperativeHandle(ref, () => ({
    show() {
      void getSearchHistory().then((history) => {
        if (isUnmountedRef.current) return
        setList(history)
        onEmptyChange?.(history.length === 0)
      })
    },
  }), [onEmptyChange])

  const handleClear = useCallback(() => {
    clearHistoryList()
    setList([])
    onEmptyChange?.(true)
  }, [onEmptyChange])

  const handleRemove = useCallback((keyword: string) => {
    setList(prev => {
      const next = [...prev]
      const index = next.indexOf(keyword)
      if (index < 0) return prev
      next.splice(index, 1)
      removeHistoryWord(index)
      onEmptyChange?.(next.length === 0)
      return next
    })
  }, [onEmptyChange])

  if (!list.length) return null

  return (
    <View style={styles.section}>
      <View style={styles.titleContent}>
        <Text style={styles.title} size={fontSize.title3} color={apple.label}>
          {t('search_history_search')}
        </Text>
        <TouchableOpacity
          onPress={handleClear}
          style={styles.clearBtn}
          activeOpacity={0.55}
          hitSlop={{ top: 6, bottom: 6, left: 8, right: 6 }}
        >
          <Text size={fontSize.subhead} color={apple.accent} style={styles.clearText}>
            {t('apple_clear')}
          </Text>
        </TouchableOpacity>
      </View>
      <View>
        {list.map((keyword, idx) => (
          <ListItem
            key={keyword}
            keyword={keyword}
            isLast={idx === list.length - 1}
            onSearch={onSearch}
            onRemove={handleRemove}
          />
        ))}
      </View>
    </View>
  )
})

const styles = createStyle({
  section: {
    paddingTop: spacing.lg + 2,
  },
  titleContent: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: spacing.xs,
  },
  title: {
    flex: 1,
    fontWeight: fontWeight.bold,
    letterSpacing: -0.3,
  },
  clearBtn: {
    paddingVertical: spacing.xs,
    paddingLeft: spacing.md,
  },
  clearText: {
    fontWeight: fontWeight.medium,
  },
  row: {
    height: size.settingRow,
    flexDirection: 'row',
    alignItems: 'center',
  },
  rowMain: {
    flex: 1,
    height: '100%',
    flexDirection: 'row',
    alignItems: 'center',
  },
  keyword: {
    flex: 1,
    marginLeft: spacing.md,
  },
  removeBtn: {
    width: size.hitTarget,
    height: '100%',
    alignItems: 'flex-end',
    justifyContent: 'center',
  },
  separator: {
    position: 'absolute',
    left: 27,
    right: 0,
    bottom: 0,
    height: hairline,
  },
})
