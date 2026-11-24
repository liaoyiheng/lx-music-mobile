import { forwardRef, useImperativeHandle, useState } from 'react'
import { StyleSheet, View, type ViewStyle } from 'react-native'

import { createStyle } from '@/utils/tools'
import DorpDownMenu, { type DorpDownMenuProps as _DorpDownMenuProps } from '@/components/common/DorpDownMenu'
import { GlassSurface } from '@/components/apple/LiquidGlass'
import { useSourceListI18n } from '@/components/SourceSelector'
import Text from '@/components/common/Text'
import { IonIcon } from '@/components/common/Icon'
import { useI18n } from '@/lang'
import { useSettingValue } from '@/store/setting/hook'
import leaderboardState, { type Source, type InitState } from '@/store/leaderboard/state'
import { useApple, fontSize, fontWeight, radius } from '@/theme/apple'

type Sources = Readonly<InitState['sources']>

export interface SourceSelectorProps {
  onSourceChange: (source: Source) => void
  style?: ViewStyle
}

export interface SourceSelectorType {
  setSource: (source: Source) => void
}

/**
 * 排行榜顶部液态玻璃音源胶囊（accent 圆点 + 音源名 + 下拉箭头）
 */
export default forwardRef<SourceSelectorType, SourceSelectorProps>(({ style, onSourceChange }, ref) => {
  const apple = useApple()
  const t = useI18n()
  const sourceNameType = useSettingValue('common.sourceNameType')
  const [list, setList] = useState<Sources>(leaderboardState.sources)
  const [source, setSource] = useState<Source>('kw')

  useImperativeHandle(ref, () => ({
    setSource(activeSource) {
      setList(leaderboardState.sources)
      setSource(activeSource)
    },
  }), [])

  const sourceList_t = useSourceListI18n(list)
  type DorpDownMenuProps = _DorpDownMenuProps<typeof sourceList_t>

  const handleChangeSource: DorpDownMenuProps['onPress'] = ({ action }) => {
    onSourceChange(action as Source)
    setSource(action as Source)
  }

  return (
    <View style={StyleSheet.compose<ViewStyle, ViewStyle, ViewStyle>(styles.selector, style)}>
      <DorpDownMenu
        menus={sourceList_t}
        onPress={handleChangeSource}
        fontSize={fontSize.subhead}
        activeId={source}
      >
        <GlassSurface borderRadius={radius.pill} innerStyle={styles.pill}>
          <View style={[styles.dot, { backgroundColor: apple.accent }]} />
          <Text
            style={styles.text}
            size={13.5}
            color={apple.accent}
            numberOfLines={1}
          >
            {t(`source_${sourceNameType}_${source}`)}
          </Text>
          <IonIcon name="chevron-down" rawSize={13} color={apple.accent} />
        </GlassSurface>
      </DorpDownMenu>
    </View>
  )
})

const styles = createStyle({
  selector: {
    flexShrink: 0,
  },
  pill: {
    height: 32,
    paddingHorizontal: 12,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 5,
  },
  dot: {
    width: 7,
    height: 7,
    borderRadius: 3.5,
  },
  text: {
    fontWeight: fontWeight.semibold,
    letterSpacing: -0.1,
  },
})
