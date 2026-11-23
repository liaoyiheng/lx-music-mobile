import { createStyle } from '@/utils/tools'
import { radius, spacing } from '@/theme/apple'

export default createStyle({
  section: {
    marginBottom: spacing.lg,
  },
  sectionTitle: {
    marginBottom: spacing.xs,
    paddingHorizontal: spacing.md,
    fontWeight: '500',
  },
  card: {
    borderRadius: radius.large,
    overflow: 'hidden',
  },
  sliderCard: {
    borderRadius: radius.large,
    paddingHorizontal: spacing.lg,
    paddingVertical: spacing.md,
  },
  content: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  label: {
    width: 52,
    textAlign: 'left',
    fontVariant: ['tabular-nums'],
  },
  resetRow: {
    marginTop: spacing.xs,
    flexDirection: 'row',
    justifyContent: 'flex-end',
  },
})
