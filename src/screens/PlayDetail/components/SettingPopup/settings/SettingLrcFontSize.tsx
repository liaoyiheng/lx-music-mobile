import { useState } from 'react'
import { View } from 'react-native'
import { fontSize, useApple } from '@/theme/apple'
import Text from '@/components/common/Text'
import { useSettingValue } from '@/store/setting/hook'
import Slider, { type SliderProps } from '@/components/common/Slider'
import { updateSetting } from '@/core/common'
import { useI18n } from '@/lang'
import styles from './style'

const LrcFontSize = ({ direction }: {
  direction: 'horizontal' | 'vertical'
}) => {
  const apple = useApple()
  const settingKey = direction == 'horizontal' ? 'playDetail.horizontal.style.lrcFontSize' : 'playDetail.vertical.style.lrcFontSize'
  const lrcFontSize = useSettingValue(settingKey)
  const [sliderSize, setSliderSize] = useState(lrcFontSize)
  const [isSliding, setSliding] = useState(false)
  const t = useI18n()

  const handleSlidingStart: SliderProps['onSlidingStart'] = () => {
    setSliding(true)
  }
  const handleValueChange: SliderProps['onValueChange'] = value => {
    setSliderSize(value)
  }
  const handleSlidingComplete: SliderProps['onSlidingComplete'] = value => {
    setSliding(false)
    if (lrcFontSize == value) return
    updateSetting({ [settingKey]: value })
  }

  return (
    <View style={styles.section}>
      <Text size={fontSize.footnote} color={apple.secondaryLabel} style={styles.sectionTitle}>
        {t('play_detail_setting_lrc_font_size')}
      </Text>
      <View style={{ ...styles.sliderCard, backgroundColor: apple.secondaryBackground }}>
        <View style={styles.content}>
          <Text size={fontSize.subhead} style={styles.label} color={apple.secondaryLabel}>{isSliding ? sliderSize : lrcFontSize}</Text>
          <Slider
            minimumValue={100}
            maximumValue={300}
            onSlidingComplete={handleSlidingComplete}
            onValueChange={handleValueChange}
            onSlidingStart={handleSlidingStart}
            step={2}
            value={lrcFontSize}
          />
        </View>
      </View>
    </View>
  )
}

export default LrcFontSize
