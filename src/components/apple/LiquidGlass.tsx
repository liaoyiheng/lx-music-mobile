import { memo, useEffect, useRef, type ReactNode } from 'react'
import {
  Animated,
  Image as RNImage,
  StyleSheet,
  TouchableOpacity,
  View,
  type StyleProp,
  type ViewStyle,
} from 'react-native'
import { IonIcon } from '@/components/common/Icon'
import Image from '@/components/common/Image'
import Text from '@/components/common/Text'
import { useApple, fontWeight, radius } from '@/theme/apple'
import { createStyle } from '@/utils/tools'

interface GlassSurfaceProps {
  children?: ReactNode
  style?: StyleProp<ViewStyle>
  innerStyle?: StyleProp<ViewStyle>
  borderRadius?: number
  /** 更致密的玻璃材质（吸顶导航、上下文菜单） */
  dense?: boolean
  /** 暗色底上使用（全屏播放页内玻璃控件） */
  onDark?: boolean
  /** 可选：背景封面 URI，用于真实弥散透色折射效果 */
  backdropUri?: string | null
  /** 是否显示柔和阴影 */
  shadow?: boolean
}

/**
 * iOS 26 液态玻璃（Liquid Glass）核心表面组件
 * 通过半透明玻璃底、顶部镜面高光折射边、柔光斜向光泽与外缘描边还原液态玻璃质感
 */
export const GlassSurface = memo(({
  children,
  style,
  innerStyle,
  borderRadius = radius.tabBar,
  dense = false,
  onDark = false,
  backdropUri,
  shadow = true,
}: GlassSurfaceProps) => {
  const apple = useApple()

  const bgColor = onDark
    ? (dense ? 'rgba(255, 255, 255, 0.20)' : 'rgba(255, 255, 255, 0.14)')
    : (dense ? apple.glassBgDense : apple.glassBg)

  const borderColor = onDark
    ? 'rgba(255, 255, 255, 0.24)'
    : apple.glassBorder

  const outerBorderColor = onDark
    ? 'rgba(0, 0, 0, 0.18)'
    : apple.glassOuterBorder

  const specularTop = onDark
    ? 'rgba(255, 255, 255, 0.36)'
    : apple.glassSpecularTop

  const specularBottom = onDark
    ? 'rgba(255, 255, 255, 0.08)'
    : apple.glassSpecularBottom

  const sheenColor = onDark
    ? 'rgba(255, 255, 255, 0.06)'
    : apple.glassSheen

  return (
    <View
      style={[
        styles.outer,
        {
          borderRadius,
          borderColor: outerBorderColor,
          shadowColor: '#000000',
          shadowOpacity: shadow ? (apple.isDark || onDark ? 0.42 : 0.14) : 0,
          shadowRadius: shadow ? 18 : 0,
          shadowOffset: { width: 0, height: shadow ? 8 : 0 },
          elevation: shadow ? (apple.isDark ? 10 : 8) : 0,
        },
        style,
      ]}
    >
      <View
        style={[
          styles.inner,
          {
            borderRadius,
            backgroundColor: bgColor,
            borderColor,
          },
          innerStyle,
        ]}
      >
        {/* 可选封面模糊弥散透色层 */}
        {backdropUri ? (
          <RNImage
            source={{ uri: backdropUri }}
            blurRadius={40}
            style={[styles.backdropImage, { opacity: apple.isDark || onDark ? 0.26 : 0.14 }]}
            resizeMode="cover"
          />
        ) : null}

        {/* 上半区柔和液态光泽折射层 */}
        <View
          pointerEvents="none"
          style={[
            styles.sheen,
            {
              backgroundColor: sheenColor,
              borderTopLeftRadius: borderRadius,
              borderTopRightRadius: borderRadius,
            },
          ]}
        />

        {/* 顶部镜面高光细线 */}
        <View
          pointerEvents="none"
          style={[
            styles.specularTop,
            {
              backgroundColor: specularTop,
              left: Math.min(borderRadius * 0.45, 18),
              right: Math.min(borderRadius * 0.45, 18),
            },
          ]}
        />

        {/* 底部柔和反光细线 */}
        <View
          pointerEvents="none"
          style={[
            styles.specularBottom,
            {
              backgroundColor: specularBottom,
              left: Math.min(borderRadius * 0.5, 20),
              right: Math.min(borderRadius * 0.5, 20),
            },
          ]}
        />

        {children}
      </View>
    </View>
  )
})

