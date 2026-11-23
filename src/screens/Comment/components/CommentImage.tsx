import { useEffect, useState } from 'react'
import { TouchableOpacity, View } from 'react-native'
import Image, { getSize } from '@/components/common/Image'
import { createStyle } from '@/utils/tools'
import Text from '@/components/common/Text'
import { scaleSizeH } from '@/utils/pixelRatio'
import { Icon } from '@/components/common/Icon'
import { useApple, fontSize, fontWeight, radius, spacing } from '@/theme/apple'

const MAX_IMAGE_HEIGHT = scaleSizeH(260)

/**
 * 评论图片：默认显示圆角占位块，点击后加载原图（保持原有按需加载行为）
 */
export default ({ url, maxWidth }: { url: string, maxWidth: number }) => {
  const [show, setShow] = useState(false)
  const [wh, setWH] = useState({ width: 0, height: 0 })
  const apple = useApple()

  useEffect(() => {
    getSize(url, (realWidth, realHeight) => {
      let w = 0
      let h = 0
      if (w && !h) {
        h = realHeight * (w / realWidth)
      } else if (!w && h) {
        w = realWidth * (h / realHeight)
      } else {
        if (maxWidth && realWidth > maxWidth) {
          w = maxWidth
          h = realHeight * (w / realWidth)
          if (MAX_IMAGE_HEIGHT && h > MAX_IMAGE_HEIGHT) {
            w = realWidth * (MAX_IMAGE_HEIGHT / realHeight)
            h = MAX_IMAGE_HEIGHT
          }
        } else if (MAX_IMAGE_HEIGHT && realHeight > MAX_IMAGE_HEIGHT) {
          w = realWidth * (h / realHeight)
          h = MAX_IMAGE_HEIGHT
        }
      }
      setWH({ width: w || realWidth, height: h || realHeight })
    })
  }, [maxWidth, url])

  return (
    wh.width ? (
      <View style={{ ...styles.container, height: wh.height, width: wh.width }}>
        {
          show ? (
            <Image
              url={url}
              style={{ height: wh.height, width: wh.width, borderRadius: radius.medium }}
            />
          ) : (
            <TouchableOpacity style={{ ...styles.defaultPic, backgroundColor: apple.fill }} activeOpacity={0.6} onPress={() => { setShow(true) }}>
              <Icon name="album" size={fontSize.title3} color={apple.secondaryLabel} />
              <Text style={styles.label} size={fontSize.footnote} color={apple.accent}>{global.i18n.t('comment_show_image')}</Text>
            </TouchableOpacity>
          )
        }
      </View>
    ) : null
  )
}

const styles = createStyle({
  container: {
    borderRadius: radius.medium,
    overflow: 'hidden',
  },
  defaultPic: {
    width: '100%',
    height: '100%',
    alignItems: 'center',
    justifyContent: 'center',
    borderRadius: radius.medium,
  },
  label: {
    marginTop: spacing.xs,
    fontWeight: fontWeight.medium,
  },
})
