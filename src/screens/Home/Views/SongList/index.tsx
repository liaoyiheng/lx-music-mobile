import { useCallback, useEffect, useRef, useState } from 'react'
import { StyleSheet, View } from 'react-native'
import Content from './Content'
import TagList from './TagList'
import Text from '@/components/common/Text'
import { GlassCapsuleButton } from '@/components/apple/LiquidGlass'
import { useI18n } from '@/lang'
import commonState from '@/store/common/state'
import { useStatusbarHeight } from '@/store/common/hook'
import { useBackHandler } from '@/utils/hooks/useBackHandler'
import { useApple, fontSize, fontWeight, pagePadding, size } from '@/theme/apple'
import { createStyle } from '@/utils/tools'

/**
 * 歌单广场
 * 液态玻璃质感标签弹层（带状态栏留白、完成胶囊按钮、返回键关闭）
 */
export default () => {
  const apple = useApple()
  const t = useI18n()
  const statusBarHeight = useStatusbarHeight()
  const [tagVisible, setTagVisible] = useState(false)
  const tagVisibleRef = useRef(false)
  tagVisibleRef.current = tagVisible

  useEffect(() => {
    const handleShow = () => {
      requestAnimationFrame(() => {
        setTagVisible(true)
      })
    }
    const handleHide = () => {
      setTagVisible(false)
    }
    global.app_event.on('showSonglistTagList', handleShow)
    global.app_event.on('hideSonglistTagList', handleHide)
    return () => {
      global.app_event.off('showSonglistTagList', handleShow)
      global.app_event.off('hideSonglistTagList', handleHide)
    }
  }, [])

  useBackHandler(useCallback(() => {
    if (!tagVisibleRef.current) return false
    if (Object.keys(commonState.componentIds).length != 1 || commonState.navActiveId != 'nav_songlist') return false
    setTagVisible(false)
    return true
  }, []))

  return (
    <View style={styles.container}>
      <Content />
      <View
        style={tagVisible ? { ...styles.overlay, backgroundColor: apple.glassBgDense } : styles.hidden}
        pointerEvents={tagVisible ? 'auto' : 'none'}
      >
        <View
          style={{
            ...styles.tagHeader,
            paddingTop: statusBarHeight,
            height: size.hitTarget + statusBarHeight + 6,
            borderBottomColor: apple.separator,
            backgroundColor: apple.glassBg,
          }}
        >
          <Text style={styles.tagTitle} size={fontSize.headline} color={apple.label}>{t('apple_tags')}</Text>
          <View style={styles.doneBtn}>
            <GlassCapsuleButton label={t('apple_done')} onPress={() => { setTagVisible(false) }} />
          </View>
        </View>
        <View style={styles.container}>
          <TagList />
        </View>
      </View>
    </View>
  )
}

const styles = createStyle({
  container: {
    flex: 1,
  },
  overlay: {
    position: 'absolute',
    left: 0,
    top: 0,
    right: 0,
    bottom: 0,
    zIndex: 5,
  },
  hidden: {
    display: 'none',
  },
  tagHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    borderBottomWidth: StyleSheet.hairlineWidth,
  },
  tagTitle: {
    fontWeight: fontWeight.semibold,
  },
  doneBtn: {
    position: 'absolute',
    right: pagePadding,
    bottom: 9,
  },
})