interface GlassCapsuleButtonProps {
  label?: string
  icon?: string
  rightIcon?: string
  onPress?: () => void
  onLongPress?: () => void
  onDark?: boolean
  color?: string
  style?: StyleProp<ViewStyle>
  accessibilityLabel?: string
}

/**
 * 液态玻璃胶囊按钮（如「编辑」「小蜗音乐 ▾」「‹ 设置」「‹ 资料库」）
 */
export const GlassCapsuleButton = memo(({
  label,
  icon,
  rightIcon,
  onPress,
  onLongPress,
  onDark = false,
  color,
  style,
  accessibilityLabel,
}: GlassCapsuleButtonProps) => {
  const apple = useApple()
  const tint = color ?? (onDark ? '#FFFFFF' : apple.accent)

  return (
    <TouchableOpacity
      activeOpacity={0.68}
      onPress={onPress}
      onLongPress={onLongPress}
      accessibilityRole="button"
      accessibilityLabel={accessibilityLabel ?? label}
      style={style}
    >
      <GlassSurface
        borderRadius={radius.pill}
        onDark={onDark}
        shadow={!onDark}
        innerStyle={styles.capsuleInner}
      >
        {icon ? <IonIcon name={icon} rawSize={17} color={tint} /> : null}
        {label ? (
          <Text
            size={13.5}
            color={tint}
            style={styles.capsuleLabel}
            numberOfLines={1}
          >
            {label}
          </Text>
        ) : null}
        {rightIcon ? <IonIcon name={rightIcon} rawSize={14} color={tint} /> : null}
      </GlassSurface>
    </TouchableOpacity>
  )
})

interface GlassCircleButtonProps {
  icon: string
  iconSize?: number
  size?: number
  onPress?: () => void
  onLongPress?: () => void
  onDark?: boolean
  active?: boolean
  color?: string
  style?: StyleProp<ViewStyle>
  accessibilityLabel?: string
}

/**
 * 液态玻璃圆形按钮（如「+」「‹」「···」「★」）
 */
export const GlassCircleButton = memo(({
  icon,
  iconSize = 18,
  size: btnSize = 34,
  onPress,
  onLongPress,
  onDark = false,
  active = false,
  color,
  style,
  accessibilityLabel,
}: GlassCircleButtonProps) => {
  const apple = useApple()
  const tint = color ?? (active ? apple.accent : (onDark ? '#FFFFFF' : apple.accent))

  if (active && onDark) {
    return (
      <TouchableOpacity
        activeOpacity={0.72}
        onPress={onPress}
        onLongPress={onLongPress}
        accessibilityRole="button"
        accessibilityLabel={accessibilityLabel}
        style={[
          {
            width: btnSize,
            height: btnSize,
            borderRadius: btnSize / 2,
            backgroundColor: 'rgba(255, 255, 255, 0.94)',
            alignItems: 'center',
            justifyContent: 'center',
          },
          style,
        ]}
      >
        <IonIcon name={icon} rawSize={iconSize} color={apple.accent} />
      </TouchableOpacity>
    )
  }

  return (
    <TouchableOpacity
      activeOpacity={0.68}
      onPress={onPress}
      onLongPress={onLongPress}
      accessibilityRole="button"
      accessibilityLabel={accessibilityLabel}
      style={style}
    >
      <GlassSurface
        borderRadius={btnSize / 2}
        onDark={onDark}
        shadow={!onDark}
        innerStyle={{
          width: btnSize,
          height: btnSize,
          alignItems: 'center',
          justifyContent: 'center',
        }}
      >
        <IonIcon name={icon} rawSize={iconSize} color={tint} />
      </GlassSurface>
    </TouchableOpacity>
  )
})

/**
 * 底部悬浮 Dock 背后的柔和渐隐遮罩，避免内容在胶囊下生硬截断
 */
export const BottomDockScrim = memo(({ height = 148 }: { height?: number }) => {
  const apple = useApple()
  const [s0, s1, s2, s3] = apple.dockScrimStops

  return (
    <View pointerEvents="none" style={[styles.scrimContainer, { height }]}>
      <View style={{ flex: 1, backgroundColor: s0 }} />
      <View style={{ flex: 1, backgroundColor: s1 }} />
      <View style={{ flex: 1, backgroundColor: s2 }} />
      <View style={{ flex: 1.2, backgroundColor: s3 }} />
    </View>
  )
})

