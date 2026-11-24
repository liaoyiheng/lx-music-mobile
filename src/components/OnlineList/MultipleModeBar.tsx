import { useState, useRef, useCallback, useMemo, forwardRef, useImperativeHandle } from 'react'
import { Animated, StyleSheet, View, TouchableOpacity } from 'react-native'

import Text from '@/components/common/Text'
import { GlassSurface } from '@/components/apple/LiquidGlass'
import { useApple, fontSize, fontWeight, radius, spacing } from '@/theme/apple'
import { createStyle } from '@/utils/tools'
import { scaleSizeH } from '@/utils/pixelRatio'

export type SelectMode = 'single' | 'range'

export const MULTI_SELECT_BAR_HEIGHT = scaleSizeH(48)

export interface MultipleModeBarProps {
  onSwitchMode: (mode: SelectMode) => void
  onSelectAll: (isAll: boolean) => void
  onExitSelectMode: () => void
  bottomOffset?: number
}
export interface MultipleModeBarType {
  show: () => void
  setIsSelectAll: (isAll: boolean) => void
  setSwitchMode: (mode: SelectMode) => void
  exitSelectMode: () => void
}

export default forwardRef<MultipleModeBarType, MultipleModeBarProps>(({ onSelectAll, onSwitchMode, onExitSelectMode, bottomOffset = 14 }, ref) => {
  const [visible, setVisible] = useState(false)
  const [animatePlayed, setAnimatPlayed] = useState(true)
  const animFade = useRef(new Animated.Value(0)).current
  const animTranslateY = useRef(new Animated.Value(0)).current
  const [selectMode, setSelectMode] = useState<SelectMode>('single')
  const [isSelectAll, setIsSelectAll] = useState(false)
  const apple = useApple()

  useImperativeHandle(ref, () => ({
    show() {
      handleShow()
    },
    setVisibleBar() {
      // noop compatibility
    },
    setIsSelectAll(isAll) {
      setIsSelectAll(isAll)
    },
    setSwitchMode(mode: SelectMode) {
      setSelectMode(mode)
    },
    exitSelectMode() {
      handleHide()
    },
  }))

  const handleShow = useCallback(() => {
    setVisible(true)
    setAnimatPlayed(false)
    requestAnimationFrame(() => {
      animTranslateY.setValue(20)

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
        toValue: 20,
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
    bottom: bottomOffset,
    opacity: animFade,
    transform: [
      { translateY: animTranslateY },
    ],
  }), [animFade, animTranslateY, bottomOffset])

  const handleSelectAll = useCallback(() => {
    const selectAll = !isSelectAll
    setIsSelectAll(selectAll)
    onSelectAll(selectAll)
  }, [isSelectAll, onSelectAll])

  const component = useMemo(() => {
    return (
      <Animated.View style={animaStyle} pointerEvents="box-none">
        <GlassSurface dense borderRadius={radius.pill} innerStyle={styles.glassInner}>
          <View style={[styles.switchGroup, { backgroundColor: apple.fill }]}>
            <TouchableOpacity
              activeOpacity={0.72}
              onPress={() => { onSwitchMode('single') }}
              style={[
                styles.switchItem,
                selectMode == 'single' && {
                  ...styles.switchActive,
                  backgroundColor: apple.glassPillBg,
                  borderColor: apple.glassPillBorder,
                },
              ]}
            >
              <Text
                size={fontSize.footnote}
                color={selectMode == 'single' ? apple.label : apple.secondaryLabel}
                style={{ fontWeight: selectMode == 'single' ? fontWeight.semibold : fontWeight.medium }}
              >
                {global.i18n.t('list_select_single')}
              </Text>
            </TouchableOpacity>
            <TouchableOpacity
              activeOpacity={0.72}
              onPress={() => { onSwitchMode('range') }}
              style={[
                styles.switchItem,
                selectMode == 'range' && {
                  ...styles.switchActive,
                  backgroundColor: apple.glassPillBg,
                  borderColor: apple.glassPillBorder,
                },
              ]}
            >
              <Text
                size={fontSize.footnote}
                color={selectMode == 'range' ? apple.label : apple.secondaryLabel}
                style={{ fontWeight: selectMode == 'range' ? fontWeight.semibold : fontWeight.medium }}
              >
                {global.i18n.t('list_select_range')}
              </Text>
            </TouchableOpacity>
          </View>

          <View style={styles.actions}>
            <TouchableOpacity onPress={handleSelectAll} activeOpacity={0.68} style={styles.actionBtn}>
              <Text size={fontSize.subhead} color={apple.accent} style={styles.actionText}>
                {global.i18n.t(isSelectAll ? 'list_select_unall' : 'list_select_all')}
              </Text>
            </TouchableOpacity>
            <TouchableOpacity onPress={onExitSelectMode} activeOpacity={0.68} style={styles.actionBtn}>
              <Text size={fontSize.subhead} color={apple.accent} style={styles.actionText}>
                {global.i18n.t('list_select_cancel')}
              </Text>
            </TouchableOpacity>
          </View>
        </GlassSurface>
      </Animated.View>
    )
  }, [animaStyle, apple, selectMode, handleSelectAll, isSelectAll, onExitSelectMode, onSwitchMode])

  return !visible && animatePlayed ? null : component
})

const styles = createStyle({
  container: {
    position: 'absolute',
    left: 14,
    right: 14,
    zIndex: 40,
  },
  glassInner: {
    height: MULTI_SELECT_BAR_HEIGHT,
    paddingHorizontal: 8,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  switchGroup: {
    flexDirection: 'row',
    padding: 2,
    borderRadius: radius.pill,
    height: 32,
  },
  switchItem: {
    paddingHorizontal: spacing.md,
    borderRadius: radius.pill,
    alignItems: 'center',
    justifyContent: 'center',
  },
  switchActive: {
    borderWidth: StyleSheet.hairlineWidth,
  },
  actions: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.xs,
  },
  actionBtn: {
    paddingHorizontal: spacing.md,
    height: 34,
    alignItems: 'center',
    justifyContent: 'center',
  },
  actionText: {
    fontWeight: fontWeight.semibold,
  },
})
