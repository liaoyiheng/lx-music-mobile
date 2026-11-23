// Modified for Apple Music style UI. Original: lyswhut/lx-music-mobile (Apache-2.0)
import { View, TouchableOpacity } from 'react-native'
import { useNavActiveId, useStatusbarHeight } from '@/store/common/hook'
import { useI18n } from '@/lang'
import { createStyle } from '@/utils/tools'
import { Icon } from '@/components/common/Icon'
import Text from '@/components/common/Text'
import StatusBar from '@/components/common/StatusBar'
import { useSettingValue } from '@/store/setting/hook'
import { scaleSizeH } from '@/utils/pixelRatio'
import { HEADER_HEIGHT } from '@/config/constant'
import { type InitState as CommonState } from '@/store/common/state'
import SearchTypeSelector from '@/screens/Home/Views/Search/SearchTypeSelector'
import { useApple, fontSize } from '@/theme/apple'

const headerComponents: Partial<Record<CommonState['navActiveId'], React.ReactNode>> = {
  nav_search: <SearchTypeSelector />,
}

/** 额外高度，用来放大标题 */
const LARGE_TITLE_EXTRA = 14

const openMenu = () => {
  global.app_event.changeMenuVisible(true)
}

/** 圆形菜单按钮（打开侧边导航，功能与原版一致） */
const MenuBtn = () => {
  const apple = useApple()
  return (
    <TouchableOpacity style={styles.btn} onPress={openMenu} activeOpacity={0.6}>
      <View style={{ ...styles.btnCircle, backgroundColor: apple.fill }}>
        <Icon color={apple.accent} name="menu" size={16} />
      </View>
    </TouchableOpacity>
  )
}

/**
 * Apple Music 风格大标题：左对齐、粗体、大字号；菜单按钮放在标题的另一侧
 * drawerLayoutPosition 为 left 时按钮在左，否则在右，与原版行为保持一致
 */
const LargeTitleHeader = ({ btnOnLeft }: { btnOnLeft: boolean }) => {
  const apple = useApple()
  const id = useNavActiveId()
  const t = useI18n()
  const statusBarHeight = useStatusbarHeight()

  return (
    <View style={{
      ...styles.container,
      height: scaleSizeH(HEADER_HEIGHT + LARGE_TITLE_EXTRA) + statusBarHeight,
      paddingTop: statusBarHeight,
      backgroundColor: apple.background,
    }}>
      {btnOnLeft ? <MenuBtn /> : null}
      <TouchableOpacity style={styles.titleBtn} onPress={openMenu} activeOpacity={0.7}>
        <Text
          style={{ ...styles.title, paddingLeft: btnOnLeft ? 4 : 16 }}
          size={fontSize.title1}
          color={apple.label}
          numberOfLines={1}
        >{t(id)}</Text>
      </TouchableOpacity>
      {headerComponents[id] ?? null}
      {btnOnLeft ? null : <MenuBtn />}
    </View>
  )
}

const Header = () => {
  const drawerLayoutPosition = useSettingValue('common.drawerLayoutPosition')

  return (
    <>
      <StatusBar />
      <LargeTitleHeader btnOnLeft={drawerLayoutPosition == 'left'} />
    </>
  )
}

const styles = createStyle({
  container: {
    paddingHorizontal: 6,
    flexDirection: 'row',
    alignItems: 'center',
    zIndex: 10,
  },
  btn: {
    width: HEADER_HEIGHT,
    height: '100%',
    alignItems: 'center',
    justifyContent: 'center',
  },
  btnCircle: {
    width: 32,
    height: 32,
    borderRadius: 16,
    alignItems: 'center',
    justifyContent: 'center',
  },
  titleBtn: {
    flex: 1,
    height: '100%',
    justifyContent: 'center',
  },
  title: {
    fontWeight: '700',
    letterSpacing: 0.3,
    paddingRight: 12,
  },
})

export default Header
