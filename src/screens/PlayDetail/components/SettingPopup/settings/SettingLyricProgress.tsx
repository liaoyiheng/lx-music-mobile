import { View } from 'react-native'
import { SwitchRow } from '@/components/apple/SwitchRow'
import { useSettingValue } from '@/store/setting/hook'
import { updateSetting } from '@/core/common'
import { useI18n } from '@/lang'
import { useApple } from '@/theme/apple'
import styles from './style'

export default () => {
  const t = useI18n()
  const apple = useApple()
  const isShowLyricProgressSetting = useSettingValue('playDetail.isShowLyricProgressSetting')
  const setShowLyricProgressSetting = (showLyricProgressSetting: boolean) => {
    updateSetting({ 'playDetail.isShowLyricProgressSetting': showLyricProgressSetting })
  }

  return (
    <View style={styles.section}>
      <View style={{ ...styles.card, backgroundColor: apple.secondaryBackground }}>
        <SwitchRow
          label={t('play_detail_setting_show_lyric_progress_setting')}
          value={isShowLyricProgressSetting}
          onValueChange={setShowLyricProgressSetting}
          showSeparator={false}
        />
      </View>
    </View>
  )
}
