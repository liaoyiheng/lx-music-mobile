// Added for Apple Music style UI (based on lyswhut/lx-music-mobile, Apache-2.0)
import { memo } from 'react'
import { TouchableOpacity, View, type GestureResponderHandlers } from 'react-native'
import { IonIcon } from '@/components/common/Icon'
import { NAV_SHEAR_NATIVE_IDS } from '@/config/constant'
import { radius, size, spacing } from '@/theme/apple'
import { createStyle } from '@/utils/tools'

export const HEADER_HEIGHT = size.hitTarget

const ICON_COLOR = 'rgba(255, 255, 255, 0.85)'

/** 顶部：左侧向下收起，中间下拉抓手条，右侧设置弹层；整条区域可下拉关闭 */
export default memo(({ onClose, onShowSetting, panHandlers }: {
  onClose: () => void
  onShowSetting: () => void
  panHandlers?: GestureResponderHandlers
}) => {
  return (
    <View style={styles.container} nativeID={NAV_SHEAR_NATIVE_IDS.playDetail_header} {...panHandlers}>
      <TouchableOpacity style={styles.btn} activeOpacity={0.5} onPress={onClose} accessibilityRole="button">
        <IonIcon name="chevron-down" color={ICON_COLOR} size={24} />
      </TouchableOpacity>
      <TouchableOpacity style={styles.grabberArea} activeOpacity={0.6} onPress={onClose}>
        <View style={styles.grabber} />
      </TouchableOpacity>
      <TouchableOpacity style={styles.btn} activeOpacity={0.5} onPress={onShowSetting} accessibilityRole="button">
        <IonIcon name="ellipsis-horizontal-circle-outline" color={ICON_COLOR} size={22} />
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
    backgroundColor: 'rgba(255, 255, 255, 0.35)',
  },
})
