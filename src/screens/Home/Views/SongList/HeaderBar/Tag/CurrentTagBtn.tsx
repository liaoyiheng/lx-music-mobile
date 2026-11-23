import { forwardRef, useImperativeHandle, useState } from 'react'
import { TouchableOpacity } from 'react-native'
import Text from '@/components/common/Text'
import { IonIcon } from '@/components/common/Icon'
import { useI18n } from '@/lang'
import { createStyle } from '@/utils/tools'
import { useApple, fontSize, fontWeight, radius, spacing } from '@/theme/apple'

export interface CurrentTagBtnProps {
  onShowList: () => void
}

export interface CurrentTagBtnType {
  setCurrentTagInfo: (name: string) => void
}

export default forwardRef<CurrentTagBtnType, CurrentTagBtnProps>(({ onShowList }, ref) => {
  const t = useI18n()
  const apple = useApple()
  const [name, setName] = useState('')

  useImperativeHandle(ref, () => ({
    setCurrentTagInfo(name) {
      if (!name) name = t('songlist_tag_default')
      setName(name)
    },
  }))

  return (
    <TouchableOpacity style={{ ...styles.btn, backgroundColor: apple.fill }} activeOpacity={0.6} onPress={onShowList}>
      <Text style={styles.text} size={fontSize.footnote} color={apple.label} numberOfLines={1}>{name}</Text>
      <IonIcon name="chevron-down" rawSize={12} color={apple.secondaryLabel} />
    </TouchableOpacity>
  )
})

const styles = createStyle({
  btn: {
    height: 30,
    maxWidth: 112,
    paddingLeft: 10,
    paddingRight: 8,
    borderRadius: radius.pill,
    flexDirection: 'row',
    alignItems: 'center',
    flexShrink: 0,
  },
  text: {
    flexShrink: 1,
    marginRight: spacing.xxs + 2,
    fontWeight: fontWeight.medium,
  },
})
