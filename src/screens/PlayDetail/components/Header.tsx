// Added for Apple Music style UI (based on lyswhut/lx-music-mobile, Apache-2.0)
import { memo } from 'react'
import { TouchableOpacity, View, type GestureResponderHandlers } from 'react-native'
import { Icon } from '@/components/common/Icon'
import { NAV_SHEAR_NATIVE_IDS } from '@/config/constant'
import { useApple, radius, size, spacing } from '@/theme/apple'
import { createStyle } from '@/utils/tools'

export const HEADER_HEIGHT = size.hitTarget

/** 顶部：左侧向下箭头关闭，中间下拉把手（点按也可关闭），右侧歌词设置；整条区域可下拉关闭 */
export default memo(({ onClose, onShowSetting, panHandlers }: {
  onClose: () => void
  onShowSetting: () => void
  panHandlers?: GestureResponderHandlers
}) => {
  const apple = useApple()

  return (
    <View style={styles.container} nativeID={NAV_SHEAR_NATIVE_IDS.playDetail_header} {...panHandlers}>
      <TouchableOpacity style={styles.btn} activeOpacity={0.5} onPress={onClose} accessibilityRole="button">
        <Icon name="chevron-left" color={apple.secondaryLabel} size={18} style={styles.down} />
      </TouchableOpacity>
      <TouchableOpacity style={styles.grabberArea} activeOpacity={0.6} onPress={onClose}>
        <View style={{ ...styles.grabber, backgroundColor: apple.tertiaryLabel }} />
      </TouchableOpacity>
      <TouchableOpacity style={styles.btn} activeOpacity={0.5} onPress={onShowSetting} accessibilityRole="button">
        <Icon name="slider" color={apple.secondaryLabel} size={18} />
      </TouchableOpacity>
    </View>
  )
})

const styles = createStyle({
  container: {
    height: HEADER_HEIGHT,
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: spacing.sm,
  },
  btn: {
    width: size.hitTarget,
    height: size.hitTarget,
    justifyContent: 'center',
    alignItems: 'center',
  },
  down: {
    transform: [{ rotate: '-90deg' }],
  },
  grabberArea: {
    flex: 1,
    height: '100%',
    justifyContent: 'center',
    alignItems: 'center',
  },
  grabber: {
    width: 36,
    height: 5,
    borderRadius: radius.pill,
  },
})
