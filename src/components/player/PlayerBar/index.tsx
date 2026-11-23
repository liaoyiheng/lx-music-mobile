// Modified for Apple Music style UI. Original: lyswhut/lx-music-mobile (Apache-2.0)
import { memo, useMemo } from 'react'
import { View } from 'react-native'
import { useKeyboard } from '@/utils/hooks'

import Pic from './components/Pic'
import Title from './components/Title'
import PlayInfo from './components/PlayInfo'
import ControlBtn from './components/ControlBtn'
import { createStyle } from '@/utils/tools'
import { useApple, radius, hairline } from '@/theme/apple'
import { useSettingValue } from '@/store/setting/hook'

/**
 * Apple Music 风格迷你播放条：悬浮圆角卡片 + 圆角封面 + 标题/歌词 + 播放/下一首
 * 交互保持不变：点击封面/标题进入播放详情，长按定位到列表
 */
export default memo(({ isHome = false }: { isHome?: boolean }) => {
  const { keyboardShown } = useKeyboard()
  const apple = useApple()
  const autoHidePlayBar = useSettingValue('common.autoHidePlayBar')

  const playerComponent = useMemo(() => (
    <View style={styles.wrapper}>
      <View style={{
        ...styles.container,
        backgroundColor: apple.isDark ? '#1C1C1E' : '#F9F9F9',
        borderColor: apple.separator,
      }}>
        <Pic isHome={isHome} />
        <View style={styles.center}>
          <Title isHome={isHome} />
          <PlayInfo isHome={isHome} />
        </View>
        <View style={styles.right}>
          <ControlBtn />
        </View>
      </View>
    </View>
  ), [apple, isHome])

  return autoHidePlayBar && keyboardShown ? null : playerComponent
})

const styles = createStyle({
  wrapper: {
    width: '100%',
    paddingHorizontal: 8,
    paddingTop: 4,
    paddingBottom: 6,
  },
  container: {
    width: '100%',
    paddingVertical: 6,
    paddingLeft: 8,
    borderRadius: radius.alert,
    borderWidth: hairline,
    flexDirection: 'row',
    alignItems: 'center',
    overflow: 'hidden',
    elevation: 8,
    shadowColor: '#000',
    shadowOpacity: 0.15,
    shadowRadius: 12,
    shadowOffset: { width: 0, height: 4 },
  },
  center: {
    flexDirection: 'column',
    flexGrow: 1,
    flexShrink: 1,
    paddingLeft: 10,
    height: '100%',
    justifyContent: 'center',
  },
  right: {
    flexDirection: 'row',
    alignItems: 'center',
    flexGrow: 0,
    flexShrink: 0,
    paddingLeft: 4,
    paddingRight: 4,
  },
})
