import Text from '@/components/common/Text'
import { createStyle } from '@/utils/tools'
import { useApple, fontWeight } from '@/theme/apple'
import { useI18n } from '@/lang'

export default ({ musicInfo, isMove }: {
  musicInfo: LX.Music.MusicInfo
  isMove: boolean
}) => {
  const apple = useApple()
  const t = useI18n()
  return (
    <Text style={styles.title} size={16} color={apple.label} numberOfLines={2}>
      {t(isMove ? 'list_add_title_first_move' : 'list_add_title_first_add')}{' '}
      <Text size={16} color={apple.accent} style={styles.highlight}>{musicInfo.name}</Text>{' '}
      {t('list_add_title_last')}
    </Text>
  )
}

const styles = createStyle({
  title: {
    textAlign: 'center',
    fontWeight: fontWeight.semibold,
    paddingHorizontal: 20,
    paddingTop: 2,
    paddingBottom: 14,
    lineHeight: 22,
  },
  highlight: {
    fontWeight: fontWeight.bold,
  },
})
