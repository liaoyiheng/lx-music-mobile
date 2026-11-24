import { StyleSheet } from 'react-native'
import { createStyle } from '@/utils/tools'
import { fontSize, fontWeight, radius, spacing } from '@/theme/apple'

export const GLASS_DARK_CARD_BG = 'rgba(255, 255, 255, 0.10)'
export const GLASS_DARK_CARD_BORDER = 'rgba(255, 255, 255, 0.16)'
export const GLASS_DARK_SEP = 'rgba(255, 255, 255, 0.12)'
export const GLASS_DARK_LABEL = '#FFFFFF'
export const GLASS_DARK_SUB_LABEL = 'rgba(235, 235, 245, 0.68)'
export const GLASS_DARK_ACCENT = '#FF375F'
export const GLASS_DARK_SWITCH_OFF = 'rgba(255, 255, 255, 0.24)'
export const GLASS_DARK_SWITCH_ON = '#30D158'

export default createStyle({
  section: {
    marginBottom: spacing.lg,
  },
  sectionTitle: {
    marginBottom: spacing.xs,
    paddingHorizontal: spacing.md,
    fontWeight: fontWeight.medium,
  },
  card: {
    borderRadius: radius.sheet,
    overflow: 'hidden',
    backgroundColor: GLASS_DARK_CARD_BG,
    borderWidth: StyleSheet.hairlineWidth,
    borderColor: GLASS_DARK_CARD_BORDER,
  },
  sliderCard: {
    borderRadius: radius.sheet,
    paddingHorizontal: spacing.lg,
    paddingVertical: spacing.md,
    backgroundColor: GLASS_DARK_CARD_BG,
    borderWidth: StyleSheet.hairlineWidth,
    borderColor: GLASS_DARK_CARD_BORDER,
  },
  content: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  label: {
    width: 52,
    textAlign: 'left',
    fontVariant: ['tabular-nums'],
    fontWeight: fontWeight.semibold,
  },
  resetRow: {
    marginTop: spacing.sm,
    flexDirection: 'row',
    justifyContent: 'flex-end',
  },
  resetBtn: {
    paddingHorizontal: 14,
    paddingVertical: 6,
    borderRadius: radius.pill,
    backgroundColor: 'rgba(255, 255, 255, 0.16)',
    borderWidth: StyleSheet.hairlineWidth,
    borderColor: 'rgba(255, 255, 255, 0.22)',
  },
  resetBtnText: {
    fontWeight: fontWeight.semibold,
    fontSize: fontSize.footnote,
  },
  row: {
    minHeight: 46,
    paddingVertical: 10,
    paddingHorizontal: 16,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: spacing.md,
  },
  rowLabel: {
    flex: 1,
    fontSize: fontSize.callout,
    lineHeight: 22,
    color: GLASS_DARK_LABEL,
  },
  rowSep: {
    height: StyleSheet.hairlineWidth,
    backgroundColor: GLASS_DARK_SEP,
    marginLeft: 16,
  },
})
