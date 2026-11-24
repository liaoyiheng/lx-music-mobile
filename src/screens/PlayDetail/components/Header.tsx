// Modified for Apple Music iOS 26 Liquid Glass UI. Original: lyswhut/lx-music-mobile (Apache-2.0)
import { memo } from 'react'
import { TouchableOpacity, View, type GestureResponderHandlers } from 'react-native'
import { NAV_SHEAR_NATIVE_IDS } from '@/config/constant'
import { useI18n } from '@/lang'
import { radius } from '@/theme/apple'
import { createStyle } from '@/utils/tools'

export const HEADER_HEIGHT = 28

/** 极简顶部下拉把手：居中 38×5dp 半透明圆角条，点击或下滑即可收起播放页 */
export default memo(({ onClose, panHandlers }: {
  onClose: () => void
  panHandlers?: GestureResponderHandlers
}) => {
  const t = useI18n()

  return (
    <View
      style={styles.container}
      nativeID={NAV_SHEAR_NATIVE_IDS.playDetail_header}
      {...panHandlers}
    >
      <TouchableOpacity
        style={styles.grabberArea}
        activeOpacity={0.65}
        onPress={onClose}
        accessibilityRole="button"
        accessibilityLabel={t('close')}
      >
        <View style={styles.grabber} />
      </TouchableOpacity>
    </View>
  )
})

const styles = createStyle({
  container: {
    height: HEADER_HEIGHT,
    justifyContent: 'center',
    alignItems: 'center',
  },
  grabberArea: {
    width: 96,
    height: HEADER_HEIGHT,
    justifyContent: 'center',
    alignItems: 'center',
  },
  grabber: {
    width: 38,
    height: 5,
    borderRadius: radius.pill,
    backgroundColor: 'rgba(255, 255, 255, 0.34)',
  },
})
