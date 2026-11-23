import { useMemo } from 'react'
import { View } from 'react-native'
import Text from '@/components/common/Text'
import { RadioRow } from '@/components/apple/RadioRow'
import { useSettingValue } from '@/store/setting/hook'
import { updateSetting } from '@/core/common'
import { useI18n } from '@/lang'
import { fontSize, useApple } from '@/theme/apple'
import styles from './style'

type Align_Type = LX.AppSetting['playDetail.style.align']

const ALIGN_LIST = [
  'left',
  'center',
  'right',
] as const

export default () => {
  const t = useI18n()
  const apple = useApple()
  const activeAlign = useSettingValue('playDetail.style.align')
  const list = useMemo(() => {
    return ALIGN_LIST.map(id => ({ id, name: t(`play_detail_setting_lrc_align_${id}`) }))
  }, [t])

  const setPosition = (id: Align_Type) => {
    updateSetting({ 'playDetail.style.align': id })
  }

  return (
    <View style={styles.section}>
      <Text size={fontSize.footnote} color={apple.secondaryLabel} style={styles.sectionTitle}>
        {t('play_detail_setting_lrc_align')}
      </Text>
      <View style={{ ...styles.card, backgroundColor: apple.secondaryBackground }}>
        {list.map(({ id, name }, index) => (
          <RadioRow
            key={id}
            label={name}
            selected={activeAlign == id}
            onPress={() => { setPosition(id) }}
            showSeparator={index < list.length - 1}
          />
        ))}
      </View>
    </View>
  )
}
