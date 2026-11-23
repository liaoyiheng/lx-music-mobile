import { forwardRef, useCallback, useEffect, useImperativeHandle, useRef, useState } from 'react'
import { TouchableOpacity, View } from 'react-native'
import { type InitState } from '@/store/hotSearch/state'
import Text from '@/components/common/Text'
import { createStyle } from '@/utils/tools'
import { useI18n } from '@/lang'
import { clearHistoryList, getSearchHistory, removeHistoryWord } from '@/core/search/search'
import { Icon } from '@/components/common/Icon'
import { useApple, fontSize, fontWeight, hairline, size, spacing } from '@/theme/apple'

export type List = NonNullable<InitState['sourceList'][keyof InitState['sourceList']]>

/**
 * 最近搜索行：搜索图标 + 关键词 + 右侧删除；长按也可删除（沿用原版）
 */
const ListItem = ({ keyword, onSearch, onRemove }: {
  keyword: string
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
        <Icon name="search-2" size={14} color={apple.secondaryLabel} />
        <Text style={styles.keyword} size={fontSize.body} color={apple.label} numberOfLines={1}>{keyword}</Text>
      </TouchableOpacity>
      <TouchableOpacity style={styles.removeBtn} activeOpacity={0.6} onPress={() => { onRemove(keyword) }}>
        <Icon name="close" size={11} color={apple.tertiaryLabel} />
      </TouchableOpacity>
      <View pointerEvents="none" style={{ ...styles.separator, backgroundColor: apple.separator }} />
    </View>
  )
}

interface HistorySearchProps {
  onSearch: (keyword: string) => void
}
export interface HistorySearchType {
  show: () => void
}

export default forwardRef<HistorySearchType, HistorySearchProps>((props, ref) => {
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
      void getSearchHistory().then((list) => {
        if (isUnmountedRef.current) return
        setList(list)
      })
    },
  }), [])

  const handleClear = () => {
    clearHistoryList()
    setList([])
  }

  const handleRemove = useCallback((keyword: string) => {
    setList(list => {
      list = [...list]
      const index = list.indexOf(keyword)
      list.splice(index, 1)
      removeHistoryWord(index)
      return list
    })
  }, [])

  return (
    list.length
      ? (
          <View style={styles.section}>
            <View style={styles.titleContent}>
              <Text style={styles.title} size={fontSize.title3} color={apple.label}>{t('search_history_search')}</Text>
              <TouchableOpacity onPress={handleClear} style={styles.clearBtn} activeOpacity={0.5}>
                <Text size={fontSize.subhead} color={apple.accent}>{t('apple_clear')}</Text>
              </TouchableOpacity>
            </View>
            <View>
              {
                list.map(keyword => <ListItem keyword={keyword} key={keyword} onSearch={props.onSearch} onRemove={handleRemove} />)
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
  titleContent: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  title: {
    flex: 1,
    fontWeight: fontWeight.bold,
  },
  clearBtn: {
    paddingVertical: spacing.xs,
    paddingLeft: spacing.md,
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
    left: 26,
    right: 0,
    bottom: 0,
    height: hairline,
  },
})
