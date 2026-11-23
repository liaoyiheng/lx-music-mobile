import { useMemo, useRef, useState, forwardRef, useImperativeHandle } from 'react'
import { FlatList, type FlatListProps, RefreshControl, TouchableOpacity, View } from 'react-native'
import CommentFloor from './CommentFloor'
import StateView from './StateView'
import { createStyle } from '@/utils/tools'
import { type Comment } from '../utils'
import { useI18n } from '@/lang'
import Text from '@/components/common/Text'
import Loading from '@/components/common/Loading'
import { useApple, fontSize, pagePadding, spacing } from '@/theme/apple'

type FlatListType = FlatListProps<Comment>

export interface ListProps {
  onRefresh: () => void
  onLoadMore: () => void
}
export interface ListType {
  setList: (list: Comment[]) => void
  getList: () => Comment[]
  setStatus: (val: Status) => void
}
export type Status = 'loading' | 'refreshing' | 'end' | 'error' | 'idle'

const List = forwardRef<ListType, ListProps>(({
  onRefresh,
  onLoadMore,
}, ref) => {
  const apple = useApple()
  const t = useI18n()
  const flatListRef = useRef<FlatList>(null)
  const [currentList, setList] = useState<Comment[]>([])
  const [status, setStatus] = useState<Status>('idle')

  useImperativeHandle(ref, () => ({
    setList(list) {
      setList(list)
    },
    getList() {
      return currentList
    },
    setStatus(val) {
      setStatus(val)
    },
  }))

  const handleLoadMore = () => {
    if (status != 'idle') return
    onLoadMore()
  }

  const renderItem: FlatListType['renderItem'] = ({ item }) => <CommentFloor comment={item} />
  const getkey: FlatListType['keyExtractor'] = item => item.id

  const refreshControl = useMemo(() => (
    <RefreshControl
      colors={[apple.accent]}
      tintColor={apple.accent}
      refreshing={status == 'refreshing'}
      onRefresh={onRefresh} />
  ), [status, onRefresh, apple])

  const isEmpty = !currentList.length

  const footerComponent = useMemo(() => {
    if (isEmpty) return null
    return <Footer status={status} onLoadMore={onLoadMore} />
  }, [isEmpty, onLoadMore, status])

  const emptyComponent = useMemo(() => {
    switch (status) {
      case 'refreshing': return null
      case 'loading': return <StateView loading title={t('apple_comment_loading')} />
      case 'error': return <StateView icon="comment" title={t('apple_comment_error')} actionLabel={t('apple_comment_retry')} onAction={onLoadMore} />
      default: return <StateView icon="comment" title={t('apple_comment_empty')} />
    }
  }, [status, onLoadMore, t])

  return (
    <FlatList
      ref={flatListRef}
      style={styles.list}
      contentContainerStyle={styles.content}
      data={currentList}
      onEndReachedThreshold={0.5}
      removeClippedSubviews={false}
      renderItem={renderItem}
      keyExtractor={getkey}
      onEndReached={handleLoadMore}
      refreshControl={refreshControl}
      ListFooterComponent={footerComponent}
      ListEmptyComponent={emptyComponent}
    />
  )
})

const Footer = ({ status, onLoadMore }: {
  status: Status
  onLoadMore: () => void
}) => {
  const apple = useApple()
  const t = useI18n()

  switch (status) {
    case 'loading':
      return (
        <View style={styles.footer}>
          <Loading color={apple.secondaryLabel} />
          <Text style={styles.footerText} size={fontSize.footnote} color={apple.secondaryLabel}>{t('apple_comment_loading')}</Text>
        </View>
      )
    case 'end':
      return (
        <View style={styles.footer}>
          <Text size={fontSize.footnote} color={apple.tertiaryLabel}>{t('apple_comment_end')}</Text>
        </View>
      )
    case 'error':
      return (
        <TouchableOpacity style={styles.footer} activeOpacity={0.5} onPress={onLoadMore}>
          <Text size={fontSize.footnote} color={apple.accent}>{t('apple_comment_load_more_error')}</Text>
        </TouchableOpacity>
      )
    default:
      return null
  }
}

const styles = createStyle({
  list: {
    flexGrow: 1,
    flexShrink: 1,
  },
  content: {
    flexGrow: 1,
    paddingHorizontal: pagePadding,
    paddingBottom: spacing.xl,
  },
  footer: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: spacing.lg,
  },
  footerText: {
    marginLeft: spacing.sm,
  },
})

export default List
