import { useMemo } from 'react'
import { TouchableOpacity, View } from 'react-native'
import { IonIcon } from '@/components/common/Icon'
import Text from '@/components/common/Text'
import { updateSetting } from '@/core/common'
import { useI18n } from '@/lang'
import { useSettingValue } from '@/store/setting/hook'
import { fontSize } from '@/theme/apple'
import styles, {
  GLASS_DARK_ACCENT,
  GLASS_DARK_LABEL,
  GLASS_DARK_SUB_LABEL,
} from './style'

type Align_Type = LX.AppSetting['playDetail.style.align']

const ALIGN_LIST = [
  'left',
  'center',
  'right',
] as const

export default () => {
  const t = useI18n()
  const activeAlign = useSettingValue('playDetail.style.align')
  const list = useMemo(() => {
    return ALIGN_LIST.map(id => ({ id, name: t(`play_detail_setting_lrc_align_${id}`) }))
  }, [t])

  const setPosition = (id: Align_Type) => {
    updateSetting({ 'playDetail.style.align': id })
  }

  return (
    <View style={styles.section}>
      <Text size={fontSize.footnote} color={GLASS_DARK_SUB_LABEL} style={styles.sectionTitle}>
        {t('play_detail_setting_lrc_align')}
      </Text>
      <View style={styles.card}>
        {list.map(({ id, name }, index) => {
          const selected = activeAlign == id
          return (
            <View key={id}>
              <TouchableOpacity
                activeOpacity={0.65}
                onPress={() => { setPosition(id) }}
                style={styles.row}
              >
                <Text style={styles.rowLabel} color={GLASS_DARK_LABEL} numberOfLines={1}>
                  {name}
                </Text>
                {selected ? <IonIcon name="checkmark" rawSize={20} color={GLASS_DARK_ACCENT} /> : null}
              </TouchableOpacity>
              {index < list.length - 1 ? <View style={styles.rowSep} /> : null}
            </View>
          )
        })}
      </View>
    </View>
  )
}
