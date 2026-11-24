import { memo } from 'react'
import { StyleSheet, TouchableOpacity, View } from 'react-native'
import Text from '@/components/common/Text'
import { IonIcon } from '@/components/common/Icon'
import { useI18n } from '@/lang'
import { createStyle } from '@/utils/tools'
import { useApple, fontSize, fontWeight, radius, spacing } from '@/theme/apple'

interface CategoryItem {
  id: string
  title: string
  keyword: string
  base: string
  glow1: string
  glow2: string
  icon: string
}

const CATEGORIES: readonly CategoryItem[] = [
  {
    id: 'mandopop',
    title: '华语流行',
    keyword: '华语流行',
    base: '#FA243C',
    glow1: 'rgba(255, 136, 94, 0.56)',
    glow2: 'rgba(138, 12, 46, 0.50)',
    icon: 'musical-notes',
  },
  {
    id: 'folk',
    title: '民谣',
    keyword: '民谣',
    base: '#0E9F6E',
    glow1: 'rgba(110, 231, 183, 0.44)',
    glow2: 'rgba(6, 78, 59, 0.52)',
    icon: 'leaf',
  },
  {
    id: 'electronic',
    title: '电子',
    keyword: '电子',
    base: '#6320EE',
    glow1: 'rgba(56, 189, 248, 0.52)',
    glow2: 'rgba(30, 14, 98, 0.56)',
    icon: 'pulse',
  },
  {
    id: 'western',
    title: '欧美',
    keyword: '欧美',
    base: '#2563EB',
    glow1: 'rgba(129, 140, 248, 0.52)',
    glow2: 'rgba(15, 23, 72, 0.56)',
    icon: 'globe-outline',
  },
  {
    id: 'rock',
    title: '摇滚',
    keyword: '摇滚',
    base: '#DC2626',
    glow1: 'rgba(251, 146, 60, 0.50)',
    glow2: 'rgba(69, 10, 10, 0.58)',
    icon: 'flame',
  },
  {
    id: 'acg',
    title: 'ACG',
    keyword: 'ACG',
    base: '#C026D3',
    glow1: 'rgba(251, 113, 133, 0.52)',
    glow2: 'rgba(74, 4, 78, 0.54)',
    icon: 'sparkles',
  },
  {
    id: 'chill',
    title: '轻音乐',
    keyword: '轻音乐',
    base: '#0F4C5C',
    glow1: 'rgba(94, 234, 212, 0.36)',
    glow2: 'rgba(9, 25, 36, 0.58)',
    icon: 'moon',
  },
  {
    id: 'cantopop',
    title: '粤语经典',
    keyword: '粤语经典',
    base: '#D97706',
    glow1: 'rgba(253, 224, 71, 0.44)',
    glow2: 'rgba(120, 32, 12, 0.54)',
    icon: 'disc',
  },
]

const CATEGORY_ROWS: ReadonlyArray<readonly [CategoryItem, CategoryItem]> = [
  [CATEGORIES[0], CATEGORIES[1]],
  [CATEGORIES[2], CATEGORIES[3]],
  [CATEGORIES[4], CATEGORIES[5]],
  [CATEGORIES[6], CATEGORIES[7]],
]

const CategoryCard = memo(({ item, onSearch }: {
  item: CategoryItem
  onSearch: (keyword: string) => void
}) => {
  return (
    <TouchableOpacity
      activeOpacity={0.82}
      onPress={() => { onSearch(item.keyword) }}
      style={[styles.card, { backgroundColor: item.base }]}
    >
      {/* 装饰性半透明几何圆弧 */}
      <View pointerEvents="none" style={[styles.arcGlowTop, { backgroundColor: item.glow1 }]} />
      <View pointerEvents="none" style={styles.arcRing} />
      <View pointerEvents="none" style={[styles.arcGlowBottom, { backgroundColor: item.glow2 }]} />

      {/* 右上角低透明度大号 Ionicon */}
      <IonIcon
        name={item.icon}
        rawSize={44}
        color="rgba(255, 255, 255, 0.22)"
        style={styles.decoIcon}
      />

      {/* 左下角 16pt Bold 纯白标题 */}
      <Text size={fontSize.callout} color="#FFFFFF" style={styles.title} numberOfLines={1}>
        {item.title}
      </Text>

      <View pointerEvents="none" style={styles.borderOverlay} />
    </TouchableOpacity>
  )
})

/**
 * 双列彩色「浏览类别」海报网格（8 张高饱和苹果风彩色海报卡）
 */
export default memo(({ onSearch }: { onSearch: (keyword: string) => void }) => {
  const apple = useApple()
  const t = useI18n()

  return (
    <View style={styles.section}>
      <Text size={fontSize.title3} color={apple.label} style={styles.sectionTitle}>
        {t('apple_search_categories')}
      </Text>
      <View style={styles.grid}>
        {CATEGORY_ROWS.map(([left, right], idx) => (
          <View key={idx} style={styles.row}>
            <CategoryCard item={left} onSearch={onSearch} />
            <CategoryCard item={right} onSearch={onSearch} />
          </View>
        ))}
      </View>
    </View>
  )
})

const styles = createStyle({
  section: {
    paddingTop: spacing.lg + 2,
  },
  sectionTitle: {
    fontWeight: fontWeight.bold,
    letterSpacing: -0.3,
    marginBottom: spacing.md,
  },
  grid: {
    gap: 12,
  },
  row: {
    flexDirection: 'row',
    gap: 12,
  },
  card: {
    flex: 1,
    height: 96,
    borderRadius: radius.alert,
    overflow: 'hidden',
    position: 'relative',
    shadowColor: '#000000',
    shadowOpacity: 0.14,
    shadowRadius: 14,
    shadowOffset: { width: 0, height: 6 },
    elevation: 4,
  },
  arcGlowTop: {
    position: 'absolute',
    top: -34,
    right: -22,
    width: 104,
    height: 104,
    borderRadius: 52,
  },
  arcRing: {
    position: 'absolute',
    top: -18,
    right: -12,
    width: 78,
    height: 78,
    borderRadius: 39,
    borderWidth: 1.5,
    borderColor: 'rgba(255, 255, 255, 0.28)',
  },
  arcGlowBottom: {
    position: 'absolute',
    bottom: -44,
    left: -26,
    width: 120,
    height: 120,
    borderRadius: 60,
  },
  decoIcon: {
    position: 'absolute',
    top: 10,
    right: 12,
  },
  title: {
    position: 'absolute',
    left: 13,
    right: 12,
    bottom: 11,
    fontWeight: fontWeight.bold,
    letterSpacing: -0.2,
    textShadowColor: 'rgba(0, 0, 0, 0.28)',
    textShadowOffset: { width: 0, height: 1 },
    textShadowRadius: 6,
  },
  borderOverlay: {
    ...StyleSheet.absoluteFillObject,
    borderRadius: radius.alert,
    borderWidth: StyleSheet.hairlineWidth,
    borderColor: 'rgba(255, 255, 255, 0.24)',
  },
})
