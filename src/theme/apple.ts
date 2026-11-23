import { useMemo } from 'react'
import { StyleSheet } from 'react-native'
import { useTheme } from '@/store/theme/hook'

/**
 * Apple Music 风格的统一设计规范（颜色、圆角、间距）
 * 所有通用控件都从这里取值，保证整体风格一致
 */

export interface ApplePalette {
  isDark: boolean
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
}

const light: ApplePalette = {
  isDark: false,
  accent: '#FA2D48',
  accentPressed: '#D8263E',
  accentFill: 'rgba(250, 45, 72, 0.12)',
  onAccent: '#FFFFFF',

  background: '#FFFFFF',
  groupedBackground: '#F2F2F7',
  secondaryBackground: '#FFFFFF',
  alertBackground: 'rgba(242, 242, 247, 0.98)',
  sheetBackground: '#F2F2F7',

  label: '#000000',
  secondaryLabel: 'rgba(60, 60, 67, 0.6)',
  tertiaryLabel: 'rgba(60, 60, 67, 0.3)',
  quaternaryLabel: 'rgba(60, 60, 67, 0.18)',

  fill: 'rgba(118, 118, 128, 0.12)',
  secondaryFill: 'rgba(118, 118, 128, 0.16)',
  tertiaryFill: 'rgba(118, 118, 128, 0.2)',

  separator: 'rgba(60, 60, 67, 0.29)',
  highlight: 'rgba(0, 0, 0, 0.06)',
  mask: 'rgba(0, 0, 0, 0.4)',

  destructive: '#FF3B30',
  success: '#34C759',
  switchOn: '#34C759',
  switchOff: 'rgba(120, 120, 128, 0.16)',
  thumb: '#FFFFFF',
}

const dark: ApplePalette = {
  isDark: true,
  accent: '#FF375F',
  accentPressed: '#E02F53',
  accentFill: 'rgba(255, 55, 95, 0.18)',
  onAccent: '#FFFFFF',

  background: '#000000',
  groupedBackground: '#000000',
  secondaryBackground: '#1C1C1E',
  alertBackground: 'rgba(44, 44, 46, 0.98)',
  sheetBackground: '#1C1C1E',

  label: '#FFFFFF',
  secondaryLabel: 'rgba(235, 235, 245, 0.6)',
  tertiaryLabel: 'rgba(235, 235, 245, 0.3)',
  quaternaryLabel: 'rgba(235, 235, 245, 0.18)',

  fill: 'rgba(118, 118, 128, 0.24)',
  secondaryFill: 'rgba(118, 118, 128, 0.32)',
  tertiaryFill: 'rgba(118, 118, 128, 0.36)',

  separator: 'rgba(84, 84, 88, 0.6)',
  highlight: 'rgba(255, 255, 255, 0.08)',
  mask: 'rgba(0, 0, 0, 0.55)',

  destructive: '#FF453A',
  success: '#30D158',
  switchOn: '#30D158',
  switchOff: 'rgba(120, 120, 128, 0.32)',
  thumb: '#FFFFFF',
}

export const getApplePalette = (isDark: boolean): ApplePalette => isDark ? dark : light

/** 根据当前主题（深色 / 浅色）返回 Apple 风格配色 */
export const useApple = (): ApplePalette => {
  const theme = useTheme()
  return useMemo(() => getApplePalette(theme.isDark), [theme.isDark])
}

/** 圆角 */
export const radius = {
  /** 小按钮、标签 */
  small: 8,
  /** 输入框、普通按钮 */
  medium: 10,
  /** 分组卡片、菜单 */
  large: 12,
  /** 弹窗 Alert */
  alert: 14,
  /** 底部弹出层 */
  sheet: 16,
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

/** 页面左右留白 */
export const pagePadding = 16

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
  /** 底部标签栏高度（不含安全区） */
  tabBar: 50,
  /** 宽屏左侧边栏宽度 */
  sidebar: 260,
  /** 迷你播放条高度 */
  miniPlayer: 64,
  /** 歌曲行高度 */
  songRow: 56,
  /** 歌曲行封面 */
  songArtwork: 44,
  /** 普通设置行高度 */
  settingRow: 44,
  /** 图标按钮点击区域 */
  hitTarget: 44,
  /** 宽屏判断阈值 */
  wideBreakpoint: 768,
} as const
