import { useCallback, useEffect, useRef, useState } from 'react'
import { TouchableOpacity, View } from 'react-native'
import Content from './Content'
import TagList from './TagList'
import Text from '@/components/common/Text'
import { useI18n } from '@/lang'
import commonState from '@/store/common/state'
import { useBackHandler } from '@/utils/hooks/useBackHandler'
import { useApple, fontSize, fontWeight, hairline, pagePadding, size } from '@/theme/apple'
import { createStyle } from '@/utils/tools'

/**
 * 歌单广场
 * 原版的标签侧边抽屉改为覆盖在内容上的「标签」页（带完成按钮，返回键关闭）
 */
export default () => {
  const apple = useApple()
  const t = useI18n()
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
        style={tagVisible ? { ...styles.overlay, backgroundColor: apple.background } : styles.hidden}
        pointerEvents={tagVisible ? 'auto' : 'none'}
      >
        <View style={{ ...styles.tagHeader, borderBottomColor: apple.separator }}>
          <Text style={styles.tagTitle} size={fontSize.headline} color={apple.label}>{t('apple_tags')}</Text>
          <TouchableOpacity style={styles.doneBtn} activeOpacity={0.5} onPress={() => { setTagVisible(false) }}>
            <Text size={fontSize.body} color={apple.accent} style={styles.doneText}>{t('apple_done')}</Text>
          </TouchableOpacity>
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
    height: size.hitTarget,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    borderBottomWidth: hairline,
  },
  tagTitle: {
    fontWeight: fontWeight.semibold,
  },
  doneBtn: {
    position: 'absolute',
    right: pagePadding - 4,
    height: '100%',
    paddingHorizontal: 4,
    justifyContent: 'center',
  },
  doneText: {
    fontWeight: fontWeight.semibold,
  },
})
