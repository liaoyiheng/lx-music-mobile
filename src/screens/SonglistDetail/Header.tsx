import { forwardRef, memo, useEffect, useImperativeHandle, useState } from 'react'
import { StyleSheet, TouchableOpacity, View, useWindowDimensions } from 'react-native'
import ButtonBar from './ActionBar'
import { useNavigationComponentDidAppear } from '@/navigation'
import { NAV_SHEAR_NATIVE_IDS } from '@/config/constant'
import Text from '@/components/common/Text'
import Image from '@/components/common/Image'
import { CoverArt } from '@/components/apple/LiquidGlass'
import { useStatusbarHeight } from '@/store/common/hook'
import { useI18n } from '@/lang'
import { createStyle } from '@/utils/tools'
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
    <View
      style={[
        styles.artworkShadow,
        {
          width,
          height: width,
          borderRadius: radius.alert,
          shadowOpacity: apple.isDark ? 0.52 : 0.22,
        },
      ]}
    >
      <CoverArt
        uri={pic}
        size={width}
        borderRadius={radius.alert}
        nativeID={`${NAV_SHEAR_NATIVE_IDS.songlistDetail_pic}_to_${info.id}`}
      />
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
  const t = useI18n()
  const info = useListInfo()
  const statusBarHeight = useStatusbarHeight()
  const { width: windowWidth } = useWindowDimensions()
  const [detailInfo, setDetailInfo] = useState<DetailInfo>({ name: info.name ?? '', desc: '', playCount: '', imgUrl: info.img })
  const [descExpanded, setDescExpanded] = useState(false)
  const [descTruncated, setDescTruncated] = useState(false)

  useImperativeHandle(ref, () => ({
    setInfo(nextInfo) {
      setDetailInfo(nextInfo)
      setDescExpanded(false)
      setDescTruncated(false)
    },
  }), [])

  // 窄屏下封面不超过屏宽的 65%
  const artworkWidth = Math.min(size.detailArtwork, Math.round(windowWidth * 0.65))
  const heroHeight = statusBarHeight + 370
  const authorText = info.author || detailInfo.playCount
  const subText = info.author && detailInfo.playCount ? detailInfo.playCount : ''

  const veilStops = apple.isDark
    ? [
        'rgba(0, 0, 0, 0.14)',
        'rgba(0, 0, 0, 0.36)',
        'rgba(0, 0, 0, 0.78)',
        apple.background,
      ]
    : [
        'rgba(255, 255, 255, 0.24)',
        'rgba(255, 255, 255, 0.56)',
        'rgba(255, 255, 255, 0.90)',
        apple.background,
      ]

  return (
    <View style={[styles.container, { paddingTop: statusBarHeight + 56, backgroundColor: apple.background }]}>
      {/* 沉浸式封面模糊融色背景，延伸至状态栏并自然过渡到 apple.background */}
      <View pointerEvents="none" style={[styles.heroBackdrop, { height: heroHeight }]}>
        {detailInfo.imgUrl ? (
          <Image
            url={detailInfo.imgUrl}
            blurRadius={50}
            resizeMode="cover"
            style={[styles.heroBlurImage, { opacity: apple.isDark ? 0.72 : 0.62 }]}
          />
        ) : null}
        <View style={StyleSheet.absoluteFillObject}>
          <View style={{ flex: 1, backgroundColor: veilStops[0] }} />
          <View style={{ flex: 1, backgroundColor: veilStops[1] }} />
          <View style={{ flex: 1, backgroundColor: veilStops[2] }} />
          <View style={{ flex: 0.9, backgroundColor: veilStops[3] }} />
        </View>
      </View>

      <Pic componentId={componentId} imgUrl={detailInfo.imgUrl} width={artworkWidth} />

      <View style={styles.titleBox} nativeID={NAV_SHEAR_NATIVE_IDS.songlistDetail_title}>
        <Text style={styles.title} size={fontSize.title2} color={apple.label} numberOfLines={2}>
          {detailInfo.name}
        </Text>
        {authorText ? (
          <Text style={styles.author} size={fontSize.callout} color={apple.accent} numberOfLines={1}>
            {authorText}
          </Text>
        ) : null}
        {subText ? (
          <Text style={styles.subline} size={fontSize.caption1} color={apple.secondaryLabel} numberOfLines={1}>
            {subText}
          </Text>
        ) : null}
      </View>

      <ButtonBar style={styles.actionBar} />

      {detailInfo.desc ? (
        <View style={styles.descWrap}>
          <Text
            style={styles.desc}
            size={13.5}
            color={apple.secondaryLabel}
            numberOfLines={descExpanded ? undefined : 2}
            onTextLayout={(e) => {
              if (!descExpanded && e.nativeEvent.lines.length > 2 && !descTruncated) {
                setDescTruncated(true)
              }
            }}
          >
            {detailInfo.desc}
          </Text>
          {(descTruncated || descExpanded || detailInfo.desc.length > 54) ? (
            <TouchableOpacity
              activeOpacity={0.6}
              onPress={() => { setDescExpanded(prev => !prev) }}
              style={styles.moreBtn}
            >
              <Text size={13.5} color={apple.accent} style={styles.moreText}>
                {descExpanded ? t('apple_detail_less') : t('apple_detail_more')}
              </Text>
            </TouchableOpacity>
          ) : null}
        </View>
      ) : null}
    </View>
  )
})

const styles = createStyle({
  container: {
    flexDirection: 'column',
    flexWrap: 'nowrap',
    alignItems: 'center',
    paddingHorizontal: pagePadding,
    paddingBottom: spacing.lg,
    position: 'relative',
    overflow: 'hidden',
  },
  heroBackdrop: {
    position: 'absolute',
    top: 0,
    left: 0,
    right: 0,
    overflow: 'hidden',
  },
  heroBlurImage: {
    width: '100%',
    height: '100%',
    transform: [{ scale: 1.15 }],
  },
  artworkShadow: {
    shadowColor: '#000000',
    shadowRadius: 22,
    shadowOffset: { width: 0, height: 12 },
    elevation: 10,
  },
  titleBox: {
    alignSelf: 'stretch',
    alignItems: 'center',
    marginTop: 18,
  },
  title: {
    textAlign: 'center',
    fontWeight: fontWeight.bold,
    letterSpacing: -0.45,
  },
  author: {
    textAlign: 'center',
    marginTop: spacing.xs,
    fontWeight: fontWeight.medium,
    letterSpacing: -0.2,
  },
  subline: {
    textAlign: 'center',
    marginTop: 5,
    fontWeight: fontWeight.medium,
  },
  actionBar: {
    marginTop: 18,
    width: '100%',
    maxWidth: size.actionBarMaxWidth,
    alignSelf: 'center',
  },
  descWrap: {
    alignSelf: 'stretch',
    marginTop: 15,
  },
  desc: {
    lineHeight: 20,
  },
  moreBtn: {
    marginTop: 2,
    alignSelf: 'flex-start',
    paddingVertical: 2,
  },
  moreText: {
    fontWeight: fontWeight.medium,
  },
})
