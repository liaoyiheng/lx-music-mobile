import { useState, useRef, useCallback, useMemo, forwardRef, useImperativeHandle } from 'react'
import { Animated, View, TouchableOpacity } from 'react-native'

import Text from '@/components/common/Text'
import Input, { type InputType } from '@/components/common/Input'
import { GlassSurface } from '@/components/apple/LiquidGlass'
import { IonIcon } from '@/components/common/Icon'
import { useI18n } from '@/lang'
import { createStyle } from '@/utils/tools'
import { useApple, fontSize, fontWeight, pagePadding, radius, spacing } from '@/theme/apple'

interface SearchInputProps {
  onSearch: (keywork: string) => void
  placeholder: string
}
type SearchInputType = InputType

const SearchInput = forwardRef<SearchInputType, SearchInputProps>(({ onSearch, placeholder }, ref) => {
  const [text, setText] = useState('')

  const handleChangeText = (text: string) => {
    setText(text)
    onSearch(text.trim())
  }

  return (
    <Input
      onChangeText={handleChangeText}
      placeholder={placeholder}
      value={text}
      style={styles.input}
      clearBtn
      ref={ref}
    />
  )
})


export interface ListSearchBarProps {
  onSearch: (keywork: string) => void
  onExitSearch: () => void
}
export interface ListSearchBarType {
  show: () => void
  hide: () => void
}

export default forwardRef<ListSearchBarType, ListSearchBarProps>(({ onSearch, onExitSearch }, ref) => {
  const t = useI18n()
  const apple = useApple()
  const [visible, setVisible] = useState(false)
  const [animatePlayed, setAnimatPlayed] = useState(true)
  const animFade = useRef(new Animated.Value(0)).current
  const animTranslateY = useRef(new Animated.Value(0)).current
  const searchInputRef = useRef<SearchInputType>(null)

  useImperativeHandle(ref, () => ({
    show() {
      handleShow()
      requestAnimationFrame(() => {
        searchInputRef.current?.focus()
      })
    },
    hide() {
      handleHide()
    },
  }))


  const handleShow = useCallback(() => {
    setVisible(true)
    setAnimatPlayed(false)
    requestAnimationFrame(() => {
      animTranslateY.setValue(-16)

      Animated.parallel([
        Animated.timing(animFade, {
          toValue: 1,
          duration: 200,
          useNativeDriver: true,
        }),
        Animated.timing(animTranslateY, {
          toValue: 0,
          duration: 200,
          useNativeDriver: true,
        }),
      ]).start(() => {
        setAnimatPlayed(true)
      })
    })
  }, [animFade, animTranslateY])

  const handleHide = useCallback(() => {
    setAnimatPlayed(false)
    Animated.parallel([
      Animated.timing(animFade, {
        toValue: 0,
        duration: 200,
        useNativeDriver: true,
      }),
      Animated.timing(animTranslateY, {
        toValue: -16,
        duration: 200,
        useNativeDriver: true,
      }),
    ]).start(finished => {
      if (!finished) return
      setVisible(false)
      setAnimatPlayed(true)
    })
  }, [animFade, animTranslateY])


  const animaStyle = useMemo(() => ({
    ...styles.container,
    opacity: animFade,
    transform: [
      { translateY: animTranslateY },
    ],
  }), [animFade, animTranslateY])

  const component = useMemo(() => {
    return (
      <Animated.View style={animaStyle}>
        <GlassSurface dense borderRadius={radius.pill} style={styles.glassWrap} innerStyle={styles.glassInner}>
          <IonIcon name="search" rawSize={16} color={apple.secondaryLabel} style={styles.searchIcon} />
          <View style={styles.content}>
            <SearchInput ref={searchInputRef} onSearch={onSearch} placeholder={t('apple_search_placeholder')} />
          </View>
        </GlassSurface>
        <TouchableOpacity onPress={onExitSearch} activeOpacity={0.68} style={styles.btn}>
          <Text size={fontSize.callout} color={apple.accent} style={styles.btnText}>
            {t('apple_cancel')}
          </Text>
        </TouchableOpacity>
      </Animated.View>
    )
  }, [animaStyle, apple, onSearch, onExitSearch, t])

  return !visible && animatePlayed ? null : component
})

const styles = createStyle({
  container: {
    position: 'absolute',
    left: pagePadding,
    right: pagePadding,
    top: 2,
    bottom: 2,
    flexDirection: 'row',
    alignItems: 'center',
  },
  glassWrap: {
    flex: 1,
  },
  glassInner: {
    height: 38,
    flexDirection: 'row',
    alignItems: 'center',
    paddingLeft: 12,
    paddingRight: 4,
  },
  searchIcon: {
    marginRight: 2,
  },
  content: {
    flexDirection: 'row',
    flex: 1,
  },
  input: {
    height: '100%',
    backgroundColor: 'transparent',
    paddingLeft: 4,
  },
  btn: {
    paddingLeft: spacing.md,
    paddingRight: spacing.xs,
    height: 38,
    alignItems: 'center',
    justifyContent: 'center',
  },
  btnText: {
    fontWeight: fontWeight.medium,
  },
})
