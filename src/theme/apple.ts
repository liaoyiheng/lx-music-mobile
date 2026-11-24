import { useMemo } from 'react'
import { StyleSheet } from 'react-native'
import { useTheme } from '@/store/theme/hook'

/**
 * Apple Music iOS 26 液态玻璃（Liquid Glass）统一设计规范
 * 预留 MaterialStyle 扩展位，后续可扩展经典毛玻璃（frosted）与固态哑光（matte）
 */
export type MaterialStyle = 'liquid-glass' | 'frosted' | 'matte'

export const CURRENT_MATERIAL: MaterialStyle = 'liquid-glass'

export interface ApplePalette {
  isDark: boolean
  material: MaterialStyle

  /** 强调色（Apple Music 红） */
  accent: string
  accentPressed: string
  /** 强调色淡底，用于次要按钮背景 */
  accentFill: string
  onAccent: string

  /** 页面背景 */
  background: string
  /** 分组列表的页面背景 */
  groupedBackground: string
  /** 分组卡片 / 弹层背景 */
  secondaryBackground: string
  /** 弹窗（Alert）背景 */
  alertBackground: string
  /** 底部弹出层背景 */
  sheetBackground: string

  /** 文字 */
  label: string
  secondaryLabel: string
  tertiaryLabel: string
  quaternaryLabel: string

  /** 填充色：输入框、未选中按钮等 */
  fill: string
  secondaryFill: string
  tertiaryFill: string

  /** 分割线 */
  separator: string
  /** 按下时的高亮 */
  highlight: string
  /** 遮罩 */
  mask: string

  destructive: string
  success: string
  /** 开关打开颜色（iOS 绿） */
  switchOn: string
  switchOff: string
  thumb: string

  /** iOS 26 液态玻璃表面令牌 */
  glassBg: string
  glassBgDense: string
  glassBorder: string
  glassOuterBorder: string
  glassSpecularTop: string
  glassSpecularBottom: string
  glassSheen: string
  glassPillBg: string
  glassPillBorder: string
  glassBtnBg: string
  coverRing: string
  dockScrimStops: readonly [string, string, string, string]
}

const light: ApplePalette = {
  isDark: false,
  material: CURRENT_MATERIAL,
  accent: '#FA243C',
  accentPressed: '#D81E34',
  accentFill: 'rgba(250, 36, 60, 0.12)',
  onAccent: '#FFFFFF',

  background: '#FFFFFF',
  groupedBackground: '#F2F2F7',
  secondaryBackground: '#FFFFFF',
  alertBackground: 'rgba(250, 250, 253, 0.96)',
  sheetBackground: '#F2F2F7',

  label: '#000000',
  secondaryLabel: 'rgba(60, 60, 67, 0.62)',
  tertiaryLabel: 'rgba(60, 60, 67, 0.34)',
  quaternaryLabel: 'rgba(60, 60, 67, 0.18)',

  fill: 'rgba(120, 120, 128, 0.12)',
  secondaryFill: 'rgba(120, 120, 128, 0.16)',
  tertiaryFill: 'rgba(120, 120, 128, 0.20)',

  separator: 'rgba(60, 60, 67, 0.16)',
  highlight: 'rgba(0, 0, 0, 0.05)',
  mask: 'rgba(0, 0, 0, 0.36)',

  destructive: '#FF3B30',
  success: '#34C759',
  switchOn: '#34C759',
  switchOff: 'rgba(120, 120, 128, 0.16)',
  thumb: '#FFFFFF',

  glassBg: 'rgba(252, 252, 255, 0.84)',
  glassBgDense: 'rgba(252, 252, 255, 0.93)',
  glassBorder: 'rgba(255, 255, 255, 0.92)',
  glassOuterBorder: 'rgba(0, 0, 0, 0.07)',
  glassSpecularTop: 'rgba(255, 255, 255, 0.98)',
  glassSpecularBottom: 'rgba(255, 255, 255, 0.42)',
  glassSheen: 'rgba(255, 255, 255, 0.28)',
  glassPillBg: 'rgba(255, 255, 255, 0.95)',
  glassPillBorder: 'rgba(255, 255, 255, 0.98)',
  glassBtnBg: 'rgba(255, 255, 255, 0.80)',
  coverRing: 'rgba(0, 0, 0, 0.09)',
  dockScrimStops: [
    'rgba(255, 255, 255, 0.0)',
    'rgba(255, 255, 255, 0.28)',
    'rgba(255, 255, 255, 0.64)',
    'rgba(255, 255, 255, 0.92)',
  ] as const,
}

