import { forwardRef, useImperativeHandle, useState } from 'react'
import { StyleSheet, View, type ViewStyle } from 'react-native'

import { createStyle } from '@/utils/tools'
import DorpDownMenu, { type DorpDownMenuProps as _DorpDownMenuProps } from '@/components/common/DorpDownMenu'
import { useSourceListI18n } from '@/components/SourceSelector'
import Text from '@/components/common/Text'
import { IonIcon } from '@/components/common/Icon'
import { useI18n } from '@/lang'
import { useSettingValue } from '@/store/setting/hook'
import songlistState, { type Source, type InitState } from '@/store/songlist/state'
import { useApple, fontSize, fontWeight, radius, spacing } from '@/theme/apple'

type Sources = Readonly<InitState['sources']>

export interface SourceSelectorProps {
  onSourceChange: (source: Source) => void
  style?: ViewStyle
}

export interface SourceSelectorType {
  setSource: (source: Source) => void
}

export default forwardRef<SourceSelectorType, SourceSelectorProps>(({ style, onSourceChange }, ref) => {
  const apple = useApple()
  const t = useI18n()
  const sourceNameType = useSettingValue('common.sourceNameType')
  const [list, setList] = useState<Sources>(songlistState.sources)
  const [source, setSource] = useState<Source>('kw')

  useImperativeHandle(ref, () => ({
    setSource(activeSource) {
      setList(songlistState.sources)
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
    <View style={StyleSheet.compose(styles.selector, style)}>
      <DorpDownMenu
        menus={sourceList_t}
        onPress={handleChangeSource}
        fontSize={fontSize.subhead}
        activeId={source}
      >
        <View style={{ ...styles.pill, backgroundColor: apple.fill }}>
          <Text
            style={styles.text}
            size={fontSize.footnote}
            color={apple.label}
            numberOfLines={1}
          >
            {t(`source_${sourceNameType}_${source}`)}
          </Text>
          <IonIcon name="chevron-down" rawSize={12} color={apple.secondaryLabel} />
        </View>
      </DorpDownMenu>
    </View>
  )
})

const styles = createStyle({
  selector: {
    flexShrink: 0,
  },
  pill: {
    height: 30,
    paddingLeft: 10,
    paddingRight: 8,
    borderRadius: radius.pill,
    flexDirection: 'row',
    alignItems: 'center',
  },
  text: {
    marginRight: spacing.xxs + 2,
    fontWeight: fontWeight.medium,
  },
})
