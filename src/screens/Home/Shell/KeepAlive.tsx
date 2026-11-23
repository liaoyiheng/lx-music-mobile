import { memo, useEffect, useRef, useState } from 'react'
import { View } from 'react-native'
import { createStyle } from '@/utils/tools'

/**
 * 这些设置变化后，不在前台的页面会被卸载，下次进入时重新创建（沿用原版行为）
 */
const resetKeys = [
  'list.isShowAlbumName',
  'list.isShowInterval',
  'theme.fontShadow',
] as Readonly<Array<keyof LX.AppSetting>>

interface Props {
  active: boolean
  children: React.ReactNode
}

/**
 * 页面保活：首次进入时才创建，切走后保留状态（滚动位置等），只是隐藏
 */
export default memo(({ active, children }: Props) => {
  const [mounted, setMounted] = useState(active)
  const activeRef = useRef(active)
  activeRef.current = active

  useEffect(() => {
    if (active && !mounted) setMounted(true)
  }, [active, mounted])

  useEffect(() => {
    const reset = () => {
      if (!activeRef.current) setMounted(false)
    }
    const handleConfigUpdated = (keys: Array<keyof LX.AppSetting>) => {
      if (keys.some(k => resetKeys.includes(k))) reset()
    }
    global.state_event.on('themeUpdated', reset)
    global.state_event.on('languageChanged', reset)
    global.state_event.on('configUpdated', handleConfigUpdated)
    return () => {
      global.state_event.off('themeUpdated', reset)
      global.state_event.off('languageChanged', reset)
      global.state_event.off('configUpdated', handleConfigUpdated)
    }
  }, [])

  return (
    <View style={active ? styles.page : styles.hidden} pointerEvents={active ? 'auto' : 'none'} collapsable={false}>
      {mounted ? children : null}
    </View>
  )
})

const styles = createStyle({
  page: {
    flex: 1,
  },
  hidden: {
    display: 'none',
  },
})
