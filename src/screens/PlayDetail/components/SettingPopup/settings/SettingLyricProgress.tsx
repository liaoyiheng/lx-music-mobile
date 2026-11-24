import { Platform, Switch, TouchableOpacity, View } from 'react-native'
import Text from '@/components/common/Text'
import { updateSetting } from '@/core/common'
import { useI18n } from '@/lang'
import { useSettingValue } from '@/store/setting/hook'
import styles, {
  GLASS_DARK_LABEL,
  GLASS_DARK_SWITCH_OFF,
  GLASS_DARK_SWITCH_ON,
} from './style'

export default () => {
  const t = useI18n()
  const isShowLyricProgressSetting = useSettingValue('playDetail.isShowLyricProgressSetting')
  const setShowLyricProgressSetting = (showLyricProgressSetting: boolean) => {
    updateSetting({ 'playDetail.isShowLyricProgressSetting': showLyricProgressSetting })
  }

  return (
    <View style={styles.section}>
      <View style={styles.card}>
        <TouchableOpacity
          activeOpacity={0.7}
          onPress={() => { setShowLyricProgressSetting(!isShowLyricProgressSetting) }}
          style={styles.row}
        >
          <Text style={styles.rowLabel} color={GLASS_DARK_LABEL} numberOfLines={2}>
            {t('play_detail_setting_show_lyric_progress_setting')}
          </Text>
          <Switch
            value={isShowLyricProgressSetting}
            onValueChange={setShowLyricProgressSetting}
            trackColor={{ false: GLASS_DARK_SWITCH_OFF, true: GLASS_DARK_SWITCH_ON }}
            thumbColor={Platform.OS === 'android' ? '#FFFFFF' : undefined}
            ios_backgroundColor={GLASS_DARK_SWITCH_OFF}
          />
        </TouchableOpacity>
      </View>
    </View>
  )
}
