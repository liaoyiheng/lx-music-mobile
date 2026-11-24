import Text from '@/components/common/Text'
import { createStyle } from '@/utils/tools'
import { useApple, fontWeight } from '@/theme/apple'
import { useI18n } from '@/lang'

export default ({ selectedList, isMove }: {
  selectedList: LX.Music.MusicInfo[]
  isMove: boolean
}) => {
  const apple = useApple()
  const t = useI18n()
  return (
    <Text style={styles.title} size={16} color={apple.label} numberOfLines={2}>
      {t(isMove ? 'list_multi_add_title_first_move' : 'list_multi_add_title_first_add')}{' '}
      <Text color={apple.accent} size={16} style={styles.highlight}>{selectedList.length}</Text>{' '}
      {t('list_multi_add_title_last')}
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
