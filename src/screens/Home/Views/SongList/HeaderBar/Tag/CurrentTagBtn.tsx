import { forwardRef, useImperativeHandle, useState } from 'react'
import { TouchableOpacity } from 'react-native'
import Text from '@/components/common/Text'
import { IonIcon } from '@/components/common/Icon'
import { GlassSurface } from '@/components/apple/LiquidGlass'
import { useI18n } from '@/lang'
import { createStyle } from '@/utils/tools'
import { useApple, fontWeight, radius, spacing } from '@/theme/apple'

export interface CurrentTagBtnProps {
  onShowList: () => void
}

export interface CurrentTagBtnType {
  setCurrentTagInfo: (name: string) => void
}

/**
 * 歌单标签液态玻璃触发胶囊：打开标签抽屉
 */
export default forwardRef<CurrentTagBtnType, CurrentTagBtnProps>(({ onShowList }, ref) => {
  const t = useI18n()
  const apple = useApple()
  const [name, setName] = useState('')
  const [hasCustomTag, setHasCustomTag] = useState(false)

  useImperativeHandle(ref, () => ({
    setCurrentTagInfo(nextName) {
      const isCustom = Boolean(nextName)
      setHasCustomTag(isCustom)
      setName(nextName || t('songlist_tag_default'))
    },
  }), [t])

  const tint = hasCustomTag ? apple.accent : apple.label

  return (
    <TouchableOpacity activeOpacity={0.7} onPress={onShowList} style={styles.wrap}>
      <GlassSurface borderRadius={radius.pill} shadow={false} innerStyle={styles.btn}>
        <IonIcon name="pricetag-outline" rawSize={12} color={hasCustomTag ? apple.accent : apple.secondaryLabel} />
        <Text style={styles.text} size={13.5} color={tint} numberOfLines={1}>
          {name || t('songlist_tag_default')}
        </Text>
        <IonIcon name="chevron-down" rawSize={12} color={hasCustomTag ? apple.accent : apple.secondaryLabel} />
      </GlassSurface>
    </TouchableOpacity>
  )
})

const styles = createStyle({
  wrap: {
    flexShrink: 0,
    marginRight: spacing.sm,
  },
  btn: {
    height: 30,
    maxWidth: 132,
    paddingHorizontal: 11,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
  },
  text: {
    flexShrink: 1,
    fontWeight: fontWeight.semibold,
  },
})
