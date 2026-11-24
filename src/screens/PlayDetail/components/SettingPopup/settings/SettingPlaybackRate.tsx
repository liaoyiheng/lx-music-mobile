import { useState } from 'react'
import { TouchableOpacity, View } from 'react-native'
import Slider, { type SliderProps } from '@/components/common/Slider'
import Text from '@/components/common/Text'
import { updateSetting } from '@/core/common'
import { setPlaybackRate as setLyricPlaybackRate } from '@/core/lyric'
import { useI18n } from '@/lang'
import { setPlaybackRate, updateMetaData } from '@/plugins/player'
import playerState from '@/store/player/state'
import { useSettingValue } from '@/store/setting/hook'
import settingState from '@/store/setting/state'
import { fontSize } from '@/theme/apple'
import styles, { GLASS_DARK_LABEL, GLASS_DARK_SUB_LABEL } from './style'

const MIN_VALUE = 60
const MAX_VALUE = 200

export default () => {
  const playbackRate = Math.trunc(useSettingValue('player.playbackRate') * 100)
  const [sliderSize, setSliderSize] = useState(playbackRate)
  const [isSliding, setSliding] = useState(false)
  const t = useI18n()

  const handleSlidingStart: SliderProps['onSlidingStart'] = () => {
    setSliding(true)
  }
  const handleValueChange: SliderProps['onValueChange'] = (value) => {
    value = Math.trunc(value)
    setSliderSize(value)
    void setPlaybackRate(parseFloat((value / 100).toFixed(2)))
  }
  const handleSlidingComplete: SliderProps['onSlidingComplete'] = (value) => {
    setSliding(false)
    value = Math.trunc(value)
    const rate = value / 100
    void setLyricPlaybackRate(rate)
    void updateMetaData(playerState.musicInfo, playerState.isPlay, true)
    if (playbackRate == value) return
    updateSetting({ 'player.playbackRate': rate })
  }
  const handleReset = () => {
    if (settingState.setting['player.playbackRate'] == 1) return
    setSliderSize(100)
    void setPlaybackRate(1).then(() => {
      void updateMetaData(playerState.musicInfo, playerState.isPlay, true)
      void setLyricPlaybackRate(1)
    })
    updateSetting({ 'player.playbackRate': 1 })
  }

  return (
    <View style={styles.section}>
      <Text size={fontSize.footnote} color={GLASS_DARK_SUB_LABEL} style={styles.sectionTitle}>
        {t('play_detail_setting_playback_rate')}
      </Text>
      <View style={styles.sliderCard}>
        <View style={styles.content}>
          <Text size={fontSize.subhead} style={styles.label} color={GLASS_DARK_SUB_LABEL}>
            {`${((isSliding ? sliderSize : playbackRate) / 100).toFixed(2)}x`}
          </Text>
          <Slider
            minimumValue={MIN_VALUE}
            maximumValue={MAX_VALUE}
            onSlidingComplete={handleSlidingComplete}
            onValueChange={handleValueChange}
            onSlidingStart={handleSlidingStart}
            step={1}
            value={playbackRate}
          />
        </View>
        <View style={styles.resetRow}>
          <TouchableOpacity style={styles.resetBtn} activeOpacity={0.7} onPress={handleReset} accessibilityRole="button">
            <Text color={GLASS_DARK_LABEL} style={styles.resetBtnText}>
              {t('play_detail_setting_playback_rate_reset')}
            </Text>
          </TouchableOpacity>
        </View>
      </View>
    </View>
  )
}
