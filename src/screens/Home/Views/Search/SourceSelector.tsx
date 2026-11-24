import { memo } from 'react'
import { StyleSheet, View, type ViewStyle } from 'react-native'

import { createStyle } from '@/utils/tools'
import DorpDownMenu, { type DorpDownMenuProps as _DorpDownMenuProps } from '@/components/common/DorpDownMenu'
import { GlassSurface } from '@/components/apple/LiquidGlass'
import { useSourceListI18n } from '@/components/SourceSelector'
import Text from '@/components/common/Text'
import { IonIcon } from '@/components/common/Icon'
import { useI18n } from '@/lang'
import { useSettingValue } from '@/store/setting/hook'
import { type Source as MusicSource } from '@/store/search/music/state'
import { type Source as SonglistSource } from '@/store/search/songlist/state'
import { useApple, fontSize, fontWeight, radius } from '@/theme/apple'

export type SearchSources = Readonly<Array<MusicSource | SonglistSource>>
export type SearchSource = SearchSources[number]

export interface SourceSelectorProps {
  list: SearchSources
  source: SearchSource
  onSourceChange: (source: SearchSource) => void
  compact?: boolean
  style?: ViewStyle
}

/**
 * 搜索页液态玻璃音源胶囊（accent 圆点 + 音源名 + 下拉箭头）
 */
export default memo(({ list, source, onSourceChange, compact = false, style }: SourceSelectorProps) => {
  const apple = useApple()
  const t = useI18n()
  const sourceNameType = useSettingValue('common.sourceNameType')

  const sourceList_t = useSourceListI18n(list)
  type DorpDownMenuProps = _DorpDownMenuProps<typeof sourceList_t>

  const handleChangeSource: DorpDownMenuProps['onPress'] = ({ action }) => {
    onSourceChange(action as SearchSource)
  }

  return (
    <View style={StyleSheet.compose<ViewStyle, ViewStyle, ViewStyle>(styles.selector, style)}>
      <DorpDownMenu
        menus={sourceList_t}
        onPress={handleChangeSource}
        fontSize={fontSize.subhead}
        activeId={source}
        center
      >
        <GlassSurface
          borderRadius={radius.pill}
          innerStyle={compact ? styles.compactPill : styles.pill}
        >
          <View style={[styles.dot, { backgroundColor: apple.accent }]} />
          <Text
            style={styles.text}
            size={compact ? 13 : 13.5}
            color={apple.accent}
            numberOfLines={1}
          >
            {t(`source_${sourceNameType}_${source}`)}
          </Text>
          <IonIcon name="chevron-down" rawSize={compact ? 12 : 13} color={apple.accent} />
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
  compactPill: {
    height: 36,
    paddingHorizontal: 11,
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