const dark: ApplePalette = {
  isDark: true,
  material: CURRENT_MATERIAL,
  accent: '#FF375F',
  accentPressed: '#E02F53',
  accentFill: 'rgba(255, 55, 95, 0.18)',
  onAccent: '#FFFFFF',

  background: '#000000',
  groupedBackground: '#000000',
  secondaryBackground: '#1C1C1E',
  alertBackground: 'rgba(38, 38, 42, 0.96)',
  sheetBackground: '#1C1C1E',

  label: '#FFFFFF',
  secondaryLabel: 'rgba(235, 235, 245, 0.62)',
  tertiaryLabel: 'rgba(235, 235, 245, 0.34)',
  quaternaryLabel: 'rgba(235, 235, 245, 0.18)',

  fill: 'rgba(120, 120, 128, 0.24)',
  secondaryFill: 'rgba(120, 120, 128, 0.32)',
  tertiaryFill: 'rgba(120, 120, 128, 0.36)',

  separator: 'rgba(84, 84, 88, 0.52)',
  highlight: 'rgba(255, 255, 255, 0.08)',
  mask: 'rgba(0, 0, 0, 0.58)',

  destructive: '#FF453A',
  success: '#30D158',
  switchOn: '#30D158',
  switchOff: 'rgba(120, 120, 128, 0.32)',
  thumb: '#FFFFFF',

  glassBg: 'rgba(34, 34, 40, 0.80)',
  glassBgDense: 'rgba(30, 30, 36, 0.92)',
  glassBorder: 'rgba(255, 255, 255, 0.20)',
  glassOuterBorder: 'rgba(0, 0, 0, 0.62)',
  glassSpecularTop: 'rgba(255, 255, 255, 0.30)',
  glassSpecularBottom: 'rgba(255, 255, 255, 0.08)',
  glassSheen: 'rgba(255, 255, 255, 0.06)',
  glassPillBg: 'rgba(255, 255, 255, 0.16)',
  glassPillBorder: 'rgba(255, 255, 255, 0.26)',
  glassBtnBg: 'rgba(44, 44, 50, 0.78)',
  coverRing: 'rgba(255, 255, 255, 0.14)',
  dockScrimStops: [
    'rgba(0, 0, 0, 0.0)',
    'rgba(0, 0, 0, 0.32)',
    'rgba(0, 0, 0, 0.68)',
    'rgba(0, 0, 0, 0.94)',
  ] as const,
}

export const getApplePalette = (isDark: boolean): ApplePalette => isDark ? dark : light

/** 根据当前主题（深色 / 浅色）返回 Apple iOS 26 液态玻璃配色 */
export const useApple = (): ApplePalette => {
  const theme = useTheme()
  return useMemo(() => getApplePalette(theme.isDark), [theme.isDark])
}

/** 圆角 */
export const radius = {
  /** 小封面、角标 */
  xs: 7,
  /** 小按钮、标签 */
  small: 8,
  /** 输入框、封面墙、普通按钮 */
  medium: 10,
  /** 操作按钮、分组卡片 */
  large: 12,
  /** 大封面、弹窗 Alert、搜索框 */
  alert: 14,
  /** 分组设置卡、底部弹出层 */
  sheet: 16,
  /** 液态玻璃弹层 */
  glassSheet: 24,
  /** 悬浮 MiniPlayer 药丸 */
  miniPlayer: 27,
  /** 悬浮胶囊 TabBar */
  tabBar: 31,
  /** 胶囊 */
  pill: 999,
} as const

/** 1 物理像素分割线 */
export const hairline = StyleSheet.hairlineWidth

/** 字号（与 iOS 动态字体对应） */
export const fontSize = {
  largeTitle: 34,
  title1: 28,
  title2: 22,
  title3: 20,
  headline: 17,
  body: 17,
  callout: 16,
  subhead: 15,
  footnote: 13,
  caption1: 12,
  caption2: 11,
} as const

/** 间距 */
export const spacing = {
  xxs: 2,
  xs: 4,
  sm: 8,
  md: 12,
  lg: 16,
  xl: 20,
  xxl: 24,
  xxxl: 32,
} as const

/** 页面左右留白（对齐设计稿 20pt 呼吸边距） */
export const pagePadding = 20

/** 字重 */
export const fontWeight = {
  regular: '400',
  medium: '500',
  semibold: '600',
  bold: '700',
  heavy: '800',
} as const

/** 常用尺寸 */
export const size = {
  /** iOS 26 悬浮胶囊底部标签栏高度 */
  tabBar: 62,
  /** 宽屏左侧边栏宽度 */
  sidebar: 260,
  /** iOS 26 悬浮药丸迷你播放条高度 */
  miniPlayer: 54,
  /** 悬浮 Dock 距左右边距 */
  dockMarginX: 14,
  /** 悬浮 Dock 距底边距 */
  dockMarginBottom: 14,
  /** MiniPlayer 与 TabBar 之间间距 */
  dockGap: 9,
  /** 滚动内容底部留白（确保内容可滑至悬浮 Dock 之上完整可见） */
  dockClearance: 156,
  /** 仅 MiniPlayer（如歌单详情页或宽屏）底部留白 */
  miniPlayerClearance: 86,
  /** 歌曲行高度 */
  songRow: 62,
  /** 歌曲行封面（对齐设计稿 46×46） */
  songArtwork: 46,
  /** 普通设置行高度 */
  settingRow: 46,
  /** 图标按钮点击区域 */
  hitTarget: 46,
  /** 歌单详情页大封面边长 */
  detailArtwork: 226,
  /** 播放 / 随机播放按钮组最大宽度（宽屏） */
  actionBarMaxWidth: 360,
  /** 宽屏判断阈值 */
  wideBreakpoint: 768,
} as const
