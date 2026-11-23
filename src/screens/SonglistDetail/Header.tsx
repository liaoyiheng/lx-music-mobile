import { forwardRef, memo, useEffect, useImperativeHandle, useState } from 'react'
import { View, useWindowDimensions } from 'react-native'
import ButtonBar from './ActionBar'
import { useNavigationComponentDidAppear } from '@/navigation'
import { NAV_SHEAR_NATIVE_IDS } from '@/config/constant'
import Text from '@/components/common/Text'
import { createStyle } from '@/utils/tools'
import Image from '@/components/common/Image'
import { useListInfo } from './state'
import { useApple, fontSize, fontWeight, pagePadding, radius, size, spacing } from '@/theme/apple'

/**
 * 大封面：等动画完成后再切换到详情返回的图片，避免共享元素过渡时闪烁
 */
const Pic = memo(({ componentId, imgUrl, width }: {
  componentId: string
  imgUrl?: string
  width: number
}) => {
  const apple = useApple()
  const [pic, setPic] = useState(imgUrl)
  const [animated, setAnimated] = useState(false)
  const info = useListInfo()
  useEffect(() => {
    if (animated) setPic(imgUrl)
  }, [imgUrl, animated])

  useNavigationComponentDidAppear(componentId, () => {
    setAnimated(true)
  })

  return (
    <View style={{ ...styles.artwork, width, height: width, backgroundColor: apple.fill }}>
      <Image nativeID={`${NAV_SHEAR_NATIVE_IDS.songlistDetail_pic}_to_${info.id}`} url={pic} style={styles.artworkImg} />
    </View>
  )
})

export interface HeaderProps {
  componentId: string
}

export interface HeaderType {
  setInfo: (info: DetailInfo) => void
}
export interface DetailInfo {
  name: string
  desc: string
  playCount: string
  imgUrl?: string
}

export default forwardRef<HeaderType, HeaderProps>(({ componentId }: { componentId: string }, ref) => {
  const apple = useApple()
  const info = useListInfo()
  const { width: windowWidth } = useWindowDimensions()
  const [detailInfo, setDetailInfo] = useState<DetailInfo>({ name: info.name ?? '', desc: '', playCount: '', imgUrl: info.img })

  useImperativeHandle(ref, () => ({
    setInfo(info) {
      setDetailInfo(info)
    },
  }), [])

  // 窄屏下封面不超过屏宽的 65%
  const artworkWidth = Math.min(size.detailArtwork, Math.round(windowWidth * 0.65))
  const meta = [info.author, detailInfo.playCount].filter(Boolean).join(' · ')

  return (
    <View style={styles.container}>
      <Pic componentId={componentId} imgUrl={detailInfo.imgUrl} width={artworkWidth} />
      <View style={styles.titleBox} nativeID={NAV_SHEAR_NATIVE_IDS.songlistDetail_title}>
        <Text style={styles.title} size={fontSize.title2} color={apple.label} numberOfLines={2}>{detailInfo.name}</Text>
        {
          meta ? <Text style={styles.meta} size={fontSize.title3} color={apple.accent} numberOfLines={1}>{meta}</Text> : null
        }
        {
          detailInfo.desc ? <Text style={styles.desc} size={fontSize.footnote} color={apple.secondaryLabel} numberOfLines={3}>{detailInfo.desc}</Text> : null
        }
      </View>
      <ButtonBar style={styles.actionBar} />
    </View>
  )
})

const styles = createStyle({
  container: {
    flexDirection: 'column',
    flexWrap: 'nowrap',
    alignItems: 'center',
    paddingHorizontal: pagePadding,
    paddingTop: spacing.sm,
    paddingBottom: spacing.lg,
  },
  artwork: {
    flexGrow: 0,
    flexShrink: 0,
    overflow: 'hidden',
    borderRadius: radius.large,
    elevation: 6,
  },
  artworkImg: {
    flex: 1,
    borderRadius: radius.large,
  },
  titleBox: {
    alignSelf: 'stretch',
    alignItems: 'center',
    marginTop: spacing.lg,
  },
  title: {
    textAlign: 'center',
    fontWeight: fontWeight.bold,
  },
  meta: {
    textAlign: 'center',
    marginTop: spacing.xxs,
    fontWeight: fontWeight.medium,
  },
  desc: {
    textAlign: 'center',
    marginTop: spacing.sm,
  },
  actionBar: {
    marginTop: spacing.lg,
    width: '100%',
    maxWidth: size.actionBarMaxWidth,
    alignSelf: 'center',
  },
})
