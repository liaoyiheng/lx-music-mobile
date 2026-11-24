import { useRef, useState, useCallback, useMemo, forwardRef, useImperativeHandle, type Ref } from 'react'
import { StyleSheet, View, Animated } from 'react-native'
import { GlassSurface } from '@/components/apple/LiquidGlass'
import { pagePadding, radius, spacing } from '@/theme/apple'
import List, { type ItemT, type ListProps, type ListType } from './List'

export interface SearchTipListProps<T> extends ListProps<T> {
  onPressBg?: () => void
}
export interface SearchTipListType<T> {
  setList: (list: T[]) => void
  setHeight: (height: number) => void
}

const noop = () => {}

/**
 * 液态玻璃浮动搜索联想弹层
 */
const Component = <T extends ItemT<T>>({ onPressBg = noop, ...props }: SearchTipListProps<T>, ref: Ref<SearchTipListType<T>>) => {
  const translateY = useRef(new Animated.Value(-8)).current
  const opacity = useRef(new Animated.Value(0)).current
  const [visible, setVisible] = useState(false)
  const [animatePlayed, setAnimatPlayed] = useState(true)
  const listRef = useRef<ListType<T>>(null)
  const prevListRef = useRef<T[]>([])
  const heightRef = useRef(0)

  const handleShow = useCallback(() => {
    setVisible(true)
    setAnimatPlayed(false)
    requestAnimationFrame(() => {
      translateY.setValue(-8)
      opacity.setValue(0)

      Animated.parallel([
        Animated.timing(translateY, {
          toValue: 0,
          duration: 180,
          useNativeDriver: true,
        }),
        Animated.timing(opacity, {
          toValue: 1,
          duration: 180,
          useNativeDriver: true,
        }),
      ]).start(() => {
        setAnimatPlayed(true)
      })
    })
  }, [translateY, opacity])

  const handleHide = useCallback(() => {
    setAnimatPlayed(false)
    Animated.parallel([
      Animated.timing(translateY, {
        toValue: -8,
        duration: 150,
        useNativeDriver: true,
      }),
      Animated.timing(opacity, {
        toValue: 0,
        duration: 150,
        useNativeDriver: true,
      }),
    ]).start((finished) => {
      if (!finished) return
      setVisible(false)
      setAnimatPlayed(true)
    })
  }, [translateY, opacity])

  useImperativeHandle(ref, () => ({
    setList(list) {
      if (prevListRef.current.length) {
        if (!list.length) handleHide()
      } else if (list.length) handleShow()
      prevListRef.current = list
      requestAnimationFrame(() => {
        listRef.current?.setList(list)
      })
    },
    setHeight(height) {
      heightRef.current = height
    },
  }))

  const component = useMemo(() => (
    <Animated.View
      style={{
        ...styles.anima,
        opacity,
        transform: [{ translateY }],
      }}
    >
      <View style={styles.sheetWrap}>
        <GlassSurface dense borderRadius={radius.sheet} innerStyle={styles.glassInner}>
          <List ref={listRef} {...props} />
        </GlassSurface>
      </View>
      <View style={styles.blank} onTouchStart={onPressBg} />
    </Animated.View>
  ), [onPressBg, props, opacity, translateY])

  return !visible && animatePlayed ? null : component
}

export default forwardRef(Component) as
  <T,>(p: SearchTipListProps<T> & { ref?: Ref<SearchTipListType<T>> }) => JSX.Element | null

const styles = StyleSheet.create({
  anima: {
    position: 'absolute',
    left: 0,
    top: 0,
    height: '100%',
    width: '100%',
    zIndex: 10,
  },
  sheetWrap: {
    paddingHorizontal: pagePadding,
    paddingTop: spacing.xs,
    maxHeight: '78%',
  },
  glassInner: {
    maxHeight: '100%',
  },
  blank: {
    flex: 1,
    flexGrow: 1,
  },
})