interface CoverArtProps {
  uri?: string | number | null
  size?: number
  borderRadius?: number
  style?: StyleProp<ViewStyle>
  nativeID?: string
  onError?: (url: string | number) => void
  children?: ReactNode
}

/**
 * Apple Music 规范封面盒：自带 0.5px 内描边环，保证浅色封面在白底上依然边缘清晰
 */
export const CoverArt = memo(({
  uri,
  size: artSize,
  borderRadius = radius.medium,
  style,
  nativeID,
  onError,
  children,
}: CoverArtProps) => {
  const apple = useApple()
  const sizeStyle = artSize != null ? { width: artSize, height: artSize } : undefined

  return (
    <View style={[styles.coverBox, sizeStyle, { borderRadius, backgroundColor: apple.fill }, style]}>
      <Image
        url={uri}
        nativeID={nativeID}
        onError={onError}
        style={[StyleSheet.absoluteFillObject, { borderRadius }]}
      />
      {children}
      <View
        pointerEvents="none"
        style={[
          StyleSheet.absoluteFillObject,
          {
            borderRadius,
            borderWidth: StyleSheet.hairlineWidth,
            borderColor: apple.coverRing,
          },
        ]}
      />
    </View>
  )
})

/**
 * 正在播放曲目的三柱动态声波指示器（Apple Music 红 / 纯白）
 */
export const EqualizerBars = memo(({
  color,
  playing = true,
  size: eqSize = 14,
}: {
  color?: string
  playing?: boolean
  size?: number
}) => {
  const apple = useApple()
  const barColor = color ?? apple.accent
  const b1 = useRef(new Animated.Value(0.45)).current
  const b2 = useRef(new Animated.Value(0.9)).current
  const b3 = useRef(new Animated.Value(0.6)).current

  useEffect(() => {
    if (!playing) {
      b1.setValue(0.35)
      b2.setValue(0.55)
      b3.setValue(0.4)
      return
    }
    const mk = (val: Animated.Value, toA: number, toB: number, dur: number) =>
      Animated.loop(
        Animated.sequence([
          Animated.timing(val, { toValue: toA, duration: dur, useNativeDriver: true }),
          Animated.timing(val, { toValue: toB, duration: dur, useNativeDriver: true }),
        ]),
      )
    const a1 = mk(b1, 0.95, 0.25, 340)
    const a2 = mk(b2, 0.3, 1.0, 290)
    const a3 = mk(b3, 0.85, 0.28, 390)
    a1.start()
    a2.start()
    a3.start()
    return () => {
      a1.stop()
      a2.stop()
      a3.stop()
    }
  }, [playing, b1, b2, b3])

  const barW = Math.max(2.2, eqSize * 0.19)
  return (
    <View style={[styles.eqWrap, { width: eqSize, height: eqSize }]}>
      {[b1, b2, b3].map((anim, idx) => (
        <Animated.View
          key={idx}
          style={{
            width: barW,
            height: eqSize,
            borderRadius: barW / 2,
            backgroundColor: barColor,
            transform: [{ scaleY: anim }],
          }}
        />
      ))}
    </View>
  )
})

const styles = createStyle({
  outer: {
    borderWidth: StyleSheet.hairlineWidth,
  },
  inner: {
    overflow: 'hidden',
    borderWidth: 0.75,
  },
  backdropImage: {
    position: 'absolute',
    top: -16,
    bottom: -16,
    left: -16,
    right: -16,
  },
  sheen: {
    position: 'absolute',
    top: 0,
    left: 0,
    right: 0,
    height: '46%',
  },
  specularTop: {
    position: 'absolute',
    top: 0,
    height: 1.2,
    borderRadius: 1,
  },
  specularBottom: {
    position: 'absolute',
    bottom: 0,
    height: 0.8,
    borderRadius: 1,
  },
  capsuleInner: {
    height: 32,
    paddingHorizontal: 12,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 4,
  },
  capsuleLabel: {
    fontWeight: fontWeight.semibold,
    letterSpacing: -0.1,
  },
  scrimContainer: {
    position: 'absolute',
    left: 0,
    right: 0,
    bottom: 0,
    flexDirection: 'column',
  },
  coverBox: {
    overflow: 'hidden',
    position: 'relative',
  },
  eqWrap: {
    flexDirection: 'row',
    alignItems: 'flex-end',
    justifyContent: 'space-between',
  },
})
